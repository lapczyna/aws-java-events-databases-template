-- V3: Seed data for local development and integration testing

INSERT INTO campaigns (advertiser_id, campaign_name, campaign_type, budget, start_date, end_date, status)
VALUES
    (101, 'Summer Sale Display 2024',     'DISPLAY',      50000.00, '2024-06-01', '2024-08-31', 'COMPLETED'),
    (101, 'Back to School Video Q3',      'VIDEO',        75000.00, '2024-08-01', '2024-09-15', 'COMPLETED'),
    (102, 'Brand Awareness Programmatic', 'PROGRAMMATIC', 120000.00,'2024-09-01', '2024-12-31', 'ACTIVE'),
    (102, 'Holiday Search Campaign',      'SEARCH',       30000.00, '2024-11-01', '2024-12-31', 'DRAFT'),
    (103, 'Social Retargeting Q4',        'SOCIAL',       25000.00, '2024-10-01', '2024-12-15', 'ACTIVE'),
    (103, 'Native Content Partnership',   'NATIVE',       10000.00, '2024-07-01', '2024-07-31', 'COMPLETED');
