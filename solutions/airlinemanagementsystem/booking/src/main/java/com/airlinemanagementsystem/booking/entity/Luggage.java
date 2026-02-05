package com.airlinemanagementsystem.booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "luggage")
public class Luggage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long luggageId;

    @Enumerated(EnumType.STRING)
    private LuggageType type; // CHECKED, CABIN

    private double weight;
    private double price;

    // --- NEW FIELDS ---
    @Column(unique = true, nullable = false)
    private String tagNumber; // The Barcode

    @Enumerated(EnumType.STRING)
    private LuggageStatus status;

    // For identification if tag is lost
    private String color;
    private String brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id")
    @ToString.Exclude
    private Passenger passenger;

    @OneToMany(mappedBy = "luggage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LuggageHistory> history = new ArrayList<>();

    @PrePersist
    public void generateTag() {
        if (this.tagNumber == null) {
            // Generate a 10-char IATA style tag (e.g., "001-ABC1234")
            this.tagNumber = "TAG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            this.status = LuggageStatus.BOOKED;
        }
    }
}
