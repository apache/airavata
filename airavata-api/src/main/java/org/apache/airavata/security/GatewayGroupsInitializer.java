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

import org.apache.airavata.catalog.sharing.models.*;
import org.apache.airavata.catalog.sharing.service.cpi.SharingRegistryService;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.credential.store.exception.CredentialStoreException;
import org.apache.airavata.factory.AiravataClientFactory;
import org.apache.airavata.factory.AiravataServiceFactory;
import org.apache.airavata.model.appcatalog.gatewaygroups.GatewayGroups;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.credential.store.PasswordCredential;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create and save an initial set of user management groups for a gateway.
 */
public class GatewayGroupsInitializer {

    private static final Logger logger = LoggerFactory.getLogger(KeyCloakSecurityManager.class);

    public static synchronized GatewayGroups initializeGatewayGroups(String gatewayId) {

        SharingRegistryService.Iface sharingRegistry = getSharingRegistry();
        RegistryService.Iface registry = getRegistry();
        CredentialStoreService.Iface credentialStore = getCredentialStore();
        try {
            GatewayGroupsInitializer gatewayGroupsInitializer =
                    new GatewayGroupsInitializer(registry, sharingRegistry, credentialStore);
            return gatewayGroupsInitializer.initialize(gatewayId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize a GatewayGroups instance for gateway: " + gatewayId, e);
        }
    }

    private final RegistryService.Iface registry;
    private final SharingRegistryService.Iface sharingRegistry;
    private final CredentialStoreService.Iface credentialStore;

    public GatewayGroupsInitializer(
            RegistryService.Iface registry,
            SharingRegistryService.Iface sharingRegistry,
            CredentialStoreService.Iface credentialStore) {

        this.registry = registry;
        this.sharingRegistry = sharingRegistry;
        this.credentialStore = credentialStore;
    }

    public GatewayGroups initialize(String gatewayId) throws TException {

        logger.info("Creating a GatewayGroups instance for gateway " + gatewayId + " ...");

        GatewayGroups gatewayGroups = new GatewayGroups();
        gatewayGroups.setGatewayId(gatewayId);

        String adminOwnerUsername = getAdminOwnerUsername(registry, credentialStore, gatewayId);
        String ownerId = adminOwnerUsername + "@" + gatewayId;
        if (!sharingRegistry.isUserExists(gatewayId, ownerId)) {
            User adminUser = new User();
            adminUser.setUserId(ownerId);
            adminUser.setDomainId(gatewayId);
            adminUser.setCreatedTime(System.currentTimeMillis());
            adminUser.setUpdatedTime(System.currentTimeMillis());
            adminUser.setUserName(adminOwnerUsername);
            sharingRegistry.createUser(adminUser);
        }

        // Gateway Users
        UserGroup gatewayUsersGroup = createGroup(
                sharingRegistry, gatewayId, ownerId, "Gateway Users", "Default group for users of the gateway.");
        gatewayGroups.setDefaultGatewayUsersGroupId(gatewayUsersGroup.getGroupId());
        // Admin Users
        UserGroup adminUsersGroup =
                createGroup(sharingRegistry, gatewayId, ownerId, "Admin Users", "Admin users group.");
        gatewayGroups.setAdminsGroupId(adminUsersGroup.getGroupId());
        // Read Only Admin Users
        UserGroup readOnlyAdminsGroup = createGroup(
                sharingRegistry,
                gatewayId,
                ownerId,
                "Read Only Admin Users",
                "Group of admin users with read-only access.");
        gatewayGroups.setReadOnlyAdminsGroupId(readOnlyAdminsGroup.getGroupId());

        try {
            registry.createGatewayGroups(gatewayGroups);
        } catch (TException e) {
            logger.error(
                    "Gateway groups created in Sharing Catalog failed to save GatewayGroups entity in Registry", e);
            throw e;
        }

        return gatewayGroups;
    }

    private UserGroup createGroup(
            SharingRegistryService.Iface sharingRegistry,
            String gatewayId,
            String ownerId,
            String groupName,
            String groupDescription)
            throws TException {

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
        sharingRegistry.createGroup(userGroup);

        return userGroup;
    }

    private String getAdminOwnerUsername(
            RegistryService.Iface registry, CredentialStoreService.Iface credentialStore, String gatewayId)
            throws TException {

        GatewayResourceProfile gatewayResourceProfile = registry.getGatewayResourceProfile(gatewayId);
        PasswordCredential credential = credentialStore.getPasswordCredential(
                gatewayResourceProfile.getIdentityServerPwdCredToken(), gatewayResourceProfile.getGatewayID());
        return credential.getLoginUserName();
    }

    private static SharingRegistryService.Iface getSharingRegistry() {
        return AiravataServiceFactory.getSharingRegistry();
    }

    private static RegistryService.Iface getRegistry() {
        return AiravataServiceFactory.getRegistry();
    }

    private static CredentialStoreService.Iface getCredentialStore() {
        try {
            final int serverPort = Integer.parseInt(ServerSettings.getApiServerPort());
            final String serverHost = ServerSettings.getApiServerHost();
            return AiravataClientFactory.getCredentialStore(serverHost, serverPort);
        } catch (CredentialStoreException e) {
            throw new RuntimeException("Unable to create credential store client...", e);
        }
    }
}
