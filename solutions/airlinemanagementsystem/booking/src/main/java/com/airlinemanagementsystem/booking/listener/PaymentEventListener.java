package com.airlinemanagementsystem.booking.listener;

import com.airlinemanagementsystem.booking.client.FlightServiceClient;
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
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final BookingRepository bookingRepository;
    private final FlightServiceClient flightServiceClient;
    private final EmailService emailService;

    @KafkaListener(topics = "payment-events", groupId = "booking-payment-group")
    @Transactional
    public void handlePaymentEvent(PaymentEventDto event) {
        log.info("Kafka: Processing Payment Event for Booking ID: {} | Status: {}", event.getBookingId(), event.getStatus());

        Booking booking = bookingRepository.findById(event.getBookingId())
                .orElse(null);

        if (booking == null) {
            log.error("CRITICAL: Received payment event for non-existent Booking ID: {}", event.getBookingId());
            return; // Stop processing to avoid NullPointerException
        }

        if (booking.getStatus() == BookingStatus.CONFIRMED && "SUCCESS".equals(event.getStatus())) {
            log.info("Booking {} is already CONFIRMED. Ignoring duplicate event.", booking.getBookingId());
            return;
        }

        if ("SUCCESS".equals(event.getStatus())) {
            try {
                log.info("Payment Success. Confirming seats with Flight Service...");

                booking.getPassengers().forEach(p ->
                        flightServiceClient.confirmSeat(booking.getFlightId(), p.getSeatNumber())
                );

                booking.setStatus(BookingStatus.CONFIRMED);
                bookingRepository.save(booking);

                if (!booking.getPassengers().isEmpty()) {
                    String userEmail = booking.getPassengers().get(0).getEmail();
                    String firstName = booking.getPassengers().get(0).getFirstName();
                    emailService.sendBookingSuccessEmail(userEmail, firstName, booking.getBookingId());
                }

                log.info("Booking {} Confirmed & Ticket Sent.", booking.getBookingId());

            } catch (Exception e) {
                log.error("CRITICAL: Payment Success but Seat Confirmation Failed! Initiating Refund.", e);
                handleSagaCompensation(booking);
            }
        }
        else if ("FAILED".equals(event.getStatus())) {
            log.warn("Payment Failed for Booking {}. Cancelling Booking.", booking.getBookingId());

            try {
                booking.getPassengers().forEach(p ->
                        flightServiceClient.unlockSeat(booking.getFlightId(), p.getSeatNumber())
                );
            } catch (Exception e) {
                log.error("Failed to unlock seats for failed booking {}. Manual intervention required.", booking.getBookingId());
            }

            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);

            if (!booking.getPassengers().isEmpty()) {
                emailService.sendBookingFailureEmail(
                        booking.getPassengers().get(0).getEmail(),
                        booking.getPassengers().get(0).getFirstName(),
                        "Payment transaction failed or was declined."
                );
            }
        }
    }

    private void handleSagaCompensation(Booking booking) {
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        log.info("SAGA: Sending Refund Request to Payment Service for Booking {}", booking.getBookingId());
        kafkaTemplate.send("booking-failure-topic", booking.getBookingId());

        if (!booking.getPassengers().isEmpty()) {
            emailService.sendBookingFailureEmail(
                    booking.getPassengers().get(0).getEmail(),
                    booking.getPassengers().get(0).getFirstName(),
                    "Payment received, but seat confirmation failed (Flight Full/Error). A full refund has been initiated."
            );
        }
    }
}