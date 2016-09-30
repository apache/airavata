package org.apache.airavata.registry.core.repositories.workflowcatalog;

import org.apache.airavata.model.EdgeModel;
import org.apache.airavata.registry.core.entities.workflowcatalog.EdgeEntity;
import org.apache.airavata.registry.core.entities.workflowcatalog.EdgePK;
import org.apache.airavata.registry.core.repositories.AbstractRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by abhij on 9/28/2016.
 */
public class EdgeRepository extends AbstractRepository<EdgeModel, EdgeEntity, EdgePK> {


    private final static Logger logger = LoggerFactory.getLogger(EdgeRepository.class);

    public EdgeRepository(Class<EdgeModel> thriftGenericClass, Class<EdgeEntity> dbEntityGenericClass) {
        super(thriftGenericClass, dbEntityGenericClass);
    }
}
