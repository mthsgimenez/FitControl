package com.mthsgimenez.fitcontrol.passwordtoken;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Component
public class PasswordTokenStore {

    private final RedisTemplate<String, String> redisTemplate;

    private final String tokenKeyPrefix = "password_token:";
    private final String emailTokenKeyTemplate = "email:%s:password_token";
    private final String cooldownKeyTemplate = "email:%s:password_token:cooldownDuration";

    private final Duration expirationMinutes = Duration.ofMinutes(60);
    private final Duration cooldownDuration = Duration.ofSeconds(60);

    public PasswordTokenStore(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void storePasswordToken(String hashedToken, String email) {
        String tokenKey = tokenKeyPrefix + hashedToken;
        String emailTokenKey = emailTokenKeyTemplate.formatted(email);
        String cooldownKey = cooldownKeyTemplate.formatted(email);

        redisTemplate.opsForValue().set(tokenKey, email, expirationMinutes);
        redisTemplate.opsForValue().set(emailTokenKey, hashedToken, expirationMinutes);
        redisTemplate.opsForValue().set(cooldownKey, "1", cooldownDuration);
    }

    public Optional<String> getEmailFromToken(String hashedToken) {
        String tokenKey = tokenKeyPrefix + hashedToken;
        return Optional.ofNullable(redisTemplate.opsForValue().get(tokenKey));
    }

    public Boolean isEmailOnCooldown(String email) {
        String cooldownKey = cooldownKeyTemplate.formatted(email);
        String cooldown = redisTemplate.opsForValue().get(cooldownKey);
        return cooldown != null;
    }

    public Long getCooldownRemainingForEmail(String email) {
        String cooldownKey = cooldownKeyTemplate.formatted(email);

        return redisTemplate.getExpire(cooldownKey);
    }

    public void revokeTokenForEmail(String email) {
        String emailTokenKey = emailTokenKeyTemplate.formatted(email);
        String cooldownKey = cooldownKeyTemplate.formatted(email);

        String hashedToken = redisTemplate.opsForValue().get(emailTokenKey);
        if (hashedToken == null) {
            return;
        }

        String tokenKey = tokenKeyPrefix + hashedToken;
        redisTemplate.delete(tokenKey);
        redisTemplate.delete(emailTokenKey);
        redisTemplate.delete(cooldownKey);
    }
}
