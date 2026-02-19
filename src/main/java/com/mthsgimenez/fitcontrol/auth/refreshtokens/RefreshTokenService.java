package com.mthsgimenez.fitcontrol.auth.refreshtokens;

import com.mthsgimenez.fitcontrol.auth.login.JWTService;
import com.mthsgimenez.fitcontrol.user.User;
import com.mthsgimenez.fitcontrol.user.UserRepository;
import com.mthsgimenez.fitcontrol.infra.cache.CacheService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RefreshTokenService {

    private final CacheService cacheService;
    private final Long expirationMinutes;
    private final String cacheKeyPrefix = "refresh_token:";
    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final RandomTokenUtil randomTokenUtil;

    public RefreshTokenService(
            CacheService cacheService,
            @Value("${app.refresh_token.expiration_minutes}") Long expirationMinutes,
            UserRepository userRepository,
            JWTService jwtService,
            RandomTokenUtil randomTokenUtil
    ) {
        this.cacheService = cacheService;
        this.expirationMinutes = expirationMinutes;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.randomTokenUtil = randomTokenUtil;
    }

    private void hashAndStoreToken(String refreshToken, Integer userId) {
        String hashedToken = randomTokenUtil.hashToken(refreshToken);
        String cacheKey = cacheKeyPrefix + hashedToken;
        cacheService.set(cacheKey, userId, Duration.ofMinutes(expirationMinutes));
    }

    public String generateAndStoreRefreshToken(User user) {
        String token = randomTokenUtil.getRandomToken();
        hashAndStoreToken(token, user.getId());
        return token;
    }

    public TokenDTO refreshTokens(String refreshToken) {
        String cacheKey = cacheKeyPrefix + randomTokenUtil.hashToken(refreshToken);
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
