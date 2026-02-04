package com.airlinemanagementsystem.flight.controller;

import com.airlinemanagementsystem.flight.dto.FlightSearchRequest;
import com.airlinemanagementsystem.flight.entity.Flight;
import com.airlinemanagementsystem.flight.service.FlightSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "Flight Search", description = "High-performance flight discovery APIs")
public class FlightSearchController {

    private final FlightSearchService flightSearchService;

    /**
     * Advanced Search API with Pagination and Filtering.
     * Use this for the main flight results page.
     * Example: /api/v1/search/advanced?source=JFK&destination=LHR&date=2026-06-01&maxPrice=500&page=0&size=10
     */
    @Operation(summary = "Search flights with dynamic filters, sorting, and pagination")
    @GetMapping("/advanced")
    public ResponseEntity<Page<Flight>> searchAdvanced(@ModelAttribute FlightSearchRequest request) {
        return ResponseEntity.ok(flightSearchService.findFlights(request));
    }

    /**
     * Simple Search API (Kept for backward compatibility or simple quick-search widgets).
     * Refactored to call the same high-performance logic under the hood.
     */
    @Operation(summary = "Legacy simple search for quick discovery")
    @GetMapping
    public ResponseEntity<List<Flight>> searchSimple(
            @ModelAttribute FlightSearchRequest request) {
        // We reuse the DTO here. Even if the user only sends 3 params,
        // the Specification logic handles it.
        Page<Flight> resultPage = flightSearchService.findFlights(request);
        return ResponseEntity.ok(resultPage.getContent());
    }
}
