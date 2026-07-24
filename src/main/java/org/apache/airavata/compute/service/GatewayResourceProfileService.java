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

import java.util.List;
import org.apache.airavata.api.gatewayprofile.GatewayResourceProfileWithAccess;
import org.apache.airavata.common.RequestContext;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.GatewayResourceProfile;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.StoragePreference;
import org.apache.airavata.model.commons.proto.AccessFlags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GatewayResourceProfileService {

    private static final Logger logger = LoggerFactory.getLogger(GatewayResourceProfileService.class);

    private final ResourceProfileRegistryService registryHandler;

    public GatewayResourceProfileService(ResourceProfileRegistryService registryHandler) {
        this.registryHandler = registryHandler;
    }

    public String registerGatewayResourceProfile(RequestContext ctx, GatewayResourceProfile gatewayResourceProfile)
            throws Exception {
        try {
            return registryHandler.registerGatewayResourceProfile(gatewayResourceProfile);
        } catch (Exception e) {
            logger.error("Error while registering gateway resource profile: " + e.getMessage(), e);
            throw new Exception("Error while registering gateway resource profile: " + e.getMessage(), e);
        }
    }

    public GatewayResourceProfile getGatewayResourceProfile(RequestContext ctx, String gatewayId)
            throws Exception {
        try {
            return registryHandler.getGatewayResourceProfile(gatewayId);
        } catch (Exception e) {
            logger.error("Error while retrieving gateway resource profile: " + e.getMessage(), e);
            throw new Exception("Error while retrieving gateway resource profile: " + e.getMessage(), e);
        }
    }

    /**
     * {@link #getGatewayResourceProfile} plus the caller's server-computed access
     * flags (additive).
     * Reuses {@code getGatewayResourceProfile} for READ enforcement so a caller can
     * never
     * self-authorize. A gateway resource profile is a gateway-level entity with no
     * owner and no
     * sharing entity, so {@code is_owner} is always false and
     * {@code user_has_write_access} reflects
     * the gateway-admin (admin-rw) role of the caller.
     */
    public GatewayResourceProfileWithAccess getGatewayResourceProfileWithAccess(RequestContext ctx, String gatewayId)
            throws Exception {
        GatewayResourceProfile profile = getGatewayResourceProfile(ctx, gatewayId);
        if (profile == null) {
            throw new Exception("Gateway resource profile " + gatewayId + " does not exist");
        }
        try {
            return GatewayResourceProfileWithAccess.newBuilder()
                    .setGatewayResourceProfile(profile)
                    .setAccess(AccessFlags.newBuilder()
                            .setIsOwner(false)
                            .setUserHasWriteAccess(ctx.isGatewayAdmin())
                            .build())
                    .build();
        } catch (Exception e) {
            logger.error("Error while computing gateway resource profile access: " + e.getMessage(), e);
            throw new Exception("Error while computing gateway resource profile access: " + e.getMessage(), e);
        }
    }

    public boolean updateGatewayResourceProfile(
            RequestContext ctx, String gatewayId, GatewayResourceProfile gatewayResourceProfile)
            throws Exception {
        try {
            return registryHandler.updateGatewayResourceProfile(gatewayId, gatewayResourceProfile);
        } catch (Exception e) {
            logger.error("Error while updating gateway resource profile: " + e.getMessage(), e);
            throw new Exception("Error while updating gateway resource profile: " + e.getMessage(), e);
        }
    }

    public boolean deleteGatewayResourceProfile(RequestContext ctx, String gatewayId) throws Exception {
        try {
            return registryHandler.deleteGatewayResourceProfile(gatewayId);
        } catch (Exception e) {
            logger.error("Error while deleting gateway resource profile: " + e.getMessage(), e);
            throw new Exception("Error while deleting gateway resource profile: " + e.getMessage(), e);
        }
    }

    public boolean addGatewayComputeResourcePreference(
            RequestContext ctx,
            String gatewayId,
            String computeResourceId,
            ComputeResourcePreference computeResourcePreference)
            throws Exception {
        try {
            return registryHandler.addGatewayComputeResourcePreference(
                    gatewayId, computeResourceId, computeResourcePreference);
        } catch (Exception e) {
            logger.error("Error while adding gateway compute resource preference: " + e.getMessage(), e);
            throw new Exception("Error while adding gateway compute resource preference: " + e.getMessage(), e);
        }
    }

    public ComputeResourcePreference getGatewayComputeResourcePreference(
            RequestContext ctx, String gatewayId, String computeResourceId) throws Exception {
        try {
            return registryHandler.getGatewayComputeResourcePreference(gatewayId, computeResourceId);
        } catch (Exception e) {
            logger.error("Error while reading gateway compute resource preference: " + e.getMessage(), e);
            throw new Exception("Error while reading gateway compute resource preference: " + e.getMessage(), e);
        }
    }

    public boolean updateGatewayComputeResourcePreference(
            RequestContext ctx,
            String gatewayId,
            String computeResourceId,
            ComputeResourcePreference computeResourcePreference)
            throws Exception {
        try {
            return registryHandler.updateGatewayComputeResourcePreference(
                    gatewayId, computeResourceId, computeResourcePreference);
        } catch (Exception e) {
            logger.error("Error while updating gateway compute resource preference: " + e.getMessage(), e);
            throw new Exception(
                    "Error while updating gateway compute resource preference: " + e.getMessage(), e);
        }
    }

    public boolean deleteGatewayComputeResourcePreference(
            RequestContext ctx, String gatewayId, String computeResourceId) throws Exception {
        try {
            return registryHandler.deleteGatewayComputeResourcePreference(gatewayId, computeResourceId);
        } catch (Exception e) {
            logger.error("Error while deleting gateway compute resource preference: " + e.getMessage(), e);
            throw new Exception(
                    "Error while deleting gateway compute resource preference: " + e.getMessage(), e);
        }
    }

    public boolean addGatewayStoragePreference(
            RequestContext ctx, String gatewayId, String storageResourceId, StoragePreference storagePreference)
            throws Exception {
        try {
            return registryHandler.addGatewayStoragePreference(gatewayId, storageResourceId, storagePreference);
        } catch (Exception e) {
            logger.error("Error while adding gateway storage preference: " + e.getMessage(), e);
            throw new Exception("Error while adding gateway storage preference: " + e.getMessage(), e);
        }
    }

    public StoragePreference getGatewayStoragePreference(RequestContext ctx, String gatewayId, String storageId)
            throws Exception {
        try {
            return registryHandler.getGatewayStoragePreference(gatewayId, storageId);
        } catch (Exception e) {
            logger.error("Error while reading gateway storage preference: " + e.getMessage(), e);
            throw new Exception("Error while reading gateway storage preference: " + e.getMessage(), e);
        }
    }

    public boolean updateGatewayStoragePreference(
            RequestContext ctx, String gatewayId, String storageId, StoragePreference storagePreference)
            throws Exception {
        try {
            return registryHandler.updateGatewayStoragePreference(gatewayId, storageId, storagePreference);
        } catch (Exception e) {
            logger.error("Error while updating gateway storage preference: " + e.getMessage(), e);
            throw new Exception("Error while updating gateway storage preference: " + e.getMessage(), e);
        }
    }

    public boolean deleteGatewayStoragePreference(RequestContext ctx, String gatewayId, String storageId)
            throws Exception {
        try {
            return registryHandler.deleteGatewayStoragePreference(gatewayId, storageId);
        } catch (Exception e) {
            logger.error("Error while deleting gateway storage preference: " + e.getMessage(), e);
            throw new Exception("Error while deleting gateway storage preference: " + e.getMessage(), e);
        }
    }

    public List<ComputeResourcePreference> getAllGatewayComputeResourcePreferences(RequestContext ctx, String gatewayId)
            throws Exception {
        try {
            return registryHandler.getAllGatewayComputeResourcePreferences(gatewayId);
        } catch (Exception e) {
            logger.error("Error while reading gateway compute resource preferences: " + e.getMessage(), e);
            throw new Exception(
                    "Error while reading gateway compute resource preferences: " + e.getMessage(), e);
        }
    }

    public List<StoragePreference> getAllGatewayStoragePreferences(RequestContext ctx, String gatewayId)
            throws Exception {
        try {
            return registryHandler.getAllGatewayStoragePreferences(gatewayId);
        } catch (Exception e) {
            logger.error("Error while reading gateway storage preferences: " + e.getMessage(), e);
            throw new Exception("Error while reading gateway storage preferences: " + e.getMessage(), e);
        }
    }

    public List<GatewayResourceProfile> getAllGatewayResourceProfiles(RequestContext ctx) throws Exception {
        try {
            return registryHandler.getAllGatewayResourceProfiles();
        } catch (Exception e) {
            logger.error("Error while retrieving all gateway resource profiles: " + e.getMessage(), e);
            throw new Exception("Error while retrieving all gateway resource profiles: " + e.getMessage(), e);
        }
    }
}
