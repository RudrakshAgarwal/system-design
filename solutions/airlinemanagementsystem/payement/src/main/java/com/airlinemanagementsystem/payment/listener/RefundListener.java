package com.airlinemanagementsystem.payment.listener;

import com.airlinemanagementsystem.payment.entity.Payment;
import com.airlinemanagementsystem.payment.entity.PaymentStatus;
import com.airlinemanagementsystem.payment.repository.PaymentRepository;
import com.airlinemanagementsystem.payment.service.EmailService;
import com.airlinemanagementsystem.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefundListener {

    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final EmailService emailService;

    /**
     * Listens for bookings that failed AFTER payment was taken.
     * Triggers the "Compensating Transaction" (Refund).
     */
    @KafkaListener(topics = "booking-failure-topic", groupId = "payment-refund-group")
    public void handleRefundRequest(Long bookingId) {
        log.info("Kafka: Received Refund Request for Booking ID: {}", bookingId);

        try {
            // 1. Process Logic (Call Razorpay API)
            paymentService.processRefund(bookingId);

            // 2. Notify User (YES, WE MUST NOTIFY)
            // We fetch the payment to get the user's email
            Payment payment = paymentRepository.findByBookingIdAndStatus(bookingId, PaymentStatus.REFUNDED)
                    .orElseThrow(() -> new RuntimeException("Payment record not found after refund"));

            emailService.sendPaymentStatusEmail(
                    payment.getEmail(),
                    "REFUNDED",
                    bookingId,
                    String.valueOf(payment.getAmount())
            );

        } catch (Exception e) {
            log.error("Automatic Refund Failed for Booking {}. Manual Intervention Required.", bookingId, e);
        }
    }
}