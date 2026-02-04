package com.airlinemanagementsystem.flight.entity;

public enum SeatStatus {
    AVAILABLE,   // Initial state
    LOCKED,      // Temporary state (only in Redis or during active transaction)
    BOOKED,      // Permanent state (in MySQL)
    BLOCKED,     // Maintenance or Operational reasons
    OCCUPIED     // Check-in complete
}
