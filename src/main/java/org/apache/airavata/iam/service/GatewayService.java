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
package org.apache.airavata.iam.service;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.airavata.compute.repository.GwyResourceProfileRepository;
import org.apache.airavata.iam.repository.GatewayGroupsRepository;
import org.apache.airavata.iam.repository.GatewayRepository;
import org.apache.airavata.iam.repository.UserProfileRepository;
import org.apache.airavata.models.appcatalog.gatewaygroups.GatewayGroups;
import org.apache.airavata.models.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.models.user.UserProfile;
import org.apache.airavata.models.workspace.Gateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class GatewayService {
    private static final Logger logger = LoggerFactory.getLogger(GatewayService.class);

    private final GatewayRepository gatewayRepository;
    private final GatewayGroupsRepository gatewayGroupsRepository;
    private final UserProfileRepository userProfileRepository;
    private final GwyResourceProfileRepository gwyResourceProfileRepository;

    @Autowired
    public GatewayService(GatewayRepository gatewayRepository,
            GatewayGroupsRepository gatewayGroupsRepository,
            UserProfileRepository userProfileRepository,
            GwyResourceProfileRepository gwyResourceProfileRepository) {
        this.gatewayRepository = gatewayRepository;
        this.gatewayGroupsRepository = gatewayGroupsRepository;
        this.userProfileRepository = userProfileRepository;
        this.gwyResourceProfileRepository = gwyResourceProfileRepository;
    }

    // =========================================================================
    // GatewayRegistry interface methods
    // =========================================================================

    public String addGateway(Gateway gateway) throws Exception {
        try {
            if (!validateString(gateway.gatewayId())) {
                logger.error("Gateway id cannot be empty...");
                throw new Exception("Internal error");
            }
            if (isGatewayExist(gateway.gatewayId())) {
                throw new Exception(
                        "Gateway with gatewayId: " + gateway.gatewayId() + ", already exists in ExperimentCatalog.");
            }
            if (gwyResourceProfileRepository.isGatewayResourceProfileExists(gateway.gatewayId())) {
                throw new Exception("GatewayResourceProfile with gatewayId: " + gateway.gatewayId()
                        + ", already exists in AppCatalog.");
            }

            String gatewayId = gatewayRepository.addGateway(gateway);

            GatewayResourceProfile gatewayResourceProfile = GatewayResourceProfile.newBuilder()
                    .setGatewayId(gatewayId)
                    .setIdentityServerTenant(gatewayId)
                    .setIdentityServerPwdCredToken(gateway.identityServerPasswordToken())
                    .build();
            gwyResourceProfileRepository.addGatewayResourceProfile(gatewayResourceProfile);
            logger.debug("Airavata added gateway with gateway id : " + gateway.gatewayId());
            return gatewayId;
        } catch (Exception e) {
            logger.error("Error while adding gateway", e);
            throw new Exception("Error while adding gateway. More info : " + e.getMessage());
        }
    }

    public Gateway getGateway(String gatewayId) throws Exception {
        try {
            if (!gatewayRepository.isGatewayExist(gatewayId)) {
                logger.error("Gateway does not exist in the system. Please provide a valid gateway ID...");
                throw new Exception(
                        "Gateway does not exist in the system. Please provide a valid gateway ID...");
            }
            Gateway gateway = gatewayRepository.getGateway(gatewayId);
            logger.debug("Airavata retrieved gateway with gateway id : " + gateway.gatewayId());
            return gateway;
        } catch (Exception e) {
            logger.error("Error while getting the gateway", e);
            throw new Exception("Error while getting the gateway. More info : " + e.getMessage());
        }
    }

    public boolean isGatewayExist(String gatewayId) throws Exception {
        try {
            return gatewayRepository.isGatewayExist(gatewayId);
        } catch (Exception e) {
            logger.error("Error while getting gateway", e);
            throw new Exception("Error while getting gateway. More info : " + e.getMessage());
        }
    }

    public boolean updateGateway(String gatewayId, Gateway updatedGateway) throws Exception {
        try {
            if (!gatewayRepository.isGatewayExist(gatewayId)) {
                logger.error("Gateway does not exist in the system. Please provide a valid gateway ID...");
                throw new Exception(
                        "Gateway does not exist in the system. Please provide a valid gateway ID...");
            }
            gatewayRepository.updateGateway(gatewayId, updatedGateway);

            GatewayResourceProfile existingGwyResourceProfile = gwyResourceProfileRepository
                    .getGatewayProfile(gatewayId);
            if (existingGwyResourceProfile.identityServerPwdCredToken().isEmpty()
                    || !existingGwyResourceProfile
                            .identityServerPwdCredToken()
                            .equals(updatedGateway.identityServerPasswordToken())) {
                GatewayResourceProfile updatedProfile = existingGwyResourceProfile.toBuilder()
                        .setIdentityServerPwdCredToken(updatedGateway.identityServerPasswordToken())
                        .build();
                gwyResourceProfileRepository.updateGatewayResourceProfile(gatewayId, updatedProfile);
            }
            logger.debug("Airavata update gateway with gateway id : " + gatewayId);
            return true;
        } catch (Exception e) {
            logger.error("Error while updating the gateway", e);
            throw new Exception("Error while updating the gateway. More info : " + e.getMessage());
        }
    }

    public boolean deleteGateway(String gatewayId) throws Exception {
        try {
            if (!gatewayRepository.isGatewayExist(gatewayId)) {
                logger.error("Gateway does not exist in the system. Please provide a valid gateway ID...");
                throw new Exception(
                        "Gateway does not exist in the system. Please provide a valid gateway ID...");
            }
            gatewayRepository.removeGateway(gatewayId);
            logger.debug("Airavata deleted gateway with gateway id : " + gatewayId);
            return true;
        } catch (Exception e) {
            logger.error("Error while deleting the gateway", e);
            throw new Exception("Error while deleting the gateway. More info : " + e.getMessage());
        }
    }

    public List<Gateway> getAllGateways() throws Exception {
        try {
            List<Gateway> gateways = gatewayRepository.getAllGateways();
            logger.debug("Airavata retrieved all available gateways...");
            return gateways;
        } catch (Exception e) {
            logger.error("Error while getting all the gateways", e);
            throw new Exception("Error while getting all the gateways. More info : " + e.getMessage());
        }
    }

    public boolean isUserExists(String gatewayId, String userName) throws Exception {
        try {
            return userProfileRepository.getUserProfileByIdAndGateWay(userName, gatewayId) != null;
        } catch (Exception e) {
            logger.error("Error while verifying user", e);
            throw new Exception("Error while verifying user. More info : " + e.getMessage());
        }
    }

    public List<String> getAllUsersInGateway(String gatewayId) throws Exception {
        try {
            return userProfileRepository.getAllUserProfilesInGateway(gatewayId, 0, -1).stream()
                    .map(up -> up.userId())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error while retrieving users", e);
            throw new Exception("Error while retrieving users. More info : " + e.getMessage());
        }
    }

    public String addUser(UserProfile userProfile) throws Exception {
        try {
            logger.info("Adding User in Registry: " + userProfile);
            if (isUserExists(userProfile.gatewayId(), userProfile.userId())) {
                throw new Exception("User already exists, with userId: " + userProfile.userId()
                        + ", and gatewayId: " + userProfile.gatewayId());
            }
            UserProfile savedUser = userProfileRepository.createUserProfile(userProfile);
            return savedUser.userId();
        } catch (Exception ex) {
            logger.error("Error while adding user in registry: " + ex, ex);
            throw new Exception("Error while adding user in registry: " + ex.getMessage());
        }
    }

    public boolean isGatewayGroupsExists(String gatewayId) throws Exception {
        try {
            return gatewayGroupsRepository.isExists(gatewayId);
        } catch (Exception e) {
            final String message = "Error checking existence of the GatewayGroups entry for gateway " + gatewayId + ".";
            logger.error(message, e);
            throw new Exception(message + " More info: " + e.getMessage());
        }
    }

    public GatewayGroups getGatewayGroups(String gatewayId) throws Exception {
        try {
            if (!gatewayGroupsRepository.isExists(gatewayId)) {
                final String message = "No GatewayGroups entry exists for " + gatewayId;
                logger.error(message);
                throw new Exception(message);
            }
            return gatewayGroupsRepository.get(gatewayId);
        } catch (Exception e) {
            final String message = "Error while retrieving the GatewayGroups entry for gateway " + gatewayId + ".";
            logger.error(message, e);
            throw new Exception(message + " More info: " + e.getMessage());
        }
    }

    public void createGatewayGroups(GatewayGroups gatewayGroups) throws Exception {
        try {
            if (gatewayGroupsRepository.isExists(gatewayGroups.gatewayId())) {
                logger.error("GatewayGroups already exists for " + gatewayGroups.gatewayId());
                throw new Exception(
                        "GatewayGroups for gatewayId: " + gatewayGroups.gatewayId() + " already exists.");
            }
            gatewayGroupsRepository.create(gatewayGroups);
        } catch (Exception e) {
            final String message = "Error while creating a GatewayGroups entry for gateway "
                    + gatewayGroups.gatewayId() + ".";
            logger.error(message, e);
            throw new Exception(message + " More info: " + e.getMessage());
        }
    }

    public void updateGatewayGroups(GatewayGroups gatewayGroups) throws Exception {
        try {
            if (!gatewayGroupsRepository.isExists(gatewayGroups.gatewayId())) {
                throw new Exception("No GatewayGroups entry exists for " + gatewayGroups.gatewayId());
            }
            gatewayGroupsRepository.update(gatewayGroups);
        } catch (Exception e) {
            throw new Exception("Error while updating the GatewayGroups entry for gateway "
                    + gatewayGroups.gatewayId() + ". More info: " + e.getMessage());
        }
    }

    // --- Private helpers ---

    private boolean validateString(String name) {
        return name != null && !name.equals("") && name.trim().length() != 0;
    }
}
