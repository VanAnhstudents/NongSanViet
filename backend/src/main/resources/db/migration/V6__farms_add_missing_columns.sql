-- =====================================================
-- V6: Bổ sung cột còn thiếu trong bảng farms
-- Entity Farm có description và created_at nhưng V2 không tạo chúng
-- =====================================================

ALTER TABLE farms ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE farms ADD COLUMN IF NOT EXISTS created_at  TIMESTAMPTZ NOT NULL DEFAULT now();
