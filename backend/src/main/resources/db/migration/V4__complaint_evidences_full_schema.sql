-- =====================================================
-- V4: Mở rộng complaint_evidences để khớp entity ComplaintEvidence
-- V2 chỉ tạo: id, complaint_id, file_url, file_type
-- V3 thêm:    created_at
-- V4 thêm các cột còn thiếu và rename cột cũ
-- =====================================================

-- Rename file_url -> media_url (entity dùng media_url)
ALTER TABLE complaint_evidences RENAME COLUMN file_url TO media_url;

-- Rename file_type -> media_type (entity dùng media_type)
-- Phải drop constraint CHECK trước khi rename
ALTER TABLE complaint_evidences DROP CONSTRAINT IF EXISTS ck_complaint_evidences_file_type;
ALTER TABLE complaint_evidences RENAME COLUMN file_type TO media_type;

-- Mở rộng độ dài và thêm constraint mới khớp entity
ALTER TABLE complaint_evidences ALTER COLUMN media_url TYPE VARCHAR(500);
ALTER TABLE complaint_evidences ALTER COLUMN media_type TYPE VARCHAR(20);
ALTER TABLE complaint_evidences ADD CONSTRAINT ck_complaint_evidences_media_type
    CHECK (media_type IN ('IMAGE', 'VIDEO'));

-- Thêm các cột còn thiếu
ALTER TABLE complaint_evidences ADD COLUMN IF NOT EXISTS uploaded_by      BIGINT;
ALTER TABLE complaint_evidences ADD COLUMN IF NOT EXISTS uploader_role    VARCHAR(20);
ALTER TABLE complaint_evidences ADD COLUMN IF NOT EXISTS storage_key      VARCHAR(255);
ALTER TABLE complaint_evidences ADD COLUMN IF NOT EXISTS original_filename VARCHAR(255);
ALTER TABLE complaint_evidences ADD COLUMN IF NOT EXISTS content_type     VARCHAR(100);
ALTER TABLE complaint_evidences ADD COLUMN IF NOT EXISTS file_size        BIGINT;
ALTER TABLE complaint_evidences ADD COLUMN IF NOT EXISTS checksum_sha256  VARCHAR(64);
ALTER TABLE complaint_evidences ADD COLUMN IF NOT EXISTS caption          VARCHAR(255);

-- storage_key phải unique (entity: @Column(unique = true))
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uq_complaint_evidences_storage_key'
          AND conrelid = 'complaint_evidences'::regclass
    ) THEN
        ALTER TABLE complaint_evidences
            ADD CONSTRAINT uq_complaint_evidences_storage_key UNIQUE (storage_key);
    END IF;
END $$;

-- Backfill NOT NULL columns từ dữ liệu cũ (nếu có row tồn tại)
UPDATE complaint_evidences
SET uploaded_by   = 0,
    uploader_role = 'BUYER'
WHERE uploaded_by IS NULL;

-- Siết NOT NULL sau backfill
ALTER TABLE complaint_evidences ALTER COLUMN uploaded_by   SET NOT NULL;
ALTER TABLE complaint_evidences ALTER COLUMN uploader_role SET NOT NULL;
