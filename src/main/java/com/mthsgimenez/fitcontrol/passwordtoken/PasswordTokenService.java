package com.mthsgimenez.fitcontrol.passwordtoken;

import com.mthsgimenez.fitcontrol.auth.refreshtokens.InvalidTokenException;
import com.mthsgimenez.fitcontrol.auth.refreshtokens.RandomTokenUtil;
import com.mthsgimenez.fitcontrol.auth.refreshtokens.RefreshTokenService;
import com.mthsgimenez.fitcontrol.infra.email.EmailMessage;
import com.mthsgimenez.fitcontrol.infra.email.EmailService;
import com.mthsgimenez.fitcontrol.user.User;
import com.mthsgimenez.fitcontrol.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// TODO: remover dependencia de randomTokenUtil do package auth
@Service
public class PasswordTokenService {

    private final EmailService emailService;
    private final RandomTokenUtil randomTokenUtil;
    private final MessageSource messageSource;
    private final String frontendUrl;
    private final PasswordTokenStore passwordTokenStore;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    public PasswordTokenService(
            @Value("${app.infra.frontend-url}") String frontendUrl,
            EmailService emailService,
            RandomTokenUtil randomTokenUtil,
            MessageSource messageSource,
            PasswordTokenStore passwordTokenStore,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            RefreshTokenService refreshTokenService
    ) {
        this.emailService = emailService;
        this.randomTokenUtil = randomTokenUtil;
        this.frontendUrl = frontendUrl;
        this.messageSource = messageSource;
        this.passwordTokenStore = passwordTokenStore;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
    }

    public void sendPasswordTokenEmail(User user, EmailType emailType) {
        String recoveryToken = randomTokenUtil.getRandomToken();
        String recoveryUrl = String.format("%s/recover-password?token=%s", frontendUrl, recoveryToken);
        String hashedToken = randomTokenUtil.hashToken(recoveryToken);

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
