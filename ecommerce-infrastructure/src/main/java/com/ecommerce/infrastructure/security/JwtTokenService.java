package com.ecommerce.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ecommerce.application.user.response.AuthResponse;
import com.ecommerce.application.user.security.AuthenticatedUser;
import com.ecommerce.application.user.security.TokenService;
import com.ecommerce.domain.user.User;
import com.ecommerce.domain.user.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class JwtTokenService implements TokenService {

    private final String secret;
    private final Long expirationSeconds;

    public JwtTokenService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-seconds}") Long expirationSeconds
    ){
        this.secret = secret;
        this.expirationSeconds = expirationSeconds;
    }

    @Override
    public AuthResponse generateToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(expirationSeconds);

        String token = JWT.create()
                .withSubject(user.getId().toString())
                .withClaim("email", user.getEmail())
                .withClaim("role", user.getRole().name())
                .withIssuedAt(now)
                .withExpiresAt(expiresAt)
                .sign(Algorithm.HMAC256(secret));

        return new AuthResponse(
                token,
                "Bearer",
                expirationSeconds
        );
    }

    @Override
    public AuthenticatedUser validateToken(String token) {
        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(secret))
                .build()
                .verify(token);

        return new AuthenticatedUser(
                UUID.fromString(decodedJWT.getSubject()),
                decodedJWT.getClaim("email").asString(),
                UserRole.valueOf(decodedJWT.getClaim("role").asString())
        );
    }
}
