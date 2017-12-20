package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.registry.core.entities.appcatalog.ComputeResourceEntity;
import org.apache.airavata.registry.core.entities.appcatalog.ComputeResourcePreferencePK;
import org.apache.airavata.registry.core.repositories.AbstractRepository;

public class ComputeResourceRepository extends AbstractRepository<ComputeResourcePreference, ComputeResourceEntity, ComputeResourcePreferencePK> {

    public ComputeResourceRepository() {
        super(ComputeResourcePreference.class, ComputeResourceEntity.class);
    }

}
