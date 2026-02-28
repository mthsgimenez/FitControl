package com.mthsgimenez.fitcontrol.passwordtoken;

import com.mthsgimenez.fitcontrol.auth.refreshtokens.InvalidTokenException;
import com.mthsgimenez.fitcontrol.auth.refreshtokens.RefreshTokenService;
import com.mthsgimenez.fitcontrol.infra.email.EmailMessage;
import com.mthsgimenez.fitcontrol.infra.email.EmailService;
import com.mthsgimenez.fitcontrol.infra.exception.TokenOnCooldownException;
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
    private final String frontendEndpoint;
    private final PasswordTokenStore passwordTokenStore;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    public PasswordTokenService(
            @Value("${app.frontend.url}") String frontendUrl,
            @Value("${app.frontend.set-password-endpoint}") String frontendEndpoint,
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
        this.frontendEndpoint = frontendEndpoint;
        this.passwordTokenStore = passwordTokenStore;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
    }

    private String createAndStoreToken(String email) {
        Long cooldownRemaining = passwordTokenStore.getCooldownRemainingForEmail(email);

        if (cooldownRemaining > 0) {
            throw new TokenOnCooldownException("Email: " + email + " is on cooldown", cooldownRemaining);
        }
        passwordTokenStore.revokeTokenForEmail(email);

        String token = randomStringUtil.getRandomString();
        String hashedToken = deterministicHashUtil.hashString(token);

        passwordTokenStore.storePasswordToken(hashedToken, email);

        return token;
    }

    public void sendPasswordTokenEmail(String email, EmailType emailType) {
        String token = createAndStoreToken(email);

        String setPasswordLink = String.format("%s/%s?token=%s", frontendUrl, frontendEndpoint, token);

        String subject;
        String text;
        switch (emailType) {
            case PASSWORD_RESET:
                subject = messageSource.getMessage("email.password-recovery.subject", null, LocaleContextHolder.getLocale());
                text = messageSource.getMessage("email.password-recovery.text", new Object[]{setPasswordLink}, LocaleContextHolder.getLocale());
                break;
            case ONBOARDING:
                subject = messageSource.getMessage("email.onboarding.subject", null, LocaleContextHolder.getLocale());
                text = messageSource.getMessage("email.onboarding.text", new Object[]{setPasswordLink}, LocaleContextHolder.getLocale());
                break;
            default:
                throw new IllegalArgumentException("Invalid email type");
        }

        EmailMessage message = new EmailMessage(
                email,
                subject,
                text
        );
        emailService.sendTextEmail(message);
    }

    public void setNewPassword(String token, String newPassword) {
        String hashedToken = deterministicHashUtil.hashString(token);

        String email = passwordTokenStore.getEmailFromToken(hashedToken)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired password token"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired password token"));

        String passwordHash = passwordEncoder.encode(newPassword);
        user.setPasswordHash(passwordHash);
        userRepository.save(user);

        passwordTokenStore.revokeTokenForEmail(email);
        refreshTokenService.revokeRefreshTokensFromUser(user.getId());
    }
}
