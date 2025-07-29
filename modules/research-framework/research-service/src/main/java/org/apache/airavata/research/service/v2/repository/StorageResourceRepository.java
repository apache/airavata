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
import org.apache.airavata.research.service.v2.entity.StorageResource;
import org.apache.airavata.research.service.v2.enums.PrivacyEnumV2;
import org.apache.airavata.research.service.v2.enums.StateEnumV2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StorageResourceRepository extends JpaRepository<StorageResource, String> {
    
    // Find by name containing (case insensitive)
    List<StorageResource> findByNameContainingIgnoreCaseAndPrivacyAndState(String name, PrivacyEnumV2 privacy, StateEnumV2 state);
    
    // Find by storage type
    List<StorageResource> findByStorageTypeAndPrivacyAndState(String storageType, PrivacyEnumV2 privacy, StateEnumV2 state);
    
    // Find all public and active resources with pagination
    Page<StorageResource> findByPrivacyAndState(PrivacyEnumV2 privacy, StateEnumV2 state, Pageable pageable);
    
    // Search by name with pagination
    @Query("SELECT s FROM StorageResource s WHERE s.privacy = :privacy AND s.state = :state AND " +
           "(LOWER(s.name) LIKE LOWER(CONCAT('%', :nameSearch, '%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%', :nameSearch, '%')) OR " +
           "LOWER(s.storageType) LIKE LOWER(CONCAT('%', :nameSearch, '%')))")
    Page<StorageResource> findByNameSearchAndPrivacyAndState(@Param("nameSearch") String nameSearch, 
                                                             @Param("privacy") PrivacyEnumV2 privacy, 
                                                             @Param("state") StateEnumV2 state, 
                                                             Pageable pageable);
    
    // Find all public and active resources
    List<StorageResource> findAllByPrivacyAndState(PrivacyEnumV2 privacy, StateEnumV2 state);
}