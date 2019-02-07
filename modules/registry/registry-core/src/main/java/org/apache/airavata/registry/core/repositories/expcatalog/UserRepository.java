/*
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
 *
 */

package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.registry.core.entities.expcatalog.UserEntity;
import org.apache.airavata.registry.core.entities.expcatalog.UserPK;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.core.utils.QueryConstants;
import org.apache.airavata.registry.cpi.RegistryException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class UserRepository extends ExpCatAbstractRepository<UserProfile, UserEntity, UserPK> {
    public UserRepository() {
        super(UserProfile.class, UserEntity.class);
    }

    public UserProfile addUser(UserProfile user) throws RegistryException {
        try {
            return create(user);
        } catch (Exception e) {
            throw new RegistryException("Failed to create user", e);
        }
    }

    public boolean isUserExists(String gatewayId, String username) throws RegistryException {
        try {
            return isExists(new UserPK(gatewayId, username));
        } catch (Exception e) {
            throw new RegistryException("Failed to create user", e);
        }
    }

    public List<String> getAllUsernamesInGateway(String gatewayId) throws RegistryException {
        try {
            List<UserProfile> users = select(QueryConstants.GET_ALL_GATEWAY_USERS, -1, 0, Collections.singletonMap(DBConstants.User.GATEWAY_ID, gatewayId));
            return users.stream().map(up -> up.getUserId()).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RegistryException("Failed to create user", e);
        }
    }
}
