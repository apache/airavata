/**
 *
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
 */
package org.apache.airavata.registry.cpi;

import org.apache.airavata.model.appcatalog.userresourceprofile.UserResourceProfile;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserStoragePreference;
import java.util.List;

public interface UsrResourceProfile {
    /**
     * This method will add user resource profile
     * @param userResourceProfile object of User resource profile
     * @return gateway id
     */
    String addUserResourceProfile(UserResourceProfile userResourceProfile) throws AppCatalogException;

    /**
     * This method will update user resource profile
     * @param userId unique User id
     * @param gatewayId unique gateway id
     * @param updatedProfile updated profile
     */
    void updateUserResourceProfile(String userId, String gatewayId, UserResourceProfile updatedProfile) throws AppCatalogException;

    /**
     * @param userId
     * @param gatewayId
     * @return UserResourceProfile
     */
    UserResourceProfile getUserResourceProfile(String userId, String gatewayId) throws AppCatalogException;

    /**
     * This method will remove a user resource profile
     * @param userId
     * @param gatewayId unique gateway id
     * @return true or false
     */
    boolean removeUserResourceProfile(String userId, String gatewayId) throws AppCatalogException;

    /**
     * This method will remove a user compute resource preference
     * @param userId
     * @param gatewayId unique gateway id
     * @param preferenceId
     * @return true or false
     */
    boolean removeUserComputeResourcePreferenceFromGateway(String userId, String gatewayId, String preferenceId) throws AppCatalogException;

    /**
     * This method will remove a user storage preference
     * @param userId
     * @param gatewayId unique gateway id
     * @param preferenceId
     * @return true or false
     */
    boolean removeUserDataStoragePreferenceFromGateway(String userId, String gatewayId, String preferenceId) throws AppCatalogException;

    /**
     * This method will check whether user resource profile exists
     * @param userId
     * @param gatewayId unique gateway id
     * @return true or false
     */
    boolean isUserResourceProfileExists(String userId, String gatewayId) throws AppCatalogException;

    /**
     *
     * @param userId
     * @param gatewayId
     * @param hostId
     * @return UserComputeResourcePreference
     */
    UserComputeResourcePreference getUserComputeResourcePreference(String userId, String gatewayId, String hostId) throws AppCatalogException;

    /**
     * @param userId
     * @param gatewayId
     * @return UserStoragePreference
     */
    UserStoragePreference getUserStoragePreference(String userId, String gatewayId, String storageId) throws AppCatalogException;

    /**
     * @param gatewayName
     * @return List of gateway ids
     */
    List<String> getGatewayProfileIds(String gatewayName) throws AppCatalogException;

    /**
     * @param userId
     * @param gatewayID
     * @return username
     */
    String getUserNamefromID(String userId, String gatewayID) throws AppCatalogException;

    /**
     * @param userId
     * @param gatewayId
     * @return List of UserComputeResourcePreference for given user and gateway
     */
    List<UserComputeResourcePreference> getAllUserComputeResourcePreferences (String userId, String gatewayId) throws AppCatalogException;

    /**
     * @param userId
     * @param gatewayId
     * @return List of UserStoragePreference for given user and gateway
     */
    List<UserStoragePreference> getAllUserStoragePreferences(String userId, String gatewayId) throws AppCatalogException;

    /**
     * @return List of user resource profiles
     */
    List<UserResourceProfile> getAllUserResourceProfiles() throws AppCatalogException;
}
