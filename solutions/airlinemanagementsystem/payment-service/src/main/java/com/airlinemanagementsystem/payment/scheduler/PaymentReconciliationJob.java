package com.airlinemanagementsystem.payment.scheduler;

import com.airlinemanagementsystem.payment.entity.Payment;
import com.airlinemanagementsystem.payment.entity.PaymentStatus;
import com.airlinemanagementsystem.payment.repository.PaymentRepository;
import com.airlinemanagementsystem.payment.service.EmailService;
import com.airlinemanagementsystem.payment.service.PaymentProducer;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentReconciliationJob {

    private final PaymentRepository paymentRepository;
    private final RazorpayClient razorpayClient;
    private final PaymentProducer paymentProducer;
    private final EmailService emailService;

    // Runs every 15 minutes
    @Scheduled(cron = "0 */15 * * * *")
    @Transactional
    public void reconcilePendingPayments() {
        log.info("Starting Payment Reconciliation Job...");

        // Find payments stuck in INITIATED state for more than 15 minutes
        LocalDateTime cutOffTime = LocalDateTime.now().minusMinutes(15);
        List<Payment> pendingPayments = paymentRepository.findAllByStatusAndCreatedAtBefore(
                PaymentStatus.INITIATED, cutOffTime);

        if (pendingPayments.isEmpty()) {
            log.info("No stuck payments found.");
            return;
        }

        for (Payment payment : pendingPayments) {
            try {
                // Check status directly with Razorpay
                Order razorpayOrder = razorpayClient.orders.fetch(payment.getRazorpayOrderId());
                String status = razorpayOrder.get("status"); // "paid", "created", "attempted"

                log.info("Reconciling Order {}: Status is '{}'", payment.getRazorpayOrderId(), status);

                if ("paid".equals(status)) {
                    // Scenario: User paid, but our API missed the callback
                    payment.setStatus(PaymentStatus.SUCCESS);
                    payment.setUpdatedAt(LocalDateTime.now());
                    payment.setRazorpayPaymentId("RECONCILED_" + System.currentTimeMillis());

                    paymentRepository.save(payment);

                    // Notify Booking Service & User
                    paymentProducer.notifyBookingService(payment.getBookingId(), "SUCCESS", payment.getRazorpayPaymentId());
                    emailService.sendPaymentStatusEmail(payment.getEmail(), "SUCCESS", payment.getBookingId(), String.valueOf(payment.getAmount()));

                } else if ("attempted".equals(status)) {
                    // Scenario: User tried but failed/cancelled -> Mark Failed
                    payment.setStatus(PaymentStatus.FAILED);
                    payment.setUpdatedAt(LocalDateTime.now());
                    paymentRepository.save(payment);
                }
                // If "created", user likely closed the window immediately. We can leave it or expire it.

            } catch (Exception e) {
                log.error("Error reconciling payment ID: {}", payment.getId(), e);
            }
        }
    }
}