package com.airlinemanagementsystem.flight.service;

import com.airlinemanagementsystem.flight.dto.FlightSearchRequest;
import com.airlinemanagementsystem.flight.entity.Flight;
import com.airlinemanagementsystem.flight.repository.FlightRepository;
import com.airlinemanagementsystem.flight.repository.spec.FlightSpecifications;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlightSearchService {

    private final FlightRepository flightRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String SEARCH_CACHE_PREFIX = "FLIGHT_SEARCH::";

    /**
     * Advanced Search with Pagination and Filters.
     * Uses Circuit Breaker to ensure that if Redis is slow/down, we hit DB directly.
     */
    @CircuitBreaker(name = "redisSearch", fallbackMethod = "fallbackFindFlights")
    public Page<Flight> findFlights(FlightSearchRequest request) {
        log.info("[SEARCH] -> Executing search: {} to {} on {}", request.getSource(), request.getDestination(), request.getDate());
        String cacheKey = generateAdvancedCacheKey(request);

        try {
            Page<Flight> cachedPage = (Page<Flight>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedPage != null) {
                log.info("[CACHE] -> Hit for key: {}", cacheKey);
                return cachedPage;
            }
        } catch (Exception e) {
            log.warn("[CACHE] -> Redis unavailable, proceeding to DB. Error: {}", e.getMessage());
        }

        log.info("[CACHE] -> Miss for key: {}. Querying MySQL.", cacheKey);
        Page<Flight> flightPage = queryDatabaseWithFilters(request);

        if (flightPage.hasContent()) {
            try {
                redisTemplate.opsForValue().set(cacheKey, flightPage, Duration.ofMinutes(10));
                log.debug("[CACHE] -> Data persisted to Redis for key: {}", cacheKey);
            } catch (Exception e) {
                log.error("[CACHE] -> Failed to update Redis: {}", e.getMessage());
            }
        }
        return flightPage;
    }

    /**
     * Fallback method for Resilience4j.
     * Returns results directly from DB if Redis fails.
     */
    public Page<Flight> fallbackFindFlights(FlightSearchRequest request, Throwable t) {
        log.error("Circuit Breaker triggered for Search. Falling back to DB. Error: {}", t.getMessage());
        return queryDatabaseWithFilters(request);
    }

    /**
     * Private helper to execute the Specification-based query.
     */
    private Page<Flight> queryDatabaseWithFilters(FlightSearchRequest request) {
        // Setup Pagination and Sorting
        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        // Build Dynamic Specification
        Specification<Flight> spec = FlightSpecifications.buildSearchQuery(request);

        return flightRepository.findAll(spec, pageable);
    }

    /**
     * Generates a unique cache key based on all search criteria.
     * Essential for real-world apps to avoid "Cache Poisoning" (returning wrong results).
     */
    private String generateAdvancedCacheKey(FlightSearchRequest request) {
        StringBuilder sb = new StringBuilder(SEARCH_CACHE_PREFIX);
        sb.append(request.getSource()).append(":")
                .append(request.getDestination()).append(":")
                .append(request.getDate()).append(":")
                .append("P").append(request.getPage()).append(":")
                .append("S").append(request.getSize());

        if (request.getMaxPrice() != null) sb.append(":MP").append(request.getMaxPrice());
        if (request.getAirlineName() != null) sb.append(":AL").append(request.getAirlineName());

        return sb.toString();
    }
}