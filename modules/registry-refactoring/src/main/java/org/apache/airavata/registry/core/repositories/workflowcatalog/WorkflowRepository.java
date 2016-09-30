package org.apache.airavata.registry.core.repositories.workflowcatalog;

import org.apache.airavata.model.WorkflowModel;
import org.apache.airavata.registry.core.entities.workflowcatalog.WorkflowEntity;
import org.apache.airavata.registry.core.repositories.AbstractRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by abhijit on 9/28/2016.
 */
public class WorkflowRepository extends AbstractRepository<WorkflowModel, WorkflowEntity, String> {

    private final static Logger logger = LoggerFactory.getLogger(WorkflowRepository.class);


    public WorkflowRepository(Class<WorkflowModel> thriftGenericClass, Class<WorkflowEntity> dbEntityGenericClass) {
        super(thriftGenericClass, dbEntityGenericClass);
    }
}
