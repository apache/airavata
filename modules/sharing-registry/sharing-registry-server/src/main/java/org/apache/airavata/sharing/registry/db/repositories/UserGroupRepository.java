/*
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
 *
*/
package org.apache.airavata.sharing.registry.db.repositories;

import org.apache.airavata.sharing.registry.db.entities.SharingEntity;
import org.apache.airavata.sharing.registry.db.entities.UserGroupEntity;
import org.apache.airavata.sharing.registry.db.entities.UserGroupPK;
import org.apache.airavata.sharing.registry.db.utils.DBConstants;
import org.apache.airavata.sharing.registry.models.GroupCardinality;
import org.apache.airavata.sharing.registry.models.SharingRegistryException;
import org.apache.airavata.sharing.registry.models.UserGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UserGroupRepository extends AbstractRepository<UserGroup, UserGroupEntity, UserGroupPK> {
    private final static Logger logger = LoggerFactory.getLogger(UserGroupRepository.class);

    public UserGroupRepository() {
        super(UserGroup.class, UserGroupEntity.class);
    }

    public List<UserGroup> getAccessibleGroups(String domainId, String entityId, String permissionTypeId) throws SharingRegistryException {
        String query = "SELECT DISTINCT g from " + UserGroupEntity.class.getSimpleName() + " g, " + SharingEntity.class.getSimpleName() + " s";
        query += " WHERE ";
        query += "g." + DBConstants.UserGroupTable.GROUP_ID + " = s." + DBConstants.SharingTable.GROUP_ID + " AND ";
        query += "g." + DBConstants.UserGroupTable.DOMAIN_ID + " = s." + DBConstants.SharingTable.DOMAIN_ID + " AND ";
        query += "g." + DBConstants.UserGroupTable.DOMAIN_ID + " = '" + domainId + "' AND ";
        query += "s." + DBConstants.SharingTable.ENTITY_ID + " = '" + entityId + "' AND ";
        query += "s." + DBConstants.SharingTable.PERMISSION_TYPE_ID + " = '" + permissionTypeId + "' AND ";
        query += "g." + DBConstants.UserGroupTable.GROUP_CARDINALITY + " = '" + GroupCardinality.MULTI_USER.toString() + "'";
        query += " ORDER BY s.createdTime DESC";
        return select(query, 0, -1);
    }
}