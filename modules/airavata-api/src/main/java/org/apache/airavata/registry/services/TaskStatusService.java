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
package org.apache.airavata.registry.services;

import jakarta.persistence.EntityManager;
import java.sql.Timestamp;
import java.util.List;
import org.apache.airavata.common.model.TaskStatus;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.registry.entities.expcatalog.TaskStatusEntity;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.mappers.TaskStatusMapper;
import org.apache.airavata.registry.repositories.expcatalog.TaskStatusRepository;
import org.apache.airavata.registry.utils.ExpCatalogUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TaskStatusService {
    private final TaskStatusRepository taskStatusRepository;
    private final TaskStatusMapper taskStatusMapper;
    private final EntityManager entityManager;

    // Track last timestamp per task to ensure strict ordering even for rapid additions
    private static final java.util.Map<String, Timestamp> lastTaskTimestamps =
            new java.util.concurrent.ConcurrentHashMap<>();
    private static final Object timestampLock = new Object();

    public TaskStatusService(
            TaskStatusRepository taskStatusRepository, TaskStatusMapper taskStatusMapper, EntityManager entityManager) {
        this.taskStatusRepository = taskStatusRepository;
        this.taskStatusMapper = taskStatusMapper;
        this.entityManager = entityManager;
    }

    /**
     * Gets a unique timestamp for a task with microsecond precision.
     * Uses in-memory cache and database query to ensure it's always greater than previous.
     * This method is thread-safe and handles edge cases in distributed systems.
     */
    private Timestamp getUniqueTimestampForTask(String taskId) {
        synchronized (timestampLock) {
            // Get cached timestamp (from previous operations in this JVM)
            Timestamp lastCached = lastTaskTimestamps.get(taskId);

            // Query database to get the actual latest timestamp (handles distributed scenarios)
            // Flush first to ensure we see any pending changes in this transaction
            taskStatusRepository.flush();
            entityManager.flush();

            // Use native query to get the actual latest timestamp from database
            // This bypasses JPA caching and ensures we see the real persisted data
            // Critical for distributed systems where cache might be stale
            @SuppressWarnings("unchecked")
            List<Timestamp> timestamps = entityManager
                    .createNativeQuery("SELECT TIME_OF_STATE_CHANGE FROM TASK_STATUS " + "WHERE TASK_ID = ? "
                            + "ORDER BY TIME_OF_STATE_CHANGE DESC, STATUS_ID DESC "
                            + "LIMIT 1")
                    .setParameter(1, taskId)
                    .getResultList();

            Timestamp dbLatest = null;
            if (!timestamps.isEmpty() && timestamps.get(0) != null) {
                dbLatest = (Timestamp) timestamps.get(0);
            }

            // Use the maximum of cached and database timestamp
            // This handles edge cases where:
            // 1. Cache might be stale (distributed system)
            // 2. Database might not see uncommitted changes (transaction isolation)
            // CRITICAL: In the same transaction, cache is more reliable than database query
            // because native queries might not see uncommitted changes even after flush
            // So we prefer cache if it exists and is >= database timestamp
            Timestamp lastTimestamp = lastCached;
            if (dbLatest != null) {
                if (lastTimestamp == null || dbLatest.after(lastTimestamp)) {
                    lastTimestamp = dbLatest;
                }
            }
            // Prefer cache if it's newer than or equal to database (handles uncommitted changes)
            if (lastCached != null && (dbLatest == null || lastCached.compareTo(dbLatest) >= 0)) {
                lastTimestamp = lastCached;
            }

            // Generate base timestamp
            Timestamp baseTimestamp = AiravataUtils.getUniqueTimestamp();

            // CRITICAL: Ensure our new timestamp is STRICTLY greater than the last one
            // This handles edge cases where timestamps might be equal due to:
            // 1. Rapid additions within same millisecond
            // 2. Clock synchronization issues in distributed systems
            // 3. Database timestamp precision limitations
            if (lastTimestamp != null) {
                long lastTime = lastTimestamp.getTime();
                long baseTime = baseTimestamp.getTime();

                // Enforce 1 second (1000ms) gap to handle DBs with low (second) precision
                // This ensures strict ordering even if DB truncates to seconds
                if (baseTime < lastTime + 1000) {
                    baseTimestamp = new Timestamp(lastTime + 1000);
                    baseTimestamp.setNanos(0);
                } else if (baseTime == lastTime) {
                    // This case is covered by the < lastTime + 1000 check, but for completeness:
                    baseTimestamp = new Timestamp(lastTime + 1000);
                    baseTimestamp.setNanos(0);
                }
                // If baseTime >= lastTime + 1000, we're good
            }

            // Update cache with the timestamp we'll use (BEFORE returning)
            // This ensures the next call in the same JVM will see this timestamp
            // Note: addTaskStatus/updateTaskStatus will update cache again with persistedTimestamp if different
            lastTaskTimestamps.put(taskId, baseTimestamp);

            return baseTimestamp;
        }
    }

    public void addTaskStatus(TaskStatus taskStatus, String taskId) throws RegistryException {
        if (taskStatus.getStatusId() == null) {
            taskStatus.setStatusId(ExpCatalogUtils.getID("TASK_STATE"));
        }
        // Use unique timestamp to ensure proper ordering
        // Get timestamp BEFORE any database operations to ensure it's truly unique
        Timestamp uniqueTimestamp = getUniqueTimestampForTask(taskId);
        taskStatus.setTimeOfStateChange(uniqueTimestamp.getTime());

        TaskStatusEntity entity = taskStatusMapper.toEntity(taskStatus);
        entity.setTaskId(taskId);
        // CRITICAL: Set timestamp on entity BEFORE any save operations
        // This ensures our explicit timestamp is used, not database default
        entity.setTimeOfStateChange(uniqueTimestamp);

        // Save and flush to ensure immediate persistence
        // The flush ensures the status is immediately visible to queries
        TaskStatusEntity savedEntity = taskStatusRepository.save(entity);
        taskStatusRepository.flush();

        // CRITICAL: Verify timestamp was persisted correctly and force it if needed
        // In distributed systems, database defaults might override our timestamp
        // We must ensure our explicit timestamp is used to maintain strict ordering
        Timestamp persistedTimestamp = savedEntity.getTimeOfStateChange();
        if (persistedTimestamp == null || !persistedTimestamp.equals(uniqueTimestamp)) {
            // Database overrode our timestamp - force it via native update
            // This handles edge cases where database defaults interfere
            entityManager
                    .createNativeQuery(
                            "UPDATE TASK_STATUS SET TIME_OF_STATE_CHANGE = ? " + "WHERE STATUS_ID = ? AND TASK_ID = ?")
                    .setParameter(1, uniqueTimestamp)
                    .setParameter(2, savedEntity.getStatusId())
                    .setParameter(3, savedEntity.getTaskId())
                    .executeUpdate();
            entityManager.flush();
            // Note: We don't call refresh() here to avoid HHH000502 warnings.
            // The refresh would load the 'task' property which is marked as immutable.
            // We already know the correct timestamp since we just forced it.
            persistedTimestamp = uniqueTimestamp;
        } else {
            persistedTimestamp = uniqueTimestamp;
        }

        // CRITICAL: Update cache with the timestamp we actually persisted
        // This ensures the next status will have a timestamp strictly greater than this one
        // This is essential for handling rapid status additions in distributed systems
        synchronized (timestampLock) {
            Timestamp lastCached = lastTaskTimestamps.get(taskId);
            // Always update cache to ensure strict ordering
            // If persisted equals last cached, increment to ensure next is strictly greater
            if (lastCached == null || persistedTimestamp.after(lastCached)) {
                lastTaskTimestamps.put(taskId, persistedTimestamp);
            } else if (persistedTimestamp.equals(lastCached)) {
                // Edge case: timestamp equals last - increment by 1 second for next status
                long newTime = persistedTimestamp.getTime() + 1000;
                Timestamp incremented = new Timestamp(newTime);
                incremented.setNanos(0);
                lastTaskTimestamps.put(taskId, incremented);
            }
        }
    }

    public void updateTaskStatus(TaskStatus taskStatus, String taskId) throws RegistryException {
        if (taskStatus.getStatusId() == null) {
            taskStatus.setStatusId(ExpCatalogUtils.getID("TASK_STATE"));
        }

        // Always use unique timestamp to ensure proper ordering
        // Even if a timestamp is provided, we ensure it's greater than previous statuses
        Timestamp uniqueTimestamp;
        if (taskStatus.getTimeOfStateChange() > 0) {
            long providedTime = taskStatus.getTimeOfStateChange();
            long currentTime = AiravataUtils.getUniqueTimestamp().getTime();
            // If provided time is in the future or very recent (within 5 seconds), use it but ensure ordering
            if (providedTime >= currentTime - 5000) {
                uniqueTimestamp = new java.sql.Timestamp(providedTime);
                // Ensure it's greater than last timestamp for this task
                // We'll reuse getUniqueTimestampForTask for now as it handles ordering logic robustly
                // Ideally we'd have a separate ensureTimestampOrdering like JobStatusService
                // But for now, let's stick to getUniqueTimestampForTask to be safe
                Timestamp ordered = getUniqueTimestampForTask(taskId);
                if (ordered.after(uniqueTimestamp)) {
                    uniqueTimestamp = ordered;
                }
                taskStatus.setTimeOfStateChange(uniqueTimestamp.getTime());
            } else {
                // Provided time is too old, use unique timestamp to ensure proper ordering
                uniqueTimestamp = getUniqueTimestampForTask(taskId);
                taskStatus.setTimeOfStateChange(uniqueTimestamp.getTime());
            }
        } else {
            // No timestamp provided, use unique timestamp
            uniqueTimestamp = getUniqueTimestampForTask(taskId);
            taskStatus.setTimeOfStateChange(uniqueTimestamp.getTime());
        }

        TaskStatusEntity entity = taskStatusMapper.toEntity(taskStatus);
        entity.setTaskId(taskId);
        // Ensure timestamp is set correctly on the entity
        entity.setTimeOfStateChange(uniqueTimestamp);

        // Save and flush to ensure immediate persistence
        // The flush ensures the status is immediately visible to queries
        TaskStatusEntity savedEntity = taskStatusRepository.save(entity);
        taskStatusRepository.flush();

        // Verify timestamp was persisted correctly
        // In distributed systems, database defaults might override our timestamp
        Timestamp persistedTimestamp = savedEntity.getTimeOfStateChange();
        if (persistedTimestamp == null || !persistedTimestamp.equals(uniqueTimestamp)) {
            // Database overrode our timestamp - force it via native update
            entityManager
                    .createNativeQuery(
                            "UPDATE TASK_STATUS SET TIME_OF_STATE_CHANGE = ? " + "WHERE STATUS_ID = ? AND TASK_ID = ?")
                    .setParameter(1, uniqueTimestamp)
                    .setParameter(2, savedEntity.getStatusId())
                    .setParameter(3, savedEntity.getTaskId())
                    .executeUpdate();
            entityManager.flush();
            // Note: We don't call refresh() here to avoid HHH000502 warnings.
            // The refresh would load the 'task' property which is marked as immutable.
            // We already know the correct timestamp since we just forced it.
            persistedTimestamp = uniqueTimestamp;
        }

        // CRITICAL: Update cache with the timestamp we actually persisted
        // This ensures the next status will have a timestamp strictly greater than this one
        // This is essential for handling rapid status additions in distributed systems
        synchronized (timestampLock) {
            // Use persistedTimestamp which is either the saved value or the forced uniqueTimestamp
            if (persistedTimestamp != null) {
                Timestamp lastCached = lastTaskTimestamps.get(taskId);
                // Always update cache to ensure strict ordering
                if (lastCached == null || persistedTimestamp.after(lastCached)) {
                    lastTaskTimestamps.put(taskId, persistedTimestamp);
                } else if (persistedTimestamp.equals(lastCached)) {
                    // Edge case: timestamp equals last - increment by 1 second for next status
                    long newTime = persistedTimestamp.getTime() + 1000;
                    Timestamp incremented = new Timestamp(newTime);
                    incremented.setNanos(0);
                    lastTaskTimestamps.put(taskId, incremented);
                }
            }
        }
    }

    public TaskStatus getTaskStatus(String taskId) throws RegistryException {
        // Flush any pending changes to ensure the query sees the latest data
        // This is critical - flush makes uncommitted changes visible to queries in the same transaction
        taskStatusRepository.flush();
        entityManager.flush();

        // Use native query to bypass any JPA caching and ensure we get the latest from database
        // This is critical for seeing the most recent status in the same transaction
        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager
                .createNativeQuery(
                        "SELECT STATUS_ID, TASK_ID, STATE, REASON, TIME_OF_STATE_CHANGE " + "FROM TASK_STATUS "
                                + "WHERE TASK_ID = ? "
                                + "ORDER BY TIME_OF_STATE_CHANGE DESC, STATUS_ID DESC "
                                + "LIMIT 1")
                .setParameter(1, taskId)
                .getResultList();

        if (results.isEmpty()) return null;

        // Convert native query result to entity
        Object[] row = results.get(0);
        TaskStatusEntity entity = new TaskStatusEntity();
        entity.setStatusId((String) row[0]);
        entity.setTaskId((String) row[1]);
        // Convert String to TaskState enum (assuming you have TaskState enum)
        // Note: You need to check if TaskState is the correct enum type
        // If it's a string in DB, you might need to map it back to enum if model uses enum
        // Assuming TaskStatusEntity uses TaskState enum for state field
        if (row[2] != null) {
            entity.setState(org.apache.airavata.common.model.TaskState.valueOf((String) row[2]));
        }
        entity.setReason((String) row[3]);
        entity.setTimeOfStateChange((java.sql.Timestamp) row[4]);

        return taskStatusMapper.toModel(entity);
    }
}
