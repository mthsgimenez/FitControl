package com.mthsgimenez.fitcontrol.emailverification;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public class EmailVerificationStore {

    private final RedisTemplate<String, String> redisTemplate;
    private final String keyPrefix = "email_verification:";
    private final Duration verificationTTL = Duration.ofMinutes(5);

    public EmailVerificationStore(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void store(String email, String code) {
        String redisKey = keyPrefix + email;
        redisTemplate.opsForValue().set(redisKey, code, verificationTTL);
    }

    public Optional<String> get(String email) {
        String redisKey = keyPrefix + email;

        String code = redisTemplate.opsForValue().get(redisKey);
        if (code == null) {
            return Optional.empty();
        }

        return Optional.of(code);
    }

    public void delete(String email) {
        String redisKey = keyPrefix + email;
        redisTemplate.delete(redisKey);
    }
}
