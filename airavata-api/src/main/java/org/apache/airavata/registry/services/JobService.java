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
import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.model.commons.airavata_commonsConstants;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.registry.entities.expcatalog.JobEntity;
import org.apache.airavata.registry.entities.expcatalog.JobPK;
import org.apache.airavata.registry.exceptions.RegistryException;
import org.apache.airavata.registry.repositories.expcatalog.JobRepository;
import org.apache.airavata.registry.utils.ExpCatalogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class JobService {
    private static final Logger logger = LoggerFactory.getLogger(JobService.class);

    private final JobRepository jobRepository;
    private final Mapper mapper;

    public JobService(JobRepository jobRepository, Mapper mapper) {
        this.jobRepository = jobRepository;
        this.mapper = mapper;
    }

    public void populateParentIds(JobEntity jobEntity) {
        String jobId = jobEntity.getJobId();
        String taskId = jobEntity.getTaskId();
        if (jobEntity.getJobStatuses() != null) {
            logger.debug("Populating the Primary Key of JobStatus objects for the Job");
            jobEntity.getJobStatuses().forEach(jobStatusEntity -> {
                jobStatusEntity.setJobId(jobId);
                jobStatusEntity.setTaskId(taskId);
                jobStatusEntity.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp());
            });
        }
    }

    public String addJob(JobModel job, String processId) throws RegistryException {
        JobPK jobPK = new JobPK();
        jobPK.setJobId(job.getJobId());
        jobPK.setTaskId(job.getTaskId());
        String jobId = saveJobModelData(job, jobPK);
        return jobId;
    }

    public String updateJob(JobModel job, JobPK jobPK) throws RegistryException {
        return saveJobModelData(job, jobPK);
    }

    public JobModel getJob(JobPK jobPK) throws RegistryException {
        JobEntity entity = jobRepository.findById(jobPK).orElse(null);
        if (entity == null) return null;
        return mapper.map(entity, JobModel.class);
    }

    public List<JobModel> getJobList(String fieldName, Object value) throws RegistryException {
        List<JobEntity> entities;
        if (fieldName.equals("PROCESS_ID")) {
            entities = jobRepository.findByProcessId((String) value);
        } else if (fieldName.equals("TASK_ID")) {
            entities = jobRepository.findByTaskId((String) value);
        } else if (fieldName.equals("JOB_ID")) {
            entities = jobRepository.findByJobId((String) value);
        } else {
            logger.error("Unsupported field name for Job module.");
            throw new IllegalArgumentException("Unsupported field name for Job module.");
        }
        List<JobModel> jobModelList = new ArrayList<>();
        entities.forEach(e -> jobModelList.add(mapper.map(e, JobModel.class)));
        return jobModelList;
    }

    public List<String> getJobIds(String fieldName, Object value) throws RegistryException {
        List<String> jobIds = new ArrayList<>();
        List<JobModel> jobModelList = getJobList(fieldName, value);
        for (JobModel jobModel : jobModelList) {
            jobIds.add(jobModel.getJobId());
        }
        return jobIds;
    }

    public boolean isJobExist(JobPK jobPK) throws RegistryException {
        return jobRepository.existsById(jobPK);
    }

    public void removeJob(JobPK jobPK) throws RegistryException {
        jobRepository.deleteById(jobPK);
    }

    public void removeJob(JobModel jobModel) throws RegistryException {
        jobRepository.deleteByJobIdAndTaskId(jobModel.getJobId(), jobModel.getTaskId());
    }

    private String saveJobModelData(JobModel jobModel, JobPK jobPK) throws RegistryException {
        JobEntity jobEntity = saveJob(jobModel, jobPK);
        return jobEntity.getJobId();
    }

    private JobEntity saveJob(JobModel jobModel, JobPK jobPK) throws RegistryException {
        if (jobModel.getJobId() == null || jobModel.getJobId().equals(airavata_commonsConstants.DEFAULT_ID)) {
            logger.debug("Setting the Job's JobId");
            jobModel.setJobId(jobPK.getJobId());
        }

        if (jobModel.getJobStatuses() != null) {
            logger.debug("Populating the status ids of JobStatus objects for the Job");
            jobModel.getJobStatuses().forEach(jobStatus -> {
                if (jobStatus.getStatusId() == null) {
                    jobStatus.setStatusId(ExpCatalogUtils.getID("JOB_STATE"));
                }
            });
        }

        if (!isJobExist(jobPK)) {
            logger.debug("Setting creation time to current time if does not exist");
            jobModel.setCreationTime(System.currentTimeMillis());
        }

        JobEntity jobEntity = mapper.map(jobModel, JobEntity.class);

        populateParentIds(jobEntity);

        return jobRepository.save(jobEntity);
    }
}
