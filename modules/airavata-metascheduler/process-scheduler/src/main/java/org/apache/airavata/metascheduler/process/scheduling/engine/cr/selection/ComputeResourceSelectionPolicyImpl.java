package org.apache.airavata.metascheduler.process.scheduling.engine.cr.selection;

import org.apache.airavata.common.utils.ThriftClientPool;
import org.apache.airavata.metascheduler.core.engine.ComputeResourceSelectionPolicy;
import org.apache.airavata.metascheduler.core.utils.Utils;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupComputeResourcePreference;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.RegistryService.Client;

public abstract class ComputeResourceSelectionPolicyImpl implements ComputeResourceSelectionPolicy {

    protected ThriftClientPool<RegistryService.Client> registryClientPool;

    public ComputeResourceSelectionPolicyImpl() {
        this.registryClientPool = Utils.getRegistryServiceClientPool();
    }


    public GroupComputeResourcePreference getGroupComputeResourcePreference(String computeResourcId, String groupResourceProfileId) throws Exception {
        RegistryService.Client client = this.registryClientPool.getResource();
        try {
            if (client.isGroupComputeResourcePreferenceExists(computeResourcId,
                    groupResourceProfileId)) {
                return client.getGroupComputeResourcePreference(
                        computeResourcId,
                        groupResourceProfileId);
            }
            return null;
        } finally {
            this.registryClientPool.returnResource(client);
        }
    }

}
