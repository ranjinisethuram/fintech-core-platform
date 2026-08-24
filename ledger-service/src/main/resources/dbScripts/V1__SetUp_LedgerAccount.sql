-- Enable UUID generation
--CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS ledger_account(
    id UUID PRIMARY KEY DEFAULT,
    account_code VARCHAR(50) NOT NULL,
    currency VARCHAR(5) NOT NULL,
    wallet_id UUID,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);