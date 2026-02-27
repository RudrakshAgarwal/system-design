package com.airlinemanagementsystem.payment.service;

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

    public void sendPaymentStatusEmail(String toEmail, String status, Long bookingId, String amount) {
        if(toEmail == null || toEmail.isEmpty()) return;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Payment Update: " + status + " - Booking #" + bookingId);

        String body = "Dear Customer,\n\n";

        switch (status) {
            case "SUCCESS":
                body += "We have received your payment of INR " + amount + ".\n" +
                        "Your booking is confirmed.";
                break;
            case "FAILED":
                body += "Your payment of INR " + amount + " has FAILED.\n" +
                        "If money was deducted, it will be refunded in 3-5 days.";
                break;
            case "PENDING":
                body += "Your payment is currently processing. We will notify you once confirmed.";
                break;
        }

        body += "\n\nRegards,\nAirline Team";
        message.setText(body);

        try {
            mailSender.send(message);
            log.info("Email sent to {} with status {}", toEmail, status);
        } catch (Exception e) {
            log.error("Failed to send email", e);
        }
    }
}
