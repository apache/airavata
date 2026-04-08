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
package org.apache.airavata.storage.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.db.AbstractRepository;
import org.apache.airavata.db.DBConstants;
import org.apache.airavata.db.QueryConstants;
import org.apache.airavata.interfaces.GatewayStoragePreferenceProvider;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.StoragePreference;
import org.apache.airavata.storage.mapper.StorageMapper;
import org.apache.airavata.storage.model.StoragePreferenceEntity;
import org.apache.airavata.storage.model.StoragePreferencePK;
import org.springframework.stereotype.Component;

@Component
public class StoragePrefRepository
        extends AbstractRepository<StoragePreference, StoragePreferenceEntity, StoragePreferencePK>
        implements GatewayStoragePreferenceProvider {

    public StoragePrefRepository() {
        super(StoragePreference.class, StoragePreferenceEntity.class);
    }

    @Override
    protected StoragePreference toModel(StoragePreferenceEntity entity) {
        return StorageMapper.INSTANCE.storagePrefToModel(entity);
    }

    @Override
    protected StoragePreferenceEntity toEntity(StoragePreference model) {
        return StorageMapper.INSTANCE.storagePrefToEntity(model);
    }

    @Override
    public StoragePreference getGatewayStoragePreference(String gatewayId, String storageResourceId) throws Exception {
        StoragePreferencePK pk = new StoragePreferencePK();
        pk.setGatewayId(gatewayId);
        pk.setStorageResourceId(storageResourceId);
        return get(pk);
    }

    @Override
    public List<StoragePreference> getAllGatewayStoragePreferences(String gatewayId) throws Exception {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put(DBConstants.StorageResourcePreference.GATEWAY_ID, gatewayId);
        return select(QueryConstants.GET_ALL_GATEWAY_STORAGE_PREFERENCES, -1, 0, queryParams);
    }

    @Override
    public boolean addGatewayStoragePreference(
            String gatewayId, String storageResourceId, StoragePreference storagePreference) throws Exception {
        StoragePreferenceEntity entity = toEntity(storagePreference);
        entity.setGatewayId(gatewayId);
        entity.setStorageResourceId(storageResourceId);
        execute(entityManager -> entityManager.merge(entity));
        return true;
    }

    @Override
    public boolean updateGatewayStoragePreference(
            String gatewayId, String storageResourceId, StoragePreference storagePreference) throws Exception {
        return addGatewayStoragePreference(gatewayId, storageResourceId, storagePreference);
    }

    @Override
    public boolean deleteGatewayStoragePreference(String gatewayId, String storageResourceId) throws Exception {
        StoragePreferencePK pk = new StoragePreferencePK();
        pk.setGatewayId(gatewayId);
        pk.setStorageResourceId(storageResourceId);
        return delete(pk);
    }
}
