package com.ecommerce.application.user.service;

import com.ecommerce.application.user.command.RegisterUserCommand;
import com.ecommerce.application.user.repository.UserRepository;
import com.ecommerce.application.user.response.UserResponse;
import com.ecommerce.application.user.security.PasswordHasher;
import com.ecommerce.domain.exception.DomainException;
import com.ecommerce.domain.user.User;
import org.springframework.stereotype.Service;

@Service
public class RegisterUserService {
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public RegisterUserService(UserRepository userRepository, PasswordHasher passwordHasher){
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    public UserResponse create(RegisterUserCommand command){
        if(userRepository.existsByEmail(command.email())){
            throw new DomainException("E-mail ja cadastrado");
        }

        String passwordHash = passwordHasher.hash(command.password());

        User user = new User(
                command.name(),
                command.email(),
                passwordHash,
                command.role()
        );

        User savedUser = userRepository.save(user);

        return toResponse(savedUser);
    }

    private UserResponse toResponse(User user){
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}
