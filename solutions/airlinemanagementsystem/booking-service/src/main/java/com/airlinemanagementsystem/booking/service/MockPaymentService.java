package com.airlinemanagementsystem.booking.service;

import com.airlinemanagementsystem.booking.dto.PaymentEventDto;
import com.airlinemanagementsystem.booking.dto.PaymentRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MockPaymentService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "payment-request-topic", groupId = "mock-payment-group")
    public void processPayment(PaymentRequestDto request) {
        log.info("💳 MOCK PAYMENT: Processing for Booking ID: {}", request.getBookingId());

        try { Thread.sleep(2000); } catch (InterruptedException e) {}

        PaymentEventDto response = new PaymentEventDto(
                request.getBookingId(),
                "SUCCESS",
                UUID.randomUUID().toString()
        );

        kafkaTemplate.send("payment-events", response);
        log.info("✅ MOCK PAYMENT: Success event sent.");
    }
}