package com.airlinemanagementsystem.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LuggageDTO {
    private String type;
    private double weight;
}
