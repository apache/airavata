package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.registry.core.entities.expcatalog.TaskEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskRepository extends ExpCatAbstractRepository<TaskModel, TaskEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(TaskRepository.class);

    public TaskRepository() { super(TaskModel.class, TaskEntity.class); }
}
