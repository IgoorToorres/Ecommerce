package com.ecommerce.domain.user;

import com.ecommerce.domain.exception.DomainException;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private UserRole role;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected User(){}

    public User(String name, String email, String passwordHash, UserRole role){
        validateName(name);
        validateEmail(email);
        validatePasswordHash(passwordHash);
        validateRole(role);

        this.id = UUID.randomUUID();
        this.name = name.trim();
        this.email = email.trim().toLowerCase();
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    private void validateName(String name){
        if(name == null || name.isBlank()){
            throw new DomainException("O nome do usuário é obrigatório.");
        }
    }

    private void validateEmail(String email){
        if(email == null || email.isBlank()){
            throw new DomainException("O e-mail do usuário é obrigatório.");
        }
    }

    private void validatePasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new DomainException("O hash da senha é obrigatório.");
        }
    }

    private void validateRole(UserRole role) {
        if (role == null) {
            throw new DomainException("O perfil do usuário é obrigatório.");
        }
    }

    private void touch(){
        this.updatedAt = Instant.now();
    }

    public void deactivate(){
        if(!this.active){
            throw new DomainException("O usuário já está desativado.");
        }
        this.active = false;
        touch();
    }

    public void activate(){
        if(this.active){
            throw new DomainException("O usuário já está ativo.");
        }
        this.active = true;
        touch();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public long getVersion() {
        return version;
    }
}
