package com.airlinemanagementsystem.flight.service;

import com.airlinemanagementsystem.flight.exception.SeatAlreadyLockedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatLockService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String LOCK_PREFIX = "LOCK::FLIGHT_SEAT::";
    private static final long LOCK_DURATION_MINUTES = 10;

    /**
     * Annotated with CircuitBreaker. If Redis fails, it prevents "ghost bookings"
     * by triggering the fallback.
     */
    @CircuitBreaker(name = "redisLock", fallbackMethod = "fallbackLock")
    public boolean acquireSeatLock(Long flightId, String seatNumber, String userId) {
        log.info("[LOCK-ATTEMPT] -> Flight: {}, Seat: {}, User: {}", flightId, seatNumber, userId);
        String lockKey = generateLockKey(flightId, seatNumber);

        Boolean success = redisTemplate.opsForValue().setIfAbsent(lockKey, userId, LOCK_DURATION_MINUTES, TimeUnit.MINUTES);

        if (Boolean.TRUE.equals(success)) {
            log.info("[LOCK-SUCCESS] -> Lock acquired for {} minutes", 10);
            return true;
        } else {
            log.warn("[LOCK-FAILED] -> Seat {} already held by another user", seatNumber);
            throw new SeatAlreadyLockedException(seatNumber);
        }
    }

    /**
     * Fallback for acquireSeatLock.
     * In a real-world system, if the locking mechanism is down, we MUST fail
     * the request to ensure two people don't book the same seat.
     */
    public boolean fallbackLock(Long flightId, String seatNumber, String userId, Throwable t) {
        log.error("Locking service (Redis) is unavailable for seat {} on flight {}. Reason: {}",
                seatNumber, flightId, t.getMessage());

        // We throw a custom RuntimeException that the GlobalExceptionHandler can turn into a 503 Service Unavailable
        throw new RuntimeException("The seat reservation system is temporarily unavailable. Please try again in a moment.");
    }

    @CircuitBreaker(name = "redisLock", fallbackMethod = "fallbackIsLocked")
    public boolean isSeatLocked(Long flightId, String seatNumber) {
        String lockKey = generateLockKey(flightId, seatNumber);
        return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
    }

    // Fallback for isSeatLocked: If Redis is down, we assume it's NOT locked
    // to allow the UI to function, but the acquireSeatLock will still catch issues.
    public boolean fallbackIsLocked(Long flightId, String seatNumber, Throwable t) {
        log.warn("Redis down while checking lock for seat {}. Defaulting to false.", seatNumber);
        return false;
    }

    public void releaseSeatLock(Long flightId, String seatNumber) {
        String lockKey = generateLockKey(flightId, seatNumber);
        log.info("Releasing lock for key: {}", lockKey);
        try {
            redisTemplate.delete(lockKey);
        } catch (Exception e) {
            log.error("Failed to release lock in Redis for key {}. It will expire automatically.", lockKey);
        }
    }

    private String generateLockKey(Long flightId, String seatNumber) {
        return LOCK_PREFIX + flightId + "::" + seatNumber;
    }
}
