package com.airlinemanagementsystem.booking.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "flight-service", url = "http://localhost:8081/api/v1/seats")
public interface FlightServiceClient {
    /**
     * Calls POST http://localhost:8081/api/v1/seats/lock
     */
    @PostMapping("/lock")
    Boolean lockSeat(@RequestParam("flightId") Long flightId,
                     @RequestParam("seatNumber") String seatNumber,
                     @RequestParam("userId") String userId);

    /**
     * Calls POST http://localhost:8081/api/v1/seats/confirm
     */
    @PostMapping("/confirm")
    void confirmSeat(@RequestParam("flightId") Long flightId,
                     @RequestParam("seatNumber") String seatNumber);

    @PostMapping("/api/v1/flights/{flightId}/seats/{seatNumber}/unlock")
    boolean unlockSeat(@PathVariable Long flightId, @PathVariable String seatNumber);
}