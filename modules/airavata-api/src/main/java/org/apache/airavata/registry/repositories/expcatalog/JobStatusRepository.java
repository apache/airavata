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
import org.apache.airavata.registry.entities.expcatalog.JobStatusEntity;
import org.apache.airavata.registry.entities.expcatalog.JobStatusPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JobStatusRepository extends JpaRepository<JobStatusEntity, JobStatusPK> {

    @Query(
            "SELECT j FROM JobStatusEntity j WHERE j.jobId = :jobId AND j.taskId = :taskId ORDER BY j.timeOfStateChange DESC")
    List<JobStatusEntity> findByJobIdAndTaskIdOrderByTimeOfStateChangeDesc(
            @Param("jobId") String jobId, @Param("taskId") String taskId);

    @Query(
            value = "SELECT DISTINCT JS.JOB_ID FROM JOB_STATUS JS WHERE JS.JOB_ID IN "
                    + "(SELECT J.JOB_ID FROM JOB J where J.PROCESS_ID IN "
                    + "(SELECT P.PROCESS_ID FROM PROCESS P  where P.EXPERIMENT_ID IN "
                    + "(SELECT E.EXPERIMENT_ID FROM EXPERIMENT E where E.GATEWAY_ID= ?1))) "
                    + "AND JS.STATE = ?2 and JS.TIME_OF_STATE_CHANGE > now() - interval ?3 minute",
            nativeQuery = true)
    List<String> findDistinctJobIdsByGatewayIdAndStateAndTime(
            @Param("gatewayId") String gatewayId, @Param("state") String state, @Param("minutes") double minutes);
}
