package com.ecommerce.application.order.response;

import com.ecommerce.domain.order.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID customerId,
        OrderStatus status,
        BigDecimal totalAmount,
        Instant createdAt,
        Instant paidAt,
        Instant cancelledAt,
        Instant shippedAt,
        Instant deliveredAt,
        List<OrderItemResponse> items
) {
}
