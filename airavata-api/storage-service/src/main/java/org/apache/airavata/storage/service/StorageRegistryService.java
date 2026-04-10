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
import org.apache.airavata.model.data.movement.proto.DMType;
import org.apache.airavata.model.data.movement.proto.DataMovementInterface;
import org.apache.airavata.model.data.movement.proto.DataMovementProtocol;
import org.apache.airavata.model.data.movement.proto.GridFTPDataMovement;
import org.apache.airavata.model.data.movement.proto.LOCALDataMovement;
import org.apache.airavata.model.data.movement.proto.SCPDataMovement;
import org.apache.airavata.model.data.movement.proto.UnicoreDataMovement;
import org.apache.airavata.model.data.replica.proto.DataProductModel;
import org.apache.airavata.model.data.replica.proto.DataReplicaLocationModel;
import org.apache.airavata.storage.repository.DataMovementRepository;
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

    private final DataMovementRepository dataMovementRepository = new DataMovementRepository();

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
    public SCPDataMovement getSCPDataMovement(String dataMovementId) throws Exception {
        try {
            return dataMovementRepository.getSCPDataMovement(dataMovementId);
        } catch (Exception e) {
            String errorMsg = "Error while retrieving SCP data movement interface...";
            logger.error(dataMovementId, errorMsg, e);
            throw new RegistryException(errorMsg + e.getMessage());
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

    // =========================================================================
    // Data movement methods (moved from compute-service)
    // =========================================================================

    public LOCALDataMovement getLocalDataMovement(String dataMovementId) throws Exception {
        try {
            return dataMovementRepository.getLocalDataMovement(dataMovementId);
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while retrieving local data movement: " + e.getMessage());
        }
    }

    public UnicoreDataMovement getUnicoreDataMovement(String dataMovementId) throws Exception {
        try {
            return dataMovementRepository.getUNICOREDataMovement(dataMovementId);
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while retrieving UNICORE data movement: " + e.getMessage());
        }
    }

    public GridFTPDataMovement getGridFTPDataMovement(String dataMovementId) throws Exception {
        try {
            return dataMovementRepository.getGridFTPDataMovement(dataMovementId);
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while retrieving GridFTP data movement: " + e.getMessage());
        }
    }

    public boolean deleteDataMovementInterface(String resourceId, String dataMovementInterfaceId, DMType dmType)
            throws Exception {
        try {
            switch (dmType) {
                case COMPUTE_RESOURCE:
                    dataMovementRepository.removeDataMovementInterface(resourceId, dataMovementInterfaceId);
                    return true;
                case STORAGE_RESOURCE:
                    storageProvider.removeDataMovementInterface(resourceId, dataMovementInterfaceId);
                    return true;
                default:
                    return false;
            }
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while deleting data movement interface: " + e.getMessage());
        }
    }

    public boolean updateGridFTPDataMovementDetails(
            String dataMovementInterfaceId, GridFTPDataMovement gridFTPDataMovement) throws Exception {
        throw new RegistryException("updateGridFTPDataMovementDetails is not yet implemented");
    }

    public String addGridFTPDataMovementDetails(
            String computeResourceId, DMType dmType, int priorityOrder, GridFTPDataMovement gridFTPDataMovement)
            throws Exception {
        try {
            String dataMovementInterfaceId = dataMovementRepository.addGridFTPDataMovement(gridFTPDataMovement);
            return addDataMovementInterface(
                    computeResourceId, dmType, dataMovementInterfaceId, DataMovementProtocol.GRID_FTP, priorityOrder);
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while adding GridFTP data movement: " + e.getMessage());
        }
    }

    public boolean updateUnicoreDataMovementDetails(
            String dataMovementInterfaceId, UnicoreDataMovement unicoreDataMovement) throws Exception {
        throw new RegistryException("updateUnicoreDataMovementDetails is not yet implemented");
    }

    public String addUnicoreDataMovementDetails(
            String resourceId, DMType dmType, int priorityOrder, UnicoreDataMovement unicoreDataMovement)
            throws Exception {
        try {
            String dataMovementInterfaceId = dataMovementRepository.addUnicoreDataMovement(unicoreDataMovement);
            return addDataMovementInterface(
                    resourceId,
                    dmType,
                    dataMovementInterfaceId,
                    DataMovementProtocol.UNICORE_STORAGE_SERVICE,
                    priorityOrder);
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while adding UNICORE data movement: " + e.getMessage());
        }
    }

    public boolean updateSCPDataMovementDetails(String dataMovementInterfaceId, SCPDataMovement scpDataMovement)
            throws Exception {
        try {
            dataMovementRepository.updateScpDataMovement(scpDataMovement);
            return true;
        } catch (Exception e) {
            throw new RegistryException("Error while updating SCP data movement: " + e.getMessage());
        }
    }

    public String addSCPDataMovementDetails(
            String resourceId, DMType dmType, int priorityOrder, SCPDataMovement scpDataMovement) throws Exception {
        try {
            String dataMovementInterfaceId = dataMovementRepository.addScpDataMovement(scpDataMovement);
            return addDataMovementInterface(
                    resourceId, dmType, dataMovementInterfaceId, DataMovementProtocol.SCP, priorityOrder);
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while adding SCP data movement: " + e.getMessage());
        }
    }

    public boolean updateLocalDataMovementDetails(String dataMovementInterfaceId, LOCALDataMovement localDataMovement)
            throws Exception {
        try {
            dataMovementRepository.updateLocalDataMovement(localDataMovement);
            return true;
        } catch (Exception e) {
            throw new RegistryException("Error while updating local data movement: " + e.getMessage());
        }
    }

    public String addLocalDataMovementDetails(
            String resourceId, DMType dataMoveType, int priorityOrder, LOCALDataMovement localDataMovement)
            throws Exception {
        try {
            String dataMovementInterfaceId = dataMovementRepository.addLocalDataMovement(localDataMovement);
            return addDataMovementInterface(
                    resourceId,
                    dataMoveType,
                    dataMovementInterfaceId,
                    DataMovementProtocol.DATA_MOVEMENT_PROTOCOL_LOCAL,
                    priorityOrder);
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while adding local data movement: " + e.getMessage());
        }
    }

    public boolean changeDataMovementPriority(String dataMovementInterfaceId, int newPriorityOrder) throws Exception {
        return false;
    }

    public boolean changeDataMovementPriorities(Map<String, Integer> dataMovementPriorityMap) throws Exception {
        return false;
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    private String addDataMovementInterface(
            String resourceId,
            DMType dmType,
            String dataMovementInterfaceId,
            DataMovementProtocol protocolType,
            int priorityOrder)
            throws Exception {
        DataMovementInterface.Builder dmiBuilder = DataMovementInterface.newBuilder()
                .setDataMovementInterfaceId(dataMovementInterfaceId)
                .setPriorityOrder(priorityOrder)
                .setDataMovementProtocol(protocolType);
        if (dmType.equals(DMType.COMPUTE_RESOURCE)) {
            return dataMovementRepository.addDataMovementProtocol(resourceId, dmiBuilder.build());
        } else if (dmType.equals(DMType.STORAGE_RESOURCE)) {
            dmiBuilder.setStorageResourceId(resourceId);
            return storageProvider.addDataMovementInterface(dmiBuilder.build());
        }
        return null;
    }
}
