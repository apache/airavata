-- ============================================================================
-- ARAVATA SCHEDULER - POSTGRESQL SCHEMA
-- ============================================================================
-- This schema defines the complete database structure for the Airavata Scheduler
-- system, including all tables, indexes, constraints, and initial data.
-- 
-- PostgreSQL Version: 12+
-- ============================================================================

-- ============================================================================
-- USER MANAGEMENT
-- ============================================================================

-- Users table with enhanced authentication support
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(255) PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    full_name VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP,
    uid INT, -- Unix user ID for compute resources
    g_id INT, -- Unix group ID for compute resources
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CHECK (LENGTH(username) >= 3 AND LENGTH(username) <= 50),
    CHECK (email LIKE '%@%'),
    CHECK (password_hash IS NULL OR password_hash = '' OR LENGTH(password_hash) >= 32)
);

-- Groups table for user organization
CREATE TABLE IF NOT EXISTS groups (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    owner_id VARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CHECK (LENGTH(name) >= 1 AND LENGTH(name) <= 100),
    
    -- Foreign keys
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Group memberships table (supports both users and groups as members)
CREATE TABLE IF NOT EXISTS group_memberships (
    id VARCHAR(255) PRIMARY KEY,
    member_type VARCHAR(20) NOT NULL, -- USER, GROUP
    member_id VARCHAR(255) NOT NULL,
    group_id VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'MEMBER', -- OWNER, ADMIN, MEMBER, VIEWER
    is_active BOOLEAN DEFAULT TRUE,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CHECK (member_type IN ('USER', 'GROUP')),
    CHECK (role IN ('OWNER', 'ADMIN', 'MEMBER', 'VIEWER')),
    
    -- Foreign keys
    FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    
    -- Unique constraint to prevent duplicate memberships
    UNIQUE (member_type, member_id, group_id)
);

-- ============================================================================
-- PROJECT MANAGEMENT
-- ============================================================================

-- Projects table for organizing experiments
CREATE TABLE IF NOT EXISTS projects (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    owner_id VARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CHECK (LENGTH(name) >= 1 AND LENGTH(name) <= 255),
    
    -- Foreign keys
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Unique constraint per owner
    UNIQUE (owner_id, name)
);

-- ============================================================================
-- EXPERIMENT MANAGEMENT
-- ============================================================================

-- Experiments table with comprehensive metadata
CREATE TABLE IF NOT EXISTS experiments (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    project_id VARCHAR(255) NOT NULL,
    owner_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'CREATED',
    command_template TEXT, -- Command template for task execution
    output_pattern TEXT, -- Output file pattern
    task_template TEXT, -- Dynamic task template (JSONB)
    generated_tasks TEXT, -- Generated task specifications (JSONB)
    execution_summary TEXT, -- Execution summary and metrics (JSONB)
    parameters JSONB, -- Parameter sweep configuration
    requirements JSONB, -- Resource requirements
    constraints JSONB, -- Experiment constraints
    priority INT DEFAULT 5, -- 1-10 scale, 10 being highest priority
    deadline TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    metadata JSONB, -- Additional experiment metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CHECK (LENGTH(name) >= 1 AND LENGTH(name) <= 255),
    CHECK (status IN ('CREATED', 'EXECUTING', 'COMPLETED', 'CANCELED')),
    CHECK (priority >= 1 AND priority <= 10),
    CHECK (deadline IS NULL OR deadline > created_at),
    
    -- Foreign keys
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Unique constraint per project
    UNIQUE (project_id, name)
);

-- Tasks table with detailed execution tracking
CREATE TABLE IF NOT EXISTS tasks (
    id VARCHAR(255) PRIMARY KEY,
    experiment_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'CREATED',
    command TEXT NOT NULL,
    execution_script TEXT,
    input_files JSONB,
    output_files JSONB,
    result_summary TEXT,
    execution_metrics TEXT,
    worker_assignment_history TEXT,
    worker_id VARCHAR(255),
    compute_resource_id VARCHAR(255),
    retry_count INT DEFAULT 0,
    max_retries INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    staging_started_at TIMESTAMP,
    staging_completed_at TIMESTAMP,
    duration BIGINT, -- in nanoseconds
    error TEXT,
    metadata JSONB,
    
    -- Constraints
    CHECK (status IN ('CREATED', 'QUEUED', 'DATA_STAGING', 'ENV_SETUP', 'RUNNING', 'OUTPUT_STAGING', 'COMPLETED', 'FAILED', 'CANCELED')),
    CHECK (retry_count >= 0),
    CHECK (max_retries >= 0),
    CHECK (retry_count <= max_retries),
    CHECK (started_at IS NULL OR started_at >= created_at),
    CHECK (completed_at IS NULL OR completed_at >= started_at),
    
    -- Foreign keys
    FOREIGN KEY (experiment_id) REFERENCES experiments(id) ON DELETE CASCADE
);

-- ============================================================================
-- WORKER MANAGEMENT
-- ============================================================================

-- Workers table with metrics
CREATE TABLE IF NOT EXISTS workers (
    id VARCHAR(255) PRIMARY KEY,
    compute_resource_id VARCHAR(255) NOT NULL,
    experiment_id VARCHAR(255),
    user_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_heartbeat TIMESTAMP,
    current_task_id VARCHAR(255),
    total_tasks_completed INT DEFAULT 0,
    total_tasks_failed INT DEFAULT 0,
    avg_task_duration_sec FLOAT,
    cpu_usage_percent FLOAT,
    memory_usage_percent FLOAT,
    walltime BIGINT,
    spawned_at TIMESTAMP,
    walltime_remaining BIGINT,
    started_at TIMESTAMP,
    terminated_at TIMESTAMP,
    capabilities JSONB,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CHECK (status IN ('IDLE', 'BUSY')),
    CHECK (total_tasks_completed >= 0),
    CHECK (total_tasks_failed >= 0),
    CHECK (cpu_usage_percent IS NULL OR (cpu_usage_percent >= 0 AND cpu_usage_percent <= 100)),
    CHECK (memory_usage_percent IS NULL OR (memory_usage_percent >= 0 AND memory_usage_percent <= 100)),
    CHECK (walltime IS NULL OR walltime > 0),
    CHECK (walltime_remaining IS NULL OR walltime_remaining >= 0),
    CHECK (last_heartbeat IS NULL OR last_heartbeat >= registered_at),
    CHECK (spawned_at IS NULL OR spawned_at >= registered_at),
    
    -- Foreign keys
    FOREIGN KEY (experiment_id) REFERENCES experiments(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Worker metrics for monitoring and optimization
CREATE TABLE IF NOT EXISTS worker_metrics (
    id VARCHAR(255) PRIMARY KEY,
    worker_id VARCHAR(255) NOT NULL,
    cpu_usage_percent FLOAT,
    memory_usage_percent FLOAT,
    tasks_completed INT DEFAULT 0,
    tasks_failed INT DEFAULT 0,
    average_task_duration BIGINT, -- in nanoseconds
    last_task_duration BIGINT,    -- in nanoseconds
    uptime BIGINT,                -- in nanoseconds
    custom_metrics JSONB,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CHECK (cpu_usage_percent IS NULL OR (cpu_usage_percent >= 0 AND cpu_usage_percent <= 100)),
    CHECK (memory_usage_percent IS NULL OR (memory_usage_percent >= 0 AND memory_usage_percent <= 100)),
    CHECK (tasks_completed >= 0),
    CHECK (tasks_failed >= 0),
    CHECK (average_task_duration IS NULL OR average_task_duration >= 0),
    CHECK (last_task_duration IS NULL OR last_task_duration >= 0),
    CHECK (uptime IS NULL OR uptime >= 0),
    
    -- Foreign keys
    FOREIGN KEY (worker_id) REFERENCES workers(id) ON DELETE CASCADE
);

-- Task claims for atomic assignment (prevents duplicate execution)
CREATE TABLE IF NOT EXISTS task_claims (
    task_id VARCHAR(255) PRIMARY KEY,
    worker_id VARCHAR(255) NOT NULL,
    claimed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    
    -- Constraints
    CHECK (expires_at > claimed_at),
    
    -- Foreign keys
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (worker_id) REFERENCES workers(id) ON DELETE CASCADE
);

-- Task execution history for historical cost calculations
CREATE TABLE IF NOT EXISTS task_execution_history (
    id VARCHAR(255) PRIMARY KEY,
    task_id VARCHAR(255) NOT NULL,
    worker_id VARCHAR(255) NOT NULL,
    compute_resource_id VARCHAR(255) NOT NULL,
    duration_sec FLOAT NOT NULL,
    cost FLOAT,
    success BOOLEAN NOT NULL,
    error_message TEXT,
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CHECK (duration_sec > 0),
    CHECK (cost IS NULL OR cost >= 0),
    
    -- Foreign keys
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (worker_id) REFERENCES workers(id) ON DELETE CASCADE
);

-- ============================================================================
-- CREDENTIAL MANAGEMENT
-- ============================================================================
-- Note: Credentials are now stored in OpenBao (vault) and authorization
-- is managed by SpiceDB. No credential tables are needed in PostgreSQL.

-- ============================================================================
-- RESOURCE MANAGEMENT
-- ============================================================================

-- Compute resources with cost metrics
CREATE TABLE IF NOT EXISTS compute_resources (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    endpoint VARCHAR(500),
    owner_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    cost_per_hour FLOAT NOT NULL,
    data_latency_ms FLOAT DEFAULT 0,
    max_workers INT NOT NULL,
    current_workers INT DEFAULT 0,
    ssh_key_path VARCHAR(500),
    port INT,
    capabilities JSONB,
    availability FLOAT DEFAULT 1.0,
    avg_task_duration_sec FLOAT,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CHECK (LENGTH(name) >= 1 AND LENGTH(name) <= 255),
    CHECK (type IN ('SLURM', 'BARE_METAL', 'KUBERNETES', 'AWS_BATCH', 'AZURE_BATCH', 'GCP_BATCH')),
    CHECK (status IN ('ACTIVE', 'INACTIVE', 'MAINTENANCE', 'ERROR')),
    CHECK (cost_per_hour >= 0),
    CHECK (data_latency_ms >= 0),
    CHECK (max_workers > 0),
    CHECK (current_workers >= 0),
    CHECK (availability >= 0 AND availability <= 1),
    CHECK (avg_task_duration_sec IS NULL OR avg_task_duration_sec > 0),
    
    -- Foreign keys
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Storage resources
CREATE TABLE IF NOT EXISTS storage_resources (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    endpoint VARCHAR(500),
    owner_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    total_capacity BIGINT,
    used_capacity BIGINT,
    available_capacity BIGINT,
    region VARCHAR(100),
    zone VARCHAR(100),
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CHECK (LENGTH(name) >= 1 AND LENGTH(name) <= 255),
    CHECK (type IN ('NFS', 'S3', 'SFTP', 'AZURE_BLOB', 'GCP_STORAGE', 'LOCAL')),
    CHECK (status IN ('ACTIVE', 'INACTIVE', 'MAINTENANCE', 'ERROR')),
    CHECK (total_capacity IS NULL OR total_capacity > 0),
    CHECK (used_capacity IS NULL OR used_capacity >= 0),
    CHECK (available_capacity IS NULL OR available_capacity >= 0),
    CHECK (used_capacity IS NULL OR total_capacity IS NULL OR used_capacity <= total_capacity),
    CHECK (available_capacity IS NULL OR total_capacity IS NULL OR available_capacity <= total_capacity),
    
    -- Foreign keys
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================================================
-- PERMISSION SYSTEM
-- ============================================================================

-- Note: Credential permissions are now managed by SpiceDB

-- Resource permissions - access control for compute/storage resources
CREATE TABLE IF NOT EXISTS resource_permissions (
    id VARCHAR(255) PRIMARY KEY,
    resource_id VARCHAR(255) NOT NULL,
    resource_type VARCHAR(50) NOT NULL, -- COMPUTE, STORAGE
    owner_id VARCHAR(255) NOT NULL,
    group_id VARCHAR(255),
    owner_perms VARCHAR(3) NOT NULL DEFAULT 'rwx',
    group_perms VARCHAR(3) NOT NULL DEFAULT 'r--',
    other_perms VARCHAR(3) NOT NULL DEFAULT '---',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CHECK (resource_type IN ('COMPUTE', 'STORAGE')),
    CHECK (owner_perms ~ '^[rwx-]{3}$'),
    CHECK (group_perms ~ '^[rwx-]{3}$'),
    CHECK (other_perms ~ '^[rwx-]{3}$'),
    
    -- Foreign keys
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    
    -- Unique constraints
    UNIQUE (resource_id, resource_type)
);

-- Experiment permissions - who can access/modify experiments
CREATE TABLE IF NOT EXISTS experiment_permissions (
    id VARCHAR(255) PRIMARY KEY,
    experiment_id VARCHAR(255) NOT NULL,
    owner_id VARCHAR(255) NOT NULL,
    group_id VARCHAR(255),
    owner_perms VARCHAR(3) NOT NULL DEFAULT 'rwx',
    group_perms VARCHAR(3) NOT NULL DEFAULT 'r--',
    other_perms VARCHAR(3) NOT NULL DEFAULT '---',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CHECK (owner_perms ~ '^[rwx-]{3}$'),
    CHECK (group_perms ~ '^[rwx-]{3}$'),
    CHECK (other_perms ~ '^[rwx-]{3}$'),
    
    -- Foreign keys
    FOREIGN KEY (experiment_id) REFERENCES experiments(id) ON DELETE CASCADE,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    
    -- Unique constraints
    UNIQUE (experiment_id)
);

-- Sharing registry - tracks all sharing relationships for audit
CREATE TABLE IF NOT EXISTS sharing_registry (
    id VARCHAR(255) PRIMARY KEY,
    resource_type VARCHAR(50) NOT NULL, -- CREDENTIAL, RESOURCE, EXPERIMENT
    resource_id VARCHAR(255) NOT NULL,
    from_user_id VARCHAR(255) NOT NULL,
    to_user_id VARCHAR(255),
    to_group_id VARCHAR(255),
    permission VARCHAR(3) NOT NULL, -- r, w, x, rw, rx, wx, rwx
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    revoked_at TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Constraints
    CHECK (resource_type IN ('CREDENTIAL', 'RESOURCE', 'EXPERIMENT')),
    CHECK (permission ~ '^[rwx]{1,3}$'),
    CHECK (to_user_id IS NOT NULL OR to_group_id IS NOT NULL),
    CHECK (to_user_id IS NULL OR to_group_id IS NULL),
    CHECK (revoked_at IS NULL OR revoked_at >= granted_at),
    
    -- Foreign keys
    FOREIGN KEY (from_user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (to_user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (to_group_id) REFERENCES groups(id) ON DELETE CASCADE
);

-- ============================================================================
-- DATA MANAGEMENT
-- ============================================================================

-- Data operations tracking
CREATE TABLE IF NOT EXISTS data_operations (
    id VARCHAR(255) PRIMARY KEY,
    task_id VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    source_path VARCHAR(1000),
    destination_path VARCHAR(1000),
    total_size BIGINT,
    transferred_size BIGINT,
    transfer_rate FLOAT,
    error_message TEXT,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CHECK (type IN ('STAGE_IN', 'STAGE_OUT', 'CACHE_HIT', 'CACHE_MISS')),
    CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED', 'CANCELLED')),
    CHECK (total_size IS NULL OR total_size >= 0),
    CHECK (transferred_size IS NULL OR transferred_size >= 0),
    CHECK (transferred_size IS NULL OR total_size IS NULL OR transferred_size <= total_size),
    CHECK (transfer_rate IS NULL OR transfer_rate >= 0),
    CHECK (started_at IS NULL OR started_at >= created_at),
    CHECK (completed_at IS NULL OR completed_at >= started_at),
    
    -- Foreign keys
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE
);

-- Data cache table - persistent cache for data locations with checksum tracking
CREATE TABLE IF NOT EXISTS data_cache (
    id VARCHAR(255) PRIMARY KEY,
    file_path VARCHAR(1000) NOT NULL,
    checksum VARCHAR(64) NOT NULL,
    compute_resource_id VARCHAR(255) NOT NULL,
    storage_resource_id VARCHAR(255) NOT NULL,
    -- Note: credential_id removed - credential scoping now handled by SpiceDB
    location_type VARCHAR(50) NOT NULL, -- CENTRAL, COMPUTE_STORAGE, WORKER
    cached_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_verified TIMESTAMP,
    size_bytes BIGINT,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CHECK (LENGTH(checksum) = 64), -- SHA-256 checksum
    CHECK (location_type IN ('CENTRAL', 'COMPUTE_STORAGE', 'WORKER')),
    CHECK (size_bytes IS NULL OR size_bytes >= 0),
    CHECK (last_verified IS NULL OR last_verified >= cached_at),
    
    -- Foreign keys
    FOREIGN KEY (compute_resource_id) REFERENCES compute_resources(id) ON DELETE CASCADE,
    FOREIGN KEY (storage_resource_id) REFERENCES storage_resources(id) ON DELETE CASCADE,
    -- Note: credential foreign key removed
    
    -- Unique constraints (credential scoping now handled by SpiceDB)
    UNIQUE (file_path, checksum, compute_resource_id, location_type)
);

-- Data lineage table - complete file movement history tracking
CREATE TABLE IF NOT EXISTS data_lineage (
    id VARCHAR(255) PRIMARY KEY,
    file_id VARCHAR(255) NOT NULL,
    source_location VARCHAR(1000) NOT NULL,
    destination_location VARCHAR(1000) NOT NULL,
    source_checksum VARCHAR(64),
    destination_checksum VARCHAR(64),
    transfer_type VARCHAR(50) NOT NULL, -- STAGE_IN, STAGE_OUT, CACHE_HIT
    task_id VARCHAR(255),
    worker_id VARCHAR(255),
    transferred_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    duration_ms BIGINT,
    size_bytes BIGINT,
    success BOOLEAN DEFAULT TRUE,
    error_message TEXT,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CHECK (transfer_type IN ('STAGE_IN', 'STAGE_OUT', 'CACHE_HIT', 'CACHE_MISS')),
    CHECK (duration_ms IS NULL OR duration_ms >= 0),
    CHECK (size_bytes IS NULL OR size_bytes >= 0),
    CHECK (source_checksum IS NULL OR LENGTH(source_checksum) = 64),
    CHECK (destination_checksum IS NULL OR LENGTH(destination_checksum) = 64),
    
    -- Foreign keys
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE SET NULL,
    FOREIGN KEY (worker_id) REFERENCES workers(id) ON DELETE SET NULL
);

-- ============================================================================
-- PRODUCTION ENHANCEMENTS
-- ============================================================================

-- Audit logs table for compliance and security
CREATE TABLE IF NOT EXISTS audit_logs (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id VARCHAR(255),
    changes JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CHECK (LENGTH(action) >= 1 AND LENGTH(action) <= 100),
    CHECK (resource_type IN ('EXPERIMENT', 'TASK', 'WORKER', 'COMPUTE_RESOURCE', 'STORAGE_RESOURCE', 'CREDENTIAL', 'PROJECT', 'USER')),
    
    -- Foreign key to users (optional, as user might be deleted)
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Experiment tags table for user-defined tagging
CREATE TABLE IF NOT EXISTS experiment_tags (
    id VARCHAR(255) PRIMARY KEY,
    experiment_id VARCHAR(255) NOT NULL,
    tag_name VARCHAR(100) NOT NULL,
    tag_value VARCHAR(255),
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CHECK (LENGTH(tag_name) >= 1 AND LENGTH(tag_name) <= 100),
    CHECK (LENGTH(tag_value) <= 255),
    
    -- Foreign keys
    FOREIGN KEY (experiment_id) REFERENCES experiments(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Unique constraint to prevent duplicate tags
    UNIQUE (experiment_id, tag_name)
);

-- Task result aggregation table for performance
CREATE TABLE IF NOT EXISTS task_result_aggregates (
    id VARCHAR(255) PRIMARY KEY,
    experiment_id VARCHAR(255) NOT NULL,
    parameter_set_id VARCHAR(255),
    total_tasks INT NOT NULL DEFAULT 0,
    completed_tasks INT NOT NULL DEFAULT 0,
    failed_tasks INT NOT NULL DEFAULT 0,
    running_tasks INT NOT NULL DEFAULT 0,
    success_rate FLOAT,
    avg_duration_sec FLOAT,
    total_cost FLOAT,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CHECK (total_tasks >= 0),
    CHECK (completed_tasks >= 0),
    CHECK (failed_tasks >= 0),
    CHECK (running_tasks >= 0),
    CHECK (success_rate IS NULL OR (success_rate >= 0 AND success_rate <= 1)),
    CHECK (avg_duration_sec IS NULL OR avg_duration_sec >= 0),
    CHECK (total_cost IS NULL OR total_cost >= 0),
    
    -- Foreign keys
    FOREIGN KEY (experiment_id) REFERENCES experiments(id) ON DELETE CASCADE,
    
    -- Unique constraint per experiment/parameter set
    UNIQUE (experiment_id, parameter_set_id)
);

-- ============================================================================
-- INDEXES FOR PERFORMANCE
-- ============================================================================

-- User indexes
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_active ON users(is_active);
CREATE INDEX IF NOT EXISTS idx_users_last_login ON users(last_login);

-- Group indexes
CREATE INDEX IF NOT EXISTS idx_groups_name ON groups(name);
CREATE INDEX IF NOT EXISTS idx_groups_owner ON groups(owner_id);
CREATE INDEX IF NOT EXISTS idx_groups_active ON groups(is_active);

-- Group membership indexes
CREATE INDEX IF NOT EXISTS idx_group_memberships_member_type ON group_memberships(member_type);
CREATE INDEX IF NOT EXISTS idx_group_memberships_member_id ON group_memberships(member_id);
CREATE INDEX IF NOT EXISTS idx_group_memberships_group ON group_memberships(group_id);
CREATE INDEX IF NOT EXISTS idx_group_memberships_role ON group_memberships(role);

-- Project indexes
CREATE INDEX IF NOT EXISTS idx_projects_name ON projects(name);
CREATE INDEX IF NOT EXISTS idx_projects_owner ON projects(owner_id);
CREATE INDEX IF NOT EXISTS idx_projects_created ON projects(created_at);

-- Experiment indexes
CREATE INDEX IF NOT EXISTS idx_experiments_status ON experiments(status);
CREATE INDEX IF NOT EXISTS idx_experiments_project ON experiments(project_id);
CREATE INDEX IF NOT EXISTS idx_experiments_owner ON experiments(owner_id);
CREATE INDEX IF NOT EXISTS idx_experiments_created ON experiments(created_at);
CREATE INDEX IF NOT EXISTS idx_experiments_deadline ON experiments(deadline);

-- Enhanced experiment indexes for advanced querying
CREATE INDEX IF NOT EXISTS idx_experiments_parameters_gin ON experiments USING GIN (parameters);
CREATE INDEX IF NOT EXISTS idx_experiments_status_created ON experiments (status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_experiments_owner_status ON experiments (owner_id, status);
CREATE INDEX IF NOT EXISTS idx_experiments_project_status ON experiments (project_id, status);
CREATE INDEX IF NOT EXISTS idx_experiments_deadline_status ON experiments (deadline, status) WHERE deadline IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_experiments_metadata_gin ON experiments USING GIN (metadata);

-- Task indexes
CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status);
CREATE INDEX IF NOT EXISTS idx_tasks_worker ON tasks(worker_id);
CREATE INDEX IF NOT EXISTS idx_tasks_experiment ON tasks(experiment_id);
CREATE INDEX IF NOT EXISTS idx_tasks_created ON tasks(created_at);
CREATE INDEX IF NOT EXISTS idx_tasks_started ON tasks(started_at);
CREATE INDEX IF NOT EXISTS idx_tasks_completed ON tasks(completed_at);

-- Enhanced task indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_tasks_experiment_status ON tasks (experiment_id, status);
CREATE INDEX IF NOT EXISTS idx_tasks_worker_status ON tasks (worker_id, status);
CREATE INDEX IF NOT EXISTS idx_tasks_created_status ON tasks (created_at, status);
CREATE INDEX IF NOT EXISTS idx_tasks_metadata_gin ON tasks USING GIN (metadata);
CREATE INDEX IF NOT EXISTS idx_tasks_output_files_gin ON tasks USING GIN (output_files);

-- Worker indexes
CREATE INDEX IF NOT EXISTS idx_workers_status ON workers(status);
CREATE INDEX IF NOT EXISTS idx_workers_compute ON workers(compute_resource_id);
CREATE INDEX IF NOT EXISTS idx_workers_heartbeat ON workers(last_heartbeat);
CREATE INDEX IF NOT EXISTS idx_workers_experiment ON workers(experiment_id);
CREATE INDEX IF NOT EXISTS idx_workers_registered ON workers(registered_at);
CREATE INDEX IF NOT EXISTS idx_workers_spawned ON workers(spawned_at);

-- Task claim indexes
CREATE INDEX IF NOT EXISTS idx_task_claims_expires ON task_claims(expires_at);
CREATE INDEX IF NOT EXISTS idx_task_claims_worker ON task_claims(worker_id);
CREATE INDEX IF NOT EXISTS idx_task_claims_claimed ON task_claims(claimed_at);

-- Task execution history indexes
CREATE INDEX IF NOT EXISTS idx_task_history_compute ON task_execution_history(compute_resource_id);
CREATE INDEX IF NOT EXISTS idx_task_history_worker ON task_execution_history(worker_id);
CREATE INDEX IF NOT EXISTS idx_task_history_executed ON task_execution_history(executed_at);
CREATE INDEX IF NOT EXISTS idx_task_history_success ON task_execution_history(success);
CREATE INDEX IF NOT EXISTS idx_task_history_experiment_success ON task_execution_history (task_id, success);
CREATE INDEX IF NOT EXISTS idx_task_history_compute_success ON task_execution_history (compute_resource_id, success);

-- Note: Credential indexes removed - credentials now stored in OpenBao

-- Compute resource indexes
CREATE INDEX IF NOT EXISTS idx_compute_resources_type ON compute_resources(type);
CREATE INDEX IF NOT EXISTS idx_compute_resources_status ON compute_resources(status);
CREATE INDEX IF NOT EXISTS idx_compute_resources_owner ON compute_resources(owner_id);
CREATE INDEX IF NOT EXISTS idx_compute_resources_cost ON compute_resources(cost_per_hour);
CREATE INDEX IF NOT EXISTS idx_compute_resources_availability ON compute_resources(availability);

-- Storage resource indexes
CREATE INDEX IF NOT EXISTS idx_storage_resources_type ON storage_resources(type);
CREATE INDEX IF NOT EXISTS idx_storage_resources_status ON storage_resources(status);
CREATE INDEX IF NOT EXISTS idx_storage_resources_owner ON storage_resources(owner_id);
CREATE INDEX IF NOT EXISTS idx_storage_resources_region ON storage_resources(region);
CREATE INDEX IF NOT EXISTS idx_storage_resources_zone ON storage_resources(zone);

-- Note: Credential permission indexes removed - permissions now managed by SpiceDB

CREATE INDEX IF NOT EXISTS idx_resource_perms_resource ON resource_permissions(resource_id, resource_type);
CREATE INDEX IF NOT EXISTS idx_resource_perms_owner ON resource_permissions(owner_id);
CREATE INDEX IF NOT EXISTS idx_resource_perms_group ON resource_permissions(group_id);

CREATE INDEX IF NOT EXISTS idx_experiment_perms_experiment ON experiment_permissions(experiment_id);
CREATE INDEX IF NOT EXISTS idx_experiment_perms_owner ON experiment_permissions(owner_id);
CREATE INDEX IF NOT EXISTS idx_experiment_perms_group ON experiment_permissions(group_id);

-- Sharing registry indexes
CREATE INDEX IF NOT EXISTS idx_sharing_resource ON sharing_registry(resource_type, resource_id);
CREATE INDEX IF NOT EXISTS idx_sharing_from_user ON sharing_registry(from_user_id);
CREATE INDEX IF NOT EXISTS idx_sharing_to_user ON sharing_registry(to_user_id);
CREATE INDEX IF NOT EXISTS idx_sharing_to_group ON sharing_registry(to_group_id);
CREATE INDEX IF NOT EXISTS idx_sharing_active ON sharing_registry(is_active);
CREATE INDEX IF NOT EXISTS idx_sharing_granted ON sharing_registry(granted_at);

-- Data operation indexes
CREATE INDEX IF NOT EXISTS idx_data_ops_task ON data_operations(task_id);
CREATE INDEX IF NOT EXISTS idx_data_ops_status ON data_operations(status);
CREATE INDEX IF NOT EXISTS idx_data_ops_type ON data_operations(type);
CREATE INDEX IF NOT EXISTS idx_data_ops_started ON data_operations(started_at);
CREATE INDEX IF NOT EXISTS idx_data_ops_completed ON data_operations(completed_at);
CREATE INDEX IF NOT EXISTS idx_data_ops_task_status ON data_operations (task_id, status);

-- Data cache indexes
CREATE INDEX IF NOT EXISTS idx_data_cache_checksum ON data_cache(checksum);
CREATE INDEX IF NOT EXISTS idx_data_cache_compute ON data_cache(compute_resource_id);
CREATE INDEX IF NOT EXISTS idx_data_cache_file_path ON data_cache(file_path);
CREATE INDEX IF NOT EXISTS idx_data_cache_location ON data_cache(location_type);
CREATE INDEX IF NOT EXISTS idx_data_cache_cached ON data_cache(cached_at);
CREATE INDEX IF NOT EXISTS idx_data_cache_verified ON data_cache(last_verified);
-- Note: Credential-related indexes removed - credential scoping now handled by SpiceDB
CREATE INDEX IF NOT EXISTS idx_data_cache_lookup ON data_cache(file_path, checksum, compute_resource_id);

-- Data lineage indexes
CREATE INDEX IF NOT EXISTS idx_data_lineage_file ON data_lineage(file_id);
CREATE INDEX IF NOT EXISTS idx_data_lineage_task ON data_lineage(task_id);
CREATE INDEX IF NOT EXISTS idx_data_lineage_worker ON data_lineage(worker_id);
CREATE INDEX IF NOT EXISTS idx_data_lineage_transfer_time ON data_lineage(transferred_at);
CREATE INDEX IF NOT EXISTS idx_data_lineage_transfer_type ON data_lineage(transfer_type);
CREATE INDEX IF NOT EXISTS idx_data_lineage_success ON data_lineage(success);

-- Audit log indexes
CREATE INDEX IF NOT EXISTS idx_audit_logs_user ON audit_logs (user_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_action ON audit_logs (action);
CREATE INDEX IF NOT EXISTS idx_audit_logs_timestamp ON audit_logs (timestamp);
CREATE INDEX IF NOT EXISTS idx_audit_logs_resource ON audit_logs (resource_type, resource_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_user_timestamp ON audit_logs (user_id, timestamp);

-- Experiment tags indexes
CREATE INDEX IF NOT EXISTS idx_experiment_tags_experiment ON experiment_tags (experiment_id);
CREATE INDEX IF NOT EXISTS idx_experiment_tags_name ON experiment_tags (tag_name);
CREATE INDEX IF NOT EXISTS idx_experiment_tags_value ON experiment_tags (tag_value);
CREATE INDEX IF NOT EXISTS idx_experiment_tags_created_by ON experiment_tags (created_by);

-- Task result aggregates indexes
CREATE INDEX IF NOT EXISTS idx_task_aggregates_experiment ON task_result_aggregates (experiment_id);
CREATE INDEX IF NOT EXISTS idx_task_aggregates_updated ON task_result_aggregates (last_updated);

-- ============================================================================
-- INITIAL DATA
-- ============================================================================

-- Insert default system user
INSERT INTO users (id, username, email, password_hash, is_active) 
VALUES ('system', 'system', 'system@airavata.org', '$2a$10$GbvGGzlt/gMdK1GZ1Hq21.to9LPLKUTEbBEQU41h.0Fvsz6dVcWyu', TRUE);

-- Insert default admin user (password: admin - should be changed on first login)
INSERT INTO users (id, username, email, password_hash, full_name, is_active, metadata) 
VALUES ('admin', 'admin', 'admin@airavata.org', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'System Administrator', TRUE, '{"isAdmin": true, "firstLogin": true}');

-- Insert default system group
INSERT INTO groups (id, name, description, owner_id, is_active)
VALUES ('system', 'system', 'System group for internal operations', 'system', TRUE);

-- Insert default admin group
INSERT INTO groups (id, name, description, owner_id, is_active)
VALUES ('admin', 'admin', 'Administrator group with full system access', 'admin', TRUE);

-- Add admin user to admin group
INSERT INTO group_memberships (id, group_id, member_type, member_id, role, is_active)
VALUES ('admin-membership', 'admin', 'USER', 'admin', 'OWNER', TRUE);

-- ============================================================================
-- SCHEDULER RECOVERY TABLES
-- ============================================================================
-- Include recovery tables for 100% failure recovery capability

-- Scheduler state management
CREATE TABLE IF NOT EXISTS scheduler_state (
    id VARCHAR(255) PRIMARY KEY DEFAULT 'scheduler',
    instance_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'STARTING',
    startup_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    shutdown_time TIMESTAMP,
    clean_shutdown BOOLEAN DEFAULT FALSE,
    last_heartbeat TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CHECK (status IN ('STARTING', 'RUNNING', 'SHUTTING_DOWN', 'STOPPED')),
    CHECK (shutdown_time IS NULL OR shutdown_time >= startup_time),
    CHECK (last_heartbeat >= startup_time)
);

-- Staging operations tracking
CREATE TABLE IF NOT EXISTS staging_operations (
    id VARCHAR(255) PRIMARY KEY,
    task_id VARCHAR(255) NOT NULL,
    worker_id VARCHAR(255) NOT NULL,
    compute_resource_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    source_path VARCHAR(1000),
    destination_path VARCHAR(1000),
    total_size BIGINT,
    transferred_size BIGINT DEFAULT 0,
    transfer_rate FLOAT,
    error_message TEXT,
    timeout_seconds INT DEFAULT 600,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    last_heartbeat TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED', 'TIMEOUT')),
    CHECK (total_size IS NULL OR total_size >= 0),
    CHECK (transferred_size >= 0),
    CHECK (total_size IS NULL OR transferred_size <= total_size),
    CHECK (transfer_rate IS NULL OR transfer_rate >= 0),
    CHECK (timeout_seconds > 0),
    CHECK (started_at IS NULL OR started_at >= created_at),
    CHECK (completed_at IS NULL OR completed_at >= started_at),
    
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (worker_id) REFERENCES workers(id) ON DELETE CASCADE,
    FOREIGN KEY (compute_resource_id) REFERENCES compute_resources(id) ON DELETE CASCADE
);

-- Background jobs tracking
CREATE TABLE IF NOT EXISTS background_jobs (
    id VARCHAR(255) PRIMARY KEY,
    job_type VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    payload JSONB,
    priority INT DEFAULT 5,
    max_retries INT DEFAULT 3,
    retry_count INT DEFAULT 0,
    error_message TEXT,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    last_heartbeat TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    timeout_seconds INT DEFAULT 300,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED', 'CANCELLED')),
    CHECK (priority >= 1 AND priority <= 10),
    CHECK (max_retries >= 0),
    CHECK (retry_count >= 0),
    CHECK (retry_count <= max_retries),
    CHECK (timeout_seconds > 0),
    CHECK (started_at IS NULL OR started_at >= created_at),
    CHECK (completed_at IS NULL OR completed_at >= started_at),
    CHECK (last_heartbeat >= created_at)
);

-- Cache entries
CREATE TABLE IF NOT EXISTS cache_entries (
    key VARCHAR(1000) PRIMARY KEY,
    value BYTEA NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    access_count INT DEFAULT 0,
    last_accessed TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CHECK (expires_at > created_at),
    CHECK (access_count >= 0)
);

-- Event queue
CREATE TABLE IF NOT EXISTS event_queue (
    id VARCHAR(255) PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    priority INT DEFAULT 5,
    max_retries INT DEFAULT 3,
    retry_count INT DEFAULT 0,
    error_message TEXT,
    processed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED')),
    CHECK (priority >= 1 AND priority <= 10),
    CHECK (max_retries >= 0),
    CHECK (retry_count >= 0),
    CHECK (retry_count <= max_retries),
    CHECK (processed_at IS NULL OR processed_at >= created_at)
);

-- Worker connection state enhancements
ALTER TABLE workers ADD COLUMN IF NOT EXISTS connection_state VARCHAR(50) DEFAULT 'DISCONNECTED';
ALTER TABLE workers ADD COLUMN IF NOT EXISTS last_seen_at TIMESTAMP;
ALTER TABLE workers ADD COLUMN IF NOT EXISTS connection_attempts INT DEFAULT 0;
ALTER TABLE workers ADD COLUMN IF NOT EXISTS last_connection_attempt TIMESTAMP;

-- Recovery table indexes
CREATE INDEX IF NOT EXISTS idx_scheduler_state_status ON scheduler_state(status);
CREATE INDEX IF NOT EXISTS idx_scheduler_state_instance ON scheduler_state(instance_id);
CREATE INDEX IF NOT EXISTS idx_scheduler_state_heartbeat ON scheduler_state(last_heartbeat);

CREATE INDEX IF NOT EXISTS idx_staging_ops_status ON staging_operations(status);
CREATE INDEX IF NOT EXISTS idx_staging_ops_task ON staging_operations(task_id);
CREATE INDEX IF NOT EXISTS idx_staging_ops_worker ON staging_operations(worker_id);
CREATE INDEX IF NOT EXISTS idx_staging_ops_heartbeat ON staging_operations(last_heartbeat);
CREATE INDEX IF NOT EXISTS idx_staging_ops_created ON staging_operations(created_at);
CREATE INDEX IF NOT EXISTS idx_staging_ops_timeout ON staging_operations(started_at, timeout_seconds) 
    WHERE status = 'RUNNING';

CREATE INDEX IF NOT EXISTS idx_bg_jobs_status ON background_jobs(status);
CREATE INDEX IF NOT EXISTS idx_bg_jobs_type ON background_jobs(job_type);
CREATE INDEX IF NOT EXISTS idx_bg_jobs_priority ON background_jobs(priority DESC);
CREATE INDEX IF NOT EXISTS idx_bg_jobs_heartbeat ON background_jobs(last_heartbeat);
CREATE INDEX IF NOT EXISTS idx_bg_jobs_created ON background_jobs(created_at);
CREATE INDEX IF NOT EXISTS idx_bg_jobs_timeout ON background_jobs(started_at, timeout_seconds) 
    WHERE status = 'RUNNING';

CREATE INDEX IF NOT EXISTS idx_cache_expires ON cache_entries(expires_at);
CREATE INDEX IF NOT EXISTS idx_cache_accessed ON cache_entries(last_accessed);
CREATE INDEX IF NOT EXISTS idx_cache_access_count ON cache_entries(access_count);

CREATE INDEX IF NOT EXISTS idx_event_queue_status ON event_queue(status);
CREATE INDEX IF NOT EXISTS idx_event_queue_type ON event_queue(event_type);
CREATE INDEX IF NOT EXISTS idx_event_queue_priority ON event_queue(priority DESC);
CREATE INDEX IF NOT EXISTS idx_event_queue_created ON event_queue(created_at);
CREATE INDEX IF NOT EXISTS idx_event_queue_retry ON event_queue(retry_count, max_retries) 
    WHERE status = 'FAILED';

CREATE INDEX IF NOT EXISTS idx_workers_connection_state ON workers(connection_state);
CREATE INDEX IF NOT EXISTS idx_workers_last_seen ON workers(last_seen_at);
CREATE INDEX IF NOT EXISTS idx_workers_connection_attempts ON workers(connection_attempts);

-- ============================================================================
-- SCHEDULER RECOVERY TABLES
-- ============================================================================
-- This section adds tables required for 100% failure recovery capability
-- 
-- PostgreSQL Version: 12+
-- ============================================================================

-- ============================================================================
-- SCHEDULER STATE MANAGEMENT
-- ============================================================================

-- Tracks scheduler lifecycle and shutdown state
CREATE TABLE IF NOT EXISTS scheduler_state (
    id VARCHAR(255) PRIMARY KEY DEFAULT 'scheduler',
    instance_id VARCHAR(255) NOT NULL, -- Unique instance identifier
    status VARCHAR(50) NOT NULL DEFAULT 'STARTING', -- STARTING, RUNNING, SHUTTING_DOWN, STOPPED
    startup_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    shutdown_time TIMESTAMP,
    clean_shutdown BOOLEAN DEFAULT FALSE,
    last_heartbeat TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CHECK (status IN ('STARTING', 'RUNNING', 'SHUTTING_DOWN', 'STOPPED')),
    CHECK (shutdown_time IS NULL OR shutdown_time >= startup_time),
    CHECK (last_heartbeat >= startup_time)
);

-- ============================================================================
-- STAGING OPERATIONS TRACKING
-- ============================================================================

-- Tracks all data staging operations for recovery
CREATE TABLE IF NOT EXISTS staging_operations (
    id VARCHAR(255) PRIMARY KEY,
    task_id VARCHAR(255) NOT NULL,
    worker_id VARCHAR(255) NOT NULL,
    compute_resource_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- PENDING, RUNNING, COMPLETED, FAILED, TIMEOUT
    source_path VARCHAR(1000),
    destination_path VARCHAR(1000),
    total_size BIGINT,
    transferred_size BIGINT DEFAULT 0,
    transfer_rate FLOAT,
    error_message TEXT,
    timeout_seconds INT DEFAULT 600, -- 10 minutes default
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    last_heartbeat TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED', 'TIMEOUT')),
    CHECK (total_size IS NULL OR total_size >= 0),
    CHECK (transferred_size >= 0),
    CHECK (total_size IS NULL OR transferred_size <= total_size),
    CHECK (transfer_rate IS NULL OR transfer_rate >= 0),
    CHECK (timeout_seconds > 0),
    CHECK (started_at IS NULL OR started_at >= created_at),
    CHECK (completed_at IS NULL OR completed_at >= started_at),
    
    -- Foreign keys
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (worker_id) REFERENCES workers(id) ON DELETE CASCADE,
    FOREIGN KEY (compute_resource_id) REFERENCES compute_resources(id) ON DELETE CASCADE
);

-- ============================================================================
-- BACKGROUND JOBS TRACKING
-- ============================================================================

-- Generic table for tracking background operations and goroutines
CREATE TABLE IF NOT EXISTS background_jobs (
    id VARCHAR(255) PRIMARY KEY,
    job_type VARCHAR(100) NOT NULL, -- STAGING_MONITOR, WORKER_HEALTH, EVENT_PROCESSOR, etc.
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- PENDING, RUNNING, COMPLETED, FAILED, CANCELLED
    payload JSONB, -- Job-specific data
    priority INT DEFAULT 5, -- 1-10 scale, 10 being highest
    max_retries INT DEFAULT 3,
    retry_count INT DEFAULT 0,
    error_message TEXT,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    last_heartbeat TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    timeout_seconds INT DEFAULT 300, -- 5 minutes default
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED', 'CANCELLED')),
    CHECK (priority >= 1 AND priority <= 10),
    CHECK (max_retries >= 0),
    CHECK (retry_count >= 0),
    CHECK (retry_count <= max_retries),
    CHECK (timeout_seconds > 0),
    CHECK (started_at IS NULL OR started_at >= created_at),
    CHECK (completed_at IS NULL OR completed_at >= started_at),
    CHECK (last_heartbeat >= created_at)
);

-- ============================================================================
-- CACHE ENTRIES
-- ============================================================================

-- PostgreSQL-backed cache storage
CREATE TABLE IF NOT EXISTS cache_entries (
    key VARCHAR(1000) PRIMARY KEY,
    value BYTEA NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    access_count INT DEFAULT 0,
    last_accessed TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CHECK (expires_at > created_at),
    CHECK (access_count >= 0)
);

-- ============================================================================
-- EVENT QUEUE
-- ============================================================================

-- Persistent event queue for reliable event processing
CREATE TABLE IF NOT EXISTS event_queue (
    id VARCHAR(255) PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- PENDING, PROCESSING, COMPLETED, FAILED
    priority INT DEFAULT 5, -- 1-10 scale, 10 being highest
    max_retries INT DEFAULT 3,
    retry_count INT DEFAULT 0,
    error_message TEXT,
    processed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED')),
    CHECK (priority >= 1 AND priority <= 10),
    CHECK (max_retries >= 0),
    CHECK (retry_count >= 0),
    CHECK (retry_count <= max_retries),
    CHECK (processed_at IS NULL OR processed_at >= created_at)
);

-- ============================================================================
-- WORKER CONNECTION STATE ENHANCEMENTS
-- ============================================================================

-- Add connection state tracking to workers table
ALTER TABLE workers ADD COLUMN IF NOT EXISTS connection_state VARCHAR(50) DEFAULT 'DISCONNECTED';
ALTER TABLE workers ADD COLUMN IF NOT EXISTS last_seen_at TIMESTAMP;
ALTER TABLE workers ADD COLUMN IF NOT EXISTS connection_attempts INT DEFAULT 0;
ALTER TABLE workers ADD COLUMN IF NOT EXISTS last_connection_attempt TIMESTAMP;

-- Add constraints for new columns
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'chk_workers_connection_state'
    ) THEN
        ALTER TABLE workers ADD CONSTRAINT chk_workers_connection_state 
            CHECK (connection_state IN ('CONNECTED', 'DISCONNECTED', 'CONNECTING', 'FAILED'));
    END IF;
END $$;

-- ============================================================================
-- INDEXES FOR PERFORMANCE
-- ============================================================================

-- Scheduler state indexes
CREATE INDEX IF NOT EXISTS idx_scheduler_state_status ON scheduler_state(status);
CREATE INDEX IF NOT EXISTS idx_scheduler_state_instance ON scheduler_state(instance_id);
CREATE INDEX IF NOT EXISTS idx_scheduler_state_heartbeat ON scheduler_state(last_heartbeat);

-- Staging operations indexes
CREATE INDEX IF NOT EXISTS idx_staging_ops_status ON staging_operations(status);
CREATE INDEX IF NOT EXISTS idx_staging_ops_task ON staging_operations(task_id);
CREATE INDEX IF NOT EXISTS idx_staging_ops_worker ON staging_operations(worker_id);
CREATE INDEX IF NOT EXISTS idx_staging_ops_heartbeat ON staging_operations(last_heartbeat);
CREATE INDEX IF NOT EXISTS idx_staging_ops_created ON staging_operations(created_at);
CREATE INDEX IF NOT EXISTS idx_staging_ops_timeout ON staging_operations(started_at, timeout_seconds) 
    WHERE status = 'RUNNING';

-- Background jobs indexes
CREATE INDEX IF NOT EXISTS idx_bg_jobs_status ON background_jobs(status);
CREATE INDEX IF NOT EXISTS idx_bg_jobs_type ON background_jobs(job_type);
CREATE INDEX IF NOT EXISTS idx_bg_jobs_priority ON background_jobs(priority DESC);
CREATE INDEX IF NOT EXISTS idx_bg_jobs_heartbeat ON background_jobs(last_heartbeat);
CREATE INDEX IF NOT EXISTS idx_bg_jobs_created ON background_jobs(created_at);
CREATE INDEX IF NOT EXISTS idx_bg_jobs_timeout ON background_jobs(started_at, timeout_seconds) 
    WHERE status = 'RUNNING';

-- Cache entries indexes
CREATE INDEX IF NOT EXISTS idx_cache_expires ON cache_entries(expires_at);
CREATE INDEX IF NOT EXISTS idx_cache_accessed ON cache_entries(last_accessed);
CREATE INDEX IF NOT EXISTS idx_cache_access_count ON cache_entries(access_count);

-- Event queue indexes
CREATE INDEX IF NOT EXISTS idx_event_queue_status ON event_queue(status);
CREATE INDEX IF NOT EXISTS idx_event_queue_type ON event_queue(event_type);
CREATE INDEX IF NOT EXISTS idx_event_queue_priority ON event_queue(priority DESC);
CREATE INDEX IF NOT EXISTS idx_event_queue_created ON event_queue(created_at);
CREATE INDEX IF NOT EXISTS idx_event_queue_retry ON event_queue(retry_count, max_retries) 
    WHERE status = 'FAILED';

-- Worker connection state indexes
CREATE INDEX IF NOT EXISTS idx_workers_connection_state ON workers(connection_state);
CREATE INDEX IF NOT EXISTS idx_workers_last_seen ON workers(last_seen_at);
CREATE INDEX IF NOT EXISTS idx_workers_connection_attempts ON workers(connection_attempts);

-- ============================================================================
-- CLEANUP FUNCTIONS
-- ============================================================================

-- Function to clean up expired cache entries
CREATE OR REPLACE FUNCTION cleanup_expired_cache_entries()
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM cache_entries WHERE expires_at < CURRENT_TIMESTAMP;
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- Function to clean up old completed background jobs
CREATE OR REPLACE FUNCTION cleanup_old_background_jobs(days_to_keep INTEGER DEFAULT 7)
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM background_jobs 
    WHERE status IN ('COMPLETED', 'FAILED', 'CANCELLED') 
    AND completed_at < CURRENT_TIMESTAMP - INTERVAL '1 day' * days_to_keep;
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- Function to clean up old processed events
CREATE OR REPLACE FUNCTION cleanup_old_processed_events(days_to_keep INTEGER DEFAULT 7)
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM event_queue 
    WHERE status IN ('COMPLETED', 'FAILED') 
    AND processed_at < CURRENT_TIMESTAMP - INTERVAL '1 day' * days_to_keep;
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- STATE CHANGE NOTIFICATIONS
-- ============================================================================

-- Function to notify state changes via PostgreSQL NOTIFY
CREATE OR REPLACE FUNCTION notify_state_change()
RETURNS TRIGGER AS $$
BEGIN
    PERFORM pg_notify('state_changes', 
        json_build_object(
            'table', TG_TABLE_NAME,
            'id', NEW.id,
            'old_status', OLD.status,
            'new_status', NEW.status,
            'timestamp', NOW()
        )::text
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger for task state changes
CREATE TRIGGER tasks_state_change
    AFTER UPDATE OF status ON tasks
    FOR EACH ROW
    WHEN (OLD.status IS DISTINCT FROM NEW.status)
    EXECUTE FUNCTION notify_state_change();

-- Trigger for worker state changes
CREATE TRIGGER workers_state_change
    AFTER UPDATE OF status ON workers
    FOR EACH ROW
    WHEN (OLD.status IS DISTINCT FROM NEW.status)
    EXECUTE FUNCTION notify_state_change();

-- Trigger for experiment state changes
CREATE TRIGGER experiments_state_change
    AFTER UPDATE OF status ON experiments
    FOR EACH ROW
    WHEN (OLD.status IS DISTINCT FROM NEW.status)
    EXECUTE FUNCTION notify_state_change();

-- ============================================================================
-- REGISTRATION TOKENS
-- ============================================================================

-- Registration tokens for one-time resource registration
CREATE TABLE IF NOT EXISTS registration_tokens (
    id VARCHAR(255) PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    resource_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign keys
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Constraints
    CHECK (LENGTH(token) >= 10),
    CHECK (expires_at > created_at)
);

-- Index for token lookups
CREATE INDEX IF NOT EXISTS idx_registration_tokens_token ON registration_tokens(token);
CREATE INDEX IF NOT EXISTS idx_registration_tokens_user_id ON registration_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_registration_tokens_expires_at ON registration_tokens(expires_at);

-- ============================================================================
-- INITIAL DATA
-- ============================================================================

-- Insert initial scheduler state
INSERT INTO scheduler_state (id, instance_id, status, startup_time, clean_shutdown)
VALUES ('scheduler', 'initial', 'STOPPED', CURRENT_TIMESTAMP, TRUE)
ON CONFLICT (id) DO NOTHING;