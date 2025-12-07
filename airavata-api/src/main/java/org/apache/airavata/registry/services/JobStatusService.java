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
package org.apache.airavata.registry.services;

import com.github.dozermapper.core.Mapper;
import java.util.List;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.model.status.JobStatus;
import org.apache.airavata.registry.entities.expcatalog.JobPK;
import org.apache.airavata.registry.entities.expcatalog.JobStatusEntity;
import org.apache.airavata.registry.exceptions.RegistryException;
import org.apache.airavata.registry.repositories.expcatalog.JobStatusRepository;
import org.apache.airavata.registry.utils.ExpCatalogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class JobStatusService {
    @Autowired
    private JobStatusRepository jobStatusRepository;

    @Autowired
    private Mapper mapper;

    public JobStatus getJobStatus(JobPK jobPK) throws RegistryException {
        List<JobStatusEntity> entities = jobStatusRepository.findByJobIdAndTaskIdOrderByTimeOfStateChangeDesc(
                jobPK.getJobId(), jobPK.getTaskId());
        if (entities.isEmpty()) return null;
        return mapper.map(entities.get(0), JobStatus.class);
    }

    public void addJobStatus(JobStatus jobStatus, JobPK jobPK) throws RegistryException {
        if (jobStatus.getStatusId() == null) {
            jobStatus.setStatusId(ExpCatalogUtils.getID("JOB_STATE"));
        }
        jobStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
        JobStatusEntity entity = mapper.map(jobStatus, JobStatusEntity.class);
        entity.setJobId(jobPK.getJobId());
        entity.setTaskId(jobPK.getTaskId());
        jobStatusRepository.save(entity);
    }

    public void updateJobStatus(JobStatus jobStatus, JobPK jobPK) throws RegistryException {
        if (jobStatus.getStatusId() == null) {
            jobStatus.setStatusId(ExpCatalogUtils.getID("JOB_STATE"));
        }
        if (jobStatus.getTimeOfStateChange() == 0) {
            jobStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
        }
        JobStatusEntity entity = mapper.map(jobStatus, JobStatusEntity.class);
        entity.setJobId(jobPK.getJobId());
        entity.setTaskId(jobPK.getTaskId());
        jobStatusRepository.save(entity);
    }

    public List<String> getDistinctListofJobStatus(String gatewayId, String state, double minutes)
            throws RegistryException {
        return jobStatusRepository.findDistinctJobIdsByGatewayIdAndStateAndTime(gatewayId, state, minutes);
    }
}
