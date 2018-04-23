package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.status.QueueStatusModel;
import org.apache.airavata.registry.core.entities.expcatalog.QueueStatusEntity;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.core.utils.QueryConstants;
import org.apache.airavata.registry.cpi.RegistryException;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class QueueStatusRepository extends ExpCatAbstractRepository<QueueStatusModel, QueueStatusEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(QueueStatusRepository.class);

    public QueueStatusRepository() { super(QueueStatusModel.class, QueueStatusEntity.class); }

    public boolean createQueueStatuses(List<QueueStatusModel> queueStatusModels) throws RegistryException {

        for (QueueStatusModel queueStatusModel : queueStatusModels) {
            Mapper mapper = ObjectMapperSingleton.getInstance();
            QueueStatusEntity queueStatusEntity = mapper.map(queueStatusModel, QueueStatusEntity.class);
            execute(entityManager -> entityManager.merge(queueStatusEntity));
        }

        return true;
    }

    public List<QueueStatusModel> getLatestQueueStatuses() throws RegistryException {
        List<QueueStatusModel> queueStatusModelList = select(QueryConstants.GET_ALL_QUEUE_STATUS_MODELS, 0);
        return queueStatusModelList;
    }

}
