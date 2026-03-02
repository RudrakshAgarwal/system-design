package com.airlinemanagementsystem.flight.integration;

import com.airlinemanagementsystem.flight.entity.Flight;

import java.time.LocalDate;
import java.util.List;

public interface ExternalAirlineAdapter {
    String getProviderName();
    List<Flight> searchFlights(String source, String destination, LocalDate date);
}
