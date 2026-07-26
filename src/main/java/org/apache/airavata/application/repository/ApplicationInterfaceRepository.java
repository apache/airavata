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
package org.apache.airavata.application.repository;

import java.sql.Timestamp;
import java.util.*;

import org.apache.airavata.db.AbstractRepository;
import org.apache.airavata.db.DBConstants;
import org.apache.airavata.db.QueryConstants;
import org.apache.airavata.application.mapper.ApplicationMapper;
import org.apache.airavata.models.appcatalog.appdeployment.ApplicationModule;
import org.apache.airavata.models.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.models.application.io.InputDataObjectType;
import org.apache.airavata.models.application.io.OutputDataObjectType;
import org.apache.airavata.application.model.ApplicationInterfaceEntity;
import org.apache.airavata.application.model.ApplicationModuleEntity;
import org.apache.airavata.common.AiravataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ApplicationInterfaceRepository
        extends AbstractRepository<ApplicationInterfaceDescription, ApplicationInterfaceEntity, String> {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationInterfaceRepository.class);

    public ApplicationInterfaceRepository() {
        super(ApplicationInterfaceDescription.class, ApplicationInterfaceEntity.class);
    }

    @Override
    protected ApplicationInterfaceDescription toModel(ApplicationInterfaceEntity entity) {
        return ApplicationMapper.INSTANCE.appInterfaceToModel(entity);
    }

    @Override
    protected ApplicationInterfaceEntity toEntity(ApplicationInterfaceDescription model) {
        return ApplicationMapper.INSTANCE.appInterfaceToEntity(model);
    }

    protected String saveApplicationInterfaceDescriptorData(
            ApplicationInterfaceDescription applicationInterfaceDescription, String gatewayId)
            throws Exception {
        ApplicationInterfaceEntity applicationInterfaceEntity = saveApplicationInterface(
                applicationInterfaceDescription, gatewayId);
        return applicationInterfaceEntity.getApplicationInterfaceId();
    }

    protected ApplicationInterfaceEntity saveApplicationInterface(
            ApplicationInterfaceDescription applicationInterfaceDescription, String gatewayId)
            throws Exception {

        if (applicationInterfaceDescription.applicationInterfaceId().trim().equals("")
                || applicationInterfaceDescription.applicationInterfaceId().equals("DO_NOT_SET_AT_CLIENTS")) {
            logger.debug(
                    "If Application Interface ID is empty or DEFAULT, set it as the Application Interface Name plus random UUID");
            applicationInterfaceDescription = applicationInterfaceDescription.toBuilder()
                    .setApplicationInterfaceId(
                            AiravataUtils.getId(applicationInterfaceDescription.applicationName()))
                    .build();
        }

        String applicationInterfaceId = applicationInterfaceDescription.applicationInterfaceId();
        ApplicationInterfaceEntity applicationInterfaceEntity = ApplicationMapper.INSTANCE
                .appInterfaceToEntity(applicationInterfaceDescription);

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
        return execute(entityManager -> entityManager.merge(applicationInterfaceEntity));
    }

    public String addApplicationInterface(
            ApplicationInterfaceDescription applicationInterfaceDescription, String gatewayId)
            throws Exception {
        return saveApplicationInterfaceDescriptorData(applicationInterfaceDescription, gatewayId);
    }

    public void updateApplicationInterface(
            String interfaceId, ApplicationInterfaceDescription updatedApplicationInterfaceDescription)
            throws Exception {
        saveApplicationInterfaceDescriptorData(updatedApplicationInterfaceDescription, null);
    }

    public ApplicationInterfaceDescription getApplicationInterface(String interfaceId) throws Exception {
        return get(interfaceId);
    }

    public List<ApplicationInterfaceDescription> getApplicationInterfaces(Map<String, String> filters)
            throws Exception {
        if (filters.containsKey(DBConstants.ApplicationInterface.APPLICATION_NAME)) {
            logger.debug("Fetching Application Interfaces for given Application Name");
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(
                    DBConstants.ApplicationInterface.APPLICATION_NAME,
                    filters.get(DBConstants.ApplicationInterface.APPLICATION_NAME));
            List<ApplicationInterfaceDescription> applicationInterfaceDescriptionList = select(
                    QueryConstants.FIND_APPLICATION_INTERFACES_FOR_APPLICATION_NAME, -1, 0, queryParameters);
            return applicationInterfaceDescriptionList;
        } else {
            logger.error("Unsupported field name for app interface.");
            throw new IllegalArgumentException("Unsupported field name for app interface.");
        }
    }

    public List<ApplicationInterfaceDescription> getAllApplicationInterfaces(String gatewayId)
            throws Exception {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.ApplicationInterface.GATEWAY_ID, gatewayId);
        List<ApplicationInterfaceDescription> applicationInterfaceDescriptionList = select(
                QueryConstants.FIND_APPLICATION_INTERFACES_FOR_GATEWAY_ID, -1, 0, queryParameters);
        return applicationInterfaceDescriptionList;
    }

    public List<String> getAllApplicationInterfaceIds() throws Exception {
        List<String> applicationInterfaceIds = new ArrayList<>();
        List<ApplicationInterfaceDescription> applicationInterfaceDescriptionList = select(
                QueryConstants.GET_ALL_APPLICATION_INTERFACES, 0);

        if (applicationInterfaceDescriptionList != null && !applicationInterfaceDescriptionList.isEmpty()) {
            logger.debug("The fetched list of Application Interfaces is not NULL or empty");
            for (ApplicationInterfaceDescription applicationDeploymentDescription : applicationInterfaceDescriptionList) {
                applicationInterfaceIds.add(applicationDeploymentDescription.applicationInterfaceId());
            }
        }

        return applicationInterfaceIds;
    }

    public List<InputDataObjectType> getApplicationInputs(String interfaceId) throws Exception {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.ApplicationInput.APPLICATION_INTERFACE_ID, interfaceId);
        ApplicationInputRepository applicationInputRepository = new ApplicationInputRepository();
        List<InputDataObjectType> applicationInputsList = applicationInputRepository.select(
                "SELECT p FROM AppIoParamEntity p WHERE p.interfaceId LIKE :"
                        + DBConstants.ApplicationInput.APPLICATION_INTERFACE_ID
                        + " AND p.direction = 'INPUT'",
                -1,
                0,
                queryParameters);
        return applicationInputsList;
    }

    public List<OutputDataObjectType> getApplicationOutputs(String interfaceId) throws Exception {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.ApplicationOutput.APPLICATION_INTERFACE_ID, interfaceId);
        ApplicationOutputRepository applicationOutputRepository = new ApplicationOutputRepository();
        List<OutputDataObjectType> applicationOutputsList = applicationOutputRepository.select(
                "SELECT p FROM AppIoParamEntity p WHERE p.interfaceId LIKE :"
                        + DBConstants.ApplicationOutput.APPLICATION_INTERFACE_ID
                        + " AND p.direction = 'OUTPUT'",
                -1,
                0,
                queryParameters);
        return applicationOutputsList;
    }

    public boolean removeApplicationInterface(String interfaceId) throws Exception {
        return delete(interfaceId);
    }

    public boolean isApplicationInterfaceExists(String interfaceId) throws Exception {
        return isExists(interfaceId);
    }

}
