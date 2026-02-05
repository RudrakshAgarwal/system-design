package com.airlinemanagementsystem.booking.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class BookingKafkaConfig {

    public static final String BOOKING_REQ_TOPIC = "booking-request-topic";

    @Bean
    public NewTopic bookingRequestTopic() {
        return TopicBuilder.name(BOOKING_REQ_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}