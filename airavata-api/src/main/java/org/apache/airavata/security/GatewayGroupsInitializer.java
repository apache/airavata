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

import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.credential.exceptions.CredentialStoreException;
import org.apache.airavata.model.appcatalog.gatewaygroups.GatewayGroups;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.credential.store.PasswordCredential;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.service.CredentialStoreService;
import org.apache.airavata.service.RegistryService;
import org.apache.airavata.service.SharingRegistryService;
import org.apache.airavata.sharing.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Create and save an initial set of user management groups for a gateway.
 */
@Component
public class GatewayGroupsInitializer {

    private static final Logger logger = LoggerFactory.getLogger(GatewayGroupsInitializer.class);
    private static ApplicationContext applicationContext;

    private final RegistryService registryService;
    private final SharingRegistryService sharingRegistryService;
    private final CredentialStoreService credentialStoreService;

    public GatewayGroupsInitializer(
            ApplicationContext applicationContext,
            RegistryService registryService,
            SharingRegistryService sharingRegistryService,
            CredentialStoreService credentialStoreService) {
        GatewayGroupsInitializer.applicationContext = applicationContext;
        this.registryService = registryService;
        this.sharingRegistryService = sharingRegistryService;
        this.credentialStoreService = credentialStoreService;
    }

    public static synchronized GatewayGroups initializeGatewayGroups(String gatewayId) {
        try {
            if (applicationContext == null) {
                throw new RuntimeException(
                        "ApplicationContext not available. GatewayGroupsInitializer cannot be retrieved.");
            }
            GatewayGroupsInitializer gatewayGroupsInitializer =
                    applicationContext.getBean(GatewayGroupsInitializer.class);
            return gatewayGroupsInitializer.initialize(gatewayId);
        } catch (SharingRegistryException | RegistryServiceException | CredentialStoreException e) {
            throw new RuntimeException("Failed to initialize a GatewayGroups instance for gateway: " + gatewayId, e);
        }
    }

    public GatewayGroupsInitializer(
            RegistryService registryService,
            SharingRegistryService sharingRegistryService,
            CredentialStoreService credentialStoreService) {
        this.registryService = registryService;
        this.sharingRegistryService = sharingRegistryService;
        this.credentialStoreService = credentialStoreService;
    }

    public GatewayGroups initialize(String gatewayId)
            throws SharingRegistryException, RegistryServiceException, CredentialStoreException {

        logger.info("Creating a GatewayGroups instance for gateway " + gatewayId + " ...");

        GatewayGroups gatewayGroups = new GatewayGroups();
        gatewayGroups.setGatewayId(gatewayId);

        String adminOwnerUsername = getAdminOwnerUsername(registryService, credentialStoreService, gatewayId);
        String ownerId = adminOwnerUsername + "@" + gatewayId;
        if (!sharingRegistryService.isUserExists(gatewayId, ownerId)) {
            User adminUser = new User();
            adminUser.setUserId(ownerId);
            adminUser.setDomainId(gatewayId);
            adminUser.setCreatedTime(System.currentTimeMillis());
            adminUser.setUpdatedTime(System.currentTimeMillis());
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
        userGroup.setCreatedTime(System.currentTimeMillis());
        userGroup.setUpdatedTime(System.currentTimeMillis());
        userGroup.setName(groupName);
        userGroup.setDescription(groupDescription);
        userGroup.setOwnerId(ownerId);
        userGroup.setGroupType(GroupType.DOMAIN_LEVEL_GROUP);
        sharingRegistryService.createGroup(userGroup);

        return userGroup;
    }

    private String getAdminOwnerUsername(
            RegistryService registryService, CredentialStoreService credentialStoreService, String gatewayId)
            throws RegistryServiceException, CredentialStoreException {

        GatewayResourceProfile gatewayResourceProfile = registryService.getGatewayResourceProfile(gatewayId);
        PasswordCredential credential = credentialStoreService.getPasswordCredential(
                gatewayResourceProfile.getIdentityServerPwdCredToken(), gatewayResourceProfile.getGatewayID());
        String adminUsername = credential.getLoginUserName();
        return adminUsername;
    }
}
