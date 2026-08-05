CREATE TABLE users (
   id UUID PRIMARY KEY,
   name VARCHAR(150) NOT NULL,
   email VARCHAR(255) NOT NULL,
   password_hash VARCHAR(255) NOT NULL,
   role VARCHAR(30) NOT NULL,
   active BOOLEAN NOT NULL,
   created_at TIMESTAMPTZ NOT NULL,
   updated_at TIMESTAMPTZ NOT NULL,
   version BIGINT NOT NULL,

   CONSTRAINT uk_users_email UNIQUE (email),

   CONSTRAINT chk_users_role
       CHECK (role IN ('ADMIN', 'CUSTOMER'))
);

CREATE INDEX idx_users_email
    ON users (email);

CREATE INDEX idx_users_active
    ON users (active);