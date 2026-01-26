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
package org.apache.airavata.registry.repositories.appcatalog;

import java.util.List;
import org.apache.airavata.common.model.PreferenceLevel;
import org.apache.airavata.registry.entities.appcatalog.UnifiedStoragePreferenceEntity;
import org.apache.airavata.registry.entities.appcatalog.UnifiedStoragePreferenceEntityPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for unified storage preferences.
 */
@Repository
public interface UnifiedStoragePreferenceRepository
        extends JpaRepository<UnifiedStoragePreferenceEntity, UnifiedStoragePreferenceEntityPK> {

    /**
     * Find all preferences for a specific owner at a specific level.
     */
    @Query("SELECT p FROM UnifiedStoragePreferenceEntity p WHERE p.ownerId = :ownerId AND p.level = :level")
    List<UnifiedStoragePreferenceEntity> findByOwnerIdAndLevel(
            @Param("ownerId") String ownerId,
            @Param("level") PreferenceLevel level);

    /**
     * Find all preferences for a specific storage resource.
     */
    @Query("SELECT p FROM UnifiedStoragePreferenceEntity p WHERE p.storageResourceId = :storageResourceId")
    List<UnifiedStoragePreferenceEntity> findByStorageResourceId(
            @Param("storageResourceId") String storageResourceId);

    /**
     * Find preferences for a storage resource at a specific level.
     */
    @Query("SELECT p FROM UnifiedStoragePreferenceEntity p WHERE p.storageResourceId = :storageResourceId AND p.level = :level")
    List<UnifiedStoragePreferenceEntity> findByStorageResourceIdAndLevel(
            @Param("storageResourceId") String storageResourceId,
            @Param("level") PreferenceLevel level);

    /**
     * Delete all preferences for a specific owner.
     */
    void deleteByOwnerId(String ownerId);

    /**
     * Delete all preferences for a specific owner at a specific level.
     */
    void deleteByOwnerIdAndLevel(String ownerId, PreferenceLevel level);
}
