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

import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentType;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.status.TaskState;
import org.apache.airavata.model.status.TaskStatus;
import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.model.task.TaskTypes;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.core.repositories.common.TestBase;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class TaskRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(TaskRepositoryTest.class);

    private GatewayRepository gatewayRepository;
    private ProjectRepository projectRepository;
    private ExperimentRepository experimentRepository;
    private ProcessRepository processRepository;
    private TaskRepository taskRepository;

    public TaskRepositoryTest() {
        super(Database.EXP_CATALOG);
        gatewayRepository = new GatewayRepository();
        projectRepository = new ProjectRepository();
        experimentRepository = new ExperimentRepository();
        processRepository = new ProcessRepository();
        taskRepository = new TaskRepository();
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
    public void addTaskRepositoryTest() throws RegistryException {
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
        taskModel.setSubTaskModel("subtask model".getBytes(StandardCharsets.UTF_8));

        TaskStatus taskStatus = new TaskStatus(TaskState.CREATED);
        taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
        taskModel.addToTaskStatuses(taskStatus);

        ErrorModel errorModel = new ErrorModel();
        errorModel.setErrorId("error");
        taskModel.addToTaskErrors(errorModel);

        JobModel jobModel = new JobModel();
        jobModel.setJobId("job" + "1");
        jobModel.setJobDescription("jobDescription" + "1");
        taskModel.addToJobs(jobModel);

        String taskId = taskRepository.addTask(taskModel, processId);
        assertNotNull(taskId);
        assertEquals(1, processRepository.getProcess(processId).getTasks().size());
        TaskModel savedTaskModel =  taskRepository.getTask(taskId);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(taskModel, savedTaskModel, "__isset_bitfield", "taskStatuses", "taskErrors", "jobs"));
        Assert.assertTrue(EqualsBuilder.reflectionEquals(taskModel.getTaskStatuses(), savedTaskModel.getTaskStatuses(), "__isset_bitfield"));
        Assert.assertTrue(EqualsBuilder.reflectionEquals(taskModel.getTaskErrors(), savedTaskModel.getTaskErrors(), "__isset_bitfield"));
        Assert.assertTrue(EqualsBuilder.reflectionEquals(taskModel.getJobs(), savedTaskModel.getJobs(), "__isset_bitfield"));
    }

    @Test
    public void updateTaskRepositoryTest() throws RegistryException {
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
        taskModel.setSubTaskModel("subtask model".getBytes(StandardCharsets.UTF_8));

        TaskStatus taskStatus = new TaskStatus(TaskState.CREATED);
        taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
        taskModel.addToTaskStatuses(taskStatus);

        ErrorModel errorModel = new ErrorModel();
        errorModel.setErrorId("error");
        taskModel.addToTaskErrors(errorModel);

        JobModel jobModel = new JobModel();
        jobModel.setJobId("job" + "1");
        jobModel.setJobDescription("jobDescription" + "1");
        taskModel.addToJobs(jobModel);

        String taskId = taskRepository.addTask(taskModel, processId);
        assertNotNull(taskId);
        assertEquals(1, processRepository.getProcess(processId).getTasks().size());

        taskModel.setTaskType(TaskTypes.MONITORING);
        taskRepository.updateTask(taskModel, taskId);
        TaskModel retrievedTask = taskRepository.getTask(taskId);
        assertEquals(TaskTypes.MONITORING, retrievedTask.getTaskType());
        assertArrayEquals("subtask model".getBytes(StandardCharsets.UTF_8), retrievedTask.getSubTaskModel());
        assertEquals(1, retrievedTask.getTaskStatusesSize());
        assertEquals(TaskState.CREATED, retrievedTask.getTaskStatuses().get(0).getState());

        TaskModel savedTaskModel =  taskRepository.getTask(taskId);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(taskModel, savedTaskModel, "__isset_bitfield", "taskStatuses", "taskErrors", "jobs"));
        Assert.assertTrue(EqualsBuilder.reflectionEquals(taskModel.getTaskStatuses(), savedTaskModel.getTaskStatuses(), "__isset_bitfield"));
        Assert.assertTrue(EqualsBuilder.reflectionEquals(taskModel.getTaskErrors(), savedTaskModel.getTaskErrors(), "__isset_bitfield"));
        Assert.assertTrue(EqualsBuilder.reflectionEquals(taskModel.getJobs(), savedTaskModel.getJobs(), "__isset_bitfield"));
    }

    @Test
    public void retrieveSingleTaskRepositoryTest() throws RegistryException {
        List<TaskModel> actualTaskModelList = new ArrayList<>();
        List<String> actualTaskIdList = new ArrayList<>();

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
            taskModel.setSubTaskModel("subtask model".getBytes(StandardCharsets.UTF_8));

            TaskStatus taskStatus = new TaskStatus(TaskState.CREATED);
            taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            taskModel.addToTaskStatuses(taskStatus);

            ErrorModel errorModel = new ErrorModel();
            errorModel.setErrorId("error");
            taskModel.addToTaskErrors(errorModel);

            JobModel jobModel = new JobModel();
            jobModel.setJobId("job" + "1");
            jobModel.setJobDescription("jobDescription" + "1");
            taskModel.addToJobs(jobModel);

            String taskId = taskRepository.addTask(taskModel, processId);
            assertNotNull(taskId);
            assertEquals(1, processRepository.getProcess(processId).getTasks().size());

            actualTaskModelList.add(taskModel);
            actualTaskIdList.add(taskId);
        }

        for (int j = 0 ; j < 5; j++) {
            TaskModel savedTaskModel = taskRepository.getTask(actualTaskIdList.get(j));
            TaskModel actualTaskModel = actualTaskModelList.get(j);

            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualTaskModel, savedTaskModel, "__isset_bitfield", "taskStatuses", "taskErrors", "jobs"));
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualTaskModel.getTaskStatuses(), savedTaskModel.getTaskStatuses(), "__isset_bitfield"));
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualTaskModel.getTaskErrors(), savedTaskModel.getTaskErrors(), "__isset_bitfield"));
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualTaskModel.getJobs(), savedTaskModel.getJobs(), "__isset_bitfield"));
        }
    }

    @Test
    public void retrieveMultipleTaskRepositoryTest() throws RegistryException {
        List<String> actualTaskIdList = new ArrayList<>();
        HashMap<String, TaskModel> actualTaskModelMap = new HashMap<>();

        for (int i = 0; i < 5; i++) {
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
            taskModel.setSubTaskModel("subtask model".getBytes(StandardCharsets.UTF_8));

            TaskStatus taskStatus = new TaskStatus(TaskState.CREATED);
            taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            taskModel.addToTaskStatuses(taskStatus);

            ErrorModel errorModel = new ErrorModel();
            errorModel.setErrorId("error");
            taskModel.addToTaskErrors(errorModel);

            JobModel jobModel = new JobModel();
            jobModel.setJobId("job" + "1");
            jobModel.setJobDescription("jobDescription" + "1");
            taskModel.addToJobs(jobModel);

            String taskId = taskRepository.addTask(taskModel, processId);
            assertNotNull(taskId);
            assertEquals(1, processRepository.getProcess(processId).getTasks().size());

            actualTaskModelMap.put(taskId, taskModel);
            actualTaskIdList.add(taskId);
        }
        for (int j = 0 ; j < 5; j++) {
            TaskModel savedTaskModel = taskRepository.getTask(actualTaskIdList.get(j));
            TaskModel actualTaskModel = actualTaskModelMap.get(actualTaskIdList.get(j));
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualTaskModel, savedTaskModel, "__isset_bitfield", "taskStatuses", "subTaskModel", "jobs", "taskErrors"));
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualTaskModel.getTaskStatuses(), savedTaskModel.getTaskStatuses(), "__isset_bitfield"));
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualTaskModel.getSubTaskModel(), savedTaskModel.getSubTaskModel(), "__isset_bitfield"));
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualTaskModel.getJobs(), savedTaskModel.getJobs(), "__isset_bitfield"));
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualTaskModel.getTaskErrors(), savedTaskModel.getTaskErrors(), "__isset_bitfield"));
        }
    }
}
