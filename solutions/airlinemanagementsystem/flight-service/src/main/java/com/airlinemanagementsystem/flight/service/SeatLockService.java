package com.airlinemanagementsystem.flight.service;

import com.airlinemanagementsystem.flight.exception.SeatAlreadyLockedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatLockService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String LOCK_PREFIX = "LOCK::FLIGHT_SEAT::";
    private static final long LOCK_DURATION_MINUTES = 10;

    @CircuitBreaker(name = "redisLock", fallbackMethod = "fallbackLock")
    public boolean acquireSeatLock(Long flightId, String seatNumber, String userId) {
        String lockKey = generateLockKey(flightId, seatNumber);

        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, userId, Duration.ofMinutes(LOCK_DURATION_MINUTES));

        if (Boolean.TRUE.equals(success)) {
            log.info("Lock acquired: Flight {} Seat {} by User {}", flightId, seatNumber, userId);
            return true;
        } else {
            Object currentOwner = redisTemplate.opsForValue().get(lockKey);
            log.warn("Seat {} already locked by user: {}", seatNumber, currentOwner);
            throw new SeatAlreadyLockedException(seatNumber); // Business exception
        }
    }

    public boolean fallbackLock(Long flightId, String seatNumber, String userId, Throwable t) {
        log.error("Redis Down! Cannot acquire lock for Seat {}. Error: {}", seatNumber, t.getMessage());
        throw new RuntimeException("Seat reservation temporarily unavailable. Please try again.");
    }

    @CircuitBreaker(name = "redisLock", fallbackMethod = "fallbackIsLocked")
    public boolean isSeatLocked(Long flightId, String seatNumber) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(generateLockKey(flightId, seatNumber)));
    }

    public boolean fallbackIsLocked(Long flightId, String seatNumber, Throwable t) {
        log.warn("Redis Down! Assuming Seat {} is NOT locked to allow viewing.", seatNumber);
        return false;
    }

    public void releaseSeatLock(Long flightId, String seatNumber, String userId) {
        String lockKey = generateLockKey(flightId, seatNumber);

        Object currentOwner = redisTemplate.opsForValue().get(lockKey);

        if (currentOwner == null) {
            log.info("Lock already expired or released idempotently for Flight {} Seat {}", flightId, seatNumber);
            return;
        }

        if (userId.equals(currentOwner.toString())) {
            redisTemplate.delete(lockKey);
            log.info("Lock safely released: Flight {} Seat {} by User {}", flightId, seatNumber, userId);
        } else {
            log.warn("SECURITY: User {} attempted to release lock owned by {}! Flight {} Seat {}",
                    userId, currentOwner, flightId, seatNumber);
        }
    }

    private String generateLockKey(Long flightId, String seatNumber) {
        return LOCK_PREFIX + flightId + "::" + seatNumber;
    }
}