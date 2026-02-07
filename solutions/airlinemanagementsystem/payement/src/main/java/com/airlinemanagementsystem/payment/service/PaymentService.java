package com.airlinemanagementsystem.payment.service;

import com.airlinemanagementsystem.payment.dto.PaymentInitRequest;
import com.airlinemanagementsystem.payment.entity.Payment;
import com.airlinemanagementsystem.payment.entity.PaymentStatus;
import com.airlinemanagementsystem.payment.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Refund;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final RazorpayClient razorpayClient;
    private final PaymentRepository paymentRepository;

    /**
     * Step 1: Create Order (Synchronous)
     * Called by the Controller when user clicks "Pay".
     */
    @Transactional
    public Payment initiatePayment(PaymentInitRequest request) throws Exception {
        log.info("Initiating payment request for Booking ID: {}", request.getBookingId());

        Payment payment = paymentRepository.findByBookingIdAndStatus(request.getBookingId(), PaymentStatus.PENDING_INITIATION)
                .orElseThrow(() -> new RuntimeException("Payment Link Expired or Invalid Booking ID. Please retry booking."));

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            log.warn("Duplicate payment attempt for Booking ID: {}", request.getBookingId());
            throw new RuntimeException("Booking is already paid! Please check your status.");
        }

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", (long)(payment.getAmount() * 100));
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "txn_" + request.getBookingId());
        orderRequest.put("payment_capture", 1);

        Order razorpayOrder = razorpayClient.orders.create(orderRequest);
        String orderId = razorpayOrder.get("id");

        payment.setRazorpayOrderId(orderId);
        payment.setStatus(PaymentStatus.INITIATED);
        payment.setEmail(request.getEmail());
        payment.setUpdatedAt(LocalDateTime.now());

        return paymentRepository.save(payment);
    }

    /**
     * SAGA COMPENSATION: Automatic Refund
     * Called by RefundListener when Booking Service fails to confirm the seat.
     */
    @Transactional
    public void processRefund(Long bookingId) {
        log.info("Initiating Refund Logic for Booking ID: {}", bookingId);

        Payment payment = paymentRepository.findByBookingIdAndStatus(bookingId, PaymentStatus.SUCCESS)
                .orElseThrow(() -> new RuntimeException("Refund Failed: No successful payment found for Booking ID " + bookingId));

        try {
            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", (long)(payment.getAmount() * 100)); // Full Refund
            refundRequest.put("speed", "normal");

            Refund refund = razorpayClient.payments.refund(payment.getRazorpayPaymentId(), refundRequest);

            log.info("Razorpay Refund Successful. Refund ID: {}", refund.get("id").toString());

            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

        } catch (RazorpayException e) {
            log.error("Razorpay API Error during refund for Booking {}: {}", bookingId, e.getMessage());
            throw new RuntimeException("Razorpay Refund Failed", e);
        } catch (Exception e) {
            log.error("System Error during refund for Booking {}: {}", bookingId, e.getMessage());
            throw new RuntimeException("System Error during Refund", e);
        }
    }
}