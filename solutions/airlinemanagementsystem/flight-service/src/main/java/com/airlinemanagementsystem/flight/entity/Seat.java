package com.airlinemanagementsystem.flight.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "seats", indexes = {
        @Index(name = "idx_flight_seat", columnList = "flight_id, seat_number")
})
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seatId;

    @Version
    private Integer version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id")
    @JsonBackReference
    @ToString.Exclude
    private Flight flight;

    @Column(nullable = false, name = "seat_number")
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type")
    private SeatType seatType;

    @Enumerated(EnumType.STRING)
    private SeatStatus status = SeatStatus.AVAILABLE;

    @Column(nullable = false)
    private Double price;

    public boolean isAvailable() {
        return SeatStatus.AVAILABLE.equals(this.status);
    }
}

