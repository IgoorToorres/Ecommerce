package com.ecommerce.application.product.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        int stockQuantity,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
