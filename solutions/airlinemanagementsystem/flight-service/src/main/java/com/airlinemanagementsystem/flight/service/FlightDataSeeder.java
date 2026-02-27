package com.airlinemanagementsystem.flight.service;

import com.airlinemanagementsystem.flight.entity.Aircraft;
import com.airlinemanagementsystem.flight.entity.Airport;
import com.airlinemanagementsystem.flight.entity.Flight;
import com.airlinemanagementsystem.flight.entity.FlightStatus;
import com.airlinemanagementsystem.flight.repository.AircraftRepository;
import com.airlinemanagementsystem.flight.repository.AirportRepository;
import com.airlinemanagementsystem.flight.repository.FlightRepository;
import com.amadeus.Amadeus;
import com.amadeus.Params;
import com.amadeus.resources.FlightOfferSearch;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
@DependsOn("referenceDataSeeder")
public class FlightDataSeeder {

    private final FlightIntegrationService flightIntegrationService;
    private final FlightRepository flightRepository;

    @PostConstruct
    public void seedFlights() {
        if (flightRepository.count() > 0) {
            log.info("Flights already exist in DB. Skipping initial seed.");
            return;
        }

        log.info("Seeding popular flight routes for demo...");

        LocalDate date = LocalDate.now().plusDays(2);

        flightIntegrationService.fetchAndSaveFlights("DEL", "BOM", date);
        flightIntegrationService.fetchAndSaveFlights("JFK", "LHR", date);
        flightIntegrationService.fetchAndSaveFlights("DXB", "LHR", date);

        log.info("Initial Flight Seeding Completed.");
    }
}