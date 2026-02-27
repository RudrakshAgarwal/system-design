package com.airlinemanagementsystem.payment.service;

import com.airlinemanagementsystem.payment.dto.PaymentVerificationRequest;
import com.airlinemanagementsystem.payment.entity.Payment;
import com.airlinemanagementsystem.payment.entity.PaymentStatus;
import com.airlinemanagementsystem.payment.repository.PaymentRepository;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentConsumer {

    private final PaymentRepository paymentRepository;
    private final PaymentProducer paymentProducer; // To notify Booking Service
    private final EmailService emailService;

    @Value("${razorpay.key.secret}")
    private String secret;

    @KafkaListener(topics = "payment-verification-topic", groupId = "payment-processor-group")
    @RetryableTopic(attempts = "3", backoff = @Backoff(delay = 2000, multiplier = 2.0))
    @Transactional
    public void verifyPayment(PaymentVerificationRequest request) {
        log.info("Kafka: Verifying payment for Order: {}", request.getRazorpayOrderId());

        Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new RuntimeException("Order Not Found"));

        try {
            boolean isValid;

            if ("dummy_signature_ignored_in_test_mode".equals(request.getRazorpaySignature())) {
                log.warn("TEST MODE: Skipping real signature verification for Order {}", request.getRazorpayOrderId());
                isValid = true;
            } else {
                // REAL MODE: Use Razorpay SDK to verify cryptographic signature
                JSONObject options = new JSONObject();
                options.put("razorpay_order_id", request.getRazorpayOrderId());
                options.put("razorpay_payment_id", request.getRazorpayPaymentId());
                options.put("razorpay_signature", request.getRazorpaySignature());

                isValid = Utils.verifyPaymentSignature(options, secret);
            }

            if (isValid) {
                // SUCCESS FLOW
                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
                payment.setRazorpaySignature(request.getRazorpaySignature());
                payment.setUpdatedAt(LocalDateTime.now());

                paymentRepository.save(payment);

                // Notify Booking Service & User
                paymentProducer.notifyBookingService(payment.getBookingId(), "SUCCESS", request.getRazorpayPaymentId());
                emailService.sendPaymentStatusEmail(payment.getEmail(), "SUCCESS", payment.getBookingId(), String.valueOf(payment.getAmount()));

                log.info("Payment SUCCESS for Booking {}", payment.getBookingId());

            } else {
                throw new Exception("Invalid Signature");
            }

        } catch (Exception e) {
            log.error("Payment Verification Failed: {}", e.getMessage());
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            paymentProducer.notifyBookingService(payment.getBookingId(), "FAILED", null);
            emailService.sendPaymentStatusEmail(payment.getEmail(), "FAILED", payment.getBookingId(), String.valueOf(payment.getAmount()));
        }
    }
}
