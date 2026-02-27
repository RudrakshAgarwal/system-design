package com.airlinemanagementsystem.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingPlacedEvent {
    private String bookingId;
    private String userEmail;
    private String firstName;
    private String lastName;
    private String flightNumber; // You might need to fetch this or store it in Booking
    private String departureTime;
    private String origin;
    private String destination;
}
