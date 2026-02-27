package com.airlinemanagementsystem.flight.service;

import com.airlinemanagementsystem.flight.entity.Airport;
import com.airlinemanagementsystem.flight.repository.AirportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AirportService {

    private final AirportRepository airportRepository;

    @Transactional
    public Airport addAirport(Airport airport) {
        try {
            if (airport.getTimezoneId() != null) {
                ZoneId.of(airport.getTimezoneId());
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Timezone ID: " + airport.getTimezoneId());
        }
        return airportRepository.save(airport);
    }

    @Transactional(readOnly = true)
    public List<Airport> getAllAirports() {
        return airportRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Airport getAirport(String code) {
        return airportRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Airport not found with code: " + code));
    }
}