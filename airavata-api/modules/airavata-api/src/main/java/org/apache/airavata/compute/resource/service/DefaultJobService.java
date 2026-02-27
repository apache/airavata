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
import org.apache.airavata.compute.resource.entity.JobEntity;
import org.apache.airavata.compute.resource.mapper.JobMapper;
import org.apache.airavata.compute.resource.model.Job;
import org.apache.airavata.compute.resource.repository.JobRepository;
import org.apache.airavata.core.exception.RegistryExceptions.RegistryException;
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

    private static final Logger logger = LoggerFactory.getLogger(DefaultJobService.class);

    private final JobRepository jobRepository;
    private final JobMapper mapper;

    public DefaultJobService(JobRepository jobRepository, JobMapper mapper) {
        this.jobRepository = jobRepository;
        this.mapper = mapper;
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
    public List<Job> getJobs(String fieldName, String fieldValue) throws RegistryException {
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
            return mapper.toModelList(entities);
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
    public void saveJob(Job jobModel) throws RegistryException {
        try {
            var entity = mapper.toEntity(jobModel);
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
