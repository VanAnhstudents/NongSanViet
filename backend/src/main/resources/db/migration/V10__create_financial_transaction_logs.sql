-- =====================================================
-- V10: Tạo bảng financial_transaction_logs
-- Khớp với entity common.audit.entity.FinancialTransactionLog
-- =====================================================

CREATE TABLE IF NOT EXISTS financial_transaction_logs (
    id               BIGINT GENERATED ALWAYS AS IDENTITY,
    order_id         BIGINT          NOT NULL,
    user_id          BIGINT          NOT NULL,
    actor_id         BIGINT          NOT NULL,
    transaction_type VARCHAR(40)     NOT NULL,
    payment_method   VARCHAR(30)     NOT NULL,
    amount           NUMERIC(15,2)   NOT NULL,
    status           VARCHAR(20)     NOT NULL,
    provider_ref     VARCHAR(120),
    note             TEXT,
    created_at       TIMESTAMPTZ     NOT NULL DEFAULT now(),
    CONSTRAINT pk_financial_transaction_logs PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_ftl_order_id ON financial_transaction_logs (order_id);
CREATE INDEX IF NOT EXISTS idx_ftl_user_id  ON financial_transaction_logs (user_id);
