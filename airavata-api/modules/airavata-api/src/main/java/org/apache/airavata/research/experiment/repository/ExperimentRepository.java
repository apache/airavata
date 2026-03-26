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
package org.apache.airavata.research.experiment.repository;

import java.util.List;
import org.apache.airavata.research.experiment.entity.ExperimentEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link ExperimentEntity}.
 *
 * <p>Provides query methods for experiments scoped to a gateway. Experiments reference
 * their gateway indirectly through the {@code gatewayId} column added directly to the
 * EXPERIMENT table, allowing efficient filtering without joining to the PROJECT table.
 *
 * <p>The {@link #findByGatewayIdOrderByCreatedAtDesc(String)} query uses explicit JPQL
 * to express the ORDER BY clause, which cannot be derived from the method name alone when
 * combined with a single-field predicate in Spring Data JPA method naming.
 */
@Repository
public interface ExperimentRepository extends JpaRepository<ExperimentEntity, String> {

    /**
     * Find all experiments belonging to a specific gateway.
     *
     * @param gatewayId the gateway identifier
     * @return list of experiments for the gateway, empty list if none found
     */
    List<ExperimentEntity> findByGatewayId(String gatewayId);

    /**
     * Find all experiments for a gateway ordered by creation time descending (most recent first).
     *
     * <p>Explicit JPQL is used to attach the ORDER BY clause alongside the WHERE predicate.
     *
     * @param gatewayId the gateway identifier
     * @return list of experiments ordered by {@code createdAt} descending
     */
    @Query("SELECT e FROM ExperimentEntity e WHERE e.gatewayId = :gatewayId ORDER BY e.createdAt DESC")
    List<ExperimentEntity> findByGatewayIdOrderByCreatedAtDesc(@Param("gatewayId") String gatewayId);

    List<ExperimentEntity> findByGatewayIdAndUserNameOrderByCreatedAtDesc(
            String gatewayId, String userName, Pageable pageable);

    List<ExperimentEntity> findByProjectIdOrderByCreatedAtDesc(String projectId, Pageable pageable);
}
