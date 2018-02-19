package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.computeresource.BatchQueue;
import org.apache.airavata.registry.core.entities.appcatalog.BatchQueueEntity;
import org.apache.airavata.registry.core.entities.appcatalog.BatchQueuePK;

public class BatchQueueRepository extends AppCatAbstractRepository<BatchQueue, BatchQueueEntity, BatchQueuePK> {

    public BatchQueueRepository() {
        super(BatchQueue.class, BatchQueueEntity.class);
    }
}
