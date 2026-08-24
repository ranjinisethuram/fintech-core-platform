-- Enable UUID generation
--CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

INSERT INTO ledger_account (id, account_code, currency, status, created_at, updated_at)
VALUES
('8a5b2c3d-e4f0-1a2b-3c4d-5e6f7a8b9c0d','PLATFORM_BANK','INR','ACTIVE', NOW(), NOW()),
('9b6c3d4e-f5a1-2b3c-4d5e-6f7a8b9c0d1e','PLATFORM_FEES','INR','ACTIVE', NOW(), NOW()),
('0c7d4e5f-a6b2-3c4d-5e6f-7a8b9c0d1e2f','PAYMENT_CLEARING','INR','ACTIVE', NOW(), NOW()),
('1d8e5f6a-b7c3-4d5e-6f7a-8b9c0d1e2f3a','WITHDRAWAL_CLEARING','INR','ACTIVE', NOW(), NOW()),
('2e9f6a7b-c8d4-5e6f-7a8b-9c0d1e2f3a4b','REFUND_ACCOUNT','INR','ACTIVE', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;