package com.mthsgimenez.fitcontrol.auth.login;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.mthsgimenez.fitcontrol.user.User;
import com.mthsgimenez.fitcontrol.user.UserRepository;
import com.mthsgimenez.fitcontrol.tenant.Tenant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Duration;
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

    public Long getExpirationSeconds(String token) {
        DecodedJWT decoded = JWT.decode(token);
        Instant expiresAt = decoded.getExpiresAt().toInstant();
        Instant now = Instant.now();

        return Duration.between(now, expiresAt).getSeconds();
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