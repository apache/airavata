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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.apache.airavata.common.model.ComputationalResourceSchedulingModel;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.ExperimentType;
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.common.model.JobModel;
import org.apache.airavata.common.model.JobState;
import org.apache.airavata.common.model.JobStatus;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.common.model.ProcessState;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.common.model.TaskModel;
import org.apache.airavata.common.model.TaskState;
import org.apache.airavata.common.model.TaskStatus;
import org.apache.airavata.common.model.TaskTypes;
import org.apache.airavata.registry.exception.RegistryException;
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
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
        classes = {
            org.apache.airavata.config.JpaConfig.class,
            org.apache.airavata.config.AiravataPropertiesConfiguration.class,
            ProcessRepositoryTest.TestConfiguration.class
        },
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.main.allow-circular-references=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "spring.aop.proxy-target-class=true",
            // Infrastructure components (including SecurityManagerConfig) excluded via @ComponentScan excludeFilters -
            // no property flags needed
        })
@TestPropertySource(locations = "classpath:airavata.properties")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class ProcessRepositoryTest extends TestBase {

    @Configuration
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.registry.services",
                "org.apache.airavata.registry.mappers",
                "org.apache.airavata.registry.repositories",
                "org.apache.airavata.registry.utils",
                "org.apache.airavata.config",
                "org.apache.airavata.common.utils"
            },
            useDefaultFilters = false,
            includeFilters = {
                @ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ANNOTATION,
                        classes = {
                            org.springframework.stereotype.Component.class,
                            org.springframework.stereotype.Service.class,
                            org.springframework.stereotype.Repository.class,
                            org.springframework.context.annotation.Configuration.class
                        })
            },
            excludeFilters = {
                // Exclude infrastructure components - use DI instead of property flags
                @ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.REGEX,
                        pattern = "org\\.apache\\.airavata\\.helix\\.\\.*"),
                @ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.REGEX,
                        pattern = "org\\.apache\\.airavata\\.monitor\\.\\.*"),
                @ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.REGEX,
                        pattern = "org\\.apache\\.airavata\\.manager\\.dbevent\\.\\.*"),
                @ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                        classes = {org.apache.airavata.config.BackgroundServicesLauncher.class}),
                @ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.REGEX,
                        pattern = "org\\.apache\\.airavata\\.orchestrator\\.\\.*")
            })
    @EnableConfigurationProperties(org.apache.airavata.config.AiravataServerProperties.class)
    @Import({
        org.apache.airavata.config.AiravataPropertiesConfiguration.class,
    })
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

        ProcessModel processModel = new ProcessModel();
        processModel.setExperimentId(experimentId);

        // Initialize tasks list if null
        if (processModel.getTasks() == null) {
            processModel.setTasks(new java.util.ArrayList<>());
        }

        TaskModel task = new TaskModel();
        task.setTaskId("task-id");
        task.setTaskType(TaskTypes.ENV_SETUP);
        processModel.getTasks().add(task);

        // Initialize taskStatuses if null
        if (task.getTaskStatuses() == null) {
            task.setTaskStatuses(new java.util.ArrayList<>());
        }
        TaskStatus taskStatus = new TaskStatus();
        taskStatus.setState(TaskState.CREATED);
        taskStatus.setStatusId("task-status-id");
        task.getTaskStatuses().add(taskStatus);

        String processId = processService.addProcess(processModel, experimentId);
        assertTrue(processId != null);
        assertTrue(experimentService.getExperiment(experimentId).getProcesses().size() == 1);

        processModel.setProcessDetail("detail");
        processModel.setUseUserCRPref(true);
        // Initialize emailAddresses if null
        if (processModel.getEmailAddresses() == null) {
            processModel.setEmailAddresses(new java.util.ArrayList<>());
        }
        processModel.getEmailAddresses().add("notify@example.com");
        processModel.getEmailAddresses().add("notify1@example.com");

        TaskModel jobSubmissionTask = new TaskModel();
        jobSubmissionTask.setTaskType(TaskTypes.JOB_SUBMISSION);
        jobSubmissionTask.setTaskId("job-task-id");
        JobModel job = new JobModel();
        job.setProcessId(processId);
        job.setJobId("job-id");
        job.setJobDescription("job-description");
        // Initialize jobStatuses if null
        if (job.getJobStatuses() == null) {
            job.setJobStatuses(new java.util.ArrayList<>());
        }
        JobStatus jobStatus = new JobStatus(JobState.SUBMITTED);
        jobStatus.setStatusId("submitted-job-status-id");
        job.getJobStatuses().add(jobStatus);
        // Initialize jobs if null
        if (jobSubmissionTask.getJobs() == null) {
            jobSubmissionTask.setJobs(new java.util.ArrayList<>());
        }
        jobSubmissionTask.getJobs().add(job);
        processModel.getTasks().add(jobSubmissionTask);
        processService.updateProcess(processModel, processId);

        ProcessModel retrievedProcess = processService.getProcess(processId);
        assertEquals(experimentId, retrievedProcess.getExperimentId());
        assertEquals("detail", retrievedProcess.getProcessDetail());
        assertTrue(retrievedProcess.getUseUserCRPref());
        assertEquals(
                1, retrievedProcess.getProcessStatuses().size(), "Added process should automatically have 1 status");
        assertEquals(
                ProcessState.CREATED,
                retrievedProcess.getProcessStatuses().get(0).getState(),
                "Added process should automatically have 1 status that is CREATED");
        assertEquals(2, retrievedProcess.getTasks().size());
        assertEquals(2, retrievedProcess.getEmailAddresses().size());
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
