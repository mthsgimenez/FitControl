package com.mthsgimenez.fitcontrol.auth.service;

import com.mthsgimenez.fitcontrol.auth.dto.LoginDTO;
import com.mthsgimenez.fitcontrol.auth.model.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    public LoginService(AuthenticationManager authenticationManager, JWTService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public String login(LoginDTO data) {
        var usernamePasswordToken = new UsernamePasswordAuthenticationToken(data.email(), data.password());
        var auth = authenticationManager.authenticate(usernamePasswordToken);

        return jwtService.generateToken((User) auth.getPrincipal());
    }
}
