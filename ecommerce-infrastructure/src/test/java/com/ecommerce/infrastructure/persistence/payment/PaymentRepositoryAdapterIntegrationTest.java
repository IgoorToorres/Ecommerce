package com.ecommerce.infrastructure.persistence.payment;

import com.ecommerce.domain.order.Order;
import com.ecommerce.domain.order.OrderItem;
import com.ecommerce.domain.payment.Payment;
import com.ecommerce.domain.payment.PaymentMethod;
import com.ecommerce.domain.payment.PaymentStatus;
import com.ecommerce.domain.product.Product;
import com.ecommerce.infrastructure.persistence.PostgresIntegrationTest;
import com.ecommerce.infrastructure.persistence.order.OrderRepositoryAdapter;
import com.ecommerce.infrastructure.persistence.product.ProductRepositoryAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentRepositoryAdapterIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private PaymentRepositoryAdapter paymentRepositoryAdapter;

    @Autowired
    private OrderRepositoryAdapter orderRepositoryAdapter;

    @Autowired
    private ProductRepositoryAdapter productRepositoryAdapter;

    @Test
    void shouldSaveAndFindPaymentByOrderIdAndIdempotencyKey() {
        Order order = createSavedOrder();
        Payment payment = new Payment(
                order.getId(),
                PaymentMethod.PIX,
                order.getTotalAmount(),
                "payment-key-1"
        );

        Payment savedPayment = paymentRepositoryAdapter.save(payment);
        Optional<Payment> foundPayment = paymentRepositoryAdapter.findByOrderIdAndIdempotencyKey(
                order.getId(),
                "payment-key-1"
        );

        assertThat(foundPayment).isPresent();
        assertThat(foundPayment.get().getId()).isEqualTo(savedPayment.getId());
        assertThat(foundPayment.get().getOrderId()).isEqualTo(order.getId());
        assertThat(foundPayment.get().getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(foundPayment.get().getMethod()).isEqualTo(PaymentMethod.PIX);
        assertThat(foundPayment.get().getAmount()).isEqualByComparingTo(order.getTotalAmount());
        assertThat(foundPayment.get().getIdempotencyKey()).isEqualTo("payment-key-1");
        assertThat(foundPayment.get().getCreatedAt()).isNotNull();
        assertThat(foundPayment.get().getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldReturnEmptyWhenPaymentDoesNotExist() {
        Optional<Payment> foundPayment = paymentRepositoryAdapter.findByOrderIdAndIdempotencyKey(
                UUID.randomUUID(),
                "payment-key-unknown"
        );

        assertThat(foundPayment).isEmpty();
    }

    private Order createSavedOrder() {
        Product product = productRepositoryAdapter.save(new Product(
                "Notebook",
                "Notebook para desenvolvimento",
                BigDecimal.valueOf(4500),
                10
        ));

        OrderItem item = new OrderItem(
                product.getId(),
                product.getName(),
                product.getPrice(),
                1
        );

        return orderRepositoryAdapter.save(new Order(UUID.randomUUID(), java.util.List.of(item)));
    }
}
