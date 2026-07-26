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

import org.apache.airavata.common.AiravataUtils;
import org.apache.airavata.db.AbstractRepository;
import org.apache.airavata.db.DBConstants;
import org.apache.airavata.db.QueryConstants;
import org.apache.airavata.models.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.storage.mapper.StorageMapper;
import org.apache.airavata.storage.model.StorageResourceEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by skariyat on 3/12/18.
 */
@Component
public class StorageResourceRepository
        extends AbstractRepository<StorageResourceDescription, StorageResourceEntity, String> {

    private static final Logger logger = LoggerFactory.getLogger(StorageResourceRepository.class);

    public StorageResourceRepository() {
        super(StorageResourceDescription.class, StorageResourceEntity.class);
    }

    @Override
    protected StorageResourceDescription toModel(StorageResourceEntity entity) {
        return StorageMapper.INSTANCE.storageResourceToModel(entity);
    }

    @Override
    protected StorageResourceEntity toEntity(StorageResourceDescription model) {
        return StorageMapper.INSTANCE.storageResourceToEntity(model);
    }

    public String addStorageResource(StorageResourceDescription description) throws Exception {
        try {
            final String storageResourceId = AiravataUtils.getId(description.hostName());
            StorageResourceDescription.Builder descBuilder = description.toBuilder();
            if (description.storageResourceId().isEmpty()
                    || "DO_NOT_SET_AT_CLIENTS".equals(description.storageResourceId())) {
                descBuilder.setStorageResourceId(storageResourceId);
            }
            descBuilder.setCreationTime(System.currentTimeMillis());
            description = descBuilder.build();
            StorageResourceDescription storageResourceDescription = create(description);
            return storageResourceDescription.storageResourceId();
        } catch (Exception e) {
            logger.error(
                    "Error while saving storage resource. StorageResourceId : " + description.storageResourceId()
                            + "" + " HostName : " + description.hostName(),
                    e);
            throw new Exception(
                    "Error while saving storage resource. StorageResourceId : " + description.storageResourceId()
                            + "" + " HostName : " + description.hostName(),
                    e);
        }
    }

    public void updateStorageResource(String storageResourceId, StorageResourceDescription updatedStorageResource)
            throws Exception {
        try {
            updatedStorageResource = updatedStorageResource.toBuilder().setUpdateTime(System.currentTimeMillis())
                    .build();
            update(updatedStorageResource);
        } catch (Exception e) {
            logger.error(
                    "Error while updating storage resource. StorageResourceId : "
                            + updatedStorageResource.storageResourceId() + "" + " HostName : "
                            + updatedStorageResource.hostName(),
                    e);
            throw new Exception(
                    "Error while updating storage resource. StorageResourceId : "
                            + updatedStorageResource.storageResourceId() + "" + " HostName : "
                            + updatedStorageResource.hostName(),
                    e);
        }
    }

    public StorageResourceDescription getStorageResource(String resourceId) throws Exception {
        try {
            return get(resourceId);
        } catch (Exception e) {
            logger.error("Error while retrieving storage resource. Resource Id: " + resourceId, e);
            throw new Exception("Error while retrieving storage resource. Resource Id: " + resourceId, e);
        }
    }

    public List<StorageResourceDescription> getStorageResourceList(Map<String, String> filters)
            throws Exception {
        try {
            if (filters.containsKey(DBConstants.StorageResource.HOST_NAME)) {
                Map<String, Object> queryParameters = new HashMap<>();
                queryParameters.put(
                        DBConstants.ComputeResource.HOST_NAME, filters.get(DBConstants.StorageResource.HOST_NAME));
                List<StorageResourceDescription> storageResourceDescriptionList = select(
                        QueryConstants.FIND_STORAGE_RESOURCE, -1, 0, queryParameters);
                return storageResourceDescriptionList;
            } else {
                logger.error("Unsupported field name for compute resource. "
                        + filters.get(DBConstants.StorageResource.HOST_NAME));
                throw new IllegalArgumentException("Unsupported field name for compute resource. "
                        + filters.get(DBConstants.StorageResource.HOST_NAME));
            }
        } catch (Exception e) {
            logger.error("Error while retrieving storage resource list", e);
            throw new Exception("Error while retrieving storage resource list", e);
        }
    }

    public List<StorageResourceDescription> getAllStorageResourceList() throws Exception {
        try {
            return select(QueryConstants.FIND_ALL_STORAGE_RESOURCES, 0);
        } catch (Exception e) {
            logger.error("Error while retrieving storage resource list", e);
            throw new Exception("Error while retrieving storage resource list", e);
        }
    }

    public Map<String, String> getAllStorageResourceIdList() throws Exception {
        try {
            List<StorageResourceDescription> storageResourceDescriptionList = select(
                    QueryConstants.FIND_ALL_STORAGE_RESOURCES, 0);
            return getStorageResourceMap(storageResourceDescriptionList);
        } catch (Exception e) {
            logger.error("Error while retrieving storage resource ID map", e);
            throw new Exception("Error while retrieving storage resource ID map", e);
        }
    }

    public Map<String, String> getAvailableStorageResourceIdList() throws Exception {
        try {
            List<StorageResourceDescription> storageResourceDescriptionList = select(
                    QueryConstants.FIND_ALL_AVAILABLE_STORAGE_RESOURCES, 0);
            return getStorageResourceMap(storageResourceDescriptionList);
        } catch (Exception e) {
            logger.error("Error while retrieving available storage resource ID map", e);
            throw new Exception("Error while retrieving available storage resource ID map", e);
        }
    }

    public boolean isStorageResourceExists(String resourceId) throws Exception {
        try {
            return isExists(resourceId);
        } catch (Exception e) {
            logger.error("Error while retrieving storage resource. Resource ID: " + resourceId, e);
            throw new Exception("Error while retrieving storage resource. Resource ID: " + resourceId, e);
        }
    }

    public void removeStorageResource(String resourceId) throws Exception {
        try {
            delete(resourceId);
        } catch (Exception e) {
            logger.error("Error while removing storage resource Resource ID: " + resourceId, e);
            throw new Exception("Error while removing storage resource Resource ID: " + resourceId, e);
        }
    }

    public void removeDataMovementInterface(String storageResourceId, String dataMovementInterfaceId)
            throws Exception {
        // Storage resources are reached over SFTP; there is no data-movement interface
        // to remove.
    }

    private Map<String, String> getStorageResourceMap(List<StorageResourceDescription> storageResourceDescriptionList) {
        Map<String, String> storageResourceMap = new HashMap<String, String>();
        if (storageResourceDescriptionList != null) {
            for (StorageResourceDescription storageResourceDescription : storageResourceDescriptionList) {
                storageResourceMap.put(
                        storageResourceDescription.storageResourceId(), storageResourceDescription.hostName());
            }
        }
        return storageResourceMap;
    }
}
