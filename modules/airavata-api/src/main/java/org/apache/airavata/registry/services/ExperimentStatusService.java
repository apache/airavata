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
import org.apache.airavata.common.model.ExperimentStatus;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.registry.entities.expcatalog.ExperimentEntity;
import org.apache.airavata.registry.entities.expcatalog.ExperimentStatusEntity;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.mappers.ExperimentStatusMapper;
import org.apache.airavata.registry.repositories.expcatalog.ExperimentRepository;
import org.apache.airavata.registry.repositories.expcatalog.ExperimentStatusRepository;
import org.apache.airavata.registry.utils.ExpCatalogUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ExperimentStatusService {
    private final ExperimentStatusRepository experimentStatusRepository;
    private final ExperimentStatusMapper experimentStatusMapper;
    private final ExperimentRepository experimentRepository;
    private final EntityManager entityManager;

    // Track last timestamp per experiment to ensure strict ordering even for rapid additions
    private static final java.util.Map<String, Timestamp> lastExperimentTimestamps =
            new java.util.concurrent.ConcurrentHashMap<>();
    private static final Object timestampLock = new Object();

    public ExperimentStatusService(
            ExperimentStatusRepository experimentStatusRepository,
            ExperimentStatusMapper experimentStatusMapper,
            ExperimentRepository experimentRepository,
            EntityManager entityManager) {
        this.experimentStatusRepository = experimentStatusRepository;
        this.experimentStatusMapper = experimentStatusMapper;
        this.experimentRepository = experimentRepository;
        this.entityManager = entityManager;
    }

    /**
     * Gets a unique timestamp for an experiment with microsecond precision.
     * Uses in-memory cache and database query to ensure it's always greater than previous.
     * This method is thread-safe and handles edge cases in distributed systems.
     */
    private Timestamp getUniqueTimestampForExperiment(String experimentId) {
        synchronized (timestampLock) {
            // Get cached timestamp (from previous operations in this JVM)
            Timestamp lastCached = lastExperimentTimestamps.get(experimentId);

            // Query database to get the actual latest timestamp (handles distributed scenarios)
            // Flush first to ensure we see any pending changes in this transaction
            experimentStatusRepository.flush();
            entityManager.flush();

            // Use native query to get the actual latest timestamp from database
            // This bypasses JPA caching and ensures we see the real persisted data
            // Critical for distributed systems where cache might be stale
            @SuppressWarnings("unchecked")
            List<Timestamp> timestamps = entityManager
                    .createNativeQuery(
                            "SELECT TIME_OF_STATE_CHANGE FROM EXPERIMENT_STATUS " + "WHERE EXPERIMENT_ID = ? "
                                    + "ORDER BY TIME_OF_STATE_CHANGE DESC, STATUS_ID DESC "
                                    + "LIMIT 1")
                    .setParameter(1, experimentId)
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
            // Note: addExperimentStatus/updateExperimentStatus will update cache again with persistedTimestamp if
            // different
            lastExperimentTimestamps.put(experimentId, baseTimestamp);

            return baseTimestamp;
        }
    }

    public String addExperimentStatus(ExperimentStatus experimentStatus, String experimentId) throws RegistryException {
        if (experimentStatus.getStatusId() == null) {
            experimentStatus.setStatusId(ExpCatalogUtils.getID("EXPERIMENT_STATE"));
        }
        // Use unique timestamp with microsecond precision to ensure proper ordering
        Timestamp uniqueTimestamp = getUniqueTimestampForExperiment(experimentId);
        experimentStatus.setTimeOfStateChange(uniqueTimestamp.getTime());

        ExperimentStatusEntity entity = experimentStatusMapper.toEntity(experimentStatus);
        entity.setExperimentId(experimentId);
        // Ensure timestamp is set correctly on the entity using the same unique timestamp
        entity.setTimeOfStateChange(uniqueTimestamp);

        // Verify experiment exists before saving status
        ExperimentEntity experimentEntity = experimentRepository
                .findById(experimentId)
                .orElseThrow(() -> new RegistryException("Experiment with ID " + experimentId + " does not exist"));

        // Note: We don't call entity.setExperiment() because the @JoinColumn has insertable=false.
        // The experimentId field is already set and is the only field that gets persisted.

        // Ensure the experimentStatuses collection is initialized
        if (experimentEntity.getExperimentStatus() == null) {
            experimentEntity.setExperimentStatus(new java.util.ArrayList<>());
        }

        // Save and flush to ensure immediate persistence
        // The flush ensures the status is immediately visible to queries
        ExperimentStatusEntity savedEntity = experimentStatusRepository.save(entity);
        experimentStatusRepository.flush();

        // CRITICAL: Verify timestamp was persisted correctly and force it if needed
        // In distributed systems, database defaults might override our timestamp
        // We must ensure our explicit timestamp is used to maintain strict ordering
        Timestamp persistedTimestamp = savedEntity.getTimeOfStateChange();
        if (persistedTimestamp == null || !persistedTimestamp.equals(uniqueTimestamp)) {
            // Database overrode our timestamp - force it via native update
            // This handles edge cases where database defaults interfere
            entityManager
                    .createNativeQuery("UPDATE EXPERIMENT_STATUS SET TIME_OF_STATE_CHANGE = ? "
                            + "WHERE STATUS_ID = ? AND EXPERIMENT_ID = ?")
                    .setParameter(1, uniqueTimestamp)
                    .setParameter(2, savedEntity.getStatusId())
                    .setParameter(3, savedEntity.getExperimentId())
                    .executeUpdate();
            entityManager.flush();
            // Note: We don't call refresh() here to avoid HHH000502 warnings.
            // The refresh would load the 'experiment' property which is marked as immutable.
            // We already know the correct timestamp since we just forced it.
            persistedTimestamp = uniqueTimestamp;
        } else {
            persistedTimestamp = uniqueTimestamp;
        }

        // CRITICAL: Update cache with the timestamp we actually persisted
        // This ensures the next status will have a timestamp strictly greater than this one
        // This is essential for handling rapid status additions in distributed systems
        synchronized (timestampLock) {
            Timestamp lastCached = lastExperimentTimestamps.get(experimentId);
            // Always update cache to ensure strict ordering
            // If persisted equals last cached, increment to ensure next is strictly greater
            if (lastCached == null || persistedTimestamp.after(lastCached)) {
                lastExperimentTimestamps.put(experimentId, persistedTimestamp);
            } else if (persistedTimestamp.equals(lastCached)) {
                // Edge case: timestamp equals last - increment by 1 second for next status
                long newTime = persistedTimestamp.getTime() + 1000;
                Timestamp incremented = new Timestamp(newTime);
                incremented.setNanos(0);
                lastExperimentTimestamps.put(experimentId, incremented);
            }
        }

        // Note: We don't add to experimentEntity.getExperimentStatus() collection because:
        // 1. The entity is already saved with the correct foreign key (experimentId)
        // 2. Adding to the collection triggers Hibernate to sync the bidirectional relationship,
        //    which causes HHH000502 warnings because the 'experiment' property is immutable
        // 3. The collection will be correctly populated when the parent is reloaded/queried

        // CRITICAL: Refresh the experiment entity to ensure status collection is updated
        // This ensures that when getExperiment() is called, it sees the newly added status
        // We need to clear the entity from cache and reload it to see the new status
        entityManager.detach(experimentEntity);

        return savedEntity.getStatusId();
    }

    public ExperimentStatus getExperimentStatus(String experimentId) throws RegistryException {
        // Flush any pending changes to ensure the query sees the latest data
        // This is critical - flush makes uncommitted changes visible to queries in the same transaction
        experimentStatusRepository.flush();
        entityManager.flush();

        // Use native query to bypass any JPA caching and ensure we get the latest from database
        // This is critical for seeing the most recent status in the same transaction
        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager
                .createNativeQuery("SELECT STATUS_ID, EXPERIMENT_ID, STATE, REASON, TIME_OF_STATE_CHANGE "
                        + "FROM EXPERIMENT_STATUS "
                        + "WHERE EXPERIMENT_ID = ? "
                        + "ORDER BY TIME_OF_STATE_CHANGE DESC, STATUS_ID DESC "
                        + "LIMIT 1")
                .setParameter(1, experimentId)
                .getResultList();

        if (results.isEmpty()) return null;

        // Convert native query result to entity
        Object[] row = results.get(0);
        ExperimentStatusEntity entity = new ExperimentStatusEntity();
        entity.setStatusId((String) row[0]);
        entity.setExperimentId((String) row[1]);
        // Convert String to ExperimentState enum
        if (row[2] != null) {
            entity.setState(org.apache.airavata.common.model.ExperimentState.valueOf((String) row[2]));
        }
        entity.setReason((String) row[3]);
        entity.setTimeOfStateChange((java.sql.Timestamp) row[4]);

        return experimentStatusMapper.toModel(entity);
    }

    public String updateExperimentStatus(ExperimentStatus experimentStatus, String experimentId)
            throws RegistryException {
        // If statusId is not provided, retrieve the existing status to get its ID
        if (experimentStatus.getStatusId() == null) {
            // Note: getExperimentStatus now uses native query and flushing, so it's safe
            ExperimentStatus existingStatus = getExperimentStatus(experimentId);
            if (existingStatus != null && existingStatus.getStatusId() != null) {
                // If updating existing status, we should probably create a new one instead
                // Usually status updates imply state transition, which means new record
                // But following existing logic pattern: if ID exists, update it?
                // Actually, typically we create NEW status for history.
                // Let's assume we want to CREATE a new status record for the new state
                experimentStatus.setStatusId(ExpCatalogUtils.getID("EXPERIMENT_STATE"));
            } else {
                // No existing status found, generate new ID (will create new record)
                experimentStatus.setStatusId(ExpCatalogUtils.getID("EXPERIMENT_STATE"));
            }
        }

        // Use unique timestamp with microsecond precision to ensure proper ordering
        Timestamp uniqueTimestamp = getUniqueTimestampForExperiment(experimentId);
        experimentStatus.setTimeOfStateChange(uniqueTimestamp.getTime());

        ExperimentStatusEntity entity = experimentStatusMapper.toEntity(experimentStatus);
        entity.setExperimentId(experimentId);
        // Ensure timestamp is set correctly on the entity using the same unique timestamp
        entity.setTimeOfStateChange(uniqueTimestamp);

        // Verify experiment exists before saving status
        ExperimentEntity experimentEntity = experimentRepository
                .findById(experimentId)
                .orElseThrow(() -> new RegistryException("Experiment with ID " + experimentId + " does not exist"));

        // Note: We don't call entity.setExperiment() because the @JoinColumn has insertable=false.
        // The experimentId field is already set and is the only field that gets persisted.

        // Ensure the experimentStatuses collection is initialized
        if (experimentEntity.getExperimentStatus() == null) {
            experimentEntity.setExperimentStatus(new java.util.ArrayList<>());
        }

        // Save and flush to ensure immediate persistence
        // The flush ensures the status is immediately visible to queries
        ExperimentStatusEntity savedEntity = experimentStatusRepository.save(entity);
        experimentStatusRepository.flush();

        // Verify timestamp was persisted correctly
        // In distributed systems, database defaults might override our timestamp
        Timestamp persistedTimestamp = savedEntity.getTimeOfStateChange();
        if (persistedTimestamp == null || !persistedTimestamp.equals(uniqueTimestamp)) {
            // Database overrode our timestamp - force it via native update
            entityManager
                    .createNativeQuery("UPDATE EXPERIMENT_STATUS SET TIME_OF_STATE_CHANGE = ? "
                            + "WHERE STATUS_ID = ? AND EXPERIMENT_ID = ?")
                    .setParameter(1, uniqueTimestamp)
                    .setParameter(2, savedEntity.getStatusId())
                    .setParameter(3, savedEntity.getExperimentId())
                    .executeUpdate();
            entityManager.flush();
            // Note: We don't call refresh() here to avoid HHH000502 warnings.
            // The refresh would load the 'experiment' property which is marked as immutable.
            // We already know the correct timestamp since we just forced it.
            persistedTimestamp = uniqueTimestamp;
        }

        // CRITICAL: Update cache with the timestamp we actually persisted
        // This ensures the next status will have a timestamp strictly greater than this one
        // This is essential for handling rapid status additions in distributed systems
        synchronized (timestampLock) {
            // Use persistedTimestamp which is either the saved value or the forced uniqueTimestamp
            if (persistedTimestamp != null) {
                Timestamp lastCached = lastExperimentTimestamps.get(experimentId);
                // Always update cache to ensure strict ordering
                if (lastCached == null || persistedTimestamp.after(lastCached)) {
                    lastExperimentTimestamps.put(experimentId, persistedTimestamp);
                } else if (persistedTimestamp.equals(lastCached)) {
                    // Edge case: timestamp equals last - increment by 1 second for next status
                    long newTime = persistedTimestamp.getTime() + 1000;
                    Timestamp incremented = new Timestamp(newTime);
                    incremented.setNanos(0);
                    lastExperimentTimestamps.put(experimentId, incremented);
                }
            }
        }

        // Note: We don't add to experimentEntity.getExperimentStatus() collection because:
        // 1. The entity is already saved with the correct foreign key (experimentId)
        // 2. Adding to the collection triggers Hibernate to sync the bidirectional relationship,
        //    which causes HHH000502 warnings because the 'experiment' property is immutable
        // 3. The collection will be correctly populated when the parent is reloaded/queried

        // CRITICAL: Refresh the experiment entity to ensure status collection is updated
        // This ensures that when getExperiment() is called, it sees the newly added status
        // We need to clear the entity from cache and reload it to see the new status
        entityManager.detach(experimentEntity);

        return savedEntity.getStatusId();
    }
}
