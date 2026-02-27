package com.airlinemanagementsystem.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequestDto {
    private Long bookingId;
    private String userId;
    private Double amount;
    private String currency; // Defaults to USD usually
}
