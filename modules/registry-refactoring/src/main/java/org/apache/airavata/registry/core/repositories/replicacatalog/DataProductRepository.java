package org.apache.airavata.registry.core.repositories.replicacatalog;

import org.apache.airavata.model.data.replica.DataProductModel;
import org.apache.airavata.registry.core.entities.replicacatalog.DataProductEntity;
import org.apache.airavata.registry.core.repositories.AbstractRepository;

/**
 * Created by abhij on 10/13/2016.
 */
public class DataProductRepository extends AbstractRepository<DataProductModel, DataProductEntity, String> {
    public DataProductRepository(Class<DataProductModel> thriftGenericClass, Class<DataProductEntity> dbEntityGenericClass) {
        super(thriftGenericClass, dbEntityGenericClass);
    }
}
