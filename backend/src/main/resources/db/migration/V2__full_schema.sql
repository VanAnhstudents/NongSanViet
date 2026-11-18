-- =====================================================
-- V2: Đồng bộ schema CSDL đầy đủ + bổ sung email cho users
-- =====================================================
-- Lý do: PBI-08/Auth/AdminSeeder dùng email và updated_at, đồng thời
-- cho phép đăng ký chỉ bằng email. Vì vậy bảng users cần có email,
-- updated_at và phone phải nullable (khác với script DB ban đầu).
-- Các bảng còn lại giữ đúng theo script CSDL đã chốt.
-- =====================================================

-- ---------- users: bổ sung cột còn thiếu / nới ràng buộc ----------
ALTER TABLE users ADD COLUMN IF NOT EXISTS email VARCHAR(254);
ALTER TABLE users ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT now();

ALTER TABLE users ALTER COLUMN phone DROP NOT NULL;
ALTER TABLE users ALTER COLUMN phone TYPE VARCHAR(15);
ALTER TABLE users ALTER COLUMN password_hash TYPE VARCHAR(255);

-- Trạng thái tài khoản có PENDING theo UserStatus.
ALTER TABLE users DROP CONSTRAINT IF EXISTS ck_users_status;
ALTER TABLE users ADD CONSTRAINT ck_users_status
    CHECK (status IN ('PENDING', 'ACTIVE', 'LOCKED'));

-- Email là duy nhất khi có giá trị.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uq_users_email' AND conrelid = 'users'::regclass
    ) THEN
        ALTER TABLE users ADD CONSTRAINT uq_users_email UNIQUE (email);
    END IF;
END $$;

-- Bắt buộc có ít nhất email hoặc số điện thoại.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'ck_users_contact' AND conrelid = 'users'::regclass
    ) THEN
        ALTER TABLE users ADD CONSTRAINT ck_users_contact
            CHECK (email IS NOT NULL OR phone IS NOT NULL);
    END IF;
END $$;

-- =====================================================
-- Nhóm 1. Tài khoản, địa chỉ
-- =====================================================

CREATE TABLE IF NOT EXISTS provinces (
    id   BIGINT GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(100) NOT NULL,
    CONSTRAINT pk_provinces PRIMARY KEY (id),
    CONSTRAINT uq_provinces_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS categories (
    id   BIGINT GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(100) NOT NULL,
    CONSTRAINT pk_categories PRIMARY KEY (id),
    CONSTRAINT uq_categories_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS farms (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY,
    user_id            BIGINT       NOT NULL,
    province_id        BIGINT       NOT NULL,
    main_category_id   BIGINT       NOT NULL,
    farm_name          VARCHAR(150) NOT NULL,
    address_line       VARCHAR(255) NOT NULL,
    certificate_url    VARCHAR(500),
    certificate_status VARCHAR(20)  NOT NULL DEFAULT 'NONE',
    CONSTRAINT pk_farms PRIMARY KEY (id),
    CONSTRAINT uq_farms_user_id UNIQUE (user_id),
    CONSTRAINT fk_farms_users      FOREIGN KEY (user_id)          REFERENCES users (id),
    CONSTRAINT fk_farms_provinces  FOREIGN KEY (province_id)      REFERENCES provinces (id),
    CONSTRAINT fk_farms_categories FOREIGN KEY (main_category_id) REFERENCES categories (id),
    CONSTRAINT ck_farms_certificate_status
        CHECK (certificate_status IN ('NONE', 'PENDING', 'VERIFIED', 'REJECTED'))
);

CREATE TABLE IF NOT EXISTS addresses (
    id             BIGINT GENERATED ALWAYS AS IDENTITY,
    user_id        BIGINT       NOT NULL,
    province_id    BIGINT       NOT NULL,
    receiver_name  VARCHAR(100) NOT NULL,
    receiver_phone VARCHAR(15)  NOT NULL,
    address_line   VARCHAR(255) NOT NULL,
    CONSTRAINT pk_addresses PRIMARY KEY (id),
    CONSTRAINT fk_addresses_users     FOREIGN KEY (user_id)     REFERENCES users (id),
    CONSTRAINT fk_addresses_provinces FOREIGN KEY (province_id) REFERENCES provinces (id)
);

-- =====================================================
-- Nhóm 2. Sản phẩm và truy xuất nguồn gốc
-- =====================================================

CREATE TABLE IF NOT EXISTS products (
    id          BIGINT GENERATED ALWAYS AS IDENTITY,
    farmer_id   BIGINT       NOT NULL,
    category_id BIGINT       NOT NULL,
    name        VARCHAR(150) NOT NULL,
    description TEXT,
    unit        VARCHAR(20)  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_products PRIMARY KEY (id),
    CONSTRAINT fk_products_users      FOREIGN KEY (farmer_id)   REFERENCES users (id),
    CONSTRAINT fk_products_categories FOREIGN KEY (category_id) REFERENCES categories (id)
);

CREATE TABLE IF NOT EXISTS product_images (
    id         BIGINT GENERATED ALWAYS AS IDENTITY,
    product_id BIGINT       NOT NULL,
    image_url  VARCHAR(500) NOT NULL,
    sort_order INT          NOT NULL DEFAULT 0,
    CONSTRAINT pk_product_images PRIMARY KEY (id),
    CONSTRAINT fk_product_images_products FOREIGN KEY (product_id) REFERENCES products (id)
);

CREATE TABLE IF NOT EXISTS batches (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY,
    product_id         BIGINT         NOT NULL,
    traceability_code  UUID           NOT NULL,
    harvest_date       DATE           NOT NULL,
    unit_price         NUMERIC(15, 2) NOT NULL,
    total_quantity     NUMERIC(10, 2) NOT NULL,
    remaining_quantity NUMERIC(10, 2) NOT NULL,
    status             VARCHAR(20)    NOT NULL,
    created_at         TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT pk_batches PRIMARY KEY (id),
    CONSTRAINT uq_batches_traceability_code UNIQUE (traceability_code),
    CONSTRAINT fk_batches_products FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT ck_batches_unit_price     CHECK (unit_price >= 0),
    CONSTRAINT ck_batches_total_quantity CHECK (total_quantity > 0),
    CONSTRAINT ck_batches_remaining      CHECK (remaining_quantity >= 0 AND remaining_quantity <= total_quantity),
    CONSTRAINT ck_batches_status
        CHECK (status IN ('PRE_ORDER', 'AVAILABLE', 'SOLD_OUT', 'CLOSED'))
);

CREATE TABLE IF NOT EXISTS farming_logs (
    id        BIGINT GENERATED ALWAYS AS IDENTITY,
    batch_id  BIGINT       NOT NULL,
    log_date  DATE         NOT NULL,
    activity  VARCHAR(100) NOT NULL,
    note      TEXT,
    image_url VARCHAR(500),
    CONSTRAINT pk_farming_logs PRIMARY KEY (id),
    CONSTRAINT fk_farming_logs_batches FOREIGN KEY (batch_id) REFERENCES batches (id)
);

-- =====================================================
-- Nhóm 3. Giỏ hàng, đơn hàng, thanh toán
-- =====================================================

CREATE TABLE IF NOT EXISTS cart_items (
    id       BIGINT GENERATED ALWAYS AS IDENTITY,
    user_id  BIGINT         NOT NULL,
    batch_id BIGINT         NOT NULL,
    quantity NUMERIC(10, 2) NOT NULL,
    CONSTRAINT pk_cart_items PRIMARY KEY (id),
    CONSTRAINT uq_cart_items_user_batch UNIQUE (user_id, batch_id),
    CONSTRAINT fk_cart_items_users   FOREIGN KEY (user_id)  REFERENCES users (id),
    CONSTRAINT fk_cart_items_batches FOREIGN KEY (batch_id) REFERENCES batches (id),
    CONSTRAINT ck_cart_items_quantity CHECK (quantity > 0)
);

CREATE TABLE IF NOT EXISTS orders (
    id             BIGINT GENERATED ALWAYS AS IDENTITY,
    buyer_id       BIGINT         NOT NULL,
    address_id     BIGINT,
    payment_method VARCHAR(20)    NOT NULL,
    status         VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    shipping_fee   NUMERIC(15, 2) NOT NULL DEFAULT 0,
    delivered_at   TIMESTAMPTZ,
    created_at     TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT pk_orders PRIMARY KEY (id),
    CONSTRAINT fk_orders_users     FOREIGN KEY (buyer_id)   REFERENCES users (id),
    CONSTRAINT fk_orders_addresses FOREIGN KEY (address_id) REFERENCES addresses (id),
    CONSTRAINT ck_orders_payment_method CHECK (payment_method IN ('COD', 'ONLINE')),
    CONSTRAINT ck_orders_status
        CHECK (status IN ('PENDING', 'CONFIRMED', 'SHIPPING', 'DELIVERED', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT ck_orders_shipping_fee CHECK (shipping_fee >= 0)
);

CREATE TABLE IF NOT EXISTS order_items (
    id         BIGINT GENERATED ALWAYS AS IDENTITY,
    order_id   BIGINT         NOT NULL,
    batch_id   BIGINT         NOT NULL,
    quantity   NUMERIC(10, 2) NOT NULL,
    unit_price NUMERIC(15, 2) NOT NULL,
    CONSTRAINT pk_order_items PRIMARY KEY (id),
    CONSTRAINT fk_order_items_orders  FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_order_items_batches FOREIGN KEY (batch_id) REFERENCES batches (id),
    CONSTRAINT ck_order_items_quantity   CHECK (quantity >= 0),
    CONSTRAINT ck_order_items_unit_price CHECK (unit_price >= 0)
);

CREATE TABLE IF NOT EXISTS order_addresses (
    id             BIGINT GENERATED ALWAYS AS IDENTITY,
    order_id       BIGINT       NOT NULL,
    receiver_name  VARCHAR(100) NOT NULL,
    receiver_phone VARCHAR(15),
    province_name  VARCHAR(100) NOT NULL,
    address_line   VARCHAR(255) NOT NULL,
    CONSTRAINT pk_order_addresses PRIMARY KEY (id),
    CONSTRAINT uq_order_addresses_order_id UNIQUE (order_id),
    CONSTRAINT fk_order_addresses_orders FOREIGN KEY (order_id) REFERENCES orders (id)
);

CREATE TABLE IF NOT EXISTS payments (
    id           BIGINT GENERATED ALWAYS AS IDENTITY,
    order_id     BIGINT         NOT NULL,
    payment_type VARCHAR(20)    NOT NULL,
    amount       NUMERIC(15, 2) NOT NULL,
    status       VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    provider_ref VARCHAR(100),
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT pk_payments PRIMARY KEY (id),
    CONSTRAINT fk_payments_orders FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT ck_payments_type   CHECK (payment_type IN ('FULL', 'DEPOSIT', 'REMAINING')),
    CONSTRAINT ck_payments_amount CHECK (amount > 0),
    CONSTRAINT ck_payments_status CHECK (status IN ('PENDING', 'PAID', 'FAILED', 'REFUNDED'))
);

-- =====================================================
-- Nhóm 4. Đánh giá, khiếu nại, hoàn tiền
-- =====================================================

CREATE TABLE IF NOT EXISTS reviews (
    id            BIGINT GENERATED ALWAYS AS IDENTITY,
    order_item_id BIGINT      NOT NULL,
    rating        SMALLINT    NOT NULL,
    comment       TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_reviews PRIMARY KEY (id),
    CONSTRAINT uq_reviews_order_item_id UNIQUE (order_item_id),
    CONSTRAINT fk_reviews_order_items FOREIGN KEY (order_item_id) REFERENCES order_items (id),
    CONSTRAINT ck_reviews_rating CHECK (rating BETWEEN 1 AND 5)
);

CREATE TABLE IF NOT EXISTS complaints (
    id              BIGINT GENERATED ALWAYS AS IDENTITY,
    order_id        BIGINT      NOT NULL,
    reason          TEXT        NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    farmer_response TEXT,
    admin_decision  TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_complaints PRIMARY KEY (id),
    CONSTRAINT fk_complaints_orders FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT ck_complaints_status
        CHECK (status IN ('PENDING', 'RESPONDED', 'ESCALATED', 'RESOLVED', 'REJECTED'))
);

CREATE TABLE IF NOT EXISTS complaint_evidences (
    id           BIGINT GENERATED ALWAYS AS IDENTITY,
    complaint_id BIGINT       NOT NULL,
    file_url     VARCHAR(500) NOT NULL,
    file_type    VARCHAR(10)  NOT NULL,
    CONSTRAINT pk_complaint_evidences PRIMARY KEY (id),
    CONSTRAINT fk_complaint_evidences_complaints FOREIGN KEY (complaint_id) REFERENCES complaints (id),
    CONSTRAINT ck_complaint_evidences_file_type CHECK (file_type IN ('IMAGE', 'VIDEO'))
);

CREATE TABLE IF NOT EXISTS refunds (
    id           BIGINT GENERATED ALWAYS AS IDENTITY,
    payment_id   BIGINT         NOT NULL,
    complaint_id BIGINT,
    amount       NUMERIC(15, 2) NOT NULL,
    reason       VARCHAR(255)   NOT NULL,
    status       VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT now(),
    processed_at TIMESTAMPTZ,
    CONSTRAINT pk_refunds PRIMARY KEY (id),
    CONSTRAINT fk_refunds_payments   FOREIGN KEY (payment_id)   REFERENCES payments (id),
    CONSTRAINT fk_refunds_complaints FOREIGN KEY (complaint_id) REFERENCES complaints (id),
    CONSTRAINT ck_refunds_amount CHECK (amount > 0),
    CONSTRAINT ck_refunds_status CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED'))
);

-- =====================================================
-- Nhóm 5. Thông báo, tin nhắn, nhật ký
-- =====================================================

CREATE TABLE IF NOT EXISTS notifications (
    id         BIGINT GENERATED ALWAYS AS IDENTITY,
    user_id    BIGINT       NOT NULL,
    content    VARCHAR(500) NOT NULL,
    is_read    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_notifications PRIMARY KEY (id),
    CONSTRAINT fk_notifications_users FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS messages (
    id          BIGINT GENERATED ALWAYS AS IDENTITY,
    sender_id   BIGINT      NOT NULL,
    receiver_id BIGINT      NOT NULL,
    content     TEXT        NOT NULL,
    sent_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_messages PRIMARY KEY (id),
    CONSTRAINT fk_messages_sender   FOREIGN KEY (sender_id)   REFERENCES users (id),
    CONSTRAINT fk_messages_receiver FOREIGN KEY (receiver_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id          BIGINT GENERATED ALWAYS AS IDENTITY,
    actor_id    BIGINT,
    action      VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id   BIGINT      NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_audit_logs PRIMARY KEY (id),
    CONSTRAINT fk_audit_logs_users FOREIGN KEY (actor_id) REFERENCES users (id)
);

-- =====================================================
-- Combo sản phẩm
-- =====================================================

CREATE TABLE IF NOT EXISTS combos (
    id          BIGINT GENERATED ALWAYS AS IDENTITY,
    farmer_id   BIGINT         NOT NULL,
    name        VARCHAR(150)   NOT NULL,
    description TEXT,
    combo_price NUMERIC(15, 2) NOT NULL,
    status      VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT pk_combos PRIMARY KEY (id),
    CONSTRAINT fk_combos_users FOREIGN KEY (farmer_id) REFERENCES users (id),
    CONSTRAINT ck_combos_price CHECK (combo_price >= 0),
    CONSTRAINT ck_combos_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE TABLE IF NOT EXISTS combo_items (
    id         BIGINT GENERATED ALWAYS AS IDENTITY,
    combo_id   BIGINT         NOT NULL,
    product_id BIGINT         NOT NULL,
    quantity   NUMERIC(10, 2) NOT NULL,
    CONSTRAINT pk_combo_items PRIMARY KEY (id),
    CONSTRAINT uq_combo_items_combo_product UNIQUE (combo_id, product_id),
    CONSTRAINT fk_combo_items_combos   FOREIGN KEY (combo_id)   REFERENCES combos (id) ON DELETE CASCADE,
    CONSTRAINT fk_combo_items_products FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT ck_combo_items_quantity CHECK (quantity > 0)
);

-- =====================================================
-- Đồng kiểm đơn hàng
-- =====================================================

CREATE TABLE IF NOT EXISTS order_co_inspections (
    id              BIGINT GENERATED ALWAYS AS IDENTITY,
    order_id        BIGINT       NOT NULL,
    buyer_id        BIGINT       NOT NULL,
    check_quantity  BOOLEAN      NOT NULL DEFAULT TRUE,
    check_quality   BOOLEAN      NOT NULL DEFAULT TRUE,
    note            VARCHAR(500),
    status          VARCHAR(20)  NOT NULL DEFAULT 'REQUESTED',
    acknowledged_by BIGINT,
    acknowledged_at TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_order_co_inspections PRIMARY KEY (id),
    CONSTRAINT uq_order_co_inspections_order_id UNIQUE (order_id),
    CONSTRAINT fk_order_co_inspections_orders    FOREIGN KEY (order_id)        REFERENCES orders (id),
    CONSTRAINT fk_order_co_inspections_buyers    FOREIGN KEY (buyer_id)        REFERENCES users (id),
    CONSTRAINT fk_order_co_inspections_ack_users FOREIGN KEY (acknowledged_by) REFERENCES users (id),
    CONSTRAINT ck_order_co_inspections_status
        CHECK (status IN ('REQUESTED', 'ACKNOWLEDGED', 'COMPLETED'))
);

-- =====================================================
-- Index cho các truy vấn thường dùng
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_products_farmer_id      ON products (farmer_id);
CREATE INDEX IF NOT EXISTS idx_products_category_id    ON products (category_id);
CREATE INDEX IF NOT EXISTS idx_products_name           ON products (name);
CREATE INDEX IF NOT EXISTS idx_batches_product_id      ON batches (product_id);
CREATE INDEX IF NOT EXISTS idx_batches_status          ON batches (status);
CREATE INDEX IF NOT EXISTS idx_orders_buyer_id         ON orders (buyer_id);
CREATE INDEX IF NOT EXISTS idx_order_items_order_id    ON order_items (order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_batch_id    ON order_items (batch_id);
CREATE INDEX IF NOT EXISTS idx_payments_order_id       ON payments (order_id);
CREATE INDEX IF NOT EXISTS idx_complaints_order_id     ON complaints (order_id);
CREATE INDEX IF NOT EXISTS idx_notifications_user_read ON notifications (user_id, is_read);
CREATE INDEX IF NOT EXISTS idx_messages_receiver_id    ON messages (receiver_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity       ON audit_logs (entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_combos_farmer_id        ON combos (farmer_id);
CREATE INDEX IF NOT EXISTS idx_combo_items_combo_id    ON combo_items (combo_id);
CREATE INDEX IF NOT EXISTS idx_order_co_inspections_buyer_id ON order_co_inspections (buyer_id);
CREATE INDEX IF NOT EXISTS idx_order_co_inspections_status   ON order_co_inspections (status);
CREATE INDEX IF NOT EXISTS idx_farming_logs_batch_id         ON farming_logs (batch_id);

-- Mỗi đơn chỉ có tối đa một giao dịch PAID cho mỗi loại.
CREATE UNIQUE INDEX IF NOT EXISTS uq_payments_order_full_paid
    ON payments (order_id) WHERE payment_type = 'FULL' AND status = 'PAID';
CREATE UNIQUE INDEX IF NOT EXISTS uq_payments_order_deposit_paid
    ON payments (order_id) WHERE payment_type = 'DEPOSIT' AND status = 'PAID';
CREATE UNIQUE INDEX IF NOT EXISTS uq_payments_order_remaining_paid
    ON payments (order_id) WHERE payment_type = 'REMAINING' AND status = 'PAID';
CREATE UNIQUE INDEX IF NOT EXISTS uq_payments_provider_ref
    ON payments (provider_ref) WHERE provider_ref IS NOT NULL;
