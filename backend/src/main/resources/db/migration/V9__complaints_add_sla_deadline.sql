-- =====================================================
-- V9: Bổ sung cột sla_deadline_at cho bảng complaints
-- Khớp với entity dispute.entity.Complaint
-- =====================================================

ALTER TABLE complaints ADD COLUMN IF NOT EXISTS sla_deadline_at TIMESTAMPTZ;
