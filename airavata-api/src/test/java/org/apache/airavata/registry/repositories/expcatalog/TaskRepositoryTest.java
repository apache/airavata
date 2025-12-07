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

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentType;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.status.TaskState;
import org.apache.airavata.model.status.TaskStatus;
import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.model.task.TaskTypes;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.exceptions.RegistryException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.ProcessService;
import org.apache.airavata.registry.services.ProjectService;
import org.apache.airavata.registry.services.TaskService;
import org.apache.airavata.registry.utils.DBConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {org.apache.airavata.config.JpaConfig.class})
@TestPropertySource(locations = "classpath:airavata.properties")
public class TaskRepositoryTest extends TestBase {

    @Autowired
    GatewayService gatewayService;

    @Autowired
    ProjectService projectService;

    @Autowired
    ExperimentService experimentService;

    @Autowired
    ProcessService processService;

    @Autowired
    TaskService taskService;

    public TaskRepositoryTest() {
        super(Database.EXP_CATALOG);
    }

    @Test
    public void testTaskRepository() throws RegistryException {
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
        taskModel.setSubTaskModel("subtask model".getBytes(StandardCharsets.UTF_8));

        TaskStatus taskStatus = new TaskStatus(TaskState.CREATED);
        taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
        taskModel.addToTaskStatuses(taskStatus);

        String taskId = taskService.addTask(taskModel, processId);
        assertTrue(taskId != null);
        assertTrue(processService.getProcess(processId).getTasks().size() == 1);

        taskModel.setTaskType(TaskTypes.MONITORING);
        taskService.updateTask(taskModel, taskId);
        TaskModel retrievedTask = taskService.getTask(taskId);
        assertEquals(TaskTypes.MONITORING, retrievedTask.getTaskType());
        assertArrayEquals("subtask model".getBytes(StandardCharsets.UTF_8), retrievedTask.getSubTaskModel());
        assertEquals(1, retrievedTask.getTaskStatusesSize());
        assertEquals(TaskState.CREATED, retrievedTask.getTaskStatuses().get(0).getState());

        List<String> taskIdList = taskService.getTaskIds(DBConstants.Task.PARENT_PROCESS_ID, processId);
        assertTrue(taskIdList.size() == 1);
        assertTrue(taskIdList.get(0).equals(taskId));

        experimentService.removeExperiment(experimentId);
        processService.removeProcess(processId);
        taskService.removeTask(taskId);
        assertFalse(taskService.isTaskExist(taskId));

        gatewayService.removeGateway(gatewayId);
        projectService.removeProject(projectId);
    }
}
