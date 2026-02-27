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

import org.apache.airavata.config.ServerProperties;
import org.apache.airavata.core.util.IdGenerator;
import org.apache.airavata.gateway.model.GatewayGroups;
import org.apache.airavata.gateway.service.GatewayService;
import org.apache.airavata.iam.exception.SharingRegistryException;
import org.apache.airavata.iam.model.GroupCardinality;
import org.apache.airavata.iam.model.GroupType;
import org.apache.airavata.iam.model.User;
import org.apache.airavata.iam.model.UserGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Create and save an initial set of user management groups for a gateway.
 */
@Component
public class GatewayGroupsInitializer {

    private static final Logger logger = LoggerFactory.getLogger(GatewayGroupsInitializer.class);

    private final GatewayService gatewayGroupsService;
    private final SharingService sharingService;
    private final ServerProperties properties;

    public GatewayGroupsInitializer(
            GatewayService gatewayGroupsService, SharingService sharingService, ServerProperties properties) {
        this.gatewayGroupsService = gatewayGroupsService;
        this.sharingService = sharingService;
        this.properties = properties;
    }

    public GatewayGroups initialize(String gatewayId)
            throws SharingRegistryException, org.apache.airavata.core.exception.RegistryExceptions.RegistryException {

        logger.info("Creating a GatewayGroups instance for gateway {} ...", gatewayId);

        GatewayGroups gatewayGroups = new GatewayGroups();
        gatewayGroups.setGatewayId(gatewayId);

        String adminOwnerUsername = properties.security().iam().superAdmin().username();
        String ownerId = adminOwnerUsername + "@" + gatewayId;
        if (!sharingService.isUserExists(gatewayId, ownerId)) {
            User adminUser = new User();
            adminUser.setUserId(ownerId);
            adminUser.setDomainId(gatewayId);
            adminUser.setCreatedTime(IdGenerator.getUniqueTimestamp().toEpochMilli());
            adminUser.setUpdatedTime(IdGenerator.getUniqueTimestamp().toEpochMilli());
            adminUser.setUserName(adminOwnerUsername);
            sharingService.createUser(adminUser);
        }

        // Gateway Users
        UserGroup gatewayUsersGroup = createGroup(
                sharingService, gatewayId, ownerId, "Gateway Users", "Default group for users of the gateway.");
        gatewayGroups.setDefaultGatewayUsersGroupId(gatewayUsersGroup.getGroupId());
        // Admin Users
        UserGroup adminUsersGroup =
                createGroup(sharingService, gatewayId, ownerId, "Admin Users", "Admin users group.");
        gatewayGroups.setAdminsGroupId(adminUsersGroup.getGroupId());
        // Read Only Admin Users
        UserGroup readOnlyAdminsGroup = createGroup(
                sharingService,
                gatewayId,
                ownerId,
                "Read Only Admin Users",
                "Group of admin users with read-only access.");
        gatewayGroups.setReadOnlyAdminsGroupId(readOnlyAdminsGroup.getGroupId());

        gatewayGroupsService.createGatewayGroups(gatewayGroups);

        return gatewayGroups;
    }

    private UserGroup createGroup(
            SharingService sharingService, String gatewayId, String ownerId, String groupName, String groupDescription)
            throws SharingRegistryException {

        UserGroup userGroup = new UserGroup();
        userGroup.setGroupId(IdGenerator.getId(groupName));
        userGroup.setDomainId(gatewayId);
        userGroup.setGroupCardinality(GroupCardinality.MULTI_USER);
        userGroup.setCreatedTime(IdGenerator.getUniqueTimestamp().toEpochMilli());
        userGroup.setUpdatedTime(IdGenerator.getUniqueTimestamp().toEpochMilli());
        userGroup.setName(groupName);
        userGroup.setDescription(groupDescription);
        userGroup.setOwnerId(ownerId);
        userGroup.setGroupType(GroupType.DOMAIN_LEVEL_GROUP);
        sharingService.createGroup(userGroup);

        return userGroup;
    }
}
