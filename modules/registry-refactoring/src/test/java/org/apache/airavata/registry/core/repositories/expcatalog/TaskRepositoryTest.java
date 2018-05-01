package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentType;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.status.TaskState;
import org.apache.airavata.model.status.TaskStatus;
import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.model.task.TaskTypes;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.core.repositories.expcatalog.util.Initialize;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.cpi.RegistryException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class TaskRepositoryTest {

    private static Initialize initialize;
    GatewayRepository gatewayRepository;
    ProjectRepository projectRepository;
    ExperimentRepository experimentRepository;
    ProcessRepository processRepository;
    TaskRepository taskRepository;
    private static final Logger logger = LoggerFactory.getLogger(JobRepositoryTest.class);

    @Before
    public void setUp() {
        try {
            initialize = new Initialize("expcatalog-derby.sql");
            initialize.initializeDB();
            gatewayRepository = new GatewayRepository();
            projectRepository = new ProjectRepository();
            experimentRepository = new ExperimentRepository();
            processRepository = new ProcessRepository();
            taskRepository = new TaskRepository();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("********** TEAR DOWN ************");
        initialize.stopDerbyServer();
    }

    @Test
    public void TaskRepositoryTest() throws RegistryException {
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
        assertTrue(processRepository.getProcess(processId).getTasks().size() == 1);

        taskModel.setTaskType(TaskTypes.MONITORING);
        taskRepository.updateTask(taskModel, taskId);
        assertEquals(TaskTypes.MONITORING, taskRepository.getTask(taskId).getTaskType());

        TaskStatus taskStatus = new TaskStatus(TaskState.EXECUTING);
        String taskStatusId = taskRepository.addTaskStatus(taskStatus, taskId);
        assertTrue(taskStatusId != null);

        taskStatus.setState(TaskState.CREATED);
        taskRepository.updateTaskStatus(taskStatus, taskId);

        TaskStatus retrievedTaskStatus = taskRepository.getTaskStatus(taskId);
        assertEquals(TaskState.CREATED, retrievedTaskStatus.getState());

        ErrorModel errorModel = new ErrorModel();
        errorModel.setErrorId("error");

        assertEquals(taskId, taskRepository.addTaskError(errorModel, taskId));

        errorModel.setActualErrorMessage("message");
        taskRepository.updateTaskError(errorModel, taskId);

        List<ErrorModel> retrievedErrorList = taskRepository.getTaskError(taskId);
        assertTrue(retrievedErrorList.size() == 1);
        assertEquals("message", retrievedErrorList.get(0).getActualErrorMessage());

        List<String> taskIdList = taskRepository.getTaskIds(DBConstants.Task.PARENT_PROCESS_ID, processId);
        assertTrue(taskIdList.size() == 1);
        assertTrue(taskIdList.get(0).equals(taskId));

        experimentRepository.removeExperiment(experimentId);
        processRepository.removeProcess(processId);
        taskRepository.removeTask(taskId);
        assertFalse(taskRepository.isTaskExist(taskId));

        gatewayRepository.removeGateway(gatewayId);
        projectRepository.removeProject(projectId);
    }

}
