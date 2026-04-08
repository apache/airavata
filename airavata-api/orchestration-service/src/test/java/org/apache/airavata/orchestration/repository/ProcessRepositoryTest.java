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

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.apache.airavata.db.DBConstants;
import org.apache.airavata.iam.repository.GatewayRepository;
import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.model.experiment.proto.ExperimentModel;
import org.apache.airavata.model.experiment.proto.ExperimentType;
import org.apache.airavata.model.experiment.proto.UserConfigurationDataModel;
import org.apache.airavata.model.job.proto.JobModel;
import org.apache.airavata.model.process.proto.ProcessModel;
import org.apache.airavata.model.scheduling.proto.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.proto.*;
import org.apache.airavata.model.task.proto.TaskModel;
import org.apache.airavata.model.task.proto.TaskTypes;
import org.apache.airavata.model.workspace.proto.Gateway;
import org.apache.airavata.model.workspace.proto.Project;
import org.apache.airavata.research.util.ExperimentTestHelper;
import org.apache.airavata.research.util.ProjectTestHelper;
import org.apache.airavata.util.TestBase;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(ProcessRepositoryTest.class);

    GatewayRepository gatewayRepository;
    ProjectTestHelper projectRepository;
    ExperimentTestHelper experimentRepository;
    ProcessRepository processRepository;

    public ProcessRepositoryTest() {
        super();
        gatewayRepository = new GatewayRepository();
        projectRepository = new ProjectTestHelper();
        experimentRepository = new ExperimentTestHelper();
        processRepository = new ProcessRepository();
    }

    @Test
    public void ProcessRepositoryTest() throws RegistryException {
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

        TaskModel task = TaskModel.newBuilder().setTaskId("task-id").build();
        task = task.toBuilder().setTaskType(TaskTypes.ENV_SETUP).build();
        task = task.toBuilder().setLastUpdateTime(System.currentTimeMillis()).build();
        processModel = processModel.toBuilder().addTasks(task).build();

        TaskStatus taskStatus = TaskStatus.newBuilder()
                .setState(TaskState.TASK_STATE_CREATED)
                .setStatusId("task-status-id")
                .build();
        task = task.toBuilder().addTaskStatuses(taskStatus).build();

        String processId = processRepository.addProcess(processModel, experimentId);
        assertTrue(processId != null);
        assertTrue(experimentRepository
                        .getExperiment(experimentId)
                        .getProcessesList()
                        .size()
                == 1);

        processModel = processModel.toBuilder().setProcessDetail("detail").build();
        processModel = processModel.toBuilder().setUseUserCrPref(true).build();
        processModel =
                processModel.toBuilder().addEmailAddresses("notify@example.com").build();
        processModel = processModel.toBuilder()
                .addEmailAddresses("notify1@example.com")
                .build();

        TaskModel jobSubmissionTask =
                TaskModel.newBuilder().setTaskType(TaskTypes.JOB_SUBMISSION).build();
        jobSubmissionTask = jobSubmissionTask.toBuilder()
                .setLastUpdateTime(System.currentTimeMillis())
                .build();
        jobSubmissionTask =
                jobSubmissionTask.toBuilder().setTaskId("job-task-id").build();
        JobModel job = JobModel.newBuilder().setProcessId(processId).build();
        job = job.toBuilder().setJobId("job-id").build();
        job = job.toBuilder().setJobDescription("job-description").build();
        JobStatus jobStatus = JobStatus.newBuilder()
                .setJobState(JobState.SUBMITTED)
                .setStatusId("submitted-job-status-id")
                .build();
        job = job.toBuilder().addJobStatuses(jobStatus).build();
        jobSubmissionTask = jobSubmissionTask.toBuilder().addJobs(job).build();
        processModel = processModel.toBuilder().addTasks(jobSubmissionTask).build();
        processRepository.updateProcess(processModel, processId);

        ProcessModel retrievedProcess = processRepository.getProcess(processId);
        assertEquals(experimentId, retrievedProcess.getExperimentId());
        assertEquals("detail", retrievedProcess.getProcessDetail());
        assertTrue(retrievedProcess.getUseUserCrPref());
        assertEquals(1, retrievedProcess.getProcessStatusesCount(), "Added process should automatically have 1 status");
        assertEquals(
                ProcessState.PROCESS_STATE_CREATED,
                retrievedProcess.getProcessStatusesList().get(0).getState(),
                "Added process should automatically have 1 status that is CREATED");
        assertEquals(2, retrievedProcess.getTasksCount());
        assertEquals(2, retrievedProcess.getEmailAddressesCount());
        assertEquals(
                "notify@example.com", retrievedProcess.getEmailAddressesList().get(0));
        assertEquals(
                "notify1@example.com", retrievedProcess.getEmailAddressesList().get(1));

        ComputationalResourceSchedulingModel computationalResourceSchedulingModel =
                ComputationalResourceSchedulingModel.getDefaultInstance();
        assertEquals(
                processId,
                processRepository.addProcessResourceSchedule(computationalResourceSchedulingModel, processId));

        computationalResourceSchedulingModel = computationalResourceSchedulingModel.toBuilder()
                .setQueueName("queue")
                .build();
        computationalResourceSchedulingModel = computationalResourceSchedulingModel.toBuilder()
                .setStaticWorkingDir("staticWorkingDir")
                .build();
        computationalResourceSchedulingModel = computationalResourceSchedulingModel.toBuilder()
                .setOverrideAllocationProjectNumber("overrideAllocationProjectNumber")
                .build();
        computationalResourceSchedulingModel = computationalResourceSchedulingModel.toBuilder()
                .setOverrideLoginUserName("overrideLoginUserName")
                .build();
        computationalResourceSchedulingModel = computationalResourceSchedulingModel.toBuilder()
                .setOverrideScratchLocation("overrideScratchLocation")
                .build();
        processRepository.updateProcessResourceSchedule(computationalResourceSchedulingModel, processId);
        ComputationalResourceSchedulingModel updatedComputationalResourceSchedulingModel =
                processRepository.getProcessResourceSchedule(processId);
        assertEquals("queue", updatedComputationalResourceSchedulingModel.getQueueName());
        assertEquals("staticWorkingDir", updatedComputationalResourceSchedulingModel.getStaticWorkingDir());
        assertEquals(
                "overrideAllocationProjectNumber",
                updatedComputationalResourceSchedulingModel.getOverrideAllocationProjectNumber());
        assertEquals("overrideLoginUserName", updatedComputationalResourceSchedulingModel.getOverrideLoginUserName());
        assertEquals(
                "overrideScratchLocation", updatedComputationalResourceSchedulingModel.getOverrideScratchLocation());

        List<String> processIdList = processRepository.getProcessIds(DBConstants.Process.EXPERIMENT_ID, experimentId);
        assertTrue(processIdList.size() == 1);
        assertTrue(processIdList.get(0).equals(processId));

        experimentRepository.removeExperiment(experimentId);
        processRepository.removeProcess(processId);
        assertFalse(processRepository.isProcessExist(processId));

        gatewayRepository.removeGateway(gatewayId);
        projectRepository.removeProject(projectId);
    }
}
