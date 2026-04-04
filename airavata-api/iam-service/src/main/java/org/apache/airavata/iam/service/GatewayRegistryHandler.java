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
import org.apache.airavata.iam.repository.GatewayGroupsRepository;
import org.apache.airavata.iam.repository.GatewayRepository;
import org.apache.airavata.iam.repository.QueueStatusRepository;
import org.apache.airavata.iam.repository.UserProfileRepository;
import org.apache.airavata.interfaces.AppCatalogException;
import org.apache.airavata.interfaces.GatewayRegistry;
import org.apache.airavata.interfaces.GwyResourceProfile;
import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.model.appcatalog.gatewaygroups.proto.GatewayGroups;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.GatewayResourceProfile;
import org.apache.airavata.model.status.proto.QueueStatusModel;
import org.apache.airavata.model.user.proto.UserProfile;
import org.apache.airavata.model.workspace.proto.Gateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class GatewayRegistryHandler implements GatewayRegistry {
    private static final Logger logger = LoggerFactory.getLogger(GatewayRegistryHandler.class);

    private final GatewayRepository gatewayRepository = new GatewayRepository();
    private final QueueStatusRepository queueStatusRepository = new QueueStatusRepository();
    private final GatewayGroupsRepository gatewayGroupsRepository = new GatewayGroupsRepository();
    private final UserProfileRepository userProfileRepository = new UserProfileRepository();

    @Autowired
    private GwyResourceProfile gwyResourceProfile;

    // =========================================================================
    // GatewayRegistry interface methods
    // =========================================================================

    @Override
    public String addGateway(Gateway gateway) throws Exception {
        try {
            if (!validateString(gateway.getGatewayId())) {
                logger.error("Gateway id cannot be empty...");
                throw new RegistryException("Internal error");
            }
            if (isGatewayExist(gateway.getGatewayId())) {
                throw new RegistryException(
                        "Gateway with gatewayId: " + gateway.getGatewayId() + ", already exists in ExperimentCatalog.");
            }
            if (gwyResourceProfile.isGatewayResourceProfileExists(gateway.getGatewayId())) {
                throw new RegistryException("GatewayResourceProfile with gatewayId: " + gateway.getGatewayId()
                        + ", already exists in AppCatalog.");
            }

            String gatewayId = gatewayRepository.addGateway(gateway);

            GatewayResourceProfile gatewayResourceProfile = GatewayResourceProfile.newBuilder()
                    .setGatewayId(gatewayId)
                    .setIdentityServerTenant(gatewayId)
                    .setIdentityServerPwdCredToken(gateway.getIdentityServerPasswordToken())
                    .build();
            gwyResourceProfile.addGatewayResourceProfile(gatewayResourceProfile);
            logger.debug("Airavata added gateway with gateway id : " + gateway.getGatewayId());
            return gatewayId;
        } catch (RegistryException e) {
            logger.error("Error while adding gateway", e);
            throw new RegistryException("Error while adding gateway. More info : " + e.getMessage());
        } catch (AppCatalogException e) {
            logger.error("Error while adding gateway profile", e);
            throw new RegistryException("Error while adding gateway profile. More info : " + e.getMessage());
        }
    }

    @Override
    public Gateway getGateway(String gatewayId) throws Exception {
        try {
            if (!gatewayRepository.isGatewayExist(gatewayId)) {
                logger.error("Gateway does not exist in the system. Please provide a valid gateway ID...");
                throw new RegistryException(
                        "Gateway does not exist in the system. Please provide a valid gateway ID...");
            }
            Gateway gateway = gatewayRepository.getGateway(gatewayId);
            logger.debug("Airavata retrieved gateway with gateway id : " + gateway.getGatewayId());
            return gateway;
        } catch (RegistryException e) {
            logger.error("Error while getting the gateway", e);
            throw new RegistryException("Error while getting the gateway. More info : " + e.getMessage());
        }
    }

    @Override
    public boolean isGatewayExist(String gatewayId) throws Exception {
        try {
            return gatewayRepository.isGatewayExist(gatewayId);
        } catch (RegistryException e) {
            logger.error("Error while getting gateway", e);
            throw new RegistryException("Error while getting gateway. More info : " + e.getMessage());
        }
    }

    @Override
    public boolean updateGateway(String gatewayId, Gateway updatedGateway) throws Exception {
        try {
            if (!gatewayRepository.isGatewayExist(gatewayId)) {
                logger.error("Gateway does not exist in the system. Please provide a valid gateway ID...");
                throw new RegistryException(
                        "Gateway does not exist in the system. Please provide a valid gateway ID...");
            }
            gatewayRepository.updateGateway(gatewayId, updatedGateway);

            GatewayResourceProfile existingGwyResourceProfile = gwyResourceProfile.getGatewayProfile(gatewayId);
            if (existingGwyResourceProfile.getIdentityServerPwdCredToken().isEmpty()
                    || !existingGwyResourceProfile
                            .getIdentityServerPwdCredToken()
                            .equals(updatedGateway.getIdentityServerPasswordToken())) {
                GatewayResourceProfile updatedProfile = existingGwyResourceProfile.toBuilder()
                        .setIdentityServerPwdCredToken(updatedGateway.getIdentityServerPasswordToken())
                        .build();
                gwyResourceProfile.updateGatewayResourceProfile(gatewayId, updatedProfile);
            }
            logger.debug("Airavata update gateway with gateway id : " + gatewayId);
            return true;
        } catch (RegistryException e) {
            logger.error("Error while updating the gateway", e);
            throw new RegistryException("Error while updating the gateway. More info : " + e.getMessage());
        } catch (AppCatalogException e) {
            logger.error("Error while updating gateway profile", e);
            throw new RegistryException("Error while updating gateway profile. More info : " + e.getMessage());
        }
    }

    @Override
    public boolean deleteGateway(String gatewayId) throws Exception {
        try {
            if (!gatewayRepository.isGatewayExist(gatewayId)) {
                logger.error("Gateway does not exist in the system. Please provide a valid gateway ID...");
                throw new RegistryException(
                        "Gateway does not exist in the system. Please provide a valid gateway ID...");
            }
            gatewayRepository.removeGateway(gatewayId);
            logger.debug("Airavata deleted gateway with gateway id : " + gatewayId);
            return true;
        } catch (RegistryException e) {
            logger.error("Error while deleting the gateway", e);
            throw new RegistryException("Error while deleting the gateway. More info : " + e.getMessage());
        }
    }

    @Override
    public List<Gateway> getAllGateways() throws Exception {
        try {
            List<Gateway> gateways = gatewayRepository.getAllGateways();
            logger.debug("Airavata retrieved all available gateways...");
            return gateways;
        } catch (RegistryException e) {
            logger.error("Error while getting all the gateways", e);
            throw new RegistryException("Error while getting all the gateways. More info : " + e.getMessage());
        }
    }

    @Override
    public boolean isUserExists(String gatewayId, String userName) throws Exception {
        try {
            return userProfileRepository.getUserProfileByIdAndGateWay(userName, gatewayId) != null;
        } catch (Exception e) {
            logger.error("Error while verifying user", e);
            throw new RegistryException("Error while verifying user. More info : " + e.getMessage());
        }
    }

    @Override
    public List<String> getAllUsersInGateway(String gatewayId) throws Exception {
        try {
            return userProfileRepository.getAllUserProfilesInGateway(gatewayId, 0, -1).stream()
                    .map(up -> up.getUserId())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error while retrieving users", e);
            throw new RegistryException("Error while retrieving users. More info : " + e.getMessage());
        }
    }

    @Override
    public String addUser(UserProfile userProfile) throws Exception {
        try {
            logger.info("Adding User in Registry: " + userProfile);
            if (isUserExists(userProfile.getGatewayId(), userProfile.getUserId())) {
                throw new RegistryException("User already exists, with userId: " + userProfile.getUserId()
                        + ", and gatewayId: " + userProfile.getGatewayId());
            }
            UserProfile savedUser = userProfileRepository.createUserProfile(userProfile);
            return savedUser.getUserId();
        } catch (RegistryException ex) {
            logger.error("Error while adding user in registry: " + ex, ex);
            throw new RegistryException("Error while adding user in registry: " + ex.getMessage());
        }
    }

    @Override
    public boolean isGatewayGroupsExists(String gatewayId) throws Exception {
        try {
            return gatewayGroupsRepository.isExists(gatewayId);
        } catch (Exception e) {
            final String message = "Error checking existence of the GatewayGroups entry for gateway " + gatewayId + ".";
            logger.error(message, e);
            throw new RegistryException(message + " More info: " + e.getMessage());
        }
    }

    @Override
    public GatewayGroups getGatewayGroups(String gatewayId) throws Exception {
        try {
            if (!gatewayGroupsRepository.isExists(gatewayId)) {
                final String message = "No GatewayGroups entry exists for " + gatewayId;
                logger.error(message);
                throw new RegistryException(message);
            }
            return gatewayGroupsRepository.get(gatewayId);
        } catch (RegistryException e) {
            throw e;
        } catch (Exception e) {
            final String message = "Error while retrieving the GatewayGroups entry for gateway " + gatewayId + ".";
            logger.error(message, e);
            throw new RegistryException(message + " More info: " + e.getMessage());
        }
    }

    @Override
    public void createGatewayGroups(GatewayGroups gatewayGroups) throws Exception {
        try {
            if (gatewayGroupsRepository.isExists(gatewayGroups.getGatewayId())) {
                logger.error("GatewayGroups already exists for " + gatewayGroups.getGatewayId());
                throw new RegistryException(
                        "GatewayGroups for gatewayId: " + gatewayGroups.getGatewayId() + " already exists.");
            }
            gatewayGroupsRepository.create(gatewayGroups);
        } catch (RegistryException e) {
            throw e;
        } catch (Exception e) {
            final String message =
                    "Error while creating a GatewayGroups entry for gateway " + gatewayGroups.getGatewayId() + ".";
            logger.error(message, e);
            throw new RegistryException(message + " More info: " + e.getMessage());
        }
    }

    @Override
    public QueueStatusModel getQueueStatus(String hostName, String queueName) throws Exception {
        try {
            Optional<QueueStatusModel> optionalQueueStatusModel =
                    queueStatusRepository.getQueueStatus(hostName, queueName);
            logger.info("Executed and present " + optionalQueueStatusModel.isPresent());
            if (optionalQueueStatusModel.isPresent()) {
                return optionalQueueStatusModel.get();
            } else {
                return QueueStatusModel.newBuilder()
                        .setHostName(hostName)
                        .setQueueName(queueName)
                        .setQueueUp(false)
                        .setRunningJobs(0)
                        .setQueuedJobs(0)
                        .setTime(0)
                        .build();
            }
        } catch (RegistryException e) {
            logger.error("Error while storing queue status models....", e);
            throw new RegistryException("Error while storing queue status models.... : " + e.getMessage());
        }
    }

    @Override
    public void registerQueueStatuses(List<QueueStatusModel> queueStatuses) throws Exception {
        try {
            queueStatusRepository.createQueueStatuses(queueStatuses);
        } catch (RegistryException e) {
            logger.error("Error while storing queue status models....", e);
            throw new RegistryException("Error while storing queue status models.... : " + e.getMessage());
        }
    }

    @Override
    public void updateGatewayGroups(GatewayGroups gatewayGroups) throws Exception {
        try {
            if (!gatewayGroupsRepository.isExists(gatewayGroups.getGatewayId())) {
                throw new RegistryException("No GatewayGroups entry exists for " + gatewayGroups.getGatewayId());
            }
            gatewayGroupsRepository.update(gatewayGroups);
        } catch (RegistryException e) {
            throw e;
        } catch (Exception e) {
            throw new RegistryException("Error while updating the GatewayGroups entry for gateway "
                    + gatewayGroups.getGatewayId() + ". More info: " + e.getMessage());
        }
    }

    @Override
    public List<QueueStatusModel> getLatestQueueStatuses() throws Exception {
        try {
            return queueStatusRepository.getLatestQueueStatuses();
        } catch (RegistryException e) {
            throw new RegistryException("Error while reading queue status models.... : " + e.getMessage());
        }
    }

    // --- Private helpers ---

    private boolean validateString(String name) {
        return name != null && !name.equals("") && name.trim().length() != 0;
    }
}
