/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/
package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.commons.airavata_commonsConstants;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.status.JobStatus;
import org.apache.airavata.registry.core.entities.expcatalog.JobEntity;
import org.apache.airavata.registry.core.entities.expcatalog.JobPK;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.core.utils.ExpCatalogUtils;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.core.utils.QueryConstants;
import org.apache.airavata.registry.cpi.RegistryException;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobRepository extends ExpCatAbstractRepository<JobModel, JobEntity, JobPK> {
    private final static Logger logger = LoggerFactory.getLogger(JobRepository.class);

    public JobRepository() { super(JobModel.class, JobEntity.class); }

    protected String saveJobModelData(JobModel jobModel, JobPK jobPK) throws RegistryException {
        JobEntity jobEntity = saveJob(jobModel, jobPK);
        return jobEntity.getJobId();
    }

    protected JobEntity saveJob(JobModel jobModel, JobPK jobPK) throws RegistryException {
        if (jobModel.getJobId() == null || jobModel.getJobId().equals(airavata_commonsConstants.DEFAULT_ID)) {
            logger.debug("Setting the Job's JobId");
            jobModel.setJobId(jobPK.getJobId());
        }

        String jobId = jobPK.getJobId();
        String taskId = jobPK.getTaskId();
        Mapper mapper = ObjectMapperSingleton.getInstance();
        JobEntity jobEntity = mapper.map(jobModel, JobEntity.class);

        if (jobEntity.getJobStatuses() != null) {
            logger.debug("Populating the Primary Key of JobStatus objects for the Job");
            jobEntity.getJobStatuses().forEach(jobStatusEntity -> {
                jobStatusEntity.setJobId(jobId);
                jobStatusEntity.setTaskId(taskId);
            });
        }

        if (!isJobExist(jobPK)) {
            logger.debug("Checking if the Job already exists");
            jobEntity.setCreationTime(new Timestamp((System.currentTimeMillis())));
        }

        return execute(entityManager -> entityManager.merge(jobEntity));
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
        return get(jobPK);
    }

    public String addJobStatus(JobStatus jobStatus, JobPK jobPK) throws RegistryException {
        JobModel jobModel = getJob(jobPK);
        List<JobStatus> jobStatusList = jobModel.getJobStatuses();

        if (jobStatusList.size() == 0 || !jobStatusList.contains(jobStatus)) {

            if (jobStatus.getStatusId() == null) {
                logger.debug("Set JobStatus's StatusId");
                jobStatus.setStatusId(ExpCatalogUtils.getID("STATUS"));
            }

            logger.debug("Adding the JobStatus to the list");
            jobStatusList.add(jobStatus);

        }

        jobModel.setJobStatuses(jobStatusList);
        updateJob(jobModel, jobPK);
        return jobStatus.getStatusId();
    }

    public String updateJobStatus(JobStatus updatedJobStatus, JobPK jobPK) throws RegistryException {
        JobModel jobModel = getJob(jobPK);
        List<JobStatus> jobStatusList = jobModel.getJobStatuses();

        for (JobStatus retrievedJobStatus : jobStatusList) {

            if (retrievedJobStatus.getStatusId().equals(updatedJobStatus.getStatusId())) {
                logger.debug("Updating the JobStatus");
                jobStatusList.remove(retrievedJobStatus);
                jobStatusList.add(updatedJobStatus);
            }

        }

        jobModel.setJobStatuses(jobStatusList);
        updateJob(jobModel, jobPK);
        return updatedJobStatus.getStatusId();

    }

    public List<JobStatus> getJobStatus(JobPK jobPK) throws RegistryException {
        JobModel jobModel = getJob(jobPK);
        return jobModel.getJobStatuses();
    }

    public List<JobModel> getJobList(String fieldName, Object value) throws RegistryException {
        JobRepository jobRepository = new JobRepository();
        List<JobModel> jobModelList;

        if (fieldName.equals(DBConstants.Job.PROCESS_ID)) {
            logger.debug("Search criteria is ProcessId");
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(DBConstants.Job.PROCESS_ID, value);
            jobModelList = jobRepository.select(QueryConstants.GET_JOB_FOR_PROCESS_ID, -1, 0, queryParameters);
        }

        else if (fieldName.equals(DBConstants.Job.TASK_ID)) {
            logger.debug("Search criteria is TaskId");
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(DBConstants.Job.TASK_ID, value);
            jobModelList = jobRepository.select(QueryConstants.GET_JOB_FOR_TASK_ID, -1, 0, queryParameters);
        }

        else {
            logger.error("Unsupported field name for Job module.");
            throw new IllegalArgumentException("Unsupported field name for Job module.");
        }

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
        return isExists(jobPK);
    }

    public void removeJob(JobPK jobPK) throws RegistryException {
        delete(jobPK);
    }

}
