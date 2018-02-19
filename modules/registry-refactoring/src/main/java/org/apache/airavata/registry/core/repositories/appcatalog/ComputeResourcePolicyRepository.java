package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.groupresourceprofile.ComputeResourcePolicy;
import org.apache.airavata.registry.core.entities.appcatalog.ComputeResourcePolicyEntity;
import org.apache.airavata.registry.core.entities.appcatalog.ComputeResourcePolicyPK;

/**
 * Created by skariyat on 2/10/18.
 */
public class ComputeResourcePolicyRepository extends AppCatAbstractRepository<ComputeResourcePolicy, ComputeResourcePolicyEntity, ComputeResourcePolicyPK>{

    public ComputeResourcePolicyRepository() {
        super(ComputeResourcePolicy.class, ComputeResourcePolicyEntity.class);
    }
}
