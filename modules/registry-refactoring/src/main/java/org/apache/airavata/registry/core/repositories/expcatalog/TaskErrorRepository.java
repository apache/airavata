package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.registry.core.entities.expcatalog.TaskErrorEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskErrorRepository extends ExpCatAbstractRepository<ErrorModel, TaskErrorEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(TaskErrorRepository.class);

    public TaskErrorRepository() { super(ErrorModel.class, TaskErrorEntity.class); }
}
