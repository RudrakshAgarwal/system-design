package com.airlinemanagementsystem.flight.service;

import com.airlinemanagementsystem.flight.entity.Airport;
import com.airlinemanagementsystem.flight.repository.AirportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AirportService {

    private final AirportRepository airportRepository;

    public Airport addAirport(Airport airport) {
        // Validate timezone before saving
        try {
            ZoneId.of(airport.getTimezoneId());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Timezone ID: " + airport.getTimezoneId());
        }
        return airportRepository.save(airport);
    }

    public List<Airport> getAllAirports() {
        return airportRepository.findAll();
    }

    public Airport getAirport(String iataCode) {
        return airportRepository.findById(iataCode.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Airport not found: " + iataCode));
    }
}