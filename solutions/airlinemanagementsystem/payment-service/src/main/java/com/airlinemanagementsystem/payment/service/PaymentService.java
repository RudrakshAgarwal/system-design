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

    @Transactional
    public Payment initiatePayment(PaymentInitRequest request) throws Exception {
        log.info("Payment Initiation Started: Booking ID [{}] | Amount [{}]", request.getBookingId(), request.getAmount());

        Payment payment = paymentRepository.findByBookingIdAndStatus(request.getBookingId(), PaymentStatus.PENDING_INITIATION)
                .orElseThrow(() -> {
                    log.error("Payment Initiation Failed: No PENDING_INITIATION record found for Booking ID [{}]", request.getBookingId());
                    return new RuntimeException("Payment Link Expired or Invalid Booking ID. Please retry booking.");
                });

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            log.warn("Payment Initiation Rejected: Booking ID [{}] is already marked as SUCCESS.", request.getBookingId());
            throw new RuntimeException("Booking is already paid! Please check your status.");
        }

        try {
            log.debug("Contacting Razorpay API to create order for Booking ID [{}]...", request.getBookingId());
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", (long)(payment.getAmount() * 100)); // Convert to paise
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "txn_" + request.getBookingId());
            orderRequest.put("payment_capture", 1);

            Order razorpayOrder = razorpayClient.orders.create(orderRequest);
            String orderId = razorpayOrder.get("id");

            log.info("Razorpay Order Created Successfully: Order ID [{}] for Booking ID [{}]", orderId, request.getBookingId());

            payment.setRazorpayOrderId(orderId);
            payment.setStatus(PaymentStatus.INITIATED);
            payment.setEmail(request.getEmail());
            payment.setUpdatedAt(LocalDateTime.now());

            return paymentRepository.save(payment);

        } catch (RazorpayException e) {
            log.error("Razorpay API Error during order creation for Booking ID [{}]: {}", request.getBookingId(), e.getMessage(), e);
            throw new RuntimeException("Failed to communicate with payment gateway.");
        }
    }

    @Transactional
    public void processRefund(Long bookingId) {
        log.info("Refund Process Started: Booking ID [{}]", bookingId);

        Payment payment = paymentRepository.findByBookingIdAndStatus(bookingId, PaymentStatus.SUCCESS)
                .orElseThrow(() -> {
                    log.error("Refund Failed: Cannot find a SUCCESS payment record for Booking ID [{}]", bookingId);
                    return new RuntimeException("Refund Failed: No successful payment found for Booking ID " + bookingId);
                });

        try {
            log.debug("Contacting Razorpay API to initiate refund for Payment ID [{}]...", payment.getRazorpayPaymentId());
            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", (long)(payment.getAmount() * 100)); // Full Refund
            refundRequest.put("speed", "normal");

            Refund refund = razorpayClient.payments.refund(payment.getRazorpayPaymentId(), refundRequest);

            log.info("Razorpay Refund Successful: Refund ID [{}] generated for Booking ID [{}]", refund.get("id").toString(), bookingId);

            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

        } catch (RazorpayException e) {
            log.error("Razorpay API Error during refund for Booking ID [{}]: {}", bookingId, e.getMessage(), e);
            throw new RuntimeException("Razorpay Refund Failed", e);
        } catch (Exception e) {
            log.error("System Error during refund processing for Booking ID [{}]: {}", bookingId, e.getMessage(), e);
            throw new RuntimeException("System Error during Refund", e);
        }
    }
}