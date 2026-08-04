package com.ecommerce.domain.order;


import com.ecommerce.domain.exception.DomainException;

import java.math.BigDecimal;
import java.util.UUID;

public class OrderItem {

    private UUID id;
    private UUID productId;
    private String productName;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal totalPrice;

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
}
