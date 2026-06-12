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
package org.apache.airavata.storage;

import java.util.List;
import java.util.Map;
import org.apache.airavata.interfaces.DataProductInterface;
import org.apache.airavata.interfaces.DataReplicaLocationInterface;
import org.apache.airavata.interfaces.StorageProvider;
import org.apache.airavata.model.appcatalog.storageresource.proto.StorageResourceDescription;
import org.apache.airavata.model.data.replica.proto.DataProductModel;
import org.apache.airavata.model.data.replica.proto.DataProductType;
import org.apache.airavata.model.data.replica.proto.DataReplicaLocationModel;
import org.apache.airavata.model.data.replica.proto.ReplicaLocationCategory;
import org.apache.airavata.model.data.replica.proto.ReplicaPersistentType;
import org.apache.airavata.storage.repository.StorageResourceRepository;
import org.springframework.stereotype.Service;

@Service
public class StorageProviderImpl implements StorageProvider {

    private final StorageResourceRepository storageResourceRepository = new StorageResourceRepository();
    private final DataProductInterface dataProductRepository;
    private final DataReplicaLocationInterface dataReplicaLocationRepository;

    public StorageProviderImpl(
            DataProductInterface dataProductRepository, DataReplicaLocationInterface dataReplicaLocationRepository) {
        this.dataProductRepository = dataProductRepository;
        this.dataReplicaLocationRepository = dataReplicaLocationRepository;
    }

    @Override
    public StorageResourceDescription getStorageResource(String storageResourceId) throws Exception {
        return storageResourceRepository.getStorageResource(storageResourceId);
    }

    @Override
    public Map<String, String> getAllStorageResourceNames() throws Exception {
        return storageResourceRepository.getAllStorageResourceIdList();
    }

    @Override
    public String registerDataProduct(DataProductModel dataProductModel) throws Exception {
        return dataProductRepository.registerDataProduct(dataProductModel);
    }

    @Override
    public DataProductModel getDataProduct(String productUri) throws Exception {
        return dataProductRepository.getDataProduct(productUri);
    }

    @Override
    public DataProductModel getParentDataProduct(String productUri) throws Exception {
        return dataProductRepository.getParentDataProduct(productUri);
    }

    @Override
    public List<DataProductModel> getChildDataProducts(String productUri) throws Exception {
        return dataProductRepository.getChildDataProducts(productUri);
    }

    @Override
    public String addStorageResource(StorageResourceDescription description) throws Exception {
        return storageResourceRepository.addStorageResource(description);
    }

    @Override
    public void updateStorageResource(String storageResourceId, StorageResourceDescription description)
            throws Exception {
        storageResourceRepository.updateStorageResource(storageResourceId, description);
    }

    @Override
    public void removeStorageResource(String storageResourceId) throws Exception {
        storageResourceRepository.removeStorageResource(storageResourceId);
    }

    @Override
    public List<DataProductModel> searchDataProductsByName(
            String gatewayId, String userId, String productName, int limit, int offset) throws Exception {
        return dataProductRepository.searchDataProductsByName(gatewayId, userId, productName, limit, offset);
    }

    @Override
    public String registerReplicaLocation(DataReplicaLocationModel replicaLocationModel) throws Exception {
        return dataReplicaLocationRepository.registerReplicaLocation(replicaLocationModel);
    }

    @Override
    public boolean updateDataProduct(DataProductModel dataProductModel) throws Exception {
        return dataProductRepository.updateDataProduct(dataProductModel);
    }

    @Override
    public boolean removeDataProduct(String productUri) throws Exception {
        return dataProductRepository.removeDataProduct(productUri);
    }

    @Override
    public DataReplicaLocationModel getReplicaLocation(String replicaId) throws Exception {
        return dataReplicaLocationRepository.getReplicaLocation(replicaId);
    }

    @Override
    public boolean updateReplicaLocation(DataReplicaLocationModel replicaLocationModel) throws Exception {
        return dataReplicaLocationRepository.updateReplicaLocation(replicaLocationModel);
    }

    @Override
    public boolean removeReplicaLocation(String replicaId) throws Exception {
        return dataReplicaLocationRepository.removeReplicaLocation(replicaId);
    }

    @Override
    public String getOrCreateDataProductByPath(
            String gatewayId, String ownerName, String fileName, String filePath, String storageResourceId)
            throws Exception {
        DataProductModel existing = dataProductRepository.getDataProductByReplicaFilePath(gatewayId, filePath);
        if (existing != null) {
            return existing.getProductUri();
        }
        DataReplicaLocationModel replica = DataReplicaLocationModel.newBuilder()
                .setReplicaName(fileName + " gateway data store copy")
                .setReplicaLocationCategory(ReplicaLocationCategory.GATEWAY_DATA_STORE)
                .setReplicaPersistentType(ReplicaPersistentType.TRANSIENT)
                .setStorageResourceId(storageResourceId != null ? storageResourceId : "")
                .setFilePath(filePath)
                .build();
        DataProductModel product = DataProductModel.newBuilder()
                .setGatewayId(gatewayId)
                .setOwnerName(ownerName != null ? ownerName : "")
                .setProductName(fileName)
                .setDataProductType(DataProductType.FILE)
                .addReplicaLocations(replica)
                .build();
        return dataProductRepository.registerDataProduct(product);
    }
}
