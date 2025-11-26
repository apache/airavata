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
package org.apache.airavata.service.profile.handlers;

import java.util.List;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.model.error.AuthorizationException;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.service.profile.user.cpi.UserProfileService;
import org.apache.airavata.service.profile.user.cpi.exception.UserProfileServiceException;
import org.apache.airavata.service.profile.user.cpi.profile_user_cpiConstants;
import org.apache.airavata.service.security.interceptor.SecurityCheck;
import org.apache.thrift.TException;

public class UserProfileServiceHandler implements UserProfileService.Iface {

    private org.apache.airavata.service.UserProfileService userProfileService;

    public UserProfileServiceHandler() {
        userProfileService = new org.apache.airavata.service.UserProfileService();
    }

    @Override
    public String getAPIVersion() throws AiravataSystemException {
        return profile_user_cpiConstants.USER_PROFILE_CPI_VERSION;
    }

    @Override
    @SecurityCheck
    public String initializeUserProfile(AuthzToken authzToken)
            throws UserProfileServiceException, AuthorizationException, TException {
        return userProfileService.initializeUserProfile(authzToken);
    }

    @Override
    @SecurityCheck
    public String addUserProfile(AuthzToken authzToken, UserProfile userProfile)
            throws UserProfileServiceException, AuthorizationException, TException {
        return userProfileService.addUserProfile(authzToken, userProfile);
    }

    @Override
    @SecurityCheck
    public boolean updateUserProfile(AuthzToken authzToken, UserProfile userProfile)
            throws UserProfileServiceException, AuthorizationException, TException {
        return userProfileService.updateUserProfile(authzToken, userProfile);
    }

    @Override
    @SecurityCheck
    public UserProfile getUserProfileById(AuthzToken authzToken, String userId, String gatewayId)
            throws UserProfileServiceException, AuthorizationException, TException {
        return userProfileService.getUserProfileById(authzToken, userId, gatewayId);
    }

    @Override
    @SecurityCheck
    public boolean deleteUserProfile(AuthzToken authzToken, String userId, String gatewayId)
            throws UserProfileServiceException, AuthorizationException, TException {
        return userProfileService.deleteUserProfile(authzToken, userId, gatewayId);
    }

    @Override
    @SecurityCheck
    public List<UserProfile> getAllUserProfilesInGateway(AuthzToken authzToken, String gatewayId, int offset, int limit)
            throws UserProfileServiceException, AuthorizationException, TException {
        return userProfileService.getAllUserProfilesInGateway(authzToken, gatewayId, offset, limit);
    }

    @Override
    public boolean doesUserExist(AuthzToken authzToken, String userId, String gatewayId)
            throws UserProfileServiceException, AuthorizationException, TException {
        return userProfileService.doesUserExist(authzToken, userId, gatewayId);
    }
}
