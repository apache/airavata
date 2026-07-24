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
import org.apache.airavata.common.AiravataUtils;
import org.apache.airavata.db.DBConstants;
import org.apache.airavata.db.QueryConstants;
import org.apache.airavata.model.appcatalog.computeresource.proto.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.proto.JobManagerCommand;
import org.apache.airavata.model.appcatalog.computeresource.proto.ResourceJobManager;
import org.apache.airavata.model.parallelism.proto.ApplicationParallelismType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ComputeResourceRepository
        extends AbstractRepository<ComputeResourceDescription, ComputeResourceEntity, String> {

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

    public String addComputeResource(ComputeResourceDescription description) throws Exception {
        if (description.getComputeResourceId().equals("")
                || description.getComputeResourceId().equals("DO_NOT_SET_AT_CLIENTS")) {
            description = description.toBuilder()
                    .setComputeResourceId(AiravataUtils.getId(description.getHostName()))
                    .build();
        }
        return saveComputeResourceDescriptorData(description);
    }

    protected String saveComputeResourceDescriptorData(ComputeResourceDescription description)
            throws Exception {
        ComputeResourceEntity computeResourceEntity = saveComputeResource(description);
        return computeResourceEntity.getComputeResourceId();
    }

    protected ComputeResourceEntity saveComputeResource(ComputeResourceDescription description)
            throws Exception {
        String computeResourceId = description.getComputeResourceId();
        ComputeResourceEntity computeResourceEntity = ComputeMapper.INSTANCE.computeResourceToEntity(description);
        if (computeResourceEntity.getBatchQueues() != null) {
            computeResourceEntity
                    .getBatchQueues()
                    .forEach(batchQueueEntity -> batchQueueEntity.setComputeResourceId(computeResourceId));
        }
        return execute(entityManager -> entityManager.merge(computeResourceEntity));
    }

    public void updateComputeResource(String computeResourceId, ComputeResourceDescription updatedComputeResource)
            throws Exception {
        saveComputeResourceDescriptorData(updatedComputeResource);
    }

    public ComputeResourceDescription getComputeResource(String resourceId) throws Exception {
        return get(resourceId);
    }

    public List<ComputeResourceDescription> getComputeResourceList(Map<String, String> filters)
            throws Exception {
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

    public List<ComputeResourceDescription> getAllComputeResourceList() throws Exception {
        return select(QueryConstants.FIND_ALL_COMPUTE_RESOURCES, 0);
    }

    public Map<String, String> getAllComputeResourceIdList() throws Exception {
        Map<String, String> computeResourceMap = new HashMap<String, String>();
        List<ComputeResourceDescription> computeResourceDescriptionList = select(
                QueryConstants.FIND_ALL_COMPUTE_RESOURCES, 0);
        if (computeResourceDescriptionList != null && !computeResourceDescriptionList.isEmpty()) {
            for (ComputeResourceDescription computeResourceDescription : computeResourceDescriptionList) {
                computeResourceMap.put(
                        computeResourceDescription.getComputeResourceId(), computeResourceDescription.getHostName());
            }
        }
        return computeResourceMap;
    }

    public Map<String, String> getAvailableComputeResourceIdList() throws Exception {
        Map<String, String> computeResourceMap = new HashMap<String, String>();
        List<ComputeResourceDescription> computeResourceDescriptionList = select(
                QueryConstants.FIND_ALL_COMPUTE_RESOURCES, 0);
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

    public boolean isComputeResourceExists(String resourceId) throws Exception {
        return isExists(resourceId);
    }

    public void removeComputeResource(String resourceId) throws Exception {
        delete(resourceId);
    }

    public String addResourceJobManager(ResourceJobManager resourceJobManager) throws Exception {
        ResourceJobManagerRepository resourceJobManagerRepository = new ResourceJobManagerRepository();
        resourceJobManager = resourceJobManager.toBuilder()
                .setResourceJobManagerId(AiravataUtils.getId("RJM"))
                .build();
        resourceJobManagerRepository.create(resourceJobManager);
        ResourceJobManagerEntity resourceJobManagerEntity = ComputeMapper.INSTANCE
                .resourceJobManagerToEntity(resourceJobManager);
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

    public void updateResourceJobManager(String resourceJobManagerId, ResourceJobManager updatedResourceJobManager)
            throws Exception {
        ResourceJobManagerRepository resourceJobManagerRepository = new ResourceJobManagerRepository();
        updatedResourceJobManager = updatedResourceJobManager.toBuilder()
                .setResourceJobManagerId(resourceJobManagerId)
                .build();
        ResourceJobManager resourceJobManager = resourceJobManagerRepository.create(updatedResourceJobManager);
        ResourceJobManagerEntity resourceJobManagerEntity = ComputeMapper.INSTANCE
                .resourceJobManagerToEntity(resourceJobManager);
        Map<Integer, String> jobManagerCommands = updatedResourceJobManager.getJobManagerCommandsMap();
        if (jobManagerCommands != null && jobManagerCommands.size() != 0) {
            resourceJobManagerRepository.createJobManagerCommand(jobManagerCommands, resourceJobManagerEntity);
        }

        Map<Integer, String> parallelismPrefix = updatedResourceJobManager.getParallelismPrefixMap();
        if (parallelismPrefix != null && parallelismPrefix.size() != 0) {
            resourceJobManagerRepository.createParallesimPrefix(parallelismPrefix, resourceJobManagerEntity);
        }
    }

    public ResourceJobManager getResourceJobManager(String resourceJobManagerId) throws Exception {
        ResourceJobManagerRepository resourceJobManagerRepository = new ResourceJobManagerRepository();
        ResourceJobManager resourceJobManager = resourceJobManagerRepository.get(resourceJobManagerId);
        if (resourceJobManager != null) {
            // Convert enum-keyed maps to integer-keyed maps for proto
            Map<JobManagerCommand, String> jmCommands = resourceJobManagerRepository
                    .getJobManagerCommand(resourceJobManagerId);
            Map<Integer, String> jmCommandsInt = new HashMap<>();
            for (Map.Entry<JobManagerCommand, String> entry : jmCommands.entrySet()) {
                jmCommandsInt.put(entry.getKey().getNumber(), entry.getValue());
            }

            Map<ApplicationParallelismType, String> parPrefix = resourceJobManagerRepository
                    .getParallelismPrefix(resourceJobManagerId);
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

    public void deleteResourceJobManager(String resourceJobManagerId) throws Exception {
        (new ResourceJobManagerRepository()).delete(resourceJobManagerId);
    }

    public void removeBatchQueue(String computeResourceId, String queueName) throws Exception {
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
