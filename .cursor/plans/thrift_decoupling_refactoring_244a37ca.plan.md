---
name: Thrift Decoupling Refactoring
overview: Systematically decouple the codebase from thrift-generated classes by creating domain models, moving thrift code to a separate module, and introducing mappers between domain and thrift models.
todos:
  - id: analyze-thrift-models
    content: Analyze all ~170 thrift-generated model classes to understand structure, fields, and relationships
    status: completed
  - id: create-domain-models
    content: Create ~170 domain model classes in org.apache.airavata.common.model.* (POJOs without thrift dependencies)
    status: completed
    dependencies:
      - analyze-thrift-models
  - id: create-domain-exceptions
    content: Create domain exception classes to replace thrift-generated exceptions
    status: completed
    dependencies:
      - analyze-thrift-models
  - id: create-domain-enums
    content: Extract and create domain enum classes from thrift models
    status: completed
    dependencies:
      - analyze-thrift-models
  - id: setup-mapper-infrastructure
    content: Set up MapStruct configuration and create mapper interface structure
    status: completed
    dependencies:
      - create-domain-models
  - id: create-mappers
    content: Create mapper interfaces for all model classes (domain ↔ thrift conversions)
    status: completed
    dependencies:
      - setup-mapper-infrastructure
  - id: create-thrift-api-module
    content: Create modules/thrift-api/ directory structure and pom.xml
    status: completed
  - id: configure-thrift-generation
    content: Configure thrift code generation in thrift-api module to output to org.apache.airavata.thriftapi.* namespace
    status: completed
    dependencies:
      - create-thrift-api-module
  - id: move-thrift-handlers
    content: Move all handler classes from airavata-api to thrift-api module and update packages
    status: completed
    dependencies:
      - create-thrift-api-module
  - id: move-thrift-server
    content: Move thrift server code to thrift-api module
    status: completed
    dependencies:
      - create-thrift-api-module
  - id: update-handlers-use-mappers
    content: Update all thrift handlers to use mappers for domain ↔ thrift conversions
    status: completed
    dependencies:
      - create-mappers
      - move-thrift-handlers
  - id: update-service-layer
    content: Update AiravataService and all domain services to use domain models instead of thrift models
    status: completed
    dependencies:
      - create-domain-models
  - id: update-utilities
    content: Update utility classes (ExperimentModelUtil, ProjectModelUtil, etc.) to use domain models
    status: pending
    dependencies:
      - create-domain-models
  - id: update-dependencies
    content: "Update pom.xml files: remove thrift generation from airavata-api, add thrift-api module, update dependencies"
    status: completed
    dependencies:
      - create-thrift-api-module
  - id: update-repositories
    content: Ensure repository/DAO layer works with domain models (update JPA entities if needed)
    status: pending
    dependencies:
      - create-domain-models
  - id: test-mappers
    content: Create unit tests for mappers to verify domain ↔ thrift conversions
    status: pending
    dependencies:
      - create-mappers
  - id: test-services
    content: Update and run service layer tests with domain models
    status: pending
    dependencies:
      - update-service-layer
  - id: test-thrift-server
    content: Test thrift server startup and client connections
    status: pending
    dependencies:
      - update-handlers-use-mappers
      - move-thrift-server
  - id: verify-compilation
    content: Verify airavata-api compiles without thrift dependencies and compilation time improves
    status: pending
    dependencies:
      - update-dependencies
      - update-service-layer
---

# Thrift Decoupling Refactoring Plan

## Overview

This plan decouples the codebase from thrift-generated classes to improve compilation speed. The refactoring will:

1. Create domain model classes (non-thrift) to replace direct thrift dependencies
2. Move all thrift-related code to `org.apache.airavata.thriftapi` namespace
3. Create a separate `thrift-api` module in `modules/` directory
4. Introduce mappers between domain models and thrift models
5. Update all services and handlers to use domain models with mappers

## Current State Analysis

- **Thrift-generated classes**: Located in `airavata-api/target/generated-sources/thrift/org/apache/airavata/common/model/` (~170 model classes)
- **Thrift service interfaces**: `org.apache.airavata.api.model.*` (Airavata, Workflow, etc.)
- **Thrift handlers**: `org.apache.airavata.api.thrift.handler.*` (implement thrift service interfaces)
- **Service layer**: `org.apache.airavata.service.*` directly uses thrift-generated model classes
- **Thrift server code**: `org.apache.airavata.api.thrift.server.*`

## Target State

- **Domain models**: `org.apache.airavata.common.model.*` (hand-written, non-thrift classes)
- **Thrift models**: `org.apache.airavata.thriftapi.model.*` (in new `thrift-api` module)
- **Thrift services**: `org.apache.airavata.thriftapi.service.*` (in new `thrift-api` module)
- **Thrift handlers**: `org.apache.airavata.thriftapi.handler.*` (in new `thrift-api` module)
- **Mappers**: `org.apache.airavata.thriftapi.mapper.*` (convert between domain and thrift models)
- **Service layer**: Uses domain models only, no thrift dependencies

## Implementation Plan

### Phase 1: Create Domain Model Classes

**Location**: `airavata-api/src/main/java/org/apache/airavata/common/model/`

1. **Analyze thrift-generated models** to understand structure

   - Review all ~170 classes in `target/generated-sources/thrift/org/apache/airavata/common/model/`
   - Document field types, relationships, and enums

2. **Create domain model classes** (one-to-one mapping with thrift models)

   - Start with core models: `ExperimentModel`, `Project`, `Gateway`, `ApplicationInterfaceDescription`, etc.
   - Use plain Java classes (POJOs) with:
     - Private fields with getters/setters
     - Constructors
     - `equals()`, `hashCode()`, `toString()` methods
     - No thrift dependencies
   - Preserve all fields and types from thrift models
   - Handle nested models (e.g., `ExperimentModel` contains `ProcessModel`)

3. **Create domain exception classes**

   - Move from `org.apache.airavata.common.exception.*` if they're thrift-generated
   - Or create new ones if needed: `AiravataException`, `InvalidRequestException`, etc.

4. **Create domain enum classes**

   - Extract enums from thrift models: `ExperimentState`, `JobState`, `DataType`, etc.

**Files to create**: ~170 domain model classes matching thrift structure

### Phase 2: Create Mapper Infrastructure

**Location**: `airavata-api/src/main/java/org/apache/airavata/thriftapi/mapper/`

1. **Set up MapStruct** (already in dependencies)

   - Create mapper interfaces using `@Mapper` annotation
   - Generate bidirectional mappers: domain ↔ thrift

2. **Create mapper interfaces**

   - `ExperimentModelMapper`: `ExperimentModel` ↔ `org.apache.airavata.thriftapi.model.ExperimentModel`
   - `ProjectMapper`: `Project` ↔ `org.apache.airavata.thriftapi.model.Project`
   - Create mappers for all ~170 model classes
   - Handle nested mappings (e.g., `ExperimentModel` → `ProcessModel` → `JobModel`)

3. **Handle special cases**

   - Enum conversions
   - List/Collection mappings
   - Null handling
   - Optional fields

**Key files**:

- `airavata-api/src/main/java/org/apache/airavata/thriftapi/mapper/ModelMapper.java` (main mapper interface)
- Individual mapper interfaces for each model type

### Phase 3: Create Thrift-API Module

**Location**: `modules/thrift-api/`

1. **Create module structure**
   ```
   modules/thrift-api/
   ├── pom.xml
   └── src/main/java/org/apache/airavata/thriftapi/
       ├── model/          (thrift-generated models)
       ├── service/        (thrift service interfaces)
       ├── handler/        (thrift service handlers)
       ├── server/         (thrift server setup)
       └── mapper/         (mappers - depends on airavata-api)
   ```

2. **Create `modules/thrift-api/pom.xml`**

   - Add as module in root `pom.xml`
   - Dependencies:
     - `airavata-api` (for domain models and mappers)
     - `libthrift`
     - Spring Boot (if handlers use Spring)

3. **Move thrift generation to new module**

   - Update Maven configuration to generate thrift classes in `thrift-api` module
   - Configure thrift plugin in `thrift-api/pom.xml`
   - Output: `modules/thrift-api/target/generated-sources/thrift/org/apache/airavata/thriftapi/model/`

4. **Update thrift IDL namespace** (if possible)

   - Modify `.thrift` files to generate classes in `org.apache.airavata.thriftapi.*`
   - Or use post-generation renaming/package relocation

### Phase 4: Move Thrift Code to Thrift-API Module

1. **Move thrift service interfaces**

   - From: `airavata-api/target/generated-sources/thrift/org/apache/airavata/api/model/`
   - To: `modules/thrift-api/target/generated-sources/thrift/org/apache/airavata/thriftapi/service/`

2. **Move thrift handlers**

   - From: `airavata-api/src/main/java/org/apache/airavata/api/thrift/handler/`
   - To: `modules/thrift-api/src/main/java/org/apache/airavata/thriftapi/handler/`
   - Update handlers to:
     - Use domain models from `airavata-api`
     - Use mappers to convert domain ↔ thrift
     - Call `AiravataService` with domain models

3. **Move thrift server code**

   - From: `airavata-api/src/main/java/org/apache/airavata/api/thrift/server/`
   - To: `modules/thrift-api/src/main/java/org/apache/airavata/thriftapi/server/`

4. **Move thrift client code** (if any)

   - From: `airavata-api/src/main/java/org/apache/airavata/api/thrift/client/`
   - To: `modules/thrift-api/src/main/java/org/apache/airavata/thriftapi/client/`

5. **Update package declarations**

   - Change all `org.apache.airavata.api.thrift.*` → `org.apache.airavata.thriftapi.*`
   - Update imports throughout moved code

### Phase 5: Update Service Layer

**Location**: `airavata-api/src/main/java/org/apache/airavata/service/`

1. **Update `AiravataService`**

   - Replace all `org.apache.airavata.common.model.*` imports (thrift) with domain model imports
   - Update method signatures to use domain models
   - Remove any thrift-specific code

2. **Update domain services**

   - `org.apache.airavata.service.experiment.ExperimentService`
   - `org.apache.airavata.service.project.ProjectService`
   - `org.apache.airavata.service.application.ApplicationService`
   - All other service classes

3. **Update repository/DAO layer**

   - Ensure repositories work with domain models
   - Update JPA entities if they reference thrift models

4. **Update utility classes**

   - `ExperimentModelUtil`, `ProjectModelUtil`, `AppInterfaceUtil`, etc.
   - Change to use domain models

### Phase 6: Update Thrift Handlers to Use Mappers

**Location**: `modules/thrift-api/src/main/java/org/apache/airavata/thriftapi/handler/`

1. **Update `AiravataServiceHandler`**
   ```java
   // Before: Direct thrift model usage
   public Gateway getGateway(AuthzToken authzToken, String gatewayId) {
       return airavataService.getGateway(gatewayId); // Returns thrift Gateway
   }
   
   // After: Use mapper
   public org.apache.airavata.thriftapi.model.Gateway getGateway(...) {
       Gateway domainGateway = airavataService.getGateway(gatewayId);
       return gatewayMapper.toThrift(domainGateway);
   }
   ```

2. **Update all handler methods**

   - Input: Convert thrift → domain using mapper
   - Call service with domain model
   - Output: Convert domain → thrift using mapper

3. **Update all other handlers**

   - `UserProfileServiceHandler`
   - `TenantProfileServiceHandler`
   - `SharingRegistryServerHandler`
   - `RegistryServiceHandler`
   - `OrchestratorServiceHandler`
   - `IamAdminServiceHandler`
   - `GroupManagerServiceHandler`
   - `CredentialServiceHandler`

### Phase 7: Update Dependencies and Build Configuration

1. **Update `airavata-api/pom.xml`**

   - Remove thrift code generation
   - Remove direct `libthrift` dependency (or make it provided/optional)
   - Add dependency on `thrift-api` module (for mappers)

2. **Update root `pom.xml`**

   - Add `modules/thrift-api` to modules list

3. **Update `modules/thrift-api/pom.xml`**

   - Configure thrift code generation
   - Add dependency on `airavata-api`
   - Configure MapStruct for mappers

4. **Update build order**

   - Ensure `airavata-api` compiles before `thrift-api` (for domain models)
   - `thrift-api` generates thrift classes, then compiles mappers

### Phase 8: Testing and Validation

1. **Unit tests**

   - Test mappers (domain ↔ thrift conversions)
   - Test service layer with domain models
   - Test handlers with mappers

2. **Integration tests**

   - Test thrift server startup
   - Test thrift client connections
   - Test end-to-end flows

3. **Compilation verification**

   - Ensure `airavata-api` compiles without thrift dependencies
   - Verify `thrift-api` module compiles correctly
   - Check that compilation time improves

## Key Files to Modify

### New Files to Create

- `modules/thrift-api/pom.xml`
- `modules/thrift-api/src/main/java/org/apache/airavata/thriftapi/**/*.java` (handlers, server)
- `airavata-api/src/main/java/org/apache/airavata/common/model/*.java` (~170 domain model classes)
- `airavata-api/src/main/java/org/apache/airavata/thriftapi/mapper/*.java` (mapper interfaces)

### Files to Move

- `airavata-api/src/main/java/org/apache/airavata/api/thrift/handler/*.java` → `modules/thrift-api/...`
- `airavata-api/src/main/java/org/apache/airavata/api/thrift/server/*.java` → `modules/thrift-api/...`
- `airavata-api/src/main/java/org/apache/airavata/api/thrift/client/*.java` → `modules/thrift-api/...`

### Files to Update

- `pom.xml` (root) - add `thrift-api` module
- `airavata-api/pom.xml` - remove thrift generation, update dependencies
- `airavata-api/src/main/java/org/apache/airavata/service/AiravataService.java` - use domain models
- All service classes in `org.apache.airavata.service.*`
- All utility classes using models

## Migration Strategy

**Big Bang Approach**: Create all domain models first, then migrate services and handlers.

1. **Week 1-2**: Create all ~170 domain model classes
2. **Week 3**: Create mapper infrastructure and mappers
3. **Week 4**: Create `thrift-api` module and move thrift code
4. **Week 5**: Update service layer to use domain models
5. **Week 6**: Update handlers to use mappers
6. **Week 7**: Testing and bug fixes

## Risks and Mitigations

1. **Risk**: Large number of models to migrate

   - **Mitigation**: Use code generation tools or scripts to generate initial domain model structure from thrift models

2. **Risk**: Breaking changes during migration

   - **Mitigation**: Comprehensive testing, incremental commits, feature flags if possible

3. **Risk**: Mapper complexity for nested models

   - **Mitigation**: Use MapStruct's automatic nested mapping, test thoroughly

4. **Risk**: Thrift namespace changes breaking external clients

   - **Mitigation**: Keep thrift service interfaces compatible, only change internal model packages

## Success Criteria

- ✅ `airavata-api` module compiles without thrift dependencies
- ✅ All services use domain models (no thrift imports)
- ✅ All thrift code isolated in `thrift-api` module
- ✅ Mappers correctly convert between domain and thrift models
- ✅ Compilation time reduced (fewer dependencies to compile)
- ✅ All tests pass
- ✅ Thrift server still works correctly