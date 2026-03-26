# Airavata Thrift Server Extraction Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Extract all thrift server concerns from `airavata-api` into a new `airavata-thrift-server` Maven module, introduce proto DTOs in `airavata-api`, and set up MapStruct mapping at the thrift handler boundary.

**Architecture:** `airavata-thrift-server` depends on `airavata-api`. Thrift IDL lives in `airavata-thrift-server/src/main/thrift/`. Server bootstrap (AiravataServer), the main API handler (AiravataServerHandler), profile handlers, OrchestratorServerHandler, ThriftAdapter, and Constants move to `airavata-thrift-server`. Data-access handlers (RegistryServerHandler, SharingRegistryServerHandler, CredentialStoreServerHandler) stay in `airavata-api` because services depend on them directly — making `airavata-api` thrift-free requires extracting their data access logic, which is deferred to a follow-up. Thrift code generation temporarily stays in `airavata-api` to avoid circular dependencies (thrift-server needs generated code from api, and api needs nothing from thrift-server). Proto files are introduced in `airavata-api/src/main/proto/` as the canonical data model.

**Tech Stack:** Maven, Apache Thrift 0.22.0, Protocol Buffers 4.30.1, MapStruct 1.6.3, protobuf-maven-plugin 3.10.2

**Spec:** `docs/superpowers/specs/2026-03-26-airavata-thrift-server-extraction-design.md`

---

## File Structure

### New files (airavata-thrift-server)

| File | Responsibility |
|------|---------------|
| `airavata-thrift-server/pom.xml` | Module POM — depends on airavata-api, libthrift, MapStruct |
| `airavata-thrift-server/src/main/thrift/**` | Moved from `thrift-interface-descriptions/` — all IDL files |
| `airavata-thrift-server/src/main/java/org/apache/airavata/api/server/AiravataServer.java` | Moved — thrift server bootstrap |
| `airavata-thrift-server/src/main/java/org/apache/airavata/api/server/util/Constants.java` | Moved — server config constants |
| `airavata-thrift-server/src/main/java/org/apache/airavata/api/server/handler/AiravataServerHandler.java` | Moved — main API thrift handler |
| `airavata-thrift-server/src/main/java/org/apache/airavata/api/server/handler/ThriftAdapter.java` | Moved — thrift error translation |
| `airavata-thrift-server/src/main/java/org/apache/airavata/service/profile/handlers/UserProfileServiceHandler.java` | Moved |
| `airavata-thrift-server/src/main/java/org/apache/airavata/service/profile/handlers/TenantProfileServiceHandler.java` | Moved |
| `airavata-thrift-server/src/main/java/org/apache/airavata/service/profile/handlers/IamAdminServicesHandler.java` | Moved |
| `airavata-thrift-server/src/main/java/org/apache/airavata/service/profile/handlers/GroupManagerServiceHandler.java` | Moved |
| `airavata-thrift-server/src/main/java/org/apache/airavata/orchestrator/server/OrchestratorServerHandler.java` | Moved |

### New files (airavata-api — proto)

| File | Responsibility |
|------|---------------|
| `airavata-api/src/main/proto/org/apache/airavata/model/workspace/workspace.proto` | Proto mirror of workspace_model.thrift |
| `airavata-api/src/main/proto/org/apache/airavata/model/experiment/experiment.proto` | Proto mirror of experiment_model.thrift |
| `airavata-api/src/main/proto/org/apache/airavata/model/status/status.proto` | Proto mirror of status_models.thrift |
| ... (one proto per thrift data-model file — 28 total) | Mechanical 1:1 translation |
| `airavata-api/src/main/proto/org/apache/airavata/model/security/security.proto` | Proto mirror of security_model.thrift |
| `airavata-api/src/main/proto/org/apache/airavata/model/error/errors.proto` | Proto mirror of airavata_errors.thrift |
| `airavata-api/src/main/proto/org/apache/airavata/model/commons/commons.proto` | Proto mirror of airavata_commons.thrift |

### Modified files

| File | Change |
|------|--------|
| `pom.xml` (root) | Add `airavata-thrift-server` module before `airavata-api` in module order |
| `airavata-api/pom.xml` | Update thrift compiler path, add protobuf-java + protobuf-maven-plugin, add MapStruct deps |

### Deleted files (from airavata-api)

| File | Reason |
|------|--------|
| `airavata-api/src/main/java/org/apache/airavata/api/server/AiravataServer.java` | Moved |
| `airavata-api/src/main/java/org/apache/airavata/api/server/util/Constants.java` | Moved |
| `airavata-api/src/main/java/org/apache/airavata/api/server/handler/AiravataServerHandler.java` | Moved |
| `airavata-api/src/main/java/org/apache/airavata/api/server/handler/ThriftAdapter.java` | Moved |
| `airavata-api/src/main/java/org/apache/airavata/service/profile/handlers/UserProfileServiceHandler.java` | Moved |
| `airavata-api/src/main/java/org/apache/airavata/service/profile/handlers/TenantProfileServiceHandler.java` | Moved |
| `airavata-api/src/main/java/org/apache/airavata/service/profile/handlers/IamAdminServicesHandler.java` | Moved |
| `airavata-api/src/main/java/org/apache/airavata/service/profile/handlers/GroupManagerServiceHandler.java` | Moved |
| `airavata-api/src/main/java/org/apache/airavata/orchestrator/server/OrchestratorServerHandler.java` | Moved |
| `thrift-interface-descriptions/` | Moved to airavata-thrift-server/src/main/thrift/ |

---

### Task 1: Create git worktree

**Files:**
- None (git operation)

- [ ] **Step 1: Create worktree branch**

```bash
cd /Users/yasith/code/artisan/airavata
git worktree add ../airavata-thrift-extraction -b feat/thrift-server-extraction
```

- [ ] **Step 2: Verify worktree**

```bash
cd /Users/yasith/code/artisan/airavata-thrift-extraction
git branch --show-current
```

Expected: `feat/thrift-server-extraction`

All subsequent tasks run inside `/Users/yasith/code/artisan/airavata-thrift-extraction/`.

---

### Task 2: Create airavata-thrift-server Maven module

**Files:**
- Create: `airavata-thrift-server/pom.xml`
- Modify: `pom.xml` (root)

- [ ] **Step 1: Create module directory**

```bash
mkdir -p airavata-thrift-server/src/main/java
```

- [ ] **Step 2: Write airavata-thrift-server/pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">

  <modelVersion>4.0.0</modelVersion>

  <parent>
    <groupId>org.apache.airavata</groupId>
    <artifactId>airavata</artifactId>
    <version>0.21-SNAPSHOT</version>
    <relativePath>../pom.xml</relativePath>
  </parent>

  <artifactId>airavata-thrift-server</artifactId>
  <packaging>jar</packaging>
  <name>Airavata Thrift Server</name>

  <dependencies>
    <dependency>
      <groupId>org.apache.airavata</groupId>
      <artifactId>airavata-api</artifactId>
      <version>${project.version}</version>
    </dependency>
    <dependency>
      <groupId>org.apache.thrift</groupId>
      <artifactId>libthrift</artifactId>
    </dependency>
    <dependency>
      <groupId>org.slf4j</groupId>
      <artifactId>slf4j-api</artifactId>
    </dependency>
  </dependencies>
</project>
```

- [ ] **Step 3: Add module to root pom.xml**

In `pom.xml`, add `airavata-thrift-server` AFTER `airavata-api` in the `<modules>` block (it depends on airavata-api):

```xml
    <modules>
        <module>airavata-api</module>
        <module>airavata-thrift-server</module>
        <module>modules/file-server</module>
        <module>modules/agent-framework/agent-service</module>
        <module>modules/research-framework/research-service</module>
        <module>modules/restproxy</module>
        <module>modules/ide-integration</module>
    </modules>
```

- [ ] **Step 4: Verify module resolves**

```bash
mvn validate -pl airavata-thrift-server -am --quiet
```

Expected: BUILD SUCCESS (no compilation yet, just POM validation)

- [ ] **Step 5: Commit**

```bash
git add airavata-thrift-server/pom.xml pom.xml
git commit -m "feat: add airavata-thrift-server Maven module scaffold"
```

---

### Task 3: Move thrift IDL files

**Files:**
- Move: `thrift-interface-descriptions/` → `airavata-thrift-server/src/main/thrift/`
- Modify: `airavata-api/pom.xml` (update thrift compiler path)

- [ ] **Step 1: Move the directory**

```bash
git mv thrift-interface-descriptions airavata-thrift-server/src/main/thrift
```

- [ ] **Step 2: Update thrift compiler path in airavata-api/pom.xml**

In `airavata-api/pom.xml`, find the `maven-antrun-plugin` execution `generate-thrift-sources` and change the thrift input path from:

```xml
<arg value="${project.basedir}/../thrift-interface-descriptions/stubs_java.thrift" />
```

to:

```xml
<arg value="${project.basedir}/../airavata-thrift-server/src/main/thrift/stubs_java.thrift" />
```

- [ ] **Step 3: Verify thrift compilation still works**

```bash
mvn generate-sources -pl airavata-api --quiet
ls airavata-api/target/generated-sources/thrift/org/apache/airavata/api/Airavata.java
```

Expected: file exists (thrift compiler found the moved IDL and generated code)

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "refactor: move thrift IDL to airavata-thrift-server/src/main/thrift"
```

---

### Task 4: Move server bootstrap to airavata-thrift-server

**Files:**
- Move: `airavata-api/src/main/java/org/apache/airavata/api/server/AiravataServer.java` → `airavata-thrift-server/src/main/java/org/apache/airavata/api/server/AiravataServer.java`
- Move: `airavata-api/src/main/java/org/apache/airavata/api/server/util/Constants.java` → `airavata-thrift-server/src/main/java/org/apache/airavata/api/server/util/Constants.java`

- [ ] **Step 1: Create target directories**

```bash
mkdir -p airavata-thrift-server/src/main/java/org/apache/airavata/api/server/util
```

- [ ] **Step 2: Move AiravataServer.java**

```bash
git mv airavata-api/src/main/java/org/apache/airavata/api/server/AiravataServer.java \
      airavata-thrift-server/src/main/java/org/apache/airavata/api/server/AiravataServer.java
```

- [ ] **Step 3: Move Constants.java**

```bash
git mv airavata-api/src/main/java/org/apache/airavata/api/server/util/Constants.java \
      airavata-thrift-server/src/main/java/org/apache/airavata/api/server/util/Constants.java
```

Note: `Constants.java` contains `API_SERVER_PORT`, `API_SERVER_HOST`, `API_SERVER_MIN_THREADS` — used only by `AiravataServer.java`. The `ThriftAdapter.java` imports `org.apache.airavata.common.utils.Constants` (a different class in commons), NOT this one.

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "refactor: move AiravataServer and Constants to airavata-thrift-server"
```

---

### Task 5: Move AiravataServerHandler and ThriftAdapter

**Files:**
- Move: `airavata-api/src/main/java/org/apache/airavata/api/server/handler/AiravataServerHandler.java` → `airavata-thrift-server/...`
- Move: `airavata-api/src/main/java/org/apache/airavata/api/server/handler/ThriftAdapter.java` → `airavata-thrift-server/...`

- [ ] **Step 1: Create target directory**

```bash
mkdir -p airavata-thrift-server/src/main/java/org/apache/airavata/api/server/handler
```

- [ ] **Step 2: Move both files**

```bash
git mv airavata-api/src/main/java/org/apache/airavata/api/server/handler/AiravataServerHandler.java \
      airavata-thrift-server/src/main/java/org/apache/airavata/api/server/handler/AiravataServerHandler.java

git mv airavata-api/src/main/java/org/apache/airavata/api/server/handler/ThriftAdapter.java \
      airavata-thrift-server/src/main/java/org/apache/airavata/api/server/handler/ThriftAdapter.java
```

No import changes needed — packages stay the same, and `airavata-thrift-server` depends on `airavata-api` which provides all service classes and handler classes they reference.

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "refactor: move AiravataServerHandler and ThriftAdapter to airavata-thrift-server"
```

---

### Task 6: Move profile handlers and OrchestratorServerHandler

**Files:**
- Move: `airavata-api/src/main/java/org/apache/airavata/service/profile/handlers/UserProfileServiceHandler.java`
- Move: `airavata-api/src/main/java/org/apache/airavata/service/profile/handlers/TenantProfileServiceHandler.java`
- Move: `airavata-api/src/main/java/org/apache/airavata/service/profile/handlers/IamAdminServicesHandler.java`
- Move: `airavata-api/src/main/java/org/apache/airavata/service/profile/handlers/GroupManagerServiceHandler.java`
- Move: `airavata-api/src/main/java/org/apache/airavata/orchestrator/server/OrchestratorServerHandler.java`

- [ ] **Step 1: Create target directories**

```bash
mkdir -p airavata-thrift-server/src/main/java/org/apache/airavata/service/profile/handlers
mkdir -p airavata-thrift-server/src/main/java/org/apache/airavata/orchestrator/server
```

- [ ] **Step 2: Move profile handlers**

```bash
git mv airavata-api/src/main/java/org/apache/airavata/service/profile/handlers/UserProfileServiceHandler.java \
      airavata-thrift-server/src/main/java/org/apache/airavata/service/profile/handlers/UserProfileServiceHandler.java

git mv airavata-api/src/main/java/org/apache/airavata/service/profile/handlers/TenantProfileServiceHandler.java \
      airavata-thrift-server/src/main/java/org/apache/airavata/service/profile/handlers/TenantProfileServiceHandler.java

git mv airavata-api/src/main/java/org/apache/airavata/service/profile/handlers/IamAdminServicesHandler.java \
      airavata-thrift-server/src/main/java/org/apache/airavata/service/profile/handlers/IamAdminServicesHandler.java

git mv airavata-api/src/main/java/org/apache/airavata/service/profile/handlers/GroupManagerServiceHandler.java \
      airavata-thrift-server/src/main/java/org/apache/airavata/service/profile/handlers/GroupManagerServiceHandler.java
```

- [ ] **Step 3: Move OrchestratorServerHandler**

```bash
git mv airavata-api/src/main/java/org/apache/airavata/orchestrator/server/OrchestratorServerHandler.java \
      airavata-thrift-server/src/main/java/org/apache/airavata/orchestrator/server/OrchestratorServerHandler.java
```

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "refactor: move profile handlers and OrchestratorServerHandler to airavata-thrift-server"
```

---

### Task 7: Verify full build compiles

**Files:**
- Possibly modify: `airavata-thrift-server/pom.xml` (if missing deps surface)

- [ ] **Step 1: Build airavata-api (should still compile — only lost handler classes it no longer owns)**

```bash
mvn compile -pl airavata-api --quiet 2>&1 | tail -5
```

Expected: BUILD SUCCESS. If any code in `airavata-api` still imports the moved handler classes, identify the files and update imports or remove dead references.

- [ ] **Step 2: Build airavata-thrift-server**

```bash
mvn compile -pl airavata-thrift-server -am 2>&1 | tail -20
```

Expected: BUILD SUCCESS. If compilation fails due to missing dependencies, add them to `airavata-thrift-server/pom.xml`. Likely candidates:
- Profile service CPI constants (thrift generated — already available via airavata-api dependency)
- Helix/ZooKeeper classes used by OrchestratorServerHandler
- Messaging classes used by AiravataServerHandler
- Monitoring/background service classes used by AiravataServer

For each missing dependency, add it to `airavata-thrift-server/pom.xml`:

```xml
<!-- Add as needed based on compilation errors -->
<dependency>
  <groupId>org.apache.helix</groupId>
  <artifactId>helix-core</artifactId>
</dependency>
<dependency>
  <groupId>io.prometheus</groupId>
  <artifactId>simpleclient</artifactId>
</dependency>
<!-- etc. -->
```

- [ ] **Step 3: Fix any compilation errors**

Iterate: read error → add dependency or fix import → rebuild. Continue until BUILD SUCCESS for both modules.

- [ ] **Step 4: Run tests**

```bash
mvn test -pl airavata-api --quiet 2>&1 | tail -5
```

Expected: Tests pass (no behavioral changes, only file moves).

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "fix: resolve airavata-thrift-server compilation dependencies"
```

---

### Task 8: Create proto files — workspace model (representative example)

**Files:**
- Create: `airavata-api/src/main/proto/org/apache/airavata/model/workspace/workspace.proto`

This task demonstrates the proto translation pattern. All subsequent proto files follow the same rules.

**Translation rules (thrift → proto):**
- `namespace java X` → `option java_package = "X"; option java_outer_classname = "XProtos";`
- `struct` → `message`
- `enum` → `enum` (add `UNKNOWN = 0` sentinel as proto3 requires 0-value)
- `required/optional` qualifiers are dropped (proto3 has no required/optional)
- Field numbers match thrift field IDs
- `string` → `string`, `i32` → `int32`, `i64` → `int64`, `bool` → `bool`, `double` → `double`, `binary` → `bytes`
- `list<T>` → `repeated T`
- `map<K,V>` → `map<K,V>`
- `set<T>` → `repeated T` (proto has no set; enforce uniqueness in application code)
- Cross-file references: use `import` and fully qualified message names

- [ ] **Step 1: Create directory**

```bash
mkdir -p airavata-api/src/main/proto/org/apache/airavata/model/workspace
```

- [ ] **Step 2: Write workspace.proto**

```protobuf
syntax = "proto3";

package org.apache.airavata.model.workspace;

option java_package = "org.apache.airavata.model.workspace";
option java_outer_classname = "WorkspaceProtos";
option java_multiple_files = true;

import "org/apache/airavata/model/commons/commons.proto";

message Group {
    string group_name = 1;
    string description = 2;
}

message Project {
    string project_id = 1;
    string owner = 2;
    string gateway_id = 3;
    string name = 4;
    string description = 5;
    int64 creation_time = 6;
    repeated string shared_users = 7;
    repeated string shared_groups = 8;
}

message User {
    string airavata_internal_user_id = 1;
    string user_name = 2;
    string gateway_id = 3;
    string first_name = 4;
    string last_name = 5;
    string email = 6;
}

enum GatewayApprovalStatus {
    GATEWAY_APPROVAL_STATUS_UNKNOWN = 0;
    REQUESTED = 1;
    APPROVED = 2;
    ACTIVE = 3;
    DEACTIVATED = 4;
    CANCELLED = 5;
    DENIED = 6;
    CREATED = 7;
    DEPLOYED = 8;
}

message Gateway {
    string airavata_internal_gateway_id = 1;
    string gateway_id = 2;
    GatewayApprovalStatus gateway_approval_status = 3;
    string gateway_name = 4;
    string domain = 5;
    string email_address = 6;
    string gateway_acronym = 7;
    string gateway_url = 8;
    string gateway_public_abstract = 9;
    string review_proposal_description = 10;
    string gateway_admin_first_name = 11;
    string gateway_admin_last_name = 12;
    string gateway_admin_email = 13;
    string identity_server_user_name = 14;
    string identity_server_password_token = 15;
    string declined_reason = 16;
    string oauth_client_id = 17;
    string oauth_client_secret = 18;
    int64 request_creation_time = 19;
    string requester_username = 20;
}

message GatewayUsageReportingCommand {
    string gateway_id = 1;
    string compute_resource_id = 2;
    string command = 3;
}

enum NotificationPriority {
    NOTIFICATION_PRIORITY_UNKNOWN = 0;
    LOW = 1;
    NORMAL = 2;
    HIGH = 3;
}

message Notification {
    string notification_id = 1;
    string gateway_id = 2;
    string title = 3;
    string notification_message = 4;
    int64 creation_time = 5;
    int64 published_time = 6;
    int64 expiration_time = 7;
    NotificationPriority priority = 8;
}
```

- [ ] **Step 3: Commit**

```bash
git add airavata-api/src/main/proto/
git commit -m "feat: add workspace proto definitions"
```

---

### Task 9: Create proto files — commons and security

**Files:**
- Create: `airavata-api/src/main/proto/org/apache/airavata/model/commons/commons.proto`
- Create: `airavata-api/src/main/proto/org/apache/airavata/model/security/security.proto`

These are foundational protos referenced by many others.

- [ ] **Step 1: Create directories**

```bash
mkdir -p airavata-api/src/main/proto/org/apache/airavata/model/commons
mkdir -p airavata-api/src/main/proto/org/apache/airavata/model/security
```

- [ ] **Step 2: Write commons.proto**

Read `airavata-thrift-server/src/main/thrift/airavata-apis/airavata_commons.thrift` to get the exact constants and types. Translate to:

```protobuf
syntax = "proto3";

package org.apache.airavata.model.commons;

option java_package = "org.apache.airavata.model.commons";
option java_outer_classname = "CommonsProtos";
option java_multiple_files = true;

// DEFAULT_ID is a compile-time constant in thrift; in proto it becomes a convention.
// Consumers should use "DO_NOT_SET_AT_CLIENTS" as the default for ID fields.

message ValidationResults {
    bool validation_state = 1;
    repeated ValidationResult validation_result_list = 2;
}

message ValidationResult {
    bool result = 1;
    string error_details = 2;
}
```

- [ ] **Step 3: Write security.proto**

Read `airavata-thrift-server/src/main/thrift/airavata-apis/security_model.thrift` and translate:

```protobuf
syntax = "proto3";

package org.apache.airavata.model.security;

option java_package = "org.apache.airavata.model.security";
option java_outer_classname = "SecurityProtos";
option java_multiple_files = true;

message AuthzToken {
    string access_token = 1;
    map<string, string> claims_map = 2;
}
```

- [ ] **Step 4: Commit**

```bash
git add airavata-api/src/main/proto/
git commit -m "feat: add commons and security proto definitions"
```

---

### Task 10: Create proto files — status and experiment models

**Files:**
- Create: `airavata-api/src/main/proto/org/apache/airavata/model/status/status.proto`
- Create: `airavata-api/src/main/proto/org/apache/airavata/model/experiment/experiment.proto`

- [ ] **Step 1: Create directories**

```bash
mkdir -p airavata-api/src/main/proto/org/apache/airavata/model/status
mkdir -p airavata-api/src/main/proto/org/apache/airavata/model/experiment
```

- [ ] **Step 2: Write status.proto**

Read `airavata-thrift-server/src/main/thrift/data-models/status_models.thrift` and translate all enums/structs. Example structure:

```protobuf
syntax = "proto3";

package org.apache.airavata.model.status;

option java_package = "org.apache.airavata.model.status";
option java_outer_classname = "StatusProtos";
option java_multiple_files = true;

enum ExperimentState {
    EXPERIMENT_STATE_UNKNOWN = 0;
    CREATED = 1;
    VALIDATED = 2;
    SCHEDULED = 3;
    LAUNCHED = 4;
    EXECUTING = 5;
    CANCELING = 6;
    CANCELED = 7;
    COMPLETED = 8;
    FAILED = 9;
}

message ExperimentStatus {
    ExperimentState state = 1;
    int64 time_of_state_change = 2;
    string reason = 3;
    int64 status_id = 4;
}

enum JobState {
    JOB_STATE_UNKNOWN = 0;
    SUBMITTED = 1;
    QUEUED = 2;
    ACTIVE = 3;
    COMPLETE = 4;
    CANCELED = 5;
    FAILED = 6;
    SUSPENDED = 7;
    NON_CRITICAL_FAIL = 8;
}

message JobStatus {
    JobState job_state = 1;
    int64 time_of_state_change = 2;
    string reason = 3;
    int64 status_id = 4;
}

enum ProcessState {
    PROCESS_STATE_UNKNOWN = 0;
    CREATED = 1;
    VALIDATED = 2;
    STARTED = 3;
    PRE_PROCESSING = 4;
    CONFIGURING_WORKSPACE = 5;
    INPUT_DATA_STAGING = 6;
    EXECUTING = 7;
    MONITORING = 8;
    OUTPUT_DATA_STAGING = 9;
    POST_PROCESSING = 10;
    COMPLETED = 11;
    FAILED = 12;
    CANCELLING = 13;
    CANCELED = 14;
}

message ProcessStatus {
    ProcessState state = 1;
    int64 time_of_state_change = 2;
    string reason = 3;
    int64 status_id = 4;
}

enum TaskState {
    TASK_STATE_UNKNOWN = 0;
    CREATED = 1;
    EXECUTING = 2;
    COMPLETED = 3;
    FAILED = 4;
    CANCELED = 5;
}

message TaskStatus {
    TaskState state = 1;
    int64 time_of_state_change = 2;
    string reason = 3;
    int64 status_id = 4;
}

message QueueStatusModel {
    string host_name = 1;
    string queue_name = 2;
    bool queue_up = 3;
    int32 running_jobs = 4;
    int32 queued_jobs = 5;
    int64 time = 6;
}
```

- [ ] **Step 3: Write experiment.proto**

Read `airavata-thrift-server/src/main/thrift/data-models/experiment_model.thrift` and translate. Use imports for cross-references:

```protobuf
syntax = "proto3";

package org.apache.airavata.model.experiment;

option java_package = "org.apache.airavata.model.experiment";
option java_outer_classname = "ExperimentProtos";
option java_multiple_files = true;

import "org/apache/airavata/model/commons/commons.proto";
import "org/apache/airavata/model/status/status.proto";
// import other model protos as needed for cross-references

enum ExperimentType {
    EXPERIMENT_TYPE_UNKNOWN = 0;
    SINGLE_APPLICATION = 1;
    WORKFLOW = 2;
}

enum ExperimentSearchFields {
    EXPERIMENT_SEARCH_FIELDS_UNKNOWN = 0;
    EXPERIMENT_NAME = 1;
    EXPERIMENT_DESC = 2;
    APPLICATION_ID = 3;
    FROM_DATE = 4;
    TO_DATE = 5;
    STATUS = 6;
    PROJECT_ID = 7;
    USER_NAME = 8;
    JOB_ID = 9;
}

enum ProjectSearchFields {
    PROJECT_SEARCH_FIELDS_UNKNOWN = 0;
    PROJECT_NAME = 1;
    PROJECT_DESCRIPTION = 2;
}

// Continue translating all structs from experiment_model.thrift:
// UserConfigurationDataModel, ExperimentModel, ExperimentSummaryModel, ExperimentStatistics
// Follow the same field-number and naming conventions as workspace.proto
```

- [ ] **Step 4: Commit**

```bash
git add airavata-api/src/main/proto/
git commit -m "feat: add status and experiment proto definitions"
```

---

### Task 11: Create remaining proto files

**Files:**
- Create: one `.proto` file per thrift data-model file (approximately 25 more)

This is mechanical translation work. For each thrift file in `airavata-thrift-server/src/main/thrift/data-models/`, create a corresponding proto file following the exact pattern from Tasks 8-10.

- [ ] **Step 1: Create all remaining proto files**

Complete checklist of thrift files to translate (check off each):

| Thrift File | Proto Output Path |
|-------------|------------------|
| `account_provisioning_model.thrift` | `org/apache/airavata/model/appcatalog/accountprovisioning/account_provisioning.proto` |
| `application_deployment_model.thrift` | `org/apache/airavata/model/appcatalog/appdeployment/app_deployment.proto` |
| `application_interface_model.thrift` | `org/apache/airavata/model/appcatalog/appinterface/app_interface.proto` |
| `application_io_models.thrift` | `org/apache/airavata/model/application/io/application_io.proto` |
| `compute_resource_model.thrift` | `org/apache/airavata/model/appcatalog/computeresource/compute_resource.proto` |
| `credential_store_models.thrift` | `org/apache/airavata/model/credential/store/credential_store.proto` |
| `data_movement_models.thrift` | `org/apache/airavata/model/data/movement/data_movement.proto` |
| `gateway_groups_model.thrift` | `org/apache/airavata/model/appcatalog/gatewaygroups/gateway_groups.proto` |
| `gateway_resource_profile_model.thrift` | `org/apache/airavata/model/appcatalog/gatewayprofile/gateway_profile.proto` |
| `group_manager_model.thrift` | `org/apache/airavata/model/group/group_manager.proto` |
| `group_resource_profile_model.thrift` | `org/apache/airavata/model/appcatalog/groupresourceprofile/group_resource_profile.proto` |
| `job_model.thrift` | `org/apache/airavata/model/job/job.proto` |
| `parallelism_model.thrift` | `org/apache/airavata/model/parallelism/parallelism.proto` |
| `parser_model.thrift` | `org/apache/airavata/model/appcatalog/parser/parser.proto` |
| `process_model.thrift` | `org/apache/airavata/model/process/process.proto` |
| `replica_catalog_models.thrift` | `org/apache/airavata/model/data/replica/replica_catalog.proto` |
| `scheduling_model.thrift` | `org/apache/airavata/model/scheduling/scheduling.proto` |
| `sharing_models.thrift` | `org/apache/airavata/model/sharing/sharing.proto` |
| `storage_resource_model.thrift` | `org/apache/airavata/model/appcatalog/storageresource/storage_resource.proto` |
| `task_model.thrift` | `org/apache/airavata/model/task/task.proto` |
| `tenant_profile_model.thrift` | `org/apache/airavata/model/tenant/tenant_profile.proto` |
| `user_profile_model.thrift` | `org/apache/airavata/model/user/user_profile.proto` |
| `user_resource_profile_model.thrift` | `org/apache/airavata/model/appcatalog/userresourceprofile/user_resource_profile.proto` |
| `workflow_data_model.thrift` | `org/apache/airavata/model/workflow/data/workflow_data.proto` |
| `workflow_model.thrift` | `org/apache/airavata/model/workflow/workflow.proto` |

Also translate API-level thrift files:

| Thrift File | Proto Output Path |
|-------------|------------------|
| `airavata_errors.thrift` | `org/apache/airavata/model/error/errors.proto` |
| `db_event_model.thrift` | `org/apache/airavata/model/dbevent/db_event.proto` |
| `messaging_events.thrift` | `org/apache/airavata/model/messaging/messaging_events.proto` |

For each file:
1. Read the thrift file
2. Create the proto file following translation rules from Task 8
3. Match field numbers to thrift field IDs
4. Add `UNKNOWN = 0` sentinel to every enum
5. Use `import` for cross-file references
6. Set `java_package` matching the thrift `namespace java`

- [ ] **Step 2: Commit**

```bash
git add airavata-api/src/main/proto/
git commit -m "feat: add all remaining proto definitions mirroring thrift IDL"
```

---

### Task 12: Set up protoc compilation in airavata-api

**Files:**
- Modify: `airavata-api/pom.xml`

- [ ] **Step 1: Add protobuf-java dependency**

In `airavata-api/pom.xml` `<dependencies>` section, add:

```xml
    <dependency>
      <groupId>com.google.protobuf</groupId>
      <artifactId>protobuf-java</artifactId>
    </dependency>
```

- [ ] **Step 2: Add protobuf-maven-plugin**

In `airavata-api/pom.xml` `<build><plugins>` section, add the protobuf-maven-plugin (already in root POM's pluginManagement):

```xml
      <plugin>
        <groupId>io.github.ascopes</groupId>
        <artifactId>protobuf-maven-plugin</artifactId>
        <executions>
          <execution>
            <id>generate-proto-sources</id>
            <goals>
              <goal>generate</goal>
            </goals>
            <configuration>
              <sourceDirectories>
                <sourceDirectory>${project.basedir}/src/main/proto</sourceDirectory>
              </sourceDirectories>
            </configuration>
          </execution>
        </executions>
      </plugin>
```

- [ ] **Step 3: Verify proto compilation**

```bash
mvn generate-sources -pl airavata-api --quiet 2>&1 | tail -10
ls airavata-api/target/generated-sources/protobuf/java/org/apache/airavata/model/workspace/
```

Expected: generated Java files for the proto messages (Project.java, Gateway.java, etc.)

- [ ] **Step 4: Full compile**

```bash
mvn compile -pl airavata-api --quiet 2>&1 | tail -5
```

Expected: BUILD SUCCESS — both thrift generated code and proto generated code compile.

- [ ] **Step 5: Commit**

```bash
git add airavata-api/pom.xml
git commit -m "build: add protobuf-java dependency and protoc compilation to airavata-api"
```

---

### Task 13: Add MapStruct to airavata-thrift-server

**Files:**
- Modify: `airavata-thrift-server/pom.xml`
- Modify: `pom.xml` (root — add MapStruct to dependency management)

- [ ] **Step 1: Add MapStruct to root pom.xml dependency management**

In `pom.xml` root, add to `<dependencyManagement><dependencies>`:

```xml
            <dependency>
                <groupId>org.mapstruct</groupId>
                <artifactId>mapstruct</artifactId>
                <version>1.6.3</version>
            </dependency>
            <dependency>
                <groupId>org.mapstruct</groupId>
                <artifactId>mapstruct-processor</artifactId>
                <version>1.6.3</version>
            </dependency>
```

- [ ] **Step 2: Add MapStruct dependency to airavata-thrift-server/pom.xml**

Add to `<dependencies>`:

```xml
    <dependency>
      <groupId>org.mapstruct</groupId>
      <artifactId>mapstruct</artifactId>
    </dependency>
```

Add to `<build><plugins>`:

```xml
    <build>
      <plugins>
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-compiler-plugin</artifactId>
          <configuration>
            <annotationProcessorPaths>
              <path>
                <groupId>org.mapstruct</groupId>
                <artifactId>mapstruct-processor</artifactId>
                <version>1.6.3</version>
              </path>
            </annotationProcessorPaths>
          </configuration>
        </plugin>
      </plugins>
    </build>
```

- [ ] **Step 3: Verify build**

```bash
mvn compile -pl airavata-thrift-server -am --quiet 2>&1 | tail -5
```

Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add pom.xml airavata-thrift-server/pom.xml
git commit -m "build: add MapStruct dependency to airavata-thrift-server"
```

---

### Task 14: Create representative MapStruct mapper

**Files:**
- Create: `airavata-thrift-server/src/main/java/org/apache/airavata/api/server/mapper/WorkspaceMapper.java`

This establishes the mapper pattern. Full mapper coverage is built incrementally as services are converted to proto types (follow-up work).

- [ ] **Step 1: Create mapper directory**

```bash
mkdir -p airavata-thrift-server/src/main/java/org/apache/airavata/api/server/mapper
```

- [ ] **Step 2: Write WorkspaceMapper.java**

```java
package org.apache.airavata.api.server.mapper;

import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Notification;
import org.apache.airavata.model.workspace.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Maps between thrift-generated workspace types and proto-generated workspace types.
 *
 * <p>Thrift types use camelCase field accessors (getGatewayId()).
 * Proto types use snake_case with standard proto getters (getGatewayId()).
 * MapStruct handles the mapping automatically when accessor names match.
 */
@Mapper
public interface WorkspaceMapper {
    WorkspaceMapper INSTANCE = Mappers.getMapper(WorkspaceMapper.class);

    // --- Project ---
    org.apache.airavata.model.workspace.ProjectProtos.Project toProto(Project thrift);
    Project toThrift(org.apache.airavata.model.workspace.ProjectProtos.Project proto);

    // --- Gateway ---
    org.apache.airavata.model.workspace.WorkspaceProtos.Gateway toProtoGateway(Gateway thrift);
    Gateway toThriftGateway(org.apache.airavata.model.workspace.WorkspaceProtos.Gateway proto);
}
```

Note: The exact proto class names depend on how protoc generates them (based on `java_multiple_files`, `java_outer_classname`). Adjust class references after verifying proto compilation output from Task 12.

- [ ] **Step 3: Verify compilation**

```bash
mvn compile -pl airavata-thrift-server -am --quiet 2>&1 | tail -10
```

If proto class names don't match, check `airavata-api/target/generated-sources/` for exact class names and update mapper imports.

- [ ] **Step 4: Commit**

```bash
git add airavata-thrift-server/src/main/java/org/apache/airavata/api/server/mapper/
git commit -m "feat: add representative MapStruct mapper for workspace types"
```

---

### Task 15: Final build verification and cleanup

**Files:**
- None (verification only)

- [ ] **Step 1: Full project build**

```bash
mvn clean compile -T4 2>&1 | tail -20
```

Expected: BUILD SUCCESS for all modules

- [ ] **Step 2: Run tests**

```bash
mvn test -pl airavata-api,airavata-thrift-server 2>&1 | tail -20
```

Expected: All tests pass

- [ ] **Step 3: Verify no remaining handler references in airavata-api**

```bash
grep -r "AiravataServer\b" airavata-api/src/main/java/ --include="*.java" | grep -v "import"
grep -r "AiravataServerHandler\b" airavata-api/src/main/java/ --include="*.java" | grep -v "import"
```

Expected: No matches (these classes have moved). If there are stale references, remove them.

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "chore: final cleanup after thrift server extraction"
```

---

## Deferred Work (Future Plans)

These items are explicitly out of scope for this plan but are required for full thrift removal from `airavata-api`:

1. **Extract data access from Registry/Sharing/Credential handlers** — Create non-thrift interfaces in `airavata-api`, refactor services to use them, move handler implementations to `airavata-thrift-server`
2. **Convert service signatures to proto types** — Change all 14 service classes to accept/return proto types instead of thrift types
3. **Move thrift code generation to airavata-thrift-server** — Once `airavata-api` no longer imports any thrift types
4. **Remove libthrift dependency from airavata-api** — Final step after all thrift references are eliminated
5. **Full MapStruct mapper coverage** — One mapper per domain (experiment, credential, sharing, etc.)
