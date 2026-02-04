package com.airlinemanagementsystem.flight.controller;

import com.airlinemanagementsystem.flight.entity.Flight;
import com.airlinemanagementsystem.flight.entity.FlightStatus;
import com.airlinemanagementsystem.flight.service.FlightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/flights")
@RequiredArgsConstructor
@Tag(name = "Flight Management", description = "Admin APIs for scheduling and managing flight operations")
public class FlightController {

    private final FlightService flightService;

    @Operation(
            summary = "Schedule a new flight",
            description = "Creates a flight record, links it to an aircraft and airports, and auto-generates the seat map."
    )
    @PostMapping("/{tailNumber}/{sourceIata}/{destIata}")
    public ResponseEntity<Flight> scheduleFlight(
            @Valid @RequestBody Flight flight,
            @Parameter(description = "Tail number of the aircraft (e.g., N12345)") @PathVariable String tailNumber,
            @Parameter(description = "Source Airport IATA Code (e.g., JFK)") @PathVariable String sourceIata,
            @Parameter(description = "Destination Airport IATA Code (e.g., LHR)") @PathVariable String destIata) {

        Flight createdFlight = flightService.createFlight(flight, tailNumber, sourceIata, destIata);
        return new ResponseEntity<>(createdFlight, HttpStatus.CREATED);
    }

    @Operation(summary = "Update flight status", description = "Changes flight status (e.g., ON_TIME to DELAYED) and triggers Kafka events.")
    @PatchMapping("/{flightId}/status")
    public ResponseEntity<Flight> updateStatus(
            @PathVariable Long flightId,
            @RequestParam FlightStatus status) {
        return ResponseEntity.ok(flightService.updateFlightStatus(flightId, status));
    }

    @Operation(summary = "Get all scheduled flights")
    @GetMapping
    public ResponseEntity<List<Flight>> getAllFlights() {
        return ResponseEntity.ok(flightService.getAllFlights());
    }
}