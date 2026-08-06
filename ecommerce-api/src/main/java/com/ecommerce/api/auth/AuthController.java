package com.ecommerce.api.auth;

import com.ecommerce.api.auth.request.LoginRequest;
import com.ecommerce.api.auth.request.RegisterUserRequest;
import com.ecommerce.application.user.command.LoginCommand;
import com.ecommerce.application.user.command.RegisterUserCommand;
import com.ecommerce.application.user.response.AuthResponse;
import com.ecommerce.application.user.response.UserResponse;
import com.ecommerce.application.user.service.LoginService;
import com.ecommerce.application.user.service.RegisterUserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "auth")
public class AuthController {
    private final RegisterUserService registerUserService;
    private final LoginService loginService;

    public AuthController(
            RegisterUserService registerUserService,
            LoginService loginService
    ){
        this.registerUserService = registerUserService;
        this.loginService = loginService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request){
        RegisterUserCommand command = new RegisterUserCommand(
                request.name(),
                request.email(),
                request.password(),
                request.role()
        );

        UserResponse response = registerUserService.create(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request){
        LoginCommand command = new LoginCommand(
                request.email(),
                request.password()
        );

        AuthResponse response = loginService.login(command);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
