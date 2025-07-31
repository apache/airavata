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
import java.util.Optional;
import org.apache.airavata.research.service.v2.entity.ComputeResource;
import org.apache.airavata.research.service.enums.PrivacyEnum;
import org.apache.airavata.research.service.enums.StateEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ComputeResourceRepository extends JpaRepository<ComputeResource, String> {
    
    // Find by name containing (case insensitive)
    List<ComputeResource> findByNameContainingIgnoreCaseAndPrivacyAndState(String name, PrivacyEnum privacy, StateEnum state);
    
    // Find by compute type
    List<ComputeResource> findByComputeTypeAndPrivacyAndState(String computeType, PrivacyEnum privacy, StateEnum state);
    
    // Find all public and active resources with pagination
    Page<ComputeResource> findByPrivacyAndState(PrivacyEnum privacy, StateEnum state, Pageable pageable);
    
    // Search by name with pagination
    @Query("SELECT c FROM ComputeResource c WHERE c.privacy = :privacy AND c.state = :state AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :nameSearch, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :nameSearch, '%')) OR " +
           "LOWER(c.computeType) LIKE LOWER(CONCAT('%', :nameSearch, '%')))")
    Page<ComputeResource> findByNameSearchAndPrivacyAndState(@Param("nameSearch") String nameSearch, 
                                                             @Param("privacy") PrivacyEnum privacy, 
                                                             @Param("state") StateEnum state, 
                                                             Pageable pageable);
    
    // Find all public and active resources
    List<ComputeResource> findAllByPrivacyAndState(PrivacyEnum privacy, StateEnum state);
    
    // Find by ID with eager fetching of queues only
    @Query("SELECT DISTINCT c FROM ComputeResource c " +
           "LEFT JOIN FETCH c.queues " +
           "WHERE c.id = :id")
    Optional<ComputeResource> findByIdWithCollections(@Param("id") String id);
}