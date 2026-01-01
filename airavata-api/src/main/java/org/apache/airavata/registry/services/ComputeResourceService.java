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
import java.util.stream.Collectors;
import org.apache.airavata.common.model.AiravataCommonsConstants;
import org.apache.airavata.common.model.ApplicationParallelismType;
import org.apache.airavata.common.model.CloudJobSubmission;
import org.apache.airavata.common.model.ComputeResourceDescription;
import org.apache.airavata.common.model.DMType;
import org.apache.airavata.common.model.DataMovementInterface;
import org.apache.airavata.common.model.FileSystems;
import org.apache.airavata.common.model.GlobusJobSubmission;
import org.apache.airavata.common.model.GridFTPDataMovement;
import org.apache.airavata.common.model.JobManagerCommand;
import org.apache.airavata.common.model.JobSubmissionInterface;
import org.apache.airavata.common.model.LOCALDataMovement;
import org.apache.airavata.common.model.LOCALSubmission;
import org.apache.airavata.common.model.ResourceJobManager;
import org.apache.airavata.common.model.SCPDataMovement;
import org.apache.airavata.common.model.SSHJobSubmission;
import org.apache.airavata.common.model.UnicoreDataMovement;
import org.apache.airavata.common.model.UnicoreJobSubmission;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.registry.entities.appcatalog.BatchQueueEntity;
import org.apache.airavata.registry.entities.appcatalog.CloudJobSubmissionEntity;
import org.apache.airavata.registry.entities.appcatalog.ComputeResourceEntity;
import org.apache.airavata.registry.entities.appcatalog.ComputeResourceFileSystemEntity;
import org.apache.airavata.registry.entities.appcatalog.DataMovementInterfaceEntity;
import org.apache.airavata.registry.entities.appcatalog.GridftpDataMovementEntity;
import org.apache.airavata.registry.entities.appcatalog.GridftpEndpointEntity;
import org.apache.airavata.registry.entities.appcatalog.JobManagerCommandEntity;
import org.apache.airavata.registry.entities.appcatalog.JobManagerCommandPK;
import org.apache.airavata.registry.entities.appcatalog.JobSubmissionInterfaceEntity;
import org.apache.airavata.registry.entities.appcatalog.LocalDataMovementEntity;
import org.apache.airavata.registry.entities.appcatalog.LocalSubmissionEntity;
import org.apache.airavata.registry.entities.appcatalog.ParallelismCommandEntity;
import org.apache.airavata.registry.entities.appcatalog.ParallelismCommandPK;
import org.apache.airavata.registry.entities.appcatalog.ResourceJobManagerEntity;
import org.apache.airavata.registry.entities.appcatalog.ScpDataMovementEntity;
import org.apache.airavata.registry.entities.appcatalog.SshJobSubmissionEntity;
import org.apache.airavata.registry.entities.appcatalog.UnicoreDatamovementEntity;
import org.apache.airavata.registry.entities.appcatalog.UnicoreSubmissionEntity;
import org.apache.airavata.registry.exception.AppCatalogException;
import org.apache.airavata.registry.mappers.BatchQueueMapper;
import org.apache.airavata.registry.mappers.CloudJobSubmissionMapper;
import org.apache.airavata.registry.mappers.ComputeResourceDataMovementInterfaceBaseMapper;
import org.apache.airavata.registry.mappers.ComputeResourceDataMovementInterfaceMapper;
import org.apache.airavata.registry.mappers.ComputeResourceMapper;
import org.apache.airavata.registry.mappers.GridFTPDataMovementMapper;
import org.apache.airavata.registry.mappers.JobSubmissionInterfaceMapper;
import org.apache.airavata.registry.mappers.LocalDataMovementMapper;
import org.apache.airavata.registry.mappers.LocalSubmissionMapper;
import org.apache.airavata.registry.mappers.ResourceJobManagerMapper;
import org.apache.airavata.registry.mappers.SCPDataMovementMapper;
import org.apache.airavata.registry.mappers.SSHJobSubmissionMapper;
import org.apache.airavata.registry.mappers.UnicoreDataMovementMapper;
import org.apache.airavata.registry.mappers.UnicoreJobSubmissionMapper;
import org.apache.airavata.registry.repositories.appcatalog.CloudJobSubmissionRepository;
import org.apache.airavata.registry.repositories.appcatalog.ComputeResourceFileSystemRepository;
import org.apache.airavata.registry.repositories.appcatalog.ComputeResourceRepository;
import org.apache.airavata.registry.repositories.appcatalog.DataMovementRepository;
import org.apache.airavata.registry.repositories.appcatalog.GridftpDataMovementRepository;
import org.apache.airavata.registry.repositories.appcatalog.GridftpEndpointRepository;
import org.apache.airavata.registry.repositories.appcatalog.JobManagerCommandRepository;
import org.apache.airavata.registry.repositories.appcatalog.JobSubmissionInterfaceRepository;
import org.apache.airavata.registry.repositories.appcatalog.LocalDataMovementRepository;
import org.apache.airavata.registry.repositories.appcatalog.LocalSubmissionRepository;
import org.apache.airavata.registry.repositories.appcatalog.ParallelismCommandRepository;
import org.apache.airavata.registry.repositories.appcatalog.ResourceJobManagerRepository;
import org.apache.airavata.registry.repositories.appcatalog.ScpDataMovementRepository;
import org.apache.airavata.registry.repositories.appcatalog.SshJobSubmissionRepository;
import org.apache.airavata.registry.repositories.appcatalog.UnicoreDatamovementRepository;
import org.apache.airavata.registry.repositories.appcatalog.UnicoreSubmissionRepository;
import org.apache.airavata.registry.utils.AppCatalogUtils;
import org.apache.airavata.registry.utils.DBConstants;
import org.apache.airavata.registry.utils.EntityMergeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ComputeResourceService {
    private static final Logger logger = LoggerFactory.getLogger(ComputeResourceService.class);

    private final ComputeResourceRepository computeResourceRepository;
    private final ComputeResourceFileSystemRepository computeResourceFileSystemRepository;
    private final ResourceJobManagerRepository resourceJobManagerRepository;
    private final JobManagerCommandRepository jobManagerCommandRepository;
    private final ParallelismCommandRepository parallelismCommandRepository;
    private final JobSubmissionInterfaceRepository jobSubmissionInterfaceRepository;
    private final SshJobSubmissionRepository sshJobSubmissionRepository;
    private final CloudJobSubmissionRepository cloudJobSubmissionRepository;
    private final LocalSubmissionRepository localSubmissionRepository;
    private final UnicoreSubmissionRepository unicoreSubmissionRepository;
    private final DataMovementRepository dataMovementRepository;
    private final LocalDataMovementRepository localDataMovementRepository;
    private final ScpDataMovementRepository scpDataMovementRepository;
    private final GridftpDataMovementRepository gridftpDataMovementRepository;
    private final GridftpEndpointRepository gridftpEndpointRepository;
    private final UnicoreDatamovementRepository unicoreDatamovementRepository;
    private final ComputeResourceMapper computeResourceMapper;
    private final BatchQueueMapper batchQueueMapper;
    private final ComputeResourceDataMovementInterfaceMapper computeResourceDataMovementInterfaceMapper;
    private final SSHJobSubmissionMapper sshJobSubmissionMapper;
    private final CloudJobSubmissionMapper cloudJobSubmissionMapper;
    private final LocalSubmissionMapper localSubmissionMapper;
    private final UnicoreJobSubmissionMapper unicoreJobSubmissionMapper;
    private final ResourceJobManagerMapper resourceJobManagerMapper;
    private final LocalDataMovementMapper localDataMovementMapper;
    private final SCPDataMovementMapper scpDataMovementMapper;
    private final UnicoreDataMovementMapper unicoreDataMovementMapper;
    private final GridFTPDataMovementMapper gridFTPDataMovementMapper;
    private final JobSubmissionInterfaceMapper jobSubmissionInterfaceMapper;
    private final ComputeResourceDataMovementInterfaceBaseMapper computeResourceDataMovementInterfaceBaseMapper;

    public ComputeResourceService(
            ComputeResourceRepository computeResourceRepository,
            ComputeResourceFileSystemRepository computeResourceFileSystemRepository,
            ResourceJobManagerRepository resourceJobManagerRepository,
            JobManagerCommandRepository jobManagerCommandRepository,
            ParallelismCommandRepository parallelismCommandRepository,
            JobSubmissionInterfaceRepository jobSubmissionInterfaceRepository,
            SshJobSubmissionRepository sshJobSubmissionRepository,
            CloudJobSubmissionRepository cloudJobSubmissionRepository,
            LocalSubmissionRepository localSubmissionRepository,
            UnicoreSubmissionRepository unicoreSubmissionRepository,
            DataMovementRepository dataMovementRepository,
            LocalDataMovementRepository localDataMovementRepository,
            ScpDataMovementRepository scpDataMovementRepository,
            GridftpDataMovementRepository gridftpDataMovementRepository,
            GridftpEndpointRepository gridftpEndpointRepository,
            UnicoreDatamovementRepository unicoreDatamovementRepository,
            ComputeResourceMapper computeResourceMapper,
            BatchQueueMapper batchQueueMapper,
            ComputeResourceDataMovementInterfaceMapper computeResourceDataMovementInterfaceMapper,
            SSHJobSubmissionMapper sshJobSubmissionMapper,
            CloudJobSubmissionMapper cloudJobSubmissionMapper,
            LocalSubmissionMapper localSubmissionMapper,
            UnicoreJobSubmissionMapper unicoreJobSubmissionMapper,
            ResourceJobManagerMapper resourceJobManagerMapper,
            LocalDataMovementMapper localDataMovementMapper,
            SCPDataMovementMapper scpDataMovementMapper,
            UnicoreDataMovementMapper unicoreDataMovementMapper,
            GridFTPDataMovementMapper gridFTPDataMovementMapper,
            JobSubmissionInterfaceMapper jobSubmissionInterfaceMapper,
            ComputeResourceDataMovementInterfaceBaseMapper computeResourceDataMovementInterfaceBaseMapper) {
        this.computeResourceRepository = computeResourceRepository;
        this.computeResourceFileSystemRepository = computeResourceFileSystemRepository;
        this.resourceJobManagerRepository = resourceJobManagerRepository;
        this.jobManagerCommandRepository = jobManagerCommandRepository;
        this.parallelismCommandRepository = parallelismCommandRepository;
        this.jobSubmissionInterfaceRepository = jobSubmissionInterfaceRepository;
        this.sshJobSubmissionRepository = sshJobSubmissionRepository;
        this.cloudJobSubmissionRepository = cloudJobSubmissionRepository;
        this.localSubmissionRepository = localSubmissionRepository;
        this.unicoreSubmissionRepository = unicoreSubmissionRepository;
        this.dataMovementRepository = dataMovementRepository;
        this.localDataMovementRepository = localDataMovementRepository;
        this.scpDataMovementRepository = scpDataMovementRepository;
        this.gridftpDataMovementRepository = gridftpDataMovementRepository;
        this.gridftpEndpointRepository = gridftpEndpointRepository;
        this.unicoreDatamovementRepository = unicoreDatamovementRepository;
        this.computeResourceMapper = computeResourceMapper;
        this.batchQueueMapper = batchQueueMapper;
        this.computeResourceDataMovementInterfaceMapper = computeResourceDataMovementInterfaceMapper;
        this.sshJobSubmissionMapper = sshJobSubmissionMapper;
        this.cloudJobSubmissionMapper = cloudJobSubmissionMapper;
        this.localSubmissionMapper = localSubmissionMapper;
        this.unicoreJobSubmissionMapper = unicoreJobSubmissionMapper;
        this.resourceJobManagerMapper = resourceJobManagerMapper;
        this.localDataMovementMapper = localDataMovementMapper;
        this.scpDataMovementMapper = scpDataMovementMapper;
        this.unicoreDataMovementMapper = unicoreDataMovementMapper;
        this.gridFTPDataMovementMapper = gridFTPDataMovementMapper;
        this.jobSubmissionInterfaceMapper = jobSubmissionInterfaceMapper;
        this.computeResourceDataMovementInterfaceBaseMapper = computeResourceDataMovementInterfaceBaseMapper;
    }

    public String addComputeResource(ComputeResourceDescription description) throws AppCatalogException {
        if (description.getComputeResourceId() == null
                || description.getComputeResourceId().equals("")
                || description.getComputeResourceId().equals(AiravataCommonsConstants.DEFAULT_ID)) {
            description.setComputeResourceId(AppCatalogUtils.getID(description.getHostName()));
        }
        return saveComputeResourceDescriptorData(description);
    }

    private String saveComputeResourceDescriptorData(ComputeResourceDescription description)
            throws AppCatalogException {
        ComputeResourceEntity computeResourceEntity = saveComputeResource(description);
        saveFileSystems(description, computeResourceEntity);
        return computeResourceEntity.getComputeResourceId();
    }

    private ComputeResourceEntity saveComputeResource(ComputeResourceDescription description)
            throws AppCatalogException {
        String computeResourceId = description.getComputeResourceId();

        ComputeResourceEntity existingEntity =
                computeResourceRepository.findById(computeResourceId).orElse(null);
        ComputeResourceEntity computeResourceEntity;

        if (existingEntity != null) {
            // Map model to new entity to get the desired state
            ComputeResourceEntity newEntity = computeResourceMapper.toEntity(description);
            // Copy simple fields to existing entity (MapStruct doesn't have update method, so we manually copy)
            existingEntity.setHostName(newEntity.getHostName());
            existingEntity.setResourceDescription(newEntity.getResourceDescription());
            existingEntity.setEnabled(newEntity.getEnabled());
            existingEntity.setGatewayUsageExecutable(newEntity.getGatewayUsageExecutable());
            existingEntity.setGatewayUsageModuleLoadCommand(newEntity.getGatewayUsageModuleLoadCommand());
            existingEntity.setGatewayUsageReporting(newEntity.isGatewayUsageReporting());
            existingEntity.setMaxMemoryPerNode(newEntity.getMaxMemoryPerNode());
            existingEntity.setCpusPerNode(newEntity.getCpusPerNode());
            existingEntity.setDefaultNodeCount(newEntity.getDefaultNodeCount());
            existingEntity.setDefaultCPUCount(newEntity.getDefaultCPUCount());
            existingEntity.setDefaultWalltime(newEntity.getDefaultWalltime());
            existingEntity.setIpAddresses(newEntity.getIpAddresses());
            existingEntity.setHostAliases(newEntity.getHostAliases());

            // For ElementCollections (ipAddresses, hostAliases), replace the list to avoid duplicates
            if (description.getIpAddresses() != null) {
                existingEntity.setIpAddresses(new java.util.ArrayList<>(description.getIpAddresses()));
            }
            if (description.getHostAliases() != null) {
                existingEntity.setHostAliases(new java.util.ArrayList<>(description.getHostAliases()));
            }

            // Properly merge lists using EntityMergeHelper (handles duplicates gracefully)
            EntityMergeHelper.mergeLists(
                    existingEntity.getBatchQueues(), newEntity.getBatchQueues(), BatchQueueEntity::getQueueName);
            EntityMergeHelper.mergeLists(
                    existingEntity.getDataMovementInterfaces(),
                    newEntity.getDataMovementInterfaces(),
                    DataMovementInterfaceEntity::getDataMovementInterfaceId);
            EntityMergeHelper.mergeLists(
                    existingEntity.getJobSubmissionInterfaces(),
                    newEntity.getJobSubmissionInterfaces(),
                    JobSubmissionInterfaceEntity::getJobSubmissionInterfaceId);

            computeResourceEntity = existingEntity;
        } else {
            computeResourceEntity = computeResourceMapper.toEntity(description);
            // Ensure creationTime is set for new entities
            if (computeResourceEntity.getCreationTime() == null) {
                computeResourceEntity.setCreationTime(AiravataUtils.getCurrentTimestamp());
            }
        }
        // Ensure updateTime is set
        if (computeResourceEntity.getUpdateTime() == null) {
            computeResourceEntity.setUpdateTime(AiravataUtils.getCurrentTimestamp());
        }

        if (computeResourceEntity.getBatchQueues() != null) {
            computeResourceEntity
                    .getBatchQueues()
                    .forEach(batchQueueEntity -> batchQueueEntity.setComputeResourceId(computeResourceId));
        }
        if (computeResourceEntity.getDataMovementInterfaces() != null) {
            computeResourceEntity
                    .getDataMovementInterfaces()
                    .forEach(dataMovementInterfaceEntity ->
                            dataMovementInterfaceEntity.setComputeResourceId(computeResourceId));
        }
        if (computeResourceEntity.getJobSubmissionInterfaces() != null) {
            computeResourceEntity.getJobSubmissionInterfaces().forEach(jobSubmissionInterfaceEntity -> {
                jobSubmissionInterfaceEntity.setComputeResourceId(computeResourceId);
                // Ensure creationTime and updateTime are set
                if (jobSubmissionInterfaceEntity.getCreationTime() == null) {
                    jobSubmissionInterfaceEntity.setCreationTime(AiravataUtils.getCurrentTimestamp());
                }
                if (jobSubmissionInterfaceEntity.getUpdateTime() == null) {
                    jobSubmissionInterfaceEntity.setUpdateTime(AiravataUtils.getCurrentTimestamp());
                }
            });
        }
        if (computeResourceEntity.getDataMovementInterfaces() != null) {
            computeResourceEntity.getDataMovementInterfaces().forEach(dataMovementInterfaceEntity -> {
                dataMovementInterfaceEntity.setComputeResourceId(computeResourceId);
                // Ensure creationTime and updateTime are set
                if (dataMovementInterfaceEntity.getCreationTime() == null) {
                    dataMovementInterfaceEntity.setCreationTime(AiravataUtils.getCurrentTimestamp());
                }
                if (dataMovementInterfaceEntity.getUpdateTime() == null) {
                    dataMovementInterfaceEntity.setUpdateTime(AiravataUtils.getCurrentTimestamp());
                }
            });
        }
        return computeResourceRepository.save(computeResourceEntity);
    }

    private void saveFileSystems(ComputeResourceDescription description, ComputeResourceEntity computeHostResource)
            throws AppCatalogException {
        Map<FileSystems, String> fileSystems = description.getFileSystems();
        if (fileSystems != null && !fileSystems.isEmpty()) {
            for (FileSystems key : fileSystems.keySet()) {
                ComputeResourceFileSystemEntity computeResourceFileSystemEntity = new ComputeResourceFileSystemEntity();
                computeResourceFileSystemEntity.setComputeResourceId(computeHostResource.getComputeResourceId());
                computeResourceFileSystemEntity.setFileSystem(key);
                computeResourceFileSystemEntity.setPath(fileSystems.get(key));
                computeResourceFileSystemEntity.setComputeResource(computeHostResource);
                computeResourceFileSystemRepository.save(computeResourceFileSystemEntity);
            }
        }
    }

    public Map<FileSystems, String> getFileSystems(String computeResourceId) {
        List<ComputeResourceFileSystemEntity> computeResourceFileSystemEntityList =
                computeResourceFileSystemRepository.findByComputeResourceId(computeResourceId);
        Map<FileSystems, String> fileSystemsMap = new HashMap<>();
        for (ComputeResourceFileSystemEntity fs : computeResourceFileSystemEntityList) {
            fileSystemsMap.put(fs.getFileSystem(), fs.getPath());
        }
        return fileSystemsMap;
    }

    public void updateComputeResource(String computeResourceId, ComputeResourceDescription updatedComputeResource)
            throws AppCatalogException {
        saveComputeResourceDescriptorData(updatedComputeResource);
    }

    public ComputeResourceDescription getComputeResource(String resourceId) throws AppCatalogException {
        ComputeResourceEntity entity =
                computeResourceRepository.findById(resourceId).orElse(null);
        if (entity == null) return null;
        ComputeResourceDescription computeResourceDescription = computeResourceMapper.toModel(entity);
        computeResourceDescription.setFileSystems(getFileSystems(resourceId));
        // Manually map nested lists - ensure they're never null
        if (entity.getBatchQueues() != null) {
            computeResourceDescription.setBatchQueues(batchQueueMapper.toModelList(entity.getBatchQueues()));
        } else {
            computeResourceDescription.setBatchQueues(new java.util.ArrayList<>());
        }
        if (entity.getDataMovementInterfaces() != null) {
            computeResourceDescription.setDataMovementInterfaces(
                    computeResourceDataMovementInterfaceMapper.toModelList(entity.getDataMovementInterfaces()));
        } else {
            computeResourceDescription.setDataMovementInterfaces(new java.util.ArrayList<>());
        }
        // Load JobSubmissionInterfaces - they are polymorphic but need to be loaded
        if (entity.getJobSubmissionInterfaces() != null
                && !entity.getJobSubmissionInterfaces().isEmpty()) {
            List<JobSubmissionInterface> jobSubmissionInterfaces = new java.util.ArrayList<>();
            for (JobSubmissionInterfaceEntity jsiEntity : entity.getJobSubmissionInterfaces()) {
                JobSubmissionInterface jsi = jobSubmissionInterfaceMapper.toModel(jsiEntity);
                if (jsi != null) {
                    jobSubmissionInterfaces.add(jsi);
                }
            }
            computeResourceDescription.setJobSubmissionInterfaces(jobSubmissionInterfaces);
        }
        return computeResourceDescription;
    }

    public List<ComputeResourceDescription> getComputeResourceList(Map<String, String> filters)
            throws AppCatalogException {
        if (filters == null || filters.isEmpty()) {
            return getAllComputeResourceList();
        }

        if (filters.containsKey(DBConstants.ComputeResource.HOST_NAME)) {
            String hostNameValue = filters.get(DBConstants.ComputeResource.HOST_NAME);
            if (hostNameValue == null || hostNameValue.trim().isEmpty()) {
                return getAllComputeResourceList();
            }
            String hostName = "%" + hostNameValue + "%";
            List<ComputeResourceEntity> entities = computeResourceRepository.findByHostName(hostName);
            List<ComputeResourceDescription> result = entities.stream()
                    .map(e -> {
                        ComputeResourceDescription desc = computeResourceMapper.toModel(e);
                        desc.setFileSystems(getFileSystems(desc.getComputeResourceId()));
                        if (e.getBatchQueues() != null) {
                            desc.setBatchQueues(batchQueueMapper.toModelList(e.getBatchQueues()));
                        }
                        if (e.getDataMovementInterfaces() != null) {
                            desc.setDataMovementInterfaces(computeResourceDataMovementInterfaceMapper.toModelList(
                                    e.getDataMovementInterfaces()));
                        }
                        return desc;
                    })
                    .collect(Collectors.toList());
            return result;
        } else {
            logger.error("Unsupported field name for compute resource: "
                    + (filters.keySet().isEmpty()
                            ? "empty filters"
                            : filters.keySet().iterator().next()));
            throw new IllegalArgumentException("Unsupported field name for compute resource.");
        }
    }

    public List<ComputeResourceDescription> getAllComputeResourceList() throws AppCatalogException {
        List<ComputeResourceEntity> entities = computeResourceRepository.findAll();
        return entities.stream()
                .map(e -> {
                    ComputeResourceDescription desc = computeResourceMapper.toModel(e);
                    desc.setFileSystems(getFileSystems(desc.getComputeResourceId()));
                    if (e.getBatchQueues() != null) {
                        desc.setBatchQueues(batchQueueMapper.toModelList(e.getBatchQueues()));
                    }
                    if (e.getDataMovementInterfaces() != null) {
                        desc.setDataMovementInterfaces(
                                computeResourceDataMovementInterfaceMapper.toModelList(e.getDataMovementInterfaces()));
                    }
                    return desc;
                })
                .collect(Collectors.toList());
    }

    public Map<String, String> getAllComputeResourceIdList() throws AppCatalogException {
        List<ComputeResourceEntity> entities = computeResourceRepository.findAll();
        Map<String, String> computeResourceMap = new HashMap<>();
        for (ComputeResourceEntity entity : entities) {
            ComputeResourceDescription desc = computeResourceMapper.toModel(entity);
            computeResourceMap.put(desc.getComputeResourceId(), desc.getHostName());
        }
        return computeResourceMap;
    }

    public Map<String, String> getAvailableComputeResourceIdList() throws AppCatalogException {
        List<ComputeResourceEntity> entities = computeResourceRepository.findAll();
        Map<String, String> computeResourceMap = new HashMap<>();
        for (ComputeResourceEntity entity : entities) {
            ComputeResourceDescription desc = computeResourceMapper.toModel(entity);
            if (desc.getEnabled()) {
                computeResourceMap.put(desc.getComputeResourceId(), desc.getHostName());
            }
        }
        return computeResourceMap;
    }

    public boolean isComputeResourceExists(String resourceId) throws AppCatalogException {
        return computeResourceRepository.existsById(resourceId);
    }

    public void removeComputeResource(String resourceId) throws AppCatalogException {
        computeResourceRepository.deleteById(resourceId);
    }

    public String addSSHJobSubmission(SSHJobSubmission sshJobSubmission) throws AppCatalogException {
        String submissionId = AppCatalogUtils.getID("SSH");
        sshJobSubmission.setJobSubmissionInterfaceId(submissionId);
        String resourceJobManagerId = addResourceJobManager(sshJobSubmission.getResourceJobManager());

        ResourceJobManagerEntity resourceJobManagerEntity = resourceJobManagerRepository
                .findById(resourceJobManagerId)
                .orElseThrow(() -> new AppCatalogException("ResourceJobManager not found: " + resourceJobManagerId));

        SshJobSubmissionEntity sshJobSubmissionEntity = sshJobSubmissionMapper.toEntity(sshJobSubmission);
        sshJobSubmissionEntity.setResourceJobManager(resourceJobManagerEntity);
        // Ensure updateTime is set
        if (sshJobSubmissionEntity.getUpdateTime() == null) {
            sshJobSubmissionEntity.setUpdateTime(AiravataUtils.getCurrentTimestamp());
        }
        // Ensure resourceJobManager updateTime is preserved (cascade MERGE might reset it)
        if (resourceJobManagerEntity.getUpdateTime() == null) {
            resourceJobManagerEntity.setUpdateTime(AiravataUtils.getCurrentTimestamp());
        }

        if (sshJobSubmission.getResourceJobManager().getParallelismPrefix() != null) {
            createParallesimPrefix(
                    sshJobSubmission.getResourceJobManager().getParallelismPrefix(),
                    sshJobSubmissionEntity.getResourceJobManager());
        }
        if (sshJobSubmission.getResourceJobManager().getJobManagerCommands() != null) {
            createJobManagerCommand(
                    sshJobSubmission.getResourceJobManager().getJobManagerCommands(),
                    sshJobSubmissionEntity.getResourceJobManager());
        }
        if (sshJobSubmission.getMonitorMode() != null) {
            sshJobSubmissionEntity.setMonitorMode(
                    sshJobSubmission.getMonitorMode().toString());
        }
        sshJobSubmissionRepository.save(sshJobSubmissionEntity);
        return submissionId;
    }

    public void updateSSHJobSubmission(SSHJobSubmission sshJobSubmission) throws AppCatalogException {
        SshJobSubmissionEntity existingEntity = sshJobSubmissionRepository
                .findById(sshJobSubmission.getJobSubmissionInterfaceId())
                .orElse(null);
        SshJobSubmissionEntity sshJobSubmissionEntity;
        if (existingEntity != null) {
            sshJobSubmissionMapper.toEntity(sshJobSubmission);
            sshJobSubmissionEntity = existingEntity;
        } else {
            sshJobSubmissionEntity = sshJobSubmissionMapper.toEntity(sshJobSubmission);
        }
        sshJobSubmissionEntity.setUpdateTime(AiravataUtils.getCurrentTimestamp());
        sshJobSubmissionRepository.save(sshJobSubmissionEntity);
    }

    public String addCloudJobSubmission(CloudJobSubmission cloudJobSubmission) throws AppCatalogException {
        cloudJobSubmission.setJobSubmissionInterfaceId(AppCatalogUtils.getID("Cloud"));
        CloudJobSubmissionEntity cloudJobSubmissionEntity = cloudJobSubmissionMapper.toEntity(cloudJobSubmission);
        cloudJobSubmissionRepository.save(cloudJobSubmissionEntity);
        return cloudJobSubmissionEntity.getJobSubmissionInterfaceId();
    }

    public void updateCloudJobSubmission(CloudJobSubmission cloudJobSubmission) throws AppCatalogException {
        CloudJobSubmissionEntity existingEntity = cloudJobSubmissionRepository
                .findById(cloudJobSubmission.getJobSubmissionInterfaceId())
                .orElse(null);
        CloudJobSubmissionEntity cloudJobSubmissionEntity;
        if (existingEntity != null) {
            // Update existing entity - MapStruct doesn't support in-place updates, so we create new and copy ID
            CloudJobSubmissionEntity updated = cloudJobSubmissionMapper.toEntity(cloudJobSubmission);
            updated.setJobSubmissionInterfaceId(existingEntity.getJobSubmissionInterfaceId());
            cloudJobSubmissionEntity = updated;
        } else {
            cloudJobSubmissionEntity = cloudJobSubmissionMapper.toEntity(cloudJobSubmission);
        }
        cloudJobSubmissionRepository.save(cloudJobSubmissionEntity);
    }

    public String addResourceJobManager(ResourceJobManager resourceJobManager) throws AppCatalogException {
        resourceJobManager.setResourceJobManagerId(AppCatalogUtils.getID("RJM"));
        ResourceJobManagerEntity resourceJobManagerEntity = resourceJobManagerMapper.toEntity(resourceJobManager);
        // Set updateTime if not already set
        if (resourceJobManagerEntity.getUpdateTime() == null) {
            resourceJobManagerEntity.setUpdateTime(AiravataUtils.getCurrentTimestamp());
        }
        resourceJobManagerEntity = resourceJobManagerRepository.save(resourceJobManagerEntity);
        var jobManagerCommands = resourceJobManager.getJobManagerCommands();
        if (jobManagerCommands != null && !jobManagerCommands.isEmpty()) {
            createJobManagerCommand(jobManagerCommands, resourceJobManagerEntity);
        }

        var parallelismPrefix = resourceJobManager.getParallelismPrefix();
        if (parallelismPrefix != null && !parallelismPrefix.isEmpty()) {
            createParallesimPrefix(parallelismPrefix, resourceJobManagerEntity);
        }
        return resourceJobManager.getResourceJobManagerId();
    }

    public void updateResourceJobManager(String resourceJobManagerId, ResourceJobManager updatedResourceJobManager)
            throws AppCatalogException {
        updatedResourceJobManager.setResourceJobManagerId(resourceJobManagerId);

        ResourceJobManagerEntity existingEntity =
                resourceJobManagerRepository.findById(resourceJobManagerId).orElse(null);
        ResourceJobManagerEntity resourceJobManagerEntity;
        if (existingEntity != null) {
            resourceJobManagerMapper.toEntity(updatedResourceJobManager);
            resourceJobManagerEntity = existingEntity;
        } else {
            resourceJobManagerEntity = resourceJobManagerMapper.toEntity(updatedResourceJobManager);
        }
        // Always update the updateTime
        resourceJobManagerEntity.setUpdateTime(AiravataUtils.getCurrentTimestamp());

        resourceJobManagerEntity = resourceJobManagerRepository.save(resourceJobManagerEntity);
        Map<JobManagerCommand, String> jobManagerCommands = updatedResourceJobManager.getJobManagerCommands();
        if (jobManagerCommands != null && jobManagerCommands.size() != 0) {
            createJobManagerCommand(jobManagerCommands, resourceJobManagerEntity);
        }

        Map<ApplicationParallelismType, String> parallelismPrefix = updatedResourceJobManager.getParallelismPrefix();
        if (parallelismPrefix != null && parallelismPrefix.size() != 0) {
            createParallesimPrefix(parallelismPrefix, resourceJobManagerEntity);
        }
    }

    public ResourceJobManager getResourceJobManager(String resourceJobManagerId) throws AppCatalogException {
        ResourceJobManagerEntity entity =
                resourceJobManagerRepository.findById(resourceJobManagerId).orElse(null);
        if (entity == null) return null;
        ResourceJobManager resourceJobManager = resourceJobManagerMapper.toModel(entity);
        resourceJobManager.setJobManagerCommands(getJobManagerCommand(resourceJobManagerId));
        resourceJobManager.setParallelismPrefix(getParallelismPrefix(resourceJobManagerId));
        return resourceJobManager;
    }

    public void deleteResourceJobManager(String resourceJobManagerId) throws AppCatalogException {
        resourceJobManagerRepository.deleteById(resourceJobManagerId);
    }

    public String addJobSubmissionProtocol(String computeResourceId, JobSubmissionInterface jobSubmissionInterface)
            throws AppCatalogException {
        return addJobSubmission(computeResourceId, jobSubmissionInterface);
    }

    public String addLocalJobSubmission(LOCALSubmission localSubmission) throws AppCatalogException {
        localSubmission.setJobSubmissionInterfaceId(AppCatalogUtils.getID("LOCAL"));
        String resourceJobManagerId = addResourceJobManager(localSubmission.getResourceJobManager());

        ResourceJobManagerEntity resourceJobManagerEntity = resourceJobManagerRepository
                .findById(resourceJobManagerId)
                .orElseThrow(() -> new AppCatalogException("ResourceJobManager not found: " + resourceJobManagerId));

        LocalSubmissionEntity localSubmissionEntity = localSubmissionMapper.toEntity(localSubmission);
        localSubmissionEntity.setResourceJobManagerId(resourceJobManagerId);
        localSubmissionEntity.setResourceJobManager(resourceJobManagerEntity);
        // Ensure updateTime is set
        if (localSubmissionEntity.getUpdateTime() == null) {
            localSubmissionEntity.setUpdateTime(AiravataUtils.getCurrentTimestamp());
        }
        // Ensure creationTime is set
        if (localSubmissionEntity.getCreationTime() == null) {
            localSubmissionEntity.setCreationTime(AiravataUtils.getCurrentTimestamp());
        }
        // Ensure resourceJobManager updateTime is preserved (cascade MERGE might reset it)
        if (resourceJobManagerEntity.getUpdateTime() == null) {
            resourceJobManagerEntity.setUpdateTime(AiravataUtils.getCurrentTimestamp());
        }

        if (localSubmission.getResourceJobManager().getParallelismPrefix() != null) {
            createParallesimPrefix(
                    localSubmission.getResourceJobManager().getParallelismPrefix(),
                    localSubmissionEntity.getResourceJobManager());
        }
        if (localSubmission.getResourceJobManager().getJobManagerCommands() != null) {
            createJobManagerCommand(
                    localSubmission.getResourceJobManager().getJobManagerCommands(),
                    localSubmissionEntity.getResourceJobManager());
        }

        localSubmissionEntity.setSecurityProtocol(localSubmission.getSecurityProtocol());
        localSubmissionRepository.save(localSubmissionEntity);
        return localSubmissionEntity.getJobSubmissionInterfaceId();
    }

    public void updateLocalJobSubmission(LOCALSubmission localSubmission) throws AppCatalogException {
        LocalSubmissionEntity existingEntity = localSubmissionRepository
                .findById(localSubmission.getJobSubmissionInterfaceId())
                .orElse(null);
        LocalSubmissionEntity localSubmissionEntity;
        if (existingEntity != null) {
            // Update existing entity - MapStruct doesn't support in-place updates, so we create new and copy ID
            LocalSubmissionEntity updated = localSubmissionMapper.toEntity(localSubmission);
            updated.setJobSubmissionInterfaceId(existingEntity.getJobSubmissionInterfaceId());
            localSubmissionEntity = updated;
        } else {
            localSubmissionEntity = localSubmissionMapper.toEntity(localSubmission);
        }
        localSubmissionEntity.setUpdateTime(AiravataUtils.getCurrentTimestamp());
        localSubmissionRepository.save(localSubmissionEntity);
    }

    public String addGlobusJobSubmission(GlobusJobSubmission globusJobSubmission) throws AppCatalogException {
        return null;
    }

    public String addUNICOREJobSubmission(UnicoreJobSubmission unicoreJobSubmission) throws AppCatalogException {
        unicoreJobSubmission.setJobSubmissionInterfaceId(AppCatalogUtils.getID("UNICORE"));
        UnicoreSubmissionEntity unicoreSubmissionEntity = unicoreJobSubmissionMapper.toEntity(unicoreJobSubmission);
        if (unicoreJobSubmission.getSecurityProtocol() != null) {
            unicoreSubmissionEntity.setSecurityProtocol(unicoreJobSubmission.getSecurityProtocol());
        }
        unicoreSubmissionRepository.save(unicoreSubmissionEntity);
        return unicoreJobSubmission.getJobSubmissionInterfaceId();
    }

    public void updateUNICOREJobSubmission(UnicoreJobSubmission unicoreJobSubmission) throws AppCatalogException {
        UnicoreSubmissionEntity existingEntity = unicoreSubmissionRepository
                .findById(unicoreJobSubmission.getJobSubmissionInterfaceId())
                .orElse(null);
        UnicoreSubmissionEntity unicoreSubmissionEntity;
        if (existingEntity != null) {
            // Update existing entity - MapStruct doesn't support in-place updates, so we create new and copy ID
            UnicoreSubmissionEntity updated = unicoreJobSubmissionMapper.toEntity(unicoreJobSubmission);
            updated.setJobSubmissionInterfaceId(existingEntity.getJobSubmissionInterfaceId());
            unicoreSubmissionEntity = updated;
        } else {
            unicoreSubmissionEntity = unicoreJobSubmissionMapper.toEntity(unicoreJobSubmission);
        }

        if (unicoreJobSubmission.getSecurityProtocol() != null) {
            unicoreSubmissionEntity.setSecurityProtocol(unicoreJobSubmission.getSecurityProtocol());
        }
        unicoreSubmissionRepository.save(unicoreSubmissionEntity);
    }

    public String addLocalDataMovement(LOCALDataMovement localDataMovement) throws AppCatalogException {
        localDataMovement.setDataMovementInterfaceId(AppCatalogUtils.getID("LOCAL"));
        LocalDataMovementEntity localDataMovementEntity = localDataMovementMapper.toEntity(localDataMovement);
        localDataMovementRepository.save(localDataMovementEntity);
        return localDataMovementEntity.getDataMovementInterfaceId();
    }

    public void updateLocalDataMovement(LOCALDataMovement localDataMovement) throws AppCatalogException {
        LocalDataMovementEntity existingEntity = localDataMovementRepository
                .findById(localDataMovement.getDataMovementInterfaceId())
                .orElse(null);
        LocalDataMovementEntity localDataMovementEntity;
        if (existingEntity != null) {
            // Update existing entity - MapStruct doesn't support in-place updates, so we create new and copy ID
            LocalDataMovementEntity updated = localDataMovementMapper.toEntity(localDataMovement);
            updated.setDataMovementInterfaceId(existingEntity.getDataMovementInterfaceId());
            localDataMovementEntity = updated;
        } else {
            localDataMovementEntity = localDataMovementMapper.toEntity(localDataMovement);
        }
        localDataMovementRepository.save(localDataMovementEntity);
    }

    public String addScpDataMovement(SCPDataMovement scpDataMovement) throws AppCatalogException {
        scpDataMovement.setDataMovementInterfaceId(AppCatalogUtils.getID("SCP"));
        ScpDataMovementEntity scpDataMovementEntity = scpDataMovementMapper.toEntity(scpDataMovement);
        // Ensure creationTime and updateTime are set
        if (scpDataMovementEntity.getCreationTime() == null) {
            scpDataMovementEntity.setCreationTime(AiravataUtils.getCurrentTimestamp());
        }
        if (scpDataMovementEntity.getUpdateTime() == null) {
            scpDataMovementEntity.setUpdateTime(AiravataUtils.getCurrentTimestamp());
        }
        scpDataMovementRepository.save(scpDataMovementEntity);
        return scpDataMovementEntity.getDataMovementInterfaceId();
    }

    public void updateScpDataMovement(SCPDataMovement scpDataMovement) throws AppCatalogException {
        ScpDataMovementEntity existingEntity = scpDataMovementRepository
                .findById(scpDataMovement.getDataMovementInterfaceId())
                .orElse(null);
        ScpDataMovementEntity scpDataMovementEntity;
        if (existingEntity != null) {
            // Update existing entity - MapStruct doesn't support in-place updates, so we create new and copy ID
            ScpDataMovementEntity updated = scpDataMovementMapper.toEntity(scpDataMovement);
            updated.setDataMovementInterfaceId(existingEntity.getDataMovementInterfaceId());
            scpDataMovementEntity = updated;
        } else {
            scpDataMovementEntity = scpDataMovementMapper.toEntity(scpDataMovement);
        }
        scpDataMovementEntity.setUpdateTime(AiravataUtils.getCurrentTimestamp());
        scpDataMovementRepository.save(scpDataMovementEntity);
    }

    public String addUnicoreDataMovement(UnicoreDataMovement unicoreDataMovement) throws AppCatalogException {
        unicoreDataMovement.setDataMovementInterfaceId(AppCatalogUtils.getID("UNICORE"));
        UnicoreDatamovementEntity unicoreDatamovementEntity = unicoreDataMovementMapper.toEntity(unicoreDataMovement);
        unicoreDatamovementRepository.save(unicoreDatamovementEntity);
        return unicoreDatamovementEntity.getDataMovementInterfaceId();
    }

    public String addDataMovementProtocol(String resourceId, DMType dmType, DataMovementInterface dataMovementInterface)
            throws AppCatalogException {
        return addDataMovementProtocol(resourceId, dataMovementInterface);
    }

    public String addGridFTPDataMovement(GridFTPDataMovement gridFTPDataMovement) throws AppCatalogException {
        gridFTPDataMovement.setDataMovementInterfaceId(AppCatalogUtils.getID("GRIDFTP"));
        GridftpDataMovementEntity gridftpDataMovementEntity = gridFTPDataMovementMapper.toEntity(gridFTPDataMovement);
        // Ensure creationTime and updateTime are set
        if (gridftpDataMovementEntity.getCreationTime() == null) {
            gridftpDataMovementEntity.setCreationTime(AiravataUtils.getCurrentTimestamp());
        }
        if (gridftpDataMovementEntity.getUpdateTime() == null) {
            gridftpDataMovementEntity.setUpdateTime(AiravataUtils.getCurrentTimestamp());
        }
        gridftpDataMovementRepository.save(gridftpDataMovementEntity);
        List<String> gridFTPEndPoint = gridFTPDataMovement.getGridFTPEndPoints();
        if (gridFTPEndPoint != null && !gridFTPEndPoint.isEmpty()) {
            for (String endpoint : gridFTPEndPoint) {
                GridftpEndpointEntity gridftpEndpointEntity = new GridftpEndpointEntity();
                gridftpEndpointEntity.setGridftpDataMovement(gridftpDataMovementEntity);
                gridftpEndpointEntity.setDataMovementInterfaceId(gridFTPDataMovement.getDataMovementInterfaceId());
                gridftpEndpointEntity.setEndpoint(endpoint);
                // Ensure creationTime and updateTime are set
                if (gridftpEndpointEntity.getCreationTime() == null) {
                    gridftpEndpointEntity.setCreationTime(AiravataUtils.getCurrentTimestamp());
                }
                if (gridftpEndpointEntity.getUpdateTime() == null) {
                    gridftpEndpointEntity.setUpdateTime(AiravataUtils.getCurrentTimestamp());
                }
                gridftpEndpointRepository.save(gridftpEndpointEntity);
            }
        }
        return gridftpDataMovementEntity.getDataMovementInterfaceId();
    }

    public SSHJobSubmission getSSHJobSubmission(String submissionId) throws AppCatalogException {
        SshJobSubmissionEntity entity =
                sshJobSubmissionRepository.findById(submissionId).orElse(null);
        if (entity == null) return null;
        SSHJobSubmission sshJobSubmission = sshJobSubmissionMapper.toModel(entity);
        sshJobSubmission
                .getResourceJobManager()
                .setParallelismPrefix(getParallelismPrefix(
                        sshJobSubmission.getResourceJobManager().getResourceJobManagerId()));
        sshJobSubmission
                .getResourceJobManager()
                .setJobManagerCommands(getJobManagerCommand(
                        sshJobSubmission.getResourceJobManager().getResourceJobManagerId()));
        return sshJobSubmission;
    }

    public UnicoreJobSubmission getUNICOREJobSubmission(String submissionId) throws AppCatalogException {
        UnicoreSubmissionEntity entity =
                unicoreSubmissionRepository.findById(submissionId).orElse(null);
        if (entity == null) return null;
        return unicoreJobSubmissionMapper.toModel(entity);
    }

    public UnicoreDataMovement getUNICOREDataMovement(String dataMovementId) throws AppCatalogException {
        UnicoreDatamovementEntity entity =
                unicoreDatamovementRepository.findById(dataMovementId).orElse(null);
        if (entity == null) return null;
        return unicoreDataMovementMapper.toModel(entity);
    }

    public CloudJobSubmission getCloudJobSubmission(String submissionId) throws AppCatalogException {
        CloudJobSubmissionEntity entity =
                cloudJobSubmissionRepository.findById(submissionId).orElse(null);
        if (entity == null) return null;
        return cloudJobSubmissionMapper.toModel(entity);
    }

    public SCPDataMovement getSCPDataMovement(String dataMoveId) throws AppCatalogException {
        ScpDataMovementEntity entity =
                scpDataMovementRepository.findById(dataMoveId).orElse(null);
        if (entity == null) return null;
        return scpDataMovementMapper.toModel(entity);
    }

    public GridFTPDataMovement getGridFTPDataMovement(String dataMoveId) throws AppCatalogException {
        GridftpDataMovementEntity entity =
                gridftpDataMovementRepository.findById(dataMoveId).orElse(null);
        if (entity == null) {
            return null;
        }

        List<GridftpEndpointEntity> endpointEntities =
                gridftpEndpointRepository.findByDataMovementInterfaceId(entity.getDataMovementInterfaceId());

        List<String> endpoints = endpointEntities.stream()
                .map(GridftpEndpointEntity::getEndpoint)
                .collect(Collectors.toList());
        GridFTPDataMovement dataMovement = gridFTPDataMovementMapper.toModel(entity);
        dataMovement.setGridFTPEndPoints(endpoints);

        return dataMovement;
    }

    public void removeJobSubmissionInterface(String computeResourceId, String jobSubmissionInterfaceId)
            throws AppCatalogException {
        ComputeResourceEntity entity =
                computeResourceRepository.findById(computeResourceId).orElse(null);
        if (entity != null && entity.getJobSubmissionInterfaces() != null) {
            entity.getJobSubmissionInterfaces()
                    .removeIf(iface -> iface.getJobSubmissionInterfaceId().equals(jobSubmissionInterfaceId));
            computeResourceRepository.save(entity);
        }
    }

    public void removeDataMovementInterface(String computeResourceId, String dataMovementInterfaceId)
            throws AppCatalogException {
        ComputeResourceEntity entity =
                computeResourceRepository.findById(computeResourceId).orElse(null);
        if (entity != null && entity.getDataMovementInterfaces() != null) {
            entity.getDataMovementInterfaces()
                    .removeIf(iface -> iface.getDataMovementInterfaceId().equals(dataMovementInterfaceId));
            computeResourceRepository.save(entity);
        }
    }

    public void removeBatchQueue(String computeResourceId, String queueName) throws AppCatalogException {
        ComputeResourceEntity entity =
                computeResourceRepository.findById(computeResourceId).orElse(null);
        if (entity != null && entity.getBatchQueues() != null) {
            entity.getBatchQueues().removeIf(queue -> queue.getQueueName().equals(queueName));
            computeResourceRepository.save(entity);
        }
    }

    public LOCALSubmission getLocalJobSubmission(String submissionId) throws AppCatalogException {
        LocalSubmissionEntity entity =
                localSubmissionRepository.findById(submissionId).orElse(null);
        if (entity == null) return null;
        LOCALSubmission localSubmission = localSubmissionMapper.toModel(entity);
        localSubmission
                .getResourceJobManager()
                .setParallelismPrefix(getParallelismPrefix(
                        localSubmission.getResourceJobManager().getResourceJobManagerId()));
        localSubmission
                .getResourceJobManager()
                .setJobManagerCommands(getJobManagerCommand(
                        localSubmission.getResourceJobManager().getResourceJobManagerId()));
        return localSubmission;
    }

    public LOCALDataMovement getLocalDataMovement(String datamovementId) throws AppCatalogException {
        LocalDataMovementEntity entity =
                localDataMovementRepository.findById(datamovementId).orElse(null);
        if (entity == null) return null;
        return localDataMovementMapper.toModel(entity);
    }

    // Helper methods for ResourceJobManager operations
    private void createParallesimPrefix(
            Map<ApplicationParallelismType, String> parallelismPrefix,
            ResourceJobManagerEntity resourceJobManagerEntity) {
        if (parallelismPrefix == null || parallelismPrefix.isEmpty()) {
            return;
        }
        String resourceJobManagerId = resourceJobManagerEntity.getResourceJobManagerId();
        for (Map.Entry<ApplicationParallelismType, String> entry : parallelismPrefix.entrySet()) {
            ParallelismCommandPK pk = new ParallelismCommandPK();
            pk.setResourceJobManagerId(resourceJobManagerId);
            pk.setCommandType(entry.getKey());

            ParallelismCommandEntity entity =
                    parallelismCommandRepository.findById(pk).orElse(new ParallelismCommandEntity());

            entity.setResourceJobManagerId(resourceJobManagerId);
            entity.setCommandType(entry.getKey());
            entity.setCommand(entry.getValue());
            // entity.setResourceJobManager(resourceJobManagerEntity); // Avoid setting relationship to prevent
            // unmanaged object exception
            parallelismCommandRepository.save(entity);
        }
    }

    private void createJobManagerCommand(
            Map<JobManagerCommand, String> jobManagerCommands, ResourceJobManagerEntity resourceJobManagerEntity) {
        if (jobManagerCommands == null || jobManagerCommands.isEmpty()) {
            return;
        }
        String resourceJobManagerId = resourceJobManagerEntity.getResourceJobManagerId();
        for (Map.Entry<JobManagerCommand, String> entry : jobManagerCommands.entrySet()) {
            JobManagerCommandPK pk = new JobManagerCommandPK();
            pk.setResourceJobManagerId(resourceJobManagerId);
            pk.setCommandType(entry.getKey());

            JobManagerCommandEntity entity =
                    jobManagerCommandRepository.findById(pk).orElse(new JobManagerCommandEntity());

            entity.setResourceJobManagerId(resourceJobManagerId);
            entity.setCommandType(entry.getKey());
            entity.setCommand(entry.getValue());
            // entity.setResourceJobManager(resourceJobManagerEntity); // Avoid setting relationship to prevent
            // unmanaged object exception
            jobManagerCommandRepository.save(entity);
        }
    }

    private Map<ApplicationParallelismType, String> getParallelismPrefix(String resourceJobManagerId) {
        List<ParallelismCommandEntity> entities =
                resourceJobManagerRepository.findParallelismCommandsByResourceJobManagerId(resourceJobManagerId);
        Map<ApplicationParallelismType, String> result = new HashMap<>();
        for (ParallelismCommandEntity entity : entities) {
            result.put(entity.getCommandType(), entity.getCommand());
        }
        return result;
    }

    private Map<JobManagerCommand, String> getJobManagerCommand(String resourceJobManagerId) {
        List<JobManagerCommandEntity> entities =
                resourceJobManagerRepository.findJobManagerCommandsByResourceJobManagerId(resourceJobManagerId);
        Map<JobManagerCommand, String> result = new HashMap<>();
        for (JobManagerCommandEntity entity : entities) {
            result.put(entity.getCommandType(), entity.getCommand());
        }
        return result;
    }

    private String addJobSubmission(String computeResourceId, JobSubmissionInterface jobSubmissionInterface)
            throws AppCatalogException {
        JobSubmissionInterfaceEntity entity = jobSubmissionInterfaceMapper.toEntity(jobSubmissionInterface);
        entity.setComputeResourceId(computeResourceId);
        // Ensure creationTime and updateTime are set
        if (entity.getCreationTime() == null) {
            entity.setCreationTime(AiravataUtils.getCurrentTimestamp());
        }
        if (entity.getUpdateTime() == null) {
            entity.setUpdateTime(AiravataUtils.getCurrentTimestamp());
        }
        JobSubmissionInterfaceEntity saved = jobSubmissionInterfaceRepository.save(entity);
        return saved.getJobSubmissionInterfaceId();
    }

    private String addDataMovementProtocol(String resourceId, DataMovementInterface dataMovementInterface)
            throws AppCatalogException {
        DataMovementInterfaceEntity entity =
                computeResourceDataMovementInterfaceBaseMapper.toEntity(dataMovementInterface);
        entity.setComputeResourceId(resourceId);
        // Ensure creationTime and updateTime are set
        if (entity.getCreationTime() == null) {
            entity.setCreationTime(AiravataUtils.getCurrentTimestamp());
        }
        if (entity.getUpdateTime() == null) {
            entity.setUpdateTime(AiravataUtils.getCurrentTimestamp());
        }
        DataMovementInterfaceEntity saved = dataMovementRepository.save(entity);
        return saved.getDataMovementInterfaceId();
    }
}
