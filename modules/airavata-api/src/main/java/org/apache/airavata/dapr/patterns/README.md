# Dapr Patterns and Conventions

This package documents the standard patterns and conventions for using Dapr across the airavata-api module.

## Table of Contents

1. [Topic Naming](#topic-naming)
2. [State Management](#state-management)
3. [Message Handlers](#message-handlers)
4. [Workflow Definitions](#workflow-definitions)
5. [Configuration](#configuration)
6. [State Transitions](#state-transitions)

## Topic Naming

All Dapr Pub/Sub topics should be defined in `DaprTopics` class.

**Location**: `org.apache.airavata.dapr.messaging.DaprTopics`

**Usage**:
```java
// ✅ Good - use constants
messagingFactory.getSubscriber(handler, routingKeys, DaprTopics.PROCESS);

// ❌ Bad - hardcoded strings
messagingFactory.getSubscriber(handler, routingKeys, "process-topic");

// ✅ Good - use constants (corrected example)
messagingFactory.getSubscriber(handler, routingKeys, DaprTopics.PROCESS);
```

**Available Topics**:
- `DaprTopics.STATUS` - Status change events
- `DaprTopics.EXPERIMENT` - Experiment lifecycle events
- `DaprTopics.PROCESS` - Process lifecycle events
- `DaprTopics.PARSING` - Parsing completion messages
- `DaprTopics.MONITORING` - Monitoring messages
- `DaprTopics.MONITORING_JOB_STATUS` - Job status monitoring

## State Management

Use `DaprStateManager` for all state operations. State keys should be generated using `DaprStateKeys`.

**Location**: 
- `org.apache.airavata.dapr.state.DaprStateManager` (interface)
- `org.apache.airavata.dapr.state.DaprStateManagerImpl` (implementation)
- `org.apache.airavata.dapr.state.DaprStateKeys` (key generators)

**Usage**:
```java
@Autowired
private DaprStateManager daprStateManager;

// Save state
String key = DaprStateKeys.cancelExperiment(experimentId);
daprStateManager.saveState(key, "CANCEL_REQUEST");

// Get state
Optional<String> value = daprStateManager.getState(key, String.class);

// Check existence
boolean exists = daprStateManager.exists(key);

// Delete state
daprStateManager.deleteState(key);
```

**State Key Naming Convention**: `{category}:{entity-type}:{identifier}`
- Example: `cancel:experiment:exp-123`
- Example: `workflow:state:workflow-456`

## Message Handlers

Extract message handlers to separate classes implementing `MessageHandler` interface.

**Pattern**:
```java
public class MyMessageHandler implements MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(MyMessageHandler.class);
    
    private final MyService service;
    private final Subscriber subscriber;
    
    public MyMessageHandler(MyService service, Subscriber subscriber) {
        this.service = service;
        this.subscriber = subscriber;
    }
    
    @Override
    public void onMessage(MessageContext messageContext) {
        try {
            // Handle message
            service.handleMessage(messageContext);
        } catch (Exception e) {
            logger.error("Error handling message", e);
            // Don't ack on error - let Dapr retry
        } finally {
            subscriber.sendAck(messageContext.getDeliveryTag());
        }
    }
}
```

**Good Examples**:
- `ExperimentMessageHandler` - handles experiment events
- `ProcessLaunchMessageHandler` - handles process launch/termination

## Workflow Definitions

Use `DaprWorkflowDefinition` interface and `WorkflowNaming` utility for workflow structure.

**Location**:
- `org.apache.airavata.dapr.workflow.DaprWorkflowDefinition` (interface)
- `org.apache.airavata.dapr.workflow.WorkflowNaming` (naming utility)

**Workflow Naming**:
```java
// ✅ Good - use WorkflowNaming
String workflowId = WorkflowNaming.preWorkflow(processId);
// Result: "process-123-PRE-a1b2c3d4-e5f6-..."

// ❌ Bad - manual UUID generation
String workflowId = processId + "-PRE-" + UUID.randomUUID();
```

**Workflow Types**:
- `WorkflowNaming.TYPE_PRE` - Pre-workflow (before process execution)
- `WorkflowNaming.TYPE_POST` - Post-workflow (after process execution)
- `WorkflowNaming.TYPE_CANCEL` - Cancel workflow
- `WorkflowNaming.TYPE_PARSING` - Parsing workflow

## Configuration

Use `DaprConfigConstants` for all configuration property keys.

**Location**: `org.apache.airavata.dapr.config.DaprConfigConstants`

**Usage**:
```java
// ✅ Good - use constants
@Value("${" + DaprConfigConstants.DAPR_ENABLED + ":false}")
private boolean daprEnabled;

// ❌ Bad - hardcoded strings
@Value("${" + DaprConfigConstants.DAPR_ENABLED + ":false}")
private boolean daprEnabled;
```

**Available Constants**:
- `DaprConfigConstants.DAPR_ENABLED` - Enable/disable Dapr
- `DaprConfigConstants.DAPR_PUBSUB_NAME` - Pub/Sub component name
- `DaprConfigConstants.DAPR_STATE_NAME` - State store component name
- `DaprConfigConstants.DEFAULT_PUBSUB_NAME` - Default pub/sub name
- `DaprConfigConstants.DEFAULT_STATE_NAME` - Default state name

## State Transitions

Always use `StateTransitionService.validateAndLog()` before state transitions.

**Location**: `org.apache.airavata.statemachine.StateTransitionService`

**Usage**:
```java
// Get current state
ProcessState currentState = registryService.getProcessStatus(processId).getState();

// Validate and log transition
if (!StateTransitionService.validateAndLog(
        ProcessStateValidator.INSTANCE, 
        currentState, 
        newState, 
        processId, 
        "process")) {
    logger.warn("Invalid state transition rejected");
    return;
}

// Proceed with state update
registryService.updateProcessStatus(new ProcessStatus(newState), processId);
```

**Validators**:
- `ProcessStateValidator.INSTANCE` - Process state transitions
- `ExperimentStateValidator.INSTANCE` - Experiment state transitions
- `TaskStateValidator.INSTANCE` - Task state transitions
- `JobStateValidator.INSTANCE` - Job state transitions

## Best Practices

1. **Always use constants** - Never hardcode topic names, state keys, or config keys
2. **Extract handlers** - Move anonymous message handlers to named classes
3. **Use abstractions** - Prefer `DaprStateManager` over direct `DaprClient` usage
4. **Validate transitions** - Always validate state transitions before applying
5. **Consistent naming** - Use `WorkflowNaming` for workflow IDs
6. **Error handling** - Log errors but don't throw in message handlers (let Dapr retry)
7. **Documentation** - Add JavaDoc comments explaining Dapr usage patterns

## Finding Patterns

All Dapr-related code follows these package conventions:
- `org.apache.airavata.dapr.messaging.*` - Pub/Sub messaging
- `org.apache.airavata.dapr.state.*` - State management
- `org.apache.airavata.dapr.workflow.*` - Workflow definitions
- `org.apache.airavata.dapr.config.*` - Configuration constants
- `org.apache.airavata.dapr.patterns.*` - Pattern documentation

## Examples

See the following files for reference implementations:
- `OrchestratorService` - Service with Dapr messaging and state
- `ExperimentMessageHandler` - Extracted message handler
- `PreWorkflowManager` - Workflow manager using standardized naming
- `DaprStateManagerImpl` - State management implementation
