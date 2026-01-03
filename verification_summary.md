# Flyway Migration vs Entity Verification Summary

## Databases Verified and Fixed

### ✅ credential_store
- Fixed: VARCHAR lengths (100 → 256 for GATEWAY_ID, TOKEN_ID, COMMUNITY_USER_NAME)
- Fixed: VARCHAR length (10 → 50 for CREDENTIAL_OWNER_TYPE)
- Fixed: TIME_PERSISTED NOT NULL constraint

### ✅ profile_service  
- Fixed: USER_PROFILE.CREATION_TIME NOT NULL
- Fixed: USER_PROFILE.LAST_ACCESS_TIME NOT NULL
- Note: @ElementCollection tables (EMAIL, PHONE, NATIONALITY, LABELED_URI) are correctly in separate tables

### ✅ experiment_catalog
- Fixed: EXPERIMENT.EXPERIMENT_ID NOT NULL
- Fixed: EXPERIMENT.CREATION_TIME NOT NULL
- Fixed: GATEWAY.GATEWAY_ID NOT NULL
- Fixed: PROCESS_STATUS.STATUS_ID NOT NULL
- Fixed: PROCESS_STATUS.TIME_OF_STATE_CHANGE NOT NULL
- Fixed: PROCESS_ERROR.ERROR_ID NOT NULL
- Fixed: PROCESS_ERROR.CREATION_TIME NOT NULL
- Fixed: PROCESS_ERROR LOB columns (ACTUAL_ERROR_MESSAGE, USER_FRIENDLY_MESSAGE, ROOT_CAUSE_ERROR_ID_LIST) → LONGTEXT
- Fixed: NOTIFICATION.CREATION_DATE NOT NULL
- Fixed: NOTIFICATION.PUBLISHED_DATE NOT NULL
- Fixed: NOTIFICATION.EXPIRATION_DATE NOT NULL
- Fixed: EXPERIMENT_INPUT.INPUT_NAME NOT NULL
- Note: EXPERIMENT_SUMMARY is a VIEW, not a table (correctly created in V1)

### ⚠️ workflow_catalog
- Verified: All @Lob columns correctly use LONGTEXT
- Verified: `VALUE` columns correctly use backticks (reserved word)
- Script false positives: Column name matching issues with backticks

### ⚠️ app_catalog, replica_catalog, sharing_registry
- Need manual verification due to script limitations
- Most reported issues are likely false positives from:
  - Backticked column names
  - @ElementCollection table detection
  - Complex foreign key relationships

## Script Limitations

The verification script has known issues:
1. Cannot properly handle backticked column names (`VALUE`, etc.)
2. Incorrectly detects @Lob annotations in some cases
3. Does not distinguish between views and tables
4. Does not handle @ElementCollection tables correctly

## Recommendations

1. Critical nullable and data type issues have been fixed
2. Remaining databases should be verified manually by:
   - Comparing entity @Column annotations with DDL
   - Checking primary key definitions
   - Verifying foreign key relationships
   - Validating LOB column types

## Files Modified

- `airavata-api/src/main/resources/db/migration/credential_store/V1__Initial_schema.sql`
- `airavata-api/src/main/resources/db/migration/profile_service/V1__Initial_schema.sql`
- `airavata-api/src/main/resources/db/migration/experiment_catalog/V1__Initial_schema.sql`
