package com.ecommerce.domain.payment;

import com.ecommerce.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentTest {

    @Test
    void shouldCreatePendingPayment() {
        UUID orderId = UUID.randomUUID();

        Payment payment = new Payment(
                orderId,
                PaymentMethod.PIX,
                BigDecimal.valueOf(150),
                " payment-key-1 "
        );

        assertThat(payment.getId()).isNotNull();
        assertThat(payment.getOrderId()).isEqualTo(orderId);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(payment.getMethod()).isEqualTo(PaymentMethod.PIX);
        assertThat(payment.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(150));
        assertThat(payment.getIdempotencyKey()).isEqualTo("payment-key-1");
        assertThat(payment.getExternalReference()).isNull();
        assertThat(payment.getCreatedAt()).isNotNull();
        assertThat(payment.getUpdatedAt()).isEqualTo(payment.getCreatedAt());
    }

    @Test
    void shouldApprovePayment() {
        Payment payment = createPayment();

        payment.approve(" external-123 ");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(payment.getExternalReference()).isEqualTo("external-123");
        assertThat(payment.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldApprovePaymentWithoutExternalReference() {
        Payment payment = createPayment();

        payment.approve(" ");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(payment.getExternalReference()).isNull();
    }

    @Test
    void shouldRejectPayment() {
        Payment payment = createPayment();

        payment.reject(" external-456 ");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REJECTED);
        assertThat(payment.getExternalReference()).isEqualTo("external-456");
        assertThat(payment.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldCancelPayment() {
        Payment payment = createPayment();

        payment.cancel();

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(payment.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldNotCreatePaymentWithoutOrderId() {
        assertThatThrownBy(() -> new Payment(
                null,
                PaymentMethod.PIX,
                BigDecimal.valueOf(150),
                "payment-key-1"
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("id do pedido é obrigatorio");
    }

    @Test
    void shouldNotCreatePaymentWithoutMethod() {
        assertThatThrownBy(() -> new Payment(
                UUID.randomUUID(),
                null,
                BigDecimal.valueOf(150),
                "payment-key-1"
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("O metodo de pagamento é obrigatorio");
    }

    @Test
    void shouldNotCreatePaymentWithoutAmount() {
        assertThatThrownBy(() -> new Payment(
                UUID.randomUUID(),
                PaymentMethod.PIX,
                null,
                "payment-key-1"
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("O valor do pagamento é obrigatório");
    }

    @Test
    void shouldNotCreatePaymentWithZeroAmount() {
        assertThatThrownBy(() -> new Payment(
                UUID.randomUUID(),
                PaymentMethod.PIX,
                BigDecimal.ZERO,
                "payment-key-1"
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("O valor do pagamento deve ser maior que zero");
    }

    @Test
    void shouldNotCreatePaymentWithNegativeAmount() {
        assertThatThrownBy(() -> new Payment(
                UUID.randomUUID(),
                PaymentMethod.PIX,
                BigDecimal.valueOf(-1),
                "payment-key-1"
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("O valor do pagamento deve ser maior que zero");
    }

    @Test
    void shouldNotCreatePaymentWithoutIdempotencyKey() {
        assertThatThrownBy(() -> new Payment(
                UUID.randomUUID(),
                PaymentMethod.PIX,
                BigDecimal.valueOf(150),
                " "
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("IdempotencyKey é obrigatoria");
    }

    @Test
    void shouldNotApprovePaymentWhenItIsNotPending() {
        Payment payment = createPayment();
        payment.approve("external-123");

        assertThatThrownBy(() -> payment.approve("external-456"))
                .isInstanceOf(DomainException.class)
                .hasMessage("Só pode aprovar pagamento pendente.");
    }

    @Test
    void shouldNotRejectPaymentWhenItIsNotPending() {
        Payment payment = createPayment();
        payment.reject("external-123");

        assertThatThrownBy(() -> payment.reject("external-456"))
                .isInstanceOf(DomainException.class)
                .hasMessage("Só pode recusar pagamento pendente.");
    }

    @Test
    void shouldNotCancelPaymentWhenItIsNotPending() {
        Payment payment = createPayment();
        payment.cancel();

        assertThatThrownBy(payment::cancel)
                .isInstanceOf(DomainException.class)
                .hasMessage("Só pode cancelar pagamento pendente.");
    }

    private Payment createPayment() {
        return new Payment(
                UUID.randomUUID(),
                PaymentMethod.PIX,
                BigDecimal.valueOf(150),
                UUID.randomUUID().toString()
        );
    }
}
