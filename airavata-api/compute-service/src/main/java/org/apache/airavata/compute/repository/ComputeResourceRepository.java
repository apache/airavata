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

import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.compute.mapper.ComputeMapper;
import org.apache.airavata.compute.model.CloudJobSubmissionEntity;
import org.apache.airavata.compute.model.ComputeResourceEntity;
import org.apache.airavata.compute.model.ComputeResourceFileSystemEntity;
import org.apache.airavata.compute.model.LocalSubmissionEntity;
import org.apache.airavata.compute.model.ResourceJobManagerEntity;
import org.apache.airavata.compute.model.SshJobSubmissionEntity;
import org.apache.airavata.compute.model.UnicoreSubmissionEntity;
import org.apache.airavata.db.AbstractRepository;
import org.apache.airavata.db.AppCatalogUtils;
import org.apache.airavata.db.DBConstants;
import org.apache.airavata.db.QueryConstants;
import org.apache.airavata.interfaces.AppCatalogException;
import org.apache.airavata.interfaces.ComputeResource;
import org.apache.airavata.model.appcatalog.computeresource.proto.CloudJobSubmission;
import org.apache.airavata.model.appcatalog.computeresource.proto.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.proto.FileSystems;
import org.apache.airavata.model.appcatalog.computeresource.proto.GlobusJobSubmission;
import org.apache.airavata.model.appcatalog.computeresource.proto.JobManagerCommand;
import org.apache.airavata.model.appcatalog.computeresource.proto.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.proto.LOCALSubmission;
import org.apache.airavata.model.appcatalog.computeresource.proto.ResourceJobManager;
import org.apache.airavata.model.appcatalog.computeresource.proto.SSHJobSubmission;
import org.apache.airavata.model.appcatalog.computeresource.proto.UnicoreJobSubmission;
import org.apache.airavata.model.parallelism.proto.ApplicationParallelismType;
import org.apache.airavata.util.AiravataUtils;
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
        // TODO remove existing one
        ComputeResourceEntity computeResourceEntity = saveComputeResource(description);
        saveFileSystems(description, computeResourceEntity);
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
        if (computeResourceEntity.getJobSubmissionInterfaces() != null) {
            computeResourceEntity
                    .getJobSubmissionInterfaces()
                    .forEach(jobSubmissionInterfaceEntity ->
                            jobSubmissionInterfaceEntity.setComputeResourceId(computeResourceId));
        }
        return execute(entityManager -> entityManager.merge(computeResourceEntity));
    }

    protected void saveFileSystems(ComputeResourceDescription description, ComputeResourceEntity computeHostResource)
            throws AppCatalogException {
        Map<Integer, String> fileSystems = description.getFileSystemsMap();
        if (!fileSystems.isEmpty()) {
            for (Map.Entry<Integer, String> entry : fileSystems.entrySet()) {
                FileSystems key = FileSystems.forNumber(entry.getKey());
                ComputeResourceFileSystemEntity computeResourceFileSystemEntity = new ComputeResourceFileSystemEntity();
                computeResourceFileSystemEntity.setComputeResourceId(computeHostResource.getComputeResourceId());
                computeResourceFileSystemEntity.setFileSystem(key);
                computeResourceFileSystemEntity.setPath(entry.getValue());
                computeResourceFileSystemEntity.setComputeResource(computeHostResource);
                execute(entityManager -> entityManager.merge(computeResourceFileSystemEntity));
            }
        }
    }

    protected Map<Integer, String> getFileSystems(String computeResourceId) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.ComputeResource.COMPUTE_RESOURCE_ID, computeResourceId);

        List resultSet = (List) execute(entityManager -> {
            Query jpaQuery = entityManager.createQuery(QueryConstants.GET_FILE_SYSTEM);
            for (Map.Entry<String, Object> entry : queryParameters.entrySet()) {
                jpaQuery.setParameter(entry.getKey(), entry.getValue());
            }
            return jpaQuery.setFirstResult(0).getResultList();
        });

        List<ComputeResourceFileSystemEntity> computeResourceFileSystemEntityList = resultSet;
        Map<Integer, String> fileSystemsMap = new HashMap<>();
        for (ComputeResourceFileSystemEntity fs : computeResourceFileSystemEntityList) {
            fileSystemsMap.put(fs.getFileSystem().getNumber(), fs.getPath());
        }
        return fileSystemsMap;
    }

    @Override
    public void updateComputeResource(String computeResourceId, ComputeResourceDescription updatedComputeResource)
            throws AppCatalogException {
        saveComputeResourceDescriptorData(updatedComputeResource);
    }

    @Override
    public ComputeResourceDescription getComputeResource(String resourceId) throws AppCatalogException {
        ComputeResourceDescription computeResourceDescription = get(resourceId);
        if (computeResourceDescription != null) {
            computeResourceDescription = computeResourceDescription.toBuilder()
                    .putAllFileSystems(getFileSystems(resourceId))
                    .build();
        }
        return computeResourceDescription;
    }

    @Override
    public List<ComputeResourceDescription> getComputeResourceList(Map<String, String> filters)
            throws AppCatalogException {
        if (filters.containsKey(DBConstants.ComputeResource.HOST_NAME)) {
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(
                    DBConstants.ComputeResource.HOST_NAME, filters.get(DBConstants.ComputeResource.HOST_NAME));
            List<ComputeResourceDescription> computeResourceDescriptionList =
                    select(QueryConstants.FIND_COMPUTE_RESOURCE, -1, 0, queryParameters);
            List<ComputeResourceDescription> result = new ArrayList<>();
            for (ComputeResourceDescription cd : computeResourceDescriptionList) {
                result.add(cd.toBuilder()
                        .putAllFileSystems(getFileSystems(cd.getComputeResourceId()))
                        .build());
            }
            return result;
        } else {
            logger.error("Unsupported field name for compute resource.", new IllegalArgumentException());
            throw new IllegalArgumentException("Unsupported field name for compute resource.");
        }
    }

    @Override
    public List<ComputeResourceDescription> getAllComputeResourceList() throws AppCatalogException {
        List<ComputeResourceDescription> computeResourceDescriptionList =
                select(QueryConstants.FIND_ALL_COMPUTE_RESOURCES, 0);
        List<ComputeResourceDescription> result = new ArrayList<>();
        for (ComputeResourceDescription cd : computeResourceDescriptionList) {
            result.add(cd.toBuilder()
                    .putAllFileSystems(getFileSystems(cd.getComputeResourceId()))
                    .build());
        }
        return result;
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
    public String addSSHJobSubmission(SSHJobSubmission sshJobSubmission) throws AppCatalogException {
        String submissionId = AppCatalogUtils.getID("SSH");
        sshJobSubmission = sshJobSubmission.toBuilder()
                .setJobSubmissionInterfaceId(submissionId)
                .build();
        String resourceJobManagerId = addResourceJobManager(sshJobSubmission.getResourceJobManager());
        SshJobSubmissionEntity sshJobSubmissionEntity =
                ComputeMapper.INSTANCE.sshJobSubmissionToEntity(sshJobSubmission);
        sshJobSubmissionEntity.getResourceJobManager().setResourceJobManagerId(resourceJobManagerId);
        if (sshJobSubmission.getResourceJobManager().getParallelismPrefixMap() != null) {
            (new ResourceJobManagerRepository())
                    .createParallesimPrefix(
                            sshJobSubmission.getResourceJobManager().getParallelismPrefixMap(),
                            sshJobSubmissionEntity.getResourceJobManager());
        }
        if (sshJobSubmission.getResourceJobManager().getJobManagerCommandsMap() != null) {
            (new ResourceJobManagerRepository())
                    .createJobManagerCommand(
                            sshJobSubmission.getResourceJobManager().getJobManagerCommandsMap(),
                            sshJobSubmissionEntity.getResourceJobManager());
        }
        if (sshJobSubmission.getMonitorMode() != null) {
            sshJobSubmissionEntity.setMonitorMode(
                    sshJobSubmission.getMonitorMode().toString());
        }
        execute(entityManager -> entityManager.merge(sshJobSubmissionEntity));
        return submissionId;
    }

    public void updateSSHJobSubmission(SSHJobSubmission sshJobSubmission) throws AppCatalogException {
        SshJobSubmissionEntity sshJobSubmissionEntity =
                ComputeMapper.INSTANCE.sshJobSubmissionToEntity(sshJobSubmission);
        sshJobSubmissionEntity.setUpdateTime(AiravataUtils.getCurrentTimestamp());
        execute(entityManager -> entityManager.merge(sshJobSubmissionEntity));
    }

    @Override
    public String addCloudJobSubmission(CloudJobSubmission cloudJobSubmission) throws AppCatalogException {
        cloudJobSubmission = cloudJobSubmission.toBuilder()
                .setJobSubmissionInterfaceId(AppCatalogUtils.getID("Cloud"))
                .build();
        CloudJobSubmissionEntity cloudJobSubmissionEntity =
                ComputeMapper.INSTANCE.cloudJobSubmissionToEntity(cloudJobSubmission);
        execute(entityManager -> entityManager.merge(cloudJobSubmissionEntity));
        return cloudJobSubmissionEntity.getJobSubmissionInterfaceId();
    }

    public void updateCloudJobSubmission(CloudJobSubmission cloudJobSubmission) throws AppCatalogException {
        CloudJobSubmissionEntity cloudJobSubmissionEntity =
                ComputeMapper.INSTANCE.cloudJobSubmissionToEntity(cloudJobSubmission);
        execute(entityManager -> entityManager.merge(cloudJobSubmissionEntity));
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
    public String addJobSubmissionProtocol(String computeResourceId, JobSubmissionInterface jobSubmissionInterface)
            throws AppCatalogException {
        return (new JobSubmissionInterfaceRepository()).addJobSubmission(computeResourceId, jobSubmissionInterface);
    }

    @Override
    public String addLocalJobSubmission(LOCALSubmission localSubmission) throws AppCatalogException {
        localSubmission = localSubmission.toBuilder()
                .setJobSubmissionInterfaceId(AppCatalogUtils.getID("LOCAL"))
                .build();
        String resourceJobManagerId = addResourceJobManager(localSubmission.getResourceJobManager());
        LocalSubmissionEntity localSubmissionEntity = ComputeMapper.INSTANCE.localSubmissionToEntity(localSubmission);
        localSubmissionEntity.setResourceJobManagerId(resourceJobManagerId);
        localSubmissionEntity.getResourceJobManager().setResourceJobManagerId(resourceJobManagerId);
        if (localSubmission.getResourceJobManager().getParallelismPrefixMap() != null) {
            (new ResourceJobManagerRepository())
                    .createParallesimPrefix(
                            localSubmission.getResourceJobManager().getParallelismPrefixMap(),
                            localSubmissionEntity.getResourceJobManager());
        }
        if (localSubmission.getResourceJobManager().getJobManagerCommandsMap() != null) {
            (new ResourceJobManagerRepository())
                    .createJobManagerCommand(
                            localSubmission.getResourceJobManager().getJobManagerCommandsMap(),
                            localSubmissionEntity.getResourceJobManager());
        }

        localSubmissionEntity.setSecurityProtocol(localSubmission.getSecurityProtocol());
        execute(entityManager -> entityManager.merge(localSubmissionEntity));
        return localSubmissionEntity.getJobSubmissionInterfaceId();
    }

    public void updateLocalJobSubmission(LOCALSubmission localSubmission) throws AppCatalogException {
        LocalSubmissionEntity localSubmissionEntity = ComputeMapper.INSTANCE.localSubmissionToEntity(localSubmission);
        localSubmissionEntity.setUpdateTime(AiravataUtils.getCurrentTimestamp());
        execute(entityManager -> entityManager.merge(localSubmissionEntity));
    }

    @Override
    public String addGlobusJobSubmission(GlobusJobSubmission globusJobSubmission) throws AppCatalogException {
        return null;
    }

    @Override
    public String addUNICOREJobSubmission(UnicoreJobSubmission unicoreJobSubmission) throws AppCatalogException {
        unicoreJobSubmission = unicoreJobSubmission.toBuilder()
                .setJobSubmissionInterfaceId(AppCatalogUtils.getID("UNICORE"))
                .build();
        UnicoreSubmissionEntity unicoreSubmissionEntity =
                ComputeMapper.INSTANCE.unicoreSubmissionToEntity(unicoreJobSubmission);
        unicoreSubmissionEntity.setSecurityProtocol(unicoreJobSubmission.getSecurityProtocol());
        execute(entityManager -> entityManager.merge(unicoreSubmissionEntity));
        return unicoreJobSubmission.getJobSubmissionInterfaceId();
    }

    @Override
    public SSHJobSubmission getSSHJobSubmission(String submissionId) throws AppCatalogException {
        SshJobSubmissionEntity entity =
                execute(entityManager -> entityManager.find(SshJobSubmissionEntity.class, submissionId));
        if (entity == null) return null;
        SSHJobSubmission sshJobSubmission = ComputeMapper.INSTANCE.sshJobSubmissionToModel(entity);
        String rjmId = sshJobSubmission.getResourceJobManager().getResourceJobManagerId();
        ResourceJobManagerRepository rjmRepo = new ResourceJobManagerRepository();
        Map<ApplicationParallelismType, String> parPrefix = rjmRepo.getParallelismPrefix(rjmId);
        Map<Integer, String> parPrefixInt = new HashMap<>();
        for (Map.Entry<ApplicationParallelismType, String> e : parPrefix.entrySet()) {
            parPrefixInt.put(e.getKey().getNumber(), e.getValue());
        }
        Map<JobManagerCommand, String> jmCommands = rjmRepo.getJobManagerCommand(rjmId);
        Map<Integer, String> jmCommandsInt = new HashMap<>();
        for (Map.Entry<JobManagerCommand, String> e : jmCommands.entrySet()) {
            jmCommandsInt.put(e.getKey().getNumber(), e.getValue());
        }
        return sshJobSubmission.toBuilder()
                .setResourceJobManager(sshJobSubmission.getResourceJobManager().toBuilder()
                        .putAllParallelismPrefix(parPrefixInt)
                        .putAllJobManagerCommands(jmCommandsInt)
                        .build())
                .build();
    }

    @Override
    public UnicoreJobSubmission getUNICOREJobSubmission(String submissionId) throws AppCatalogException {
        UnicoreSubmissionEntity entity =
                execute(entityManager -> entityManager.find(UnicoreSubmissionEntity.class, submissionId));
        if (entity == null) return null;
        return ComputeMapper.INSTANCE.unicoreSubmissionToModel(entity);
    }

    @Override
    public CloudJobSubmission getCloudJobSubmission(String submissionId) throws AppCatalogException {
        CloudJobSubmissionEntity entity =
                execute(entityManager -> entityManager.find(CloudJobSubmissionEntity.class, submissionId));
        if (entity == null) return null;
        return ComputeMapper.INSTANCE.cloudJobSubmissionToModel(entity);
    }

    @Override
    public void removeJobSubmissionInterface(String computeResourceId, String jobSubmissionInterfaceId)
            throws AppCatalogException {
        execute(entityManager -> {
            ComputeResourceEntity parent = entityManager.find(ComputeResourceEntity.class, computeResourceId);
            if (parent != null && parent.getJobSubmissionInterfaces() != null) {
                parent.getJobSubmissionInterfaces()
                        .removeIf(e -> jobSubmissionInterfaceId.equals(e.getJobSubmissionInterfaceId()));
                entityManager.merge(parent);
            }
            return null;
        });
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

    @Override
    public LOCALSubmission getLocalJobSubmission(String submissionId) throws AppCatalogException {
        LocalSubmissionEntity entity =
                execute(entityManager -> entityManager.find(LocalSubmissionEntity.class, submissionId));
        if (entity == null) return null;
        LOCALSubmission localSubmission = ComputeMapper.INSTANCE.localSubmissionToModel(entity);
        String rjmId = localSubmission.getResourceJobManager().getResourceJobManagerId();
        ResourceJobManagerRepository rjmRepo = new ResourceJobManagerRepository();
        Map<ApplicationParallelismType, String> parPrefix = rjmRepo.getParallelismPrefix(rjmId);
        Map<Integer, String> parPrefixInt = new HashMap<>();
        for (Map.Entry<ApplicationParallelismType, String> e : parPrefix.entrySet()) {
            parPrefixInt.put(e.getKey().getNumber(), e.getValue());
        }
        Map<JobManagerCommand, String> jmCommands = rjmRepo.getJobManagerCommand(rjmId);
        Map<Integer, String> jmCommandsInt = new HashMap<>();
        for (Map.Entry<JobManagerCommand, String> e : jmCommands.entrySet()) {
            jmCommandsInt.put(e.getKey().getNumber(), e.getValue());
        }
        return localSubmission.toBuilder()
                .setResourceJobManager(localSubmission.getResourceJobManager().toBuilder()
                        .putAllParallelismPrefix(parPrefixInt)
                        .putAllJobManagerCommands(jmCommandsInt)
                        .build())
                .build();
    }
}
