package com.ecommerce.application.order.service;

import com.ecommerce.application.order.command.ListOrdersCommand;
import com.ecommerce.application.order.mapper.OrderResponseMapper;
import com.ecommerce.application.order.repository.OrderRepository;
import com.ecommerce.application.order.response.OrderResponse;
import com.ecommerce.application.order.response.PageResponse;
import com.ecommerce.domain.order.Order;
import com.ecommerce.domain.order.OrderItem;
import com.ecommerce.domain.order.OrderStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ListOrdersServiceTest {

    @Test
    void shouldListOrdersWithPaginationAndFilters() {
        UUID customerId = UUID.randomUUID();
        Order order = new Order(
                customerId,
                List.of(new OrderItem(
                        UUID.randomUUID(),
                        "Mouse",
                        BigDecimal.valueOf(100),
                        2
                ))
        );
        FakeOrderRepository orderRepository = new FakeOrderRepository(List.of(order));
        ListOrdersService service = new ListOrdersService(orderRepository, new OrderResponseMapper());
        ListOrdersCommand command = new ListOrdersCommand(customerId, OrderStatus.PENDING_PAYMENT, 0, 10);

        PageResponse<OrderResponse> response = service.findAll(command);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().getFirst().id()).isEqualTo(order.getId());
        assertThat(response.content().getFirst().customerId()).isEqualTo(customerId);
        assertThat(response.content().getFirst().status()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(response.content().getFirst().totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(200));
        assertThat(response.content().getFirst().items()).hasSize(1);
        assertThat(response.page()).isEqualTo(0);
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.totalPages()).isEqualTo(1);
        assertThat(orderRepository.receivedCustomerId()).isEqualTo(customerId);
        assertThat(orderRepository.receivedStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(orderRepository.receivedPage()).isEqualTo(0);
        assertThat(orderRepository.receivedSize()).isEqualTo(10);
    }

    @Test
    void shouldReturnEmptyPageWhenThereAreNoOrders() {
        FakeOrderRepository orderRepository = new FakeOrderRepository(List.of());
        ListOrdersService service = new ListOrdersService(orderRepository, new OrderResponseMapper());
        ListOrdersCommand command = new ListOrdersCommand(null, null, 1, 20);

        PageResponse<OrderResponse> response = service.findAll(command);

        assertThat(response.content()).isEmpty();
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(20);
        assertThat(response.totalElements()).isZero();
        assertThat(response.totalPages()).isZero();
    }

    private static final class FakeOrderRepository implements OrderRepository {

        private final List<Order> orders;
        private UUID receivedCustomerId;
        private OrderStatus receivedStatus;
        private int receivedPage;
        private int receivedSize;

        private FakeOrderRepository(List<Order> orders) {
            this.orders = orders;
        }

        @Override
        public Order save(Order order) {
            return order;
        }

        @Override
        public Optional<Order> findById(UUID id) {
            return Optional.empty();
        }

        @Override
        public List<Order> findAll() {
            return orders;
        }

        @Override
        public PageResponse<Order> findAll(UUID customerId, OrderStatus status, int page, int size) {
            this.receivedCustomerId = customerId;
            this.receivedStatus = status;
            this.receivedPage = page;
            this.receivedSize = size;

            return new PageResponse<>(
                    orders,
                    page,
                    size,
                    orders.size(),
                    orders.isEmpty() ? 0 : 1
            );
        }

        private UUID receivedCustomerId() {
            return receivedCustomerId;
        }

        private OrderStatus receivedStatus() {
            return receivedStatus;
        }

        private int receivedPage() {
            return receivedPage;
        }

        private int receivedSize() {
            return receivedSize;
        }
    }
}
