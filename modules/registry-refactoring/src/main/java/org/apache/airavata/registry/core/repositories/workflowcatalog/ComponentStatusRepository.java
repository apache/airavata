package org.apache.airavata.registry.core.repositories.workflowcatalog;

import org.apache.airavata.model.ComponentStatus;
import org.apache.airavata.registry.core.entities.workflowcatalog.ComponentStatusEntity;
import org.apache.airavata.registry.core.repositories.AbstractRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by abhij on 9/28/2016.
 */
public class ComponentStatusRepository extends AbstractRepository<ComponentStatus, ComponentStatusEntity, String> {


    private final static Logger logger = LoggerFactory.getLogger(ComponentStatusRepository.class);

    public ComponentStatusRepository(Class<ComponentStatus> thriftGenericClass, Class<ComponentStatusEntity> dbEntityGenericClass) {
        super(thriftGenericClass, dbEntityGenericClass);
    }
}
