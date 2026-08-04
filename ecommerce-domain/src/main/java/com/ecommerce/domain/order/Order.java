package com.ecommerce.domain.order;

import com.ecommerce.domain.exception.DomainException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class Order {
    private UUID id;
    private UUID customerId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private Instant createdAt;
    private Instant paidAt;
    private Instant cancelledAt;
    private Instant shippedAt;
    private Instant deliveredAt;
    private List<OrderItem> items;

    public Order(UUID customerId, List<OrderItem> items) {
        validateCustomerId(customerId);
        validateItems(items);

        this.id = UUID.randomUUID();
        this.customerId = customerId;
        this.items = List.copyOf(items);
        this.totalAmount = calculateTotalAmount(items);
        this.status = OrderStatus.PENDING_PAYMENT;
        this.createdAt = Instant.now();
    }

    private void validateCustomerId(UUID customerId){
        if(customerId == null){
            throw new DomainException("O cliente do pedido é obrigatório.");
        }
    }

    private void validateItems(List<OrderItem> items){
        if(items == null || items.isEmpty()){
            throw new DomainException("O pedido precisa ter pelo menos um item.");
        }
        for(OrderItem item : items){
            if(item == null){
                throw new DomainException("O pedido não pode ter item nulo.");
            }
        }
    }

    private BigDecimal calculateTotalAmount(List<OrderItem> items){
        BigDecimal total = BigDecimal.ZERO;

        for(OrderItem item : items){
            total = total.add(item.getTotalPrice());
        }

        return total;
    }

    public void markAsPaid(){
        if(this.status != OrderStatus.PENDING_PAYMENT){
            throw new DomainException("O pedido só pode ser pago quando estiver aguardando pagamento.");
        }
        this.paidAt = Instant.now();
        this.status = OrderStatus.PAID;
    }

    public void markAsPreparing(){
        if(this.status != OrderStatus.PAID){
            throw new DomainException("O pedido só pode entrar em preparação após ser pago.");
        }
        this.status = OrderStatus.PREPARING;
    }

    public void markAsShipped(){
        if(this.status != OrderStatus.PREPARING){
            throw new DomainException("O pedido só pode ser enviado quando estiver em preparação.");
        }
        this.shippedAt = Instant.now();
        this.status = OrderStatus.SHIPPED;
    }

    public void markAsDelivered(){
        if(this.status != OrderStatus.SHIPPED){
            throw new DomainException("O pedido só pode ser entregue após envio.");
        }
        this.deliveredAt = Instant.now();
        this.status = OrderStatus.DELIVERED;
    }

    public void cancel(){
        if(this.status == OrderStatus.SHIPPED || this.status == OrderStatus.DELIVERED){
            throw new DomainException("Pedidos enviados ou entregues não podem ser cancelados.");
        }
        if(this.status == OrderStatus.CANCELLED){
            throw new DomainException("O pedido já está cancelado.");
        }
        this.cancelledAt = Instant.now();
        this.status = OrderStatus.CANCELLED;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public Instant getShippedAt() {
        return shippedAt;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }

    public List<OrderItem> getItems() {
        return items;
    }
}
