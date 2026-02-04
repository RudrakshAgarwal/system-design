package com.airlinemanagementsystem.flight.service;

import com.airlinemanagementsystem.flight.config.KafkaConfig;
import com.airlinemanagementsystem.flight.dto.FlightEvent;
import com.airlinemanagementsystem.flight.entity.Flight;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlightEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendFlightUpdateEvent(Flight flight, String eventType) {
        FlightEvent event = FlightEvent.builder()
                .flightId(flight.getId())
                .flightNumber(flight.getFlightNumber())
                .sourceAirport(flight.getSourceAirport().getIataCode())
                .destinationAirport(flight.getDestinationAirport().getIataCode())
                .departureTime(flight.getDepartureTime())
                .status(flight.getStatus())
                .eventType(eventType)
                .build();


        log.info("Publishing Flight Event: {} for Flight: {}", eventType, flight.getFlightNumber());

        // Using FlightNumber as partition key to ensure order for the same flight
        kafkaTemplate.send(KafkaConfig.FLIGHT_STATUS_TOPIC, flight.getFlightNumber(), event);
    }
}
