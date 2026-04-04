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
package org.apache.airavata.orchestration.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.db.AbstractRepository;
import org.apache.airavata.db.DBConstants;
import org.apache.airavata.db.QueryConstants;
import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.model.job.proto.JobModel;
import org.apache.airavata.orchestration.mapper.ExecutionMapper;
import org.apache.airavata.orchestration.model.JobEntity;
import org.apache.airavata.orchestration.model.JobPK;
import org.apache.airavata.util.AiravataUtils;
import org.apache.airavata.util.ExpCatalogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class JobRepository extends AbstractRepository<JobModel, JobEntity, JobPK> {
    private static final Logger logger = LoggerFactory.getLogger(JobRepository.class);

    public JobRepository() {
        super(JobModel.class, JobEntity.class);
    }

    @Override
    protected JobModel toModel(JobEntity entity) {
        return ExecutionMapper.INSTANCE.jobToModel(entity);
    }

    @Override
    protected JobEntity toEntity(JobModel model) {
        return ExecutionMapper.INSTANCE.jobToEntity(model);
    }

    protected String saveJobModelData(JobModel jobModel, JobPK jobPK) throws RegistryException {
        JobEntity jobEntity = saveJob(jobModel, jobPK);
        return jobEntity.getJobId();
    }

    protected JobEntity saveJob(JobModel jobModel, JobPK jobPK) throws RegistryException {
        if (jobModel.getJobId().isEmpty() || jobModel.getJobId().equals("DO_NOT_SET_AT_CLIENTS")) {
            logger.debug("Setting the Job's JobId");
            jobModel = jobModel.toBuilder().setJobId(jobPK.getJobId()).build();
        }

        if (!jobModel.getJobStatusesList().isEmpty()) {
            logger.debug("Populating the status ids of JobStatus objects for the Job");
            JobModel.Builder jobBuilder = jobModel.toBuilder().clearJobStatuses();
            for (org.apache.airavata.model.status.proto.JobStatus jobStatus : jobModel.getJobStatusesList()) {
                if (jobStatus.getStatusId().isEmpty()) {
                    jobStatus = jobStatus.toBuilder()
                            .setStatusId(ExpCatalogUtils.getID("JOB_STATE"))
                            .build();
                }
                jobBuilder.addJobStatuses(jobStatus);
            }
            jobModel = jobBuilder.build();
        }

        if (!isJobExist(jobPK)) {
            logger.debug("Setting creation time to current time if does not exist");
            jobModel = jobModel.toBuilder()
                    .setCreationTime(System.currentTimeMillis())
                    .build();
        }
        JobEntity jobEntity = ExecutionMapper.INSTANCE.jobToEntity(jobModel);

        populateParentIds(jobEntity);

        return execute(entityManager -> entityManager.merge(jobEntity));
    }

    protected void populateParentIds(JobEntity jobEntity) {

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
        return get(jobPK);
    }

    public List<JobModel> getJobList(String fieldName, Object value) throws RegistryException {
        JobRepository jobRepository = new JobRepository();
        List<JobModel> jobModelList;

        if (fieldName.equals(DBConstants.Job.PROCESS_ID)) {
            logger.debug("Search criteria is ProcessId");
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(DBConstants.Job.PROCESS_ID, value);
            jobModelList = jobRepository.select(QueryConstants.GET_JOB_FOR_PROCESS_ID, -1, 0, queryParameters);
        } else if (fieldName.equals(DBConstants.Job.TASK_ID)) {
            logger.debug("Search criteria is TaskId");
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(DBConstants.Job.TASK_ID, value);
            jobModelList = jobRepository.select(QueryConstants.GET_JOB_FOR_TASK_ID, -1, 0, queryParameters);
        } else if (fieldName.equals(DBConstants.Job.JOB_ID)) {
            logger.debug("Search criteria is JobId");
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(DBConstants.Job.JOB_ID, value);
            jobModelList = jobRepository.select(QueryConstants.GET_JOB_FOR_JOB_ID, -1, 0, queryParameters);
        } else {
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

    public void removeJob(JobModel jobModel) throws RegistryException {
        executeWithNativeQuery(QueryConstants.DELETE_JOB_NATIVE_QUERY, jobModel.getJobId(), jobModel.getTaskId());
    }
}
