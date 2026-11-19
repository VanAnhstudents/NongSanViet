-- =====================================================
-- V5: Tạo bảng farm_certificates (thiếu trong V1-V4)
-- Khớp với entity FarmCertificate
-- =====================================================

CREATE TABLE IF NOT EXISTS farm_certificates (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY,
    farm_id            BIGINT          NOT NULL,
    certificate_type   VARCHAR(50)     NOT NULL,
    certificate_number VARCHAR(100),
    issued_by          VARCHAR(150),
    issue_date         DATE,
    expiry_date        DATE,
    file_url           VARCHAR(500)    NOT NULL,
    status             VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    rejection_reason   VARCHAR(500),
    reviewed_by        BIGINT,
    reviewed_at        TIMESTAMPTZ,
    created_at         TIMESTAMPTZ     NOT NULL DEFAULT now(),
    CONSTRAINT pk_farm_certificates PRIMARY KEY (id),
    CONSTRAINT fk_farm_certificates_farm
        FOREIGN KEY (farm_id) REFERENCES farms (id) ON DELETE CASCADE,
    CONSTRAINT ck_farm_certificates_status
        CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    CONSTRAINT ck_farm_certificates_type
        CHECK (certificate_type IN ('VIETGAP', 'GLOBALGAP', 'ORGANIC', 'OCOP', 'HACCP', 'OTHER'))
);

CREATE INDEX IF NOT EXISTS idx_farm_certificates_farm_id
    ON farm_certificates (farm_id);
