package com.airlinemanagementsystem.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PassengerDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String seatNumber;

    private List<LuggageDTO> luggage;
}
