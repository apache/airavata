package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.gatewayprofile.StoragePreference;
import org.apache.airavata.registry.core.entities.appcatalog.StoragePreferenceEntity;
import org.apache.airavata.registry.core.entities.appcatalog.StoragePreferencePK;
import org.apache.airavata.registry.core.repositories.AbstractRepository;

public class StoragePrefRepository extends AbstractRepository<StoragePreference, StoragePreferenceEntity, StoragePreferencePK> {

    public StoragePrefRepository() {
        super(StoragePreference.class, StoragePreferenceEntity.class);
    }
}
