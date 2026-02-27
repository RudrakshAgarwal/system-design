package com.airlinemanagementsystem.booking.service;

import com.airlinemanagementsystem.booking.config.BookingKafkaConfig;
import com.airlinemanagementsystem.booking.dto.BookingRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void queueBookingRequest(BookingRequest request) {
        log.info("Queueing booking request for User: {}", request.getUserId());

        // Use UserID as the key to ensure requests from the same user go to the same partition
        kafkaTemplate.send(BookingKafkaConfig.BOOKING_REQ_TOPIC, request.getUserId(), request);
    }
}