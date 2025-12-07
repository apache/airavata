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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.model.appcatalog.computeresource.CloudJobSubmission;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.FileSystems;
import org.apache.airavata.model.appcatalog.computeresource.GlobusJobSubmission;
import org.apache.airavata.model.appcatalog.computeresource.JobManagerCommand;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.LOCALSubmission;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManager;
import org.apache.airavata.model.appcatalog.computeresource.SSHJobSubmission;
import org.apache.airavata.model.appcatalog.computeresource.UnicoreJobSubmission;
import org.apache.airavata.model.commons.airavata_commonsConstants;
import org.apache.airavata.model.data.movement.DMType;
import org.apache.airavata.model.data.movement.DataMovementInterface;
import org.apache.airavata.model.data.movement.GridFTPDataMovement;
import org.apache.airavata.model.data.movement.LOCALDataMovement;
import org.apache.airavata.model.data.movement.SCPDataMovement;
import org.apache.airavata.model.data.movement.UnicoreDataMovement;
import org.apache.airavata.model.parallelism.ApplicationParallelismType;
import org.apache.airavata.registry.entities.appcatalog.BatchQueuePK;
import org.apache.airavata.registry.entities.appcatalog.CloudJobSubmissionEntity;
import org.apache.airavata.registry.entities.appcatalog.ComputeResourceEntity;
import org.apache.airavata.registry.entities.appcatalog.ComputeResourceFileSystemEntity;
import org.apache.airavata.registry.entities.appcatalog.DataMovementInterfaceEntity;
import org.apache.airavata.registry.entities.appcatalog.DataMovementInterfacePK;
import org.apache.airavata.registry.entities.appcatalog.GridftpDataMovementEntity;
import org.apache.airavata.registry.entities.appcatalog.GridftpEndpointEntity;
import org.apache.airavata.registry.entities.appcatalog.JobManagerCommandEntity;
import org.apache.airavata.registry.entities.appcatalog.JobSubmissionInterfaceEntity;
import org.apache.airavata.registry.entities.appcatalog.JobSubmissionInterfacePK;
import org.apache.airavata.registry.entities.appcatalog.LocalDataMovementEntity;
import org.apache.airavata.registry.entities.appcatalog.LocalSubmissionEntity;
import org.apache.airavata.registry.entities.appcatalog.ParallelismCommandEntity;
import org.apache.airavata.registry.entities.appcatalog.ResourceJobManagerEntity;
import org.apache.airavata.registry.entities.appcatalog.ScpDataMovementEntity;
import org.apache.airavata.registry.entities.appcatalog.SshJobSubmissionEntity;
import org.apache.airavata.registry.entities.appcatalog.UnicoreDatamovementEntity;
import org.apache.airavata.registry.entities.appcatalog.UnicoreSubmissionEntity;
import org.apache.airavata.registry.exceptions.AppCatalogException;
import org.apache.airavata.registry.repositories.appcatalog.BatchQueueRepository;
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
import org.apache.airavata.registry.utils.ObjectMapperSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ComputeResourceService {
    private static final Logger logger = LoggerFactory.getLogger(ComputeResourceService.class);

    @Autowired
    private ComputeResourceRepository computeResourceRepository;

    @Autowired
    private ComputeResourceFileSystemRepository computeResourceFileSystemRepository;

    @Autowired
    private ResourceJobManagerRepository resourceJobManagerRepository;

    @Autowired
    private JobManagerCommandRepository jobManagerCommandRepository;

    @Autowired
    private ParallelismCommandRepository parallelismCommandRepository;

    @Autowired
    private JobSubmissionInterfaceRepository jobSubmissionInterfaceRepository;

    @Autowired
    private SshJobSubmissionRepository sshJobSubmissionRepository;

    @Autowired
    private CloudJobSubmissionRepository cloudJobSubmissionRepository;

    @Autowired
    private LocalSubmissionRepository localSubmissionRepository;

    @Autowired
    private UnicoreSubmissionRepository unicoreSubmissionRepository;

    @Autowired
    private DataMovementRepository dataMovementRepository;

    @Autowired
    private LocalDataMovementRepository localDataMovementRepository;

    @Autowired
    private ScpDataMovementRepository scpDataMovementRepository;

    @Autowired
    private GridftpDataMovementRepository gridftpDataMovementRepository;

    @Autowired
    private GridftpEndpointRepository gridftpEndpointRepository;

    @Autowired
    private UnicoreDatamovementRepository unicoreDatamovementRepository;

    @Autowired
    private BatchQueueRepository batchQueueRepository;

    @PersistenceContext(unitName = "appcatalog_data_new")
    private EntityManager entityManager;

    public String addComputeResource(ComputeResourceDescription description) throws AppCatalogException {
        if (description.getComputeResourceId().equals("")
                || description.getComputeResourceId().equals(airavata_commonsConstants.DEFAULT_ID)) {
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
        Mapper mapper = ObjectMapperSingleton.getInstance();
        ComputeResourceEntity computeResourceEntity = mapper.map(description, ComputeResourceEntity.class);
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
            computeResourceEntity
                    .getJobSubmissionInterfaces()
                    .forEach(jobSubmissionInterfaceEntity ->
                            jobSubmissionInterfaceEntity.setComputeResourceId(computeResourceId));
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

    private Map<FileSystems, String> getFileSystems(String computeResourceId) {
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
        Mapper mapper = ObjectMapperSingleton.getInstance();
        ComputeResourceDescription computeResourceDescription = mapper.map(entity, ComputeResourceDescription.class);
        computeResourceDescription.setFileSystems(getFileSystems(resourceId));
        return computeResourceDescription;
    }

    public List<ComputeResourceDescription> getComputeResourceList(Map<String, String> filters)
            throws AppCatalogException {
        if (filters.containsKey(DBConstants.ComputeResource.HOST_NAME)) {
            String hostName = "%" + filters.get(DBConstants.ComputeResource.HOST_NAME) + "%";
            List<ComputeResourceEntity> entities = computeResourceRepository.findByHostName(hostName);
            Mapper mapper = ObjectMapperSingleton.getInstance();
            List<ComputeResourceDescription> result = entities.stream()
                    .map(e -> {
                        ComputeResourceDescription desc = mapper.map(e, ComputeResourceDescription.class);
                        desc.setFileSystems(getFileSystems(desc.getComputeResourceId()));
                        return desc;
                    })
                    .collect(Collectors.toList());
            return result;
        } else {
            logger.error("Unsupported field name for compute resource.", new IllegalArgumentException());
            throw new IllegalArgumentException("Unsupported field name for compute resource.");
        }
    }

    public List<ComputeResourceDescription> getAllComputeResourceList() throws AppCatalogException {
        List<ComputeResourceEntity> entities = computeResourceRepository.findAll();
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return entities.stream()
                .map(e -> {
                    ComputeResourceDescription desc = mapper.map(e, ComputeResourceDescription.class);
                    desc.setFileSystems(getFileSystems(desc.getComputeResourceId()));
                    return desc;
                })
                .collect(Collectors.toList());
    }

    public Map<String, String> getAllComputeResourceIdList() throws AppCatalogException {
        List<ComputeResourceEntity> entities = computeResourceRepository.findAll();
        Map<String, String> computeResourceMap = new HashMap<>();
        Mapper mapper = ObjectMapperSingleton.getInstance();
        for (ComputeResourceEntity entity : entities) {
            ComputeResourceDescription desc = mapper.map(entity, ComputeResourceDescription.class);
            computeResourceMap.put(desc.getComputeResourceId(), desc.getHostName());
        }
        return computeResourceMap;
    }

    public Map<String, String> getAvailableComputeResourceIdList() throws AppCatalogException {
        List<ComputeResourceEntity> entities = computeResourceRepository.findAll();
        Map<String, String> computeResourceMap = new HashMap<>();
        Mapper mapper = ObjectMapperSingleton.getInstance();
        for (ComputeResourceEntity entity : entities) {
            ComputeResourceDescription desc = mapper.map(entity, ComputeResourceDescription.class);
            if (desc.isEnabled()) {
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
        Mapper mapper = ObjectMapperSingleton.getInstance();
        SshJobSubmissionEntity sshJobSubmissionEntity = mapper.map(sshJobSubmission, SshJobSubmissionEntity.class);
        sshJobSubmissionEntity.getResourceJobManager().setResourceJobManagerId(resourceJobManagerId);
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
        Mapper mapper = ObjectMapperSingleton.getInstance();
        SshJobSubmissionEntity sshJobSubmissionEntity = mapper.map(sshJobSubmission, SshJobSubmissionEntity.class);
        sshJobSubmissionEntity.setUpdateTime(AiravataUtils.getCurrentTimestamp());
        sshJobSubmissionRepository.save(sshJobSubmissionEntity);
    }

    public String addCloudJobSubmission(CloudJobSubmission cloudJobSubmission) throws AppCatalogException {
        cloudJobSubmission.setJobSubmissionInterfaceId(AppCatalogUtils.getID("Cloud"));
        Mapper mapper = ObjectMapperSingleton.getInstance();
        CloudJobSubmissionEntity cloudJobSubmissionEntity =
                mapper.map(cloudJobSubmission, CloudJobSubmissionEntity.class);
        cloudJobSubmissionRepository.save(cloudJobSubmissionEntity);
        return cloudJobSubmissionEntity.getJobSubmissionInterfaceId();
    }

    public void updateCloudJobSubmission(CloudJobSubmission cloudJobSubmission) throws AppCatalogException {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        CloudJobSubmissionEntity cloudJobSubmissionEntity =
                mapper.map(cloudJobSubmission, CloudJobSubmissionEntity.class);
        cloudJobSubmissionRepository.save(cloudJobSubmissionEntity);
    }

    public String addResourceJobManager(ResourceJobManager resourceJobManager) throws AppCatalogException {
        resourceJobManager.setResourceJobManagerId(AppCatalogUtils.getID("RJM"));
        Mapper mapper = ObjectMapperSingleton.getInstance();
        ResourceJobManagerEntity resourceJobManagerEntity =
                mapper.map(resourceJobManager, ResourceJobManagerEntity.class);
        resourceJobManagerEntity = resourceJobManagerRepository.save(resourceJobManagerEntity);
        Map<JobManagerCommand, String> jobManagerCommands = resourceJobManager.getJobManagerCommands();
        if (jobManagerCommands != null && jobManagerCommands.size() != 0) {
            createJobManagerCommand(jobManagerCommands, resourceJobManagerEntity);
        }

        Map<ApplicationParallelismType, String> parallelismPrefix = resourceJobManager.getParallelismPrefix();
        if (parallelismPrefix != null && parallelismPrefix.size() != 0) {
            createParallesimPrefix(parallelismPrefix, resourceJobManagerEntity);
        }
        return resourceJobManager.getResourceJobManagerId();
    }

    public void updateResourceJobManager(String resourceJobManagerId, ResourceJobManager updatedResourceJobManager)
            throws AppCatalogException {
        updatedResourceJobManager.setResourceJobManagerId(resourceJobManagerId);
        Mapper mapper = ObjectMapperSingleton.getInstance();
        ResourceJobManagerEntity resourceJobManagerEntity =
                mapper.map(updatedResourceJobManager, ResourceJobManagerEntity.class);
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
        Mapper mapper = ObjectMapperSingleton.getInstance();
        ResourceJobManager resourceJobManager = mapper.map(entity, ResourceJobManager.class);
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
        Mapper mapper = ObjectMapperSingleton.getInstance();
        LocalSubmissionEntity localSubmissionEntity = mapper.map(localSubmission, LocalSubmissionEntity.class);
        localSubmissionEntity.setResourceJobManagerId(resourceJobManagerId);
        localSubmissionEntity.getResourceJobManager().setResourceJobManagerId(resourceJobManagerId);
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
        Mapper mapper = ObjectMapperSingleton.getInstance();
        LocalSubmissionEntity localSubmissionEntity = mapper.map(localSubmission, LocalSubmissionEntity.class);
        localSubmissionEntity.setUpdateTime(AiravataUtils.getCurrentTimestamp());
        localSubmissionRepository.save(localSubmissionEntity);
    }

    public String addGlobusJobSubmission(GlobusJobSubmission globusJobSubmission) throws AppCatalogException {
        return null;
    }

    public String addUNICOREJobSubmission(UnicoreJobSubmission unicoreJobSubmission) throws AppCatalogException {
        unicoreJobSubmission.setJobSubmissionInterfaceId(AppCatalogUtils.getID("UNICORE"));
        Mapper mapper = ObjectMapperSingleton.getInstance();
        UnicoreSubmissionEntity unicoreSubmissionEntity =
                mapper.map(unicoreJobSubmission, UnicoreSubmissionEntity.class);
        if (unicoreJobSubmission.getSecurityProtocol() != null) {
            unicoreSubmissionEntity.setSecurityProtocol(unicoreJobSubmission.getSecurityProtocol());
        }
        unicoreSubmissionRepository.save(unicoreSubmissionEntity);
        return unicoreJobSubmission.getJobSubmissionInterfaceId();
    }

    public void updateUNICOREJobSubmission(UnicoreJobSubmission unicoreJobSubmission) throws AppCatalogException {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        UnicoreSubmissionEntity unicoreSubmissionEntity =
                mapper.map(unicoreJobSubmission, UnicoreSubmissionEntity.class);
        if (unicoreJobSubmission.getSecurityProtocol() != null) {
            unicoreSubmissionEntity.setSecurityProtocol(unicoreJobSubmission.getSecurityProtocol());
        }
        unicoreSubmissionRepository.save(unicoreSubmissionEntity);
    }

    public String addLocalDataMovement(LOCALDataMovement localDataMovement) throws AppCatalogException {
        localDataMovement.setDataMovementInterfaceId(AppCatalogUtils.getID("LOCAL"));
        Mapper mapper = ObjectMapperSingleton.getInstance();
        LocalDataMovementEntity localDataMovementEntity = mapper.map(localDataMovement, LocalDataMovementEntity.class);
        localDataMovementRepository.save(localDataMovementEntity);
        return localDataMovementEntity.getDataMovementInterfaceId();
    }

    public void updateLocalDataMovement(LOCALDataMovement localDataMovement) throws AppCatalogException {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        LocalDataMovementEntity localDataMovementEntity = mapper.map(localDataMovement, LocalDataMovementEntity.class);
        localDataMovementRepository.save(localDataMovementEntity);
    }

    public String addScpDataMovement(SCPDataMovement scpDataMovement) throws AppCatalogException {
        scpDataMovement.setDataMovementInterfaceId(AppCatalogUtils.getID("SCP"));
        Mapper mapper = ObjectMapperSingleton.getInstance();
        ScpDataMovementEntity scpDataMovementEntity = mapper.map(scpDataMovement, ScpDataMovementEntity.class);
        scpDataMovementRepository.save(scpDataMovementEntity);
        return scpDataMovementEntity.getDataMovementInterfaceId();
    }

    public void updateScpDataMovement(SCPDataMovement scpDataMovement) throws AppCatalogException {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        ScpDataMovementEntity scpDataMovementEntity = mapper.map(scpDataMovement, ScpDataMovementEntity.class);
        scpDataMovementEntity.setUpdateTime(AiravataUtils.getCurrentTimestamp());
        scpDataMovementRepository.save(scpDataMovementEntity);
    }

    public String addUnicoreDataMovement(UnicoreDataMovement unicoreDataMovement) throws AppCatalogException {
        unicoreDataMovement.setDataMovementInterfaceId(AppCatalogUtils.getID("UNICORE"));
        Mapper mapper = ObjectMapperSingleton.getInstance();
        UnicoreDatamovementEntity unicoreDatamovementEntity =
                mapper.map(unicoreDataMovement, UnicoreDatamovementEntity.class);
        unicoreDatamovementRepository.save(unicoreDatamovementEntity);
        return unicoreDatamovementEntity.getDataMovementInterfaceId();
    }

    public String addDataMovementProtocol(String resourceId, DMType dmType, DataMovementInterface dataMovementInterface)
            throws AppCatalogException {
        return addDataMovementProtocol(resourceId, dataMovementInterface);
    }

    public String addGridFTPDataMovement(GridFTPDataMovement gridFTPDataMovement) throws AppCatalogException {
        gridFTPDataMovement.setDataMovementInterfaceId(AppCatalogUtils.getID("GRIDFTP"));
        Mapper mapper = ObjectMapperSingleton.getInstance();
        GridftpDataMovementEntity gridftpDataMovementEntity =
                mapper.map(gridFTPDataMovement, GridftpDataMovementEntity.class);
        gridftpDataMovementRepository.save(gridftpDataMovementEntity);
        List<String> gridFTPEndPoint = gridFTPDataMovement.getGridFTPEndPoints();
        if (gridFTPEndPoint != null && !gridFTPEndPoint.isEmpty()) {
            for (String endpoint : gridFTPEndPoint) {
                GridftpEndpointEntity gridftpEndpointEntity = new GridftpEndpointEntity();
                gridftpEndpointEntity.setGridftpDataMovement(gridftpDataMovementEntity);
                gridftpEndpointEntity.setDataMovementInterfaceId(gridFTPDataMovement.getDataMovementInterfaceId());
                gridftpEndpointEntity.setEndpoint(endpoint);
                gridftpEndpointRepository.save(gridftpEndpointEntity);
            }
        }
        return gridftpDataMovementEntity.getDataMovementInterfaceId();
    }

    public SSHJobSubmission getSSHJobSubmission(String submissionId) throws AppCatalogException {
        SshJobSubmissionEntity entity =
                sshJobSubmissionRepository.findById(submissionId).orElse(null);
        if (entity == null) return null;
        Mapper mapper = ObjectMapperSingleton.getInstance();
        SSHJobSubmission sshJobSubmission = mapper.map(entity, SSHJobSubmission.class);
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
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return mapper.map(entity, UnicoreJobSubmission.class);
    }

    public UnicoreDataMovement getUNICOREDataMovement(String dataMovementId) throws AppCatalogException {
        UnicoreDatamovementEntity entity =
                unicoreDatamovementRepository.findById(dataMovementId).orElse(null);
        if (entity == null) return null;
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return mapper.map(entity, UnicoreDataMovement.class);
    }

    public CloudJobSubmission getCloudJobSubmission(String submissionId) throws AppCatalogException {
        CloudJobSubmissionEntity entity =
                cloudJobSubmissionRepository.findById(submissionId).orElse(null);
        if (entity == null) return null;
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return mapper.map(entity, CloudJobSubmission.class);
    }

    public SCPDataMovement getSCPDataMovement(String dataMoveId) throws AppCatalogException {
        ScpDataMovementEntity entity =
                scpDataMovementRepository.findById(dataMoveId).orElse(null);
        if (entity == null) return null;
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return mapper.map(entity, SCPDataMovement.class);
    }

    public GridFTPDataMovement getGridFTPDataMovement(String dataMoveId) throws AppCatalogException {
        GridftpDataMovementEntity entity =
                gridftpDataMovementRepository.findById(dataMoveId).orElse(null);
        if (entity == null) {
            return null;
        }

        List<GridftpEndpointEntity> endpointEntities =
                gridftpEndpointRepository.findByDataMovementInterfaceId(entity.getDataMovementInterfaceId());

        Mapper mapper = ObjectMapperSingleton.getInstance();

        List<String> endpoints = endpointEntities.stream()
                .map(GridftpEndpointEntity::getEndpoint)
                .collect(Collectors.toList());
        GridFTPDataMovement dataMovement = mapper.map(entity, GridFTPDataMovement.class);
        dataMovement.setGridFTPEndPoints(endpoints);

        return dataMovement;
    }

    public void removeJobSubmissionInterface(String computeResourceId, String jobSubmissionInterfaceId)
            throws AppCatalogException {
        JobSubmissionInterfacePK jobSubmissionInterfacePK = new JobSubmissionInterfacePK();
        jobSubmissionInterfacePK.setComputeResourceId(computeResourceId);
        jobSubmissionInterfacePK.setJobSubmissionInterfaceId(jobSubmissionInterfaceId);
        jobSubmissionInterfaceRepository.deleteById(jobSubmissionInterfacePK);
    }

    public void removeDataMovementInterface(String computeResourceId, String dataMovementInterfaceId)
            throws AppCatalogException {
        DataMovementInterfacePK dataMovementInterfacePK = new DataMovementInterfacePK();
        dataMovementInterfacePK.setDataMovementInterfaceId(dataMovementInterfaceId);
        dataMovementInterfacePK.setComputeResourceId(computeResourceId);
        dataMovementRepository.deleteById(dataMovementInterfacePK);
    }

    public void removeBatchQueue(String computeResourceId, String queueName) throws AppCatalogException {
        BatchQueuePK batchQueuePK = new BatchQueuePK();
        batchQueuePK.setQueueName(queueName);
        batchQueuePK.setComputeResourceId(computeResourceId);
        batchQueueRepository.deleteById(batchQueuePK);
    }

    public LOCALSubmission getLocalJobSubmission(String submissionId) throws AppCatalogException {
        LocalSubmissionEntity entity =
                localSubmissionRepository.findById(submissionId).orElse(null);
        if (entity == null) return null;
        Mapper mapper = ObjectMapperSingleton.getInstance();
        LOCALSubmission localSubmission = mapper.map(entity, LOCALSubmission.class);
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
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return mapper.map(entity, LOCALDataMovement.class);
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
            ParallelismCommandEntity entity = new ParallelismCommandEntity();
            entity.setResourceJobManagerId(resourceJobManagerId);
            entity.setCommandType(entry.getKey());
            entity.setCommand(entry.getValue());
            entity.setResourceJobManager(resourceJobManagerEntity);
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
            JobManagerCommandEntity entity = new JobManagerCommandEntity();
            entity.setResourceJobManagerId(resourceJobManagerId);
            entity.setCommandType(entry.getKey());
            entity.setCommand(entry.getValue());
            entity.setResourceJobManager(resourceJobManagerEntity);
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
        Mapper mapper = ObjectMapperSingleton.getInstance();
        JobSubmissionInterfaceEntity entity = mapper.map(jobSubmissionInterface, JobSubmissionInterfaceEntity.class);
        entity.setComputeResourceId(computeResourceId);
        JobSubmissionInterfaceEntity saved = jobSubmissionInterfaceRepository.save(entity);
        return saved.getJobSubmissionInterfaceId();
    }

    private String addDataMovementProtocol(String resourceId, DataMovementInterface dataMovementInterface)
            throws AppCatalogException {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        DataMovementInterfaceEntity entity = mapper.map(dataMovementInterface, DataMovementInterfaceEntity.class);
        entity.setComputeResourceId(resourceId);
        DataMovementInterfaceEntity saved = dataMovementRepository.save(entity);
        return saved.getDataMovementInterfaceId();
    }
}
