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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.airavata.common.model.AiravataCommonsConstants;
import org.apache.airavata.common.model.ApplicationDeploymentDescription;
import org.apache.airavata.common.model.CommandObject;
import org.apache.airavata.common.model.ComputeResourceDescription;
import org.apache.airavata.common.model.DeploymentCommandType;
import org.apache.airavata.common.model.LibraryPathType;
import org.apache.airavata.common.model.SetEnvPaths;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.registry.entities.appcatalog.ApplicationDeploymentCommandEntity;
import org.apache.airavata.registry.entities.appcatalog.ApplicationDeploymentEntity;
import org.apache.airavata.registry.entities.appcatalog.LibraryPathEntity;
import org.apache.airavata.registry.exception.AppCatalogException;
import org.apache.airavata.registry.mappers.ApplicationDeploymentMapper;
import org.apache.airavata.registry.mappers.CommandObjectMapper;
import org.apache.airavata.registry.mappers.SetEnvPathsMapper;
import org.apache.airavata.registry.model.ApplicationDeployment;
import org.apache.airavata.registry.repositories.appcatalog.ApplicationDeploymentRepository;
import org.apache.airavata.registry.utils.DBConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ApplicationDeploymentService implements ApplicationDeployment {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationDeploymentService.class);

    private final ApplicationDeploymentRepository applicationDeploymentRepository;
    private final ComputeResourceService computeResourceService;
    private final ApplicationDeploymentMapper applicationDeploymentMapper;
    private final CommandObjectMapper commandObjectMapper;
    private final SetEnvPathsMapper setEnvPathsMapper;

    public ApplicationDeploymentService(
            ApplicationDeploymentRepository applicationDeploymentRepository,
            ComputeResourceService computeResourceService,
            ApplicationDeploymentMapper applicationDeploymentMapper,
            CommandObjectMapper commandObjectMapper,
            SetEnvPathsMapper setEnvPathsMapper) {
        this.applicationDeploymentRepository = applicationDeploymentRepository;
        this.computeResourceService = computeResourceService;
        this.applicationDeploymentMapper = applicationDeploymentMapper;
        this.commandObjectMapper = commandObjectMapper;
        this.setEnvPathsMapper = setEnvPathsMapper;
    }

    @Override
    public String addApplicationDeployment(
            ApplicationDeploymentDescription applicationDeploymentDescription, String gatewayId)
            throws AppCatalogException {
        return saveApplicationDeploymentDescriptorData(applicationDeploymentDescription, gatewayId);
    }

    @Override
    public void updateApplicationDeployment(
            String deploymentId, ApplicationDeploymentDescription updatedApplicationDeploymentDescription)
            throws AppCatalogException {
        saveApplicationDeploymentDescriptorData(updatedApplicationDeploymentDescription, null);
    }

    @Override
    public ApplicationDeploymentDescription getApplicationDeployement(String deploymentId) throws AppCatalogException {
        ApplicationDeploymentEntity entity =
                applicationDeploymentRepository.findById(deploymentId).orElse(null);
        if (entity == null) return null;
        return mapEntityToModel(entity);
    }

    @Override
    public List<ApplicationDeploymentDescription> getApplicationDeployments(Map<String, String> filters)
            throws AppCatalogException {
        List<ApplicationDeploymentDescription> deploymentDescriptions = new ArrayList<>();
        try {
            boolean firstTry = true;
            for (String fieldName : filters.keySet()) {
                List<ApplicationDeploymentDescription> tmpDescriptions;

                switch (fieldName) {
                    case DBConstants.ApplicationDeployment.APPLICATION_MODULE_ID: {
                        logger.debug(
                                "Fetching all Application Deployments for Application Module ID {}",
                                filters.get(DBConstants.ApplicationDeployment.APPLICATION_MODULE_ID));

                        List<ApplicationDeploymentEntity> entities =
                                applicationDeploymentRepository.findByAppModuleId(filters.get(fieldName));
                        tmpDescriptions =
                                entities.stream().map(this::mapEntityToModel).collect(Collectors.toList());
                        break;
                    }

                    case DBConstants.ApplicationDeployment.COMPUTE_HOST_ID: {
                        logger.debug(
                                "Fetching Application Deployments for Compute Host ID {}",
                                filters.get(DBConstants.ApplicationDeployment.COMPUTE_HOST_ID));

                        List<ApplicationDeploymentEntity> entities =
                                applicationDeploymentRepository.findByComputeHostId(filters.get(fieldName));
                        tmpDescriptions =
                                entities.stream().map(this::mapEntityToModel).collect(Collectors.toList());
                        break;
                    }

                    default:
                        logger.warn("Unsupported field name for app deployment in filters: {}", filters);
                        throw new IllegalArgumentException(
                                "Unsupported field name for app deployment in filters: " + filters);
                }

                if (firstTry) {
                    deploymentDescriptions.addAll(tmpDescriptions);
                    firstTry = false;
                } else {
                    List<String> ids = new ArrayList<>();
                    for (ApplicationDeploymentDescription applicationDeploymentDescription : deploymentDescriptions) {
                        ids.add(applicationDeploymentDescription.getAppDeploymentId());
                    }
                    List<ApplicationDeploymentDescription> tmp2Descriptions = new ArrayList<>();
                    for (ApplicationDeploymentDescription applicationDeploymentDescription : tmpDescriptions) {
                        if (ids.contains(applicationDeploymentDescription.getAppDeploymentId())) {
                            tmp2Descriptions.add(applicationDeploymentDescription);
                        }
                    }
                    deploymentDescriptions.clear();
                    deploymentDescriptions.addAll(tmp2Descriptions);
                }
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error while retrieving app deployment list...", e);
            throw new AppCatalogException(e);
        }
        return deploymentDescriptions;
    }

    @Override
    public List<ApplicationDeploymentDescription> getAllApplicationDeployements(String gatewayId)
            throws AppCatalogException {
        List<ApplicationDeploymentEntity> entities = applicationDeploymentRepository.findByGatewayId(gatewayId);
        return entities.stream().map(this::mapEntityToModel).collect(Collectors.toList());
    }

    @Override
    public List<ApplicationDeploymentDescription> getAccessibleApplicationDeployments(
            String gatewayId, List<String> accessibleAppIds, List<String> accessibleCompHostIds)
            throws AppCatalogException {
        if (accessibleAppIds.isEmpty() || accessibleCompHostIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<ApplicationDeploymentEntity> entities =
                applicationDeploymentRepository.findAccessibleApplicationDeployments(
                        gatewayId, accessibleAppIds, accessibleCompHostIds);
        return entities.stream().map(this::mapEntityToModel).collect(Collectors.toList());
    }

    @Override
    public List<ApplicationDeploymentDescription> getAccessibleApplicationDeployments(
            String gatewayId,
            String appModuleId,
            List<String> accessibleAppIds,
            List<String> accessibleComputeResourceIds)
            throws AppCatalogException {
        if (accessibleAppIds.isEmpty() || accessibleComputeResourceIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<ApplicationDeploymentEntity> entities =
                applicationDeploymentRepository.findAccessibleApplicationDeploymentsForAppModule(
                        gatewayId, appModuleId, accessibleAppIds, accessibleComputeResourceIds);
        return entities.stream().map(this::mapEntityToModel).collect(Collectors.toList());
    }

    @Override
    public List<String> getAllApplicationDeployementIds() throws AppCatalogException {
        List<String> applicationDeploymentIds = new ArrayList<>();
        List<ApplicationDeploymentEntity> entities = applicationDeploymentRepository.findAll();

        if (entities != null && !entities.isEmpty()) {
            for (ApplicationDeploymentEntity entity : entities) {
                applicationDeploymentIds.add(entity.getAppDeploymentId());
            }
        }
        return applicationDeploymentIds;
    }

    @Override
    public boolean isAppDeploymentExists(String deploymentId) throws AppCatalogException {
        return applicationDeploymentRepository.existsById(deploymentId);
    }

    @Override
    public void removeAppDeployment(String deploymentId) throws AppCatalogException {
        applicationDeploymentRepository.deleteById(deploymentId);
    }

    private String saveApplicationDeploymentDescriptorData(
            ApplicationDeploymentDescription applicationDeploymentDescription, String gatewayId)
            throws AppCatalogException {
        ApplicationDeploymentEntity applicationDeploymentEntity =
                saveApplicationDeployment(applicationDeploymentDescription, gatewayId);
        return applicationDeploymentEntity.getAppDeploymentId();
    }

    private ApplicationDeploymentEntity saveApplicationDeployment(
            ApplicationDeploymentDescription applicationDeploymentDescription, String gatewayId)
            throws AppCatalogException {

        if (applicationDeploymentDescription.getAppDeploymentId().trim().isEmpty()
                || applicationDeploymentDescription.getAppDeploymentId().equals(AiravataCommonsConstants.DEFAULT_ID)) {
            ComputeResourceDescription computeResourceDescription =
                    computeResourceService.getComputeResource(applicationDeploymentDescription.getComputeHostId());
            applicationDeploymentDescription.setAppDeploymentId(
                    computeResourceDescription.getHostName() + "_" + applicationDeploymentDescription.getAppModuleId());
        }

        String applicationDeploymentId = applicationDeploymentDescription.getAppDeploymentId();
        ApplicationDeploymentEntity applicationDeploymentEntity =
                applicationDeploymentMapper.toEntity(applicationDeploymentDescription);

        if (gatewayId != null) {
            applicationDeploymentEntity.setGatewayId(gatewayId);
        }

        // Build unified command list from all command types
        List<ApplicationDeploymentCommandEntity> commands = new ArrayList<>();
        
        if (applicationDeploymentDescription.getModuleLoadCmds() != null) {
            for (CommandObject cmd : applicationDeploymentDescription.getModuleLoadCmds()) {
                ApplicationDeploymentCommandEntity entity = new ApplicationDeploymentCommandEntity();
                entity.setDeploymentId(applicationDeploymentId);
                entity.setCommandType(DeploymentCommandType.MODULE_LOAD);
                entity.setCommand(cmd.getCommand());
                entity.setCommandOrder(cmd.getCommandOrder());
                commands.add(entity);
            }
        }
        
        if (applicationDeploymentDescription.getPreJobCommands() != null) {
            for (CommandObject cmd : applicationDeploymentDescription.getPreJobCommands()) {
                ApplicationDeploymentCommandEntity entity = new ApplicationDeploymentCommandEntity();
                entity.setDeploymentId(applicationDeploymentId);
                entity.setCommandType(DeploymentCommandType.PREJOB);
                entity.setCommand(cmd.getCommand());
                entity.setCommandOrder(cmd.getCommandOrder());
                commands.add(entity);
            }
        }
        
        if (applicationDeploymentDescription.getPostJobCommands() != null) {
            for (CommandObject cmd : applicationDeploymentDescription.getPostJobCommands()) {
                ApplicationDeploymentCommandEntity entity = new ApplicationDeploymentCommandEntity();
                entity.setDeploymentId(applicationDeploymentId);
                entity.setCommandType(DeploymentCommandType.POSTJOB);
                entity.setCommand(cmd.getCommand());
                entity.setCommandOrder(cmd.getCommandOrder());
                commands.add(entity);
            }
        }
        
        applicationDeploymentEntity.setCommands(commands);

        // Build unified library path list from prepend and append paths
        List<LibraryPathEntity> libraryPaths = new ArrayList<>();
        
        if (applicationDeploymentDescription.getLibPrependPaths() != null) {
            for (SetEnvPaths path : applicationDeploymentDescription.getLibPrependPaths()) {
                LibraryPathEntity entity = new LibraryPathEntity();
                entity.setDeploymentId(applicationDeploymentId);
                entity.setPathType(LibraryPathType.PREPEND);
                entity.setName(path.getName());
                entity.setValue(path.getValue());
                libraryPaths.add(entity);
            }
        }
        
        if (applicationDeploymentDescription.getLibAppendPaths() != null) {
            for (SetEnvPaths path : applicationDeploymentDescription.getLibAppendPaths()) {
                LibraryPathEntity entity = new LibraryPathEntity();
                entity.setDeploymentId(applicationDeploymentId);
                entity.setPathType(LibraryPathType.APPEND);
                entity.setName(path.getName());
                entity.setValue(path.getValue());
                libraryPaths.add(entity);
            }
        }
        
        applicationDeploymentEntity.setLibraryPaths(libraryPaths);

        // Set environment variables
        if (applicationDeploymentDescription.getSetEnvironment() != null) {
            applicationDeploymentEntity.setSetEnvironment(
                    setEnvPathsMapper.toEntityListToEnvironment(applicationDeploymentDescription.getSetEnvironment()));
            applicationDeploymentEntity.getSetEnvironment()
                    .forEach(env -> env.setDeploymentId(applicationDeploymentId));
        }

        if (!isAppDeploymentExists(applicationDeploymentId)) {
            applicationDeploymentEntity.setCreationTime(AiravataUtils.getUniqueTimestamp());
        }

        applicationDeploymentEntity.setUpdateTime(AiravataUtils.getUniqueTimestamp());
        return applicationDeploymentRepository.save(applicationDeploymentEntity);
    }

    private ApplicationDeploymentDescription mapEntityToModel(ApplicationDeploymentEntity entity) {
        ApplicationDeploymentDescription model = applicationDeploymentMapper.toModel(entity);
        
        // Map module load commands
        List<ApplicationDeploymentCommandEntity> moduleLoadCmds = entity.getModuleLoadCommands();
        if (moduleLoadCmds != null && !moduleLoadCmds.isEmpty()) {
            model.setModuleLoadCmds(commandObjectMapper.toModelList(moduleLoadCmds));
        }
        
        // Map prejob commands
        List<ApplicationDeploymentCommandEntity> preJobCmds = entity.getPreJobCommands();
        if (preJobCmds != null && !preJobCmds.isEmpty()) {
            model.setPreJobCommands(commandObjectMapper.toModelList(preJobCmds));
        }
        
        // Map postjob commands
        List<ApplicationDeploymentCommandEntity> postJobCmds = entity.getPostJobCommands();
        if (postJobCmds != null && !postJobCmds.isEmpty()) {
            model.setPostJobCommands(commandObjectMapper.toModelList(postJobCmds));
        }
        
        // Map library prepend paths
        List<LibraryPathEntity> prependPaths = entity.getLibPrependPaths();
        if (prependPaths != null && !prependPaths.isEmpty()) {
            model.setLibPrependPaths(setEnvPathsMapper.toModelListFromLibraryPath(prependPaths));
        }
        
        // Map library append paths
        List<LibraryPathEntity> appendPaths = entity.getLibAppendPaths();
        if (appendPaths != null && !appendPaths.isEmpty()) {
            model.setLibAppendPaths(setEnvPathsMapper.toModelListFromLibraryPath(appendPaths));
        }
        
        // Map environment variables
        if (entity.getSetEnvironment() != null) {
            model.setSetEnvironment(setEnvPathsMapper.toModelListFromEnvironment(entity.getSetEnvironment()));
        }
        
        return model;
    }
}
