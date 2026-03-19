package com.airlinemanagementsystem.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "idempotency_records")
public class IdempotencyRecord {

    @Id
    @Column(nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    @Column(columnDefinition = "TEXT")
    private String responsePayload;

    @Column(nullable = false)
    private boolean isProcessing;

    private LocalDateTime createdAt;
}
