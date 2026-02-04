package com.airlinemanagementsystem.flight.repository;

import com.airlinemanagementsystem.flight.entity.Airport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AirportRepository extends JpaRepository<Airport, String> {
    List<Airport> findByCountry(String country);
    List<Airport> findByCityContainingIgnoreCase(String city);
}