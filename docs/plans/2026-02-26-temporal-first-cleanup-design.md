# Temporal-First Architecture & Legacy Cleanup

## Context

The codebase has manual `Thread.sleep` retry loops, custom backoff utilities, dead legacy stubs, and inconsistent HTTP patterns. This refactoring enforces a Temporal-first architecture: every operation that can fail or needs waiting becomes an atomic Temporal activity with a retry policy. No more `Thread.sleep` in production code.

## Architectural Principle

**No `Thread.sleep`, no manual retry loops, no custom backoff utilities.** Every retryable/waitable operation is a Temporal activity with a `RetryOptions` policy. `Workflow.sleep()` for durable timers. `continueAsNew` for infinite loops.

## Changes

### 1. DAG Execution — Each Node Becomes Its Own Activity

**Current:** Entire DAG runs as one 30-minute Temporal activity. `ProcessDAGEngine` loops through nodes in Java.

**New:** Workflow code orchestrates the DAG. Each node is a separate activity call with its own retry options. `dagState` (`Map<String, String>`) flows between activities as input/output.

- **Delete:** `ProcessDAGEngine` — loop moves to workflow, single-node logic moves to `ActivitiesImpl.executeDagNode()`
- **Delete:** `ExponentialBackoffWaiter` — Temporal retries replace it
- **Modify:** `ProcessActivity` workflows — add DAG traversal logic using `ProcessDAG`/`TaskNode`
- **Modify:** `Activities` interface — replace `executePreDag`/`executePostDag`/`executeCancelDag` with `executeDagNode(String processId, String gatewayId, String taskBeanName, Map<String, String> dagState)` returning updated dagState
- **Keep:** `DagTask` interface, all task beans, `ComputeProvider`, `TaskContextFactory`, interceptors, `ProcessDAG`, `TaskNode`, `DAGTemplates`

### 2. Retry Tiers Per Node Type

| Tier | Nodes | Attempts | Initial | Max | Backoff |
|------|-------|----------|---------|-----|---------|
| Infrastructure | provision, submit | 10 | 5s | 30s | 2.0x |
| Data | stageIn, outputStaging, archive | 3 | 5s | 15s | 2.0x |
| Check | checkOutputs, checkDataMovement, checkIntermediate | 3 | 2s | 10s | 2.0x |
| Monitor | monitor | 5 | 30s | 120s | 2.0x |
| Cleanup | deprovision, cancel, markFailed | 2 | 2s | 5s | 1.5x |

Tiers are configured as `RetryOptions` on activity stubs in the workflow. `TaskNode` carries a tier annotation so the workflow picks the right stub.

### 3. Remove Thread.sleep From Providers

Provider methods become single-attempt — try once, succeed or throw. Temporal retries the activity.

- **`SlurmComputeProvider.submit()`** — remove job verification retry loop (lines 240-261)
- **`SlurmComputeProvider.pollJobUntilSaturated()`** — remove polling loop (lines 375-415), single status check per activity invocation
- **`AwsComputeProvider.submit()`** — remove `ExponentialBackoffWaiter` SSH wait (lines 257-274)
- **`AwsComputeProvider.verifyInstanceIsRunning()`** — remove `ExponentialBackoffWaiter` EC2 wait (lines 464-515)
- **`AwsComputeProvider.pollJobUntilSaturated()`** — same as SLURM
- **`DataStagingSupport.transferFileToStorage()`** — remove file existence retry loop (lines 312-323)

### 4. Compute Package Legacy Cleanup

Delete dead/stub classes, migrate consumers to clean APIs:

- **Delete:** `AwsComputeResourcePreference` — dead, never referenced
- **Delete:** `GroupComputeResourcePreference` — explicitly marked temporary stub
- **Delete:** `GroupResourceProfile` — explicitly marked temporary stub
- **Delete:** `AwsProcessContext` — thin wrapper that only exists to hold legacy preference
- **Remove:** `ResourceProfileAdapter.getGroupComputeResourcePreference()` and `getGroupResourceProfile()` legacy methods
- **Migrate:** `AwsComputeProvider` to read from `ResourceProfileAdapter.getBinding()` directly
- **Migrate:** `MethodAuthorizationConfig` to use binding-level authorization

### 5. Keycloak HTTP Pattern Cleanup

Replace manual HTTP/URL/form patterns with Spring utilities already available in the module:

- **`KeycloakRequestAuthenticator.getFromUrl()`** — replace `HttpURLConnection` with `RestTemplate.exchange()` (RestTemplate already in same file)
- **`DefaultKeycloakLogoutService`** — replace `StringBuilder` form data with `LinkedMultiValueMap`, replace `StringBuilder` URL with `UriComponentsBuilder`
- **`KeycloakRestClient`** — replace manual form body building with `LinkedMultiValueMap`, standardize URL construction with `UriComponentsBuilder` (already used in 2 places in same file)

### 6. Race Condition Fix

- **`JobStatusEventToResultConverter.getJobIdByJobNameWithRetry()`** — remove retry loop entirely. Job record is already saved before submission completes. If not found, log warning and return — it's a bug to fix at source, not paper over with retries.

### 7. EmailMonitorWorkflow → Temporal Workflow

Rewrite the standalone daemon thread as a Temporal workflow:

- **Workflow:** Infinite loop: `Workflow.sleep(pollInterval)` → call `pollEmails` activity → repeat. `continueAsNew` every N iterations to bound history.
- **Activity:** `pollEmails()` — single poll batch (connect IMAP, fetch unseen, parse, publish, disconnect). Retry options handle transient IMAP failures.
- **Startup:** Launch workflow on application boot instead of daemon thread.
- **Delete:** `ServerLifecycle` daemon thread, `while(!stopRequested)` loop, both `Thread.sleep` calls.

## What Gets Deleted

| Class | Reason |
|-------|--------|
| `ProcessDAGEngine` | Loop moves to workflow, single-node logic to ActivitiesImpl |
| `ExponentialBackoffWaiter` | Temporal retries replace it |
| `AwsComputeResourcePreference` | Dead code — never referenced |
| `GroupComputeResourcePreference` | Explicit temporary stub |
| `GroupResourceProfile` | Explicit temporary stub |
| `AwsProcessContext` | Wrapper for deleted legacy preference |

## Post-Refactoring Invariant

`grep -r "Thread.sleep" modules/airavata-api/src/main/java/` returns **zero results**.
