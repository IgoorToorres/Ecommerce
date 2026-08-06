package com.ecommerce.application.user.service;

import com.ecommerce.application.user.command.LoginCommand;
import com.ecommerce.application.user.repository.UserRepository;
import com.ecommerce.application.user.response.AuthResponse;
import com.ecommerce.application.user.security.PasswordHasher;
import com.ecommerce.application.user.security.TokenService;
import com.ecommerce.domain.exception.DomainException;
import com.ecommerce.domain.user.User;
import com.ecommerce.domain.user.UserRole;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginServiceTest {

    @Test
    void shouldLoginUser() {
        User user = createUser();
        FakeUserRepository userRepository = new FakeUserRepository(user);
        FakePasswordHasher passwordHasher = new FakePasswordHasher(true);
        FakeTokenService tokenService = new FakeTokenService();
        LoginService service = new LoginService(userRepository, passwordHasher, tokenService);
        LoginCommand command = new LoginCommand("igor@email.com", "123456");

        AuthResponse response = service.login(command);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(3600L);
        assertThat(passwordHasher.receivedRawPassword()).isEqualTo("123456");
        assertThat(passwordHasher.receivedPasswordHash()).isEqualTo(user.getPasswordHash());
        assertThat(tokenService.receivedUser()).isSameAs(user);
    }

    @Test
    void shouldNotLoginWhenUserDoesNotExist() {
        FakeUserRepository userRepository = new FakeUserRepository(null);
        FakePasswordHasher passwordHasher = new FakePasswordHasher(true);
        FakeTokenService tokenService = new FakeTokenService();
        LoginService service = new LoginService(userRepository, passwordHasher, tokenService);
        LoginCommand command = new LoginCommand("igor@email.com", "123456");

        assertThatThrownBy(() -> service.login(command))
                .isInstanceOf(DomainException.class)
                .hasMessage("Credenciais incorretas");

        assertThat(passwordHasher.matchesWasCalled()).isFalse();
        assertThat(tokenService.generateTokenWasCalled()).isFalse();
    }

    @Test
    void shouldNotLoginWhenUserIsInactive() {
        User user = createUser();
        user.deactivate();
        FakePasswordHasher passwordHasher = new FakePasswordHasher(true);
        FakeTokenService tokenService = new FakeTokenService();
        LoginService service = new LoginService(
                new FakeUserRepository(user),
                passwordHasher,
                tokenService
        );
        LoginCommand command = new LoginCommand("igor@email.com", "123456");

        assertThatThrownBy(() -> service.login(command))
                .isInstanceOf(DomainException.class)
                .hasMessage("usuario inativo");

        assertThat(passwordHasher.matchesWasCalled()).isFalse();
        assertThat(tokenService.generateTokenWasCalled()).isFalse();
    }

    @Test
    void shouldNotLoginWhenPasswordDoesNotMatch() {
        User user = createUser();
        FakePasswordHasher passwordHasher = new FakePasswordHasher(false);
        FakeTokenService tokenService = new FakeTokenService();
        LoginService service = new LoginService(
                new FakeUserRepository(user),
                passwordHasher,
                tokenService
        );
        LoginCommand command = new LoginCommand("igor@email.com", "wrong-password");

        assertThatThrownBy(() -> service.login(command))
                .isInstanceOf(DomainException.class)
                .hasMessage("Credencias incorretas");

        assertThat(passwordHasher.matchesWasCalled()).isTrue();
        assertThat(tokenService.generateTokenWasCalled()).isFalse();
    }

    private User createUser() {
        return new User(
                "Igor",
                "igor@email.com",
                "hashed-password",
                UserRole.CUSTOMER
        );
    }

    private static final class FakeUserRepository implements UserRepository {

        private final User user;

        private FakeUserRepository(User user) {
            this.user = user;
        }

        @Override
        public User save(User user) {
            return user;
        }

        @Override
        public Optional<User> findById(UUID id) {
            return Optional.empty();
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return Optional.ofNullable(user)
                    .filter(existingUser -> existingUser.getEmail().equals(email));
        }

        @Override
        public boolean existsByEmail(String email) {
            return user != null && user.getEmail().equals(email);
        }
    }

    private static final class FakePasswordHasher implements PasswordHasher {

        private final boolean matches;
        private String receivedRawPassword;
        private String receivedPasswordHash;

        private FakePasswordHasher(boolean matches) {
            this.matches = matches;
        }

        @Override
        public String hash(String password) {
            return "hashed-" + password;
        }

        @Override
        public boolean matches(String rawPassword, String passwordHash) {
            this.receivedRawPassword = rawPassword;
            this.receivedPasswordHash = passwordHash;
            return matches;
        }

        private boolean matchesWasCalled() {
            return receivedRawPassword != null;
        }

        private String receivedRawPassword() {
            return receivedRawPassword;
        }

        private String receivedPasswordHash() {
            return receivedPasswordHash;
        }
    }

    private static final class FakeTokenService implements TokenService {

        private User receivedUser;

        @Override
        public AuthResponse generateToken(User user) {
            this.receivedUser = user;
            return new AuthResponse("access-token", "Bearer", 3600L);
        }

        private boolean generateTokenWasCalled() {
            return receivedUser != null;
        }

        private User receivedUser() {
            return receivedUser;
        }
    }
}
