-- Migration to merge NOTEBOOK and MODEL resource types into REPOSITORY
-- This migration updates existing records in the CATALOG_RESOURCE table

UPDATE CATALOG_RESOURCE 
SET RESOURCE_TYPE = 'REPOSITORY' 
WHERE RESOURCE_TYPE IN ('NOTEBOOK', 'MODEL');
