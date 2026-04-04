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
package org.apache.airavata.compute.service;

import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.exception.ServiceException;
import org.apache.airavata.interfaces.ConfigParam;
import org.apache.airavata.interfaces.ResourceProfileRegistry;
import org.apache.airavata.interfaces.SSHAccountProvisionerFactory;
import org.apache.airavata.interfaces.SSHAccountProvisionerProvider;
import org.apache.airavata.model.appcatalog.accountprovisioning.proto.SSHAccountProvisioner;
import org.apache.airavata.model.appcatalog.accountprovisioning.proto.SSHAccountProvisionerConfigParam;
import org.apache.airavata.model.appcatalog.accountprovisioning.proto.SSHAccountProvisionerConfigParamType;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.GatewayResourceProfile;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.StoragePreference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GatewayResourceProfileService {

    private static final Logger logger = LoggerFactory.getLogger(GatewayResourceProfileService.class);

    private final ResourceProfileRegistry registryHandler;
    private final SSHAccountProvisionerFactory sshAccountProvisionerFactory;

    public GatewayResourceProfileService(
            ResourceProfileRegistry registryHandler, SSHAccountProvisionerFactory sshAccountProvisionerFactory) {
        this.registryHandler = registryHandler;
        this.sshAccountProvisionerFactory = sshAccountProvisionerFactory;
    }

    public String registerGatewayResourceProfile(RequestContext ctx, GatewayResourceProfile gatewayResourceProfile)
            throws ServiceException {
        try {
            return registryHandler.registerGatewayResourceProfile(gatewayResourceProfile);
        } catch (Exception e) {
            throw new ServiceException("Error while registering gateway resource profile: " + e.getMessage(), e);
        }
    }

    public GatewayResourceProfile getGatewayResourceProfile(RequestContext ctx, String gatewayId)
            throws ServiceException {
        try {
            return registryHandler.getGatewayResourceProfile(gatewayId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving gateway resource profile: " + e.getMessage(), e);
        }
    }

    public boolean updateGatewayResourceProfile(
            RequestContext ctx, String gatewayId, GatewayResourceProfile gatewayResourceProfile)
            throws ServiceException {
        try {
            return registryHandler.updateGatewayResourceProfile(gatewayId, gatewayResourceProfile);
        } catch (Exception e) {
            throw new ServiceException("Error while updating gateway resource profile: " + e.getMessage(), e);
        }
    }

    public boolean deleteGatewayResourceProfile(RequestContext ctx, String gatewayId) throws ServiceException {
        try {
            return registryHandler.deleteGatewayResourceProfile(gatewayId);
        } catch (Exception e) {
            throw new ServiceException("Error while deleting gateway resource profile: " + e.getMessage(), e);
        }
    }

    public boolean addGatewayComputeResourcePreference(
            RequestContext ctx,
            String gatewayId,
            String computeResourceId,
            ComputeResourcePreference computeResourcePreference)
            throws ServiceException {
        try {
            return registryHandler.addGatewayComputeResourcePreference(
                    gatewayId, computeResourceId, computeResourcePreference);
        } catch (Exception e) {
            throw new ServiceException("Error while adding gateway compute resource preference: " + e.getMessage(), e);
        }
    }

    public ComputeResourcePreference getGatewayComputeResourcePreference(
            RequestContext ctx, String gatewayId, String computeResourceId) throws ServiceException {
        try {
            return registryHandler.getGatewayComputeResourcePreference(gatewayId, computeResourceId);
        } catch (Exception e) {
            throw new ServiceException("Error while reading gateway compute resource preference: " + e.getMessage(), e);
        }
    }

    public boolean updateGatewayComputeResourcePreference(
            RequestContext ctx,
            String gatewayId,
            String computeResourceId,
            ComputeResourcePreference computeResourcePreference)
            throws ServiceException {
        try {
            return registryHandler.updateGatewayComputeResourcePreference(
                    gatewayId, computeResourceId, computeResourcePreference);
        } catch (Exception e) {
            throw new ServiceException(
                    "Error while updating gateway compute resource preference: " + e.getMessage(), e);
        }
    }

    public boolean deleteGatewayComputeResourcePreference(
            RequestContext ctx, String gatewayId, String computeResourceId) throws ServiceException {
        try {
            return registryHandler.deleteGatewayComputeResourcePreference(gatewayId, computeResourceId);
        } catch (Exception e) {
            throw new ServiceException(
                    "Error while deleting gateway compute resource preference: " + e.getMessage(), e);
        }
    }

    public boolean addGatewayStoragePreference(
            RequestContext ctx, String gatewayId, String storageResourceId, StoragePreference storagePreference)
            throws ServiceException {
        try {
            return registryHandler.addGatewayStoragePreference(gatewayId, storageResourceId, storagePreference);
        } catch (Exception e) {
            throw new ServiceException("Error while adding gateway storage preference: " + e.getMessage(), e);
        }
    }

    public StoragePreference getGatewayStoragePreference(RequestContext ctx, String gatewayId, String storageId)
            throws ServiceException {
        try {
            return registryHandler.getGatewayStoragePreference(gatewayId, storageId);
        } catch (Exception e) {
            throw new ServiceException("Error while reading gateway storage preference: " + e.getMessage(), e);
        }
    }

    public boolean updateGatewayStoragePreference(
            RequestContext ctx, String gatewayId, String storageId, StoragePreference storagePreference)
            throws ServiceException {
        try {
            return registryHandler.updateGatewayStoragePreference(gatewayId, storageId, storagePreference);
        } catch (Exception e) {
            throw new ServiceException("Error while updating gateway storage preference: " + e.getMessage(), e);
        }
    }

    public boolean deleteGatewayStoragePreference(RequestContext ctx, String gatewayId, String storageId)
            throws ServiceException {
        try {
            return registryHandler.deleteGatewayStoragePreference(gatewayId, storageId);
        } catch (Exception e) {
            throw new ServiceException("Error while deleting gateway storage preference: " + e.getMessage(), e);
        }
    }

    public List<ComputeResourcePreference> getAllGatewayComputeResourcePreferences(RequestContext ctx, String gatewayId)
            throws ServiceException {
        try {
            return registryHandler.getAllGatewayComputeResourcePreferences(gatewayId);
        } catch (Exception e) {
            throw new ServiceException(
                    "Error while reading gateway compute resource preferences: " + e.getMessage(), e);
        }
    }

    public List<StoragePreference> getAllGatewayStoragePreferences(RequestContext ctx, String gatewayId)
            throws ServiceException {
        try {
            return registryHandler.getAllGatewayStoragePreferences(gatewayId);
        } catch (Exception e) {
            throw new ServiceException("Error while reading gateway storage preferences: " + e.getMessage(), e);
        }
    }

    public List<GatewayResourceProfile> getAllGatewayResourceProfiles(RequestContext ctx) throws ServiceException {
        try {
            return registryHandler.getAllGatewayResourceProfiles();
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving all gateway resource profiles: " + e.getMessage(), e);
        }
    }

    public List<SSHAccountProvisioner> getSSHAccountProvisioners(RequestContext ctx) throws ServiceException {
        try {
            List<SSHAccountProvisioner> sshAccountProvisioners = new ArrayList<>();
            List<SSHAccountProvisionerProvider> providers =
                    sshAccountProvisionerFactory.getSSHAccountProvisionerProviders();
            for (SSHAccountProvisionerProvider provider : providers) {
                List<SSHAccountProvisionerConfigParam> configParams = new ArrayList<>();
                for (ConfigParam configParam : provider.getConfigParams()) {
                    SSHAccountProvisionerConfigParam.Builder paramBuilder =
                            SSHAccountProvisionerConfigParam.newBuilder()
                                    .setName(configParam.getName())
                                    .setDescription(configParam.getDescription())
                                    .setIsOptional(configParam.isOptional());
                    switch (configParam.getType()) {
                        case STRING:
                            paramBuilder.setType(SSHAccountProvisionerConfigParamType.STRING);
                            break;
                        case CRED_STORE_PASSWORD_TOKEN:
                            paramBuilder.setType(SSHAccountProvisionerConfigParamType.CRED_STORE_PASSWORD_TOKEN);
                            break;
                    }
                    configParams.add(paramBuilder.build());
                }
                SSHAccountProvisioner provisioner = SSHAccountProvisioner.newBuilder()
                        .setCanCreateAccount(provider.canCreateAccount())
                        .setCanInstallSshKey(provider.canInstallSSHKey())
                        .setName(provider.getName())
                        .addAllConfigParams(configParams)
                        .build();
                sshAccountProvisioners.add(provisioner);
            }
            return sshAccountProvisioners;
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving SSH account provisioners: " + e.getMessage(), e);
        }
    }
}
