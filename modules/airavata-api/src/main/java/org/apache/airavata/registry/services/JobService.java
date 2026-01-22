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

import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.common.model.AiravataCommonsConstants;
import org.apache.airavata.common.model.JobModel;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.registry.entities.expcatalog.JobEntity;
import org.apache.airavata.registry.entities.expcatalog.JobPK;
import org.apache.airavata.registry.entities.expcatalog.JobStatusEntity;
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
@Transactional
public class JobService {
    private static final Logger logger = LoggerFactory.getLogger(JobService.class);

    private final JobRepository jobRepository;
    private final JobModelMapper jobModelMapper;
    private final EntityManager entityManager;

    public JobService(JobRepository jobRepository, JobModelMapper jobModelMapper, EntityManager entityManager) {
        this.jobRepository = jobRepository;
        this.jobModelMapper = jobModelMapper;
        this.entityManager = entityManager;
    }

    public void populateParentIds(JobEntity jobEntity) {
        var jobId = jobEntity.getJobId();
        var taskId = jobEntity.getTaskId();
        if (jobEntity.getJobStatuses() != null) {
            logger.debug("Populating the Primary Key of JobStatus objects for the Job");
            jobEntity.getJobStatuses().forEach(jobStatusEntity -> {
                jobStatusEntity.setJobId(jobId);
                jobStatusEntity.setTaskId(taskId);
                jobStatusEntity.setTimeOfStateChange(AiravataUtils.getUniqueTimestamp());
            });
        }
    }

    public String addJob(JobModel job, String processId) throws RegistryException {
        var jobPK = new JobPK();
        jobPK.setJobId(job.getJobId());
        jobPK.setTaskId(job.getTaskId());
        var jobId = saveJobModelData(job, jobPK);
        return jobId;
    }

    public String updateJob(JobModel job, JobPK jobPK) throws RegistryException {
        return saveJobModelData(job, jobPK);
    }

    public JobModel getJob(JobPK jobPK) throws RegistryException {
        var entity = jobRepository.findById(jobPK).orElse(null);
        if (entity == null) return null;
        // Refresh entity to get latest data from database, including any statuses
        // added via JobStatusService in the same transaction
        entityManager.refresh(entity);
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
        var jobIds = new ArrayList<String>();
        var jobModelList = getJobList(fieldName, value);
        for (var jobModel : jobModelList) {
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
        var jobEntity = saveJob(jobModel, jobPK);
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

        long currentTime = AiravataUtils.getUniqueTimestamp().getTime();

        // Use entityManager.find() to get managed entity for proper update
        var existingEntity = entityManager.find(JobEntity.class, jobPK);

        if (existingEntity == null) {
            // New job - create entity from model
            logger.debug("Creating new job");
            jobModel.setCreationTime(currentTime);
            var jobEntity = jobModelMapper.toEntity(jobModel);
            if (jobEntity.getCreationTime() == null) {
                jobEntity.setCreationTime(AiravataUtils.getTime(currentTime));
            }
            populateParentIds(jobEntity);
            return jobRepository.save(jobEntity);
        } else {
            // Existing job - update fields on the existing entity
            logger.debug("Updating existing job");

            // Update fields from model
            existingEntity.setJobDescription(jobModel.getJobDescription());
            existingEntity.setJobName(jobModel.getJobName());
            existingEntity.setWorkingDir(jobModel.getWorkingDir());
            existingEntity.setStdOut(jobModel.getStdOut());
            existingEntity.setStdErr(jobModel.getStdErr());
            existingEntity.setExitCode(jobModel.getExitCode());
            existingEntity.setComputeResourceConsumed(jobModel.getComputeResourceConsumed());

            // Update job statuses if provided
            if (jobModel.getJobStatuses() != null && !jobModel.getJobStatuses().isEmpty()) {
                if (existingEntity.getJobStatuses() == null) {
                    existingEntity.setJobStatuses(new ArrayList<>());
                }
                // Add new statuses (don't replace existing ones)
                for (var statusModel : jobModel.getJobStatuses()) {
                    var exists = existingEntity.getJobStatuses().stream()
                            .anyMatch(s ->
                                    s.getStatusId() != null && s.getStatusId().equals(statusModel.getStatusId()));
                    if (!exists) {
                        var statusEntity = new JobStatusEntity();
                        statusEntity.setStatusId(statusModel.getStatusId());
                        statusEntity.setJobState(statusModel.getJobState());
                        statusEntity.setReason(statusModel.getReason());
                        if (statusModel.getTimeOfStateChange() > 0) {
                            statusEntity.setTimeOfStateChange(
                                    AiravataUtils.getTime(statusModel.getTimeOfStateChange()));
                        }
                        statusEntity.setJobId(jobPK.getJobId());
                        statusEntity.setTaskId(jobPK.getTaskId());
                        existingEntity.getJobStatuses().add(statusEntity);
                    }
                }
            }

            populateParentIds(existingEntity);
            // Flush to ensure changes are persisted
            entityManager.flush();
            return existingEntity;
        }
    }
}
