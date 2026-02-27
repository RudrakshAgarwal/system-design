package com.airlinemanagementsystem.booking.controller;

import com.airlinemanagementsystem.booking.entity.Luggage;
import com.airlinemanagementsystem.booking.entity.LuggageHistory;
import com.airlinemanagementsystem.booking.entity.LuggageStatus;
import com.airlinemanagementsystem.booking.repository.LuggageRepository;
import com.airlinemanagementsystem.booking.service.LuggageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/luggage")
@RequiredArgsConstructor
@Tag(name = "Luggage Operations", description = "For Ground Staff & Tracking")
public class LuggageController {

    private final LuggageService luggageService; // Inject Service, NOT Repository

    @PostMapping("/{tagNumber}/scan")
    public ResponseEntity<String> scanLuggage(
            @PathVariable String tagNumber,
            @RequestParam String location,
            @RequestParam LuggageStatus status,
            @RequestParam(defaultValue = "STAFF_SYSTEM") String scannedBy) {

        // Delegate to Service
        luggageService.scanLuggage(tagNumber, location, status, scannedBy);

        return ResponseEntity.ok("Scan Successful: Bag " + tagNumber + " is now " + status);
    }

    @GetMapping("/{tagNumber}")
    public ResponseEntity<Luggage> trackLuggage(@PathVariable String tagNumber) {
        return ResponseEntity.ok(luggageService.getLuggageDetails(tagNumber));
    }
}
