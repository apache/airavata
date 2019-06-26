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
import org.apache.airavata.sharing.registry.db.entities.UserGroupEntity;
import org.apache.airavata.sharing.registry.db.entities.UserGroupPK;
import org.apache.airavata.sharing.registry.db.utils.DBConstants;
import org.apache.airavata.sharing.registry.models.GroupCardinality;
import org.apache.airavata.sharing.registry.models.SharingRegistryException;
import org.apache.airavata.sharing.registry.models.SharingType;
import org.apache.airavata.sharing.registry.models.UserGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserGroupRepository extends AbstractRepository<UserGroup, UserGroupEntity, UserGroupPK> {
    private final static Logger logger = LoggerFactory.getLogger(UserGroupRepository.class);

    public UserGroupRepository() {
        super(UserGroup.class, UserGroupEntity.class);
    }

    public List<UserGroup> getAccessibleGroups(String domainId, String entityId, String permissionTypeId) throws SharingRegistryException {
        return getAccessibleGroupsInternal(domainId, entityId, permissionTypeId);
    }

    public List<UserGroup> getDirectlyAccessibleGroups(String domainId, String entityId, String permissionTypeId) throws SharingRegistryException {
        return getAccessibleGroupsInternal(domainId, entityId, permissionTypeId, SharingType.DIRECT_CASCADING, SharingType.DIRECT_NON_CASCADING);
    }

    private List<UserGroup> getAccessibleGroupsInternal(String domainId, String entityId, String permissionTypeId, SharingType... sharingTypes) throws SharingRegistryException {
        String query = "SELECT DISTINCT g from " + UserGroupEntity.class.getSimpleName() + " g, " + SharingEntity.class.getSimpleName() + " s";
        query += " WHERE ";
        query += "g." + DBConstants.UserGroupTable.GROUP_ID + " = s." + DBConstants.SharingTable.GROUP_ID + " AND ";
        query += "g." + DBConstants.UserGroupTable.DOMAIN_ID + " = s." + DBConstants.SharingTable.DOMAIN_ID + " AND ";
        query += "g." + DBConstants.UserGroupTable.DOMAIN_ID + " = :" + DBConstants.UserGroupTable.DOMAIN_ID + " AND ";
        query += "s." + DBConstants.SharingTable.ENTITY_ID + " = :" + DBConstants.SharingTable.ENTITY_ID + " AND ";
        query += "s." + DBConstants.SharingTable.PERMISSION_TYPE_ID + " = :" + DBConstants.SharingTable.PERMISSION_TYPE_ID + " AND ";
        query += "g." + DBConstants.UserGroupTable.GROUP_CARDINALITY + " = :" + DBConstants.UserGroupTable.GROUP_CARDINALITY;
        if (!Arrays.asList(sharingTypes).isEmpty()) {
            query += " AND s." + DBConstants.SharingTable.SHARING_TYPE + " IN :" + DBConstants.SharingTable.SHARING_TYPE;
        }
        query += " ORDER BY s.createdTime DESC";
        Map<String,Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.UserGroupTable.DOMAIN_ID, domainId);
        queryParameters.put(DBConstants.SharingTable.ENTITY_ID, entityId);
        queryParameters.put(DBConstants.SharingTable.PERMISSION_TYPE_ID, permissionTypeId);
        queryParameters.put(DBConstants.UserGroupTable.GROUP_CARDINALITY, GroupCardinality.MULTI_USER.toString());
        if (!Arrays.asList(sharingTypes).isEmpty()) {
            queryParameters.put(DBConstants.SharingTable.SHARING_TYPE, Arrays.asList(sharingTypes).stream().map(s -> s.name()).collect(Collectors.toList()));
        }
        return select(query, queryParameters, 0, -1);
    }

    //checks whether is shared with any user or group with any permission
    public boolean isShared(String domainId, String entityId) throws SharingRegistryException {
        String query = "SELECT DISTINCT g from " + UserGroupEntity.class.getSimpleName() + " g, " + SharingEntity.class.getSimpleName() + " s";
        query += " WHERE ";
        query += "g." + DBConstants.UserGroupTable.GROUP_ID + " = s." + DBConstants.SharingTable.GROUP_ID + " AND ";
        query += "g." + DBConstants.UserGroupTable.DOMAIN_ID + " = s." + DBConstants.SharingTable.DOMAIN_ID + " AND ";
        query += "g." + DBConstants.UserGroupTable.DOMAIN_ID + " = :" + DBConstants.UserGroupTable.DOMAIN_ID + " AND ";
        query += "s." + DBConstants.SharingTable.ENTITY_ID + " = :" + DBConstants.SharingTable.ENTITY_ID + " AND ";
        query += "s." + DBConstants.SharingTable.PERMISSION_TYPE_ID + " <> :" + DBConstants.SharingTable.PERMISSION_TYPE_ID;
        query += " ORDER BY s.createdTime DESC";
        Map<String,Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.UserGroupTable.DOMAIN_ID, domainId);
        queryParameters.put(DBConstants.SharingTable.ENTITY_ID, entityId);
        String ownerPermissionTypeIdForDomain = (new PermissionTypeRepository()).getOwnerPermissionTypeIdForDomain(domainId);
        queryParameters.put(DBConstants.SharingTable.PERMISSION_TYPE_ID, ownerPermissionTypeIdForDomain);
        return select(query, queryParameters, 0, -1).size() != 0;
    }
}
