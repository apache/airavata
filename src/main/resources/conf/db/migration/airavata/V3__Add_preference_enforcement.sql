-- Add ENFORCED column to RESOURCE_PREFERENCE table
-- When true, this preference cannot be overridden by lower-level preferences
-- Enables top-down preference enforcement: GATEWAY (enforced) > GROUP (enforced) > USER
ALTER TABLE RESOURCE_PREFERENCE ADD COLUMN IF NOT EXISTS ENFORCED BOOLEAN DEFAULT FALSE;
