package com.airlinemanagementsystem.search.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "flights")
public class FlightDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String flightNumber;

    @Field(type = FieldType.Text)
    private String airline;

    @Field(type = FieldType.Keyword)
    private String sourceAirport;

    @Field(type = FieldType.Keyword)
    private String destinationAirport;

    @Field(type = FieldType.Date)
    private Instant departureTime;

    @Field(type = FieldType.Date)
    private Instant arrivalTime;

    @Field(type = FieldType.Double)
    private Double basePrice;

    @Field(type = FieldType.Keyword)
    private String status;
}
