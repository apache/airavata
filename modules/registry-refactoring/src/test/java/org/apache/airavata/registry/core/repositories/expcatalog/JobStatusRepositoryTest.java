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
import org.apache.airavata.model.status.JobState;
import org.apache.airavata.model.status.JobStatus;
import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.model.task.TaskTypes;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.core.entities.expcatalog.JobPK;
import org.apache.airavata.registry.core.repositories.common.TestBase;
import org.apache.airavata.registry.cpi.RegistryException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JobStatusRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(JobStatusRepositoryTest.class);

    GatewayRepository gatewayRepository;
    ProjectRepository projectRepository;
    ExperimentRepository experimentRepository;
    ProcessRepository processRepository;
    TaskRepository taskRepository;
    JobRepository jobRepository;
    JobStatusRepository jobStatusRepository;

    public JobStatusRepositoryTest() {
        super(Database.EXP_CATALOG);
        gatewayRepository = new GatewayRepository();
        projectRepository = new ProjectRepository();
        experimentRepository = new ExperimentRepository();
        processRepository = new ProcessRepository();
        taskRepository = new TaskRepository();
        jobRepository = new JobRepository();
        jobStatusRepository = new JobStatusRepository();
    }

    @Test
    public void JobStatusRepositoryTest() throws RegistryException {
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
        String processId = processRepository.addProcess(processModel, experimentId);

        TaskModel taskModel = new TaskModel();
        taskModel.setTaskType(TaskTypes.JOB_SUBMISSION);
        taskModel.setParentProcessId(processId);

        String taskId = taskRepository.addTask(taskModel, processId);
        assertTrue(taskId != null);

        taskModel.setTaskType(TaskTypes.MONITORING);
        taskRepository.updateTask(taskModel, taskId);

        JobPK jobPK = new JobPK();
        jobPK.setJobId("job");
        jobPK.setTaskId(taskId);

        JobModel jobModel = new JobModel();
        jobModel.setJobId(jobPK.getJobId());
        jobModel.setTaskId(jobPK.getTaskId());
        jobModel.setJobDescription("jobDescription");

        String jobId = jobRepository.addJob(jobModel, processId);
        assertTrue(jobId != null);

        JobStatus jobStatus = new JobStatus(JobState.QUEUED);
        String jobStatusId = jobStatusRepository.addJobStatus(jobStatus, jobPK);
        assertTrue(jobStatusId != null);
        assertTrue(jobRepository.getJob(jobPK).getJobStatuses().size() == 1);

        jobStatus.setJobState(JobState.ACTIVE);
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
