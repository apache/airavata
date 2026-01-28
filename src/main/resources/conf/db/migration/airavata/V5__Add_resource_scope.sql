-- Migration to add resource scope (USER, GATEWAY) for multi-level resource access.
-- 
-- Resource Scope Model:
-- - USER: Resources owned by a specific user (stored in DB with scope=USER, ownerId=userId)
-- - GATEWAY: Resources owned at gateway level (stored in DB with scope=GATEWAY, ownerId=null)
-- - DELEGATED: Resources accessible via group credentials but not directly owned (inferred at runtime, not stored)
-- 
-- Only USER and GATEWAY are stored in the database. DELEGATED scope is automatically
-- inferred by the service layer when returning resources accessible via groups.

-- Add scope column to CATALOG_RESOURCE (only USER or GATEWAY)
ALTER TABLE CATALOG_RESOURCE 
ADD COLUMN RESOURCE_SCOPE VARCHAR(50) DEFAULT 'USER' AFTER PRIVACY;

-- Add group resource profile ID for tracking delegation (not for scope, but for access control)
ALTER TABLE CATALOG_RESOURCE 
ADD COLUMN GROUP_RESOURCE_PROFILE_ID VARCHAR(255) AFTER OWNER_ID;

-- Migrate existing data: set scope based on current ownership
-- If OWNER_ID is set, it's USER scope
-- If only GATEWAY_ID is set and no OWNER_ID, it's GATEWAY scope (for gateway admins)
UPDATE CATALOG_RESOURCE 
SET RESOURCE_SCOPE = CASE 
    WHEN OWNER_ID IS NOT NULL AND OWNER_ID != '' THEN 'USER'
    WHEN GATEWAY_ID IS NOT NULL AND GATEWAY_ID != '' THEN 'GATEWAY'
    ELSE 'USER'
END;

-- Add scope column to APPLICATION_INTERFACE (only USER or GATEWAY)
ALTER TABLE APPLICATION_INTERFACE 
ADD COLUMN RESOURCE_SCOPE VARCHAR(50) DEFAULT 'GATEWAY' AFTER GATEWAY_ID;

-- Add owner ID to APPLICATION_INTERFACE for USER scope
ALTER TABLE APPLICATION_INTERFACE 
ADD COLUMN OWNER_ID VARCHAR(255) AFTER GATEWAY_ID;

-- Add group resource profile ID for tracking delegation (not for scope, but for access control)
ALTER TABLE APPLICATION_INTERFACE 
ADD COLUMN GROUP_RESOURCE_PROFILE_ID VARCHAR(255) AFTER OWNER_ID;

-- Migrate existing applications: all existing are GATEWAY scope (they only had GATEWAY_ID)
UPDATE APPLICATION_INTERFACE 
SET RESOURCE_SCOPE = 'GATEWAY';

-- Add indexes for efficient scope-based queries
CREATE INDEX IF NOT EXISTS idx_catalog_resource_scope ON CATALOG_RESOURCE(RESOURCE_SCOPE);
CREATE INDEX IF NOT EXISTS idx_catalog_resource_group ON CATALOG_RESOURCE(GROUP_RESOURCE_PROFILE_ID);
CREATE INDEX IF NOT EXISTS idx_app_interface_scope ON APPLICATION_INTERFACE(RESOURCE_SCOPE);
CREATE INDEX IF NOT EXISTS idx_app_interface_owner ON APPLICATION_INTERFACE(OWNER_ID);
CREATE INDEX IF NOT EXISTS idx_app_interface_group ON APPLICATION_INTERFACE(GROUP_RESOURCE_PROFILE_ID);
