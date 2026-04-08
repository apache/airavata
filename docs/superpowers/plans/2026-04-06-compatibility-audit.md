# Full Branch Compatibility Audit — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ensure 100% consistency between all documentation, configuration, and code across the `feat/grpc-armeria-migration` branch.

**Architecture:** Automated grep sweep for stale references in non-Java files, then regenerate persistence.xml from `@Entity` scan, then targeted manual fixes to CLAUDE.md and README.md.

**Tech Stack:** Java 17, Maven, JPA/Hibernate persistence.xml, Markdown, YAML

---

## File Structure

| File | Action |
|------|--------|
| `airavata-api/src/test/resources/META-INF/persistence.xml` | Regenerate entity list from code |
| `/Users/yasith/code/artisan/CLAUDE.md` | Update airavata module table |
| `README.md` | Targeted fixes after line-by-line review |

---

### Task 1: Regenerate persistence.xml from @Entity scan

The test persistence.xml has 69 stale entity references, 19 missing entities, 2 duplicates, and 2 wrong package paths. Replace the entire entity list with what actually exists in the codebase.

**Files:**
- Modify: `airavata-api/src/test/resources/META-INF/persistence.xml`

- [ ] **Step 1: Generate the correct entity list**

Run this command to get all actual `@Entity` FQCNs (excluding config classes and abstract repos):

```bash
grep -r "@Entity" --include="*.java" airavata-api/ -l | grep -v "Config.java" | grep -v "AbstractRepository" | sed 's|.*src/main/java/||' | sed 's|\.java$||' | sed 's|/|.|g' | sort
```

This produces 76 entity classes across 8 modules.

- [ ] **Step 2: Rewrite persistence.xml with correct entity list**

Replace the entire content of `airavata-api/src/test/resources/META-INF/persistence.xml` with the following structure. The entity list must contain exactly the 76 classes from Step 1, grouped by module:

```xml
<?xml version="1.0"?>
<!--
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements. See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership. The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License. You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied. See the License for the
 specific language governing permissions and limitations
 under the License.
-->
<persistence xmlns="https://jakarta.ee/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="https://jakarta.ee/xml/ns/persistence
               https://jakarta.ee/xml/ns/persistence/persistence_3_0.xsd"
             version="3.0">

  <!-- Single persistence unit for tests, consolidating all modules -->
  <persistence-unit name="airavata">
    <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>

    <!-- agent-service -->
    <class>org.apache.airavata.agent.model.AgentExecutionEntity</class>
    <class>org.apache.airavata.agent.model.AgentExecutionStatusEntity</class>
    <class>org.apache.airavata.agent.model.PlanEntity</class>

    <!-- compute-service -->
    <class>org.apache.airavata.compute.model.AgentDeploymentInfoEntity</class>
    <class>org.apache.airavata.compute.model.AWSGroupComputeResourcePrefEntity</class>
    <class>org.apache.airavata.compute.model.BatchQueueEntity</class>
    <class>org.apache.airavata.compute.model.BatchQueueResourcePolicyEntity</class>
    <class>org.apache.airavata.compute.model.ComputationalResourceSchedulingEntity</class>
    <class>org.apache.airavata.compute.model.ComputeJobSubmissionEntity</class>
    <class>org.apache.airavata.compute.model.ComputeResourceEntity</class>
    <class>org.apache.airavata.compute.model.ComputeResourcePolicyEntity</class>
    <class>org.apache.airavata.compute.model.ComputeResourcePreferenceEntity</class>
    <class>org.apache.airavata.compute.model.ComputeResourceReservationEntity</class>
    <class>org.apache.airavata.compute.model.GatewayProfileEntity</class>
    <class>org.apache.airavata.compute.model.GatewayUsageReportingCommandEntity</class>
    <class>org.apache.airavata.compute.model.GroupComputeResourcePrefEntity</class>
    <class>org.apache.airavata.compute.model.GroupResourceProfileEntity</class>
    <class>org.apache.airavata.compute.model.JobManagerCommandEntity</class>
    <class>org.apache.airavata.compute.model.JobSubmissionInterfaceEntity</class>
    <class>org.apache.airavata.compute.model.ParallelismCommandEntity</class>
    <class>org.apache.airavata.compute.model.ProcessResourceScheduleEntity</class>
    <class>org.apache.airavata.compute.model.QueueStatusEntity</class>
    <class>org.apache.airavata.compute.model.ResourceJobManagerEntity</class>
    <class>org.apache.airavata.compute.model.SlurmGroupComputeResourcePrefEntity</class>
    <class>org.apache.airavata.compute.model.UserComputeResourcePreferenceEntity</class>
    <class>org.apache.airavata.compute.model.UserResourceProfileEntity</class>
    <class>org.apache.airavata.compute.model.UserStoragePreferenceEntity</class>

    <!-- credential-service -->
    <class>org.apache.airavata.credential.model.CredentialEntity</class>

    <!-- iam-service -->
    <class>org.apache.airavata.iam.model.CommunityUserEntity</class>
    <class>org.apache.airavata.iam.model.GatewayEntity</class>
    <class>org.apache.airavata.iam.model.GatewayGroupsEntity</class>
    <class>org.apache.airavata.iam.model.UserProfileEntity</class>

    <!-- orchestration-service -->
    <class>org.apache.airavata.orchestration.model.ExecErrorEntity</class>
    <class>org.apache.airavata.orchestration.model.ExecIoParamEntity</class>
    <class>org.apache.airavata.orchestration.model.ExecStatusEntity</class>
    <class>org.apache.airavata.orchestration.model.JobEntity</class>
    <class>org.apache.airavata.orchestration.model.ProcessEntity</class>
    <class>org.apache.airavata.orchestration.model.ProcessWorkflowEntity</class>
    <class>org.apache.airavata.orchestration.model.TaskEntity</class>
    <class>org.apache.airavata.orchestration.model.UserConfigurationDataEntity</class>

    <!-- research-service -->
    <class>org.apache.airavata.research.model.AppIoParamEntity</class>
    <class>org.apache.airavata.research.model.ApplicationDeploymentEntity</class>
    <class>org.apache.airavata.research.model.ApplicationInterfaceEntity</class>
    <class>org.apache.airavata.research.model.ApplicationModuleEntity</class>
    <class>org.apache.airavata.research.model.AppModuleMappingEntity</class>
    <class>org.apache.airavata.research.model.DataProductEntity</class>
    <class>org.apache.airavata.research.model.DataReplicaLocationEntity</class>
    <class>org.apache.airavata.research.model.DatasetResourceEntity</class>
    <class>org.apache.airavata.research.model.ExperimentEntity</class>
    <class>org.apache.airavata.research.model.ExperimentErrorEntity</class>
    <class>org.apache.airavata.research.model.ExperimentStatusEntity</class>
    <class>org.apache.airavata.research.model.ExperimentSummaryEntity</class>
    <class>org.apache.airavata.research.model.NotificationEntity</class>
    <class>org.apache.airavata.research.model.ParserEntity</class>
    <class>org.apache.airavata.research.model.ParsingTemplateEntity</class>
    <class>org.apache.airavata.research.model.ProjectEntity</class>
    <class>org.apache.airavata.research.model.RepositoryResourceEntity</class>
    <class>org.apache.airavata.research.model.ResearchIoParamEntity</class>
    <class>org.apache.airavata.research.model.ResearchProjectEntity</class>
    <class>org.apache.airavata.research.model.SessionEntity</class>

    <!-- sharing-service -->
    <class>org.apache.airavata.sharing.model.DomainEntity</class>
    <class>org.apache.airavata.sharing.model.EntityEntity</class>
    <class>org.apache.airavata.sharing.model.EntityTypeEntity</class>
    <class>org.apache.airavata.sharing.model.GroupAdminEntity</class>
    <class>org.apache.airavata.sharing.model.GroupMembershipEntity</class>
    <class>org.apache.airavata.sharing.model.PermissionTypeEntity</class>
    <class>org.apache.airavata.sharing.model.ResourceEntity</class>
    <class>org.apache.airavata.sharing.model.ResourceStarEntity</class>
    <class>org.apache.airavata.sharing.model.SharingEntity</class>
    <class>org.apache.airavata.sharing.model.TagEntity</class>
    <class>org.apache.airavata.sharing.model.UserEntity</class>
    <class>org.apache.airavata.sharing.model.UserGroupEntity</class>

    <!-- storage-service -->
    <class>org.apache.airavata.storage.model.DataMovementInterfaceEntity</class>
    <class>org.apache.airavata.storage.model.StorageDataMovementEntity</class>
    <class>org.apache.airavata.storage.model.StoragePreferenceEntity</class>
    <class>org.apache.airavata.storage.model.StorageResourceEntity</class>

    <exclude-unlisted-classes>true</exclude-unlisted-classes>

    <properties>
      <property name="hibernate.enable_lazy_load_no_trans" value="true"/>
    </properties>
  </persistence-unit>

</persistence>
```

- [ ] **Step 3: Build and test**

```bash
cd airavata-api && mvn compile -T4 && mvn test -T4
```

Expected: BUILD SUCCESS, all tests pass.

- [ ] **Step 4: Commit**

```bash
git add airavata-api/src/test/resources/META-INF/persistence.xml
git commit -m "fix: regenerate persistence.xml — remove 69 stale entities, add 19 missing"
```

---

### Task 2: Update CLAUDE.md module table

The workspace-level CLAUDE.md at `/Users/yasith/code/artisan/CLAUDE.md` has a module table that doesn't reflect the entity placement changes. The `sharing-service`, `orchestration-service`, and `iam-service` modules are not listed at all.

**Files:**
- Modify: `/Users/yasith/code/artisan/CLAUDE.md`

- [ ] **Step 1: Replace the Airavata Module Structure table**

Find this section in the file:

```markdown
### Airavata Module Structure

| Module | Purpose |
|--------|---------|
| `airavata-api` | Service layer, data access, background services, proto definitions |
| `airavata-api/compute-service` | HPC resource catalog, resource profiles |
| `airavata-api/storage-service` | Data products, file service, storage resources |
| `airavata-api/credential-service` | SSH keys, passwords, credential store |
| `airavata-api/research-service` | Research catalog (embedded in unified server) |
| `airavata-api/agent-service` | Agent communication, bidirectional streaming (embedded in unified server) |
| `airavata-server` | Unified launcher — Armeria serving gRPC + REST (HTTP/JSON transcoding) on port 9090 |
```

Replace with:

```markdown
### Airavata Module Structure

| Module | Purpose |
|--------|---------|
| `airavata-api` | Service layer, data access, background services, proto definitions |
| `airavata-api/compute-service` | HPC resource catalog, resource profiles, resource scheduling, agent deployment config |
| `airavata-api/storage-service` | Storage resources, data movement interfaces and protocols |
| `airavata-api/credential-service` | SSH keys, passwords, credential store |
| `airavata-api/research-service` | Research catalog, application catalog, output parsing, data products, replica catalog |
| `airavata-api/agent-service` | Agent sidecar lifecycle, bidirectional streaming, agent execution state |
| `airavata-api/iam-service` | Identity, access management, gateways, user profiles |
| `airavata-api/sharing-service` | Permissions, groups, tags, resource discovery |
| `airavata-api/orchestration-service` | Process/task/job execution, workflow orchestration |
| `airavata-server` | Unified launcher — Armeria serving gRPC + REST (HTTP/JSON transcoding) on port 9090 |
```

- [ ] **Step 2: Commit**

```bash
git add /Users/yasith/code/artisan/CLAUDE.md
git commit -m "docs: update CLAUDE.md module table to reflect entity placement changes"
```

---

### Task 3: Line-by-line README.md review and fixes

Review the root `README.md` against actual codebase state and fix any inaccuracies.

**Files:**
- Modify: `README.md`

- [ ] **Step 1: Verify module table at line 177-184**

The README lists "Additional service modules" but is missing `iam-service`, `sharing-service`, and `orchestration-service`. The descriptions also need updating.

Current (lines 177-184):
```markdown
Additional service modules in `airavata-api/`:

| Module | Purpose |
|--------|---------|
| `compute-service` | HPC resource catalog, resource profiles |
| `storage-service` | Data products, file service, storage resources |
| `credential-service` | SSH keys, passwords, credential store |
```

Replace with:
```markdown
Additional service modules in `airavata-api/`:

| Module | Purpose |
|--------|---------|
| `compute-service` | HPC resource catalog, resource profiles, resource scheduling |
| `storage-service` | Storage resources, data movement interfaces and protocols |
| `credential-service` | SSH keys, passwords, credential store |
| `iam-service` | Identity, access management, gateways, user profiles |
| `sharing-service` | Permissions, groups, tags, resource discovery |
| `orchestration-service` | Process/task/job execution, workflow orchestration |
```

- [ ] **Step 2: Verify embedded services table at line 172-176**

Current:
```markdown
| **Agent Service** | `airavata-api/agent-service` | Backend for interactive jobs via bidirectional gRPC |
| **Research Service** | `airavata-api/research-service` | Research catalog API (notebooks, datasets, models) |
```

Update research-service description to include app catalog:
```markdown
| **Agent Service** | `airavata-api/agent-service` | Agent sidecar lifecycle, bidirectional gRPC for interactive jobs |
| **Research Service** | `airavata-api/research-service` | Research catalog, application catalog, data products, output parsing |
```

- [ ] **Step 3: Verify all other sections**

Check these sections are still accurate (they should be — just confirm):
- Mermaid architecture diagram (line 60-86) — should show Armeria :9090, DB, RMQ, ZK, KFK, KC
- Service table (line 102-118) — verify service names match actual gRPC service implementations
- Background services table (line 123-137) — verify worker names
- Startup sequence diagram (line 153-165) — verify 7-step init
- Build commands (line 203-220) — verify `tilt up`, `mvn clean package`, jar name
- Ports table (line 254-257) — verify port 9090
- Health endpoints table (line 233-237)

For each: read the corresponding code/config to verify. Only fix what's actually wrong.

- [ ] **Step 4: Build to ensure no broken markdown**

Visually verify the markdown renders correctly (no broken tables, diagrams, etc.).

- [ ] **Step 5: Commit**

```bash
git add README.md
git commit -m "docs: update README.md module tables to reflect entity placement and service changes"
```

---

### Task 4: Automated stale reference sweep

Search ALL non-Java files for stale references from the refactoring.

**Files to scan:** Everything outside `*.java` and `target/` directories.

- [ ] **Step 1: Search for deleted entity names**

Run these greps across the entire repo (exclude target/ and .git/):

```bash
# Deleted submission entities
grep -r --include="*.{xml,yml,yaml,properties,md,sh,py,txt}" -l "SshJobSubmissionEntity\|GlobusSubmissionEntity\|GsisshSubmissionEntity\|CloudJobSubmissionEntity\|LocalSubmissionEntity\|UnicoreSubmissionEntity" . --exclude-dir={target,.git}

# Deleted workflow entities
grep -r --include="*.{xml,yml,yaml,properties,md,sh,py,txt}" -l "AiravataWorkflowEntity\|WorkflowApplicationEntity\|WorkflowConnectionEntity\|WorkflowDataBlockEntity\|WorkflowHandlerEntity\|WorkflowStatusEntity\|HandlerErrorEntity\|HandlerInputEntity\|HandlerOutputEntity\|HandlerStatusEntity\|ApplicationErrorEntity\|ApplicationStatusEntity\|ComponentStatusEntity" . --exclude-dir={target,.git}

# Deleted research entities
grep -r --include="*.{xml,yml,yaml,properties,md,sh,py,txt}" -l "EdgeEntity\|NodeEntity\|PortEntity\|WorkflowEntity\|WorkflowInputEntity\|WorkflowOutputEntity\|ProjectUserEntity\|ExperimentInputEntity\|ExperimentOutputEntity" . --exclude-dir={target,.git}

# Deleted compute entities
grep -r --include="*.{xml,yml,yaml,properties,md,sh,py,txt}" -l "GlobusGkEndpointEntity\|GridftpEndpointEntity\|ComputeResourceFileSystemEntity\|GsisshExportEntity\|GsisshPostjobcommandEntity\|GsisshPrejobcommandEntity\|LibraryApendPathEntity\|LibraryPrependPathEntity\|AppEnvironmentEntity\|PrejobCommandEntity\|PostjobCommandEntity\|ModuleLoadCmdEntity\|SSHAccountProvisionerConfiguration\|GroupSSHAccountProvisionerConfig" . --exclude-dir={target,.git}

# Deleted storage entities
grep -r --include="*.{xml,yml,yaml,properties,md,sh,py,txt}" -l "GridftpDataMovementEntity\|ScpDataMovementEntity\|LocalDataMovementEntity\|UnicoreDatamovementEntity\|GridftpEndpointEntity\|DataProductMetadataEntity\|DataReplicaMetadataEntity\|ConfigurationEntity" . --exclude-dir={target,.git}

# Deleted iam entities
grep -r --include="*.{xml,yml,yaml,properties,md,sh,py,txt}" -l "NSFDemographicsEntity\|CustomizedDashboardEntity\|TenantGatewayEntity" . --exclude-dir={target,.git}

# Deleted orchestration entities
grep -r --include="*.{xml,yml,yaml,properties,md,sh,py,txt}" -l "GatewayWorkerEntity\|ProcessStatusEntity\|ProcessErrorEntity\|TaskStatusEntity\|TaskErrorEntity\|JobStatusEntity\|ProcessInputEntity\|ProcessOutputEntity" . --exclude-dir={target,.git}

# Removed classes
grep -r --include="*.{xml,yml,yaml,properties,md,sh,py,txt}" -l "TenantProfileRepository\|RegistryProviderImpl\|SharingProviderImpl\|IamAdminGrpcHandler" . --exclude-dir={target,.git}
```

- [ ] **Step 2: Search for old package paths of moved entities**

```bash
# App catalog moved from compute to research
grep -r --include="*.{xml,yml,yaml,properties,md,sh,py,txt}" -l "org.apache.airavata.compute.model.Application\|org.apache.airavata.compute.model.AppIoParam\|org.apache.airavata.compute.model.AppModule\|org.apache.airavata.compute.model.Parser\|org.apache.airavata.compute.repository.Application\|org.apache.airavata.compute.service.AppCatalog" . --exclude-dir={target,.git}

# CommunityUser moved from credential to iam
grep -r --include="*.{xml,yml,yaml,properties,md,sh,py,txt}" -l "org.apache.airavata.credential.model.CommunityUser\|org.apache.airavata.credential.repository.CommunityUser" . --exclude-dir={target,.git}

# GatewayUsageReporting moved from iam to compute
grep -r --include="*.{xml,yml,yaml,properties,md,sh,py,txt}" -l "org.apache.airavata.iam.model.GatewayUsageReporting\|org.apache.airavata.iam.repository.GatewayUsageReporting" . --exclude-dir={target,.git}

# DataProduct/DataReplica moved from storage to research
grep -r --include="*.{xml,yml,yaml,properties,md,sh,py,txt}" -l "org.apache.airavata.storage.model.DataProduct\|org.apache.airavata.storage.model.DataReplica" . --exclude-dir={target,.git}

# UserStoragePreference moved from storage to compute
grep -r --include="*.{xml,yml,yaml,properties,md,sh,py,txt}" -l "org.apache.airavata.storage.model.UserStoragePreference\|org.apache.airavata.storage.repository.UserStoragePreference" . --exclude-dir={target,.git}

# Resource scheduling moved from orchestration to compute
grep -r --include="*.{xml,yml,yaml,properties,md,sh,py,txt}" -l "org.apache.airavata.orchestration.model.ProcessResourceSchedule\|org.apache.airavata.orchestration.model.ComputationalResourceScheduling" . --exclude-dir={target,.git}

# Tag/Resource/ResourceStar moved from research to sharing
grep -r --include="*.{xml,yml,yaml,properties,md,sh,py,txt}" -l "org.apache.airavata.research.model.TagEntity\|org.apache.airavata.research.model.ResourceEntity\|org.apache.airavata.research.model.ResourceStarEntity\|org.apache.airavata.research.repository.TagRepository\|org.apache.airavata.research.repository.ResourceRepository\|org.apache.airavata.research.repository.ResourceStarRepository" . --exclude-dir={target,.git}
```

- [ ] **Step 3: Fix any hits found**

For each file with stale references:
- If it's persistence.xml → already fixed in Task 1
- If it's a design spec/plan doc → these are historical documents, leave them as-is (they document what WAS true at time of writing)
- If it's any other config/doc → update the reference to the current correct value
- If it's a script or Dockerfile → update the reference

- [ ] **Step 4: Build and test**

```bash
cd airavata-api && mvn compile -T4 && mvn test -T4
```

- [ ] **Step 5: Commit (if any changes)**

```bash
git add -A
git commit -m "fix: remove stale entity/package references from configs and docs"
```

---

## Final Verification

After all 4 tasks:

- [ ] **Full build**: `cd airavata-api && mvn clean compile -T4`
- [ ] **Full test suite**: `cd airavata-api && mvn test -T4`
- [ ] **Verify persistence.xml entity count matches @Entity scan**: `grep -c "<class>" airavata-api/src/test/resources/META-INF/persistence.xml` should equal the number of `@Entity` classes found
