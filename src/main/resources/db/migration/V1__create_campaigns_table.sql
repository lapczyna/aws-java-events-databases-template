-- V1: Initial campaigns table
-- Flyway chosen over Liquibase for its simplicity (plain SQL migrations),
-- minimal configuration, excellent Spring Boot autoconfiguration, and
-- strong community adoption for relational schema versioning.

CREATE TABLE IF NOT EXISTS campaigns (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    advertiser_id   BIGINT          NOT NULL,
    campaign_name   VARCHAR(255)    NOT NULL,
    campaign_type   VARCHAR(50)     NOT NULL,
    budget          DECIMAL(15, 2)  NOT NULL,
    start_date      DATE            NOT NULL,
    end_date        DATE            NOT NULL,
    status          VARCHAR(30)     NOT NULL DEFAULT 'DRAFT',
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_campaigns PRIMARY KEY (id),
    CONSTRAINT chk_campaign_type   CHECK (campaign_type IN ('DISPLAY','VIDEO','SEARCH','SOCIAL','NATIVE','PROGRAMMATIC')),
    CONSTRAINT chk_campaign_status CHECK (status IN ('DRAFT','ACTIVE','PAUSED','COMPLETED','CANCELLED')),
    CONSTRAINT chk_campaign_budget CHECK (budget > 0),
    CONSTRAINT chk_campaign_dates  CHECK (end_date > start_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
