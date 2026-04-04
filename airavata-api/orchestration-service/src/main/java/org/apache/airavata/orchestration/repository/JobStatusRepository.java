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

import java.sql.Timestamp;
import java.util.List;
import org.apache.airavata.db.AbstractRepository;
import org.apache.airavata.db.QueryConstants;
import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.model.job.proto.JobModel;
import org.apache.airavata.model.status.proto.JobState;
import org.apache.airavata.model.status.proto.JobStatus;
import org.apache.airavata.orchestration.mapper.ExecutionMapper;
import org.apache.airavata.orchestration.model.JobPK;
import org.apache.airavata.orchestration.model.JobStatusEntity;
import org.apache.airavata.orchestration.model.JobStatusPK;
import org.apache.airavata.util.ExpCatalogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class JobStatusRepository extends AbstractRepository<JobStatus, JobStatusEntity, JobStatusPK> {
    private static final Logger logger = LoggerFactory.getLogger(JobStatusRepository.class);

    public JobStatusRepository() {
        super(JobStatus.class, JobStatusEntity.class);
    }

    @Override
    protected JobStatus toModel(JobStatusEntity entity) {
        return ExecutionMapper.INSTANCE.jobStatusToModel(entity);
    }

    @Override
    protected JobStatusEntity toEntity(JobStatus model) {
        return ExecutionMapper.INSTANCE.jobStatusToEntity(model);
    }

    protected String saveJobStatus(JobStatus jobStatus, JobPK jobPK) throws RegistryException {
        JobStatusEntity jobStatusEntity = ExecutionMapper.INSTANCE.jobStatusToEntity(jobStatus);

        if (jobStatusEntity.getJobId() == null) {
            logger.debug("Setting the JobStatusEntity's JobId");
            jobStatusEntity.setJobId(jobPK.getJobId());
        }

        if (jobStatusEntity.getTaskId() == null) {
            logger.debug("Setting the JobStatusEntity's TaskId");
            jobStatusEntity.setTaskId(jobPK.getTaskId());
        }

        execute(entityManager -> entityManager.merge(jobStatusEntity));
        return jobStatusEntity.getStatusId();
    }

    public String addJobStatus(JobStatus jobStatus, JobPK jobPK) throws RegistryException {

        if (jobStatus.getStatusId().isEmpty()) {
            logger.debug("Setting the JobStatusEntity's StatusId");
            jobStatus = jobStatus.toBuilder()
                    .setStatusId(ExpCatalogUtils.getID("JOB_STATE"))
                    .build();
        }

        return saveJobStatus(jobStatus, jobPK);
    }

    public String updateJobStatus(JobStatus updatedJobStatus, JobPK jobPK) throws RegistryException {
        return saveJobStatus(updatedJobStatus, jobPK);
    }

    public JobStatus getJobStatus(JobPK jobPK) throws RegistryException {
        JobRepository jobRepository = new JobRepository();
        JobModel jobModel = jobRepository.getJob(jobPK);
        List<JobStatus> jobStatusList = jobModel.getJobStatusesList();

        if (jobStatusList.size() == 0) {
            logger.debug("JobStatus list is empty");
            return null;
        } else {
            JobStatus latestJobStatus = jobStatusList.get(0);

            for (int i = 1; i < jobStatusList.size(); i++) {
                Timestamp timeOfStateChange = new Timestamp(jobStatusList.get(i).getTimeOfStateChange());

                if (timeOfStateChange.after(new Timestamp(latestJobStatus.getTimeOfStateChange()))
                        || (timeOfStateChange.equals(latestJobStatus.getTimeOfStateChange())
                                && jobStatusList.get(i).getJobState().equals(JobState.COMPLETE.toString()))
                        || (timeOfStateChange.equals(latestJobStatus.getTimeOfStateChange())
                                && jobStatusList.get(i).getJobState().equals(JobState.FAILED.toString()))
                        || (timeOfStateChange.equals(latestJobStatus.getTimeOfStateChange())
                                && jobStatusList.get(i).getJobState().equals(JobState.CANCELED.toString()))) {
                    latestJobStatus = jobStatusList.get(i);
                }
            }

            return latestJobStatus;
        }
    }

    public List<JobStatus> getDistinctListofJobStatus(String gatewayId, String status, double time) {
        JobStatusRepository jobStatusRepository = new JobStatusRepository();
        return jobStatusRepository.selectWithNativeQuery(
                QueryConstants.FIND_JOB_COUNT_NATIVE_QUERY, gatewayId, status, String.valueOf(time));
    }
}
