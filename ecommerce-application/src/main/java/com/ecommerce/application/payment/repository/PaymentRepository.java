package com.ecommerce.application.payment.repository;

import com.ecommerce.domain.payment.Payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {
    Payment save(Payment payment);

    Optional<Payment> findByOrderIdAndIdempotencyKey(
            UUID orderId,
            String idempotencyKey
    );
}
