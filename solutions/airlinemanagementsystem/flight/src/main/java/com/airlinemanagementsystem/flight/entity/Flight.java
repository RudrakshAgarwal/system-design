package com.airlinemanagementsystem.flight.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "flights", indexes = {
        @Index(name = "idx_flight_search", columnList = "source_airport_id, destination_airport_id, departureTime")
})
public class Flight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String airline;

    @Version
    private Integer version; // Prevents Lost Updates in high-concurrency seat booking

    @NotBlank
    @Column(unique = true)
    private String flightNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "source_airport_id")
    private Airport sourceAirport;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "destination_airport_id")
    private Airport destinationAirport;

    @NotNull
    private Instant departureTime;

    @NotNull
    private Instant arrivalTime;

    @Enumerated(EnumType.STRING)
    private FlightStatus status = FlightStatus.ON_TIME;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aircraft_id")
    private Aircraft aircraft;

    @NotNull(message = "Base price cannot be null")
    @Positive(message = "Price must be greater than zero")
    private Double basePrice;

    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Seat> seats = new ArrayList<>();

    public void generateSeats() {
        if (this.aircraft == null) return;

        int capacity = this.aircraft.getTotalCapacity();
        for (int i = 1; i <= capacity; i++) {
            Seat seat = new Seat();
            seat.setFlight(this);
            seat.setStatus(SeatStatus.AVAILABLE);

            if (i <= 10) {
                seat.setSeatNumber("F" + i);
                seat.setSeatType(SeatType.FIRST);
            } else if (i <= 30) {
                seat.setSeatNumber("B" + i);
                seat.setSeatType(SeatType.BUSINESS);
            } else {
                seat.setSeatNumber("E" + i);
                seat.setSeatType(SeatType.ECONOMY);
            }
            this.seats.add(seat);
        }
    }
}