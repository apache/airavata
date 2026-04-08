# Module Entity Placement Refactor — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Move ~51 misplaced JPA entities and repositories to their correct module, following the design spec at `docs/superpowers/specs/2026-04-05-module-entity-placement-design.md`.

**Architecture:** Each module owns entities matching its bounded context. Entities move with their repositories, PK classes, and mapper methods. Shared interfaces in `airavata-api/src/.../interfaces/` use proto types (not entity types), so they remain unchanged. Cross-module references go through the shared interface layer — no direct repo imports across modules.

**Tech Stack:** Java 17, Spring Data JPA, MapStruct, Protobuf/gRPC, Maven multi-module

---

## File Structure Overview

Each migration step moves files from one module to another following the established package convention:
- Entities: `org.apache.airavata.<module>.model.*`
- Repositories: `org.apache.airavata.<module>.repository.*`
- Mappers: `org.apache.airavata.<module>.mapper.*`
- Services: `org.apache.airavata.<module>.service.*`

The shared interface layer at `airavata-api/src/main/java/org/apache/airavata/interfaces/` uses proto types and does NOT need changes when entities move.

---

### Task 1: Move app catalog entities + repos from compute-service to research-service

This is the largest migration — the application catalog (interfaces, deployments, modules, I/O params) plus the service facade and mapper methods.

**Files to move (compute-service → research-service):**

Entities (change package `org.apache.airavata.compute.model` → `org.apache.airavata.research.model`):
- `airavata-api/compute-service/src/main/java/org/apache/airavata/compute/model/ApplicationInterfaceEntity.java`
- `airavata-api/compute-service/src/main/java/org/apache/airavata/compute/model/ApplicationDeploymentEntity.java`
- `airavata-api/compute-service/src/main/java/org/apache/airavata/compute/model/ApplicationModuleEntity.java`
- `airavata-api/compute-service/src/main/java/org/apache/airavata/compute/model/AppModuleMappingEntity.java`
- `airavata-api/compute-service/src/main/java/org/apache/airavata/compute/model/AppModuleMappingPK.java`
- `airavata-api/compute-service/src/main/java/org/apache/airavata/compute/model/AppIoParamEntity.java`

Repositories (change package `org.apache.airavata.compute.repository` → `org.apache.airavata.research.repository`):
- `airavata-api/compute-service/src/main/java/org/apache/airavata/compute/repository/ApplicationInterfaceRepository.java`
- `airavata-api/compute-service/src/main/java/org/apache/airavata/compute/repository/ApplicationDeploymentRepository.java`
- `airavata-api/compute-service/src/main/java/org/apache/airavata/compute/repository/ApplicationModuleRepository.java`
- `airavata-api/compute-service/src/main/java/org/apache/airavata/compute/repository/ApplicationInputRepository.java`
- `airavata-api/compute-service/src/main/java/org/apache/airavata/compute/repository/ApplicationOutputRepository.java`

Service facade (change package `org.apache.airavata.compute.service` → `org.apache.airavata.research.service`):
- `airavata-api/compute-service/src/main/java/org/apache/airavata/compute/service/AppCatalogDataAccessImpl.java`

**Files to modify:**
- `airavata-api/compute-service/src/main/java/org/apache/airavata/compute/mapper/ComputeMapper.java` — remove app catalog mapping methods
- `airavata-api/research-service/src/main/java/org/apache/airavata/research/mapper/ResearchMapper.java` — add app catalog mapping methods

- [ ] **Step 1: Move the 6 entity files + PK class to research-service**

Move each file from `compute-service/src/main/java/org/apache/airavata/compute/model/` to `research-service/src/main/java/org/apache/airavata/research/model/`. In each file, change the package declaration from `org.apache.airavata.compute.model` to `org.apache.airavata.research.model`.

Files to move:
```
ApplicationInterfaceEntity.java
ApplicationDeploymentEntity.java
ApplicationModuleEntity.java
AppModuleMappingEntity.java
AppModuleMappingPK.java
AppIoParamEntity.java
```

For each file, update the package line:
```java
// OLD:
package org.apache.airavata.compute.model;
// NEW:
package org.apache.airavata.research.model;
```

- [ ] **Step 2: Move the 5 repository files to research-service**

Move each file from `compute-service/src/main/java/org/apache/airavata/compute/repository/` to `research-service/src/main/java/org/apache/airavata/research/repository/`. Update package declarations and imports.

Files to move:
```
ApplicationInterfaceRepository.java
ApplicationDeploymentRepository.java
ApplicationModuleRepository.java
ApplicationInputRepository.java
ApplicationOutputRepository.java
```

For each file:
1. Change package from `org.apache.airavata.compute.repository` to `org.apache.airavata.research.repository`
2. Change entity imports from `org.apache.airavata.compute.model.*` to `org.apache.airavata.research.model.*`
3. Change mapper import from `org.apache.airavata.compute.mapper.ComputeMapper` to `org.apache.airavata.research.mapper.ResearchMapper`
4. Replace all `ComputeMapper.INSTANCE` references with `ResearchMapper.INSTANCE`

- [ ] **Step 3: Move AppCatalogDataAccessImpl to research-service**

Move `compute-service/.../compute/service/AppCatalogDataAccessImpl.java` to `research-service/.../research/service/AppCatalogDataAccessImpl.java`.

1. Change package from `org.apache.airavata.compute.service` to `org.apache.airavata.research.service`
2. Change repository imports from `org.apache.airavata.compute.repository.*` to `org.apache.airavata.research.repository.*`
3. Remove the `ComputeResourceRepository` field and `getAvailableComputeResourceIdList()` method (that stays in compute-service)
4. Update the `AppCatalogDataAccess` interface in `airavata-api/src/.../interfaces/AppCatalogDataAccess.java` if it has the `getAvailableComputeResourceIdList()` method — move that method to the appropriate compute-service interface instead

- [ ] **Step 4: Move app catalog mapper methods from ComputeMapper to ResearchMapper**

In `ComputeMapper.java`, identify and remove the following mapping methods (and their entity imports):
- `appInterfaceToModel` / `appInterfaceToEntity`
- `appDeploymentToModel` / `appDeploymentToEntity`
- `appModuleToModel` / `appModuleToEntity`
- `appInputToModel` / `appInputToEntity`
- `appOutputToModel` / `appOutputToEntity`

Add equivalent methods to `ResearchMapper.java` with updated entity references (`org.apache.airavata.research.model.*`).

- [ ] **Step 5: Delete moved files from compute-service**

After confirming all files are in research-service with correct packages, delete the original files from compute-service.

- [ ] **Step 6: Fix compilation — update all remaining imports across the codebase**

Search the entire codebase for any remaining imports of the old packages:

```bash
grep -r "org.apache.airavata.compute.model.Application" --include="*.java" airavata-api/
grep -r "org.apache.airavata.compute.model.AppIoParam" --include="*.java" airavata-api/
grep -r "org.apache.airavata.compute.model.AppModule" --include="*.java" airavata-api/
grep -r "org.apache.airavata.compute.repository.Application" --include="*.java" airavata-api/
grep -r "org.apache.airavata.compute.service.AppCatalog" --include="*.java" airavata-api/
```

Update each hit to point to the new `research` packages.

- [ ] **Step 7: Build and verify**

```bash
cd airavata-api && mvn compile -T4
```

Expected: BUILD SUCCESS with zero compilation errors.

- [ ] **Step 8: Run tests**

```bash
cd airavata-api && mvn test -T4
```

Expected: All tests pass.

- [ ] **Step 9: Commit**

```bash
git add -A
git commit -m "refactor: move app catalog entities/repos from compute-service to research-service"
```

---

### Task 2: Move parser entities + repos from compute-service to research-service

**Files to move (compute-service → research-service):**

Entities:
- `airavata-api/compute-service/src/main/java/org/apache/airavata/compute/model/ParserEntity.java`
- `airavata-api/compute-service/src/main/java/org/apache/airavata/compute/model/ParsingTemplateEntity.java`

Repositories:
- `airavata-api/compute-service/src/main/java/org/apache/airavata/compute/repository/ParserRepository.java`
- `airavata-api/compute-service/src/main/java/org/apache/airavata/compute/repository/ParsingTemplateRepository.java`
- `airavata-api/compute-service/src/main/java/org/apache/airavata/compute/repository/ParserInputRepository.java`
- `airavata-api/compute-service/src/main/java/org/apache/airavata/compute/repository/ParserOutputRepository.java`

Note: `ParserInputEntity` and `ParserOutputEntity` don't exist as separate JPA entities — they are JSON-embedded in `ParserEntity`. The `ParserInputRepository` and `ParserOutputRepository` are accessor classes.

- [ ] **Step 1: Move 2 entity files to research-service**

Move `ParserEntity.java` and `ParsingTemplateEntity.java` from `compute-service/.../compute/model/` to `research-service/.../research/model/`.

Update package declarations from `org.apache.airavata.compute.model` to `org.apache.airavata.research.model`.

- [ ] **Step 2: Move 4 repository files to research-service**

Move `ParserRepository.java`, `ParsingTemplateRepository.java`, `ParserInputRepository.java`, `ParserOutputRepository.java` from `compute-service/.../compute/repository/` to `research-service/.../research/repository/`.

Update:
1. Package declarations to `org.apache.airavata.research.repository`
2. Entity imports to `org.apache.airavata.research.model.*`
3. Mapper references from `ComputeMapper` to `ResearchMapper`

- [ ] **Step 3: Move parser mapper methods from ComputeMapper to ResearchMapper**

Move parser-related mapping methods from `ComputeMapper.java` to `ResearchMapper.java`:
- `parserToModel` / `parserToEntity`
- `parsingTemplateToModel` / `parsingTemplateToEntity`
- `parserInputToModel` / `parserOutputToModel`
- Any helper methods for `IOType` enum mapping

Remove these methods and their imports from `ComputeMapper.java`.

- [ ] **Step 4: Update AppCatalogDataAccessImpl parser references**

Since `AppCatalogDataAccessImpl` was already moved to research-service in Task 1, update its parser repository imports to use the new `research.repository` package.

- [ ] **Step 5: Delete moved files from compute-service, fix imports, build and test**

```bash
grep -r "org.apache.airavata.compute.model.Parser" --include="*.java" airavata-api/
grep -r "org.apache.airavata.compute.repository.Parser" --include="*.java" airavata-api/
```

Fix any remaining references, then:

```bash
cd airavata-api && mvn compile -T4 && mvn test -T4
```

Expected: BUILD SUCCESS, all tests pass.

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "refactor: move parser entities/repos from compute-service to research-service"
```

---

### Task 3: Move ComputeJobSubmissionEntity from compute-service to orchestration-service

**Files to move:**
- `airavata-api/compute-service/src/main/java/org/apache/airavata/compute/model/ComputeJobSubmissionEntity.java`
- Associated repository (if exists — search for `ComputeJobSubmissionRepository`)

- [ ] **Step 1: Find and move entity + repository**

```bash
grep -r "ComputeJobSubmission" --include="*.java" -l airavata-api/compute-service/
```

Move found files to `orchestration-service/.../orchestration/model/` and `orchestration-service/.../orchestration/repository/`. Update packages from `org.apache.airavata.compute.*` to `org.apache.airavata.orchestration.*`.

- [ ] **Step 2: Move mapper methods if any exist**

Check `ComputeMapper.java` for any `jobSubmission*` methods that map `ComputeJobSubmissionEntity`. Move to orchestration's mapper (check `airavata-api/orchestration-service/src/main/java/org/apache/airavata/orchestration/mapper/ExecutionMapper.java`).

- [ ] **Step 3: Delete originals, fix imports, build and test**

```bash
grep -r "org.apache.airavata.compute.model.ComputeJobSubmission" --include="*.java" airavata-api/
cd airavata-api && mvn compile -T4 && mvn test -T4
```

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "refactor: move ComputeJobSubmissionEntity from compute-service to orchestration-service"
```

---

### Task 4: Move AgentDeploymentInfoEntity from agent-service to compute-service

**Files to move:**
- `airavata-api/agent-service/src/main/java/org/apache/airavata/agent/model/AgentDeploymentInfoEntity.java`
- `airavata-api/agent-service/src/main/java/org/apache/airavata/agent/repository/AgentDeploymentInfoRepository.java`

- [ ] **Step 1: Move entity and repository**

Move to `compute-service/.../compute/model/AgentDeploymentInfoEntity.java` and `compute-service/.../compute/repository/AgentDeploymentInfoRepository.java`.

Update packages from `org.apache.airavata.agent.*` to `org.apache.airavata.compute.*`.

- [ ] **Step 2: Fix imports across codebase**

```bash
grep -r "org.apache.airavata.agent.model.AgentDeploymentInfo" --include="*.java" airavata-api/
grep -r "org.apache.airavata.agent.repository.AgentDeploymentInfo" --include="*.java" airavata-api/
```

Update all hits.

- [ ] **Step 3: Build and test**

```bash
cd airavata-api && mvn compile -T4 && mvn test -T4
```

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "refactor: move AgentDeploymentInfoEntity from agent-service to compute-service"
```

---

### Task 5: Move CommunityUserEntity from credential-service to iam-service

**Files to move:**
- `airavata-api/credential-service/src/main/java/org/apache/airavata/credential/model/CommunityUserEntity.java`
- `airavata-api/credential-service/src/main/java/org/apache/airavata/credential/model/CommunityUserPK.java`
- `airavata-api/credential-service/src/main/java/org/apache/airavata/credential/repository/CommunityUserRepository.java`

**Files to update:**
- `airavata-api/credential-service/src/main/java/org/apache/airavata/credential/service/CredentialStoreService.java` — update imports
- `airavata-api/credential-service/src/test/java/org/apache/airavata/credential/service/CredentialStoreServiceTest.java` — update imports

- [ ] **Step 1: Move 3 files to iam-service**

Move to `iam-service/.../iam/model/CommunityUserEntity.java`, `iam-service/.../iam/model/CommunityUserPK.java`, `iam-service/.../iam/repository/CommunityUserRepository.java`.

Update packages from `org.apache.airavata.credential.*` to `org.apache.airavata.iam.*`.

- [ ] **Step 2: Update CredentialStoreService imports**

In `CredentialStoreService.java` and its test, change:
```java
// OLD:
import org.apache.airavata.credential.model.CommunityUserEntity;
import org.apache.airavata.credential.repository.CommunityUserRepository;
// NEW:
import org.apache.airavata.iam.model.CommunityUserEntity;
import org.apache.airavata.iam.repository.CommunityUserRepository;
```

Note: credential-service already depends on iam-service or vice versa — check the POM. If credential-service doesn't depend on iam-service, add the dependency.

- [ ] **Step 3: Build and test**

```bash
cd airavata-api && mvn compile -T4 && mvn test -T4
```

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "refactor: move CommunityUserEntity from credential-service to iam-service"
```

---

### Task 6: Move GatewayUsageReportingCommandEntity from iam-service to compute-service

**Files to move:**
- `airavata-api/iam-service/src/main/java/org/apache/airavata/iam/model/GatewayUsageReportingCommandEntity.java`
- `airavata-api/iam-service/src/main/java/org/apache/airavata/iam/repository/GatewayUsageReportingCommandRepository.java`

**Files to update:**
- `airavata-api/iam-service/src/main/java/org/apache/airavata/iam/mapper/GatewayEntityMapper.java` — update imports
- `airavata-api/orchestration-service/src/main/java/org/apache/airavata/orchestration/mapper/ExecutionMapper.java` — update imports

- [ ] **Step 1: Move entity and repository to compute-service**

Update packages from `org.apache.airavata.iam.*` to `org.apache.airavata.compute.*`.

- [ ] **Step 2: Fix imports in GatewayEntityMapper and ExecutionMapper**

Update the import statements to point to `org.apache.airavata.compute.model.GatewayUsageReportingCommandEntity` and `org.apache.airavata.compute.repository.GatewayUsageReportingCommandRepository`.

- [ ] **Step 3: Build and test**

```bash
cd airavata-api && mvn compile -T4 && mvn test -T4
```

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "refactor: move GatewayUsageReportingCommandEntity from iam-service to compute-service"
```

---

### Task 7: Move Tag/Resource/ResourceStar entities from research-service to sharing-service

**Files to move:**
- `airavata-api/research-service/src/main/java/org/apache/airavata/research/model/TagEntity.java`
- `airavata-api/research-service/src/main/java/org/apache/airavata/research/model/ResourceEntity.java`
- `airavata-api/research-service/src/main/java/org/apache/airavata/research/model/ResourceStarEntity.java`
- `airavata-api/research-service/src/main/java/org/apache/airavata/research/repository/TagRepository.java`
- `airavata-api/research-service/src/main/java/org/apache/airavata/research/repository/ResourceRepository.java`
- `airavata-api/research-service/src/main/java/org/apache/airavata/research/repository/ResourceStarRepository.java`

**Important:** `ResourceEntity` is an abstract base with `@Inheritance(strategy = InheritanceType.JOINED)`. Its subclasses `DatasetResourceEntity` and `RepositoryResourceEntity` stay in research-service. This creates a cross-module JPA inheritance — the subclasses must import from sharing-service.

**Files to update:**
- `airavata-api/research-service/src/main/java/org/apache/airavata/research/model/DatasetResourceEntity.java` — update `extends ResourceEntity` import
- `airavata-api/research-service/src/main/java/org/apache/airavata/research/model/RepositoryResourceEntity.java` — update `extends ResourceEntity` import
- `airavata-api/research-service/src/main/java/org/apache/airavata/research/service/ResearchResourceService.java` — update imports
- `airavata-api/research-service/src/main/java/org/apache/airavata/research/service/ResearchProjectService.java` — update imports
- `airavata-api/research-service/src/main/java/org/apache/airavata/research/config/DevDataInitializer.java` — update imports

- [ ] **Step 1: Move 3 entities to sharing-service**

Move to `sharing-service/.../sharing/model/`. Update packages from `org.apache.airavata.research.model` to `org.apache.airavata.sharing.model`.

- [ ] **Step 2: Move 3 repositories to sharing-service**

Move to `sharing-service/.../sharing/repository/`. Update packages and entity imports.

- [ ] **Step 3: Update research-service subclasses and services**

In `DatasetResourceEntity.java`, `RepositoryResourceEntity.java`, and service classes, update the import of `ResourceEntity` from `org.apache.airavata.research.model.ResourceEntity` to `org.apache.airavata.sharing.model.ResourceEntity`.

Check research-service `pom.xml` — if it doesn't already depend on sharing-service, add:
```xml
<dependency>
    <groupId>org.apache.airavata</groupId>
    <artifactId>sharing-service</artifactId>
    <version>${project.version}</version>
</dependency>
```

Watch for circular dependencies. If sharing-service depends on research-service, this creates a cycle. In that case, consider moving `ResourceEntity` to the shared `airavata-api` parent instead.

- [ ] **Step 4: Build and test**

```bash
cd airavata-api && mvn compile -T4 && mvn test -T4
```

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "refactor: move Tag/Resource/ResourceStar entities from research-service to sharing-service"
```

---

### Task 8: Move UserStoragePreferenceEntity from storage-service to compute-service

**Files to move:**
- `airavata-api/storage-service/src/main/java/org/apache/airavata/storage/model/UserStoragePreferenceEntity.java`
- `airavata-api/storage-service/src/main/java/org/apache/airavata/storage/model/UserStoragePreferencePK.java`
- `airavata-api/storage-service/src/main/java/org/apache/airavata/storage/repository/UserStoragePreferenceRepository.java`

- [ ] **Step 1: Move 3 files to compute-service**

Update packages from `org.apache.airavata.storage.*` to `org.apache.airavata.compute.*`.

- [ ] **Step 2: Fix imports across codebase**

```bash
grep -r "org.apache.airavata.storage.model.UserStoragePreference" --include="*.java" airavata-api/
grep -r "org.apache.airavata.storage.repository.UserStoragePreference" --include="*.java" airavata-api/
```

- [ ] **Step 3: Build and test**

```bash
cd airavata-api && mvn compile -T4 && mvn test -T4
```

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "refactor: move UserStoragePreferenceEntity from storage-service to compute-service"
```

---

### Task 9: Move resource scheduling entities from orchestration-service to compute-service

**Files to move:**
- `airavata-api/orchestration-service/src/main/java/org/apache/airavata/orchestration/model/ProcessResourceScheduleEntity.java`
- `airavata-api/orchestration-service/src/main/java/org/apache/airavata/orchestration/model/ComputationalResourceSchedulingEntity.java`
- `airavata-api/orchestration-service/src/main/java/org/apache/airavata/orchestration/model/ComputationalResourceSchedulingPK.java`
- Associated repositories (search for matching repository files)

**Files to update:**
- `airavata-api/orchestration-service/src/main/java/org/apache/airavata/orchestration/infrastructure/ExecutionDataAccessImpl.java` — update imports
- `airavata-api/orchestration-service/src/main/java/org/apache/airavata/orchestration/repository/ProcessRepository.java` — update imports

- [ ] **Step 1: Move entity files + PK class to compute-service**

Update packages from `org.apache.airavata.orchestration.model` to `org.apache.airavata.compute.model`.

- [ ] **Step 2: Move associated repositories**

Search for and move any repositories for these entities. Update packages.

- [ ] **Step 3: Fix imports in orchestration-service**

Update `ExecutionDataAccessImpl.java`, `ProcessRepository.java`, and any other files importing these entities.

- [ ] **Step 4: Build and test**

```bash
cd airavata-api && mvn compile -T4 && mvn test -T4
```

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "refactor: move resource scheduling entities from orchestration-service to compute-service"
```

---

### Task 10: Split UserConfigurationDataEntity

**File to split:**
- `airavata-api/orchestration-service/src/main/java/org/apache/airavata/orchestration/model/UserConfigurationDataEntity.java`

- [ ] **Step 1: Read the entity and identify fields**

Read `UserConfigurationDataEntity.java` to identify:
- Auth fields (userDN, generateCert) → will move to credential-service
- Execution pref fields (autoSchedule, etc.) → stay in orchestration-service

- [ ] **Step 2: Create UserAuthConfigEntity in credential-service**

Create `credential-service/.../credential/model/UserAuthConfigEntity.java` with auth fields extracted from `UserConfigurationDataEntity`.

Create `credential-service/.../credential/repository/UserAuthConfigRepository.java`.

- [ ] **Step 3: Remove auth fields from UserConfigurationDataEntity**

Remove `userDN`, `generateCert`, and any other auth-specific fields from the orchestration-service entity. Keep execution preference fields.

- [ ] **Step 4: Update ExecutionDataAccessImpl and ExecutionMapper**

Update references to handle the split — the orchestration-service code that previously read auth fields from `UserConfigurationDataEntity` now needs to query credential-service for `UserAuthConfigEntity`.

- [ ] **Step 5: Build and test**

```bash
cd airavata-api && mvn compile -T4 && mvn test -T4
```

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "refactor: split UserConfigurationDataEntity — auth fields to credential-service"
```

---

### Task 11: Remove UserEntity from sharing-service

**Files to delete:**
- `airavata-api/sharing-service/src/main/java/org/apache/airavata/sharing/model/UserEntity.java`
- `airavata-api/sharing-service/src/main/java/org/apache/airavata/sharing/model/UserPK.java`
- `airavata-api/sharing-service/src/main/java/org/apache/airavata/sharing/repository/UserRepository.java`

**Files to update (12+ usages in SharingService):**
- `airavata-api/sharing-service/src/main/java/org/apache/airavata/sharing/service/SharingService.java`
- `airavata-api/sharing-service/src/main/java/org/apache/airavata/sharing/repository/GroupMembershipRepository.java`
- `airavata-api/sharing-service/src/main/java/org/apache/airavata/sharing/util/ThriftDataModelConversion.java`
- `airavata-api/sharing-service/src/test/java/org/apache/airavata/sharing/service/ResourceSharingServiceTest.java`

- [ ] **Step 1: Audit all UserEntity usages in sharing-service**

```bash
grep -r "UserEntity\|UserRepository\|UserPK" --include="*.java" -l airavata-api/sharing-service/
```

For each usage, plan the replacement: replace UserEntity references with userId (String) references, and delegate user lookups to iam-service via the `UserProfileProvider` interface.

- [ ] **Step 2: Refactor SharingService to use userId references**

Replace all `UserEntity` usage in `SharingService.java` with userId-based lookups. Where user profile data is needed (name, email), call iam-service's `UserProfileProvider` interface.

This is the most involved substep — `SharingService` has 12+ usages including create, get, update, delete, isExists, select, getAccessibleUsers, getDirectlyAccessibleUsers.

- [ ] **Step 3: Update GroupMembershipRepository and ThriftDataModelConversion**

Refactor these classes to work with userId references instead of UserEntity objects.

- [ ] **Step 4: Update tests**

Update `ResourceSharingServiceTest.java` to reflect the refactored approach.

- [ ] **Step 5: Delete UserEntity, UserPK, and UserRepository**

Remove the three files after all references are eliminated.

- [ ] **Step 6: Build and test**

```bash
cd airavata-api && mvn compile -T4 && mvn test -T4
```

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "refactor: remove duplicate UserEntity from sharing-service, use userId references"
```

---

### Task 12: Consolidate TenantProfileRepository into GatewayRepository

**File to delete:**
- `airavata-api/iam-service/src/main/java/org/apache/airavata/iam/repository/TenantProfileRepository.java`

**File to modify:**
- `airavata-api/iam-service/src/main/java/org/apache/airavata/iam/repository/GatewayRepository.java`

- [ ] **Step 1: Read both repositories and identify unique queries**

Read `TenantProfileRepository.java` and `GatewayRepository.java` to identify which query methods in TenantProfileRepository are not already in GatewayRepository.

- [ ] **Step 2: Merge unique queries into GatewayRepository**

Add any unique query methods from `TenantProfileRepository` to `GatewayRepository`.

- [ ] **Step 3: Update all TenantProfileRepository references**

```bash
grep -r "TenantProfileRepository" --include="*.java" -l airavata-api/
```

Replace all references with `GatewayRepository`.

- [ ] **Step 4: Delete TenantProfileRepository**

Remove the file after all references are updated.

- [ ] **Step 5: Build and test**

```bash
cd airavata-api && mvn compile -T4 && mvn test -T4
```

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "refactor: consolidate TenantProfileRepository into GatewayRepository"
```

---

## Final Verification

After all 12 tasks are complete:

- [ ] **Full build**: `cd airavata-api && mvn clean compile -T4`
- [ ] **Full test suite**: `cd airavata-api && mvn test -T4`
- [ ] **Integration tests**: `cd airavata-api && mvn test -pl airavata-api -Dgroups=runtime`
- [ ] **Tilt check**: `tilt up` and verify health at `http://localhost:9090/internal/actuator/health`
