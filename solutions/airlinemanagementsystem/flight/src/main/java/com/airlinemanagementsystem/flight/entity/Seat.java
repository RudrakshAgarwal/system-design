package com.airlinemanagementsystem.flight.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "seats", indexes = {
        @Index(name = "idx_flight_seat", columnList = "flight_id, seatNumber")
})
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seatId;

    @Version
    private Integer version; // For JPA Optimistic Locking

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id")
    @JsonBackReference // Prevents infinite recursion during JSON serialization
    private Flight flight;

    @Column(nullable = false)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    private SeatType seatType;

    @Enumerated(EnumType.STRING)
    private SeatStatus status = SeatStatus.AVAILABLE;

    // We can remove isReserved because 'status' (BOOKED/LOCKED) covers this logic more accurately

    /**
     * Helper method to check if seat is available for locking/booking
     */
    public boolean isAvailable() {
        return SeatStatus.AVAILABLE.equals(this.status);
    }
}

