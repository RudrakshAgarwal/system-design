package com.airlinemanagementsystem.flight.controller;

import com.airlinemanagementsystem.flight.dto.SeatResponseDTO;
import com.airlinemanagementsystem.flight.service.SeatLockService;
import com.airlinemanagementsystem.flight.service.SeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seats")
@RequiredArgsConstructor
@Tag(name = "Seat Management", description = "Internal APIs for locking, unlocking, and confirming seats during the Booking Saga")
public class SeatController {

    private final SeatService seatService;
    private final SeatLockService seatLockService;

    @Operation(summary = "Get all seats for a flight", description = "Retrieves the current seat map and availability status")
    @GetMapping("/{flightId}")
    public ResponseEntity<List<SeatResponseDTO>> getSeats(
            @Parameter(description = "ID of the flight", example = "100")
            @PathVariable Long flightId) {
        return ResponseEntity.ok(seatService.getSeatsByFlight(flightId));
    }

    @Operation(
            summary = "Acquire a Seat Lock (Redis)",
            description = "Attempts to place a 10-minute TTL lock on a specific seat for a specific user to prevent double-booking.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lock acquired successfully"),
                    @ApiResponse(responseCode = "409", description = "Seat is already locked by another user")
            }
    )
    @PostMapping("/lock")
    public ResponseEntity<Boolean> lockSeat(
            @Parameter(description = "ID of the flight", example = "100") @RequestParam Long flightId,
            @Parameter(description = "Seat Number (e.g., 1A)", example = "1A") @RequestParam String seatNumber,
            @Parameter(description = "ID of the user attempting to lock", example = "user-123") @RequestParam String userId) {

        boolean locked = seatLockService.acquireSeatLock(flightId, seatNumber, userId);
        return ResponseEntity.ok(locked);
    }

    @Operation(
            summary = "Confirm Seat Booking",
            description = "Finalizes the booking in MySQL. Usually called by the Saga Orchestrator after payment success."
    )
    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmSeat(
            @Parameter(description = "ID of the flight", example = "100") @RequestParam Long flightId,
            @Parameter(description = "Seat Number", example = "1A") @RequestParam String seatNumber,
            @Parameter(description = "ID of the user", example = "user-123") @RequestParam String userId) { // <-- ADDED THIS

        seatService.confirmSeatBooking(flightId, seatNumber, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Unlock a seat (Idempotent)",
            description = "Safely removes the Redis lock. Only the user who acquired the lock can release it. Called if Payment Fails or user abandons cart."
    )
    @PostMapping("/unlock")
    public ResponseEntity<String> unlockSeat(
            @Parameter(description = "ID of the flight", example = "100") @RequestParam Long flightId,
            @Parameter(description = "Seat Number (e.g., 1A)", example = "1A") @RequestParam String seatNumber,
            @Parameter(description = "ID of the user who owns the lock", example = "user-123") @RequestParam String userId) {

        seatLockService.releaseSeatLock(flightId, seatNumber, userId);
        return ResponseEntity.ok("Seat unlocked explicitly (or already expired).");
    }
}