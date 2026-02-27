package com.airlinemanagementsystem.notification.consumer;

import com.airlinemanagementsystem.notification.dto.BookingPlacedEvent;
import com.airlinemanagementsystem.notification.dto.FlightStatusEvent;
import com.airlinemanagementsystem.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {
    private final EmailService emailService;

    /**
     * Listen for Booking Events (Topic: booking-events)
     */
    @KafkaListener(topics = "booking-events", groupId = "notification-group")
    public void handleBookingEvent(BookingPlacedEvent event) {
        log.info("📨 Kafka received Booking Event: {}", event.getBookingId());

        String subject = "Booking Confirmed: " + event.getBookingId();
        String body = String.format(
                "<h1>Flight Confirmed! ✈️</h1>" +
                        "<p>Dear %s,</p>" +
                        "<p>Your flight <b>%s</b> from %s to %s is confirmed.</p>" +
                        "<p><b>Departure:</b> %s</p>" +
                        "<p>Safe Travels!</p>",
                event.getFirstName(), event.getFlightNumber(), event.getOrigin(), event.getDestination(), event.getDepartureTime()
        );

        emailService.sendBookingEmail(event.getUserEmail(), subject, body);
    }

    /**
     * Listen for Flight Status Changes (Topic: flight-events)
     */
    @KafkaListener(topics = "flight-events", groupId = "notification-group")
    public void handleFlightEvent(FlightStatusEvent event) {
        log.info("🚨 Kafka received Flight Status Update: {}", event.getFlightNumber());

        // For simulation, we log it. In Phase 4, we query DB for passengers.
        log.info("⚠️ TODO: Notify passengers that Flight {} is now {}", event.getFlightNumber(), event.getNewStatus());
    }
}
