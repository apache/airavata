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
package org.apache.airavata.compute.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.compute.mapper.ComputeMapper;
import org.apache.airavata.compute.model.ComputeResourceEntity;
import org.apache.airavata.compute.model.ResourceJobManagerEntity;
import org.apache.airavata.db.AbstractRepository;
import org.apache.airavata.db.AppCatalogUtils;
import org.apache.airavata.db.DBConstants;
import org.apache.airavata.db.QueryConstants;
import org.apache.airavata.interfaces.AppCatalogException;
import org.apache.airavata.interfaces.ComputeResource;
import org.apache.airavata.model.appcatalog.computeresource.proto.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.proto.JobManagerCommand;
import org.apache.airavata.model.appcatalog.computeresource.proto.ResourceJobManager;
import org.apache.airavata.model.parallelism.proto.ApplicationParallelismType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ComputeResourceRepository
        extends AbstractRepository<ComputeResourceDescription, ComputeResourceEntity, String>
        implements ComputeResource {

    private static final Logger logger = LoggerFactory.getLogger(ComputeResourceRepository.class);

    public ComputeResourceRepository() {
        super(ComputeResourceDescription.class, ComputeResourceEntity.class);
    }

    @Override
    protected ComputeResourceDescription toModel(ComputeResourceEntity entity) {
        return ComputeMapper.INSTANCE.computeResourceToModel(entity);
    }

    @Override
    protected ComputeResourceEntity toEntity(ComputeResourceDescription model) {
        return ComputeMapper.INSTANCE.computeResourceToEntity(model);
    }

    @Override
    public String addComputeResource(ComputeResourceDescription description) throws AppCatalogException {
        if (description.getComputeResourceId().equals("")
                || description.getComputeResourceId().equals("DO_NOT_SET_AT_CLIENTS")) {
            description = description.toBuilder()
                    .setComputeResourceId(AppCatalogUtils.getID(description.getHostName()))
                    .build();
        }
        return saveComputeResourceDescriptorData(description);
    }

    protected String saveComputeResourceDescriptorData(ComputeResourceDescription description)
            throws AppCatalogException {
        ComputeResourceEntity computeResourceEntity = saveComputeResource(description);
        return computeResourceEntity.getComputeResourceId();
    }

    protected ComputeResourceEntity saveComputeResource(ComputeResourceDescription description)
            throws AppCatalogException {
        String computeResourceId = description.getComputeResourceId();
        ComputeResourceEntity computeResourceEntity = ComputeMapper.INSTANCE.computeResourceToEntity(description);
        if (computeResourceEntity.getBatchQueues() != null) {
            computeResourceEntity
                    .getBatchQueues()
                    .forEach(batchQueueEntity -> batchQueueEntity.setComputeResourceId(computeResourceId));
        }
        return execute(entityManager -> entityManager.merge(computeResourceEntity));
    }

    @Override
    public void updateComputeResource(String computeResourceId, ComputeResourceDescription updatedComputeResource)
            throws AppCatalogException {
        saveComputeResourceDescriptorData(updatedComputeResource);
    }

    @Override
    public ComputeResourceDescription getComputeResource(String resourceId) throws AppCatalogException {
        return get(resourceId);
    }

    @Override
    public List<ComputeResourceDescription> getComputeResourceList(Map<String, String> filters)
            throws AppCatalogException {
        if (filters.containsKey(DBConstants.ComputeResource.HOST_NAME)) {
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(
                    DBConstants.ComputeResource.HOST_NAME, filters.get(DBConstants.ComputeResource.HOST_NAME));
            return select(QueryConstants.FIND_COMPUTE_RESOURCE, -1, 0, queryParameters);
        } else {
            logger.error("Unsupported field name for compute resource.", new IllegalArgumentException());
            throw new IllegalArgumentException("Unsupported field name for compute resource.");
        }
    }

    @Override
    public List<ComputeResourceDescription> getAllComputeResourceList() throws AppCatalogException {
        return select(QueryConstants.FIND_ALL_COMPUTE_RESOURCES, 0);
    }

    @Override
    public Map<String, String> getAllComputeResourceIdList() throws AppCatalogException {
        Map<String, String> computeResourceMap = new HashMap<String, String>();
        List<ComputeResourceDescription> computeResourceDescriptionList =
                select(QueryConstants.FIND_ALL_COMPUTE_RESOURCES, 0);
        if (computeResourceDescriptionList != null && !computeResourceDescriptionList.isEmpty()) {
            for (ComputeResourceDescription computeResourceDescription : computeResourceDescriptionList) {
                computeResourceMap.put(
                        computeResourceDescription.getComputeResourceId(), computeResourceDescription.getHostName());
            }
        }
        return computeResourceMap;
    }

    @Override
    public Map<String, String> getAvailableComputeResourceIdList() throws AppCatalogException {
        Map<String, String> computeResourceMap = new HashMap<String, String>();
        List<ComputeResourceDescription> computeResourceDescriptionList =
                select(QueryConstants.FIND_ALL_COMPUTE_RESOURCES, 0);
        if (computeResourceDescriptionList != null && !computeResourceDescriptionList.isEmpty()) {
            for (ComputeResourceDescription computeResourceDescription : computeResourceDescriptionList) {
                if (computeResourceDescription.getEnabled()) {
                    computeResourceMap.put(
                            computeResourceDescription.getComputeResourceId(),
                            computeResourceDescription.getHostName());
                }
            }
        }
        return computeResourceMap;
    }

    @Override
    public boolean isComputeResourceExists(String resourceId) throws AppCatalogException {
        return isExists(resourceId);
    }

    @Override
    public void removeComputeResource(String resourceId) throws AppCatalogException {
        delete(resourceId);
    }

    @Override
    public String addResourceJobManager(ResourceJobManager resourceJobManager) throws AppCatalogException {
        ResourceJobManagerRepository resourceJobManagerRepository = new ResourceJobManagerRepository();
        resourceJobManager = resourceJobManager.toBuilder()
                .setResourceJobManagerId(AppCatalogUtils.getID("RJM"))
                .build();
        resourceJobManagerRepository.create(resourceJobManager);
        ResourceJobManagerEntity resourceJobManagerEntity =
                ComputeMapper.INSTANCE.resourceJobManagerToEntity(resourceJobManager);
        Map<Integer, String> jobManagerCommands = resourceJobManager.getJobManagerCommandsMap();
        if (jobManagerCommands != null && jobManagerCommands.size() != 0) {
            resourceJobManagerRepository.createJobManagerCommand(jobManagerCommands, resourceJobManagerEntity);
        }

        Map<Integer, String> parallelismPrefix = resourceJobManager.getParallelismPrefixMap();
        if (parallelismPrefix != null && parallelismPrefix.size() != 0) {
            resourceJobManagerRepository.createParallesimPrefix(parallelismPrefix, resourceJobManagerEntity);
        }
        return resourceJobManager.getResourceJobManagerId();
    }

    @Override
    public void updateResourceJobManager(String resourceJobManagerId, ResourceJobManager updatedResourceJobManager)
            throws AppCatalogException {
        ResourceJobManagerRepository resourceJobManagerRepository = new ResourceJobManagerRepository();
        updatedResourceJobManager = updatedResourceJobManager.toBuilder()
                .setResourceJobManagerId(resourceJobManagerId)
                .build();
        ResourceJobManager resourceJobManager = resourceJobManagerRepository.create(updatedResourceJobManager);
        ResourceJobManagerEntity resourceJobManagerEntity =
                ComputeMapper.INSTANCE.resourceJobManagerToEntity(resourceJobManager);
        Map<Integer, String> jobManagerCommands = updatedResourceJobManager.getJobManagerCommandsMap();
        if (jobManagerCommands != null && jobManagerCommands.size() != 0) {
            resourceJobManagerRepository.createJobManagerCommand(jobManagerCommands, resourceJobManagerEntity);
        }

        Map<Integer, String> parallelismPrefix = updatedResourceJobManager.getParallelismPrefixMap();
        if (parallelismPrefix != null && parallelismPrefix.size() != 0) {
            resourceJobManagerRepository.createParallesimPrefix(parallelismPrefix, resourceJobManagerEntity);
        }
    }

    @Override
    public ResourceJobManager getResourceJobManager(String resourceJobManagerId) throws AppCatalogException {
        ResourceJobManagerRepository resourceJobManagerRepository = new ResourceJobManagerRepository();
        ResourceJobManager resourceJobManager = resourceJobManagerRepository.get(resourceJobManagerId);
        if (resourceJobManager != null) {
            // Convert enum-keyed maps to integer-keyed maps for proto
            Map<JobManagerCommand, String> jmCommands =
                    resourceJobManagerRepository.getJobManagerCommand(resourceJobManagerId);
            Map<Integer, String> jmCommandsInt = new HashMap<>();
            for (Map.Entry<JobManagerCommand, String> entry : jmCommands.entrySet()) {
                jmCommandsInt.put(entry.getKey().getNumber(), entry.getValue());
            }

            Map<ApplicationParallelismType, String> parPrefix =
                    resourceJobManagerRepository.getParallelismPrefix(resourceJobManagerId);
            Map<Integer, String> parPrefixInt = new HashMap<>();
            for (Map.Entry<ApplicationParallelismType, String> entry : parPrefix.entrySet()) {
                parPrefixInt.put(entry.getKey().getNumber(), entry.getValue());
            }

            resourceJobManager = resourceJobManager.toBuilder()
                    .putAllJobManagerCommands(jmCommandsInt)
                    .putAllParallelismPrefix(parPrefixInt)
                    .build();
        }
        return resourceJobManager;
    }

    @Override
    public void deleteResourceJobManager(String resourceJobManagerId) throws AppCatalogException {
        (new ResourceJobManagerRepository()).delete(resourceJobManagerId);
    }

    @Override
    public void removeBatchQueue(String computeResourceId, String queueName) throws AppCatalogException {
        execute(entityManager -> {
            ComputeResourceEntity parent = entityManager.find(ComputeResourceEntity.class, computeResourceId);
            if (parent != null && parent.getBatchQueues() != null) {
                parent.getBatchQueues().removeIf(e -> queueName.equals(e.getQueueName()));
                entityManager.merge(parent);
            }
            return null;
        });
    }
}
