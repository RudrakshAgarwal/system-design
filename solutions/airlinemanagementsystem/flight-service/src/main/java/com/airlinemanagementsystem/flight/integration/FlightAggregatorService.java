package com.airlinemanagementsystem.flight.integration;

import com.airlinemanagementsystem.flight.entity.Flight;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FlightAggregatorService {

    private final List<ExternalAirlineAdapter> airlineAdapters;

    public List<Flight> aggregateFlights(String source, String destination, LocalDate date) {
        log.info("[Aggregator] Initiating Scatter-Gather across {} providers...", airlineAdapters.size());
        long startTime = System.currentTimeMillis();

        List<CompletableFuture<List<Flight>>> futures = airlineAdapters.stream()
                .map(adapter -> CompletableFuture.supplyAsync(() ->
                        adapter.searchFlights(source, destination, date)
                ))
                .toList();

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allOf.join();

        List<Flight> aggregatedFlights = futures.stream()
                .map(CompletableFuture::join)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        long endTime = System.currentTimeMillis();
        log.info("[Aggregator] Finished in {} ms. Total flights found: {}", (endTime - startTime), aggregatedFlights.size());

        return aggregatedFlights;
    }
}