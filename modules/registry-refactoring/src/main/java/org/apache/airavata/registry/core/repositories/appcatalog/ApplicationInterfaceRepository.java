/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.appdeployment.ApplicationModule;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.appinterface.application_interface_modelConstants;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.core.utils.QueryConstants;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.registry.cpi.ApplicationInterface;
import org.apache.airavata.registry.core.entities.appcatalog.*;
import org.apache.airavata.registry.core.utils.AppCatalogUtils;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationInterfaceRepository extends AppCatAbstractRepository<ApplicationInterfaceDescription, ApplicationInterfaceEntity, String> implements ApplicationInterface {
    private final static Logger logger = LoggerFactory.getLogger(ApplicationInterfaceRepository.class);

    public ApplicationInterfaceRepository () {
        super(ApplicationInterfaceDescription.class, ApplicationInterfaceEntity.class);
    }

    @Override
    public String addApplicationModule(ApplicationModule applicationModule, String gatewayId) throws AppCatalogException {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        ApplicationModuleEntity applicationModuleEntity = mapper.map(applicationModule, ApplicationModuleEntity.class);
        applicationModuleEntity.setModuleName(applicationModule.getAppModuleName());
        applicationModuleEntity.setGatewayId(gatewayId);
        if (!applicationModuleEntity.getModuleId().equals("") && !applicationModule.getAppModuleId().equals(application_interface_modelConstants.DEFAULT_ID)) {
            applicationModuleEntity.setModuleId(applicationModule.getAppModuleId());
        } else {
            applicationModuleEntity.setModuleId(applicationModule.getAppModuleName());
        }
        applicationModuleEntity.setModuleDesc(applicationModule.getAppModuleDescription());
        applicationModuleEntity.setModuleVersion(applicationModule.getAppModuleVersion());
        execute(entityManager -> entityManager.merge(applicationModuleEntity));
        applicationModule.setAppModuleId(applicationModuleEntity.getModuleId());
        return applicationModuleEntity.getModuleId();
    }

    @Override
    public String addApplicationInterface(ApplicationInterfaceDescription applicationInterfaceDescription, String gatewayId) throws AppCatalogException {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        ApplicationInterfaceEntity applicationInterfaceEntity = mapper.map(applicationInterfaceDescription, ApplicationInterfaceEntity.class);
        applicationInterfaceEntity.setApplicationName(applicationInterfaceDescription.getApplicationName());
        if (!applicationInterfaceDescription.getApplicationInterfaceId().equals("") && !applicationInterfaceDescription.getApplicationInterfaceId().equals(application_interface_modelConstants.DEFAULT_ID)){
            applicationInterfaceEntity.setInterfaceId(applicationInterfaceDescription.getApplicationInterfaceId());
        } else {
            applicationInterfaceEntity.setInterfaceId(AppCatalogUtils.getID(applicationInterfaceDescription.getApplicationName()));
        }
        applicationInterfaceEntity.setApplicationDescription(applicationInterfaceDescription.getApplicationDescription());
        applicationInterfaceEntity.setGatewayId(gatewayId);
        applicationInterfaceEntity.setArchiveWorkingDirectory(applicationInterfaceDescription.isArchiveWorkingDirectory());
        applicationInterfaceEntity.setHasOptionalFileInputs(applicationInterfaceDescription.isHasOptionalFileInputs());
        execute(entityManager -> entityManager.merge(applicationInterfaceEntity));
        applicationInterfaceDescription.setApplicationInterfaceId(applicationInterfaceEntity.getInterfaceId());

        List<String> applicationModules = applicationInterfaceDescription.getApplicationModules();
        if (applicationModules != null && !applicationModules.isEmpty()){
            for (String moduleId : applicationModules){
                ApplicationModuleEntity applicationModuleEntity = new ApplicationModuleEntity();
                AppModuleMappingEntity appModuleMappingEntity = new AppModuleMappingEntity();
                appModuleMappingEntity.setInterfaceId(applicationInterfaceEntity.getInterfaceId());
                appModuleMappingEntity.setModuleId(moduleId);
                appModuleMappingEntity.setApplicationModule((ApplicationModuleEntity) applicationModuleEntity.get(moduleId));
                appModuleMappingEntity.setApplicationInterface(applicationInterfaceEntity);
                execute(entityManager -> entityManager.merge(appModuleMappingEntity));
            }
        }

        List<InputDataObjectType> applicationInputs = applicationInterfaceDescription.getApplicationInputs();
        if (applicationInputs != null && !applicationInputs.isEmpty()){
            for (InputDataObjectType input : applicationInputs){
                ApplicationInputEntity applicationInputEntity = new ApplicationInputEntity();
                applicationInputEntity.setInputKey(input.getName());
                applicationInputEntity.setInterfaceId(applicationInterfaceEntity.getInterfaceId());
                applicationInputEntity.setApplicationInterface(applicationInterfaceEntity);
                applicationInputEntity.setUserFriendlyDesc(input.getUserFriendlyDescription());
                applicationInputEntity.setInputValue(input.getValue());
                applicationInputEntity.setDataType(input.getType().toString());
                applicationInputEntity.setMetadata(input.getMetaData());
                applicationInputEntity.setStandardInput(input.isStandardInput());
                applicationInputEntity.setAppArgument(input.getApplicationArgument());
                applicationInputEntity.setInputOrder(input.getInputOrder());
                applicationInputEntity.setIsRequired(input.isIsRequired());
                applicationInputEntity.setRequiredToCommandline(input.isRequiredToAddedToCommandLine());
                execute(entityManager -> entityManager.merge(applicationInputEntity));
            }
        }

        List<OutputDataObjectType> applicationOutputs = applicationInterfaceDescription.getApplicationOutputs();
        if (applicationOutputs != null && !applicationOutputs.isEmpty()) {
            for (OutputDataObjectType output : applicationOutputs) {
                ApplicationOutputEntity applicationOutputEntity = new ApplicationOutputEntity();
                applicationOutputEntity.setOutputKey(output.getName());
                applicationOutputEntity.setInterfaceId(applicationInterfaceEntity.getInterfaceId());
                applicationOutputEntity.setApplicationInterface(applicationInterfaceEntity);
                applicationOutputEntity.setOutputValue(output.getValue());
                applicationOutputEntity.setDataType(output.getType().toString());
                applicationOutputEntity.setIsRequired(output.isIsRequired());
                applicationOutputEntity.setRequiredToCommandline(output.isRequiredToAddedToCommandLine());
                applicationOutputEntity.setDataMovement(output.isDataMovement());
                applicationOutputEntity.setDataNameLocation(output.getLocation());
                applicationOutputEntity.setAppArgument(output.getApplicationArgument());
                applicationOutputEntity.setSearchQuery(output.getSearchQuery());
                applicationOutputEntity.setOutputStreaming(output.isOutputStreaming());
                execute(entityManager -> entityManager.merge(applicationOutputEntity));
            }
        }
        return applicationInterfaceEntity.getInterfaceId();
    }

    @Override
    public void addApplicationModuleMapping(String moduleId, String interfaceId) throws AppCatalogException {
        ApplicationModuleEntity applicationModuleEntity = new ApplicationModuleEntity();
        ApplicationInterfaceEntity applicationInterfaceEntity = new ApplicationInterfaceEntity();
        AppModuleMappingEntity appModuleMappingEntity = new AppModuleMappingEntity();
        appModuleMappingEntity.setModuleId(moduleId);
        appModuleMappingEntity.setInterfaceId(interfaceId);
        appModuleMappingEntity.setApplicationModule(get(moduleId));
        appModuleMappingEntity.setApplicationInterface(get(interfaceId));
        execute(entityManager -> entityManager.merge(appModuleMappingEntity));
    }

    @Override
    public void updateApplicationModule(String moduleId, ApplicationModule updatedModule) throws AppCatalogException {
        ApplicationModuleEntity applicationModuleEntity = new ApplicationModuleEntity();
        ApplicationModuleEntity existingModule = get(moduleId);
        existingModule.setModuleName(updatedModule.getAppModuleName());
        existingModule.setModuleDesc(updatedModule.getAppModuleDescription());
        existingModule.setModuleVersion(updatedModule.getAppModuleVersion());
        execute(entityManager -> entityManager.merge(existingModule));
    }

    @Override
    public void updateApplicationInterface(String interfaceId, ApplicationInterfaceDescription updatedInterface) throws AppCatalogException {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        ApplicationInterfaceEntity applicationInterfaceEntity = mapper.map(updatedInterface, ApplicationInterfaceEntity.class);
        ApplicationInterfaceEntity existingInterface = get(interfaceId);
        existingInterface.setApplicationName(applicationInterfaceEntity.getApplicationName());
        existingInterface.setApplicationDescription(applicationInterfaceEntity.getApplicationDescription());
        existingInterface.setHasOptionalFileInputs(applicationInterfaceEntity.getHasOptionalFileInputs());
        existingInterface.setArchiveWorkingDirectory(applicationInterfaceEntity.getArchiveWorkingDirectory());
        execute(entityManager -> entityManager.merge(existingInterface));

        // remove existing modules before adding
        Map<String, String> ids = new HashMap<>();
        ids.put(AppCatAbstractResource.AppModuleMappingConstants.INTERFACE_ID, interfaceId);
        AppModuleMappingEntity appModuleMappingEntity = new AppModuleMappingEntity();
        appModuleMappingEntity.remove(ids);
        List<String> applicationModules = updatedInterface.getApplicationModules();
        if (applicationModules != null && !applicationModules.isEmpty()) {
            for (String moduleId : applicationModules) {
                ApplicationModuleEntity applicationModuleEntity = new ApplicationModuleEntity();
                appModuleMappingEntity = new AppModuleMappingEntity();
                ids = new HashMap<>();
                ids.put(AppCatAbstractResource.AppModuleMappingConstants.MODULE_ID, moduleId);
                ids.put(AppCatAbstractResource.AppModuleMappingConstants.INTERFACE_ID, interfaceId);
                AppModuleMappingEntity existingMapping;
                if (!appModuleMappingEntity.isExists(ids)) {
                    existingMapping = new AppModuleMappingAppCatalogResourceAppCat();
                } else {
                    existingMapping = (AppModuleMappingAppCatalogResourceAppCat) appModuleMappingEntity.get(ids);
                }
                existingMapping.setInterfaceId(interfaceId);
                existingMapping.setModuleId(moduleId);
                existingMapping.setApplicationModule((ApplicationModuleEntity) applicationModuleEntity.get(moduleId));
                existingMapping.setApplicationInterface(existingInterface);
                execute(entityManager -> entityManager.merge(existingMapping));
            }
        }

        // remove existing application inputs
        ApplicationInputEntity applicationInputEntity = new ApplicationInputEntity();
        ids = new HashMap<>();
        ids.put(AppCatAbstractResource.AppInputConstants.INTERFACE_ID, interfaceId);
        applicationInputEntity.remove(ids);
        List<InputDataObjectType> applicationInputs = updatedInterface.getApplicationInputs();
        if (applicationInputs != null && !applicationInputs.isEmpty()) {
            for (InputDataObjectType input : applicationInputs) {
                applicationInputEntity = new ApplicationInputEntity();
                ids = new HashMap<>();
                ids.put(AppCatAbstractResource.AppInputConstants.INTERFACE_ID, interfaceId);
                ids.put(AppCatAbstractResource.AppInputConstants.INPUT_KEY, input.getName());
                if (applicationInputEntity.isExists(ids)) {
                    applicationInputEntity = (ApplicationInputEntity) applicationInputEntity.get(ids);
                }
                applicationInputEntity.setInputKey(input.getName());
                applicationInputEntity.setInterfaceId(interfaceId);
                applicationInputEntity.setApplicationInterface(existingInterface);
                applicationInputEntity.setUserFriendlyDesc(input.getUserFriendlyDescription());
                applicationInputEntity.setInputValue(input.getValue());
                applicationInputEntity.setDataType(input.getType().toString());
                applicationInputEntity.setMetadata(input.getMetaData());
                applicationInputEntity.setStandardInput(input.isStandardInput());
                applicationInputEntity.setAppArgument(input.getApplicationArgument());
                applicationInputEntity.setInputOrder(input.getInputOrder());
                applicationInputEntity.setIsRequired(input.isIsRequired());
                applicationInputEntity.setRequiredToCommandline(input.isRequiredToAddedToCommandLine());
                applicationInputEntity.setDataStaged(input.isDataStaged());
                applicationInputEntity.setIsReadOnly(input.isIsReadOnly());
                execute(entityManager -> entityManager.merge(applicationInputEntity));
            }
        }

        // remove existing app outputs before adding
        ApplicationOutputEntity applicationOutputEntity = new ApplicationOutputEntity();
        ids = new HashMap<>();
        ids.put(AppCatAbstractResource.AppOutputConstants.INTERFACE_ID, interfaceId);
        applicationOutputEntity.remove(ids);
        List<OutputDataObjectType> applicationOutputs = updatedInterface.getApplicationOutputs();
        if (applicationOutputs != null && !applicationOutputs.isEmpty()) {
            for (OutputDataObjectType output : applicationOutputs) {
                applicationOutputEntity = new ApplicationOutputEntity();
                ids = new HashMap<>();
                ids.put(AppCatAbstractResource.AppOutputConstants.INTERFACE_ID, interfaceId);
                ids.put(AppCatAbstractResource.AppOutputConstants.OUTPUT_KEY, output.getName());
                if (applicationOutputEntity.isExists(ids)) {
                    applicationOutputEntity = (ApplicationOutputEntity) applicationOutputEntity.get(ids);
                }
                applicationOutputEntity.setOutputKey(output.getName());
                applicationOutputEntity.setInterfaceId(interfaceId);
                applicationOutputEntity.setApplicationInterface(existingInterface);
                applicationOutputEntity.setOutputValue(output.getValue());
                applicationOutputEntity.setDataType(output.getType().toString());
                applicationOutputEntity.setIsRequired(output.isIsRequired());
                applicationOutputEntity.setRequiredToCommandline(output.isRequiredToAddedToCommandLine());
                applicationOutputEntity.setDataMovement(output.isDataMovement());
                applicationOutputEntity.setDataNameLocation(output.getLocation());
                applicationOutputEntity.setAppArgument(output.getApplicationArgument());
                applicationOutputEntity.setSearchQuery(output.getSearchQuery());
                applicationOutputEntity.setOutputStreaming(output.isOutputStreaming());
                execute(entityManager -> entityManager.merge(applicationOutputEntity));
            }
        }
    }

    @Override
    public ApplicationModule getApplicationModule(String moduleId) throws AppCatalogException {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.ApplicationModule.APPLICATION_MODULE_ID, moduleId);
        ApplicationModule applicationModule = (ApplicationModule) select(QueryConstants.FIND_APPLICATION_MODULE, -1, 0, queryParameters);
        return applicationModule;
    }

    @Override
    public ApplicationInterfaceDescription getApplicationInterface(String interfaceId) throws AppCatalogException {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.ApplicationInterface.APPLICATION_INTERFACE_ID, interfaceId);
        ApplicationInterfaceDescription applicationInterfaceDescription = (ApplicationInterfaceDescription) select(QueryConstants.FIND_APPLICATION_INTERFACE, -1, 0, queryParameters);
        return applicationInterfaceDescription;
    }

    @Override
    public List<ApplicationModule> getApplicationModules(Map<String, String> filters) throws AppCatalogException {
        if(filters.containsKey(DBConstants.ApplicationModule.APPLICATION_MODULE_NAME)) {
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(DBConstants.ApplicationModule.APPLICATION_MODULE_NAME, filters.get(DBConstants.ApplicationModule.APPLICATION_MODULE_NAME));
            List<ApplicationModule> applicationModuleList = select(QueryConstants.FIND_APPLICATION_MODULES_FOR_APPLICATION_MODULE_NAME, -1,0, queryParameters);
            return applicationModuleList;
        } else {
            logger.error("Unsupported field name for app module.", new IllegalArgumentException());
            throw new IllegalArgumentException("Unsupported field name for app module.");
        }
    }

    @Override
    public List<ApplicationModule> getAllApplicationModules(String gatewayId) throws AppCatalogException {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.ApplicationModule.GATEWAY_ID, gatewayId);
        List<ApplicationModule> applicationModuleList = select(QueryConstants.FIND_APPLICATION_MODULES_FOR_GATEWAY_ID, -1, 0, queryParameters);
        return applicationModuleList;
    }

    @Override
    public List<ApplicationInterfaceDescription> getApplicationInterfaces(Map<String, String> filters) throws AppCatalogException {
        if(filters.containsKey(DBConstants.ApplicationInterface.APPLICATION_NAME)) {
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(DBConstants.ApplicationInterface.APPLICATION_NAME, filters.get(DBConstants.ApplicationInterface.APPLICATION_NAME));
            List<ApplicationInterfaceDescription> applicationInterfaceDescriptionList = select(QueryConstants.FIND_APPLICATION_INTERFACES_FOR_APPLICATION_NAME, -1,0, queryParameters);
            return applicationInterfaceDescriptionList;
        } else {
            logger.error("Unsupported field name for app interface.", new IllegalArgumentException());
            throw new IllegalArgumentException("Unsupported field name for app interface.");
        }
    }

    @Override
    public List<ApplicationInterfaceDescription> getAllApplicationInterfaces(String gatewayId) throws AppCatalogException {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.ApplicationInterface.GATEWAY_ID, gatewayId);
        List<ApplicationInterfaceDescription> applicationInterfaceDescriptionList = select(QueryConstants.FIND_APPLICATION_INTERFACES_FOR_GATEWAY_ID, -1, 0, queryParameters);
        return applicationInterfaceDescriptionList;
    }

    @Override
    public List<String> getAllApplicationInterfaceIds() throws AppCatalogException {
        List<String> applicationInterfaceIds = new ArrayList<>();
        List<ApplicationInterfaceDescription> applicationInterfaceDescriptionList = select(QueryConstants.GET_ALL_APPLICATION_INTERFACES, 0);
        if (applicationInterfaceDescriptionList != null && !applicationInterfaceDescriptionList.isEmpty()) {
            for (ApplicationInterfaceDescription applicationDeploymentDescription: applicationInterfaceDescriptionList) {
                applicationInterfaceIds.add(applicationDeploymentDescription.getApplicationInterfaceId());
            }
        }
        return applicationInterfaceIds;
    }

    @Override
    public List<InputDataObjectType> getApplicationInputs(String interfaceId) throws AppCatalogException {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.ApplicationInputs.APPLICATION_INTERFACE_ID, interfaceId);
        List<InputDataObjectType> applicationInputsList = select(QueryConstants.FIND_APPLICATION_INPUTS, -1, 0, queryParameters);
        return applicationInputsList;
    }

    @Override
    public List<OutputDataObjectType> getApplicationOutputs(String interfaceId) throws AppCatalogException {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.ApplicationInputs.APPLICATION_INTERFACE_ID, interfaceId);
        List<OutputDataObjectType> applicationOutputsList = select(QueryConstants.FIND_APPLICATION_OUTPUTS, -1, 0, queryParameters);
        return applicationOutputsList;
    }

    @Override
    public boolean removeApplicationInterface(String interfaceId) throws AppCatalogException {
        return delete(interfaceId);
    }

    @Override
    public boolean removeApplicationModule(String moduleId) throws AppCatalogException {
        return delete(moduleId);
    }

    @Override
    public boolean isApplicationInterfaceExists(String interfaceId) throws AppCatalogException {
        return isExists(interfaceId);
    }

    @Override
    public boolean isApplicationModuleExists(String moduleId) throws AppCatalogException {
        return isExists(moduleId);
    }

}
