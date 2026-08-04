package com.ecommerce.domain.order;

import com.ecommerce.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderItemTest {

    @Test
    void shouldCreateOrderItem() {
        UUID productId = UUID.randomUUID();

        OrderItem item = new OrderItem(
                productId,
                "Mouse sem fio",
                BigDecimal.valueOf(100),
                2
        );

        assertThat(item.getId()).isNotNull();
        assertThat(item.getProductId()).isEqualTo(productId);
        assertThat(item.getProductName()).isEqualTo("Mouse sem fio");
        assertThat(item.getUnitPrice()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(item.getQuantity()).isEqualTo(2);
        assertThat(item.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(200));
    }

    @Test
    void shouldTrimProductName() {
        OrderItem item = new OrderItem(
                UUID.randomUUID(),
                " Mouse sem fio ",
                BigDecimal.valueOf(100),
                2
        );

        assertThat(item.getProductName()).isEqualTo("Mouse sem fio");
    }

    @Test
    void shouldCalculateTotalPrice() {
        OrderItem item = new OrderItem(
                UUID.randomUUID(),
                "Mouse sem fio",
                BigDecimal.valueOf(100),
                3
        );

        assertThat(item.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(300));
    }

    @Test
    void shouldNotCreateOrderItemWhenProductIdIsNull() {
        assertThatThrownBy(() -> new OrderItem(
                null,
                "Mouse sem fio",
                BigDecimal.valueOf(100),
                2
        )).isInstanceOf(DomainException.class);
    }

    @Test
    void shouldNotCreateOrderItemWhenProductNameIsNull() {
        assertThatThrownBy(() -> new OrderItem(
                UUID.randomUUID(),
                null,
                BigDecimal.valueOf(100),
                2
        )).isInstanceOf(DomainException.class);
    }

    @Test
    void shouldNotCreateOrderItemWhenProductNameIsBlank() {
        assertThatThrownBy(() -> new OrderItem(
                UUID.randomUUID(),
                " ",
                BigDecimal.valueOf(100),
                2
        )).isInstanceOf(DomainException.class);
    }

    @Test
    void shouldNotCreateOrderItemWhenUnitPriceIsNull() {
        assertThatThrownBy(() -> new OrderItem(
                UUID.randomUUID(),
                "Mouse sem fio",
                null,
                2
        )).isInstanceOf(DomainException.class);
    }

    @Test
    void shouldNotCreateOrderItemWhenUnitPriceIsZero() {
        assertThatThrownBy(() -> new OrderItem(
                UUID.randomUUID(),
                "Mouse sem fio",
                BigDecimal.ZERO,
                2
        )).isInstanceOf(DomainException.class);
    }

    @Test
    void shouldNotCreateOrderItemWhenUnitPriceIsNegative() {
        assertThatThrownBy(() -> new OrderItem(
                UUID.randomUUID(),
                "Mouse sem fio",
                BigDecimal.valueOf(-1),
                2
        )).isInstanceOf(DomainException.class);
    }

    @Test
    void shouldNotCreateOrderItemWhenQuantityIsZero() {
        assertThatThrownBy(() -> new OrderItem(
                UUID.randomUUID(),
                "Mouse sem fio",
                BigDecimal.valueOf(100),
                0
        )).isInstanceOf(DomainException.class);
    }

    @Test
    void shouldNotCreateOrderItemWhenQuantityIsNegative() {
        assertThatThrownBy(() -> new OrderItem(
                UUID.randomUUID(),
                "Mouse sem fio",
                BigDecimal.valueOf(100),
                -1
        )).isInstanceOf(DomainException.class);
    }
}
