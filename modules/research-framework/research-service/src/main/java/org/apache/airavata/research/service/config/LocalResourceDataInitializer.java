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
package org.apache.airavata.research.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.airavata.research.service.entity.LocalComputeResourceEntity;
import org.apache.airavata.research.service.entity.LocalStorageResourceEntity;
import org.apache.airavata.research.service.repository.LocalComputeResourceRepository;
import org.apache.airavata.research.service.repository.LocalStorageResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Local data initializer using existing airavata-api entities
 * Generates sample data for development without external registry services
 */
@Component
public class LocalResourceDataInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalResourceDataInitializer.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final LocalStorageResourceRepository storageResourceRepository;
    private final LocalComputeResourceRepository computeResourceRepository;

    public LocalResourceDataInitializer(LocalStorageResourceRepository storageResourceRepository,
                                       LocalComputeResourceRepository computeResourceRepository) {
        this.storageResourceRepository = storageResourceRepository;
        this.computeResourceRepository = computeResourceRepository;
    }

    @PostConstruct
    public void initializeData() {
        LOGGER.info("Initializing local resource data using airavata-api entities...");
        
        try {
            initializeStorageResources();
            initializeComputeResources();
            
            LOGGER.info("Local resource data initialization completed.");
        } catch (Exception e) {
            LOGGER.error("Error during local resource data initialization: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize local resource data", e);
        }
    }

    private void initializeStorageResources() {
        if (storageResourceRepository.count() == 0) {
            LOGGER.info("Creating local storage resources using LocalStorageResourceEntity...");
            
            LocalStorageResourceEntity[] storageResources = {
                createS3StorageResource(
                    "HPC Research Data Lake",
                    "s3.research.university.edu",
                    "Large-scale S3-compatible storage for computational research data with 500TB capacity",
                    createS3UIFields("hpc-research-data-lake", "us-east-1", 500L)
                ),
                
                createS3StorageResource(
                    "Genomics Cloud Storage", 
                    "genomics-s3.cloud.edu",
                    "Specialized S3 storage for genomics research data with HIPAA compliance",
                    createS3UIFields("genomics-research-bucket", "us-west-2", 1000L)
                ),
                
                createS3StorageResource(
                    "Neural Network Model Archive",
                    "ml-models.storage.edu", 
                    "S3 storage optimized for deep learning model artifacts and training datasets",
                    createS3UIFields("neural-network-models", "us-east-1", 250L)
                ),
                
                createSCPStorageResource(
                    "Supercomputer Scratch Storage",
                    "hpc-cluster.university.edu",
                    "High-performance parallel filesystem on supercomputing cluster for active computations",
                    createSCPUIFields(22, "research_user", "/scratch/research_projects", 2000L)
                ),
                
                createSCPStorageResource(
                    "Lab Server Archive",
                    "lab-server.research.university.edu", 
                    "Local lab server storage for secure research data archival and backup",
                    createSCPUIFields(2222, "lab_admin", "/data/archive", 50L)
                ),
                
                createSCPStorageResource(
                    "Collaboration Storage Server",
                    "collab-storage.consortium.org",
                    "Shared storage server for multi-institutional research collaboration",
                    createSCPUIFields(22, "collab_user", "/shared/projects", 100L)
                )
            };
            
            for (LocalStorageResourceEntity storage : storageResources) {
                storageResourceRepository.save(storage);
            }
            
            LOGGER.info("Created {} local storage resources", storageResources.length);
        }
    }

    private void initializeComputeResources() {
        if (computeResourceRepository.count() == 0) {
            LOGGER.info("Creating local compute resources using LocalComputeResourceEntity...");
            
            LocalComputeResourceEntity[] computeResources = {
                createComputeResource(
                    "Anvil Supercomputer (CPU)",
                    "anvil.rcac.purdue.edu",
                    "NSF-funded supercomputer at Purdue University with CPU nodes for large-scale scientific computing",
                    128, 4, 256, 30,
                    createComputeUIFields("ANVIL_CPU", "CentOS_7", "SLURM", "SCP")
                ),
                
                createComputeResource(
                    "Anvil Supercomputer (GPU)", 
                    "anvil-gpu.rcac.purdue.edu",
                    "NSF-funded supercomputer at Purdue University with GPU nodes for AI/ML workloads",
                    128, 4, 256, 30,
                    createComputeUIFields("ANVIL_GPU", "CentOS_7", "SLURM", "SCP")
                ),
                
                createComputeResource(
                    "Bridges-2 (PSC)",
                    "bridges2.psc.edu", 
                    "NSF-funded supercomputer at Pittsburgh Supercomputing Center for diverse research workloads",
                    128, 8, 512, 48,
                    createComputeUIFields("BRIDGES2", "CentOS_7", "SLURM", "SCP")
                ),
                
                createComputeResource(
                    "Expanse (SDSC)",
                    "login.expanse.sdsc.edu",
                    "NSF-funded supercomputer at San Diego Supercomputer Center for computational research",
                    128, 2, 256, 48,
                    createComputeUIFields("EXPANSE", "CentOS_8", "SLURM", "SCP")
                ),
                
                createComputeResource(
                    "Delta GPU Cluster",
                    "login.delta.ncsa.illinois.edu",
                    "GPU-focused supercomputer at NCSA for AI, machine learning, and data science workloads", 
                    64, 8, 256, 30,
                    createComputeUIFields("DELTA_GPU", "Red_Hat_8", "SLURM", "SCP")
                ),
                
                createComputeResource(
                    "Stampede3 (TACC)",
                    "stampede3.tacc.utexas.edu",
                    "Advanced supercomputer at Texas Advanced Computing Center for high-performance computing",
                    96, 4, 192, 48,
                    createComputeUIFields("STAMPEDE3", "CentOS_7", "SLURM", "SCP")
                )
            };
            
            for (LocalComputeResourceEntity compute : computeResources) {
                computeResourceRepository.save(compute);
            }
            
            LOGGER.info("Created {} local compute resources", computeResources.length);
        }
    }

    private LocalStorageResourceEntity createS3StorageResource(String description, String hostName, 
                                                         String fullDescription, String uiFieldsJson) {
        LocalStorageResourceEntity storage = new LocalStorageResourceEntity();
        storage.setStorageResourceId(UUID.randomUUID().toString());
        storage.setStorageResourceDescription(fullDescription + "\n\nUI_FIELDS: " + uiFieldsJson);
        storage.setHostName(hostName);
        storage.setEnabled(true);
        storage.setCreationTime(new Timestamp(System.currentTimeMillis()));
        storage.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        return storage;
    }

    private LocalStorageResourceEntity createSCPStorageResource(String description, String hostName,
                                                          String fullDescription, String uiFieldsJson) {
        LocalStorageResourceEntity storage = new LocalStorageResourceEntity();
        storage.setStorageResourceId(UUID.randomUUID().toString());
        storage.setStorageResourceDescription(fullDescription + "\n\nUI_FIELDS: " + uiFieldsJson);
        storage.setHostName(hostName);
        storage.setEnabled(true);
        storage.setCreationTime(new Timestamp(System.currentTimeMillis()));
        storage.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        return storage;
    }

    private LocalComputeResourceEntity createComputeResource(String description, String hostName, String fullDescription,
                                                       int cpusPerNode, int defaultNodeCount, int maxMemoryPerNode,
                                                       int defaultWalltime, String uiFieldsJson) {
        LocalComputeResourceEntity compute = new LocalComputeResourceEntity();
        compute.setComputeResourceId(UUID.randomUUID().toString());
        compute.setResourceDescription(fullDescription + "\n\nUI_FIELDS: " + uiFieldsJson);
        compute.setHostName(hostName);
        compute.setEnabled((short) 1);
        compute.setCpusPerNode(cpusPerNode);
        compute.setDefaultNodeCount(defaultNodeCount);
        compute.setDefaultCPUCount(cpusPerNode * defaultNodeCount);
        compute.setMaxMemoryPerNode(maxMemoryPerNode);
        compute.setDefaultWalltime(defaultWalltime);
        compute.setCreationTime(new Timestamp(System.currentTimeMillis()));
        compute.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        return compute;
    }

    // Helper methods to create UI-specific field JSON
    private String createS3UIFields(String bucketName, String region, Long capacityTB) {
        try {
            Map<String, Object> uiFields = new HashMap<>();
            uiFields.put("storageType", "S3");
            uiFields.put("accessProtocol", "S3");
            uiFields.put("bucketName", bucketName);
            uiFields.put("region", region);
            uiFields.put("capacityTB", capacityTB);
            uiFields.put("supportsEncryption", true);
            uiFields.put("supportsVersioning", true);
            return objectMapper.writeValueAsString(uiFields);
        } catch (Exception e) {
            LOGGER.warn("Failed to serialize S3 UI fields", e);
            return "{}";
        }
    }

    private String createSCPUIFields(Integer port, String username, String remotePath, Long capacityTB) {
        try {
            Map<String, Object> uiFields = new HashMap<>();
            uiFields.put("storageType", "SCP");
            uiFields.put("accessProtocol", "SCP");
            uiFields.put("port", port);
            uiFields.put("username", username);
            uiFields.put("remotePath", remotePath);
            uiFields.put("capacityTB", capacityTB);
            uiFields.put("authenticationMethod", "SSH_KEY");
            return objectMapper.writeValueAsString(uiFields);
        } catch (Exception e) {
            LOGGER.warn("Failed to serialize SCP UI fields", e);
            return "{}";
        }
    }

    private String createComputeUIFields(String computeType, String operatingSystem, String schedulerType, String dataMovementProtocol) {
        try {
            Map<String, Object> uiFields = new HashMap<>();
            uiFields.put("computeType", computeType);
            uiFields.put("operatingSystem", operatingSystem);
            uiFields.put("schedulerType", schedulerType);
            uiFields.put("dataMovementProtocol", dataMovementProtocol);
            return objectMapper.writeValueAsString(uiFields);
        } catch (Exception e) {
            LOGGER.warn("Failed to serialize compute UI fields", e);
            return "{}";
        }
    }
}