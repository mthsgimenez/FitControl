package com.mthsgimenez.fitcontrol.emailverification;

import com.mthsgimenez.fitcontrol.infra.email.EmailMessage;
import com.mthsgimenez.fitcontrol.infra.email.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailVerificationService {

    private final EmailVerificationStore emailVerificationStore;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final OTPUtil otpUtil;
    private final MessageSource messageSource;

    public EmailVerificationService(
            EmailVerificationStore emailVerificationStore,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            OTPUtil otpUtil,
            MessageSource messageSource
    ) {
        this.emailVerificationStore = emailVerificationStore;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.otpUtil = otpUtil;
        this.messageSource = messageSource;
    }

    public void sendVerificationEmail(String email) {
        String code = otpUtil.generateOtp();
        String hashedCode = passwordEncoder.encode(code);

        emailVerificationStore.store(email, hashedCode);

        EmailMessage emailMessage = new EmailMessage(
                email,
                messageSource.getMessage("email.verification-code.subject", null, LocaleContextHolder.getLocale()),
                messageSource.getMessage("email.verification-code.text", new Object[]{code}, LocaleContextHolder.getLocale())
        );
        emailService.sendTextEmail(emailMessage);

        log.info("Email verification has been sent to {}", email);
    }

    public void verifyEmail(String email, String code) throws EmailNotVerifiedException {
        String hashedCode = emailVerificationStore.get(email)
                .orElseThrow(() -> new EmailNotVerifiedException(
                        messageSource.getMessage("exception.email-not-verified-exception", null, LocaleContextHolder.getLocale())
                ));

        if (!passwordEncoder.matches(code, hashedCode)) {
            throw new EmailNotVerifiedException(
                    messageSource.getMessage("exception.email-not-verified-exception", null, LocaleContextHolder.getLocale())
            );
        }
    }

    public void deleteVerificationForEmail(String email) {
        emailVerificationStore.delete(email);
    }
}
