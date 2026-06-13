/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.orchestration.executor;

import jakarta.persistence.EntityManager;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.apache.airavata.config.ConditionalOnServer;
import org.apache.airavata.config.ServerSettings;
import org.apache.airavata.interfaces.RegistryHandler;
import org.apache.airavata.model.status.proto.ExperimentState;
import org.apache.airavata.model.status.proto.ExperimentStatus;
import org.apache.airavata.model.status.proto.ProcessState;
import org.apache.airavata.model.status.proto.ProcessStatus;
import org.apache.airavata.model.status.proto.TaskState;
import org.apache.airavata.model.status.proto.TaskStatus;
import org.apache.airavata.model.task.proto.DataStageType;
import org.apache.airavata.model.task.proto.DataStagingTaskModel;
import org.apache.airavata.model.task.proto.TaskTypes;
import org.apache.airavata.task.AiravataTask;
import org.apache.airavata.task.DbTaskResult;
import org.apache.airavata.task.SchedulerUtils;
import org.apache.airavata.task.TaskHelperImpl;
import org.apache.airavata.task.TaskUtil;
import org.apache.airavata.util.ExpCatalogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

/**
 * DB-transactional task executor. A pool of worker threads claims runnable tasks from the
 * {@code PROCESS}/{@code TASK}/{@code EXEC_STATUS} tables using {@code SELECT ... FOR UPDATE
 * SKIP LOCKED} (exactly-once across threads and future JVMs, with no Helix/ZooKeeper/Curator),
 * runs each task's {@link org.apache.airavata.task.DbTask} body, and advances TASK/PROCESS
 * state in the DB. Task order comes from {@code PROCESS.TASK_DAG}; the current position is
 * derived from the latest {@code EXEC_STATUS} row per task.
 *
 * <p>The DB row lock is held only for the short claim transaction (which marks the task
 * {@code TASK_STATE_EXECUTING} as the durable claim marker); the actual work runs outside any
 * transaction so long-running tasks never hold a lock.
 */
@Component
@ConditionalOnServer("orchestrator")
public class ProcessExecutor implements SmartLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(ProcessExecutor.class);

    /** Latest PROCESS states the executor will keep advancing (everything but terminal/pre-launch). */
    private static final List<String> RUNNABLE_PROCESS_STATES = List.of(
            ProcessState.PROCESS_STATE_STARTED.name(),
            ProcessState.PROCESS_STATE_REQUEUED.name(),
            ProcessState.PROCESS_STATE_CONFIGURING_WORKSPACE.name(),
            ProcessState.PROCESS_STATE_INPUT_DATA_STAGING.name(),
            ProcessState.PROCESS_STATE_EXECUTING.name(),
            ProcessState.PROCESS_STATE_MONITORING.name(),
            ProcessState.PROCESS_STATE_OUTPUT_DATA_STAGING.name(),
            ProcessState.PROCESS_STATE_QUEUED.name(),
            ProcessState.PROCESS_STATE_DEQUEUING.name());

    @Value("${orchestration.executor.threads:4}")
    private int threads;

    @Value("${orchestration.executor.poll.interval.ms:3000}")
    private long pollIntervalMs;

    @Value("${orchestration.executor.lease.timeout.ms:600000}")
    private long leaseTimeoutMs;

    @Value("${orchestration.executor.max.attempts:3}")
    private int maxAttempts;

    private final OrchestratorExecutorRepository repo = new OrchestratorExecutorRepository();

    private volatile boolean running = false;
    private ExecutorService workers;
    private ScheduledExecutorService sweeper;

    // ----------------------------------------------------------------- lifecycle

    @Override
    public void start() {
        if (running) {
            return;
        }
        running = true;
        workers = Executors.newFixedThreadPool(threads, namedDaemon("process-executor-worker"));
        for (int i = 0; i < threads; i++) {
            final int id = i;
            workers.submit(() -> workerLoop(id));
        }
        sweeper = Executors.newSingleThreadScheduledExecutor(namedDaemon("process-executor-sweeper"));
        sweeper.scheduleWithFixedDelay(this::sweep, pollIntervalMs, pollIntervalMs, TimeUnit.MILLISECONDS);
        logger.info(
                "ProcessExecutor started: {} worker(s), poll {} ms, lease {} ms, maxAttempts {}",
                threads,
                pollIntervalMs,
                leaseTimeoutMs,
                maxAttempts);
    }

    @Override
    public void stop() {
        running = false;
        if (sweeper != null) {
            sweeper.shutdownNow();
        }
        if (workers != null) {
            workers.shutdownNow();
            try {
                workers.awaitTermination(15, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        logger.info("ProcessExecutor stopped");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    // ----------------------------------------------------------------- worker loop

    private void workerLoop(int workerId) {
        while (running) {
            boolean didWork = false;
            try {
                didWork = pollOnce(workerId);
            } catch (Exception e) {
                logger.error("Executor worker {} error", workerId, e);
            }
            if (!running) {
                break;
            }
            if (!didWork) {
                try {
                    Thread.sleep(pollIntervalMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    /** Claim and run one task. Returns true if a task ran (loop should immediately try again). */
    private boolean pollOnce(int workerId) {
        RegistryHandler registry = SchedulerUtils.getRegistryHandler();
        if (registry == null) {
            return false;
        }
        Optional<Claim> claimOpt = claimNextRunnableTask();
        if (claimOpt.isEmpty()) {
            return false;
        }
        Claim claim = claimOpt.get();
        logger.info(
                "Executor worker {} claimed task {} ({}) of process {}",
                workerId,
                claim.taskId,
                claim.taskType,
                claim.processId);
        DbTaskResult result = runRealTask(claim);
        finishTask(registry, claim, result);
        return true;
    }

    /**
     * Resolve, populate, and run the concrete {@link AiravataTask} for this claim. The executor is the
     * sole status authority: the task runs with {@code forceRunTask=true} (so it does not skip itself,
     * since the executor already wrote TASK_STATE_EXECUTING as the claim) and all skip-publish flags set
     * (so it performs the work but does not write TASK/PROCESS/EXPERIMENT status). The task's
     * {@link DbTaskResult} drives subsequent TASK/PROCESS state transitions.
     */
    private DbTaskResult runRealTask(Claim claim) {
        try {
            Class<? extends AiravataTask> taskClass = resolveTask(claim.taskId, claim.taskType);
            if (taskClass == null) {
                logger.info("No real task for {} ({}); treating as no-op COMPLETED", claim.taskId, claim.taskType);
                return DbTaskResult.completed("no-op for task type " + claim.taskType);
            }

            String gatewayId = gatewayIdFor(claim.experimentId);

            AiravataTask task = taskClass.getDeclaredConstructor().newInstance();
            task.setTaskHelper(new TaskHelperImpl());

            Map<String, String> params = new HashMap<>();
            params.put("taskId", claim.taskId);
            params.put("Process Id", claim.processId);
            params.put("experimentId", claim.experimentId);
            params.put("gatewayId", gatewayId);
            params.put("Force Run Task", "true");
            params.put("Skip All Status Publish", "true");
            params.put("Skip Process Status Publish", "true");
            params.put("Skip Experiment Status Publish", "true");
            TaskUtil.deserializeTaskData(task, params);

            logger.info(
                    "Running real task {} ({}) [{}] of process {}",
                    claim.taskId,
                    claim.taskType,
                    taskClass.getSimpleName(),
                    claim.processId);
            DbTaskResult result = task.onRun(task.getTaskHelper());
            return result != null ? result : DbTaskResult.completed(null);
        } catch (Exception e) {
            logger.error("Real task {} ({}) of process {} threw", claim.taskId, claim.taskType, claim.processId, e);
            return DbTaskResult.failed(e.getMessage() == null ? e.toString() : e.getMessage());
        }
    }

    /**
     * Concrete {@link AiravataTask} class for a task, using the same class names as
     * {@code SlurmProvisioningAdapter}. DATA_STAGING resolves to input vs output staging by deserializing the
     * TASK row's {@code SUB_TASK_MODEL} into a {@link DataStagingTaskModel} and reading its type. Task
     * types with no real body return {@code null} so the executor records COMPLETED.
     */
    @SuppressWarnings("unchecked")
    private Class<? extends AiravataTask> resolveTask(String taskId, TaskTypes taskType) throws Exception {
        String className;
        switch (taskType) {
            case ENV_SETUP:
                className = "org.apache.airavata.compute.task.EnvSetupTask";
                break;
            case JOB_SUBMISSION:
                className = "org.apache.airavata.compute.task.DefaultJobSubmissionTask";
                break;
            case MONITORING:
                className = "org.apache.airavata.compute.task.MonitoringTask";
                break;
            case DATA_STAGING:
                className = dataStagingTaskClass(taskId);
                break;
            default:
                // OUTPUT_FETCHING, ENV_CLEANUP, etc. have no DB-executor task body yet.
                return null;
        }
        return (Class<? extends AiravataTask>) Class.forName(className);
    }

    /** INPUT staging vs OUTPUT/ARCHIVE staging, read from the TASK row's serialized DataStagingTaskModel. */
    private String dataStagingTaskClass(String taskId) {
        DataStageType type = DataStageType.INPUT;
        byte[] sub = repo.execute(em -> {
            try {
                return (byte[]) em.createNativeQuery("SELECT SUB_TASK_MODEL FROM TASK WHERE TASK_ID = :tid")
                        .setParameter("tid", taskId)
                        .getSingleResult();
            } catch (Exception e) {
                return null;
            }
        });
        if (sub != null) {
            try {
                type = DataStagingTaskModel.parseFrom(sub).getType();
            } catch (Exception e) {
                logger.warn("Failed to parse DataStagingTaskModel for task {}; defaulting to INPUT", taskId, e);
            }
        }
        return type == DataStageType.INPUT
                ? "org.apache.airavata.storage.task.InputDataStagingTask"
                : "org.apache.airavata.storage.task.OutputDataStagingTask";
    }

    /** Gateway for an experiment, with a default-gateway fallback. */
    private String gatewayIdFor(String experimentId) {
        try {
            RegistryHandler registry = SchedulerUtils.getRegistryHandler();
            if (registry != null && experimentId != null) {
                String gw = registry.getExperiment(experimentId).getGatewayId();
                if (gw != null && !gw.isEmpty()) {
                    return gw;
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to resolve gateway for experiment {}; using default", experimentId, e);
        }
        try {
            return ServerSettings.getDefaultUserGateway();
        } catch (Exception e) {
            return "default";
        }
    }

    // ----------------------------------------------------------------- claim (FOR UPDATE SKIP LOCKED)

    private Optional<Claim> claimNextRunnableTask() {
        for (String pid : findRunnableProcessIds()) {
            Optional<Claim> claim = repo.execute(em -> tryClaimUnderLock(em, pid));
            if (claim.isPresent()) {
                return claim;
            }
        }
        return Optional.empty();
    }

    /**
     * Within one transaction: lock the PROCESS row (skip if another worker holds it), compute the
     * next runnable task from the DAG + latest task states, and write the EXECUTING marker. The lock
     * is released when this transaction commits.
     */
    private Optional<Claim> tryClaimUnderLock(EntityManager em, String pid) {
        List<?> locked = em.createNativeQuery(
                        "SELECT PROCESS_ID FROM PROCESS WHERE PROCESS_ID = :pid FOR UPDATE SKIP LOCKED")
                .setParameter("pid", pid)
                .getResultList();
        if (locked.isEmpty()) {
            return Optional.empty(); // another worker holds this process
        }

        List<String> dag = taskDag(em, pid);
        if (dag.isEmpty()) {
            return Optional.empty();
        }
        String experimentId = experimentIdOf(em, pid);
        Map<String, String> latest = latestTaskStates(em, pid);

        // if a task is already EXECUTING, the process is in flight under another worker / lease
        for (String state : latest.values()) {
            if (TaskState.TASK_STATE_EXECUTING.name().equals(state)) {
                return Optional.empty();
            }
        }

        String nextTaskId = null;
        for (String tid : dag) {
            String state = latest.getOrDefault(tid, TaskState.TASK_STATE_CREATED.name());
            if (TaskState.TASK_STATE_COMPLETED.name().equals(state)) {
                continue;
            }
            if (TaskState.TASK_STATE_FAILED.name().equals(state)
                    || TaskState.TASK_STATE_CANCELED.name().equals(state)) {
                return Optional.empty(); // a terminal-failed task blocks the DAG
            }
            nextTaskId = tid; // CREATED (or no status yet)
            break;
        }
        if (nextTaskId == null) {
            return Optional.empty(); // all tasks completed; finalize handled by finishTask/sweep
        }

        Map<String, TaskTypes> types = taskTypes(em, pid);
        TaskTypes taskType = types.getOrDefault(nextTaskId, TaskTypes.TASK_TYPES_UNKNOWN);

        long now = System.currentTimeMillis();
        insertStatus(em, "TASK", nextTaskId, TaskState.TASK_STATE_EXECUTING.name(), now, "claimed by executor");

        ProcessState stage = processStageFor(taskType, nextTaskId, dag, types);
        if (stage != null && !stage.name().equals(latestProcessState(em, pid))) {
            insertStatus(em, "PROCESS", pid, stage.name(), now, "");
        }

        return Optional.of(new Claim(pid, experimentId, nextTaskId, taskType));
    }

    // ----------------------------------------------------------------- finalize state after run

    private void finishTask(RegistryHandler registry, Claim claim, DbTaskResult result) {
        try {
            DbTaskResult.Status status = result.status();
            if (status == DbTaskResult.Status.COMPLETED || status == DbTaskResult.Status.SKIPPED) {
                addTaskStatus(registry, claim.taskId, TaskState.TASK_STATE_COMPLETED, result.message());
                logger.info("Task {} ({}) COMPLETED for process {}", claim.taskId, claim.taskType, claim.processId);
                if (allTasksCompleted(claim.processId)) {
                    addProcessStatus(registry, claim.processId, ProcessState.PROCESS_STATE_COMPLETED, null);
                    logger.info("Process {} COMPLETED (all DAG tasks done)", claim.processId);
                    rollUpExperiment(registry, claim.experimentId);
                }
            } else if (status == DbTaskResult.Status.FAILED && shouldRetry(claim.taskId)) {
                addTaskStatus(
                        registry,
                        claim.taskId,
                        TaskState.TASK_STATE_CREATED,
                        "requeued after failure: " + result.message());
                addProcessStatus(registry, claim.processId, ProcessState.PROCESS_STATE_REQUEUED, null);
                logger.warn("Task {} failed and was requeued for process {}", claim.taskId, claim.processId);
            } else {
                addTaskStatus(registry, claim.taskId, TaskState.TASK_STATE_FAILED, result.message());
                addProcessStatus(registry, claim.processId, ProcessState.PROCESS_STATE_FAILED, result.message());
                logger.error(
                        "Task {} ({}) FAILED for process {}: {}",
                        claim.taskId,
                        claim.taskType,
                        claim.processId,
                        result.message());
                rollUpExperiment(registry, claim.experimentId);
            }
        } catch (Exception e) {
            logger.error("Failed to finalize task {} of process {}", claim.taskId, claim.processId, e);
        }
    }

    private void rollUpExperiment(RegistryHandler registry, String experimentId) throws Exception {
        if (experimentId == null) {
            return;
        }
        // Roll up from native EXEC_STATUS reads rather than mapping each sibling ProcessModel, so a
        // single unmappable process cannot block the experiment's roll-up.
        boolean[] flags = repo.execute(em -> {
            @SuppressWarnings("unchecked")
            List<String> pids = em.createNativeQuery("SELECT PROCESS_ID FROM PROCESS WHERE EXPERIMENT_ID = :exp")
                    .setParameter("exp", experimentId)
                    .getResultList();
            boolean allDone = !pids.isEmpty();
            boolean failed = false;
            for (String pid : pids) {
                String state = latestProcessState(em, pid);
                if (ProcessState.PROCESS_STATE_FAILED.name().equals(state)) {
                    failed = true;
                }
                if (!ProcessState.PROCESS_STATE_COMPLETED.name().equals(state)) {
                    allDone = false;
                }
            }
            return new boolean[] {allDone, failed};
        });
        boolean allCompleted = flags[0];
        boolean anyFailed = flags[1];
        if (anyFailed) {
            updateExperimentStatus(registry, experimentId, ExperimentState.EXPERIMENT_STATE_FAILED);
            logger.info("Experiment {} marked FAILED (a process failed)", experimentId);
        } else if (allCompleted) {
            updateExperimentStatus(registry, experimentId, ExperimentState.EXPERIMENT_STATE_COMPLETED);
            logger.info("Experiment {} COMPLETED (all processes done)", experimentId);
        }
    }

    // ----------------------------------------------------------------- periodic sweep (recovery)

    private void sweep() {
        try {
            reclaimStaleLeases();
            finalizeCompletedProcesses();
        } catch (Exception e) {
            logger.error("Executor sweep failed", e);
        }
    }

    /** Re-mark tasks stuck in EXECUTING past the lease timeout as CREATED so they get re-claimed. */
    private void reclaimStaleLeases() {
        long cutoff = System.currentTimeMillis() - leaseTimeoutMs;
        List<String> stale = repo.execute(em -> {
            @SuppressWarnings("unchecked")
            List<Object[]> rows = em.createNativeQuery(
                            "SELECT s.ENTITY_ID, MAX(s.TIME_OF_STATE_CHANGE) FROM EXEC_STATUS s "
                                    + "WHERE s.ENTITY_TYPE = 'TASK' AND s.STATE = :st GROUP BY s.ENTITY_ID")
                    .setParameter("st", TaskState.TASK_STATE_EXECUTING.name())
                    .getResultList();
            List<String> result = new ArrayList<>();
            for (Object[] a : rows) {
                String taskId = (String) a[0];
                if (toMillis(a[1]) >= cutoff) {
                    continue;
                }
                if (TaskState.TASK_STATE_EXECUTING.name().equals(latestStateForTask(em, taskId))) {
                    result.add(taskId);
                }
            }
            return result;
        });
        for (String taskId : stale) {
            repo.execute(em -> {
                insertStatus(
                        em,
                        "TASK",
                        taskId,
                        TaskState.TASK_STATE_CREATED.name(),
                        System.currentTimeMillis(),
                        "lease expired; reclaimed");
                return null;
            });
            logger.warn("Reclaimed stale EXECUTING lease for task {}", taskId);
        }
    }

    /** Safety net: finalize runnable processes whose DAG tasks are all completed. */
    private void finalizeCompletedProcesses() {
        RegistryHandler registry = SchedulerUtils.getRegistryHandler();
        if (registry == null) {
            return;
        }
        for (String pid : findRunnableProcessIds()) {
            if (!allTasksCompleted(pid)) {
                continue;
            }
            try {
                addProcessStatus(registry, pid, ProcessState.PROCESS_STATE_COMPLETED, "finalized by sweep");
                rollUpExperiment(registry, repo.execute(em -> experimentIdOf(em, pid)));
                logger.info("Sweep finalized process {} as COMPLETED", pid);
            } catch (Exception e) {
                logger.error("Failed to finalize process {}", pid, e);
            }
        }
    }

    // ----------------------------------------------------------------- registry-backed state writes

    private void addTaskStatus(RegistryHandler registry, String taskId, TaskState state, String reason)
            throws Exception {
        TaskStatus.Builder b = TaskStatus.newBuilder().setState(state);
        if (reason != null) {
            b.setReason(reason);
        }
        registry.addTaskStatus(b.build(), taskId);
    }

    private void addProcessStatus(RegistryHandler registry, String processId, ProcessState state, String reason)
            throws Exception {
        ProcessStatus.Builder b = ProcessStatus.newBuilder().setState(state);
        if (reason != null) {
            b.setReason(reason);
        }
        registry.addProcessStatus(b.build(), processId);
    }

    private void updateExperimentStatus(RegistryHandler registry, String experimentId, ExperimentState state)
            throws Exception {
        ExperimentStatus es = ExperimentStatus.newBuilder()
                .setState(state)
                .setTimeOfStateChange(System.currentTimeMillis())
                .build();
        registry.updateExperimentStatus(es, experimentId);
    }

    private boolean shouldRetry(String taskId) {
        long attempts = repo.execute(em -> ((Number)
                        em.createNativeQuery("SELECT COUNT(*) FROM EXEC_STATUS WHERE ENTITY_TYPE = 'TASK' "
                                        + "AND ENTITY_ID = :eid AND STATE = :st")
                                .setParameter("eid", taskId)
                                .setParameter("st", TaskState.TASK_STATE_EXECUTING.name())
                                .getSingleResult())
                .longValue());
        return attempts < maxAttempts;
    }

    private boolean allTasksCompleted(String pid) {
        return repo.execute(em -> {
            List<String> dag = taskDag(em, pid);
            if (dag.isEmpty()) {
                return false;
            }
            Map<String, String> latest = latestTaskStates(em, pid);
            for (String tid : dag) {
                if (!TaskState.TASK_STATE_COMPLETED.name().equals(latest.get(tid))) {
                    return false;
                }
            }
            return true;
        });
    }

    // ----------------------------------------------------------------- native read helpers

    private List<String> findRunnableProcessIds() {
        String inList = RUNNABLE_PROCESS_STATES.stream().map(s -> "'" + s + "'").collect(Collectors.joining(","));
        String sql = "SELECT p.PROCESS_ID FROM PROCESS p "
                + "JOIN (SELECT s.ENTITY_ID, s.STATE FROM EXEC_STATUS s "
                + "  JOIN (SELECT ENTITY_ID, MAX(TIME_OF_STATE_CHANGE) mt FROM EXEC_STATUS "
                + "        WHERE ENTITY_TYPE = 'PROCESS' GROUP BY ENTITY_ID) m "
                + "    ON s.ENTITY_ID = m.ENTITY_ID AND s.TIME_OF_STATE_CHANGE = m.mt AND s.ENTITY_TYPE = 'PROCESS') latest "
                + "  ON latest.ENTITY_ID = p.PROCESS_ID "
                + "WHERE latest.STATE IN (" + inList + ") ORDER BY p.CREATION_TIME ASC";
        return repo.execute(em -> {
            List<?> rows = em.createNativeQuery(sql).getResultList();
            List<String> ids = new ArrayList<>();
            for (Object row : rows) {
                ids.add(row instanceof Object[] arr ? (String) arr[0] : (String) row);
            }
            return new ArrayList<>(new LinkedHashSet<>(ids));
        });
    }

    private List<String> taskDag(EntityManager em, String pid) {
        Object dagObj;
        try {
            dagObj = em.createNativeQuery("SELECT TASK_DAG FROM PROCESS WHERE PROCESS_ID = :pid")
                    .setParameter("pid", pid)
                    .getSingleResult();
        } catch (Exception e) {
            return List.of();
        }
        if (dagObj == null) {
            return List.of();
        }
        List<String> ids = new ArrayList<>();
        for (String s : dagObj.toString().split(",")) {
            String t = s.trim();
            if (!t.isEmpty()) {
                ids.add(t);
            }
        }
        return ids;
    }

    private String experimentIdOf(EntityManager em, String pid) {
        try {
            return (String) em.createNativeQuery("SELECT EXPERIMENT_ID FROM PROCESS WHERE PROCESS_ID = :pid")
                    .setParameter("pid", pid)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, TaskTypes> taskTypes(EntityManager em, String pid) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery("SELECT TASK_ID, TASK_TYPE FROM TASK WHERE PARENT_PROCESS_ID = :pid")
                .setParameter("pid", pid)
                .getResultList();
        Map<String, TaskTypes> m = new HashMap<>();
        for (Object[] a : rows) {
            m.put((String) a[0], parseTaskType(a[1] == null ? null : a[1].toString()));
        }
        return m;
    }

    /** Latest state per task for a process, resolving same-timestamp ties by state rank. */
    private Map<String, String> latestTaskStates(EntityManager em, String pid) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(
                        "SELECT s.ENTITY_ID, s.STATE, s.TIME_OF_STATE_CHANGE FROM EXEC_STATUS s "
                                + "WHERE s.ENTITY_TYPE = 'TASK' AND s.ENTITY_ID IN "
                                + "(SELECT TASK_ID FROM TASK WHERE PARENT_PROCESS_ID = :pid)")
                .setParameter("pid", pid)
                .getResultList();
        Map<String, String> best = new HashMap<>();
        Map<String, Long> bestTime = new HashMap<>();
        Map<String, Integer> bestRank = new HashMap<>();
        for (Object[] a : rows) {
            String tid = (String) a[0];
            String state = (String) a[1];
            long time = toMillis(a[2]);
            int rank = stateRank(state);
            Long ct = bestTime.get(tid);
            if (ct == null || time > ct || (time == ct && rank > bestRank.get(tid))) {
                best.put(tid, state);
                bestTime.put(tid, time);
                bestRank.put(tid, rank);
            }
        }
        return best;
    }

    private String latestStateForTask(EntityManager em, String taskId) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery("SELECT s.STATE, s.TIME_OF_STATE_CHANGE FROM EXEC_STATUS s "
                        + "WHERE s.ENTITY_TYPE = 'TASK' AND s.ENTITY_ID = :tid")
                .setParameter("tid", taskId)
                .getResultList();
        String best = null;
        long bestTime = -1;
        int bestRank = -1;
        for (Object[] a : rows) {
            long time = toMillis(a[1]);
            int rank = stateRank((String) a[0]);
            if (time > bestTime || (time == bestTime && rank > bestRank)) {
                bestTime = time;
                bestRank = rank;
                best = (String) a[0];
            }
        }
        return best;
    }

    private String latestProcessState(EntityManager em, String pid) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery("SELECT s.STATE, s.TIME_OF_STATE_CHANGE FROM EXEC_STATUS s "
                        + "WHERE s.ENTITY_TYPE = 'PROCESS' AND s.ENTITY_ID = :pid")
                .setParameter("pid", pid)
                .getResultList();
        String best = null;
        long bestTime = -1;
        for (Object[] a : rows) {
            long time = toMillis(a[1]);
            if (time > bestTime) {
                bestTime = time;
                best = (String) a[0];
            }
        }
        return best;
    }

    private void insertStatus(
            EntityManager em, String entityType, String entityId, String state, long timeMillis, String reason) {
        String prefix =
                "PROCESS".equals(entityType) ? "PROCESS_STATE" : "JOB".equals(entityType) ? "JOB_STATE" : "TASK_STATE";
        em.createNativeQuery("INSERT INTO EXEC_STATUS "
                        + "(STATUS_ID, ENTITY_TYPE, ENTITY_ID, STATE, TIME_OF_STATE_CHANGE, REASON) "
                        + "VALUES (:sid, :etype, :eid, :state, :ts, :reason)")
                .setParameter("sid", ExpCatalogUtils.getID(prefix))
                .setParameter("etype", entityType)
                .setParameter("eid", entityId)
                .setParameter("state", state)
                .setParameter("ts", new Timestamp(timeMillis))
                .setParameter("reason", reason == null ? "" : reason)
                .executeUpdate();
    }

    // ----------------------------------------------------------------- pure helpers

    private ProcessState processStageFor(
            TaskTypes taskType, String taskId, List<String> dag, Map<String, TaskTypes> types) {
        switch (taskType) {
            case ENV_SETUP:
                return ProcessState.PROCESS_STATE_CONFIGURING_WORKSPACE;
            case JOB_SUBMISSION:
                return ProcessState.PROCESS_STATE_EXECUTING;
            case MONITORING:
                return ProcessState.PROCESS_STATE_MONITORING;
            case ENV_CLEANUP:
                return ProcessState.PROCESS_STATE_POST_PROCESSING;
            case OUTPUT_FETCHING:
                return ProcessState.PROCESS_STATE_OUTPUT_DATA_STAGING;
            case DATA_STAGING:
                int submissionIdx = -1;
                for (int i = 0; i < dag.size(); i++) {
                    if (types.get(dag.get(i)) == TaskTypes.JOB_SUBMISSION) {
                        submissionIdx = i;
                        break;
                    }
                }
                int myIdx = dag.indexOf(taskId);
                return (submissionIdx >= 0 && myIdx > submissionIdx)
                        ? ProcessState.PROCESS_STATE_OUTPUT_DATA_STAGING
                        : ProcessState.PROCESS_STATE_INPUT_DATA_STAGING;
            default:
                return ProcessState.PROCESS_STATE_EXECUTING;
        }
    }

    /**
     * Tie-break rank for two EXEC_STATUS rows of the same task that share a
     * TIME_OF_STATE_CHANGE. TIME_OF_STATE_CHANGE is second-precision, so a fast
     * EXECUTING -> CREATED requeue (finishTask writes CREATED in the same second the
     * task was claimed EXECUTING) lands on the same timestamp. CREATED must out-rank
     * EXECUTING so that requeue wins the tie and the task is re-claimed on the next poll
     * instead of being masked as still-EXECUTING and stalled for a full lease interval.
     * This is only a same-timestamp tiebreak; the primary ordering is still by time, so
     * any later EXECUTING (the normal CREATED -> EXECUTING start, written with a fresh
     * timestamp at claim time) still wins by being strictly newer.
     */
    private static int stateRank(String state) {
        TaskState s = parseTaskState(state);
        if (s == TaskState.TASK_STATE_COMPLETED
                || s == TaskState.TASK_STATE_FAILED
                || s == TaskState.TASK_STATE_CANCELED) {
            return 4;
        }
        if (s == TaskState.TASK_STATE_CREATED) {
            return 3;
        }
        if (s == TaskState.TASK_STATE_EXECUTING) {
            return 2;
        }
        return 1;
    }

    private static TaskTypes parseTaskType(String s) {
        if (s == null) {
            return TaskTypes.TASK_TYPES_UNKNOWN;
        }
        try {
            return TaskTypes.valueOf(s);
        } catch (IllegalArgumentException e) {
            return TaskTypes.TASK_TYPES_UNKNOWN;
        }
    }

    private static TaskState parseTaskState(String s) {
        if (s == null) {
            return TaskState.TASK_STATE_UNKNOWN;
        }
        try {
            return TaskState.valueOf(s);
        } catch (IllegalArgumentException e) {
            return TaskState.TASK_STATE_UNKNOWN;
        }
    }

    private static long toMillis(Object dbTimestamp) {
        if (dbTimestamp == null) {
            return 0L;
        }
        if (dbTimestamp instanceof Timestamp ts) {
            return ts.getTime();
        }
        if (dbTimestamp instanceof java.util.Date d) {
            return d.getTime();
        }
        if (dbTimestamp instanceof java.time.LocalDateTime ldt) {
            return ldt.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
        if (dbTimestamp instanceof java.time.Instant in) {
            return in.toEpochMilli();
        }
        return 0L;
    }

    private static ThreadFactory namedDaemon(String prefix) {
        AtomicInteger seq = new AtomicInteger();
        return r -> {
            Thread t = new Thread(r, prefix + "-" + seq.incrementAndGet());
            t.setDaemon(true);
            return t;
        };
    }

    private static final class Claim {
        final String processId;
        final String experimentId;
        final String taskId;
        final TaskTypes taskType;

        Claim(String processId, String experimentId, String taskId, TaskTypes taskType) {
            this.processId = processId;
            this.experimentId = experimentId;
            this.taskId = taskId;
            this.taskType = taskType;
        }
    }
}
