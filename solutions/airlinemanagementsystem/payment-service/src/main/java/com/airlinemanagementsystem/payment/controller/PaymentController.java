package com.airlinemanagementsystem.payment.controller;

import com.airlinemanagementsystem.payment.dto.PaymentInitRequest;
import com.airlinemanagementsystem.payment.dto.PaymentVerificationRequest;
import com.airlinemanagementsystem.payment.entity.IdempotencyRecord;
import com.airlinemanagementsystem.payment.entity.Payment;
import com.airlinemanagementsystem.payment.service.IdempotencyService;
import com.airlinemanagementsystem.payment.service.PaymentProducer;
import com.airlinemanagementsystem.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "Endpoints for initiating payments, verifying transactions, and handling refunds.")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentProducer paymentProducer;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    @Operation(summary = "Initiate a Payment (Idempotent)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment initiated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Payment.class))),
            @ApiResponse(responseCode = "409", description = "Request already processing", content = @Content)
    })
    @PostMapping("/initiate")
    public ResponseEntity<?> initiatePayment(
            @RequestHeader(value = "Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody PaymentInitRequest request) throws Exception {

        IdempotencyRecord existingRecord = idempotencyService.checkAndLock(idempotencyKey);

        if (existingRecord != null) {
            if (existingRecord.isProcessing()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Request is currently processing. Please wait.");
            }

            objectMapper.registerModule(new JavaTimeModule());
            Payment cachedPayment = objectMapper.readValue(existingRecord.getResponsePayload(), Payment.class);
            return ResponseEntity.ok(cachedPayment);
        }

        Payment payment = paymentService.initiatePayment(request);

        idempotencyService.cacheResponse(idempotencyKey, payment);

        return ResponseEntity.ok(payment);
    }

    @Operation(summary = "Verify Payment (Async)")
    @PostMapping("/verify")
    public ResponseEntity<String> verifyPayment(@RequestBody PaymentVerificationRequest request) {
        paymentProducer.queuePaymentVerification(request);
        return ResponseEntity.accepted().body("Payment verification queued.");
    }
}