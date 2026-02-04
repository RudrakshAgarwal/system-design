package com.airlinemanagementsystem.flight.dto;

import com.airlinemanagementsystem.flight.entity.SeatStatus;
import com.airlinemanagementsystem.flight.entity.SeatType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SeatResponseDTO {
    private Long seatId;
    private String seatNumber;
    private SeatType seatType;
    private SeatStatus status;
    private Double priceAdjustment; // e.g., +$50 for Exit Row
}
