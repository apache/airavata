package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.status.TaskState;
import org.apache.airavata.model.status.TaskStatus;
import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.registry.core.entities.expcatalog.TaskStatusEntity;
import org.apache.airavata.registry.core.entities.expcatalog.TaskStatusPK;
import org.apache.airavata.registry.core.utils.ExpCatalogUtils;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.cpi.RegistryException;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.List;

public class TaskStatusRepository extends ExpCatAbstractRepository<TaskStatus, TaskStatusEntity, TaskStatusPK> {
    private final static Logger logger = LoggerFactory.getLogger(TaskStatusRepository.class);

    public TaskStatusRepository() { super(TaskStatus.class, TaskStatusEntity.class); }

    protected String saveTaskStatus(TaskStatus taskStatus, String taskId) throws RegistryException {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        TaskStatusEntity taskStatusEntity = mapper.map(taskStatus, TaskStatusEntity.class);

        if (taskStatusEntity.getTaskId() == null) {
            logger.debug("Setting the TaskStatusEntity's TaskId");
            taskStatusEntity.setTaskId(taskId);
        }

        execute(entityManager -> entityManager.merge(taskStatusEntity));
        return taskStatusEntity.getStatusId();
    }

    public String addTaskStatus(TaskStatus taskStatus, String taskId) throws RegistryException {

        if (taskStatus.getStatusId() == null) {
            logger.debug("Setting the TaskStatus's StatusId");
            taskStatus.setStatusId(ExpCatalogUtils.getID("TASK_STATE"));
        }

        return saveTaskStatus(taskStatus, taskId);
    }

    public String updateTaskStatus(TaskStatus updatedTaskStatus, String taskId) throws RegistryException {
        return saveTaskStatus(updatedTaskStatus, taskId);
    }

    public TaskStatus getTaskStatus(String taskId) throws RegistryException {
        TaskRepository taskRepository = new TaskRepository();
        TaskModel taskModel = taskRepository.getTask(taskId);
        List<TaskStatus> taskStatusList = taskModel.getTaskStatuses();

        if(taskStatusList.size() == 0) {
            logger.debug("TaskStatus list is empty");
            return null;
        }

        else {
            TaskStatus latestTaskStatus = taskStatusList.get(0);

            for(int i = 1; i < taskStatusList.size(); i++) {
                Timestamp timeOfStateChange = new Timestamp(taskStatusList.get(i).getTimeOfStateChange());

                if (timeOfStateChange.after(new Timestamp(latestTaskStatus.getTimeOfStateChange()))
                        || (timeOfStateChange.equals(latestTaskStatus.getTimeOfStateChange()) && taskStatusList.get(i).getState().equals(TaskState.COMPLETED.toString()))
                        || (timeOfStateChange.equals(latestTaskStatus.getTimeOfStateChange()) && taskStatusList.get(i).getState().equals(TaskState.FAILED.toString()))
                        || (timeOfStateChange.equals(latestTaskStatus.getTimeOfStateChange()) && taskStatusList.get(i).getState().equals(TaskState.CANCELED.toString()))) {
                    latestTaskStatus = taskStatusList.get(i);
                }

            }

            return latestTaskStatus;
        }
    }

}
