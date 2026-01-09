package com.mthsgimenez.fitcontrol.util;

import lombok.Getter;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class OTPUtil {
    @Getter
    private final String OTP;
    @Getter
    private final Instant expirationDate;
    @Getter
    private final Instant issuedAt;

    private static final SecureRandom rnd = new SecureRandom();

    public OTPUtil() {
        this.OTP = generateOTP();
        this.issuedAt = Instant.now();
        this.expirationDate = issuedAt.plus(Duration.of(5, ChronoUnit.MINUTES));
    }

    private static String generateOTP() {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            result.append(rnd.nextInt(10));
        }

        return result.toString();
    }
}
