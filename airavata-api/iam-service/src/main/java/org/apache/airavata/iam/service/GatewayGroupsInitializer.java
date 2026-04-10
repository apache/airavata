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

import org.apache.airavata.interfaces.CredentialProvider;
import org.apache.airavata.interfaces.GatewayGroupsProvider;
import org.apache.airavata.interfaces.RegistryProvider;
import org.apache.airavata.interfaces.SharingProvider;
import org.apache.airavata.model.appcatalog.gatewaygroups.proto.GatewayGroups;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.GatewayResourceProfile;
import org.apache.airavata.model.credential.store.proto.PasswordCredential;
import org.apache.airavata.sharing.registry.models.proto.GroupCardinality;
import org.apache.airavata.sharing.registry.models.proto.GroupType;
import org.apache.airavata.sharing.registry.models.proto.UserGroup;
import org.apache.airavata.util.AiravataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Create and save an initial set of user management groups for a gateway.
 */
@Component
public class GatewayGroupsInitializer implements GatewayGroupsProvider {

    private static final Logger logger = LoggerFactory.getLogger(GatewayGroupsInitializer.class);

    private final RegistryProvider registryClient;
    private final SharingProvider sharingClient;
    private final CredentialProvider credentialStoreClient;

    public GatewayGroupsInitializer(
            RegistryProvider registryClient, SharingProvider sharingClient, CredentialProvider credentialStoreClient) {
        this.registryClient = registryClient;
        this.sharingClient = sharingClient;
        this.credentialStoreClient = credentialStoreClient;
    }

    @Override
    public synchronized GatewayGroups initializeGatewayGroups(String gatewayId) {
        try {
            return initialize(gatewayId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize a GatewayGroups instance for gateway: " + gatewayId, e);
        }
    }

    private GatewayGroups initialize(String gatewayId) throws Exception {

        logger.info("Creating a GatewayGroups instance for gateway " + gatewayId + " ...");

        GatewayGroups.Builder gatewayGroupsBuilder = GatewayGroups.newBuilder().setGatewayId(gatewayId);

        String adminOwnerUsername = getAdminOwnerUsername(gatewayId);
        String ownerId = adminOwnerUsername + "@" + gatewayId;
        if (!sharingClient.isUserExists(gatewayId, ownerId)) {
            sharingClient.createUser(ownerId, gatewayId, adminOwnerUsername);
        }

        // Gateway Users
        UserGroup gatewayUsersGroup =
                createGroup(gatewayId, ownerId, "Gateway Users", "Default group for users of the gateway.");
        gatewayGroupsBuilder.setDefaultGatewayUsersGroupId(gatewayUsersGroup.getGroupId());
        // Admin Users
        UserGroup adminUsersGroup = createGroup(gatewayId, ownerId, "Admin Users", "Admin users group.");
        gatewayGroupsBuilder.setAdminsGroupId(adminUsersGroup.getGroupId());
        // Read Only Admin Users
        UserGroup readOnlyAdminsGroup =
                createGroup(gatewayId, ownerId, "Read Only Admin Users", "Group of admin users with read-only access.");
        gatewayGroupsBuilder.setReadOnlyAdminsGroupId(readOnlyAdminsGroup.getGroupId());
        GatewayGroups gatewayGroups = gatewayGroupsBuilder.build();

        try {
            registryClient.createGatewayGroups(gatewayGroups);
        } catch (Exception e) {
            logger.error(
                    "Gateway groups created in Sharing Catalog failed to save GatewayGroups entity in Registry", e);
            throw e;
        }

        return gatewayGroups;
    }

    private UserGroup createGroup(String gatewayId, String ownerId, String groupName, String groupDescription)
            throws Exception {

        UserGroup userGroup = UserGroup.newBuilder()
                .setGroupId(AiravataUtils.getId(groupName))
                .setDomainId(gatewayId)
                .setGroupCardinality(GroupCardinality.MULTI_USER)
                .setCreatedTime(System.currentTimeMillis())
                .setUpdatedTime(System.currentTimeMillis())
                .setName(groupName)
                .setDescription(groupDescription)
                .setOwnerId(ownerId)
                .setGroupType(GroupType.DOMAIN_LEVEL_GROUP)
                .build();
        sharingClient.createGroup(userGroup);

        return userGroup;
    }

    private String getAdminOwnerUsername(String gatewayId) throws Exception {

        GatewayResourceProfile gatewayResourceProfile = registryClient.getGatewayResourceProfile(gatewayId);
        PasswordCredential credential = credentialStoreClient.getPasswordCredential(
                gatewayResourceProfile.getIdentityServerPwdCredToken(), gatewayResourceProfile.getGatewayId());
        String adminUsername = credential.getLoginUserName();
        return adminUsername;
    }
}
