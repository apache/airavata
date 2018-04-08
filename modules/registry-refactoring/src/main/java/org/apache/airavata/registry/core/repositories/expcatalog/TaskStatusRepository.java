package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.status.TaskStatus;
import org.apache.airavata.registry.core.entities.expcatalog.TaskStatusEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskStatusRepository extends ExpCatAbstractRepository<TaskStatus, TaskStatusEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(TaskStatusRepository.class);

    public TaskStatusRepository() { super(TaskStatus.class, TaskStatusEntity.class); }
}
