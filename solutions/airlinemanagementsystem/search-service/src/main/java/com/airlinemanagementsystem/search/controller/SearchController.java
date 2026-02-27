package com.airlinemanagementsystem.search.controller;

import com.airlinemanagementsystem.search.document.FlightDocument;
import com.airlinemanagementsystem.search.dto.FlightSearchRequest;
import com.airlinemanagementsystem.search.service.FlightSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "Flight Search Engine", description = "High-performance search API backed by Elasticsearch (CQRS Read Model)")
public class SearchController {

    private final FlightSearchService flightSearchService;

    @Operation(summary = "Search for flights", description = "Query available flights by source, destination, and date. Bypasses the transactional database for high throughput.")
    @GetMapping
    public ResponseEntity<List<FlightDocument>> searchFlights(FlightSearchRequest request) {
        List<FlightDocument> results = flightSearchService.searchFlights(request);
        return ResponseEntity.ok(results);
    }
}
