package com.mthsgimenez.fitcontrol.passwordtoken;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
public class PasswordTokenStore {

    private final RedisTemplate<String, String> redisTemplate;

    private final String tokenKeyPrefix = "password_token:";
    private final String emailTokensKeyTemplate = "email:%s:password_tokens";

    private final Duration expirationMinutes = Duration.ofMinutes(60);

    public PasswordTokenStore(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void storePasswordToken(String hashedToken, String email) {
        String tokenKey = tokenKeyPrefix + hashedToken;
        String emailTokensKey = emailTokensKeyTemplate.formatted(email);

        redisTemplate.opsForValue()
                .set(tokenKey, email, expirationMinutes);

        redisTemplate.opsForSet()
                .add(emailTokensKey, hashedToken);

        redisTemplate.expire(emailTokensKey, expirationMinutes);
    }

    public Optional<String> getPasswordTokenEmail(String hashedToken) {
        String tokenKey = tokenKeyPrefix + hashedToken;
        return Optional.ofNullable(redisTemplate.opsForValue().get(tokenKey));
    }

    public void deletePasswordToken(String hashedToken) {
        String tokenKey = tokenKeyPrefix + hashedToken;

        String email = redisTemplate.opsForValue().get(tokenKey);
        if (email != null) {
            String emailTokensKey = emailTokensKeyTemplate.formatted(email);
            redisTemplate.opsForSet().remove(emailTokensKey, hashedToken);
        }

        redisTemplate.delete(tokenKey);
    }

    public void revokePasswordTokens(String email) {
        String emailTokensKey = emailTokensKeyTemplate.formatted(email);
        Set<String> tokenHashes = redisTemplate.opsForSet().members(emailTokensKey);

        if (tokenHashes != null && !tokenHashes.isEmpty()) {
            for (String hash : tokenHashes) {
                String tokenKey = tokenKeyPrefix + hash;
                redisTemplate.delete(tokenKey);
            }
        }

        redisTemplate.delete(emailTokensKey);
    }
}
