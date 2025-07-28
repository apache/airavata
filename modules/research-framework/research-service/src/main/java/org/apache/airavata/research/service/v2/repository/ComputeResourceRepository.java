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
package org.apache.airavata.research.service.v2.repository;

import java.util.List;
import org.apache.airavata.research.service.v2.entity.ComputeResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ComputeResourceRepository extends JpaRepository<ComputeResource, String> {
    
    // Find by name containing (case insensitive)
    List<ComputeResource> findByNameContainingIgnoreCaseAndIsPublicTrue(String name);
    
    // Find by compute type
    List<ComputeResource> findByComputeTypeAndIsPublicTrue(String computeType);
    
    // Find all public and active resources with pagination
    Page<ComputeResource> findByIsPublicTrueAndIsActiveTrue(Pageable pageable);
    
    // Search by name with pagination
    @Query("SELECT c FROM ComputeResource c WHERE c.isPublic = true AND c.isActive = true AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :nameSearch, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :nameSearch, '%')) OR " +
           "LOWER(c.computeType) LIKE LOWER(CONCAT('%', :nameSearch, '%')))")
    Page<ComputeResource> findByNameSearchAndIsPublicTrueAndIsActiveTrue(@Param("nameSearch") String nameSearch, Pageable pageable);
    
    // Find all public and active resources
    List<ComputeResource> findAllByIsPublicTrueAndIsActiveTrue();
}