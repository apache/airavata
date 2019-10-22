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
package org.apache.airavata.service.profile.iam.admin.services.core.interfaces;

import org.apache.airavata.model.credential.store.PasswordCredential;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.service.profile.iam.admin.services.cpi.exception.IamAdminServicesException;

import java.util.List;

public interface TenantManagementInterface {

    /**
     * Method to add Identity server tenant for Airavata gateway creation.
     *
     * @param isSuperAdminPasswordCreds identity server super admin credentials
     * @param gatewayDetails gateway details from workspace catalog
     * @return Gateway object.
     */
    Gateway addTenant(PasswordCredential isSuperAdminPasswordCreds, Gateway gatewayDetails) throws IamAdminServicesException;

    /**
     * Method to add tenant Admin account in Identity Server.
     *
     * @param isSuperAdminPasswordCreds identity server super admin credentials
     * @param gatewayDetails gateway details from workspace catalog
     * @param gatewayAdminPassword password to use when creating tenant admin account
     * @return Gateway object.
     */
    boolean createTenantAdminAccount(PasswordCredential isSuperAdminPasswordCreds, Gateway gatewayDetails, String gatewayAdminPassword) throws IamAdminServicesException;

    /**
     * Method to configure application client in Identity Server
     *
     * @param isSuperAdminPasswordCreds identity server super admin credentials
     * @param gatewayDetails gateway details from workspace catalog
     * @return Gateway object.
     */
    Gateway configureClient(PasswordCredential isSuperAdminPasswordCreds, Gateway gatewayDetails) throws IamAdminServicesException;

    /**
     * Check if username is available to be used for creating a new user account.
     * @param accessToken needs to have access to searching across users by username
     * @param tenantId
     * @param username
     * @return
     */
    boolean isUsernameAvailable(String accessToken, String tenantId, String username) throws IamAdminServicesException;

    /**
     * Method to create user in Identity Server
     *
     * @param accessToken
     * @param tenantId
     * @param username
     * @param emailAddress
     * @param firstName
     * @param lastName
     * @param newPassword
     * @return true if user created
     * @throws IamAdminServicesException
     */
    boolean createUser(String accessToken, String tenantId, String username, String emailAddress, String firstName, String lastName, String newPassword) throws IamAdminServicesException;

    /**
     * Method to enable user in Identity Server
     *
     * @param accessToken
     * @param tenantId
     * @param username
     * @return boolean.
     */
    boolean enableUserAccount(String accessToken, String tenantId, String username) throws IamAdminServicesException;

    /**
     * Method to check if user is enabled in Identity Server
     *
     * @param accessToken
     * @param tenantId
     * @param username
     * @return boolean.
     */
    boolean isUserAccountEnabled(String accessToken, String tenantId, String username) throws IamAdminServicesException;

    /**
     * Return whether user exists with username.
     *
     * @param accessToken
     * @param tenantId
     * @param username
     * @return
     */
    boolean isUserExist(String accessToken, String tenantId, String username) throws IamAdminServicesException;

    /**
     * Get user profile information from Identity Server
     *
     * @param accessToken
     * @param tenantId
     * @param username
     * @return
     */
    UserProfile getUser(String accessToken, String tenantId, String username) throws IamAdminServicesException;

    /**
     * Get a paginated list of user profiles from Identity Server
     * 
     * @param accessToken
     * @param tenantId
     * @param offset
     * @param limit
     * @param search String - optional, if specified used to search user profiles
     * @return
     * @throws IamAdminServicesException
     */
    List<UserProfile> getUsers(String accessToken, String tenantId, int offset, int limit, String search) throws IamAdminServicesException;

    /**
     * Method to reset user password in Identity Server
     *
     * @param accessToken
     * @param tenantId
     * @param username
     * @param newPassword
     * @return boolean
     */
    boolean resetUserPassword(String accessToken, String tenantId, String username, String newPassword) throws IamAdminServicesException;

    /**
     * Method to find user in Identity Server
     *
     * @param accessToken
     * @param tenantId required
     * @param email required
     * @param username can be null
     * @return Gateway object.
     */
    List<UserProfile> findUser(String accessToken, String tenantId, String email, String username) throws IamAdminServicesException;

    /**
     * Update the user's profile in the Identity Server
     * @param accessToken
     * @param tenantId
     * @param username
     * @param userDetails
     */
    void updateUserProfile(String accessToken, String tenantId, String username, UserProfile userDetails) throws IamAdminServicesException;

    /**
     * Delete this user from the IAM service.
     * @param accessToken
     * @param tenantId
     * @param username
     * @return
     * @throws IamAdminServicesException
     */
    boolean deleteUser(String accessToken, String tenantId, String username) throws IamAdminServicesException;

    /**
     * Add the given role to the user.
     *
     * @param realmAdminCreds
     * @param tenantId
     * @param username
     * @param roleName
     * @return
     * @throws IamAdminServicesException
     */
    @Deprecated
    boolean addRoleToUser(PasswordCredential realmAdminCreds, String tenantId, String username, String roleName) throws IamAdminServicesException;

    /**
     * Remove the given role from the user.
     *
     * @param realmAdminCreds
     * @param tenantId
     * @param username
     * @param roleName
     * @return
     * @throws IamAdminServicesException
     */
    @Deprecated
    boolean removeRoleFromUser(PasswordCredential realmAdminCreds, String tenantId, String username, String roleName) throws IamAdminServicesException;

    /**
     * Get all users having the given role.
     *
     * @param realmAdminCreds
     * @param tenantId
     * @param roleName
     * @return
     * @throws IamAdminServicesException
     */
    @Deprecated
    List<UserProfile> getUsersWithRole(PasswordCredential realmAdminCreds, String tenantId, String roleName) throws IamAdminServicesException;
}
