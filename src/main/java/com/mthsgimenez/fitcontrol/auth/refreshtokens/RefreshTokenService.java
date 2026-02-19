package com.mthsgimenez.fitcontrol.auth.refreshtokens;

import com.mthsgimenez.fitcontrol.auth.login.JWTService;
import com.mthsgimenez.fitcontrol.user.User;
import com.mthsgimenez.fitcontrol.user.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RefreshTokenService {

    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final RandomTokenUtil randomTokenUtil;
    private final RefreshTokenStore refreshTokenStore;

    public RefreshTokenService(
            UserRepository userRepository,
            JWTService jwtService,
            RandomTokenUtil randomTokenUtil,
            RefreshTokenStore refreshTokenStore
    ) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.randomTokenUtil = randomTokenUtil;
        this.refreshTokenStore = refreshTokenStore;
    }

    private void hashAndStoreToken(String refreshToken, Integer userId) {
        String hashedToken = randomTokenUtil.hashToken(refreshToken);
        refreshTokenStore.storeRefreshToken(hashedToken, userId);
    }

    public String generateAndStoreRefreshToken(User user) {
        String token = randomTokenUtil.getRandomToken();
        hashAndStoreToken(token, user.getId());
        return token;
    }

    public TokenDTO refreshTokens(String refreshToken) {
        String hashedToken = randomTokenUtil.hashToken(refreshToken);
        Integer userId = refreshTokenStore.getRefreshTokenUserId(hashedToken)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired refresh token"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired refresh token"));

        String newRefreshToken = generateAndStoreRefreshToken(user);
        refreshTokenStore.deleteRefreshToken(hashedToken);

        String newAccessToken = jwtService.generateToken(user);
        return new TokenDTO(newAccessToken, newRefreshToken, jwtService.getExpirationSeconds(newAccessToken));
    }

    public void revokeRefreshTokensFromUser(Integer userId) {
        refreshTokenStore.revokeRefreshTokens(userId);
    }
}
