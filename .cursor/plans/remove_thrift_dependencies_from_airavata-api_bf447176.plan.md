---
name: Remove Thrift Dependencies from airavata-api
overview: Completely remove all Thrift dependencies from airavata-api module, replace Thrift serialization with JSON (Jackson), and restructure messaging to use OOP concepts instead of byte serialization.
todos:
  - id: create-message-wrapper
    content: Create MessageWrapper class for JSON serialization of MessageContext
    status: completed
  - id: update-publisher-json
    content: Update RabbitMQPublisher to use Jackson JSON serialization instead of Thrift
    status: completed
    dependencies:
      - create-message-wrapper
  - id: update-consumers-json
    content: Update all message consumers (MessageConsumer, StatusConsumer, ExperimentConsumer, ProcessConsumer) to use Jackson JSON deserialization
    status: completed
    dependencies:
      - create-message-wrapper
  - id: update-dbevent-handlers
    content: Remove ThriftUtils from DB event handlers and use mappers for entity data conversion
    status: completed
  - id: update-orchestrator-services
    content: Remove ThriftUtils from orchestrator and task services, use mappers for task model conversion
    status: completed
  - id: remove-thriftutils
    content: Delete ThriftUtils.java file completely
    status: completed
    dependencies:
      - update-publisher-json
      - update-consumers-json
      - update-dbevent-handlers
      - update-orchestrator-services
  - id: remove-thrift-dependency
    content: Remove libthrift dependency from airavata-api pom.xml
    status: completed
    dependencies:
      - remove-thriftutils
  - id: verify-no-thrift-imports
    content: Verify no org.apache.thrift imports remain in airavata-api module
    status: completed
    dependencies:
      - remove-thrift-dependency
---

# Remove Thrift Dependencies from airavata-api

## Problem

- `airavata-api` currently depends on Thrift libraries (`org.apache.thrift.*`)
- Messages are serialized using Thrift binary format with ByteBuffer
- `ThriftUtils` class provides Thrift serialization utilities
- The messaging system uses byte serialization which is not OOP-friendly

## Solution

Restructure messaging to use JSON serialization with Jackson, remove all Thrift dependencies, and make the messaging system work with domain objects directly.

## Implementation Steps

### 1. Create JSON-serializable Message Wrapper

- **File**: `airavata-api/src/main/java/org/apache/airavata/messaging/core/MessageWrapper.java`
- Create a wrapper class that contains MessageContext fields and can be serialized to/from JSON
- Include: event (as JSON), messageId, messageType, updatedTime, gatewayId, deliveryTag
- Use Jackson annotations for proper serialization

### 2. Update RabbitMQPublisher to use JSON

- **File**: `airavata-api/src/main/java/org/apache/airavata/messaging/core/impl/RabbitMQPublisher.java`
- Remove all `org.apache.thrift.*` imports
- Replace Thrift serialization with Jackson ObjectMapper
- Serialize MessageWrapper to JSON bytes
- Remove `convertDomainEventToThrift()` method

### 3. Update All Message Consumers to use JSON

- **Files**:
  - `airavata-api/src/main/java/org/apache/airavata/messaging/core/impl/MessageConsumer.java`
  - `airavata-api/src/main/java/org/apache/airavata/messaging/core/impl/StatusConsumer.java`
  - `airavata-api/src/main/java/org/apache/airavata/messaging/core/impl/ExperimentConsumer.java`
  - `airavata-api/src/main/java/org/apache/airavata/messaging/core/impl/ProcessConsumer.java`
- Remove all `org.apache.thrift.*` imports
- Replace Thrift deserialization with Jackson ObjectMapper
- Deserialize JSON bytes to MessageWrapper, then reconstruct MessageContext
- Use mappers (via reflection) to convert event JSON to domain models

### 4. Update DB Event Handlers

- **Files**:
  - `airavata-api/src/main/java/org/apache/airavata/sharing/messaging/SharingServiceDBEventHandler.java`
  - `airavata-api/src/main/java/org/apache/airavata/registry/messaging/RegistryServiceDBEventHandler.java`
  - `airavata-api/src/main/java/org/apache/airavata/messaging/core/util/DBEventPublisherUtils.java`
- Remove ThriftUtils usage
- For entity data models: Use mappers to convert Thrift models (from entityDataModel ByteBuffer) to domain models
- Remove Thrift serialization calls

### 5. Update Orchestrator and Task Services

- **Files**:
  - `airavata-api/src/main/java/org/apache/airavata/orchestrator/impl/SimpleOrchestratorImpl.java`
  - `airavata-api/src/main/java/org/apache/airavata/service/orchestrator/OrchestratorService.java`
- Remove ThriftUtils usage for task model serialization
- Use mappers to convert domain task models to Thrift models when needed for storage
- Store task models as JSON or use mappers for conversion

### 6. Update MessageContext and Related Classes

- **File**: `airavata-api/src/main/java/org/apache/airavata/messaging/core/MessageContext.java`
- Ensure it works with domain models only (no Thrift dependencies)
- Add JSON serialization support if needed

### 7. Remove ThriftUtils Class

- **File**: `airavata-api/src/main/java/org/apache/airavata/common/utils/ThriftUtils.java`
- Delete the file completely
- The `close()` method for TServiceClient should be moved to thrift-api module if still needed

### 8. Update Maven Dependencies

- **File**: `airavata-api/pom.xml`
- Remove `libthrift` dependency
- Ensure Jackson dependencies are present (already included)
- Remove any Thrift-related plugin configurations

### 9. Update Message Domain Model

- **File**: `airavata-api/src/main/java/org/apache/airavata/common/model/Message.java`
- Consider if this class is still needed or if MessageWrapper replaces it
- If keeping, ensure it has no Thrift dependencies

### 10. Handle Entity Data Model Serialization

- For DB events, the `entityDataModel` ByteBuffer contains Thrift-serialized data
- Use mappers to convert: ByteBuffer → Thrift model → Domain model
- Or store entity data as JSON in the future

## Architecture Changes

### Before:

```
MessageContext (domain) → Thrift serialization → ByteBuffer → RabbitMQ
RabbitMQ → ByteBuffer → Thrift deserialization → MessageContext (domain)
```

### After:

```
MessageContext (domain) → MessageWrapper (JSON) → JSON bytes → RabbitMQ
RabbitMQ → JSON bytes → MessageWrapper (JSON) → MessageContext (domain)
```

## Notes

- Jackson is already a dependency (via Spring Boot)
- All event types extend MessagingEvent, so they can be serialized as JSON
- Mappers are in thrift-api module, accessed via reflection to avoid circular dependency
- Entity data models in DB events may still need Thrift deserialization initially, but should migrate to JSON
- The Message class with ByteBuffer event field should be replaced with MessageWrapper that has the event as a JSON-serializable object