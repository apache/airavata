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
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.cpi.RegistryException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class JobRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(JobRepositoryTest.class);

    GatewayRepository gatewayRepository;
    ProjectRepository projectRepository;
    ExperimentRepository experimentRepository;
    ProcessRepository processRepository;
    TaskRepository taskRepository;
    JobRepository jobRepository;

    public JobRepositoryTest() {
        super(Database.EXP_CATALOG);
        gatewayRepository = new GatewayRepository();
        projectRepository = new ProjectRepository();
        experimentRepository = new ExperimentRepository();
        processRepository = new ProcessRepository();
        taskRepository = new TaskRepository();
        jobRepository = new JobRepository();
    }

    @Test
    public void JobRepositoryTest() throws RegistryException {
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

        JobModel jobModel = new JobModel();
        jobModel.setJobId("job");
        jobModel.setTaskId(taskId);
        jobModel.setJobDescription("jobDescription");

        JobStatus jobStatus = new JobStatus(JobState.SUBMITTED);
        jobModel.addToJobStatuses(jobStatus);

        String jobId = jobRepository.addJob(jobModel, processId);
        assertTrue(jobId != null);
        assertTrue(taskRepository.getTask(taskId).getJobs().size() == 1);

        JobPK jobPK = new JobPK();
        jobPK.setJobId(jobId);
        jobPK.setTaskId(taskId);

        jobModel.setJobName("jobName");
        jobRepository.updateJob(jobModel, jobPK);
        final JobModel retrievedJob = jobRepository.getJob(jobPK);
        assertEquals("jobName", retrievedJob.getJobName());
        assertEquals(1, retrievedJob.getJobStatusesSize());
        assertEquals(JobState.SUBMITTED, retrievedJob.getJobStatuses().get(0).getJobState());


        List<String> jobIdList = jobRepository.getJobIds(DBConstants.Job.TASK_ID, taskId);
        assertTrue(jobIdList.size() == 1);
        assertTrue(jobIdList.get(0).equals(jobId));

        experimentRepository.removeExperiment(experimentId);
        processRepository.removeProcess(processId);
        taskRepository.removeTask(taskId);
        jobRepository.removeJob(jobPK);
        assertFalse(jobRepository.isJobExist(jobPK));

        gatewayRepository.removeGateway(gatewayId);
        projectRepository.removeProject(projectId);
    }

}
