package com.airlinemanagementsystem.flight.service.cache;

import com.airlinemanagementsystem.flight.config.KafkaConfig;
import com.airlinemanagementsystem.flight.dto.FlightEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlightCacheInvalidator {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String SEARCH_CACHE_PREFIX = "FLIGHT_SEARCH::";

    @KafkaListener(topics = KafkaConfig.FLIGHT_STATUS_TOPIC, groupId = "flight-cache-group")
    public void handleFlightUpdate(FlightEvent event) {
        log.info("Received Kafka Event: {}. Invalidating Cache...", event.getEventType());

        // Invalidate specific search key: FLIGHT_SEARCH::SOURCE:DEST:DATE
        LocalDate flightDate = LocalDate.ofInstant(event.getDepartureTime(), ZoneOffset.UTC);
        String cacheKey = SEARCH_CACHE_PREFIX + event.getSourceAirport() + ":" +
                event.getDestinationAirport() + ":" + flightDate;

        redisTemplate.delete(cacheKey);
        log.info("Cache evicted for key: {}", cacheKey);

        // Optimization: In a real system, we might delete all keys matching
        // the source-destination pair because time changes might move flights between dates.
    }
}
