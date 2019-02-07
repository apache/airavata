/*
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
package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.model.commons.airavata_commonsConstants;
import org.apache.airavata.model.data.movement.DataMovementInterface;
import org.apache.airavata.registry.core.entities.appcatalog.DataMovementInterfaceEntity;
import org.apache.airavata.registry.core.entities.appcatalog.StorageInterfaceEntity;
import org.apache.airavata.registry.core.entities.appcatalog.StorageInterfacePK;
import org.apache.airavata.registry.core.entities.appcatalog.StorageResourceEntity;
import org.apache.airavata.registry.core.utils.AppCatalogUtils;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.core.utils.QueryConstants;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.registry.cpi.StorageResource;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by skariyat on 3/12/18.
 */
public class StorageResourceRepository extends AppCatAbstractRepository<StorageResourceDescription, StorageResourceEntity, String> implements StorageResource {

    private final static Logger logger = LoggerFactory.getLogger(StorageResourceRepository.class);


    public StorageResourceRepository() {
        super(StorageResourceDescription.class, StorageResourceEntity.class);
    }


    @Override
    public String addStorageResource(StorageResourceDescription description) throws AppCatalogException {
        try {
            final String storageResourceId = AppCatalogUtils.getID(description.getHostName());
            if ("".equals(description.getStorageResourceId()) || airavata_commonsConstants.DEFAULT_ID.equals(description.getStorageResourceId())) {
                description.setStorageResourceId(storageResourceId);
            }
            description.setCreationTime(System.currentTimeMillis());
            if (description.getDataMovementInterfaces() != null) {
                description.getDataMovementInterfaces().stream().forEach(dm -> dm.setStorageResourceId(description.getStorageResourceId()));
            }
            StorageResourceDescription storageResourceDescription = create(description);
            return storageResourceDescription.getStorageResourceId();
        } catch (Exception e) {
            logger.error("Error while saving storage resource. StorageResourceId : " + description.getStorageResourceId() + ""
                    + " HostName : " + description.getHostName(), e);
            throw new AppCatalogException("Error while saving storage resource. StorageResourceId : " + description.getStorageResourceId() + ""
                    + " HostName : " + description.getHostName(), e);
        }
    }

    @Override
    public void updateStorageResource(String storageResourceId, StorageResourceDescription updatedStorageResource) throws AppCatalogException {
        try {
            updatedStorageResource.setUpdateTime(System.currentTimeMillis());
            if (updatedStorageResource.getDataMovementInterfaces() != null) {
                updatedStorageResource.getDataMovementInterfaces().stream().forEach(dm -> dm.setStorageResourceId(updatedStorageResource.getStorageResourceId()));
            }
            update(updatedStorageResource);
        } catch (Exception e) {
            logger.error("Error while updating storage resource. StorageResourceId : " + updatedStorageResource.getStorageResourceId() + ""
                    + " HostName : " + updatedStorageResource.getHostName(), e);
            throw new AppCatalogException("Error while updating storage resource. StorageResourceId : " + updatedStorageResource.getStorageResourceId() + ""
                    + " HostName : " + updatedStorageResource.getHostName(), e);
        }
    }

    @Override
    public StorageResourceDescription getStorageResource(String resourceId) throws AppCatalogException {
        try {
            return get(resourceId);
        } catch (Exception e) {
            logger.error("Error while retrieving storage resource. Resource Id: " + resourceId , e);
            throw new AppCatalogException("Error while retrieving storage resource. Resource Id: " + resourceId, e);
        }
    }

    @Override
    public List<StorageResourceDescription> getStorageResourceList(Map<String, String> filters) throws AppCatalogException {
        try {
            if (filters.containsKey(DBConstants.StorageResource.HOST_NAME)) {
                Map<String,Object> queryParameters = new HashMap<>();
                queryParameters.put(DBConstants.ComputeResource.HOST_NAME, filters.get(DBConstants.StorageResource.HOST_NAME));
                List<StorageResourceDescription> storageResourceDescriptionList = select(QueryConstants.FIND_STORAGE_RESOURCE, -1, 0, queryParameters);
                return storageResourceDescriptionList;
            } else {
                logger.error("Unsupported field name for compute resource. " + filters.get(DBConstants.StorageResource.HOST_NAME));
                throw new IllegalArgumentException("Unsupported field name for compute resource. " + filters.get(DBConstants.StorageResource.HOST_NAME));
            }
        } catch (Exception e) {
            logger.error("Error while retrieving storage resource list", e);
            throw new AppCatalogException("Error while retrieving storage resource list", e);
        }
    }

    @Override
    public List<StorageResourceDescription> getAllStorageResourceList() throws AppCatalogException {
        try {
            return select(QueryConstants.FIND_ALL_STORAGE_RESOURCES, 0);
        } catch (Exception e) {
            logger.error("Error while retrieving storage resource list", e);
            throw new AppCatalogException("Error while retrieving storage resource list", e);
        }
    }

    @Override
    public Map<String, String> getAllStorageResourceIdList() throws AppCatalogException {
        try {
            Map<String, String> storageResourceMap = new HashMap<String, String>();
            List<StorageResourceDescription> storageResourceDescriptionList = select(QueryConstants.FIND_ALL_STORAGE_RESOURCES, 0);
            return getStorageResourceMap(storageResourceDescriptionList);
        } catch (Exception e) {
            logger.error("Error while retrieving storage resource ID map", e);
            throw new AppCatalogException("Error while retrieving storage resource ID map", e);
        }
    }

    @Override
    public Map<String, String> getAvailableStorageResourceIdList() throws AppCatalogException {
        try {
            Map<String, String> storageResourceMap = new HashMap<String, String>();
            List<StorageResourceDescription> storageResourceDescriptionList = select(QueryConstants.FIND_ALL_AVAILABLE_STORAGE_RESOURCES, 0);
            return getStorageResourceMap(storageResourceDescriptionList);
        } catch (Exception e) {
            logger.error("Error while retrieving available storage resource ID map", e);
            throw new AppCatalogException("Error while retrieving available storage resource ID map", e);
        }
    }

    @Override
    public boolean isStorageResourceExists(String resourceId) throws AppCatalogException {
        try {
            return isExists(resourceId);
        } catch (Exception e) {
            logger.error("Error while retrieving storage resource. Resource ID: "+ resourceId, e);
            throw new AppCatalogException("Error while retrieving storage resource. Resource ID: "+ resourceId, e);
        }
    }

    @Override
    public void removeStorageResource(String resourceId) throws AppCatalogException {
        try {
            delete(resourceId);
        } catch (Exception e) {
            logger.error("Error while removing storage resource Resource ID: "+ resourceId, e);
            throw new AppCatalogException("Error while removing storage resource Resource ID: "+ resourceId, e);
        }
    }

    public String addDataMovementInterface(DataMovementInterface dataMovementInterface) {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        StorageInterfaceEntity storageInterfaceEntity = mapper.map(dataMovementInterface, StorageInterfaceEntity.class);
        execute(entityManager -> entityManager.merge(storageInterfaceEntity));
        return dataMovementInterface.getDataMovementInterfaceId();
    }

    @Override
    public void removeDataMovementInterface(String storageResourceId, String dataMovementInterfaceId) throws AppCatalogException {
        try {
            StorageInterfacePK storageInterfacePK = new StorageInterfacePK();
            storageInterfacePK.setDataMovementInterfaceId(dataMovementInterfaceId);
            storageInterfacePK.setStorageResourceId(storageResourceId);
            execute(entityManager -> {
                StorageInterfaceEntity entity = entityManager.find(StorageInterfaceEntity.class, storageInterfacePK);
                entityManager.remove(entity);
                return entity;
            });
        } catch (Exception e) {
            logger.error("Error removing storage data movement interface. StorageResourceId: "+ storageResourceId + ""
                    + " DataMovementInterfaceId: "+ dataMovementInterfaceId, e);
            throw new AppCatalogException("Error removing storage data movement interface. StorageResourceId: "+ storageResourceId + ""
                    + " DataMovementInterfaceId: "+ dataMovementInterfaceId, e);
        }
    }

    private Map<String, String> getStorageResourceMap(List<StorageResourceDescription> storageResourceDescriptionList) {
        Map<String, String> storageResourceMap = new HashMap<String, String>();
        if (storageResourceDescriptionList != null) {
            for (StorageResourceDescription storageResourceDescription: storageResourceDescriptionList) {
                storageResourceMap.put(storageResourceDescription.getStorageResourceId(), storageResourceDescription.getHostName());
            }
        }
        return storageResourceMap;
    }
}
