package com.ecommerce.application.user.service;

import com.ecommerce.application.user.command.RegisterUserCommand;
import com.ecommerce.application.user.repository.UserRepository;
import com.ecommerce.application.user.response.UserResponse;
import com.ecommerce.application.user.security.PasswordHasher;
import com.ecommerce.domain.exception.DomainException;
import com.ecommerce.domain.user.User;
import com.ecommerce.domain.user.UserRole;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegisterUserServiceTest {

    @Test
    void shouldRegisterUser() {
        FakeUserRepository userRepository = new FakeUserRepository(false);
        FakePasswordHasher passwordHasher = new FakePasswordHasher();
        RegisterUserService service = new RegisterUserService(userRepository, passwordHasher);
        RegisterUserCommand command = new RegisterUserCommand(
                " Igor ",
                " IGOR@EMAIL.COM ",
                "123456",
                UserRole.CUSTOMER
        );

        UserResponse response = service.create(command);

        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("Igor");
        assertThat(response.email()).isEqualTo("igor@email.com");
        assertThat(response.role()).isEqualTo(UserRole.CUSTOMER);
        assertThat(response.active()).isTrue();
        assertThat(response.createdAt()).isNotNull();
        assertThat(userRepository.saveWasCalled()).isTrue();
        assertThat(userRepository.savedUser()).isNotNull();
        assertThat(userRepository.savedUser().getPasswordHash()).isEqualTo("hashed-123456");
        assertThat(userRepository.savedUser().getPasswordHash()).isNotEqualTo("123456");
        assertThat(passwordHasher.receivedPassword()).isEqualTo("123456");
    }

    @Test
    void shouldNotRegisterUserWhenEmailAlreadyExists() {
        FakeUserRepository userRepository = new FakeUserRepository(true);
        FakePasswordHasher passwordHasher = new FakePasswordHasher();
        RegisterUserService service = new RegisterUserService(userRepository, passwordHasher);
        RegisterUserCommand command = new RegisterUserCommand(
                "Igor",
                "igor@email.com",
                "123456",
                UserRole.CUSTOMER
        );

        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(DomainException.class)
                .hasMessage("E-mail ja cadastrado");

        assertThat(userRepository.saveWasCalled()).isFalse();
        assertThat(passwordHasher.hashWasCalled()).isFalse();
    }

    @Test
    void shouldCreateAdminUser() {
        FakeUserRepository userRepository = new FakeUserRepository(false);
        RegisterUserService service = new RegisterUserService(userRepository, new FakePasswordHasher());
        RegisterUserCommand command = new RegisterUserCommand(
                "Admin",
                "admin@email.com",
                "123456",
                UserRole.ADMIN
        );

        UserResponse response = service.create(command);

        assertThat(response.role()).isEqualTo(UserRole.ADMIN);
        assertThat(userRepository.savedUser().getRole()).isEqualTo(UserRole.ADMIN);
    }

    private static final class FakeUserRepository implements UserRepository {

        private final boolean emailExists;
        private User savedUser;

        private FakeUserRepository(boolean emailExists) {
            this.emailExists = emailExists;
        }

        @Override
        public User save(User user) {
            this.savedUser = user;
            return user;
        }

        @Override
        public Optional<User> findById(UUID id) {
            return Optional.empty();
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return Optional.empty();
        }

        @Override
        public boolean existsByEmail(String email) {
            return emailExists;
        }

        private boolean saveWasCalled() {
            return savedUser != null;
        }

        private User savedUser() {
            return savedUser;
        }
    }

    private static final class FakePasswordHasher implements PasswordHasher {

        private String receivedPassword;

        @Override
        public String hash(String password) {
            this.receivedPassword = password;
            return "hashed-" + password;
        }

        @Override
        public boolean matches(String rawPassword, String passwordHash) {
            return passwordHash.equals("hashed-" + rawPassword);
        }

        private boolean hashWasCalled() {
            return receivedPassword != null;
        }

        private String receivedPassword() {
            return receivedPassword;
        }
    }
}
