<!--
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
-->

# Apache Airavata Research Service

A comprehensive Spring Boot REST API service for managing research resources, computational infrastructure, and research workflows. This service provides a unified interface for researchers to discover, manage, and utilize computational resources and research artifacts.

## 🏗️ Architecture Overview

The Research Service employs a **dual database architecture** designed to separate research data from infrastructure management:

- **H2 Database (In-Memory)**: Manages v1 research resources (Projects, Datasets, Models, Notebooks, Repositories)
- **MariaDB Database**: Manages v2 infrastructure resources (Compute Resources, Storage Resources) using imported airavata-api entities
- **RESTful API**: Comprehensive v1 and v2 endpoints with different authentication requirements
- **Multi-Profile Configuration**: Supports development and production environments

### Key Components

- **Layered Architecture**: Controllers → Handlers/Services → Repositories → Entities
- **Authentication**: JWT + API Key dual authentication system
- **Data Conversion**: Advanced DTO-Entity mapping with JSON serialization for UI fields
- **Auto-Initialization**: Automated sample data seeding for development

## 🚀 Quick Start

### 1. Prerequisites

- Java 11+
- Maven 3.6+
- Docker & Docker Compose
- MariaDB client (for migrations)

### 2. Start Airavata Database Stack

```bash
cd "/Users/krishkatariya/dev/Professional Work/Google Summer of Code/airavata"

# Add hostname mapping (one-time setup)
echo "127.0.0.1 airavata.host" | sudo tee -a /etc/hosts

# Start MariaDB + Adminer web interface
docker-compose -f .devcontainer/docker-compose.yml up db adminer
```

**Database Access:**
- **Host**: `airavata.host:13306`
- **Username**: `airavata`
- **Password**: `123456`
- **Database**: `app_catalog`
- **Web Admin**: http://localhost:18088

### 3. Apply Database Migrations

```bash
cd airavata/modules/research-framework/research-service

# Run column length migration (REQUIRED for UI field JSON storage)
# This migration increases column lengths in airavata-api entities to support
# JSON serialization of UI-specific fields like queues, hostAliases, etc.
mysql -h airavata.host -P 13306 -u airavata -p123456 app_catalog < database-migrations/001-increase-description-column-lengths.sql
```

### 4. Start Research Service

```bash
# Development mode (includes sample data)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Production mode
mvn spring-boot:run
```

### 5. Access the Service

- **API Base URL**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console
- **Health Check**: http://localhost:8080/actuator/health

## 📊 Database Architecture

### Dual Database System

#### H2 Database (v1 Resources)
- **Purpose**: Research-focused entities and sample data
- **Location**: In-memory (`jdbc:h2:mem:testdb`)
- **Entities**: `Resource`, `Project`, `ResourceStar`, `Tag`, `Session`
- **Resource Types**: `DatasetResource`, `ModelResource`, `NotebookResource`, `RepositoryResource`
- **Sample Data**: 39+ resources across neuroscience research projects

#### MariaDB Database (v2 Infrastructure)
- **Purpose**: Production infrastructure and computational resources
- **Location**: `airavata.host:13306/app_catalog`
- **Entities**: `ComputeResourceEntity`, `StorageResourceEntity` (imported from airavata-api)
- **Resource Types**: HPC clusters, supercomputers, cloud resources, storage systems
- **Sample Data**: 12+ infrastructure resources

### Data Initializers

- **`DatasetInitializer`**: Creates 9 research datasets (all profiles)
- **`DevDataInitializer`**: Creates 10 neuroscience projects with full resource sets (dev profile only)

## 🔐 Authentication

### JWT Authentication (Users)
```bash
# Headers for authenticated requests
Authorization: Bearer <jwt_token>
X-Claims: {"userName":"user@domain.com","gatewayID":"default"}
```

### API Key Authentication (Services)
```bash
# Headers for service requests
X-API-Key: dev-research-api-key-12345
```

### Development Token Generation
```bash
# Generate test JWT
curl -X POST http://localhost:8080/api/dev/auth/token \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","name":"Test User"}'
```

## 🌐 API Endpoints

### V1 API - Research Resources (H2 Database)

#### Projects (`/api/v1/rf/projects`)
- `GET /` - List all projects
- `GET /{ownerId}` - Get projects by owner
- `POST /` - Create new project
- `DELETE /{projectId}` - Delete project

#### Resources (`/api/v1/rf/resources`)
- `GET /public` - List all public resources (with pagination, filtering)
- `GET /public/{id}` - Get resource by ID
- `GET /public/tags/all` - Get all tags sorted by popularity
- `GET /search` - Search resources by type and name
- `POST /dataset` - Create dataset resource
- `POST /notebook` - Create notebook resource  
- `POST /repository` - Create repository resource
- `POST /model` - Create model resource
- `PATCH /repository` - Modify repository resource
- `DELETE /{id}` - Delete resource
- `POST /{id}/star` - Star/unstar resource
- `GET /{id}/star` - Check star status
- `GET /resources/{id}/count` - Get star count
- `GET /{userId}/stars` - Get user's starred resources

#### Sessions (`/api/v1/rf/sessions`) 
- `GET /` - List user sessions (with status filtering)
- `PATCH /{sessionId}` - Update session status
- `DELETE /{sessionId}` - Delete session

#### Research Hub (`/api/v1/rf/hub`)
- `GET /start/project/{projectId}` - Start hub session for project
- `GET /resume/session/{sessionId}` - Resume existing session

### V2 API - Infrastructure Resources (MariaDB)

#### Compute Resources (`/api/v2/rf/compute-resources`) 🔒
- `GET /` - List compute resources (with name search)
- `GET /{id}` - Get compute resource by ID
- `POST /` - Create compute resource
- `PUT /{id}` - Update compute resource
- `DELETE /{id}` - Delete compute resource
- `GET /search` - Search by keyword
- `POST /{id}/star` - Star/unstar resource
- `GET /{id}/star` - Check star status
- `GET /{id}/stars/count` - Get star count
- `GET /starred` - Get starred resources

#### Storage Resources (`/api/v2/rf/storage-resources`) 🔒
- `GET /` - List storage resources (with name search)
- `GET /{id}` - Get storage resource by ID  
- `POST /` - Create storage resource
- `PUT /{id}` - Update storage resource
- `DELETE /{id}` - Delete storage resource
- `GET /search` - Search by keyword
- `GET /type/{storageType}` - Filter by storage type
- `POST /{id}/star` - Star/unstar resource
- `GET /{id}/star` - Check star status
- `GET /{id}/stars/count` - Get star count
- `GET /starred` - Get starred resources

### Development APIs (`/api/dev`)
- `POST /auth/token` - Generate test JWT token
- `POST /auth/api-key-info` - Get API key information

🔒 = Requires authentication (JWT or API Key)

## 📝 Sample API Requests

### Create Compute Resource
```json
POST /api/v2/rf/compute-resources/
Headers: X-API-Key: dev-research-api-key-12345

{
  "name": "Titan Supercomputer",
  "resourceDescription": "A powerful HPC cluster for scientific simulations",
  "hostName": "titan.supercluster.edu",
  "computeType": "HPC",
  "cpuCores": 299008,
  "memoryGB": 710000,
  "operatingSystem": "Cray Linux Environment",
  "hostAliases": ["titan-login1.supercluster.edu"],
  "ipAddresses": ["128.219.10.1"],
  "sshPort": 22,
  "alternativeSSHHostName": "titan-login.supercluster.edu",
  "securityProtocol": "SSH_KEYS",
  "resourceJobManagerType": "SLURM",
  "dataMovementProtocol": "SCP",
  "queueSystem": "SLURM",
  "resourceManager": "XSEDE",
  "queues": [
    {
      "queueName": "default",
      "maxNodes": 100,
      "maxProcessors": 2048,
      "maxRunTime": 7200
    }
  ],
  "enabled": true
}
```

### Create Storage Resource (S3)
```json
POST /api/v2/rf/storage-resources/
Headers: X-API-Key: dev-research-api-key-12345

{
  "name": "S3 Research Storage",
  "hostName": "s3.amazonaws.com",
  "storageResourceDescription": "AWS S3 bucket for research data",
  "storageType": "S3",
  "capacityTB": 1000,
  "accessProtocol": "S3",
  "endpoint": "https://s3.amazonaws.com",
  "supportsEncryption": true,
  "supportsVersioning": true,
  "bucketName": "my-research-bucket",
  "accessKey": "AKIAIOSFODNN7EXAMPLE",
  "secretKey": "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY",
  "resourceManager": "AWS",
  "enabled": true
}
```

## 🧪 Development Configuration

### IntelliJ IDEA Setup
1. Go to **Run > Edit Configurations**
2. Select your Spring Boot run configuration
3. In **Program arguments**, add: `--spring.profiles.active=dev`

### Environment Variables
```bash
# Override database configuration
export SPRING_DATASOURCE_URL=jdbc:mariadb://custom-host:3306/app_catalog
export SPRING_DATASOURCE_USERNAME=custom_user
export SPRING_DATASOURCE_PASSWORD=custom_password

# Override authentication
export RESEARCH_AUTH_DEV_API_KEY=your-custom-api-key
```

### Profile-Specific Behavior
- **Default Profile**: Production-ready with minimal sample data
- **Dev Profile**: Includes comprehensive sample data and relaxed security

## 🔧 Key Features

### Enhanced Data Management
- **Field Preservation**: Complete round-trip data integrity for complex objects
- **JSON Serialization**: Advanced UI field embedding in database description columns
- **Validation**: Comprehensive Jakarta Bean Validation with detailed error messages

### Search & Discovery
- **Multi-faceted Search**: Search by keyword, type, tags, author, language, framework
- **Pagination**: Configurable page size and sorting
- **Popularity Metrics**: Star counts and trending algorithms

### Integration Ready
- **CORS Support**: Configurable for frontend integration
- **OpenAPI Documentation**: Auto-generated Swagger documentation
- **Health Monitoring**: Spring Boot Actuator endpoints

## 🐛 Troubleshooting

### Common Issues

**Database Connection Failed**
```bash
# Verify database is running
docker ps | grep mariadb

# Check hostname mapping
ping airavata.host

# Test database connection
mysql -h airavata.host -P 13306 -u airavata -p123456 -e "SHOW DATABASES;"
```

**Column Length Errors**
```bash
# Apply the database migration
mysql -h airavata.host -P 13306 -u airavata -p123456 app_catalog < database-migrations/001-increase-description-column-lengths.sql
```

**Authentication Issues**
- Verify JWT token format and claims
- Check API key matches configuration
- Ensure proper headers are set

### Logging
```bash
# Enable debug logging
export LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_SECURITY=DEBUG
```

## 📚 Development Resources

- **API Documentation**: http://localhost:8080/swagger-ui.html
- **Database Console**: http://localhost:8080/h2-console (H2 only)
- **Source Code**: `src/main/java/org/apache/airavata/research/service/`
- **Configuration**: `src/main/resources/application.yml`
- **Migrations**: `database-migrations/`

## 🤝 Contributing

When making significant changes to the Research Service:

1. **Update Documentation**: Keep this README current with new endpoints, configuration changes, and architectural updates
2. **Run Tests**: Ensure all existing functionality remains intact
3. **Database Migrations**: Create numbered migration scripts in `database-migrations/` for schema changes
4. **API Documentation**: Update Swagger annotations for new endpoints

---

## 📋 UI vs Database Field Mapping

### Compute Resources

#### UI Fields (Frontend Forms)
- `name` - Resource display name
- `resourceDescription` - Resource description
- `hostName` - Primary hostname
- `computeType` - Type of compute resource (HPC, Cloud, etc.)
- `cpuCores` - Number of CPU cores
- `memoryGB` - Memory in gigabytes
- `operatingSystem` - Operating system
- `hostAliases` - Array of alternative hostnames
- `ipAddresses` - Array of IP addresses
- `sshPort` - SSH port number
- `alternativeSSHHostName` - Alternative SSH hostname
- `securityProtocol` - Security protocol (SSH_KEYS, etc.)
- `resourceJobManagerType` - Job manager type (SLURM, PBS, etc.)
- `dataMovementProtocol` - Data movement protocol (SCP, SFTP, etc.)
- `queueSystem` - Queue system type
- `resourceManager` - Resource manager name
- `queues` - Array of queue configurations with maxNodes, maxProcessors, maxRunTime
- `enabled` - Boolean status

#### App Catalog Database Fields (airavata-api entities)
- `computeResourceId` - Primary key UUID
- `computeResourceDescription` - Resource description
- `hostName` - Primary hostname
- `hostAliases` - JSON array in description field
- `ipAddresses` - JSON array in description field
- `resourceJobManagerType` - From JobSubmissionInterface enum
- `gatewayUsageReporting` - Boolean flag
- `gatewayUsageModuleLoadCommand` - Command string
- `gatewayUsageExecutable` - Executable path
- `cpuCount` - CPU core count
- `nodeCount` - Node count
- `ppn` - Processes per node
- `maxRunTime` - Maximum runtime
- `memoryPerNode` - Memory per node
- `loginUserName` - Login username
- `scratchLocation` - Scratch directory
- `allocationProjectNumber` - Project allocation number
- `resourceSpecificCredentialStoreToken` - Credential token
- `usageReportingGatewayId` - Gateway ID for reporting
- `creationTime` - Timestamp
- `updateTime` - Timestamp

### Storage Resources

#### UI Fields (Frontend Forms)
- `name` - Storage resource name
- `hostName` - Storage hostname
- `storageResourceDescription` - Description
- `storageType` - Type (S3, SFTP, etc.)
- `capacityTB` - Storage capacity in TB
- `accessProtocol` - Access protocol
- `endpoint` - Storage endpoint URL
- `supportsEncryption` - Encryption support boolean
- `supportsVersioning` - Versioning support boolean
- `bucketName` - S3 bucket name (S3 specific)
- `accessKey` - Access key (S3 specific)
- `secretKey` - Secret key (S3 specific)
- `resourceManager` - Resource manager name
- `enabled` - Boolean status

#### App Catalog Database Fields (airavata-api entities)
- `storageResourceId` - Primary key UUID
- `hostName` - Storage hostname
- `storageResourceDescription` - Description
- `enabled` - Boolean status
- `creationTime` - Timestamp
- `updateTime` - Timestamp
- DataMovementInterface relations for protocol-specific configurations
- Storage-specific fields stored in related entities and JSON serialization

### Field Mapping Strategy

**Current Implementation (Post-August 2025):**
- **Direct Field Mapping**: UI fields map directly to DTO fields which map to entity fields
- **No JSON Injection**: Complex UI fields (arrays, objects) are handled through proper entity relationships
- **Entity Relationships**: Uses airavata-api's JobSubmissionInterface, DataMovementInterface, BatchQueue entities
- **Description Field**: Used only for actual descriptions, no JSON serialization

**Previous Implementation (Pre-August 2025):**
- **JSON-in-Description**: UI-specific fields were serialized as JSON in the description column
- **Field Mismatch**: UI used deprecated field names that didn't match backend entities
- **Workaround**: DTOConverter extracted/encoded JSON from description fields

---

**Apache Airavata Research Service** - Empowering scientific discovery through unified research resource management.