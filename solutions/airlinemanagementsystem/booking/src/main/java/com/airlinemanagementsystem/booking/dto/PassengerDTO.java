package com.airlinemanagementsystem.booking.dto;

import lombok.Data;

import java.util.List;

@Data
public class PassengerDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String seatNumber;

    private List<LuggageDTO> luggage;
}
