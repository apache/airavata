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
import org.apache.airavata.models.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.models.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.application.mapper.ApplicationMapper;
import org.apache.airavata.application.model.ApplicationDeploymentEntity;
import org.apache.airavata.compute.repository.ComputeResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ApplicationDeploymentRepository
        extends AbstractRepository<ApplicationDeploymentDescription, ApplicationDeploymentEntity, String> {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationDeploymentRepository.class);

    private final ComputeResourceRepository computeResource;

    public ApplicationDeploymentRepository(ComputeResourceRepository computeResource) {
        super(ApplicationDeploymentDescription.class, ApplicationDeploymentEntity.class);
        this.computeResource = computeResource;
    }

    @Override
    protected ApplicationDeploymentDescription toModel(ApplicationDeploymentEntity entity) {
        return ApplicationMapper.INSTANCE.appDeploymentToModel(entity);
    }

    @Override
    protected ApplicationDeploymentEntity toEntity(ApplicationDeploymentDescription model) {
        return ApplicationMapper.INSTANCE.appDeploymentToEntity(model);
    }

    protected String saveApplicationDeploymentDescriptorData(
            ApplicationDeploymentDescription applicationDeploymentDescription, String gatewayId)
            throws Exception {
        ApplicationDeploymentEntity applicationDeploymentEntity = saveApplicationDeployment(
                applicationDeploymentDescription, gatewayId);
        return applicationDeploymentEntity.getAppDeploymentId();
    }

    protected ApplicationDeploymentEntity saveApplicationDeployment(
            ApplicationDeploymentDescription applicationDeploymentDescription, String gatewayId)
            throws Exception {

        if (applicationDeploymentDescription.appDeploymentId().trim().isEmpty()
                || applicationDeploymentDescription.appDeploymentId().equals("DO_NOT_SET_AT_CLIENTS")) {
            logger.debug(
                    "If Application Deployment ID is empty or DEFAULT, set it as the compute host name plus the App Module ID");
            ComputeResourceDescription computeResourceDescription = computeResource
                    .getComputeResource(applicationDeploymentDescription.computeHostId());
            applicationDeploymentDescription = applicationDeploymentDescription.toBuilder()
                    .setAppDeploymentId(computeResourceDescription.hostName() + "_"
                            + applicationDeploymentDescription.appModuleId())
                    .build();
        }

        String applicationDeploymentId = applicationDeploymentDescription.appDeploymentId();
        ApplicationDeploymentEntity applicationDeploymentEntity = ApplicationMapper.INSTANCE
                .appDeploymentToEntity(applicationDeploymentDescription);

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

    public String addApplicationDeployment(
            ApplicationDeploymentDescription applicationDeploymentDescription, String gatewayId)
            throws Exception {
        return saveApplicationDeploymentDescriptorData(applicationDeploymentDescription, gatewayId);
    }

    public void updateApplicationDeployment(
            String deploymentId, ApplicationDeploymentDescription updatedApplicationDeploymentDescription)
            throws Exception {
        saveApplicationDeploymentDescriptorData(updatedApplicationDeploymentDescription, null);
    }

    public ApplicationDeploymentDescription getApplicationDeployement(String deploymentId) throws Exception {
        return get(deploymentId);
    }

    public List<ApplicationDeploymentDescription> getApplicationDeployments(Map<String, String> filters)
            throws Exception {

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
                        ids.add(applicationDeploymentDescription.appDeploymentId());
                    }
                    List<ApplicationDeploymentDescription> tmp2Descriptions = new ArrayList<>();
                    for (ApplicationDeploymentDescription applicationDeploymentDescription : tmpDescriptions) {
                        if (ids.contains(applicationDeploymentDescription.appDeploymentId())) {
                            tmp2Descriptions.add(applicationDeploymentDescription);
                        }
                    }
                    deploymentDescriptions.clear();
                    deploymentDescriptions.addAll(tmp2Descriptions);
                }
            }
        } catch (Exception e) {
            logger.error("Error while retrieving app deployment list...", e);
            throw new Exception(e);
        }
        return deploymentDescriptions;
    }

    public List<ApplicationDeploymentDescription> getAllApplicationDeployements(String gatewayId)
            throws Exception {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.ApplicationDeployment.GATEWAY_ID, gatewayId);
        return select(QueryConstants.FIND_APPLICATION_DEPLOYMENTS_FOR_GATEWAY_ID, -1, 0, queryParameters);
    }

    public List<ApplicationDeploymentDescription> getAccessibleApplicationDeployments(
            String gatewayId, List<String> accessibleAppIds, List<String> accessibleCompHostIds)
            throws Exception {
        if (accessibleAppIds.isEmpty() || accessibleCompHostIds.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.ApplicationDeployment.GATEWAY_ID, gatewayId);
        queryParameters.put(DBConstants.ApplicationDeployment.ACCESSIBLE_APPLICATION_DEPLOYMENT_IDS, accessibleAppIds);
        queryParameters.put(DBConstants.ApplicationDeployment.ACCESSIBLE_COMPUTE_HOST_IDS, accessibleCompHostIds);
        return select(QueryConstants.FIND_ACCESSIBLE_APPLICATION_DEPLOYMENTS, -1, 0, queryParameters);
    }

    public List<ApplicationDeploymentDescription> getAccessibleApplicationDeployments(
            String gatewayId,
            String appModuleId,
            List<String> accessibleAppIds,
            List<String> accessibleComputeResourceIds)
            throws Exception {
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

    public List<String> getAllApplicationDeployementIds() {
        List<String> applicationDeploymentIds = new ArrayList<>();
        List<ApplicationDeploymentDescription> applicationDeploymentDescriptionList = select(
                QueryConstants.GET_ALL_APPLICATION_DEPLOYMENTS, 0);

        if (applicationDeploymentDescriptionList != null && !applicationDeploymentDescriptionList.isEmpty()) {
            logger.debug("The fetched list of Application Deployment is not NULL or empty");
            for (ApplicationDeploymentDescription applicationDeploymentDescription : applicationDeploymentDescriptionList) {
                applicationDeploymentIds.add(applicationDeploymentDescription.appDeploymentId());
            }
        }
        return applicationDeploymentIds;
    }

    public boolean isAppDeploymentExists(String deploymentId) {
        return isExists(deploymentId);
    }

    public void removeAppDeployment(String deploymentId) throws Exception {
        delete(deploymentId);
    }
}
