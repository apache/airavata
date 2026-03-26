# airavata-service Layer Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Extract experiment business logic from `AiravataServerHandler` into a service layer package, proving the pattern before migrating remaining domains.

**Architecture:** New `org.apache.airavata.service.*` package within `airavata-api` containing `ExperimentService` (business logic), `RequestContext` (transport-agnostic identity), service exceptions, and `EventPublisher` (messaging wrapper). The existing handler becomes a thin Thrift transport layer using `ThriftAdapter` to eliminate boilerplate. A separate Maven module would create a circular dependency (model classes, repositories, and handlers all live in `airavata-api`), so the service layer lives as a package for now and can be extracted when those shared types are factored out.

**Tech Stack:** Java 17, Maven, JUnit 5, Mockito 5, existing Thrift-generated model classes

---

## File Structure

### New files (all within airavata-api)

| File | Responsibility |
|------|----------------|
| `.../service/context/RequestContext.java` | Transport-agnostic identity (userId, gatewayId, claims) |
| `.../service/exception/ServiceException.java` | General service error |
| `.../service/exception/ServiceAuthorizationException.java` | Permission denied |
| `.../service/exception/ServiceNotFoundException.java` | Resource not found |
| `.../service/messaging/EventPublisher.java` | Wraps `Publisher` with typed methods for experiment events |
| `.../service/experiment/ExperimentService.java` | Experiment business logic extracted from handler |
| `.../api/server/handler/ThriftAdapter.java` | DRY exception translation + RequestContext construction |
| `...test.../service/context/RequestContextTest.java` | Unit tests for RequestContext |
| `...test.../service/experiment/ExperimentServiceTest.java` | Unit tests for ExperimentService |

All paths are relative to `airavata-api/src/main/java/org/apache/airavata/` (or `src/test/java/...` for tests).

### Modified files

| File | Change |
|------|--------|
| `airavata-api/pom.xml` | Add mockito test dependencies |
| `AiravataServerHandler.java` | Add `ExperimentService` field, rewire experiment methods to one-liner delegates |

---

### Task 1: Add test dependencies to airavata-api POM

**Files:**
- Modify: `airavata-api/pom.xml`

- [ ] **Step 1: Add Mockito dependencies**

In `airavata-api/pom.xml`, add inside the `<dependencies>` block (after the existing JUnit dependency around line 172):

```xml
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>5.11.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <version>5.11.0</version>
    <scope>test</scope>
</dependency>
```

- [ ] **Step 2: Verify POM resolves**

```bash
cd /Users/yasith/code/artisan/airavata && mvn validate -pl airavata-api
```

Expected: BUILD SUCCESS.

- [ ] **Step 3: Commit**

```bash
git add airavata-api/pom.xml
git commit -m "build: add mockito test dependencies to airavata-api"
```

---

### Task 2: Implement RequestContext

**Files:**
- Create: `airavata-api/src/main/java/org/apache/airavata/service/context/RequestContext.java`
- Test: `airavata-api/src/test/java/org/apache/airavata/service/context/RequestContextTest.java`

- [ ] **Step 1: Write the failing test**

```java
package org.apache.airavata.service.context;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RequestContextTest {

    @Test
    void constructorSetsFields() {
        RequestContext ctx = new RequestContext("testUser", "testGateway", "token123",
                Map.of("role", "admin"));

        assertEquals("testUser", ctx.getUserId());
        assertEquals("testGateway", ctx.getGatewayId());
        assertEquals("token123", ctx.getAccessToken());
        assertEquals("admin", ctx.getClaims().get("role"));
    }

    @Test
    void claimsMapIsUnmodifiable() {
        RequestContext ctx = new RequestContext("u", "g", "t", Map.of("k", "v"));
        assertThrows(UnsupportedOperationException.class, () -> ctx.getClaims().put("new", "val"));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
cd /Users/yasith/code/artisan/airavata && mvn test -pl airavata-api -Dtest="org.apache.airavata.service.context.RequestContextTest" -DfailIfNoTests=false
```

Expected: compilation error — `RequestContext` class does not exist.

- [ ] **Step 3: Write the implementation**

```java
package org.apache.airavata.service.context;

import java.util.Collections;
import java.util.Map;

public class RequestContext {

    private final String userId;
    private final String gatewayId;
    private final String accessToken;
    private final Map<String, String> claims;

    public RequestContext(String userId, String gatewayId, String accessToken, Map<String, String> claims) {
        this.userId = userId;
        this.gatewayId = gatewayId;
        this.accessToken = accessToken;
        this.claims = Collections.unmodifiableMap(claims);
    }

    public String getUserId() { return userId; }
    public String getGatewayId() { return gatewayId; }
    public String getAccessToken() { return accessToken; }
    public Map<String, String> getClaims() { return claims; }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
cd /Users/yasith/code/artisan/airavata && mvn test -pl airavata-api -Dtest="org.apache.airavata.service.context.RequestContextTest"
```

Expected: 2 tests PASS.

- [ ] **Step 5: Commit**

```bash
git add airavata-api/src/main/java/org/apache/airavata/service/context/RequestContext.java \
       airavata-api/src/test/java/org/apache/airavata/service/context/RequestContextTest.java
git commit -m "feat: add RequestContext for transport-agnostic identity"
```

---

### Task 3: Implement service exceptions

**Files:**
- Create: `airavata-api/src/main/java/org/apache/airavata/service/exception/ServiceException.java`
- Create: `airavata-api/src/main/java/org/apache/airavata/service/exception/ServiceAuthorizationException.java`
- Create: `airavata-api/src/main/java/org/apache/airavata/service/exception/ServiceNotFoundException.java`

- [ ] **Step 1: Create ServiceException**

```java
package org.apache.airavata.service.exception;

public class ServiceException extends Exception {

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

- [ ] **Step 2: Create ServiceAuthorizationException**

```java
package org.apache.airavata.service.exception;

public class ServiceAuthorizationException extends ServiceException {

    public ServiceAuthorizationException(String message) {
        super(message);
    }

    public ServiceAuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

- [ ] **Step 3: Create ServiceNotFoundException**

```java
package org.apache.airavata.service.exception;

public class ServiceNotFoundException extends ServiceException {

    public ServiceNotFoundException(String message) {
        super(message);
    }

    public ServiceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

- [ ] **Step 4: Verify compilation**

```bash
cd /Users/yasith/code/artisan/airavata && mvn compile -pl airavata-api
```

Expected: BUILD SUCCESS.

- [ ] **Step 5: Commit**

```bash
git add airavata-api/src/main/java/org/apache/airavata/service/exception/
git commit -m "feat: add service exception hierarchy"
```

---

### Task 4: Implement EventPublisher

**Files:**
- Create: `airavata-api/src/main/java/org/apache/airavata/service/messaging/EventPublisher.java`

Wraps the existing `Publisher` interface (`org.apache.airavata.messaging.core.Publisher`) with typed methods for experiment events, replacing the `MessageContext` construction scattered across `AiravataServerHandler` (lines 6469-6506).

- [ ] **Step 1: Write EventPublisher**

```java
package org.apache.airavata.service.messaging;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.model.messaging.event.ExperimentStatusChangeEvent;
import org.apache.airavata.model.messaging.event.ExperimentSubmitEvent;
import org.apache.airavata.model.messaging.event.ExperimentIntermediateOutputsEvent;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.status.ExperimentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class EventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(EventPublisher.class);

    private final Publisher statusPublisher;
    private final Publisher experimentPublisher;

    public EventPublisher(Publisher statusPublisher, Publisher experimentPublisher) {
        this.statusPublisher = statusPublisher;
        this.experimentPublisher = experimentPublisher;
    }

    public void publishExperimentStatus(String experimentId, String gatewayId, ExperimentState state) {
        if (statusPublisher == null) return;
        try {
            ExperimentStatusChangeEvent event = new ExperimentStatusChangeEvent(state, experimentId, gatewayId);
            String messageId = AiravataUtils.getId("EXPERIMENT");
            MessageContext messageContext = new MessageContext(event, MessageType.EXPERIMENT, messageId, gatewayId);
            messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            statusPublisher.publish(messageContext);
        } catch (AiravataException e) {
            logger.error("Failed to publish experiment status event for {}", experimentId, e);
        }
    }

    public void publishExperimentLaunch(String experimentId, String gatewayId) {
        if (experimentPublisher == null) return;
        try {
            ExperimentSubmitEvent event = new ExperimentSubmitEvent(experimentId, gatewayId);
            MessageContext messageContext = new MessageContext(
                    event, MessageType.EXPERIMENT, "LAUNCH.EXP-" + UUID.randomUUID(), gatewayId);
            messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            experimentPublisher.publish(messageContext);
        } catch (AiravataException e) {
            logger.error("Failed to publish experiment launch event for {}", experimentId, e);
        }
    }

    public void publishExperimentCancel(String experimentId, String gatewayId) {
        if (experimentPublisher == null) return;
        try {
            ExperimentSubmitEvent event = new ExperimentSubmitEvent(experimentId, gatewayId);
            MessageContext messageContext = new MessageContext(
                    event, MessageType.EXPERIMENT_CANCEL, "CANCEL.EXP-" + UUID.randomUUID(), gatewayId);
            messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            experimentPublisher.publish(messageContext);
        } catch (AiravataException e) {
            logger.error("Failed to publish experiment cancel event for {}", experimentId, e);
        }
    }

    public void publishIntermediateOutputs(String experimentId, String gatewayId, List<String> outputNames) {
        if (experimentPublisher == null) return;
        try {
            ExperimentIntermediateOutputsEvent event =
                    new ExperimentIntermediateOutputsEvent(experimentId, gatewayId, outputNames);
            MessageContext messageContext = new MessageContext(
                    event, MessageType.INTERMEDIATE_OUTPUTS,
                    "INTERMEDIATE_OUTPUTS.EXP-" + UUID.randomUUID(), gatewayId);
            messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            experimentPublisher.publish(messageContext);
        } catch (AiravataException e) {
            logger.error("Failed to publish intermediate outputs event for {}", experimentId, e);
        }
    }
}
```

- [ ] **Step 2: Verify compilation**

```bash
cd /Users/yasith/code/artisan/airavata && mvn compile -pl airavata-api
```

Expected: BUILD SUCCESS.

- [ ] **Step 3: Commit**

```bash
git add airavata-api/src/main/java/org/apache/airavata/service/messaging/EventPublisher.java
git commit -m "feat: add EventPublisher wrapping messaging infrastructure"
```

---

### Task 5: Implement ExperimentService — createExperiment and getExperiment

**Files:**
- Create: `airavata-api/src/main/java/org/apache/airavata/service/experiment/ExperimentService.java`
- Create: `airavata-api/src/test/java/org/apache/airavata/service/experiment/ExperimentServiceTest.java`

Extracts `createExperiment` from `AiravataServerHandler` lines 1417-1472 and `getExperiment` from lines 1554-1594.

- [ ] **Step 1: Write the failing test**

```java
package org.apache.airavata.service.experiment;

import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.registry.api.service.handler.RegistryServerHandler;
import org.apache.airavata.service.context.RequestContext;
import org.apache.airavata.service.messaging.EventPublisher;
import org.apache.airavata.sharing.registry.server.SharingRegistryServerHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExperimentServiceTest {

    @Mock RegistryServerHandler registryHandler;
    @Mock SharingRegistryServerHandler sharingHandler;
    @Mock EventPublisher eventPublisher;

    ExperimentService experimentService;
    RequestContext ctx;

    @BeforeEach
    void setUp() {
        experimentService = new ExperimentService(registryHandler, sharingHandler, eventPublisher);
        ctx = new RequestContext("testUser", "testGateway", "token123",
                Map.of("userName", "testUser", "gatewayId", "testGateway"));
    }

    @Test
    void createExperiment_returnsExperimentId() throws Exception {
        ExperimentModel experiment = new ExperimentModel();
        experiment.setExperimentName("test-exp");
        experiment.setGatewayId("testGateway");
        experiment.setUserName("testUser");
        experiment.setProjectId("proj-1");

        when(registryHandler.createExperiment("testGateway", experiment)).thenReturn("exp-123");

        String result = experimentService.createExperiment(ctx, experiment);

        assertEquals("exp-123", result);
        verify(registryHandler).createExperiment("testGateway", experiment);
    }

    @Test
    void getExperiment_ownerGetsAccess() throws Exception {
        ExperimentModel experiment = new ExperimentModel();
        experiment.setUserName("testUser");
        experiment.setGatewayId("testGateway");

        when(registryHandler.getExperiment("exp-123")).thenReturn(experiment);

        ExperimentModel result = experimentService.getExperiment(ctx, "exp-123");

        assertNotNull(result);
        assertEquals("testUser", result.getUserName());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
cd /Users/yasith/code/artisan/airavata && mvn test -pl airavata-api -Dtest="org.apache.airavata.service.experiment.ExperimentServiceTest" -DfailIfNoTests=false
```

Expected: compilation error — `ExperimentService` does not exist.

- [ ] **Step 3: Write ExperimentService with createExperiment and getExperiment**

```java
package org.apache.airavata.service.experiment;

import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentSummaryModel;
import org.apache.airavata.model.experiment.ExperimentSearchFields;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.model.status.ExperimentStatus;
import org.apache.airavata.registry.api.service.handler.RegistryServerHandler;
import org.apache.airavata.service.context.RequestContext;
import org.apache.airavata.service.exception.ServiceAuthorizationException;
import org.apache.airavata.service.exception.ServiceException;
import org.apache.airavata.service.exception.ServiceNotFoundException;
import org.apache.airavata.service.messaging.EventPublisher;
import org.apache.airavata.sharing.registry.models.Entity;
import org.apache.airavata.sharing.registry.models.SearchCriteria;
import org.apache.airavata.sharing.registry.models.EntitySearchField;
import org.apache.airavata.sharing.registry.models.SearchCondition;
import org.apache.airavata.sharing.registry.server.SharingRegistryServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExperimentService {

    private static final Logger logger = LoggerFactory.getLogger(ExperimentService.class);

    private final RegistryServerHandler registryHandler;
    private final SharingRegistryServerHandler sharingHandler;
    private final EventPublisher eventPublisher;

    public ExperimentService(
            RegistryServerHandler registryHandler,
            SharingRegistryServerHandler sharingHandler,
            EventPublisher eventPublisher) {
        this.registryHandler = registryHandler;
        this.sharingHandler = sharingHandler;
        this.eventPublisher = eventPublisher;
    }

    public String createExperiment(RequestContext ctx, ExperimentModel experiment) throws ServiceException {
        try {
            String experimentId = registryHandler.createExperiment(ctx.getGatewayId(), experiment);

            if (ServerSettings.isEnableSharing()) {
                try {
                    Entity entity = new Entity();
                    entity.setEntityId(experimentId);
                    String domainId = experiment.getGatewayId();
                    entity.setDomainId(domainId);
                    entity.setEntityTypeId(domainId + ":" + "EXPERIMENT");
                    entity.setOwnerId(experiment.getUserName() + "@" + domainId);
                    entity.setName(experiment.getExperimentName());
                    entity.setDescription(experiment.getDescription());
                    entity.setParentEntityId(experiment.getProjectId());
                    sharingHandler.createEntity(entity);
                } catch (Exception ex) {
                    logger.error("Rolling back experiment creation Exp ID : {}", experimentId, ex);
                    registryHandler.deleteExperiment(experimentId);
                    throw new ServiceException("Failed to create sharing registry record", ex);
                }
            }

            eventPublisher.publishExperimentStatus(experimentId, ctx.getGatewayId(), ExperimentState.CREATED);
            logger.info("Created new experiment with name {} and id {}",
                    experiment.getExperimentName(), experimentId);
            return experimentId;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error while creating the experiment: " + e.getMessage(), e);
        }
    }

    public ExperimentModel getExperiment(RequestContext ctx, String experimentId) throws ServiceException {
        try {
            ExperimentModel experiment = registryHandler.getExperiment(experimentId);
            if (experiment == null) {
                throw new ServiceNotFoundException("Experiment " + experimentId + " does not exist");
            }

            // Owner always has access
            if (ctx.getUserId().equals(experiment.getUserName())
                    && ctx.getGatewayId().equals(experiment.getGatewayId())) {
                return experiment;
            }

            // Check sharing permissions
            if (ServerSettings.isEnableSharing()) {
                String qualifiedUserId = ctx.getUserId() + "@" + ctx.getGatewayId();
                if (!sharingHandler.userHasAccess(
                        ctx.getGatewayId(), qualifiedUserId, experimentId, ctx.getGatewayId() + ":READ")) {
                    throw new ServiceAuthorizationException(
                            "User does not have permission to access this resource");
                }
                return experiment;
            }

            return null;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error while getting the experiment: " + e.getMessage(), e);
        }
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

```bash
cd /Users/yasith/code/artisan/airavata && mvn test -pl airavata-api -Dtest="org.apache.airavata.service.experiment.ExperimentServiceTest"
```

Expected: 2 tests PASS.

- [ ] **Step 5: Commit**

```bash
git add airavata-api/src/main/java/org/apache/airavata/service/experiment/ExperimentService.java \
       airavata-api/src/test/java/org/apache/airavata/service/experiment/ExperimentServiceTest.java
git commit -m "feat: add ExperimentService with createExperiment and getExperiment"
```

---

### Task 6: Add remaining ExperimentService methods

**Files:**
- Modify: `airavata-api/src/main/java/org/apache/airavata/service/experiment/ExperimentService.java`
- Modify: `airavata-api/src/test/java/org/apache/airavata/service/experiment/ExperimentServiceTest.java`

Adds `deleteExperiment`, `getExperimentByAdmin`, `searchExperiments`, `getExperimentStatus`, `getExperimentOutputs`, `terminateExperiment`, `cloneExperiment`. Business logic extracted from `AiravataServerHandler` lines 1487-1528, 1598-1617, 1134-1235, 1845-1854, 1858-1870, 2413-2453, 2250-2370.

- [ ] **Step 1: Write failing tests for new methods**

Add to `ExperimentServiceTest.java`:

```java
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.model.status.ExperimentStatus;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.service.exception.ServiceAuthorizationException;
import org.apache.airavata.service.exception.ServiceException;

import java.util.List;

// Add these test methods inside the class:

@Test
void deleteExperiment_onlyDeletesCreatedExperiments() throws Exception {
    ExperimentModel experiment = new ExperimentModel();
    experiment.setUserName("testUser");
    experiment.setGatewayId("testGateway");
    ExperimentStatus status = new ExperimentStatus();
    status.setState(ExperimentState.CREATED);
    experiment.addToExperimentStatus(status);

    when(registryHandler.getExperiment("exp-123")).thenReturn(experiment);
    when(registryHandler.deleteExperiment("exp-123")).thenReturn(true);

    boolean result = experimentService.deleteExperiment(ctx, "exp-123");

    assertTrue(result);
    verify(registryHandler).deleteExperiment("exp-123");
}

@Test
void deleteExperiment_rejectsNonCreatedExperiment() throws Exception {
    ExperimentModel experiment = new ExperimentModel();
    experiment.setUserName("testUser");
    experiment.setGatewayId("testGateway");
    ExperimentStatus status = new ExperimentStatus();
    status.setState(ExperimentState.EXECUTING);
    experiment.addToExperimentStatus(status);

    when(registryHandler.getExperiment("exp-123")).thenReturn(experiment);

    assertThrows(ServiceException.class,
            () -> experimentService.deleteExperiment(ctx, "exp-123"));
}

@Test
void getExperimentByAdmin_allowsSameGateway() throws Exception {
    ExperimentModel experiment = new ExperimentModel();
    experiment.setUserName("otherUser");
    experiment.setGatewayId("testGateway");

    when(registryHandler.getExperiment("exp-123")).thenReturn(experiment);

    ExperimentModel result = experimentService.getExperimentByAdmin(ctx, "exp-123");

    assertNotNull(result);
}

@Test
void getExperimentByAdmin_rejectsDifferentGateway() throws Exception {
    ExperimentModel experiment = new ExperimentModel();
    experiment.setUserName("otherUser");
    experiment.setGatewayId("otherGateway");

    when(registryHandler.getExperiment("exp-123")).thenReturn(experiment);

    assertThrows(ServiceAuthorizationException.class,
            () -> experimentService.getExperimentByAdmin(ctx, "exp-123"));
}

@Test
void getExperimentStatus_delegatesToRegistry() throws Exception {
    ExperimentStatus status = new ExperimentStatus();
    status.setState(ExperimentState.COMPLETED);
    when(registryHandler.getExperimentStatus("exp-123")).thenReturn(status);

    ExperimentStatus result = experimentService.getExperimentStatus(ctx, "exp-123");

    assertEquals(ExperimentState.COMPLETED, result.getState());
}

@Test
void getExperimentOutputs_delegatesToRegistry() throws Exception {
    List<OutputDataObjectType> outputs = List.of(new OutputDataObjectType());
    when(registryHandler.getExperimentOutputs("exp-123")).thenReturn(outputs);

    List<OutputDataObjectType> result = experimentService.getExperimentOutputs(ctx, "exp-123");

    assertEquals(1, result.size());
}
```

- [ ] **Step 2: Run tests to verify they fail**

```bash
cd /Users/yasith/code/artisan/airavata && mvn test -pl airavata-api -Dtest="org.apache.airavata.service.experiment.ExperimentServiceTest" -DfailIfNoTests=false
```

Expected: compilation errors — methods don't exist on `ExperimentService`.

- [ ] **Step 3: Add the methods to ExperimentService**

Add these methods to `ExperimentService.java`:

```java
public boolean deleteExperiment(RequestContext ctx, String experimentId) throws ServiceException {
    try {
        ExperimentModel experiment = registryHandler.getExperiment(experimentId);

        if (!ctx.getUserId().equals(experiment.getUserName())
                || !ctx.getGatewayId().equals(experiment.getGatewayId())) {
            if (ServerSettings.isEnableSharing()) {
                String qualifiedUserId = ctx.getUserId() + "@" + ctx.getGatewayId();
                if (!sharingHandler.userHasAccess(
                        ctx.getGatewayId(), qualifiedUserId, experimentId,
                        ctx.getGatewayId() + ":WRITE")) {
                    throw new ServiceAuthorizationException(
                            "User does not have permission to delete this resource");
                }
            }
        }

        if (experiment.getExperimentStatus().get(0).getState() != ExperimentState.CREATED) {
            throw new ServiceException(
                    "Experiment is not in CREATED state. Cannot be deleted. ID: " + experimentId);
        }

        return registryHandler.deleteExperiment(experimentId);
    } catch (ServiceException e) {
        throw e;
    } catch (Exception e) {
        throw new ServiceException("Error while deleting the experiment: " + e.getMessage(), e);
    }
}

public ExperimentModel getExperimentByAdmin(RequestContext ctx, String experimentId)
        throws ServiceException {
    try {
        ExperimentModel experiment = registryHandler.getExperiment(experimentId);
        if (ctx.getGatewayId().equals(experiment.getGatewayId())) {
            return experiment;
        }
        throw new ServiceAuthorizationException(
                "User does not have permission to access this resource");
    } catch (ServiceException e) {
        throw e;
    } catch (Exception e) {
        throw new ServiceException("Error while getting the experiment: " + e.getMessage(), e);
    }
}

public List<ExperimentSummaryModel> searchExperiments(
        RequestContext ctx, String gatewayId, String userName,
        Map<ExperimentSearchFields, String> filters, int limit, int offset)
        throws ServiceException {
    try {
        List<String> accessibleExpIds = new ArrayList<>();
        Map<ExperimentSearchFields, String> filtersCopy = new HashMap<>(filters);
        List<SearchCriteria> sharingFilters = new ArrayList<>();

        SearchCriteria entityTypeCriteria = new SearchCriteria();
        entityTypeCriteria.setSearchField(EntitySearchField.ENTITY_TYPE_ID);
        entityTypeCriteria.setSearchCondition(SearchCondition.EQUAL);
        entityTypeCriteria.setValue(gatewayId + ":EXPERIMENT");
        sharingFilters.add(entityTypeCriteria);

        if (filtersCopy.containsKey(ExperimentSearchFields.FROM_DATE)) {
            String fromTime = filtersCopy.remove(ExperimentSearchFields.FROM_DATE);
            SearchCriteria c = new SearchCriteria();
            c.setSearchField(EntitySearchField.CREATED_TIME);
            c.setSearchCondition(SearchCondition.GTE);
            c.setValue(fromTime);
            sharingFilters.add(c);
        }
        if (filtersCopy.containsKey(ExperimentSearchFields.TO_DATE)) {
            String toTime = filtersCopy.remove(ExperimentSearchFields.TO_DATE);
            SearchCriteria c = new SearchCriteria();
            c.setSearchField(EntitySearchField.CREATED_TIME);
            c.setSearchCondition(SearchCondition.LTE);
            c.setValue(toTime);
            sharingFilters.add(c);
        }
        if (filtersCopy.containsKey(ExperimentSearchFields.PROJECT_ID)) {
            String projectId = filtersCopy.remove(ExperimentSearchFields.PROJECT_ID);
            SearchCriteria c = new SearchCriteria();
            c.setSearchField(EntitySearchField.PARRENT_ENTITY_ID);
            c.setSearchCondition(SearchCondition.EQUAL);
            c.setValue(projectId);
            sharingFilters.add(c);
        }
        if (filtersCopy.containsKey(ExperimentSearchFields.USER_NAME)) {
            String username = filtersCopy.remove(ExperimentSearchFields.USER_NAME);
            SearchCriteria c = new SearchCriteria();
            c.setSearchField(EntitySearchField.OWNER_ID);
            c.setSearchCondition(SearchCondition.EQUAL);
            c.setValue(username + "@" + gatewayId);
            sharingFilters.add(c);
        }
        if (filtersCopy.containsKey(ExperimentSearchFields.EXPERIMENT_NAME)) {
            String name = filtersCopy.remove(ExperimentSearchFields.EXPERIMENT_NAME);
            SearchCriteria c = new SearchCriteria();
            c.setSearchField(EntitySearchField.NAME);
            c.setSearchCondition(SearchCondition.LIKE);
            c.setValue(name);
            sharingFilters.add(c);
        }
        if (filtersCopy.containsKey(ExperimentSearchFields.EXPERIMENT_DESC)) {
            String desc = filtersCopy.remove(ExperimentSearchFields.EXPERIMENT_DESC);
            SearchCriteria c = new SearchCriteria();
            c.setSearchField(EntitySearchField.DESCRIPTION);
            c.setSearchCondition(SearchCondition.LIKE);
            c.setValue(desc);
            sharingFilters.add(c);
        }

        int searchOffset = 0;
        int searchLimit = Integer.MAX_VALUE;
        boolean filteredInSharing = filtersCopy.isEmpty();
        if (filteredInSharing) {
            searchOffset = offset;
            searchLimit = limit;
        }

        sharingHandler.searchEntities(
                gatewayId, userName + "@" + gatewayId,
                sharingFilters, searchOffset, searchLimit)
                .forEach(e -> accessibleExpIds.add(e.getEntityId()));

        int finalOffset = filteredInSharing ? 0 : offset;
        return registryHandler.searchExperiments(
                gatewayId, userName, accessibleExpIds, filtersCopy, limit, finalOffset);
    } catch (Exception e) {
        throw new ServiceException("Error while searching experiments: " + e.getMessage(), e);
    }
}

public ExperimentStatus getExperimentStatus(RequestContext ctx, String experimentId)
        throws ServiceException {
    try {
        return registryHandler.getExperimentStatus(experimentId);
    } catch (Exception e) {
        throw new ServiceException(
                "Error while getting experiment status: " + e.getMessage(), e);
    }
}

public List<OutputDataObjectType> getExperimentOutputs(RequestContext ctx, String experimentId)
        throws ServiceException {
    try {
        return registryHandler.getExperimentOutputs(experimentId);
    } catch (Exception e) {
        throw new ServiceException(
                "Error while retrieving experiment outputs: " + e.getMessage(), e);
    }
}

public void terminateExperiment(RequestContext ctx, String experimentId)
        throws ServiceException {
    try {
        ExperimentModel experiment = registryHandler.getExperiment(experimentId);
        if (experiment == null) {
            throw new ServiceNotFoundException(
                    "Experiment " + experimentId + " does not exist");
        }
        ExperimentStatus status = registryHandler.getExperimentStatus(experimentId);
        switch (status.getState()) {
            case COMPLETED:
            case CANCELED:
            case FAILED:
            case CANCELING:
                logger.warn("Can't terminate already {} experiment",
                        status.getState().name());
                return;
            case CREATED:
                logger.warn("Experiment termination is only allowed for launched experiments.");
                return;
            default:
                eventPublisher.publishExperimentCancel(experimentId, ctx.getGatewayId());
                logger.debug("Cancelled experiment {}", experimentId);
        }
    } catch (ServiceException e) {
        throw e;
    } catch (Exception e) {
        throw new ServiceException(
                "Error while cancelling the experiment: " + e.getMessage(), e);
    }
}

public String cloneExperiment(RequestContext ctx, String existingExperimentId,
                               String newExperimentName, String newExperimentProjectId,
                               boolean adminMode) throws ServiceException {
    try {
        ExperimentModel existingExperiment;
        if (adminMode) {
            existingExperiment = getExperimentByAdmin(ctx, existingExperimentId);
        } else {
            existingExperiment = getExperiment(ctx, existingExperimentId);
        }

        if (existingExperiment == null) {
            throw new ServiceNotFoundException(
                    "Experiment " + existingExperimentId + " does not exist");
        }

        if (newExperimentProjectId != null) {
            existingExperiment.setProjectId(newExperimentProjectId);
        }

        // Verify write access to target project
        String qualifiedUserId = ctx.getUserId() + "@" + ctx.getGatewayId();
        if (!sharingHandler.userHasAccess(
                ctx.getGatewayId(), qualifiedUserId,
                existingExperiment.getProjectId(), ctx.getGatewayId() + ":WRITE")) {
            throw new ServiceAuthorizationException(
                    "User does not have permission to clone an experiment in this project");
        }

        existingExperiment.setCreationTime(System.currentTimeMillis());
        if (existingExperiment.getExecutionId() != null) {
            List<OutputDataObjectType> appOutputs =
                    registryHandler.getApplicationOutputs(existingExperiment.getExecutionId());
            existingExperiment.setExperimentOutputs(appOutputs);
        }
        if (newExperimentName != null && !newExperimentName.isEmpty()) {
            existingExperiment.setExperimentName(newExperimentName);
        }
        existingExperiment.unsetErrors();
        existingExperiment.unsetProcesses();
        existingExperiment.unsetExperimentStatus();

        return createExperiment(ctx, existingExperiment);
    } catch (ServiceException e) {
        throw e;
    } catch (Exception e) {
        throw new ServiceException(
                "Error while cloning experiment: " + e.getMessage(), e);
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

```bash
cd /Users/yasith/code/artisan/airavata && mvn test -pl airavata-api -Dtest="org.apache.airavata.service.experiment.ExperimentServiceTest"
```

Expected: 8 tests PASS (2 from Task 5 + 6 new).

- [ ] **Step 5: Commit**

```bash
git add airavata-api/src/main/java/org/apache/airavata/service/experiment/ExperimentService.java \
       airavata-api/src/test/java/org/apache/airavata/service/experiment/ExperimentServiceTest.java
git commit -m "feat: add remaining ExperimentService methods"
```

---

### Task 7: Implement ThriftAdapter

**Files:**
- Create: `airavata-api/src/main/java/org/apache/airavata/api/server/handler/ThriftAdapter.java`

- [ ] **Step 1: Write ThriftAdapter**

```java
package org.apache.airavata.api.server.handler;

import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.model.error.AiravataErrorType;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.model.error.AuthorizationException;
import org.apache.airavata.model.error.ExperimentNotFoundException;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.service.context.RequestContext;
import org.apache.airavata.service.exception.ServiceAuthorizationException;
import org.apache.airavata.service.exception.ServiceException;
import org.apache.airavata.service.exception.ServiceNotFoundException;

import java.util.Map;

public class ThriftAdapter {

    @FunctionalInterface
    public interface ServiceCall<T> {
        T apply(RequestContext ctx) throws Exception;
    }

    @FunctionalInterface
    public interface ServiceVoidCall {
        void apply(RequestContext ctx) throws Exception;
    }

    public static <T> T execute(AuthzToken authzToken, String gatewayId, ServiceCall<T> call)
            throws AiravataSystemException, AuthorizationException {
        try {
            RequestContext ctx = toRequestContext(authzToken, gatewayId);
            return call.apply(ctx);
        } catch (ServiceAuthorizationException e) {
            throw new AuthorizationException(e.getMessage());
        } catch (ServiceNotFoundException e) {
            throw new ExperimentNotFoundException(e.getMessage());
        } catch (ServiceException e) {
            AiravataSystemException ase = new AiravataSystemException();
            ase.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            ase.setMessage(e.getMessage());
            throw ase;
        } catch (AuthorizationException | AiravataSystemException e) {
            throw e;
        } catch (Exception e) {
            AiravataSystemException ase = new AiravataSystemException();
            ase.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            ase.setMessage(e.getMessage());
            throw ase;
        }
    }

    public static void executeVoid(AuthzToken authzToken, String gatewayId, ServiceVoidCall call)
            throws AiravataSystemException, AuthorizationException {
        execute(authzToken, gatewayId, ctx -> {
            call.apply(ctx);
            return null;
        });
    }

    private static RequestContext toRequestContext(AuthzToken authzToken, String gatewayId) {
        Map<String, String> claims = authzToken.getClaimsMap();
        String userId = claims.get(Constants.USER_NAME);
        String gw = claims.getOrDefault(Constants.GATEWAY_ID, gatewayId);
        return new RequestContext(userId, gw, authzToken.getAccessToken(), claims);
    }
}
```

- [ ] **Step 2: Verify compilation**

```bash
cd /Users/yasith/code/artisan/airavata && mvn compile -pl airavata-api
```

Expected: BUILD SUCCESS.

- [ ] **Step 3: Commit**

```bash
git add airavata-api/src/main/java/org/apache/airavata/api/server/handler/ThriftAdapter.java
git commit -m "feat: add ThriftAdapter for DRY exception translation"
```

---

### Task 8: Rewire AiravataServerHandler experiment methods

**Files:**
- Modify: `airavata-api/src/main/java/org/apache/airavata/api/server/handler/AiravataServerHandler.java`

The handler gains an `ExperimentService` field. Experiment methods become one-liner delegates via `ThriftAdapter`.

- [ ] **Step 1: Add ExperimentService field and update constructor**

Add import at the top of `AiravataServerHandler.java`:

```java
import org.apache.airavata.service.experiment.ExperimentService;
import org.apache.airavata.service.messaging.EventPublisher;
```

Add field after line 112 (`private Publisher experimentPublisher;`):

```java
private final ExperimentService experimentService;
```

In the 3-arg constructor (lines 118-137), after line 127 (`experimentPublisher = MessagingFactory.getPublisher(Type.EXPERIMENT_LAUNCH);`), add:

```java
EventPublisher eventPub = new EventPublisher(statusPublisher, experimentPublisher);
this.experimentService = new ExperimentService(registryHandler, sharingHandler, eventPub);
```

- [ ] **Step 2: Rewire createExperiment**

Replace the body of `createExperiment` (lines 1417-1472) with:

```java
@Override
@SecurityCheck
public String createExperiment(AuthzToken authzToken, String gatewayId, ExperimentModel experiment)
        throws InvalidRequestException, AiravataClientException, AiravataSystemException,
                AuthorizationException, TException {
    return ThriftAdapter.execute(authzToken, gatewayId,
            ctx -> experimentService.createExperiment(ctx, experiment));
}
```

- [ ] **Step 3: Rewire deleteExperiment**

Replace lines 1487-1528:

```java
@Override
@SecurityCheck
public boolean deleteExperiment(AuthzToken authzToken, String experimentId)
        throws InvalidRequestException, AiravataClientException, AiravataSystemException,
                AuthorizationException, TException {
    return ThriftAdapter.execute(authzToken, null,
            ctx -> experimentService.deleteExperiment(ctx, experimentId));
}
```

- [ ] **Step 4: Rewire getExperiment**

Replace lines 1554-1594:

```java
@Override
@SecurityCheck
public ExperimentModel getExperiment(AuthzToken authzToken, String airavataExperimentId)
        throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException,
                AiravataSystemException, AuthorizationException, TException {
    return ThriftAdapter.execute(authzToken, null,
            ctx -> experimentService.getExperiment(ctx, airavataExperimentId));
}
```

- [ ] **Step 5: Rewire getExperimentByAdmin**

Replace lines 1598-1617:

```java
@Override
@SecurityCheck
public ExperimentModel getExperimentByAdmin(AuthzToken authzToken, String airavataExperimentId)
        throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException,
                AiravataSystemException, AuthorizationException, TException {
    return ThriftAdapter.execute(authzToken, null,
            ctx -> experimentService.getExperimentByAdmin(ctx, airavataExperimentId));
}
```

- [ ] **Step 6: Rewire searchExperiments**

Replace lines 1134-1235:

```java
@Override
@SecurityCheck
public List<ExperimentSummaryModel> searchExperiments(
        AuthzToken authzToken, String gatewayId, String userName,
        Map<ExperimentSearchFields, String> filters, int limit, int offset)
        throws InvalidRequestException, AiravataClientException, AiravataSystemException,
                AuthorizationException, TException {
    return ThriftAdapter.execute(authzToken, gatewayId,
            ctx -> experimentService.searchExperiments(ctx, gatewayId, userName, filters, limit, offset));
}
```

- [ ] **Step 7: Rewire getExperimentStatus**

Replace lines 1845-1854:

```java
@Override
@SecurityCheck
public ExperimentStatus getExperimentStatus(AuthzToken authzToken, String airavataExperimentId)
        throws TException {
    return ThriftAdapter.execute(authzToken, null,
            ctx -> experimentService.getExperimentStatus(ctx, airavataExperimentId));
}
```

- [ ] **Step 8: Rewire getExperimentOutputs**

Replace lines 1858-1870:

```java
@Override
@SecurityCheck
public List<OutputDataObjectType> getExperimentOutputs(AuthzToken authzToken, String airavataExperimentId)
        throws AuthorizationException, TException {
    return ThriftAdapter.execute(authzToken, null,
            ctx -> experimentService.getExperimentOutputs(ctx, airavataExperimentId));
}
```

- [ ] **Step 9: Rewire terminateExperiment**

Replace lines 2413-2453:

```java
@Override
@SecurityCheck
public void terminateExperiment(AuthzToken authzToken, String airavataExperimentId, String gatewayId)
        throws TException {
    ThriftAdapter.executeVoid(authzToken, gatewayId,
            ctx -> experimentService.terminateExperiment(ctx, airavataExperimentId));
}
```

- [ ] **Step 10: Rewire cloneExperiment**

Replace lines 2250-2268:

```java
@Override
@SecurityCheck
public String cloneExperiment(
        AuthzToken authzToken, String existingExperimentID,
        String newExperimentName, String newExperimentProjectId)
        throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException,
                AiravataSystemException, AuthorizationException, ProjectNotFoundException, TException {
    return ThriftAdapter.execute(authzToken, null,
            ctx -> experimentService.cloneExperiment(
                    ctx, existingExperimentID, newExperimentName, newExperimentProjectId, false));
}
```

- [ ] **Step 11: Rewire cloneExperimentByAdmin**

Replace lines 2272-2290:

```java
@Override
@SecurityCheck
public String cloneExperimentByAdmin(
        AuthzToken authzToken, String existingExperimentID,
        String newExperimentName, String newExperimentProjectId)
        throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException,
                AiravataSystemException, AuthorizationException, ProjectNotFoundException, TException {
    return ThriftAdapter.execute(authzToken, null,
            ctx -> experimentService.cloneExperiment(
                    ctx, existingExperimentID, newExperimentName, newExperimentProjectId, true));
}
```

- [ ] **Step 12: Remove cloneExperimentInternal private method**

Delete the `cloneExperimentInternal` method (lines 2292-2370) since its logic now lives in `ExperimentService.cloneExperiment()`.

- [ ] **Step 13: Check if submit helpers can be removed**

```bash
grep -n "submitExperiment\|submitCancelExperiment\|submitExperimentIntermediateOutputsEvent" \
    airavata-api/src/main/java/org/apache/airavata/api/server/handler/AiravataServerHandler.java
```

If `submitExperiment` is only called from `launchExperiment` (which we haven't migrated yet — it's complex and has `getGroupResourceList` and `getApplicationInterface` dependencies), leave it for now. Same for `submitCancelExperiment` and `submitExperimentIntermediateOutputsEvent`. These can be cleaned up when `launchExperiment` is migrated.

- [ ] **Step 14: Verify compilation**

```bash
cd /Users/yasith/code/artisan/airavata && mvn compile -pl airavata-api
```

Expected: BUILD SUCCESS.

- [ ] **Step 15: Commit**

```bash
git add airavata-api/src/main/java/org/apache/airavata/api/server/handler/AiravataServerHandler.java
git commit -m "refactor: rewire AiravataServerHandler experiment methods to ExperimentService"
```

---

### Task 9: Run full test suite and verify

**Files:** None (verification only)

- [ ] **Step 1: Run all tests**

```bash
cd /Users/yasith/code/artisan/airavata && mvn test
```

Expected: All tests pass. The Thrift interface is unchanged — callers see no difference.

- [ ] **Step 2: Run service layer tests independently**

```bash
cd /Users/yasith/code/artisan/airavata && mvn test -pl airavata-api -Dtest="org.apache.airavata.service.**"
```

Expected: All ExperimentServiceTest and RequestContextTest pass.

- [ ] **Step 3: Fix and commit any test failures**

If tests fail, fix the issues and commit. If all tests pass, no commit needed.
