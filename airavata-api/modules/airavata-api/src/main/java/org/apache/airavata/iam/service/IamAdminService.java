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
import org.apache.airavata.credential.exception.CredentialStoreException;
import org.apache.airavata.gateway.model.Gateway;
import org.apache.airavata.iam.exception.IamAdminServicesException;
import org.apache.airavata.iam.model.AuthzToken;
import org.apache.airavata.iam.model.UserProfile;

public interface IamAdminService {

    Gateway setUpGateway(AuthzToken authzToken, Gateway gateway)
            throws IamAdminServicesException, CredentialStoreException;

    boolean isUsernameAvailable(AuthzToken authzToken, String username) throws IamAdminServicesException;

    boolean registerUser(
            AuthzToken authzToken,
            String username,
            String emailAddress,
            String firstName,
            String lastName,
            String newPassword)
            throws IamAdminServicesException;

    boolean enableUser(AuthzToken authzToken, String username) throws IamAdminServicesException;

    boolean disableUser(AuthzToken authzToken, String username) throws IamAdminServicesException;

    boolean isUserEnabled(AuthzToken authzToken, String username) throws IamAdminServicesException;

    boolean isUserExist(AuthzToken authzToken, String username) throws IamAdminServicesException;

    UserProfile getUser(AuthzToken authzToken, String username) throws IamAdminServicesException;

    List<UserProfile> getUsers(AuthzToken authzToken, int offset, int limit, String search)
            throws IamAdminServicesException;

    boolean resetUserPassword(AuthzToken authzToken, String username, String newPassword)
            throws IamAdminServicesException;

    List<UserProfile> findUsers(AuthzToken authzToken, String email, String userId) throws IamAdminServicesException;

    void updateUserProfile(AuthzToken authzToken, UserProfile userDetails) throws IamAdminServicesException;

    boolean deleteUser(AuthzToken authzToken, String username) throws IamAdminServicesException;

    boolean addRoleToUser(AuthzToken authzToken, String username, String roleName)
            throws IamAdminServicesException, RegistryException, CredentialStoreException;

    boolean removeRoleFromUser(AuthzToken authzToken, String username, String roleName)
            throws IamAdminServicesException, RegistryException, CredentialStoreException;

    List<UserProfile> getUsersWithRole(AuthzToken authzToken, String roleName)
            throws IamAdminServicesException, RegistryException, CredentialStoreException;
}
