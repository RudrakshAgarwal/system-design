package com.airlinemanagementsystem.flight.exception;

public class FlightNotFoundException extends BusinessException {
    public FlightNotFoundException(Long id) {
        super("Flight with ID " + id + " could not be found.");
    }
}
