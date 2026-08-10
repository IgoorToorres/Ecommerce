package com.ecommerce.domain.payment;

import com.ecommerce.domain.exception.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "order_id", nullable = false, updatable = false)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 30, updatable = false)
    private PaymentMethod method;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal amount;

    @Column(name = "idempotency_key", nullable = false, length = 100, updatable = false)
    private String idempotencyKey;

    @Column(name = "external_reference", length = 100)
    private String externalReference;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected Payment(){
    }

    public Payment(
            UUID orderId,
            PaymentMethod method,
            BigDecimal amount,
            String idempotencyKey
    ){
        validateOrderId(orderId);
        validateMethod(method);
        validateAmount(amount);
        validateIdempotencyKey(idempotencyKey);

        this.id = UUID.randomUUID();
        this.orderId = orderId;
        this.method = method;
        this.amount = amount;
        this.idempotencyKey = idempotencyKey.trim();
        this.status = PaymentStatus.PENDING;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    private void validateOrderId(UUID orderId){
        if(orderId == null ){
            throw new DomainException("id do pedido é obrigatorio");
        }
    }

    private void validateMethod(PaymentMethod method){
        if(method ==  null){
            throw new DomainException("O metodo de pagamento é obrigatorio");
        }
    }

    private void validateAmount(BigDecimal amount){
        if(amount == null){
            throw new DomainException("O valor do pagamento é obrigatório");
        }
        if(amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new DomainException("O valor do pagamento deve ser maior que zero");
        }
    }

    private void validateIdempotencyKey(String idempotencyKey){
        if(idempotencyKey == null || idempotencyKey.isBlank()){
            throw new DomainException("IdempotencyKey é obrigatoria");
        }
    }

    public void approve(String externalReference){
        if(this.status != PaymentStatus.PENDING){
            throw new DomainException("Só pode aprovar pagamento pendente.");
        }

        this.status = PaymentStatus.APPROVED;
        this.externalReference = normalizeExternalReference(externalReference);
        touch();
    }

    public void reject(String externalReference){
        if(this.status != PaymentStatus.PENDING){
            throw new DomainException("Só pode recusar pagamento pendente.");
        }

        this.status = PaymentStatus.REJECTED;
        this.externalReference = normalizeExternalReference(externalReference);
        touch();
    }

    public void cancel(){
        if(this.status != PaymentStatus.PENDING){
            throw new DomainException("Só pode cancelar pagamento pendente.");
        }

        this.status = PaymentStatus.CANCELLED;
        touch();
    }

    private String normalizeExternalReference(String externalReference) {
        if (externalReference == null || externalReference.isBlank()) {
            return null;
        }

        return externalReference.trim();
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public long getVersion() {
        return version;
    }

}
