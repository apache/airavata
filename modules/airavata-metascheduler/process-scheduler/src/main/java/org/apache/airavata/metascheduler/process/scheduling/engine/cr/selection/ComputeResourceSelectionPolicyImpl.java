package org.apache.airavata.metascheduler.process.scheduling.engine.cr.selection;

import org.apache.airavata.common.utils.ThriftClientPool;
import org.apache.airavata.metascheduler.core.engine.ComputeResourceSelectionPolicy;
import org.apache.airavata.metascheduler.core.utils.Utils;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupResourceProfile;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserResourceProfile;

import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.RegistryService.Client;
import org.apache.airavata.registry.api.exception.RegistryServiceException;

public abstract class ComputeResourceSelectionPolicyImpl implements ComputeResourceSelectionPolicy {

    protected ThriftClientPool<RegistryService.Client> registryClientPool;

    public ComputeResourceSelectionPolicyImpl() {
        this.registryClientPool =Utils.getRegistryServiceClientPool();
    }

    private boolean isValid(String str) {
        return str != null && !str.trim().isEmpty();
    }

    public UserResourceProfile getUserResourceProfile(String username, String gatewayId) throws Exception {
        RegistryService.Client client = this.registryClientPool.getResource();
        try {
            return client.getUserResourceProfile(username, gatewayId);
        }finally {
            this.registryClientPool.returnResource(client);
        }

    }


    private UserComputeResourcePreference getUserComputeResourcePreference(String gatewayId, String username,
                                                                           String computeResourceId) throws Exception {
        RegistryService.Client client = this.registryClientPool.getResource();
        try {
            return client.getUserComputeResourcePreference(username, gatewayId, computeResourceId);
        }finally {
            this.registryClientPool.returnResource(client);
        }
    }

    public String getComputeResourceCredentialToken(String gatewayId,
                                                    String username, String computeResourceId, boolean isUseUserCRPref,
                                                    boolean isSetGroupResourceProfileId, String groupResourceProfileId) throws Exception {
        if (isUseUserCRPref) {
            if (getUserComputeResourcePreference(gatewayId, username, computeResourceId) != null &&
                    isValid(getUserComputeResourcePreference(gatewayId, username, computeResourceId).getResourceSpecificCredentialStoreToken())) {
                return getUserComputeResourcePreference(gatewayId, username, computeResourceId).getResourceSpecificCredentialStoreToken();
            } else {
                return getUserResourceProfile(username, gatewayId).getCredentialStoreToken();
            }
        } else if (isSetGroupResourceProfileId &&
                getGroupComputeResourcePreference(computeResourceId, groupResourceProfileId) != null &&
                isValid(getGroupComputeResourcePreference(computeResourceId, groupResourceProfileId).getResourceSpecificCredentialStoreToken())) {
            return getGroupComputeResourcePreference(computeResourceId, groupResourceProfileId).getResourceSpecificCredentialStoreToken();
        } else {
            return getGroupResourceProfile(groupResourceProfileId).getDefaultCredentialStoreToken();
        }
    }

    public GroupComputeResourcePreference getGroupComputeResourcePreference(String computeResourcId, String groupResourceProfileId) throws Exception {
        RegistryService.Client client = this.registryClientPool.getResource();
        try {
            return client.getGroupComputeResourcePreference(
                    computeResourcId,
                    groupResourceProfileId);
        }finally {
            this.registryClientPool.returnResource(client);
        }

    }

    public GroupResourceProfile getGroupResourceProfile(String groupResourceProfileId) throws Exception {
        RegistryService.Client client = this.registryClientPool.getResource();
        try {
            return client.getGroupResourceProfile(groupResourceProfileId);
        }finally {
            this.registryClientPool.returnResource(client);
        }

    }

    public String getComputeResourceLoginUserName(String gatewayId,
                                                  String username, String computeResourceId, boolean isUseUserCRPref,
                                                  boolean isSetGroupResourceProfileId, String groupResourceProfileId,
                                                  String overrideLoginUsername) throws Exception {
        if (isUseUserCRPref &&
                getUserComputeResourcePreference(gatewayId, username, computeResourceId) != null &&
                isValid(getUserComputeResourcePreference(gatewayId, username, computeResourceId).getLoginUserName())) {
            return getUserComputeResourcePreference(gatewayId, username, computeResourceId).getLoginUserName();
        } else if (isValid(overrideLoginUsername)) {
            return overrideLoginUsername;
        } else if (isSetGroupResourceProfileId &&
                getGroupComputeResourcePreference(computeResourceId, groupResourceProfileId) != null &&
                isValid(getGroupComputeResourcePreference(computeResourceId, groupResourceProfileId).getLoginUserName())){
            return getGroupComputeResourcePreference(computeResourceId, groupResourceProfileId).getLoginUserName();
        }
        throw new RuntimeException("Can't find login username for compute resource");
    }


}
