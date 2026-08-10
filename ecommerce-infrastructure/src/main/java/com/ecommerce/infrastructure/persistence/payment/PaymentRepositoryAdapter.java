package com.ecommerce.infrastructure.persistence.payment;

import com.ecommerce.application.payment.repository.PaymentRepository;
import com.ecommerce.domain.payment.Payment;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;

    public PaymentRepositoryAdapter(PaymentJpaRepository paymentJpaRepository){
        this.paymentJpaRepository = paymentJpaRepository;
    }

    @Override
    public Payment save(Payment payment) {
        return paymentJpaRepository.save(payment);
    }

    @Override
    public Optional<Payment> findByOrderIdAndIdempotencyKey(UUID orderId, String idempotencyKey) {
        return paymentJpaRepository.findByOrderIdAndIdempotencyKey(orderId, idempotencyKey);
    }
}
