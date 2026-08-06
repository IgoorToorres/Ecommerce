package com.ecommerce.application.user.security;

import com.ecommerce.domain.user.UserRole;

import java.util.UUID;

public record AuthenticatedUser(
        UUID id,
        String email,
        UserRole role
) {}
