package com.airlinemanagementsystem.booking.repository;

import com.airlinemanagementsystem.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    /**
     * Finds the single most recent booking for a specific user.
     * * @param userId The ID of the user (from JWT/Request)
     * @return The latest booking entity, or null if none found.
     */
    Booking findTopByUserIdOrderByBookingDateDesc(String userId);
}
