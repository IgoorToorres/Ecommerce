package com.ecommerce.application.user.service;

import com.ecommerce.application.exception.ResourceNotFoundException;
import com.ecommerce.application.user.command.LoginCommand;
import com.ecommerce.application.user.repository.UserRepository;
import com.ecommerce.application.user.response.AuthResponse;
import com.ecommerce.application.user.security.PasswordHasher;
import com.ecommerce.application.user.security.TokenService;
import com.ecommerce.domain.exception.DomainException;
import com.ecommerce.domain.user.User;
import org.springframework.stereotype.Service;

@Service
public class LoginService {
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenService tokenService;

    public LoginService(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            TokenService tokenService
        ){
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenService = tokenService;
    }

    public AuthResponse login(LoginCommand command){
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new DomainException("Credenciais incorretas"));

        if(!user.isActive()){
            throw new DomainException("usuario inativo");
        }

        if(!passwordHasher.matches(command.password(), user.getPasswordHash())){
            throw new DomainException("Credencias incorretas");
        }

        return tokenService.generateToken(user);
    }
}
