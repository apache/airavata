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

import com.github.dozermapper.core.Mapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.model.commons.airavata_commonsConstants;
import org.apache.airavata.model.data.movement.DataMovementInterface;
import org.apache.airavata.registry.entities.appcatalog.StorageInterfaceEntity;
import org.apache.airavata.registry.entities.appcatalog.StorageInterfacePK;
import org.apache.airavata.registry.entities.appcatalog.StorageResourceEntity;
import org.apache.airavata.registry.exceptions.AppCatalogException;
import org.apache.airavata.registry.repositories.appcatalog.StorageInterfaceRepository;
import org.apache.airavata.registry.repositories.appcatalog.StorageResourceRepository;
import org.apache.airavata.registry.utils.AppCatalogUtils;
import org.apache.airavata.registry.utils.DBConstants;
import org.apache.airavata.registry.utils.ObjectMapperSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StorageResourceService {
    private static final Logger logger = LoggerFactory.getLogger(StorageResourceService.class);

    @Autowired
    private StorageResourceRepository storageResourceRepository;

    @Autowired
    private StorageInterfaceRepository storageInterfaceRepository;

    public String addStorageResource(StorageResourceDescription description) throws AppCatalogException {
        try {
            final String storageResourceId = AppCatalogUtils.getID(description.getHostName());
            if ("".equals(description.getStorageResourceId())
                    || airavata_commonsConstants.DEFAULT_ID.equals(description.getStorageResourceId())) {
                description.setStorageResourceId(storageResourceId);
            }
            description.setCreationTime(System.currentTimeMillis());
            if (description.getDataMovementInterfaces() != null) {
                description.getDataMovementInterfaces().stream()
                        .forEach(dm -> dm.setStorageResourceId(description.getStorageResourceId()));
            }
            Mapper mapper = ObjectMapperSingleton.getInstance();
            StorageResourceEntity entity = mapper.map(description, StorageResourceEntity.class);
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
            Mapper mapper = ObjectMapperSingleton.getInstance();
            StorageResourceEntity entity = mapper.map(updatedStorageResource, StorageResourceEntity.class);
            storageResourceRepository.save(entity);
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
            Mapper mapper = ObjectMapperSingleton.getInstance();
            return mapper.map(entity, StorageResourceDescription.class);
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
                Mapper mapper = ObjectMapperSingleton.getInstance();
                return entities.stream()
                        .map(e -> mapper.map(e, StorageResourceDescription.class))
                        .collect(Collectors.toList());
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
            Mapper mapper = ObjectMapperSingleton.getInstance();
            return entities.stream()
                    .map(e -> mapper.map(e, StorageResourceDescription.class))
                    .collect(Collectors.toList());
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
        Mapper mapper = ObjectMapperSingleton.getInstance();
        StorageInterfaceEntity storageInterfaceEntity = mapper.map(dataMovementInterface, StorageInterfaceEntity.class);
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
                Mapper mapper = ObjectMapperSingleton.getInstance();
                StorageResourceDescription description = mapper.map(entity, StorageResourceDescription.class);
                storageResourceMap.put(description.getStorageResourceId(), description.getHostName());
            }
        }
        return storageResourceMap;
    }
}
