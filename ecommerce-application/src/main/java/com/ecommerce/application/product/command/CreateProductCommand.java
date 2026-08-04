package com.ecommerce.application.product.command;

import java.math.BigDecimal;

public record CreateProductCommand (
    String name,
    String description,
    BigDecimal price,
    int stockQuantity
){}
