package com.airlinemanagementsystem.flight.service;

import com.airlinemanagementsystem.flight.entity.Airport;
import com.airlinemanagementsystem.flight.entity.Aircraft;
import com.airlinemanagementsystem.flight.entity.Flight;
import com.airlinemanagementsystem.flight.entity.FlightStatus;
import com.airlinemanagementsystem.flight.exception.FlightNotFoundException;
import com.airlinemanagementsystem.flight.repository.AircraftRepository;
import com.airlinemanagementsystem.flight.repository.AirportRepository;
import com.airlinemanagementsystem.flight.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlightService {

    private final FlightRepository flightRepository;
    private final AircraftRepository aircraftRepository;
    private final AirportRepository airportRepository;
    private final FlightEventProducer flightEventProducer;

    @Transactional
    public Flight createFlight(Flight flight, String tailNumber, String sourceCode, String destCode) {
        log.info("Creating flight {} with aircraft {}", flight.getFlightNumber(), tailNumber);

        Aircraft aircraft = aircraftRepository.findByTailNumber(tailNumber)
                .orElseThrow(() -> new RuntimeException("Aircraft not found with tail number: " + tailNumber));

        Airport source = airportRepository.findByCode(sourceCode)
                .orElseThrow(() -> new RuntimeException("Source airport not found: " + sourceCode));
        Airport dest = airportRepository.findByCode(destCode)
                .orElseThrow(() -> new RuntimeException("Destination airport not found: " + destCode));

        flight.setAircraft(aircraft);
        flight.setSourceAirport(source);
        flight.setDestinationAirport(dest);

        flight.generateSeats();

        Flight savedFlight = flightRepository.save(flight);

        flightEventProducer.sendFlightUpdateEvent(savedFlight, "FLIGHT_CREATED");

        return savedFlight;
    }

    @Transactional(readOnly = true)
    public List<Flight> getAllFlights() {
        return flightRepository.findAll();
    }

    @Transactional
    public Flight updateFlightStatus(Long flightId, FlightStatus newStatus) {
        log.info("Updating flight {} status to {}", flightId, newStatus);

        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new FlightNotFoundException(flightId));

        if (flight.getStatus() != newStatus) {
            flight.setStatus(newStatus);
            Flight updatedFlight = flightRepository.save(flight);

            flightEventProducer.sendFlightUpdateEvent(updatedFlight, "FLIGHT_STATUS_CHANGED");
            return updatedFlight;
        }
        return flight;
    }
}