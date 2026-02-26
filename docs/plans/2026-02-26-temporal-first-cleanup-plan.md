# Temporal-First Architecture & Legacy Cleanup — Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Eliminate all manual `Thread.sleep` retry loops, custom backoff utilities, and legacy compute stubs. Every retryable/waitable operation becomes an atomic Temporal activity with a `RetryOptions` policy.

**Architecture:** Each DAG node becomes its own Temporal activity call with tier-specific retry options. Workflows walk the DAG graph deterministically. Provider methods become single-attempt (try once, succeed or throw). Email monitoring becomes a Temporal workflow with `continueAsNew`.

**Tech Stack:** Java 21, Spring Boot, Temporal Java SDK, Spring RestTemplate/UriComponentsBuilder, JUnit 5 + Mockito

**Design doc:** `docs/plans/2026-02-26-temporal-first-cleanup-design.md`

---

### Task 1: Remove Thread.sleep from SlurmComputeProvider

**Files:**
- Modify: `modules/airavata-api/src/main/java/org/apache/airavata/compute/provider/slurm/SlurmComputeProvider.java:245-261` (submit retry loop)
- Modify: `modules/airavata-api/src/main/java/org/apache/airavata/compute/provider/slurm/SlurmComputeProvider.java:375-415` (pollJobUntilSaturated)

**Context:** SlurmComputeProvider has two `Thread.sleep` sites: (1) a job verification retry loop in submit() (lines 245-261) that retries 3 times with 10-30s backoff, and (2) a `pollJobUntilSaturated` loop (lines 375-415) that polls 4 times with 30-120s linear backoff. Both will be replaced by single-attempt logic — Temporal's retry policy handles retries at the activity level.

**Step 1: Remove submit verification retry loop**

In `SlurmComputeProvider.java`, replace the `while (verificationTryCount++ < 3)` loop (lines 245-261) with a single verification attempt. The whole block starting at line 245 (`int verificationTryCount = 0;`) through line 261 (`}`) becomes:

```java
            String verifyJobId = verifyJobSubmission(
                    adapter, jobModel.getJobName(), context.getComputeResourceLoginUserName(), context);
            if (verifyJobId != null && !verifyJobId.isEmpty()) {
                jobId = verifyJobId;
                jobModel.setJobId(jobId);
                jobService.saveJob(jobModel);
                jobSubmissionSupport.publishJobStatus(jobModel, JobState.QUEUED, "Verification step succeeded");
                logger.info("Job id {} verification succeeded", verifyJobId);
            }
```

This is the same logic but runs once. If verification fails, jobId stays null and the existing null-check at line 264 handles it by returning `DagTaskResult.Failure` — which Temporal will retry per the Infrastructure tier policy (10 attempts, 5-30s backoff).

**Step 2: Convert pollJobUntilSaturated to single status check**

Replace the entire `pollJobUntilSaturated` method (lines 375-415) with a single status check:

```java
    private void pollJobUntilSaturated(AgentAdapter adapter, JobManagerSpec config, JobModel job) {
        try {
            var monitorCommand = config.getMonitorCommand(job.getJobId());
            if (monitorCommand.isEmpty()) {
                logger.info("No monitor command for job {} — skipping", job.getJobId());
                return;
            }

            CommandOutput output = adapter.executeCommand(monitorCommand.get().getRawCommand(), null);
            if (output.getExitCode() != 0) {
                logger.warn("Monitor command failed for job {}: stdout={}, stderr={}",
                        job.getJobId(), output.getStdOut(), output.getStdError());
                return;
            }

            StatusModel<JobState> jobStatus = config.getParser()
                    .parseJobStatus(job.getJobId(), output.getStdOut());
            if (jobStatus != null) {
                logger.info("Job {} status: {}", job.getJobId(), jobStatus.getState());
            }
        } catch (Exception e) {
            logger.warn("Error polling job {} — continuing", job.getJobId(), e);
        }
    }
```

This removes the `for` loop, the `Thread.sleep`, and the saturation check. Each invocation checks once. Temporal's Monitor tier retry policy (5 attempts, 30-120s backoff) handles repeated polling at the activity level.

**Step 3: Run build**

```bash
mvn install -pl modules/airavata-api -am -DskipTests
```
Expected: BUILD SUCCESS

**Step 4: Run existing tests**

```bash
mvn test -pl modules/airavata-api -Dtest="SlurmComputeProvider*" -DfailIfNoTests=false
```
Expected: No SLURM-specific unit tests exist (only integration test), so this should pass vacuously.

**Step 5: Commit**

```bash
git add modules/airavata-api/src/main/java/org/apache/airavata/compute/provider/slurm/SlurmComputeProvider.java
git commit -m "refactor: remove Thread.sleep retry loops from SlurmComputeProvider

Submit verification and job polling are now single-attempt operations.
Temporal's activity retry policy handles retries at the workflow level."
```

---

### Task 2: Remove Thread.sleep from AwsComputeProvider + Delete ExponentialBackoffWaiter

**Files:**
- Modify: `modules/airavata-api/src/main/java/org/apache/airavata/compute/provider/aws/AwsComputeProvider.java:257-274` (SSH waiter), `:400-425` (poll loop), `:464-515` (EC2 waiter)
- Delete: `modules/airavata-api/src/main/java/org/apache/airavata/compute/provider/aws/ExponentialBackoffWaiter.java`

**Context:** AwsComputeProvider uses `ExponentialBackoffWaiter` in two places (SSH readiness at lines 257-268, EC2 running check at lines 464-515) and a manual poll loop (lines 383-425, identical pattern to SLURM). After making all three single-attempt, `ExponentialBackoffWaiter` has zero references and can be deleted.

**Step 1: Replace SSH readiness waiter with single attempt**

Replace lines 257-274 (the `ExponentialBackoffWaiter sshWaiter = ...` block through the catch) with:

```java
            try {
                adapter.createDirectory(remoteWorkingDir, true);
            } catch (Exception e) {
                String reason = "Failed to connect to SSH daemon or create remote directory " + remoteWorkingDir + ". "
                        + e.getMessage();
                logger.error(reason, e);
                return new DagTaskResult.Failure(reason, false, e);
            }
```

Single attempt. If SSH is not ready, returns non-fatal Failure → Temporal retries per Infrastructure tier.

**Step 2: Replace EC2 instance verification with single check**

Replace the entire `verifyInstanceIsRunning` method (lines 464-515) with:

```java
    private String verifyInstanceIsRunning(String token, String instanceId, String region, TaskContext context)
            throws Exception {
        try (Ec2Client ec2Client = awsTaskUtil.buildEc2Client(token, context.getGatewayId(), region)) {
            DescribeInstancesRequest request = DescribeInstancesRequest.builder()
                    .instanceIds(instanceId)
                    .build();
            DescribeInstancesResponse response = ec2Client.describeInstances(request);

            if (response.reservations().isEmpty()
                    || response.reservations().get(0).instances().isEmpty()) {
                throw new Exception("No instance found with ID: " + instanceId
                        + " for process: " + context.getProcessId());
            }

            Instance instance = response.reservations().get(0).instances().get(0);
            InstanceStateName state = instance.state().name();
            logger.info("Instance {} state: {} for process {}", instanceId, state, context.getProcessId());

            if (state == InstanceStateName.RUNNING) {
                String publicIp = instance.publicIpAddress();
                if (publicIp == null || publicIp.isEmpty()) {
                    throw new Exception("Instance " + instanceId + " is running but has no public IP yet");
                }
                return publicIp;
            }

            if (state == InstanceStateName.SHUTTING_DOWN
                    || state == InstanceStateName.TERMINATED
                    || state == InstanceStateName.STOPPED) {
                throw new Exception("Instance entered failure state: " + state
                        + " for process: " + context.getProcessId());
            }

            // Still pending — throw so Temporal retries
            throw new Exception("Instance " + instanceId + " not yet running (state: " + state + ")");
        }
    }
```

Single check. Returns IP if running, throws if pending or failed. Temporal's Infrastructure tier retries pending cases.

**Step 3: Convert pollJobUntilSaturated to single check**

Same pattern as SLURM (Task 1, Step 2). Replace the `for` loop in `pollJobUntilSaturated` with a single status check — remove the loop, the `Thread.sleep`, and the `retryDelaySeconds` variable. Keep the single execution + parse + log.

**Step 4: Remove ExponentialBackoffWaiter import from AwsComputeProvider**

Remove the `import` statement and any `TimeUnit` import that was only used by the waiter.

**Step 5: Delete ExponentialBackoffWaiter.java**

```bash
rm modules/airavata-api/src/main/java/org/apache/airavata/compute/provider/aws/ExponentialBackoffWaiter.java
```

**Step 6: Run build**

```bash
mvn install -pl modules/airavata-api -am -DskipTests
```
Expected: BUILD SUCCESS

**Step 7: Commit**

```bash
git add -A modules/airavata-api/src/main/java/org/apache/airavata/compute/provider/aws/
git commit -m "refactor: remove ExponentialBackoffWaiter and Thread.sleep from AwsComputeProvider

SSH readiness, EC2 instance verification, and job polling are now
single-attempt operations. Temporal retry policies handle retries.
ExponentialBackoffWaiter deleted (zero references)."
```

---

### Task 3: Inline AwsProcessContext into dagState + Delete AwsProcessContext

**Files:**
- Modify: `modules/airavata-api/src/main/java/org/apache/airavata/compute/provider/aws/AwsComputeProvider.java`
- Modify: `modules/airavata-api/src/main/java/org/apache/airavata/compute/provider/aws/AwsTaskUtil.java` (if it references AwsProcessContext)
- Delete: `modules/airavata-api/src/main/java/org/apache/airavata/compute/provider/aws/AwsProcessContext.java`
- Delete: `modules/airavata-api/src/main/java/org/apache/airavata/compute/resource/model/AwsComputeResourcePreference.java`

**Context:** `AwsProcessContext` is a thin wrapper over `ProcessModel.providerContext` JSON, providing typed get/save for AWS fields (instanceId, securityGroupId, keyPairName, sshCredentialToken, publicIp, jobId). Its `getAwsComputeResourcePreference()` returns null (dead). `AwsComputeResourcePreference` is never actually used for anything. Both should be deleted, with the state storage inlined into `AwsComputeProvider` as private helpers using the existing `dagState` map + `providerContext` persistence for crash recovery.

**Step 1: Add private AWS context helpers to AwsComputeProvider**

Add constants and helper methods to `AwsComputeProvider`:

```java
    // AWS provider context keys
    private static final String AWS_INSTANCE_ID = "AWS_INSTANCE_ID";
    private static final String AWS_SECURITY_GROUP_ID = "AWS_SECURITY_GROUP_ID";
    private static final String AWS_KEY_PAIR_NAME = "AWS_KEY_PAIR_NAME";
    private static final String AWS_SSH_CREDENTIAL_TOKEN = "AWS_SSH_CREDENTIAL_TOKEN";
    private static final String AWS_PUBLIC_IP = "AWS_PUBLIC_IP";
    private static final String AWS_JOB_ID = "AWS_JOB_ID";

    private void saveProviderState(TaskContext context, String key, String value) {
        try {
            context.getDagState().put(key, value);
            Map<String, String> contextMap = loadProviderContext(context);
            contextMap.put(key, value);
            context.getProcessModel().setProviderContext(MAPPER.writeValueAsString(contextMap));
            processService.updateProcess(context.getProcessModel(), context.getProcessId());
        } catch (Exception e) {
            logger.warn("Failed to persist provider state key '{}' for process {}", key, context.getProcessId(), e);
        }
    }

    private String getProviderState(TaskContext context, String key) {
        // Prefer dagState (in-flight), fall back to persisted providerContext
        String value = context.getDagState().get(key);
        if (value != null) return value;
        try {
            return loadProviderContext(context).get(key);
        } catch (Exception e) {
            logger.warn("Failed to load provider context for process {}", context.getProcessId(), e);
            return null;
        }
    }

    private Map<String, String> loadProviderContext(TaskContext context) throws Exception {
        String json = context.getProcessModel().getProviderContext();
        if (json == null || json.isEmpty()) return new HashMap<>();
        return MAPPER.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<>() {});
    }
```

Where `MAPPER` is a `private static final ObjectMapper MAPPER = new ObjectMapper();` field.

**Step 2: Replace all AwsProcessContext usages in AwsComputeProvider**

Find every `awsContext.saveInstanceId(x)` → `saveProviderState(context, AWS_INSTANCE_ID, x)`
Find every `awsContext.getInstanceId()` → `getProviderState(context, AWS_INSTANCE_ID)`
Same for all other fields. Remove the `AwsProcessContext` constructor call and field.

**Step 3: Delete AwsProcessContext.java and AwsComputeResourcePreference.java**

```bash
rm modules/airavata-api/src/main/java/org/apache/airavata/compute/provider/aws/AwsProcessContext.java
rm modules/airavata-api/src/main/java/org/apache/airavata/compute/resource/model/AwsComputeResourcePreference.java
```

**Step 4: Remove stale imports from AwsComputeProvider and AwsTaskUtil**

Remove `import ...AwsProcessContext` and `import ...AwsComputeResourcePreference` from any file that referenced them.

**Step 5: Run build**

```bash
mvn install -pl modules/airavata-api -am -DskipTests
```
Expected: BUILD SUCCESS

**Step 6: Commit**

```bash
git add -A modules/airavata-api/src/main/java/org/apache/airavata/compute/
git commit -m "refactor: inline AwsProcessContext into AwsComputeProvider, delete dead classes

AwsProcessContext was a thin JSON wrapper — state now stored directly in
dagState + providerContext. AwsComputeResourcePreference was never used.
Both classes deleted."
```

---

### Task 4: Remove Thread.sleep from DataStagingSupport

**Files:**
- Modify: `modules/airavata-api/src/main/java/org/apache/airavata/storage/client/sftp/DataStagingSupport.java:311-323`

**Context:** `DataStagingSupport.transferFileToStorage()` has a file existence retry loop (3 retries, 10s fixed delay) at lines 311-323. This runs inside a Temporal activity (Data tier). Make it single-attempt — if file doesn't exist, throw. Temporal retries.

**Step 1: Replace retry loop with single check**

Replace lines 311-323:
```java
            if (!fileExists) {
                for (int i = 1; i <= 3; i++) {
                    logger.warn("File " + sourcePath + " was not found in path. Retrying in 10 seconds. Try " + i);
                    try {
                        Thread.sleep(10 * 1000);
                    } catch (InterruptedException e) {
                        logger.error("Unexpected error in waiting", e);
                    }
                    fileExists = adapter.doesFileExist(sourcePath);
                    if (fileExists) {
                        break;
                    }
                }
            }
```

With:
```java
            if (!fileExists) {
                logger.warn("File {} not found at source path. Will be retried by Temporal.", sourcePath);
                throw new TaskFailureException(
                        "Source file not found: " + sourcePath, false,
                        new java.io.FileNotFoundException(sourcePath));
            }
```

The subsequent `if (!fileExists)` check (line 326) that returns false can be removed since we now throw.

**Step 2: Run build**

```bash
mvn install -pl modules/airavata-api -am -DskipTests
```
Expected: BUILD SUCCESS

**Step 3: Commit**

```bash
git add modules/airavata-api/src/main/java/org/apache/airavata/storage/client/sftp/DataStagingSupport.java
git commit -m "refactor: remove file existence retry loop from DataStagingSupport

Single check — throws TaskFailureException if file not found.
Temporal Data tier retry policy handles retries."
```

---

### Task 5: Fix Race Condition in JobStatusEventToResultConverter

**Files:**
- Modify: `modules/airavata-api/src/main/java/org/apache/airavata/execution/monitoring/JobStatusEventToResultConverter.java:40-41,75-94`

**Context:** `getJobIdByJobNameWithRetry()` retries 5 times with 2s delay. This runs in an HTTP callback context (NOT a Temporal activity). The job record should already exist because `ForkJobSubmissionTask` pre-saves it. Remove the retry and do a single lookup. If not found, it's a legitimate bug — log and return null.

**Step 1: Replace retry method with single lookup**

Remove the constants at lines 40-41:
```java
    private static final int RETRIES = 5;
    private static final long RETRY_DELAY_MS = 2000;
```

Replace `getJobIdByJobNameWithRetry` (lines 75-94) with:

```java
    private static String getJobIdByJobName(String jobName, String taskId, JobService jobService)
            throws RegistryException {
        var jobsOfTask = jobService.getJobs("taskId", taskId);
        if (jobsOfTask == null || jobsOfTask.isEmpty()) {
            log.warn("No jobs found for task {}. Job record should have been saved before submission.", taskId);
            return null;
        }
        return jobsOfTask.stream()
                .filter(job -> jobName.equals(job.getJobName()))
                .findFirst()
                .map(job -> job.getJobId())
                .orElse(null);
    }
```

Update the call site at line 53 to remove `WithRetry` from the method name and remove `InterruptedException` from the throws clause of the caller.

**Step 2: Run build**

```bash
mvn install -pl modules/airavata-api -am -DskipTests
```
Expected: BUILD SUCCESS

**Step 3: Commit**

```bash
git add modules/airavata-api/src/main/java/org/apache/airavata/execution/monitoring/JobStatusEventToResultConverter.java
git commit -m "fix: remove retry loop from JobStatusEventToResultConverter

Job record is pre-saved before submission. Single lookup is correct.
Retrying masked a potential race condition that should be fixed at source."
```

---

### Task 6: Add RetryTier to TaskNode and DAGTemplates

**Files:**
- Create: `modules/airavata-api/src/main/java/org/apache/airavata/execution/dag/RetryTier.java`
- Modify: `modules/airavata-api/src/main/java/org/apache/airavata/execution/dag/DAGTemplates.java`
- Modify: `modules/airavata-api/src/test/java/org/apache/airavata/execution/dag/DAGTemplatesTest.java`

**Context:** Each DAG node needs a retry tier annotation so the workflow can pick the right Temporal `RetryOptions` per node. The tier is carried in `TaskNode.metadata()` under the key `"retryTier"`. No changes to `TaskNode` record itself — just metadata entries.

Tiers:
| Tier | Nodes | Attempts | Initial | Max | Backoff |
|------|-------|----------|---------|-----|---------|
| INFRASTRUCTURE | provision, submit | 10 | 5s | 30s | 2.0x |
| DATA | stageIn, outputStaging, archive | 3 | 5s | 15s | 2.0x |
| CHECK | checkOutputs, checkDataMovement, checkIntermediate | 3 | 2s | 10s | 2.0x |
| MONITOR | monitor | 5 | 30s | 120s | 2.0x |
| CLEANUP | deprovision, cancel, markFailed | 2 | 2s | 5s | 1.5x |

**Step 1: Create RetryTier enum**

```java
package org.apache.airavata.execution.dag;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import java.time.Duration;

/**
 * Retry tier for DAG task nodes. Determines the Temporal {@link RetryOptions}
 * applied when the node runs as an individual activity.
 */
public enum RetryTier {
    INFRASTRUCTURE(10, 5, 30, 2.0, Duration.ofMinutes(10)),
    DATA(3, 5, 15, 2.0, Duration.ofMinutes(5)),
    CHECK(3, 2, 10, 2.0, Duration.ofMinutes(2)),
    MONITOR(5, 30, 120, 2.0, Duration.ofMinutes(30)),
    CLEANUP(2, 2, 5, 1.5, Duration.ofMinutes(5));

    private final ActivityOptions activityOptions;

    RetryTier(int maxAttempts, int initialSec, int maxSec, double backoff, Duration startToClose) {
        this.activityOptions = ActivityOptions.newBuilder()
                .setStartToCloseTimeout(startToClose)
                .setRetryOptions(RetryOptions.newBuilder()
                        .setMaximumAttempts(maxAttempts)
                        .setInitialInterval(Duration.ofSeconds(initialSec))
                        .setMaximumInterval(Duration.ofSeconds(maxSec))
                        .setBackoffCoefficient(backoff)
                        .build())
                .build();
    }

    public ActivityOptions activityOptions() {
        return activityOptions;
    }
}
```

**Step 2: Add retryTier metadata to all DAGTemplates nodes**

In `DAGTemplates.java`, add `.metadata("retryTier", "INFRASTRUCTURE")` etc. to each node:

```java
    public static ProcessDAG preDag(ComputeResourceType type) {
        return ProcessDAG.builder("provision")
                .node("provision", provisioningBean(type))
                    .metadata("processState", "CONFIGURING_WORKSPACE")
                    .metadata("retryTier", "INFRASTRUCTURE")
                    .onSuccess("stageIn").onFailure("fail")
                .node("stageIn", inputStagingBean())
                    .metadata("processState", "INPUT_DATA_STAGING")
                    .metadata("retryTier", "DATA")
                    .onSuccess("submit").onFailure("fail")
                .node("submit", submitBean(type))
                    .metadata("processState", "EXECUTING")
                    .metadata("retryTier", "INFRASTRUCTURE")
                    .onSuccess("checkIntermediate").onFailure("fail")
                .node("checkIntermediate", "checkIntermediateTransferTask")
                    .metadata("retryTier", "CHECK")
                    .onSuccess("preDeprovision").onFailure(null)
                .node("preDeprovision", deprovisioningBean(type))
                    .metadata("retryTier", "CLEANUP")
                    .terminal()
                .node("fail", "markFailedTask")
                    .metadata("retryTier", "CLEANUP")
                    .terminal()
                .build();
    }

    public static ProcessDAG postDag(ComputeResourceType type) {
        return ProcessDAG.builder("monitor")
                .node("monitor", monitoringBean(type))
                    .metadata("processState", "MONITORING")
                    .metadata("retryTier", "MONITOR")
                    .onSuccess("checkOutputs").onFailure("checkOutputs")
                .node("checkOutputs", "checkOutputsTask")
                    .metadata("retryTier", "CHECK")
                    .onSuccess("checkDataMovement").onFailure("deprovision")
                .node("checkDataMovement", "checkDataMovementTask")
                    .metadata("retryTier", "CHECK")
                    .onSuccess("outputStaging").onFailure("archive")
                .node("outputStaging", outputStagingBean())
                    .metadata("processState", "OUTPUT_DATA_STAGING")
                    .metadata("retryTier", "DATA")
                    .onSuccess("archive").onFailure("archive")
                .node("archive", archiveBean())
                    .metadata("retryTier", "DATA")
                    .onSuccess("deprovision").onFailure("deprovision")
                .node("deprovision", deprovisioningBean(type))
                    .metadata("processState", "COMPLETED")
                    .metadata("retryTier", "CLEANUP")
                    .terminal()
                .build();
    }

    public static ProcessDAG cancelDag(ComputeResourceType type) {
        return ProcessDAG.builder("cancel")
                .node("cancel", cancelBean(type))
                    .metadata("processState", "CANCELED")
                    .metadata("retryTier", "CLEANUP")
                    .terminal()
                .build();
    }
```

**Step 3: Add retryTier assertions to DAGTemplatesTest**

Add parameterized tests that verify every node in every DAG template has a `retryTier` metadata entry:

```java
    @ParameterizedTest(name = "preDag({0}) all nodes have retryTier metadata")
    @EnumSource(ComputeResourceType.class)
    public void preDag_allNodes_haveRetryTierMetadata(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.preDag(type);
        dag.nodes().values().forEach(node ->
            assertNotNull(node.metadata().get("retryTier"),
                    "Node '" + node.id() + "' must have retryTier metadata"));
    }

    @ParameterizedTest(name = "postDag({0}) all nodes have retryTier metadata")
    @EnumSource(ComputeResourceType.class)
    public void postDag_allNodes_haveRetryTierMetadata(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.postDag(type);
        dag.nodes().values().forEach(node ->
            assertNotNull(node.metadata().get("retryTier"),
                    "Node '" + node.id() + "' must have retryTier metadata"));
    }

    @ParameterizedTest(name = "cancelDag({0}) all nodes have retryTier metadata")
    @EnumSource(ComputeResourceType.class)
    public void cancelDag_allNodes_haveRetryTierMetadata(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.cancelDag(type);
        dag.nodes().values().forEach(node ->
            assertNotNull(node.metadata().get("retryTier"),
                    "Node '" + node.id() + "' must have retryTier metadata"));
    }

    @Test
    public void retryTier_allValues_areValidEnumConstants() {
        for (ComputeResourceType type : ComputeResourceType.values()) {
            for (ProcessDAG dag : List.of(DAGTemplates.preDag(type), DAGTemplates.postDag(type), DAGTemplates.cancelDag(type))) {
                dag.nodes().values().forEach(node -> {
                    String tier = node.metadata().get("retryTier");
                    assertNotNull(RetryTier.valueOf(tier),
                            "retryTier '" + tier + "' on node '" + node.id() + "' must be a valid RetryTier enum");
                });
            }
        }
    }
```

**Step 4: Run tests**

```bash
mvn test -pl modules/airavata-api -Dtest="DAGTemplatesTest" -DfailIfNoTests=false
```
Expected: All existing tests pass + new retryTier tests pass.

**Step 5: Commit**

```bash
git add modules/airavata-api/src/main/java/org/apache/airavata/execution/dag/RetryTier.java
git add modules/airavata-api/src/main/java/org/apache/airavata/execution/dag/DAGTemplates.java
git add modules/airavata-api/src/test/java/org/apache/airavata/execution/dag/DAGTemplatesTest.java
git commit -m "feat: add RetryTier enum and wire into DAGTemplates metadata

Each DAG node now carries a retryTier metadata entry that determines
the Temporal RetryOptions when the node runs as its own activity.
Five tiers: INFRASTRUCTURE, DATA, CHECK, MONITOR, CLEANUP."
```

---

### Task 7: Restructure ProcessActivity — Per-Node Temporal Activities

**Files:**
- Modify: `modules/airavata-api/src/main/java/org/apache/airavata/execution/activity/ProcessActivity.java` (complete rewrite of internals)
- Modify: `modules/airavata-api/src/test/java/org/apache/airavata/execution/activity/ProcessActivityTest.java` (update for new Activities interface)

**Context:** This is the core architectural change. Currently, each workflow (Pre/Post/Cancel) calls a single monolithic activity that runs an entire DAG via `ProcessDAGEngine`. After this change, the workflow code walks the DAG deterministically and calls `executeDagNode` for each node as a separate activity. Each activity gets its own `RetryOptions` based on the node's retry tier.

Key design decisions:
- `Activities` interface gets 2 methods: `resolveResourceType(processId)` and `executeDagNode(processId, gatewayId, nodeId, taskBeanName, dagState, nodeMetadata)`
- `NodeResult` record: return type for `executeDagNode` — `(String message, Map<String, String> output)`
- Non-fatal `DagTaskResult.Failure` → throw regular RuntimeException (Temporal retries per tier)
- Fatal `DagTaskResult.Failure` → throw `ApplicationFailure.newNonRetryableFailure()` (Temporal does NOT retry)
- On activity exhausting all retries → `ActivityFailure` caught by workflow → follow DAG failure edge
- `ProcessDAGEngine` dependency removed from `ActivitiesImpl` (engine deleted in Task 8)
- DAG walking extracted to a private static `walkDag` helper shared by all three WfImpls

**Step 1: Rewrite ProcessActivity.java**

Complete rewrite of `ProcessActivity.java`:

```java
package org.apache.airavata.execution.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.failure.ActivityFailure;
import io.temporal.failure.ApplicationFailure;
import io.temporal.spring.boot.ActivityImpl;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import java.io.Serializable;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.airavata.compute.resource.model.ComputeResourceType;
import org.apache.airavata.compute.resource.model.Resource;
import org.apache.airavata.compute.resource.service.ResourceService;
import org.apache.airavata.config.ServiceConditionals.ConditionalOnParticipant;
import org.apache.airavata.core.model.DagTaskResult;
import org.apache.airavata.execution.dag.DAGTemplates;
import org.apache.airavata.execution.dag.DagTask;
import org.apache.airavata.execution.dag.ProcessDAG;
import org.apache.airavata.execution.dag.RetryTier;
import org.apache.airavata.execution.dag.TaskContextFactory;
import org.apache.airavata.execution.dag.TaskInterceptor;
import org.apache.airavata.execution.dag.TaskNode;
import org.apache.airavata.execution.service.ProcessService;
import org.apache.airavata.execution.task.TaskContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Temporal durable workflows for process execution.
 *
 * <p>Each workflow walks a {@link ProcessDAG} deterministically, calling
 * {@link Activities#executeDagNode} for each node as a separate activity
 * with tier-specific retry options. The DAG defines task order and
 * success/failure branching; Temporal handles retries and durability.
 */
public class ProcessActivity {

    public static final String TASK_QUEUE = "airavata-workflows";

    // -------------------------------------------------------------------------
    // Workflow contracts
    // -------------------------------------------------------------------------

    @WorkflowInterface
    public interface PreWf {
        @WorkflowMethod
        String execute(PreInput input);
    }

    @WorkflowInterface
    public interface PostWf {
        @WorkflowMethod
        String execute(PostInput input);
    }

    @WorkflowInterface
    public interface CancelWf {
        @WorkflowMethod
        String execute(CancelInput input);
    }

    // -------------------------------------------------------------------------
    // Input records
    // -------------------------------------------------------------------------

    public record PreInput(String processId, String experimentId, String gatewayId, String tokenId)
            implements Serializable {}

    public record PostInput(String processId, String experimentId, String gatewayId, boolean forceRun)
            implements Serializable {}

    public record CancelInput(String processId, String experimentId, String gatewayId) implements Serializable {}

    // -------------------------------------------------------------------------
    // Activity return type
    // -------------------------------------------------------------------------

    public record NodeResult(String message, Map<String, String> output) implements Serializable {}

    // -------------------------------------------------------------------------
    // Activity interface — one method per node, not per DAG
    // -------------------------------------------------------------------------

    @ActivityInterface
    public interface Activities {
        @ActivityMethod
        ComputeResourceType resolveResourceType(String processId);

        @ActivityMethod
        NodeResult executeDagNode(String processId, String gatewayId, String nodeId,
                String taskBeanName, Map<String, String> dagState, Map<String, String> nodeMetadata);
    }

    // -------------------------------------------------------------------------
    // DAG walking helper (deterministic — safe for workflow code)
    // -------------------------------------------------------------------------

    private static String walkDag(ProcessDAG dag, String processId, String gatewayId,
            Map<RetryTier, Activities> tierStubs, Activities setupStub) {
        String currentNodeId = dag.entryNodeId();
        Map<String, String> dagState = new HashMap<>();
        String lastMessage = null;

        while (currentNodeId != null) {
            TaskNode node = dag.getNode(currentNodeId);
            if (node == null) {
                throw ApplicationFailure.newFailure(
                        "DAG node '" + currentNodeId + "' not found", "DAG_ERROR");
            }

            RetryTier tier = RetryTier.valueOf(
                    node.metadata().getOrDefault("retryTier", "INFRASTRUCTURE"));
            Activities activities = tierStubs.get(tier);

            try {
                NodeResult result = activities.executeDagNode(
                        processId, gatewayId, node.id(), node.taskBeanName(),
                        dagState, node.metadata());
                dagState.putAll(result.output());
                lastMessage = result.message();
                currentNodeId = node.onSuccess();
            } catch (ActivityFailure e) {
                lastMessage = "Node '" + node.id() + "' failed after retries";
                currentNodeId = node.onFailure();
                if (currentNodeId == null) {
                    throw e;
                }
            }
        }

        return lastMessage != null ? lastMessage : "DAG completed for process " + processId;
    }

    private static Map<RetryTier, Activities> buildTierStubs() {
        Map<RetryTier, Activities> stubs = new HashMap<>();
        for (RetryTier tier : RetryTier.values()) {
            stubs.put(tier, Workflow.newActivityStub(Activities.class, tier.activityOptions()));
        }
        return stubs;
    }

    private static Activities buildSetupStub() {
        return Workflow.newActivityStub(Activities.class,
                ActivityOptions.newBuilder()
                        .setStartToCloseTimeout(Duration.ofSeconds(30))
                        .setRetryOptions(RetryOptions.newBuilder()
                                .setMaximumAttempts(3).build())
                        .build());
    }

    // -------------------------------------------------------------------------
    // Workflow implementations
    // -------------------------------------------------------------------------

    @WorkflowImpl(taskQueues = TASK_QUEUE)
    public static class PreWfImpl implements PreWf {
        private final Activities setup = buildSetupStub();
        private final Map<RetryTier, Activities> tierStubs = buildTierStubs();

        @Override
        public String execute(PreInput input) {
            ComputeResourceType type = setup.resolveResourceType(input.processId());
            ProcessDAG dag = DAGTemplates.preDag(type);
            return walkDag(dag, input.processId(), input.gatewayId(), tierStubs, setup);
        }
    }

    @WorkflowImpl(taskQueues = TASK_QUEUE)
    public static class PostWfImpl implements PostWf {
        private final Activities setup = buildSetupStub();
        private final Map<RetryTier, Activities> tierStubs = buildTierStubs();

        @Override
        public String execute(PostInput input) {
            ComputeResourceType type = setup.resolveResourceType(input.processId());
            ProcessDAG dag = DAGTemplates.postDag(type);
            return walkDag(dag, input.processId(), input.gatewayId(), tierStubs, setup);
        }
    }

    @WorkflowImpl(taskQueues = TASK_QUEUE)
    public static class CancelWfImpl implements CancelWf {
        private final Activities setup = buildSetupStub();
        private final Map<RetryTier, Activities> tierStubs = buildTierStubs();

        @Override
        public String execute(CancelInput input) {
            ComputeResourceType type = setup.resolveResourceType(input.processId());
            ProcessDAG dag = DAGTemplates.cancelDag(type);
            return walkDag(dag, input.processId(), input.gatewayId(), tierStubs, setup);
        }
    }

    // -------------------------------------------------------------------------
    // Activity implementation (Spring-managed, full DI)
    // -------------------------------------------------------------------------

    @ConditionalOnParticipant
    @Component
    @ActivityImpl(taskQueues = TASK_QUEUE)
    public static class ActivitiesImpl implements Activities {

        private static final Logger logger = LoggerFactory.getLogger(ActivitiesImpl.class);

        private final ApplicationContext applicationContext;
        private final TaskContextFactory contextFactory;
        private final List<TaskInterceptor> interceptors;
        private final ProcessService processService;
        private final ResourceService resourceService;

        public ActivitiesImpl(
                ApplicationContext applicationContext,
                TaskContextFactory contextFactory,
                List<TaskInterceptor> interceptors,
                ProcessService processService,
                ResourceService resourceService) {
            this.applicationContext = applicationContext;
            this.contextFactory = contextFactory;
            this.interceptors = interceptors;
            this.processService = processService;
            this.resourceService = resourceService;
        }

        @Override
        public ComputeResourceType resolveResourceType(String processId) {
            try {
                var processModel = processService.getProcess(processId);
                Resource resource = resourceService.getResource(processModel.getResourceId());
                if (resource != null && resource.getCapabilities() != null
                        && resource.getCapabilities().getCompute() != null) {
                    return resource.getCapabilities().getCompute().getComputeResourceType();
                }
            } catch (Exception e) {
                logger.warn("Failed to resolve resource type for process {}, defaulting to SLURM", processId, e);
            }
            return ComputeResourceType.SLURM;
        }

        @Override
        public NodeResult executeDagNode(String processId, String gatewayId, String nodeId,
                String taskBeanName, Map<String, String> dagState, Map<String, String> nodeMetadata) {
            String taskId = UUID.randomUUID().toString();
            TaskContext context = contextFactory.buildContext(processId, gatewayId, taskId);
            context.getDagState().putAll(dagState);

            // Build a minimal TaskNode for interceptors
            TaskNode node = new TaskNode(nodeId, taskBeanName, null, null, nodeMetadata);
            DagTask task = applicationContext.getBean(taskBeanName, DagTask.class);

            logger.info("Executing node '{}' (bean: {}) for process {}", nodeId, taskBeanName, processId);

            for (TaskInterceptor interceptor : interceptors) {
                interceptor.before(context, node);
            }

            DagTaskResult result;
            try {
                result = task.execute(context);
            } catch (Exception e) {
                logger.error("Uncaught exception in node '{}' for process {}", nodeId, processId, e);
                result = new DagTaskResult.Failure("Uncaught exception: " + e.getMessage(), false, e);
            }

            return switch (result) {
                case DagTaskResult.Success success -> {
                    logger.info("Node '{}' succeeded: {}", nodeId, success.message());
                    for (TaskInterceptor interceptor : interceptors) {
                        interceptor.afterSuccess(context, node, success);
                    }
                    yield new NodeResult(success.message(), success.output());
                }
                case DagTaskResult.Failure failure -> {
                    logger.warn("Node '{}' failed: {} (fatal={})", nodeId, failure.reason(), failure.fatal());
                    for (TaskInterceptor interceptor : interceptors) {
                        interceptor.afterFailure(context, node, failure);
                    }
                    if (failure.fatal()) {
                        throw ApplicationFailure.newNonRetryableFailure(
                                failure.reason(), "FATAL_TASK_FAILURE");
                    }
                    throw ApplicationFailure.newFailure(
                            failure.reason(), "TASK_FAILURE");
                }
            };
        }
    }
}
```

**Step 2: Update ProcessActivityTest.java**

The test needs to be updated for the new Activities interface. Key changes:
- Activities now has 2 methods: `resolveResourceType` and `executeDagNode` (not 3 execute*Dag methods)
- Input records unchanged
- Add `NodeResult` serialization tests

Update the reflection-based tests:
- `activitiesInterface_hasExactlyThreeMethods` → `activitiesInterface_hasExactlyTwoMethods` checking for `resolveResourceType` and `executeDagNode`
- Remove checks for `executePreDag`, `executePostDag`, `executeCancelDag`
- Add checks for `resolveResourceType` and `executeDagNode`
- Add `NodeResult` record tests (accessors, serialization, equality)

**Step 3: Run tests**

```bash
mvn test -pl modules/airavata-api -Dtest="ProcessActivityTest,DAGTemplatesTest,ProcessDAGTest" -DfailIfNoTests=false
```
Expected: All pass.

**Step 4: Run build**

```bash
mvn install -pl modules/airavata-api -am -DskipTests
```
Expected: BUILD SUCCESS

**Step 5: Commit**

```bash
git add modules/airavata-api/src/main/java/org/apache/airavata/execution/activity/ProcessActivity.java
git add modules/airavata-api/src/test/java/org/apache/airavata/execution/activity/ProcessActivityTest.java
git commit -m "feat: restructure ProcessActivity — each DAG node is its own Temporal activity

Workflows now walk the DAG deterministically and call executeDagNode
per node with tier-specific RetryOptions. The monolithic execute*Dag
activities are replaced with atomic per-node execution.

- Activities interface: resolveResourceType + executeDagNode
- NodeResult record: serializable return type
- Non-fatal failures throw (Temporal retries per tier)
- Fatal failures throw non-retryable ApplicationFailure
- Activity exhaustion → workflow follows DAG failure edge"
```

---

### Task 8: Delete ProcessDAGEngine

**Files:**
- Delete: `modules/airavata-api/src/main/java/org/apache/airavata/execution/dag/ProcessDAGEngine.java`
- Delete: `modules/airavata-api/src/test/java/org/apache/airavata/execution/dag/ProcessDAGEngineTest.java`

**Context:** `ProcessDAGEngine` was the in-process DAG walker called by the old monolithic activities. Its logic has moved into `ProcessActivity.ActivitiesImpl.executeDagNode()` (single-node execution) and the workflow implementations (DAG walking). Zero references remain after Task 7.

**Step 1: Verify zero references**

Search for `ProcessDAGEngine` in all Java files. After Task 7, only the engine itself and its test should reference it. The import was removed from ProcessActivity in Task 7.

**Step 2: Delete files**

```bash
rm modules/airavata-api/src/main/java/org/apache/airavata/execution/dag/ProcessDAGEngine.java
rm modules/airavata-api/src/test/java/org/apache/airavata/execution/dag/ProcessDAGEngineTest.java
```

**Step 3: Run build**

```bash
mvn install -pl modules/airavata-api -am -DskipTests
```
Expected: BUILD SUCCESS

**Step 4: Commit**

```bash
git add -A modules/airavata-api/src/main/java/org/apache/airavata/execution/dag/
git add -A modules/airavata-api/src/test/java/org/apache/airavata/execution/dag/
git commit -m "refactor: delete ProcessDAGEngine — DAG walking moved to Temporal workflows

The engine's single-node execution logic is now in
ProcessActivity.ActivitiesImpl.executeDagNode(). DAG traversal
is in the workflow implementations. Engine and its tests removed."
```

---

### Task 9: Delete Legacy Compute Stubs + Migrate Consumers

**Files:**
- Delete: `modules/airavata-api/src/main/java/org/apache/airavata/compute/resource/model/GroupComputeResourcePreference.java`
- Delete: `modules/airavata-api/src/main/java/org/apache/airavata/compute/resource/model/GroupResourceProfile.java`
- Modify: `modules/airavata-api/src/main/java/org/apache/airavata/compute/resource/adapter/ResourceProfileAdapter.java:189-262` (remove legacy methods)
- Modify: `modules/airavata-api/src/main/java/org/apache/airavata/research/experiment/service/DefaultExperimentService.java` (migrate GroupResourceProfile usage)
- Modify: `modules/airavata-api/src/main/java/org/apache/airavata/execution/orchestration/ProcessResourceResolver.java` (remove unused imports)
- Modify: `modules/airavata-api/src/main/java/org/apache/airavata/research/experiment/model/UserConfigurationDataModel.java` (if GroupResourceProfile import)
- Modify: `modules/airavata-api/src/main/java/org/apache/airavata/research/experiment/util/ExperimentModelUtil.java` (remove unused imports)
- Modify: `modules/airavata-api/src/main/java/org/apache/airavata/execution/model/ProcessModel.java` (remove unused imports)
- Modify: `modules/airavata-api/src/main/java/org/apache/airavata/iam/service/MethodAuthorizationConfig.java` (cosmetic — rename constant for clarity)

**Context:** `GroupComputeResourcePreference` and `GroupResourceProfile` are explicitly marked "Temporary placeholder pending pipeline rewrite". The pipeline rewrite (Temporal-first architecture) is happening now. All consumers need to use `ResourceBinding` and `ResourceProfileAdapter.getBinding()` directly.

**Step 1: Identify all consumers**

From grep, the consumers are:
1. `ResourceProfileAdapter` — defines `getGroupComputeResourcePreference()` and `getGroupResourceProfile()` (these are the sources, to be deleted)
2. `DefaultExperimentService` — uses `GroupResourceProfile` for auto-assigning resource profiles (lines 314-338, 742-773, 779-794)
3. `ProcessResourceResolver` — only uses `groupResourceProfileId` as a String (no import of the class itself needed — verify)
4. `UserConfigurationDataModel` — has `groupResourceProfileId` field (String), may import the class (verify)
5. `ExperimentModelUtil` — may reference (verify)
6. `ProcessModel` — may reference (verify)
7. `MethodAuthorizationConfig` — URL pattern strings referencing old endpoint names (harmless, can clean up)
8. `DBConstants` — may reference (verify)

**Step 2: Migrate DefaultExperimentService**

The key change: `getGroupResourceList` currently returns `List<GroupResourceProfile>`. It should return `List<String>` (list of accessible profile IDs). And `createDefaultGroupResourceProfileForUser` should just return a gatewayId-based default profile ID string without wrapping in `GroupResourceProfile`.

Replace `getGroupResourceList`:
```java
    private List<String> getAccessibleGroupResourceProfileIds(AuthzToken authzToken, String gatewayId)
            throws AiravataSystemException {
        // ... existing sharing service query to get accessible profile IDs ...
        // Return the list of String IDs directly instead of wrapping in GroupResourceProfile
    }
```

Replace `createDefaultGroupResourceProfileForUser`:
```java
    private String createDefaultGroupResourceProfileForUser(AuthzToken authzToken, String gatewayId, String username)
            throws AiravataSystemException {
        // The "profile" is just the gatewayId — create sharing entity and return
        String profileId = gatewayId; // or generate UUID if needed
        // ... existing sharing entity creation code ...
        return profileId;
    }
```

Update callers (around line 314):
```java
    List<String> profileIds = getAccessibleGroupResourceProfileIds(authzToken, gatewayId);
    if (profileIds != null && !profileIds.isEmpty()) {
        String groupResourceProfileId = profileIds.get(0);
        // ... existing logic unchanged ...
    }
```

**Step 3: Remove legacy methods from ResourceProfileAdapter**

Delete `getGroupComputeResourcePreference()` (lines 189-233) and `getGroupResourceProfile()` (lines 245-262) and `getGroupResourceProfiles()` if it exists. Keep all the clean methods: `getBinding()`, `getUserBinding()`, `getStorageBinding()`, `resolveStorageRootLocation()`.

**Step 4: Delete model classes**

```bash
rm modules/airavata-api/src/main/java/org/apache/airavata/compute/resource/model/GroupComputeResourcePreference.java
rm modules/airavata-api/src/main/java/org/apache/airavata/compute/resource/model/GroupResourceProfile.java
```

**Step 5: Fix all compilation errors**

Remove stale imports from all files that referenced these classes. Update any remaining references to use the clean API.

**Step 6: Run build**

```bash
mvn install -pl modules/airavata-api -am -DskipTests
```
Expected: BUILD SUCCESS

**Step 7: Run tests**

```bash
mvn test -pl modules/airavata-api -DfailIfNoTests=false
```
Expected: All pass.

**Step 8: Commit**

```bash
git add -A modules/airavata-api/src/main/java/org/apache/airavata/compute/resource/
git add -A modules/airavata-api/src/main/java/org/apache/airavata/research/experiment/
git add -A modules/airavata-api/src/main/java/org/apache/airavata/execution/
git add -A modules/airavata-api/src/main/java/org/apache/airavata/iam/
git commit -m "refactor: delete GroupResourceProfile and GroupComputeResourcePreference

These were explicitly marked 'temporary placeholder pending pipeline
rewrite'. Consumers migrated to use ResourceBinding and
ResourceProfileAdapter.getBinding() directly. Legacy adapter methods removed."
```

---

### Task 10: Keycloak HTTP Pattern Cleanup

**Files:**
- Modify: `modules/airavata-api/src/main/java/org/apache/airavata/iam/service/KeycloakRequestAuthenticator.java:241-256`
- Modify: `modules/airavata-api/src/main/java/org/apache/airavata/iam/service/DefaultKeycloakLogoutService.java:105-172`
- Modify: `modules/airavata-api/src/main/java/org/apache/airavata/iam/keycloak/KeycloakRestClient.java:180-197` + scattered URL concatenations

**Context:** Three IAM files use manual HTTP patterns (HttpURLConnection, StringBuilder form data, string URL concatenation) when Spring utilities (RestTemplate, LinkedMultiValueMap, UriComponentsBuilder) are already available in the same codebase. This task standardizes on Spring HTTP utilities.

**Step 1: Fix KeycloakRequestAuthenticator.getFromUrl()**

Replace `HttpURLConnection` with `RestTemplate.exchange()`:

```java
    public String getFromUrl(String urlToRead, String token) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        if (token != null) {
            headers.setBearerAuth(token);
        }
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                urlToRead, HttpMethod.GET, entity, String.class);
        return response.getBody();
    }
```

Also replace `getOpenIDConfigurationUrl()` string concatenation with `UriComponentsBuilder`:

```java
    private String getOpenIDConfigurationUrl(String realm) {
        return UriComponentsBuilder.fromHttpUrl(properties.security().iam().serverUrl())
                .pathSegment("realms", realm, ".well-known", "openid-configuration")
                .toUriString();
    }
```

Remove `HttpURLConnection`, `URL`, `BufferedReader`, `InputStreamReader`, `Collectors` imports if no longer needed.

**Step 2: Fix DefaultKeycloakLogoutService.revokeRefreshToken()**

Replace `StringBuilder` form data with `LinkedMultiValueMap`:

```java
        LinkedMultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", clientId);
        if (clientSecret != null && !clientSecret.isEmpty()) {
            formData.add("client_secret", clientSecret);
        }
        formData.add("token", refreshToken);
        formData.add("token_type_hint", "refresh_token");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);
```

Replace URL concatenation with `UriComponentsBuilder`:

```java
        String revokeUrl = UriComponentsBuilder.fromHttpUrl(keycloakUrl)
                .pathSegment("realms", realm, "protocol", "openid-connect", "revoke")
                .toUriString();
```

**Step 3: Fix DefaultKeycloakLogoutService.buildLogoutUrl()**

Replace `StringBuilder` URL with `UriComponentsBuilder`:

```java
    public String buildLogoutUrl(String idToken, String postLogoutRedirectUri) {
        String keycloakUrl = getKeycloakServerUrl();
        String realm = getKeycloakRealm();

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(keycloakUrl)
                .pathSegment("realms", realm, "protocol", "openid-connect", "logout");

        if (idToken != null && !idToken.isEmpty()) {
            builder.queryParam("id_token_hint", idToken);
            if (postLogoutRedirectUri != null && !postLogoutRedirectUri.isEmpty()) {
                builder.queryParam("post_logout_redirect_uri", postLogoutRedirectUri);
            }
        }

        return builder.toUriString();
    }
```

Remove the `encode()` utility method if no longer used.

**Step 4: Fix KeycloakRestClient.obtainAdminToken()**

Replace `HashMap` + `StringBuilder` form body with `LinkedMultiValueMap`:

```java
            var tokenUrl = UriComponentsBuilder.fromHttpUrl(serverUrl)
                    .pathSegment("realms", realm, "protocol", "openid-connect", "token")
                    .toUriString();

            var headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            var formData = new LinkedMultiValueMap<String, String>();
            formData.add("grant_type", GRANT_TYPE_PASSWORD);
            formData.add("username", credentials.getLoginUserName());
            formData.add("password", credentials.getPassword());
            formData.add("client_id", ADMIN_CLI_CLIENT_ID);

            var request = new HttpEntity<>(formData, headers);
```

Also apply the same `UriComponentsBuilder` pattern to other URL concatenations in KeycloakRestClient wherever string concatenation is used (lines 248, 262, 295, 339, etc.).

**Step 5: Remove unused imports**

Clean up: `java.net.URL`, `java.net.HttpURLConnection`, `java.io.BufferedReader`, `java.io.InputStreamReader`, `java.util.stream.Collectors`, etc.

**Step 6: Run build**

```bash
mvn install -pl modules/airavata-api -am -DskipTests
```
Expected: BUILD SUCCESS

**Step 7: Commit**

```bash
git add modules/airavata-api/src/main/java/org/apache/airavata/iam/
git commit -m "refactor: standardize Keycloak HTTP patterns on Spring utilities

Replace HttpURLConnection with RestTemplate.exchange(),
StringBuilder form data with LinkedMultiValueMap,
string URL concatenation with UriComponentsBuilder.
These utilities were already available in the same codebase."
```

---

### Task 11: Rewrite EmailMonitorWorkflow as Temporal Workflow

**Files:**
- Rewrite: `modules/airavata-api/src/main/java/org/apache/airavata/execution/monitoring/EmailMonitorWorkflow.java`

**Context:** `EmailMonitorWorkflow` is a `ServerLifecycle` daemon thread that polls an IMAP inbox with two `Thread.sleep` calls (poll interval at line 278, connection retry at line 316). This needs to become a Temporal workflow with durable sleep and automatic retry.

Structure:
- `MonitorWf` — workflow interface with `continueAsNew` loop
- `MonitorActivities` — activity interface with `pollEmails()` method
- `MonitorWfImpl` — workflow: sleep → poll → repeat → continueAsNew
- `MonitorActivitiesImpl` — activity: connect IMAP, fetch unseen, parse, publish, disconnect
- `EmailMonitorLauncher` — starts the workflow on application boot (replaces ServerLifecycle)

The email config loading, parser map initialization, and message parsing logic all move into the activity implementation (Spring-managed, full DI). The workflow is pure orchestration.

**Step 1: Rewrite EmailMonitorWorkflow.java**

Complete rewrite. The class keeps its name but changes from `ServerLifecycle` to a Temporal workflow container:

```java
package org.apache.airavata.execution.monitoring;

// ... imports ...

/**
 * Temporal workflow for email-based job status monitoring.
 *
 * <p>Replaces the former daemon thread with a durable Temporal workflow
 * that sleeps between polls and calls {@link MonitorActivities#pollEmails()}
 * as a retryable activity. Uses {@code continueAsNew} to bound history.
 */
public class EmailMonitorWorkflow {

    public static final String TASK_QUEUE = ProcessActivity.TASK_QUEUE;
    private static final int MAX_ITERATIONS_BEFORE_CONTINUE_AS_NEW = 100;

    // --- Workflow ---

    @WorkflowInterface
    public interface MonitorWf {
        @WorkflowMethod
        void run(MonitorInput input);
    }

    public record MonitorInput(long pollIntervalMs, long connectionRetryMs) implements Serializable {}

    @ActivityInterface
    public interface MonitorActivities {
        @ActivityMethod
        void pollEmails();
    }

    @WorkflowImpl(taskQueues = TASK_QUEUE)
    public static class MonitorWfImpl implements MonitorWf {
        private final MonitorActivities activities = Workflow.newActivityStub(
                MonitorActivities.class,
                ActivityOptions.newBuilder()
                        .setStartToCloseTimeout(Duration.ofMinutes(5))
                        .setRetryOptions(RetryOptions.newBuilder()
                                .setMaximumAttempts(3)
                                .setInitialInterval(Duration.ofSeconds(10))
                                .setMaximumInterval(Duration.ofSeconds(60))
                                .setBackoffCoefficient(2.0)
                                .build())
                        .build());

        @Override
        public void run(MonitorInput input) {
            for (int i = 0; i < MAX_ITERATIONS_BEFORE_CONTINUE_AS_NEW; i++) {
                Workflow.sleep(Duration.ofMillis(input.pollIntervalMs()));
                try {
                    activities.pollEmails();
                } catch (ActivityFailure e) {
                    // Activity exhausted retries — log and continue polling
                    // The next iteration will retry with a fresh activity
                }
            }
            Workflow.continueAsNew(input);
        }
    }

    // --- Activity implementation ---

    @ConditionalOnProperty(prefix = "airavata.services.monitor.email", name = "enabled", havingValue = "true")
    @Profile("!test")
    @Component
    @ActivityImpl(taskQueues = TASK_QUEUE)
    public static class MonitorActivitiesImpl implements MonitorActivities {

        private static final Logger log = LoggerFactory.getLogger(MonitorActivitiesImpl.class);

        private final ServerProperties airavataProperties;
        private final JobService jobService;
        private final ApplicationContext applicationContext;
        private final JobStatusMonitor jobStatusMonitor;

        // Email config (initialized once in @PostConstruct)
        private String host, emailAddress, password, storeProtocol, folderName, publisherId;
        private Properties mailProperties;
        private long emailExpirationTimeMinutes;
        private final Map<ResourceJobManagerType, SLURMEmailParser> emailParserMap = new HashMap<>();
        private final Map<String, ResourceJobManagerType> addressMap = new HashMap<>();

        public MonitorActivitiesImpl(
                JobService jobService,
                ServerProperties airavataProperties,
                ApplicationContext applicationContext) {
            this.jobService = jobService;
            this.airavataProperties = airavataProperties;
            this.applicationContext = applicationContext;
            JobStatusMonitor monitor = null;
            try {
                monitor = applicationContext.getBean(JobStatusMonitor.class);
            } catch (Exception ignored) {}
            this.jobStatusMonitor = monitor;
        }

        @PostConstruct
        public void init() {
            // ... same init/loadContext/populateAddressAndParserMap logic as before ...
            // (move the existing init(), loadContext(), populateAddressAndParserMap() methods here)
        }

        @Override
        public void pollEmails() {
            // Single poll: connect, fetch unseen, parse, publish, disconnect
            Store store = null;
            Folder folder = null;
            try {
                Session session = Session.getDefaultInstance(mailProperties);
                store = session.getStore(storeProtocol);
                store.connect(host, emailAddress, password);
                folder = store.getFolder(folderName);
                folder.open(Folder.READ_WRITE);

                SearchTerm unseenBefore = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
                Message[] messages = folder.search(unseenBefore);

                if (messages == null || messages.length == 0) {
                    log.info("[EJM]: No new email messages");
                    return;
                }

                log.info("[EJM]: {} new email/s received", messages.length);
                processMessages(messages, folder, store);

            } catch (Exception e) {
                log.error("[EJM]: Error during email poll", e);
                throw new RuntimeException("Email poll failed", e);
            } finally {
                // ... close folder and store safely ...
            }
        }

        // ... processMessages(), parse(), getJobMonitorType() from existing class ...
    }

    // --- Launcher (starts workflow on app boot) ---

    @ConditionalOnProperty(prefix = "airavata.services.monitor.email", name = "enabled", havingValue = "true")
    @Profile("!test")
    @Component
    public static class EmailMonitorLauncher {

        private static final Logger log = LoggerFactory.getLogger(EmailMonitorLauncher.class);
        private final WorkflowClient workflowClient;
        private final ServerProperties properties;

        public EmailMonitorLauncher(WorkflowClient workflowClient, ServerProperties properties) {
            this.workflowClient = workflowClient;
            this.properties = properties;
        }

        @EventListener(ApplicationStartedEvent.class)
        public void startEmailMonitor() {
            try {
                MonitorWf workflow = workflowClient.newWorkflowStub(
                        MonitorWf.class,
                        WorkflowOptions.newBuilder()
                                .setWorkflowId("email-monitor")
                                .setTaskQueue(TASK_QUEUE)
                                .build());

                long pollInterval = properties.services().monitor().email().period();
                long retryInterval = properties.services().monitor().email().connectionRetryInterval();

                WorkflowClient.start(workflow::run, new MonitorInput(pollInterval, retryInterval));
                log.info("Started email monitor Temporal workflow");
            } catch (Exception e) {
                log.error("Failed to start email monitor workflow", e);
            }
        }
    }
}
```

**Step 2: Move existing helper methods into MonitorActivitiesImpl**

The existing methods `loadContext()`, `populateAddressAndParserMap()`, `parse()`, `getJobMonitorType()`, and `processMessages()` move as-is into `MonitorActivitiesImpl`. They become instance methods of the activity class instead of the old daemon class.

The key change in `processMessages` — it now receives `folder` and `store` as parameters instead of reading instance fields.

**Step 3: Remove ServerLifecycle implements**

The class no longer implements `ServerLifecycle`. Remove `start()`, `stop()`, `doStart()`, `doStop()`, `isRunning()`, `isAutoStartup()`, `getServerName()`, `getServerVersion()`, `getPhase()` methods. Remove the daemon thread, `stopRequested` flag, and `emailThread` field.

**Step 4: Run build**

```bash
mvn install -pl modules/airavata-api -am -DskipTests
```
Expected: BUILD SUCCESS

**Step 5: Commit**

```bash
git add modules/airavata-api/src/main/java/org/apache/airavata/execution/monitoring/EmailMonitorWorkflow.java
git commit -m "feat: rewrite EmailMonitorWorkflow as Temporal workflow

Replace daemon thread with Temporal workflow using:
- Workflow.sleep() for durable poll interval
- pollEmails() activity for single IMAP poll batch
- Temporal retry options for transient IMAP failures
- continueAsNew every 100 iterations to bound history
- ApplicationStartedEvent listener to launch on boot

No more Thread.sleep in email monitoring."
```

---

### Task 12: Final Verification — Zero Thread.sleep

**Files:**
- All files in `modules/airavata-api/src/main/java/`

**Step 1: Verify zero Thread.sleep in production code**

```bash
grep -r "Thread.sleep" modules/airavata-api/src/main/java/
```
Expected: **zero results**.

**Step 2: Verify ExponentialBackoffWaiter deleted**

```bash
find modules/airavata-api -name "ExponentialBackoffWaiter.java"
```
Expected: no results.

**Step 3: Verify legacy classes deleted**

```bash
find modules/airavata-api -name "GroupComputeResourcePreference.java" -o -name "GroupResourceProfile.java" -o -name "AwsComputeResourcePreference.java" -o -name "AwsProcessContext.java" -o -name "ProcessDAGEngine.java"
```
Expected: no results.

**Step 4: Full build**

```bash
mvn clean install -DskipTests
```
Expected: BUILD SUCCESS

**Step 5: Run all tests**

```bash
mvn test -pl modules/airavata-api -DfailIfNoTests=false
```
Expected: All pass.

**Step 6: Final commit (if any fixups needed)**

Only if previous steps revealed issues that needed fixing.
