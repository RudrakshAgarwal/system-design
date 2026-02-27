package com.airlinemanagementsystem.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentEventDto {
    private Long bookingId;
    private String status;
    private String transactionId;
}
