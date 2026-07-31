package com.ecommerce.application.product;

import java.math.BigDecimal;

public record CreateProductCommand (
    String name,
    String description,
    BigDecimal price,
    int stockQuantity
){}
