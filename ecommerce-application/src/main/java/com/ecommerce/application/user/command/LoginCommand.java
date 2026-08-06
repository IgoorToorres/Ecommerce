package com.ecommerce.application.user.command;

public record LoginCommand(
    String email,
    String password
) {
}
