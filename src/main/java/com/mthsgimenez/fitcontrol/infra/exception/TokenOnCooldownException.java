package com.mthsgimenez.fitcontrol.infra.exception;

import lombok.Getter;

public class TokenOnCooldownException extends RuntimeException {
    @Getter
    private final Long cooldownRemaining;

    public TokenOnCooldownException(String message, Long cooldownRemaining) {
        super(message);
        this.cooldownRemaining = cooldownRemaining;
    }
}
