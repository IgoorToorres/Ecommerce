package com.ecommerce.application.payment.mapper;

import com.ecommerce.application.payment.response.PaymentResponse;
import com.ecommerce.domain.payment.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentResponseMapper {
    public PaymentResponse toResponse(Payment payment){
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getStatus(),
                payment.getMethod(),
                payment.getAmount(),
                payment.getIdempotencyKey(),
                payment.getExternalReference(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
