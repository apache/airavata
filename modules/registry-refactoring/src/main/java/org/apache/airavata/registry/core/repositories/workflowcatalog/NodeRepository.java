package org.apache.airavata.registry.core.repositories.workflowcatalog;

import org.apache.airavata.model.NodeModel;
import org.apache.airavata.registry.core.entities.workflowcatalog.NodeEntity;
import org.apache.airavata.registry.core.entities.workflowcatalog.NodePK;
import org.apache.airavata.registry.core.repositories.AbstractRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by abhij on 9/28/2016.
 */
public class NodeRepository extends AbstractRepository<NodeModel, NodeEntity, NodePK> {


    private final static Logger logger = LoggerFactory.getLogger(NodeRepository.class);

    public NodeRepository(Class<NodeModel> thriftGenericClass, Class<NodeEntity> dbEntityGenericClass) {
        super(thriftGenericClass, dbEntityGenericClass);
    }
}
