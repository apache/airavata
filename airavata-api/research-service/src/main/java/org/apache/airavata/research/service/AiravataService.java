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
package org.apache.airavata.research.service;

import org.apache.airavata.config.UserContext;
import org.apache.airavata.interfaces.UserProfileProvider;
import org.apache.airavata.model.user.proto.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("researchAiravataService")
public class AiravataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiravataService.class);

    private final UserProfileProvider userProfileProvider;

    public AiravataService(UserProfileProvider userProfileProvider) {
        this.userProfileProvider = userProfileProvider;
    }

    public UserProfile getUserProfile(String userId) {
        UserProfile profile = userProfileProvider.getUserProfileByIdAndGateWay(userId, UserContext.gatewayId());
        if (profile == null) {
            throw new RuntimeException("User profile not found for id: " + userId);
        }
        return profile;
    }

    public UserProfile getUserProfile(String authzToken, String userId, String gatewayId) {
        UserProfile profile = userProfileProvider.getUserProfileByIdAndGateWay(userId, gatewayId);
        if (profile == null) {
            throw new RuntimeException("User profile not found for id: " + userId + " in gateway: " + gatewayId);
        }
        return profile;
    }
}
