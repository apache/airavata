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
 * Apache Airavata - Unified Database Schema (V1 Baseline)
 *
 * Design principles:
 *   - Unified resource table (compute + storage in one)
 *   - Single credential table with token-based VARCHAR PK
 *   - application with embedded I/O definitions and scripts
 *   - Experiment I/O as proper entity tables (not JSON blobs)
 *   - Experiment state as direct column (mutated by process cascade)
 *   - Events at process-level only (status + error audit trail)
 *   - research_artifact hierarchy managed by Hibernate (not in this migration)
 *
 * Requires: MariaDB 10.2+; database must exist (run create-database.sql first).
 */

-- ============================================================================
-- 1. gateway - Tenant/domain configuration
-- ============================================================================

CREATE TABLE IF NOT EXISTS gateway (
    gateway_id VARCHAR(255) NOT NULL,
    gateway_name VARCHAR(255) NOT NULL,
    gateway_domain VARCHAR(255),
    email_address VARCHAR(255),
    gateway_approval_status VARCHAR(50),
    gateway_acronym VARCHAR(255),
    gateway_url VARCHAR(255),
    gateway_public_abstract TEXT,
    gateway_review_proposal_description TEXT,
    gateway_admin_first_name VARCHAR(255),
    gateway_admin_last_name VARCHAR(255),
    gateway_admin_email VARCHAR(255),
    declined_reason TEXT,
    request_creation_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    requester_username VARCHAR(255),
    domain_description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    initial_user_group_id VARCHAR(255),
    admins_group_id VARCHAR(255),
    read_only_admins_group_id VARCHAR(255),
    default_gateway_users_group_id VARCHAR(255),
    PRIMARY KEY (gateway_id),
    UNIQUE KEY uk_gateway_name (gateway_name(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX IF NOT EXISTS idx_gateway_approval_status ON gateway(gateway_approval_status);

-- ============================================================================
-- 2. user - Identity (OIDC subject linked to a gateway)
-- ============================================================================

CREATE TABLE IF NOT EXISTS `user` (
    user_id VARCHAR(512) NOT NULL,
    sub VARCHAR(255) NOT NULL,
    gateway_id VARCHAR(255) NOT NULL,
    personal_group_id VARCHAR(512),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    email VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    INDEX idx_user_sub (sub),
    INDEX idx_user_gateway_id (gateway_id),
    INDEX idx_user_sub_gateway (sub, gateway_id),
    FOREIGN KEY (gateway_id) REFERENCES gateway(gateway_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 3. notification - User-facing messages
-- ============================================================================

CREATE TABLE IF NOT EXISTS notification (
    notification_id VARCHAR(255) NOT NULL,
    gateway_id VARCHAR(255),
    title VARCHAR(255),
    notification_message VARCHAR(4096) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    priority VARCHAR(50),
    PRIMARY KEY (notification_id),
    INDEX idx_notification_gateway (gateway_id),
    FOREIGN KEY (gateway_id) REFERENCES gateway(gateway_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 4. resource - Unified compute and storage resources
--
--    capabilities JSON schema:
--    {
--      compute?: { type: "SLURM"|"FORK", batchQueues?: [...] },
--      storage?: { protocol: "SFTP"|"SCP", basePath?: "/home" }
--    }
-- ============================================================================

CREATE TABLE IF NOT EXISTS resource (
    resource_id VARCHAR(255) NOT NULL,
    gateway_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    host_name VARCHAR(255) NOT NULL,
    port INT DEFAULT 22,
    description TEXT,
    capabilities JSON NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (resource_id),
    INDEX idx_resource_gateway (gateway_id),
    FOREIGN KEY (gateway_id) REFERENCES gateway(gateway_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 5. credential - SSH keys, passwords, and certificates
--
--    credential_id is the token string (UUID) used throughout the system.
--    Encrypted credential data (JSON blob) stored in credential_data.
-- ============================================================================

CREATE TABLE IF NOT EXISTS credential (
    credential_id VARCHAR(255) NOT NULL,
    gateway_id VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL,
    credential_data LONGBLOB NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (credential_id),
    INDEX idx_credential_gateway (gateway_id),
    INDEX idx_credential_user (user_id),
    INDEX idx_credential_gateway_user (gateway_id, user_id),
    FOREIGN KEY (gateway_id) REFERENCES gateway(gateway_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 6. resource_binding - Maps a credential to a resource with runtime metadata
--
--    metadata JSON schema:
--    {
--      allocationProjectNumber?: string,
--      scratchPath?: string,
--      defaultQueue?: string,
--      maxWalltime?: number,
--      storagePath?: string
--    }
-- ============================================================================

CREATE TABLE IF NOT EXISTS resource_binding (
    binding_id VARCHAR(255) NOT NULL,
    credential_id VARCHAR(255) NOT NULL,
    resource_id VARCHAR(255) NOT NULL,
    login_username VARCHAR(255) NOT NULL,
    metadata JSON,
    enabled BOOLEAN DEFAULT TRUE,
    gateway_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (binding_id),
    UNIQUE KEY uk_binding_cred_resource (credential_id, resource_id),
    INDEX idx_binding_credential (credential_id),
    INDEX idx_binding_resource (resource_id),
    INDEX idx_binding_gateway (gateway_id),
    FOREIGN KEY (credential_id) REFERENCES credential(credential_id) ON DELETE CASCADE,
    FOREIGN KEY (resource_id) REFERENCES resource(resource_id) ON DELETE CASCADE,
    FOREIGN KEY (gateway_id) REFERENCES gateway(gateway_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 7. application - Consolidated app catalog
--
--    inputs  JSON: [{name, type, description, required, defaultValue, commandLineArg}]
--    outputs JSON: [{name, type, description, required, commandLineArg}]
--    install_script: bash script, runs once per (resource, username)
--    run_script: bash script template with $INPUT_xxx variables
-- ============================================================================

CREATE TABLE IF NOT EXISTS application (
    application_id VARCHAR(255) NOT NULL,
    gateway_id VARCHAR(255) NOT NULL,
    owner_name VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    version VARCHAR(100),
    description TEXT,
    inputs JSON,
    outputs JSON,
    install_script MEDIUMTEXT,
    run_script MEDIUMTEXT,
    scope VARCHAR(50) DEFAULT 'GATEWAY',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (application_id),
    INDEX idx_application_gateway (gateway_id),
    INDEX idx_application_owner (owner_name),
    FOREIGN KEY (gateway_id) REFERENCES gateway(gateway_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 8. application_installation - Tracks install state per (app, resource, user)
-- ============================================================================

CREATE TABLE IF NOT EXISTS application_installation (
    installation_id VARCHAR(255) NOT NULL,
    application_id VARCHAR(255) NOT NULL,
    resource_id VARCHAR(255) NOT NULL,
    login_username VARCHAR(255) NOT NULL,
    install_path VARCHAR(500),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    installed_at TIMESTAMP NULL,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (installation_id),
    UNIQUE KEY uk_installation (application_id, resource_id, login_username),
    INDEX idx_installation_app (application_id),
    INDEX idx_installation_resource (resource_id),
    FOREIGN KEY (application_id) REFERENCES application(application_id) ON DELETE CASCADE,
    FOREIGN KEY (resource_id) REFERENCES resource(resource_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 9. project - Research project (primary organizational unit)
--
--    Groups experiments, artifacts, and allocation projects.
--    Artifact associations (repository, datasets) managed by Hibernate
--    via research_artifact JOINED hierarchy.
-- ============================================================================

CREATE TABLE IF NOT EXISTS project (
    project_id VARCHAR(255) NOT NULL,
    gateway_id VARCHAR(255) NOT NULL,
    owner_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    state VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (project_id),
    INDEX idx_project_gateway (gateway_id),
    INDEX idx_project_owner (owner_id),
    FOREIGN KEY (gateway_id) REFERENCES gateway(gateway_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 10. experiment
--
--    State is a direct column, mutated by process status cascade.
--    Inputs and outputs are separate entity tables (see below).
--    scheduling JSON: {queueName, nodeCount, cpuCount, walltime, ...}
-- ============================================================================

CREATE TABLE IF NOT EXISTS experiment (
    experiment_id VARCHAR(255) NOT NULL,
    project_id VARCHAR(255),
    gateway_id VARCHAR(255) NOT NULL,
    user_name VARCHAR(255) NOT NULL,
    experiment_name VARCHAR(255) NOT NULL,
    description TEXT,
    application_id VARCHAR(255) NOT NULL,
    binding_id VARCHAR(255) NOT NULL,
    state VARCHAR(50) NOT NULL DEFAULT 'CREATED',
    scheduling JSON,
    created_at BIGINT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (experiment_id),
    INDEX idx_experiment_project (project_id),
    INDEX idx_experiment_gateway (gateway_id),
    INDEX idx_experiment_user (user_name),
    INDEX idx_experiment_app (application_id),
    INDEX idx_experiment_binding (binding_id),
    INDEX idx_experiment_state (state),
    INDEX idx_experiment_created_at (created_at),
    FOREIGN KEY (project_id) REFERENCES project(project_id) ON DELETE SET NULL,
    FOREIGN KEY (gateway_id) REFERENCES gateway(gateway_id) ON DELETE CASCADE,
    FOREIGN KEY (application_id) REFERENCES application(application_id) ON DELETE RESTRICT,
    FOREIGN KEY (binding_id) REFERENCES resource_binding(binding_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 11. experiment_input - Structured experiment inputs
--
--    Each input can be a plain parameter (STRING, INTEGER, FLOAT) or
--    an artifact reference (type=ARTIFACT, artifact_id populated).
--    artifact_id references the research_artifact Hibernate table (no DDL FK).
-- ============================================================================

CREATE TABLE IF NOT EXISTS experiment_input (
    input_id VARCHAR(255) NOT NULL,
    experiment_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    artifact_id VARCHAR(48),
    value TEXT,
    command_line_arg VARCHAR(255),
    required BOOLEAN NOT NULL DEFAULT FALSE,
    add_to_command_line BOOLEAN NOT NULL DEFAULT TRUE,
    order_index INT NOT NULL DEFAULT 0,
    description TEXT,
    PRIMARY KEY (input_id),
    INDEX idx_exp_input_experiment (experiment_id),
    INDEX idx_exp_input_artifact (artifact_id),
    FOREIGN KEY (experiment_id) REFERENCES experiment(experiment_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 12. experiment_output - Structured experiment outputs
--
--    Same artifact-or-parameter pattern as inputs.
--    value is populated after experiment completion.
-- ============================================================================

CREATE TABLE IF NOT EXISTS experiment_output (
    output_id VARCHAR(255) NOT NULL,
    experiment_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    artifact_id VARCHAR(48),
    value TEXT,
    command_line_arg VARCHAR(255),
    required BOOLEAN NOT NULL DEFAULT FALSE,
    data_movement BOOLEAN NOT NULL DEFAULT FALSE,
    order_index INT NOT NULL DEFAULT 0,
    description TEXT,
    location VARCHAR(1024),
    PRIMARY KEY (output_id),
    INDEX idx_exp_output_experiment (experiment_id),
    INDEX idx_exp_output_artifact (artifact_id),
    FOREIGN KEY (experiment_id) REFERENCES experiment(experiment_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 13. process - Execution unit; inherits context from experiment
--
--    resource_schedule JSON: overrides/details used at submission time
-- ============================================================================

CREATE TABLE IF NOT EXISTS process (
    process_id VARCHAR(255) NOT NULL,
    experiment_id VARCHAR(255) NOT NULL,
    application_id VARCHAR(255),
    resource_id VARCHAR(255),
    binding_id VARCHAR(255),
    process_detail LONGTEXT,
    resource_schedule JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (process_id),
    INDEX idx_process_experiment (experiment_id),
    FOREIGN KEY (experiment_id) REFERENCES experiment(experiment_id) ON DELETE CASCADE,
    FOREIGN KEY (resource_id) REFERENCES resource(resource_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 14. event - Process-level audit trail (status changes + errors)
--
--    All events are at the process level. Experiment state is derived
--    from process events and stored directly on the experiment.state column.
-- ============================================================================

CREATE TABLE IF NOT EXISTS event (
    event_id VARCHAR(255) NOT NULL,
    process_id VARCHAR(255) NOT NULL,
    event_kind VARCHAR(20) NOT NULL,
    event_time TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    sequence_num BIGINT NOT NULL DEFAULT 0,
    state VARCHAR(255),
    reason LONGTEXT,
    actual_error_message LONGTEXT,
    user_friendly_message LONGTEXT,
    transient_or_persistent BOOLEAN,
    root_cause_error_id_list LONGTEXT,
    PRIMARY KEY (event_id),
    INDEX idx_event_process (process_id),
    INDEX idx_event_kind (event_kind),
    INDEX idx_event_process_kind_seq (process_id, event_kind, sequence_num DESC),
    INDEX idx_event_time (event_time),
    FOREIGN KEY (process_id) REFERENCES process(process_id) ON DELETE CASCADE,
    CONSTRAINT chk_event_kind CHECK (event_kind IN ('STATUS', 'ERROR'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 15. job - HPC/fork job tracking
-- ============================================================================

CREATE TABLE IF NOT EXISTS job (
    job_id              VARCHAR(255)  NOT NULL,
    process_id          VARCHAR(255)  NOT NULL,
    task_id             VARCHAR(255),
    job_name            VARCHAR(255),
    working_dir         MEDIUMTEXT,
    job_description     MEDIUMTEXT,
    std_out             MEDIUMTEXT,
    std_err             MEDIUMTEXT,
    exit_code           INT           DEFAULT 0,
    created_at          BIGINT        DEFAULT 0,
    compute_resource_consumed VARCHAR(255),
    job_statuses        JSON,
    PRIMARY KEY (job_id),
    INDEX idx_job_process_id (process_id),
    INDEX idx_job_task_id (task_id),
    INDEX idx_job_name (job_name),
    FOREIGN KEY (process_id) REFERENCES process(process_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 16. user_group - Sharing groups
-- ============================================================================

CREATE TABLE IF NOT EXISTS user_group (
    group_id VARCHAR(255) NOT NULL,
    gateway_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    owner_id VARCHAR(255) NOT NULL,
    group_type VARCHAR(255) NOT NULL,
    group_cardinality VARCHAR(255) NOT NULL,
    is_personal_group BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (group_id, gateway_id),
    FOREIGN KEY (gateway_id) REFERENCES gateway(gateway_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX IF NOT EXISTS idx_user_group_owner ON user_group(owner_id);
CREATE INDEX IF NOT EXISTS idx_user_group_type ON user_group(group_type);
CREATE INDEX IF NOT EXISTS idx_personal_group ON user_group(is_personal_group, owner_id(255));

-- ============================================================================
-- 17. entity_relationship - Unified group membership and sharing
-- ============================================================================

CREATE TABLE IF NOT EXISTS entity_relationship (
    relationship_id VARCHAR(512) NOT NULL,
    source_type VARCHAR(50) NOT NULL,
    source_id VARCHAR(255) NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id VARCHAR(255) NOT NULL,
    relation_kind VARCHAR(50) NOT NULL,
    role VARCHAR(50),
    permission_type_id VARCHAR(255),
    domain_id VARCHAR(255),
    metadata JSON,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    PRIMARY KEY (relationship_id),
    INDEX idx_er_source (source_type, source_id),
    INDEX idx_er_target (target_type, target_id),
    INDEX idx_er_kind (relation_kind)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 18. resource_preference - Multi-level preference overrides
-- ============================================================================

CREATE TABLE IF NOT EXISTS resource_preference (
    preference_id BIGINT NOT NULL AUTO_INCREMENT,
    resource_type VARCHAR(64) NOT NULL,
    resource_id VARCHAR(512) NOT NULL,
    owner_id VARCHAR(512) NOT NULL,
    preference_level VARCHAR(32) NOT NULL,
    pref_key VARCHAR(255) NOT NULL,
    pref_value MEDIUMTEXT,
    value_type VARCHAR(32),
    enforced BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (preference_id),
    INDEX idx_rp_resource (resource_type, resource_id(255)),
    INDEX idx_rp_owner (owner_id(255)),
    INDEX idx_rp_key (pref_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 19. allocation_project - HPC allocation projects per resource
-- ============================================================================

CREATE TABLE IF NOT EXISTS allocation_project (
    allocation_project_id VARCHAR(255) NOT NULL,
    project_code VARCHAR(255) NOT NULL,
    resource_id VARCHAR(255) NOT NULL,
    description TEXT,
    gateway_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (allocation_project_id),
    UNIQUE KEY uk_alloc_project (project_code, resource_id),
    INDEX idx_alloc_project_resource (resource_id),
    INDEX idx_alloc_project_gateway (gateway_id),
    FOREIGN KEY (resource_id) REFERENCES resource(resource_id) ON DELETE CASCADE,
    FOREIGN KEY (gateway_id) REFERENCES gateway(gateway_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 20. credential_allocation_project - Links credentials to allocation projects
-- ============================================================================

CREATE TABLE IF NOT EXISTS credential_allocation_project (
    credential_id VARCHAR(255) NOT NULL,
    allocation_project_id VARCHAR(255) NOT NULL,
    binding_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (credential_id, allocation_project_id),
    FOREIGN KEY (allocation_project_id) REFERENCES allocation_project(allocation_project_id) ON DELETE CASCADE,
    FOREIGN KEY (binding_id) REFERENCES resource_binding(binding_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 21. workflow + workflow_run - User-level DAG workflow definitions and runs
-- ============================================================================

CREATE TABLE IF NOT EXISTS workflow (
    workflow_id  VARCHAR(255)  NOT NULL,
    project_id   VARCHAR(255)  NOT NULL,
    gateway_id   VARCHAR(255)  NOT NULL,
    user_name    VARCHAR(255)  NOT NULL,
    workflow_name VARCHAR(255) NOT NULL,
    description  TEXT,
    steps        MEDIUMTEXT    NOT NULL,
    edges        MEDIUMTEXT    NOT NULL,
    created_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (workflow_id),
    INDEX idx_workflow_project (project_id, gateway_id),
    INDEX idx_workflow_user (user_name, gateway_id),
    FOREIGN KEY (project_id) REFERENCES project(project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS workflow_run (
    run_id       VARCHAR(255)  NOT NULL,
    workflow_id  VARCHAR(255)  NOT NULL,
    user_name    VARCHAR(255)  NOT NULL,
    status       VARCHAR(50)   NOT NULL DEFAULT 'CREATED',
    step_states  MEDIUMTEXT    NOT NULL,
    created_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (run_id),
    INDEX idx_workflow_run_workflow (workflow_id),
    INDEX idx_workflow_run_user (user_name),
    FOREIGN KEY (workflow_id) REFERENCES workflow(workflow_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 22. compute_submission_tracking - Rate-limiting guard per resource
-- ============================================================================

CREATE TABLE IF NOT EXISTS compute_submission_tracking (
    compute_resource_id VARCHAR(255)  NOT NULL,
    last_submission_time BIGINT       NOT NULL,
    PRIMARY KEY (compute_resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- VIEWS
-- ============================================================================

-- Experiment summary: state is directly on the experiment row
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
-- END OF BASELINE SCHEMA
-- ============================================================================
