package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.status.QueueStatusModel;
import org.apache.airavata.registry.core.entities.expcatalog.QueueStatusEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueueStatusRepository extends ExpCatAbstractRepository<QueueStatusModel, QueueStatusEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(QueueStatusRepository.class);

    public QueueStatusRepository() { super(QueueStatusModel.class, QueueStatusEntity.class); }
}
