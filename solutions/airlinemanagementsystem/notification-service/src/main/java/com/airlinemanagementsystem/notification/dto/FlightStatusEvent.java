package com.airlinemanagementsystem.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightStatusEvent {
    private String flightNumber;
    private String oldStatus;
    private String newStatus;
}
