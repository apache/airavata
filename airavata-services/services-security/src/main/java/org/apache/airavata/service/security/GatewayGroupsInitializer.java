/*
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

package org.apache.airavata.service.security;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.CustosUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.model.appcatalog.gatewaygroups.GatewayGroups;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.client.RegistryServiceClientFactory;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.sharing.registry.models.GroupCardinality;
import org.apache.airavata.sharing.registry.models.GroupType;
import org.apache.airavata.sharing.registry.models.UserGroup;
import org.apache.custos.group.management.client.GroupManagementClient;
import org.apache.custos.iam.service.GroupRepresentation;
import org.apache.custos.iam.service.GroupsResponse;
import org.apache.custos.resource.secret.management.client.ResourceSecretManagementClient;
import org.apache.custos.tenant.management.service.GetTenantResponse;
import org.apache.custos.tenant.manamgement.client.TenantManagementClient;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create and save an initial set of user management groups for a gateway.
 */
public class GatewayGroupsInitializer {

    private final static Logger logger = LoggerFactory.getLogger(KeyCloakSecurityManager.class);

    public static GatewayGroups initializeGatewayGroups(String gatewayId, String custosId) {
        RegistryService.Client registryClient = createRegistryClient();
        try {
            GroupManagementClient groupManagementClient = CustosUtils
                    .getCustosClientProvider().getGroupManagementClient();
            ResourceSecretManagementClient resourceSecretClient = CustosUtils
                    .getCustosClientProvider().getResourceSecretManagementClient();
            TenantManagementClient tenantManagementClient = CustosUtils.getCustosClientProvider()
                    .getTenantManagementClient();

            GatewayGroupsInitializer gatewayGroupsInitializer = new GatewayGroupsInitializer(registryClient,
                    groupManagementClient, resourceSecretClient, tenantManagementClient);
            return gatewayGroupsInitializer.initialize(gatewayId, custosId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize a GatewayGroups instance for gateway: " + gatewayId, e);
        } finally {
            ThriftUtils.close(registryClient);
        }
    }

    private RegistryService.Client registryClient;
    private GroupManagementClient groupManagementClient;
    private ResourceSecretManagementClient credentialStoreClient;
    private TenantManagementClient tenantManagementClient;

    public GatewayGroupsInitializer(RegistryService.Client registryClient, GroupManagementClient groupManagementClient,
                                    ResourceSecretManagementClient credentialStoreClient,
                                    TenantManagementClient tenantManagementClient) {

        this.registryClient = registryClient;
        this.groupManagementClient = groupManagementClient;
        this.credentialStoreClient = credentialStoreClient;
        this.tenantManagementClient = tenantManagementClient;
    }

    public GatewayGroups initialize(String gatewayId, String custosId) throws TException {

        logger.info("Creating a GatewayGroups instance for gateway " + gatewayId + " ...");

        GatewayGroups gatewayGroups = new GatewayGroups();
        gatewayGroups.setGatewayId(gatewayId);

        GetTenantResponse getTenantResponse = tenantManagementClient.getTenant(custosId);

        String ownerId = getTenantResponse.getAdminUsername();


        // Gateway Users
        UserGroup gatewayUsersGroup = createGroup(groupManagementClient, gatewayId, ownerId,
                "Gateway Users",
                "Default group for users of the gateway.", custosId);
        gatewayGroups.setDefaultGatewayUsersGroupId(gatewayUsersGroup.getGroupId());
        // Admin Users
        UserGroup adminUsersGroup = createGroup(groupManagementClient, gatewayId, ownerId,
                "Admin Users",
                "Admin users group.", custosId);
        gatewayGroups.setAdminsGroupId(adminUsersGroup.getGroupId());
        // Read Only Admin Users
        UserGroup readOnlyAdminsGroup = createGroup(groupManagementClient, gatewayId, ownerId,
                "Read Only Admin Users",
                "Group of admin users with read-only access.", custosId);
        gatewayGroups.setReadOnlyAdminsGroupId(readOnlyAdminsGroup.getGroupId());

        registryClient.createGatewayGroups(gatewayGroups);

        return gatewayGroups;
    }


    private UserGroup createGroup(GroupManagementClient groupManagementClient, String gatewayId, String ownerId,
                                  String groupName, String groupDescription, String custosId) throws TException {

        UserGroup userGroup = new UserGroup();
        userGroup.setDomainId(gatewayId);
        userGroup.setGroupCardinality(GroupCardinality.MULTI_USER);
        userGroup.setCreatedTime(System.currentTimeMillis());
        userGroup.setUpdatedTime(System.currentTimeMillis());
        userGroup.setName(groupName);
        userGroup.setDescription(groupDescription);
        userGroup.setOwnerId(ownerId);
        userGroup.setGroupType(GroupType.DOMAIN_LEVEL_GROUP);

        GroupRepresentation groupRepresentation = GroupRepresentation
                .newBuilder()
                .setName(groupName)
                .setDescription(groupDescription)
                .setOwnerId(ownerId)
                .build();

        GroupRepresentation[] representations = {groupRepresentation};

        GroupsResponse groupsResponse = groupManagementClient.createGroup(custosId, representations);

        String groupId = groupsResponse.getGroupsList().get(0).getId();
        userGroup.setGroupId(groupId);
        return userGroup;
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


}
