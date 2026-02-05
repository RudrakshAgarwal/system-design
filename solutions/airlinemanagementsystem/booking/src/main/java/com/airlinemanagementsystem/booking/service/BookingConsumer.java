package com.airlinemanagementsystem.booking.service;

import com.airlinemanagementsystem.booking.dto.BookingRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingConsumer {

    private final BookingService bookingService;
    private final EmailService emailService;

    @KafkaListener(topics = "booking-request-topic", groupId = "booking-processor-group")
    public void onBookingRequestReceived(BookingRequest request) {
        log.info("Kafka: Received booking request for User: {}", request.getUserId());
        String contactEmail = request.getPassengers().get(0).getEmail();
        String firstName = request.getPassengers().get(0).getFirstName();

        try {
            Long bookingId = bookingService.processBooking(request);

            log.info("Kafka: Booking processed successfully. ID: {}", bookingId);
            emailService.sendBookingSuccessEmail(contactEmail, firstName, bookingId);

        } catch (Exception e) {
            log.error("Kafka: Booking Failed. Reason: {}", e.getMessage());
            emailService.sendBookingFailureEmail(contactEmail, firstName, e.getMessage());
        }
    }
}