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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
public class FlightIntegrationService {

    private final FlightRepository flightRepository;
    private final AirportRepository airportRepository;
    private final AircraftRepository aircraftRepository;
    private final FlightEventProducer flightEventProducer;

    @Value("${amadeus.api.key}")
    private String apiKey;

    @Value("${amadeus.api.secret}")
    private String apiSecret;

    private Amadeus amadeus;

    @PostConstruct
    public void init() {
        if (apiKey != null && !apiKey.isBlank() && !apiKey.contains("TEST_KEY")) {
            this.amadeus = Amadeus.builder(apiKey, apiSecret).build();
            log.info("Amadeus Client Initialized successfully.");
        } else {
            log.warn("Amadeus API Key is missing or invalid. External calls will fail.");
        }
    }

    /**
     * Main Entry Point: Fetches from Amadeus. If it fails, generates MOCK data.
     */
    public void fetchAndSaveFlights(String origin, String destination, LocalDate date) {
        try {
            if (amadeus == null) {
                throw new RuntimeException("Amadeus client is null (Check API Keys)");
            }

            log.info("Calling Amadeus API for route: {} -> {} on {}", origin, destination, date);

            FlightOfferSearch[] offers = amadeus.shopping.flightOffersSearch.get(
                    Params.with("originLocationCode", origin)
                            .and("destinationLocationCode", destination)
                            .and("departureDate", date.toString())
                            .and("adults", 1)
                            .and("max", 5)
            );

            if (offers != null && offers.length > 0) {
                int savedCount = 0;
                for (FlightOfferSearch offer : offers) {
                    if (saveFlight(offer)) {
                        savedCount++;
                    }
                }
                log.info("Successfully imported {} flights from Amadeus.", savedCount);
            } else {
                log.warn("Amadeus returned no flights. Generating mocks...");
                generateMockFlights(origin, destination, date);
            }

        } catch (Exception e) {
            log.error("Amadeus API Failed: {}. Triggering Fallback...", e.getMessage());
            generateMockFlights(origin, destination, date);
        }
    }

    /**
     * Parses and saves a single flight offer.
     * Returns true if successful, false if skipped.
     */
    private boolean saveFlight(FlightOfferSearch offer) {
        try {
            if (offer.getItineraries() == null || offer.getItineraries().length == 0) return false;

            FlightOfferSearch.Itinerary itinerary = offer.getItineraries()[0];
            FlightOfferSearch.SearchSegment segment = itinerary.getSegments()[0];

            Airport source = airportRepository.findByCode(segment.getDeparture().getIataCode()).orElse(null);
            Airport dest = airportRepository.findByCode(segment.getArrival().getIataCode()).orElse(null);

            if (source == null || dest == null) {
                log.debug("Skipping flight: Airports not found in DB.");
                return false;
            }

            String aircraftCode = segment.getAircraft().getCode();
            Aircraft aircraft = aircraftRepository.findFirstByTailNumberStartingWith(aircraftCode)
                    .orElse(aircraftRepository.findAll().stream().findFirst().orElse(null));

            Instant departureTime = parseToInstant(segment.getDeparture().getAt());
            String flightNumber = segment.getCarrierCode() + segment.getNumber();

            if (flightRepository.existsByFlightNumberAndDepartureTime(flightNumber, departureTime)) {
                return false;
            }

            Flight flight = Flight.builder()
                    .flightNumber(flightNumber)
                    .airline(getAirlineName(segment.getCarrierCode()))
                    .sourceAirport(source)
                    .destinationAirport(dest)
                    .aircraft(aircraft)
                    .departureTime(parseToInstant(segment.getDeparture().getAt()))
                    .arrivalTime(parseToInstant(segment.getArrival().getAt()))
                    .basePrice(parsePrice(offer.getPrice().getTotal()))
                    .status(FlightStatus.ON_TIME)
                    .build();

            flight.generateSeats();
            Flight savedFlight = flightRepository.save(flight);
            flightEventProducer.sendFlightUpdateEvent(savedFlight, "FLIGHT_CREATED");
            return true;

        } catch (Exception e) {
            log.warn("Failed to parse/save flight offer: {}", e.getMessage());
            return false;
        }
    }

    /**
     * FALLBACK: Generates 2 dummy flights if Amadeus is down.
     */
    private void generateMockFlights(String origin, String destination, LocalDate date) {
        log.info("Generating Mock Flights for {} -> {}", origin, destination);

        Airport source = airportRepository.findByCode(origin).orElse(null);
        Airport dest = airportRepository.findByCode(destination).orElse(null);

        if (source == null || dest == null) {
            log.error("Cannot generate mocks: Airports {} or {} missing from DB.", origin, destination);
            return;
        }

        Aircraft aircraft = aircraftRepository.findAll().stream().findFirst().orElse(null);
        if (aircraft == null) return;

        createDummyFlight(source, dest, aircraft, date.atTime(8, 0).toInstant(ZoneOffset.UTC), "MOCK-101", 150.0);

        createDummyFlight(source, dest, aircraft, date.atTime(18, 0).toInstant(ZoneOffset.UTC), "MOCK-202", 220.0);
    }

    private void createDummyFlight(Airport source, Airport dest, Aircraft aircraft, Instant departure, String flightNumber, Double price) {
        if (flightRepository.existsByFlightNumberAndDepartureTime(flightNumber, departure)) {
            return;
        }

        Flight flight = Flight.builder()
                .flightNumber(flightNumber)
                .airline("Mock Airlines")
                .sourceAirport(source)
                .destinationAirport(dest)
                .aircraft(aircraft)
                .departureTime(departure)
                .arrivalTime(departure.plusSeconds(7200))
                .basePrice(price)
                .status(FlightStatus.ON_TIME)
                .build();

        flight.generateSeats();
        Flight savedFlight = flightRepository.save(flight);
        flightEventProducer.sendFlightUpdateEvent(savedFlight, "FLIGHT_CREATED");
        log.info("Saved Mock Flight: {}", flightNumber);
    }

    private Instant parseToInstant(String dateString) {
        return LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                .toInstant(ZoneOffset.UTC);
    }

    private Double parsePrice(String price) {
        try {
            return Double.parseDouble(price);
        } catch (NumberFormatException e) {
            return 100.0;
        }
    }

    private String getAirlineName(String carrierCode) {
        return switch (carrierCode) {
            case "AI" -> "Air India";
            case "EK" -> "Emirates";
            case "BA" -> "British Airways";
            case "LH" -> "Lufthansa";
            case "UA" -> "United Airlines";
            case "DL" -> "Delta Airlines";
            default -> carrierCode + " Airlines";
        };
    }
}
