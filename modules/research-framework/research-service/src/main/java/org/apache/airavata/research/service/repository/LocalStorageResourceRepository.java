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
package org.apache.airavata.research.service.repository;

import java.util.List;
import org.apache.airavata.research.service.entity.LocalStorageResourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Local JPA Repository for airavata-api StorageResourceEntity
 * Used for local development data instead of external registry services
 */
@Repository
public interface LocalStorageResourceRepository extends JpaRepository<LocalStorageResourceEntity, String> {

    // Find enabled storage resources
    List<LocalStorageResourceEntity> findByEnabled(boolean enabled);

    // Search by hostname
    List<LocalStorageResourceEntity> findByHostNameContainingIgnoreCase(String hostName);

    // Search by description
    List<LocalStorageResourceEntity> findByStorageResourceDescriptionContainingIgnoreCase(String description);

    // Combined search functionality
    @Query("SELECT s FROM LocalStorageResourceEntity s WHERE " +
           "s.enabled = true AND (" +
           "LOWER(s.hostName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.storageResourceDescription) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<LocalStorageResourceEntity> searchEnabledStorageResources(@Param("keyword") String keyword);

    // Get all enabled storage resources (for public API)
    @Query("SELECT s FROM LocalStorageResourceEntity s WHERE s.enabled = true ORDER BY s.creationTime DESC")
    List<LocalStorageResourceEntity> findAllEnabledOrderByCreationTime();
}