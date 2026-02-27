package com.airlinemanagementsystem.flight.service;

import com.airlinemanagementsystem.flight.dto.SeatResponseDTO;
import com.airlinemanagementsystem.flight.entity.Seat;
import com.airlinemanagementsystem.flight.entity.SeatStatus;
import com.airlinemanagementsystem.flight.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final SeatLockService seatLockService; // Your existing service

    @Transactional(readOnly = true)
    public List<SeatResponseDTO> getSeatsByFlight(Long flightId) {
        List<Seat> seats = seatRepository.findByFlightId(flightId);
        return seats.stream()
                .map(seat -> {
                    SeatStatus status = seat.getStatus();

                    if (status == SeatStatus.AVAILABLE) {
                        if (seatLockService.isSeatLocked(flightId, seat.getSeatNumber())) {
                            status = SeatStatus.LOCKED;
                        }
                    }

                    return SeatResponseDTO.builder()
                            .seatId(seat.getSeatId())
                            .seatNumber(seat.getSeatNumber())
                            .seatType(seat.getSeatType())
                            .status(status)
                            .price(seat.getPrice())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void confirmSeatBooking(Long flightId, String seatNumber) {
        log.info("Confirming seat: Flight {} Seat {}", flightId, seatNumber);

        Seat seat = seatRepository.findByFlightIdAndSeatNumber(flightId, seatNumber)
                .orElseThrow(() -> new RuntimeException("Seat not found: " + seatNumber));

        if (seat.getStatus() == SeatStatus.BOOKED) {
            return;
        }

        seat.setStatus(SeatStatus.BOOKED);
        seatRepository.save(seat);

        seatLockService.releaseSeatLock(flightId, seatNumber);
    }
}
