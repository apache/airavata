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

import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.common.model.AiravataCommonsConstants;
import org.apache.airavata.common.model.JobModel;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.registry.entities.expcatalog.JobEntity;
import org.apache.airavata.registry.entities.expcatalog.JobPK;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.mappers.JobModelMapper;
import org.apache.airavata.registry.repositories.expcatalog.JobRepository;
import org.apache.airavata.registry.utils.DBConstants;
import org.apache.airavata.registry.utils.ExpCatalogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional("expCatalogTransactionManager")
public class JobService {
    private static final Logger logger = LoggerFactory.getLogger(JobService.class);

    private final JobRepository jobRepository;
    private final JobModelMapper jobModelMapper;

    public JobService(JobRepository jobRepository, JobModelMapper jobModelMapper) {
        this.jobRepository = jobRepository;
        this.jobModelMapper = jobModelMapper;
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
        return jobModelMapper.toModel(entity);
    }

    public List<JobModel> getJobList(String fieldName, Object value) throws RegistryException {
        List<JobEntity> entities;
        if (fieldName.equals("PROCESS_ID") || fieldName.equals(DBConstants.Job.PROCESS_ID)) {
            entities = jobRepository.findByProcessId((String) value);
        } else if (fieldName.equals("TASK_ID") || fieldName.equals(DBConstants.Job.TASK_ID)) {
            entities = jobRepository.findByTaskId((String) value);
        } else if (fieldName.equals("JOB_ID") || fieldName.equals(DBConstants.Job.JOB_ID)) {
            entities = jobRepository.findByJobId((String) value);
        } else {
            logger.error("Unsupported field name for Job module.");
            throw new IllegalArgumentException("Unsupported field name for Job module.");
        }
        return jobModelMapper.toModelList(entities);
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
        if (jobModel.getJobId() == null || jobModel.getJobId().equals(AiravataCommonsConstants.DEFAULT_ID)) {
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

        long currentTime = System.currentTimeMillis();
        // Always ensure creationTime is set on the model before mapping
        if (!isJobExist(jobPK)) {
            logger.debug("Setting creation time to current time if does not exist");
            jobModel.setCreationTime(currentTime);
        } else {
            // For updates, preserve existing creation time if set, otherwise use current time
            if (jobModel.getCreationTime() <= 0) {
                // Try to get existing job to preserve its creation time
                JobModel existingJob = getJob(jobPK);
                if (existingJob != null && existingJob.getCreationTime() > 0) {
                    jobModel.setCreationTime(existingJob.getCreationTime());
                } else {
                    jobModel.setCreationTime(currentTime);
                }
            }
        }

        JobEntity jobEntity = jobModelMapper.toEntity(jobModel);

        // Ensure CREATION_TIME is set if mapper didn't set it (mapper returns null if model time is 0 or negative)
        if (jobEntity.getCreationTime() == null) {
            long creationTime = (jobModel.getCreationTime() > 0) ? jobModel.getCreationTime() : currentTime;
            jobEntity.setCreationTime(new java.sql.Timestamp(creationTime));
        }

        populateParentIds(jobEntity);

        return jobRepository.save(jobEntity);
    }
}
