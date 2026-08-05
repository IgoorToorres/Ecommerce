package com.ecommerce.domain.user;

import com.ecommerce.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    @Test
    void shouldCreateUser() {
        User user = new User(
                " Igor ",
                " IGOR@EMAIL.COM ",
                "hashed-password",
                UserRole.CUSTOMER
        );

        assertThat(user.getId()).isNotNull();
        assertThat(user.getName()).isEqualTo("Igor");
        assertThat(user.getEmail()).isEqualTo("igor@email.com");
        assertThat(user.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(user.getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(user.isActive()).isTrue();
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isEqualTo(user.getCreatedAt());
    }

    @Test
    void shouldCreateAdminUser() {
        User user = new User(
                "Admin",
                "admin@email.com",
                "hashed-password",
                UserRole.ADMIN
        );

        assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void shouldNotCreateUserWhenNameIsNull() {
        assertThatThrownBy(() -> new User(
                null,
                "igor@email.com",
                "hashed-password",
                UserRole.CUSTOMER
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("O nome do usuário é obrigatório.");
    }

    @Test
    void shouldNotCreateUserWhenNameIsBlank() {
        assertThatThrownBy(() -> new User(
                " ",
                "igor@email.com",
                "hashed-password",
                UserRole.CUSTOMER
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("O nome do usuário é obrigatório.");
    }

    @Test
    void shouldNotCreateUserWhenEmailIsNull() {
        assertThatThrownBy(() -> new User(
                "Igor",
                null,
                "hashed-password",
                UserRole.CUSTOMER
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("O e-mail do usuário é obrigatório.");
    }

    @Test
    void shouldNotCreateUserWhenEmailIsBlank() {
        assertThatThrownBy(() -> new User(
                "Igor",
                " ",
                "hashed-password",
                UserRole.CUSTOMER
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("O e-mail do usuário é obrigatório.");
    }

    @Test
    void shouldNotCreateUserWhenPasswordHashIsNull() {
        assertThatThrownBy(() -> new User(
                "Igor",
                "igor@email.com",
                null,
                UserRole.CUSTOMER
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("O hash da senha é obrigatório.");
    }

    @Test
    void shouldNotCreateUserWhenPasswordHashIsBlank() {
        assertThatThrownBy(() -> new User(
                "Igor",
                "igor@email.com",
                " ",
                UserRole.CUSTOMER
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("O hash da senha é obrigatório.");
    }

    @Test
    void shouldNotCreateUserWhenRoleIsNull() {
        assertThatThrownBy(() -> new User(
                "Igor",
                "igor@email.com",
                "hashed-password",
                null
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("O perfil do usuário é obrigatório.");
    }

    @Test
    void shouldDeactivateUser() {
        User user = createUser();

        user.deactivate();

        assertThat(user.isActive()).isFalse();
        assertThat(user.getUpdatedAt()).isAfterOrEqualTo(user.getCreatedAt());
    }

    @Test
    void shouldNotDeactivateUserWhenUserIsAlreadyInactive() {
        User user = createUser();
        user.deactivate();

        assertThatThrownBy(user::deactivate)
                .isInstanceOf(DomainException.class)
                .hasMessage("O usuário já está desativado.");
    }

    @Test
    void shouldActivateUser() {
        User user = createUser();
        user.deactivate();

        user.activate();

        assertThat(user.isActive()).isTrue();
        assertThat(user.getUpdatedAt()).isAfterOrEqualTo(user.getCreatedAt());
    }

    @Test
    void shouldNotActivateUserWhenUserIsAlreadyActive() {
        User user = createUser();

        assertThatThrownBy(user::activate)
                .isInstanceOf(DomainException.class)
                .hasMessage("O usuário já está ativo.");
    }

    private User createUser() {
        return new User(
                "Igor",
                "igor@email.com",
                "hashed-password",
                UserRole.CUSTOMER
        );
    }
}
