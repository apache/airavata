# Server-Side Storage + Experiment Utilities — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add user storage gRPC RPCs to the file service so the portal can manage user files server-side instead of running client-side storage logic.

**Architecture:** Expand `file_service.proto` with user storage RPCs. Implement a `UserStorageGrpcService` in storage-service that delegates to the existing `StorageProvider` interface. The service handles storage backend selection, data product registration, and auth. Add corresponding methods to the Python SDK's `StorageClient` facade.

**Tech Stack:** Java 17, Protobuf/gRPC, Spring, Armeria HTTP/JSON transcoding, Python 3.10+

---

## Context

The existing file service has two scopes:
1. **Process files** (`FileController` + `AirvataFileService`) — operates on files within a running job's workspace via agent adaptor. Already implemented.
2. **User storage** (portal-sdk's `user_storage`) — operates on user's gateway data store files (upload, download, list, delete, move). Currently client-side. **This is what we're adding.**

The user storage operations need:
- A `UserStorageProvider` backend (currently in portal-sdk, connects to storage resources via adaptor)
- Data product registration (links files to Airavata's data catalog)
- Auth token validation

---

### Task 1: Define user storage gRPC RPCs in file_service.proto

**Files:**
- Modify: `airavata-api/storage-service/src/main/proto/file_service.proto`

- [ ] **Step 1: Add UserStorageService with RPCs and messages**

Expand the proto file with a full `UserStorageService` definition. Read the existing `file_service.proto` first (it currently only has `FileUploadResponse` message, no service).

Add the following service and messages:

```protobuf
import "google/api/annotations.proto";
import "google/protobuf/empty.proto";
import "org/apache/airavata/model/data/replica/data_replica_models.proto";

service UserStorageService {

  // Upload a file to user storage
  rpc UploadFile(UploadFileRequest) returns (UploadFileResponse) {
    option (google.api.http) = {
      post: "/api/v1/user-storage/{storage_resource_id}/files/{path}"
      body: "*"
    };
  }

  // Download a file from user storage
  rpc DownloadFile(DownloadFileRequest) returns (DownloadFileResponse) {
    option (google.api.http) = {
      get: "/api/v1/user-storage/{storage_resource_id}/files/{path}"
    };
  }

  // Check if a file exists
  rpc FileExists(FileExistsRequest) returns (FileExistsResponse) {
    option (google.api.http) = {
      get: "/api/v1/user-storage/{storage_resource_id}/files/{path}:exists"
    };
  }

  // Check if a directory exists
  rpc DirExists(DirExistsRequest) returns (DirExistsResponse) {
    option (google.api.http) = {
      get: "/api/v1/user-storage/{storage_resource_id}/dirs/{path}:exists"
    };
  }

  // List directory contents
  rpc ListDir(ListDirRequest) returns (ListDirResponse) {
    option (google.api.http) = {
      get: "/api/v1/user-storage/{storage_resource_id}/dirs/{path}"
    };
  }

  // Delete a file and its data product
  rpc DeleteFile(DeleteFileRequest) returns (google.protobuf.Empty) {
    option (google.api.http) = {
      delete: "/api/v1/user-storage/{storage_resource_id}/files/{path}"
    };
  }

  // Delete a directory
  rpc DeleteDir(DeleteDirRequest) returns (google.protobuf.Empty) {
    option (google.api.http) = {
      delete: "/api/v1/user-storage/{storage_resource_id}/dirs/{path}"
    };
  }

  // Move/rename a file
  rpc MoveFile(MoveFileRequest) returns (MoveFileResponse) {
    option (google.api.http) = {
      post: "/api/v1/user-storage/{storage_resource_id}/files/{source_path}:move"
      body: "*"
    };
  }

  // Create a directory
  rpc CreateDir(CreateDirRequest) returns (CreateDirResponse) {
    option (google.api.http) = {
      post: "/api/v1/user-storage/{storage_resource_id}/dirs/{path}"
    };
  }

  // Create a symlink
  rpc CreateSymlink(CreateSymlinkRequest) returns (google.protobuf.Empty) {
    option (google.api.http) = {
      post: "/api/v1/user-storage/{storage_resource_id}/files/{dest_path}:symlink"
      body: "*"
    };
  }

  // Get file/directory metadata
  rpc GetFileMetadata(GetFileMetadataRequest) returns (FileMetadataResponse) {
    option (google.api.http) = {
      get: "/api/v1/user-storage/{storage_resource_id}/files/{path}:metadata"
    };
  }

  // List an experiment's output directory
  rpc ListExperimentDir(ListExperimentDirRequest) returns (ListDirResponse) {
    option (google.api.http) = {
      get: "/api/v1/user-storage/experiments/{experiment_id}/dirs/{path}"
    };
  }

  // Get the default storage resource ID for the gateway
  rpc GetDefaultStorageResourceId(GetDefaultStorageResourceIdRequest) returns (GetDefaultStorageResourceIdResponse) {
    option (google.api.http) = {
      get: "/api/v1/user-storage/default-storage-resource"
    };
  }
}

// --- Request/Response Messages ---

message UploadFileRequest {
  string storage_resource_id = 1;
  string path = 2;
  bytes content = 3;
  string name = 4;
  string content_type = 5;
}

message UploadFileResponse {
  org.apache.airavata.model.data.replica.proto.DataProductModel data_product = 1;
}

message DownloadFileRequest {
  string storage_resource_id = 1;
  string path = 2;
}

message DownloadFileResponse {
  bytes content = 1;
  string name = 2;
  string content_type = 3;
}

message FileExistsRequest {
  string storage_resource_id = 1;
  string path = 2;
}

message FileExistsResponse {
  bool exists = 1;
}

message DirExistsRequest {
  string storage_resource_id = 1;
  string path = 2;
}

message DirExistsResponse {
  bool exists = 1;
}

message ListDirRequest {
  string storage_resource_id = 1;
  string path = 2;
}

message ListDirResponse {
  repeated FileMetadataResponse directories = 1;
  repeated FileMetadataResponse files = 2;
}

message DeleteFileRequest {
  string storage_resource_id = 1;
  string path = 2;
}

message DeleteDirRequest {
  string storage_resource_id = 1;
  string path = 2;
}

message MoveFileRequest {
  string storage_resource_id = 1;
  string source_path = 2;
  string dest_path = 3;
}

message MoveFileResponse {
  org.apache.airavata.model.data.replica.proto.DataProductModel data_product = 1;
}

message CreateDirRequest {
  string storage_resource_id = 1;
  string path = 2;
  repeated string dir_names = 3;
  bool create_unique = 4;
}

message CreateDirResponse {
  string created_path = 1;
}

message CreateSymlinkRequest {
  string storage_resource_id = 1;
  string source_path = 2;
  string dest_path = 3;
}

message GetFileMetadataRequest {
  string storage_resource_id = 1;
  string path = 2;
}

message FileMetadataResponse {
  string name = 1;
  string path = 2;
  int64 size = 3;
  int64 created_time = 4;
  int64 modified_time = 5;
  bool is_directory = 6;
  string content_type = 7;
  string data_product_uri = 8;
}

message ListExperimentDirRequest {
  string experiment_id = 1;
  string path = 2;
}

message GetDefaultStorageResourceIdRequest {}

message GetDefaultStorageResourceIdResponse {
  string storage_resource_id = 1;
}
```

- [ ] **Step 2: Build to verify proto compiles**

```bash
cd airavata-api && mvn compile -T4 -pl storage-service
```

- [ ] **Step 3: Commit**

```bash
git add -A && git commit -m "feat: define UserStorageService gRPC RPCs in file_service.proto"
```

---

### Task 2: Implement UserStorageGrpcService handler

**Files:**
- Create: `airavata-api/storage-service/src/main/java/org/apache/airavata/storage/grpc/UserStorageGrpcService.java`

- [ ] **Step 1: Create the gRPC service handler**

This handler extends the generated `UserStorageServiceGrpc.UserStorageServiceImplBase` and delegates to the `StorageProvider` interface and `AdaptorSupport` for actual file operations.

Read the existing `AirvataFileService.java` to understand how `AdaptorSupport` and `AgentAdaptor` work for file operations. The user storage service will use a similar pattern but scoped to the user's storage resource rather than a process.

The handler should:
1. Get the user's storage resource configuration from the `StorageRegistry`
2. Create an adaptor for that storage resource via `AdaptorSupport`
3. Perform the file operation
4. For upload/move operations, register/update data products via `DataProductInterface`
5. Use `GrpcStatusMapper` for error handling

```java
package org.apache.airavata.storage.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.apache.airavata.api.file.*;
import org.apache.airavata.grpc.GrpcStatusMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UserStorageGrpcService extends UserStorageServiceGrpc.UserStorageServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(UserStorageGrpcService.class);

    // Inject: StorageRegistry (for storage resource lookup)
    // Inject: DataProductInterface (for data product CRUD)
    // Inject: AdaptorSupport (for file operations via agent adaptor)
    // Inject: RequestContext (for auth token extraction)

    @Override
    public void listDir(ListDirRequest request, StreamObserver<ListDirResponse> responseObserver) {
        try {
            // 1. Get storage resource config
            // 2. Get adaptor for resource
            // 3. List directory
            // 4. Build response with file metadata
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(GrpcStatusMapper.mapToGrpcException(e));
        }
    }

    // ... implement all other RPCs following the same pattern
}
```

The implementer should:
1. Read how `AirvataFileService` uses `ProcessDataManager` + `AgentAdaptor` for file operations
2. Adapt that pattern for user storage: instead of `ProcessDataManager.getBaseDir()`, use the user's storage resource path
3. For data product operations, use `DataProductInterface.registerDataProduct()`, etc.
4. Read the portal-sdk's `user_storage/api.py` to understand the business logic each operation performs

- [ ] **Step 2: Build and test**

```bash
cd airavata-api && mvn compile -T4 && mvn test -T4
```

- [ ] **Step 3: Commit**

```bash
git add -A && git commit -m "feat: implement UserStorageGrpcService handler for user file operations"
```

---

### Task 3: Register UserStorageService with Armeria

**Files:**
- Modify: `airavata-api/storage-service/src/main/java/org/apache/airavata/storage/config/StorageServiceConfig.java` (verify @ComponentScan covers `storage.grpc` package)

- [ ] **Step 1: Verify the handler is auto-discovered**

The `UserStorageGrpcService` is `@Component` annotated. Check that `StorageServiceConfig.java`'s component scan includes the `grpc` package. If the scan is `org.apache.airavata.storage`, it will auto-discover `org.apache.airavata.storage.grpc.*`.

If not, update the component scan.

Also verify in `AiravataArmeriaConfig.java` that all `BindableService` beans are collected and registered. This should already work since the Armeria config collects all `BindableService` instances.

- [ ] **Step 2: Build full project and verify service registration**

```bash
cd airavata-api && mvn compile -T4 && mvn test -T4
```

- [ ] **Step 3: Commit (if changes needed)**

```bash
git add -A && git commit -m "feat: register UserStorageService with Armeria server"
```

---

### Task 4: Add user storage methods to Python SDK StorageClient facade

**Files:**
- Modify: `airavata-python-sdk/airavata_sdk/facade/storage.py`
- Modify: `airavata-python-sdk/airavata_sdk/transport/utils.py`

- [ ] **Step 1: Regenerate Python proto stubs for updated file_service.proto**

```bash
cd airavata-python-sdk
python3 -m grpc_tools.protoc \
  -I../airavata-api/src/main/proto \
  -I../airavata-api/storage-service/src/main/proto \
  -I$(python3 -c "import grpc_tools; import os; print(os.path.join(os.path.dirname(grpc_tools.__file__), '_proto'))") \
  --python_out=airavata_sdk/generated/services \
  --grpc_python_out=airavata_sdk/generated/services \
  --pyi_out=airavata_sdk/generated/services \
  ../airavata-api/storage-service/src/main/proto/file_service.proto
```

- [ ] **Step 2: Add stub factory for UserStorageService**

In `transport/utils.py`, add:

```python
def create_user_storage_service_stub(channel: grpc.Channel):
    from airavata_sdk.generated.services import file_service_pb2_grpc
    return file_service_pb2_grpc.UserStorageServiceStub(channel)
```

- [ ] **Step 3: Add user storage methods to StorageClient**

In `facade/storage.py`, add the `UserStorageService` stub and methods:

```python
from airavata_sdk.transport.utils import (
    create_resource_service_stub,
    create_user_storage_service_stub,
)


class StorageClient:
    def __init__(self, channel, metadata, gateway_id):
        self._metadata = metadata
        self._gateway_id = gateway_id
        self._resource = create_resource_service_stub(channel)
        self._user_storage = create_user_storage_service_stub(channel)

    # ... existing storage resource methods ...

    # --- User Storage Operations ---

    def upload_file(self, path, content, name, storage_resource_id=None, content_type=""):
        pb2 = self._svc("file_service_pb2")
        return self._user_storage.UploadFile(
            pb2.UploadFileRequest(
                storage_resource_id=storage_resource_id or "",
                path=path, content=content, name=name, content_type=content_type,
            ),
            metadata=self._metadata,
        )

    def download_file(self, path, storage_resource_id=None):
        pb2 = self._svc("file_service_pb2")
        return self._user_storage.DownloadFile(
            pb2.DownloadFileRequest(
                storage_resource_id=storage_resource_id or "", path=path,
            ),
            metadata=self._metadata,
        )

    def file_exists(self, path, storage_resource_id=None):
        pb2 = self._svc("file_service_pb2")
        resp = self._user_storage.FileExists(
            pb2.FileExistsRequest(
                storage_resource_id=storage_resource_id or "", path=path,
            ),
            metadata=self._metadata,
        )
        return resp.exists

    def dir_exists(self, path, storage_resource_id=None):
        pb2 = self._svc("file_service_pb2")
        resp = self._user_storage.DirExists(
            pb2.DirExistsRequest(
                storage_resource_id=storage_resource_id or "", path=path,
            ),
            metadata=self._metadata,
        )
        return resp.exists

    def list_dir(self, path, storage_resource_id=None):
        pb2 = self._svc("file_service_pb2")
        return self._user_storage.ListDir(
            pb2.ListDirRequest(
                storage_resource_id=storage_resource_id or "", path=path,
            ),
            metadata=self._metadata,
        )

    def delete_file(self, path, storage_resource_id=None):
        pb2 = self._svc("file_service_pb2")
        self._user_storage.DeleteFile(
            pb2.DeleteFileRequest(
                storage_resource_id=storage_resource_id or "", path=path,
            ),
            metadata=self._metadata,
        )

    def delete_dir(self, path, storage_resource_id=None):
        pb2 = self._svc("file_service_pb2")
        self._user_storage.DeleteDir(
            pb2.DeleteDirRequest(
                storage_resource_id=storage_resource_id or "", path=path,
            ),
            metadata=self._metadata,
        )

    def move_file(self, source_path, dest_path, storage_resource_id=None):
        pb2 = self._svc("file_service_pb2")
        return self._user_storage.MoveFile(
            pb2.MoveFileRequest(
                storage_resource_id=storage_resource_id or "",
                source_path=source_path, dest_path=dest_path,
            ),
            metadata=self._metadata,
        )

    def create_dir(self, path, dir_names=None, create_unique=False, storage_resource_id=None):
        pb2 = self._svc("file_service_pb2")
        return self._user_storage.CreateDir(
            pb2.CreateDirRequest(
                storage_resource_id=storage_resource_id or "",
                path=path, dir_names=dir_names or [], create_unique=create_unique,
            ),
            metadata=self._metadata,
        )

    def create_symlink(self, source_path, dest_path, storage_resource_id=None):
        pb2 = self._svc("file_service_pb2")
        self._user_storage.CreateSymlink(
            pb2.CreateSymlinkRequest(
                storage_resource_id=storage_resource_id or "",
                source_path=source_path, dest_path=dest_path,
            ),
            metadata=self._metadata,
        )

    def get_file_metadata(self, path, storage_resource_id=None):
        pb2 = self._svc("file_service_pb2")
        return self._user_storage.GetFileMetadata(
            pb2.GetFileMetadataRequest(
                storage_resource_id=storage_resource_id or "", path=path,
            ),
            metadata=self._metadata,
        )

    def list_experiment_dir(self, experiment_id, path=""):
        pb2 = self._svc("file_service_pb2")
        return self._user_storage.ListExperimentDir(
            pb2.ListExperimentDirRequest(
                experiment_id=experiment_id, path=path,
            ),
            metadata=self._metadata,
        )

    def get_default_storage_resource_id(self):
        pb2 = self._svc("file_service_pb2")
        resp = self._user_storage.GetDefaultStorageResourceId(
            pb2.GetDefaultStorageResourceIdRequest(),
            metadata=self._metadata,
        )
        return resp.storage_resource_id
```

- [ ] **Step 4: Verify Python imports**

```bash
cd airavata-python-sdk && python3 -c "from airavata_sdk.facade.storage import StorageClient; print('OK')"
```

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "feat: add user storage methods to Python SDK StorageClient facade"
```

---

### Task 5: Verify experiment launch handles storage setup

**Files:**
- Read: `airavata-api/research-service/src/main/java/org/apache/airavata/research/grpc/ExperimentGrpcService.java`
- Read: `airavata-api/orchestration-service/src/main/java/org/apache/airavata/orchestration/service/RegistryServerHandler.java`

- [ ] **Step 1: Trace the LaunchExperiment flow**

Read the server's `LaunchExperiment` implementation to verify it handles:
1. Setting storage ID and data directory on the experiment
2. Moving temp input file uploads to the experiment directory
3. Copying cloned experiment input URIs

If these steps are missing, the portal-sdk's `experiment_util/api.py` logic needs to be added to the server's launch flow.

If they're already handled (likely, since the server does full experiment orchestration), document this finding.

- [ ] **Step 2: Document findings and commit if changes needed**

```bash
git add -A && git commit -m "feat: verify/enhance LaunchExperiment storage setup"
```

---

## Final Verification

- [ ] **Java build**: `cd airavata-api && mvn clean compile -T4`
- [ ] **Java tests**: `cd airavata-api && mvn test -T4`
- [ ] **Python imports**: `cd airavata-python-sdk && python3 -c "from airavata_sdk.facade.storage import StorageClient; print('OK')"`
- [ ] **Spotless**: `cd /Users/yasith/code/artisan/airavata/feat-grpc-armeria-migration && mvn spotless:apply`
