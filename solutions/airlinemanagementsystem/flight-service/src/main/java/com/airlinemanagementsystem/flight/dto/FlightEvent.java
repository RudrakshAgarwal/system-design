package com.airlinemanagementsystem.flight.dto;

import com.airlinemanagementsystem.flight.entity.FlightStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightEvent {
    private Long flightId;
    private String flightNumber;
    private String airline;
    private String sourceAirport;
    private String destinationAirport;
    private Instant arrivalTime;
    private Instant departureTime;
    private Double basePrice;
    private FlightStatus status;
    private String eventType;

    @Builder.Default
    private Instant eventTimestamp = Instant.now();
}
