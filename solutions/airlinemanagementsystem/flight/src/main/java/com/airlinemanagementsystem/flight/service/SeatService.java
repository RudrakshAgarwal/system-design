package com.airlinemanagementsystem.flight.service;

import com.airlinemanagementsystem.flight.dto.SeatResponseDTO;
import com.airlinemanagementsystem.flight.entity.Seat;
import com.airlinemanagementsystem.flight.entity.SeatStatus;
import com.airlinemanagementsystem.flight.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final SeatLockService seatLockService;

    /**
     * Fetches all seats for a flight and overlays Redis lock info.
     */
    @Transactional(readOnly = true)
    public List<SeatResponseDTO> getSeatsByFlight(Long flightId) {
        return seatRepository.findByFlightId(flightId).stream()
                .map(seat -> {
                    SeatStatus currentStatus = seat.getStatus();

                    // Logic: If DB says AVAILABLE but Redis has a lock, return LOCKED
                    if (currentStatus == SeatStatus.AVAILABLE &&
                            seatLockService.isSeatLocked(flightId, seat.getSeatNumber())) {
                        currentStatus = SeatStatus.LOCKED;
                    }

                    return SeatResponseDTO.builder()
                            .seatId(seat.getSeatId())
                            .seatNumber(seat.getSeatNumber())
                            .seatType(seat.getSeatType())
                            .status(currentStatus)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Finalizes the seat status in the DB once the Booking Service confirms payment.
     */
    @Transactional
    public void confirmSeatBooking(Long flightId, String seatNumber) {
        Seat seat = seatRepository.findByFlightIdAndSeatNumber(flightId, seatNumber)
                .orElseThrow(() -> new RuntimeException("Seat not found"));

        seat.setStatus(SeatStatus.BOOKED);
        seatRepository.save(seat);

        // Remove the temporary Redis lock now that it's permanent in MySQL
        seatLockService.releaseSeatLock(flightId, seatNumber);
    }
}
