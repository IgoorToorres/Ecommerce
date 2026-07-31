package com.ecommerce.domain.product;

import com.ecommerce.domain.exception.DomainException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "products")
public class Product {

    private static final int MAX_NAME_LENGTH = 150;

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected Product(){}

    public Product(String name, String description, BigDecimal price, int stockQuantity) {
        validateName(name);
        validatePrice(price);
        validateInitialStock(stockQuantity);

        this.id = UUID.randomUUID();
        this.name = name.trim();
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void activate() {
        this.active = true;
        touch();
    }

    public void deactivate() {
        this.active = false;
        touch();
    }

    public void changePrice(BigDecimal newPrice) {
        validatePrice(newPrice);

        this.price = newPrice;
        touch();
    }

    public void increaseStock(int quantity) {
        if (quantity <= 0) {
            throw new DomainException("A quantidade para aumentar estoque deve ser maior que zero.");
        }

        this.stockQuantity += quantity;
        touch();
    }

    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new DomainException("A quantidade para reduzir estoque deve ser maior que zero.");
        }

        if (quantity > stockQuantity) {
            throw new DomainException("Estoque insuficiente.");
        }

        this.stockQuantity -= quantity;
        touch();
    }

    public boolean hasAvailableStock(int quantity) {
        return quantity > 0 && stockQuantity >= quantity;
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainException("O nome do produto é obrigatório.");
        }

        if (name.trim().length() > MAX_NAME_LENGTH) {
            throw new DomainException("O nome do produto deve ter no máximo 150 caracteres.");
        }
    }

    private void validatePrice(BigDecimal price) {
        if (price == null) {
            throw new DomainException("O preço do produto é obrigatório.");
        }

        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("O preço do produto deve ser maior que zero.");
        }
    }

    private void validateInitialStock(int stockQuantity) {
        if (stockQuantity < 0) {
            throw new DomainException("O estoque inicial não pode ser negativo.");
        }
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getStockQuantity() {
        return stockQuantity;
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

    public long getVersion(){ return version; }
}
