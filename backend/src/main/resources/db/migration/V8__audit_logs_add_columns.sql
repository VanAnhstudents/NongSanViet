-- =====================================================
-- V8: Bổ sung các cột còn thiếu cho bảng audit_logs
-- Khớp với entity common.audit.entity.AuditLog
-- =====================================================

ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS actor_role    VARCHAR(20)  NOT NULL DEFAULT 'ADMIN';
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS category      VARCHAR(30)  NOT NULL DEFAULT 'ADMIN_ACTION';
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS order_id      BIGINT;
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS target_user_id BIGINT;
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS details       TEXT;

-- action cột cũ có length 50, entity mới dùng 60
ALTER TABLE audit_logs ALTER COLUMN action TYPE VARCHAR(60);

-- actor_id: entity mới đánh NOT NULL, backfill trước khi siết
UPDATE audit_logs SET actor_id = 0 WHERE actor_id IS NULL;
ALTER TABLE audit_logs ALTER COLUMN actor_id SET NOT NULL;
