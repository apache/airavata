package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.registry.core.entities.appcatalog.ComputeResourcePreferenceEntity;
import org.apache.airavata.registry.core.entities.appcatalog.ComputeResourcePreferencePK;
import org.apache.airavata.registry.core.entities.appcatalog.SSHAccountProvisionerConfiguration;

import java.util.HashMap;
import java.util.Map;

public class ComputeResourcePrefRepository extends AppCatAbstractRepository<ComputeResourcePreference, ComputeResourcePreferenceEntity, ComputeResourcePreferencePK> {

    public ComputeResourcePrefRepository() {
        super(ComputeResourcePreference.class, ComputeResourcePreferenceEntity.class);
    }

    public Map<String,String> getsshAccountProvisionerConfig(String gatewayId, String hostId) {
        ComputeResourcePreferencePK computeResourcePreferencePK = new ComputeResourcePreferencePK();
        computeResourcePreferencePK.setGatewayId(gatewayId);
        computeResourcePreferencePK.setComputeResourceId(hostId);
        ComputeResourcePreferenceEntity computeResourcePreferenceEntity = execute(entityManager -> entityManager
                .find(ComputeResourcePreferenceEntity.class, computeResourcePreferencePK));
        if (computeResourcePreferenceEntity.getSshAccountProvisionerConfigurations()!= null && !computeResourcePreferenceEntity.getSshAccountProvisionerConfigurations().isEmpty()){
            Map<String,String> sshAccountProvisionerConfigurations = new HashMap<>();
            for (SSHAccountProvisionerConfiguration config : computeResourcePreferenceEntity.getSshAccountProvisionerConfigurations()){
                sshAccountProvisionerConfigurations.put(config.getConfigName(), config.getConfigValue());
            }
            return sshAccountProvisionerConfigurations;
        }
        return null;
    }
}
