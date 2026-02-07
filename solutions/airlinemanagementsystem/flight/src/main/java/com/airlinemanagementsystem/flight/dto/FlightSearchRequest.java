package com.airlinemanagementsystem.flight.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlightSearchRequest {
    private String source;
    private String destination;
    private LocalDate date;

    private Double maxPrice;
    private String airlineName;
    private Integer minAvailableSeats;

    private int page = 0;
    private int size = 10;
    private String sortBy = "departureTime";
    private String sortDir = "asc";
}
