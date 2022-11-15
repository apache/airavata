package org.apache.airavata.compute.resource.monitoring.job;

import org.apache.airavata.common.utils.ThriftClientPool;
import org.apache.airavata.metascheduler.core.engine.ComputeResourceSelectionPolicy;
import org.apache.airavata.metascheduler.core.utils.Utils;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupResourceProfile;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserResourceProfile;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.RegistryService.Client;

public abstract class ComputeResourceMonitor {

    protected ThriftClientPool<RegistryService.Client> registryClientPool;

    public ComputeResourceMonitor() {
        this.registryClientPool = Utils.getRegistryServiceClientPool();
    }

    private boolean isValid(String str) {
        return str != null && !str.trim().isEmpty();
    }

    public UserResourceProfile getUserResourceProfile(String username, String gatewayId) throws Exception {
        RegistryService.Client client = this.registryClientPool.getResource();
        try {
            if (client.isUserResourceProfileExists(username, gatewayId)) {
                return client.getUserResourceProfile(username, gatewayId);
            }
            return null;
        } finally {
            this.registryClientPool.returnResource(client);
        }

    }


    private UserComputeResourcePreference getUserComputeResourcePreference(String gatewayId, String username,
                                                                           String computeResourceId) throws Exception {
        RegistryService.Client client = this.registryClientPool.getResource();
        try {
            if (client.isUserComputeResourcePreferenceExists(username, gatewayId, computeResourceId)){
                return  client.getUserComputeResourcePreference(username, gatewayId, computeResourceId);
            }
            return null;
        } finally {
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

    public GroupResourceProfile getGroupResourceProfile(String groupResourceProfileId) throws Exception {
        RegistryService.Client client = this.registryClientPool.getResource();
        try {
            if (client.isGroupResourceProfileExists(groupResourceProfileId)) {
                return client.getGroupResourceProfile(groupResourceProfileId);
            }
            return null;
        } finally {
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
                isValid(getGroupComputeResourcePreference(computeResourceId, groupResourceProfileId).getLoginUserName())) {
            return getGroupComputeResourcePreference(computeResourceId, groupResourceProfileId).getLoginUserName();
        }
        throw new RuntimeException("Can't find login username for compute resource");
    }



}
