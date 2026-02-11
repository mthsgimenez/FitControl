package com.mthsgimenez.fitcontrol.auth.service;

import com.mthsgimenez.fitcontrol.auth.dto.LoginDTO;
import com.mthsgimenez.fitcontrol.auth.dto.TokenDTO;
import com.mthsgimenez.fitcontrol.auth.model.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;
    private final RefreshTokenService refreshTokenService;

    public LoginService(
            AuthenticationManager authenticationManager,
            JWTService jwtService,
            RefreshTokenService refreshTokenService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    public TokenDTO login(LoginDTO data) {
        var usernamePasswordToken = new UsernamePasswordAuthenticationToken(data.email(), data.password());
        var auth = authenticationManager.authenticate(usernamePasswordToken);

        User user = (User) auth.getPrincipal();

        String accessToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.generateAndStoreRefreshToken(user);

        return new TokenDTO(
                accessToken,
                refreshToken,
                jwtService.getExpirationSeconds(accessToken)
        );
    }
}
