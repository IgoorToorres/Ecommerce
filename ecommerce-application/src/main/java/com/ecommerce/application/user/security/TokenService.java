package com.ecommerce.application.user.security;

import com.ecommerce.application.user.response.AuthResponse;
import com.ecommerce.domain.user.User;
import org.springframework.stereotype.Component;

@Component
public interface TokenService {
    AuthResponse generateToken(User user);
    AuthenticatedUser validateToken(String token);
}
