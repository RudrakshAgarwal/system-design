package com.airlinemanagementsystem.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentEvent {
    private Long bookingId;
    private String status;
    private String transactionId;
}
