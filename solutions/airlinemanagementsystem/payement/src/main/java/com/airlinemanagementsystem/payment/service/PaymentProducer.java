package com.airlinemanagementsystem.payment.service;

import com.airlinemanagementsystem.payment.dto.PaymentEvent;
import com.airlinemanagementsystem.payment.dto.PaymentVerificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void queuePaymentVerification(PaymentVerificationRequest request) {
        log.info("Queueing verification for Order: {}", request.getRazorpayOrderId());
        kafkaTemplate.send("payment-verification-topic", request.getRazorpayOrderId(), request);
    }

    public void notifyBookingService(Long bookingId, String status, String txnId) {
        PaymentEvent event = new PaymentEvent(bookingId, status, txnId);
        kafkaTemplate.send("payment-events", String.valueOf(bookingId), event);
    }
}
