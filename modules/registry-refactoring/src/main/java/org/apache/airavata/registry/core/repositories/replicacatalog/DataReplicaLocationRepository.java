package org.apache.airavata.registry.core.repositories.replicacatalog;

import org.apache.airavata.model.data.replica.DataReplicaLocationModel;
import org.apache.airavata.registry.core.entities.replicacatalog.DataReplicaLocationEntity;
import org.apache.airavata.registry.core.repositories.AbstractRepository;

/**
 * Created by abhij on 10/13/2016.
 */
public class DataReplicaLocationRepository extends AbstractRepository<DataReplicaLocationModel, DataReplicaLocationEntity, String> {
    public DataReplicaLocationRepository(Class<DataReplicaLocationModel> thriftGenericClass, Class<DataReplicaLocationEntity> dbEntityGenericClass) {
        super(thriftGenericClass, dbEntityGenericClass);
    }
}
