package com.mthsgimenez.fitcontrol.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.mthsgimenez.fitcontrol.model.User;
import com.mthsgimenez.fitcontrol.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
public class JWTUtil {
    private final String issuer;
    private final Algorithm algorithm;
    private final Long expirationMillis;
    private final UserRepository userRepository;

    public JWTUtil(UserRepository userRepository,
                   @Value("${app.jwt.secret}") String secret,
                   @Value("${app.jwt.issuer}") String issuer,
                   @Value("${app.jwt.expiration_ms}") Long expirationMillis) {
        this.userRepository = userRepository;
        this.algorithm = Algorithm.HMAC256(secret);
        this.issuer = issuer;
        this.expirationMillis = expirationMillis;
    }

    public String generateAuthToken(User user) {
        Instant issueDate = Instant.now();

        return JWT.create()
                .withIssuer(this.issuer)
                .withSubject(user.getUuid().toString())
                .withClaim("type", TokenType.AUTH.name())
                .withClaim("email", user.getEmail())
                .withIssuedAt(issueDate)
                .withNotBefore(issueDate)
                .withExpiresAt(issueDate.plusMillis(this.expirationMillis))
                .sign(this.algorithm);
    }

    public String generateRegistrationToken(String email) {
        Instant issueDate = Instant.now();

        return JWT.create()
                .withIssuer(this.issuer)
                .withSubject(email)
                .withClaim("type", TokenType.REGISTRATION.name())
                .withIssuedAt(issueDate)
                .withNotBefore(issueDate)
                .withExpiresAt(issueDate.plus(Duration.ofMinutes(30)))
                .sign(this.algorithm);
    }

    private DecodedJWT decodeToken(String token) {
        JWTVerifier verifier = JWT.require(this.algorithm)
                .withIssuer(this.issuer)
                .build();

        return verifier.verify(token);
    }

    public User verifyAuthTokenAndGetUser(String token) {
        DecodedJWT decodedJWT = decodeToken(token);

        if (!TokenType.AUTH.name().equals(decodedJWT.getClaim("type").asString())) {
            throw new JWTVerificationException("Invalid token type");
        }

        UUID uuid = UUID.fromString(decodedJWT.getSubject());
        return userRepository.findByUuid(uuid).orElseThrow(
                () -> new JWTVerificationException("User not found with uuid " + uuid)
        );
    }

    public String verifyRegistrationTokenAndGetEmail(String token) {
        DecodedJWT decodedJWT = decodeToken(token);

        if (!TokenType.REGISTRATION.name().equals(decodedJWT.getClaim("type").asString())) {
            throw new JWTVerificationException("Invalid token type");
        }

        return decodedJWT.getSubject();
    }
}
