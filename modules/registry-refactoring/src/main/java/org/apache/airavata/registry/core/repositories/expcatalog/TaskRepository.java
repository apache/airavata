package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.status.TaskStatus;
import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.registry.core.entities.expcatalog.TaskEntity;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.core.utils.ExpCatalogUtils;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.core.utils.QueryConstants;
import org.apache.airavata.registry.cpi.RegistryException;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.*;

public class TaskRepository extends ExpCatAbstractRepository<TaskModel, TaskEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(TaskRepository.class);

    public TaskRepository() { super(TaskModel.class, TaskEntity.class); }

    protected String saveTaskModelData(TaskModel taskModel) throws RegistryException {
        TaskEntity taskEntity = saveTask(taskModel);
        return taskEntity.getTaskId();
    }

    protected TaskEntity saveTask(TaskModel taskModel) throws RegistryException {
        if (taskModel.getTaskId() == null || taskModel.getTaskId().equals("DO_NOT_SET_AT_CLIENTS")) {
            logger.debug("Setting the Task's TaskId");
            taskModel.setTaskId(ExpCatalogUtils.getID("TASK"));
        }

        String taskId = taskModel.getTaskId();
        Mapper mapper = ObjectMapperSingleton.getInstance();
        TaskEntity taskEntity = mapper.map(taskModel, TaskEntity.class);

        if (taskEntity.getTaskStatuses() != null) {
            logger.debug("Populating the Primary Key of TaskStatus objects for the Task");
            taskEntity.getTaskStatuses().forEach(taskStatusEntity -> { taskStatusEntity.setTaskId(taskId);
                if (taskStatusEntity.getStatusId() == null) {
                    taskStatusEntity.setStatusId(ExpCatalogUtils.getID("STATUS"));
                }
            });
        }

        if (taskEntity.getTaskErrors() != null) {
            logger.debug("Populating the Primary Key of TaskError objects for the Task");
            taskEntity.getTaskErrors().forEach(taskErrorEntity -> taskErrorEntity.setTaskId(taskId));
        }

        if (taskEntity.getJobs() != null) {
            logger.debug("Populating the Job objects' Task ID for the Task");
            taskEntity.getJobs().forEach(jobEntity -> jobEntity.setTaskId(taskId));
        }

        if (!isTaskExist(taskId)) {
            logger.debug("Checking if the Task already exists");
            taskEntity.setCreationTime(new Timestamp((System.currentTimeMillis())));
        }

        taskEntity.setLastUpdateTime(new Timestamp((System.currentTimeMillis())));
        return execute(entityManager -> entityManager.merge(taskEntity));
    }

    public String addTask(TaskModel task, String processId) throws RegistryException {
        task.setParentProcessId(processId);
        String taskId = saveTaskModelData(task);
        ProcessRepository processRepository = new ProcessRepository();
        ProcessModel processModel = processRepository.getProcess(processId);
        List<TaskModel> taskModelList = processModel.getTasks();
        System.out.println("*****"+taskModelList.size());

        if (taskModelList != null && !taskModelList.contains(task)) {
            logger.debug("Adding the Task to the list");
            taskModelList.add(task);
            System.out.println("*****"+taskModelList.size());
            processModel.setTasks(taskModelList);
            processRepository.updateProcess(processModel, processId);
        }

        return taskId;
    }

    public String updateTask(TaskModel task, String taskId) throws RegistryException {
        return saveTaskModelData(task);
    }

    public TaskModel getTask(String taskId) throws RegistryException {
        TaskRepository taskRepository = new TaskRepository();
        return taskRepository.get(taskId);
    }

    public String addTaskStatus(TaskStatus taskStatus, String taskId) throws RegistryException {
        TaskModel taskModel = getTask(taskId);
        List<TaskStatus> taskStatusList = taskModel.getTaskStatuses();

        if (taskStatusList == null) {
            logger.debug("Adding the first TaskStatus to the list");
            taskModel.setTaskStatuses(Arrays.asList(taskStatus));
        }

        else if (!taskStatusList.contains(taskStatus)) {
            logger.debug("Adding the TaskStatus to the list");
            taskStatusList.add(taskStatus);
            taskModel.setTaskStatuses(taskStatusList);
        }

        updateTask(taskModel, taskId);
        return taskId;
    }

    public String updateTaskStatus(TaskStatus taskStatus, String taskId) throws RegistryException {
        return addTaskStatus(taskStatus, taskId);
    }

    public List<TaskStatus> getTaskStatus(String taskId) throws RegistryException {
        TaskModel taskModel = getTask(taskId);
        return taskModel.getTaskStatuses();
    }

    public String addTaskError(ErrorModel taskError, String taskId) throws RegistryException {
        TaskModel taskModel = getTask(taskId);
        List<ErrorModel> errorModelList = taskModel.getTaskErrors();

        if (errorModelList == null) {
            logger.debug("Adding the first TaskError to the list");
            taskModel.setTaskErrors(Arrays.asList(taskError));
        }

        else if (!errorModelList.contains(taskError)) {
            logger.debug("Adding the TaskError to the list");
            errorModelList.add(taskError);
            taskModel.setTaskErrors(errorModelList);
        }

        updateTask(taskModel, taskId);
        return taskId;
    }

    public String updateTaskError(ErrorModel taskError, String taskId) throws RegistryException {
        return addTaskError(taskError, taskId);
    }

    public List<ErrorModel> getTaskError(String taskId) throws RegistryException {
        TaskModel taskModel = getTask(taskId);
        return taskModel.getTaskErrors();
    }

    public List<TaskModel> getTaskList(String fieldName, Object value) throws RegistryException {
        TaskRepository taskRepository = new TaskRepository();
        List<TaskModel> taskModelList;

        if (fieldName.equals(DBConstants.Task.PARENT_PROCESS_ID)) {
            logger.debug("Search criteria is ParentProcessId");
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(DBConstants.Task.PARENT_PROCESS_ID, value);
            taskModelList = taskRepository.select(QueryConstants.GET_TASK_FOR_PARENT_PROCESS_ID, -1, 0, queryParameters);
        }

        else {
            logger.error("Unsupported field name for Task module.");
            throw new IllegalArgumentException("Unsupported field name for Task module.");
        }

        return taskModelList;
    }

    public List<String> getTaskIds(String fieldName, Object value) throws RegistryException {
        List<String> taskIds = new ArrayList<>();
        List<TaskModel> taskModelList = getTaskList(fieldName, value);
        for (TaskModel taskModel : taskModelList) {
            taskIds.add(taskModel.getTaskId());
        }
        return taskIds;
    }

    public boolean isTaskExist(String taskId) throws RegistryException {
        return isExists(taskId);
    }

    public void removeTask(String taskId) throws RegistryException {
        delete(taskId);
    }

}
