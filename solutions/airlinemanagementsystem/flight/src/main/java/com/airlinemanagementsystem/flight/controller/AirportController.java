package com.airlinemanagementsystem.flight.controller;

import com.airlinemanagementsystem.flight.entity.Airport;
import com.airlinemanagementsystem.flight.service.AirportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/airports")
@RequiredArgsConstructor
@Tag(name = "Airport Management", description = "Endpoints for managing global airport data")
public class AirportController {

    private final AirportService airportService;

    @PostMapping
    @Operation(summary = "Add a new airport", description = "Provide IATA code, location, and a valid IANA timezone ID")
    public ResponseEntity<Airport> createAirport(@Valid @RequestBody Airport airport) {
        return new ResponseEntity<>(airportService.addAirport(airport), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Airport>> listAll() {
        return ResponseEntity.ok(airportService.getAllAirports());
    }

    @GetMapping("/{iataCode}")
    public ResponseEntity<Airport> getByCode(@PathVariable String iataCode) {
        return ResponseEntity.ok(airportService.getAirport(iataCode));
    }
}
