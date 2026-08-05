package com.ecommerce.application.user.command;

import com.ecommerce.domain.user.UserRole;

public record RegisterUserCommand(
        String name,
        String email,
        String password,
        UserRole role
) {
}
