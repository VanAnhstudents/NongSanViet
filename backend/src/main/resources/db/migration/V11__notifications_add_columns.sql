-- =====================================================
-- V11: Bổ sung các cột còn thiếu cho bảng notifications
-- Khớp với entity common.notification.entity.AppNotification
-- =====================================================

ALTER TABLE notifications ADD COLUMN IF NOT EXISTS type           VARCHAR(40)  NOT NULL DEFAULT 'SYSTEM';
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS title          VARCHAR(150) NOT NULL DEFAULT 'Thông báo hệ thống';
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS reference_type VARCHAR(40);
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS reference_id   BIGINT;
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS read_at        TIMESTAMPTZ;

-- content cũ là VARCHAR(500), entity mới dùng TEXT
ALTER TABLE notifications ALTER COLUMN content TYPE TEXT;
