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
package org.apache.airavata.security.service;

import org.apache.airavata.common.config.ServerSettings;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.util.AiravataUtils;
import org.apache.airavata.common.util.ThriftUtils;
import org.apache.airavata.credential.handler.CredentialStoreServerHandler;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.execution.util.RegistryServiceClientFactory;
import org.apache.airavata.model.appcatalog.gatewaygroups.GatewayGroups;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.credential.store.PasswordCredential;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.sharing.handler.SharingRegistryServerHandler;
import org.apache.airavata.sharing.registry.models.*;
import org.apache.airavata.sharing.registry.service.cpi.SharingRegistryService;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create and save an initial set of user management groups for a gateway.
 */
public class GatewayGroupsInitializer {

    private static final Logger logger = LoggerFactory.getLogger(KeyCloakSecurityManager.class);

    public static synchronized GatewayGroups initializeGatewayGroups(String gatewayId) {

        SharingRegistryService.Iface sharingRegistryHandler;
        try {
            sharingRegistryHandler = new SharingRegistryServerHandler();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SharingRegistryServerHandler", e);
        }
        RegistryService.Client registryClient = createRegistryClient();
        CredentialStoreService.Iface credentialStoreHandler = createCredentialStoreHandler();
        try {
            GatewayGroupsInitializer gatewayGroupsInitializer =
                    new GatewayGroupsInitializer(registryClient, sharingRegistryHandler, credentialStoreHandler);
            return gatewayGroupsInitializer.initialize(gatewayId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize a GatewayGroups instance for gateway: " + gatewayId, e);
        } finally {
            ThriftUtils.close(registryClient);
        }
    }

    private RegistryService.Client registryClient;
    private SharingRegistryService.Iface sharingRegistryClient;
    private CredentialStoreService.Iface credentialStoreClient;

    public GatewayGroupsInitializer(
            RegistryService.Client registryClient,
            SharingRegistryService.Iface sharingRegistryClient,
            CredentialStoreService.Iface credentialStoreClient) {

        this.registryClient = registryClient;
        this.sharingRegistryClient = sharingRegistryClient;
        this.credentialStoreClient = credentialStoreClient;
    }

    public GatewayGroups initialize(String gatewayId) throws TException {

        logger.info("Creating a GatewayGroups instance for gateway " + gatewayId + " ...");

        GatewayGroups gatewayGroups = new GatewayGroups();
        gatewayGroups.setGatewayId(gatewayId);

        String adminOwnerUsername = getAdminOwnerUsername(registryClient, credentialStoreClient, gatewayId);
        String ownerId = adminOwnerUsername + "@" + gatewayId;
        if (!sharingRegistryClient.isUserExists(gatewayId, ownerId)) {
            User adminUser = new User();
            adminUser.setUserId(ownerId);
            adminUser.setDomainId(gatewayId);
            adminUser.setCreatedTime(System.currentTimeMillis());
            adminUser.setUpdatedTime(System.currentTimeMillis());
            adminUser.setUserName(adminOwnerUsername);
            sharingRegistryClient.createUser(adminUser);
        }

        // Gateway Users
        UserGroup gatewayUsersGroup = createGroup(
                sharingRegistryClient, gatewayId, ownerId, "Gateway Users", "Default group for users of the gateway.");
        gatewayGroups.setDefaultGatewayUsersGroupId(gatewayUsersGroup.getGroupId());
        // Admin Users
        UserGroup adminUsersGroup =
                createGroup(sharingRegistryClient, gatewayId, ownerId, "Admin Users", "Admin users group.");
        gatewayGroups.setAdminsGroupId(adminUsersGroup.getGroupId());
        // Read Only Admin Users
        UserGroup readOnlyAdminsGroup = createGroup(
                sharingRegistryClient,
                gatewayId,
                ownerId,
                "Read Only Admin Users",
                "Group of admin users with read-only access.");
        gatewayGroups.setReadOnlyAdminsGroupId(readOnlyAdminsGroup.getGroupId());

        try {
            registryClient.createGatewayGroups(gatewayGroups);
        } catch (TException e) {
            logger.error(
                    "Gateway groups created in Sharing Catalog failed to save GatewayGroups entity in Registry", e);
            throw e;
        }

        return gatewayGroups;
    }

    private UserGroup createGroup(
            SharingRegistryService.Iface sharingRegistryClient,
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
        sharingRegistryClient.createGroup(userGroup);

        return userGroup;
    }

    private String getAdminOwnerUsername(
            RegistryService.Client registryClient,
            CredentialStoreService.Iface credentialStoreClient,
            String gatewayId)
            throws TException {

        GatewayResourceProfile gatewayResourceProfile = registryClient.getGatewayResourceProfile(gatewayId);
        PasswordCredential credential = credentialStoreClient.getPasswordCredential(
                gatewayResourceProfile.getIdentityServerPwdCredToken(), gatewayResourceProfile.getGatewayID());
        String adminUsername = credential.getLoginUserName();
        return adminUsername;
    }

    private static RegistryService.Client createRegistryClient() {
        try {
            final int serverPort = Integer.parseInt(ServerSettings.getRegistryServerPort());
            final String serverHost = ServerSettings.getRegistryServerHost();
            return RegistryServiceClientFactory.createRegistryClient(serverHost, serverPort);
        } catch (ApplicationSettingsException | RegistryServiceException e) {
            throw new RuntimeException("Unable to create registry client...", e);
        }
    }

    private static CredentialStoreService.Iface createCredentialStoreHandler() {
        try {
            return new CredentialStoreServerHandler();
        } catch (Exception e) {
            throw new RuntimeException("Unable to create CredentialStoreServerHandler...", e);
        }
    }
}
