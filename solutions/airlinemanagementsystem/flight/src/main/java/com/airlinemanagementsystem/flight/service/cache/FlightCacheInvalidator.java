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
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlightCacheInvalidator {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String SEARCH_CACHE_PREFIX = "FLIGHT_SEARCH::";

    @KafkaListener(topics = KafkaConfig.FLIGHT_STATUS_TOPIC, groupId = "flight-cache-group")
    public void handleFlightUpdate(FlightEvent event) {
        log.info("Received Kafka Event: {}. Invalidating Cache...", event.getEventType());

        // Pattern: FLIGHT_SEARCH::SOURCE:DEST:DATE*
        LocalDate flightDate = LocalDate.ofInstant(event.getDepartureTime(), ZoneOffset.UTC);
        String pattern = SEARCH_CACHE_PREFIX + event.getSourceAirport() + ":" +
                event.getDestinationAirport() + ":" + flightDate + "*";

        Set<String> keys = redisTemplate.keys(pattern);

        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("Evicted {} cache keys matching pattern: {}", keys.size(), pattern);
        }
    }
}
