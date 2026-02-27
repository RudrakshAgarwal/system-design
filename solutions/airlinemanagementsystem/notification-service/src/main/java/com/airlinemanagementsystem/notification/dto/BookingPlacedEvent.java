package com.airlinemanagementsystem.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingPlacedEvent {
    private String bookingId;
    private String userEmail;
    private String firstName;
    private String lastName;
    private String flightNumber;
    private String departureTime;
    private String origin;
    private String destination;
}
