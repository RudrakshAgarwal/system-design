package com.airlinemanagementsystem.flight.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "airports")
public class Airport {

    @Id
    @Pattern(regexp = "^[A-Z]{3}$", message = "IATA code must be exactly 3 uppercase letters")
    private String iataCode;

    @NotBlank(message = "Airport name is required")
    private String name;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Country is required")
    private String country;

    @NotBlank(message = "Timezone is required")
    @Column(name = "timezone_id")
    private String timezoneId;
}
