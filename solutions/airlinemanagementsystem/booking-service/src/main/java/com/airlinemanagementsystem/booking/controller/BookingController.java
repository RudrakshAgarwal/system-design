package com.airlinemanagementsystem.booking.controller;

import com.airlinemanagementsystem.booking.dto.BookingRequest;
import com.airlinemanagementsystem.booking.entity.Booking;
import com.airlinemanagementsystem.booking.service.BookingProducer;
import com.airlinemanagementsystem.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking Controller", description = "High-scale async booking")
public class BookingController {

    private final BookingProducer bookingProducer;
    private final BookingService bookingService;

    @Operation(summary = "Submit a booking request", description = "Queues the request for processing. Returns immediately.")
    @PostMapping
    public ResponseEntity<String> createBooking(@RequestBody BookingRequest request, @AuthenticationPrincipal Jwt jwt) {
        if (request.getPassengers() == null || request.getPassengers().isEmpty()) {
            return ResponseEntity.badRequest().body("Passenger list cannot be empty.");
        }

        String secureUserId = jwt.getSubject();

        request.setUserId(secureUserId);

        log.info("Received booking request for User ID: {}", secureUserId);

        bookingProducer.queueBookingRequest(request);
        return ResponseEntity.accepted()
                .body("Booking request received! We are processing it and will notify you shortly.");
    }

    @Operation(summary = "Check booking status", description = "Frontend polls this to update the UI")
    @GetMapping("/status")
    public ResponseEntity<?> checkLatestBookingStatus(@AuthenticationPrincipal Jwt jwt) {
        String secureUserId = jwt.getSubject();
        Booking latest = bookingService.getLatestBooking(secureUserId);

        if (latest == null) {
            return ResponseEntity.ok("NO_BOOKING_FOUND");
        }

        return ResponseEntity.ok(latest.getStatus());
    }
}