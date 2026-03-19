package com.airlinemanagementsystem.payment.service;

import com.airlinemanagementsystem.payment.entity.IdempotencyRecord;
import com.airlinemanagementsystem.payment.repository.IdempotencyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyRepository idempotencyRepository;
    private final ObjectMapper objectMapper;

    /**
     * Attempts to acquire a lock for the given key.
     */
    public IdempotencyRecord checkAndLock(String idempotencyKey) {
        log.info("Idempotency Check: Verifying key [{}]", idempotencyKey);

        Optional<IdempotencyRecord> existingRecord = idempotencyRepository.findById(idempotencyKey);

        if (existingRecord.isPresent()) {
            IdempotencyRecord record = existingRecord.get();
            if (record.isProcessing()) {
                log.warn("Idempotency Hit: Key [{}] is currently PROCESSING by another thread. Blocking duplicate request.", idempotencyKey);
            } else {
                log.info("Idempotency Hit: Key [{}] already processed. Returning cached Razorpay response.", idempotencyKey);
            }
            return record;
        }

        try {
            log.debug("Idempotency Miss: Key [{}] is new. Attempting to insert and acquire lock...", idempotencyKey);
            IdempotencyRecord newRecord = IdempotencyRecord.builder()
                    .idempotencyKey(idempotencyKey)
                    .isProcessing(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            idempotencyRepository.saveAndFlush(newRecord);
            log.info("Idempotency Lock Acquired: Key [{}] is now locked for processing.", idempotencyKey);

            return null;

        } catch (DataIntegrityViolationException e) {
            log.warn("Idempotency Race Condition Caught! Another thread locked key [{}] literally milliseconds ago.", idempotencyKey);

            return idempotencyRepository.findById(idempotencyKey)
                    .orElseThrow(() -> {
                        log.error("Critical Error: Key [{}] threw constraint violation but cannot be found in DB!", idempotencyKey);
                        return new RuntimeException("Idempotency conflict error");
                    });
        }
    }

    /**
     * Caches the final successful response.
     */
    public void cacheResponse(String idempotencyKey, Object responseObj) {
        log.debug("Idempotency Cache: Attempting to serialize and cache response for key [{}]", idempotencyKey);

        try {
            objectMapper.registerModule(new JavaTimeModule());
            String jsonResponse = objectMapper.writeValueAsString(responseObj);

            IdempotencyRecord record = idempotencyRepository.findById(idempotencyKey)
                    .orElseThrow(() -> new RuntimeException("Record not found for caching"));

            record.setResponsePayload(jsonResponse);
            record.setProcessing(false);
            idempotencyRepository.save(record);

            log.info("Idempotency Cache Success: Response safely cached and lock released for key [{}]", idempotencyKey);

        } catch (Exception e) {
            log.error("Idempotency Cache Failed: Could not serialize or save response for key [{}]. Error: {}", idempotencyKey, e.getMessage(), e);
            throw new RuntimeException("Failed to serialize cached response", e);
        }
    }
}