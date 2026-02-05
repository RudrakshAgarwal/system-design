package com.airlinemanagementsystem.booking.service;

import com.airlinemanagementsystem.booking.client.FlightServiceClient;
import com.airlinemanagementsystem.booking.dto.BookingRequest;
import com.airlinemanagementsystem.booking.dto.LuggageDTO;
import com.airlinemanagementsystem.booking.dto.PassengerDTO;
import com.airlinemanagementsystem.booking.entity.Booking;
import com.airlinemanagementsystem.booking.entity.BookingStatus;
import com.airlinemanagementsystem.booking.entity.Luggage;
import com.airlinemanagementsystem.booking.entity.Passenger;
import com.airlinemanagementsystem.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public Booking getLatestBooking(String userId) {
        return bookingRepository.findTopByUserIdOrderByBookingDateDesc(userId);
    }

    /**
     * The Master Method: Locks Seats -> Creates Booking -> Saves Data
     * Returns Booking ID on success, throws Exception on failure.
     */
    @Transactional
    public Long processBooking(BookingRequest request) {
        log.info("Service: Processing booking for Flight {}...", request.getFlightId());

        // 1. ACQUIRE LOCKS (Distributed Lock via Redis)
        // If this fails, we throw exception immediately to stop the flow.
        for (PassengerDTO p : request.getPassengers()) {
            boolean isLocked = flightServiceClient.lockSeat(
                    request.getFlightId(),
                    p.getSeatNumber(),
                    request.getUserId()
            );

            if (!isLocked) {
                throw new RuntimeException("Seat " + p.getSeatNumber() + " is already occupied.");
            }
        }

        // 2. BUILD THE ENTITY GRAPH (Booking -> Passengers -> Luggage)
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
                    .booking(booking) // Link Parent
                    .luggageList(new ArrayList<>())
                    .build();

            // 2a. Handle Luggage Mapping (The missing piece in your code)
            if (pDto.getLuggage() != null) {
                for (LuggageDTO lDto : pDto.getLuggage()) {
                    Luggage luggage = Luggage.builder()
                            .type(com.airlinemanagementsystem.booking.entity.LuggageType.valueOf(lDto.getType())) // CHECKED/CABIN
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

        // 3. SAVE TO DB (Cascading save will insert Booking, Passengers, and Luggage)
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Service: Booking saved successfully. ID: {}", savedBooking.getBookingId());

        return savedBooking.getBookingId();
    }

    // --- Helper Methods ---

    private Double calculateTotalAmount(BookingRequest request) {
        double baseFare = 150.0; // Mock base fare
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
        return 0.0; // CABIN is free
    }
}
