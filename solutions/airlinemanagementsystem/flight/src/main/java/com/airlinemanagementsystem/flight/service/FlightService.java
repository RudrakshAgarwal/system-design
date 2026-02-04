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
    private final FlightEventProducer flightEventProducer; // Added missing injection

    @Transactional
    public Flight createFlight(Flight flight, String tailNumber, String sourceIata, String destIata) {
        log.info("Creating flight {} with aircraft {}", flight.getFlightNumber(), tailNumber);

        // 1. Link Aircraft
        Aircraft aircraft = aircraftRepository.findByTailNumber(tailNumber)
                .orElseThrow(() -> new RuntimeException("Aircraft not found: " + tailNumber));

        // 2. Link Airports (Crucial for a Major Project to ensure data integrity)
        Airport source = airportRepository.findById(sourceIata)
                .orElseThrow(() -> new RuntimeException("Source airport not found: " + sourceIata));
        Airport dest = airportRepository.findById(destIata)
                .orElseThrow(() -> new RuntimeException("Destination airport not found: " + destIata));

        flight.setAircraft(aircraft);
        flight.setSourceAirport(source);
        flight.setDestinationAirport(dest);

        // 3. Trigger auto-generation of seats
        flight.generateSeats();

        // 4. Save Flight (Cascades to Seats)
        Flight savedFlight = flightRepository.save(flight);

        // 5. Notify system of new flight (Optional but good for real-time updates)
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

        flight.setStatus(newStatus);
        Flight updatedFlight = flightRepository.save(flight);

        // Trigger Event for Cache Invalidation & Notifications
        flightEventProducer.sendFlightUpdateEvent(updatedFlight, "FLIGHT_STATUS_CHANGED");

        return updatedFlight;
    }
}