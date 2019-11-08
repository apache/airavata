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

import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentType;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.*;
import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.model.task.TaskTypes;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.core.repositories.common.TestBase;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.cpi.RegistryException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.*;

public class ProcessRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(ProcessRepositoryTest.class);

    GatewayRepository gatewayRepository;
    ProjectRepository projectRepository;
    ExperimentRepository experimentRepository;
    ProcessRepository processRepository;

    public ProcessRepositoryTest() {
        super(Database.EXP_CATALOG);
        gatewayRepository = new GatewayRepository();
        projectRepository = new ProjectRepository();
        experimentRepository = new ExperimentRepository();
        processRepository = new ProcessRepository();
    }

    @Test
    public void ProcessRepositoryTest() throws RegistryException {
        Gateway gateway = new Gateway();
        gateway.setGatewayId("gateway");
        gateway.setDomain("SEAGRID");
        gateway.setEmailAddress("abc@d.com");
        String gatewayId = gatewayRepository.addGateway(gateway);

        Project project = new Project();
        project.setName("projectName");
        project.setOwner("user");
        project.setGatewayId(gatewayId);

        String projectId = projectRepository.addProject(project, gatewayId);

        ExperimentModel experimentModel = new ExperimentModel();
        experimentModel.setProjectId(projectId);
        experimentModel.setGatewayId(gatewayId);
        experimentModel.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        experimentModel.setUserName("user");
        experimentModel.setExperimentName("name");

        String experimentId = experimentRepository.addExperiment(experimentModel);

        ProcessModel processModel = new ProcessModel(null, experimentId);

        TaskModel task = new TaskModel();
        task.setTaskId("task-id");
        task.setTaskType(TaskTypes.ENV_SETUP);
        processModel.addToTasks(task);

        TaskStatus taskStatus = new TaskStatus(TaskState.CREATED);
        taskStatus.setStatusId("task-status-id");
        task.addToTaskStatuses(taskStatus);

        String processId = processRepository.addProcess(processModel, experimentId);
        assertTrue(processId != null);
        assertTrue(experimentRepository.getExperiment(experimentId).getProcesses().size() == 1);

        processModel.setProcessDetail("detail");
        processModel.setUseUserCRPref(true);
        processModel.addToEmailAddresses("notify@example.com");
        processModel.addToEmailAddresses("notify1@example.com");

        TaskModel jobSubmissionTask = new TaskModel();
        jobSubmissionTask.setTaskType(TaskTypes.JOB_SUBMISSION);
        jobSubmissionTask.setTaskId("job-task-id");
        JobModel job = new JobModel();
        job.setProcessId(processId);
        job.setJobId("job-id");
        job.setJobDescription("job-description");
        JobStatus jobStatus = new JobStatus(JobState.SUBMITTED);
        jobStatus.setStatusId("submitted-job-status-id");
        job.addToJobStatuses(jobStatus);
        jobSubmissionTask.addToJobs(job);
        processModel.addToTasks(jobSubmissionTask);
        processRepository.updateProcess(processModel, processId);

        ProcessModel retrievedProcess = processRepository.getProcess(processId);
        assertEquals(experimentId, retrievedProcess.getExperimentId());
        assertEquals("detail", retrievedProcess.getProcessDetail());
        assertTrue(retrievedProcess.isUseUserCRPref());
        assertEquals("Added process should automatically have 1 status", 1, retrievedProcess.getProcessStatusesSize());
        assertEquals("Added process should automatically have 1 status that is CREATED", ProcessState.CREATED, retrievedProcess.getProcessStatuses().get(0).getState());
        assertEquals(2, retrievedProcess.getTasksSize());
        assertEquals(2, retrievedProcess.getEmailAddressesSize());
        assertEquals("notify@example.com", retrievedProcess.getEmailAddresses().get(0));
        assertEquals("notify1@example.com", retrievedProcess.getEmailAddresses().get(1));

        ComputationalResourceSchedulingModel computationalResourceSchedulingModel = new ComputationalResourceSchedulingModel();
        assertEquals(processId, processRepository.addProcessResourceSchedule(computationalResourceSchedulingModel, processId));

        computationalResourceSchedulingModel.setQueueName("queue");
        computationalResourceSchedulingModel.setStaticWorkingDir("staticWorkingDir");
        computationalResourceSchedulingModel.setOverrideAllocationProjectNumber("overrideAllocationProjectNumber");
        computationalResourceSchedulingModel.setOverrideLoginUserName("overrideLoginUserName");
        computationalResourceSchedulingModel.setOverrideScratchLocation("overrideScratchLocation");
        processRepository.updateProcessResourceSchedule(computationalResourceSchedulingModel, processId);
        ComputationalResourceSchedulingModel updatedComputationalResourceSchedulingModel = processRepository.getProcessResourceSchedule(processId);
        assertEquals("queue", updatedComputationalResourceSchedulingModel.getQueueName());
        assertEquals("staticWorkingDir", updatedComputationalResourceSchedulingModel.getStaticWorkingDir());
        assertEquals("overrideAllocationProjectNumber", updatedComputationalResourceSchedulingModel.getOverrideAllocationProjectNumber());
        assertEquals("overrideLoginUserName", updatedComputationalResourceSchedulingModel.getOverrideLoginUserName());
        assertEquals("overrideScratchLocation", updatedComputationalResourceSchedulingModel.getOverrideScratchLocation());

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
