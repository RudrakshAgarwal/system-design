package com.airlinemanagementsystem.notification.controller;

import com.airlinemanagementsystem.notification.dto.BookingPlacedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @GetMapping("/test-email")
    public String triggerTestEmail(@RequestParam String email) {

        // 1. Create a Fake Booking Event
        BookingPlacedEvent event = new BookingPlacedEvent(
                "TEST-BKG-123",
                email, // Sends to the email you provide in the URL
                "Test User",
                "Doe",
                "AI-999",
                "2026-03-01 10:00 AM",
                "DEL",
                "JFK"
        );

        // 2. Publish to Kafka (This mimics the Booking Service)
        // Note: The topic name MUST match what your Consumer is listening to
        kafkaTemplate.send("booking-events", event);

        return "✅ Test Message Sent to Kafka! Check your Notification Service logs and your Inbox.";
    }
}
