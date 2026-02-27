package com.airlinemanagementsystem.flight.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({ "name", "iata", "icao" })
public class AircraftSourceDto {
    private String name;
    private String iata;
    private String icao;
}
