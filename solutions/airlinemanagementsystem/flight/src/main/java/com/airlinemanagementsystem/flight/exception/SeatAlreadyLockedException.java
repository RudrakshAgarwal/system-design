package com.airlinemanagementsystem.flight.exception;

// Thrown when Redis lock fails
public class SeatAlreadyLockedException extends BusinessException {
    public SeatAlreadyLockedException(String seatNumber) {
        super("Seat " + seatNumber + " is currently held by another user. Please try again in 10 minutes.");
    }
}
