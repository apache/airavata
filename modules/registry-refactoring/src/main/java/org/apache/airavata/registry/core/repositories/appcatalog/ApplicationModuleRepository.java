package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.appdeployment.ApplicationModule;
import org.apache.airavata.registry.core.entities.appcatalog.ApplicationModuleEntity;

public class ApplicationModuleRepository extends AppCatAbstractRepository<ApplicationModule, ApplicationModuleEntity, String> {

    public ApplicationModuleRepository () {
        super(ApplicationModule.class, ApplicationModuleEntity.class);
    }

}
