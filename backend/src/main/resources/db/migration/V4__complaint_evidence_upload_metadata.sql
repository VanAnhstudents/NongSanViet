-- =====================================================
-- V4: Đồng bộ complaint_evidences với model upload minh chứng
-- =====================================================
-- Entity ComplaintEvidence lưu cả minh chứng dạng URL lẫn file tải lên
-- (storage_key, checksum, metadata). Các cột file_url/file_type cũ được
-- chuyển sang media_url/media_type rồi loại bỏ để không còn cột bắt buộc
-- mà entity mới không ghi giá trị.
-- =====================================================

ALTER TABLE complaint_evidences ADD COLUMN IF NOT EXISTS uploaded_by BIGINT;
ALTER TABLE complaint_evidences ADD COLUMN IF NOT EXISTS uploader_role VARCHAR(20);
ALTER TABLE complaint_evidences ADD COLUMN IF NOT EXISTS media_type VARCHAR(20);
ALTER TABLE complaint_evidences ADD COLUMN IF NOT EXISTS media_url VARCHAR(500);
ALTER TABLE complaint_evidences ADD COLUMN IF NOT EXISTS storage_key VARCHAR(255);
ALTER TABLE complaint_evidences ADD COLUMN IF NOT EXISTS original_filename VARCHAR(255);
ALTER TABLE complaint_evidences ADD COLUMN IF NOT EXISTS content_type VARCHAR(100);
ALTER TABLE complaint_evidences ADD COLUMN IF NOT EXISTS file_size BIGINT;
ALTER TABLE complaint_evidences ADD COLUMN IF NOT EXISTS checksum_sha256 VARCHAR(64);
ALTER TABLE complaint_evidences ADD COLUMN IF NOT EXISTS caption VARCHAR(255);

-- Chuyển dữ liệu minh chứng cũ sang bộ cột mới.
-- Minh chứng cũ không lưu người tải lên; quy về người mua của khiếu nại.
UPDATE complaint_evidences ce
SET uploaded_by = COALESCE(ce.uploaded_by, c.buyer_id),
    uploader_role = COALESCE(ce.uploader_role, 'BUYER'),
    media_type = COALESCE(ce.media_type, ce.file_type),
    media_url = COALESCE(ce.media_url, ce.file_url)
FROM complaints c
WHERE ce.complaint_id = c.id
  AND (ce.uploaded_by IS NULL
       OR ce.uploader_role IS NULL
       OR ce.media_type IS NULL
       OR ce.media_url IS NULL);

ALTER TABLE complaint_evidences ALTER COLUMN uploaded_by SET NOT NULL;
ALTER TABLE complaint_evidences ALTER COLUMN uploader_role SET NOT NULL;
ALTER TABLE complaint_evidences ALTER COLUMN media_type SET NOT NULL;
ALTER TABLE complaint_evidences ALTER COLUMN media_url SET NOT NULL;

-- Bỏ các cột legacy sau khi đã backfill.
ALTER TABLE complaint_evidences DROP CONSTRAINT IF EXISTS ck_complaint_evidences_file_type;
ALTER TABLE complaint_evidences DROP COLUMN IF EXISTS file_url;
ALTER TABLE complaint_evidences DROP COLUMN IF EXISTS file_type;

-- Ràng buộc tương đương dữ liệu cũ: media_type chỉ nhận IMAGE/VIDEO.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'ck_complaint_evidences_media_type'
          AND conrelid = 'complaint_evidences'::regclass
    ) THEN
        ALTER TABLE complaint_evidences ADD CONSTRAINT ck_complaint_evidences_media_type
            CHECK (media_type IN ('IMAGE', 'VIDEO'));
    END IF;
END $$;

-- Mỗi file lưu trữ chỉ gắn với một bản ghi minh chứng.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uq_complaint_evidences_storage_key'
          AND conrelid = 'complaint_evidences'::regclass
    ) THEN
        ALTER TABLE complaint_evidences ADD CONSTRAINT uq_complaint_evidences_storage_key
            UNIQUE (storage_key);
    END IF;
END $$;
