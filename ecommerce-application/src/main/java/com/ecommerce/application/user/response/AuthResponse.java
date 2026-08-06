package com.ecommerce.application.user.response;

public record AuthResponse(
        String accessToken,
        String tokenType,
        Long expiresIn
) {
}
