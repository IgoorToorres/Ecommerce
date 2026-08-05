package com.ecommerce.application.order.service;

import com.ecommerce.application.exception.ResourceNotFoundException;
import com.ecommerce.application.order.command.CreateOrderCommand;
import com.ecommerce.application.order.command.CreateOrderItemCommand;
import com.ecommerce.application.order.repository.OrderRepository;
import com.ecommerce.application.order.response.OrderResponse;
import com.ecommerce.application.product.repository.ProductRepository;
import com.ecommerce.domain.exception.DomainException;
import com.ecommerce.domain.order.Order;
import com.ecommerce.domain.order.OrderStatus;
import com.ecommerce.domain.product.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateOrderServiceTest {

    @Test
    void shouldCreateOrder() {
        Product mouse = createProduct("Mouse", BigDecimal.valueOf(100), 5);
        Product keyboard = createProduct("Teclado", BigDecimal.valueOf(250), 3);
        FakeOrderRepository orderRepository = new FakeOrderRepository();
        FakeProductRepository productRepository = new FakeProductRepository(List.of(mouse, keyboard));
        CreateOrderService service = new CreateOrderService(orderRepository, productRepository);
        CreateOrderCommand command = new CreateOrderCommand(
                UUID.randomUUID(),
                List.of(
                        new CreateOrderItemCommand(mouse.getId(), 2),
                        new CreateOrderItemCommand(keyboard.getId(), 1)
                )
        );

        OrderResponse response = service.create(command);

        assertThat(response.id()).isNotNull();
        assertThat(response.customerId()).isEqualTo(command.customerId());
        assertThat(response.status()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(response.totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(450));
        assertThat(response.createdAt()).isNotNull();
        assertThat(response.items()).hasSize(2);
        assertThat(response.items().getFirst().productId()).isEqualTo(mouse.getId());
        assertThat(response.items().getFirst().productName()).isEqualTo("Mouse");
        assertThat(response.items().getFirst().unitPrice()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(response.items().getFirst().quantity()).isEqualTo(2);
        assertThat(response.items().getFirst().totalPrice()).isEqualByComparingTo(BigDecimal.valueOf(200));

        assertThat(mouse.getStockQuantity()).isEqualTo(3);
        assertThat(keyboard.getStockQuantity()).isEqualTo(2);
        assertThat(orderRepository.saveWasCalled()).isTrue();
        assertThat(orderRepository.savedOrder()).isNotNull();
    }

    @Test
    void shouldThrowErrorWhenProductDoesNotExist() {
        FakeOrderRepository orderRepository = new FakeOrderRepository();
        FakeProductRepository productRepository = new FakeProductRepository(List.of());
        CreateOrderService service = new CreateOrderService(orderRepository, productRepository);
        CreateOrderCommand command = new CreateOrderCommand(
                UUID.randomUUID(),
                List.of(new CreateOrderItemCommand(UUID.randomUUID(), 1))
        );

        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Produto não encontrado.");

        assertThat(orderRepository.saveWasCalled()).isFalse();
    }

    @Test
    void shouldThrowErrorWhenProductIsInactive() {
        Product product = createProduct("Mouse", BigDecimal.valueOf(100), 5);
        product.deactivate();
        FakeOrderRepository orderRepository = new FakeOrderRepository();
        FakeProductRepository productRepository = new FakeProductRepository(List.of(product));
        CreateOrderService service = new CreateOrderService(orderRepository, productRepository);
        CreateOrderCommand command = new CreateOrderCommand(
                UUID.randomUUID(),
                List.of(new CreateOrderItemCommand(product.getId(), 1))
        );

        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(DomainException.class)
                .hasMessage("Produto inativo não pode ser adicionado ao pedido.");

        assertThat(product.getStockQuantity()).isEqualTo(5);
        assertThat(orderRepository.saveWasCalled()).isFalse();
    }

    @Test
    void shouldThrowErrorWhenThereIsNotEnoughStock() {
        Product product = createProduct("Mouse", BigDecimal.valueOf(100), 1);
        FakeOrderRepository orderRepository = new FakeOrderRepository();
        FakeProductRepository productRepository = new FakeProductRepository(List.of(product));
        CreateOrderService service = new CreateOrderService(orderRepository, productRepository);
        CreateOrderCommand command = new CreateOrderCommand(
                UUID.randomUUID(),
                List.of(new CreateOrderItemCommand(product.getId(), 2))
        );

        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(DomainException.class)
                .hasMessage("Estoque insuficiente.");

        assertThat(product.getStockQuantity()).isEqualTo(1);
        assertThat(orderRepository.saveWasCalled()).isFalse();
    }

    @Test
    void shouldThrowErrorWhenQuantityIsInvalid() {
        Product product = createProduct("Mouse", BigDecimal.valueOf(100), 5);
        FakeOrderRepository orderRepository = new FakeOrderRepository();
        FakeProductRepository productRepository = new FakeProductRepository(List.of(product));
        CreateOrderService service = new CreateOrderService(orderRepository, productRepository);
        CreateOrderCommand command = new CreateOrderCommand(
                UUID.randomUUID(),
                List.of(new CreateOrderItemCommand(product.getId(), 0))
        );

        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(DomainException.class);

        assertThat(product.getStockQuantity()).isEqualTo(5);
        assertThat(orderRepository.saveWasCalled()).isFalse();
    }

    private Product createProduct(String name, BigDecimal price, int stockQuantity) {
        return new Product(
                name,
                name + " descrição",
                price,
                stockQuantity
        );
    }

    private static final class FakeOrderRepository implements OrderRepository {

        private Order savedOrder;

        @Override
        public Order save(Order order) {
            this.savedOrder = order;
            return order;
        }

        @Override
        public Optional<Order> findById(UUID id) {
            return Optional.empty();
        }

        @Override
        public List<Order> findAll() {
            return List.of();
        }

        private boolean saveWasCalled() {
            return savedOrder != null;
        }

        private Order savedOrder() {
            return savedOrder;
        }
    }

    private static final class FakeProductRepository implements ProductRepository {

        private final List<Product> products;

        private FakeProductRepository(List<Product> products) {
            this.products = new ArrayList<>(products);
        }

        @Override
        public Product save(Product product) {
            return product;
        }

        @Override
        public Optional<Product> findById(UUID id) {
            return products.stream()
                    .filter(product -> product.getId().equals(id))
                    .findFirst();
        }

        @Override
        public List<Product> findAll() {
            return products;
        }
    }
}
