package com.mthsgimenez.fitcontrol.util;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;

public class OTPUtil {
    private final static SecureRandom rnd = new SecureRandom();

    private final static ConcurrentHashMap<String, OTP> OTPStorage = new ConcurrentHashMap<String, OTP>();

    public static OTP generateOTP() {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            result.append(rnd.nextInt(10));
        }

        return new OTP(result.toString());
    }

    public static void saveOTP(String key, OTP code) {
        OTPStorage.put(key, code);
    }

    public static boolean verifyOTP(String key, String code) {
        OTP otp = OTPStorage.get(key);
        if (otp == null) return false;

        Instant now = Instant.now();
        return code.equals(otp.getCode())
                && now.isBefore(otp.getExpirationDate())
                && now.isAfter(otp.getIssueDate());
    }
}
