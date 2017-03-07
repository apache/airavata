/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.airavata.service.profile.gateway.core.impl;

import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.service.profile.gateway.core.GwyResourceProfile;
import org.apache.airavata.service.profile.gateway.core.resources.GatewayProfileResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class GwyResourceProfileImpl implements GwyResourceProfile {

    private final static Logger logger = LoggerFactory.getLogger(GwyResourceProfileImpl.class);

    @Override
    public String addGatewayResourceProfile(GatewayResourceProfile gatewayProfile) throws Exception {
        try {
            GatewayProfileResource profileResource = new GatewayProfileResource();
            if (!gatewayProfile.getGatewayID().equals("")){
                profileResource.setGatewayID(gatewayProfile.getGatewayID());
            }
            if (gatewayProfile.getCredentialStoreToken()!= null){
                profileResource.setCredentialStoreToken(gatewayProfile.getCredentialStoreToken());
            }
            if (gatewayProfile.getIdentityServerTenant() != null){
                profileResource.setIdentityServerTenant(gatewayProfile.getIdentityServerTenant());
            }
            if (gatewayProfile.getIdentityServerPwdCredToken() != null){
                profileResource.setIdentityServerPwdCredToken(gatewayProfile.getIdentityServerPwdCredToken());
            }
            profileResource.setGatewayID(gatewayProfile.getGatewayID());
            profileResource.save();
            return profileResource.getGatewayID();
        } catch (Exception e) {
            logger.error("Error while saving gateway profile, exception: " + e, e);
            throw e;
        }
    }

    @Override
    public void updateGatewayResourceProfile(String gatewayId, GatewayResourceProfile updatedProfile) throws Exception {
        try {
            GatewayProfileResource existingGP = new GatewayProfileResource().getByID(gatewayId);
            existingGP.setCredentialStoreToken(updatedProfile.getCredentialStoreToken());
            existingGP.setIdentityServerTenant(updatedProfile.getIdentityServerTenant());
            existingGP.setIdentityServerPwdCredToken(updatedProfile.getIdentityServerPwdCredToken());
            existingGP.save();
        } catch (Exception e) {
            logger.error("Error while updating gateway profile, exception: " + e, e);
            throw e;
        }
    }

    @Override
    public GatewayResourceProfile getGatewayProfile(String gatewayId) throws Exception {
        try {
            GatewayProfileResource gw = new GatewayProfileResource().getByID(gatewayId);
            GatewayResourceProfile gatewayProfile = new GatewayResourceProfile();
            gatewayProfile.setGatewayID(gw.getGatewayID());
            gatewayProfile.setCredentialStoreToken(gw.getCredentialStoreToken());
            gatewayProfile.setIdentityServerTenant(gw.getIdentityServerTenant());
            gatewayProfile.setIdentityServerPwdCredToken(gw.getIdentityServerPwdCredToken());
            return gatewayProfile;
        } catch (Exception e) {
            logger.error("Error while retrieving gateway profile, exception: " + e, e);
            throw e;
        }
    }

    @Override
    public boolean removeGatewayResourceProfile(String gatewayId) throws Exception {
       try {
           GatewayProfileResource resource = new GatewayProfileResource();
           resource.remove(gatewayId);
           return true;
       } catch (Exception e) {
           logger.error("Error while deleting gateway profile, exception: " + e, e);
           throw e;
       }
    }

    @Override
    public boolean isGatewayResourceProfileExists(String gatewayId) throws Exception {
        try {
            GatewayProfileResource resource = new GatewayProfileResource();
            return resource.isExists(gatewayId);
        } catch (Exception e) {
            logger.error("Error while retrieving gateway profile, exception: " + e, e);
            throw e;
        }
    }

    @Override
    public List<String> getGatewayProfileIds(String gatewayName) throws Exception {
        try {
            GatewayProfileResource profileResource = new GatewayProfileResource();
            List<String> gatewayIds = profileResource.getGatewayProfileIds(gatewayName);
            return gatewayIds;
        } catch (Exception e) {
            logger.error("Error while retrieving gateway ids, exception: " + e, e);
            throw e;
        }
    }

    @Override
    public List<GatewayResourceProfile> getAllGatewayProfiles() throws Exception {
        try {
            List<GatewayResourceProfile> gatewayResourceProfileList = new ArrayList<>();
            List<GatewayProfileResource> resourceList = new GatewayProfileResource().getAll();
            if (resourceList != null && !resourceList.isEmpty()){
                for (GatewayProfileResource gw : resourceList) {
                    GatewayResourceProfile gatewayProfile = new GatewayResourceProfile();
                    gatewayProfile.setGatewayID(gw.getGatewayID());
                    gatewayProfile.setCredentialStoreToken(gw.getCredentialStoreToken());
                    gatewayProfile.setIdentityServerTenant(gw.getIdentityServerTenant());
                    gatewayProfile.setIdentityServerPwdCredToken(gw.getIdentityServerPwdCredToken());
                    gatewayResourceProfileList.add(gatewayProfile);
                }
            }
            return gatewayResourceProfileList;
        } catch (Exception e) {
            logger.error("Error while retrieving gateway ids, exception: " + e, e);
            throw e;
        }
    }
}
