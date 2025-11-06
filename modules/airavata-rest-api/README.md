# Airavata REST API

REST API module for managing compute and storage resources in Airavata. This module provides REST endpoints that replace the Thrift API for resource management operations.

## Overview

The Airavata REST API provides HTTP/REST endpoints for:
- **Compute Resources**: CRUD operations for compute resources, resource job managers (RJM), and batch queues
- **Storage Resources**: CRUD operations for storage resources

All endpoints use the same authorization logic as the Thrift servers and call handlers directly (no network calls).

## Prerequisites

- Java 17 or higher
- Maven 3.8 or higher
- Airavata API module built and available
- Database configured (reuses OpenJPA from airavata-api)

## Building

Build the module from the root directory:

```bash
cd /path/to/airavata
mvn clean install -pl modules/airavata-rest-api -am -DskipTests
```

Or build from the module directory:

```bash
cd modules/airavata-rest-api
mvn clean install -DskipTests
```

## Running

### Using Maven

```bash
cd modules/airavata-rest-api
mvn spring-boot:run
```

The service will start on port 8080 by default.

### Using JAR

After building, run the generated JAR:

```bash
java -jar target/airavata-rest-api-0.21-SNAPSHOT.jar
```

### Configuration

Server port and other settings can be configured in `src/main/resources/application.properties`:

```properties
server.port=8080
spring.application.name=airavata-rest-api
```

## API Endpoints

### Base URL

All endpoints are prefixed with `/api/v1`

### Compute Resources

#### List all compute resources
```http
GET /api/v1/compute
```

Returns a map of compute resource IDs and hostnames.

#### Get compute resource by ID
```http
GET /api/v1/compute/{id}
```

Returns detailed compute resource information.

#### Register compute resource
```http
POST /api/v1/compute
Content-Type: application/json

{
  "hostName": "example.host.edu",
  "hostAliases": ["alias1.example.edu", "alias2.example.edu"],
  "ipAddresses": ["192.168.1.1"],
  "resourceDescription": "Example compute resource",
  "enabled": true,
  "cpusPerNode": 24,
  "defaultNodeCount": 1,
  "defaultCPUCount": 24,
  "defaultWalltime": 3600,
  "maxMemoryPerNode": 128
}
```

Returns the created compute resource ID.

#### Update compute resource
```http
PUT /api/v1/compute/{id}
Content-Type: application/json

{
  "computeResourceId": "existing-resource-id",
  "hostName": "example.host.edu",
  "hostAliases": ["alias1.example.edu"],
  "ipAddresses": ["192.168.1.1"],
  "resourceDescription": "Updated description",
  "enabled": true,
  "cpusPerNode": 24,
  "defaultNodeCount": 1,
  "defaultCPUCount": 24,
  "defaultWalltime": 3600
}
```

#### Delete compute resource
```http
DELETE /api/v1/compute/{id}
```

### Resource Job Managers (RJM)

#### Register resource job manager
```http
POST /api/v1/compute/{id}/rjm
Content-Type: application/json

{
  "resourceJobManagerId": "DO_NOT_SET_AT_CLIENTS",
  "resourceJobManagerType": "SLURM",
  "jobManagerBinPath": "/usr/bin",
  "jobManagerCommands": {
    "SUBMISSION": "sbatch",
    "JOB_MONITORING": "squeue",
    "DELETION": "scancel"
  },
  "pushMonitoringEndpoint": null
}
```

#### Get resource job manager
```http
GET /api/v1/compute/{id}/rjm/{rjmId}
```

#### Update resource job manager
```http
PUT /api/v1/compute/{id}/rjm/{rjmId}
Content-Type: application/json

{
  "resourceJobManagerId": "existing-rjm-id",
  "resourceJobManagerType": "SLURM",
  "jobManagerBinPath": "/usr/bin",
  "jobManagerCommands": {
    "SUBMISSION": "sbatch",
    "JOB_MONITORING": "squeue",
    "DELETION": "scancel",
    "CHECK_JOB": "scontrol show job"
  },
  "pushMonitoringEndpoint": null
}
```

#### Delete resource job manager
```http
DELETE /api/v1/compute/{id}/rjm/{rjmId}
```

### Batch Queues

#### Delete batch queue
```http
DELETE /api/v1/compute/{id}/queue/{queueName}
```

### Storage Resources

#### List all storage resources
```http
GET /api/v1/storage
```

Returns a map of storage resource IDs and hostnames.

#### Get storage resource by ID
```http
GET /api/v1/storage/{id}
```

Returns detailed storage resource information.

#### Register storage resource
```http
POST /api/v1/storage
Content-Type: application/json

{
  "storageResourceId": "DO_NOT_SET_AT_CLIENTS",
  "hostName": "storage.host.edu",
  "storageResourceDescription": "Example storage resource",
  "enabled": true,
  "dataMovementInterfaces": []
}
```

Returns the created storage resource ID.

#### Update storage resource
```http
PUT /api/v1/storage/{id}
Content-Type: application/json

{
  "storageResourceId": "existing-storage-resource-id",
  "hostName": "storage.host.edu",
  "storageResourceDescription": "Updated description",
  "enabled": true,
  "dataMovementInterfaces": []
}
```

#### Delete storage resource
```http
DELETE /api/v1/storage/{id}
```

## Authentication

The API uses Bearer token authentication with claims. Include the following headers:

```http
Authorization: Bearer <access_token>
X-Claims: {"userName": "username", "gatewayID": "gateway-id"}
```

The `X-Claims` header should be a JSON object containing:
- `userName`: The username
- `gatewayID`: The gateway identifier

### Example with curl

```bash
curl -X GET http://localhost:8080/api/v1/compute \
  -H "Authorization: Bearer your-token-here" \
  -H "X-Claims: {\"userName\": \"user\", \"gatewayID\": \"gateway\"}"
```

**Note**: If TLS is not enabled in Airavata configuration, authentication may be optional. When TLS is enabled, authentication is required for all endpoints.

## Swagger UI

Interactive API documentation is available via Swagger UI:

- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **OpenAPI Docs**: http://localhost:8080/v3/api-docs/public

Swagger UI provides:
- Interactive API testing
- Complete endpoint documentation
- Request/response schema definitions
- Authentication testing

## Error Handling

The API returns standard HTTP status codes:

- `200 OK` - Successful GET request
- `201 Created` - Successful POST request
- `204 No Content` - Successful PUT/DELETE request
- `400 Bad Request` - Invalid request
- `401 Unauthorized` - Authentication required or failed
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

Error responses include a JSON body with error details:

```json
{
  "status": 400,
  "error": "Invalid request",
  "message": "Error details here",
  "timestamp": 1234567890
}
```

## Example Usage

### Create a compute resource

```bash
curl -X POST http://localhost:8080/api/v1/compute \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer token" \
  -H "X-Claims: {\"userName\": \"user\", \"gatewayID\": \"gateway\"}" \
  -d '{
    "hostName": "compute.example.edu",
    "hostAliases": ["compute-alias.example.edu"],
    "ipAddresses": ["192.168.1.100"],
    "resourceDescription": "Example HPC cluster",
    "enabled": true,
    "cpusPerNode": 24,
    "defaultNodeCount": 1,
    "defaultCPUCount": 24,
    "defaultWalltime": 3600,
    "maxMemoryPerNode": 128
  }'
```

### Get all compute resources

```bash
curl -X GET http://localhost:8080/api/v1/compute \
  -H "Authorization: Bearer token" \
  -H "X-Claims: {\"userName\": \"user\", \"gatewayID\": \"gateway\"}"
```

### Create a resource job manager

```bash
curl -X POST http://localhost:8080/api/v1/compute/{computeResourceId}/rjm \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer token" \
  -H "X-Claims: {\"userName\": \"user\", \"gatewayID\": \"gateway\"}" \
  -d '{
    "resourceJobManagerId": "DO_NOT_SET_AT_CLIENTS",
    "resourceJobManagerType": "SLURM",
    "jobManagerBinPath": "/usr/bin",
    "jobManagerCommands": {
      "SUBMISSION": "sbatch",
      "JOB_MONITORING": "squeue",
      "DELETION": "scancel",
      "CHECK_JOB": "scontrol show job"
    }
  }'
```

## Development

### Project Structure

```
modules/airavata-rest-api/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── org/apache/airavata/rest/api/
│   │   │       ├── AiravataRestApiApplication.java
│   │   │       ├── config/
│   │   │       │   ├── AuthzTokenFilter.java
│   │   │       │   └── OpenApiConfig.java
│   │   │       ├── controller/
│   │   │       │   ├── ComputeResourceController.java
│   │   │       │   └── StorageResourceController.java
│   │   │       ├── service/
│   │   │       │   ├── ComputeResourceService.java
│   │   │       │   └── StorageResourceService.java
│   │   │       └── exception/
│   │   │           └── GlobalExceptionHandler.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── log4j2.xml
│   └── test/
└── pom.xml
```

### Key Components

- **Controllers**: REST endpoints for compute and storage resources
- **Services**: Business logic that calls RegistryServerHandler directly
- **AuthzTokenFilter**: Authentication and authorization filter
- **OpenApiConfig**: Swagger/OpenAPI configuration
- **GlobalExceptionHandler**: Exception to HTTP status code mapping

## Compatibility

This REST API is designed to be fully compatible with the existing Thrift API:
- Same request/response models
- Same authorization logic
- Same database layer (reuses OpenJPA from airavata-api)
- Direct handler calls (no network overhead)

## Troubleshooting

### Service won't start

- Check that port 8080 is available
- Verify airavata-api module is built
- Check database configuration in airavata-api

### Authentication errors

- Verify TLS settings in Airavata configuration
- Check that Authorization and X-Claims headers are properly formatted
- Ensure the access token is valid

### Endpoints not found

- Verify the service is running on the correct port
- Check that the path starts with `/api/v1`
- Review Swagger UI to see available endpoints

## License

Licensed to the Apache Software Foundation (ASF) under the Apache License, Version 2.0.

