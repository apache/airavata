package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.data.movement.DataMovementInterface;
import org.apache.airavata.registry.core.entities.appcatalog.DataMovementInterfaceEntity;
import org.apache.airavata.registry.core.entities.appcatalog.DataMovementInterfacePK;

public class DataMovementRepository extends AppCatAbstractRepository<DataMovementInterface, DataMovementInterfaceEntity, DataMovementInterfacePK> {

    public DataMovementRepository() {
        super(DataMovementInterface.class, DataMovementInterfaceEntity.class);
    }
}
