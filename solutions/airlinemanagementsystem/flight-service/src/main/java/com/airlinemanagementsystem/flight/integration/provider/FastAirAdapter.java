package com.airlinemanagementsystem.flight.integration.provider;

import com.airlinemanagementsystem.flight.entity.Airport;
import com.airlinemanagementsystem.flight.entity.Flight;
import com.airlinemanagementsystem.flight.entity.FlightStatus;
import com.airlinemanagementsystem.flight.integration.ExternalAirlineAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
public class FastAirAdapter implements ExternalAirlineAdapter {

    @Override
    public String getProviderName() {
        return "FastAir";
    }

    @Override
    public List<Flight> searchFlights(String source, String destination, LocalDate date) {
        log.info("🛫 [FastAir] Searching flights from {} to {}", source, destination);

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return List.of(createMockFlight("FA-101", "FastAir", source, destination, date, 150.0));
    }

    private Flight createMockFlight(String number, String airline, String src, String dest, LocalDate date, Double price) {
        Airport sourceAirport = new Airport();
        Airport destAirport = new Airport();

        Instant departure = date.atStartOfDay().toInstant(ZoneOffset.UTC).plus(10, ChronoUnit.HOURS);
        Instant arrival = departure.plus(2, ChronoUnit.HOURS);

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
