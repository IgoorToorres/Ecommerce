package com.ecommerce.infrastructure.persistence.user;

import com.ecommerce.domain.user.User;
import com.ecommerce.domain.user.UserRole;
import com.ecommerce.infrastructure.persistence.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryAdapterIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private UserRepositoryAdapter userRepositoryAdapter;

    @Test
    void shouldSaveAndFindUserById() {
        User user = new User("Cliente Teste", "cliente@email.com", "hashed-password", UserRole.CUSTOMER);

        User savedUser = userRepositoryAdapter.save(user);
        Optional<User> foundUser = userRepositoryAdapter.findById(savedUser.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(savedUser.getId());
        assertThat(foundUser.get().getName()).isEqualTo("Cliente Teste");
        assertThat(foundUser.get().getEmail()).isEqualTo("cliente@email.com");
        assertThat(foundUser.get().getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(foundUser.get().isActive()).isTrue();
    }

    @Test
    void shouldFindUserByEmailAndCheckIfEmailExists() {
        User user = new User("Admin Teste", "ADMIN@EMAIL.COM", "hashed-password", UserRole.ADMIN);

        userRepositoryAdapter.save(user);

        Optional<User> foundUser = userRepositoryAdapter.findByEmail("admin@email.com");
        boolean existsByEmail = userRepositoryAdapter.existsByEmail("admin@email.com");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("admin@email.com");
        assertThat(existsByEmail).isTrue();
    }
}
