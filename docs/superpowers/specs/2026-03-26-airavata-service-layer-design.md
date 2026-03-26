# airavata-service: Transport-Agnostic Service Layer

## Goal

Extract business logic from Thrift handlers into a standalone `airavata-service` Maven module so that multiple transport layers (Thrift, REST, gRPC) can share the same business logic without duplication.

## Current State

- `AiravataServerHandler` (~6,800 lines) implements `Airavata.Iface` with business logic, auth checks, messaging, and exception translation mixed together.
- Other handlers (`RegistryServerHandler`, `SharingRegistryServerHandler`, `CredentialStoreServerHandler`, `OrchestratorServerHandler`, profile handlers) follow the same pattern.
- `AiravataServerHandler` composes other handlers via direct Java method calls.
- A clean repository layer already exists under `registry/core/repositories/`.

## Architecture

### Module Layout

```
airavata/
  airavata-service/                    # NEW
    pom.xml
    src/main/java/org/apache/airavata/service/
      context/RequestContext.java
      exception/ServiceException.java
      exception/AuthorizationException.java
      exception/NotFoundException.java
      experiment/ExperimentService.java
      registry/RegistryService.java
      sharing/SharingService.java
      credential/CredentialService.java
      orchestrator/OrchestratorService.java
      profile/UserProfileService.java
      profile/TenantProfileService.java
      profile/GroupManagerService.java
      profile/IamAdminService.java
      messaging/EventPublisher.java
  airavata-api/                        # EXISTING - becomes thin Thrift transport
  airavata-rest-server/                # FUTURE
  airavata-grpc-server/                # FUTURE
```

### Dependency Flow

```
airavata-thrift-server (airavata-api)  --\
airavata-rest-server (future)          ----> airavata-service --> registry-core (repositories)
airavata-grpc-server (future)          --/
```

### RequestContext

Transport-agnostic identity object. Each transport constructs it from its own auth mechanism.

```java
public class RequestContext {
    private String userId;
    private String gatewayId;
    private String accessToken;
    private Map<String, String> claims;

    public static RequestContext from(AuthzToken authzToken, String gatewayId) { ... }
    // Future: fromBearer(), fromGrpcContext()
}
```

### Service Classes

Concrete classes, no interfaces. Constructor-injected dependencies. Services call other services directly (same JVM).

```java
public class ExperimentService {
    private final ExperimentRepository experimentRepo;
    private final SharingService sharingService;
    private final EventPublisher eventPublisher;

    public ExperimentService(ExperimentRepository experimentRepo,
                             SharingService sharingService,
                             EventPublisher eventPublisher) { ... }

    public String createExperiment(RequestContext ctx, ExperimentModel experiment) { ... }
    public void deleteExperiment(RequestContext ctx, String experimentId) { ... }
    public ExperimentModel getExperiment(RequestContext ctx, String experimentId) { ... }
    // ... 1-1 mapping from handler methods
}
```

Service dependency graph:
```
ExperimentService   --> SharingService, RegistryService, EventPublisher
RegistryService     --> SharingService, EventPublisher
OrchestratorService --> RegistryService, CredentialService
```

### Exceptions

Services throw their own exception types:
- `ServiceException` — general errors
- `AuthorizationException` — permission denied
- `NotFoundException` — resource not found

Transport layers translate these to protocol-specific errors.

### EventPublisher

Wraps the current `MessagingFactory` / RabbitMQ publishing scattered across handlers into a single injectable dependency.

### Transport Adapters (DRY)

One utility class per transport eliminates repeated try/catch and RequestContext construction:

```java
public class ThriftAdapter {
    public static <T> T execute(AuthzToken authzToken, String gatewayId,
                                ServiceCall<T> call)
            throws AiravataSystemException, AuthorizationException {
        try {
            RequestContext ctx = RequestContext.from(authzToken, gatewayId);
            return call.apply(ctx);
        } catch (ServiceException e) {
            throw new AiravataSystemException(e.getMessage());
        } catch (org.apache.airavata.service.exception.AuthorizationException e) {
            throw new AuthorizationException(e.getMessage());
        }
    }

    @FunctionalInterface
    public interface ServiceCall<T> { T apply(RequestContext ctx) throws Exception; }
}
```

Handler methods become one-liners:
```java
@Override
public String createExperiment(AuthzToken token, String gatewayId,
                               ExperimentModel experiment)
        throws AiravataSystemException, AuthorizationException {
    return ThriftAdapter.execute(token, gatewayId,
        ctx -> experimentService.createExperiment(ctx, experiment));
}
```

### Auth

- `@SecurityCheck` annotation removed from handlers.
- Transport-specific middleware validates credentials and populates `RequestContext` before handler methods run.
- Services perform authorization checks (e.g., "does this user have access?") using `RequestContext`.

### Model Objects

Services use the existing Thrift-generated model classes (`ExperimentModel`, `ProjectModel`, etc.) as-is. They're plain Java POJOs. Replacing them with hand-written domain objects is a separate concern.

## Migration Strategy (Incremental)

1. **Create `airavata-service` module** with `RequestContext`, exception types, `EventPublisher`.
2. **Extract `ExperimentService`** from `AiravataServerHandler` experiment-related methods:
   - `createExperiment`, `deleteExperiment`, `getExperiment`, `getExperimentByAdmin`
   - `getExperimentList`, `searchExperiments`
   - `launchExperiment`, `cancelExperiment`, `cloneExperiment`
   - `getExperimentStatus`, `getExperimentOutputs`, `getIntermediateOutputs`
3. **Create `ThriftAdapter`** in `airavata-api`. Rewire handler experiment methods to delegate through adapter.
4. **Verify existing tests pass.** Thrift interface is unchanged.
5. **Repeat** for remaining domains: Registry, Sharing, Credential, Orchestrator, Profile services.
6. **REST and gRPC servers** come later as separate modules once the service layer is proven.
