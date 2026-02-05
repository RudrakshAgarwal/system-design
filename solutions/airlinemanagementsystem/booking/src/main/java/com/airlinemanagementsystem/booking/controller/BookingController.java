package com.airlinemanagementsystem.booking.controller;

import com.airlinemanagementsystem.booking.dto.BookingRequest;
import com.airlinemanagementsystem.booking.entity.Booking;
import com.airlinemanagementsystem.booking.repository.BookingRepository;
import com.airlinemanagementsystem.booking.service.BookingProducer;
import com.airlinemanagementsystem.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking Controller", description = "High-scale async booking")
public class BookingController {

    private final BookingProducer bookingProducer;
    private final BookingService bookingService;

    @Operation(summary = "Submit a booking request", description = "Queues the request for processing. Returns immediately.")
    @PostMapping
    public ResponseEntity<String> createBooking(@RequestBody BookingRequest request) {
        if (request.getPassengers() == null || request.getPassengers().isEmpty()) {
            return ResponseEntity.badRequest().body("Passenger list cannot be empty.");
        }
        bookingProducer.queueBookingRequest(request);
        return ResponseEntity.accepted()
                .body("Booking request received! We are processing it and will notify you shortly.");
    }

    @Operation(summary = "Check booking status", description = "Frontend polls this to update the UI")
    @GetMapping("/status/{userId}")
    public ResponseEntity<?> checkLatestBookingStatus(@PathVariable String userId) {
        Booking latest = bookingService.getLatestBooking(userId);

        if (latest == null) {
            return ResponseEntity.ok("NO_BOOKING_FOUND");
        }

        return ResponseEntity.ok(latest.getStatus());
    }
}