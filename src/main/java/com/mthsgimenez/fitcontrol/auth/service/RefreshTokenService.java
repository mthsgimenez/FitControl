package com.mthsgimenez.fitcontrol.auth.service;

import com.mthsgimenez.fitcontrol.auth.dto.TokenDTO;
import com.mthsgimenez.fitcontrol.auth.exception.InvalidTokenException;
import com.mthsgimenez.fitcontrol.auth.model.User;
import com.mthsgimenez.fitcontrol.auth.repository.UserRepository;
import com.mthsgimenez.fitcontrol.infra.cache.CacheService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

@Service
public class RefreshTokenService {

    private final CacheService cacheService;
    private final Long expirationMinutes;
    private final String cacheKeyPrefix = "refresh_token:";
    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final SecureRandom random = new SecureRandom();
    private final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

    public RefreshTokenService(
            CacheService cacheService,
            @Value("${app.refresh_token.expiration_minutes}") Long expirationMinutes,
            UserRepository userRepository,
            JWTService jwtService
    ) {
        this.cacheService = cacheService;
        this.expirationMinutes = expirationMinutes;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    private String generateRandomString() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return encoder.encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private void hashAndStoreToken(String refreshToken, Integer userId) {
        String hashedToken = hashToken(refreshToken);
        String cacheKey = cacheKeyPrefix + hashedToken;
        cacheService.set(cacheKey, userId, Duration.ofMinutes(expirationMinutes));
    }

    public String generateAndStoreRefreshToken(User user) {
        String token = generateRandomString();
        hashAndStoreToken(token, user.getId());
        return token;
    }

    public TokenDTO refreshTokens(String refreshToken) {
        String cacheKey = cacheKeyPrefix + hashToken(refreshToken);
        Object obj = cacheService.getObject(cacheKey);

        if (obj == null) {
            throw new InvalidTokenException("Invalid or expired refresh token");
        }

        Integer userId = (Integer) obj;
        User user = userRepository.findById(userId).orElseThrow(() -> new InvalidTokenException("Invalid or expired refresh token"));

        String newRefreshToken = generateAndStoreRefreshToken(user);
        cacheService.delete(cacheKey);

        String newAccessToken = jwtService.generateToken(user);
        return new TokenDTO(newAccessToken, newRefreshToken, jwtService.getExpirationSeconds(newAccessToken));
    }
}
