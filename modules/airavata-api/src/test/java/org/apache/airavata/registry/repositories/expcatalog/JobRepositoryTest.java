/**
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
import org.springframework.test.context.TestConstructor;

/**
 * Integration tests for JobRepository.
 */
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class JobRepositoryTest extends TestBase {

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
        this.gatewayService = gatewayService;
        this.projectService = projectService;
        this.experimentService = experimentService;
        this.processService = processService;
        this.taskService = taskService;
        this.jobService = jobService;
    }

    @BeforeEach
    public void setUp() throws Exception {
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
        assertNotNull(taskId);
    }

    @Test
    public void testJobRepository_CreateAndUpdate() throws Exception {
        JobModel jobModel = new JobModel();
        jobModel.setJobId("job-" + java.util.UUID.randomUUID().toString());
        jobModel.setTaskId(taskId);
        jobModel.setJobDescription("Test job description");

        if (jobModel.getJobStatuses() == null) {
            jobModel.setJobStatuses(new java.util.ArrayList<>());
        }
        JobStatus jobStatus = new JobStatus(JobState.SUBMITTED);
        jobModel.getJobStatuses().add(jobStatus);

        String jobId = jobService.addJob(jobModel, processId);
        assertNotNull(jobId);
        assertEquals(1, taskService.getTask(taskId).getJobs().size());

        JobPK jobPK = new JobPK();
        jobPK.setJobId(jobId);
        jobPK.setTaskId(taskId);

        jobModel.setJobName("Updated job name");
        jobService.updateJob(jobModel, jobPK);

        final JobModel retrievedJob = jobService.getJob(jobPK);
        assertNotNull(retrievedJob);
        assertEquals("Updated job name", retrievedJob.getJobName());
        assertEquals(1, retrievedJob.getJobStatuses().size());
        assertEquals(JobState.SUBMITTED, retrievedJob.getJobStatuses().get(0).getJobState());
    }

    @Test
    public void testJobRepository_GetJobIdsByTaskId() throws Exception {
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
        assertNotNull(jobId);

        List<String> jobIdList = jobService.getJobIds(DBConstants.Job.TASK_ID, taskId);
        assertEquals(1, jobIdList.size());
        assertEquals(jobId, jobIdList.get(0));
    }

    @Test
    public void testJobRepository_JobDeletion() throws Exception {
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

        assertTrue(jobService.isJobExist(jobPK));
        jobService.removeJob(jobPK);
        assertFalse(jobService.isJobExist(jobPK));
    }
}
