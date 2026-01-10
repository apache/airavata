/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.
 */
package org.apache.airavata.registry.repositories.expcatalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.ExperimentType;
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.common.model.TaskModel;
import org.apache.airavata.common.model.TaskState;
import org.apache.airavata.common.model.TaskStatus;
import org.apache.airavata.common.model.TaskTypes;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.ProcessService;
import org.apache.airavata.registry.services.ProjectService;
import org.apache.airavata.registry.services.TaskService;
import org.apache.airavata.registry.utils.DBConstants;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestConstructor;

@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class TaskRepositoryTest extends TestBase {

    private final GatewayService gatewayService;
    private final ProjectService projectService;
    private final ExperimentService experimentService;
    private final ProcessService processService;
    private final TaskService taskService;

    public TaskRepositoryTest(
            GatewayService gatewayService,
            ProjectService projectService,
            ExperimentService experimentService,
            ProcessService processService,
            TaskService taskService) {
        this.gatewayService = gatewayService;
        this.projectService = projectService;
        this.experimentService = experimentService;
        this.processService = processService;
        this.taskService = taskService;
    }

    @Test
    public void testTaskRepository() throws Exception {
        Gateway gateway = new Gateway();
        gateway.setGatewayId("gateway-" + java.util.UUID.randomUUID().toString());
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

        ProcessModel processModel = new ProcessModel();
        processModel.setExperimentId(experimentId);
        String processId = processService.addProcess(processModel, experimentId);

        TaskModel taskModel = new TaskModel();
        taskModel.setTaskType(TaskTypes.JOB_SUBMISSION);
        taskModel.setParentProcessId(processId);
        taskModel.setSubTaskModel("test-subtask".getBytes(StandardCharsets.UTF_8));

        if (taskModel.getTaskStatuses() == null) {
            taskModel.setTaskStatuses(new java.util.ArrayList<>());
        }
        TaskStatus taskStatus = new TaskStatus();
        taskStatus.setState(TaskState.CREATED);
        taskStatus.setTimeOfStateChange(AiravataUtils.getUniqueTimestamp().getTime());
        taskModel.getTaskStatuses().add(taskStatus);

        String taskId = taskService.addTask(taskModel, processId);
        assertNotNull(taskId);
        assertTrue(taskService.isTaskExist(taskId));

        TaskModel retrievedTask = taskService.getTask(taskId);
        assertNotNull(retrievedTask);
        assertEquals(TaskTypes.JOB_SUBMISSION, retrievedTask.getTaskType());
        assertNotNull(retrievedTask.getSubTaskModel());
        assertEquals(1, retrievedTask.getTaskStatuses().size());
        assertEquals(TaskState.CREATED, retrievedTask.getTaskStatuses().get(0).getState());

        List<String> taskIds = taskService.getTaskIds(DBConstants.Task.PARENT_PROCESS_ID, processId);
        assertEquals(1, taskIds.size());

        taskService.removeTask(taskId);
        assertFalse(taskService.isTaskExist(taskId));
    }
}
