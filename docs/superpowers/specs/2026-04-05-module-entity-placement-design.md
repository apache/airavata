# Module Entity Placement Refactor

## Problem

Models and repositories across the 8 airavata-api modules have accumulated in wrong modules over time. ~51 entities/repositories violate module boundaries, creating unclear ownership and tight coupling.

## Revised Module Responsibilities

| Module | Responsibility |
|---|---|
| **agent-service** | Sidecar agent lifecycle, bidirectional streaming, agent execution state. Airavata submits an agent sidecar for all jobs; agent-service maintains that connection. |
| **compute-service** | HPC resource catalog, resource profiles, resource scheduling, agent deployment config, usage reporting |
| **credential-service** | SSH keys, passwords, credential store, user auth config (userDN, generateCert) |
| **storage-service** | Storage resources, storage preferences, data movement interfaces, data movement protocol config |
| **research-service** | Research catalog (projects, experiments), application catalog (interfaces, deployments, modules), output parsing, data products, replica catalog, datasets, repository resources, notifications |
| **iam-service** | Identity, access management, gateways, user profiles, community user profiles |
| **sharing-service** | Permissions, groups, shareable entities, tags, resource discovery, resource stars |
| **orchestration-service** | Process/task/job execution, workflow orchestration, user execution preferences |

## Migration Strategy

Module-by-module migration. Each step is a self-contained PR. Entities move with their repositories, PK classes, and enums.

### Cross-Module Dependency Handling

1. Move entity + repository to destination module
2. Update service interfaces in `airavata-api/src/.../interfaces/` (shared parent) if needed
3. Update imports in any service class referencing moved entities
4. No direct cross-module repo imports — always go through shared interface layer

## Migration Steps

### Step 1-2: compute-service -> research-service (app catalog + parsers)

**Entities:**
- ApplicationInterfaceEntity
- ApplicationDeploymentEntity
- ApplicationModuleEntity
- AppModuleMappingEntity
- AppIoParamEntity
- ParserEntity
- ParsingTemplateEntity
- ParserInputEntity
- ParserOutputEntity

**Repositories:**
- ApplicationInterfaceRepository
- ApplicationDeploymentRepository
- ApplicationModuleRepository
- ApplicationInputRepository
- ApplicationOutputRepository
- ParserRepository
- ParsingTemplateRepository
- ParserInputRepository
- ParserOutputRepository

**Plus:** All associated composite PK classes.

### Step 3: compute-service -> orchestration-service

**Entities:**
- ComputeJobSubmissionEntity

**Repositories:**
- ComputeJobSubmissionRepository

### Step 4: agent-service -> compute-service

**Entities:**
- AgentDeploymentInfoEntity

**Repositories:**
- AgentDeploymentInfoRepository

### Step 5: credential-service -> iam-service

**Entities:**
- CommunityUserEntity

**Repositories:**
- CommunityUserRepository

**Plus:** CommunityUserPK

### Step 6: iam-service -> compute-service

**Entities:**
- GatewayUsageReportingCommandEntity

**Repositories:**
- GatewayUsageReportingCommandRepository

### Step 7: research-service -> sharing-service

**Entities:**
- TagEntity
- ResourceEntity
- ResourceStarEntity

**Repositories:**
- TagRepository
- ResourceRepository
- ResourceStarRepository

### Step 8: storage-service -> compute-service

**Entities:**
- UserStoragePreferenceEntity

**Repositories:**
- UserStoragePreferenceRepository

**Plus:** UserStoragePreferencePK

### Step 9: orchestration-service -> compute-service

**Entities:**
- ProcessResourceScheduleEntity
- ComputationalResourceSchedulingEntity

**Repositories:**
- ProcessResourceScheduleRepository
- ComputationalResourceSchedulingRepository

### Step 10: orchestration-service — split UserConfigurationDataEntity

- Extract auth fields (userDN, generateCert) into a new entity in **credential-service**
- Keep execution preferences (autoSchedule, etc.) in **orchestration-service**

### Step 11: sharing-service — remove UserEntity

- Remove UserEntity + UserRepository
- Replace with userId references throughout sharing-service
- Delegate user lookups to iam-service via shared interface

### Step 12: iam-service — consolidate TenantProfileRepository

- Merge TenantProfileRepository queries into GatewayRepository
- Delete TenantProfileRepository

## Decisions Made

| Decision | Choice | Reason |
|---|---|---|
| Application catalog destination | research-service | Keeps app->experiment->output chain together |
| Parser entities destination | research-service | Parsing tied to experiment outputs and app interfaces |
| Agent execution entities | Stay in agent-service | Agent-service is the sidecar management layer, owns execution state |
| AgentDeploymentInfoEntity | Move to compute-service | Compute resource config, not agent lifecycle |
| CommunityUserEntity | Move to iam-service | User identity, not credential storage |
| Sharing-service UserEntity | Remove entirely | Reference by ID only, query iam-service for details |
| TenantProfileRepository | Consolidate into GatewayRepository | Redundant — both operate on GatewayEntity |
| UserConfigurationDataEntity | Split | Auth fields to credential-service, execution prefs stay in orchestration |
| DataMovementInterfaceEntity vs StorageDataMovementEntity | Keep separate | Different concepts: resource-protocol mapping vs protocol implementation config |
| Storage-service scope | Infrastructure only | Data products, datasets, replica catalog go to research-service |
| Migration approach | Module-by-module | Reviewable PRs, incremental validation |
