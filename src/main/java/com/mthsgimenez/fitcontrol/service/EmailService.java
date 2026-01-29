package com.mthsgimenez.fitcontrol.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String from;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.properties.mail.from}") String from
    ) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Async
    public void sendOTPEmail(String to, String OTP) throws MessagingException {
        String htmlBody = String.format("<h1>Seu código: %s</h1>", OTP);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper =
                new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("Código de verificação FitControl");
        helper.setText(htmlBody, true);
        helper.setFrom(from);

        mailSender.send(message);
    }
}