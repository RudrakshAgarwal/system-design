package com.airlinemanagementsystem.flight.controller;

import com.airlinemanagementsystem.flight.dto.SeatResponseDTO;
import com.airlinemanagementsystem.flight.service.SeatLockService;
import com.airlinemanagementsystem.flight.service.SeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seats")
@RequiredArgsConstructor
@Tag(name = "Seat Management", description = "Internal APIs for Booking Service to lock/confirm seats")
public class SeatController {
    private final SeatService seatService;
    private final SeatLockService seatLockService;

    @Operation(summary = "Get seat map for a flight")
    @GetMapping("/{flightId}")
    public ResponseEntity<List<SeatResponseDTO>> getSeats(@PathVariable Long flightId) {
        return ResponseEntity.ok(seatService.getSeatsByFlight(flightId));
    }

    @PostMapping("/lock")
    public ResponseEntity<Boolean> lockSeat(
            @RequestParam Long flightId,
            @RequestParam String seatNumber,
            @RequestParam String userId) {
        return ResponseEntity.ok(seatLockService.acquireSeatLock(flightId, seatNumber, userId));
    }

    @Operation(summary = "Unlock a seat", description = "Called by Booking Service if Payment Fails")
    @PostMapping("/unlock")
    public ResponseEntity<String> unlockSeat(
            @RequestParam Long flightId,
            @RequestParam String seatNumber) {
        seatLockService.releaseSeatLock(flightId, seatNumber);
        return ResponseEntity.ok("Seat unlocked successfully");
    }

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmSeat(
            @RequestParam Long flightId,
            @RequestParam String seatNumber) {
        seatService.confirmSeatBooking(flightId, seatNumber);
        return ResponseEntity.ok().build();
    }
}
