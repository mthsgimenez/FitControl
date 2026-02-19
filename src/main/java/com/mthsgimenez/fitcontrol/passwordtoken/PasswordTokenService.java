package com.mthsgimenez.fitcontrol.passwordtoken;

import com.mthsgimenez.fitcontrol.auth.refreshtokens.RandomTokenUtil;
import com.mthsgimenez.fitcontrol.infra.email.EmailMessage;
import com.mthsgimenez.fitcontrol.infra.email.EmailService;
import com.mthsgimenez.fitcontrol.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

// TODO: remover dependencia de randomTokenUtil do package auth
public class PasswordTokenService {

    private final EmailService emailService;
    private final RandomTokenUtil randomTokenUtil;
    private final MessageSource messageSource;
    private final String frontendUrl;
    private final PasswordTokenStore passwordTokenStore;

    public PasswordTokenService(
            @Value("${app.infra.frontend-url}") String frontendUrl,
            EmailService emailService,
            RandomTokenUtil randomTokenUtil,
            MessageSource messageSource,
            PasswordTokenStore passwordTokenStore
    ) {
        this.emailService = emailService;
        this.randomTokenUtil = randomTokenUtil;
        this.frontendUrl = frontendUrl;
        this.messageSource = messageSource;
        this.passwordTokenStore = passwordTokenStore;
    }

    public void sendRecoveryPasswordEmail(User user) {
        String recoveryToken = randomTokenUtil.getRandomToken();
        String recoveryUrl = String.format("%s/recover-password?token=%s", frontendUrl, recoveryToken);
        String hashedToken = randomTokenUtil.hashToken(recoveryToken);

        passwordTokenStore.storePasswordToken(hashedToken, user.getId());

        EmailMessage message = new EmailMessage(
                user.getEmail(),
                messageSource.getMessage("email.password-recovery.subject", null, LocaleContextHolder.getLocale()),
                messageSource.getMessage("email.password-recovery.text", new Object[]{recoveryUrl}, LocaleContextHolder.getLocale())
        );
        emailService.sendTextEmail(message);
    }
}
