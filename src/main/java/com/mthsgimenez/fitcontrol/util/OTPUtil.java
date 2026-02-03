package com.mthsgimenez.fitcontrol.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class OTPUtil {
    private final SecureRandom rnd = new SecureRandom();

    public String generateOtp() {
        StringBuilder otp = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            otp.append(rnd.nextInt(10));
        }

        return otp.toString();
    }
}
