package com.airlinemanagementsystem.booking.service;

import com.airlinemanagementsystem.booking.client.FlightServiceClient;
import com.airlinemanagementsystem.booking.dto.BookingRequest;
import com.airlinemanagementsystem.booking.dto.LuggageDTO;
import com.airlinemanagementsystem.booking.dto.PassengerDTO;
import com.airlinemanagementsystem.booking.dto.PaymentRequestDto;
import com.airlinemanagementsystem.booking.entity.Booking;
import com.airlinemanagementsystem.booking.entity.BookingStatus;
import com.airlinemanagementsystem.booking.entity.Luggage;
import com.airlinemanagementsystem.booking.entity.Passenger;
import com.airlinemanagementsystem.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final FlightServiceClient flightServiceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public Booking getLatestBooking(String userId) {
        return bookingRepository.findTopByUserIdOrderByBookingDateDesc(userId);
    }

    @Transactional
    public Long processBooking(BookingRequest request) {
        log.info("Service: Processing booking for Flight {}...", request.getFlightId());

        List<String> lockedSeats = new ArrayList<>();

        try {
            for (PassengerDTO p : request.getPassengers()) {
                boolean isLocked = flightServiceClient.lockSeat(
                        request.getFlightId(),
                        p.getSeatNumber(),
                        request.getUserId()
                );

                if (!isLocked) {
                    throw new RuntimeException("Seat " + p.getSeatNumber() + " is already occupied.");
                }

                lockedSeats.add(p.getSeatNumber());
            }

            Booking booking = Booking.builder()
                    .flightId(request.getFlightId())
                    .userId(request.getUserId())
                    .status(BookingStatus.PENDING)
                    .bookingDate(LocalDateTime.now())
                    .totalAmount(calculateTotalAmount(request))
                    .build();

            List<Passenger> passengers = new ArrayList<>();

            for (PassengerDTO pDto : request.getPassengers()) {
                Passenger passenger = Passenger.builder()
                        .firstName(pDto.getFirstName())
                        .lastName(pDto.getLastName())
                        .email(pDto.getEmail())
                        .seatNumber(pDto.getSeatNumber())
                        .booking(booking)
                        .luggageList(new ArrayList<>())
                        .build();

                if (pDto.getLuggage() != null) {
                    for (LuggageDTO lDto : pDto.getLuggage()) {
                        Luggage luggage = Luggage.builder()
                                .type(com.airlinemanagementsystem.booking.entity.LuggageType.valueOf(lDto.getType()))
                                .weight(lDto.getWeight())
                                .price(calculateLuggagePrice(lDto.getType()))
                                .passenger(passenger)
                                .build();
                        passenger.addLuggage(luggage);
                    }
                }
                passengers.add(passenger);
            }

            booking.setPassengers(passengers);

            Booking savedBooking = bookingRepository.save(booking);
            log.info("Service: Booking saved PENDING. ID: {}", savedBooking.getBookingId());

            PaymentRequestDto paymentRequest = PaymentRequestDto.builder()
                    .bookingId(savedBooking.getBookingId())
                    .userId(savedBooking.getUserId())
                    .amount(savedBooking.getTotalAmount())
                    .currency("INR")
                    .build();

            kafkaTemplate.send("payment-request-topic", savedBooking.getBookingId().toString(), paymentRequest);
            log.info("Service: Payment Request sent for Booking ID: {}", savedBooking.getBookingId());

            return savedBooking.getBookingId();

        } catch (Exception e) {
            log.error("Booking failed! Initiating compensation rollback. Reason: {}", e.getMessage());

            // --- COMPENSATING TRANSACTION: Release only the seats we locked ---
            if (!lockedSeats.isEmpty()) {
                log.info("Rolling back locks for seats: {}", lockedSeats);
                for (String seatNum : lockedSeats) {
                    try {
                        flightServiceClient.unlockSeat(request.getFlightId(), seatNum);
                    } catch (Exception unlockEx) {
                        log.error("CRITICAL: Failed to rollback lock for seat {}. Manual intervention required.", seatNum);
                    }
                }
            }
            throw e;
        }
    }

    private Double calculateTotalAmount(BookingRequest request) {
        double baseFare = 150.0;
        double total = 0.0;
        for (PassengerDTO p : request.getPassengers()) {
            total += baseFare;
            if (p.getLuggage() != null) {
                for (LuggageDTO l : p.getLuggage()) {
                    total += calculateLuggagePrice(l.getType());
                }
            }
        }
        return total;
    }

    private double calculateLuggagePrice(String type) {
        if ("CHECKED".equals(type)) return 40.0;
        if ("OVERSIZED".equals(type)) return 100.0;
        return 0.0;
    }
}