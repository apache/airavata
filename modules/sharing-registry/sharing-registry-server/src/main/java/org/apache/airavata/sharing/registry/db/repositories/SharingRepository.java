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
import org.apache.airavata.sharing.registry.db.entities.SharingPK;
import org.apache.airavata.sharing.registry.db.utils.DBConstants;
import org.apache.airavata.sharing.registry.models.Sharing;
import org.apache.airavata.sharing.registry.models.SharingRegistryException;
import org.apache.airavata.sharing.registry.models.SharingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

public class SharingRepository extends AbstractRepository<Sharing, SharingEntity, SharingPK> {
    private final static Logger logger = LoggerFactory.getLogger(SharingRepository.class);

    public SharingRepository() {
        super(Sharing.class, SharingEntity.class);
    }

    public List<Sharing> getIndirectSharedChildren(String domainId, String parentId, String permissionTypeId) throws SharingRegistryException {
        HashMap<String, String> filters = new HashMap<>();
        filters.put(DBConstants.SharingTable.DOMAIN_ID, domainId);
        filters.put(DBConstants.SharingTable.INHERITED_PARENT_ID, parentId);
        filters.put(DBConstants.SharingTable.SHARING_TYPE, SharingType.INDIRECT_CASCADING.toString());
        filters.put(DBConstants.SharingTable.PERMISSION_TYPE_ID, permissionTypeId);

        return select(filters, 0, -1);
    }

    public List<Sharing> getCascadingPermissionsForEntity(String domainId, String entityId) throws SharingRegistryException {
        String query = "SELECT DISTINCT p from " + SharingEntity.class.getSimpleName() + " as p";
        query += " WHERE ";
        query += "p." + DBConstants.SharingTable.DOMAIN_ID + " = :" + DBConstants.SharingTable.DOMAIN_ID + " AND ";
        query += "p." + DBConstants.SharingTable.ENTITY_ID + " = :" + DBConstants.SharingTable.ENTITY_ID + " AND ";
        query += "p." + DBConstants.SharingTable.SHARING_TYPE + " IN('" + SharingType.DIRECT_CASCADING.toString()
                + "', '" + SharingType.INDIRECT_CASCADING + "') ";
        query += " ORDER BY p.createdTime DESC";
        Map<String,Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.SharingTable.DOMAIN_ID, domainId);
        queryParameters.put(DBConstants.SharingTable.ENTITY_ID, entityId);
        return select(query, queryParameters, 0, -1);
    }

    public boolean hasAccess(String domainId, String entityId, List<String> groupIds, List<String> permissionTypeIds) throws SharingRegistryException {
        Map<String,Object> queryParameters = new HashMap<>();
        String query = "SELECT p from " + SharingEntity.class.getSimpleName() + " as p";
        query += " WHERE ";
        query += "p." + DBConstants.SharingTable.DOMAIN_ID + " = :" + DBConstants.SharingTable.DOMAIN_ID + " AND ";
        query += "p." + DBConstants.SharingTable.ENTITY_ID + " = :" + DBConstants.SharingTable.ENTITY_ID + " AND ";
        queryParameters.put(DBConstants.SharingTable.DOMAIN_ID, domainId);
        queryParameters.put(DBConstants.SharingTable.ENTITY_ID, entityId);
        query += "p." + DBConstants.SharingTable.PERMISSION_TYPE_ID + " IN :" + DBConstants.SharingTable.PERMISSION_TYPE_ID + " AND ";
        queryParameters.put(DBConstants.SharingTable.PERMISSION_TYPE_ID, permissionTypeIds);
        query += "p." + DBConstants.SharingTable.GROUP_ID + " IN :" + DBConstants.SharingTable.GROUP_ID + " ";
        queryParameters.put(DBConstants.SharingTable.GROUP_ID, groupIds);
        query += " ORDER BY p.createdTime DESC";
        return select(query, queryParameters, 0, -1).size() > 0;
    }

    public int getSharedCount(String domainId, String entityId) throws SharingRegistryException {
        Map<String,Object> queryParameters = new HashMap<>();
        String query = "SELECT p from " + SharingEntity.class.getSimpleName() + " as p";
        query += " WHERE ";
        query += "p." + DBConstants.SharingTable.DOMAIN_ID + " = :" + DBConstants.SharingTable.DOMAIN_ID + " AND ";
        queryParameters.put(DBConstants.SharingTable.DOMAIN_ID, domainId);
        query += "p." + DBConstants.SharingTable.ENTITY_ID + " = :" + DBConstants.SharingTable.ENTITY_ID + " AND ";
        queryParameters.put(DBConstants.SharingTable.ENTITY_ID, entityId);
        String permissionTypeIdString = (new PermissionTypeRepository()).getOwnerPermissionTypeIdForDomain(domainId);
        query += "p." + DBConstants.SharingTable.PERMISSION_TYPE_ID + " <> :" + DBConstants.SharingTable.PERMISSION_TYPE_ID + " AND ";
        queryParameters.put(DBConstants.SharingTable.PERMISSION_TYPE_ID, permissionTypeIdString);
        query += "p." + DBConstants.SharingTable.SHARING_TYPE + " <> :" + DBConstants.SharingTable.SHARING_TYPE;
        queryParameters.put(DBConstants.SharingTable.SHARING_TYPE, SharingType.INDIRECT_CASCADING.toString());
        return select(query, queryParameters, 0, -1).size();
    }

    public void removeAllIndirectCascadingPermissionsForEntity(String domainId, String entityId) throws SharingRegistryException {
        String query = "DELETE from " + SharingEntity.class.getSimpleName() + " as p";
        query += " WHERE ";
        query += "p." + DBConstants.SharingTable.DOMAIN_ID + " = :" + DBConstants.SharingTable.DOMAIN_ID + " AND ";
        query += "p." + DBConstants.SharingTable.ENTITY_ID + " = :" + DBConstants.SharingTable.ENTITY_ID + " AND ";
        query += "p." + DBConstants.SharingTable.SHARING_TYPE + " = '" + SharingType.INDIRECT_CASCADING.toString() + "' ";
        final String finalQuery = query;
        execute(em -> {
            Query q = em.createQuery(finalQuery);
            q.setParameter(DBConstants.SharingTable.DOMAIN_ID, domainId);
            q.setParameter(DBConstants.SharingTable.ENTITY_ID, entityId);
            q.executeUpdate();
            return true;
        });
    }
}
