package com.airlinemanagementsystem.payment.controller;

import com.airlinemanagementsystem.payment.dto.PaymentInitRequest;
import com.airlinemanagementsystem.payment.dto.PaymentVerificationRequest;
import com.airlinemanagementsystem.payment.entity.Payment;
import com.airlinemanagementsystem.payment.service.PaymentProducer;
import com.airlinemanagementsystem.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "Endpoints for initiating payments, verifying transactions, and handling refunds.")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentProducer paymentProducer;

    @Operation(
            summary = "Initiate a Payment",
            description = "Creates a Razorpay Order and saves the initial payment state in the database. Returns the Razorpay Order ID needed for the frontend checkout."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment initiated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Payment.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input (e.g., negative amount or missing email)", content = @Content),
            @ApiResponse(responseCode = "409", description = "Booking is already paid for (Idempotency check)", content = @Content)
    })
    @PostMapping("/initiate")
    public ResponseEntity<Payment> initiatePayment(
            @Valid @RequestBody PaymentInitRequest request) throws Exception {
        return ResponseEntity.ok(paymentService.initiatePayment(request));
    }

    @Operation(
            summary = "Verify Payment (Async)",
            description = "Accepts the Razorpay payment details (ID & Signature) and pushes them to a Kafka Queue for asynchronous verification. Returns 202 Accepted immediately."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Verification request accepted and queued", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @PostMapping("/verify")
    public ResponseEntity<String> verifyPayment(
            @RequestBody PaymentVerificationRequest request) {
        paymentProducer.queuePaymentVerification(request);
        return ResponseEntity.accepted().body("Payment verification queued.");
    }
}