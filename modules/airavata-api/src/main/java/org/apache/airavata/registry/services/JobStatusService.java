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
import org.apache.airavata.common.model.JobState;
import org.apache.airavata.common.model.JobStatus;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.registry.entities.expcatalog.JobEntity;
import org.apache.airavata.registry.entities.expcatalog.JobPK;
import org.apache.airavata.registry.entities.expcatalog.JobStatusEntity;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.mappers.JobStatusMapper;
import org.apache.airavata.registry.repositories.expcatalog.JobRepository;
import org.apache.airavata.registry.repositories.expcatalog.JobStatusRepository;
import org.apache.airavata.registry.utils.ExpCatalogUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class JobStatusService {
    private final JobStatusRepository jobStatusRepository;
    private final JobStatusMapper jobStatusMapper;
    private final JobRepository jobRepository;
    private final EntityManager entityManager;

    // Track last timestamp per job to ensure strict ordering even for rapid additions
    private static final java.util.Map<String, Timestamp> lastJobTimestamps =
            new java.util.concurrent.ConcurrentHashMap<>();
    private static final Object timestampLock = new Object();

    /**
     * Gets a unique timestamp for a job with microsecond precision.
     * Uses in-memory cache and database query to ensure it's always greater than previous.
     * This method is thread-safe and handles edge cases in distributed systems.
     */
    private Timestamp getUniqueTimestampForJob(JobPK jobPK) {
        String jobKey = jobPK.getJobId() + ":" + jobPK.getTaskId();
        synchronized (timestampLock) {
            // Get cached timestamp (from previous operations in this JVM)
            Timestamp lastCached = lastJobTimestamps.get(jobKey);

            // Query database to get the actual latest timestamp (handles distributed scenarios)
            // Note: Native queries in the same transaction see uncommitted changes, so no flush needed
            // Avoid flushing to prevent triggering persistence of unrelated entities (like Process)
            // that might not be fully initialized

            // Use native query to get the actual latest timestamp from database
            // This bypasses JPA caching and ensures we see the real persisted data
            // Critical for distributed systems where cache might be stale
            @SuppressWarnings("unchecked")
            List<Timestamp> timestamps = entityManager
                    .createNativeQuery(
                            "SELECT TIME_OF_STATE_CHANGE FROM JOB_STATUS " + "WHERE JOB_ID = ? AND TASK_ID = ? "
                                    + "ORDER BY TIME_OF_STATE_CHANGE DESC, STATUS_ID DESC "
                                    + "LIMIT 1")
                    .setParameter(1, jobPK.getJobId())
                    .setParameter(2, jobPK.getTaskId())
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

                if (baseTime < lastTime) {
                    // Clock went backwards (edge case in distributed systems)
                    // Increment by 1 millisecond
                    baseTimestamp = new Timestamp(lastTime + 1);
                    baseTimestamp.setNanos(0);
                } else if (baseTime == lastTime) {
                    // Same millisecond - must increment nanos to ensure strict ordering
                    int lastNanos = lastTimestamp.getNanos();
                    int baseNanos = baseTimestamp.getNanos();

                    // Ensure new nanos is strictly greater
                    // Use 100ms increment to handle DBs with only millisecond/low precision
                    // 1 second (1000ms) might be safer but 100ms is a good compromise
                    int newNanos = Math.max(baseNanos, lastNanos + 100_000_000);

                    if (newNanos >= 1_000_000_000) {
                        // Overflow to next second(s)
                        long extraSeconds = newNanos / 1_000_000_000;
                        baseTimestamp = new Timestamp(lastTime + extraSeconds * 1000); // Add seconds
                        baseTimestamp.setNanos(newNanos % 1_000_000_000);
                    } else {
                        baseTimestamp = new Timestamp(baseTime);
                        baseTimestamp.setNanos(newNanos);
                    }
                }
                // If baseTime > lastTime, we're good - baseTimestamp is already greater
            }

            // Update cache with the timestamp we'll use (BEFORE returning)
            // This ensures the next call in the same JVM will see this timestamp
            // Note: addJobStatus/updateJobStatus will update cache again with persistedTimestamp if different
            lastJobTimestamps.put(jobKey, baseTimestamp);

            return baseTimestamp;
        }
    }

    /**
     * Ensures the given timestamp is greater than the last timestamp for this job.
     * Queries database to get actual latest timestamp.
     */
    private Timestamp ensureTimestampOrdering(JobPK jobPK, Timestamp timestamp) {
        // Note: Native queries in the same transaction see uncommitted changes, so no flush needed
        // Avoid flushing to prevent triggering persistence of unrelated entities (like Process)
        // that might not be fully initialized

        // Check cache first - it's more reliable in the same transaction
        String jobKey = jobPK.getJobId() + ":" + jobPK.getTaskId();
        Timestamp lastCached = lastJobTimestamps.get(jobKey);

        // Use native query to get the actual latest timestamp for this job
        // This ensures we see flushed changes in the same transaction
        @SuppressWarnings("unchecked")
        List<Timestamp> timestamps = entityManager
                .createNativeQuery("SELECT TIME_OF_STATE_CHANGE FROM JOB_STATUS " + "WHERE JOB_ID = ? AND TASK_ID = ? "
                        + "ORDER BY TIME_OF_STATE_CHANGE DESC, STATUS_ID DESC "
                        + "LIMIT 1")
                .setParameter(1, jobPK.getJobId())
                .setParameter(2, jobPK.getTaskId())
                .getResultList();

        Timestamp dbLatest = null;
        if (!timestamps.isEmpty() && timestamps.get(0) != null) {
            dbLatest = (Timestamp) timestamps.get(0);
        }

        // Use the maximum of cache and database
        // CRITICAL: In the same transaction, prefer cache if it's >= database
        // because native queries might not see uncommitted changes even after flush
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

        if (lastTimestamp != null) {
            if (!timestamp.after(lastTimestamp)) {
                // Increment by 1 millisecond to ensure strict ordering
                timestamp = new Timestamp(lastTimestamp.getTime() + 1);
                timestamp.setNanos(0);
            } else if (timestamp.getTime() == lastTimestamp.getTime()) {
                // Same millisecond - increment nanos
                int lastNanos = lastTimestamp.getNanos();
                int newNanos = lastNanos + 1_000_000; // Add 1 millisecond
                if (newNanos >= 1_000_000_000) {
                    timestamp = new Timestamp(lastTimestamp.getTime() + 1);
                    timestamp.setNanos(newNanos % 1_000_000_000);
                } else {
                    timestamp = new Timestamp(timestamp.getTime());
                    timestamp.setNanos(newNanos);
                }
            }
        }

        return timestamp;
    }

    public JobStatusService(
            JobStatusRepository jobStatusRepository,
            JobStatusMapper jobStatusMapper,
            JobRepository jobRepository,
            EntityManager entityManager) {
        this.jobStatusRepository = jobStatusRepository;
        this.jobStatusMapper = jobStatusMapper;
        this.jobRepository = jobRepository;
        this.entityManager = entityManager;
    }

    public JobStatus getJobStatus(JobPK jobPK) throws RegistryException {
        // Note: Native queries in the same transaction see uncommitted changes, so no flush needed
        // Avoid flushing to prevent triggering persistence of unrelated entities (like Process)
        // that might not be fully initialized

        // Use native query to bypass any JPA caching and ensure we get the latest from database
        // This is critical for seeing the most recent status in the same transaction
        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager
                .createNativeQuery(
                        "SELECT STATUS_ID, JOB_ID, TASK_ID, STATE, REASON, TIME_OF_STATE_CHANGE " + "FROM JOB_STATUS "
                                + "WHERE JOB_ID = ? AND TASK_ID = ? "
                                + "ORDER BY TIME_OF_STATE_CHANGE DESC, STATUS_ID DESC "
                                + "LIMIT 1")
                .setParameter(1, jobPK.getJobId())
                .setParameter(2, jobPK.getTaskId())
                .getResultList();

        if (results.isEmpty()) return null;

        // Convert native query result to entity
        Object[] row = results.get(0);
        JobStatusEntity entity = new JobStatusEntity();
        entity.setStatusId((String) row[0]);
        entity.setJobId((String) row[1]);
        entity.setTaskId((String) row[2]);
        // Convert String to JobState enum
        if (row[3] != null) {
            entity.setJobState(JobState.valueOf((String) row[3]));
        }
        entity.setReason((String) row[4]);
        entity.setTimeOfStateChange((java.sql.Timestamp) row[5]);

        return jobStatusMapper.toModel(entity);
    }

    public void addJobStatus(JobStatus jobStatus, JobPK jobPK) throws RegistryException {
        if (jobStatus.getStatusId() == null) {
            jobStatus.setStatusId(ExpCatalogUtils.getID("JOB_STATE"));
        }
        // Use unique timestamp to ensure proper ordering
        // Get timestamp BEFORE any database operations to ensure it's truly unique
        Timestamp uniqueTimestamp = getUniqueTimestampForJob(jobPK);
        jobStatus.setTimeOfStateChange(uniqueTimestamp.getTime());

        JobStatusEntity entity = jobStatusMapper.toEntity(jobStatus);
        entity.setJobId(jobPK.getJobId());
        entity.setTaskId(jobPK.getTaskId());
        // CRITICAL: Set timestamp on entity BEFORE any save operations
        // This ensures our explicit timestamp is used, not database default
        entity.setTimeOfStateChange(uniqueTimestamp);

        // Verify job exists before saving status
        JobEntity jobEntity = jobRepository
                .findById(jobPK)
                .orElseThrow(() -> new RegistryException("Job with ID " + jobPK.getJobId() + " does not exist"));

        // Note: We don't call entity.setJob() because the @JoinColumn has insertable=false.
        // The jobId and taskId fields are already set and are the only fields that get persisted.

        // Ensure the jobStatuses collection is initialized
        if (jobEntity.getJobStatuses() == null) {
            jobEntity.setJobStatuses(new java.util.ArrayList<>());
        }

        // Save the entity - it will be persisted when the transaction commits
        JobStatusEntity savedEntity = jobStatusRepository.save(entity);

        // CRITICAL: Flush to ensure the status is visible when getJob() is called in the same transaction
        // This is necessary because getJob() uses entityManager.refresh() which queries the database
        entityManager.flush();

        // CRITICAL: Verify timestamp was persisted correctly and force it if needed
        // In distributed systems, database defaults might override our timestamp
        // We must ensure our explicit timestamp is used to maintain strict ordering
        Timestamp persistedTimestamp = savedEntity.getTimeOfStateChange();
        if (persistedTimestamp == null || !persistedTimestamp.equals(uniqueTimestamp)) {
            // Database overrode our timestamp - force it via native update
            // This handles edge cases where database defaults interfere
            entityManager
                    .createNativeQuery("UPDATE JOB_STATUS SET TIME_OF_STATE_CHANGE = ? "
                            + "WHERE STATUS_ID = ? AND JOB_ID = ? AND TASK_ID = ?")
                    .setParameter(1, uniqueTimestamp)
                    .setParameter(2, savedEntity.getStatusId())
                    .setParameter(3, savedEntity.getJobId())
                    .setParameter(4, savedEntity.getTaskId())
                    .executeUpdate();
            // Flush only after native update to ensure the update is persisted
            // This is safe because we're only updating an existing entity, not creating new ones
            entityManager.flush();
            // Note: We don't call refresh() here to avoid HHH000502 warnings.
            // The refresh would load the 'job' property which is marked as immutable.
            // We already know the correct timestamp since we just forced it.
            persistedTimestamp = uniqueTimestamp;
        } else {
            persistedTimestamp = uniqueTimestamp;
        }

        // CRITICAL: Update cache with the timestamp we actually persisted
        // This ensures the next status will have a timestamp strictly greater than this one
        // This is essential for handling rapid status additions in distributed systems
        String jobKey = jobPK.getJobId() + ":" + jobPK.getTaskId();
        synchronized (timestampLock) {
            Timestamp lastCached = lastJobTimestamps.get(jobKey);
            // Always update cache to ensure strict ordering
            // If persisted equals last cached, increment to ensure next is strictly greater
            if (lastCached == null || persistedTimestamp.after(lastCached)) {
                lastJobTimestamps.put(jobKey, persistedTimestamp);
            } else if (persistedTimestamp.equals(lastCached)) {
                // Edge case: timestamp equals last - increment by 1 second for next status
                long newTime = persistedTimestamp.getTime() + 1000;
                Timestamp incremented = new Timestamp(newTime);
                incremented.setNanos(0);
                lastJobTimestamps.put(jobKey, incremented);
            }
        }

        // Note: We don't add to jobEntity.getJobStatuses() collection because:
        // 1. The entity is already saved with the correct foreign key (jobId, taskId)
        // 2. Adding to the collection triggers Hibernate to sync the bidirectional relationship,
        //    which causes HHH000502 warnings because the 'job' property is immutable
        // 3. The collection will be correctly populated when the parent is reloaded/queried
    }

    public void updateJobStatus(JobStatus jobStatus, JobPK jobPK) throws RegistryException {
        // Always generate a new statusId to ensure we create a new status entry, not update an existing one
        jobStatus.setStatusId(ExpCatalogUtils.getID("JOB_STATE"));

        // Always use unique timestamp to ensure proper ordering
        // Even if a timestamp is provided, we ensure it's greater than previous statuses
        Timestamp uniqueTimestamp;
        if (jobStatus.getTimeOfStateChange() > 0) {
            long providedTime = jobStatus.getTimeOfStateChange();
            long currentTime = AiravataUtils.getUniqueTimestamp().getTime();
            // If provided time is in the future or very recent (within 5 seconds), use it but ensure ordering
            if (providedTime >= currentTime - 5000) {
                uniqueTimestamp = new java.sql.Timestamp(providedTime);
                // Ensure it's greater than last timestamp for this job
                uniqueTimestamp = ensureTimestampOrdering(jobPK, uniqueTimestamp);
                jobStatus.setTimeOfStateChange(uniqueTimestamp.getTime());
            } else {
                // Provided time is too old, use unique timestamp to ensure proper ordering
                uniqueTimestamp = getUniqueTimestampForJob(jobPK);
                jobStatus.setTimeOfStateChange(uniqueTimestamp.getTime());
            }
        } else {
            // No timestamp provided, use unique timestamp
            uniqueTimestamp = getUniqueTimestampForJob(jobPK);
            jobStatus.setTimeOfStateChange(uniqueTimestamp.getTime());
        }
        JobStatusEntity entity = jobStatusMapper.toEntity(jobStatus);
        entity.setJobId(jobPK.getJobId());
        entity.setTaskId(jobPK.getTaskId());
        // Ensure timestamp is set correctly on the entity using the same unique timestamp
        entity.setTimeOfStateChange(uniqueTimestamp);

        // Verify job exists before saving status
        JobEntity jobEntity = jobRepository
                .findById(jobPK)
                .orElseThrow(() -> new RegistryException("Job with ID " + jobPK.getJobId() + " does not exist"));

        // Note: We don't call entity.setJob() because the @JoinColumn has insertable=false.
        // The jobId and taskId fields are already set and are the only fields that get persisted.

        // Ensure the jobStatuses collection is initialized
        if (jobEntity.getJobStatuses() == null) {
            jobEntity.setJobStatuses(new java.util.ArrayList<>());
        }

        // Save the entity - it will be persisted when the transaction commits
        JobStatusEntity savedEntity = jobStatusRepository.save(entity);

        // CRITICAL: Flush to ensure the status is visible when getJob() is called in the same transaction
        // This is necessary because getJob() uses entityManager.refresh() which queries the database
        entityManager.flush();

        // Verify timestamp was persisted correctly
        // In distributed systems, database defaults might override our timestamp
        Timestamp persistedTimestamp = savedEntity.getTimeOfStateChange();
        if (persistedTimestamp == null || !persistedTimestamp.equals(uniqueTimestamp)) {
            // Database overrode our timestamp - force it via native update
            entityManager
                    .createNativeQuery("UPDATE JOB_STATUS SET TIME_OF_STATE_CHANGE = ? "
                            + "WHERE STATUS_ID = ? AND JOB_ID = ? AND TASK_ID = ?")
                    .setParameter(1, uniqueTimestamp)
                    .setParameter(2, savedEntity.getStatusId())
                    .setParameter(3, savedEntity.getJobId())
                    .setParameter(4, savedEntity.getTaskId())
                    .executeUpdate();
            // Flush only after native update to ensure the update is persisted
            // This is safe because we're only updating an existing entity, not creating new ones
            entityManager.flush();
            // Note: We don't call refresh() here to avoid HHH000502 warnings.
            // The refresh would load the 'job' property which is marked as immutable.
            // We already know the correct timestamp since we just forced it.
            persistedTimestamp = uniqueTimestamp;
        }

        // CRITICAL: Update cache with the timestamp we actually persisted
        // This ensures the next status will have a timestamp strictly greater than this one
        String jobKey = jobPK.getJobId() + ":" + jobPK.getTaskId();
        synchronized (timestampLock) {
            // Use persistedTimestamp which is either the saved value or the forced uniqueTimestamp
            if (persistedTimestamp != null) {
                Timestamp lastCached = lastJobTimestamps.get(jobKey);
                // Always update cache to ensure strict ordering
                if (lastCached == null || persistedTimestamp.after(lastCached)) {
                    lastJobTimestamps.put(jobKey, persistedTimestamp);
                } else if (persistedTimestamp.equals(lastCached)) {
                    // Edge case: timestamp equals last - increment by 1 microsecond for next status
                    long newTime = persistedTimestamp.getTime();
                    int newNanos = persistedTimestamp.getNanos() + 1000;
                    if (newNanos >= 1_000_000_000) {
                        newTime += 1;
                        newNanos = 0;
                    }
                    Timestamp incremented = new Timestamp(newTime);
                    incremented.setNanos(newNanos);
                    lastJobTimestamps.put(jobKey, incremented);
                }
            }
        }

        // Note: We don't add to jobEntity.getJobStatuses() collection because:
        // 1. The entity is already saved with the correct foreign key (jobId, taskId)
        // 2. Adding to the collection triggers Hibernate to sync the bidirectional relationship,
        //    which causes HHH000502 warnings because the 'job' property is immutable
        // 3. The collection will be correctly populated when the parent is reloaded/queried
    }

    public List<String> getDistinctListofJobStatus(String gatewayId, String state, double minutes)
            throws RegistryException {
        return jobStatusRepository.findDistinctJobIdsByGatewayIdAndStateAndTime(gatewayId, state, minutes);
    }
}
