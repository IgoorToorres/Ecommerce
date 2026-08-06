package com.ecommerce.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ecommerce.application.user.response.AuthResponse;
import com.ecommerce.application.user.security.AuthenticatedUser;
import com.ecommerce.domain.user.User;
import com.ecommerce.domain.user.UserRole;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenServiceTest {

    @Test
    void shouldGenerateToken() {
        String secret = "test-secret-with-at-least-32-characters";
        Long expirationSeconds = 3600L;
        JwtTokenService service = new JwtTokenService(secret, expirationSeconds);
        User user = new User(
                "Igor",
                "igor@email.com",
                "hashed-password",
                UserRole.CUSTOMER
        );

        AuthResponse response = service.generateToken(user);

        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(expirationSeconds);

        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(secret))
                .build()
                .verify(response.accessToken());

        assertThat(decodedJWT.getSubject()).isEqualTo(user.getId().toString());
        assertThat(decodedJWT.getClaim("email").asString()).isEqualTo(user.getEmail());
        assertThat(decodedJWT.getClaim("role").asString()).isEqualTo(user.getRole().name());
        assertThat(decodedJWT.getIssuedAt()).isNotNull();
        assertThat(decodedJWT.getExpiresAt()).isNotNull();
        assertThat(decodedJWT.getExpiresAt().toInstant())
                .isAfter(Instant.now().plusSeconds(3500))
                .isBefore(Instant.now().plusSeconds(3700));
    }

    @Test
    void shouldValidateToken() {
        String secret = "test-secret-with-at-least-32-characters";
        Long expirationSeconds = 3600L;
        JwtTokenService service = new JwtTokenService(secret, expirationSeconds);
        User user = new User(
                "Igor",
                "igor@email.com",
                "hashed-password",
                UserRole.CUSTOMER
        );
        AuthResponse response = service.generateToken(user);

        AuthenticatedUser authenticatedUser = service.validateToken(response.accessToken());

        assertThat(authenticatedUser.id()).isEqualTo(user.getId());
        assertThat(authenticatedUser.email()).isEqualTo(user.getEmail());
        assertThat(authenticatedUser.role()).isEqualTo(user.getRole());
    }
}
