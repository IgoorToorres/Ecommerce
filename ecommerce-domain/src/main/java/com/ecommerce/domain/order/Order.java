package com.ecommerce.domain.order;

import com.ecommerce.domain.exception.DomainException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "customer_id", nullable = false, updatable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "shipped_at")
    private Instant shippedAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderItem> items = new ArrayList<>();

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected Order(){}

    public Order(UUID customerId, List<OrderItem> items) {
        validateCustomerId(customerId);
        validateItems(items);

        this.id = UUID.randomUUID();
        this.customerId = customerId;
        this.items = new ArrayList<>(items);
        this.totalAmount = calculateTotalAmount(items);
        this.status = OrderStatus.PENDING_PAYMENT;
        this.createdAt = Instant.now();

        for (OrderItem item : this.items) {
            item.attachToOrder(this);
        }
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
        return List.copyOf(items);
    }

    public long getVersion() {
        return version;
    }
}
