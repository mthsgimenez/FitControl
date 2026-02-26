package com.mthsgimenez.fitcontrol.user;

import com.mthsgimenez.fitcontrol.passwordtoken.EmailType;
import com.mthsgimenez.fitcontrol.passwordtoken.PasswordTokenService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class UserEventListener {

    private final PasswordTokenService passwordTokenService;

    public UserEventListener(PasswordTokenService passwordTokenService) {
        this.passwordTokenService = passwordTokenService;
    }

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserCreatedEvent(UserCreatedEvent event) {
        passwordTokenService.sendPasswordTokenEmail(event.user().getEmail(), EmailType.ONBOARDING);
    }
}
