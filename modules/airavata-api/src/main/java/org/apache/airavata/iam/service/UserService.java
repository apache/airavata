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

import java.util.List;
import org.apache.airavata.core.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.iam.exception.IamAdminServicesException;
import org.apache.airavata.iam.exception.UserProfileServiceException;
import org.apache.airavata.iam.model.AuthzToken;
import org.apache.airavata.iam.model.UserProfile;

/**
 * Service for managing users in the registry.
 * Uses the unified OIDC-based UserEntity from registry.entities.
 */
public interface UserService {

    boolean isUserExists(String gatewayId, String userName) throws RegistryException;

    List<String> getAllUsernamesInGateway(String gatewayName) throws RegistryException;

    UserProfile addUser(UserProfile userProfile) throws RegistryException;

    UserProfile get(String userId, String gatewayId) throws RegistryException;

    UserProfile getByInternalUserId(String airavataInternalUserId) throws RegistryException;

    void delete(String userId, String gatewayId) throws RegistryException;

    void deleteByInternalUserId(String airavataInternalUserId) throws RegistryException;

    // --- UserProfile operations (merged from UserProfileService) ---

    String initializeUserProfile(AuthzToken authzToken) throws UserProfileServiceException;

    String addUserProfile(AuthzToken authzToken, UserProfile userProfile)
            throws UserProfileServiceException, IamAdminServicesException;

    boolean updateUserProfile(AuthzToken authzToken, UserProfile userProfile)
            throws UserProfileServiceException, IamAdminServicesException;

    UserProfile getUserProfileById(AuthzToken authzToken, String userId, String gatewayId)
            throws UserProfileServiceException;

    boolean deleteUserProfile(AuthzToken authzToken, String userId, String gatewayId)
            throws UserProfileServiceException;

    List<UserProfile> getAllUserProfilesInGateway(AuthzToken authzToken, String gatewayId, int offset, int limit)
            throws UserProfileServiceException;

    boolean doesUserExist(AuthzToken authzToken, String userId, String gatewayId) throws UserProfileServiceException;

    UserProfile getUserProfileByIdAndGateWay(String userId, String gatewayId);

    UserProfile createUserProfile(UserProfile userProfile);

    UserProfile getUserProfileByAiravataInternalUserId(String userId);
}
