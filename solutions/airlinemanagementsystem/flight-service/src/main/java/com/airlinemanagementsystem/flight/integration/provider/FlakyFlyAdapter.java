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
import java.util.Random;

@Service
@Slf4j
public class FlakyFlyAdapter implements ExternalAirlineAdapter {

    private final Random random = new Random();

    @Override
    public String getProviderName() {
        return "FlakyFly";
    }

    @Override
    @CircuitBreaker(name = "flakyFly", fallbackMethod = "fallbackEmptyFlights")
    public List<Flight> searchFlights(String source, String destination, LocalDate date) {
        log.info("[FlakyFly] Searching flights from {} to {}...", source, destination);
        if (random.nextInt(100) < 20) {
            log.error("[FlakyFly] Simulated 500 Internal Server Error!");
            throw new RuntimeException("FlakyFly API is down!");
        }

        return List.of(createMockFlight("FF-777", "FlakyFly", source, destination, date, 85.0));
    }

    public List<Flight> fallbackEmptyFlights(String source, String destination, LocalDate date, Throwable t) {
        log.warn("[FlakyFly Fallback] API Failed or Circuit Open! Returning empty list. Error: {}", t.getMessage());
        return Collections.emptyList();
    }

    private Flight createMockFlight(String number, String airline, String src, String dest, LocalDate date, Double price) {
        Airport sourceAirport = new Airport();
        Airport destAirport = new Airport();

        Instant departure = date.atStartOfDay().toInstant(ZoneOffset.UTC).plus(18, ChronoUnit.HOURS);
        Instant arrival = departure.plus(1, ChronoUnit.HOURS);

        Flight flight = new Flight();
        flight.setFlightNumber(number);
        flight.setAirline(airline);
        flight.setSourceAirport(sourceAirport);
        flight.setDestinationAirport(destAirport);
        flight.setDepartureTime(departure);
        flight.setArrivalTime(arrival);
        flight.setBasePrice(price);
        flight.setStatus(FlightStatus.DELAYED);

        return flight;
    }
}
