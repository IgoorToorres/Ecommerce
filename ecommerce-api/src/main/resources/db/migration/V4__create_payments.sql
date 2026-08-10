CREATE TABLE payments (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    status VARCHAR(30) NOT NULL,
    method VARCHAR(30) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    idempotency_key VARCHAR(100) NOT NULL,
    external_reference VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL,

    CONSTRAINT fk_payments_order
        FOREIGN KEY (order_id)
        REFERENCES orders (id),

    CONSTRAINT chk_payments_status
        CHECK (status IN (
            'PENDING',
            'APPROVED',
            'REJECTED',
            'CANCELLED'
        )),

    CONSTRAINT chk_payments_method
        CHECK (method IN (
            'CREDIT_CARD',
            'PIX'
        )),

    CONSTRAINT chk_payments_amount_positive
        CHECK (amount > 0),

    CONSTRAINT uk_payments_order_id_idempotency_key
        UNIQUE (order_id, idempotency_key)
);

CREATE INDEX idx_payments_order_id
    ON payments (order_id);

CREATE INDEX idx_payments_status
    ON payments (status);
