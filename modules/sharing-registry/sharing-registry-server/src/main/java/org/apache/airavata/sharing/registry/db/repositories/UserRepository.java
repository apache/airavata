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
package org.apache.airavata.sharing.registry.db.repositories;


import org.apache.airavata.sharing.registry.db.entities.SharingEntity;
import org.apache.airavata.sharing.registry.db.entities.UserEntity;
import org.apache.airavata.sharing.registry.db.entities.UserPK;
import org.apache.airavata.sharing.registry.db.utils.DBConstants;
import org.apache.airavata.sharing.registry.models.SharingRegistryException;
import org.apache.airavata.sharing.registry.models.SharingType;
import org.apache.airavata.sharing.registry.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserRepository extends AbstractRepository<User, UserEntity, UserPK> {
    private final static Logger logger = LoggerFactory.getLogger(UserRepository.class);

    public UserRepository() {
        super(User.class, UserEntity.class);
    }


    public List<User> getAccessibleUsers(String domainId, String entityId, String permissionTypeId) throws SharingRegistryException {
        if(permissionTypeId.equals((new PermissionTypeRepository()).getOwnerPermissionTypeIdForDomain(domainId))){
            return getAccessibleUsersInternal(domainId, entityId, permissionTypeId, SharingType.DIRECT_CASCADING, SharingType.DIRECT_NON_CASCADING);
        } else {
            return getAccessibleUsersInternal(domainId, entityId, permissionTypeId);
        }
    }

    public List<User> getDirectlyAccessibleUsers(String domainId, String entityId, String permissionTypeId) throws SharingRegistryException {
        return getAccessibleUsersInternal(domainId, entityId, permissionTypeId, SharingType.DIRECT_CASCADING, SharingType.DIRECT_NON_CASCADING);
    }

    private List<User> getAccessibleUsersInternal(String domainId, String entityId, String permissionTypeId, SharingType... sharingTypes) throws SharingRegistryException {
        Map<String,Object> queryParameters = new HashMap<>();
        String query = "SELECT DISTINCT u from " + UserEntity.class.getSimpleName() + " u, " + SharingEntity.class.getSimpleName() + " s";
        query += " WHERE ";
        query += "u." + DBConstants.UserTable.USER_ID + " = s." + DBConstants.SharingTable.GROUP_ID + " AND ";
        query += "u." + DBConstants.UserTable.DOMAIN_ID + " = s." + DBConstants.SharingTable.DOMAIN_ID + " AND ";
        query += "u." + DBConstants.UserTable.DOMAIN_ID + " = :" + DBConstants.UserTable.DOMAIN_ID + " AND ";
        query += "s." + DBConstants.SharingTable.ENTITY_ID + " = :" + DBConstants.SharingTable.ENTITY_ID + " AND ";
        query += "s." + DBConstants.SharingTable.PERMISSION_TYPE_ID + " = :" + DBConstants.SharingTable.PERMISSION_TYPE_ID;
        queryParameters.put(DBConstants.UserTable.DOMAIN_ID, domainId);
        queryParameters.put(DBConstants.SharingTable.ENTITY_ID, entityId);
        queryParameters.put(DBConstants.SharingTable.PERMISSION_TYPE_ID, permissionTypeId);

        if (!Arrays.asList(sharingTypes).isEmpty()) {
            query += " AND s." + DBConstants.SharingTable.SHARING_TYPE + " IN :" + DBConstants.SharingTable.SHARING_TYPE;
            queryParameters.put(DBConstants.SharingTable.SHARING_TYPE, Arrays.asList(sharingTypes).stream().map(s -> s.name()).collect(Collectors.toList()));
        }

        query += " ORDER BY s.createdTime DESC";
        return select(query, queryParameters,0, -1);
    }
}
