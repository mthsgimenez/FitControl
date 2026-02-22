package com.mthsgimenez.fitcontrol.passwordtoken;

import com.mthsgimenez.fitcontrol.auth.refreshtokens.InvalidTokenException;
import com.mthsgimenez.fitcontrol.auth.refreshtokens.RefreshTokenService;
import com.mthsgimenez.fitcontrol.infra.email.EmailMessage;
import com.mthsgimenez.fitcontrol.infra.email.EmailService;
import com.mthsgimenez.fitcontrol.user.User;
import com.mthsgimenez.fitcontrol.user.UserRepository;
import com.mthsgimenez.fitcontrol.util.DeterministicHashUtil;
import com.mthsgimenez.fitcontrol.util.RandomStringUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordTokenService {

    private final EmailService emailService;
    private final RandomStringUtil randomStringUtil;
    private final DeterministicHashUtil deterministicHashUtil;
    private final MessageSource messageSource;
    private final String frontendUrl;
    private final PasswordTokenStore passwordTokenStore;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    public PasswordTokenService(
            @Value("${app.infra.frontend-url}") String frontendUrl,
            EmailService emailService,
            RandomStringUtil randomStringUtil,
            DeterministicHashUtil deterministicHashUtil,
            MessageSource messageSource,
            PasswordTokenStore passwordTokenStore,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            RefreshTokenService refreshTokenService
    ) {
        this.emailService = emailService;
        this.randomStringUtil = randomStringUtil;
        this.frontendUrl = frontendUrl;
        this.deterministicHashUtil = deterministicHashUtil;
        this.messageSource = messageSource;
        this.passwordTokenStore = passwordTokenStore;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
    }

    public void sendPasswordTokenEmail(User user, EmailType emailType) {
        String recoveryToken = randomStringUtil.getRandomString();
        String recoveryUrl = String.format("%s/recover-password?token=%s", frontendUrl, recoveryToken);
        String hashedToken = deterministicHashUtil.hashString(recoveryToken);

        passwordTokenStore.storePasswordToken(hashedToken, user.getId());

        String subject;
        String text;
        switch (emailType) {
            case PASSWORD_RESET:
                subject = messageSource.getMessage("email.password-recovery.subject", null, LocaleContextHolder.getLocale());
                text = messageSource.getMessage("email.password-recovery.text", new Object[]{recoveryUrl}, LocaleContextHolder.getLocale());
                break;
            case ONBOARDING:
                subject = messageSource.getMessage("email.onboarding.subject", null, LocaleContextHolder.getLocale());
                text = messageSource.getMessage("email.onboarding.text", new Object[]{recoveryUrl}, LocaleContextHolder.getLocale());
                break;
            default:
                throw new IllegalArgumentException("Invalid email type");
        }

        EmailMessage message = new EmailMessage(
                user.getEmail(),
                subject,
                text
        );
        emailService.sendTextEmail(message);
    }

    public void setNewPassword(String hashedToken, String newPassword) {
        Integer userId = passwordTokenStore.getPasswordTokenUserId(hashedToken)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired password token"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired password token"));

        String passwordHash = passwordEncoder.encode(newPassword);
        user.setPasswordHash(passwordHash);
        userRepository.save(user);

        passwordTokenStore.deletePasswordToken(hashedToken);
        refreshTokenService.revokeRefreshTokensFromUser(user.getId());
    }
}
