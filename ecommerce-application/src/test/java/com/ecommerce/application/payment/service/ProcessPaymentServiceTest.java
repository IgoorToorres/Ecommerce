package com.ecommerce.application.payment.service;

import com.ecommerce.application.exception.ForbiddenException;
import com.ecommerce.application.exception.ResourceNotFoundException;
import com.ecommerce.application.order.repository.OrderRepository;
import com.ecommerce.application.order.response.PageResponse;
import com.ecommerce.application.payment.command.ProcessPaymentCommand;
import com.ecommerce.application.payment.mapper.PaymentResponseMapper;
import com.ecommerce.application.payment.repository.PaymentRepository;
import com.ecommerce.application.payment.response.PaymentResponse;
import com.ecommerce.domain.exception.DomainException;
import com.ecommerce.domain.order.Order;
import com.ecommerce.domain.order.OrderItem;
import com.ecommerce.domain.order.OrderStatus;
import com.ecommerce.domain.payment.Payment;
import com.ecommerce.domain.payment.PaymentMethod;
import com.ecommerce.domain.payment.PaymentStatus;
import com.ecommerce.domain.user.UserRole;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProcessPaymentServiceTest {

    @Test
    void shouldApprovePaymentAndMarkOrderAsPaid() {
        Order order = createOrder();
        FakeOrderRepository orderRepository = new FakeOrderRepository(List.of(order));
        FakePaymentRepository paymentRepository = new FakePaymentRepository();
        ProcessPaymentService service = createService(paymentRepository, orderRepository);
        ProcessPaymentCommand command = createCommand(
                order.getId(),
                order.getCustomerId(),
                UserRole.CUSTOMER,
                true,
                "payment-key-1"
        );

        PaymentResponse response = service.process(command);

        assertThat(response.status()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(response.orderId()).isEqualTo(order.getId());
        assertThat(response.amount()).isEqualByComparingTo(order.getTotalAmount());
        assertThat(response.method()).isEqualTo(PaymentMethod.PIX);
        assertThat(response.idempotencyKey()).isEqualTo("payment-key-1");
        assertThat(response.externalReference()).startsWith("SIMULATED-");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getPaidAt()).isNotNull();
        assertThat(paymentRepository.saveWasCalled()).isTrue();
        assertThat(orderRepository.saveWasCalled()).isTrue();
    }

    @Test
    void shouldRejectPaymentAndKeepOrderPendingPayment() {
        Order order = createOrder();
        FakeOrderRepository orderRepository = new FakeOrderRepository(List.of(order));
        FakePaymentRepository paymentRepository = new FakePaymentRepository();
        ProcessPaymentService service = createService(paymentRepository, orderRepository);
        ProcessPaymentCommand command = createCommand(
                order.getId(),
                order.getCustomerId(),
                UserRole.CUSTOMER,
                false,
                "payment-key-1"
        );

        PaymentResponse response = service.process(command);

        assertThat(response.status()).isEqualTo(PaymentStatus.REJECTED);
        assertThat(response.externalReference()).startsWith("SIMULATED-");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(order.getPaidAt()).isNull();
        assertThat(paymentRepository.saveWasCalled()).isTrue();
        assertThat(orderRepository.saveWasCalled()).isTrue();
    }

    @Test
    void shouldReturnExistingPaymentWhenIdempotencyKeyAlreadyExists() {
        Order order = createOrder();
        Payment existingPayment = new Payment(
                order.getId(),
                PaymentMethod.PIX,
                order.getTotalAmount(),
                "payment-key-1"
        );
        existingPayment.approve("external-123");
        FakeOrderRepository orderRepository = new FakeOrderRepository(List.of(order));
        FakePaymentRepository paymentRepository = new FakePaymentRepository(List.of(existingPayment));
        ProcessPaymentService service = createService(paymentRepository, orderRepository);
        ProcessPaymentCommand command = createCommand(
                order.getId(),
                order.getCustomerId(),
                UserRole.CUSTOMER,
                true,
                "payment-key-1"
        );

        PaymentResponse response = service.process(command);

        assertThat(response.id()).isEqualTo(existingPayment.getId());
        assertThat(response.status()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(response.externalReference()).isEqualTo("external-123");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(paymentRepository.saveWasCalled()).isFalse();
        assertThat(orderRepository.saveWasCalled()).isFalse();
    }

    @Test
    void shouldThrowWhenOrderDoesNotExist() {
        FakeOrderRepository orderRepository = new FakeOrderRepository(List.of());
        FakePaymentRepository paymentRepository = new FakePaymentRepository();
        ProcessPaymentService service = createService(paymentRepository, orderRepository);
        ProcessPaymentCommand command = createCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UserRole.CUSTOMER,
                true,
                "payment-key-1"
        );

        assertThatThrownBy(() -> service.process(command))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Pedido não encontrado.");

        assertThat(paymentRepository.saveWasCalled()).isFalse();
        assertThat(orderRepository.saveWasCalled()).isFalse();
    }

    @Test
    void shouldThrowWhenCustomerTriesToPayAnotherCustomerOrder() {
        Order order = createOrder();
        FakeOrderRepository orderRepository = new FakeOrderRepository(List.of(order));
        FakePaymentRepository paymentRepository = new FakePaymentRepository();
        ProcessPaymentService service = createService(paymentRepository, orderRepository);
        ProcessPaymentCommand command = createCommand(
                order.getId(),
                UUID.randomUUID(),
                UserRole.CUSTOMER,
                true,
                "payment-key-1"
        );

        assertThatThrownBy(() -> service.process(command))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Você não tem permissão para pagar este pedido.");

        assertThat(paymentRepository.saveWasCalled()).isFalse();
        assertThat(orderRepository.saveWasCalled()).isFalse();
    }

    @Test
    void shouldThrowWhenOrderIsAlreadyPaid() {
        Order order = createOrder();
        order.markAsPaid();
        FakeOrderRepository orderRepository = new FakeOrderRepository(List.of(order));
        FakePaymentRepository paymentRepository = new FakePaymentRepository();
        ProcessPaymentService service = createService(paymentRepository, orderRepository);
        ProcessPaymentCommand command = createCommand(
                order.getId(),
                order.getCustomerId(),
                UserRole.CUSTOMER,
                true,
                "payment-key-1"
        );

        assertThatThrownBy(() -> service.process(command))
                .isInstanceOf(DomainException.class)
                .hasMessage("O pedido só pode ser pago quando estiver aguardando pagamento.");

        assertThat(paymentRepository.saveWasCalled()).isFalse();
        assertThat(orderRepository.saveWasCalled()).isFalse();
    }

    private ProcessPaymentService createService(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository
    ) {
        return new ProcessPaymentService(
                paymentRepository,
                orderRepository,
                new PaymentResponseMapper()
        );
    }

    private ProcessPaymentCommand createCommand(
            UUID orderId,
            UUID authenticatedUserId,
            UserRole authenticatedUserRole,
            boolean approved,
            String idempotencyKey
    ) {
        return new ProcessPaymentCommand(
                orderId,
                authenticatedUserId,
                authenticatedUserRole,
                PaymentMethod.PIX,
                idempotencyKey,
                approved
        );
    }

    private Order createOrder() {
        OrderItem item = new OrderItem(
                UUID.randomUUID(),
                "Mouse",
                BigDecimal.valueOf(100),
                2
        );

        return new Order(UUID.randomUUID(), List.of(item));
    }

    private static final class FakePaymentRepository implements PaymentRepository {

        private final List<Payment> payments;
        private Payment savedPayment;

        private FakePaymentRepository() {
            this(List.of());
        }

        private FakePaymentRepository(List<Payment> payments) {
            this.payments = new ArrayList<>(payments);
        }

        @Override
        public Payment save(Payment payment) {
            this.savedPayment = payment;
            this.payments.add(payment);
            return payment;
        }

        @Override
        public Optional<Payment> findByOrderIdAndIdempotencyKey(UUID orderId, String idempotencyKey) {
            return payments.stream()
                    .filter(payment -> payment.getOrderId().equals(orderId))
                    .filter(payment -> payment.getIdempotencyKey().equals(idempotencyKey))
                    .findFirst();
        }

        private boolean saveWasCalled() {
            return savedPayment != null;
        }
    }

    private static final class FakeOrderRepository implements OrderRepository {

        private final List<Order> orders;
        private Order savedOrder;

        private FakeOrderRepository(List<Order> orders) {
            this.orders = new ArrayList<>(orders);
        }

        @Override
        public Order save(Order order) {
            this.savedOrder = order;
            return order;
        }

        @Override
        public Optional<Order> findById(UUID id) {
            return orders.stream()
                    .filter(order -> order.getId().equals(id))
                    .findFirst();
        }

        @Override
        public List<Order> findAll() {
            return List.copyOf(orders);
        }

        @Override
        public PageResponse<Order> findAll(UUID customerId, OrderStatus status, int page, int size) {
            return new PageResponse<>(List.of(), page, size, 0, 0);
        }

        private boolean saveWasCalled() {
            return savedOrder != null;
        }
    }
}
