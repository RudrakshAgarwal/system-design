package com.airlinemanagementsystem.flight.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String FLIGHT_STATUS_TOPIC = "flight-status-topic";
    public static final String SEAT_RESERVATION_TOPIC = "seat-reservation-topic";

    @Bean
    public NewTopic flightStatusTopic() {
        return TopicBuilder.name(FLIGHT_STATUS_TOPIC)
                .partitions(3) // High availability
                .replicas(1)   // Set to 3 in a real production cluster
                .build();
    }

    @Bean
    public NewTopic seatReservationTopic() {
        return TopicBuilder.name(SEAT_RESERVATION_TOPIC)
                .partitions(3)
                .build();
    }
}
