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
package org.apache.airavata.execution.process;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link ProcessEntity}.
 *
 * <p>Provides query methods for process records, which represent the execution attempts
 * of an experiment on a compute resource. An experiment may have one or more processes,
 * each corresponding to a distinct job submission cycle.
 */
@Repository
public interface ProcessRepository extends JpaRepository<ProcessEntity, String> {

    /**
     * Find all processes belonging to a specific experiment.
     *
     * @param experimentId the experiment identifier
     * @return list of processes for the experiment, empty list if none found
     */
    List<ProcessEntity> findByExperimentId(String experimentId);

    /**
     * Find processes whose latest STATUS event matches the given state.
     *
     * <p>Uses a correlated subquery to pick the maximum sequence_num per process,
     * then filters by the desired state value.
     *
     * @param state the process state name (e.g. "QUEUED", "REQUEUED")
     * @return list of processes in that state
     */
    @Query("SELECT p FROM ProcessEntity p WHERE p.processId IN ("
            + "SELECT e.parentId FROM EventEntity e "
            + "WHERE e.parentType = org.apache.airavata.status.model.ParentType.PROCESS "
            + "AND e.eventKind = org.apache.airavata.status.model.EventKind.STATUS "
            + "AND e.state = :state "
            + "AND e.sequenceNum = ("
            + "  SELECT MAX(e2.sequenceNum) FROM EventEntity e2 "
            + "  WHERE e2.parentId = e.parentId "
            + "  AND e2.parentType = 'PROCESS' "
            + "  AND e2.eventKind = org.apache.airavata.status.model.EventKind.STATUS"
            + ")"
            + ")")
    List<ProcessEntity> findByLatestState(@Param("state") String state);
}
