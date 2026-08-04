package com.ecommerce.domain.order;


import com.ecommerce.domain.exception.DomainException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    @Column(name = "product_name", nullable = false, length = 150, updatable = false)
    private String productName;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal unitPrice;

    @Column(name = "quantity", nullable = false, updatable = false)
    private int quantity;

    @Column(name = "total_price", nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal totalPrice;

    protected OrderItem(){}

    public OrderItem(UUID productId, String productName, BigDecimal unitPrice, int quantity){
        validateProductId(productId);
        validateProductName(productName);
        validateUnitPrice(unitPrice);
        validateQuantity(quantity);

        this.id = UUID.randomUUID();
        this.productId = productId;
        this.productName = productName.trim();
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    private void validateProductId(UUID productId){
        if(productId == null){
            throw new DomainException("O produto do item é obrigatorio");
        }
    }

    private void validateProductName(String productName){
        if(productName == null || productName.isBlank()){
            throw new DomainException("O nome do produto é orbigatório");
        }
    }

    private void validateUnitPrice(BigDecimal unitPrice){
        if(unitPrice == null){
            throw new DomainException("O preço unitario do item é obrigatorio");
        }
        if(unitPrice.compareTo(BigDecimal.ZERO) <= 0){
            throw new DomainException("A quantidade do item deve ser maior que zero");
        }
    }

    private void validateQuantity(int quantity){
        if(quantity <= 0){
            throw new DomainException("A quantidade do item deve ser maior que zero");
        }
    }

    void attachToOrder(Order order) {
        if (order == null) {
            throw new DomainException("O pedido do item é obrigatório.");
        }

        this.order = order;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    Order getOrder() {
        return order;
    }
}
