package com.mthsgimenez.fitcontrol.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

@Component
public class OTPUtil {
    private final SecureRandom rnd = new SecureRandom();
    private final PasswordEncoder passwordEncoder;
    private final String secretKey;
    private final RedisTemplate<String, String> redisTemplate;

    public OTPUtil(
            RedisTemplate<String, String> redisTemplate,
            PasswordEncoder passwordEncoder,
            @Value("${app.hmac.secret}") String secretKey
    ) {
        this.redisTemplate = redisTemplate;
        this.passwordEncoder = passwordEncoder;
        this.secretKey = secretKey;
    }

    private String generateOtp() {
        StringBuilder otp = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            otp.append(rnd.nextInt(10));
        }

        return otp.toString();
    }

    private String hashIdentifier(String subject) throws Exception {
        String HMAC_ALGORITHM = "HmacSHA256";

        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
        mac.init(secretKeySpec);

        byte[] digest = mac.doFinal(subject.toLowerCase().trim().getBytes(StandardCharsets.UTF_8));

        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }

    public String generateAndStoreOTP(String otpIdentifier) throws RuntimeException {
        String otp = generateOtp();

        String hashedOtp = passwordEncoder.encode(otp);
        String hashedIdentifier;
        try {
            hashedIdentifier = hashIdentifier(otpIdentifier);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing identifier", e);
        }

        redisTemplate.opsForValue().set("otp:" + hashedIdentifier, hashedOtp, Duration.ofMinutes(5));

        return otp;
    }

    public boolean verifyOtp(String otpIdentifier, String otp) {
        String hashedIdentifier;
        try {
            hashedIdentifier = hashIdentifier(otpIdentifier);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing identifier", e);
        }

        String redisKey = "otp:" + hashedIdentifier;
        String hashedOtp = (String) redisTemplate.opsForValue().get(redisKey);

        if (hashedOtp == null) {
            return false;
        }

        if (passwordEncoder.matches(otp, hashedOtp)) {
            redisTemplate.delete(redisKey);
            return true;
        }

        return false;
    }
}
