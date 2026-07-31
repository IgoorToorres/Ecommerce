CREATE TABLE products (
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(1000),
    price NUMERIC(19, 2) NOT NULL,
    stock_quantity INTEGER NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL,

    CONSTRAINT chk_products_price_positive
      CHECK (price > 0),

    CONSTRAINT chk_products_stock_quantity_non_negative
      CHECK (stock_quantity >= 0)
);

CREATE INDEX idx_products_active
    ON products (active);

CREATE INDEX idx_products_name
    ON products (name);