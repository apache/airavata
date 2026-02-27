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
package org.apache.airavata.research.project.repository;

import java.util.List;
import org.apache.airavata.research.project.entity.ProjectEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link ProjectEntity}.
 *
 * <p>Provides query methods for gateway projects, which serve as grouping constructs for
 * experiments. Method naming conventions are used for simple lookups; explicit JPQL is
 * avoided where derivation is straightforward.
 */
@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, String> {

    /**
     * Find all projects belonging to a specific gateway.
     *
     * @param gatewayId the gateway identifier
     * @return list of projects for the gateway, empty list if none found
     */
    List<ProjectEntity> findByGatewayId(String gatewayId);

    List<ProjectEntity> findByGatewayIdOrderByCreatedAtDesc(String gatewayId, Pageable pageable);
}
