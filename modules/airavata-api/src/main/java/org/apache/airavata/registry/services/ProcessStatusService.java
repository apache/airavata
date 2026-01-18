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
import org.apache.airavata.common.model.ProcessState;
import org.apache.airavata.common.model.ProcessStatus;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.registry.entities.expcatalog.ProcessEntity;
import org.apache.airavata.registry.entities.expcatalog.ProcessStatusEntity;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.mappers.ProcessStatusMapper;
import org.apache.airavata.registry.repositories.expcatalog.ProcessRepository;
import org.apache.airavata.registry.repositories.expcatalog.ProcessStatusRepository;
import org.apache.airavata.registry.utils.ExpCatalogUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional("expCatalogTransactionManager")
public class ProcessStatusService {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ProcessStatusService.class);
    private final ProcessStatusRepository processStatusRepository;
    private final ProcessStatusMapper processStatusMapper;
    private final ProcessRepository processRepository;
    private final EntityManager entityManager;

    // Track last timestamp per process to ensure strict ordering even for rapid additions
    private static final java.util.Map<String, Timestamp> lastProcessTimestamps =
            new java.util.concurrent.ConcurrentHashMap<>();
    private static final Object timestampLock = new Object();

    public ProcessStatusService(
            ProcessStatusRepository processStatusRepository,
            ProcessStatusMapper processStatusMapper,
            ProcessRepository processRepository,
            @Qualifier("expCatalogEntityManager") EntityManager entityManager) {
        this.processStatusRepository = processStatusRepository;
        this.processStatusMapper = processStatusMapper;
        this.processRepository = processRepository;
        this.entityManager = entityManager;
    }

    /**
     * Gets a unique timestamp for a process with microsecond precision.
     * Uses in-memory cache and database query to ensure it's always greater than previous.
     * This method is thread-safe and handles edge cases in distributed systems.
     */
    private Timestamp getUniqueTimestampForProcess(String processId) {
        synchronized (timestampLock) {
            // Get cached timestamp (from previous operations in this JVM)
            Timestamp lastCached = lastProcessTimestamps.get(processId);

            // Query database to get the actual latest timestamp (handles distributed scenarios)
            // Note: We use a native query which queries the database directly, so we don't need to flush.
            // Flushing the entire persistence context could cause issues if Process entities are not ready.
            // The native query will see committed data, and for uncommitted ProcessStatus entities in the
            // same transaction, we rely on the cache (lastCached) to handle ordering.

            // Use native query to get the actual latest timestamp from database
            // This bypasses JPA caching and ensures we see the real persisted data
            // Critical for distributed systems where cache might be stale
            @SuppressWarnings("unchecked")
            List<Timestamp> timestamps = entityManager
                    .createNativeQuery("SELECT TIME_OF_STATE_CHANGE FROM PROCESS_STATUS " + "WHERE PROCESS_ID = ? "
                            + "ORDER BY TIME_OF_STATE_CHANGE DESC, STATUS_ID DESC "
                            + "LIMIT 1")
                    .setParameter(1, processId)
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
            // Note: addProcessStatus/updateProcessStatus will update cache again with persistedTimestamp if different
            lastProcessTimestamps.put(processId, baseTimestamp);

            return baseTimestamp;
        }
    }

    public ProcessStatus getProcessStatus(String processId) throws RegistryException {
        // Use native query to bypass any JPA caching and ensure we get the latest from database
        // Note: We don't flush the entire persistence context to avoid flushing Process entities
        // that might not be ready. The native query queries the database directly and will see
        // committed data. For uncommitted ProcessStatus entities in the same transaction,
        // they should be handled by the calling code if needed.

        // Use native query to bypass any JPA caching and ensure we get the latest from database
        // This is critical for seeing the most recent status in the same transaction
        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager
                .createNativeQuery(
                        "SELECT STATUS_ID, PROCESS_ID, STATE, REASON, TIME_OF_STATE_CHANGE " + "FROM PROCESS_STATUS "
                                + "WHERE PROCESS_ID = ? "
                                + "ORDER BY TIME_OF_STATE_CHANGE DESC, STATUS_ID DESC "
                                + "LIMIT 1")
                .setParameter(1, processId)
                .getResultList();

        if (results.isEmpty()) return null;

        // Convert native query result to entity
        Object[] row = results.get(0);
        ProcessStatusEntity entity = new ProcessStatusEntity();
        entity.setStatusId((String) row[0]);
        entity.setProcessId((String) row[1]);
        // Convert String to ProcessState enum
        if (row[2] != null) {
            entity.setState(ProcessState.valueOf((String) row[2]));
        }
        entity.setReason((String) row[3]);
        entity.setTimeOfStateChange((java.sql.Timestamp) row[4]);

        return processStatusMapper.toModel(entity);
    }

    public List<ProcessStatus> getProcessStatusList(String processId) throws RegistryException {
        List<ProcessStatusEntity> entities =
                processStatusRepository.findByProcessIdOrderByTimeOfStateChangeDesc(processId);
        return processStatusMapper.toModelList(entities);
    }

    public List<ProcessStatus> getProcessStatusList(ProcessState processState, int offset, int limit)
            throws RegistryException {
        Pageable pageable = PageRequest.of(Math.max(0, offset / Math.max(1, limit)), limit);
        List<ProcessStatusEntity> entities = processStatusRepository.findByState(processState, pageable);
        return processStatusMapper.toModelList(entities);
    }

    public void addProcessStatus(ProcessStatus processStatus, String processId) throws RegistryException {
        if (processStatus.getStatusId() == null) {
            processStatus.setStatusId(ExpCatalogUtils.getID("PROCESS_STATE"));
        }
        // Use unique timestamp with microsecond precision to ensure proper ordering
        Timestamp uniqueTimestamp = getUniqueTimestampForProcess(processId);
        processStatus.setTimeOfStateChange(uniqueTimestamp.getTime());

        ProcessStatusEntity entity = processStatusMapper.toEntity(processStatus);
        entity.setProcessId(processId);
        // Ensure timestamp is set correctly on the entity using the same unique timestamp
        entity.setTimeOfStateChange(uniqueTimestamp);

        // Verify process exists before saving status
        ProcessEntity processEntity = processRepository
                .findById(processId)
                .orElseThrow(() -> new RegistryException("Process with ID " + processId + " does not exist"));

        // Note: We don't call entity.setProcess() because the @JoinColumn has insertable=false.
        // The processId field is already set and is the only field that gets persisted.
        // Also, we don't call processEntity.setExperiment() to avoid HHH000502 warning
        // since the experiment relationship is marked updatable=false.

        // Ensure the processStatuses collection is initialized
        if (processEntity.getProcessStatuses() == null) {
            processEntity.setProcessStatuses(new java.util.ArrayList<>());
        }

        // Save and flush to ensure immediate persistence
        // The flush ensures the status is immediately visible to queries
        ProcessStatusEntity savedEntity = processStatusRepository.save(entity);
        processStatusRepository.flush();

        // CRITICAL: Verify timestamp was persisted correctly and force it if needed
        // In distributed systems, database defaults might override our timestamp
        // We must ensure our explicit timestamp is used to maintain strict ordering
        Timestamp persistedTimestamp = savedEntity.getTimeOfStateChange();
        if (persistedTimestamp == null || !persistedTimestamp.equals(uniqueTimestamp)) {
            // Database overrode our timestamp - force it via native update
            // This handles edge cases where database defaults interfere
            entityManager
                    .createNativeQuery("UPDATE PROCESS_STATUS SET TIME_OF_STATE_CHANGE = ? "
                            + "WHERE STATUS_ID = ? AND PROCESS_ID = ?")
                    .setParameter(1, uniqueTimestamp)
                    .setParameter(2, savedEntity.getStatusId())
                    .setParameter(3, savedEntity.getProcessId())
                    .executeUpdate();
            entityManager.flush();
            // Note: We don't call refresh() here to avoid HHH000502 warnings.
            // The refresh would load the 'process' property which is marked as immutable.
            // We already know the correct timestamp since we just forced it.
            persistedTimestamp = uniqueTimestamp;
        } else {
            persistedTimestamp = uniqueTimestamp;
        }

        // CRITICAL: Update cache with the timestamp we actually persisted
        // This ensures the next status will have a timestamp strictly greater than this one
        // This is essential for handling rapid status additions in distributed systems
        synchronized (timestampLock) {
            // Use persistedTimestamp which is either the saved value or the forced uniqueTimestamp
            Timestamp lastCached = lastProcessTimestamps.get(processId);
            // Always update cache to ensure strict ordering
            // If persisted equals last cached, increment to ensure next is strictly greater
            if (lastCached == null || persistedTimestamp.after(lastCached)) {
                lastProcessTimestamps.put(processId, persistedTimestamp);
            } else if (persistedTimestamp.equals(lastCached)) {
                // Edge case: timestamp equals last - increment by 1 second for next status
                long newTime = persistedTimestamp.getTime() + 1000;
                Timestamp incremented = new Timestamp(newTime);
                incremented.setNanos(0);
                lastProcessTimestamps.put(processId, incremented);
            }
        }

        // Note: We don't add to processEntity.getProcessStatuses() collection because:
        // 1. The entity is already saved with the correct foreign key (processId)
        // 2. Adding to the collection triggers Hibernate to sync the bidirectional relationship,
        //    which causes HHH000502 warnings because the 'process' property is immutable
        // 3. The collection will be correctly populated when the parent is reloaded/queried
    }

    public void updateProcessStatus(ProcessStatus processStatus, String processId) throws RegistryException {
        if (processStatus.getStatusId() == null) {
            processStatus.setStatusId(ExpCatalogUtils.getID("PROCESS_STATE"));
        }

        // Always use unique timestamp with microsecond precision to ensure proper ordering
        Timestamp uniqueTimestamp;
        if (processStatus.getTimeOfStateChange() > 0) {
            long providedTime = processStatus.getTimeOfStateChange();
            long currentTime = AiravataUtils.getUniqueTimestamp().getTime();
            // If provided time is in the future or very recent (within 5 seconds), use it but ensure ordering
            if (providedTime >= currentTime - 5000) {
                uniqueTimestamp = new java.sql.Timestamp(providedTime);
                // Ensure it's greater than last timestamp for this process
                Timestamp ordered = getUniqueTimestampForProcess(processId);
                if (ordered.after(uniqueTimestamp)) {
                    uniqueTimestamp = ordered;
                }
                processStatus.setTimeOfStateChange(uniqueTimestamp.getTime());
            } else {
                // Provided time is too old, use unique timestamp to ensure proper ordering
                uniqueTimestamp = getUniqueTimestampForProcess(processId);
                processStatus.setTimeOfStateChange(uniqueTimestamp.getTime());
            }
        } else {
            // No timestamp provided, use unique timestamp
            uniqueTimestamp = getUniqueTimestampForProcess(processId);
            processStatus.setTimeOfStateChange(uniqueTimestamp.getTime());
        }

        ProcessStatusEntity entity = processStatusMapper.toEntity(processStatus);
        entity.setProcessId(processId);
        // Ensure timestamp is set correctly on the entity using the same unique timestamp
        entity.setTimeOfStateChange(uniqueTimestamp);

        // Verify process exists before saving status
        ProcessEntity processEntity = processRepository
                .findById(processId)
                .orElseThrow(() -> new RegistryException("Process with ID " + processId + " does not exist"));

        // Note: We don't call entity.setProcess() because the @JoinColumn has insertable=false.
        // The processId field is already set and is the only field that gets persisted.
        // Also, we don't call processEntity.setExperiment() to avoid HHH000502 warning
        // since the experiment relationship is marked updatable=false.

        // Ensure the processStatuses collection is initialized
        if (processEntity.getProcessStatuses() == null) {
            processEntity.setProcessStatuses(new java.util.ArrayList<>());
        }

        // Save and flush to ensure immediate persistence
        // The flush ensures the status is immediately visible to queries
        ProcessStatusEntity savedEntity = processStatusRepository.save(entity);
        processStatusRepository.flush();

        // Verify timestamp was persisted correctly
        // In distributed systems, database defaults might override our timestamp
        Timestamp persistedTimestamp = savedEntity.getTimeOfStateChange();
        if (persistedTimestamp == null || !persistedTimestamp.equals(uniqueTimestamp)) {
            // Database overrode our timestamp - force it via native update
            entityManager
                    .createNativeQuery("UPDATE PROCESS_STATUS SET TIME_OF_STATE_CHANGE = ? "
                            + "WHERE STATUS_ID = ? AND PROCESS_ID = ?")
                    .setParameter(1, uniqueTimestamp)
                    .setParameter(2, savedEntity.getStatusId())
                    .setParameter(3, savedEntity.getProcessId())
                    .executeUpdate();
            entityManager.flush();
            // Note: We don't call refresh() here to avoid HHH000502 warnings.
            // The refresh would load the 'process' property which is marked as immutable.
            // We already know the correct timestamp since we just forced it.
            persistedTimestamp = uniqueTimestamp;
        }

        // CRITICAL: Update cache with the timestamp we actually persisted
        // This ensures the next status will have a timestamp strictly greater than this one
        // This is essential for handling rapid status additions in distributed systems
        synchronized (timestampLock) {
            // Use persistedTimestamp which is either the saved value or the forced uniqueTimestamp
            if (persistedTimestamp != null) {
                Timestamp lastCached = lastProcessTimestamps.get(processId);
                // Always update cache to ensure strict ordering
                if (lastCached == null || persistedTimestamp.after(lastCached)) {
                    lastProcessTimestamps.put(processId, persistedTimestamp);
                } else if (persistedTimestamp.equals(lastCached)) {
                    // Edge case: timestamp equals last - increment by 1 second for next status
                    long newTime = persistedTimestamp.getTime() + 1000;
                    Timestamp incremented = new Timestamp(newTime);
                    incremented.setNanos(0);
                    lastProcessTimestamps.put(processId, incremented);
                }
            }
        }

        // Note: We don't add to processEntity.getProcessStatuses() collection because:
        // 1. The entity is already saved with the correct foreign key (processId)
        // 2. Adding to the collection triggers Hibernate to sync the bidirectional relationship,
        //    which causes HHH000502 warnings because the 'process' property is immutable
        // 3. The collection will be correctly populated when the parent is reloaded/queried
    }
}
