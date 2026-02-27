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
        try {
            FlightEvent event = FlightEvent.builder()
                    .flightId(flight.getId())
                    .flightNumber(flight.getFlightNumber())
                    .airline(flight.getAirline())
                    .sourceAirport(flight.getSourceAirport().getCode())
                    .destinationAirport(flight.getDestinationAirport().getCode())
                    .departureTime(flight.getDepartureTime())
                    .arrivalTime(flight.getArrivalTime())
                    .basePrice(flight.getBasePrice())
                    .status(flight.getStatus())
                    .eventType(eventType)
                    .build();

            log.info("Publishing Flight Event: [{}] for Flight: {}", eventType, flight.getFlightNumber());

            // Using FlightNumber as partition key guarantees ordering
            kafkaTemplate.send(KafkaConfig.FLIGHT_STATUS_TOPIC, flight.getFlightNumber(), event);

        } catch (Exception e) {
            log.error("Failed to publish Kafka event for flight {}: {}", flight.getFlightNumber(), e.getMessage());
        }
    }
}
