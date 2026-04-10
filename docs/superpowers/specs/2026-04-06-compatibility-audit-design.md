# Full Branch Compatibility Audit

## Problem

The `feat/grpc-armeria-migration` branch includes major changes (Thrift→gRPC, entity consolidation, entity placement refactor) that may have left stale references in non-Java files: persistence.xml, CLAUDE.md, README.md, configs, scripts, and Docker files.

## Scope

Full audit of everything on this branch vs master. Python SDK and proto files already verified as compatible — focus is on:

1. **persistence.xml** — 69 stale entities, 19 missing, 2 duplicates, 2 wrong packages
2. **CLAUDE.md** — stale module descriptions and architecture info
3. **README.md** — line-by-line review against actual codebase state
4. **Config/script/Docker sweep** — grep all non-Java files for stale references

## Approach

Automated sweep (grep/glob for stale patterns) + targeted manual review of narrative docs.

## Tasks

### 1. Regenerate persistence.xml

Scan all modules for `@Entity`-annotated classes. Rebuild the persistence unit with correct package paths. This eliminates:
- 69 stale entity references (deleted/renamed/moved classes)
- 2 duplicate declarations (GatewayEntity, UserEntity)
- 2 wrong package paths (DataProductEntity, DataReplicaLocationEntity)
- Adds 19 missing entities from new/renamed classes

Source of truth: `@Entity` annotation in Java code.

### 2. Update CLAUDE.md (workspace-level)

Update airavata-specific sections:
- Module responsibility table — reflect new ownership (app catalog in research-service, etc.)
- Build commands — verify accuracy
- Architecture description — unified Armeria server, gRPC + REST on port 9090

### 3. Line-by-line README.md review

Review root README.md against actual codebase state:
- Module list/table — names, purposes, service counts
- Mermaid architecture diagrams — service names, ports, connections
- Startup sequence diagram — 7-step init accuracy
- Build/run commands — verify they work
- Port references (9090, 9097)
- Endpoint references (health, DocService)
- Experiment lifecycle diagram — state names vs proto enums

Fix only what's actually wrong.

### 4. Automated stale reference sweep

Search ALL non-Java files for:

**Deleted entities:** SshJobSubmissionEntity, GlobusSubmissionEntity, GsisshSubmissionEntity, CloudJobSubmissionEntity, LocalSubmissionEntity, UnicoreSubmissionEntity, AiravataWorkflowEntity, WorkflowEntity, NodeEntity, PortEntity, EdgeEntity, ProjectUserEntity, ExperimentInputEntity, ExperimentOutputEntity, ProcessStatusEntity, ProcessErrorEntity, TaskStatusEntity, TaskErrorEntity, JobStatusEntity, GatewayWorkerEntity, and all associated PK classes.

**Old package paths for moved entities:** org.apache.airavata.compute.model.Application*, org.apache.airavata.compute.model.Parser*, org.apache.airavata.compute.model.AppIo*, org.apache.airavata.credential.model.CommunityUser*, org.apache.airavata.iam.model.GatewayUsageReporting*, org.apache.airavata.storage.model.UserStoragePreference*, org.apache.airavata.orchestration.model.ProcessResourceSchedule*, org.apache.airavata.orchestration.model.ComputationalResourceScheduling*, org.apache.airavata.storage.model.DataProduct*, org.apache.airavata.storage.model.DataReplicaLocation*

**Removed modules/classes:** TenantProfileRepository, RegistryProviderImpl, SharingProviderImpl, IamAdminGrpcHandler, grpc/, rest/, thrift/ module references

**Renamed entities:** ScpDataMovementEntity→StorageDataMovementEntity, ExperimentInputEntity→ResearchIoParamEntity, TaskStatusEntity→ExecStatusEntity, TaskErrorEntity→ExecErrorEntity, ProcessInputEntity→ExecIoParamEntity

**Files to scan:** Tiltfile, Dockerfile, compose.yml, all pom.xml, application.yml/properties, .sh scripts, .py files, Ansible configs, all .md files, all .xml files.

## Decisions

| Decision | Choice | Reason |
|---|---|---|
| persistence.xml approach | Regenerate from @Entity scan | Eliminates all categories of issues at once |
| CLAUDE.md scope | Update workspace-level only | Keep everything in one place per user preference |
| README.md approach | Line-by-line review, targeted fixes | README is mostly correct, only fix actual errors |
| SDK/proto audit | Skip | Already verified as 100% compatible |
