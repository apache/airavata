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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationModule;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.commons.airavata_commonsConstants;
import org.apache.airavata.registry.cpi.ApplicationInterface;
import org.apache.airavata.registry.entities.appcatalog.AppModuleMappingEntity;
import org.apache.airavata.registry.entities.appcatalog.ApplicationInputEntity;
import org.apache.airavata.registry.entities.appcatalog.ApplicationInterfaceEntity;
import org.apache.airavata.registry.entities.appcatalog.ApplicationModuleEntity;
import org.apache.airavata.registry.entities.appcatalog.ApplicationOutputEntity;
import org.apache.airavata.registry.exceptions.AppCatalogException;
import org.apache.airavata.registry.repositories.appcatalog.AppModuleMappingRepository;
import org.apache.airavata.registry.repositories.appcatalog.ApplicationInputRepository;
import org.apache.airavata.registry.repositories.appcatalog.ApplicationInterfaceRepository;
import org.apache.airavata.registry.repositories.appcatalog.ApplicationModuleRepository;
import org.apache.airavata.registry.repositories.appcatalog.ApplicationOutputRepository;
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
    private final ApplicationInputRepository applicationInputRepository;
    private final ApplicationOutputRepository applicationOutputRepository;
    private final AppModuleMappingRepository appModuleMappingRepository;
    private final Mapper mapper;

    public ApplicationInterfaceService(
            ApplicationInterfaceRepository applicationInterfaceRepository,
            ApplicationModuleRepository applicationModuleRepository,
            ApplicationInputRepository applicationInputRepository,
            ApplicationOutputRepository applicationOutputRepository,
            AppModuleMappingRepository appModuleMappingRepository,
            Mapper mapper) {
        this.applicationInterfaceRepository = applicationInterfaceRepository;
        this.applicationModuleRepository = applicationModuleRepository;
        this.applicationInputRepository = applicationInputRepository;
        this.applicationOutputRepository = applicationOutputRepository;
        this.appModuleMappingRepository = appModuleMappingRepository;
        this.mapper = mapper;
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
        ApplicationModule applicationModule = getApplicationModule(moduleId);
        ApplicationInterfaceDescription applicationInterfaceDescription = getApplicationInterface(interfaceId);
        ApplicationModuleEntity applicationModuleEntity = mapper.map(applicationModule, ApplicationModuleEntity.class);
        ApplicationInterfaceEntity applicationInterfaceEntity =
                mapper.map(applicationInterfaceDescription, ApplicationInterfaceEntity.class);
        AppModuleMappingEntity appModuleMappingEntity = new AppModuleMappingEntity();
        appModuleMappingEntity.setModuleId(moduleId);
        appModuleMappingEntity.setInterfaceId(interfaceId);
        appModuleMappingEntity.setApplicationModule(applicationModuleEntity);
        appModuleMappingEntity.setApplicationInterface(applicationInterfaceEntity);
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
        ApplicationModuleEntity entity =
                applicationModuleRepository.findById(moduleId).orElse(null);
        if (entity == null) return null;
        return mapper.map(entity, ApplicationModule.class);
    }

    @Override
    public ApplicationInterfaceDescription getApplicationInterface(String interfaceId) throws AppCatalogException {
        ApplicationInterfaceEntity entity =
                applicationInterfaceRepository.findById(interfaceId).orElse(null);
        if (entity == null) return null;
        return mapper.map(entity, ApplicationInterfaceDescription.class);
    }

    @Override
    public List<ApplicationModule> getApplicationModules(Map<String, String> filters) throws AppCatalogException {
        if (filters.containsKey(DBConstants.ApplicationModule.APPLICATION_MODULE_NAME)) {
            logger.debug("Fetching Application Modules for given Application Module Name");
            String appModuleName = filters.get(DBConstants.ApplicationModule.APPLICATION_MODULE_NAME);
            List<ApplicationModuleEntity> entities = applicationModuleRepository.findByAppModuleName(appModuleName);
            return entities.stream()
                    .map(e -> mapper.map(e, ApplicationModule.class))
                    .collect(Collectors.toList());
        } else {
            logger.error("Unsupported field name for app module.");
            throw new IllegalArgumentException("Unsupported field name for app module.");
        }
    }

    @Override
    public List<ApplicationModule> getAllApplicationModules(String gatewayId) throws AppCatalogException {
        List<ApplicationModuleEntity> entities = applicationModuleRepository.findByGatewayId(gatewayId);
        return entities.stream()
                .map(e -> mapper.map(e, ApplicationModule.class))
                .collect(Collectors.toList());
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
        return entities.stream()
                .map(e -> mapper.map(e, ApplicationModule.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ApplicationInterfaceDescription> getApplicationInterfaces(Map<String, String> filters)
            throws AppCatalogException {
        if (filters.containsKey(DBConstants.ApplicationInterface.APPLICATION_NAME)) {
            logger.debug("Fetching Application Interfaces for given Application Name");
            String applicationName = filters.get(DBConstants.ApplicationInterface.APPLICATION_NAME);
            List<ApplicationInterfaceEntity> entities =
                    applicationInterfaceRepository.findByApplicationName(applicationName);
            return entities.stream()
                    .map(e -> mapper.map(e, ApplicationInterfaceDescription.class))
                    .collect(Collectors.toList());
        } else {
            logger.error("Unsupported field name for app interface.");
            throw new IllegalArgumentException("Unsupported field name for app interface.");
        }
    }

    @Override
    public List<ApplicationInterfaceDescription> getAllApplicationInterfaces(String gatewayId)
            throws AppCatalogException {
        List<ApplicationInterfaceEntity> entities = applicationInterfaceRepository.findByGatewayId(gatewayId);
        return entities.stream()
                .map(e -> mapper.map(e, ApplicationInterfaceDescription.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAllApplicationInterfaceIds() throws AppCatalogException {
        List<String> applicationInterfaceIds = new ArrayList<>();
        List<ApplicationInterfaceEntity> entities = applicationInterfaceRepository.findAll();

        if (entities != null && !entities.isEmpty()) {
            logger.debug("The fetched list of Application Interfaces is not NULL or empty");
            for (ApplicationInterfaceEntity entity : entities) {
                applicationInterfaceIds.add(entity.getApplicationInterfaceId());
            }
        }

        return applicationInterfaceIds;
    }

    @Override
    public List<InputDataObjectType> getApplicationInputs(String interfaceId) throws AppCatalogException {
        List<ApplicationInputEntity> entities = applicationInputRepository.findByInterfaceId(interfaceId);
        return entities.stream()
                .map(e -> mapper.map(e, InputDataObjectType.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<OutputDataObjectType> getApplicationOutputs(String interfaceId) throws AppCatalogException {
        List<ApplicationOutputEntity> entities = applicationOutputRepository.findByInterfaceId(interfaceId);
        return entities.stream()
                .map(e -> mapper.map(e, OutputDataObjectType.class))
                .collect(Collectors.toList());
    }

    @Override
    public boolean removeApplicationInterface(String interfaceId) throws AppCatalogException {
        if (!applicationInterfaceRepository.existsById(interfaceId)) {
            return false;
        }
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

        if (applicationInterfaceDescription.getApplicationInterfaceId().trim().equals("")
                || applicationInterfaceDescription
                        .getApplicationInterfaceId()
                        .equals(airavata_commonsConstants.DEFAULT_ID)) {
            logger.debug(
                    "If Application Interface ID is empty or DEFAULT, set it as the Application Interface Name plus random UUID");
            applicationInterfaceDescription.setApplicationInterfaceId(
                    AiravataUtils.getId(applicationInterfaceDescription.getApplicationName()));
        }

        String applicationInterfaceId = applicationInterfaceDescription.getApplicationInterfaceId();
        ApplicationInterfaceEntity applicationInterfaceEntity =
                mapper.map(applicationInterfaceDescription, ApplicationInterfaceEntity.class);

        if (gatewayId != null) {
            logger.debug("Setting the gateway ID of the Application Interface");
            applicationInterfaceEntity.setGatewayId(gatewayId);
        }

        if (applicationInterfaceEntity.getApplicationInputs() != null) {
            logger.debug("Populating the Primary Key of ApplicationInputs objects for the Application Interface");
            applicationInterfaceEntity
                    .getApplicationInputs()
                    .forEach(applicationInputEntity -> applicationInputEntity.setInterfaceId(applicationInterfaceId));
        }

        if (applicationInterfaceEntity.getApplicationOutputs() != null) {
            logger.debug("Populating the Primary Key of ApplicationOutputs objects for the Application Interface");
            applicationInterfaceEntity
                    .getApplicationOutputs()
                    .forEach(applicationOutputEntity -> applicationOutputEntity.setInterfaceId(applicationInterfaceId));
        }

        if (!isApplicationInterfaceExists(applicationInterfaceId)) {
            logger.debug("Checking if the Application Interface already exists");
            applicationInterfaceEntity.setCreationTime(new Timestamp(System.currentTimeMillis()));
        }

        applicationInterfaceEntity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        return applicationInterfaceRepository.save(applicationInterfaceEntity);
    }

    private String saveApplicationModuleData(ApplicationModule applicationModule, String gatewayId)
            throws AppCatalogException {
        ApplicationModuleEntity applicationModuleEntity = saveApplicationModule(applicationModule, gatewayId);
        return applicationModuleEntity.getAppModuleId();
    }

    private ApplicationModuleEntity saveApplicationModule(ApplicationModule applicationModule, String gatewayId)
            throws AppCatalogException {

        if (applicationModule.getAppModuleId().trim().equals("")
                || applicationModule.getAppModuleId().equals(airavata_commonsConstants.DEFAULT_ID)) {
            logger.debug(
                    "If Application Module ID is empty or DEFAULT, set it as the Application Module Name plus random UUID");
            applicationModule.setAppModuleId(AiravataUtils.getId(applicationModule.getAppModuleName()));
        }

        String applicationModuleId = applicationModule.getAppModuleId();
        ApplicationModuleEntity applicationModuleEntity = mapper.map(applicationModule, ApplicationModuleEntity.class);

        if (gatewayId != null) {
            logger.debug("Setting the gateway ID of the Application Module");
            applicationModuleEntity.setGatewayId(gatewayId);
        }

        if (!isApplicationModuleExists(applicationModuleId)) {
            logger.debug("Checking if the Application Module already exists");
            applicationModuleEntity.setCreationTime(new Timestamp(System.currentTimeMillis()));
        }

        applicationModuleEntity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        return applicationModuleRepository.save(applicationModuleEntity);
    }
}
