package com.mthsgimenez.fitcontrol.auth.service;

import com.mthsgimenez.fitcontrol.auth.dto.EmailDTO;
import com.mthsgimenez.fitcontrol.auth.dto.EmailVerificationDTO;
import com.mthsgimenez.fitcontrol.auth.exception.EmailNotVerifiedException;
import com.mthsgimenez.fitcontrol.infra.cache.CacheService;
import com.mthsgimenez.fitcontrol.infra.email.EmailMessage;
import com.mthsgimenez.fitcontrol.infra.email.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.UUID;

@Service
@Slf4j
public class EmailVerificationService {

    private final CacheService cacheService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final OTPUtil otpUtil;
    private final MessageSource messageSource;
    private final ObjectMapper objectMapper;
    private final String cacheKeyPrefix = "email_verification:";

    public EmailVerificationService(
            CacheService cacheService,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            OTPUtil otpUtil,
            MessageSource messageSource, ObjectMapper objectMapper
    ) {
        this.cacheService = cacheService;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.otpUtil = otpUtil;
        this.messageSource = messageSource;
        this.objectMapper = objectMapper;
    }

    public UUID sendVerificationEmail(EmailDTO to) {
        String code = otpUtil.generateOtp();
        String hashedCode = passwordEncoder.encode(code);
        UUID verificationId = UUID.randomUUID();

        EmailVerificationDTO data = new EmailVerificationDTO(
                verificationId,
                to.email(),
                hashedCode
        );

        String cacheKey = cacheKeyPrefix + verificationId;
        cacheService.set(cacheKey, data, Duration.ofMinutes(5));

        EmailMessage emailMessage = new EmailMessage(
                to.email(),
                messageSource.getMessage("email.verification-code.subject", null, LocaleContextHolder.getLocale()),
                messageSource.getMessage("email.verification-code.text", new Object[]{code}, LocaleContextHolder.getLocale())
        );
        emailService.sendTextEmail(emailMessage);

        log.info("Email verification has been sent to {}", to);

        return verificationId;
    }

    public void verifyEmail(EmailVerificationDTO data) throws EmailNotVerifiedException {
        String cacheKey = cacheKeyPrefix + data.verificationId().toString();
        Object obj = cacheService.getObject(cacheKey);

        if (obj == null) {
            throw new EmailNotVerifiedException(
                    messageSource.getMessage("exception.email-not-verified-exception.not-found", null, LocaleContextHolder.getLocale())
            );
        }

        EmailVerificationDTO verificationData = objectMapper.convertValue(obj, EmailVerificationDTO.class);

        if (!data.email().equals(verificationData.email())) {
            throw new EmailNotVerifiedException(
                    messageSource.getMessage("exception.email-not-verified-exception.email-mismatch", null, LocaleContextHolder.getLocale())
            );
        }

        if (!passwordEncoder.matches(data.code(), verificationData.code())) {
            throw new EmailNotVerifiedException(
                    messageSource.getMessage("exception.email-not-verified-exception.invalid-code", null, LocaleContextHolder.getLocale())
            );
        }
    }
}
