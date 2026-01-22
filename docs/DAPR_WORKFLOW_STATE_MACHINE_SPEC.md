# Dapr Workflow and Experiment State Machine Spec

This document specifies: (1) all state transitions for Experiment, Process, Task, and Job and the conditions under which they occur; (2) the Dapr-based implementation of the experiment flow with proper state machine encoding.

**Status:** ✅ Implementation Complete - All Dapr workflows and activities are implemented. Helix has been fully removed.

---

## 1. State Enums and Allowed Transitions

### 1.1 ExperimentState

| State      | Value | Description                                      |
|------------|-------|--------------------------------------------------|
| CREATED    | 0     | Initial state after experiment create            |
| VALIDATED  | 1     | Validation passed (pre-launch)                   |
| SCHEDULED  | 2     | Queued; compute not ready or job requeued        |
| LAUNCHED   | 3     | All processes submitted                          |
| EXECUTING  | 4     | At least one process in STARTED or beyond        |
| CANCELING  | 5     | User requested cancel; waiting for workflows     |
| CANCELED   | 6     | Cancel completed or process canceled             |
| COMPLETED  | 7     | All processes completed successfully             |
| FAILED     | 8     | Validation/registry error or process failed      |

**Experiment transition rules (from `OrchestratorService` and `handleProcessStatusChange`):**

| From       | To         | Condition |
|------------|------------|-----------|
| CREATED    | SCHEDULED  | `launchSingleAppExperiment`: `airavataAutoSchedule==true` and `processScheduler.canLaunch(experimentId)==false` (compute not ready). |
| CREATED    | LAUNCHED   | `launchSingleAppExperiment` returns true and `launchExperiment` calls `updateAndPublishExperimentStatus(LAUNCHED)`. |
| CREATED    | FAILED     | `launchExperiment` catches `LaunchValidationException`, `RegistryServiceException`, `ExperimentNotFoundException`, `RuntimeException` and sets FAILED. |
| SCHEDULED  | LAUNCHED   | `handleProcessStatusChange(DEQUEUING)` when not CANCELING: `launchQueuedExperiment` → LAUNCHED then `launchSingleAppExperimentInternal`. |
| SCHEDULED  | SCHEDULED  | `handleProcessStatusChange(QUEUED)` or `(REQUEUED)` (reasons: compute not available, requeued for resubmit). |
| LAUNCHED   | EXECUTING  | `handleProcessStatusChange(STARTED)` when not CANCELING. |
| LAUNCHED   | CANCELING  | `handleProcessStatusChange(STARTED)` when current experiment state is CANCELING. |
| EXECUTING  | COMPLETED  | `handleProcessStatusChange(COMPLETED)` when not CANCELING. |
| EXECUTING  | FAILED     | `handleProcessStatusChange(FAILED)` when not CANCELING. |
| EXECUTING  | CANCELED   | `handleProcessStatusChange(CANCELED)` or `(COMPLETED|FAILED)` when current state is CANCELING. |
| EXECUTING  | CANCELING  | `handleProcessStatusChange(DEQUEUING)` when current state is CANCELING. |
| EXECUTING  | SCHEDULED  | `handleProcessStatusChange(QUEUED)` or `(REQUEUED)` (e.g. job requeued). |
| CANCELING  | CANCELING  | `handleProcessStatusChange(STARTED)` or `(DEQUEUING)`—keep CANCELING. |
| CANCELING  | CANCELED   | `handleProcessStatusChange(COMPLETED)`, `(FAILED)`, or `(CANCELED)`. |
| *          | CANCELING  | `terminateExperiment` when state ∉ {COMPLETED, CANCELED, FAILED, CANCELING} and ≠ CREATED. |

**Terminal states:** COMPLETED, FAILED, CANCELED. No transitions out.

**ExperimentStateValidator (proposed) – allowed (from, to):**

- CREATED → SCHEDULED, LAUNCHED, FAILED
- SCHEDULED → LAUNCHED, SCHEDULED, CANCELING
- LAUNCHED → EXECUTING, CANCELING
- EXECUTING → COMPLETED, FAILED, CANCELED, SCHEDULED, CANCELING
- CANCELING → CANCELING, CANCELED
- COMPLETED, FAILED, CANCELED → (terminal)

---

### 1.2 ProcessState

| State                 | Value | Description                                  |
|-----------------------|-------|----------------------------------------------|
| CREATED               | 0     | Initial after process create                 |
| VALIDATED             | 1     | Validation passed                            |
| STARTED               | 2     | Pre-workflow launched (PreWorkflowManager)   |
| PRE_PROCESSING        | 3     | Env / config / staging (pre-workflow tasks)  |
| CONFIGURING_WORKSPACE | 4     | (substep)                                    |
| INPUT_DATA_STAGING    | 5     | Input staging task                           |
| EXECUTING             | 6     | Job submitted and running                    |
| MONITORING            | 7     | Job monitored                                |
| OUTPUT_DATA_STAGING   | 8     | Output staging (post-workflow)               |
| POST_PROCESSING       | 9     | Completing / parsing (post-workflow)         |
| COMPLETED             | 10    | Process finished successfully                |
| FAILED                | 11    | Process failed                               |
| CANCELLING            | 12    | Cancel workflow running                      |
| CANCELED              | 13    | Cancel completed                             |
| QUEUED                | 14    | Compute not ready; queued                    |
| DEQUEUING             | 15    | About to launch from queue                   |
| REQUEUED              | 16    | Job requeued for resubmit                    |

**Process transition rules (from WorkflowManagers, OrchestratorService, PostWorkflowManager):**

| From     | To        | Condition |
|----------|-----------|-----------|
| CREATED  | STARTED   | PreWorkflowManager `ProcessLaunchMessageHandler` after `createAndLaunchPreWorkflow`; `publishProcessStatus(STARTED)`. |
| CREATED  | CANCELLING| Cancel workflow started (e.g. `createAndLaunchCancelWorkflow`). |
| STARTED  | PRE_PROCESSING, CONFIGURING_WORKSPACE, INPUT_DATA_STAGING, EXECUTING | Driven by Helix tasks (EnvSetup, InputDataStaging, JobSubmission). In Dapr, same sequence via activities. |
| *        | EXECUTING | JobSubmissionTask succeeds; job running. |
| *        | QUEUED    | Job/compute reports queued or `handleProcessStatusChange(QUEUED/REQUEUED)` → experiment SCHEDULED. |
| *        | DEQUEUING | Scheduler signals dequeue; then `launchQueuedExperiment`. |
| *        | CANCELLING| Terminate/cancel initiated; cancel workflow running. |
| CANCELLING | CANCELED | Cancel workflow or job status CANCELED/COMPLETE/FAILED and process was CANCELLING (PostWorkflowManager). |
| EXECUTING (and post) | OUTPUT_DATA_STAGING, POST_PROCESSING, COMPLETED | PostWorkflowManager: on `JobState.COMPLETE` or `FAILED`, `executePostWorkflow` runs JobVerification → OutputDataStaging/Archive → Completing → ParsingTriggering. |
| *        | FAILED    | Task/workflow fails and not CANCELLING. |

**Successful process sequence (from `StateMachineTestUtils.getSuccessfulProcessStateSequence`):**  
CREATED → VALIDATED → STARTED → PRE_PROCESSING → CONFIGURING_WORKSPACE → INPUT_DATA_STAGING → EXECUTING → MONITORING → OUTPUT_DATA_STAGING → POST_PROCESSING → COMPLETED.

**ProcessStateValidator (proposed) – allowed (from, to):**

- CREATED → VALIDATED, STARTED, CANCELLING, FAILED
- VALIDATED → STARTED, CANCELLING, FAILED
- STARTED → PRE_PROCESSING, CONFIGURING_WORKSPACE, INPUT_DATA_STAGING, EXECUTING, QUEUED, CANCELLING, FAILED
- PRE_PROCESSING, CONFIGURING_WORKSPACE, INPUT_DATA_STAGING → same as STARTED plus prior steps
- EXECUTING → MONITORING, OUTPUT_DATA_STAGING, POST_PROCESSING, COMPLETED, FAILED, QUEUED, REQUEUED, CANCELLING, CANCELED
- MONITORING → OUTPUT_DATA_STAGING, POST_PROCESSING, COMPLETED, FAILED, CANCELLING, CANCELED
- OUTPUT_DATA_STAGING, POST_PROCESSING → COMPLETED, FAILED, CANCELLING, CANCELED
- QUEUED → DEQUEUING, EXECUTING, CANCELLING, CANCELED, FAILED
- REQUEUED → QUEUED, EXECUTING, CANCELLING, CANCELED, FAILED
- DEQUEUING → EXECUTING, QUEUED, CANCELLING, CANCELED
- CANCELLING → CANCELED, FAILED
- COMPLETED, FAILED, CANCELED → (terminal, no out-edges)

---

### 1.3 TaskState

| State     | Value | Description        |
|-----------|-------|--------------------|
| CREATED   | 0     | Initial            |
| EXECUTING | 1     | Task running       |
| COMPLETED | 2     | Task done          |
| FAILED    | 3     | Task failed        |
| CANCELED  | 4     | Task canceled      |

**Task transitions:** Implicit in AiravataTask: CREATED → EXECUTING → COMPLETED | FAILED | CANCELED. No explicit validator; order enforced by workflow/activity sequencing.

---

### 1.4 JobState (JobStateValidator)

| State           | Value | Description                    |
|-----------------|-------|--------------------------------|
| SUBMITTED       | 0     | Submitted to compute           |
| QUEUED          | 1     | Queued on cluster              |
| ACTIVE          | 2     | Running                        |
| COMPLETE        | 3     | Finished successfully          |
| CANCELED        | 4     | Canceled                       |
| FAILED          | 5     | Failed                         |
| SUSPENDED       | 6     | Suspended                      |
| UNKNOWN         | 7     | Unknown                        |
| NON_CRITICAL_FAIL | 8   | Recoverable; can requeue       |

**Job transition matrix (JobStateValidator):**

- **SUBMITTED** → QUEUED, ACTIVE, COMPLETE, CANCELED, FAILED, SUSPENDED, UNKNOWN, NON_CRITICAL_FAIL
- **QUEUED** → ACTIVE, COMPLETE, CANCELED, FAILED, SUSPENDED, UNKNOWN, NON_CRITICAL_FAIL
- **ACTIVE** → COMPLETE, CANCELED, FAILED, SUSPENDED, UNKNOWN, NON_CRITICAL_FAIL
- **NON_CRITICAL_FAIL** → QUEUED, ACTIVE, COMPLETE, CANCELED, FAILED, SUSPENDED, UNKNOWN
- **COMPLETE, CANCELED, FAILED, SUSPENDED, UNKNOWN** → no transitions (terminal)

PostWorkflowManager enforces these via `JobStateValidator.isValid(prev, next)` before `saveAndPublishJobStatus`.

---

## 2. Current Dapr-Based Flow (Implemented)

- **Dapr Workflow Runtime:** Started via `DaprWorkflowRuntimeConfig` when `airavata.services.controller.enabled=true`. Registers all workflows and activities, manages workflow execution.
- **Dapr Activities:** Execute task logic (EnvSetup, InputDataStaging, JobSubmission, etc.) as Dapr activities. Activities are registered with the workflow runtime and called by workflows.
- **PreWorkflowManager:** Subscribes to `process-topic` (Type.PROCESS_LAUNCH). On `ProcessSubmitEvent`, schedules `ProcessPreWorkflow` via `DaprWorkflowClient.scheduleNewWorkflow()` and publishes `ProcessStatus(STARTED)`.
- **PostWorkflowManager:** Implements `DaprJobStatusHandler`; receives `monitoring-job-status-topic`. On `JobState.COMPLETE` or `FAILED`, schedules `ProcessPostWorkflow` via `DaprWorkflowClient.scheduleNewWorkflow()`.
- **ParserWorkflowManager:** Subscribes to `parsing-data-topic`; schedules `ParsingWorkflow` via `DaprWorkflowClient.scheduleNewWorkflow()` for `ProcessCompletionMessage`.
- **ProcessCancelWorkflow:** Scheduled by `PreWorkflowManager` on `ProcessTerminateEvent` to cancel running workflows and jobs.
- **Experiment status:** `OrchestratorService` subscribes to `status-topic`; on `ProcessStatusChangeEvent` runs `handleProcessStatusChange` and maps ProcessState to ExperimentState per the table in §1.1. State transitions are validated using `ExperimentStateValidator` and `ProcessStateValidator`.

---

## 3. Dapr Workflow Reimplementation

### 3.1 Principles

- **Remove:** HelixController, HelixParticipant, GlobalParticipant, WorkflowOperator, WorkflowManager’s ZK/Helix admin and `getWorkflowOperator()`, WorkflowCleanupAgent, and all `org.apache.helix` / ZK/Curator usage.
- **Replace with:** Dapr Workflow (durable orchestration) and Dapr Activities (replacing Helix task execution). State transitions are applied only when the state machine allows them.
- **Keep:** Pub/Sub (experiment-topic, process-topic, status-topic, monitoring-job-status-topic, parsing-data-topic), DaprSubscriptionController, RegistryService, and the *logic* inside current AiravataTask implementations (refactored into activities). JobStateValidator stays for Job transitions.

### 3.2 Dapr Workflows

| Workflow                  | Trigger                        | Input                     | Role |
|---------------------------|--------------------------------|---------------------------|------|
| **ExperimentWorkflow**    | experiment-topic (or direct)   | `ExperimentSubmitEvent`   | Optional: wrap launch in a workflow; or keep `OrchestratorService.handleLaunchExperiment` as-is and only replace process/parsing workflows. |
| **ProcessPreWorkflow**    | process-topic (LAUNCHPROCESS)  | `ProcessSubmitEvent`      | Replaces PreWorkflowManager’s `createAndLaunchPreWorkflow` + `WorkflowOperator.launchWorkflow`. Runs: EnvSetup → InputDataStaging → JobSubmission [→ Completing for intermediate]. |
| **ProcessPostWorkflow**   | Internal (from job status)     | `{ processId, experimentId, gatewayId, forceRun }` | Replaces PostWorkflowManager’s `executePostWorkflow` + `WorkflowOperator.launchWorkflow`. Runs: JobVerification → OutputDataStaging/Archive → Completing → ParsingTriggering. |
| **ProcessCancelWorkflow** | process-topic (TERMINATEPROCESS) | `ProcessTerminateEvent` | Replaces `createAndLaunchCancelWorkflow` + Helix cancel workflow. Runs: WorkflowCancellation (for any Dapr workflow IDs) → RemoteJobCancellation (if SLURM) → CancelCompleting. |
| **ParsingWorkflow**       | parsing-data-topic             | `ProcessCompletionMessage`| Replaces ParserWorkflowManager’s Helix parsing workflow. |

For a minimal first cut, **ExperimentWorkflow** can be omitted: `OrchestratorService` continues to handle experiment-topic and call `launchExperimentInternal` / `launchSingleAppExperimentInternal`, which will publish to process-topic. The Dapr change is at the **process** level: process-topic and post-workflow entry points start Dapr workflows instead of Helix.

### 3.3 Dapr Activities (replace Helix tasks)

Each current `AiravataTask` (or equivalent) becomes an **Activity** that:

- Receives an input DTO (processId, experimentId, gatewayId, taskId, plus task-specific params).
- Injects `RegistryService`, `CredentialStoreService`, etc. (via Spring or Dapr’s activity context).
- Reuses the core logic of the existing task (e.g. `JobSubmissionTask.run()`, `OutputDataStagingTask.run()`).
- Updates process/task status and publishes to status-topic only when the **state machine** allows the transition. If a transition is invalid, the activity can throw or return a failure and let the workflow retry or fail.

**Activity mapping** (task → activity):

| Activity | Source Task | Package |
|----------|-------------|---------|
| `EnvSetupActivity` | EnvSetupTask | `org.apache.airavata.activities.process.pre` |
| `InputDataStagingActivity` | InputDataStagingTask | `org.apache.airavata.activities.process.pre` |
| `JobSubmissionActivity` | JobSubmissionTask (SLURM, etc.) | `org.apache.airavata.activities.process.pre` |
| `OutputDataStagingActivity` | OutputDataStagingTask | `org.apache.airavata.activities.process.post` |
| `ArchiveActivity` | ArchiveTask | `org.apache.airavata.activities.process.post` |
| `JobVerificationActivity` | JobVerificationTask | `org.apache.airavata.activities.process.post` |
| `ParsingTriggeringActivity` | ParsingTriggeringTask | `org.apache.airavata.activities.process.post` |
| `CompletingActivity` | CompletingTask | `org.apache.airavata.activities.shared` |
| `DataParsingActivity` | DataParsingTask (parser) | `org.apache.airavata.activities.parsing` |
| `WorkflowCancellationActivity` | WorkflowCancellationTask (Dapr workflow IDs) | `org.apache.airavata.activities.process.cancel` |
| `RemoteJobCancellationActivity` | RemoteJobCancellationTask | `org.apache.airavata.activities.process.cancel` |
| `CancelCompletingActivity` | CancelCompletingTask | `org.apache.airavata.activities.process.cancel` |

Activities are registered with `WorkflowRuntimeBuilder.registerActivity(...)` in `DaprWorkflowRuntimeConfig` (`org.apache.airavata.orchestrator.internal.workflow`).

### 3.4 State Machine Enforcement in Dapr Layer

All state validators are in `org.apache.airavata.orchestrator.state`:

- **Experiment:** `ExperimentStateValidator.isValid(prev, next)` enforces experiment state transitions. Called via `OrchestratorService.handleProcessStatusChange` before `updateAndPublishExperimentStatus`.
- **Process:** `ProcessStateValidator.isValid(currentProcessState, state)` enforces process state transitions. Called before `publishProcessStatus(processId, experimentId, gatewayId, state)` or `registry.updateProcessStatus`. Transition matrix derived from §1.2.
- **Job:** `JobStateValidator.isValid` enforced in PostWorkflowManager before `saveAndPublishJobStatus`.
- **Task:** `TaskStateValidator` enforces CREATED→EXECUTING→COMPLETED|FAILED|CANCELED transitions.

### 3.5 Workflow Stub Examples (Pseudocode)

**ProcessPreWorkflow**

```
Input: ProcessSubmitEvent { processId, experimentId, gatewayId }
1. EnvSetupActivity(processId, ...)           // CREATED → PRE_PROCESSING / CONFIGURING_WORKSPACE as needed
2. InputDataStagingActivity(processId, ...)   // → INPUT_DATA_STAGING
3. JobSubmissionActivity(processId, ...)      // → EXECUTING (or QUEUED/REQUEUED; experiment → SCHEDULED)
   - If intermediateOutput: CompletingActivity(processId, ...)
4. publishProcessStatus(STARTED) at start; other process states inside activities as today.
5. ctx.complete()
```

**ProcessPostWorkflow**

```
Input: { processId, experimentId, gatewayId, forceRun }
1. JobVerificationActivity(processId, ...)
2. For each OUTPUT/ARCHIVE_OUTPUT: OutputDataStagingActivity or ArchiveActivity(processId, ...)
3. CompletingActivity(processId, ...)         // → COMPLETED
4. ParsingTriggeringActivity(processId, ...)
5. ctx.complete()
```

**ProcessCancelWorkflow**

```
Input: ProcessTerminateEvent { processId, gatewayId }
1. WorkflowCancellationActivity(processId)    // cancel Dapr workflow instances for this process
2. RemoteJobCancellationActivity(processId, gatewayId) if SLURM
3. CancelCompletingActivity(processId, gatewayId)
4. ctx.complete()
```

### 3.6 Scheduling and Entry Points (Implemented)

- **ProcessPreWorkflow:**  
  - Implemented in `PreWorkflowManager.createAndLaunchPreWorkflow()`: on `ProcessSubmitEvent` from `ProcessLaunchMessageHandler`, calls `DaprWorkflowClient.scheduleNewWorkflow(ProcessPreWorkflow.class, workflowInstanceId, event)`.

- **ProcessPostWorkflow:**  
  - Implemented in `PostWorkflowManager.executePostWorkflow()`: when `JobState.COMPLETE` or `FAILED`, after `JobStateValidator.isValid` and `saveAndPublishJobStatus`, calls `DaprWorkflowClient.scheduleNewWorkflow(ProcessPostWorkflow.class, workflowInstanceId, input)`.

- **ProcessCancelWorkflow:**  
  - Implemented in `PreWorkflowManager.createAndLaunchCancelWorkflow()`: process-topic handler for `ProcessTerminateEvent` calls `DaprWorkflowClient.scheduleNewWorkflow(ProcessCancelWorkflow.class, workflowInstanceId, event)`.

- **ParsingWorkflow:**  
  - Implemented in `ParserWorkflowManager.process()`: on `ProcessCompletionMessage` from `DaprParsingHandler`, calls `DaprWorkflowClient.scheduleNewWorkflow(ParsingWorkflow.class, workflowInstanceId, message)`.

### 3.7 WorkflowRuntime and DaprWorkflowClient (Implemented)

- **WorkflowRuntime:** Implemented in `DaprWorkflowRuntimeConfig`. Built with `WorkflowRuntimeBuilder`, registers all workflows and activities. Started via `@PostConstruct` when `airavata.services.controller.enabled=true`. `runtime.start(false)` runs workflows non-blocking.
- **DaprWorkflowClient:** Implemented in `DaprWorkflowClientHolder` as a Spring bean. Injected into workflow managers and used to `scheduleNewWorkflow`. Instance IDs use `WorkflowNaming` utility (e.g., `processId-PRE-uuid`, `processId-POST-uuid`).
- **Cleanup:** WorkflowCleanupAgent removed. Dapr workflows are self-managing; completed workflows can be queried via Dapr API if needed. Workflow instance IDs are registered with processes via `registerWorkflowForProcess()` for tracking.

### 3.8 Configuration and Feature Flags (Implemented)

- `airavata.services.controller.enabled`: when true, starts WorkflowRuntime and Dapr workflow/activity registration via `DaprWorkflowRuntimeConfig`; when false, workflows are not available.
- `airavata.services.prewm.enabled` / `airavata.services.postwm.enabled`: gate Pre/Post behavior. When enabled, managers subscribe to topics and schedule Dapr workflows.
- `airavata.dapr.enabled`: enables Dapr client and state management.
- `airavata.dapr.state.name`: Dapr state store component name (default: `redis-state`).
- All `airavata.helix.*` properties removed. Helix dependencies removed from `pom.xml`.

---

## 4. Files Created (Implementation Complete)

### Dapr Workflows

| Class | Package | Status |
|-------|---------|--------|
| `ProcessPreWorkflow` | `org.apache.airavata.workflow.process.pre` | ✅ Implemented |
| `ProcessPostWorkflow` | `org.apache.airavata.workflow.process.post` | ✅ Implemented |
| `ProcessCancelWorkflow` | `org.apache.airavata.workflow.process.cancel` | ✅ Implemented |
| `ParsingWorkflow` | `org.apache.airavata.workflow.process.parsing` | ✅ Implemented |

### Workflow Managers

| Class | Package | Status |
|-------|---------|--------|
| `PreWorkflowManager` | `org.apache.airavata.workflow.process.pre` | ✅ Implemented |
| `PostWorkflowManager` | `org.apache.airavata.workflow.process.post` | ✅ Implemented |
| `ParserWorkflowManager` | `org.apache.airavata.workflow.process.parsing` | ✅ Implemented |
| `WorkflowManager` | `org.apache.airavata.workflow.common` | ✅ Implemented |

### Dapr Activities

Activities are organized by function in `org.apache.airavata.activities.*`:

| Category | Activities | Package |
|----------|-----------|---------|
| **Pre-Processing** | `EnvSetupActivity`, `InputDataStagingActivity`, `JobSubmissionActivity` | `org.apache.airavata.activities.process.pre` |
| **Post-Processing** | `OutputDataStagingActivity`, `ArchiveActivity`, `JobVerificationActivity`, `ParsingTriggeringActivity` | `org.apache.airavata.activities.process.post` |
| **Cancellation** | `WorkflowCancellationActivity`, `RemoteJobCancellationActivity`, `CancelCompletingActivity` | `org.apache.airavata.activities.process.cancel` |
| **Parsing** | `DataParsingActivity` | `org.apache.airavata.activities.parsing` |
| **Monitoring** | `ClusterStatusMonitorActivity`, `ComputeMonitorActivity`, `DataAnalyzerActivity` | `org.apache.airavata.activities.monitoring.*` |
| **Scheduling** | `ProcessScannerActivity` | `org.apache.airavata.activities.scheduling` |
| **Shared** | `CompletingActivity`, `TaskExecutorHelper`, `BaseActivityInput` | `org.apache.airavata.activities.shared` |

### Dapr Runtime Configuration

| Class | Package | Status |
|-------|---------|--------|
| `DaprWorkflowRuntimeConfig` | `org.apache.airavata.orchestrator.internal.workflow` | ✅ Implemented |
| `DaprWorkflowClientHolder` | `org.apache.airavata.orchestrator.internal.workflow` | ✅ Implemented |
| `ProcessStatusUpdateHelper` | `org.apache.airavata.orchestrator.internal.workflow` | ✅ Implemented |

### State Validators

| Class | Package | Status |
|-------|---------|--------|
| `ProcessStateValidator` | `org.apache.airavata.orchestrator.state` | ✅ Implemented |
| `ExperimentStateValidator` | `org.apache.airavata.orchestrator.state` | ✅ Implemented |
| `JobStateValidator` | `org.apache.airavata.orchestrator.state` | ✅ Implemented |
| `TaskStateValidator` | `org.apache.airavata.orchestrator.state` | ✅ Implemented |
| `StateValidator` | `org.apache.airavata.orchestrator.state` | ✅ Implemented (interface) |

---

## 5. Files to Modify

| Path | Changes |
|------|---------|
| `PreWorkflowManager` | Remove `createAndLaunchPreWorkflow`’s `getWorkflowOperator().launchWorkflow` and Helix-specific code. Replace with `DaprWorkflowClient.scheduleNewWorkflow(ProcessPreWorkflow.class, event)`. Keep `ProcessLaunchMessageHandler` and `publishProcessStatus(STARTED)`; move logic that builds the task chain into `ProcessPreWorkflow` and activities. |
| `PostWorkflowManager` | In `executePostWorkflow`, remove `getWorkflowOperator().launchWorkflow`. Replace with `DaprWorkflowClient.scheduleNewWorkflow(ProcessPostWorkflow.class, input)`. Keep `onJobStatusMessage`, `JobStateValidator`, `saveAndPublishJobStatus`, and `publishProcessStatus`. |
| `ParserWorkflowManager` | Remove Helix workflow launch. On parsing-data-topic, call `DaprWorkflowClient.scheduleNewWorkflow(ParsingWorkflow.class, message)`. |
| `WorkflowManager` | Remove `initHelixAdmin`, `initWorkflowOperators`, `getWorkflowOperator`, `zkHelixAdmin`, `workflowOperators`, `loadBalanceClusters`, and all `org.apache.helix`/ZK usage. Keep `publishProcessStatus`, `registerWorkflowForProcess` (adapt to store Dapr workflow instance ID if needed), `getStatusPublisher`, `normalizeTaskId`, `getRegistryService`. |
| `OrchestratorService` | `handleProcessStatusChange` unchanged in rules; optionally call `ExperimentStateValidator.isValid` before `updateAndPublishExperimentStatus`. Remove any remaining Helix/ZK references. |
| `AiravataServerProperties` | Remove or deprecate `Helix` record and `airavata.helix.*` properties. |
| `application.properties` | Remove `airavata.helix.*`. |
| `DaprParsingHandler` | Call `DaprWorkflowClient.scheduleNewWorkflow(ParsingWorkflow.class, msg)` instead of starting a Helix workflow. |
| New process-topic handler or `PreWorkflowManager` | Ensure `ProcessTerminateEvent` triggers `DaprWorkflowClient.scheduleNewWorkflow(ProcessCancelWorkflow.class, event)`. |

---

## 6. Files Deleted (Cleanup Complete)

| Path | Status |
|------|--------|
| `HelixController.java` | ✅ Removed (replaced by Dapr Workflow runtime) |
| `HelixParticipant.java` | ✅ Removed (replaced by Dapr Activities) |
| `GlobalParticipant.java` | ✅ Removed (replaced by Dapr Activities) |
| `WorkflowOperator.java` | ✅ Removed (replaced by `DaprWorkflowClient` + Dapr Workflow definitions) |
| `WorkflowCleanupAgent.java` | ✅ Removed (Dapr workflows are self-managing) |

All `org.apache.helix` and `org.apache.helix.zookeeper` and Curator dependencies: ✅ Removed from `pom.xml`. Zookeeper/Helix references removed from Ansible and deployment docs.

---

## 7. Task/Activity Migration Notes

- **AbstractTask / AiravataTask:** The `run()` logic moves into activities. `TaskUtil.serializeTaskData` / `paramMap` are replaced by activity input DTOs. `TaskDef` can be dropped or kept only for naming activity. `OutPort`/parent-child is expressed by the order of `ctx.callActivity(...).await()` in the workflow.
- **TaskHelper / TaskHelperImpl:** If they only support Helix task context, remove or refactor for activity context (e.g. pass `processId`, `registryService` into activities).
- **HelixTaskFactory, SlurmTaskFactory, AWSTaskFactory:** The *creation* of the chain (which tasks for which `TaskTypes`) moves into `ProcessPreWorkflow` / `ProcessPostWorkflow`; the *execution* of each step is in activities. Factories can become “activity input builders” or be inlined into the workflow.
- **Parsing:** `ParsingWorkflow` runs `DataParsingActivity`; `ParserWorkflowManager`’s current logic moves there. `DaprParsingHandler` only receives the pub/sub message and starts the workflow.

---

## 8. Implementation Summary (Completed)

- **State machines:** ✅ Experiment and Process transitions documented in §1. `ProcessStateValidator` and `ExperimentStateValidator` implemented and integrated. `JobStateValidator` already existed. All validators called before status persistence.
- **Helix removal:** ✅ HelixController, HelixParticipant, GlobalParticipant, WorkflowOperator, WorkflowCleanupAgent deleted. All Helix/ZK/Curator dependencies removed from `pom.xml`.
- **Dapr replacement:** ✅ ProcessPreWorkflow, ProcessPostWorkflow, ProcessCancelWorkflow, ParsingWorkflow implemented as Dapr Workflows. All tasks implemented as Dapr Activities. `DaprWorkflowClient.scheduleNewWorkflow` called from process-topic, job-status, and parsing-data-topic handlers. `WorkflowRuntime` started via `DaprWorkflowRuntimeConfig` when controller is enabled.
- **State machine enforcement:** ✅ Validators (`ProcessStateValidator`, `ExperimentStateValidator`, `JobStateValidator`) gate all status writes. Workflow and activity logic determines *when* a transition is attempted; validators enforce *whether* it is allowed.
- **State management:** ✅ UserContentStore now uses Dapr State Store. Workflow instance tracking via DaprStateKeys. Process and experiment state persisted in Dapr State Store.
