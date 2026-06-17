-- V2: Performance indexes for campaigns

-- Primary query pattern: look up campaigns by advertiser
CREATE INDEX idx_campaigns_advertiser_id
    ON campaigns (advertiser_id);

-- Filter campaigns by status for operational dashboards
CREATE INDEX idx_campaigns_status
    ON campaigns (status);

-- Composite index for the most common filtered query: advertiser + status
CREATE INDEX idx_campaigns_advertiser_status
    ON campaigns (advertiser_id, status);

-- Date-range queries for reporting (active campaigns in a period)
CREATE INDEX idx_campaigns_dates
    ON campaigns (start_date, end_date);
