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
package org.apache.airavata.agent.connection.service.services;

import io.micrometer.common.util.StringUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.airavata.agent.connection.service.UserContext;
import org.apache.airavata.common.model.ComputeResourceType;
import org.apache.airavata.common.model.ExperimentSummaryModel;
import org.apache.airavata.common.model.GroupComputeResourcePreference;
import org.apache.airavata.common.model.GroupResourceProfile;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.thriftapi.client.AiravataServiceClientFactory;
import org.apache.airavata.thriftapi.exception.AiravataClientException;
import org.apache.airavata.thriftapi.exception.AiravataSystemException;
import org.apache.airavata.thriftapi.exception.AuthorizationException;
import org.apache.airavata.thriftapi.exception.InvalidRequestException;
import org.apache.airavata.thriftapi.service.Airavata;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AiravataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiravataService.class);

    @Value("${airavata.server.url:localhost}")
    private String serverUrl;

    @Value("${services.thrift.port:8930}")
    private int port;

    @Value("${airavata.server.secure:false}")
    private boolean secure;

    private final AiravataServerProperties properties;

    public AiravataService(AiravataServerProperties properties) {
        this.properties = properties;
    }

    private org.apache.airavata.thriftapi.security.model.AuthzToken convertAuthzToken(
            org.apache.airavata.security.model.AuthzToken domainToken) {
        org.apache.airavata.thriftapi.security.model.AuthzToken thriftToken =
                new org.apache.airavata.thriftapi.security.model.AuthzToken();
        thriftToken.setAccessToken(domainToken.getAccessToken());
        thriftToken.setClaimsMap(domainToken.getClaimsMap());
        return thriftToken;
    }

    public Airavata.Client airavata() {
        try {
            return AiravataServiceClientFactory.createAiravataClient(serverUrl, port, secure, properties);
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            LOGGER.error("Error while creating Airavata client", e);
            throw new RuntimeException("Error while creating Airavata client", e);
        }
    }

    public String getProjectId(Airavata.Client airavataClient, String projectName) {
        int limit = 10;
        int offset = 0;

        while (true) {
            List<Project> userProjects;
            try {
                List<org.apache.airavata.thriftapi.model.Project> thriftUserProjects = airavataClient.getUserProjects(
                        convertAuthzToken(UserContext.authzToken()),
                        UserContext.gatewayId(),
                        UserContext.username(),
                        limit,
                        offset);
                userProjects =
                        thriftUserProjects.stream().map(this::convertProject).collect(Collectors.toList());
            } catch (InvalidRequestException e) {
                String msg = String.format(
                        "Error getting user projects: projectName=%s, gatewayId=%s, username=%s, limit=%d, offset=%d. Reason: %s",
                        projectName, UserContext.gatewayId(), UserContext.username(), limit, offset, e.getMessage());
                LOGGER.error(msg, e);
                throw new RuntimeException(msg, e);
            } catch (AuthorizationException e) {
                String msg = String.format(
                        "Error getting user projects: projectName=%s, gatewayId=%s, username=%s, limit=%d, offset=%d. Reason: %s",
                        projectName, UserContext.gatewayId(), UserContext.username(), limit, offset, e.getMessage());
                LOGGER.error(msg, e);
                throw new RuntimeException(msg, e);
            } catch (AiravataSystemException e) {
                String msg = String.format(
                        "Error getting user projects: projectName=%s, gatewayId=%s, username=%s, limit=%d, offset=%d. Reason: %s",
                        projectName, UserContext.gatewayId(), UserContext.username(), limit, offset, e.getMessage());
                LOGGER.error(msg, e);
                throw new RuntimeException(msg, e);
            } catch (AiravataClientException e) {
                String msg = String.format(
                        "Error getting user projects: projectName=%s, gatewayId=%s, username=%s, limit=%d, offset=%d. Reason: %s",
                        projectName, UserContext.gatewayId(), UserContext.username(), limit, offset, e.getMessage());
                LOGGER.error(msg, e);
                throw new RuntimeException(msg, e);
            } catch (TException e) {
                String msg = String.format(
                        "Error getting user projects: projectName=%s, gatewayId=%s, username=%s, limit=%d, offset=%d. Reason: %s",
                        projectName, UserContext.gatewayId(), UserContext.username(), limit, offset, e.getMessage());
                LOGGER.error(msg, e);
                throw new RuntimeException(msg, e);
            }

            Optional<Project> defaultProject = userProjects.stream()
                    .filter(project -> projectName.equals(project.getName()))
                    .findFirst();

            if (defaultProject.isPresent()) {
                return defaultProject.get().getProjectID();
            }
            if (userProjects.size() < limit) {
                break;
            }
            offset += limit;
        }

        throw new RuntimeException(
                "Could not find project: " + projectName + " for the user: " + UserContext.username());
    }

    public GroupComputeResourcePreference extractGroupComputeResourcePreference(
            Airavata.Client airavataClient, String group, String remoteCluster) {
        List<GroupResourceProfile> groupResourceList;
        try {
            List<org.apache.airavata.thriftapi.model.GroupResourceProfile> thriftGroupResourceList =
                    airavataClient.getGroupResourceList(
                            convertAuthzToken(UserContext.authzToken()), UserContext.gatewayId());
            groupResourceList = thriftGroupResourceList.stream()
                    .map(this::convertGroupResourceProfile)
                    .collect(Collectors.toList());
        } catch (InvalidRequestException e) {
            String msg = String.format(
                    "Error getting group resource list: group=%s, remoteCluster=%s, gatewayId=%s, username=%s. Reason: %s",
                    group, remoteCluster, UserContext.gatewayId(), UserContext.username(), e.getMessage());
            LOGGER.error(msg, e);
            throw new RuntimeException(msg, e);
        } catch (AuthorizationException e) {
            String msg = String.format(
                    "Error getting group resource list: group=%s, remoteCluster=%s, gatewayId=%s, username=%s. Reason: %s",
                    group, remoteCluster, UserContext.gatewayId(), UserContext.username(), e.getMessage());
            LOGGER.error(msg, e);
            throw new RuntimeException(msg, e);
        } catch (AiravataSystemException e) {
            String msg = String.format(
                    "Error getting group resource list: group=%s, remoteCluster=%s, gatewayId=%s, username=%s. Reason: %s",
                    group, remoteCluster, UserContext.gatewayId(), UserContext.username(), e.getMessage());
            LOGGER.error(msg, e);
            throw new RuntimeException(msg, e);
        } catch (AiravataClientException e) {
            String msg = String.format(
                    "Error getting group resource list: group=%s, remoteCluster=%s, gatewayId=%s, username=%s. Reason: %s",
                    group, remoteCluster, UserContext.gatewayId(), UserContext.username(), e.getMessage());
            LOGGER.error(msg, e);
            throw new RuntimeException(msg, e);
        } catch (TException e) {
            String msg = String.format(
                    "Error getting group resource list: group=%s, remoteCluster=%s, gatewayId=%s, username=%s. Reason: %s",
                    group, remoteCluster, UserContext.gatewayId(), UserContext.username(), e.getMessage());
            LOGGER.error(msg, e);
            throw new RuntimeException(msg, e);
        }

        String groupProfileName = StringUtils.isNotBlank(group) ? group : "Default";

        return groupResourceList.stream()
                .filter(profile -> groupProfileName.equalsIgnoreCase(profile.getGroupResourceProfileName()))
                .flatMap(profile -> profile.getComputePreferences().stream()
                        .filter(preference -> preference.getComputeResourceId().startsWith(remoteCluster)))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Could not find a matching Compute Resource Preference in the "
                        + groupProfileName + " group resource profile for the user: " + UserContext.username()));
    }

    public List<String> getUserExperimentIDs(Airavata.Client airavataClient) {
        int limit = 100;
        Map<org.apache.airavata.thriftapi.model.ExperimentSearchFields, String> filters = Map.of(
                org.apache.airavata.thriftapi.model.ExperimentSearchFields.PROJECT_ID,
                getProjectId(airavataClient, "Default Project"));

        return Stream.iterate(0, offset -> offset + limit)
                .<List<org.apache.airavata.thriftapi.model.ExperimentSummaryModel>>map(offset -> {
                    try {
                        return airavataClient.searchExperiments(
                                convertAuthzToken(UserContext.authzToken()),
                                UserContext.gatewayId(),
                                UserContext.username(),
                                filters,
                                limit,
                                offset);
                    } catch (InvalidRequestException e) {
                        String msg = String.format(
                                "Error searching experiments: gatewayId=%s, username=%s, filters=%s, limit=%d, offset=%d. Reason: %s",
                                UserContext.gatewayId(),
                                UserContext.username(),
                                filters,
                                limit,
                                offset,
                                e.getMessage());
                        LOGGER.error(msg, e);
                        throw new RuntimeException(msg, e);
                    } catch (AuthorizationException e) {
                        String msg = String.format(
                                "Error searching experiments: gatewayId=%s, username=%s, filters=%s, limit=%d, offset=%d. Reason: %s",
                                UserContext.gatewayId(),
                                UserContext.username(),
                                filters,
                                limit,
                                offset,
                                e.getMessage());
                        LOGGER.error(msg, e);
                        throw new RuntimeException(msg, e);
                    } catch (AiravataSystemException e) {
                        String msg = String.format(
                                "Error searching experiments: gatewayId=%s, username=%s, filters=%s, limit=%d, offset=%d. Reason: %s",
                                UserContext.gatewayId(),
                                UserContext.username(),
                                filters,
                                limit,
                                offset,
                                e.getMessage());
                        LOGGER.error(msg, e);
                        throw new RuntimeException(msg, e);
                    } catch (AiravataClientException e) {
                        String msg = String.format(
                                "Error searching experiments: gatewayId=%s, username=%s, filters=%s, limit=%d, offset=%d. Reason: %s",
                                UserContext.gatewayId(),
                                UserContext.username(),
                                filters,
                                limit,
                                offset,
                                e.getMessage());
                        LOGGER.error(msg, e);
                        throw new RuntimeException(msg, e);
                    } catch (TException e) {
                        String msg = String.format(
                                "Error searching experiments: gatewayId=%s, username=%s, filters=%s, limit=%d, offset=%d. Reason: %s",
                                UserContext.gatewayId(),
                                UserContext.username(),
                                filters,
                                limit,
                                offset,
                                e.getMessage());
                        LOGGER.error(msg, e);
                        throw new RuntimeException(msg, e);
                    }
                })
                .takeWhile(list -> !list.isEmpty())
                .flatMap(List::stream)
                .map(thriftSummary ->
                        convertExperimentSummaryModel(thriftSummary).getExperimentId())
                .collect(Collectors.toList());
    }

    private GroupResourceProfile convertGroupResourceProfile(
            org.apache.airavata.thriftapi.model.GroupResourceProfile thriftModel) {
        if (thriftModel == null) return null;
        GroupResourceProfile domainModel = new GroupResourceProfile();
        domainModel.setGroupResourceProfileId(thriftModel.getGroupResourceProfileId());
        domainModel.setGroupResourceProfileName(thriftModel.getGroupResourceProfileName());
        if (thriftModel.getComputePreferences() != null) {
            domainModel.setComputePreferences(thriftModel.getComputePreferences().stream()
                    .map(this::convertGroupComputeResourcePreference)
                    .collect(Collectors.toList()));
        }
        return domainModel;
    }

    private GroupComputeResourcePreference convertGroupComputeResourcePreference(
            org.apache.airavata.thriftapi.model.GroupComputeResourcePreference thriftModel) {
        if (thriftModel == null) return null;
        GroupComputeResourcePreference domainModel = new GroupComputeResourcePreference();
        domainModel.setComputeResourceId(thriftModel.getComputeResourceId());
        domainModel.setGroupResourceProfileId(thriftModel.getGroupResourceProfileId());
        if (thriftModel.getResourceType() != null) {
            domainModel.setResourceType(
                    ComputeResourceType.valueOf(thriftModel.getResourceType().name()));
        }
        domainModel.setScratchLocation(thriftModel.getScratchLocation());
        domainModel.setLoginUserName(thriftModel.getLoginUserName());
        return domainModel;
    }

    private Project convertProject(org.apache.airavata.thriftapi.model.Project thriftModel) {
        if (thriftModel == null) return null;
        Project domainModel = new Project();
        domainModel.setProjectID(thriftModel.getProjectID());
        domainModel.setName(thriftModel.getName());
        domainModel.setDescription(thriftModel.getDescription());
        domainModel.setOwner(thriftModel.getOwner());
        domainModel.setGatewayId(thriftModel.getGatewayId());
        domainModel.setCreationTime(thriftModel.getCreationTime());
        return domainModel;
    }

    private ExperimentSummaryModel convertExperimentSummaryModel(
            org.apache.airavata.thriftapi.model.ExperimentSummaryModel thriftModel) {
        if (thriftModel == null) return null;
        ExperimentSummaryModel domainModel = new ExperimentSummaryModel();
        domainModel.setExperimentId(thriftModel.getExperimentId());
        domainModel.setProjectId(thriftModel.getProjectId());
        domainModel.setGatewayId(thriftModel.getGatewayId());
        domainModel.setCreationTime(thriftModel.getCreationTime());
        domainModel.setUserName(thriftModel.getUserName());
        domainModel.setName(thriftModel.getName());
        if (thriftModel.getExperimentStatus() != null) {
            domainModel.setExperimentStatus(thriftModel.getExperimentStatus());
        }
        return domainModel;
    }
}
