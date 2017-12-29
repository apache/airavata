package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.registry.core.entities.appcatalog.ComputeResourcePreferenceEntity;
import org.apache.airavata.registry.core.entities.appcatalog.ComputeResourcePreferencePK;

public class ComputeResourcePrefRepository extends AppCatAbstractRepository<ComputeResourcePreference, ComputeResourcePreferenceEntity, ComputeResourcePreferencePK> {

    public ComputeResourcePrefRepository() {
        super(ComputeResourcePreference.class, ComputeResourcePreferenceEntity.class);
    }
}
