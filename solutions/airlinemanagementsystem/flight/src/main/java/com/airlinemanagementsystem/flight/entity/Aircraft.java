package com.airlinemanagementsystem.flight.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Data
@Table(name = "aircrafts")
public class Aircraft {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long aircraftId;

    @NotBlank(message = "Tail number is required")
    @Column(unique = true, nullable = false)
    private String tailNumber;

    private String model;

    @Min(value = 1, message = "Capacity must be at least 1")
    private int totalCapacity;
}
