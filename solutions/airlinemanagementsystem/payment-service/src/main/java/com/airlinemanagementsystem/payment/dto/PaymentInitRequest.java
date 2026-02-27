package com.airlinemanagementsystem.payment.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentInitRequest {
    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @Min(value = 1, message = "Amount must be greater than 0")
    @NotNull(message = "Amount is required")
    private Double amount;

    @Email(message = "Invalid email format")
    @NotNull(message = "Email is required")
    private String email;
}
