package com.airlinemanagementsystem.flight.repository;

import com.airlinemanagementsystem.flight.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByFlightId(Long flightId);
    Optional<Seat> findByFlightIdAndSeatNumber(Long flightId, String seatNumber);
}
