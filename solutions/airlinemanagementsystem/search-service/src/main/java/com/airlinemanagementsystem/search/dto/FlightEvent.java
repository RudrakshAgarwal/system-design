package com.airlinemanagementsystem.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightEvent {
    private Long flightId;
    private String flightNumber;
    private String airline;
    private String sourceAirport;
    private String destinationAirport;
    private Instant departureTime;
    private Instant arrivalTime;
    private Double basePrice;
    private String status;
    private String eventType;
    private Instant eventTimestamp;
}
