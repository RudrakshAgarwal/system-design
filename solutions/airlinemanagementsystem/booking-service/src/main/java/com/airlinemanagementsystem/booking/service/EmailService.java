package com.airlinemanagementsystem.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendBookingFailureEmail(String toEmail, String userName, String reason) {
        log.info("Sending failure email to {}", toEmail);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@airlinesystem.com");
        message.setTo(toEmail);
        message.setSubject("Booking Failed - Airline Management System");
        message.setText("Dear " + userName + ",\n\n" +
                "We regret to inform you that your booking request could not be processed.\n" +
                "Reason: " + reason + "\n\n" +
                "Any deducted amount will be refunded within 3-5 business days.\n\n" +
                "Regards,\nAirline Team");

        try {
            mailSender.send(message);
            log.info("Failure email sent successfully.");
        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage());
        }
    }

    public void sendBookingSuccessEmail(String toEmail, String userName, Long bookingId) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Booking Confirmed! - #" + bookingId);
        message.setText("Dear " + userName + ",\n\nYour booking is confirmed! Booking ID: " + bookingId);
        mailSender.send(message);
    }
}