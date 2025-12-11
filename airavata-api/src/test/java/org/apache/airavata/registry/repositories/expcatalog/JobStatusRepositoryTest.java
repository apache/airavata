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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentType;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.status.JobState;
import org.apache.airavata.model.status.JobStatus;
import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.model.task.TaskTypes;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.entities.expcatalog.JobPK;
import org.apache.airavata.registry.exceptions.RegistryException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.JobService;
import org.apache.airavata.registry.services.JobStatusService;
import org.apache.airavata.registry.services.ProcessService;
import org.apache.airavata.registry.services.ProjectService;
import org.apache.airavata.registry.services.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
        classes = {org.apache.airavata.config.JpaConfig.class, JobStatusRepositoryTest.TestConfiguration.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "services.background.enabled=false",
            "services.thrift.enabled=false",
            "services.helix.enabled=false"
        })
@TestPropertySource(locations = "classpath:airavata.properties")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class JobStatusRepositoryTest extends TestBase {

    @Configuration
    @ComponentScan(
            basePackages = {"org.apache.airavata.service", "org.apache.airavata.registry", "org.apache.airavata.config"
            },
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
    private final TaskService taskService;
    private final JobService jobService;
    private final JobStatusService jobStatusService;

    public JobStatusRepositoryTest(
            GatewayService gatewayService,
            ProjectService projectService,
            ExperimentService experimentService,
            ProcessService processService,
            TaskService taskService,
            JobService jobService,
            JobStatusService jobStatusService) {
        super(Database.EXP_CATALOG);
        this.gatewayService = gatewayService;
        this.projectService = projectService;
        this.experimentService = experimentService;
        this.processService = processService;
        this.taskService = taskService;
        this.jobService = jobService;
        this.jobStatusService = jobStatusService;
    }

    @Test
    public void testJobStatusRepository() throws RegistryException {
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
        String processId = processService.addProcess(processModel, experimentId);

        TaskModel taskModel = new TaskModel();
        taskModel.setTaskType(TaskTypes.JOB_SUBMISSION);
        taskModel.setParentProcessId(processId);

        String taskId = taskService.addTask(taskModel, processId);
        assertTrue(taskId != null);

        taskModel.setTaskType(TaskTypes.MONITORING);
        taskService.updateTask(taskModel, taskId);

        JobPK jobPK = new JobPK();
        jobPK.setJobId("job");
        jobPK.setTaskId(taskId);

        JobModel jobModel = new JobModel();
        jobModel.setJobId(jobPK.getJobId());
        jobModel.setTaskId(jobPK.getTaskId());
        jobModel.setJobDescription("jobDescription");

        String jobId = jobService.addJob(jobModel, processId);
        assertTrue(jobId != null);

        JobStatus jobStatus = new JobStatus(JobState.QUEUED);
        jobStatusService.addJobStatus(jobStatus, jobPK);
        assertTrue(jobService.getJob(jobPK).getJobStatuses().size() == 1);

        jobStatus.setJobState(JobState.ACTIVE);
        jobStatusService.updateJobStatus(jobStatus, jobPK);

        JobStatus retrievedJobStatus = jobStatusService.getJobStatus(jobPK);
        assertEquals(JobState.ACTIVE, retrievedJobStatus.getJobState());

        experimentService.removeExperiment(experimentId);
        processService.removeProcess(processId);
        taskService.removeTask(taskId);
        jobService.removeJob(jobPK);
        gatewayService.removeGateway(gatewayId);
        projectService.removeProject(projectId);
    }
}
