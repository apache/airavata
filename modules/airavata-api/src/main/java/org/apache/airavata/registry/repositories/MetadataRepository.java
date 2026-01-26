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
package org.apache.airavata.registry.repositories;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.airavata.common.model.MetadataParentType;
import org.apache.airavata.registry.entities.MetadataEntity;
import org.apache.airavata.registry.entities.MetadataEntityPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Unified repository for metadata records across all parent types.
 *
 * <p>This repository consolidates the functionality of:
 * <ul>
 *   <li>{@code DataProductMetadataEntity} (via @ElementCollection)</li>
 *   <li>{@code DataReplicaMetadataEntity} (via @ElementCollection)</li>
 * </ul>
 *
 * <p>Provides methods for CRUD operations on metadata key-value pairs associated
 * with various entity types like data products, data replicas, experiments, etc.
 */
@Repository
public interface MetadataRepository extends JpaRepository<MetadataEntity, MetadataEntityPK> {

    /**
     * Find all metadata entries for a specific parent entity.
     *
     * @param parentType the type of parent entity
     * @param parentId the parent entity ID
     * @return list of metadata entities
     */
    @Query("SELECT m FROM MetadataEntity m WHERE m.parentType = :parentType AND m.parentId = :parentId")
    List<MetadataEntity> findByParentTypeAndParentId(
            @Param("parentType") MetadataParentType parentType, @Param("parentId") String parentId);

    /**
     * Find a specific metadata entry by parent and key.
     *
     * @param parentType the type of parent entity
     * @param parentId the parent entity ID
     * @param key the metadata key
     * @return the metadata entity if found
     */
    @Query("SELECT m FROM MetadataEntity m WHERE m.parentType = :parentType AND m.parentId = :parentId AND m.key = :key")
    Optional<MetadataEntity> findByParentTypeAndParentIdAndKey(
            @Param("parentType") MetadataParentType parentType,
            @Param("parentId") String parentId,
            @Param("key") String key);

    /**
     * Delete all metadata entries for a specific parent entity.
     *
     * @param parentType the type of parent entity
     * @param parentId the parent entity ID
     */
    @Modifying
    @Query("DELETE FROM MetadataEntity m WHERE m.parentType = :parentType AND m.parentId = :parentId")
    void deleteByParentTypeAndParentId(
            @Param("parentType") MetadataParentType parentType, @Param("parentId") String parentId);

    /**
     * Delete a specific metadata entry.
     *
     * @param parentType the type of parent entity
     * @param parentId the parent entity ID
     * @param key the metadata key
     */
    @Modifying
    @Query("DELETE FROM MetadataEntity m WHERE m.parentType = :parentType AND m.parentId = :parentId AND m.key = :key")
    void deleteByParentTypeAndParentIdAndKey(
            @Param("parentType") MetadataParentType parentType,
            @Param("parentId") String parentId,
            @Param("key") String key);

    /**
     * Count metadata entries for a specific parent entity.
     *
     * @param parentType the type of parent entity
     * @param parentId the parent entity ID
     * @return count of metadata entries
     */
    @Query("SELECT COUNT(m) FROM MetadataEntity m WHERE m.parentType = :parentType AND m.parentId = :parentId")
    long countByParentTypeAndParentId(
            @Param("parentType") MetadataParentType parentType, @Param("parentId") String parentId);

    /**
     * Find all metadata entries with a specific key across all parents of a given type.
     *
     * @param parentType the type of parent entity
     * @param key the metadata key
     * @return list of metadata entities
     */
    @Query("SELECT m FROM MetadataEntity m WHERE m.parentType = :parentType AND m.key = :key")
    List<MetadataEntity> findByParentTypeAndKey(
            @Param("parentType") MetadataParentType parentType, @Param("key") String key);

    /**
     * Find all metadata for a data product.
     *
     * @param productUri the data product URI
     * @return list of metadata entities
     */
    default List<MetadataEntity> findByDataProductUri(String productUri) {
        return findByParentTypeAndParentId(MetadataParentType.DATA_PRODUCT, productUri);
    }

    /**
     * Find all metadata for a data replica.
     *
     * @param replicaId the data replica ID
     * @return list of metadata entities
     */
    default List<MetadataEntity> findByDataReplicaId(String replicaId) {
        return findByParentTypeAndParentId(MetadataParentType.DATA_REPLICA, replicaId);
    }

    /**
     * Find all metadata for an experiment.
     *
     * @param experimentId the experiment ID
     * @return list of metadata entities
     */
    default List<MetadataEntity> findByExperimentId(String experimentId) {
        return findByParentTypeAndParentId(MetadataParentType.EXPERIMENT, experimentId);
    }

    /**
     * Find all metadata for a process.
     *
     * @param processId the process ID
     * @return list of metadata entities
     */
    default List<MetadataEntity> findByProcessId(String processId) {
        return findByParentTypeAndParentId(MetadataParentType.PROCESS, processId);
    }

    /**
     * Delete all metadata for a data product.
     *
     * @param productUri the data product URI
     */
    default void deleteByDataProductUri(String productUri) {
        deleteByParentTypeAndParentId(MetadataParentType.DATA_PRODUCT, productUri);
    }

    /**
     * Delete all metadata for a data replica.
     *
     * @param replicaId the data replica ID
     */
    default void deleteByDataReplicaId(String replicaId) {
        deleteByParentTypeAndParentId(MetadataParentType.DATA_REPLICA, replicaId);
    }

    /**
     * Get metadata as a Map for a specific parent.
     *
     * @param parentType the type of parent entity
     * @param parentId the parent entity ID
     * @return map of key-value pairs
     */
    default Map<String, String> getMetadataMap(MetadataParentType parentType, String parentId) {
        return findByParentTypeAndParentId(parentType, parentId).stream()
                .collect(Collectors.toMap(MetadataEntity::getKey, MetadataEntity::getValue));
    }

    /**
     * Get data product metadata as a Map.
     *
     * @param productUri the data product URI
     * @return map of key-value pairs
     */
    default Map<String, String> getDataProductMetadataMap(String productUri) {
        return getMetadataMap(MetadataParentType.DATA_PRODUCT, productUri);
    }

    /**
     * Get data replica metadata as a Map.
     *
     * @param replicaId the data replica ID
     * @return map of key-value pairs
     */
    default Map<String, String> getDataReplicaMetadataMap(String replicaId) {
        return getMetadataMap(MetadataParentType.DATA_REPLICA, replicaId);
    }

    /**
     * Check if a metadata key exists for a parent.
     *
     * @param parentType the type of parent entity
     * @param parentId the parent entity ID
     * @param key the metadata key
     * @return true if the key exists
     */
    default boolean existsByParentTypeAndParentIdAndKey(MetadataParentType parentType, String parentId, String key) {
        return findByParentTypeAndParentIdAndKey(parentType, parentId, key).isPresent();
    }
}
