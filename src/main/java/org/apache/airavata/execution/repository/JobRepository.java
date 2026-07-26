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
package org.apache.airavata.execution.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.db.AbstractRepository;
import org.apache.airavata.db.DBConstants;
import org.apache.airavata.db.QueryConstants;
import org.apache.airavata.models.job.JobModel;
import org.apache.airavata.execution.mapper.ExecutionMapper;
import org.apache.airavata.execution.model.JobEntity;
import org.apache.airavata.execution.model.JobPK;
import org.apache.airavata.common.AiravataUtils;
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

    protected String saveJobModelData(JobModel jobModel, JobPK jobPK) throws Exception {
        JobEntity jobEntity = saveJob(jobModel, jobPK);
        return jobEntity.getJobId();
    }

    protected JobEntity saveJob(JobModel jobModel, JobPK jobPK) throws Exception {
        if (jobModel.jobId().isEmpty() || jobModel.jobId().equals("DO_NOT_SET_AT_CLIENTS")) {
            logger.debug("Setting the Job's JobId");
            jobModel = jobModel.toBuilder().setJobId(jobPK.getJobId()).build();
        }

        if (!jobModel.jobStatuses().isEmpty()) {
            logger.debug("Populating the status ids of JobStatus objects for the Job");
            JobModel.Builder jobBuilder = jobModel.toBuilder().clearJobStatuses();
            for (org.apache.airavata.models.status.JobStatus jobStatus : jobModel.jobStatuses()) {
                if (jobStatus.statusId().isEmpty()) {
                    jobStatus = jobStatus.toBuilder()
                            .setStatusId(AiravataUtils.getId("JOB_STATE"))
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
        if (jobEntity.getJobStatuses() != null) {
            logger.debug("Populating entityType for JobStatus objects for the Job");
            jobEntity.getJobStatuses().forEach(e -> {
                e.setEntityType("JOB");
                if (e.getTimeOfStateChange() == null) {
                    e.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp());
                }
            });
        }
    }

    public String addJob(JobModel job, String processId) throws Exception {
        JobPK jobPK = new JobPK();
        jobPK.setJobId(job.jobId());
        jobPK.setTaskId(job.taskId());
        String jobId = saveJobModelData(job, jobPK);
        return jobId;
    }

    public String updateJob(JobModel job, JobPK jobPK) throws Exception {
        return saveJobModelData(job, jobPK);
    }

    public JobModel getJob(JobPK jobPK) throws Exception {
        return get(jobPK);
    }

    public List<JobModel> getJobList(String fieldName, Object value) throws Exception {
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

    public List<String> getJobIds(String fieldName, Object value) throws Exception {
        List<String> jobIds = new ArrayList<>();
        List<JobModel> jobModelList = getJobList(fieldName, value);
        for (JobModel jobModel : jobModelList) {
            jobIds.add(jobModel.jobId());
        }
        return jobIds;
    }

    public boolean isJobExist(JobPK jobPK) throws Exception {
        return isExists(jobPK);
    }

    public void removeJob(JobPK jobPK) throws Exception {
        delete(jobPK);
    }

    public void removeJob(JobModel jobModel) throws Exception {
        executeWithNativeQuery(QueryConstants.DELETE_JOB_NATIVE_QUERY, jobModel.jobId(), jobModel.taskId());
    }
}
