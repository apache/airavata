# Dapr Workflow and Experiment State Machine Spec

This document specifies: (1) all state transitions for Experiment, Process, Task, and Job and the conditions under which they occur; (2) the Dapr-based reimplementation of the experiment flow so the state machine is properly encoded and Helix can be fully removed.

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
| COMPLETED  | 7    | All processes completed successfully             |
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

| State               | Value | Description                                  |
|---------------------|-------|----------------------------------------------|
| CREATED             | 0     | Initial after process create                 |
| VALIDATED           | 1     | Validation passed                            |
| STARTED             | 2     | Pre-workflow launched (PreWorkflowManager)   |
| PRE_PROCESSING      | 3     | Env / config / staging (pre-workflow tasks)  |
| CONFIGURING_WORKSPACE | 4   | (substep)                                    |
| INPUT_DATA_STAGING  | 5     | Input staging task                           |
| EXECUTING           | 6     | Job submitted and running                    |
| MONITORING          | 7     | Job monitored                                |
| OUTPUT_DATA_STAGING | 8     | Output staging (post-workflow)               |
| POST_PROCESSING     | 9     | Completing / parsing (post-workflow)         |
| COMPLETED           | 10    | Process finished successfully                |
| FAILED              | 11    | Process failed                               |
| CANCELLING          | 12    | Cancel workflow running                      |
| CANCELED            | 13    | Cancel completed                             |
| QUEUED              | 14    | Compute not ready; queued                    |
| DEQUEUING           | 15    | About to launch from queue                   |
| REQUEUED            | 16    | Job requeued for resubmit                    |

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

## 2. Current Helix-Based Flow (Summary)

- **HelixController:** starts `HelixControllerMain` (ZkHelixManager) for the cluster; manages cluster state in ZK.
- **HelixParticipant:** connects to same cluster, runs `TaskStateModelFactory`; receives tasks by `@TaskDef name`, deserializes `AbstractTask`, runs `task.run()`.
- **WorkflowManager:** holds `WorkflowOperator`(s). `WorkflowOperator` uses Helix `TaskDriver` to submit a `Workflow` of `JobConfig`(s); each job has `TaskConfig` with `command=TaskDef.name` and serialized task params. Parent-child dependencies via `OutPort`/`addParentChildDependency`.
- **PreWorkflowManager:** subscribes to `process-topic` (Type.PROCESS_LAUNCH). On `ProcessSubmitEvent` builds a chain of `AiravataTask` (EnvSetup, InputDataStaging, JobSubmission, optional Completing for intermediate); calls `getWorkflowOperator().launchWorkflow(..., tasks, ...)` and `publishProcessStatus(STARTED)`.
- **PostWorkflowManager:** implements `DaprJobStatusHandler`; receives `monitoring-job-status-topic`. On `JobState.COMPLETE` or `FAILED` runs `executePostWorkflow`: JobVerification → OutputDataStaging/Archive → Completing → ParsingTriggering; each step is a Helix workflow via `getWorkflowOperator().launchWorkflow`.
- **ParserWorkflowManager:** subscribes to `parsing-data-topic`; runs parsing workflow (Helix) for `ProcessCompletionMessage`.
- **WorkflowCleanupAgent:** periodically cleans finished Helix workflows.
- **Experiment status:** `OrchestratorService` subscribes to `status-topic`; on `ProcessStatusChangeEvent` runs `handleProcessStatusChange` and maps ProcessState to ExperimentState per the table in §1.1.

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

**Activity name** can match `@TaskDef name` for easier mapping:

- `EnvSetupActivity` ← EnvSetupTask  
- `InputDataStagingActivity` ← InputDataStagingTask  
- `JobSubmissionActivity` ← JobSubmissionTask (SLURM, etc.)  
- `OutputDataStagingActivity` ← OutputDataStagingTask  
- `ArchiveActivity` ← ArchiveTask  
- `JobVerificationActivity` ← JobVerificationTask  
- `CompletingActivity` ← CompletingTask  
- `ParsingTriggeringActivity` ← ParsingTriggeringTask  
- `DataParsingActivity` ← DataParsingTask (parser)  
- `WorkflowCancellationActivity` ← WorkflowCancellationTask (target: Dapr workflow instance IDs)  
- `RemoteJobCancellationActivity` ← RemoteJobCancellationTask  
- `CancelCompletingActivity` ← CancelCompletingTask  

Activities must be registered with `WorkflowRuntimeBuilder.registerActivity(...)`.

### 3.4 State Machine Enforcement in Dapr Layer

- **Experiment:** `OrchestratorService.handleProcessStatusChange` already encodes the ExperimentState rules. No change to the rules; ensure it remains the single place that applies experiment transitions from process events. Optionally introduce `ExperimentStateValidator.isValid(prev, next)` and call it before `updateAndPublishExperimentStatus`.
- **Process:** Before `publishProcessStatus(processId, experimentId, gatewayId, state)` or `registry.updateProcessStatus`, call `ProcessStateValidator.isValid(currentProcessState, state)`. Add `ProcessStateValidator` with a transition matrix derived from §1.2 (and `StateMachineTestUtils.getSuccessfulProcessStateSequence`). If invalid, log and skip or fail the activity.
- **Job:** Already enforced by `JobStateValidator.isValid` in PostWorkflowManager before `saveAndPublishJobStatus`; keep as-is.
- **Task:** Optionally add `TaskStateValidator` for CREATED→EXECUTING→COMPLETED|FAILED|CANCELED. Less critical if workflow structure guarantees order.

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

### 3.6 Scheduling and Entry Points

- **ProcessPreWorkflow:**  
  - **Option A:** process-topic subscriber (replacing `PreWorkflowManager.ProcessLaunchMessageHandler`): on `ProcessSubmitEvent`, call `DaprWorkflowClient.scheduleNewWorkflow(ProcessPreWorkflow.class, event)` and ack.  
  - **Option B:** Keep a thin `ProcessLaunchMessageHandler` in a new `DaprProcessLaunchHandler` that only translates pub/sub → `DaprWorkflowClient.scheduleNewWorkflow`.

- **ProcessPostWorkflow:**  
  - From `PostWorkflowManager` (or a renamed `DaprJobStatusHandler` implementation): when `JobState.COMPLETE` or `FAILED`, after `JobStateValidator.isValid` and `saveAndPublishJobStatus`, call `DaprWorkflowClient.scheduleNewWorkflow(ProcessPostWorkflow.class, new ProcessPostInput(processId, experimentId, gatewayId, forceRun))`.

- **ProcessCancelWorkflow:**  
  - process-topic handler for `ProcessTerminateEvent`: `DaprWorkflowClient.scheduleNewWorkflow(ProcessCancelWorkflow.class, event)`.

- **ParsingWorkflow:**  
  - parsing-data-topic handler (or `DaprParsingHandler`): on `ProcessCompletionMessage`, `DaprWorkflowClient.scheduleNewWorkflow(ParsingWorkflow.class, message)`.

### 3.7 WorkflowRuntime and DaprWorkflowClient

- **WorkflowRuntime:** Built with `WorkflowRuntimeBuilder`, register all workflows and activities. Start in the same process as the API (e.g. in a `@PostConstruct` or `ApplicationRunner` when `airavata.services.controller` and prewm/postwm are enabled). `runtime.start(false)` to pull and run workflows.
- **DaprWorkflowClient:** One per app or injectable; used by process-topic handler, `DaprJobStatusHandler`, and `DaprParsingHandler` to `scheduleNewWorkflow`. Instance ID can be `"pre-" + processId + "-" + UUID` etc., for idempotency and cleanup.
- **Cleanup:** Replace `WorkflowCleanupAgent` with a simple scheduler that queries Dapr for completed workflow instances (if Dapr exposes it) or maintains a TTL in Redis/Dapr state for instance IDs and forgets them. Optional and can be done in a follow-up.

### 3.8 Configuration and Feature Flags

- `airavata.services.controller.enabled`: when true, start WorkflowRuntime and Dapr workflow/activity registration; when false, do not.
- `airavata.services.prewm.enabled` / `airavata.services.postwm.enabled`: as today, gate Pre/Post behavior. In Dapr design, they gate whether we subscribe to process-topic and run ProcessPreWorkflow, and whether we run ProcessPostWorkflow from job-status.
- Remove all `airavata.helix.*` (or leave unused and deprecated): `airavata.helix.zookeeper-connection`, `airavata.helix.cluster.name`, etc. Remove `Helix` record from `AiravataServerProperties` once Helix is gone.

---

## 4. Files to Create

| Path | Purpose |
|------|---------|
| `org.apache.airavata.workflow.dapr.ProcessPreWorkflow` | Dapr Workflow for pre-process (env, input staging, job submission). |
| `org.apache.airavata.workflow.dapr.ProcessPostWorkflow` | Dapr Workflow for post-process (verify, output staging, completing, parsing trigger). |
| `org.apache.airavata.workflow.dapr.ProcessCancelWorkflow` | Dapr Workflow for process cancel. |
| `org.apache.airavata.workflow.dapr.ParsingWorkflow` | Dapr Workflow for parsing. |
| `org.apache.airavata.workflow.dapr.activities.*` | One class per activity (EnvSetup, InputDataStaging, JobSubmission, OutputDataStaging, Archive, JobVerification, Completing, ParsingTriggering, DataParsing, WorkflowCancellation, RemoteJobCancellation, CancelCompleting). |
| `org.apache.airavata.workflow.dapr.DaprWorkflowRuntimeConfig` | `@Configuration` to build `WorkflowRuntime`, register workflows/activities, and start runtime when controller is enabled. |
| `org.apache.airavata.workflow.dapr.DaprWorkflowClientHolder` or `@Bean DaprWorkflowClient` | Bean for `DaprWorkflowClient` used by handlers. |
| `org.apache.airavata.orchestrator.validator.ProcessStateValidator` | `isValid(ProcessState from, ProcessState to)`; used before process status updates. |
| `org.apache.airavata.orchestrator.validator.ExperimentStateValidator` | Optional: `isValid(ExperimentState from, ExperimentState to)` per §1.1; call before `updateAndPublishExperimentStatus`. |

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

## 6. Files to Delete

| Path | Note |
|------|------|
| `HelixController.java` | Replaced by Dapr Workflow runtime (no cluster controller). |
| `HelixParticipant.java` | Replaced by Dapr Activities. |
| `GlobalParticipant.java` | Same. |
| `WorkflowOperator.java` | Replaced by `DaprWorkflowClient` + Dapr Workflow definitions. |
| `WorkflowCleanupAgent.java` | Replace with optional Dapr/Redis-based cleanup or remove. |
| `QueueOperator.java` | If it only wraps Helix; else adapt. |

All `org.apache.helix` and `org.apache.helix.zookeeper` and Curator dependencies: remove from `pom.xml`. Remove Zookeeper/Helix from Ansible and deployment docs.

---

## 7. Task/Activity Migration Notes

- **AbstractTask / AiravataTask:** The `run()` logic moves into activities. `TaskUtil.serializeTaskData` / `paramMap` are replaced by activity input DTOs. `TaskDef` can be dropped or kept only for naming activity. `OutPort`/parent-child is expressed by the order of `ctx.callActivity(...).await()` in the workflow.
- **TaskHelper / TaskHelperImpl:** If they only support Helix task context, remove or refactor for activity context (e.g. pass `processId`, `registryService` into activities).
- **HelixTaskFactory, SlurmTaskFactory, AWSTaskFactory:** The *creation* of the chain (which tasks for which `TaskTypes`) moves into `ProcessPreWorkflow` / `ProcessPostWorkflow`; the *execution* of each step is in activities. Factories can become “activity input builders” or be inlined into the workflow.
- **Parsing:** `ParsingWorkflow` runs `DataParsingActivity`; `ParserWorkflowManager`’s current logic moves there. `DaprParsingHandler` only receives the pub/sub message and starts the workflow.

---

## 8. Summary

- **State machines:** Experiment and Process transitions are documented in §1; Job is already in `JobStateValidator`. Introduce `ProcessStateValidator` and optionally `ExperimentStateValidator` and call them before persisting.
- **Helix removal:** Delete HelixController, HelixParticipant, GlobalParticipant, WorkflowOperator, WorkflowCleanupAgent, and all Helix/ZK/Curator deps.
- **Dapr replacement:** ProcessPreWorkflow, ProcessPostWorkflow, ProcessCancelWorkflow, ParsingWorkflow as Dapr Workflows; current tasks as Dapr Activities; `DaprWorkflowClient.scheduleNewWorkflow` from process-topic, job-status, and parsing-data-topic handlers. `WorkflowRuntime` started when controller is enabled.
- **State machine in Dapr:** Validators gate all status writes; workflow and activity logic remains the source of *when* a transition is attempted, and validators enforce *whether* it is allowed.
