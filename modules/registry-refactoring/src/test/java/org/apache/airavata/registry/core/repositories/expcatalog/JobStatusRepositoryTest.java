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
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class JobStatusRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(JobStatusRepositoryTest.class);

    private GatewayRepository gatewayRepository;
    private ProjectRepository projectRepository;
    private ExperimentRepository experimentRepository;
    private ProcessRepository processRepository;
    private TaskRepository taskRepository;
    private JobRepository jobRepository;
    private JobStatusRepository jobStatusRepository;

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

    private Gateway createSampleGateway(String tag) {
        Gateway gateway = new Gateway();
        gateway.setGatewayId("gateway" + tag);
        gateway.setDomain("SEAGRID" + tag);
        gateway.setEmailAddress("abc@d + " + tag + "+.com");
        return gateway;
    }

    private Project createSampleProject(String tag) {
        Project project = new Project();
        project.setName("projectName" + tag);
        project.setOwner("user" + tag);
        return project;
    }

    private ExperimentModel createSampleExperiment(String projectId, String gatewayId, String tag) {
        ExperimentModel experimentModel = new ExperimentModel();
        experimentModel.setProjectId(projectId);
        experimentModel.setGatewayId(gatewayId);
        experimentModel.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        experimentModel.setUserName("user" + tag);
        experimentModel.setExperimentName("name" + tag);
        return experimentModel;
    }

    @Test
    public void addJobStatusRepositoryTest() throws RegistryException {
        Gateway gateway = createSampleGateway("1");
        String gatewayId = gatewayRepository.addGateway(gateway);
        Assert.assertNotNull(gatewayId);

        Project project = createSampleProject("1");
        String projectId = projectRepository.addProject(project, gatewayId);
        Assert.assertNotNull(projectId);

        ExperimentModel experimentModel = createSampleExperiment(projectId, gatewayId, "1");
        String experimentId = experimentRepository.addExperiment(experimentModel);
        Assert.assertNotNull(experimentId);

        ProcessModel processModel = new ProcessModel(null, experimentId);
        String processId = processRepository.addProcess(processModel, experimentId);

        TaskModel taskModel = new TaskModel();
        taskModel.setTaskType(TaskTypes.JOB_SUBMISSION);
        taskModel.setParentProcessId(processId);

        String taskId = taskRepository.addTask(taskModel, processId);
        assertNotNull(taskId);

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
        assertNotNull(jobId);

        JobStatus jobStatus = new JobStatus(JobState.QUEUED);
        String jobStatusId = jobStatusRepository.addJobStatus(jobStatus, jobPK);
        assertNotNull(jobStatusId);
        assertEquals(1, jobRepository.getJob(jobPK).getJobStatuses().size());

        List<JobStatus> savedJobStatusList = jobRepository.getJob(jobPK).getJobStatuses();
        JobStatus savedJobStatus = savedJobStatusList.get(0);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(jobStatus, savedJobStatus, "__isset_bitfield"));
    }

    @Test
    public void updateJobStatusRepositoryTest() throws RegistryException {
        Gateway gateway = createSampleGateway("1");
        String gatewayId = gatewayRepository.addGateway(gateway);
        Assert.assertNotNull(gatewayId);

        Project project = createSampleProject("1");
        String projectId = projectRepository.addProject(project, gatewayId);
        Assert.assertNotNull(projectId);

        ExperimentModel experimentModel = createSampleExperiment(projectId, gatewayId, "1");
        String experimentId = experimentRepository.addExperiment(experimentModel);
        Assert.assertNotNull(experimentId);

        ProcessModel processModel = new ProcessModel(null, experimentId);
        String processId = processRepository.addProcess(processModel, experimentId);

        TaskModel taskModel = new TaskModel();
        taskModel.setTaskType(TaskTypes.JOB_SUBMISSION);
        taskModel.setParentProcessId(processId);

        String taskId = taskRepository.addTask(taskModel, processId);
        assertNotNull(taskId);

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
        assertNotNull(jobId);

        JobStatus jobStatus = new JobStatus(JobState.QUEUED);
        String jobStatusId = jobStatusRepository.addJobStatus(jobStatus, jobPK);
        assertNotNull(jobStatusId);
        assertEquals(1, jobRepository.getJob(jobPK).getJobStatuses().size());

        //Update Logic
        jobStatus.setJobState(JobState.ACTIVE);
        jobStatusRepository.updateJobStatus(jobStatus, jobPK);

        JobStatus retrievedJobStatus = jobStatusRepository.getJobStatus(jobPK);
        assertEquals(JobState.ACTIVE, retrievedJobStatus.getJobState());

        List<JobStatus> savedJobStatusList = jobRepository.getJob(jobPK).getJobStatuses();
        JobStatus savedJobStatus = savedJobStatusList.get(0);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(jobStatus, savedJobStatus, "__isset_bitfield"));
    }

    @Test
    public void retreiveSingleExperimentTest() throws RegistryException {
        List<JobStatus> actualStatusList = new ArrayList<>();
        List<JobPK> jobPKList = new ArrayList<>();

        for (int i = 0 ; i < 5; i++) {
            Gateway gateway = createSampleGateway("" + i);
            String gatewayId = gatewayRepository.addGateway(gateway);
            Assert.assertNotNull(gatewayId);

            Project project = createSampleProject("1" + i);
            String projectId = projectRepository.addProject(project, gatewayId);
            Assert.assertNotNull(projectId);

            ExperimentModel experimentModel = createSampleExperiment(projectId, gatewayId, "1" + i);
            String experimentId = experimentRepository.addExperiment(experimentModel);
            Assert.assertNotNull(experimentId);

            ProcessModel processModel = new ProcessModel(null, experimentId);
            String processId = processRepository.addProcess(processModel, experimentId);

            TaskModel taskModel = new TaskModel();
            taskModel.setTaskType(TaskTypes.JOB_SUBMISSION);
            taskModel.setParentProcessId(processId);

            String taskId = taskRepository.addTask(taskModel, processId);
            assertNotNull(taskId);

            taskModel.setTaskType(TaskTypes.MONITORING);
            taskRepository.updateTask(taskModel, taskId);

            JobPK jobPK = new JobPK();
            jobPK.setJobId("job" + i);
            jobPK.setTaskId(taskId);

            JobModel jobModel = new JobModel();
            jobModel.setJobId(jobPK.getJobId());
            jobModel.setTaskId(jobPK.getTaskId());
            jobModel.setJobDescription("jobDescription" + i);

            String jobId = jobRepository.addJob(jobModel, processId);
            assertNotNull(jobId);

            JobStatus jobStatus = new JobStatus(JobState.QUEUED);
            String jobStatusId = jobStatusRepository.addJobStatus(jobStatus, jobPK);
            assertNotNull(jobStatusId);
            assertEquals(1, jobRepository.getJob(jobPK).getJobStatuses().size());

            jobPKList.add(jobPK);
            actualStatusList.add(jobStatus);
        }

        for (int j = 0 ; j < 5; j++) {
            List<JobStatus> savedjobStatusList = jobRepository.getJob(jobPKList.get(j)).getJobStatuses();
            Assert.assertEquals(1, savedjobStatusList.size());

            JobStatus actualJobStatus = actualStatusList.get(j);
            JobStatus savedJobStatus = savedjobStatusList.get(0);
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualJobStatus, savedJobStatus, "__isset_bitfield"));
        }
    }

    @Test
    public void retrieveMultiJobStatusRepoTest() throws RegistryException {
        List<JobPK> jobPKList  = new ArrayList<>();
        HashMap<JobPK, JobStatus> actualJobStatusMap = new HashMap<>();

        for (int i = 0 ; i < 5; i++) {
            Gateway gateway = createSampleGateway("" + i);
            String gatewayId = gatewayRepository.addGateway(gateway);
            Assert.assertNotNull(gatewayId);

            Project project = createSampleProject("1" + i);
            String projectId = projectRepository.addProject(project, gatewayId);
            Assert.assertNotNull(projectId);

            ExperimentModel experimentModel = createSampleExperiment(projectId, gatewayId, "1" + i);
            String experimentId = experimentRepository.addExperiment(experimentModel);
            Assert.assertNotNull(experimentId);

            ProcessModel processModel = new ProcessModel(null, experimentId);
            String processId = processRepository.addProcess(processModel, experimentId);

            TaskModel taskModel = new TaskModel();
            taskModel.setTaskType(TaskTypes.JOB_SUBMISSION);
            taskModel.setParentProcessId(processId);

            String taskId = taskRepository.addTask(taskModel, processId);
            assertNotNull(taskId);

            taskModel.setTaskType(TaskTypes.MONITORING);
            taskRepository.updateTask(taskModel, taskId);

            JobPK jobPK = new JobPK();
            jobPK.setJobId("job" + i);
            jobPK.setTaskId(taskId);

            JobModel jobModel = new JobModel();
            jobModel.setJobId(jobPK.getJobId());
            jobModel.setTaskId(jobPK.getTaskId());
            jobModel.setJobDescription("jobDescription" + i);

            String jobId = jobRepository.addJob(jobModel, processId);
            assertNotNull(jobId);

            JobStatus jobStatus = new JobStatus(JobState.QUEUED);
            String jobStatusId = jobStatusRepository.addJobStatus(jobStatus, jobPK);
            assertNotNull(jobStatusId);
            assertEquals(1, jobRepository.getJob(jobPK).getJobStatuses().size());

            jobPKList.add(jobPK);
            actualJobStatusMap.put(jobPK, jobStatus);
        }

        for (int j = 0 ; j < 5; j++) {
            List<JobStatus> savedJobStatusList = jobRepository.getJob(jobPKList.get(j)).getJobStatuses();
            Assert.assertEquals(1, savedJobStatusList.size());

            JobStatus actualJobStatus = actualJobStatusMap.get(jobPKList.get(j));
            JobStatus savedJobStatus = savedJobStatusList.get(0);

            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualJobStatus, savedJobStatus,"__isset_bitfield"));
        }
    }

}
