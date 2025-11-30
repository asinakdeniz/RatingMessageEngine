CREATE TABLE rating_engine.billing_line
(
    id          BIGSERIAL PRIMARY KEY,
    contract_id VARCHAR(255)             NOT NULL,
    start_date  TIMESTAMP WITH TIME ZONE NOT NULL,
    end_date    TIMESTAMP WITH TIME ZONE NOT NULL,
    product_id  VARCHAR(255)             NOT NULL,
    consumption NUMERIC(19, 4)           NOT NULL,
    status      VARCHAR(50)              NOT NULL,
    CONSTRAINT chk_status CHECK (status IN ('UNPROCESSED', 'PROCESSED', 'FAILED'))
);


-- Indexes for performance
CREATE INDEX idx_billing_line_status ON rating_engine.billing_line (status);

CREATE TABLE rating_engine.billing_line_gold
(
    id           BIGSERIAL PRIMARY KEY,
    contract_id  VARCHAR(255)             NOT NULL,
    start_date   TIMESTAMP WITH TIME ZONE NOT NULL,
    end_date     TIMESTAMP WITH TIME ZONE NOT NULL,
    product_id   VARCHAR(255)             NOT NULL,
    price        NUMERIC(19, 4)           NOT NULL,
    created_date TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE rating_engine.product
(
    id          BIGSERIAL PRIMARY KEY,
    product_id  VARCHAR(255)   NOT NULL,
    price       NUMERIC(19, 4) NOT NULL,
    coefficient NUMERIC(4, 2)  NOT NULL,
    type        VARCHAR(50)    NOT NULL,
    formula     VARCHAR(500),
    monthly_fee NUMERIC(19, 4)
);

INSERT INTO rating_engine.product (product_id, price, type, coefficient, formula, monthly_fee)
VALUES ('PRODUCT_1', 99.99, 'SUBSCRIPTION', 1.15, 'BASE_PRICE * COEFFICIENT', 9.99),
       ('PRODUCT_2', 29.99, 'SUBSCRIPTION', 1.05, 'BASE_PRICE * COEFFICIENT + MONTHLY_FEE', 4.99),
       ('PRODUCT_3', 199.99, 'PREMIUM', 1.25, 'BASE_PRICE * COEFFICIENT', 19.99),
       ('PRODUCT_4', 49.99, 'BASIC', 1.00, 'BASE_PRICE', 0.00);