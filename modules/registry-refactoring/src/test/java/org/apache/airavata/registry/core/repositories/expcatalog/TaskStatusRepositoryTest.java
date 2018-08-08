package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentType;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class TaskStatusRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(TaskStatusRepositoryTest.class);

    private GatewayRepository gatewayRepository;
    private ProjectRepository projectRepository;
    private ExperimentRepository experimentRepository;
    private ProcessRepository processRepository;
    private TaskRepository taskRepository;
    private TaskStatusRepository taskStatusRepository;

    public TaskStatusRepositoryTest() {
        super(Database.EXP_CATALOG);
        gatewayRepository = new GatewayRepository();
        projectRepository = new ProjectRepository();
        experimentRepository = new ExperimentRepository();
        processRepository = new ProcessRepository();
        taskRepository = new TaskRepository();
        taskStatusRepository = new TaskStatusRepository();
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
    public void addTaskStatusRepositoryTest() throws RegistryException {
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

        TaskStatus taskStatus = new TaskStatus(TaskState.EXECUTING);
        String taskStatusId = taskStatusRepository.addTaskStatus(taskStatus, taskId);
        assertNotNull(taskStatusId);
        assertEquals(1, taskRepository.getTask(taskId).getTaskStatuses().size());

        TaskStatus savedTaskStatus = taskStatusRepository.getTaskStatus(taskId);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(taskStatus, savedTaskStatus));
    }

    @Test
    public void updateTaskStatusRepositoryTest() throws RegistryException {
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

        TaskStatus taskStatus = new TaskStatus(TaskState.EXECUTING);
        String taskStatusId = taskStatusRepository.addTaskStatus(taskStatus, taskId);
        assertNotNull(taskStatusId);
        assertEquals(1, taskRepository.getTask(taskId).getTaskStatuses().size());


        taskStatus.setState(TaskState.CREATED);
        taskStatusRepository.updateTaskStatus(taskStatus, taskId);

        TaskStatus retrievedTaskStatus = taskStatusRepository.getTaskStatus(taskId);
        assertEquals(TaskState.CREATED, retrievedTaskStatus.getState());

        TaskStatus savedTaskStatus = taskStatusRepository.getTaskStatus(taskId);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(taskStatus, savedTaskStatus));
    }

    @Test
    public void retrieveSingleTaskStatusRepositoryTest() throws RegistryException {
        List<TaskStatus> actualTaskStatusList = new ArrayList<>();
        List<String> actualTaskIdList = new ArrayList<>();

        for (int i = 0 ; i < 5; i++) {
            Gateway gateway = createSampleGateway("1" + i);
            String gatewayId = gatewayRepository.addGateway(gateway);
            Assert.assertNotNull(gatewayId);

            Project project = createSampleProject("1" + i);
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

            TaskStatus taskStatus = new TaskStatus(TaskState.EXECUTING);
            String taskStatusId = taskStatusRepository.addTaskStatus(taskStatus, taskId);
            assertNotNull(taskStatusId);
            assertEquals(1, taskRepository.getTask(taskId).getTaskStatuses().size());

            actualTaskIdList.add(taskId);
            actualTaskStatusList.add(taskStatus);
        }

        for (int j = 0 ; j < 5; j++) {
            TaskStatus savedTaskStatus = taskStatusRepository.getTaskStatus(actualTaskIdList.get(j));
            TaskStatus actualTaskStatus = actualTaskStatusList.get(j);
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualTaskStatus, savedTaskStatus, "__isset_bitfield", "taskStatuses", "taskErrors", "jobs"));
        }
    }

    @Test
    public void retrieveMultipleTaskStatusRepositoryTest() throws RegistryException {
        List<String> actualTaskIdList = new ArrayList<>();
        HashMap<String, TaskStatus> actualTaskStatusMap = new HashMap<>();

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

            String taskId = taskRepository.addTask(taskModel, processId);
            assertNotNull(taskId);

            TaskStatus taskStatus = new TaskStatus(TaskState.EXECUTING);
            String taskStatusId = taskStatusRepository.addTaskStatus(taskStatus, taskId);
            assertNotNull(taskStatusId);
            assertEquals(1, taskRepository.getTask(taskId).getTaskStatuses().size());

            actualTaskIdList.add(taskId);
            actualTaskStatusMap.put(taskId, taskStatus);
        }
        for (int j = 0 ; j < 5; j++) {
            TaskStatus savedTaskStatus = taskStatusRepository.getTaskStatus(actualTaskIdList.get(j));
            TaskStatus actualTaskStatus = actualTaskStatusMap.get(actualTaskIdList.get(j));
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualTaskStatus, savedTaskStatus, "__isset_bitfield"));
        }
    }
}
