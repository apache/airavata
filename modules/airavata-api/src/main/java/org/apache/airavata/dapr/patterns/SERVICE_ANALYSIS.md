# Service vs Task Pattern Analysis

## Analysis Summary

This document analyzes whether certain services should follow task patterns or remain as services.

### OrchestratorService

**Current State**: Service that orchestrates experiment and process lifecycles

**Dapr Usage**: 
- Uses Dapr messaging for experiment launch events
- Uses Dapr state management for cancel operations
- Publishes status updates via Dapr Pub/Sub

**Should it be a Task?** No

**Rationale**:
- OrchestratorService is a coordination service, not a unit of work
- It orchestrates workflows and delegates actual work to tasks
- It handles lifecycle management and event routing
- It's properly structured as a service with Dapr messaging integration

**Recommendation**: Keep as service. Current Dapr integration pattern is appropriate.

### Metascheduler (ProcessScheduler)

**Current State**: Component interface used by OrchestratorService

**Dapr Usage**: None (delegated through OrchestratorService)

**Should it be a Task?** No

**Rationale**:
- ProcessScheduler is a component/utility used by OrchestratorService
- It's not a standalone service
- Scheduling logic is synchronous and part of orchestration flow
- No need for separate Dapr integration

**Recommendation**: Keep as component interface. No changes needed.

### AccountProvisioning

**Current State**: Utility service for SSH account provisioning

**Dapr Usage**: None

**Should it use Dapr?** Optional

**Rationale**:
- AccountProvisioning is primarily a utility for account management
- Currently synchronous operations
- Could benefit from Dapr messaging if:
  - Account provisioning becomes async
  - Multiple provisioning steps are needed
  - Status updates need to be published
- Current synchronous pattern is acceptable

**Recommendation**: 
- Keep as utility service for now
- Consider Dapr integration if async provisioning is needed
- If integrated, follow message handler patterns defined in this package

## Pattern Decision Guidelines

### Use Service Pattern When:
- Component coordinates or orchestrates other components
- Provides lifecycle management
- Acts as a gateway or facade
- Handles event routing and delegation

### Use Task Pattern When:
- Represents a unit of work that can be executed independently
- Needs retry logic and error handling
- Can be part of a workflow DAG
- Has well-defined input/output

### Use Dapr Integration When:
- Async communication is needed
- State needs to be shared across components
- Event-driven architecture is desired
- Distributed coordination is required
