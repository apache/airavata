# File API Module

The File API module provides HTTP endpoints for file operations (list, upload, download) for process data directories.

## API Type

- **Server**: HTTP Server (unified)
- **Port**: 8090 (default)
- **Configuration**: `airavata.services.http.server.port`

## Overview

The File API provides HTTP endpoints for managing files in process data directories. It runs as part of the unified HTTP server on port 8090.

See [Architecture](../../README.md#architecture) for the full API layer overview.

## HTTP Endpoints

All endpoints are under the base path **`/api/v1/files`**.

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/files/list/{live}/{processId}` | GET | List files in process root directory |
| `/api/v1/files/list/{live}/{processId}/{*subPath}` | GET | List files in subdirectory or get file info |
| `/api/v1/files/download/{live}/{processId}/{*subPath}` | GET | Download a file |
| `/api/v1/files/upload/{live}/{processId}/{*subPath}` | POST | Upload a file |

### Path Parameters

- `live` - Indicates whether accessing live or archived process data
- `processId` - Process identifier
- `subPath` - Relative path within the process data directory (wildcard path variable)

### Response Formats

- **List Directory**: Returns JSON array of file/directory information
- **List File**: Returns JSON object with file metadata
- **Download**: Returns file content with appropriate Content-Type header
- **Upload**: Returns JSON object with upload confirmation

## Internal Services Used

The File API uses the following internal components:

- **AiravataFileService** - File operations service
- **ProcessDataManager** - Process data directory management

These are internal components within the File API module, not separate services.

## Configuration Properties

| Property | Default | Description |
|----------|---------|-------------|
| `airavata.services.fileserver.enabled` | `true` | Enable/disable File API |
| `airavata.services.http.server.port` | `8090` | Unified HTTP server port |
| `airavata.services.fileserver.spring.servlet.multipart.max-file-size` | `10MB` | Maximum file upload size |
| `airavata.services.fileserver.spring.servlet.multipart.max-request-size` | `10MB` | Maximum request size |

## Implementation

### Controller
- `src/main/java/org/apache/airavata/file/server/controller/FileController.java`

### Services
- `src/main/java/org/apache/airavata/file/server/service/AiravataFileService.java`
- `src/main/java/org/apache/airavata/file/server/service/ProcessDataManager.java`

### Configuration
- `src/main/java/org/apache/airavata/file/server/FileServerConfiguration.java`

### Models
- `src/main/java/org/apache/airavata/file/server/model/AiravataDirectory.java`
- `src/main/java/org/apache/airavata/file/server/model/AiravataFile.java`
- `src/main/java/org/apache/airavata/file/server/model/FileUploadResponse.java`

## Related Documentation

- Main project README: [../../README.md](../../README.md)
- Deployment scripts: [../../dev-tools/deployment-scripts/README.md](../../dev-tools/deployment-scripts/README.md)
