package com.airlinemanagementsystem.booking.listener;

import com.airlinemanagementsystem.booking.client.FlightServiceClient;
import com.airlinemanagementsystem.booking.dto.BookingPlacedEvent;
import com.airlinemanagementsystem.booking.dto.PaymentEventDto;
import com.airlinemanagementsystem.booking.entity.Booking;
import com.airlinemanagementsystem.booking.entity.BookingStatus;
import com.airlinemanagementsystem.booking.repository.BookingRepository;
import com.airlinemanagementsystem.booking.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final BookingRepository bookingRepository;
    private final FlightServiceClient flightServiceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "payment-events", groupId = "booking-payment-group")
    @Transactional
    public void handlePaymentEvent(PaymentEventDto event) {
        log.info("Kafka: Processing Payment Event for Booking ID: {} | Status: {}", event.getBookingId(), event.getStatus());

        Booking booking = bookingRepository.findById(event.getBookingId()).orElse(null);
        if (booking == null) return;

        if (booking.getStatus() == BookingStatus.CONFIRMED) return;

        if ("SUCCESS".equals(event.getStatus())) {
            try {
                booking.getPassengers().forEach(p ->
                        flightServiceClient.confirmSeat(booking.getFlightId(), p.getSeatNumber(), booking.getUserId())
                );

                booking.setStatus(BookingStatus.CONFIRMED);
                bookingRepository.save(booking);

                BookingPlacedEvent notificationEvent = new BookingPlacedEvent(
                        booking.getBookingReference(),
                        booking.getPassengers().get(0).getEmail(),
                        booking.getPassengers().get(0).getFirstName(),
                        booking.getPassengers().get(0).getLastName(),
                        "FL-" + booking.getFlightId(),
                        booking.getBookingDate().toString(),
                        "Unknown", "Unknown"
                );

                kafkaTemplate.send("booking-events", notificationEvent);
                log.info("✅ Booking Confirmed. Notification Sent.");

            } catch (Exception e) {
                log.error("Confirmation Failed. Rolling back! Critical Reason: {}", e.getMessage(), e);
                handleSagaCompensation(booking);
            }
        } else {
            handleSagaCompensation(booking);
        }
    }

    private void handleSagaCompensation(Booking booking) {
        log.warn("💰 Payment Failed/Compensating for Booking {}", booking.getBookingId());
        booking.getPassengers().forEach(p -> {
            try {
                flightServiceClient.unlockSeat(booking.getFlightId(), p.getSeatNumber());
            } catch (Exception e) {
                log.error("Failed to unlock seat {}", p.getSeatNumber());
            }
        });

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }
}