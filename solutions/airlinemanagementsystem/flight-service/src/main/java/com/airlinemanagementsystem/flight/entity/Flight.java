package com.airlinemanagementsystem.flight.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "flights", indexes = {
        @Index(name = "idx_flight_search", columnList = "source_airport_id, destination_airport_id, departure_time")
})
public class Flight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String airline;

    @Version
    private Integer version;

    @NotBlank
    @Column(unique = true, name = "flight_number")
    private String flightNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "source_airport_id")
    private Airport sourceAirport;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "destination_airport_id")
    private Airport destinationAirport;

    @NotNull
    @Column(name = "departure_time")
    private Instant departureTime;

    @NotNull
    @Column(name = "arrival_time")
    private Instant arrivalTime;

    @Enumerated(EnumType.STRING)
    private FlightStatus status = FlightStatus.ON_TIME;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aircraft_id")
    private Aircraft aircraft;

    @NotNull(message = "Base price cannot be null")
    @Positive(message = "Price must be greater than zero")
    @Column(name = "base_price")
    private Double basePrice;

    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    @ToString.Exclude
    private List<Seat> seats = new ArrayList<>();

    public void generateSeats() {
        if (this.aircraft == null || this.basePrice == null) return;

        this.seats.clear();
        int capacity = this.aircraft.getTotalCapacity();

        for (int i = 1; i <= capacity; i++) {
            Seat seat = new Seat();
            seat.setFlight(this);
            seat.setStatus(SeatStatus.AVAILABLE);

            double priceMultiplier = 1.0;

            if (i <= 10) {
                seat.setSeatNumber("F" + i);
                seat.setSeatType(SeatType.FIRST);
                priceMultiplier = 2.5;
            } else if (i <= 30) {
                seat.setSeatNumber("B" + i);
                seat.setSeatType(SeatType.BUSINESS);
                priceMultiplier = 1.5;
            } else {
                seat.setSeatNumber("E" + i);
                seat.setSeatType(SeatType.ECONOMY);
                priceMultiplier = 1.0;
            }

            seat.setPrice(this.basePrice * priceMultiplier);

            this.seats.add(seat);
        }
    }
}