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
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class JobRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(JobRepositoryTest.class);

    private GatewayRepository gatewayRepository;
    private ProjectRepository projectRepository;
    private ExperimentRepository experimentRepository;
    private ProcessRepository processRepository;
    private TaskRepository taskRepository;
    private JobRepository jobRepository;

    public JobRepositoryTest() {
        super(Database.EXP_CATALOG);
        gatewayRepository = new GatewayRepository();
        projectRepository = new ProjectRepository();
        experimentRepository = new ExperimentRepository();
        processRepository = new ProcessRepository();
        taskRepository = new TaskRepository();
        jobRepository = new JobRepository();
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
    public void addJobRepositoryTest() throws RegistryException {
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

        JobModel jobModel = new JobModel();
        jobModel.setJobId("job");
        jobModel.setTaskId(taskId);
        jobModel.setJobDescription("jobDescription");

        JobStatus jobStatus = new JobStatus(JobState.SUBMITTED);
        jobModel.addToJobStatuses(jobStatus);

        String jobId = jobRepository.addJob(jobModel, processId);
        assertNotNull(jobId);
        assertEquals(1, taskRepository.getTask(taskId).getJobs().size());

        JobModel savedJob = jobRepository.getJobList(DBConstants.Job.TASK_ID, taskId).get(0);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(jobModel, savedJob,"__isset_bitfield"));
    }

    @Test
    public void updateJobRepositoryTest() throws RegistryException {
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

        JobModel jobModel = new JobModel();
        jobModel.setJobId("job");
        jobModel.setTaskId(taskId);
        jobModel.setJobDescription("jobDescription");

        JobStatus jobStatus = new JobStatus(JobState.SUBMITTED);
        jobModel.addToJobStatuses(jobStatus);

        String jobId = jobRepository.addJob(jobModel, processId);
        assertNotNull(jobId);
        assertEquals(1, taskRepository.getTask(taskId).getJobs().size());

        JobPK jobPK = new JobPK();
        jobPK.setJobId(jobId);
        jobPK.setTaskId(taskId);

        jobModel.setJobName("jobName");
        jobRepository.updateJob(jobModel, jobPK);

        List<String> jobIdList = jobRepository.getJobIds(DBConstants.Job.TASK_ID, taskId);
        assertEquals(1, jobIdList.size());
        assertEquals(jobIdList.get(0), jobId);

        JobModel savedJob = jobRepository.getJobList(DBConstants.Job.TASK_ID, taskId).get(0);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(jobModel, savedJob,"__isset_bitfield"));
    }

    @Test
    public void retrieveSingleJobRepositoryTest() throws RegistryException {
        List<JobModel> actualJobModelList = new ArrayList<>();
        List<String> taskIdList = new ArrayList<>();

        for (int i = 0 ; i < 5; i++) {
            Gateway gateway = createSampleGateway("" + i);
            String gatewayId = gatewayRepository.addGateway(gateway);
            Assert.assertNotNull(gatewayId);

            Project project = createSampleProject("" + i);
            String projectId = projectRepository.addProject(project, gatewayId);
            Assert.assertNotNull(projectId);

            ExperimentModel experimentModel = createSampleExperiment(projectId, gatewayId, "" + i);
            String experimentId = experimentRepository.addExperiment(experimentModel);
            Assert.assertNotNull(experimentId);

            ProcessModel processModel = new ProcessModel(null, experimentId);
            String processId = processRepository.addProcess(processModel, experimentId);

            TaskModel taskModel = new TaskModel();
            taskModel.setTaskType(TaskTypes.JOB_SUBMISSION);
            taskModel.setParentProcessId(processId);

            String taskId = taskRepository.addTask(taskModel, processId);
            assertNotNull(taskId);
            taskIdList.add(taskId);

            taskModel.setTaskType(TaskTypes.MONITORING);
            taskRepository.updateTask(taskModel, taskId);

            JobModel jobModel = new JobModel();
            jobModel.setJobId("job" + i);
            jobModel.setTaskId(taskId);
            jobModel.setJobDescription("jobDescription" + i);

            JobStatus jobStatus = new JobStatus(JobState.SUBMITTED);
            jobModel.addToJobStatuses(jobStatus);

            String jobId = jobRepository.addJob(jobModel, processId);
            assertNotNull(jobId);
            assertEquals(1, taskRepository.getTask(taskId).getJobs().size());

            actualJobModelList.add(jobModel);
        }

        for (int j = 0 ; j < 5; j++) {
            List<JobModel> savedjobModelList = jobRepository.getJobList(DBConstants.Job.TASK_ID, taskIdList.get(j));
            Assert.assertEquals(1, savedjobModelList.size());

            JobModel actualJobModel = actualJobModelList.get(j);
            JobModel savedJobModel = savedjobModelList.get(0);
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualJobModel, savedJobModel, "__isset_bitfield"));
        }
    }

    @Test
    public void retrieveMultipleJobRepositoryTest() throws RegistryException {
        List<String> taskIdList = new ArrayList<>();
        HashMap<String, JobModel> actualJobModelMap = new HashMap<>();

        for (int i = 0 ; i < 5; i++) {
            Gateway gateway = createSampleGateway("" + i);
            String gatewayId = gatewayRepository.addGateway(gateway);
            Assert.assertNotNull(gatewayId);

            Project project = createSampleProject("" + i);
            String projectId = projectRepository.addProject(project, gatewayId);
            Assert.assertNotNull(projectId);

            ExperimentModel experimentModel = createSampleExperiment(projectId, gatewayId, "" + i);
            String experimentId = experimentRepository.addExperiment(experimentModel);
            Assert.assertNotNull(experimentId);

            ProcessModel processModel = new ProcessModel(null, experimentId);
            String processId = processRepository.addProcess(processModel, experimentId);

            TaskModel taskModel = new TaskModel();
            taskModel.setTaskType(TaskTypes.JOB_SUBMISSION);
            taskModel.setParentProcessId(processId);

            String taskId = taskRepository.addTask(taskModel, processId);
            assertNotNull(taskId);
            taskIdList.add(taskId);

            taskModel.setTaskType(TaskTypes.MONITORING);
            taskRepository.updateTask(taskModel, taskId);

            JobModel jobModel = new JobModel();
            jobModel.setJobId("job" + i);
            jobModel.setTaskId(taskId);
            jobModel.setJobDescription("jobDescription" + i);

            JobStatus jobStatus = new JobStatus(JobState.SUBMITTED);
            jobModel.addToJobStatuses(jobStatus);

            String jobId = jobRepository.addJob(jobModel, processId);
            assertNotNull(jobId);
            assertEquals(1, taskRepository.getTask(taskId).getJobs().size());

            actualJobModelMap.put(taskId, jobModel);
        }

        for (int j = 0 ; j < 5; j++) {
            List<JobModel> savedJobModelList = jobRepository.getJobList(DBConstants.Job.TASK_ID, taskIdList.get(j));
            Assert.assertEquals(1, savedJobModelList.size());

            JobModel actualJobModel = actualJobModelMap.get(taskIdList.get(j));
            JobModel savedJobModel = savedJobModelList.get(0);

            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualJobModel, savedJobModel,"__isset_bitfield"));
        }
    }

}
