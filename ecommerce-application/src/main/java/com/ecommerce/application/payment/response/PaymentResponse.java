package com.ecommerce.application.payment.response;

import com.ecommerce.domain.payment.PaymentMethod;
import com.ecommerce.domain.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID orderId,
        PaymentStatus status,
        PaymentMethod method,
        BigDecimal amount,
        String idempotencyKey,
        String externalReference,
        Instant createdAt,
        Instant updatedAt
) {
}
