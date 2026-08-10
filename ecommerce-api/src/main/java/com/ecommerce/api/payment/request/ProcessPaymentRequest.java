package com.ecommerce.api.payment.request;

import com.ecommerce.domain.payment.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record ProcessPaymentRequest(
        @NotNull(message = "O metodo é obrigatorio")
        PaymentMethod method,

        boolean approved
) {
}
