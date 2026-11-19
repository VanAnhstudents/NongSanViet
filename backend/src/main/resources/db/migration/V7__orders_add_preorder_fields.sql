-- V7: Bo sung thong tin dat truoc (pre-order) cho don hang
ALTER TABLE orders ADD COLUMN IF NOT EXISTS order_type VARCHAR(20) NOT NULL DEFAULT 'STANDARD';
ALTER TABLE orders ADD COLUMN IF NOT EXISTS deposit_rate NUMERIC(5, 4);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS deposit_amount NUMERIC(15, 2) NOT NULL DEFAULT 0;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS remaining_amount NUMERIC(15, 2) NOT NULL DEFAULT 0;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS expected_harvest_date DATE;

ALTER TABLE orders DROP CONSTRAINT IF EXISTS ck_orders_status;
ALTER TABLE orders ADD CONSTRAINT ck_orders_status
    CHECK (status IN (
        'PENDING', 'CONFIRMED', 'SHIPPING', 'DELIVERED', 'COMPLETED', 'CANCELLED',
        'AWAITING_DEPOSIT', 'DEPOSIT_PAID'
    ));

ALTER TABLE orders DROP CONSTRAINT IF EXISTS ck_orders_order_type;
ALTER TABLE orders ADD CONSTRAINT ck_orders_order_type
    CHECK (order_type IN ('STANDARD', 'PRE_ORDER'));

ALTER TABLE orders DROP CONSTRAINT IF EXISTS ck_orders_deposit_rate;
ALTER TABLE orders ADD CONSTRAINT ck_orders_deposit_rate
    CHECK (deposit_rate IS NULL OR (deposit_rate >= 0 AND deposit_rate <= 1));

ALTER TABLE orders DROP CONSTRAINT IF EXISTS ck_orders_deposit_amount;
ALTER TABLE orders ADD CONSTRAINT ck_orders_deposit_amount
    CHECK (deposit_amount >= 0);

ALTER TABLE orders DROP CONSTRAINT IF EXISTS ck_orders_remaining_amount;
ALTER TABLE orders ADD CONSTRAINT ck_orders_remaining_amount
    CHECK (remaining_amount >= 0);

CREATE INDEX IF NOT EXISTS idx_orders_buyer_type_created
    ON orders (buyer_id, order_type, created_at DESC);
