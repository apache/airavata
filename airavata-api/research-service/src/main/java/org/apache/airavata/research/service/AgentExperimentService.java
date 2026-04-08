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
package org.apache.airavata.research.service;

import io.micrometer.common.util.StringUtils;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.config.UserContext;
import org.apache.airavata.exception.ServiceException;
import org.apache.airavata.interfaces.GroupResourceProfileProvider;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupResourceProfile;
import org.apache.airavata.model.experiment.proto.ExperimentSearchFields;
import org.apache.airavata.model.experiment.proto.ExperimentSummaryModel;
import org.apache.airavata.model.workspace.proto.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AgentExperimentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentExperimentService.class);

    private final ExperimentService experimentService;
    private final ProjectService projectService;
    private final GroupResourceProfileProvider groupResourceProfileService;
    private final ApplicationCatalogService applicationCatalogService;

    public AgentExperimentService(
            ExperimentService experimentService,
            ProjectService projectService,
            GroupResourceProfileProvider groupResourceProfileService,
            ApplicationCatalogService applicationCatalogService) {
        this.experimentService = experimentService;
        this.projectService = projectService;
        this.groupResourceProfileService = groupResourceProfileService;
        this.applicationCatalogService = applicationCatalogService;
    }

    public ExperimentService experimentService() {
        return experimentService;
    }

    public ProjectService projectService() {
        return projectService;
    }

    public GroupResourceProfileProvider groupResourceProfileService() {
        return groupResourceProfileService;
    }

    public ApplicationCatalogService applicationCatalogService() {
        return applicationCatalogService;
    }

    public RequestContext requestContext() {
        var token = UserContext.authzToken();
        Map<String, String> claims = (token != null) ? token.getClaimsMap() : Collections.emptyMap();
        if (claims == null) {
            claims = Collections.emptyMap();
        }
        return new RequestContext(
                UserContext.userId(), UserContext.gatewayId(), token != null ? token.getAccessToken() : null, claims);
    }

    public String getProjectId(String projectName) throws ServiceException {
        RequestContext ctx = requestContext();
        int limit = 10;
        int offset = 0;

        while (true) {
            List<Project> userProjects =
                    projectService.getUserProjects(ctx, ctx.getGatewayId(), ctx.getUserId(), limit, offset);
            Optional<Project> defaultProject = userProjects.stream()
                    .filter(project -> projectName.equals(project.getName()))
                    .findFirst();

            if (defaultProject.isPresent()) {
                return defaultProject.get().getProjectId();
            }
            if (userProjects.size() < limit) {
                break;
            }
            offset += limit;
        }

        throw new RuntimeException("Could not find project: " + projectName + " for the user: " + ctx.getUserId());
    }

    public GroupComputeResourcePreference extractGroupComputeResourcePreference(String group, String remoteCluster)
            throws ServiceException {
        RequestContext ctx = requestContext();
        List<GroupResourceProfile> groupResourceList =
                groupResourceProfileService.getGroupResourceList(ctx, ctx.getGatewayId());
        String groupProfileName = StringUtils.isNotBlank(group) ? group : "Default";

        return groupResourceList.stream()
                .filter(profile -> groupProfileName.equalsIgnoreCase(profile.getGroupResourceProfileName()))
                .flatMap(profile -> profile.getComputePreferencesList().stream()
                        .filter(preference -> preference.getComputeResourceId().startsWith(remoteCluster)))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Could not find a matching Compute Resource Preference in the "
                        + groupProfileName + " group resource profile for the user: " + ctx.getUserId()));
    }

    public List<String> getUserExperimentIDs() throws ServiceException {
        RequestContext ctx = requestContext();
        int limit = 100;
        String projectId = getProjectId("Default Project");
        Map<ExperimentSearchFields, String> filters = Map.of(ExperimentSearchFields.PROJECT_ID, projectId);

        return Stream.iterate(0, o -> o + limit)
                .map(o -> {
                    try {
                        return experimentService.searchExperiments(
                                ctx, ctx.getGatewayId(), ctx.getUserId(), filters, limit, o);
                    } catch (ServiceException e) {
                        throw new RuntimeException(e);
                    }
                })
                .takeWhile(list -> !list.isEmpty())
                .flatMap(List::stream)
                .map(ExperimentSummaryModel::getExperimentId)
                .collect(Collectors.toList());
    }
}
