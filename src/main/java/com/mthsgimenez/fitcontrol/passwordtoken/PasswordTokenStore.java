package com.mthsgimenez.fitcontrol.passwordtoken;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Optional;

@Slf4j
public class PasswordTokenStore {

    private final RedisTemplate<String, String> redisTemplate;
    private final String keyPrefix = "password_token:";

    public PasswordTokenStore(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Optional<Integer> getPasswordTokenUserId(String hashedToken) {
        String key = keyPrefix + hashedToken;
        String userId = redisTemplate.opsForValue().get(key);

        try {
            return Optional.ofNullable(userId).map(Integer::parseInt);
        } catch (NumberFormatException e) {
            log.error("Error parsing password token (userId may be corrupted): ", e);
            return Optional.empty();
        }
    }

    public void storePasswordToken(String hashedToken, Integer userId) {
        String key = keyPrefix + hashedToken;
        redisTemplate.opsForValue().set(key, userId.toString(), Duration.ofMinutes(60));
    }

    public void deletePasswordToken(String hashedToken) {
        String key = keyPrefix + hashedToken;
        redisTemplate.delete(key);
    }
}
