package com.airlinemanagementsystem.flight.repository;

import com.airlinemanagementsystem.flight.entity.Aircraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AircraftRepository extends JpaRepository<Aircraft, Long> {
    Optional<Aircraft> findByTailNumber(String tailNumber);
}
