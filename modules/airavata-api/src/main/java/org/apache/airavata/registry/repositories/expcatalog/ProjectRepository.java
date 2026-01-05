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
package org.apache.airavata.registry.repositories.expcatalog;

import java.util.List;
import org.apache.airavata.registry.entities.expcatalog.ProjectEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, String> {

    @Query("SELECT p FROM ProjectEntity p WHERE p.owner LIKE :owner")
    List<ProjectEntity> findByOwner(@Param("owner") String owner, Pageable pageable);

    @Query("SELECT p FROM ProjectEntity p WHERE p.gatewayId LIKE :gatewayId")
    List<ProjectEntity> findByGatewayId(@Param("gatewayId") String gatewayId);

    // Note: Complex search methods (searchProjects, searchAllAccessibleProjects) with dynamic queries
    // should be implemented using Spring Data JPA Specifications or in a service class
}
