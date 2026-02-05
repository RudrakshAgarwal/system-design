package com.airlinemanagementsystem.booking.dto;

import lombok.Data;

import java.util.List;

@Data
public class BookingRequest {
    private Long flightId;
    private String userId;
    private List<PassengerDTO> passengers;
}