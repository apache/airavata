/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.research.service;

import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.research.service.model.UserContext;
import org.apache.airavata.service.profile.client.ProfileServiceClientFactory;
import org.apache.airavata.service.profile.user.cpi.UserProfileService;
import org.apache.airavata.service.profile.user.cpi.exception.UserProfileServiceException;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AiravataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiravataService.class);

    @Value("${airavata.user-profile.server.url:api.dev.cybershuttle.org}")
    private String profileServerUrl;

    @Value("${airavata.user-profile.server.port:8962}")
    private int profileServerPort;

    public UserProfileService.Client userProfileClient() {
        try {
            LOGGER.info("User profile client initialized");
            return ProfileServiceClientFactory.createUserProfileServiceClient(profileServerUrl, profileServerPort);
        } catch (UserProfileServiceException e) {
            LOGGER.error("Error while creating user profile client", e);
            throw new RuntimeException(e);
        }
    }

    public UserProfile getUserProfile(String userId) {
        try {
            return userProfileClient().getUserProfileById(UserContext.authzToken(), userId, UserContext.gatewayId());
        } catch (TException e) {
            LOGGER.error("Error while getting user profile with the id: {}", userId, e);
            throw new RuntimeException("Error while getting user profile with the id: " + userId, e);
        }
    }


    public UserProfile getUserProfile(AuthzToken authzToken, String userId, String gatewayId) {
        try {
            return userProfileClient().getUserProfileById(authzToken, userId, gatewayId);
        } catch (TException e) {
            LOGGER.error("Error while getting user profile with the id: {} in the gateway: {}", userId, gatewayId, e);
            throw new RuntimeException("Error while getting user profile with the id: " + userId + " in the gateway: " + gatewayId, e);
        }
    }
}
