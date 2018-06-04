package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.registry.core.entities.expcatalog.TaskErrorEntity;
import org.apache.airavata.registry.core.entities.expcatalog.TaskErrorPK;
import org.apache.airavata.registry.core.utils.ExpCatalogUtils;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.cpi.RegistryException;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TaskErrorRepository extends ExpCatAbstractRepository<ErrorModel, TaskErrorEntity, TaskErrorPK> {
    private final static Logger logger = LoggerFactory.getLogger(TaskErrorRepository.class);

    public TaskErrorRepository() { super(ErrorModel.class, TaskErrorEntity.class); }

    protected String saveTaskError(ErrorModel error, String taskId) throws RegistryException {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        TaskErrorEntity taskErrorEntity = mapper.map(error, TaskErrorEntity.class);

        if (taskErrorEntity.getTaskId() == null) {
            logger.debug("Setting the TaskErrorEntity's TaskId");
            taskErrorEntity.setTaskId(taskId);
        }

        execute(entityManager -> entityManager.merge(taskErrorEntity));
        return taskErrorEntity.getErrorId();
    }

    public String addTaskError(ErrorModel taskError, String taskId) throws RegistryException {

        if (taskError.getErrorId() == null) {
            logger.debug("Setting the TaskError's ErrorId");
            taskError.setErrorId(ExpCatalogUtils.getID("ERROR"));
        }

        return saveTaskError(taskError, taskId);
    }

    public String updateTaskError(ErrorModel updatedTaskError, String taskId) throws RegistryException {
        return saveTaskError(updatedTaskError, taskId);
    }

    public List<ErrorModel> getTaskError(String taskId) throws RegistryException {
        TaskRepository taskRepository = new TaskRepository();
        TaskModel taskModel = taskRepository.getTask(taskId);
        return taskModel.getTaskErrors();
    }

}
