package com.ecommerce.api.auth;

import com.ecommerce.api.auth.request.LoginRequest;
import com.ecommerce.api.auth.request.RegisterUserRequest;
import com.ecommerce.api.exception.ApiErrorResponse;
import com.ecommerce.application.user.command.LoginCommand;
import com.ecommerce.application.user.command.RegisterUserCommand;
import com.ecommerce.application.user.response.AuthResponse;
import com.ecommerce.application.user.response.UserResponse;
import com.ecommerce.application.user.service.LoginService;
import com.ecommerce.application.user.service.RegisterUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Auth", description = "Operações de cadastro e autenticação de usuários")
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
    @Operation(
            summary = "Cadastrar usuário",
            description = "Cria uma nova conta de usuário e retorna os dados públicos do cadastro."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuário cadastrado com sucesso",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de entrada inválidos ou regra de negócio violada",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
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
    @Operation(
            summary = "Realizar login",
            description = "Autentica o usuário com e-mail e senha e retorna um token JWT para acessar rotas protegidas."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Login realizado com sucesso",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Credenciais inválidas, usuário inativo ou dados de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
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
