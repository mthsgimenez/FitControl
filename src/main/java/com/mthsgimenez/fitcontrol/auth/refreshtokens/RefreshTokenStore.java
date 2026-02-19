package com.mthsgimenez.fitcontrol.auth.refreshtokens;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
@Slf4j
public class RefreshTokenStore {

    private final RedisTemplate<String, String> redisTemplate;
    private final String tokenKeyPrefix = "refresh_token:";
    private final String userTokensKeyTemplate = "user:%d:refresh_tokens";
    private final Long expirationMinutes;

    public RefreshTokenStore(
            RedisTemplate<String, String> redisTemplate,
            @Value("${app.refresh_token.expiration_minutes}") Long expirationMinutes
    ) {
        this.redisTemplate = redisTemplate;
        this.expirationMinutes = expirationMinutes;
    }

    public void storeRefreshToken(String hashedRefreshToken, Integer userId) {
        String tokenKey = tokenKeyPrefix + hashedRefreshToken;
        String userTokensKey = userTokensKeyTemplate.formatted(userId);

        redisTemplate.opsForValue().set(tokenKey, hashedRefreshToken, expirationMinutes);
        redisTemplate.opsForSet().add(userTokensKey, hashedRefreshToken);
    }

    public Optional<Integer> getRefreshTokenUserId(String hashedRefreshToken) {
        String tokenKey = tokenKeyPrefix + hashedRefreshToken;

        String obj = redisTemplate.opsForValue().get(tokenKey);
        try {
            return Optional.ofNullable(obj).map(Integer::parseInt);
        } catch (NumberFormatException e) {
            log.error("Error parsing refresh token (userId may be corrupted): ", e);
            return Optional.empty();
        }
    }

    public void deleteRefreshToken(String hashedRefreshToken) {
        String tokenKey = tokenKeyPrefix + hashedRefreshToken;
        redisTemplate.delete(tokenKey);
    }

    public void revokeRefreshTokens(Integer userId) {
        String userTokensKey = userTokensKeyTemplate.formatted(userId);
        Set<String> tokenHashes = redisTemplate.opsForSet().members(userTokensKey);

        if (tokenHashes != null && !tokenHashes.isEmpty()) {
            for (String hash : tokenHashes) {
                String tokenKey = tokenKeyPrefix + hash;
                redisTemplate.delete(tokenKey);
            }
        }

        redisTemplate.delete(userTokensKey);
    }
}
