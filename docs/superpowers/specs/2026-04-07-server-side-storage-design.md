# Server-Side Storage + Experiment Utilities

## Problem

The Django portal's `airavata-django-portal-sdk` package contains business logic for file storage operations and experiment launch/clone utilities. This logic runs client-side (in the portal Django process), directly accessing storage backends (MFT gRPC, local filesystem) and orchestrating multi-step operations.

This violates the principle that business logic belongs on the server. The portal should be a thin UI layer calling server-side APIs.

## Goal

Move all storage/experiment business logic to the Airavata server as gRPC RPCs, expose them through the Python SDK facade, and eliminate the portal-sdk's client-side business logic.

## Architecture

```
BEFORE:
Portal → portal-sdk (business logic + MFT client + filesystem) → Storage backends

AFTER:
Portal → Python SDK facade → Airavata Server gRPC → Storage backends
```

## What moves to the server

### 1. User Storage Operations → `file_service.proto` RPCs

The portal-sdk's `user_storage` module provides 27+ file management functions. These become gRPC RPCs on the existing FileService:

| Portal-SDK Function | New gRPC RPC | HTTP Path |
|---|---|---|
| `save(path, file)` | `UploadFile` | POST `/api/v1/files/{path}` |
| `open_file(path)` | `DownloadFile` | GET `/api/v1/files/{path}` |
| `exists(path)` | `FileExists` | GET `/api/v1/files/{path}:exists` |
| `dir_exists(path)` | `DirExists` | GET `/api/v1/files/dirs/{path}:exists` |
| `listdir(path)` | `ListDir` | GET `/api/v1/files/dirs/{path}` |
| `delete(path)` | `DeleteFile` | DELETE `/api/v1/files/{path}` |
| `delete_dir(path)` | `DeleteDir` | DELETE `/api/v1/files/dirs/{path}` |
| `move(source, dest)` | `MoveFile` | POST `/api/v1/files/{path}:move` |
| `create_user_dir(path)` | `CreateDir` | POST `/api/v1/files/dirs/{path}` |
| `create_symlink(source, dest)` | `CreateSymlink` | POST `/api/v1/files/{path}:symlink` |
| `get_file_metadata(path)` | `GetFileMetadata` | GET `/api/v1/files/{path}:metadata` |
| `list_experiment_dir(exp_id)` | `ListExperimentDir` | GET `/api/v1/files/experiments/{exp_id}` |
| `get_download_url(path)` | `GetDownloadUrl` | GET `/api/v1/files/{path}:download-url` |

The server handles:
- Storage backend selection (MFT vs local filesystem based on config)
- Data product registration/tracking
- Replica location management
- Auth token validation

### 2. Experiment Launch/Clone Utilities → Existing experiment RPCs

The portal-sdk's `experiment_util` handles:
- Setting storage IDs and data dirs before launch
- Moving temp input file uploads to experiment dir
- Copying cloned experiment input URIs

These are pre-launch orchestration steps. They belong in the server's `LaunchExperiment` and `CloneExperiment` RPC implementations — the server already does experiment launch orchestration. The portal-sdk just duplicates/pre-processes what the server should handle.

**Action:** Verify the server's `LaunchExperiment` already handles storage setup. If not, add the logic there. The portal should just call `client.research.launch_experiment()` without pre-processing.

### 3. Intermediate Output Fetching → Already exists as RPC

`FetchIntermediateOutputs` and `GetIntermediateOutputProcessStatus` already exist as gRPC RPCs. The portal-sdk's `intermediate_output.py` is client-side logic that duplicates what the server can do. The portal should call the SDK facade directly.

## What stays in the portal (inlined)

- `queue_settings_calculators` — Django-specific decorator/registry pattern
- `dynamic_apps` — Django app loading (from portal-commons)
- Django views/serializers for rendering file data
- The `UserFiles` Django ORM model (portal-specific tracking)

## Python SDK Facade additions

Add methods to `client.storage` sub-client:

```python
client.storage.upload_file(path, file_content)
client.storage.download_file(path)
client.storage.list_dir(path)
client.storage.file_exists(path)
client.storage.delete_file(path)
client.storage.move_file(source, dest)
client.storage.create_dir(path)
client.storage.get_file_metadata(path)
client.storage.list_experiment_dir(experiment_id)
```

## Implementation order

1. **Expand `file_service.proto`** with user storage RPCs (messages + service definition)
2. **Implement server-side handlers** in `FileController` or a new `FileGrpcService`
3. **Wire storage backend** (the server already has `AdaptorSupport` for file operations)
4. **Add SDK facade methods** to `StorageClient`
5. **Regenerate Python proto stubs**

## Decisions

| Decision | Choice | Reason |
|----------|--------|--------|
| Where business logic lives | Server (Java) | Single source of truth, proper auth, no client-side backends |
| File upload mechanism | HTTP multipart (existing FileController) + gRPC for metadata ops | Binary file upload is better via HTTP; metadata via gRPC |
| MFT backend | Server-side only | Server has direct access to MFT, no need for client-side MFT gRPC |
| experiment_util launch logic | Server's LaunchExperiment handles it | Pre-launch orchestration belongs in the server |
| intermediate_output | Use existing RPCs | Already server-side, portal-sdk version is redundant |
