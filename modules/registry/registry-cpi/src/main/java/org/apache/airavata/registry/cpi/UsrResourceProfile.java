/**
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
     * This method will add a gateway profile
     * @param userResourceProfile object of User resource profile
     * @return gateway id
     */
    String addUserResourceProfile(UserResourceProfile userResourceProfile) throws AppCatalogException;

    /**
     * This method will update a gateway profile
     * @param userId unique User id
     * @param gatewayId unique gateway id
     * @param updatedProfile updated profile
     */
    void updateUserResourceProfile(String userId, String gatewayId, UserResourceProfile updatedProfile) throws AppCatalogException;

    /**
     * @param userId
     * @param gatewayId
     * @return
     */
    UserResourceProfile getUserResourceProfile(String userId, String gatewayId) throws AppCatalogException;

    /**
     * This method will remove a gateway profile
     * @param userId
     * @param gatewayId unique gateway id
     * @return true or false
     */
    boolean removeUserResourceProfile(String userId, String gatewayId) throws AppCatalogException;
    boolean removeUserComputeResourcePreferenceFromGateway(String userId, String gatewayId, String preferenceId) throws AppCatalogException;
    boolean removeUserDataStoragePreferenceFromGateway(String userId, String gatewayId, String preferenceId) throws AppCatalogException;

    /**
     * This method will check whether gateway profile exists
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
     * @return ComputeResourcePreference
     */
    UserComputeResourcePreference getUserComputeResourcePreference(String userId, String gatewayId, String hostId) throws AppCatalogException;
    UserStoragePreference getUserStoragePreference(String userId, String gatewayId, String storageId) throws AppCatalogException;


    List<String> getGatewayProfileIds(String gatewayName) throws AppCatalogException;

    /* Implementing this method is critical to validate User Resource Profile
     *
     */
    String getUserNamefromID(String userId, String gatewayID) throws AppCatalogException;

    List<UserComputeResourcePreference> getAllUserComputeResourcePreferences (String userId, String gatewayId) throws AppCatalogException;
    List<UserStoragePreference> getAllUserStoragePreferences(String userId, String gatewayId) throws AppCatalogException;

    List<UserResourceProfile> getAllUserResourceProfiles() throws AppCatalogException;
}
