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
package org.apache.airavata.compute.resource.service;

import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.compute.resource.model.JobModel;
import org.apache.airavata.compute.resource.entity.JobEntity;
import org.apache.airavata.core.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.compute.resource.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for CRUD operations on job records.
 *
 * <p>A job represents a single batch or fork submission on a compute resource,
 * owned by a process. Callers query by jobId or processId.
 */
@Service
@Transactional
public class DefaultJobService implements JobService {

    private static final Logger logger = LoggerFactory.getLogger(JobService.class);

    private final JobRepository jobRepository;

    public DefaultJobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    private JobModel toModel(JobEntity entity) {
        var model = new JobModel();
        model.setJobId(entity.getJobId());
        model.setProcessId(entity.getProcessId());
        model.setJobDescription(entity.getJobDescription());
        model.setCreatedAt(entity.getCreatedAt());
        model.setJobStatuses(entity.getJobStatuses());
        model.setComputeResourceConsumed(entity.getComputeResourceConsumed());
        model.setJobName(entity.getJobName());
        model.setWorkingDir(entity.getWorkingDir());
        model.setStdOut(entity.getStdOut());
        model.setStdErr(entity.getStdErr());
        model.setExitCode(entity.getExitCode());
        return model;
    }

    private JobEntity toEntity(JobModel model) {
        var entity = new JobEntity();
        entity.setJobId(model.getJobId());
        entity.setProcessId(model.getProcessId());
        entity.setJobDescription(model.getJobDescription());
        entity.setCreatedAt(model.getCreatedAt());
        entity.setComputeResourceConsumed(model.getComputeResourceConsumed());
        entity.setJobName(model.getJobName());
        entity.setWorkingDir(model.getWorkingDir());
        entity.setStdOut(model.getStdOut());
        entity.setStdErr(model.getStdErr());
        entity.setExitCode(model.getExitCode());
        return entity;
    }

    /**
     * Query jobs by a dynamic field name. Supports "jobId" and "processId".
     *
     * @param fieldName the field to filter by
     * @param fieldValue the expected value
     * @return list of matching job models
     * @throws RegistryException on retrieval failure
     */
    @Transactional(readOnly = true)
    public List<JobModel> getJobs(String fieldName, String fieldValue) throws RegistryException {
        try {
            if (fieldValue == null || fieldValue.isBlank()) {
                return new ArrayList<>();
            }
            List<JobEntity> entities;
            switch (fieldName) {
                case "jobId":
                    entities = jobRepository.findById(fieldValue).map(List::of).orElse(List.of());
                    break;
                case "processId":
                    entities = jobRepository.findByProcessId(fieldValue);
                    break;
                default:
                    logger.warn("Unsupported job query field: {}", fieldName);
                    return new ArrayList<>();
            }
            return entities.stream().map(this::toModel).toList();
        } catch (Exception e) {
            throw new RegistryException("Failed to query jobs by " + fieldName + "=" + fieldValue, e);
        }
    }

    /**
     * Save or update a job record.
     *
     * @param jobModel the job to persist
     * @throws RegistryException on persistence failure
     */
    public void saveJob(JobModel jobModel) throws RegistryException {
        try {
            JobEntity entity = toEntity(jobModel);
            jobRepository.save(entity);
            logger.debug("Saved job {}", jobModel.getJobId());
        } catch (Exception e) {
            throw new RegistryException("Failed to save job " + jobModel.getJobId(), e);
        }
    }

    /**
     * Delete all jobs for a given process.
     *
     * @param processId the process identifier
     * @throws RegistryException on deletion failure
     */
    public void deleteJobsByProcessId(String processId) throws RegistryException {
        try {
            List<JobEntity> jobs = jobRepository.findByProcessId(processId);
            jobRepository.deleteAll(jobs);
            logger.debug("Deleted {} jobs for process {}", jobs.size(), processId);
        } catch (Exception e) {
            throw new RegistryException("Failed to delete jobs for process " + processId, e);
        }
    }
}
