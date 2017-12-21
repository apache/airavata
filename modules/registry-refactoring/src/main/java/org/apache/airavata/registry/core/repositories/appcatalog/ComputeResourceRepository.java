package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.registry.core.entities.appcatalog.ComputeResourceEntity;
import org.apache.airavata.registry.core.entities.appcatalog.ComputeResourcePreferenceEntity;
import org.apache.airavata.registry.core.entities.appcatalog.ComputeResourcePreferencePK;
import org.apache.airavata.registry.core.repositories.AbstractRepository;

public class ComputeResourceRepository extends AppCatAbstractRepository<ComputeResourcePreference, ComputeResourcePreferenceEntity, ComputeResourcePreferencePK> {

    public ComputeResourceRepository() {
        super(ComputeResourcePreference.class, ComputeResourcePreferenceEntity.class);
    }
}
