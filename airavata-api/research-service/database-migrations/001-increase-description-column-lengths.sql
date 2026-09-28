-- Migration: Increase Description Column Lengths
-- Date: 2025-01-02
-- Issue: "Data too long for column" error when serializing UI fields to JSON
-- 
-- This migration increases the column lengths for RESOURCE_DESCRIPTION and DESCRIPTION
-- columns to accommodate enhanced JSON serialization of UI fields including:
-- - name, hostAliases, ipAddresses, queues (compute resources)
-- - name and additional UI fields (storage resources)

-- Increase RESOURCE_DESCRIPTION column length in COMPUTE_RESOURCE table
ALTER TABLE COMPUTE_RESOURCE MODIFY COLUMN RESOURCE_DESCRIPTION VARCHAR(2048);

-- Increase DESCRIPTION column length in STORAGE_RESOURCE table  
ALTER TABLE STORAGE_RESOURCE MODIFY COLUMN DESCRIPTION VARCHAR(2048);

-- Verification query (optional - run to confirm changes)
-- SELECT 
--     TABLE_NAME, 
--     COLUMN_NAME, 
--     DATA_TYPE, 
--     CHARACTER_MAXIMUM_LENGTH 
-- FROM INFORMATION_SCHEMA.COLUMNS 
-- WHERE TABLE_SCHEMA = 'app_catalog' 
--     AND TABLE_NAME IN ('COMPUTE_RESOURCE', 'STORAGE_RESOURCE')
--     AND COLUMN_NAME IN ('RESOURCE_DESCRIPTION', 'DESCRIPTION');