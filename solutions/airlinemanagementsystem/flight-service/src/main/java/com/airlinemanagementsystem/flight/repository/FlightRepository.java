package com.airlinemanagementsystem.flight.repository;

import com.airlinemanagementsystem.flight.entity.Flight;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {

    boolean existsByFlightNumberAndDepartureTime(String flightNumber, Instant departureTime);

    /**
     * KEPT: Get All Flights (Admin API)
     * We MUST override this to force eager loading of the Aircraft and Airports
     * so the Admin Dashboard doesn't suffer from N+1 query performance issues.
     */
    @Override
    @EntityGraph(attributePaths = {"sourceAirport", "destinationAirport", "aircraft"})
    List<Flight> findAll();
}