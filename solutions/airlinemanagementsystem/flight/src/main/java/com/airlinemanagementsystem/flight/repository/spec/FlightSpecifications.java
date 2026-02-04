package com.airlinemanagementsystem.flight.repository.spec;

import com.airlinemanagementsystem.flight.dto.FlightSearchRequest;
import com.airlinemanagementsystem.flight.entity.Flight;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class FlightSpecifications {
    public static Specification<Flight> buildSearchQuery(FlightSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Mandatory: Route
            predicates.add(cb.equal(root.get("sourceAirport").get("iataCode"), request.getSource()));
            predicates.add(cb.equal(root.get("destinationAirport").get("iataCode"), request.getDestination()));

            // 2. Mandatory: Date Range
            Instant start = request.getDate().atStartOfDay(ZoneOffset.UTC).toInstant();
            Instant end = request.getDate().plusDays(1).atStartOfDay(ZoneOffset.UTC).minusNanos(1).toInstant();
            predicates.add(cb.between(root.get("departureTime"), start, end));

            // 3. Optional Filter: Max Price
            if (request.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("basePrice"), request.getMaxPrice()));
            }

            // 4. Optional Filter: Airline Name
            if (request.getAirlineName() != null) {
                predicates.add(cb.like(cb.lower(root.get("airline")), "%" + request.getAirlineName().toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
