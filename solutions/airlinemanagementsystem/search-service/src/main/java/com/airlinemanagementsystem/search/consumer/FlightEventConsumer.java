package com.airlinemanagementsystem.search.consumer;

import com.airlinemanagementsystem.search.document.FlightDocument;
import com.airlinemanagementsystem.search.dto.FlightEvent;
import com.airlinemanagementsystem.search.repository.SearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FlightEventConsumer {

    private final SearchRepository searchRepository;

    @KafkaListener(topics = "flight-status-topic", groupId = "search-indexer-group")
    public void consumeFlightEvent(FlightEvent event) {
        log.info("📥 Received Kafka Event: [{}] for Flight: {}", event.getEventType(), event.getFlightNumber());

        try {
            FlightDocument document = FlightDocument.builder()
                    .id(String.valueOf(event.getFlightId()))
                    .flightNumber(event.getFlightNumber())
                    .airline(event.getAirline())
                    .sourceAirport(event.getSourceAirport())
                    .destinationAirport(event.getDestinationAirport())
                    .departureTime(event.getDepartureTime())
                    .arrivalTime(event.getArrivalTime())
                    .basePrice(event.getBasePrice())
                    .status(event.getStatus())
                    .build();

            searchRepository.save(document);
            log.info("✅ Successfully indexed flight {} into Elasticsearch.", document.getFlightNumber());

        } catch (Exception e) {
            log.error("❌ Failed to index flight {}: {}", event.getFlightNumber(), e.getMessage());
        }
    }
}
