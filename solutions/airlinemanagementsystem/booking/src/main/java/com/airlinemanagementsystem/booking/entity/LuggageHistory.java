package com.airlinemanagementsystem.booking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "luggage_history")
public class LuggageHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "luggage_id")
    private Luggage luggage;

    private String location;
    private String scannedBy;

    @Enumerated(EnumType.STRING)
    private LuggageStatus status;

    private LocalDateTime scannedAt;
}
