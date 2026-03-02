package com.airlinemanagementsystem.flight.integration.provider;

import com.airlinemanagementsystem.flight.entity.Airport;
import com.airlinemanagementsystem.flight.entity.Flight;
import com.airlinemanagementsystem.flight.entity.FlightStatus;
import com.airlinemanagementsystem.flight.integration.ExternalAirlineAdapter;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class SlowJetAdapter implements ExternalAirlineAdapter {

    @Override
    public String getProviderName() {
        return "SlowJet";
    }

    @Override
    @CircuitBreaker(name = "slowJet", fallbackMethod = "fallbackEmptyFlights")
    public List<Flight> searchFlights(String source, String destination, LocalDate date) {
        log.info("🐢 [SlowJet] Searching flights from {} to {} (This will take 3 seconds...)", source, destination);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("[SlowJet] Finally responded!");
        return List.of(createMockFlight("SJ-999", "SlowJet", source, destination, date, 110.0));
    }

    public List<Flight> fallbackEmptyFlights(String source, String destination, LocalDate date, Throwable t) {
        log.warn("[SlowJet Fallback] Circuit Open or Timeout! Returning empty list. Error: {}", t.getMessage());
        return Collections.emptyList();
    }

    private Flight createMockFlight(String number, String airline, String src, String dest, LocalDate date, Double price) {
        Airport sourceAirport = new Airport();
        Airport destAirport = new Airport();

        Instant departure = date.atStartOfDay().toInstant(ZoneOffset.UTC).plus(14, ChronoUnit.HOURS);
        Instant arrival = departure.plus(3, ChronoUnit.HOURS);

        Flight flight = new Flight();
        flight.setFlightNumber(number);
        flight.setAirline(airline);
        flight.setSourceAirport(sourceAirport);
        flight.setDestinationAirport(destAirport);
        flight.setDepartureTime(departure);
        flight.setArrivalTime(arrival);
        flight.setBasePrice(price);
        flight.setStatus(FlightStatus.ON_TIME);

        return flight;
    }
}