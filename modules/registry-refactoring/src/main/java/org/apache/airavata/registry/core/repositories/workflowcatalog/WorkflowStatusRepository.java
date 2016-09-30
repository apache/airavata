package org.apache.airavata.registry.core.repositories.workflowcatalog;

import org.apache.airavata.model.WorkflowStatus;
import org.apache.airavata.registry.core.entities.workflowcatalog.WorkflowStatusEntity;
import org.apache.airavata.registry.core.entities.workflowcatalog.WorkflowStatusPK;
import org.apache.airavata.registry.core.repositories.AbstractRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by abhijit on 9/28/2016.
 */
public class WorkflowStatusRepository extends AbstractRepository<WorkflowStatus, WorkflowStatusEntity, WorkflowStatusPK> {

    private final static Logger logger = LoggerFactory.getLogger(WorkflowStatusRepository.class);

    public WorkflowStatusRepository(Class<WorkflowStatus> thriftGenericClass, Class<WorkflowStatusEntity> dbEntityGenericClass) {
        super(thriftGenericClass, dbEntityGenericClass);
    }
}
