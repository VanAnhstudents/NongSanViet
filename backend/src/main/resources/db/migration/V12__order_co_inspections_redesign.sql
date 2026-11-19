-- =====================================================
-- V12: Redesign bảng order_co_inspections
-- Schema cũ: check_quantity, check_quality, note, status, acknowledged_by, acknowledged_at
-- Schema mới (entity OrderCoInspection): result, notes, is_locked, confirmed_at, updated_at
-- =====================================================

-- Thêm các cột mới
ALTER TABLE order_co_inspections ADD COLUMN IF NOT EXISTS result      VARCHAR(20);
ALTER TABLE order_co_inspections ADD COLUMN IF NOT EXISTS notes       TEXT;
ALTER TABLE order_co_inspections ADD COLUMN IF NOT EXISTS is_locked   BOOLEAN     NOT NULL DEFAULT false;
ALTER TABLE order_co_inspections ADD COLUMN IF NOT EXISTS confirmed_at TIMESTAMPTZ;
ALTER TABLE order_co_inspections ADD COLUMN IF NOT EXISTS updated_at  TIMESTAMPTZ NOT NULL DEFAULT now();

-- Backfill result từ status cũ (REQUESTED -> PASSED, còn lại -> PASSED mặc định)
UPDATE order_co_inspections
SET result = CASE
    WHEN status = 'FAILED' THEN 'FAILED'
    ELSE 'PASSED'
END
WHERE result IS NULL;

-- Backfill notes từ note cũ
UPDATE order_co_inspections SET notes = note WHERE notes IS NULL AND note IS NOT NULL;

-- Backfill confirmed_at từ acknowledged_at cũ
UPDATE order_co_inspections SET confirmed_at = acknowledged_at WHERE confirmed_at IS NULL AND acknowledged_at IS NOT NULL;

-- Siết NOT NULL sau backfill
ALTER TABLE order_co_inspections ALTER COLUMN result SET NOT NULL;

-- Xóa cột cũ không còn dùng
ALTER TABLE order_co_inspections DROP COLUMN IF EXISTS check_quantity;
ALTER TABLE order_co_inspections DROP COLUMN IF EXISTS check_quality;
ALTER TABLE order_co_inspections DROP COLUMN IF EXISTS note;
ALTER TABLE order_co_inspections DROP COLUMN IF EXISTS status;
ALTER TABLE order_co_inspections DROP COLUMN IF EXISTS acknowledged_by;
ALTER TABLE order_co_inspections DROP COLUMN IF EXISTS acknowledged_at;

-- Drop FK cũ nếu còn
ALTER TABLE order_co_inspections DROP CONSTRAINT IF EXISTS fk_order_co_inspections_ack_users;
