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
import org.apache.airavata.research.service.entity.LocalComputeResourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Local JPA Repository for airavata-api LocalComputeResourceEntity
 * Used for local development data instead of external registry services
 */
@Repository
public interface LocalComputeResourceRepository extends JpaRepository<LocalComputeResourceEntity, String> {

    // Find enabled compute resources
    List<LocalComputeResourceEntity> findByEnabled(short enabled);

    // Search by hostname
    List<LocalComputeResourceEntity> findByHostNameContainingIgnoreCase(String hostName);

    // Search by description
    List<LocalComputeResourceEntity> findByResourceDescriptionContainingIgnoreCase(String description);

    // Combined search functionality
    @Query("SELECT c FROM LocalComputeResourceEntity c WHERE " +
           "c.enabled = 1 AND (" +
           "LOWER(c.hostName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.resourceDescription) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<LocalComputeResourceEntity> searchEnabledComputeResources(@Param("keyword") String keyword);

    // Get all enabled compute resources (for public API)
    @Query("SELECT c FROM LocalComputeResourceEntity c WHERE c.enabled = 1 ORDER BY c.creationTime DESC")
    List<LocalComputeResourceEntity> findAllEnabledOrderByCreationTime();
}