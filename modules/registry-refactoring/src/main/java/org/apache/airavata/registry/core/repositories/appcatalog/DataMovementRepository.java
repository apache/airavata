package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.data.movement.DataMovementInterface;
import org.apache.airavata.registry.core.entities.appcatalog.ComputeResourceEntity;
import org.apache.airavata.registry.core.entities.appcatalog.DataMovementInterfaceEntity;
import org.apache.airavata.registry.core.entities.appcatalog.DataMovementInterfacePK;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.dozer.Mapper;

public class DataMovementRepository extends AppCatAbstractRepository<DataMovementInterface, DataMovementInterfaceEntity, DataMovementInterfacePK> {

    public DataMovementRepository() {
        super(DataMovementInterface.class, DataMovementInterfaceEntity.class);
    }

    public String addDataMovementProtocol(String resourceId, DataMovementInterface dataMovementInterface) {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        DataMovementInterfaceEntity dataMovementInterfaceEntity = mapper.map(dataMovementInterface, DataMovementInterfaceEntity.class);
        ComputeResourceEntity computeResourceEntity = mapper.map(new ComputeResourceRepository().get(resourceId), ComputeResourceEntity.class);
        dataMovementInterfaceEntity.setComputeResource(computeResourceEntity);
        dataMovementInterfaceEntity.setComputeResourceId(resourceId);
        execute(entityManager -> entityManager.merge(dataMovementInterfaceEntity));
        return dataMovementInterfaceEntity.getDataMovementInterfaceId();
    }
}
