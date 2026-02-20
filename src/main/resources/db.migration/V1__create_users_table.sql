CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS users (
    id                      UUID          PRIMARY KEY,
    name                    VARCHAR(255)  NOT NULL,
    role                    VARCHAR(255)  NOT NULL,
    password                VARCHAR       NOT NULL,
    email                   VARCHAR(255)  NOT NULL UNIQUE,
    email_validated_at      TIMESTAMP,
    created_at              TIMESTAMP,
    password_recovery_code  VARCHAR(10)
)
