package com.airlinemanagementsystem.booking.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "flight-service", url = "${FLIGHT_SERVICE_URL:http://localhost:8081}")
public interface FlightServiceClient {
    @PostMapping("/api/v1/seats/lock")
    Boolean lockSeat(@RequestParam("flightId") Long flightId,
                     @RequestParam("seatNumber") String seatNumber,
                     @RequestParam("userId") String userId);

    @PostMapping("/api/v1/seats/confirm")
    void confirmSeat(@RequestParam("flightId") Long flightId,
                     @RequestParam("seatNumber") String seatNumber,
                     @RequestParam("userId") String userId);

    @PostMapping("/api/v1/flights/{flightId}/seats/{seatNumber}/unlock")
    boolean unlockSeat(@PathVariable Long flightId, @PathVariable String seatNumber);
}