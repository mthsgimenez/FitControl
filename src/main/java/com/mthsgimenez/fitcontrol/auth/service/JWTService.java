package com.mthsgimenez.fitcontrol.auth.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.mthsgimenez.fitcontrol.auth.model.User;
import com.mthsgimenez.fitcontrol.auth.repository.UserRepository;
import com.mthsgimenez.fitcontrol.tenant.model.Tenant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class JWTService {
    private final String issuer;
    private final Algorithm algorithm;
    private final Long expirationMillis;

    public JWTService(UserRepository userRepository,
                      @Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.issuer}") String issuer,
                      @Value("${app.jwt.expiration_ms}") Long expirationMillis) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.issuer = issuer;
        this.expirationMillis = expirationMillis;
    }

    public String generateToken(User user) {
        Instant issueDate = Instant.now();

        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList();

        Tenant tenant = user.getTenant();

        return JWT.create()
                .withIssuer(this.issuer)
                .withSubject(user.getUuid().toString())
                .withClaim("roles", roles)
                .withClaim("email", user.getEmail())
                .withClaim("tenant", tenant.getUuid().toString())
                .withClaim("schema", tenant.getSchemaName())
                .withIssuedAt(issueDate)
                .withNotBefore(issueDate)
                .withExpiresAt(issueDate.plusMillis(this.expirationMillis))
                .sign(this.algorithm);
    }
}