package com.ecommerce.application.payment.command;

import com.ecommerce.domain.payment.PaymentMethod;
import com.ecommerce.domain.user.UserRole;

import java.util.UUID;

public record ProcessPaymentCommand (
        UUID orderId,
        UUID authenticatedUserId,
        UserRole authenticatedUserRole,
        PaymentMethod method,
        String idempotencyKey,
        boolean approved
){
}
