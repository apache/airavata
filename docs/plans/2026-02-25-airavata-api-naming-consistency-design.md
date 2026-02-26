# Airavata-API Naming & Structure Consistency — Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Consolidate misplaced files, fix naming inconsistencies, and move a controller to the correct module — 7 file moves + 1 rename with import updates.

**Architecture:** Purely structural refactoring. No behavioral changes, no API changes. Move files to their correct packages, update all import references, verify compilation.

**Tech Stack:** Java 21, Spring Boot, MapStruct, Maven multi-module

---

## Context

- **Base path (airavata-api):** `modules/airavata-api/src/main/java/org/apache/airavata/`
- **Base path (rest-api):** `modules/rest-api/src/main/java/org/apache/airavata/restapi/`
- **Test config:** `modules/airavata-api/src/test/java/org/apache/airavata/config/IntegrationTestConfiguration.java`
- **Build command:** `mvn clean install -DskipTests` (from repo root)
- **Test command:** `mvn test -pl modules/airavata-api` (for unit/integration tests)

---

### Task 1: Consolidate Project Domain — Move Project.java

**Files:**
- Delete: `modules/airavata-api/src/main/java/org/apache/airavata/research/experiment/model/Project.java`
- Create: `modules/airavata-api/src/main/java/org/apache/airavata/research/project/model/Project.java`

**Step 1: Create the target directory and file**

Create `research/project/model/Project.java` with updated package declaration:

```java
package org.apache.airavata.research.project.model;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public class Project {
    private String projectId;
    private String gatewayId;
    private String userName;

    @NotBlank(message = "projectName is required")
    private String projectName;

    private String description;
    private Instant createdAt;
    private Instant updatedAt;

    public Project() {}

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getGatewayId() { return gatewayId; }
    public void setGatewayId(String gatewayId) { this.gatewayId = gatewayId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
```

**Step 2: Delete the old file**

```bash
rm modules/airavata-api/src/main/java/org/apache/airavata/research/experiment/model/Project.java
```

**Step 3: Update imports in all referencing files**

Update `import org.apache.airavata.research.experiment.model.Project` → `import org.apache.airavata.research.project.model.Project` in:

1. `modules/airavata-api/src/main/java/org/apache/airavata/research/experiment/service/ExperimentService.java`
2. `modules/airavata-api/src/main/java/org/apache/airavata/research/experiment/service/DefaultExperimentService.java`
3. `modules/airavata-api/src/main/java/org/apache/airavata/research/experiment/service/ProjectService.java` (will move in Task 4, but update now)
4. `modules/airavata-api/src/main/java/org/apache/airavata/research/experiment/mapper/ProjectMapper.java` (will move in Task 2, but update now)
5. `modules/airavata-api/src/main/java/org/apache/airavata/research/experiment/service/DefaultProjectService.java` (will move in Task 5, but update now)
6. `modules/rest-api/src/main/java/org/apache/airavata/restapi/controller/ProjectController.java`
7. `modules/agent-framework/agent-service/src/main/java/org/apache/airavata/agent/service/AgentManagementService.java`
8. `modules/agent-framework/agent-service/src/main/java/org/apache/airavata/agent/service/AiravataFileService.java`

---

### Task 2: Consolidate Project Domain — Move ProjectMapper.java

**Files:**
- Delete: `modules/airavata-api/src/main/java/org/apache/airavata/research/experiment/mapper/ProjectMapper.java`
- Create: `modules/airavata-api/src/main/java/org/apache/airavata/research/project/mapper/ProjectMapper.java`

**Step 1: Create the target directory and file**

Create `research/project/mapper/ProjectMapper.java` with updated package and import:

```java
package org.apache.airavata.research.project.mapper;

import org.apache.airavata.config.EntityMapperConfiguration;
import org.apache.airavata.core.mapper.EntityMapper;
import org.apache.airavata.research.project.entity.ProjectEntity;
import org.apache.airavata.research.project.model.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between {@link ProjectEntity} and {@link Project}.
 *
 * <p>Field names differ between the model and the entity:
 * {@code Project.userName} maps to {@code ProjectEntity.ownerId}, and
 * {@code Project.projectName} maps to {@code ProjectEntity.name}.
 * The {@code experiments} lazy collection, {@code repositoryArtifact},
 * {@code datasetArtifacts}, and {@code state} are excluded from the
 * {@code toEntity} direction to prevent unintended Hibernate proxy
 * initialisation and to avoid overwriting managed state.
 */
@Mapper(componentModel = "spring", config = EntityMapperConfiguration.class)
public interface ProjectMapper extends EntityMapper<ProjectEntity, Project> {

    @Override
    @Mapping(target = "ownerId", source = "userName")
    @Mapping(target = "name", source = "projectName")
    @Mapping(target = "experiments", ignore = true)
    @Mapping(target = "repositoryArtifact", ignore = true)
    @Mapping(target = "datasetArtifacts", ignore = true)
    @Mapping(target = "state", ignore = true)
    ProjectEntity toEntity(Project model);

    @Override
    @Mapping(target = "userName", source = "ownerId")
    @Mapping(target = "projectName", source = "name")
    Project toModel(ProjectEntity entity);
}
```

**Step 2: Delete the old file**

```bash
rm modules/airavata-api/src/main/java/org/apache/airavata/research/experiment/mapper/ProjectMapper.java
```

**Step 3: Update imports in referencing files**

Update `import org.apache.airavata.research.experiment.mapper.ProjectMapper` → `import org.apache.airavata.research.project.mapper.ProjectMapper` in:

1. `modules/airavata-api/src/main/java/org/apache/airavata/research/experiment/service/DefaultProjectService.java` (will move in Task 5)

**Step 4: Update IntegrationTestConfiguration component scan**

In `modules/airavata-api/src/test/java/org/apache/airavata/config/IntegrationTestConfiguration.java`, add to the `basePackages` array:

```
"org.apache.airavata.research.project.mapper",
```

The old `"org.apache.airavata.research.experiment.mapper"` can stay — it still has `ExperimentMapper`, `ExperimentSummaryMapper`, `NotificationMapper`.

---

### Task 3: Consolidate Project Domain — Move ProjectRepository.java

**Files:**
- Delete: `modules/airavata-api/src/main/java/org/apache/airavata/research/experiment/repository/ProjectRepository.java`
- Create: `modules/airavata-api/src/main/java/org/apache/airavata/research/project/repository/ProjectRepository.java`

**Step 1: Create the file**

Create `research/project/repository/ProjectRepository.java` with updated package:

```java
package org.apache.airavata.research.project.repository;

import java.util.List;
import org.apache.airavata.research.project.entity.ProjectEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link ProjectEntity}.
 *
 * <p>Provides query methods for gateway projects, which serve as grouping constructs for
 * experiments. Method naming conventions are used for simple lookups; explicit JPQL is
 * avoided where derivation is straightforward.
 */
@Repository("projectRepository")
public interface ProjectRepository extends JpaRepository<ProjectEntity, String> {

    List<ProjectEntity> findByGatewayId(String gatewayId);

    List<ProjectEntity> findByGatewayIdOrderByCreatedAtDesc(String gatewayId, Pageable pageable);
}
```

**Step 2: Delete the old file**

```bash
rm modules/airavata-api/src/main/java/org/apache/airavata/research/experiment/repository/ProjectRepository.java
```

**Step 3: Update imports in referencing files**

Update `import org.apache.airavata.research.experiment.repository.ProjectRepository` → `import org.apache.airavata.research.project.repository.ProjectRepository` in:

1. `modules/airavata-api/src/main/java/org/apache/airavata/research/experiment/service/DefaultProjectService.java` (will move in Task 5)

---

### Task 4: Consolidate Project Domain — Move ProjectService.java

**Files:**
- Delete: `modules/airavata-api/src/main/java/org/apache/airavata/research/experiment/service/ProjectService.java`
- Create: `modules/airavata-api/src/main/java/org/apache/airavata/research/project/service/ProjectService.java`

**Step 1: Create the file**

Create `research/project/service/ProjectService.java` with updated package and import:

```java
package org.apache.airavata.research.project.service;

import java.util.List;
import org.apache.airavata.core.service.CrudService;
import org.apache.airavata.research.project.model.Project;

/**
 * Domain service for managing experiment projects within a gateway.
 *
 * <p>Extends {@link CrudService} for the standard create/get/update/delete/listByGateway
 * contract. Domain-specific methods with differing signatures are declared here.
 */
public interface ProjectService extends CrudService<Project> {

    String createProject(String gatewayId, Project project);

    default Project getProject(String projectId) {
        return get(projectId);
    }

    default void updateProject(String projectId, Project updatedProject) {
        update(projectId, updatedProject);
    }

    boolean deleteProject(String projectId);

    List<Project> searchProjects(String gatewayId, String userName, Object searchFields, int limit, int offset);
}
```

**Step 2: Delete the old file**

```bash
rm modules/airavata-api/src/main/java/org/apache/airavata/research/experiment/service/ProjectService.java
```

**Step 3: Update imports in referencing files**

Update `import org.apache.airavata.research.experiment.service.ProjectService` → `import org.apache.airavata.research.project.service.ProjectService` in:

1. `modules/rest-api/src/main/java/org/apache/airavata/restapi/controller/ProjectController.java`

Note: `DefaultExperimentService` injects `ProjectService` — check if it imports the interface or uses constructor injection by type. Update as needed.

**Step 4: Update IntegrationTestConfiguration component scan**

In `modules/airavata-api/src/test/java/org/apache/airavata/config/IntegrationTestConfiguration.java`, add to the `basePackages` array:

```
"org.apache.airavata.research.project.service",
```

The old `"org.apache.airavata.research.experiment.service"` stays — it still has `ExperimentService`, `NotificationService`, etc.

---

### Task 5: Consolidate Project Domain — Move DefaultProjectService.java

**Files:**
- Delete: `modules/airavata-api/src/main/java/org/apache/airavata/research/experiment/service/DefaultProjectService.java`
- Create: `modules/airavata-api/src/main/java/org/apache/airavata/research/project/service/DefaultProjectService.java`

**Step 1: Create the file**

Create `research/project/service/DefaultProjectService.java` with all updated imports:

```java
package org.apache.airavata.research.project.service;

import java.util.List;
import org.apache.airavata.core.service.AbstractCrudService;
import org.apache.airavata.core.util.IdGenerator;
import org.apache.airavata.core.util.PaginationUtil;
import org.apache.airavata.research.project.entity.ProjectEntity;
import org.apache.airavata.research.project.mapper.ProjectMapper;
import org.apache.airavata.research.project.model.Project;
import org.apache.airavata.research.project.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link ProjectService}.
 *
 * <p>Standard CRUD operations (get/update/delete/listByGateway) are provided by
 * {@link AbstractCrudService}. Domain-specific methods ({@link #createProject},
 * {@link #deleteProject}, {@link #searchProjects}) are implemented here because they carry
 * extra parameters or return types that differ from the generic contract.
 */
@Service("projectServiceFacade")
public class DefaultProjectService extends AbstractCrudService<ProjectEntity, Project>
        implements ProjectService {

    private final ProjectRepository projectRepository;

    public DefaultProjectService(ProjectRepository repository, ProjectMapper mapper) {
        super(repository, mapper);
        this.projectRepository = repository;
    }

    @Override
    protected String getId(Project model) {
        return model.getProjectId();
    }

    @Override
    protected void setId(Project model, String id) {
        model.setProjectId(id);
    }

    @Override
    protected List<ProjectEntity> findByGateway(String gatewayId) {
        return projectRepository.findByGatewayId(gatewayId);
    }

    @Override
    protected String entityName() {
        return "Project";
    }

    @Override
    @Transactional
    public String createProject(String gatewayId, Project project) {
        project.setProjectId(IdGenerator.ensureId(project.getProjectId()));
        project.setGatewayId(gatewayId);
        var saved = projectRepository.save(mapper.toEntity(project));
        logger.debug("Created project with id={}", saved.getProjectId());
        return saved.getProjectId();
    }

    @Override
    @Transactional
    public boolean deleteProject(String projectId) {
        if (!projectRepository.existsById(projectId)) {
            return false;
        }
        projectRepository.deleteById(projectId);
        logger.debug("Deleted {} id={}", entityName(), projectId);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Project> searchProjects(
            String gatewayId, String userName, Object searchFields, int limit, int offset) {
        var pageable = PaginationUtil.toPageRequest(limit, offset);
        return mapper.toModelList(projectRepository.findByGatewayIdOrderByCreatedAtDesc(gatewayId, pageable));
    }
}
```

**Step 2: Delete the old file**

```bash
rm modules/airavata-api/src/main/java/org/apache/airavata/research/experiment/service/DefaultProjectService.java
```

**Step 3: Verify no remaining references to old packages**

```bash
grep -r "research.experiment.model.Project" modules/ --include="*.java"
grep -r "research.experiment.mapper.ProjectMapper" modules/ --include="*.java"
grep -r "research.experiment.repository.ProjectRepository" modules/ --include="*.java"
grep -r "research.experiment.service.ProjectService" modules/ --include="*.java"
grep -r "research.experiment.service.DefaultProjectService" modules/ --include="*.java"
```

All should return empty.

**Step 4: Clean up empty directories**

If `research/experiment/mapper/` is now empty (check first — it may still contain `ExperimentMapper`, `ExperimentSummaryMapper`, `NotificationMapper`), leave it. Only delete if empty:

```bash
# Only if empty:
rmdir modules/airavata-api/src/main/java/org/apache/airavata/research/experiment/mapper/ 2>/dev/null || true
rmdir modules/airavata-api/src/main/java/org/apache/airavata/research/experiment/repository/ 2>/dev/null || true
```

**Step 5: Build to verify**

```bash
mvn clean install -DskipTests
```

Expected: BUILD SUCCESS

**Step 6: Commit**

```bash
git add -A
git commit -m "refactor: consolidate Project domain into research/project package

Move Project, ProjectMapper, ProjectRepository, ProjectService, and
DefaultProjectService from research/experiment/ to research/project/.
Update all import references across airavata-api, rest-api, and
agent-framework modules."
```

---

### Task 6: Move TokenResponse to iam/dto/

**Files:**
- Delete: `modules/airavata-api/src/main/java/org/apache/airavata/iam/model/TokenResponse.java`
- Create: `modules/airavata-api/src/main/java/org/apache/airavata/iam/dto/TokenResponse.java`

**Step 1: Create the target directory and file**

Create `iam/dto/TokenResponse.java` with updated package:

```java
package org.apache.airavata.iam.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representing an OAuth token response from Keycloak.
 */
public class TokenResponse {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private Integer expiresIn;

    @JsonProperty("refresh_token")
    private String refreshToken;

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public Integer getExpiresIn() { return expiresIn; }
    public void setExpiresIn(Integer expiresIn) { this.expiresIn = expiresIn; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
```

**Step 2: Delete the old file**

```bash
rm modules/airavata-api/src/main/java/org/apache/airavata/iam/model/TokenResponse.java
```

**Step 3: Update imports**

Update `import org.apache.airavata.iam.model.TokenResponse` → `import org.apache.airavata.iam.dto.TokenResponse` in:

1. `modules/airavata-api/src/main/java/org/apache/airavata/iam/keycloak/KeycloakRestClient.java`

**Step 4: Build to verify**

```bash
mvn clean install -DskipTests
```

Expected: BUILD SUCCESS

**Step 5: Commit**

```bash
git add -A
git commit -m "refactor: move TokenResponse to iam/dto package

TokenResponse is an OAuth DTO, not a domain model. Move it from
iam/model/ to iam/dto/ for consistency with the project's DTO
convention (dto/ package with Request/Response suffixes)."
```

---

### Task 7: Move MonitoringJobStatusController to rest-api Module

**Files:**
- Delete: `modules/airavata-api/src/main/java/org/apache/airavata/execution/monitoring/MonitoringJobStatusController.java`
- Create: `modules/rest-api/src/main/java/org/apache/airavata/restapi/controller/MonitoringJobStatusController.java`

**Step 1: Create the file in rest-api**

Create `restapi/controller/MonitoringJobStatusController.java` with updated package and added OpenAPI tag:

```java
package org.apache.airavata.restapi.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.airavata.execution.monitoring.JobStatusMonitor;
import org.apache.airavata.execution.monitoring.MessagingContracts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoint for job scripts to report status. Replaces Kafka REST / status-publish-endpoint.
 * Configure airavata.services.monitor.compute.resource-status-callback-url to the base URL of the API
 * plus /api/v1/monitoring/job-status (e.g. http://airavata-api:8080/api/v1/monitoring/job-status).
 *
 * <p>Accepts JSON: {"jobName":"...", "status":"RUNNING"|"COMPLETED"|"FAILED"|..., "task":"taskId"}.
 * Publishes canonical JobStatusUpdateEvent to status-change-topic (same path as email and realtime).
 */
@RestController
@RequestMapping("/api/v1/monitoring")
@Tag(name = "Monitoring")
public class MonitoringJobStatusController {

    private static final Logger log = LoggerFactory.getLogger(MonitoringJobStatusController.class);

    private final JobStatusMonitor jobStatusMonitor;

    @Autowired
    public MonitoringJobStatusController(@Autowired(required = false) JobStatusMonitor jobStatusMonitor) {
        this.jobStatusMonitor = jobStatusMonitor;
    }

    @PostMapping("/job-status")
    public ResponseEntity<Void> jobStatus(@RequestBody JobStatusRequest req) {
        if (jobStatusMonitor == null) {
            log.warn("job-status callback received but JobStatusMonitor not available");
            return ResponseEntity.ok().build();
        }
        if (req == null || req.jobName == null || req.status == null || req.task == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            var event = new MessagingContracts.JobStatusUpdateEvent(
                    req.jobName(), req.status(), req.task(), "job-callback", null);
            jobStatusMonitor.publish(event);
        } catch (Exception e) {
            log.error("Error publishing job-status callback jobName={} status={}", req.jobName(), req.status(), e);
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok().build();
    }

    public record JobStatusRequest(String jobName, String status, String task) {}
}
```

**Step 2: Delete the old file**

```bash
rm modules/airavata-api/src/main/java/org/apache/airavata/execution/monitoring/MonitoringJobStatusController.java
```

**Step 3: Verify rest-api imports are resolved**

The `rest-api` module already depends on `airavata-api` in its pom.xml, so `JobStatusMonitor` and `MessagingContracts` from `execution/monitoring/` are accessible. Verify the OpenAPI `@Tag` import — check if `io.swagger.v3.oas.annotations.tags.Tag` is available in the rest-api module's dependencies.

**Step 4: Build to verify**

```bash
mvn clean install -DskipTests
```

Expected: BUILD SUCCESS

**Step 5: Commit**

```bash
git add -A
git commit -m "refactor: move MonitoringJobStatusController to rest-api module

This was the only REST controller in airavata-api. All 31 other
controllers live in rest-api. Move it there for consistency and add
@Tag for OpenAPI documentation."
```

---

### Task 8: Rename ResourceConfiguration to ResourceConfig

**Files:**
- Modify: `modules/airavata-api/src/main/java/org/apache/airavata/compute/provider/slurm/ResourceConfiguration.java` → rename to `ResourceConfig.java`

**Step 1: Create the renamed file**

Create `compute/provider/slurm/ResourceConfig.java`:

```java
package org.apache.airavata.compute.provider.slurm;

import java.util.List;
import org.apache.airavata.compute.resource.model.ResourceJobManagerType;

public class ResourceConfig {
    private ResourceJobManagerType jobManagerType;
    private String emailParser;
    private List<String> resourceEmailAddresses;

    public ResourceJobManagerType getJobManagerType() {
        return jobManagerType;
    }

    public void setJobManagerType(ResourceJobManagerType jobManagerType) {
        this.jobManagerType = jobManagerType;
    }

    public String getEmailParser() {
        return emailParser;
    }

    public void setEmailParser(String emailParser) {
        this.emailParser = emailParser;
    }

    public List<String> getResourceEmailAddresses() {
        return resourceEmailAddresses;
    }

    public void setResourceEmailAddresses(List<String> resourceEmailAddresses) {
        this.resourceEmailAddresses = resourceEmailAddresses;
    }
}
```

**Step 2: Delete the old file**

```bash
rm modules/airavata-api/src/main/java/org/apache/airavata/compute/provider/slurm/ResourceConfiguration.java
```

**Step 3: Update imports**

Update `import org.apache.airavata.compute.provider.slurm.ResourceConfiguration` → `import org.apache.airavata.compute.provider.slurm.ResourceConfig` in:

1. `modules/airavata-api/src/main/java/org/apache/airavata/execution/monitoring/EmailMonitorWorkflow.java`

Also update all usages of `ResourceConfiguration` class name to `ResourceConfig` within `EmailMonitorWorkflow.java`:
- Field: `Map<ResourceJobManagerType, ResourceConfiguration>` → `Map<ResourceJobManagerType, ResourceConfig>`
- Constructor: `ResourceConfiguration resourceConfig = new ResourceConfiguration();` → `ResourceConfig resourceConfig = new ResourceConfig();`
- Method param: `Map<ResourceJobManagerType, ResourceConfiguration>` → `Map<ResourceJobManagerType, ResourceConfig>`
- Loop var: `ResourceConfiguration config = ...` → `ResourceConfig config = ...`

**Step 4: Build to verify**

```bash
mvn clean install -DskipTests
```

Expected: BUILD SUCCESS

**Step 5: Commit**

```bash
git add -A
git commit -m "refactor: rename ResourceConfiguration to ResourceConfig

This is a data class, not a Spring @Configuration bean. Use Config
suffix for data classes, Configuration for Spring beans."
```

---

### Task 9: Final Verification

**Step 1: Full build with tests**

```bash
mvn clean install
```

Expected: BUILD SUCCESS with all tests passing.

**Step 2: Verify no stale references**

```bash
grep -r "research.experiment.model.Project[^E]" modules/ --include="*.java"
grep -r "research.experiment.mapper.ProjectMapper" modules/ --include="*.java"
grep -r "research.experiment.repository.ProjectRepository" modules/ --include="*.java"
grep -r "research.experiment.service.ProjectService" modules/ --include="*.java"
grep -r "research.experiment.service.DefaultProjectService" modules/ --include="*.java"
grep -r "iam.model.TokenResponse" modules/ --include="*.java"
grep -r "execution.monitoring.MonitoringJobStatusController" modules/ --include="*.java"
grep -r "slurm.ResourceConfiguration" modules/ --include="*.java"
```

All should return empty.

**Step 3: Verify no empty directories remain**

```bash
find modules/airavata-api/src/main/java -type d -empty
```

Remove any empty directories found.
