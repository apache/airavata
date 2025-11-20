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
package org.apache.airavata.restapi.service;

import org.apache.airavata.model.appcatalog.computeresource.BatchQueue;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.restapi.dto.BatchQueueDTO;
import org.apache.airavata.restapi.dto.ComputeResourceDTO;
import org.apache.airavata.restapi.dto.StorageResourceDTO;
import org.apache.airavata.service.AiravataService;
import org.apache.airavata.service.exception.AppCatalogException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ResourceService {

    private static final Logger logger = LoggerFactory.getLogger(ResourceService.class);
    private final AiravataService airavataService;

    public ResourceService() {
        this.airavataService = new AiravataService();
    }

    // Compute Resource methods
    public String registerComputeResource(ComputeResourceDTO dto) throws AppCatalogException {
        ComputeResourceDescription description = convertToComputeResourceDescription(dto);
        return airavataService.registerComputeResource(description);
    }

    public ComputeResourceDTO getComputeResource(String computeResourceId) throws AppCatalogException {
        ComputeResourceDescription description = airavataService.getComputeResource(computeResourceId);
        return convertToComputeResourceDTO(description);
    }

    public boolean updateComputeResource(String computeResourceId, ComputeResourceDTO dto) throws AppCatalogException {
        ComputeResourceDescription description = convertToComputeResourceDescription(dto);
        return airavataService.updateComputeResource(computeResourceId, description);
    }

    public boolean deleteComputeResource(String computeResourceId) throws AppCatalogException {
        return airavataService.deleteComputeResource(computeResourceId);
    }

    public List<Map<String, String>> getAllComputeResourceNames() throws AppCatalogException {
        Map<String, String> names = airavataService.getAllComputeResourceNames();
        return names.entrySet().stream()
                .map(entry -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("id", entry.getKey());
                    map.put("name", entry.getValue());
                    return map;
                })
                .collect(Collectors.toList());
    }

    // Storage Resource methods
    public String registerStorageResource(StorageResourceDTO dto) throws AppCatalogException {
        StorageResourceDescription description = convertToStorageResourceDescription(dto);
        return airavataService.registerStorageResource(description);
    }

    public StorageResourceDTO getStorageResource(String storageResourceId) throws AppCatalogException {
        StorageResourceDescription description = airavataService.getStorageResource(storageResourceId);
        return convertToStorageResourceDTO(description);
    }

    public boolean updateStorageResource(String storageResourceId, StorageResourceDTO dto) throws AppCatalogException {
        StorageResourceDescription description = convertToStorageResourceDescription(dto);
        return airavataService.updateStorageResource(storageResourceId, description);
    }

    public boolean deleteStorageResource(String storageResourceId) throws AppCatalogException {
        return airavataService.deleteStorageResource(storageResourceId);
    }

    public List<Map<String, String>> getAllStorageResourceNames() throws AppCatalogException {
        Map<String, String> names = airavataService.getAllStorageResourceNames();
        return names.entrySet().stream()
                .map(entry -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("id", entry.getKey());
                    map.put("name", entry.getValue());
                    return map;
                })
                .collect(Collectors.toList());
    }

    // Conversion methods
    private ComputeResourceDescription convertToComputeResourceDescription(ComputeResourceDTO dto) {
        ComputeResourceDescription description = new ComputeResourceDescription();
        if (dto.getComputeResourceId() != null && !dto.getComputeResourceId().equals("DO_NOT_SET_AT_CLIENTS")) {
            description.setComputeResourceId(dto.getComputeResourceId());
        } else {
            description.setComputeResourceId("DO_NOT_SET_AT_CLIENTS");
        }
        description.setHostName(dto.getHostName());
        if (dto.getHostAliases() != null) {
            description.setHostAliases(dto.getHostAliases());
        }
        if (dto.getIpAddresses() != null) {
            description.setIpAddresses(dto.getIpAddresses());
        }
        description.setResourceDescription(dto.getResourceDescription());
        description.setEnabled(dto.getEnabled());
        if (dto.getBatchQueues() != null) {
            List<BatchQueue> queues = new ArrayList<>();
            for (BatchQueueDTO queueDTO : dto.getBatchQueues()) {
                queues.add(convertToBatchQueue(queueDTO));
            }
            description.setBatchQueues(queues);
        }
        if (dto.getFileSystems() != null) {
            Map<org.apache.airavata.model.appcatalog.computeresource.FileSystems, String> fileSystems = new HashMap<>();
            for (Map.Entry<String, String> entry : dto.getFileSystems().entrySet()) {
                try {
                    org.apache.airavata.model.appcatalog.computeresource.FileSystems fs =
                            org.apache.airavata.model.appcatalog.computeresource.FileSystems.valueOf(entry.getKey());
                    fileSystems.put(fs, entry.getValue());
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid file system type: " + entry.getKey());
                }
            }
            description.setFileSystems(fileSystems);
        }
        description.setMaxMemoryPerNode(dto.getMaxMemoryPerNode());
        description.setGatewayUsageReporting(dto.getGatewayUsageReporting());
        description.setGatewayUsageModuleLoadCommand(dto.getGatewayUsageModuleLoadCommand());
        description.setGatewayUsageExecutable(dto.getGatewayUsageExecutable());
        description.setCpusPerNode(dto.getCpusPerNode());
        description.setDefaultNodeCount(dto.getDefaultNodeCount());
        description.setDefaultCPUCount(dto.getDefaultCPUCount());
        description.setDefaultWalltime(dto.getDefaultWalltime());
        return description;
    }

    private ComputeResourceDTO convertToComputeResourceDTO(ComputeResourceDescription description) {
        ComputeResourceDTO dto = new ComputeResourceDTO();
        dto.setComputeResourceId(description.getComputeResourceId());
        dto.setHostName(description.getHostName());
        dto.setHostAliases(description.getHostAliases());
        dto.setIpAddresses(description.getIpAddresses());
        dto.setResourceDescription(description.getResourceDescription());
        dto.setEnabled(description.isEnabled());
        if (description.getBatchQueues() != null) {
            List<BatchQueueDTO> queues = new ArrayList<>();
            for (BatchQueue queue : description.getBatchQueues()) {
                queues.add(convertToBatchQueueDTO(queue));
            }
            dto.setBatchQueues(queues);
        }
        if (description.getFileSystems() != null) {
            Map<String, String> fileSystems = new HashMap<>();
            for (Map.Entry<org.apache.airavata.model.appcatalog.computeresource.FileSystems, String> entry :
                    description.getFileSystems().entrySet()) {
                fileSystems.put(entry.getKey().name(), entry.getValue());
            }
            dto.setFileSystems(fileSystems);
        }
        dto.setMaxMemoryPerNode(description.getMaxMemoryPerNode());
        dto.setGatewayUsageReporting(description.isGatewayUsageReporting());
        dto.setGatewayUsageModuleLoadCommand(description.getGatewayUsageModuleLoadCommand());
        dto.setGatewayUsageExecutable(description.getGatewayUsageExecutable());
        dto.setCpusPerNode(description.getCpusPerNode());
        dto.setDefaultNodeCount(description.getDefaultNodeCount());
        dto.setDefaultCPUCount(description.getDefaultCPUCount());
        dto.setDefaultWalltime(description.getDefaultWalltime());
        return dto;
    }

    private BatchQueue convertToBatchQueue(BatchQueueDTO dto) {
        BatchQueue queue = new BatchQueue();
        queue.setQueueName(dto.getQueueName());
        queue.setQueueDescription(dto.getQueueDescription());
        queue.setMaxRunTime(dto.getMaxRunTime());
        queue.setMaxNodes(dto.getMaxNodes());
        queue.setMaxProcessors(dto.getMaxProcessors());
        queue.setMaxJobsInQueue(dto.getMaxJobsInQueue());
        queue.setMaxMemory(dto.getMaxMemory());
        queue.setCpuPerNode(dto.getCpuPerNode());
        queue.setDefaultNodeCount(dto.getDefaultNodeCount());
        queue.setDefaultCPUCount(dto.getDefaultCPUCount());
        queue.setDefaultWalltime(dto.getDefaultWalltime());
        queue.setQueueSpecificMacros(dto.getQueueSpecificMacros());
        queue.setIsDefaultQueue(dto.getIsDefaultQueue());
        return queue;
    }

    private BatchQueueDTO convertToBatchQueueDTO(BatchQueue queue) {
        BatchQueueDTO dto = new BatchQueueDTO();
        dto.setQueueName(queue.getQueueName());
        dto.setQueueDescription(queue.getQueueDescription());
        dto.setMaxRunTime(queue.getMaxRunTime());
        dto.setMaxNodes(queue.getMaxNodes());
        dto.setMaxProcessors(queue.getMaxProcessors());
        dto.setMaxJobsInQueue(queue.getMaxJobsInQueue());
        dto.setMaxMemory(queue.getMaxMemory());
        dto.setCpuPerNode(queue.getCpuPerNode());
        dto.setDefaultNodeCount(queue.getDefaultNodeCount());
        dto.setDefaultCPUCount(queue.getDefaultCPUCount());
        dto.setDefaultWalltime(queue.getDefaultWalltime());
        dto.setQueueSpecificMacros(queue.getQueueSpecificMacros());
        dto.setIsDefaultQueue(queue.isIsDefaultQueue());
        return dto;
    }

    private StorageResourceDescription convertToStorageResourceDescription(StorageResourceDTO dto) {
        StorageResourceDescription description = new StorageResourceDescription();
        if (dto.getStorageResourceId() != null && !dto.getStorageResourceId().equals("DO_NOT_SET_AT_CLIENTS")) {
            description.setStorageResourceId(dto.getStorageResourceId());
        } else {
            description.setStorageResourceId("DO_NOT_SET_AT_CLIENTS");
        }
        description.setHostName(dto.getHostName());
        description.setStorageResourceDescription(dto.getStorageResourceDescription());
        description.setEnabled(dto.getEnabled());
        return description;
    }

    private StorageResourceDTO convertToStorageResourceDTO(StorageResourceDescription description) {
        StorageResourceDTO dto = new StorageResourceDTO();
        dto.setStorageResourceId(description.getStorageResourceId());
        dto.setHostName(description.getHostName());
        dto.setStorageResourceDescription(description.getStorageResourceDescription());
        dto.setEnabled(description.isEnabled());
        if (description.getCreationTime() != null) {
            dto.setCreationTime(description.getCreationTime());
        }
        if (description.getUpdateTime() != null) {
            dto.setUpdateTime(description.getUpdateTime());
        }
        return dto;
    }
}



