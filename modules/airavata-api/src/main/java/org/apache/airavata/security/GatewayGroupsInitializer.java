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
package org.apache.airavata.security;

import org.apache.airavata.common.model.GatewayGroups;
import org.apache.airavata.common.model.GatewayResourceProfile;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.credential.exception.CredentialStoreException;
import org.apache.airavata.credential.model.PasswordCredential;
import org.apache.airavata.registry.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.service.SharingRegistryService;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.service.security.CredentialStoreService;
import org.apache.airavata.sharing.model.GroupCardinality;
import org.apache.airavata.sharing.model.GroupType;
import org.apache.airavata.sharing.model.SharingRegistryException;
import org.apache.airavata.sharing.model.User;
import org.apache.airavata.sharing.model.UserGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Create and save an initial set of user management groups for a gateway.
 */
@Component
public class GatewayGroupsInitializer {

    private static final Logger logger = LoggerFactory.getLogger(GatewayGroupsInitializer.class);

    private final RegistryService registryService;
    private final SharingRegistryService sharingRegistryService;
    private final CredentialStoreService credentialStoreService;

    public GatewayGroupsInitializer(
            RegistryService registryService,
            SharingRegistryService sharingRegistryService,
            CredentialStoreService credentialStoreService) {
        this.registryService = registryService;
        this.sharingRegistryService = sharingRegistryService;
        this.credentialStoreService = credentialStoreService;
    }

    public GatewayGroups initialize(String gatewayId)
            throws SharingRegistryException, RegistryException, CredentialStoreException {

        logger.info("Creating a GatewayGroups instance for gateway " + gatewayId + " ...");

        GatewayGroups gatewayGroups = new GatewayGroups();
        gatewayGroups.setGatewayId(gatewayId);

        String adminOwnerUsername = getAdminOwnerUsername(registryService, credentialStoreService, gatewayId);
        String ownerId = adminOwnerUsername + "@" + gatewayId;
        if (!sharingRegistryService.isUserExists(gatewayId, ownerId)) {
            User adminUser = new User();
            adminUser.setUserId(ownerId);
            adminUser.setDomainId(gatewayId);
            adminUser.setCreatedTime(AiravataUtils.getUniqueTimestamp().getTime());
            adminUser.setUpdatedTime(AiravataUtils.getUniqueTimestamp().getTime());
            adminUser.setUserName(adminOwnerUsername);
            sharingRegistryService.createUser(adminUser);
        }

        // Gateway Users
        UserGroup gatewayUsersGroup = createGroup(
                sharingRegistryService, gatewayId, ownerId, "Gateway Users", "Default group for users of the gateway.");
        gatewayGroups.setDefaultGatewayUsersGroupId(gatewayUsersGroup.getGroupId());
        // Admin Users
        UserGroup adminUsersGroup =
                createGroup(sharingRegistryService, gatewayId, ownerId, "Admin Users", "Admin users group.");
        gatewayGroups.setAdminsGroupId(adminUsersGroup.getGroupId());
        // Read Only Admin Users
        UserGroup readOnlyAdminsGroup = createGroup(
                sharingRegistryService,
                gatewayId,
                ownerId,
                "Read Only Admin Users",
                "Group of admin users with read-only access.");
        gatewayGroups.setReadOnlyAdminsGroupId(readOnlyAdminsGroup.getGroupId());

        registryService.createGatewayGroups(gatewayGroups);

        return gatewayGroups;
    }

    private UserGroup createGroup(
            SharingRegistryService sharingRegistryService,
            String gatewayId,
            String ownerId,
            String groupName,
            String groupDescription)
            throws SharingRegistryException {

        UserGroup userGroup = new UserGroup();
        userGroup.setGroupId(AiravataUtils.getId(groupName));
        userGroup.setDomainId(gatewayId);
        userGroup.setGroupCardinality(GroupCardinality.MULTI_USER);
        userGroup.setCreatedTime(AiravataUtils.getUniqueTimestamp().getTime());
        userGroup.setUpdatedTime(AiravataUtils.getUniqueTimestamp().getTime());
        userGroup.setName(groupName);
        userGroup.setDescription(groupDescription);
        userGroup.setOwnerId(ownerId);
        userGroup.setGroupType(GroupType.DOMAIN_LEVEL_GROUP);
        sharingRegistryService.createGroup(userGroup);

        return userGroup;
    }

    private String getAdminOwnerUsername(
            RegistryService registryService, CredentialStoreService credentialStoreService, String gatewayId)
            throws RegistryException, CredentialStoreException {

        GatewayResourceProfile gatewayResourceProfile = registryService.getGatewayResourceProfile(gatewayId);
        PasswordCredential credential = credentialStoreService.getPasswordCredential(
                gatewayResourceProfile.getIdentityServerPwdCredToken(), gatewayResourceProfile.getGatewayID());
        String adminUsername = credential.getLoginUserName();
        return adminUsername;
    }
}
