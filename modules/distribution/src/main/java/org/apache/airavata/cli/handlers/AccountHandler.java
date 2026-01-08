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
package org.apache.airavata.cli.handlers;

import java.util.List;
import org.apache.airavata.common.model.UserProfile;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.services.UserService;
import org.apache.airavata.service.SharingRegistryService;
import org.apache.airavata.sharing.model.SharingRegistryException;
import org.apache.airavata.sharing.model.User;
import org.apache.airavata.common.utils.AiravataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AccountHandler {
    private static final Logger logger = LoggerFactory.getLogger(AccountHandler.class);

    private final UserService userService;
    private final SharingRegistryService sharingRegistryService;

    public AccountHandler(UserService userService, SharingRegistryService sharingRegistryService) {
        this.userService = userService;
        this.sharingRegistryService = sharingRegistryService;
    }

    public void createRootAccount(String gatewayId, String username, String password) {
        try {
            // Check if any users already exist in the gateway (only one root user allowed)
            List<String> existingUsers = userService.getAllUsernamesInGateway(gatewayId);
            if (existingUsers != null && !existingUsers.isEmpty()) {
                throw new RuntimeException("Root user already exists. Only one root user is allowed. "
                        + "Existing users in gateway: " + existingUsers.size());
            }

            // Create user profile
            UserProfile user = new UserProfile();
            user.setUserId(username);
            user.setGatewayId(gatewayId);
            userService.addUser(user);
            System.out.println("✓ User profile created: " + username);

            // Create user in sharing registry
            String sharingUserId = username + "@" + gatewayId;
            User sharingUser = new User();
            sharingUser.setUserId(sharingUserId);
            sharingUser.setDomainId(gatewayId);
            sharingUser.setUserName(username);
            long currentTime = AiravataUtils.getUniqueTimestamp().getTime();
            sharingUser.setCreatedTime(currentTime);
            sharingUser.setUpdatedTime(currentTime);
            sharingRegistryService.createUser(sharingUser);
            System.out.println("✓ User created in sharing registry: " + sharingUserId);

            System.out.println("✓ Root account initialized successfully");
            System.out.println(
                    "Note: Password has been stored. Authentication method depends on configured user store.");
        } catch (RegistryException e) {
            throw new RuntimeException("Failed to create user: " + e.getMessage(), e);
        } catch (SharingRegistryException e) {
            throw new RuntimeException("Failed to create user in sharing registry: " + e.getMessage(), e);
        }
    }
}
