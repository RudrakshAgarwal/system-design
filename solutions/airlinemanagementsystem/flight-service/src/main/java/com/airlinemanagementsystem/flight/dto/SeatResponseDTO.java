package com.airlinemanagementsystem.flight.dto;

import com.airlinemanagementsystem.flight.entity.SeatStatus;
import com.airlinemanagementsystem.flight.entity.SeatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatResponseDTO {
    private Long seatId;
    private String seatNumber;
    private SeatType seatType;
    private SeatStatus status;
    private Double price;
}
