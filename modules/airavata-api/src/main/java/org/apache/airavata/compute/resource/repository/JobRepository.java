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
package org.apache.airavata.compute.resource.repository;

import java.util.List;
import org.apache.airavata.compute.resource.entity.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link JobEntity}.
 *
 * <p>Provides query methods for batch job records submitted through Airavata processes.
 * A process may spawn one or more jobs; each job tracks its own lifecycle state,
 * standard output/error, and exit code.
 */
@Repository("jobRepository")
public interface JobRepository extends JpaRepository<JobEntity, String> {

    /**
     * Find all jobs submitted under a specific process.
     *
     * @param processId the process identifier
     * @return list of jobs for the process, empty list if none found
     */
    List<JobEntity> findByProcessId(String processId);
}
