package com.airlinemanagementsystem.booking.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @Column(name = "booking_reference", unique = true, nullable = false, length = 50)
    private String bookingReference;

    @NotNull(message = "Flight ID is required")
    private Long flightId;

    @NotNull(message = "User ID is required")
    private String userId;

    @NotNull
    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private LocalDateTime bookingDate;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Passenger> passengers = new ArrayList<>();

    /**
     * Helper method to add a passenger.
     * This ensures the bidirectional link is set correctly in Java memory.
     */
    public void addPassenger(Passenger passenger) {
        passengers.add(passenger);
        passenger.setBooking(this);
    }

    @PrePersist
    public void generateReference() {
        if (this.bookingReference == null) {
            this.bookingReference = "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }
}