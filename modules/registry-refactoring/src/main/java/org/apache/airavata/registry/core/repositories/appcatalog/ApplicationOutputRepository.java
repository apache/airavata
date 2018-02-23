package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.registry.core.entities.appcatalog.ApplicationOutputEntity;
import org.apache.airavata.registry.core.entities.appcatalog.ApplicationOutputPK;

public class ApplicationOutputRepository extends AppCatAbstractRepository<OutputDataObjectType, ApplicationOutputEntity, ApplicationOutputPK> {

    public ApplicationOutputRepository () {
        super(OutputDataObjectType.class, ApplicationOutputEntity.class);
    }

}
