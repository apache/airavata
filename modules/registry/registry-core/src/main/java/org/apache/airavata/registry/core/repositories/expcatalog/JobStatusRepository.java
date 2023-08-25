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

import org.apache.airavata.model.experiment.CpuUsage;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.status.JobState;
import org.apache.airavata.model.status.JobStatus;
import org.apache.airavata.registry.core.entities.expcatalog.JobEntity;
import org.apache.airavata.registry.core.entities.expcatalog.JobPK;
import org.apache.airavata.registry.core.entities.expcatalog.JobStatusEntity;
import org.apache.airavata.registry.core.entities.expcatalog.JobStatusPK;
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

public class JobStatusRepository extends ExpCatAbstractRepository<JobStatus, JobStatusEntity, JobStatusPK> {
    private final static Logger logger = LoggerFactory.getLogger(JobStatusRepository.class);

    public JobStatusRepository() { super(JobStatus.class, JobStatusEntity.class); }

    protected String saveJobStatus(JobStatus jobStatus, JobPK jobPK) throws RegistryException {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        JobStatusEntity jobStatusEntity = mapper.map(jobStatus, JobStatusEntity.class);

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

        if (jobStatus.getStatusId() == null) {
            logger.debug("Setting the JobStatusEntity's StatusId");
            jobStatus.setStatusId(ExpCatalogUtils.getID("JOB_STATE"));
        }

        return saveJobStatus(jobStatus, jobPK);

    }

    public String updateJobStatus(JobStatus updatedJobStatus, JobPK jobPK) throws RegistryException {
        return saveJobStatus(updatedJobStatus, jobPK);
    }

    public JobStatus getJobStatus(JobPK jobPK) throws RegistryException {
        JobRepository jobRepository = new JobRepository();
        JobModel jobModel = jobRepository.getJob(jobPK);
        List<JobStatus> jobStatusList = jobModel.getJobStatuses();

        if(jobStatusList.size() == 0) {
            logger.debug("JobStatus list is empty");
            return null;
        }

        else {
            JobStatus latestJobStatus = jobStatusList.get(0);

            for(int i = 1; i < jobStatusList.size(); i++) {
                Timestamp timeOfStateChange = new Timestamp(jobStatusList.get(i).getTimeOfStateChange());

                if (timeOfStateChange.after(new Timestamp(latestJobStatus.getTimeOfStateChange()))
                        || (timeOfStateChange.equals(latestJobStatus.getTimeOfStateChange()) && jobStatusList.get(i).getJobState().equals(JobState.COMPLETE.toString()))
                        || (timeOfStateChange.equals(latestJobStatus.getTimeOfStateChange()) && jobStatusList.get(i).getJobState().equals(JobState.FAILED.toString()))
                        || (timeOfStateChange.equals(latestJobStatus.getTimeOfStateChange()) && jobStatusList.get(i).getJobState().equals(JobState.CANCELED.toString()))) {
                    latestJobStatus = jobStatusList.get(i);
                }

            }

            return latestJobStatus;
        }
    }


    public List<JobStatus> getDistinctListofJobStatus(String gatewayId,String status, double time){
        JobStatusRepository jobStatusRepository = new JobStatusRepository();
        return  jobStatusRepository.selectWithNativeQuery(QueryConstants.FIND_JOB_COUNT_NATIVE_QUERY,
                gatewayId,status,String.valueOf(time));
    }

    public List<CpuUsage> getCpuUsages(String gatewayId, long fromTime, long toTime) {
        JobStatusRepository jobStatusRepository = new JobStatusRepository();
        List<CpuUsage> cpuUsages = new ArrayList<>();
        List<JobStatus> startedJobStatusList = jobStatusRepository.selectWithNativeQuery(
                QueryConstants.FIND_JOB_STATUS_STARTED_BEFORE_TIME, gatewayId, JobState.ACTIVE.name(),
                String.valueOf(toTime));
        ;
        List<JobStatus> finishedJobStatusList = jobStatusRepository.selectWithNativeQuery(
                QueryConstants.FIND_JOB_STATUS_FINISHED_AFTER_TIME, gatewayId, JobState.COMPLETE.name(),
                JobState.FAILED.name(), JobState.CANCELED.name(), String.valueOf(fromTime));
        Mapper mapper = ObjectMapperSingleton.getInstance();
        for (JobStatus startedJobStatus : startedJobStatusList) {
            for (JobStatus finishedJobStatus : finishedJobStatusList) {
                JobStatusEntity startedJobStatusEntity = mapper.map(startedJobStatus, JobStatusEntity.class);
                JobStatusEntity finishedJobStatusEntity = mapper.map(finishedJobStatus, JobStatusEntity.class);
                if (startedJobStatusEntity.getJobId() == finishedJobStatusEntity.getJobId()) {
                    String jobId = startedJobStatusEntity.getJobId();
                    List<ExperimentModel> experiments = (new ExperimentRepository())
                            .selectWithNativeQuery(QueryConstants.FIND_EXPERIMENT_WITH_JOB_ID, jobId);
                    if (!experiments.isEmpty()) {
                        ExperimentModel experiment = experiments.get(0);
                        List<Integer> res = (new ExperimentRepository().selectWithNativeQuery(
                                QueryConstants.FIND_TOTAL_CPU_COUNT_WITH_EXPERIMENT_ID, experiment.getExperimentId()));
                        if (!res.isEmpty()) {
                            CpuUsage cpuUsage = new CpuUsage();
                            cpuUsage.setExperimentId(experiment.getExperimentId());
                            cpuUsage.setExecutionId(experiment.getExecutionId());
                            cpuUsage.setUserName(experiment.getUserName());
                            Timestamp fromTimestamp = new Timestamp(fromTime);
                            Timestamp toTimestamp = new Timestamp(toTime);
                            Timestamp startTime = startedJobStatusEntity.getTimeOfStateChange().after(fromTimestamp)
                                    ? startedJobStatusEntity.getTimeOfStateChange()
                                    : fromTimestamp;
                            Timestamp finishTime = finishedJobStatusEntity.getTimeOfStateChange().before(toTimestamp)
                                    ? finishedJobStatusEntity.getTimeOfStateChange()
                                    : toTimestamp;
                            long duration = finishTime.getTime() - startTime.getTime(); // milliseconds
                            int totalCPUCount = res.get(0);
                            cpuUsage.setCpuHours(duration * totalCPUCount / (1000 * 60 * 60));
                        }
                    }
                }
            }
        }
        return cpuUsages;
    }
}
