package com.airlinemanagementsystem.flight.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class FlightSearchRequest {
    private String source;
    private String destination;
    private LocalDate date;

    // Optional Filters
    private Double maxPrice;
    private String airlineName;
    private Integer minAvailableSeats;

    // Pagination & Sorting
    private int page = 0;
    private int size = 10;
    private String sortBy = "departureTime";
    private String sortDir = "asc";
}
