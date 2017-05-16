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

import org.apache.airavata.sharing.registry.db.entities.PermissionTypeEntity;
import org.apache.airavata.sharing.registry.db.entities.PermissionTypePK;
import org.apache.airavata.sharing.registry.db.utils.DBConstants;
import org.apache.airavata.sharing.registry.models.SharingRegistryException;
import org.apache.airavata.sharing.registry.models.PermissionType;
import org.apache.airavata.sharing.registry.server.SharingRegistryServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

public class PermissionTypeRepository extends AbstractRepository<PermissionType, PermissionTypeEntity, PermissionTypePK> {
    private final static Logger logger = LoggerFactory.getLogger(PermissionTypeRepository.class);

    public PermissionTypeRepository() {
        super(PermissionType.class, PermissionTypeEntity.class);
    }

    public String getOwnerPermissionTypeIdForDomain(String domainId) throws SharingRegistryException {
        HashMap<String, String> filters = new HashMap<>();
        filters.put(DBConstants.PermissionTypeTable.DOMAIN_ID, domainId);
        filters.put(DBConstants.PermissionTypeTable.NAME, SharingRegistryServerHandler.OWNER_PERMISSION_NAME);
        List<PermissionType> permissionTypeList = select(filters, 0, -1);
        if(permissionTypeList.size() != 1){
            throw new SharingRegistryException("GLOBAL Permission inconsistency. Found " + permissionTypeList.size()
                    + " records with " + SharingRegistryServerHandler.OWNER_PERMISSION_NAME + " name");
        }
        return permissionTypeList.get(0).getPermissionTypeId();
    }
}