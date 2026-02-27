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
package org.apache.airavata.accounting.repository;

import java.util.List;
import java.util.Optional;
import org.apache.airavata.accounting.entity.AllocationProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link AllocationProjectEntity}.
 *
 * <p>Provides query methods for HPC allocation projects (charge accounts) that grant
 * compute time on specific resources within a gateway. All finder methods use Spring
 * Data JPA method naming conventions for automatic query derivation.
 */
@Repository
public interface AllocationProjectRepository extends JpaRepository<AllocationProjectEntity, String> {

    /**
     * Find all allocation projects belonging to a specific gateway.
     *
     * @param gatewayId the gateway identifier
     * @return list of allocation projects for the gateway, empty list if none found
     */
    List<AllocationProjectEntity> findByGatewayId(String gatewayId);

    /**
     * Find all allocation projects associated with a specific compute resource.
     *
     * @param resourceId the resource identifier
     * @return list of allocation projects for the resource, empty list if none found
     */
    List<AllocationProjectEntity> findByResourceId(String resourceId);

    /**
     * Find an allocation project by its scheduler-level project code on a specific resource.
     *
     * <p>The combination of {@code projectCode} and {@code resourceId} uniquely identifies
     * a single allocation project (scheduler account code is unique per resource).
     *
     * @param projectCode the scheduler account or charge code (e.g. SLURM {@code --account})
     * @param resourceId  the resource identifier
     * @return Optional containing the allocation project if found
     */
    Optional<AllocationProjectEntity> findByProjectCodeAndResourceId(String projectCode, String resourceId);
}
