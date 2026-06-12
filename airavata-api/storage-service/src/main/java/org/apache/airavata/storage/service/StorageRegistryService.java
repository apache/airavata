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
import org.apache.airavata.interfaces.AppCatalogException;
import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.interfaces.StorageProvider;
import org.apache.airavata.interfaces.StorageRegistry;
import org.apache.airavata.model.appcatalog.storageresource.proto.StorageResourceDescription;
import org.apache.airavata.model.data.replica.proto.DataProductModel;
import org.apache.airavata.model.data.replica.proto.DataReplicaLocationModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class StorageRegistryService implements StorageRegistry {
    private static final Logger logger = LoggerFactory.getLogger(StorageRegistryService.class);

    @org.springframework.beans.factory.annotation.Autowired
    private StorageProvider storageProvider;

    // =========================================================================
    // StorageRegistry interface methods
    // =========================================================================

    @Override
    public StorageResourceDescription getStorageResource(String storageResourceId) throws Exception {
        try {
            StorageResourceDescription storageResource = storageProvider.getStorageResource(storageResourceId);
            logger.debug("Airavata retrieved storage resource with storage resource Id : " + storageResourceId);
            return storageResource;
        } catch (AppCatalogException e) {
            logger.error(storageResourceId, "Error while retrieving storage resource...", e);
            throw new RegistryException("Error while retrieving storage resource. More info : " + e.getMessage());
        }
    }

    @Override
    public String registerDataProduct(DataProductModel dataProductModel) throws Exception {
        try {
            String productUrl = storageProvider.registerDataProduct(dataProductModel);
            return productUrl;
        } catch (RegistryException e) {
            String msg = "Error in registering the data resource" + dataProductModel.getProductName() + ".";
            logger.error(msg, e);
            throw new RegistryException(msg + " More info : " + e.getMessage());
        }
    }

    @Override
    public DataProductModel getDataProduct(String productUri) throws Exception {
        try {
            DataProductModel dataProductModel = storageProvider.getDataProduct(productUri);
            return dataProductModel;
        } catch (RegistryException e) {
            String msg = "Error in retreiving the data product " + productUri + ".";
            logger.error(msg, e);
            throw new RegistryException(msg + " More info : " + e.getMessage());
        }
    }

    @Override
    public DataProductModel getParentDataProduct(String productUri) throws Exception {
        try {
            DataProductModel dataProductModel = storageProvider.getParentDataProduct(productUri);
            return dataProductModel;
        } catch (RegistryException e) {
            String msg = "Error in retreiving the parent data product for " + productUri + ".";
            logger.error(msg, e);
            throw new RegistryException(msg + " More info : " + e.getMessage());
        }
    }

    @Override
    public List<DataProductModel> getChildDataProducts(String productUri) throws Exception {
        try {
            List<DataProductModel> dataProductModels = storageProvider.getChildDataProducts(productUri);
            return dataProductModels;
        } catch (RegistryException e) {
            String msg = "Error in retreiving the child products for " + productUri + ".";
            logger.error(msg, e);
            throw new RegistryException(msg + " More info : " + e.getMessage());
        }
    }

    @Override
    public String registerReplicaLocation(DataReplicaLocationModel replicaLocationModel) throws Exception {
        try {
            String replicaId = storageProvider.registerReplicaLocation(replicaLocationModel);
            return replicaId;
        } catch (RegistryException e) {
            String msg = "Error in retreiving the replica " + replicaLocationModel.getReplicaName() + ".";
            logger.error(msg, e);
            throw new RegistryException(msg + " More info : " + e.getMessage());
        }
    }

    @Override
    public List<DataProductModel> searchDataProductsByName(
            String gatewayId, String userId, String productName, int limit, int offset) throws Exception {
        try {
            List<DataProductModel> dataProductModels =
                    storageProvider.searchDataProductsByName(gatewayId, userId, productName, limit, offset);
            return dataProductModels;
        } catch (RegistryException e) {
            String msg = "Error in searching the data products for name " + productName + ".";
            logger.error(msg, e);
            throw new RegistryException(msg + " More info : " + e.getMessage());
        }
    }

    @Override
    public boolean updateDataProduct(DataProductModel dataProductModel) throws Exception {
        try {
            return storageProvider.updateDataProduct(dataProductModel);
        } catch (RegistryException e) {
            String msg = "Error in updating the data product " + dataProductModel.getProductUri() + ".";
            logger.error(msg, e);
            throw new RegistryException(msg + " More info : " + e.getMessage());
        }
    }

    @Override
    public boolean removeDataProduct(String productUri) throws Exception {
        try {
            return storageProvider.removeDataProduct(productUri);
        } catch (RegistryException e) {
            String msg = "Error in removing the data product " + productUri + ".";
            logger.error(msg, e);
            throw new RegistryException(msg + " More info : " + e.getMessage());
        }
    }

    @Override
    public DataReplicaLocationModel getReplicaLocation(String replicaId) throws Exception {
        try {
            return storageProvider.getReplicaLocation(replicaId);
        } catch (RegistryException e) {
            String msg = "Error in retrieving the replica location " + replicaId + ".";
            logger.error(msg, e);
            throw new RegistryException(msg + " More info : " + e.getMessage());
        }
    }

    @Override
    public boolean updateReplicaLocation(DataReplicaLocationModel replicaLocationModel) throws Exception {
        try {
            return storageProvider.updateReplicaLocation(replicaLocationModel);
        } catch (RegistryException e) {
            String msg = "Error in updating the replica location " + replicaLocationModel.getReplicaId() + ".";
            logger.error(msg, e);
            throw new RegistryException(msg + " More info : " + e.getMessage());
        }
    }

    @Override
    public boolean removeReplicaLocation(String replicaId) throws Exception {
        try {
            return storageProvider.removeReplicaLocation(replicaId);
        } catch (RegistryException e) {
            String msg = "Error in removing the replica location " + replicaId + ".";
            logger.error(msg, e);
            throw new RegistryException(msg + " More info : " + e.getMessage());
        }
    }

    // =========================================================================
    // Additional storage methods (not yet on the interface)
    // =========================================================================

    public Map<String, String> getAllStorageResourceNames() throws Exception {
        try {
            return storageProvider.getAllStorageResourceNames();
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while retrieving storage resource. More info : " + e.getMessage());
        }
    }

    public boolean deleteStorageResource(String storageResourceId) throws Exception {
        try {
            storageProvider.removeStorageResource(storageResourceId);
            return true;
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while deleting storage resource. More info : " + e.getMessage());
        }
    }

    public boolean updateStorageResource(
            String storageResourceId, StorageResourceDescription storageResourceDescription) throws Exception {
        try {
            storageProvider.updateStorageResource(storageResourceId, storageResourceDescription);
            return true;
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while updaing storage resource. More info : " + e.getMessage());
        }
    }

    public String registerStorageResource(StorageResourceDescription storageResourceDescription) throws Exception {
        try {
            return storageProvider.addStorageResource(storageResourceDescription);
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while saving storage resource. More info : " + e.getMessage());
        }
    }
}
