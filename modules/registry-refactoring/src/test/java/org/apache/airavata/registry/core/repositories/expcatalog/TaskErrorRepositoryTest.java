/*
 *
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
 */
package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentType;
import org.apache.airavata.model.process.ProcessModel;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class TaskErrorRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(TaskErrorRepositoryTest.class);

    private GatewayRepository gatewayRepository;
    private ProjectRepository projectRepository;
    private ExperimentRepository experimentRepository;
    private ProcessRepository processRepository;
    private TaskRepository taskRepository;
    private TaskErrorRepository taskErrorRepository;

    public TaskErrorRepositoryTest() {
        super(Database.EXP_CATALOG);
        gatewayRepository = new GatewayRepository();
        projectRepository = new ProjectRepository();
        experimentRepository = new ExperimentRepository();
        processRepository = new ProcessRepository();
        taskRepository = new TaskRepository();
        taskErrorRepository = new TaskErrorRepository();
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
    public void addTaskErrorRepositoryTest() throws RegistryException {
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

        ErrorModel errorModel = new ErrorModel();
        errorModel.setErrorId("error");

        String taskErrorId = taskErrorRepository.addTaskError(errorModel, taskId);
        assertNotNull(taskErrorId);
        assertEquals(1, taskRepository.getTask(taskId).getTaskErrors().size());

        ErrorModel retrievedErrorModel = taskErrorRepository.getTaskError(taskId).get(0);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(errorModel, retrievedErrorModel, "__isset_bitfield"));
    }

    @Test
    public void updateTaskErrorRepositoryTest() throws RegistryException {
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

        ErrorModel errorModel = new ErrorModel();
        errorModel.setErrorId("error");

        String taskErrorId = taskErrorRepository.addTaskError(errorModel, taskId);
        assertNotNull(taskErrorId);
        assertEquals(1, taskRepository.getTask(taskId).getTaskErrors().size());

        errorModel.setActualErrorMessage("message");
        taskErrorRepository.updateTaskError(errorModel, taskId);

        List<ErrorModel> retrievedErrorList = taskErrorRepository.getTaskError(taskId);
        assertEquals(1, retrievedErrorList.size());
        assertEquals("message", retrievedErrorList.get(0).getActualErrorMessage());

        Assert.assertTrue(EqualsBuilder.reflectionEquals(errorModel, retrievedErrorList.get(0), "__isset_bitfield"));
    }

    @Test
    public void retrieveSingleTaskErrorRepositoryTest() throws RegistryException {
        List<ErrorModel> actualErrorModelList = new ArrayList<>();
        List<String> taskIdList = new ArrayList<>();

        for (int i = 0 ; i < 5; i++) {
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
            taskIdList.add(taskId);

            ErrorModel errorModel = new ErrorModel();
            errorModel.setErrorId("error");
            actualErrorModelList.add(errorModel);

            String taskErrorId = taskErrorRepository.addTaskError(errorModel, taskId);
            assertNotNull(taskErrorId);
            assertEquals(1, taskRepository.getTask(taskId).getTaskErrors().size());
        }

        for (int j = 0 ; j < 5; j++) {
            ErrorModel savedErrorModel = taskErrorRepository.getTaskError(taskIdList.get(j)).get(0);
            ErrorModel actualErrorModel = actualErrorModelList.get(j);
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualErrorModel, savedErrorModel, "__isset_bitfield"));
        }
    }

    @Test
    public void retrieveMultipleTaskErrorRepositoryTest() throws RegistryException {
        List<String> actualTaskIdList = new ArrayList<>();
        HashMap<String, ErrorModel> actualTaskErrorModelMap = new HashMap<>();

        for (int i = 0 ; i < 5; i++) {
            Gateway gateway = createSampleGateway("" + i);
            String gatewayId = gatewayRepository.addGateway(gateway);
            Assert.assertNotNull(gatewayId);

            Project project = createSampleProject("" + i);
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
            actualTaskIdList.add(taskId);

            ErrorModel errorModel = new ErrorModel();
            errorModel.setErrorId("error");
            actualTaskErrorModelMap.put(taskId, errorModel);

            String taskErrorId = taskErrorRepository.addTaskError(errorModel, taskId);
            assertNotNull(taskErrorId);
            assertEquals(1, taskRepository.getTask(taskId).getTaskErrors().size());
        }

        for (int j = 0 ; j < 5; j++) {
            ErrorModel savedErrorModel = taskErrorRepository.getTaskError(actualTaskIdList.get(j)).get(0);
            ErrorModel actualErrorModel = actualTaskErrorModelMap.get(actualTaskIdList.get(j));
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualErrorModel, savedErrorModel, "__isset_bitfield"));
        }
    }

}
