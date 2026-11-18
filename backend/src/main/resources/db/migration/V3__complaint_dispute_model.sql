-- =====================================================
-- V3: Bổ sung model đầy đủ cho khiếu nại / tranh chấp
-- =====================================================
-- Entity Complaint (dispute) cần thêm buyer_id, description,
-- admin_note, refund_amount, resolved_by, resolved_at, updated_at
-- và đổi bộ trạng thái sang OPEN / IN_REVIEW / RESOLVED_REFUND / RESOLVED_REJECTED.
-- =====================================================

-- ---------- complaints ----------
ALTER TABLE complaints ADD COLUMN IF NOT EXISTS buyer_id BIGINT;
ALTER TABLE complaints ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE complaints ADD COLUMN IF NOT EXISTS admin_note TEXT;
ALTER TABLE complaints ADD COLUMN IF NOT EXISTS refund_amount NUMERIC(15, 2) NOT NULL DEFAULT 0;
ALTER TABLE complaints ADD COLUMN IF NOT EXISTS resolved_by BIGINT;
ALTER TABLE complaints ADD COLUMN IF NOT EXISTS resolved_at TIMESTAMPTZ;
ALTER TABLE complaints ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT now();

-- Backfill dữ liệu cũ từ orders/reason trước khi siết NOT NULL.
UPDATE complaints c
SET buyer_id = o.buyer_id
FROM orders o
WHERE c.order_id = o.id
  AND c.buyer_id IS NULL;

UPDATE complaints
SET description = reason
WHERE description IS NULL;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM complaints WHERE buyer_id IS NULL) THEN
        RAISE EXCEPTION 'complaints.buyer_id không thể backfill: có order_id không tồn tại trong orders';
    END IF;
END $$;

ALTER TABLE complaints ALTER COLUMN buyer_id SET NOT NULL;
ALTER TABLE complaints ALTER COLUMN description SET NOT NULL;

-- Bộ trạng thái mới; chuyển các giá trị cũ trước khi thêm check mới.
ALTER TABLE complaints DROP CONSTRAINT IF EXISTS ck_complaints_status;
ALTER TABLE complaints ALTER COLUMN status DROP DEFAULT;
ALTER TABLE complaints ALTER COLUMN status TYPE VARCHAR(25);

UPDATE complaints
SET status = CASE status
    WHEN 'PENDING'   THEN 'OPEN'
    WHEN 'RESPONDED' THEN 'IN_REVIEW'
    WHEN 'ESCALATED' THEN 'IN_REVIEW'
    WHEN 'RESOLVED'  THEN 'RESOLVED_REFUND'
    WHEN 'REJECTED'  THEN 'RESOLVED_REJECTED'
    ELSE status
END;

ALTER TABLE complaints ALTER COLUMN status SET DEFAULT 'OPEN';
ALTER TABLE complaints ADD CONSTRAINT ck_complaints_status
    CHECK (status IN ('OPEN', 'IN_REVIEW', 'RESOLVED_REFUND', 'RESOLVED_REJECTED'));

-- Mỗi đơn hàng chỉ có tối đa một khiếu nại.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uq_complaints_order_id' AND conrelid = 'complaints'::regclass
    ) THEN
        ALTER TABLE complaints ADD CONSTRAINT uq_complaints_order_id UNIQUE (order_id);
    END IF;
END $$;

-- Khóa ngoại cho buyer_id / resolved_by.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_complaints_buyers' AND conrelid = 'complaints'::regclass
    ) THEN
        ALTER TABLE complaints ADD CONSTRAINT fk_complaints_buyers
            FOREIGN KEY (buyer_id) REFERENCES users (id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_complaints_resolved_by' AND conrelid = 'complaints'::regclass
    ) THEN
        ALTER TABLE complaints ADD CONSTRAINT fk_complaints_resolved_by
            FOREIGN KEY (resolved_by) REFERENCES users (id);
    END IF;
END $$;

-- ---------- complaint_evidences ----------
ALTER TABLE complaint_evidences ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT now();
