# Entity-Relationship Description

This document describes the Apache Airavata unified database schema: tables, primary keys, and relationships as implemented by JPA entities and the Flyway baseline (V1).

The schema uses **unified entities** for cross-cutting concerns: a single `STATUS` table (keyed by `PARENT_ID`, `PARENT_TYPE`) stores status history for experiments, processes, tasks, jobs, and applications; similarly `ERROR`, `INPUT_DATA`, and `OUTPUT_DATA` are parent-scoped.

---

## 1. Core unified entities

| Table | Primary key | Description |
|-------|-------------|-------------|
| **GATEWAY** | `AIRAVATA_INTERNAL_GATEWAY_ID` | Gateway/tenant; `GATEWAY_ID` is the logical id. |
| **AIRAVATA_USER** | `AIRAVATA_INTERNAL_USER_ID` | User profile (OIDC scope); `SUB` + `GATEWAY_ID` from IdP. |

**Unified tracking (parent-scoped):**

| Table | Primary key | Description |
|-------|-------------|-------------|
| **STATUS** | `(STATUS_ID, PARENT_ID, PARENT_TYPE)` | Status history for any parent (EXPERIMENT, PROCESS, TASK, JOB, APPLICATION, etc.). |
| **ERROR** | `(ERROR_ID, PARENT_ID, PARENT_TYPE)` | Error records for any parent. |
| **INPUT_DATA** | `(PARENT_ID, PARENT_TYPE, INPUT_NAME)` | Inputs for experiment/process/application/handler. |
| **OUTPUT_DATA** | `(PARENT_ID, PARENT_TYPE, OUTPUT_NAME)` | Outputs for experiment/process/application/handler. |
| **METADATA** | `(PARENT_TYPE, PARENT_ID, METADATA_KEY)` | Key-value metadata for data products, replicas, etc. |

- **Relationships:** No FKs from STATUS/ERROR/INPUT_DATA/OUTPUT_DATA to parents; application logic resolves `PARENT_ID`/`PARENT_TYPE` to the correct entity.

---

## 2. Resource management

| Table | Primary key | Description |
|-------|-------------|-------------|
| **RESOURCE_PROFILE** | `(PROFILE_ID, PROFILE_TYPE)` | Gateway or user resource profile; optional link to CREDENTIALS via `GATEWAY_ID` + `CREDENTIAL_STORE_TOKEN`. |
| **RESOURCE_PREFERENCE** | `(RESOURCE_TYPE, RESOURCE_ID, OWNER_ID, PREFERENCE_LEVEL, PREFERENCE_KEY)` | Hierarchical preferences (GATEWAY / GROUP / USER) for compute, storage, queue, application, gateway, system. |
| **RESOURCE_INTERFACE** | `(RESOURCE_ID, INTERFACE_ID, INTERFACE_TYPE)` | Generic interface registry (job submission, data movement, storage). |
| **JOB_MANAGER_COMMAND** | `(RESOURCE_JOB_MANAGER_ID, COMMAND_CATEGORY, COMMAND_TYPE)` | Job manager commands (e.g. submission, cancellation, monitoring). |

---

## 3. Compute and storage resources

**Compute:**

| Table | Primary key | Relationships |
|-------|-------------|----------------|
| **COMPUTE_RESOURCE** | `RESOURCE_ID` | Root compute resource. |
| **HOST_ALIAS** | — | `RESOURCE_ID` → COMPUTE_RESOURCE (CASCADE). |
| **HOST_IPADDRESS** | — | `RESOURCE_ID` → COMPUTE_RESOURCE (CASCADE). |
| **BATCH_QUEUE** | `(COMPUTE_RESOURCE_ID, QUEUE_NAME)` | FK → COMPUTE_RESOURCE (CASCADE). |
| **COMPUTE_RESOURCE_FILE_SYSTEM** | `(COMPUTE_RESOURCE_ID, FILE_SYSTEM)` | FK → COMPUTE_RESOURCE (CASCADE). |
| **RESOURCE_JOB_MANAGER** | `RESOURCE_JOB_MANAGER_ID` | Referenced by job submission and data movement. |
| **JOB_SUBMISSION_INTERFACE** | `(COMPUTE_RESOURCE_ID, JOB_SUBMISSION_INTERFACE_ID)` | FK → COMPUTE_RESOURCE (CASCADE). |
| **SSH_JOB_SUBMISSION** | `JOB_SUBMISSION_INTERFACE_ID` | FK → RESOURCE_JOB_MANAGER (CASCADE). |
| **DATA_MOVEMENT_INTERFACE** | `DATA_MOVEMENT_INTERFACE_ID` | FK → COMPUTE_RESOURCE (optional, CASCADE). |
| **SCP_DATA_MOVEMENT** | `DATA_MOVEMENT_INTERFACE_ID` | Logical extension of data movement. |
| **GRIDFTP_ENDPOINT** | `(ENDPOINT, DATA_MOVEMENT_INTERFACE_ID)` | Endpoints for GridFTP data movement. |
| **LOCAL_DATA_MOVEMENT**, **UNICORE_DATAMOVEMENT** | (per entity) | Data movement variants. |

**Storage:**

| Table | Primary key | Relationships |
|-------|-------------|----------------|
| **STORAGE_RESOURCE** | `STORAGE_RESOURCE_ID` | Root storage resource. |
| **STORAGE_INTERFACE** | `(STORAGE_RESOURCE_ID, DATA_MOVEMENT_INTERFACE_ID)` | FK → STORAGE_RESOURCE (CASCADE); links to DATA_MOVEMENT_INTERFACE. |

**Compute resource projects (V1):**

| Table | Primary key | Relationships |
|-------|-------------|----------------|
| **COMPUTE_RESOURCE_PROJECT** | `(COMPUTE_RESOURCE_ID, PROJECT_NAME)` | FK → COMPUTE_RESOURCE. |
| **PROJECT_QUEUE_ACCESS** | Composite (compute resource + project + queue) | FK → COMPUTE_RESOURCE_PROJECT. |

---

## 4. Application catalog

| Table | Primary key | Relationships |
|-------|-------------|----------------|
| **APPLICATION_INTERFACE** | `INTERFACE_ID` | Gateway-scoped; optional MODULE_NAME/MODULE_VERSION. |
| **APP_MODULE_MAPPING** | `INTERFACE_ID` (+ MODULE_ID) | FK → APPLICATION_INTERFACE (CASCADE). |
| **APPLICATION_MODULE** | `MODULE_ID` | Referenced by APP_MODULE_MAPPING and APPLICATION_DEPLOYMENT. |
| **APPLICATION_DEPLOYMENT** | `DEPLOYMENT_ID` | `APP_MODULE_ID`, `COMPUTE_HOSTID` (compute resource); optional `APPLICATION_INTERFACE_ID`. |
| **APPLICATION_DEPLOYMENT_COMMAND** | `(DEPLOYMENT_ID, COMMAND_TYPE, COMMAND)` | FK → APPLICATION_DEPLOYMENT (CASCADE). |
| **APP_ENVIRONMENT** | `(DEPLOYMENT_ID, NAME)` | FK → APPLICATION_DEPLOYMENT (CASCADE). |
| **LIBRARY_PATH** | `(DEPLOYMENT_ID, PATH_TYPE, NAME)` | FK → APPLICATION_DEPLOYMENT (CASCADE). |

**Parsing:**

| Table | Primary key | Relationships |
|-------|-------------|----------------|
| **PARSER** | `PARSER_ID` | Gateway-scoped. |
| **PARSER_IO** | `PARSER_IO_ID` | FK → PARSER (CASCADE); DIRECTION = INPUT/OUTPUT. |
| **PARSING_TEMPLATE** | `PARSING_TEMPLATE_ID` | Links to APP_INTERFACE_ID, GATEWAY_ID. |
| **PARSER_CONNECTOR** | `PARSER_CONNECTOR_ID` | FK → PARSER (parent/child), PARSING_TEMPLATE (CASCADE). |
| **PARSER_CONNECTOR_INPUT** | `PARSER_CONNECTOR_INPUT_ID` | FK → PARSER_IO (input/output), PARSER_CONNECTOR (CASCADE). |
| **PARSING_TEMPLATE_INPUT** | `PARSING_TEMPLATE_INPUT_ID` | FK → PARSER_IO, PARSING_TEMPLATE (CASCADE). |

---

## 5. Experiment catalog

| Table | Primary key | Relationships |
|-------|-------------|----------------|
| **PROJECT** | `PROJECT_ID` | `GATEWAY_ID`. |
| **PROJECT_USER** | `(PROJECT_ID, USER_NAME)` | FK → PROJECT (CASCADE). |
| **EXPERIMENT** | `EXPERIMENT_ID` | `PROJECT_ID`, `GATEWAY_ID`; status/errors/inputs/outputs via STATUS, ERROR, INPUT_DATA, OUTPUT_DATA with PARENT_TYPE = EXPERIMENT. |
| **USER_CONFIGURATION_DATA** | `EXPERIMENT_ID` | FK → EXPERIMENT (CASCADE). |
| **PROCESS** | `PROCESS_ID` | FK → EXPERIMENT (CASCADE); optional APPLICATION_INTERFACE_ID, APPLICATION_DEPLOYMENT_ID, COMPUTE_RESOURCE_ID. |
| **PROCESS_RESOURCE_SCHEDULE** | `PROCESS_ID` | FK → PROCESS (CASCADE). |
| **PROCESS_WORKFLOW** | `(PROCESS_ID, WORKFLOW_ID)` | FK → PROCESS (CASCADE); links to AIRAVATA_WORKFLOW. |
| **TASK** | `TASK_ID` | FK → PROCESS (PARENT_PROCESS_ID) (CASCADE). |
| **JOB** | `(JOB_ID, TASK_ID)` | FK → TASK (CASCADE). |

- **Status/error/IO:** EXPERIMENT, PROCESS, TASK, JOB (and APPLICATION) use the unified STATUS, ERROR, INPUT_DATA, OUTPUT_DATA tables with appropriate `PARENT_ID` and `PARENT_TYPE`.

**Gateway workers / usage (V1):**

| Table | Primary key | Relationships |
|-------|-------------|----------------|
| **GATEWAY_WORKER** | Composite | Gateway-specific worker. |
| **GATEWAY_USAGE_REPORTING_COMMAND** | Composite | Gateway usage reporting. |

**Notifications:**

| Table | Primary key | Relationships |
|-------|-------------|----------------|
| **NOTIFICATION** | (per entity) | Experiment/process notifications. |

---

## 6. Replica catalog (data products)

| Table | Primary key | Relationships |
|-------|-------------|----------------|
| **DATA_PRODUCT** | `PRODUCT_URI` | Gateway and owner scoped. |
| **DATA_REPLICA_LOCATION** | `REPLICA_ID` | FK → DATA_PRODUCT (CASCADE); optional STORAGE_RESOURCE_ID. |

- **DATA_PRODUCT_AUTHOR**, **DATA_PRODUCT_TAG** (unified data products).

---

## 7. Workflow catalog

| Table | Primary key | Relationships |
|-------|-------------|----------------|
| **AIRAVATA_WORKFLOW** | `ID` | Optional EXPERIMENT_ID (logical link). |
| **WORKFLOW_APPLICATION** | `(ID, WORKFLOW_ID)` | FK → AIRAVATA_WORKFLOW (CASCADE); optional PROCESS_ID, APPLICATION_INTERFACE_ID, COMPUTE_RESOURCE_ID. |
| **WORKFLOW_HANDLER** | `(ID, WORKFLOW_ID)` | FK → AIRAVATA_WORKFLOW (CASCADE). |
| **WORKFLOW_CONNECTION** | `(ID, WORKFLOW_ID)` | FK → AIRAVATA_WORKFLOW (CASCADE); optional DATA_BLOCK_ID → WORKFLOW_DATA_BLOCK. |
| **WORKFLOW_DATA_BLOCK** | `(ID, WORKFLOW_ID)` | FK → AIRAVATA_WORKFLOW (CASCADE). |

---

## 8. Sharing (Zanzibar-style)

| Table | Primary key | Description |
|-------|-------------|-------------|
| **USER_GROUP** | `(GROUP_ID, DOMAIN_ID)` | Groups within a domain. |
| **GROUP_ADMIN** | `(GROUP_ID, DOMAIN_ID, ADMIN_ID)` | Group admins. |
| **GROUP_MEMBERSHIP** | `(PARENT_ID, CHILD_ID, DOMAIN_ID)` | Parent/child group or user membership. |
| **ENTITY_TYPE** | `(ENTITY_TYPE_ID, DOMAIN_ID)` | Shareable entity types. |
| **PERMISSION_TYPE** | `(PERMISSION_TYPE_ID, DOMAIN_ID)` | Permission types. |
| **ENTITY** | `(ENTITY_ID, DOMAIN_ID)` | Shareable entity; ENTITY_TYPE_ID, OWNER_ID, optional PARENT_ENTITY_ID. |
| **SHARING** | `(PERMISSION_TYPE_ID, ENTITY_ID, GROUP_ID, INHERITED_PARENT_ID, DOMAIN_ID)` | Permission assignments. |

---

## 9. Credential store and resource access

| Table | Primary key | Relationships |
|-------|-------------|----------------|
| **CREDENTIALS** | `(GATEWAY_ID, TOKEN_ID)` | User credentials (e.g. SSH); USER_ID owner. |
| **RESOURCE_ACCESS** | `ID` | (GATEWAY_ID, CREDENTIAL_TOKEN) → CREDENTIALS; RESOURCE_TYPE, RESOURCE_ID; OWNER_ID, OWNER_TYPE. |
| **RESOURCE_ACCESS_GRANT** | `ID` | (GATEWAY_ID, CREDENTIAL_TOKEN) → CREDENTIALS; COMPUTE_RESOURCE_ID and grant details. |
| **CREDENTIAL_CLUSTER_INFO** | `(GATEWAY_ID, CREDENTIAL_TOKEN, COMPUTE_RESOURCE_ID)` | Cached cluster/partition info per credential. |
| **ALLOCATION_POOL** | `ALLOCATION_POOL_ID` | Gateway allocation pools. |
| **ALLOCATION_POOL_GROUP** | `(ALLOCATION_POOL_ID, GROUP_RESOURCE_PROFILE_ID)` | Links allocation pool to group resource profile. |
| **PROJECT_RESOURCE_ACCOUNT** | `(PROJECT_ID, COMPUTE_RESOURCE_ID)` | Project-scoped compute resource account; FK → PROJECT. |

---

## 10. Other tables

| Table | Primary key | Description |
|-------|-------------|-------------|
| **CONFIGURATION** | (per entity) | Key-value configuration. |
| **GATEWAY_GROUPS** | (per entity) | Gateway group mappings. |
| **COMPUTATIONAL_RESOURCE_SCHEDULING** | (expcatalog) | Scheduling details (can reference process/task). |
| **CATALOG_RESOURCE** | `RESOURCE_ID` | Catalog resource entity. |
| **DATA_PRODUCT_AUTHOR**, **DATA_PRODUCT_TAG** | (per entity) | Data product authors and tags. |
| **USER_GROUP_SELECTION** | composite | User’s selected group (e.g. for submission). |

---

## Diagram summary (key hierarchies)

```
GATEWAY
  ├── AIRAVATA_USER (SUB, GATEWAY_ID)
  ├── PROJECT (GATEWAY_ID)
  │     ├── PROJECT_USER
  │     └── EXPERIMENT (PROJECT_ID)
  │           ├── USER_CONFIGURATION_DATA
  │           ├── PROCESS (EXPERIMENT_ID)
  │           │     ├── PROCESS_RESOURCE_SCHEDULE
  │           │     ├── PROCESS_WORKFLOW → AIRAVATA_WORKFLOW
  │           │     └── TASK (PARENT_PROCESS_ID)
  │           │           └── JOB (TASK_ID)
  │           └── [STATUS/ERROR/INPUT_DATA/OUTPUT_DATA PARENT_TYPE=EXPERIMENT]
  ├── CREDENTIALS (GATEWAY_ID, TOKEN_ID)
  │     ├── RESOURCE_ACCESS
  │     ├── RESOURCE_ACCESS_GRANT
  │     └── CREDENTIAL_CLUSTER_INFO
  ├── RESOURCE_PROFILE (GATEWAY_ID)
  └── APPLICATION_INTERFACE (GATEWAY_ID)
        ├── APP_MODULE_MAPPING → APPLICATION_MODULE
        └── APPLICATION_DEPLOYMENT (COMPUTE_HOSTID → COMPUTE_RESOURCE)
              ├── APPLICATION_DEPLOYMENT_COMMAND
              ├── APP_ENVIRONMENT
              └── LIBRARY_PATH

COMPUTE_RESOURCE (RESOURCE_ID)
  ├── BATCH_QUEUE
  ├── COMPUTE_RESOURCE_FILE_SYSTEM
  ├── JOB_SUBMISSION_INTERFACE
  ├── DATA_MOVEMENT_INTERFACE
  └── COMPUTE_RESOURCE_PROJECT → PROJECT_QUEUE_ACCESS

RESOURCE_JOB_MANAGER
  ├── SSH_JOB_SUBMISSION
  ├── LOCAL_SUBMISSION
  └── JOB_MANAGER_COMMAND

STORAGE_RESOURCE
  └── STORAGE_INTERFACE → DATA_MOVEMENT_INTERFACE

PARSER
  └── PARSER_IO
        ├── PARSING_TEMPLATE_INPUT
        └── PARSER_CONNECTOR_INPUT
PARSING_TEMPLATE
  ├── PARSER_CONNECTOR (parent/child PARSER)
  └── PARSING_TEMPLATE_INPUT

AIRAVATA_WORKFLOW
  ├── WORKFLOW_APPLICATION
  ├── WORKFLOW_HANDLER
  ├── WORKFLOW_DATA_BLOCK
  └── WORKFLOW_CONNECTION (→ WORKFLOW_DATA_BLOCK)

DATA_PRODUCT
  └── DATA_REPLICA_LOCATION
```

All status, error, and I/O for experiments, processes, tasks, jobs, and applications are stored in the unified **STATUS**, **ERROR**, **INPUT_DATA**, and **OUTPUT_DATA** tables with the appropriate `PARENT_ID` and `PARENT_TYPE`.
