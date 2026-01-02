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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.ExperimentType;
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.common.model.JobModel;
import org.apache.airavata.common.model.JobState;
import org.apache.airavata.common.model.JobStatus;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.common.model.TaskModel;
import org.apache.airavata.common.model.TaskTypes;
import org.apache.airavata.registry.entities.expcatalog.JobPK;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.JobService;
import org.apache.airavata.registry.services.ProcessService;
import org.apache.airavata.registry.services.ProjectService;
import org.apache.airavata.registry.services.TaskService;
import org.apache.airavata.registry.utils.DBConstants;
import org.junit.jupiter.api.BeforeEach;
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
            org.apache.airavata.config.TestcontainersConfig.class,
            org.apache.airavata.config.AiravataPropertiesConfiguration.class,
            JobRepositoryTest.TestConfiguration.class
        },
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "spring.aop.proxy-target-class=true",
            "flyway.enabled=false"
            // Infrastructure components (including SecurityManagerConfig) excluded via @ComponentScan excludeFilters -
            // no property flags needed
        })
@org.springframework.test.context.ActiveProfiles("test")
@TestPropertySource(locations = "classpath:airavata.properties")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class JobRepositoryTest extends TestBase {

    @Configuration
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.registry.services",
                "org.apache.airavata.registry.mappers",
                "org.apache.airavata.registry.repositories",
                "org.apache.airavata.registry.utils",
                "org.apache.airavata.config",
                "org.apache.airavata.common.utils"
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
    private final TaskService taskService;
    private final JobService jobService;

    private String gatewayId;
    private String projectId;
    private String experimentId;
    private String processId;
    private String taskId;

    public JobRepositoryTest(
            GatewayService gatewayService,
            ProjectService projectService,
            ExperimentService experimentService,
            ProcessService processService,
            TaskService taskService,
            JobService jobService) {
        super(Database.EXP_CATALOG);
        this.gatewayService = gatewayService;
        this.projectService = projectService;
        this.experimentService = experimentService;
        this.processService = processService;
        this.taskService = taskService;
        this.jobService = jobService;
    }

    @BeforeEach
    public void setUp() throws RegistryException {
        Gateway gateway = new Gateway();
        gateway.setGatewayId("gateway-" + java.util.UUID.randomUUID().toString());
        gateway.setDomain("SEAGRID");
        gateway.setEmailAddress("test@example.com");
        gatewayId = gatewayService.addGateway(gateway);

        Project project = new Project();
        project.setName("testProject");
        project.setOwner("testUser");
        project.setGatewayId(gatewayId);
        projectId = projectService.addProject(project, gatewayId);

        ExperimentModel experimentModel = new ExperimentModel();
        experimentModel.setProjectId(projectId);
        experimentModel.setGatewayId(gatewayId);
        experimentModel.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        experimentModel.setUserName("testUser");
        experimentModel.setExperimentName("testExperiment");
        experimentId = experimentService.addExperiment(experimentModel);

        ProcessModel processModel = new ProcessModel();
        processModel.setExperimentId(experimentId);
        processId = processService.addProcess(processModel, experimentId);

        TaskModel taskModel = new TaskModel();
        taskModel.setTaskType(TaskTypes.JOB_SUBMISSION);
        taskModel.setParentProcessId(processId);
        taskId = taskService.addTask(taskModel, processId);
        assertNotNull(taskId, "Task ID should not be null");
    }

    @Test
    public void testJobRepository_CreateAndUpdate() throws RegistryException {
        // Test creating and updating jobs
        JobModel jobModel = new JobModel();
        jobModel.setJobId("job-" + java.util.UUID.randomUUID().toString());
        jobModel.setTaskId(taskId);
        jobModel.setJobDescription("Test job description");

        // Initialize jobStatuses if null
        if (jobModel.getJobStatuses() == null) {
            jobModel.setJobStatuses(new java.util.ArrayList<>());
        }
        JobStatus jobStatus = new JobStatus(JobState.SUBMITTED);
        jobModel.getJobStatuses().add(jobStatus);

        String jobId = jobService.addJob(jobModel, processId);
        assertNotNull(jobId, "Job ID should not be null");
        assertEquals(1, taskService.getTask(taskId).getJobs().size(), "Task should have one job");

        JobPK jobPK = new JobPK();
        jobPK.setJobId(jobId);
        jobPK.setTaskId(taskId);

        // Update job
        jobModel.setJobName("Updated job name");
        jobService.updateJob(jobModel, jobPK);

        final JobModel retrievedJob = jobService.getJob(jobPK);
        assertNotNull(retrievedJob, "Retrieved job should not be null");
        assertEquals("Updated job name", retrievedJob.getJobName(), "Job name should be updated");
        assertEquals(1, retrievedJob.getJobStatuses().size(), "Job should have one status");
        assertEquals(
                JobState.SUBMITTED,
                retrievedJob.getJobStatuses().get(0).getJobState(),
                "Job status should be SUBMITTED");
    }

    @Test
    public void testJobRepository_GetJobIdsByTaskId() throws RegistryException {
        // Test retrieving job IDs by task ID (important for job lookup)
        JobModel jobModel = new JobModel();
        jobModel.setJobId("job-" + java.util.UUID.randomUUID().toString());
        jobModel.setTaskId(taskId);
        jobModel.setJobDescription("Test job");

        if (jobModel.getJobStatuses() == null) {
            jobModel.setJobStatuses(new java.util.ArrayList<>());
        }
        JobStatus jobStatus = new JobStatus(JobState.SUBMITTED);
        jobModel.getJobStatuses().add(jobStatus);

        String jobId = jobService.addJob(jobModel, processId);
        assertNotNull(jobId, "Job ID should not be null");

        List<String> jobIdList = jobService.getJobIds(DBConstants.Job.TASK_ID, taskId);
        assertEquals(1, jobIdList.size(), "Should have one job ID");
        assertEquals(jobId, jobIdList.get(0), "Job ID should match");
    }

    @Test
    public void testJobRepository_JobDeletion() throws RegistryException {
        // Test that job deletion works correctly
        JobModel jobModel = new JobModel();
        jobModel.setJobId("job-" + java.util.UUID.randomUUID().toString());
        jobModel.setTaskId(taskId);
        jobModel.setJobDescription("Job to be deleted");

        if (jobModel.getJobStatuses() == null) {
            jobModel.setJobStatuses(new java.util.ArrayList<>());
        }
        JobStatus jobStatus = new JobStatus(JobState.SUBMITTED);
        jobModel.getJobStatuses().add(jobStatus);

        String jobId = jobService.addJob(jobModel, processId);
        JobPK jobPK = new JobPK();
        jobPK.setJobId(jobId);
        jobPK.setTaskId(taskId);

        // Verify job exists
        assertTrue(jobService.isJobExist(jobPK), "Job should exist before deletion");

        // Delete job
        jobService.removeJob(jobPK);

        // Verify job no longer exists
        assertFalse(jobService.isJobExist(jobPK), "Job should not exist after deletion");
    }
}
