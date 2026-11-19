-- =====================================================
-- V5: Bổ sung schema cho hồ sơ nông trại / chứng nhận
-- =====================================================
-- Entity Farm có description, created_at; FarmCertificate là bảng mới
-- phục vụ hồ sơ chứng nhận của nông trại.
-- =====================================================

ALTER TABLE farms ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE farms ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT now();

CREATE TABLE IF NOT EXISTS farm_certificates (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY,
    farm_id            BIGINT       NOT NULL,
    certificate_type   VARCHAR(50)  NOT NULL,
    certificate_number VARCHAR(100),
    issued_by          VARCHAR(150),
    issue_date         DATE,
    expiry_date        DATE,
    file_url           VARCHAR(500) NOT NULL,
    status             VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    rejection_reason   VARCHAR(500),
    reviewed_by        BIGINT,
    reviewed_at        TIMESTAMPTZ,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_farm_certificates PRIMARY KEY (id),
    CONSTRAINT fk_farm_certificates_farms FOREIGN KEY (farm_id) REFERENCES farms (id),
    CONSTRAINT fk_farm_certificates_reviewed_by FOREIGN KEY (reviewed_by) REFERENCES users (id),
    CONSTRAINT ck_farm_certificates_status
        CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

CREATE INDEX IF NOT EXISTS idx_farm_certificates_farm_id ON farm_certificates (farm_id);
CREATE INDEX IF NOT EXISTS idx_farm_certificates_status ON farm_certificates (status);
