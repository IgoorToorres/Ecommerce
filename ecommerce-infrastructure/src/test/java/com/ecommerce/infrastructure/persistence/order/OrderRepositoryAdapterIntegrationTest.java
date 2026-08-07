package com.ecommerce.infrastructure.persistence.order;

import com.ecommerce.application.order.response.PageResponse;
import com.ecommerce.domain.order.Order;
import com.ecommerce.domain.order.OrderItem;
import com.ecommerce.domain.order.OrderStatus;
import com.ecommerce.domain.product.Product;
import com.ecommerce.infrastructure.persistence.PostgresIntegrationTest;
import com.ecommerce.infrastructure.persistence.product.ProductRepositoryAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class OrderRepositoryAdapterIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private OrderRepositoryAdapter orderRepositoryAdapter;

    @Autowired
    private ProductRepositoryAdapter productRepositoryAdapter;

    @Test
    void shouldSaveAndFindOrderWithItems() {
        Product product = productRepositoryAdapter.save(new Product(
                "Notebook",
                "Notebook para desenvolvimento",
                BigDecimal.valueOf(4500),
                10
        ));
        UUID customerId = UUID.randomUUID();
        Order order = createOrder(customerId, product, 2);

        Order savedOrder = orderRepositoryAdapter.save(order);
        Optional<Order> foundOrder = orderRepositoryAdapter.findById(savedOrder.getId());

        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get().getId()).isEqualTo(savedOrder.getId());
        assertThat(foundOrder.get().getCustomerId()).isEqualTo(customerId);
        assertThat(foundOrder.get().getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(foundOrder.get().getTotalAmount()).isEqualByComparingTo("9000.00");
        assertThat(foundOrder.get().getItems()).hasSize(1);
        assertThat(foundOrder.get().getItems().getFirst().getProductId()).isEqualTo(product.getId());
        assertThat(foundOrder.get().getItems().getFirst().getTotalPrice()).isEqualByComparingTo("9000.00");
    }

    @Test
    void shouldFindOrdersByCustomerIdAndStatusWithPagination() {
        Product product = productRepositoryAdapter.save(new Product(
                "Mouse",
                "Mouse sem fio",
                BigDecimal.valueOf(120),
                30
        ));
        UUID customerId = UUID.randomUUID();
        UUID otherCustomerId = UUID.randomUUID();

        Order pendingOrder = createOrder(customerId, product, 1);
        Order paidOrder = createOrder(customerId, product, 2);
        paidOrder.markAsPaid();
        Order otherCustomerOrder = createOrder(otherCustomerId, product, 1);

        orderRepositoryAdapter.save(pendingOrder);
        orderRepositoryAdapter.save(paidOrder);
        orderRepositoryAdapter.save(otherCustomerOrder);

        PageResponse<Order> page = orderRepositoryAdapter.findAll(
                customerId,
                OrderStatus.PENDING_PAYMENT,
                0,
                10
        );

        assertThat(page.content()).hasSize(1);
        assertThat(page.content().getFirst().getCustomerId()).isEqualTo(customerId);
        assertThat(page.content().getFirst().getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(page.totalElements()).isEqualTo(1);
        assertThat(page.totalPages()).isEqualTo(1);
    }

    private Order createOrder(UUID customerId, Product product, int quantity) {
        OrderItem item = new OrderItem(
                product.getId(),
                product.getName(),
                product.getPrice(),
                quantity
        );

        return new Order(customerId, java.util.List.of(item));
    }
}
