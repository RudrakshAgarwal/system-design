package com.airlinemanagementsystem.payment.listener;

import com.airlinemanagementsystem.payment.dto.PaymentRequestDto;
import com.airlinemanagementsystem.payment.entity.Payment;
import com.airlinemanagementsystem.payment.entity.PaymentStatus;
import com.airlinemanagementsystem.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRequestConsumer {

    private final PaymentRepository paymentRepository;

    /**
     * Listens to Booking Service.
     * PURPOSE: Securely store the Booking Amount so the Frontend can't tamper with it.
     */
    @KafkaListener(topics = "payment-request-topic", groupId = "payment-service-group")
    public void onPaymentRequest(PaymentRequestDto request) {
        log.info("Kafka: Received Payment Request for Booking: {} | Amount: {}", request.getBookingId(), request.getAmount());

        // Check idempotency (in case Kafka sends duplicate)
        if (paymentRepository.existsByBookingIdAndStatus(request.getBookingId(), PaymentStatus.INITIATED)) {
            return;
        }

        Payment payment = Payment.builder()
                .bookingId(request.getBookingId())
                .amount(request.getAmount()) // SECURE AMOUNT FROM BACKEND
                .userId(request.getUserId())
                .status(PaymentStatus.PENDING_INITIATION) // Waiting for user to click "Pay"
                .createdAt(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);
    }
}