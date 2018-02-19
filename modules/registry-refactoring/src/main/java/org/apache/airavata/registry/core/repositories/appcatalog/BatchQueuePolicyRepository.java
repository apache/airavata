package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.groupresourceprofile.BatchQueueResourcePolicy;
import org.apache.airavata.registry.core.entities.appcatalog.BatchQueueResourcePolicyEntity;
import org.apache.airavata.registry.core.entities.appcatalog.BatchQueueResourcePolicyPK;

/**
 * Created by skariyat on 2/10/18.
 */
public class BatchQueuePolicyRepository extends AppCatAbstractRepository<BatchQueueResourcePolicy, BatchQueueResourcePolicyEntity, BatchQueueResourcePolicyPK> {

    public BatchQueuePolicyRepository() {
        super(BatchQueueResourcePolicy.class, BatchQueueResourcePolicyEntity.class);
    }
}
