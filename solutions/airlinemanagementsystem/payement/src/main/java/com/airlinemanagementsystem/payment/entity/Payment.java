package com.airlinemanagementsystem.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payments", indexes = {
        @Index(name = "idx_booking", columnList = "bookingId"),
        @Index(name = "idx_order", columnList = "razorpayOrderId")
})
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long bookingId;

    @Column(nullable = false)
    private String userId;

    private Double amount;

    @Column(unique = true)
    private String razorpayOrderId;

    private String razorpayPaymentId;
    private String razorpaySignature;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
