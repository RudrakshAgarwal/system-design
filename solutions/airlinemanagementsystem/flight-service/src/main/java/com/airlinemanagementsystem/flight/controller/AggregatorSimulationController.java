package com.airlinemanagementsystem.flight.controller;

import com.airlinemanagementsystem.flight.entity.Flight;
import com.airlinemanagementsystem.flight.integration.FlightAggregatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/simulation/aggregate")
@RequiredArgsConstructor
@Tag(name = "Flight Aggregation Simulation", description = "Simulates the Scatter-Gather pattern querying multiple external airlines with Resilience4j Circuit Breakers")
public class AggregatorSimulationController {

    private final FlightAggregatorService aggregatorService;

    @Operation(
            summary = "Aggregate Flights from External Providers",
            description = "Fires concurrent requests to FastAir (50ms), SlowJet (3000ms), and FlakyFly (20% failure rate). Demonstrates partial success, graceful degradation, and circuit breaker short-circuiting.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully aggregated flights (returns partial results if a provider failed or its circuit is open)")
            }
    )
    @GetMapping
    public ResponseEntity<List<Flight>> simulateAggregation(
            @Parameter(description = "Source Airport Code", example = "DEL")
            @RequestParam(defaultValue = "DEL") String source,

            @Parameter(description = "Destination Airport Code", example = "BOM")
            @RequestParam(defaultValue = "BOM") String destination) {

        List<Flight> results = aggregatorService.aggregateFlights(source, destination, LocalDate.now());
        return ResponseEntity.ok(results);
    }
}