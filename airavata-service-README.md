# AiravataService - Unified Thrift Service

## Overview

AiravataService is a unified thrift service that combines all Airavata services into a single server process using TMultiplexedProcessor. This simplifies deployment and provides a foundation for future gRPC migration.

## Services Included

The unified service includes all 10 Airavata services:

1. **Airavata API** - Main API service
2. **Registry Service** - Data registry operations
3. **Credential Store Service** - Credential management
4. **Sharing Registry Service** - Data sharing operations
5. **Orchestrator Service** - Workflow orchestration
6. **Workflow Service** - Workflow management
7. **User Profile Service** - User profile management
8. **Tenant Profile Service** - Tenant/gateway management
9. **IAM Admin Services** - Identity and access management
10. **Group Manager Service** - Group management

## Deployment

### Starting the Unified Service

```bash
# Start only the unified service
java -jar airavata-api.jar --servers=airavata-service

# Or use the ServerMain class directly
java org.apache.airavata.server.ServerMain --servers=airavata-service
```

### Configuration

The service uses the following configuration properties:

```properties
# Server settings
airavata_service.class=org.apache.airavata.service.airavata.AiravataServiceServer
airavata.service.port=9930
airavata.service.host=0.0.0.0
airavata.service.min.threads=30
airavata.service.max.threads=100
```

## Client Usage

### Using the Unified Client Factory

```java
import org.apache.airavata.service.airavata.client.AiravataServiceClientFactory;

// Create all clients with default settings (localhost:9930)
AiravataServiceClientFactory.AiravataServiceClients clients = 
    AiravataServiceClientFactory.createAllClients();

// Or specify host and port
AiravataServiceClientFactory.AiravataServiceClients clients = 
    AiravataServiceClientFactory.createAllClients("localhost", 9930);

// Access individual services
String version = clients.airavata.getAPIVersion();
// Use other services...
```

### Using Individual Service Clients

```java
// Create individual clients
Airavata.Client airavataClient = AiravataServiceClientFactory.createAiravataClient("localhost", 9930);
RegistryService.Client registryClient = AiravataServiceClientFactory.createRegistryClient("localhost", 9930);
// ... other services
```

### Backward Compatibility

Existing client code can continue to work by configuring all service hosts/ports to point to the unified server:

```properties
# Point all services to the unified server
registry.server.host=localhost
registry.server.port=9930
credential.store.server.host=localhost
credential.store.server.port=9930
sharing.registry.server.host=localhost
sharing.registry.server.port=9930
# ... etc
```

## Benefits

1. **Single Server Process** - Simplifies deployment and resource management
2. **Backward Compatible** - Existing clients work with multiplexed protocol
3. **Migration Path** - New client factory for simplified access
4. **Foundation for gRPC** - Same structure can be replicated in gRPC
5. **Reduced Network Overhead** - In-process calls instead of network calls between services

## Development

### Building

```bash
# Generate thrift stubs (includes airavata service)
./thrift-interface-descriptions/generate-thrift-stubs.sh java

# Build the project
mvn clean compile
```

### Testing

```bash
# Run integration tests
mvn test -Dtest=AiravataServiceIntegrationTest
```

## Architecture

The unified service uses Apache Thrift's TMultiplexedProcessor to route calls to the appropriate service handlers:

```
Client Request → TMultiplexedProcessor → Service Handler
                ↓
            Service Name Router
                ↓
        [Airavata, Registry, CredentialStore, ...]
```

Each service maintains its original interface and behavior, but all run within the same JVM process.

## Migration from Separate Services

### For New Applications

Use the new `AiravataServiceClientFactory` for simplified client management.

### For Existing Applications

1. **Option 1**: Update configuration to point all services to the unified server port
2. **Option 2**: Gradually migrate to use the new client factory
3. **Option 3**: Continue using separate services (backward compatible)

## Future gRPC Migration

The unified service structure provides a foundation for gRPC migration:

1. The same service handlers can be used
2. The multiplexed approach can be replicated with gRPC services
3. Client factory pattern can be adapted for gRPC clients

## Troubleshooting

### Common Issues

1. **Port conflicts**: Ensure port 9930 is available
2. **Database initialization**: Check that all required databases are configured
3. **Client connection**: Verify clients are using the correct host/port

### Logs

The service logs all operations. Check logs for:
- Service startup messages
- Database initialization status
- Client connection attempts
- Service routing information
