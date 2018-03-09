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
import org.apache.airavata.registry.core.utils.*;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.registry.cpi.ApplicationInterface;
import org.apache.airavata.registry.core.entities.appcatalog.*;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationInterfaceRepository extends AppCatAbstractRepository<ApplicationInterfaceDescription, ApplicationInterfaceEntity, String> implements ApplicationInterface {
    private final static Logger logger = LoggerFactory.getLogger(ApplicationInterfaceRepository.class);

    public ApplicationInterfaceRepository () {
        super(ApplicationInterfaceDescription.class, ApplicationInterfaceEntity.class);
    }

    protected String saveApplicationInterfaceDescriptorData(
            ApplicationInterfaceDescription applicationInterfaceDescription, String gatewayId) throws AppCatalogException {
        ApplicationInterfaceEntity applicationInterfaceEntity = saveApplicationInterface(applicationInterfaceDescription, gatewayId);
        return applicationInterfaceEntity.getApplicationInterfaceId();
    }

    protected ApplicationInterfaceEntity saveApplicationInterface(
            ApplicationInterfaceDescription applicationInterfaceDescription, String gatewayId) throws AppCatalogException {
        String applicationInterfaceId = applicationInterfaceDescription.getApplicationInterfaceId();
        Mapper mapper = ObjectMapperSingleton.getInstance();
        ApplicationInterfaceEntity applicationInterfaceEntity = mapper.map(applicationInterfaceDescription, ApplicationInterfaceEntity.class);
        if (gatewayId != null)
            applicationInterfaceEntity.setGatewayId(gatewayId);
        if (applicationInterfaceEntity.getApplicationInputs() != null) {
            applicationInterfaceEntity.getApplicationInputs().forEach(applicationInputEntity -> applicationInputEntity.setInterfaceId(applicationInterfaceId));
        }
        if (applicationInterfaceEntity.getApplicationOutputs() != null) {
            applicationInterfaceEntity.getApplicationOutputs().forEach(applicationOutputEntity -> applicationOutputEntity.setInterfaceId(applicationInterfaceId));
        }
        if (!isApplicationInterfaceExists(applicationInterfaceId))
            applicationInterfaceEntity.setCreationTime(new Timestamp(System.currentTimeMillis()));
        applicationInterfaceEntity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        return execute(entityManager -> entityManager.merge(applicationInterfaceEntity));
    }

    protected String saveApplicationModuleData(
            ApplicationModule applicationModule, String gatewayId) throws AppCatalogException {
        ApplicationModuleEntity applicationModuleEntity = saveApplicationModule(applicationModule, gatewayId);
        return applicationModuleEntity.getAppModuleId();
    }

    protected ApplicationModuleEntity saveApplicationModule(
            ApplicationModule applicationModule, String gatewayId) throws AppCatalogException {
        String applicationModuleId = applicationModule.getAppModuleId();
        Mapper mapper = ObjectMapperSingleton.getInstance();
        ApplicationModuleEntity applicationModuleEntity = mapper.map(applicationModule, ApplicationModuleEntity.class);
        if (gatewayId != null)
            applicationModuleEntity.setGatewayId(gatewayId);
        if (!applicationModuleEntity.getAppModuleId().equals("") && !applicationModule.getAppModuleId().equals(application_interface_modelConstants.DEFAULT_ID)) {
            applicationModuleEntity.setAppModuleId(applicationModule.getAppModuleId());
        } else {
            applicationModuleEntity.setAppModuleId(applicationModule.getAppModuleName());
        }
        if (!isApplicationModuleExists(applicationModuleId))
            applicationModuleEntity.setCreationTime(new Timestamp(System.currentTimeMillis()));
        applicationModuleEntity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        return execute(entityManager -> entityManager.merge(applicationModuleEntity));
    }

    @Override
    public String addApplicationModule(ApplicationModule applicationModule, String gatewayId) throws AppCatalogException {
        return saveApplicationModuleData(applicationModule, gatewayId);
    }

    @Override
    public String addApplicationInterface(ApplicationInterfaceDescription applicationInterfaceDescription, String gatewayId) throws AppCatalogException {
        return saveApplicationInterfaceDescriptorData(applicationInterfaceDescription, gatewayId);
    }

    @Override
    public void addApplicationModuleMapping(String moduleId, String interfaceId) throws AppCatalogException {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        ApplicationModule applicationModule = getApplicationModule(moduleId);
        ApplicationInterfaceDescription applicationInterfaceDescription = getApplicationInterface(interfaceId);
        ApplicationModuleEntity applicationModuleEntity = mapper.map(applicationModule, ApplicationModuleEntity.class);
        ApplicationInterfaceEntity applicationInterfaceEntity = mapper.map(applicationInterfaceDescription, ApplicationInterfaceEntity.class);
        AppModuleMappingEntity appModuleMappingEntity = new AppModuleMappingEntity();
        appModuleMappingEntity.setModuleId(moduleId);
        appModuleMappingEntity.setInterfaceId(interfaceId);
        appModuleMappingEntity.setApplicationModule(applicationModuleEntity);
        appModuleMappingEntity.setApplicationInterface(applicationInterfaceEntity);
        execute(entityManager -> entityManager.merge(appModuleMappingEntity));
    }

    @Override
    public void updateApplicationModule(String moduleId, ApplicationModule updatedApplicationModule) throws AppCatalogException {
        saveApplicationModuleData(updatedApplicationModule, null);
    }

    @Override
    public void updateApplicationInterface(String interfaceId, ApplicationInterfaceDescription updatedApplicationInterfaceDescription) throws AppCatalogException {
        saveApplicationInterfaceDescriptorData(updatedApplicationInterfaceDescription, null);
    }

    @Override
    public ApplicationModule getApplicationModule(String moduleId) throws AppCatalogException {
        ApplicationModuleRepository applicationModuleRepository = new ApplicationModuleRepository();
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.ApplicationModule.APPLICATION_MODULE_ID, moduleId);
        ApplicationModule applicationModule = applicationModuleRepository.select(QueryConstants.FIND_APPLICATION_MODULE, -1, 0, queryParameters).get(0);
        return applicationModule;
    }

    @Override
    public ApplicationInterfaceDescription getApplicationInterface(String interfaceId) throws AppCatalogException {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.ApplicationInterface.APPLICATION_INTERFACE_ID, interfaceId);
        ApplicationInterfaceDescription applicationInterfaceDescription = select(QueryConstants.FIND_APPLICATION_INTERFACE, -1, 0, queryParameters).get(0);
        return applicationInterfaceDescription;
    }

    @Override
    public List<ApplicationModule> getApplicationModules(Map<String, String> filters) throws AppCatalogException {
        ApplicationModuleRepository applicationModuleRepository = new ApplicationModuleRepository();
        if(filters.containsKey(DBConstants.ApplicationModule.APPLICATION_MODULE_NAME)) {
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(DBConstants.ApplicationModule.APPLICATION_MODULE_NAME, filters.get(DBConstants.ApplicationModule.APPLICATION_MODULE_NAME));
            List<ApplicationModule> applicationModuleList = applicationModuleRepository.select(QueryConstants.FIND_APPLICATION_MODULES_FOR_APPLICATION_MODULE_NAME, -1,0, queryParameters);
            return applicationModuleList;
        } else {
            logger.error("Unsupported field name for app module.", new IllegalArgumentException());
            throw new IllegalArgumentException("Unsupported field name for app module.");
        }
    }

    @Override
    public List<ApplicationModule> getAllApplicationModules(String gatewayId) throws AppCatalogException {
        ApplicationModuleRepository applicationModuleRepository = new ApplicationModuleRepository();
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.ApplicationModule.GATEWAY_ID, gatewayId);
        List<ApplicationModule> applicationModuleList = applicationModuleRepository.select(QueryConstants.FIND_APPLICATION_MODULES_FOR_GATEWAY_ID, -1, 0, queryParameters);
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
    public List<ApplicationModule> getAccessibleApplicationModules(String gatewayId, List<String> accessibleAppIds, List<String> accessibleCompHostIds) throws AppCatalogException {
        ApplicationModuleRepository applicationModuleRepository = new ApplicationModuleRepository();
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.ApplicationModule.GATEWAY_ID, gatewayId);
        queryParameters.put(DBConstants.ApplicationModule.ACCESSIBLE_APPLICATION_MODULE_IDS, accessibleAppIds);
        queryParameters.put(DBConstants.ApplicationDeployment.ACCESSIBLE_COMPUTE_HOST_IDS, accessibleCompHostIds);
        List<ApplicationModule> accessibleApplicationModules = applicationModuleRepository.select(QueryConstants.FIND_ACCESSIBLE_APPLICATION_MODULES, -1, 0, queryParameters);
        return accessibleApplicationModules;
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
        queryParameters.put(DBConstants.ApplicationInput.APPLICATION_INTERFACE_ID, interfaceId);
        ApplicationInputRepository applicationInputRepository = new ApplicationInputRepository();
        List<InputDataObjectType> applicationInputsList = applicationInputRepository.select(QueryConstants.FIND_APPLICATION_INPUTS, -1, 0, queryParameters);
        return applicationInputsList;
    }

    @Override
    public List<OutputDataObjectType> getApplicationOutputs(String interfaceId) throws AppCatalogException {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.ApplicationOutput.APPLICATION_INTERFACE_ID, interfaceId);
        ApplicationOutputRepository applicationOutputRepository = new ApplicationOutputRepository();
        List<OutputDataObjectType> applicationOutputsList = applicationOutputRepository.select(QueryConstants.FIND_APPLICATION_OUTPUTS, -1, 0, queryParameters);
        return applicationOutputsList;
    }

    @Override
    public boolean removeApplicationInterface(String interfaceId) throws AppCatalogException {
        return delete(interfaceId);
    }

    @Override
    public boolean removeApplicationModule(String moduleId) throws AppCatalogException {
        ApplicationModuleRepository applicationModuleRepository = new ApplicationModuleRepository();
        return applicationModuleRepository.delete(moduleId);
    }

    @Override
    public boolean isApplicationInterfaceExists(String interfaceId) throws AppCatalogException {
        return isExists(interfaceId);
    }

    @Override
    public boolean isApplicationModuleExists(String moduleId) throws AppCatalogException {
        ApplicationModuleRepository applicationModuleRepository = new ApplicationModuleRepository();
        return applicationModuleRepository.isExists(moduleId);
    }

}
