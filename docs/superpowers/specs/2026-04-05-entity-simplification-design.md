# Entity Simplification Design

## Goal

Reduce the Airavata database schema from ~150 tables to ~50 by eliminating Thrift-era normalization, consolidating redundant patterns, and using JSON columns for child collections. Maintain full functionality with a cleaner, domain-aligned structure.

## Principles

1. **Surrogate keys** unless the composite natural key is immutable (e.g., sharing ACL tables)
2. **JSON columns** for child collections that don't need indexing (commands, environment vars, config)
3. **Discriminator columns** to merge type-specific tables (submissions, preferences, status)
4. **Two-layer status history** — research-level and infra-level, both append-only audit logs
5. **Service-prefixed table names** for logical sorting (`iam_`, `sharing_`, `compute_`, `storage_`, `research_`, `exec_`, `agent_`)

## Current State

- 145 JPA entities across 8 modules
- 68 composite PK classes (~5,400 lines of boilerplate)
- 13 status/error entities following identical patterns
- 8 input/output entities following identical patterns
- 7 job submission entities with overlapping fields
- ~30 child-collection tables that exist only because Thrift mapped every nested list to a table

## Target Schema (50 tables)

### Identity & Access (15 tables)

#### IAM (6)

| Table | PK | Key Columns | JSON Columns | Notes |
|-------|-----|-------------|--------------|-------|
| `iam_gateway` | id (surrogate) | gateway_id (unique), gateway_name, domain, email, admin info, oauth | — | Merged GATEWAY + TENANT_GATEWAY. airavata_internal_gateway_id kept as unique column. |
| `iam_user_profile` | airavata_internal_user_id | user_id, gateway_id, first_name, last_name, orcid, country, org | emails, phones, nationality, labeled_uris | Canonical user. ElementCollections → JSON arrays. |
| `iam_nsf_demographics` | user_id (FK) | — | ethnicities, races, disabilities | 1:1 with user_profile. Collections → JSON. |
| `iam_dashboard` | user_id (FK) | — | dashboard_config | 1:1 with user_profile. |
| `iam_gateway_groups` | gateway_id | admins_group_id, read_only_group_id, default_group_id | — | Gateway role groups. |
| `iam_credential` | token (surrogate) | gateway_id, portal_user_name, credential_type | credential_data | Merged CREDENTIALS + COMMUNITY_USER. |

#### Sharing (9) — keep existing generic ACL model

| Table | PK | Notes |
|-------|-----|-------|
| `sharing_domain` | domain_id | Organizational boundary |
| `sharing_user` | (user_id, domain_id) | Per-domain identity. Natural key — immutable. |
| `sharing_user_group` | (group_id, domain_id) | Permission groups. Natural key. |
| `sharing_group_admin` | (group_id, domain_id, admin_id) | Natural key. |
| `sharing_group_membership` | (parent_id, child_id, domain_id) | Natural key. |
| `sharing_entity_type` | (entity_type_id, domain_id) | Type registry. Natural key. |
| `sharing_entity` | (entity_id, domain_id) | Shareable entity registry. Natural key. |
| `sharing_permission_type` | (permission_type_id, domain_id) | Natural key. |
| `sharing_grant` | (permission_type_id, entity_id, group_id, inherited_parent_id, domain_id) | Access grants. Natural key. |

### Compute (10 tables)

| Table | PK | Key Columns | JSON Columns | Absorbs |
|-------|-----|-------------|--------------|---------|
| `compute_resource` | id (surrogate) | resource_id (unique), host_name, enabled | host_aliases, ip_addresses, file_systems | COMPUTE_RESOURCE_FILE_SYSTEM |
| `compute_batch_queue` | id (surrogate) | compute_resource_id (FK), queue_name | — | Stays — queried/filtered by schedulers |
| `compute_job_submission` | id (surrogate) | submission_id (unique), type, compute_resource_id | config (type-specific: ssh_port, security_protocol, etc.) | SSH, GSISSH, Cloud, Local, Globus, Unicore + GSISSH_EXPORT/PREJOB/POSTJOB + GLOBUS_GK_ENDPOINT + GRIDFTP_ENDPOINT |
| `compute_job_manager` | id (surrogate) | resource_job_manager_id (unique), resource_job_manager_type | commands | JOB_MANAGER_COMMAND |
| `compute_app_module` | id (surrogate) | module_id (unique), gateway_id, module_name | — | — |
| `compute_app_interface` | id (surrogate) | interface_id (unique), gateway_id, app_name | — | Inputs/outputs → research_io_param |
| `compute_app_deployment` | id (surrogate) | deployment_id (unique), app_module_id, compute_resource_id, gateway_id | env_vars, module_load_cmds, prejob_cmds, postjob_cmds, lib_prepend_paths, lib_append_paths | APP_ENVIRONMENT, MODULE_LOAD_CMD, PREJOB_COMMAND, POSTJOB_COMMAND, LIBRARY_PREPAND_PATH, LIBRARY_APEND_PATH |
| `compute_resource_preference` | id (surrogate) | scope_type (GATEWAY/USER/GROUP), scope_id, compute_resource_id | ssh_provisioner_config, reservations, policies (allowed_queues, batch_queue_policies) | COMPUTE_RESOURCE_PREFERENCE + USER_COMPUTE_RESOURCE_PREFERENCE + GROUP_COMPUTE_RESOURCE_PREFERENCE + COMPUTE_RESOURCE_POLICY + BATCH_QUEUE_RESOURCE_POLICY + COMPUTE_RESOURCE_RESERVATION + SSH_ACCOUNT_PROVISIONER_CONFIG. Policies and reservations stored as JSON arrays since they're always read/written as a unit with the preference. |
| `compute_queue_status` | (host_name, queue_name, created_time) | queue_up, running_jobs, queued_jobs | — | Natural key — immutable time-series. |
| `compute_parser` | id (surrogate) | parser_id (unique), gateway_id | inputs, outputs, connectors, templates | PARSER_INPUT, PARSER_OUTPUT, PARSER_CONNECTOR, PARSER_CONNECTOR_INPUT, PARSING_TEMPLATE, PARSING_TEMPLATE_INPUT |

### Storage (3 tables)

| Table | PK | Key Columns | JSON Columns | Absorbs |
|-------|-----|-------------|--------------|---------|
| `storage_resource` | id (surrogate) | resource_id (unique), host_name, enabled | — | — |
| `storage_data_movement` | id (surrogate) | resource_id (FK), type, priority | config (type-specific: hostname, port, security_protocol) | DATA_MOVEMENT_INTERFACE + SCP_DATA_MOVEMENT + LOCAL_DATA_MOVEMENT + GRIDFTP_DATA_MOVEMENT + GRIDFTP_ENDPOINT + UNICORE_DATAMOVEMENT |
| `storage_preference` | id (surrogate) | scope_type (GATEWAY/USER), scope_id, storage_resource_id | — | STORAGE_PREFERENCE + USER_STORAGE_PREFERENCE |

### Research (12 tables)

| Table | PK | Key Columns | JSON Columns | Notes |
|-------|-----|-------------|--------------|-------|
| `research_project` | id (surrogate) | project_id (unique), user_name, gateway_id | — | PROJECT_USER absorbed as @JoinTable on entity |
| `research_experiment` | id (surrogate) | experiment_id (unique), project_id, gateway_id, user_name, execution_id | — | — |
| `research_workflow` | id (surrogate) | workflow_id (unique), gateway_id | — | — |
| `research_io_param` | id (surrogate) | entity_type (EXPERIMENT/APPLICATION/WORKFLOW), entity_id, direction (INPUT/OUTPUT), name | metadata (applicationArgument, dataStaged, searchQuery, etc.) | Consolidates 6 entities: Experiment/Application/Workflow × Input/Output |
| `research_status_history` | id (surrogate) | entity_type (EXPERIMENT/WORKFLOW/APPLICATION), entity_id, state, created_at | detail | Consolidates 6 entities: Experiment/Workflow/Application × Status/Error |
| `research_data_product` | id (surrogate) | product_uri (unique), gateway_id, product_type | metadata | DATA_PRODUCT_METADATA absorbed |
| `research_data_replica` | id (surrogate) | replica_id (unique), product_uri (FK) | metadata | DATA_REPLICA_METADATA absorbed |
| `research_resource` | id (surrogate) | resource_type (DATASET/NOTEBOOK/MODEL/REPOSITORY), owner_id | authors | JOINED inheritance subtypes stay for type-specific fields |
| `research_resource_star` | (user_id, resource_id) | — | — | Natural key — immutable. |
| `research_tag` | id (surrogate) | name (unique) | — | — |
| `research_session` | id (surrogate) | session_id (unique), status | — | — |
| `research_notification` | id (surrogate) | gateway_id, title, priority | — | — |

### Execution (7 tables)

| Table | PK | Key Columns | JSON Columns | Absorbs |
|-------|-----|-------------|--------------|---------|
| `exec_process` | id (surrogate) | process_id (unique), experiment_id | resource_schedule, user_config, compute_scheduling | PROCESS_RESOURCE_SCHEDULE, USER_CONFIGURATION_DATA, COMPUTE_RESOURCE_SCHEDULING |
| `exec_task` | id (surrogate) | task_id (unique), process_id (FK), task_type | — | — |
| `exec_job` | id (surrogate) | job_id (unique), task_id (FK) | — | — |
| `exec_io_param` | id (surrogate) | entity_type (PROCESS/HANDLER), entity_id, direction, name | metadata | Consolidates Process/Handler × Input/Output |
| `exec_status_history` | id (surrogate) | entity_type (PROCESS/TASK/JOB/HANDLER), entity_id, state, created_at | detail | Consolidates 8 entities: Process/Task/Job/Handler × Status/Error + AGENT_EXECUTION_STATUS |
| `exec_workflow` | id (surrogate) | workflow_id (unique) | connections, data_blocks | WORKFLOW_CONNECTION, WORKFLOW_DATA_BLOCK absorbed |
| `exec_workflow_step` | id (surrogate) | workflow_id (FK), step_type (APPLICATION/HANDLER) | inputs, outputs, errors | Merges WORKFLOW_APPLICATION + WORKFLOW_HANDLER + their status/error/IO children |

### Agent (3 tables)

| Table | PK | Key Columns | Notes |
|-------|-----|-------------|-------|
| `agent_deployment` | id (surrogate) | agent_id (unique), gateway_id | — |
| `agent_execution` | id (surrogate) | execution_id (unique), agent_id (FK) | Status → exec_status_history |
| `agent_plan` | id (surrogate) | plan_id (unique), gateway_id, user_id | — |

## Migration Strategy

### Flyway Migration Sequence

1. **V3** — Drop orphaned tables (USERS, GATEWAY_WORKER) *(already done)*
2. **V4** — Merge TENANT_GATEWAY into GATEWAY *(already done)*
3. **V5** — Rename all tables to service-prefixed names
4. **V6** — Consolidate status/error entities into `research_status_history` + `exec_status_history`
5. **V7** — Consolidate input/output entities into `research_io_param` + `exec_io_param`
6. **V8** — Consolidate job submission types into `compute_job_submission`
7. **V9** — Merge preference tables into `compute_resource_preference` + `storage_preference`
8. **V10** — Absorb child-collection tables into JSON columns on parents
9. **V11** — Add surrogate keys, drop composite PK tables
10. **V12** — Create EXPERIMENT_SUMMARY view, drop ExperimentSummaryEntity table
11. **V13** — Final cleanup: drop absorbed tables, add indexes

Each migration is idempotent and preserves existing data. JSON columns populated from child table data before child tables are dropped.

### JPA Entity Changes (per migration)

Each Flyway migration is paired with corresponding entity class changes:
- New entity classes with service-prefixed `@Table` names
- `@GeneratedValue(strategy = IDENTITY)` for surrogate keys
- JSON columns via `@Convert` with a `JsonConverter` (Jackson)
- Delete old entity + PK classes after migration
- Update repository interfaces
- Update service layer references

## Impact Summary

| Metric | Before | After | Reduction |
|--------|--------|-------|-----------|
| Tables | ~150 | 50 | 67% |
| JPA Entities | 145 | ~50 | 66% |
| PK Classes | 68 | ~10 | 85% |
| PK Boilerplate | ~5,400 lines | ~500 lines | 91% |
| Status/Error entities | 13 | 2 | 85% |
| Input/Output entities | 8 | 2 | 75% |
| Submission entities | 7 | 1 | 86% |

## Non-Goals

- Changing the sharing service ACL model (proven, generic, well-designed)
- Splitting into separate databases per service (single DB is fine for this scale)
- ORM migration away from JPA/Hibernate
