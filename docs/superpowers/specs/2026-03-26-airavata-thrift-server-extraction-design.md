# Airavata Thrift Server Extraction Design

**Date:** 2026-03-26
**Status:** Draft

## Goal

Extract all thrift server concerns from `airavata-api` into a new `airavata-thrift-server` Maven module. Introduce protobuf DTOs as the canonical data types in the service layer, making `airavata-api` completely thrift-free. MapStruct mappers in `airavata-thrift-server` bridge proto and thrift types at the handler boundary.

## Repository Layout (Post-Extraction)

```
airavata/
├── airavata-thrift-server/             # NEW — all thrift concerns
│   ├── pom.xml
│   └── src/main/
│       ├── java/
│       │   └── org/apache/airavata/
│       │       ├── server/             # AiravataServer.java (TMultiplexedProcessor bootstrap)
│       │       ├── handler/            # All 9 thrift handlers
│       │       └── mapper/             # MapStruct mappers (proto <-> thrift)
│       └── thrift/                     # Moved from thrift-interface-descriptions/
│           ├── airavata-apis/
│           ├── base-api/
│           ├── data-models/
│           ├── service-cpis/
│           └── stubs_java.thrift (etc.)
│
├── airavata-api/                       # SLIMMED — services + background processes
│   ├── pom.xml
│   └── src/main/
│       ├── java/
│       │   └── org/apache/airavata/
│       │       ├── service/            # Business logic services
│       │       ├── repository/         # JPA data access
│       │       └── background/         # Helix, monitors, event managers, etc.
│       └── proto/                      # NEW — .proto files mirroring thrift IDL
│           └── org/apache/airavata/
│               ├── model/
│               ├── api/
│               ├── registry/
│               ├── credential/
│               ├── sharing/
│               └── ...
│
├── modules/                            # Unchanged
└── pom.xml                             # Root — adds airavata-thrift-server module
```

## Dependency Graph

```
airavata-thrift-server
├── depends on: airavata-api (service layer + proto generated classes)
├── depends on: libthrift (Apache Thrift runtime)
├── owns: thrift IDL compilation -> generated thrift Java classes
├── owns: MapStruct mappers (proto <-> thrift)
└── owns: TMultiplexedProcessor, TThreadPoolServer bootstrap

airavata-api
├── depends on: protobuf-java (for generated proto classes)
├── owns: service layer (o.a.a.service.*)
├── owns: repository / data access layer
├── owns: background services (Helix, monitors, event managers)
├── owns: proto definitions + protoc generation
└── NO thrift dependency
```

## Thrift Services Moved (All 9)

| Service Name        | Handler Class                      |
|---------------------|------------------------------------|
| Airavata            | AiravataServerHandler              |
| RegistryService     | RegistryServerHandler              |
| SharingRegistry     | SharingRegistryServerHandler       |
| CredentialStore     | CredentialStoreServerHandler       |
| UserProfile         | UserProfileServiceHandler          |
| TenantProfile       | TenantProfileServiceHandler        |
| IamAdminServices    | IamAdminServicesHandler            |
| GroupManager        | GroupManagerServiceHandler          |
| Orchestrator        | OrchestratorServerHandler          |

## What Stays in `airavata-api`

### Services (refactored to proto types)
All 14 business logic services: ExperimentService, ApplicationCatalogService, CredentialService, GatewayService, GatewayResourceProfileService, GroupResourceProfileService, NotificationService, ParserService, ProjectService, ResourceService, UserResourceProfileService, DataProductService, ResourceSharingService, SSHAccountService.

### Data Access
JPA entities, repositories, Hibernate config, database initialization (ExpCatalogDB, AppCatalogDB, ReplicaCatalogDB, WorkflowCatalogDB, CredentialStoreDB, UserProfileCatalogDB).

### Background Services (retains its own main entry point)
- DB Event Manager
- Monitoring Server (Prometheus on port 9097)
- Cluster Status Monitor
- Data Interpreter
- Process Rescheduler
- Helix Controller/Participant
- Workflow Managers (Pre/Post/Parser)
- Job Monitors (Email + Real-time)

### Removed from `airavata-api`
- `AiravataServer.java`
- All 9 thrift handler classes
- Thrift IDL files
- `libthrift` dependency
- Thrift code generation Maven plugin config

## Proto File Strategy

Proto files mirror thrift IDL 1:1 — same fields, same nesting, same enums. Mechanical translation, not a redesign.

### Conventions
- Field numbers match thrift field IDs where possible
- Proto uses `snake_case` per protobuf convention; MapStruct handles naming differences
- No proto service definitions — only message types (no gRPC yet)
- Package structure mirrors thrift: `org.apache.airavata.model.*`, `org.apache.airavata.api.*`, etc.
- Generated via protoc Maven plugin in `airavata-api`

### Example

Thrift:
```thrift
struct ExperimentModel {
    1: string experimentId,
    2: string projectId,
    3: string gatewayId,
    4: ExperimentType experimentType,
    5: string userName,
    6: string experimentName,
    7: i64 creationTime
}
```

Proto:
```protobuf
message ExperimentModel {
    string experiment_id = 1;
    string project_id = 2;
    string gateway_id = 3;
    ExperimentType experiment_type = 4;
    string user_name = 5;
    string experiment_name = 6;
    int64 creation_time = 7;
}
```

## Handler Conversion Pattern

```
Thrift Client
  -> TMultiplexedProcessor (airavata-thrift-server)
    -> ThriftHandler.method(ThriftRequest)
      -> MapStruct: ThriftRequest -> ProtoDTO
        -> Service.method(ProtoDTO)          (airavata-api)
        -> returns ProtoResult
      -> MapStruct: ProtoResult -> ThriftResponse
    -> returns ThriftResponse
```

### Example — createExperiment

```java
// In airavata-thrift-server: AiravataServerHandler
public String createExperiment(AuthzToken authzToken, String gatewayId, ExperimentModel experiment) {
    var protoExperiment = ExperimentMapper.INSTANCE.toProto(experiment);
    var protoAuthz = AuthzTokenMapper.INSTANCE.toProto(authzToken);
    var result = experimentService.createExperiment(protoAuthz, gatewayId, protoExperiment);
    return result;
}
```

### MapStruct Mapper

```java
@Mapper
public interface ExperimentMapper {
    ExperimentMapper INSTANCE = Mappers.getMapper(ExperimentMapper.class);
    ExperimentProto toProto(ExperimentModel thriftModel);
    ExperimentModel toThrift(ExperimentProto proto);
}
```

## Build Order

1. `airavata-api` builds first — compiles proto files, produces service classes + proto generated code
2. `airavata-thrift-server` builds second — compiles thrift IDL, compiles handlers + mappers against airavata-api

## Development

Work is done in a git worktree off the main `airavata` repo for isolation. Thrift IDL content is unchanged (no SDK breakage). Thrift spec changes are a separate future effort.
