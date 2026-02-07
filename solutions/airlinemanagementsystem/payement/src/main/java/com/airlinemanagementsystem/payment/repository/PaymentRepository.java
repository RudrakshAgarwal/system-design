package com.airlinemanagementsystem.payment.repository;

import com.airlinemanagementsystem.payment.entity.Payment;
import com.airlinemanagementsystem.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Idempotency Check: Prevents duplicate payments for the same booking.
     */
    boolean existsByBookingIdAndStatus(Long bookingId, PaymentStatus status);

    /**
     * Used for verification lookup.
     */
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    /**
     * Used by the Reconciliation Job to find "Ghost Payments".
     * Finds all payments that are still INITIATED and created before a certain time.
     */
    List<Payment> findAllByStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime dateTime);

    // Finds the successful payment record so we can process a refund
    Optional<Payment> findByBookingIdAndStatus(Long bookingId, PaymentStatus status);
}
