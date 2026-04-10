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
package org.apache.airavata.research.repository;

import java.sql.Timestamp;
import java.util.*;
import org.apache.airavata.db.AbstractRepository;
import org.apache.airavata.db.DBConstants;
import org.apache.airavata.db.QueryConstants;
import org.apache.airavata.interfaces.AppCatalogException;
import org.apache.airavata.interfaces.ApplicationDeployment;
import org.apache.airavata.interfaces.ComputeResource;
import org.apache.airavata.model.appcatalog.appdeployment.proto.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.computeresource.proto.ComputeResourceDescription;
import org.apache.airavata.research.mapper.ResearchMapper;
import org.apache.airavata.research.model.ApplicationDeploymentEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ApplicationDeploymentRepository
        extends AbstractRepository<ApplicationDeploymentDescription, ApplicationDeploymentEntity, String>
        implements ApplicationDeployment {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationDeploymentRepository.class);

    private ComputeResource computeResource;

    public ApplicationDeploymentRepository() {
        super(ApplicationDeploymentDescription.class, ApplicationDeploymentEntity.class);
    }

    public ApplicationDeploymentRepository(ComputeResource computeResource) {
        super(ApplicationDeploymentDescription.class, ApplicationDeploymentEntity.class);
        this.computeResource = computeResource;
    }

    @Override
    protected ApplicationDeploymentDescription toModel(ApplicationDeploymentEntity entity) {
        return ResearchMapper.INSTANCE.appDeploymentToModel(entity);
    }

    @Override
    protected ApplicationDeploymentEntity toEntity(ApplicationDeploymentDescription model) {
        return ResearchMapper.INSTANCE.appDeploymentToEntity(model);
    }

    protected String saveApplicationDeploymentDescriptorData(
            ApplicationDeploymentDescription applicationDeploymentDescription, String gatewayId)
            throws AppCatalogException {
        ApplicationDeploymentEntity applicationDeploymentEntity =
                saveApplicationDeployment(applicationDeploymentDescription, gatewayId);
        return applicationDeploymentEntity.getAppDeploymentId();
    }

    protected ApplicationDeploymentEntity saveApplicationDeployment(
            ApplicationDeploymentDescription applicationDeploymentDescription, String gatewayId)
            throws AppCatalogException {

        if (applicationDeploymentDescription.getAppDeploymentId().trim().isEmpty()
                || applicationDeploymentDescription.getAppDeploymentId().equals("DO_NOT_SET_AT_CLIENTS")) {
            logger.debug(
                    "If Application Deployment ID is empty or DEFAULT, set it as the compute host name plus the App Module ID");
            ComputeResourceDescription computeResourceDescription =
                    computeResource.getComputeResource(applicationDeploymentDescription.getComputeHostId());
            applicationDeploymentDescription = applicationDeploymentDescription.toBuilder()
                    .setAppDeploymentId(computeResourceDescription.getHostName() + "_"
                            + applicationDeploymentDescription.getAppModuleId())
                    .build();
        }

        String applicationDeploymentId = applicationDeploymentDescription.getAppDeploymentId();
        ApplicationDeploymentEntity applicationDeploymentEntity =
                ResearchMapper.INSTANCE.appDeploymentToEntity(applicationDeploymentDescription);

        if (gatewayId != null) {
            logger.debug("Setting the gateway ID of the Application Deployment");
            applicationDeploymentEntity.setGatewayId(gatewayId);
        }

        if (!isAppDeploymentExists(applicationDeploymentId)) {
            logger.debug("Checking if the Application Deployment already exists");
            applicationDeploymentEntity.setCreationTime(new Timestamp(System.currentTimeMillis()));
        }

        applicationDeploymentEntity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        return execute(entityManager -> entityManager.merge(applicationDeploymentEntity));
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
        return get(deploymentId);
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

                        Map<String, Object> queryParameters = new HashMap<>();
                        queryParameters.put(
                                DBConstants.ApplicationDeployment.APPLICATION_MODULE_ID, filters.get(fieldName));
                        tmpDescriptions = select(
                                QueryConstants.FIND_APPLICATION_DEPLOYMENTS_FOR_APPLICATION_MODULE_ID,
                                -1,
                                0,
                                queryParameters);
                        break;
                    }

                    case DBConstants.ApplicationDeployment.COMPUTE_HOST_ID: {
                        logger.debug(
                                "Fetching Application Deployments for Compute Host ID {}",
                                filters.get(DBConstants.ApplicationDeployment.COMPUTE_HOST_ID));

                        Map<String, Object> queryParameters = new HashMap<>();
                        queryParameters.put(DBConstants.ApplicationDeployment.COMPUTE_HOST_ID, filters.get(fieldName));
                        tmpDescriptions = select(
                                QueryConstants.FIND_APPLICATION_DEPLOYMENTS_FOR_COMPUTE_HOST_ID,
                                -1,
                                0,
                                queryParameters);
                        break;
                    }

                    default:
                        logger.error("Unsupported field name for app deployment in filters: {}", filters);
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
        } catch (Exception e) {
            logger.error("Error while retrieving app deployment list...", e);
            throw new AppCatalogException(e);
        }
        return deploymentDescriptions;
    }

    @Override
    public List<ApplicationDeploymentDescription> getAllApplicationDeployements(String gatewayId)
            throws AppCatalogException {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.ApplicationDeployment.GATEWAY_ID, gatewayId);
        return select(QueryConstants.FIND_APPLICATION_DEPLOYMENTS_FOR_GATEWAY_ID, -1, 0, queryParameters);
    }

    @Override
    public List<ApplicationDeploymentDescription> getAccessibleApplicationDeployments(
            String gatewayId, List<String> accessibleAppIds, List<String> accessibleCompHostIds)
            throws AppCatalogException {
        if (accessibleAppIds.isEmpty() || accessibleCompHostIds.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.ApplicationDeployment.GATEWAY_ID, gatewayId);
        queryParameters.put(DBConstants.ApplicationDeployment.ACCESSIBLE_APPLICATION_DEPLOYMENT_IDS, accessibleAppIds);
        queryParameters.put(DBConstants.ApplicationDeployment.ACCESSIBLE_COMPUTE_HOST_IDS, accessibleCompHostIds);
        return select(QueryConstants.FIND_ACCESSIBLE_APPLICATION_DEPLOYMENTS, -1, 0, queryParameters);
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
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.ApplicationDeployment.GATEWAY_ID, gatewayId);
        queryParameters.put(DBConstants.ApplicationDeployment.APPLICATION_MODULE_ID, appModuleId);
        queryParameters.put(DBConstants.ApplicationDeployment.ACCESSIBLE_APPLICATION_DEPLOYMENT_IDS, accessibleAppIds);
        queryParameters.put(
                DBConstants.ApplicationDeployment.ACCESSIBLE_COMPUTE_HOST_IDS, accessibleComputeResourceIds);
        return select(QueryConstants.FIND_ACCESSIBLE_APPLICATION_DEPLOYMENTS_FOR_APP_MODULE, -1, 0, queryParameters);
    }

    @Override
    public List<String> getAllApplicationDeployementIds() {
        List<String> applicationDeploymentIds = new ArrayList<>();
        List<ApplicationDeploymentDescription> applicationDeploymentDescriptionList =
                select(QueryConstants.GET_ALL_APPLICATION_DEPLOYMENTS, 0);

        if (applicationDeploymentDescriptionList != null && !applicationDeploymentDescriptionList.isEmpty()) {
            logger.debug("The fetched list of Application Deployment is not NULL or empty");
            for (ApplicationDeploymentDescription applicationDeploymentDescription :
                    applicationDeploymentDescriptionList) {
                applicationDeploymentIds.add(applicationDeploymentDescription.getAppDeploymentId());
            }
        }
        return applicationDeploymentIds;
    }

    @Override
    public boolean isAppDeploymentExists(String deploymentId) {
        return isExists(deploymentId);
    }

    @Override
    public void removeAppDeployment(String deploymentId) throws AppCatalogException {
        delete(deploymentId);
    }
}
