package com.mthsgimenez.fitcontrol.auth.refreshtokens;

import com.mthsgimenez.fitcontrol.auth.login.JWTService;
import com.mthsgimenez.fitcontrol.user.User;
import com.mthsgimenez.fitcontrol.user.UserRepository;
import com.mthsgimenez.fitcontrol.util.DeterministicHashUtil;
import com.mthsgimenez.fitcontrol.util.RandomStringUtil;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {

    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final RandomStringUtil randomStringUtil;
    private final DeterministicHashUtil deterministicHashUtil;
    private final RefreshTokenStore refreshTokenStore;

    public RefreshTokenService(
            UserRepository userRepository,
            JWTService jwtService,
            RandomStringUtil randomStringUtil,
            DeterministicHashUtil deterministicHashUtil,
            RefreshTokenStore refreshTokenStore
    ) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.randomStringUtil = randomStringUtil;
        this.deterministicHashUtil = deterministicHashUtil;
        this.refreshTokenStore = refreshTokenStore;
    }

    private void hashAndStoreToken(String refreshToken, Integer userId) {
        String hashedToken = deterministicHashUtil.hashString(refreshToken);
        refreshTokenStore.storeRefreshToken(hashedToken, userId);
    }

    public String generateAndStoreRefreshToken(User user) {
        String token = randomStringUtil.getRandomString();
        hashAndStoreToken(token, user.getId());
        return token;
    }

    public TokenDTO refreshTokens(String refreshToken) {
        String hashedToken = deterministicHashUtil.hashString(refreshToken);
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
