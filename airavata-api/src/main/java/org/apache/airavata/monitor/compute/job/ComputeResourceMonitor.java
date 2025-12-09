/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.monitor.compute.job;

import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupResourceProfile;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserResourceProfile;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.service.RegistryService;

public abstract class ComputeResourceMonitor {

    protected final RegistryService registryService;

    protected ComputeResourceMonitor(RegistryService registryService) {
        this.registryService = registryService;
    }

    private boolean isValid(String str) {
        return str != null && !str.trim().isEmpty();
    }

    public UserResourceProfile getUserResourceProfile(String username, String gatewayId)
            throws RegistryServiceException {
        if (registryService == null) {
            throw new RegistryServiceException("RegistryService not injected. This class must be managed by Spring.");
        }
        if (registryService.isUserResourceProfileExists(username, gatewayId)) {
            return registryService.getUserResourceProfile(username, gatewayId);
        }
        return null;
    }

    private UserComputeResourcePreference getUserComputeResourcePreference(
            String gatewayId, String username, String computeResourceId) throws RegistryServiceException {
        if (registryService == null) {
            throw new RegistryServiceException("RegistryService not injected. This class must be managed by Spring.");
        }
        if (registryService.isUserComputeResourcePreferenceExists(username, gatewayId, computeResourceId)) {
            return registryService.getUserComputeResourcePreference(username, gatewayId, computeResourceId);
        }
        return null;
    }

    public String getComputeResourceCredentialToken(
            String gatewayId,
            String username,
            String computeResourceId,
            boolean isUseUserCRPref,
            boolean isSetGroupResourceProfileId,
            String groupResourceProfileId)
            throws Exception {
        if (isUseUserCRPref) {
            if (getUserComputeResourcePreference(gatewayId, username, computeResourceId) != null
                    && isValid(getUserComputeResourcePreference(gatewayId, username, computeResourceId)
                            .getResourceSpecificCredentialStoreToken())) {
                return getUserComputeResourcePreference(gatewayId, username, computeResourceId)
                        .getResourceSpecificCredentialStoreToken();
            } else {
                return getUserResourceProfile(username, gatewayId).getCredentialStoreToken();
            }
        } else if (isSetGroupResourceProfileId
                && getGroupComputeResourcePreference(computeResourceId, groupResourceProfileId) != null
                && isValid(getGroupComputeResourcePreference(computeResourceId, groupResourceProfileId)
                        .getResourceSpecificCredentialStoreToken())) {
            return getGroupComputeResourcePreference(computeResourceId, groupResourceProfileId)
                    .getResourceSpecificCredentialStoreToken();
        } else {
            return getGroupResourceProfile(groupResourceProfileId).getDefaultCredentialStoreToken();
        }
    }

    public GroupComputeResourcePreference getGroupComputeResourcePreference(
            String computeResourcId, String groupResourceProfileId) throws RegistryServiceException {
        if (registryService == null) {
            throw new RegistryServiceException("RegistryService not injected. This class must be managed by Spring.");
        }
        if (registryService.isGroupComputeResourcePreferenceExists(computeResourcId, groupResourceProfileId)) {
            return registryService.getGroupComputeResourcePreference(computeResourcId, groupResourceProfileId);
        }
        return null;
    }

    public GroupResourceProfile getGroupResourceProfile(String groupResourceProfileId) throws RegistryServiceException {
        if (registryService == null) {
            throw new RegistryServiceException("RegistryService not injected. This class must be managed by Spring.");
        }
        if (registryService.isGroupResourceProfileExists(groupResourceProfileId)) {
            return registryService.getGroupResourceProfile(groupResourceProfileId);
        }
        return null;
    }

    public String getComputeResourceLoginUserName(
            String gatewayId,
            String username,
            String computeResourceId,
            boolean isUseUserCRPref,
            boolean isSetGroupResourceProfileId,
            String groupResourceProfileId,
            String overrideLoginUsername)
            throws Exception {
        if (isUseUserCRPref
                && getUserComputeResourcePreference(gatewayId, username, computeResourceId) != null
                && isValid(getUserComputeResourcePreference(gatewayId, username, computeResourceId)
                        .getLoginUserName())) {
            return getUserComputeResourcePreference(gatewayId, username, computeResourceId)
                    .getLoginUserName();
        } else if (isValid(overrideLoginUsername)) {
            return overrideLoginUsername;
        } else if (isSetGroupResourceProfileId
                && getGroupComputeResourcePreference(computeResourceId, groupResourceProfileId) != null
                && isValid(getGroupComputeResourcePreference(computeResourceId, groupResourceProfileId)
                        .getLoginUserName())) {
            return getGroupComputeResourcePreference(computeResourceId, groupResourceProfileId)
                    .getLoginUserName();
        }
        throw new RuntimeException("Can't find login username for compute resource");
    }
}
