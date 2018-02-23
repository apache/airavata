package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.registry.core.entities.appcatalog.ApplicationInputEntity;
import org.apache.airavata.registry.core.entities.appcatalog.ApplicationInputPK;

public class ApplicationInputRepository extends AppCatAbstractRepository<InputDataObjectType, ApplicationInputEntity, ApplicationInputPK> {

    public ApplicationInputRepository () {
        super(InputDataObjectType.class, ApplicationInputEntity.class);
    }

}
