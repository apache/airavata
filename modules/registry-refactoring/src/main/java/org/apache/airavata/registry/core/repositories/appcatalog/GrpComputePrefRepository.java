package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupComputeResourcePreference;
import org.apache.airavata.registry.core.entities.appcatalog.GroupComputeResourcePrefEntity;
import org.apache.airavata.registry.core.entities.appcatalog.GroupComputeResourcePrefPK;

/**
 * Created by skariyat on 2/10/18.
 */
public class GrpComputePrefRepository extends AppCatAbstractRepository<GroupComputeResourcePreference, GroupComputeResourcePrefEntity, GroupComputeResourcePrefPK> {

    public GrpComputePrefRepository() {
        super(GroupComputeResourcePreference.class, GroupComputeResourcePrefEntity.class);
    }
}
