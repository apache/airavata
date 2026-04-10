# gRPC CRUD + Python SDK Completeness — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Complete CRUD coverage at the gRPC layer and bring the Python SDK to full parity with all user-facing gRPC services.

**Architecture:** Add missing RPC definitions to existing proto files, implement thin gRPC handler methods delegating to existing service layer, regenerate Python proto stubs, add SDK client classes following existing patterns.

**Tech Stack:** Protobuf/gRPC, Java 17 (handlers), Python 3.10+ (SDK clients), grpcio-tools (stub generation)

---

## Part 1: Java gRPC CRUD Gap Fixes

### Task 1: Add missing DataProduct/Replica CRUD RPCs

The `DataProductInterface` and `DataReplicaLocationInterface` already have full CRUD methods. We just need proto definitions + handler wiring.

**Files:**
- Modify: `airavata-api/research-service/src/main/proto/data_product_service.proto`
- Modify: `airavata-api/research-service/src/main/java/org/apache/airavata/research/grpc/DataProductGrpcService.java`

- [ ] **Step 1: Add 5 new RPC definitions to data_product_service.proto**

Add these RPCs to the `DataProductService` service block, with HTTP annotations following the existing pattern:

```protobuf
  // Update an existing data product
  rpc UpdateDataProduct(UpdateDataProductRequest) returns (google.protobuf.Empty) {
    option (google.api.http) = {
      put: "/api/v1/data-products/{product_uri}"
      body: "data_product"
    };
  }

  // Delete a data product
  rpc DeleteDataProduct(DeleteDataProductRequest) returns (google.protobuf.Empty) {
    option (google.api.http) = {
      delete: "/api/v1/data-products/{product_uri}"
    };
  }

  // Get a specific replica location
  rpc GetReplicaLocation(GetReplicaLocationRequest) returns (org.apache.airavata.model.data.replica.proto.DataReplicaLocationModel) {
    option (google.api.http) = {
      get: "/api/v1/data-products/replicas/{replica_id}"
    };
  }

  // Update a replica location
  rpc UpdateReplicaLocation(UpdateReplicaLocationRequest) returns (google.protobuf.Empty) {
    option (google.api.http) = {
      put: "/api/v1/data-products/replicas/{replica_id}"
      body: "replica_location"
    };
  }

  // Delete a replica location
  rpc DeleteReplicaLocation(DeleteReplicaLocationRequest) returns (google.protobuf.Empty) {
    option (google.api.http) = {
      delete: "/api/v1/data-products/replicas/{replica_id}"
    };
  }
```

Add the corresponding request messages:

```protobuf
message UpdateDataProductRequest {
  string product_uri = 1;
  org.apache.airavata.model.data.replica.proto.DataProductModel data_product = 2;
}

message DeleteDataProductRequest {
  string product_uri = 1;
}

message GetReplicaLocationRequest {
  string replica_id = 1;
}

message UpdateReplicaLocationRequest {
  string replica_id = 1;
  org.apache.airavata.model.data.replica.proto.DataReplicaLocationModel replica_location = 2;
}

message DeleteReplicaLocationRequest {
  string replica_id = 1;
}
```

- [ ] **Step 2: Add 5 handler methods to DataProductGrpcService.java**

Follow the existing pattern (try-catch + GrpcStatusMapper). The service uses `storageRegistry` which implements both `DataProductInterface` and `DataReplicaLocationInterface`:

```java
@Override
public void updateDataProduct(UpdateDataProductRequest request, StreamObserver<Empty> responseObserver) {
    try {
        storageRegistry.updateDataProduct(request.getDataProduct());
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    } catch (Exception e) {
        responseObserver.onError(GrpcStatusMapper.mapToGrpcException(e));
    }
}

@Override
public void deleteDataProduct(DeleteDataProductRequest request, StreamObserver<Empty> responseObserver) {
    try {
        storageRegistry.removeDataProduct(request.getProductUri());
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    } catch (Exception e) {
        responseObserver.onError(GrpcStatusMapper.mapToGrpcException(e));
    }
}

@Override
public void getReplicaLocation(GetReplicaLocationRequest request, StreamObserver<DataReplicaLocationModel> responseObserver) {
    try {
        DataReplicaLocationModel replica = storageRegistry.getReplicaLocation(request.getReplicaId());
        responseObserver.onNext(replica);
        responseObserver.onCompleted();
    } catch (Exception e) {
        responseObserver.onError(GrpcStatusMapper.mapToGrpcException(e));
    }
}

@Override
public void updateReplicaLocation(UpdateReplicaLocationRequest request, StreamObserver<Empty> responseObserver) {
    try {
        storageRegistry.updateReplicaLocation(request.getReplicaLocation());
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    } catch (Exception e) {
        responseObserver.onError(GrpcStatusMapper.mapToGrpcException(e));
    }
}

@Override
public void deleteReplicaLocation(DeleteReplicaLocationRequest request, StreamObserver<Empty> responseObserver) {
    try {
        storageRegistry.removeReplicaLocation(request.getReplicaId());
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    } catch (Exception e) {
        responseObserver.onError(GrpcStatusMapper.mapToGrpcException(e));
    }
}
```

Add necessary imports: `com.google.protobuf.Empty`, `DataReplicaLocationModel`.

- [ ] **Step 3: Build and test**

```bash
cd airavata-api && mvn compile -T4 && mvn test -T4
```

- [ ] **Step 4: Commit**

```bash
git add -A && git commit -m "feat: add missing DataProduct/Replica CRUD RPCs to data_product_service"
```

---

### Task 2: Add missing GroupResourceProfile Create RPCs

The `GroupResourceProfileService` Java service already has create methods via `createGroupResourceProfile` which handles sub-resources. Check if separate create RPCs for preferences/policies are needed or if they're created as part of the group profile.

**Files:**
- Modify: `airavata-api/compute-service/src/main/proto/group_resource_profile_service.proto`
- Modify: `airavata-api/compute-service/src/main/java/org/apache/airavata/compute/grpc/GroupResourceProfileGrpcService.java`

- [ ] **Step 1: Check if create operations exist in the service layer**

Read `GroupResourceProfileService.java` to determine if standalone create methods exist for:
- GroupComputeResourcePreference
- ComputeResourcePolicy
- BatchQueueResourcePolicy

If they're only created as part of `createGroupResourceProfile()` / `updateGroupResourceProfile()` (embedded in the parent profile), then standalone create RPCs may not be needed — the update profile RPC covers adding new preferences/policies. In that case, skip adding new RPCs and document this in the commit message.

If standalone create methods DO exist, add proto RPCs following the same pattern as Task 1.

- [ ] **Step 2: Add RPCs if needed (same pattern as Task 1)**

For each needed create operation, add:
1. RPC definition with HTTP POST annotation
2. Request message type
3. Handler method delegating to service layer

- [ ] **Step 3: Build and test**

```bash
cd airavata-api && mvn compile -T4 && mvn test -T4
```

- [ ] **Step 4: Commit**

```bash
git add -A && git commit -m "feat: add missing GroupResourceProfile preference/policy Create RPCs"
```

---

## Part 2: Python SDK Completeness

### Task 3: Regenerate Python proto stubs

The proto files were updated in Tasks 1-2. Python stubs need regeneration.

**Files:**
- Modify: All files in `airavata-python-sdk/airavata_sdk/generated/services/`
- Modify: All files in `airavata-python-sdk/airavata_sdk/generated/org/`

- [ ] **Step 1: Add proto generation target to Makefile**

Add a `proto` target to `airavata-python-sdk/Makefile`:

```makefile
PROTO_SRC:=$(abspath $(dir $(lastword $(MAKEFILE_LIST)))/../airavata-api)
PROTO_OUT:=$(OUTDIR)/airavata_sdk/generated

proto:
	python3 -m grpc_tools.protoc \
		-I$(PROTO_SRC)/src/main/proto \
		-I$(PROTO_SRC)/agent-service/src/main/proto \
		-I$(PROTO_SRC)/compute-service/src/main/proto \
		-I$(PROTO_SRC)/credential-service/src/main/proto \
		-I$(PROTO_SRC)/iam-service/src/main/proto \
		-I$(PROTO_SRC)/research-service/src/main/proto \
		-I$(PROTO_SRC)/sharing-service/src/main/proto \
		-I$(PROTO_SRC)/storage-service/src/main/proto \
		-I$(PROTO_SRC)/orchestration-service/src/main/proto \
		--python_out=$(PROTO_OUT) \
		--grpc_python_out=$(PROTO_OUT) \
		--pyi_out=$(PROTO_OUT) \
		$(PROTO_SRC)/*/src/main/proto/*.proto \
		$(PROTO_SRC)/src/main/proto/org/apache/airavata/model/**/*.proto
```

- [ ] **Step 2: Run proto generation**

```bash
cd airavata-python-sdk && pip install grpcio-tools && make proto
```

If the Makefile approach doesn't work due to import path complexity, generate stubs manually per service:

```bash
cd airavata-python-sdk
python3 -m grpc_tools.protoc \
  -I../airavata-api/src/main/proto \
  -I../airavata-api/research-service/src/main/proto \
  --python_out=airavata_sdk/generated/services \
  --grpc_python_out=airavata_sdk/generated/services \
  --pyi_out=airavata_sdk/generated/services \
  ../airavata-api/research-service/src/main/proto/data_product_service.proto
```

Repeat for each service proto that was modified or needs new stubs.

- [ ] **Step 3: Verify new stubs exist**

Check that the 7 missing service stub files now exist:
```bash
ls airavata_sdk/generated/services/agent_service_pb2_grpc.py
ls airavata_sdk/generated/services/experiment_management_service_pb2_grpc.py
ls airavata_sdk/generated/services/research_service_pb2_grpc.py  # Contains ResearchHub/Project/Resource/Session
```

Note: agent-service.proto defines AgentInteractionService + PlanService in one file. research-service.proto defines ResearchHub/Project/Resource/Session services.

- [ ] **Step 4: Commit**

```bash
git add -A && git commit -m "build: regenerate Python proto stubs for updated and new services"
```

---

### Task 4: Add 7 new stub factories to transport/utils.py

**Files:**
- Modify: `airavata-python-sdk/airavata_sdk/transport/utils.py`

- [ ] **Step 1: Add stub factory functions**

Add these functions after the existing factories, following the exact same pattern:

```python
def create_agent_interaction_service_stub(channel: grpc.Channel):
    from airavata_sdk.generated.services import agent_service_pb2_grpc
    return agent_service_pb2_grpc.AgentInteractionServiceStub(channel)

def create_plan_service_stub(channel: grpc.Channel):
    from airavata_sdk.generated.services import agent_service_pb2_grpc
    return agent_service_pb2_grpc.PlanServiceStub(channel)

def create_experiment_management_service_stub(channel: grpc.Channel):
    from airavata_sdk.generated.services import experiment_management_service_pb2_grpc
    return experiment_management_service_pb2_grpc.ExperimentManagementServiceStub(channel)

def create_research_hub_service_stub(channel: grpc.Channel):
    from airavata_sdk.generated.services import research_service_pb2_grpc
    return research_service_pb2_grpc.ResearchHubServiceStub(channel)

def create_research_project_service_stub(channel: grpc.Channel):
    from airavata_sdk.generated.services import research_service_pb2_grpc
    return research_service_pb2_grpc.ResearchProjectServiceStub(channel)

def create_research_resource_service_stub(channel: grpc.Channel):
    from airavata_sdk.generated.services import research_service_pb2_grpc
    return research_service_pb2_grpc.ResearchResourceServiceStub(channel)

def create_research_session_service_stub(channel: grpc.Channel):
    from airavata_sdk.generated.services import research_service_pb2_grpc
    return research_service_pb2_grpc.ResearchSessionServiceStub(channel)
```

- [ ] **Step 2: Commit**

```bash
git add -A && git commit -m "feat: add 7 new stub factories for user-facing gRPC services"
```

---

### Task 5: Create 7 new Python SDK client classes

**Files to create:**
- `airavata-python-sdk/airavata_sdk/clients/agent_interaction_client.py`
- `airavata-python-sdk/airavata_sdk/clients/experiment_management_client.py`
- `airavata-python-sdk/airavata_sdk/clients/plan_client.py`
- `airavata-python-sdk/airavata_sdk/clients/research_hub_client.py`
- `airavata-python-sdk/airavata_sdk/clients/research_project_client.py`
- `airavata-python-sdk/airavata_sdk/clients/research_resource_client.py`
- `airavata-python-sdk/airavata_sdk/clients/research_session_client.py`

Each client follows the `CredentialStoreClient` pattern:
1. Constructor: builds GrpcChannel, stores auth metadata, creates stub
2. One method per RPC: lazy-imports pb2, constructs request, calls stub, returns response

- [ ] **Step 1: Create all 7 client classes**

Each class follows this template (adapted per service):

```python
from airavata_sdk import Settings
from airavata_sdk.transport.utils import GrpcChannel, build_metadata, create_xxx_service_stub

class XxxClient:
    def __init__(self, access_token=None, claims=None):
        self.settings = Settings()
        channel = GrpcChannel(
            self.settings.API_SERVER_HOST,
            self.settings.API_SERVER_PORT,
            self.settings.API_SERVER_SECURE,
        )
        self._stub = create_xxx_service_stub(channel.channel)
        self._metadata = build_metadata(access_token, claims)

    def method_name(self, param1, param2):
        from airavata_sdk.generated.services import xxx_service_pb2 as pb2
        return self._stub.MethodName(
            pb2.MethodNameRequest(param1=param1, param2=param2),
            metadata=self._metadata,
        )
```

For each service, create methods matching every RPC defined in the proto. Use the proto request message fields as method parameters.

**Client RPCs to implement:**

- AgentInteractionClient: 20 methods (GetAgentInfo, SetupTunnel, ExecuteCommand, ExecuteAsyncCommand, ListAsyncCommands, TerminateAsyncCommand, ExecuteJupyter, ExecutePython, SetupEnv, RestartKernel, and their response-polling counterparts)
- ExperimentManagementClient: 5 methods (GetExperiment, LaunchExperiment, LaunchOptimizedExperiment, TerminateExperiment, GetProcessModel)
- PlanClient: 4 methods (SavePlan, GetPlan, GetPlansByUser, UpdatePlan)
- ResearchHubClient: 2 methods (StartProjectSession, ResumeSession)
- ResearchProjectClient: 4 methods (GetAllProjects, GetProjectsByOwner, CreateProject, DeleteProject)
- ResearchResourceClient: 15 methods (CreateDataset, CreateNotebook, CreateRepository, ModifyRepository, CreateModel, GetTags, GetResource, DeleteResource, GetAllResources, SearchResources, GetProjectsForResource, StarResource, CheckUserStarredResource, GetResourceStarCount, GetStarredResources)
- ResearchSessionClient: 4 methods (GetSessions, UpdateSessionStatus, DeleteSession, DeleteSessions)

- [ ] **Step 2: Commit**

```bash
git add -A && git commit -m "feat: add 7 new Python SDK client classes for user-facing services"
```

---

### Task 6: Add 42 missing methods to existing SDK clients

**Files:**
- Modify: `airavata-python-sdk/airavata_sdk/clients/api_server_client.py`
- Modify: `airavata-python-sdk/airavata_sdk/clients/sharing_registry_client.py`
- Modify: `airavata-python-sdk/airavata_sdk/clients/credential_store_client.py`

- [ ] **Step 1: Add 15 missing ResourceService methods to APIServerClient**

Add methods for job submission and data movement CRUD. Each follows the existing pattern in the class. The stub is `self._resource`:

Job submissions (9 methods):
- `add_ssh_job_submission`, `update_ssh_job_submission`, `get_ssh_job_submission`
- `add_cloud_job_submission`, `update_cloud_job_submission`
- `add_local_submission`, `update_local_submission`
- `add_unicore_job_submission`, `update_unicore_job_submission`

Data movements (6 methods):
- `add_scp_data_movement`, `update_scp_data_movement`
- `add_local_data_movement`, `update_local_data_movement`
- `add_gridftp_data_movement`, `update_gridftp_data_movement`

- [ ] **Step 2: Add 10 missing GatewayResourceProfile methods to APIServerClient**

Compute preferences (5): `add_compute_preference`, `get_compute_preference`, `update_compute_preference`, `delete_compute_preference`, `get_all_compute_preferences`

Storage preferences (5): `add_storage_preference`, `get_storage_preference`, `update_storage_preference`, `delete_storage_preference`, `get_all_storage_preferences`

Stub: `self._gateway_profile`

- [ ] **Step 3: Add 5 missing UserResourceProfile methods to APIServerClient**

`add_user_compute_preference`, `get_user_compute_preference`, `update_user_compute_preference`, `delete_user_compute_preference`, `get_all_user_compute_preferences`

Stub: `self._user_profile_service` (the user resource profile stub)

- [ ] **Step 4: Add 3 missing GroupResourceProfile methods to APIServerClient**

`get_group_compute_preference`, `get_group_compute_pref_list`, `get_group_batch_queue_policy_list`

Stub: `self._group_profile`

- [ ] **Step 5: Add 2 missing ExperimentService methods to APIServerClient**

`fetch_intermediate_outputs`, `get_intermediate_output_process_status`

Stub: `self._experiment`

- [ ] **Step 6: Add 2 missing ApplicationCatalog methods to APIServerClient**

`get_available_compute_resources`, `get_deployments_for_module_and_profile`

Stub: `self._app_catalog`

- [ ] **Step 7: Add 3 missing SharingService methods to SharingRegistryClient**

`revoke_from_users`, `revoke_from_groups`, `update_user`

- [ ] **Step 8: Add 2 missing CredentialService methods to CredentialStoreClient**

`is_ssh_setup_complete`, `setup_ssh_account`

- [ ] **Step 9: Commit**

```bash
git add -A && git commit -m "feat: add 42 missing methods to existing Python SDK clients"
```

---

## Final Verification

- [ ] **Java build**: `cd airavata-api && mvn clean compile -T4`
- [ ] **Java tests**: `cd airavata-api && mvn test -T4`
- [ ] **Python import check**: `cd airavata-python-sdk && python3 -c "from airavata_sdk.clients.agent_interaction_client import AgentInteractionClient; print('OK')"`
- [ ] **Spotless**: `cd /Users/yasith/code/artisan/airavata/feat-grpc-armeria-migration && mvn spotless:apply`
