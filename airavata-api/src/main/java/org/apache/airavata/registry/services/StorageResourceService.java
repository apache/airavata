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
package org.apache.airavata.registry.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.common.model.AiravataCommonsConstants;
import org.apache.airavata.common.model.DataMovementInterface;
import org.apache.airavata.common.model.StorageResourceDescription;
import org.apache.airavata.registry.entities.appcatalog.StorageInterfaceEntity;
import org.apache.airavata.registry.entities.appcatalog.StorageInterfacePK;
import org.apache.airavata.registry.entities.appcatalog.StorageResourceEntity;
import org.apache.airavata.registry.exception.AppCatalogException;
import org.apache.airavata.registry.mappers.DataMovementInterfaceMapper;
import org.apache.airavata.registry.mappers.StorageResourceMapper;
import org.apache.airavata.registry.repositories.appcatalog.StorageInterfaceRepository;
import org.apache.airavata.registry.repositories.appcatalog.StorageResourceRepository;
import org.apache.airavata.registry.utils.AppCatalogUtils;
import org.apache.airavata.registry.utils.DBConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StorageResourceService {
    private static final Logger logger = LoggerFactory.getLogger(StorageResourceService.class);

    private final StorageResourceRepository storageResourceRepository;
    private final StorageInterfaceRepository storageInterfaceRepository;
    private final StorageResourceMapper storageResourceMapper;
    private final DataMovementInterfaceMapper dataMovementInterfaceMapper;

    public StorageResourceService(
            StorageResourceRepository storageResourceRepository,
            StorageInterfaceRepository storageInterfaceRepository,
            StorageResourceMapper storageResourceMapper,
            DataMovementInterfaceMapper dataMovementInterfaceMapper) {
        this.storageResourceRepository = storageResourceRepository;
        this.storageInterfaceRepository = storageInterfaceRepository;
        this.storageResourceMapper = storageResourceMapper;
        this.dataMovementInterfaceMapper = dataMovementInterfaceMapper;
    }

    public String addStorageResource(StorageResourceDescription description) throws AppCatalogException {
        try {
            // Generate storageResourceId if not set
            String storageResourceId = description.getStorageResourceId();
            if (storageResourceId == null
                    || storageResourceId.isEmpty()
                    || AiravataCommonsConstants.DEFAULT_ID.equals(storageResourceId)) {
                storageResourceId = AppCatalogUtils.getID(description.getHostName());
                description.setStorageResourceId(storageResourceId);
            }
            description.setCreationTime(System.currentTimeMillis());
            if (description.getDataMovementInterfaces() != null) {
                description.getDataMovementInterfaces().stream()
                        .forEach(dm -> dm.setStorageResourceId(description.getStorageResourceId()));
            }
            StorageResourceEntity entity = storageResourceMapper.toEntity(description);
            // Ensure storageResourceId is set on entity (Dozer might not map it correctly)
            entity.setStorageResourceId(storageResourceId);
            // Ensure creationTime and updateTime are set
            if (entity.getCreationTime() == null) {
                entity.setCreationTime(new java.sql.Timestamp(description.getCreationTime()));
            }
            if (entity.getUpdateTime() == null) {
                entity.setUpdateTime(new java.sql.Timestamp(System.currentTimeMillis()));
            }
            // Ensure storageResourceId is set on all StorageInterfaceEntity objects
            if (entity.getDataMovementInterfaces() != null) {
                for (StorageInterfaceEntity storageInterface : entity.getDataMovementInterfaces()) {
                    if (storageInterface.getStorageResourceId() == null) {
                        storageInterface.setStorageResourceId(storageResourceId);
                    }
                    if (storageInterface.getCreationTime() == null) {
                        storageInterface.setCreationTime(new java.sql.Timestamp(System.currentTimeMillis()));
                    }
                    if (storageInterface.getUpdateTime() == null) {
                        storageInterface.setUpdateTime(new java.sql.Timestamp(System.currentTimeMillis()));
                    }
                }
            }
            StorageResourceEntity saved = storageResourceRepository.save(entity);
            return saved.getStorageResourceId();
        } catch (Exception e) {
            logger.error(
                    "Error while saving storage resource. StorageResourceId : " + description.getStorageResourceId()
                            + "" + " HostName : " + description.getHostName(),
                    e);
            throw new AppCatalogException(
                    "Error while saving storage resource. StorageResourceId : " + description.getStorageResourceId()
                            + "" + " HostName : " + description.getHostName(),
                    e);
        }
    }

    public void updateStorageResource(String storageResourceId, StorageResourceDescription updatedStorageResource)
            throws AppCatalogException {
        try {
            updatedStorageResource.setUpdateTime(System.currentTimeMillis());
            if (updatedStorageResource.getDataMovementInterfaces() != null) {
                updatedStorageResource.getDataMovementInterfaces().stream()
                        .forEach(dm -> dm.setStorageResourceId(updatedStorageResource.getStorageResourceId()));
            }

            StorageResourceEntity existingEntity =
                    storageResourceRepository.findById(storageResourceId).orElse(null);
            if (existingEntity != null) {
                // Preserve existing dataMovementInterfaces to avoid duplicate entity issues
                List<StorageInterfaceEntity> existingInterfaces = existingEntity.getDataMovementInterfaces();
                // Temporarily null out to prevent MapStruct from merging incorrectly
                existingEntity.setDataMovementInterfaces(null);
                // Update basic fields
                existingEntity.setStorageResourceDescription(updatedStorageResource.getStorageResourceDescription());
                existingEntity.setEnabled(updatedStorageResource.getEnabled());
                existingEntity.setHostName(updatedStorageResource.getHostName());
                // Manually handle dataMovementInterfaces to avoid duplicate entity issues
                if (updatedStorageResource.getDataMovementInterfaces() != null) {
                    List<StorageInterfaceEntity> newInterfaces = new java.util.ArrayList<>();
                    Map<String, StorageInterfaceEntity> existingMap = new java.util.HashMap<>();
                    if (existingInterfaces != null) {
                        existingInterfaces.forEach(iface -> existingMap.put(iface.getDataMovementInterfaceId(), iface));
                    }
                    for (DataMovementInterface dmInterface : updatedStorageResource.getDataMovementInterfaces()) {
                        StorageInterfaceEntity ifaceEntity = existingMap.get(dmInterface.getDataMovementInterfaceId());
                        if (ifaceEntity == null) {
                            ifaceEntity = dataMovementInterfaceMapper.toEntity(dmInterface);
                            ifaceEntity.setStorageResourceId(storageResourceId);
                            ifaceEntity.setStorageResource(existingEntity);
                            // Ensure creationTime and updateTime are set
                            if (ifaceEntity.getCreationTime() == null) {
                                ifaceEntity.setCreationTime(new java.sql.Timestamp(System.currentTimeMillis()));
                            }
                            if (ifaceEntity.getUpdateTime() == null) {
                                ifaceEntity.setUpdateTime(new java.sql.Timestamp(System.currentTimeMillis()));
                            }
                        } else {
                            // Update existing interface
                            ifaceEntity.setDataMovementProtocol(dmInterface.getDataMovementProtocol());
                            ifaceEntity.setPriorityOrder(dmInterface.getPriorityOrder());
                            ifaceEntity.setUpdateTime(new java.sql.Timestamp(System.currentTimeMillis()));
                        }
                        newInterfaces.add(ifaceEntity);
                    }
                    existingEntity.setDataMovementInterfaces(newInterfaces);
                } else {
                    existingEntity.setDataMovementInterfaces(new java.util.ArrayList<>());
                }
                // Ensure updateTime is set
                existingEntity.setUpdateTime(new java.sql.Timestamp(updatedStorageResource.getUpdateTime()));
                storageResourceRepository.save(existingEntity);
            } else {
                StorageResourceEntity entity = storageResourceMapper.toEntity(updatedStorageResource);
                // Ensure storageResourceId is set
                entity.setStorageResourceId(storageResourceId);
                // Ensure creationTime and updateTime are set
                if (entity.getCreationTime() == null) {
                    entity.setCreationTime(new java.sql.Timestamp(updatedStorageResource.getCreationTime()));
                }
                if (entity.getUpdateTime() == null) {
                    entity.setUpdateTime(new java.sql.Timestamp(updatedStorageResource.getUpdateTime()));
                }
                // Ensure dataMovementInterfaces have storageResourceId and timestamps set
                if (entity.getDataMovementInterfaces() != null) {
                    for (StorageInterfaceEntity iface : entity.getDataMovementInterfaces()) {
                        iface.setStorageResourceId(storageResourceId);
                        iface.setStorageResource(entity);
                        if (iface.getCreationTime() == null) {
                            iface.setCreationTime(new java.sql.Timestamp(System.currentTimeMillis()));
                        }
                        if (iface.getUpdateTime() == null) {
                            iface.setUpdateTime(new java.sql.Timestamp(System.currentTimeMillis()));
                        }
                    }
                }
                storageResourceRepository.save(entity);
            }
        } catch (Exception e) {
            logger.error(
                    "Error while updating storage resource. StorageResourceId : "
                            + updatedStorageResource.getStorageResourceId() + "" + " HostName : "
                            + updatedStorageResource.getHostName(),
                    e);
            throw new AppCatalogException(
                    "Error while updating storage resource. StorageResourceId : "
                            + updatedStorageResource.getStorageResourceId() + "" + " HostName : "
                            + updatedStorageResource.getHostName(),
                    e);
        }
    }

    public StorageResourceDescription getStorageResource(String resourceId) throws AppCatalogException {
        try {
            StorageResourceEntity entity =
                    storageResourceRepository.findById(resourceId).orElse(null);
            if (entity == null) return null;
            return storageResourceMapper.toModel(entity);
        } catch (Exception e) {
            logger.error("Error while retrieving storage resource. Resource Id: " + resourceId, e);
            throw new AppCatalogException("Error while retrieving storage resource. Resource Id: " + resourceId, e);
        }
    }

    public List<StorageResourceDescription> getStorageResourceList(Map<String, String> filters)
            throws AppCatalogException {
        try {
            if (filters.containsKey(DBConstants.StorageResource.HOST_NAME)) {
                String hostName = "%" + filters.get(DBConstants.StorageResource.HOST_NAME) + "%";
                List<StorageResourceEntity> entities = storageResourceRepository.findByHostName(hostName);
                return storageResourceMapper.toModelList(entities);
            } else {
                logger.error("Unsupported field name for storage resource. "
                        + filters.get(DBConstants.StorageResource.HOST_NAME));
                throw new IllegalArgumentException("Unsupported field name for storage resource. "
                        + filters.get(DBConstants.StorageResource.HOST_NAME));
            }
        } catch (Exception e) {
            logger.error("Error while retrieving storage resource list", e);
            throw new AppCatalogException("Error while retrieving storage resource list", e);
        }
    }

    public List<StorageResourceDescription> getAllStorageResourceList() throws AppCatalogException {
        try {
            List<StorageResourceEntity> entities = storageResourceRepository.findAll();
            return storageResourceMapper.toModelList(entities);
        } catch (Exception e) {
            logger.error("Error while retrieving storage resource list", e);
            throw new AppCatalogException("Error while retrieving storage resource list", e);
        }
    }

    public Map<String, String> getAllStorageResourceIdList() throws AppCatalogException {
        try {
            List<StorageResourceEntity> entities = storageResourceRepository.findAll();
            return getStorageResourceMap(entities);
        } catch (Exception e) {
            logger.error("Error while retrieving storage resource ID map", e);
            throw new AppCatalogException("Error while retrieving storage resource ID map", e);
        }
    }

    public Map<String, String> getAvailableStorageResourceIdList() throws AppCatalogException {
        try {
            List<StorageResourceEntity> entities = storageResourceRepository.findAvailableStorageResources();
            return getStorageResourceMap(entities);
        } catch (Exception e) {
            logger.error("Error while retrieving available storage resource ID map", e);
            throw new AppCatalogException("Error while retrieving available storage resource ID map", e);
        }
    }

    public boolean isStorageResourceExists(String resourceId) throws AppCatalogException {
        try {
            return storageResourceRepository.existsById(resourceId);
        } catch (Exception e) {
            logger.error("Error while retrieving storage resource. Resource ID: " + resourceId, e);
            throw new AppCatalogException("Error while retrieving storage resource. Resource ID: " + resourceId, e);
        }
    }

    public void removeStorageResource(String resourceId) throws AppCatalogException {
        try {
            storageResourceRepository.deleteById(resourceId);
        } catch (Exception e) {
            logger.error("Error while removing storage resource Resource ID: " + resourceId, e);
            throw new AppCatalogException("Error while removing storage resource Resource ID: " + resourceId, e);
        }
    }

    public String addDataMovementInterface(DataMovementInterface dataMovementInterface) {
        StorageInterfaceEntity storageInterfaceEntity = dataMovementInterfaceMapper.toEntity(dataMovementInterface);
        StorageInterfaceEntity saved = storageInterfaceRepository.save(storageInterfaceEntity);
        return saved.getDataMovementInterfaceId();
    }

    public void removeDataMovementInterface(String storageResourceId, String dataMovementInterfaceId)
            throws AppCatalogException {
        try {
            StorageInterfacePK storageInterfacePK = new StorageInterfacePK();
            storageInterfacePK.setDataMovementInterfaceId(dataMovementInterfaceId);
            storageInterfacePK.setStorageResourceId(storageResourceId);
            storageInterfaceRepository.deleteById(storageInterfacePK);
        } catch (Exception e) {
            logger.error(
                    "Error removing storage data movement interface. StorageResourceId: " + storageResourceId + ""
                            + " DataMovementInterfaceId: " + dataMovementInterfaceId,
                    e);
            throw new AppCatalogException(
                    "Error removing storage data movement interface. StorageResourceId: " + storageResourceId + ""
                            + " DataMovementInterfaceId: " + dataMovementInterfaceId,
                    e);
        }
    }

    private Map<String, String> getStorageResourceMap(List<StorageResourceEntity> entities) {
        Map<String, String> storageResourceMap = new HashMap<>();
        if (entities != null) {
            for (StorageResourceEntity entity : entities) {
                StorageResourceDescription description = storageResourceMapper.toModel(entity);
                storageResourceMap.put(description.getStorageResourceId(), description.getHostName());
            }
        }
        return storageResourceMap;
    }
}
