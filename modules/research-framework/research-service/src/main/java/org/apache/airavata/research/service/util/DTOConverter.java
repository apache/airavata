/**
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
package org.apache.airavata.research.service.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.airavata.model.appcatalog.computeresource.BatchQueue;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionInterface;
import org.apache.airavata.model.data.movement.DataMovementInterface;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.data.movement.DataMovementProtocol;
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.research.service.dto.ComputeResourceDTO;
import org.apache.airavata.research.service.dto.ComputeResourceQueueDTO;
import org.apache.airavata.research.service.dto.StorageResourceDTO;
import org.apache.airavata.registry.core.entities.appcatalog.ComputeResourceEntity;
import org.apache.airavata.registry.core.entities.appcatalog.StorageResourceEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


/**
 * Utility class for converting between entities and DTOs
 * Handles JSON serialization of UI-specific fields into description fields
 */
@Component
public class DTOConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DTOConverter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    // JSON field names for UI-specific data
    private static final String UI_FIELDS_KEY = "uiFields";
    private static final String COMPUTE_TYPE_KEY = "computeType";
    private static final String OPERATING_SYSTEM_KEY = "operatingSystem";
    private static final String QUEUE_SYSTEM_KEY = "queueSystem";
    private static final String ADDITIONAL_INFO_KEY = "additionalInfo";
    private static final String RESOURCE_MANAGER_KEY = "resourceManager";
    private static final String SSH_CONFIG_KEY = "sshConfig";
    private static final String SSH_USERNAME_KEY = "sshUsername";
    private static final String SSH_PORT_KEY = "sshPort";
    private static final String AUTH_METHOD_KEY = "authenticationMethod";
    private static final String SSH_KEY_KEY = "sshKey";
    private static final String WORKING_DIR_KEY = "workingDirectory";
    private static final String SCHEDULER_TYPE_KEY = "schedulerType";
    private static final String DATA_MOVEMENT_PROTOCOL_KEY = "dataMovementProtocol";
    
    // Additional compute fields that need to be preserved
    private static final String NAME_KEY = "name";
    private static final String HOST_ALIASES_KEY = "hostAliases";
    private static final String IP_ADDRESSES_KEY = "ipAddresses";
    private static final String QUEUES_KEY = "queues";

    // Storage-specific field names
    private static final String STORAGE_TYPE_KEY = "storageType";
    private static final String CAPACITY_TB_KEY = "capacityTB";
    private static final String ACCESS_PROTOCOL_KEY = "accessProtocol";
    private static final String ENDPOINT_KEY = "endpoint";
    private static final String SUPPORTS_ENCRYPTION_KEY = "supportsEncryption";
    private static final String SUPPORTS_VERSIONING_KEY = "supportsVersioning";
    private static final String S3_CONFIG_KEY = "s3Config";
    private static final String BUCKET_NAME_KEY = "bucketName";
    private static final String ACCESS_KEY_KEY = "accessKey";
    private static final String SECRET_KEY_KEY = "secretKey";
    private static final String SCP_CONFIG_KEY = "scpConfig";
    private static final String PORT_KEY = "port";
    private static final String USERNAME_KEY = "username";
    private static final String REMOTE_PATH_KEY = "remotePath";

    /**
     * Convert ComputeResourceDescription to ComputeResourceDTO
     */
    public ComputeResourceDTO thriftToDTO(ComputeResourceDescription thriftModel) {
        if (thriftModel == null) {
            return null;
        }

        ComputeResourceDTO dto = new ComputeResourceDTO();
        
        // Direct mappings
        dto.setComputeResourceId(thriftModel.getComputeResourceId());
        dto.setHostName(thriftModel.getHostName());
        dto.setHostAliases(thriftModel.getHostAliases());
        dto.setIpAddresses(thriftModel.getIpAddresses());
        dto.setEnabled(thriftModel.isEnabled());
        
        // Map memory (convert from MB to GB if needed)
        if (thriftModel.isSetMaxMemoryPerNode()) {
            dto.setMemoryGB(thriftModel.getMaxMemoryPerNode() / 1024); // Assuming thrift is in MB
        }
        
        // Map CPU cores
        if (thriftModel.isSetCpusPerNode()) {
            dto.setCpuCores(thriftModel.getCpusPerNode());
        }

        // Extract UI-specific fields from resourceDescription JSON
        parseResourceDescriptionForComputeResource(thriftModel.getResourceDescription(), dto);

        // Convert batch queues to queue DTOs
        if (thriftModel.getBatchQueues() != null) {
            dto.setQueues(thriftModel.getBatchQueues().stream()
                .map(this::batchQueueToDTO)
                .collect(Collectors.toList()));
        }

        // Extract data movement protocol from DataMovementInterface
        if (thriftModel.getDataMovementInterfaces() != null && !thriftModel.getDataMovementInterfaces().isEmpty()) {
            // Get the first (highest priority) data movement interface
            DataMovementInterface dmInterface = thriftModel.getDataMovementInterfaces().get(0);
            if (dmInterface != null && dmInterface.getDataMovementProtocol() != null) {
                dto.setDataMovementProtocol(dmInterface.getDataMovementProtocol().toString());
            }
        }

        // Extract resource job manager type from JobSubmissionInterface
        if (thriftModel.getJobSubmissionInterfaces() != null && !thriftModel.getJobSubmissionInterfaces().isEmpty()) {
            // Get the first (highest priority) job submission interface
            JobSubmissionInterface jsInterface = thriftModel.getJobSubmissionInterfaces().get(0);
            if (jsInterface != null && jsInterface.getJobSubmissionProtocol() != null) {
                dto.setResourceJobManagerType(jsInterface.getJobSubmissionProtocol().toString());
            }
        }

        return dto;
    }

    /**
     * Convert ComputeResourceDTO to ComputeResourceDescription
     */
    public ComputeResourceDescription dtoToThrift(ComputeResourceDTO dto) {
        if (dto == null) {
            return null;
        }

        ComputeResourceDescription thriftModel = new ComputeResourceDescription();
        
        // Direct mappings
        thriftModel.setComputeResourceId(dto.getComputeResourceId());
        thriftModel.setHostName(dto.getHostName());
        thriftModel.setHostAliases(dto.getHostAliases());
        thriftModel.setIpAddresses(dto.getIpAddresses());
        thriftModel.setEnabled(dto.isEnabled());
        
        // Map memory (convert from GB to MB)
        if (dto.getMemoryGB() != null) {
            thriftModel.setMaxMemoryPerNode(dto.getMemoryGB() * 1024);
        }
        
        // Map CPU cores
        if (dto.getCpuCores() != null) {
            thriftModel.setCpusPerNode(dto.getCpuCores());
        }

        // Store UI-specific fields as JSON in resourceDescription
        thriftModel.setResourceDescription(buildResourceDescriptionForComputeResource(dto));

        // Convert queue DTOs to batch queues
        if (dto.getQueues() != null) {
            thriftModel.setBatchQueues(dto.getQueues().stream()
                .map(this::dtoToBatchQueue)
                .collect(Collectors.toList()));
        }

        // Create DataMovementInterface if protocol is specified
        if (dto.getDataMovementProtocol() != null && !dto.getDataMovementProtocol().trim().isEmpty()) {
            List<DataMovementInterface> dataMovementInterfaces = new ArrayList<>();
            DataMovementInterface dmInterface = new DataMovementInterface();
            dmInterface.setDataMovementInterfaceId(generateInterfaceId("dm"));
            dmInterface.setPriorityOrder(1); // Highest priority
            try {
                dmInterface.setDataMovementProtocol(DataMovementProtocol.valueOf(dto.getDataMovementProtocol()));
                dataMovementInterfaces.add(dmInterface);
                thriftModel.setDataMovementInterfaces(dataMovementInterfaces);
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Invalid data movement protocol: " + dto.getDataMovementProtocol(), e);
            }
        }

        // Create JobSubmissionInterface if protocol is specified  
        if (dto.getResourceJobManagerType() != null && !dto.getResourceJobManagerType().trim().isEmpty()) {
            List<JobSubmissionInterface> jobSubmissionInterfaces = new ArrayList<>();
            JobSubmissionInterface jsInterface = new JobSubmissionInterface();
            jsInterface.setJobSubmissionInterfaceId(generateInterfaceId("js"));
            jsInterface.setPriorityOrder(1); // Highest priority
            try {
                jsInterface.setJobSubmissionProtocol(JobSubmissionProtocol.valueOf(dto.getResourceJobManagerType()));
                jobSubmissionInterfaces.add(jsInterface);
                thriftModel.setJobSubmissionInterfaces(jobSubmissionInterfaces);
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Invalid job submission protocol: " + dto.getResourceJobManagerType(), e);
            }
        }

        return thriftModel;
    }

    /**
     * Convert StorageResourceDescription to StorageResourceDTO
     */
    public StorageResourceDTO thriftToDTO(StorageResourceDescription thriftModel) {
        if (thriftModel == null) {
            return null;
        }

        StorageResourceDTO dto = new StorageResourceDTO();
        
        // Direct mappings
        dto.setStorageResourceId(thriftModel.getStorageResourceId());
        dto.setHostName(thriftModel.getHostName());
        dto.setEnabled(thriftModel.isEnabled());
        dto.setCreationTime(thriftModel.getCreationTime());
        dto.setUpdateTime(thriftModel.getUpdateTime());

        // Extract UI-specific fields from storageResourceDescription JSON
        parseResourceDescriptionForStorageResource(thriftModel.getStorageResourceDescription(), dto);

        return dto;
    }

    /**
     * Convert StorageResourceDTO to StorageResourceDescription
     */
    public StorageResourceDescription dtoToThrift(StorageResourceDTO dto) {
        if (dto == null) {
            return null;
        }

        StorageResourceDescription thriftModel = new StorageResourceDescription();
        
        // Direct mappings
        thriftModel.setStorageResourceId(dto.getStorageResourceId());
        thriftModel.setHostName(dto.getHostName());
        thriftModel.setEnabled(dto.isEnabled());

        // Store UI-specific fields as JSON in storageResourceDescription
        thriftModel.setStorageResourceDescription(buildResourceDescriptionForStorageResource(dto));

        return thriftModel;
    }

    /**
     * Convert BatchQueue to ComputeResourceQueueDTO
     */
    public ComputeResourceQueueDTO batchQueueToDTO(BatchQueue batchQueue) {
        if (batchQueue == null) {
            return null;
        }

        ComputeResourceQueueDTO dto = new ComputeResourceQueueDTO();
        dto.setQueueName(batchQueue.getQueueName());
        dto.setQueueDescription(batchQueue.getQueueDescription());
        dto.setMaxRunTime(batchQueue.getMaxRunTime());
        dto.setMaxNodes(batchQueue.getMaxNodes());
        dto.setMaxProcessors(batchQueue.getMaxProcessors());
        dto.setMaxJobsInQueue(batchQueue.getMaxJobsInQueue());
        dto.setMaxMemory(batchQueue.getMaxMemory());
        dto.setCpusPerNode(batchQueue.getCpuPerNode());
        dto.setDefaultNodeCount(batchQueue.getDefaultNodeCount());
        dto.setDefaultCpuCount(batchQueue.getDefaultCPUCount());
        dto.setDefaultWallTime(batchQueue.getDefaultWalltime());
        dto.setQueueSpecificMacros(batchQueue.getQueueSpecificMacros());
        dto.setIsDefaultQueue(batchQueue.isIsDefaultQueue());
        
        return dto;
    }

    /**
     * Convert ComputeResourceQueueDTO to BatchQueue
     */
    public BatchQueue dtoToBatchQueue(ComputeResourceQueueDTO dto) {
        if (dto == null) {
            return null;
        }

        BatchQueue batchQueue = new BatchQueue();
        batchQueue.setQueueName(dto.getQueueName());
        batchQueue.setQueueDescription(dto.getQueueDescription());
        batchQueue.setMaxRunTime(dto.getMaxRunTime() != null ? dto.getMaxRunTime() : 0);
        batchQueue.setMaxNodes(dto.getMaxNodes() != null ? dto.getMaxNodes() : 0);
        batchQueue.setMaxProcessors(dto.getMaxProcessors() != null ? dto.getMaxProcessors() : 0);
        batchQueue.setMaxJobsInQueue(dto.getMaxJobsInQueue() != null ? dto.getMaxJobsInQueue() : 0);
        batchQueue.setMaxMemory(dto.getMaxMemory() != null ? dto.getMaxMemory() : 0);
        batchQueue.setCpuPerNode(dto.getCpusPerNode() != null ? dto.getCpusPerNode() : 0);
        batchQueue.setDefaultNodeCount(dto.getDefaultNodeCount() != null ? dto.getDefaultNodeCount() : 0);
        batchQueue.setDefaultCPUCount(dto.getDefaultCpuCount() != null ? dto.getDefaultCpuCount() : 0);
        batchQueue.setDefaultWalltime(dto.getDefaultWallTime() != null ? dto.getDefaultWallTime() : 0);
        batchQueue.setQueueSpecificMacros(dto.getQueueSpecificMacros());
        batchQueue.setIsDefaultQueue(dto.getIsDefaultQueue() != null ? dto.getIsDefaultQueue() : false);
        
        return batchQueue;
    }

    /**
     * Parse JSON from resourceDescription and populate ComputeResourceDTO UI fields
     */
    private void parseResourceDescriptionForComputeResource(String resourceDescription, ComputeResourceDTO dto) {
        if (resourceDescription == null || resourceDescription.trim().isEmpty()) {
            return;
        }

        try {
            JsonNode rootNode = objectMapper.readTree(resourceDescription);
            JsonNode uiFieldsNode = rootNode.get(UI_FIELDS_KEY);
            
            if (uiFieldsNode != null) {
                // Extract UI-specific fields
                dto.setComputeType(getStringValue(uiFieldsNode, COMPUTE_TYPE_KEY));
                dto.setOperatingSystem(getStringValue(uiFieldsNode, OPERATING_SYSTEM_KEY));
                dto.setQueueSystem(getStringValue(uiFieldsNode, QUEUE_SYSTEM_KEY));
                dto.setAdditionalInfo(getStringValue(uiFieldsNode, ADDITIONAL_INFO_KEY));
                dto.setResourceManager(getStringValue(uiFieldsNode, RESOURCE_MANAGER_KEY));
                dto.setResourceJobManagerType(getStringValue(uiFieldsNode, SCHEDULER_TYPE_KEY)); // Map old schedulerType to new field
                dto.setDataMovementProtocol(getStringValue(uiFieldsNode, DATA_MOVEMENT_PROTOCOL_KEY));

                // Extract SSH configuration
                JsonNode sshConfigNode = uiFieldsNode.get(SSH_CONFIG_KEY);
                if (sshConfigNode != null) {
                    dto.setAlternativeSSHHostName(getStringValue(sshConfigNode, SSH_USERNAME_KEY)); // Repurpose for alternative hostname
                    dto.setSshPort(getIntegerValue(sshConfigNode, SSH_PORT_KEY));
                    dto.setSecurityProtocol(getStringValue(sshConfigNode, AUTH_METHOD_KEY)); // Map to securityProtocol
                }
            }
            
            // Set basic description (without UI fields)
            JsonNode basicDescNode = rootNode.get("description");
            if (basicDescNode != null) {
                dto.setResourceDescription(basicDescNode.asText());
            }
            
        } catch (JsonProcessingException e) {
            LOGGER.warn("Failed to parse resourceDescription JSON, treating as plain text: {}", e.getMessage());
            dto.setResourceDescription(resourceDescription);
        }
    }

    /**
     * Build JSON resourceDescription from ComputeResourceDTO
     */
    private String buildResourceDescriptionForComputeResource(ComputeResourceDTO dto) {
        Map<String, Object> rootMap = new HashMap<>();
        
        // Basic description
        if (dto.getResourceDescription() != null) {
            rootMap.put("description", dto.getResourceDescription());
        }

        // UI-specific fields
        Map<String, Object> uiFields = new HashMap<>();
        uiFields.put(COMPUTE_TYPE_KEY, dto.getComputeType());
        uiFields.put(OPERATING_SYSTEM_KEY, dto.getOperatingSystem());
        uiFields.put(QUEUE_SYSTEM_KEY, dto.getQueueSystem());
        uiFields.put(ADDITIONAL_INFO_KEY, dto.getAdditionalInfo());
        uiFields.put(RESOURCE_MANAGER_KEY, dto.getResourceManager());
        uiFields.put(SCHEDULER_TYPE_KEY, dto.getResourceJobManagerType()); // Use new field name
        uiFields.put(DATA_MOVEMENT_PROTOCOL_KEY, dto.getDataMovementProtocol());

        // SSH configuration
        Map<String, Object> sshConfig = new HashMap<>();
        sshConfig.put(SSH_USERNAME_KEY, dto.getAlternativeSSHHostName()); // Repurposed field
        sshConfig.put(SSH_PORT_KEY, dto.getSshPort());
        sshConfig.put(AUTH_METHOD_KEY, dto.getSecurityProtocol()); // Use new field name
        uiFields.put(SSH_CONFIG_KEY, sshConfig);

        rootMap.put(UI_FIELDS_KEY, uiFields);

        try {
            return objectMapper.writeValueAsString(rootMap);
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to serialize UI fields to JSON", e);
            return dto.getResourceDescription() != null ? dto.getResourceDescription() : "";
        }
    }

    /**
     * Parse JSON from storageResourceDescription and populate StorageResourceDTO UI fields
     */
    private void parseResourceDescriptionForStorageResource(String storageResourceDescription, StorageResourceDTO dto) {
        if (storageResourceDescription == null || storageResourceDescription.trim().isEmpty()) {
            return;
        }

        try {
            JsonNode rootNode = objectMapper.readTree(storageResourceDescription);
            JsonNode uiFieldsNode = rootNode.get(UI_FIELDS_KEY);
            
            if (uiFieldsNode != null) {
                // Extract UI-specific fields
                dto.setStorageType(getStringValue(uiFieldsNode, STORAGE_TYPE_KEY));
                dto.setCapacityTB(getLongValue(uiFieldsNode, CAPACITY_TB_KEY));
                dto.setAccessProtocol(getStringValue(uiFieldsNode, ACCESS_PROTOCOL_KEY));
                dto.setEndpoint(getStringValue(uiFieldsNode, ENDPOINT_KEY));
                dto.setSupportsEncryption(getBooleanValue(uiFieldsNode, SUPPORTS_ENCRYPTION_KEY));
                dto.setSupportsVersioning(getBooleanValue(uiFieldsNode, SUPPORTS_VERSIONING_KEY));
                dto.setAdditionalInfo(getStringValue(uiFieldsNode, ADDITIONAL_INFO_KEY));
                dto.setResourceManager(getStringValue(uiFieldsNode, RESOURCE_MANAGER_KEY));

                // Extract S3 configuration
                JsonNode s3ConfigNode = uiFieldsNode.get(S3_CONFIG_KEY);
                if (s3ConfigNode != null) {
                    dto.setBucketName(getStringValue(s3ConfigNode, BUCKET_NAME_KEY));
                    dto.setAccessKey(getStringValue(s3ConfigNode, ACCESS_KEY_KEY));
                    dto.setSecretKey(getStringValue(s3ConfigNode, SECRET_KEY_KEY));
                }

                // Extract SCP configuration
                JsonNode scpConfigNode = uiFieldsNode.get(SCP_CONFIG_KEY);
                if (scpConfigNode != null) {
                    dto.setPort(getIntegerValue(scpConfigNode, PORT_KEY));
                    dto.setUsername(getStringValue(scpConfigNode, USERNAME_KEY));
                    dto.setAuthenticationMethod(getStringValue(scpConfigNode, AUTH_METHOD_KEY));
                    dto.setSshKey(getStringValue(scpConfigNode, SSH_KEY_KEY));
                    dto.setRemotePath(getStringValue(scpConfigNode, REMOTE_PATH_KEY));
                }
            }
            
            // Set basic description (without UI fields)
            JsonNode basicDescNode = rootNode.get("description");
            if (basicDescNode != null) {
                dto.setStorageResourceDescription(basicDescNode.asText());
            }
            
        } catch (JsonProcessingException e) {
            LOGGER.warn("Failed to parse storageResourceDescription JSON, treating as plain text: {}", e.getMessage());
            dto.setStorageResourceDescription(storageResourceDescription);
        }
    }

    /**
     * Build JSON storageResourceDescription from StorageResourceDTO
     */
    private String buildResourceDescriptionForStorageResource(StorageResourceDTO dto) {
        Map<String, Object> rootMap = new HashMap<>();
        
        // Basic description
        if (dto.getStorageResourceDescription() != null) {
            rootMap.put("description", dto.getStorageResourceDescription());
        }

        // UI-specific fields
        Map<String, Object> uiFields = new HashMap<>();
        uiFields.put(STORAGE_TYPE_KEY, dto.getStorageType());
        uiFields.put(CAPACITY_TB_KEY, dto.getCapacityTB());
        uiFields.put(ACCESS_PROTOCOL_KEY, dto.getAccessProtocol());
        uiFields.put(ENDPOINT_KEY, dto.getEndpoint());
        uiFields.put(SUPPORTS_ENCRYPTION_KEY, dto.getSupportsEncryption());
        uiFields.put(SUPPORTS_VERSIONING_KEY, dto.getSupportsVersioning());
        uiFields.put(ADDITIONAL_INFO_KEY, dto.getAdditionalInfo());
        uiFields.put(RESOURCE_MANAGER_KEY, dto.getResourceManager());

        // S3 configuration
        if ("S3".equalsIgnoreCase(dto.getStorageType())) {
            Map<String, Object> s3Config = new HashMap<>();
            s3Config.put(BUCKET_NAME_KEY, dto.getBucketName());
            s3Config.put(ACCESS_KEY_KEY, dto.getAccessKey());
            s3Config.put(SECRET_KEY_KEY, dto.getSecretKey());
            uiFields.put(S3_CONFIG_KEY, s3Config);
        }

        // SCP configuration
        if ("SCP".equalsIgnoreCase(dto.getStorageType())) {
            Map<String, Object> scpConfig = new HashMap<>();
            scpConfig.put(PORT_KEY, dto.getPort());
            scpConfig.put(USERNAME_KEY, dto.getUsername());
            scpConfig.put(AUTH_METHOD_KEY, dto.getAuthenticationMethod());
            scpConfig.put(SSH_KEY_KEY, dto.getSshKey());
            scpConfig.put(REMOTE_PATH_KEY, dto.getRemotePath());
            uiFields.put(SCP_CONFIG_KEY, scpConfig);
        }

        rootMap.put(UI_FIELDS_KEY, uiFields);

        try {
            return objectMapper.writeValueAsString(rootMap);
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to serialize UI fields to JSON", e);
            return dto.getStorageResourceDescription() != null ? dto.getStorageResourceDescription() : "";
        }
    }

    // Helper methods for extracting values from JSON nodes
    private String getStringValue(JsonNode node, String key) {
        JsonNode valueNode = node.get(key);
        return valueNode != null && !valueNode.isNull() ? valueNode.asText() : null;
    }

    private Integer getIntegerValue(JsonNode node, String key) {
        JsonNode valueNode = node.get(key);
        return valueNode != null && !valueNode.isNull() ? valueNode.asInt() : null;
    }

    private Long getLongValue(JsonNode node, String key) {
        JsonNode valueNode = node.get(key);
        return valueNode != null && !valueNode.isNull() ? valueNode.asLong() : null;
    }

    private Boolean getBooleanValue(JsonNode node, String key) {
        JsonNode valueNode = node.get(key);
        return valueNode != null && !valueNode.isNull() ? valueNode.asBoolean() : null;
    }

    // ===============================
    // JPA Entity Conversion Methods
    // ===============================

    /**
     * Convert StorageResourceEntity (JPA) to StorageResourceDTO
     */
    public StorageResourceDTO storageEntityToDTO(StorageResourceEntity entity) {
        if (entity == null) {
            return null;
        }

        StorageResourceDTO dto = new StorageResourceDTO();
        
        // Core fields
        dto.setStorageResourceId(entity.getStorageResourceId());
        dto.setHostName(entity.getHostName());
        dto.setStorageResourceDescription(entity.getStorageResourceDescription());
        // Handle enabled field - boolean type from database
        dto.setEnabled(entity.isEnabled());
        
        // Extract name from UI fields or generate fallback
        String extractedName = extractNameFromStorageDescription(entity.getStorageResourceDescription());
        if (extractedName != null && !extractedName.trim().isEmpty()) {
            dto.setName(extractedName);
        } else {
            // Generate name from hostname and description
            dto.setName(generateStorageResourceName(entity.getHostName(), entity.getStorageResourceDescription()));
        }
        
        // Timestamps
        if (entity.getCreationTime() != null) {
            dto.setCreationTime(entity.getCreationTime().getTime());
        }
        if (entity.getUpdateTime() != null) {
            dto.setUpdateTime(entity.getUpdateTime().getTime());
        }

        // Extract UI-specific fields from JSON stored in description
        extractStorageUIFieldsFromDescription(entity.getStorageResourceDescription(), dto);

        return dto;
    }

    /**
     * Convert StorageResourceDTO to StorageResourceEntity (JPA)
     */
    public StorageResourceEntity storageResourceDTOToEntity(StorageResourceDTO dto) {
        if (dto == null) {
            return null;
        }

        StorageResourceEntity entity = new StorageResourceEntity();
        
        // Core fields
        entity.setStorageResourceId(dto.getStorageResourceId());
        entity.setHostName(dto.getHostName());
        entity.setEnabled(dto.isEnabled());
        
        // Encode UI-specific fields into JSON within description
        entity.setStorageResourceDescription(encodeStorageUIFieldsIntoDescription(dto));

        return entity;
    }

    /**
     * Convert ComputeResourceEntity (JPA) to ComputeResourceDTO
     */
    public ComputeResourceDTO computeEntityToDTO(ComputeResourceEntity entity) {
        if (entity == null) {
            return null;
        }

        ComputeResourceDTO dto = new ComputeResourceDTO();
        
        // Core fields
        dto.setComputeResourceId(entity.getComputeResourceId());
        dto.setHostName(entity.getHostName());
        dto.setResourceDescription(entity.getResourceDescription());
        // Handle enabled field safely - Short type from database
        Short enabledValue = entity.getEnabled();
        dto.setEnabled(enabledValue != null && enabledValue.shortValue() == 1);
        dto.setCpuCores(entity.getCpusPerNode());
        dto.setMemoryGB(entity.getMaxMemoryPerNode());
        
        // Extract name from UI fields or generate fallback
        String extractedName = extractNameFromDescription(entity.getResourceDescription());
        if (extractedName != null && !extractedName.trim().isEmpty()) {
            dto.setName(extractedName);
        } else {
            // Generate name from hostname and description
            dto.setName(generateComputeResourceName(entity.getHostName(), entity.getResourceDescription()));
        }
        
        // Timestamps
        if (entity.getCreationTime() != null) {
            dto.setCreationTime(entity.getCreationTime().getTime());
        }
        if (entity.getUpdateTime() != null) {
            dto.setUpdateTime(entity.getUpdateTime().getTime());
        }

        // Extract UI-specific fields from JSON stored in description
        extractComputeUIFieldsFromDescription(entity.getResourceDescription(), dto);
        
        // Initialize empty arrays for fields not stored in database
        if (dto.getHostAliases() == null) {
            dto.setHostAliases(new ArrayList<>());
        }
        if (dto.getIpAddresses() == null) {
            dto.setIpAddresses(new ArrayList<>());
        }
        if (dto.getQueues() == null) {
            dto.setQueues(new ArrayList<>());
        }

        return dto;
    }

    /**
     * Convert ComputeResourceDTO to ComputeResourceEntity (JPA)
     */
    public ComputeResourceEntity computeResourceDTOToEntity(ComputeResourceDTO dto) {
        if (dto == null) {
            return null;
        }

        ComputeResourceEntity entity = new ComputeResourceEntity();
        
        // Core fields
        entity.setComputeResourceId(dto.getComputeResourceId());
        entity.setHostName(dto.getHostName());
        entity.setEnabled(dto.isEnabled() ? Short.valueOf((short) 1) : Short.valueOf((short) 0));
        entity.setCpusPerNode(dto.getCpuCores());
        entity.setMaxMemoryPerNode(dto.getMemoryGB());
        
        // Encode UI-specific fields into JSON within description
        entity.setResourceDescription(encodeComputeUIFieldsIntoDescription(dto));

        return entity;
    }

    // Helper method to extract storage UI fields from JSON in description
    private void extractStorageUIFieldsFromDescription(String description, StorageResourceDTO dto) {
        if (description == null || !description.contains("UI_FIELDS:")) {
            return;
        }

        try {
            // Extract JSON part after UI_FIELDS:
            String jsonPart = description.substring(description.indexOf("UI_FIELDS:") + 10).trim();
            JsonNode rootNode = objectMapper.readTree(jsonPart);

            // Extract UI-specific fields
            dto.setStorageType(getStringValue(rootNode, STORAGE_TYPE_KEY));
            dto.setCapacityTB(getLongValue(rootNode, CAPACITY_TB_KEY));
            dto.setAccessProtocol(getStringValue(rootNode, ACCESS_PROTOCOL_KEY));
            dto.setSupportsEncryption(getBooleanValue(rootNode, SUPPORTS_ENCRYPTION_KEY));
            dto.setSupportsVersioning(getBooleanValue(rootNode, SUPPORTS_VERSIONING_KEY));
            
            // Extract preserved fields
            dto.setName(getStringValue(rootNode, NAME_KEY));

            // S3-specific fields
            dto.setBucketName(getStringValue(rootNode, BUCKET_NAME_KEY));
            dto.setAccessKey(getStringValue(rootNode, ACCESS_KEY_KEY));
            dto.setSecretKey(getStringValue(rootNode, SECRET_KEY_KEY));

            // SCP-specific fields  
            dto.setPort(getIntegerValue(rootNode, PORT_KEY));
            dto.setUsername(getStringValue(rootNode, USERNAME_KEY));
            dto.setAuthenticationMethod(getStringValue(rootNode, AUTH_METHOD_KEY));
            dto.setRemotePath(getStringValue(rootNode, REMOTE_PATH_KEY));

            // Clean description (remove UI_FIELDS part)
            String cleanDescription = description.substring(0, description.indexOf("UI_FIELDS:")).trim();
            if (cleanDescription.endsWith("\n\n")) {
                cleanDescription = cleanDescription.substring(0, cleanDescription.length() - 2);
            }
            dto.setStorageResourceDescription(cleanDescription);
            
        } catch (Exception e) {
            LOGGER.warn("Failed to extract storage UI fields from description", e);
        }
    }
    
    // Helper method to extract compute UI fields from JSON in description
    private void extractComputeUIFieldsFromDescription(String description, ComputeResourceDTO dto) {
        if (description == null || !description.contains("UI_FIELDS:")) {
            return;
        }

        try {
            // Extract JSON part after UI_FIELDS:
            String jsonPart = description.substring(description.indexOf("UI_FIELDS:") + 10).trim();
            JsonNode rootNode = objectMapper.readTree(jsonPart);

            // Extract UI-specific fields
            dto.setComputeType(getStringValue(rootNode, COMPUTE_TYPE_KEY));
            dto.setOperatingSystem(getStringValue(rootNode, OPERATING_SYSTEM_KEY));
            dto.setResourceJobManagerType(getStringValue(rootNode, SCHEDULER_TYPE_KEY)); // Map old schedulerType to new field
            dto.setDataMovementProtocol(getStringValue(rootNode, DATA_MOVEMENT_PROTOCOL_KEY));
            dto.setQueueSystem(getStringValue(rootNode, QUEUE_SYSTEM_KEY));
            dto.setResourceManager(getStringValue(rootNode, RESOURCE_MANAGER_KEY));
            
            // Extract SSH fields (updated field names)
            dto.setSshPort(getIntegerValue(rootNode, SSH_PORT_KEY));
            dto.setSecurityProtocol(getStringValue(rootNode, AUTH_METHOD_KEY)); // Map authenticationMethod to securityProtocol
            dto.setAlternativeSSHHostName(getStringValue(rootNode, SSH_USERNAME_KEY)); // Repurpose for alternative hostname
            
            // Extract preserved fields
            dto.setName(getStringValue(rootNode, NAME_KEY));
            
            // Extract arrays
            JsonNode hostAliasesNode = rootNode.get(HOST_ALIASES_KEY);
            if (hostAliasesNode != null && hostAliasesNode.isArray()) {
                List<String> hostAliases = new ArrayList<>();
                hostAliasesNode.forEach(node -> hostAliases.add(node.asText()));
                dto.setHostAliases(hostAliases);
            }
            
            JsonNode ipAddressesNode = rootNode.get(IP_ADDRESSES_KEY);
            if (ipAddressesNode != null && ipAddressesNode.isArray()) {
                List<String> ipAddresses = new ArrayList<>();
                ipAddressesNode.forEach(node -> ipAddresses.add(node.asText()));
                dto.setIpAddresses(ipAddresses);
            }
            
            JsonNode queuesNode = rootNode.get(QUEUES_KEY);
            if (queuesNode != null && queuesNode.isArray()) {
                List<ComputeResourceQueueDTO> queues = new ArrayList<>();
                queuesNode.forEach(queueNode -> {
                    ComputeResourceQueueDTO queue = new ComputeResourceQueueDTO();
                    queue.setQueueName(getStringValue(queueNode, "queueName"));
                    queue.setMaxNodes(getIntegerValue(queueNode, "maxNodes"));
                    queue.setMaxProcessors(getIntegerValue(queueNode, "maxProcessors"));
                    queue.setMaxRunTime(getIntegerValue(queueNode, "maxRunTime"));
                    queues.add(queue);
                });
                dto.setQueues(queues);
            }

            // Clean description (remove UI_FIELDS part)
            String cleanDescription = description.substring(0, description.indexOf("UI_FIELDS:")).trim();
            if (cleanDescription.endsWith("\n\n")) {
                cleanDescription = cleanDescription.substring(0, cleanDescription.length() - 2);
            }
            dto.setResourceDescription(cleanDescription);
            
        } catch (Exception e) {
            LOGGER.warn("Failed to extract compute UI fields from description", e);
        }
    }
    
    // Helper method to encode storage UI fields into description
    private String encodeStorageUIFieldsIntoDescription(StorageResourceDTO dto) {
        StringBuilder description = new StringBuilder();
        
        // Add base description
        if (dto.getStorageResourceDescription() != null) {
            description.append(dto.getStorageResourceDescription());
        }
        
        // Add UI fields as JSON
        try {
            Map<String, Object> uiFields = new HashMap<>();
            uiFields.put(STORAGE_TYPE_KEY, dto.getStorageType());
            uiFields.put(CAPACITY_TB_KEY, dto.getCapacityTB());
            uiFields.put(ACCESS_PROTOCOL_KEY, dto.getAccessProtocol());
            uiFields.put(SUPPORTS_ENCRYPTION_KEY, dto.getSupportsEncryption());
            uiFields.put(SUPPORTS_VERSIONING_KEY, dto.getSupportsVersioning());
            
            // Preserve critical fields
            uiFields.put(NAME_KEY, dto.getName());
            
            // S3-specific fields
            if (dto.getBucketName() != null) {
                uiFields.put(BUCKET_NAME_KEY, dto.getBucketName());
            }
            if (dto.getAccessKey() != null) {
                uiFields.put(ACCESS_KEY_KEY, dto.getAccessKey());
            }
            if (dto.getSecretKey() != null) {
                uiFields.put(SECRET_KEY_KEY, dto.getSecretKey());
            }
            
            // SCP-specific fields
            if (dto.getPort() != null) {
                uiFields.put(PORT_KEY, dto.getPort());
            }
            if (dto.getUsername() != null) {
                uiFields.put(USERNAME_KEY, dto.getUsername());
            }
            if (dto.getAuthenticationMethod() != null) {
                uiFields.put(AUTH_METHOD_KEY, dto.getAuthenticationMethod());
            }
            if (dto.getRemotePath() != null) {
                uiFields.put(REMOTE_PATH_KEY, dto.getRemotePath());
            }
            
            String uiFieldsJson = objectMapper.writeValueAsString(uiFields);
            String result = description.toString() + "\n\nUI_FIELDS: " + uiFieldsJson;
            
            // Check if result exceeds database column limit
            if (result.length() > 2000) {
                LOGGER.warn("JSON serialization length ({}) may exceed database column limit. Consider running the database migration script.", result.length());
            }
            
            return result;
            
        } catch (Exception e) {
            LOGGER.warn("Failed to encode storage UI fields", e);
            return description.toString();
        }
    }
    
    // Helper method to encode compute UI fields into description
    private String encodeComputeUIFieldsIntoDescription(ComputeResourceDTO dto) {
        StringBuilder description = new StringBuilder();
        
        // Add base description
        if (dto.getResourceDescription() != null) {
            description.append(dto.getResourceDescription());
        }
        
        // Add UI fields as JSON
        try {
            Map<String, Object> uiFields = new HashMap<>();
            uiFields.put(COMPUTE_TYPE_KEY, dto.getComputeType());
            uiFields.put(OPERATING_SYSTEM_KEY, dto.getOperatingSystem());
            uiFields.put(SCHEDULER_TYPE_KEY, dto.getResourceJobManagerType()); // Use new field name
            uiFields.put(DATA_MOVEMENT_PROTOCOL_KEY, dto.getDataMovementProtocol());
            uiFields.put(QUEUE_SYSTEM_KEY, dto.getQueueSystem());
            uiFields.put(RESOURCE_MANAGER_KEY, dto.getResourceManager());
            
            // SSH configuration fields (updated field names)
            uiFields.put(SSH_USERNAME_KEY, dto.getAlternativeSSHHostName()); // Repurposed field
            uiFields.put(SSH_PORT_KEY, dto.getSshPort());
            uiFields.put(AUTH_METHOD_KEY, dto.getSecurityProtocol()); // Use new field name
            
            // Preserve critical fields that might be lost
            uiFields.put(NAME_KEY, dto.getName());
            uiFields.put(HOST_ALIASES_KEY, dto.getHostAliases());
            uiFields.put(IP_ADDRESSES_KEY, dto.getIpAddresses());
            uiFields.put(QUEUES_KEY, dto.getQueues());
            
            String uiFieldsJson = objectMapper.writeValueAsString(uiFields);
            String result = description.toString() + "\n\nUI_FIELDS: " + uiFieldsJson;
            
            // Check if result exceeds database column limit (assume 255 for safety if not migrated)
            if (result.length() > 2000) {
                LOGGER.warn("JSON serialization length ({}) may exceed database column limit. Consider running the database migration script.", result.length());
                // Could implement compression here if needed, but for now just log the warning
            }
            
            return result;
            
        } catch (Exception e) {
            LOGGER.warn("Failed to encode compute UI fields", e);
            return description.toString();
        }
    }
    
    /**
     * Generate a human-readable name for storage resource from hostname and description
     */
    private String generateStorageResourceName(String hostName, String description) {
        if (description != null && description.length() > 10) {
            // Try to extract first line/sentence as name
            String firstLine = description.split("\n")[0].trim();
            if (firstLine.length() > 5 && firstLine.length() < 100) {
                return firstLine;
            }
        }
        
        // Fallback to hostname-based name
        if (hostName != null && !hostName.trim().isEmpty()) {
            String name = hostName.replace(".edu", "")
                                 .replace(".org", "")
                                 .replace(".com", "")
                                 .replace("-", " ")
                                 .replace(".", " ");
            
            // Capitalize words for better display
            String[] words = name.split("\\s+");
            StringBuilder result = new StringBuilder();
            for (String word : words) {
                if (word.length() > 0) {
                    if (result.length() > 0) result.append(" ");
                    result.append(word.substring(0, 1).toUpperCase())
                          .append(word.substring(1).toLowerCase());
                }
            }
            return result.toString();
        }
        
        // Ultimate fallback if hostname is also null/empty
        return "Unnamed Storage Resource";
    }
    
    /**
     * Generate a human-readable name for compute resource from hostname and description
     */
    private String generateComputeResourceName(String hostName, String description) {
        if (description != null && description.length() > 10) {
            // Try to extract first line/sentence as name
            String firstLine = description.split("\n")[0].trim();
            if (firstLine.length() > 5 && firstLine.length() < 100) {
                return firstLine;
            }
        }
        
        // Fallback to hostname-based name
        if (hostName != null && !hostName.trim().isEmpty()) {
            String name = hostName.replace(".edu", "")
                                 .replace(".org", "")
                                 .replace(".com", "")
                                 .replace("-", " ")
                                 .replace(".", " ");
            
            // Capitalize words for better display
            String[] words = name.split("\\s+");
            StringBuilder result = new StringBuilder();
            for (String word : words) {
                if (word.length() > 0) {
                    if (result.length() > 0) result.append(" ");
                    result.append(word.substring(0, 1).toUpperCase())
                          .append(word.substring(1).toLowerCase());
                }
            }
            return result.toString();
        }
        
        // Ultimate fallback if hostname is also null/empty
        return "Unnamed Compute Resource";
    }
    
    /**
     * Extract name from description UI fields
     */
    private String extractNameFromDescription(String description) {
        if (description == null || !description.contains("UI_FIELDS:")) {
            return null;
        }
        
        try {
            String jsonPart = description.substring(description.indexOf("UI_FIELDS:") + 10).trim();
            JsonNode rootNode = objectMapper.readTree(jsonPart);
            return getStringValue(rootNode, NAME_KEY);
        } catch (Exception e) {
            LOGGER.warn("Failed to extract name from description", e);
            return null;
        }
    }
    
    /**
     * Extract name from storage description UI fields
     */
    private String extractNameFromStorageDescription(String description) {
        if (description == null || !description.contains("UI_FIELDS:")) {
            return null;
        }
        
        try {
            String jsonPart = description.substring(description.indexOf("UI_FIELDS:") + 10).trim();
            JsonNode rootNode = objectMapper.readTree(jsonPart);
            return getStringValue(rootNode, NAME_KEY);
        } catch (Exception e) {
            LOGGER.warn("Failed to extract name from storage description", e);
            return null;
        }
    }
    
    /**
     * Generate a unique interface ID for JobSubmissionInterface or DataMovementInterface
     */
    private String generateInterfaceId(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}