package com.airlinemanagementsystem.flight.repository;

import com.airlinemanagementsystem.flight.entity.Flight;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long>, JpaSpecificationExecutor<Flight> {

    /**
     * 1. High-Performance Basic Search
     * Forces immediate loading of Airports and Aircraft to prevent Hibernate Proxies
     * which cause the Redis Serialization errors we saw earlier.
     */
    @Query("SELECT f FROM Flight f " +
            "JOIN FETCH f.sourceAirport " +
            "JOIN FETCH f.destinationAirport " +
            "JOIN FETCH f.aircraft " +
            "WHERE f.sourceAirport.iataCode = :source " +
            "AND f.destinationAirport.iataCode = :destination " +
            "AND f.departureTime >= :start AND f.departureTime <= :end")
    List<Flight> findAvailableFlights(
            @Param("source") String source,
            @Param("destination") String destination,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    /**
     * 2. Advanced Dynamic Search with Pagination
     * Using @EntityGraph ensures that even with dynamic Specifications,
     * Hibernate will perform the JOINS in a single query.
     */
    @Override
    @EntityGraph(attributePaths = {"sourceAirport", "destinationAirport", "aircraft"})
    Page<Flight> findAll(Specification<Flight> spec, Pageable pageable);
}
