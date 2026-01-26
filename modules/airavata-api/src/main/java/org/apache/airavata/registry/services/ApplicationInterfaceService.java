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
import org.apache.airavata.common.model.AiravataCommonsConstants;
import org.apache.airavata.common.model.ApplicationInterfaceDescription;
import org.apache.airavata.common.model.ApplicationModule;
import org.apache.airavata.common.model.DataObjectParentType;
import org.apache.airavata.common.model.InputDataObjectType;
import org.apache.airavata.common.model.OutputDataObjectType;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.registry.entities.InputDataEntity;
import org.apache.airavata.registry.entities.OutputDataEntity;
import org.apache.airavata.registry.entities.appcatalog.AppModuleMappingEntity;
import org.apache.airavata.registry.entities.appcatalog.AppModuleMappingPK;
import org.apache.airavata.registry.entities.appcatalog.ApplicationInterfaceEntity;
import org.apache.airavata.registry.entities.appcatalog.ApplicationModuleEntity;
import org.apache.airavata.registry.exception.AppCatalogException;
import org.apache.airavata.registry.mappers.ApplicationInterfaceMapper;
import org.apache.airavata.registry.mappers.ApplicationModuleMapper;
import org.apache.airavata.registry.mappers.InputDataObjectTypeMapper;
import org.apache.airavata.registry.mappers.OutputDataObjectTypeMapper;
import org.apache.airavata.registry.model.ApplicationInterface;
import org.apache.airavata.registry.repositories.InputDataRepository;
import org.apache.airavata.registry.repositories.OutputDataRepository;
import org.apache.airavata.registry.repositories.appcatalog.AppModuleMappingRepository;
import org.apache.airavata.registry.repositories.appcatalog.ApplicationInterfaceRepository;
import org.apache.airavata.registry.repositories.appcatalog.ApplicationModuleRepository;
import org.apache.airavata.registry.utils.DBConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ApplicationInterfaceService implements ApplicationInterface {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationInterfaceService.class);

    private final ApplicationInterfaceRepository applicationInterfaceRepository;
    private final ApplicationModuleRepository applicationModuleRepository;
    private final InputDataRepository inputDataRepository;
    private final OutputDataRepository outputDataRepository;
    private final AppModuleMappingRepository appModuleMappingRepository;
    private final ApplicationInterfaceMapper applicationInterfaceMapper;
    private final ApplicationModuleMapper applicationModuleMapper;
    private final InputDataObjectTypeMapper inputDataObjectTypeMapper;
    private final OutputDataObjectTypeMapper outputDataObjectTypeMapper;

    public ApplicationInterfaceService(
            ApplicationInterfaceRepository applicationInterfaceRepository,
            ApplicationModuleRepository applicationModuleRepository,
            InputDataRepository inputDataRepository,
            OutputDataRepository outputDataRepository,
            AppModuleMappingRepository appModuleMappingRepository,
            ApplicationInterfaceMapper applicationInterfaceMapper,
            ApplicationModuleMapper applicationModuleMapper,
            InputDataObjectTypeMapper inputDataObjectTypeMapper,
            OutputDataObjectTypeMapper outputDataObjectTypeMapper) {
        this.applicationInterfaceRepository = applicationInterfaceRepository;
        this.applicationModuleRepository = applicationModuleRepository;
        this.inputDataRepository = inputDataRepository;
        this.outputDataRepository = outputDataRepository;
        this.appModuleMappingRepository = appModuleMappingRepository;
        this.applicationInterfaceMapper = applicationInterfaceMapper;
        this.applicationModuleMapper = applicationModuleMapper;
        this.inputDataObjectTypeMapper = inputDataObjectTypeMapper;
        this.outputDataObjectTypeMapper = outputDataObjectTypeMapper;
    }

    @Override
    public String addApplicationModule(ApplicationModule applicationModule, String gatewayId)
            throws AppCatalogException {
        return saveApplicationModuleData(applicationModule, gatewayId);
    }

    @Override
    public String addApplicationInterface(
            ApplicationInterfaceDescription applicationInterfaceDescription, String gatewayId)
            throws AppCatalogException {
        return saveApplicationInterfaceDescriptorData(applicationInterfaceDescription, gatewayId);
    }

    @Override
    public void addApplicationModuleMapping(String moduleId, String interfaceId) throws AppCatalogException {
        // Check if mapping already exists
        AppModuleMappingPK mappingPK = new AppModuleMappingPK();
        mappingPK.setInterfaceId(interfaceId);
        mappingPK.setModuleId(moduleId);
        if (appModuleMappingRepository.existsById(mappingPK)) {
            logger.debug("Module mapping already exists for moduleId: {} and interfaceId: {}", moduleId, interfaceId);
            return;
        }

        // Also check using the query method
        if (appModuleMappingRepository.existsByInterfaceIdAndModuleId(interfaceId, moduleId)) {
            logger.debug(
                    "Module mapping already exists (via query) for moduleId: {} and interfaceId: {}",
                    moduleId,
                    interfaceId);
            return;
        }

        // Create and save the mapping entity with just the IDs
        // Avoid loading full entities to prevent cascade issues
        var appModuleMappingEntity = new AppModuleMappingEntity();
        appModuleMappingEntity.setModuleId(moduleId);
        appModuleMappingEntity.setInterfaceId(interfaceId);

        // Use references instead of loading full entities
        var moduleRef = applicationModuleRepository.getReferenceById(moduleId);
        var interfaceRef = applicationInterfaceRepository.getReferenceById(interfaceId);
        appModuleMappingEntity.setApplicationModule(moduleRef);
        appModuleMappingEntity.setApplicationInterface(interfaceRef);

        // Save the mapping
        appModuleMappingRepository.save(appModuleMappingEntity);
    }

    @Override
    public void updateApplicationModule(String moduleId, ApplicationModule updatedApplicationModule)
            throws AppCatalogException {
        saveApplicationModuleData(updatedApplicationModule, null);
    }

    @Override
    public void updateApplicationInterface(
            String interfaceId, ApplicationInterfaceDescription updatedApplicationInterfaceDescription)
            throws AppCatalogException {
        saveApplicationInterfaceDescriptorData(updatedApplicationInterfaceDescription, null);
    }

    @Override
    public ApplicationModule getApplicationModule(String moduleId) throws AppCatalogException {
        var entity = applicationModuleRepository.findById(moduleId).orElse(null);
        if (entity == null) return null;
        return applicationModuleMapper.toModel(entity);
    }

    @Override
    public ApplicationInterfaceDescription getApplicationInterface(String interfaceId) throws AppCatalogException {
        var entity = applicationInterfaceRepository.findById(interfaceId).orElse(null);
        if (entity == null) return null;

        // Load application modules from AppModuleMapping table
        // The ElementCollection might not be automatically synced when AppModuleMappingEntity is saved separately
        var mappings = appModuleMappingRepository.findByInterfaceId(interfaceId);
        List<String> moduleIds = mappings.stream()
                .map(AppModuleMappingEntity::getModuleId)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
        entity.setApplicationModules(moduleIds);

        var model = applicationInterfaceMapper.toModel(entity);

        // Map inputs and outputs from unified tables
        List<InputDataEntity> inputEntities = inputDataRepository.findByApplicationId(interfaceId);
        model.setApplicationInputs(inputDataObjectTypeMapper.toModelList(inputEntities));

        List<OutputDataEntity> outputEntities = outputDataRepository.findByApplicationId(interfaceId);
        model.setApplicationOutputs(outputDataObjectTypeMapper.toModelList(outputEntities));

        return model;
    }

    @Override
    public List<ApplicationModule> getApplicationModules(Map<String, String> filters) throws AppCatalogException {
        if (filters.containsKey(DBConstants.ApplicationModule.APPLICATION_MODULE_NAME)) {
            logger.debug("Fetching Application Modules for given Application Module Name");
            String appModuleName = filters.get(DBConstants.ApplicationModule.APPLICATION_MODULE_NAME);
            List<ApplicationModuleEntity> entities = applicationModuleRepository.findByAppModuleName(appModuleName);
            return applicationModuleMapper.toModelList(entities);
        } else {
            logger.error("Unsupported field name for app module.");
            throw new IllegalArgumentException("Unsupported field name for app module.");
        }
    }

    @Override
    public List<ApplicationModule> getAllApplicationModules(String gatewayId) throws AppCatalogException {
        List<ApplicationModuleEntity> entities = applicationModuleRepository.findByGatewayId(gatewayId);
        return applicationModuleMapper.toModelList(entities);
    }

    @Override
    public List<ApplicationModule> getAccessibleApplicationModules(
            String gatewayId, List<String> accessibleAppIds, List<String> accessibleCompHostIds)
            throws AppCatalogException {
        if (accessibleAppIds.isEmpty() || accessibleCompHostIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<ApplicationModuleEntity> entities = applicationModuleRepository.findAccessibleApplicationModules(
                gatewayId, accessibleAppIds, accessibleCompHostIds);
        return applicationModuleMapper.toModelList(entities);
    }

    @Override
    public List<ApplicationInterfaceDescription> getApplicationInterfaces(Map<String, String> filters)
            throws AppCatalogException {
        if (filters.containsKey(DBConstants.ApplicationInterface.APPLICATION_NAME)) {
            logger.debug("Fetching Application Interfaces for given Application Name");
            String applicationName = filters.get(DBConstants.ApplicationInterface.APPLICATION_NAME);
            List<ApplicationInterfaceEntity> entities =
                    applicationInterfaceRepository.findByApplicationName(applicationName);
            return applicationInterfaceMapper.toModelList(entities);
        } else {
            logger.error("Unsupported field name for app interface.");
            throw new IllegalArgumentException("Unsupported field name for app interface.");
        }
    }

    @Override
    public List<ApplicationInterfaceDescription> getAllApplicationInterfaces(String gatewayId)
            throws AppCatalogException {
        List<ApplicationInterfaceEntity> entities = applicationInterfaceRepository.findByGatewayId(gatewayId);
        return applicationInterfaceMapper.toModelList(entities);
    }

    @Override
    public List<String> getAllApplicationInterfaceIds() throws AppCatalogException {
        var applicationInterfaceIds = new ArrayList<String>();
        var entities = applicationInterfaceRepository.findAll();

        if (entities != null && !entities.isEmpty()) {
            logger.debug("The fetched list of Application Interfaces is not NULL or empty");
            for (var entity : entities) {
                applicationInterfaceIds.add(entity.getApplicationInterfaceId());
            }
        }

        return applicationInterfaceIds;
    }

    @Override
    public List<InputDataObjectType> getApplicationInputs(String interfaceId) throws AppCatalogException {
        List<InputDataEntity> entities = inputDataRepository.findByApplicationId(interfaceId);
        return inputDataObjectTypeMapper.toModelList(entities);
    }

    @Override
    public List<OutputDataObjectType> getApplicationOutputs(String interfaceId) throws AppCatalogException {
        List<OutputDataEntity> entities = outputDataRepository.findByApplicationId(interfaceId);
        return outputDataObjectTypeMapper.toModelList(entities);
    }

    @Override
    public boolean removeApplicationInterface(String interfaceId) throws AppCatalogException {
        if (!applicationInterfaceRepository.existsById(interfaceId)) {
            return false;
        }
        // Delete associated inputs and outputs from unified tables
        inputDataRepository.deleteByParentIdAndParentType(interfaceId, DataObjectParentType.APPLICATION);
        outputDataRepository.deleteByParentIdAndParentType(interfaceId, DataObjectParentType.APPLICATION);
        applicationInterfaceRepository.deleteById(interfaceId);
        return true;
    }

    @Override
    public boolean removeApplicationModule(String moduleId) throws AppCatalogException {
        if (!applicationModuleRepository.existsById(moduleId)) {
            return false;
        }
        applicationModuleRepository.deleteById(moduleId);
        return true;
    }

    @Override
    public boolean isApplicationInterfaceExists(String interfaceId) throws AppCatalogException {
        return applicationInterfaceRepository.existsById(interfaceId);
    }

    @Override
    public boolean isApplicationModuleExists(String moduleId) throws AppCatalogException {
        return applicationModuleRepository.existsById(moduleId);
    }

    private String saveApplicationInterfaceDescriptorData(
            ApplicationInterfaceDescription applicationInterfaceDescription, String gatewayId)
            throws AppCatalogException {
        ApplicationInterfaceEntity applicationInterfaceEntity =
                saveApplicationInterface(applicationInterfaceDescription, gatewayId);
        return applicationInterfaceEntity.getApplicationInterfaceId();
    }

    private ApplicationInterfaceEntity saveApplicationInterface(
            ApplicationInterfaceDescription applicationInterfaceDescription, String gatewayId)
            throws AppCatalogException {

        if (applicationInterfaceDescription.getApplicationInterfaceId() == null
                || applicationInterfaceDescription
                        .getApplicationInterfaceId()
                        .trim()
                        .equals("")
                || applicationInterfaceDescription
                        .getApplicationInterfaceId()
                        .equals(AiravataCommonsConstants.DEFAULT_ID)) {
            logger.debug(
                    "If Application Interface ID is null, empty or DEFAULT, set it as the Application Interface Name plus random UUID");
            applicationInterfaceDescription.setApplicationInterfaceId(
                    AiravataUtils.getId(applicationInterfaceDescription.getApplicationName()));
        }

        String applicationInterfaceId = applicationInterfaceDescription.getApplicationInterfaceId();
        ApplicationInterfaceEntity applicationInterfaceEntity =
                applicationInterfaceMapper.toEntity(applicationInterfaceDescription);

        if (gatewayId != null) {
            logger.debug("Setting the gateway ID of the Application Interface");
            applicationInterfaceEntity.setGatewayId(gatewayId);
        }

        if (!isApplicationInterfaceExists(applicationInterfaceId)) {
            logger.debug("Checking if the Application Interface already exists");
            applicationInterfaceEntity.setCreationTime(AiravataUtils.getUniqueTimestamp());
        }

        applicationInterfaceEntity.setUpdateTime(AiravataUtils.getUniqueTimestamp());

        // Save the entity first (without inputs/outputs which are now in unified tables)
        var savedEntity = applicationInterfaceRepository.save(applicationInterfaceEntity);

        // Delete existing inputs and outputs, then save new ones
        inputDataRepository.deleteByParentIdAndParentType(applicationInterfaceId, DataObjectParentType.APPLICATION);
        outputDataRepository.deleteByParentIdAndParentType(applicationInterfaceId, DataObjectParentType.APPLICATION);

        // Save inputs to unified table
        if (applicationInterfaceDescription.getApplicationInputs() != null) {
            logger.debug("Saving ApplicationInputs to unified INPUT_DATA table");
            for (var input : applicationInterfaceDescription.getApplicationInputs()) {
                var inputEntity = inputDataObjectTypeMapper.toApplicationInputEntity(input, applicationInterfaceId);
                inputDataRepository.save(inputEntity);
            }
        }

        // Save outputs to unified table
        if (applicationInterfaceDescription.getApplicationOutputs() != null) {
            logger.debug("Saving ApplicationOutputs to unified OUTPUT_DATA table");
            for (var output : applicationInterfaceDescription.getApplicationOutputs()) {
                var outputEntity =
                        outputDataObjectTypeMapper.toApplicationOutputEntity(output, applicationInterfaceId);
                outputDataRepository.save(outputEntity);
            }
        }

        return savedEntity;
    }

    private String saveApplicationModuleData(ApplicationModule applicationModule, String gatewayId)
            throws AppCatalogException {
        var applicationModuleEntity = saveApplicationModule(applicationModule, gatewayId);
        return applicationModuleEntity.getAppModuleId();
    }

    private ApplicationModuleEntity saveApplicationModule(ApplicationModule applicationModule, String gatewayId)
            throws AppCatalogException {

        if (applicationModule.getAppModuleId() == null
                || applicationModule.getAppModuleId().trim().equals("")
                || applicationModule.getAppModuleId().equals(AiravataCommonsConstants.DEFAULT_ID)) {
            logger.debug(
                    "If Application Module ID is null, empty or DEFAULT, set it as the Application Module Name plus random UUID");
            applicationModule.setAppModuleId(AiravataUtils.getId(applicationModule.getAppModuleName()));
        }

        var applicationModuleId = applicationModule.getAppModuleId();
        var applicationModuleEntity = applicationModuleMapper.toEntity(applicationModule);

        if (gatewayId != null) {
            logger.debug("Setting the gateway ID of the Application Module");
            applicationModuleEntity.setGatewayId(gatewayId);
        }

        if (!isApplicationModuleExists(applicationModuleId)) {
            logger.debug("Checking if the Application Module already exists");
            applicationModuleEntity.setCreationTime(AiravataUtils.getUniqueTimestamp());
        }

        applicationModuleEntity.setUpdateTime(AiravataUtils.getUniqueTimestamp());
        return applicationModuleRepository.save(applicationModuleEntity);
    }
}
