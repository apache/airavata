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
package org.apache.airavata.registry.repositories.expcatalog;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
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
import org.apache.airavata.registry.exceptions.RegistryException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.ProcessService;
import org.apache.airavata.registry.services.ProjectService;
import org.apache.airavata.registry.utils.DBConstants;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
        classes = {org.apache.airavata.config.JpaConfig.class, ProcessRepositoryTest.TestConfiguration.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
        })
@TestPropertySource(locations = "classpath:airavata.properties")
public class ProcessRepositoryTest extends TestBase {

    @Configuration
    @ComponentScan(
            basePackages = {"org.apache.airavata.service", "org.apache.airavata.registry", "org.apache.airavata.config"},
            excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = {
                            org.apache.airavata.config.BackgroundServicesLauncher.class,
                            org.apache.airavata.config.ThriftServerLauncher.class,
                            org.apache.airavata.monitor.realtime.RealtimeMonitor.class,
                            org.apache.airavata.monitor.email.EmailBasedMonitor.class,
                            org.apache.airavata.monitor.cluster.ClusterStatusMonitorJob.class,
                            org.apache.airavata.monitor.AbstractMonitor.class,
                            org.apache.airavata.helix.impl.controller.HelixController.class,
                            org.apache.airavata.helix.impl.participant.GlobalParticipant.class,
                            org.apache.airavata.helix.impl.workflow.PreWorkflowManager.class,
                            org.apache.airavata.helix.impl.workflow.PostWorkflowManager.class,
                            org.apache.airavata.helix.impl.workflow.ParserWorkflowManager.class
                        }),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "org\\.apache\\.airavata\\.monitor\\..*"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "org\\.apache\\.airavata\\.helix\\..*")
            })
    @EnableConfigurationProperties(org.apache.airavata.config.AiravataServerProperties.class)
    @Import(org.apache.airavata.config.AiravataPropertiesConfiguration.class)
    static class TestConfiguration {}

    private final GatewayService gatewayService;
    private final ProjectService projectService;
    private final ExperimentService experimentService;
    private final ProcessService processService;

    public ProcessRepositoryTest(
            GatewayService gatewayService,
            ProjectService projectService,
            ExperimentService experimentService,
            ProcessService processService) {
        super(Database.EXP_CATALOG);
        this.gatewayService = gatewayService;
        this.projectService = projectService;
        this.experimentService = experimentService;
        this.processService = processService;
    }

    @Test
    public void testProcessRepository() throws RegistryException {
        Gateway gateway = new Gateway();
        gateway.setGatewayId("gateway");
        gateway.setDomain("SEAGRID");
        gateway.setEmailAddress("abc@d.com");
        String gatewayId = gatewayService.addGateway(gateway);

        Project project = new Project();
        project.setName("projectName");
        project.setOwner("user");
        project.setGatewayId(gatewayId);

        String projectId = projectService.addProject(project, gatewayId);

        ExperimentModel experimentModel = new ExperimentModel();
        experimentModel.setProjectId(projectId);
        experimentModel.setGatewayId(gatewayId);
        experimentModel.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        experimentModel.setUserName("user");
        experimentModel.setExperimentName("name");

        String experimentId = experimentService.addExperiment(experimentModel);

        ProcessModel processModel = new ProcessModel(null, experimentId);

        TaskModel task = new TaskModel();
        task.setTaskId("task-id");
        task.setTaskType(TaskTypes.ENV_SETUP);
        processModel.addToTasks(task);

        TaskStatus taskStatus = new TaskStatus(TaskState.CREATED);
        taskStatus.setStatusId("task-status-id");
        task.addToTaskStatuses(taskStatus);

        String processId = processService.addProcess(processModel, experimentId);
        assertTrue(processId != null);
        assertTrue(experimentService.getExperiment(experimentId).getProcesses().size() == 1);

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
        processService.updateProcess(processModel, processId);

        ProcessModel retrievedProcess = processService.getProcess(processId);
        assertEquals(experimentId, retrievedProcess.getExperimentId());
        assertEquals("detail", retrievedProcess.getProcessDetail());
        assertTrue(retrievedProcess.isUseUserCRPref());
        assertEquals(1, retrievedProcess.getProcessStatusesSize(), "Added process should automatically have 1 status");
        assertEquals(
                ProcessState.CREATED,
                retrievedProcess.getProcessStatuses().get(0).getState(),
                "Added process should automatically have 1 status that is CREATED");
        assertEquals(2, retrievedProcess.getTasksSize());
        assertEquals(2, retrievedProcess.getEmailAddressesSize());
        assertEquals("notify@example.com", retrievedProcess.getEmailAddresses().get(0));
        assertEquals("notify1@example.com", retrievedProcess.getEmailAddresses().get(1));

        ComputationalResourceSchedulingModel computationalResourceSchedulingModel =
                new ComputationalResourceSchedulingModel();
        assertEquals(
                processId, processService.addProcessResourceSchedule(computationalResourceSchedulingModel, processId));

        computationalResourceSchedulingModel.setQueueName("queue");
        computationalResourceSchedulingModel.setStaticWorkingDir("staticWorkingDir");
        computationalResourceSchedulingModel.setOverrideAllocationProjectNumber("overrideAllocationProjectNumber");
        computationalResourceSchedulingModel.setOverrideLoginUserName("overrideLoginUserName");
        computationalResourceSchedulingModel.setOverrideScratchLocation("overrideScratchLocation");
        processService.updateProcessResourceSchedule(computationalResourceSchedulingModel, processId);
        ComputationalResourceSchedulingModel updatedComputationalResourceSchedulingModel =
                processService.getProcessResourceSchedule(processId);
        assertEquals("queue", updatedComputationalResourceSchedulingModel.getQueueName());
        assertEquals("staticWorkingDir", updatedComputationalResourceSchedulingModel.getStaticWorkingDir());
        assertEquals(
                "overrideAllocationProjectNumber",
                updatedComputationalResourceSchedulingModel.getOverrideAllocationProjectNumber());
        assertEquals("overrideLoginUserName", updatedComputationalResourceSchedulingModel.getOverrideLoginUserName());
        assertEquals(
                "overrideScratchLocation", updatedComputationalResourceSchedulingModel.getOverrideScratchLocation());

        List<String> processIdList = processService.getProcessIds(DBConstants.Process.EXPERIMENT_ID, experimentId);
        assertTrue(processIdList.size() == 1);
        assertTrue(processIdList.get(0).equals(processId));

        experimentService.removeExperiment(experimentId);
        processService.removeProcess(processId);
        assertFalse(processService.isProcessExist(processId));

        gatewayService.removeGateway(gatewayId);
        projectService.removeProject(projectId);
    }
}
