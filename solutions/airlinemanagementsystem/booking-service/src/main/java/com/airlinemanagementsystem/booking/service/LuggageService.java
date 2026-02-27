package com.airlinemanagementsystem.booking.service;

import com.airlinemanagementsystem.booking.entity.Luggage;
import com.airlinemanagementsystem.booking.entity.LuggageHistory;
import com.airlinemanagementsystem.booking.entity.LuggageStatus;
import com.airlinemanagementsystem.booking.repository.LuggageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class LuggageService {

    private final LuggageRepository luggageRepository;

    /**
     * Core Business Logic: Updates status and records the audit trail.
     */
    @Transactional
    public void scanLuggage(String tagNumber, String location, LuggageStatus newStatus, String scannedBy) {
        log.info("Scanning Bag {} at {} -> {}", tagNumber, location, newStatus);

        // 1. Fetch Bag (Fail if not found)
        Luggage bag = luggageRepository.findByTagNumber(tagNumber)
                .orElseThrow(() -> new RuntimeException("Luggage not found with Tag: " + tagNumber));

        // 2. Validation Logic (Optional but recommended)
        // Example: Prevent scanning a bag as "LOADED" if it hasn't been "CHECKED_IN"
         if (newStatus == LuggageStatus.LOADED && bag.getStatus() == LuggageStatus.BOOKED) {
             throw new RuntimeException("Security Violation: Bag must be checked-in before loading.");
         }

        // 3. Update Status
        bag.setStatus(newStatus);

        // 4. Create Audit History
        LuggageHistory history = LuggageHistory.builder()
                .luggage(bag)
                .location(location)
                .status(newStatus)
                .scannedBy(scannedBy)
                .scannedAt(LocalDateTime.now())
                .build();

        // 5. Link & Save (Cascading save handles history)
        bag.getHistory().add(history);
        luggageRepository.save(bag);
    }

    public Luggage getLuggageDetails(String tagNumber) {
        return luggageRepository.findByTagNumber(tagNumber)
                .orElseThrow(() -> new RuntimeException("Luggage not found"));
    }
}
