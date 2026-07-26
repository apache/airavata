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
package org.apache.airavata.storage.service;

import java.util.List;
import java.util.Map;
import org.apache.airavata.models.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.models.data.replica.DataProductModel;
import org.apache.airavata.models.data.replica.DataReplicaLocationModel;
import org.apache.airavata.storage.repository.DataProductRepository;
import org.apache.airavata.storage.repository.DataReplicaLocationRepository;
import org.apache.airavata.storage.repository.StorageResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class StorageRegistryService {
    private static final Logger logger = LoggerFactory.getLogger(StorageRegistryService.class);

    private final DataProductRepository storageProvider;
    private final DataReplicaLocationRepository dataReplicaLocationRepository;
    private final StorageResourceRepository storageResourceRepository;

    public StorageRegistryService(DataProductRepository storageProvider,
            DataReplicaLocationRepository dataReplicaLocationRepository,
            StorageResourceRepository storageResourceRepository) {
        this.storageProvider = storageProvider;
        this.dataReplicaLocationRepository = dataReplicaLocationRepository;
        this.storageResourceRepository = storageResourceRepository;
    }

    // =========================================================================
    // StorageRegistry interface methods
    // =========================================================================

    public StorageResourceDescription getStorageResource(String storageResourceId) throws Exception {
        try {
            StorageResourceDescription storageResource = storageResourceRepository
                    .getStorageResource(storageResourceId);
            logger.debug("Retrieved storage resource {}", storageResourceId);
            return storageResource;
        } catch (Exception e) {
            logger.error("Error retrieving storage resource {}: {}", storageResourceId, e.getMessage(), e);
            throw e;
        }
    }

    public String registerDataProduct(DataProductModel dataProductModel) throws Exception {
        try {
            String productUrl = storageProvider.registerDataProduct(dataProductModel);
            return productUrl;
        } catch (Exception e) {
            logger.error("Error registering data product {}: {}", dataProductModel.productName(), e.getMessage(), e);
            throw e;
        }
    }

    public DataProductModel getDataProduct(String productUri) throws Exception {
        try {
            DataProductModel dataProductModel = storageProvider.getDataProduct(productUri);
            return dataProductModel;
        } catch (Exception e) {
            logger.error("Error retrieving data product {}: {}", productUri, e.getMessage(), e);
            throw e;
        }
    }

    public DataProductModel getParentDataProduct(String productUri) throws Exception {
        try {
            DataProductModel dataProductModel = storageProvider.getParentDataProduct(productUri);
            return dataProductModel;
        } catch (Exception e) {
            logger.error("Error retrieving parent data product for {}: {}", productUri, e.getMessage(), e);
            throw e;
        }
    }

    public List<DataProductModel> getChildDataProducts(String productUri) throws Exception {
        try {
            List<DataProductModel> dataProductModels = storageProvider.getChildDataProducts(productUri);
            return dataProductModels;
        } catch (Exception e) {
            logger.error("Error retrieving child products for {}: {}", productUri, e.getMessage(), e);
            throw e;
        }
    }

    public String registerReplicaLocation(DataReplicaLocationModel replicaLocationModel) throws Exception {
        try {
            String replicaId = dataReplicaLocationRepository.registerReplicaLocation(replicaLocationModel);
            return replicaId;
        } catch (Exception e) {
            logger.error("Error registering replica {}: {}", replicaLocationModel.replicaName(), e.getMessage(), e);
            throw e;
        }
    }

    public List<DataProductModel> searchDataProductsByName(
            String gatewayId, String userId, String productName, int limit, int offset) throws Exception {
        try {
            List<DataProductModel> dataProductModels = storageProvider.searchDataProductsByName(gatewayId, userId,
                    productName, limit, offset);
            return dataProductModels;
        } catch (Exception e) {
            logger.error("Error searching data products for name {}: {}", productName, e.getMessage(), e);
            throw e;
        }
    }

    public boolean updateDataProduct(DataProductModel dataProductModel) throws Exception {
        try {
            return storageProvider.updateDataProduct(dataProductModel);
        } catch (Exception e) {
            logger.error("Error updating data product {}: {}", dataProductModel.productUri(), e.getMessage(), e);
            throw e;
        }
    }

    public boolean removeDataProduct(String productUri) throws Exception {
        try {
            return storageProvider.removeDataProduct(productUri);
        } catch (Exception e) {
            logger.error("Error removing data product {}: {}", productUri, e.getMessage(), e);
            throw e;
        }
    }

    public DataReplicaLocationModel getReplicaLocation(String replicaId) throws Exception {
        try {
            return dataReplicaLocationRepository.getReplicaLocation(replicaId);
        } catch (Exception e) {
            logger.error("Error retrieving replica location {}: {}", replicaId, e.getMessage(), e);
            throw e;
        }
    }

    public boolean updateReplicaLocation(DataReplicaLocationModel replicaLocationModel) throws Exception {
        try {
            return dataReplicaLocationRepository.updateReplicaLocation(replicaLocationModel);
        } catch (Exception e) {
            logger.error("Error updating replica location {}: {}", replicaLocationModel.replicaId(), e.getMessage(),
                    e);
            throw e;
        }
    }

    public boolean removeReplicaLocation(String replicaId) throws Exception {
        try {
            return dataReplicaLocationRepository.removeReplicaLocation(replicaId);
        } catch (Exception e) {
            logger.error("Error removing replica location {}: {}", replicaId, e.getMessage(), e);
            throw e;
        }
    }

    // =========================================================================
    // Additional storage methods (not yet on the interface)
    // =========================================================================

    public Map<String, String> getAllStorageResourceNames() throws Exception {
        try {
            return storageResourceRepository.getAllStorageResourceIdList();
        } catch (Exception e) {
            logger.error("Error retrieving storage resource names: {}", e.getMessage(), e);
            throw e;
        }
    }

    public boolean deleteStorageResource(String storageResourceId) throws Exception {
        try {
            storageResourceRepository.removeStorageResource(storageResourceId);
            return true;
        } catch (Exception e) {
            logger.error("Error deleting storage resource {}: {}", storageResourceId, e.getMessage(), e);
            throw e;
        }
    }

    public boolean updateStorageResource(
            String storageResourceId, StorageResourceDescription storageResourceDescription) throws Exception {
        try {
            storageResourceRepository.updateStorageResource(storageResourceId, storageResourceDescription);
            return true;
        } catch (Exception e) {
            logger.error("Error updating storage resource {}: {}", storageResourceId, e.getMessage(), e);
            throw e;
        }
    }

    public String registerStorageResource(StorageResourceDescription storageResourceDescription) throws Exception {
        try {
            return storageResourceRepository.addStorageResource(storageResourceDescription);
        } catch (Exception e) {
            logger.error("Error saving storage resource: {}", e.getMessage(), e);
            throw e;
        }
    }
}
