package com.mthsgimenez.fitcontrol.userpassword;

import com.mthsgimenez.fitcontrol.auth.refreshtokens.RandomTokenUtil;
import com.mthsgimenez.fitcontrol.infra.cache.CacheService;
import com.mthsgimenez.fitcontrol.infra.email.EmailMessage;
import com.mthsgimenez.fitcontrol.infra.email.EmailService;
import com.mthsgimenez.fitcontrol.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.Duration;

// TODO: remover dependencia de randomTokenUtil do package auth
// TODO: criar store ao invés de utilizar cacheService
public class PasswordRecoveryService {

    private final EmailService emailService;
    private final RandomTokenUtil randomTokenUtil;
    private final CacheService cacheService;
    private final MessageSource messageSource;
    private final String frontendUrl;
    private final String cacheKeyPrefix = "password_recovery:";

    public PasswordRecoveryService(
            @Value("${app.infra.frontend-url}") String frontendUrl,
            EmailService emailService,
            RandomTokenUtil randomTokenUtil,
            CacheService cacheService,
            MessageSource messageSource
            ) {
        this.emailService = emailService;
        this.randomTokenUtil = randomTokenUtil;
        this.cacheService = cacheService;
        this.frontendUrl = frontendUrl;
        this.messageSource = messageSource;
    }

    public void sendRecoveryPasswordEmail(User user) {
        String recoveryToken = randomTokenUtil.getRandomToken();
        String recoveryUrl = String.format("%s/recover-password?token=%s", frontendUrl, recoveryToken);
        String cacheKey = cacheKeyPrefix + randomTokenUtil.hashToken(recoveryToken);

        cacheService.set(cacheKey, user.getId(), Duration.ofMinutes(60));

        EmailMessage message = new EmailMessage(
                user.getEmail(),
                messageSource.getMessage("email.password-recovery.subject", null, LocaleContextHolder.getLocale()),
                messageSource.getMessage("email.password-recovery.text", new Object[]{recoveryUrl}, LocaleContextHolder.getLocale())
        );
        emailService.sendTextEmail(message);
    }
}
