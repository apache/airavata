package org.apache.airavata.registry.core.repositories.workflowcatalog;

import org.apache.airavata.model.PortModel;
import org.apache.airavata.registry.core.entities.workflowcatalog.PortEntity;
import org.apache.airavata.registry.core.entities.workflowcatalog.PortPK;
import org.apache.airavata.registry.core.repositories.AbstractRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by abhij on 9/28/2016.
 */
public class PortRepository extends AbstractRepository<PortModel, PortEntity, PortPK> {

    private final static Logger logger = LoggerFactory.getLogger(PortRepository.class);

    public PortRepository(Class<PortModel> thriftGenericClass, Class<PortEntity> dbEntityGenericClass) {
        super(thriftGenericClass, dbEntityGenericClass);
    }
}
