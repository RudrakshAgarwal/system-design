package com.airlinemanagementsystem.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequest {
    private Long flightId;
    private String userId;
    private List<PassengerDTO> passengers;
}