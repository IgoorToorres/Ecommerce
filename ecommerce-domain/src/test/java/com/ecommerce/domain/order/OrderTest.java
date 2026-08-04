package com.ecommerce.domain.order;

import com.ecommerce.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    @Test
    void shouldCreateOrder() {
        UUID customerId = UUID.randomUUID();
        OrderItem mouse = createOrderItem("Mouse", BigDecimal.valueOf(100), 2);
        OrderItem keyboard = createOrderItem("Teclado", BigDecimal.valueOf(250), 1);

        Order order = new Order(customerId, List.of(mouse, keyboard));

        assertThat(order.getId()).isNotNull();
        assertThat(order.getCustomerId()).isEqualTo(customerId);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(order.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(450));
        assertThat(order.getCreatedAt()).isNotNull();
        assertThat(order.getPaidAt()).isNull();
        assertThat(order.getCancelledAt()).isNull();
        assertThat(order.getShippedAt()).isNull();
        assertThat(order.getDeliveredAt()).isNull();
        assertThat(order.getItems()).containsExactly(mouse, keyboard);
    }

    @Test
    void shouldNotCreateOrderWhenCustomerIdIsNull() {
        assertThatThrownBy(() -> new Order(null, List.of(createOrderItem())))
                .isInstanceOf(DomainException.class)
                .hasMessage("O cliente do pedido é obrigatório.");
    }

    @Test
    void shouldNotCreateOrderWhenItemsIsNull() {
        assertThatThrownBy(() -> new Order(UUID.randomUUID(), null))
                .isInstanceOf(DomainException.class)
                .hasMessage("O pedido precisa ter pelo menos um item.");
    }

    @Test
    void shouldNotCreateOrderWhenItemsIsEmpty() {
        assertThatThrownBy(() -> new Order(UUID.randomUUID(), List.of()))
                .isInstanceOf(DomainException.class)
                .hasMessage("O pedido precisa ter pelo menos um item.");
    }

    @Test
    void shouldNotCreateOrderWhenThereIsNullItem() {
        assertThatThrownBy(() -> new Order(UUID.randomUUID(), createListWithNullItem()))
                .isInstanceOf(DomainException.class)
                .hasMessage("O pedido não pode ter item nulo.");
    }

    @Test
    void shouldReturnImmutableItems() {
        Order order = new Order(UUID.randomUUID(), List.of(createOrderItem()));

        assertThatThrownBy(() -> order.getItems().add(createOrderItem()))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldMarkOrderAsPaid() {
        Order order = createOrder();

        order.markAsPaid();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getPaidAt()).isNotNull();
    }

    @Test
    void shouldNotMarkOrderAsPaidWhenOrderIsAlreadyPaid() {
        Order order = createOrder();
        order.markAsPaid();

        assertThatThrownBy(order::markAsPaid)
                .isInstanceOf(DomainException.class)
                .hasMessage("O pedido só pode ser pago quando estiver aguardando pagamento.");
    }

    @Test
    void shouldNotMarkOrderAsPaidWhenOrderIsCancelled() {
        Order order = createOrder();
        order.cancel();

        assertThatThrownBy(order::markAsPaid)
                .isInstanceOf(DomainException.class)
                .hasMessage("O pedido só pode ser pago quando estiver aguardando pagamento.");
    }

    @Test
    void shouldMarkOrderAsPreparing() {
        Order order = createOrder();
        order.markAsPaid();

        order.markAsPreparing();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PREPARING);
    }

    @Test
    void shouldNotMarkOrderAsPreparingBeforePayment() {
        Order order = createOrder();

        assertThatThrownBy(order::markAsPreparing)
                .isInstanceOf(DomainException.class)
                .hasMessage("O pedido só pode entrar em preparação após ser pago.");
    }

    @Test
    void shouldMarkOrderAsShipped() {
        Order order = createPaidOrderInPreparation();

        order.markAsShipped();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        assertThat(order.getShippedAt()).isNotNull();
    }

    @Test
    void shouldNotMarkOrderAsShippedBeforePreparation() {
        Order order = createOrder();
        order.markAsPaid();

        assertThatThrownBy(order::markAsShipped)
                .isInstanceOf(DomainException.class)
                .hasMessage("O pedido só pode ser enviado quando estiver em preparação.");
    }

    @Test
    void shouldMarkOrderAsDelivered() {
        Order order = createPaidOrderInPreparation();
        order.markAsShipped();

        order.markAsDelivered();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(order.getDeliveredAt()).isNotNull();
    }

    @Test
    void shouldNotMarkOrderAsDeliveredBeforeShipping() {
        Order order = createPaidOrderInPreparation();

        assertThatThrownBy(order::markAsDelivered)
                .isInstanceOf(DomainException.class)
                .hasMessage("O pedido só pode ser entregue após envio.");
    }

    @Test
    void shouldCancelPendingOrder() {
        Order order = createOrder();

        order.cancel();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getCancelledAt()).isNotNull();
    }

    @Test
    void shouldCancelPaidOrder() {
        Order order = createOrder();
        order.markAsPaid();

        order.cancel();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getCancelledAt()).isNotNull();
    }

    @Test
    void shouldNotCancelShippedOrder() {
        Order order = createPaidOrderInPreparation();
        order.markAsShipped();

        assertThatThrownBy(order::cancel)
                .isInstanceOf(DomainException.class)
                .hasMessage("Pedidos enviados ou entregues não podem ser cancelados.");
    }

    @Test
    void shouldNotCancelDeliveredOrder() {
        Order order = createPaidOrderInPreparation();
        order.markAsShipped();
        order.markAsDelivered();

        assertThatThrownBy(order::cancel)
                .isInstanceOf(DomainException.class)
                .hasMessage("Pedidos enviados ou entregues não podem ser cancelados.");
    }

    @Test
    void shouldNotCancelOrderWhenOrderIsAlreadyCancelled() {
        Order order = createOrder();
        order.cancel();

        assertThatThrownBy(order::cancel)
                .isInstanceOf(DomainException.class)
                .hasMessage("O pedido já está cancelado.");
    }

    private Order createOrder() {
        return new Order(UUID.randomUUID(), List.of(createOrderItem()));
    }

    private Order createPaidOrderInPreparation() {
        Order order = createOrder();
        order.markAsPaid();
        order.markAsPreparing();
        return order;
    }

    private OrderItem createOrderItem() {
        return createOrderItem("Mouse", BigDecimal.valueOf(100), 2);
    }

    private OrderItem createOrderItem(String productName, BigDecimal unitPrice, int quantity) {
        return new OrderItem(
                UUID.randomUUID(),
                productName,
                unitPrice,
                quantity
        );
    }

    private List<OrderItem> createListWithNullItem() {
        return java.util.Arrays.asList(createOrderItem(), null);
    }
}
