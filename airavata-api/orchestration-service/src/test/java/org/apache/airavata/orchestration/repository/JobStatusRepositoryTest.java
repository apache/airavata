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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.airavata.iam.repository.GatewayRepository;
import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.model.experiment.proto.ExperimentModel;
import org.apache.airavata.model.experiment.proto.ExperimentType;
import org.apache.airavata.model.experiment.proto.UserConfigurationDataModel;
import org.apache.airavata.model.job.proto.JobModel;
import org.apache.airavata.model.process.proto.ProcessModel;
import org.apache.airavata.model.status.proto.JobState;
import org.apache.airavata.model.status.proto.JobStatus;
import org.apache.airavata.model.task.proto.TaskModel;
import org.apache.airavata.model.task.proto.TaskTypes;
import org.apache.airavata.model.workspace.proto.Gateway;
import org.apache.airavata.model.workspace.proto.Project;
import org.apache.airavata.orchestration.model.JobPK;
import org.apache.airavata.research.util.ExperimentTestHelper;
import org.apache.airavata.research.util.ProjectTestHelper;
import org.apache.airavata.util.TestBase;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobStatusRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(JobStatusRepositoryTest.class);

    GatewayRepository gatewayRepository;
    ProjectTestHelper projectRepository;
    ExperimentTestHelper experimentRepository;
    ProcessRepository processRepository;
    TaskRepository taskRepository;
    JobRepository jobRepository;
    JobStatusRepository jobStatusRepository;

    public JobStatusRepositoryTest() {
        super();
        gatewayRepository = new GatewayRepository();
        projectRepository = new ProjectTestHelper();
        experimentRepository = new ExperimentTestHelper();
        processRepository = new ProcessRepository();
        taskRepository = new TaskRepository();
        jobRepository = new JobRepository();
        jobStatusRepository = new JobStatusRepository();
    }

    @Test
    public void JobStatusRepositoryTest() throws RegistryException {
        Gateway gateway = Gateway.newBuilder().setGatewayId("gateway").build();
        gateway = gateway.toBuilder().setDomain("SEAGRID").build();
        gateway = gateway.toBuilder().setEmailAddress("abc@d.com").build();
        String gatewayId = gatewayRepository.addGateway(gateway);

        Project project = Project.newBuilder().setName("projectName").build();
        project = project.toBuilder().setOwner("user").build();
        project = project.toBuilder().setGatewayId(gatewayId).build();

        String projectId = projectRepository.addProject(project, gatewayId);

        ExperimentModel experimentModel =
                ExperimentModel.newBuilder().setProjectId(projectId).build();
        experimentModel = experimentModel.toBuilder().setGatewayId(gatewayId).build();
        experimentModel = experimentModel.toBuilder()
                .setExperimentType(ExperimentType.SINGLE_APPLICATION)
                .build();
        experimentModel = experimentModel.toBuilder().setUserName("user").build();
        experimentModel = experimentModel.toBuilder().setExperimentName("name").build();
        experimentModel = experimentModel.toBuilder()
                .setUserConfigurationData(UserConfigurationDataModel.getDefaultInstance())
                .build();

        String experimentId = experimentRepository.addExperiment(experimentModel);

        ProcessModel processModel =
                ProcessModel.newBuilder().setExperimentId(experimentId).build();
        String processId = processRepository.addProcess(processModel, experimentId);

        TaskModel taskModel =
                TaskModel.newBuilder().setTaskType(TaskTypes.JOB_SUBMISSION).build();
        taskModel = taskModel.toBuilder()
                .setLastUpdateTime(System.currentTimeMillis())
                .build();
        taskModel = taskModel.toBuilder().setParentProcessId(processId).build();

        String taskId = taskRepository.addTask(taskModel, processId);
        assertTrue(taskId != null);

        taskModel = taskModel.toBuilder().setTaskType(TaskTypes.MONITORING).build();
        taskModel = taskModel.toBuilder()
                .setLastUpdateTime(System.currentTimeMillis())
                .build();
        taskRepository.updateTask(taskModel, taskId);

        JobPK jobPK = new JobPK();
        jobPK.setJobId("job");
        jobPK.setTaskId(taskId);

        JobModel jobModel = JobModel.newBuilder().setJobId(jobPK.getJobId()).build();
        jobModel = jobModel.toBuilder().setTaskId(jobPK.getTaskId()).build();
        jobModel = jobModel.toBuilder().setJobDescription("jobDescription").build();

        String jobId = jobRepository.addJob(jobModel, processId);
        assertTrue(jobId != null);

        JobStatus jobStatus =
                JobStatus.newBuilder().setJobState(JobState.QUEUED).build();
        String jobStatusId = jobStatusRepository.addJobStatus(jobStatus, jobPK);
        assertTrue(jobStatusId != null);
        assertTrue(jobRepository.getJob(jobPK).getJobStatusesList().size() == 1);

        jobStatus = jobStatus.toBuilder().setJobState(JobState.ACTIVE).build();
        jobStatusRepository.updateJobStatus(jobStatus, jobPK);

        JobStatus retrievedJobStatus = jobStatusRepository.getJobStatus(jobPK);
        assertEquals(JobState.ACTIVE, retrievedJobStatus.getJobState());

        experimentRepository.removeExperiment(experimentId);
        processRepository.removeProcess(processId);
        taskRepository.removeTask(taskId);
        jobRepository.removeJob(jobPK);
        gatewayRepository.removeGateway(gatewayId);
        projectRepository.removeProject(projectId);
    }
}
