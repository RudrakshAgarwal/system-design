package com.airlinemanagementsystem.flight.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AirportSourceDto {
    @JsonProperty("iata")
    private String iataCode;

    @JsonProperty("icao")
    private String icaoCode;

    private String name;
    private String city;
    private String state;
    private String country;

    @JsonProperty("tz")
    private String timezone;
}
