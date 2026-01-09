package com.mthsgimenez.fitcontrol.util;

import lombok.Getter;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Getter
public class OTP {
    private final String code;
    private final Instant issueDate;
    private final Instant expirationDate;

    public OTP(String code) {
        this.code = code;
        this.issueDate = Instant.now();
        this.expirationDate = issueDate.plus(Duration.of(5, ChronoUnit.MINUTES));
    }
}
