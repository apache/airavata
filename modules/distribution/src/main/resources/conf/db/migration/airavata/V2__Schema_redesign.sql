/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 * V2 Schema Redesign
 *
 * Changes:
 *   1. Drop legacy tables (user_content, catalog_entry, repository, experiment_artifact)
 *   2. Gateway: strip to essentials
 *   3. Resource: add resource_type discriminator
 *   4. Resource binding: fix unique constraint to include login_username
 *   5. Experiment: add parent_experiment_id + tags
 *   6. Event: generalize to parent_id + parent_type
 *   7. Job: migrate job_statuses to event, drop task_id
 *   8. Split entity_relationship into group_membership + sharing_permission
 *   9. Process: rename process_detail to provider_context
 *  10. Timestamp standardization (experiment.created_at, job.created_at)
 *  11. Artifact: size int -> bigint
 */

-- ============================================================================
-- 1. DROP LEGACY TABLES
-- ============================================================================

DROP TABLE IF EXISTS user_content;
DROP TABLE IF EXISTS catalog_entry;
DROP TABLE IF EXISTS repository;
DROP TABLE IF EXISTS experiment_artifact;
DROP TABLE IF EXISTS user_allocation_project;

-- ============================================================================
-- 2. gateway - Strip to essentials
-- ============================================================================

ALTER TABLE gateway DROP COLUMN IF EXISTS gateway_approval_status;
ALTER TABLE gateway DROP COLUMN IF EXISTS gateway_acronym;
ALTER TABLE gateway DROP COLUMN IF EXISTS gateway_url;
ALTER TABLE gateway DROP COLUMN IF EXISTS gateway_public_abstract;
ALTER TABLE gateway DROP COLUMN IF EXISTS gateway_review_proposal_description;
ALTER TABLE gateway DROP COLUMN IF EXISTS gateway_admin_first_name;
ALTER TABLE gateway DROP COLUMN IF EXISTS gateway_admin_last_name;
ALTER TABLE gateway DROP COLUMN IF EXISTS gateway_admin_email;
ALTER TABLE gateway DROP COLUMN IF EXISTS declined_reason;
ALTER TABLE gateway DROP COLUMN IF EXISTS request_creation_time;
ALTER TABLE gateway DROP COLUMN IF EXISTS requester_username;
ALTER TABLE gateway DROP COLUMN IF EXISTS domain_description;

DROP INDEX IF EXISTS idx_gateway_approval_status ON gateway;

-- ============================================================================
-- 3. resource - Add resource_type discriminator column
-- ============================================================================

ALTER TABLE resource ADD COLUMN resource_type VARCHAR(20) NOT NULL DEFAULT 'COMPUTE';

-- Backfill resource_type from capabilities JSON:
-- If capabilities contains a storage key, mark as STORAGE; otherwise COMPUTE
UPDATE resource
SET resource_type = 'STORAGE'
WHERE JSON_CONTAINS_PATH(capabilities, 'one', '$.storage') = 1
  AND JSON_CONTAINS_PATH(capabilities, 'one', '$.compute') = 0;

-- ============================================================================
-- 4. resource_binding - Fix unique constraint to include login_username
-- ============================================================================

ALTER TABLE resource_binding DROP INDEX uk_binding_cred_resource;
ALTER TABLE resource_binding ADD UNIQUE KEY uk_binding_cred_resource_user (credential_id, resource_id, login_username);

-- ============================================================================
-- 5. experiment - Add parent_experiment_id + tags
-- ============================================================================

ALTER TABLE experiment ADD COLUMN parent_experiment_id VARCHAR(255);
ALTER TABLE experiment ADD COLUMN tags JSON;
CREATE INDEX idx_experiment_parent ON experiment(parent_experiment_id);
ALTER TABLE experiment ADD CONSTRAINT fk_experiment_parent
    FOREIGN KEY (parent_experiment_id) REFERENCES experiment(experiment_id) ON DELETE SET NULL;

-- ============================================================================
-- 6. event - Generalize to parent_id + parent_type
-- ============================================================================

-- 6a. Rename process_id -> parent_id and add parent_type
ALTER TABLE experiment DROP FOREIGN KEY IF EXISTS fk_experiment_parent;
ALTER TABLE event CHANGE COLUMN process_id parent_id VARCHAR(255) NOT NULL;
ALTER TABLE event ADD COLUMN parent_type VARCHAR(20) NOT NULL DEFAULT 'PROCESS';

-- Re-add the experiment FK (it was accidentally dropped by the rename - it's unrelated)
ALTER TABLE experiment ADD CONSTRAINT fk_experiment_parent
    FOREIGN KEY (parent_experiment_id) REFERENCES experiment(experiment_id) ON DELETE SET NULL;

-- Remove the old FK from event -> process (parent_id now can reference multiple types)
-- Must drop FK BEFORE dropping indexes, since MariaDB won't drop an index needed by a FK.
SET @fk_name = (
    SELECT CONSTRAINT_NAME
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE TABLE_NAME = 'event'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
      AND TABLE_SCHEMA = DATABASE()
    LIMIT 1
);
SET @sql = IF(@fk_name IS NOT NULL,
    CONCAT('ALTER TABLE event DROP FOREIGN KEY ', @fk_name),
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Now drop old process-specific indexes and add generalized ones
DROP INDEX IF EXISTS idx_event_process ON event;
DROP INDEX IF EXISTS idx_event_process_kind_seq ON event;
CREATE INDEX idx_event_parent ON event(parent_id);
CREATE INDEX idx_event_parent_type_kind_seq ON event(parent_id, parent_type, event_kind, sequence_num DESC);

-- Update CHECK constraint for event_kind to include JOB parent type
ALTER TABLE event DROP CONSTRAINT IF EXISTS chk_event_kind;
ALTER TABLE event ADD CONSTRAINT chk_event_kind CHECK (event_kind IN ('STATUS', 'ERROR'));

-- ============================================================================
-- 7. job - Migrate job_statuses to event rows, drop task_id + job_statuses
-- ============================================================================

-- 7a. Migrate existing job_statuses JSON to event rows
-- Each job_statuses entry has: state, reason, timeOfStateChange, statusId
-- We insert them as event rows with parent_type='JOB'
INSERT INTO event (event_id, parent_id, parent_type, event_kind, event_time, sequence_num, state, reason)
SELECT
    COALESCE(
        JSON_UNQUOTE(JSON_EXTRACT(status.val, '$.statusId')),
        UUID()
    ) AS event_id,
    j.job_id AS parent_id,
    'JOB' AS parent_type,
    'STATUS' AS event_kind,
    COALESCE(
        FROM_UNIXTIME(JSON_EXTRACT(status.val, '$.timeOfStateChange') / 1000),
        CURRENT_TIMESTAMP(6)
    ) AS event_time,
    status.idx + 1 AS sequence_num,
    JSON_UNQUOTE(JSON_EXTRACT(status.val, '$.state')) AS state,
    JSON_UNQUOTE(JSON_EXTRACT(status.val, '$.reason')) AS reason
FROM job j
CROSS JOIN JSON_TABLE(
    j.job_statuses,
    '$[*]' COLUMNS (
        idx FOR ORDINALITY,
        val JSON PATH '$'
    )
) AS status
WHERE j.job_statuses IS NOT NULL
  AND JSON_LENGTH(j.job_statuses) > 0;

-- 7b. Drop task_id column and its index
DROP INDEX IF EXISTS idx_job_task_id ON job;
ALTER TABLE job DROP COLUMN IF EXISTS task_id;

-- 7c. Drop job_statuses JSON column
ALTER TABLE job DROP COLUMN IF EXISTS job_statuses;

-- ============================================================================
-- 8. Split entity_relationship into group_membership + sharing_permission
-- ============================================================================

-- 8a. Create group_membership table
CREATE TABLE IF NOT EXISTS group_membership (
    membership_id VARCHAR(512) NOT NULL,
    group_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    role VARCHAR(50),
    domain_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (membership_id),
    UNIQUE KEY uk_group_user (group_id, user_id, domain_id),
    INDEX idx_gm_group (group_id),
    INDEX idx_gm_user (user_id),
    INDEX idx_gm_domain (domain_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8b. Create sharing_permission table
CREATE TABLE IF NOT EXISTS sharing_permission (
    permission_id VARCHAR(512) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id VARCHAR(255) NOT NULL,
    grantee_type VARCHAR(20) NOT NULL,
    grantee_id VARCHAR(255) NOT NULL,
    permission VARCHAR(255) NOT NULL,
    domain_id VARCHAR(255),
    metadata JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (permission_id),
    UNIQUE KEY uk_sharing_perm (resource_type, resource_id, grantee_type, grantee_id, permission, domain_id),
    INDEX idx_sp_resource (resource_type, resource_id),
    INDEX idx_sp_grantee (grantee_type, grantee_id),
    INDEX idx_sp_domain (domain_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8c. Migrate MEMBER_OF relationships to group_membership
INSERT INTO group_membership (membership_id, group_id, user_id, role, domain_id, created_at)
SELECT
    relationship_id,
    source_id,
    target_id,
    role,
    domain_id,
    COALESCE(created_at, CURRENT_TIMESTAMP)
FROM entity_relationship
WHERE relation_kind = 'MEMBER_OF';

-- 8d. Migrate HAS_PERMISSION relationships to sharing_permission
INSERT INTO sharing_permission (permission_id, resource_type, resource_id, grantee_type, grantee_id, permission, domain_id, metadata, created_at)
SELECT
    relationship_id,
    source_type,
    source_id,
    target_type,
    target_id,
    COALESCE(permission_type_id, ''),
    domain_id,
    metadata,
    COALESCE(created_at, CURRENT_TIMESTAMP)
FROM entity_relationship
WHERE relation_kind = 'HAS_PERMISSION';

-- 8e. Drop the legacy entity_relationship table
DROP TABLE IF EXISTS entity_relationship;

-- ============================================================================
-- 9. process - Rename process_detail to provider_context, change to JSON
-- ============================================================================

ALTER TABLE process CHANGE COLUMN process_detail provider_context JSON;

-- ============================================================================
-- 10. TIMESTAMP STANDARDIZATION
-- ============================================================================

-- 10a. experiment: convert BIGINT millis -> TIMESTAMP
ALTER TABLE experiment ADD COLUMN created_at_ts TIMESTAMP NULL;
UPDATE experiment SET created_at_ts = FROM_UNIXTIME(created_at / 1000)
    WHERE created_at IS NOT NULL AND created_at > 0;
ALTER TABLE experiment DROP COLUMN created_at;
ALTER TABLE experiment CHANGE COLUMN created_at_ts created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Recreate index on created_at (was lost when column was dropped)
CREATE INDEX idx_experiment_created_at ON experiment(created_at);

-- 10b. job: convert BIGINT millis -> TIMESTAMP
ALTER TABLE job ADD COLUMN created_at_ts TIMESTAMP NULL;
UPDATE job SET created_at_ts = FROM_UNIXTIME(created_at / 1000)
    WHERE created_at > 0;
ALTER TABLE job DROP COLUMN created_at;
ALTER TABLE job CHANGE COLUMN created_at_ts created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- ============================================================================
-- 11. DROP LEGACY ARTIFACT TABLE
-- ============================================================================
-- The legacy flat artifact table is replaced by the research_artifact hierarchy
-- (managed by Hibernate JPA). Drop it if it exists from older installs.

DROP TABLE IF EXISTS artifact;

-- ============================================================================
-- 12. Update experiment_summary view (created_at type changed)
-- ============================================================================

CREATE OR REPLACE VIEW experiment_summary AS
SELECT e.experiment_id,
       e.project_id,
       e.gateway_id,
       e.user_name,
       e.experiment_name,
       e.created_at,
       e.description,
       e.state,
       e.updated_at AS time_of_state_change
FROM experiment e;

-- ============================================================================
-- END OF V2 SCHEMA REDESIGN
-- ============================================================================
