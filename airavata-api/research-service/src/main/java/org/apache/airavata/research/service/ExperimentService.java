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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.exception.ServiceAuthorizationException;
import org.apache.airavata.exception.ServiceException;
import org.apache.airavata.exception.ServiceNotFoundException;
import org.apache.airavata.interfaces.AppCatalogRegistry;
import org.apache.airavata.interfaces.ExperimentRegistry;
import org.apache.airavata.interfaces.ProjectRegistry;
import org.apache.airavata.interfaces.SharingFacade;
import org.apache.airavata.messaging.service.EventPublisher;
import org.apache.airavata.model.application.io.proto.OutputDataObjectType;
import org.apache.airavata.model.experiment.proto.ExperimentModel;
import org.apache.airavata.model.experiment.proto.ExperimentSearchFields;
import org.apache.airavata.model.experiment.proto.ExperimentStatistics;
import org.apache.airavata.model.experiment.proto.ExperimentSummaryModel;
import org.apache.airavata.model.experiment.proto.UserConfigurationDataModel;
import org.apache.airavata.model.job.proto.JobModel;
import org.apache.airavata.model.process.proto.ProcessModel;
import org.apache.airavata.model.scheduling.proto.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.proto.ExperimentState;
import org.apache.airavata.model.status.proto.ExperimentStatus;
import org.apache.airavata.model.status.proto.JobState;
import org.apache.airavata.model.status.proto.JobStatus;
import org.apache.airavata.model.status.proto.ProcessState;
import org.apache.airavata.model.status.proto.ProcessStatus;
import org.apache.airavata.model.task.proto.TaskTypes;
import org.apache.airavata.model.workspace.proto.Project;
import org.apache.airavata.sharing.registry.models.proto.EntitySearchField;
import org.apache.airavata.sharing.registry.models.proto.SearchCondition;
import org.apache.airavata.sharing.registry.models.proto.SearchCriteria;
import org.apache.airavata.util.SharingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ExperimentService {

    private static final Logger logger = LoggerFactory.getLogger(ExperimentService.class);

    private final ExperimentRegistry experimentRegistry;
    private final AppCatalogRegistry appCatalogRegistry;
    private final ProjectRegistry projectRegistry;
    private final SharingFacade sharingHandler;
    private final EventPublisher eventPublisher;
    private GroupResourceProfileListProvider groupResourceProfileListProvider;

    /**
     * Functional interface for providing accessible group resource profiles.
     * This decouples ExperimentService from the concrete GroupResourceProfileService
     * in the compute-service module.
     */
    @FunctionalInterface
    public interface GroupResourceProfileListProvider {
        List<org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupResourceProfile> getGroupResourceList(
                RequestContext ctx, String gatewayId) throws ServiceException;
    }

    public ExperimentService(
            ExperimentRegistry experimentRegistry,
            AppCatalogRegistry appCatalogRegistry,
            ProjectRegistry projectRegistry,
            SharingFacade sharingHandler,
            EventPublisher eventPublisher) {
        this.experimentRegistry = experimentRegistry;
        this.appCatalogRegistry = appCatalogRegistry;
        this.projectRegistry = projectRegistry;
        this.sharingHandler = sharingHandler;
        this.eventPublisher = eventPublisher;
    }

    public void setGroupResourceProfileListProvider(GroupResourceProfileListProvider groupResourceProfileListProvider) {
        this.groupResourceProfileListProvider = groupResourceProfileListProvider;
    }

    public String createExperiment(RequestContext ctx, ExperimentModel experiment) throws ServiceException {
        try {
            String experimentId = experimentRegistry.createExperiment(ctx.getGatewayId(), experiment);

            if (SharingHelper.isSharingEnabled()) {
                try {
                    String domainId = experiment.getGatewayId();
                    sharingHandler.createEntity(
                            experimentId,
                            domainId,
                            domainId + ":" + "EXPERIMENT",
                            experiment.getUserName() + "@" + domainId,
                            experiment.getExperimentName(),
                            experiment.getDescription(),
                            experiment.getProjectId());
                } catch (Exception ex) {
                    logger.error("Rolling back experiment creation Exp ID : {}", experimentId, ex);
                    experimentRegistry.deleteExperiment(experimentId);
                    throw new ServiceException("Failed to create sharing registry record", ex);
                }
            }

            eventPublisher.publishExperimentStatus(
                    experimentId, ctx.getGatewayId(), ExperimentState.EXPERIMENT_STATE_CREATED);
            logger.info("Created new experiment with name {} and id {}", experiment.getExperimentName(), experimentId);
            return experimentId;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error while creating the experiment: " + e.getMessage(), e);
        }
    }

    public ExperimentModel getExperiment(RequestContext ctx, String experimentId) throws ServiceException {
        try {
            ExperimentModel experiment = experimentRegistry.getExperiment(experimentId);
            if (experiment == null) {
                throw new ServiceNotFoundException("Experiment " + experimentId + " does not exist");
            }

            // Owner always has access
            if (ctx.getUserId().equals(experiment.getUserName())
                    && ctx.getGatewayId().equals(experiment.getGatewayId())) {
                return experiment;
            }

            // Check sharing permissions
            if (SharingHelper.isSharingEnabled()) {
                String qualifiedUserId = ctx.getUserId() + "@" + ctx.getGatewayId();
                if (!sharingHandler.userHasAccess(
                        ctx.getGatewayId(), qualifiedUserId, experimentId, ctx.getGatewayId() + ":READ")) {
                    throw new ServiceAuthorizationException("User does not have permission to access this resource");
                }
                return experiment;
            }

            return null;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error while getting the experiment: " + e.getMessage(), e);
        }
    }

    public boolean deleteExperiment(RequestContext ctx, String experimentId) throws ServiceException {
        try {
            ExperimentModel experiment = experimentRegistry.getExperiment(experimentId);

            if (!ctx.getUserId().equals(experiment.getUserName())
                    || !ctx.getGatewayId().equals(experiment.getGatewayId())) {
                if (SharingHelper.isSharingEnabled()) {
                    String qualifiedUserId = ctx.getUserId() + "@" + ctx.getGatewayId();
                    if (!sharingHandler.userHasAccess(
                            ctx.getGatewayId(), qualifiedUserId, experimentId, ctx.getGatewayId() + ":WRITE")) {
                        throw new ServiceAuthorizationException(
                                "User does not have permission to delete this resource");
                    }
                }
            }

            if (experiment.getExperimentStatusList().get(0).getState() != ExperimentState.EXPERIMENT_STATE_CREATED) {
                throw new ServiceException(
                        "Experiment is not in CREATED state. Cannot be deleted. ID: " + experimentId);
            }

            return experimentRegistry.deleteExperiment(experimentId);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error while deleting the experiment: " + e.getMessage(), e);
        }
    }

    public ExperimentModel getExperimentByAdmin(RequestContext ctx, String experimentId) throws ServiceException {
        try {
            ExperimentModel experiment = experimentRegistry.getExperiment(experimentId);
            if (ctx.getGatewayId().equals(experiment.getGatewayId())) {
                return experiment;
            }
            throw new ServiceAuthorizationException("User does not have permission to access this resource");
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error while getting the experiment: " + e.getMessage(), e);
        }
    }

    public List<ExperimentSummaryModel> searchExperiments(
            RequestContext ctx,
            String gatewayId,
            String userName,
            Map<ExperimentSearchFields, String> filters,
            int limit,
            int offset)
            throws ServiceException {
        try {
            List<String> accessibleExpIds = new ArrayList<>();
            Map<ExperimentSearchFields, String> filtersCopy = new HashMap<>(filters);
            List<SearchCriteria> sharingFilters = new ArrayList<>();

            sharingFilters.add(SearchCriteria.newBuilder()
                    .setSearchField(EntitySearchField.ENTITY_TYPE_ID)
                    .setSearchCondition(SearchCondition.EQUAL)
                    .setValue(gatewayId + ":EXPERIMENT")
                    .build());

            if (filtersCopy.containsKey(ExperimentSearchFields.FROM_DATE)) {
                String fromTime = filtersCopy.remove(ExperimentSearchFields.FROM_DATE);
                sharingFilters.add(SearchCriteria.newBuilder()
                        .setSearchField(EntitySearchField.CREATED_TIME)
                        .setSearchCondition(SearchCondition.GTE)
                        .setValue(fromTime)
                        .build());
            }
            if (filtersCopy.containsKey(ExperimentSearchFields.TO_DATE)) {
                String toTime = filtersCopy.remove(ExperimentSearchFields.TO_DATE);
                sharingFilters.add(SearchCriteria.newBuilder()
                        .setSearchField(EntitySearchField.CREATED_TIME)
                        .setSearchCondition(SearchCondition.LTE)
                        .setValue(toTime)
                        .build());
            }
            if (filtersCopy.containsKey(ExperimentSearchFields.PROJECT_ID)) {
                String projectId = filtersCopy.remove(ExperimentSearchFields.PROJECT_ID);
                sharingFilters.add(SearchCriteria.newBuilder()
                        .setSearchField(EntitySearchField.PARRENT_ENTITY_ID)
                        .setSearchCondition(SearchCondition.EQUAL)
                        .setValue(projectId)
                        .build());
            }
            if (filtersCopy.containsKey(ExperimentSearchFields.USER_NAME)) {
                String username = filtersCopy.remove(ExperimentSearchFields.USER_NAME);
                sharingFilters.add(SearchCriteria.newBuilder()
                        .setSearchField(EntitySearchField.OWNER_ID)
                        .setSearchCondition(SearchCondition.EQUAL)
                        .setValue(username + "@" + gatewayId)
                        .build());
            }
            if (filtersCopy.containsKey(ExperimentSearchFields.EXPERIMENT_NAME)) {
                String name = filtersCopy.remove(ExperimentSearchFields.EXPERIMENT_NAME);
                sharingFilters.add(SearchCriteria.newBuilder()
                        .setSearchField(EntitySearchField.NAME)
                        .setSearchCondition(SearchCondition.LIKE)
                        .setValue(name)
                        .build());
            }
            if (filtersCopy.containsKey(ExperimentSearchFields.EXPERIMENT_DESC)) {
                String desc = filtersCopy.remove(ExperimentSearchFields.EXPERIMENT_DESC);
                sharingFilters.add(SearchCriteria.newBuilder()
                        .setSearchField(EntitySearchField.DESCRIPTION)
                        .setSearchCondition(SearchCondition.LIKE)
                        .setValue(desc)
                        .build());
            }

            int searchOffset = 0;
            int searchLimit = Integer.MAX_VALUE;
            boolean filteredInSharing = filtersCopy.isEmpty();
            if (filteredInSharing) {
                searchOffset = offset;
                searchLimit = limit;
            }

            accessibleExpIds.addAll(sharingHandler.searchEntityIds(
                    gatewayId, userName + "@" + gatewayId, sharingFilters, searchOffset, searchLimit));

            int finalOffset = filteredInSharing ? 0 : offset;
            return experimentRegistry.searchExperiments(
                    gatewayId, userName, accessibleExpIds, filtersCopy, limit, finalOffset);
        } catch (Exception e) {
            throw new ServiceException("Error while searching experiments: " + e.getMessage(), e);
        }
    }

    public ExperimentStatus getExperimentStatus(RequestContext ctx, String experimentId) throws ServiceException {
        try {
            return experimentRegistry.getExperimentStatus(experimentId);
        } catch (Exception e) {
            throw new ServiceException("Error while getting experiment status: " + e.getMessage(), e);
        }
    }

    public List<OutputDataObjectType> getExperimentOutputs(RequestContext ctx, String experimentId)
            throws ServiceException {
        try {
            return experimentRegistry.getExperimentOutputs(experimentId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving experiment outputs: " + e.getMessage(), e);
        }
    }

    public void terminateExperiment(RequestContext ctx, String experimentId) throws ServiceException {
        try {
            ExperimentModel experiment = experimentRegistry.getExperiment(experimentId);
            if (experiment == null) {
                throw new ServiceNotFoundException("Experiment " + experimentId + " does not exist");
            }
            ExperimentStatus status = experimentRegistry.getExperimentStatus(experimentId);
            switch (status.getState()) {
                case EXPERIMENT_STATE_COMPLETED:
                case EXPERIMENT_STATE_CANCELED:
                case EXPERIMENT_STATE_FAILED:
                case EXPERIMENT_STATE_CANCELING:
                    logger.warn(
                            "Can't terminate already {} experiment",
                            status.getState().name());
                    return;
                case EXPERIMENT_STATE_CREATED:
                    logger.warn("Experiment termination is only allowed for launched experiments.");
                    return;
                default:
                    eventPublisher.publishExperimentCancel(experimentId, ctx.getGatewayId());
                    logger.debug("Cancelled experiment {}", experimentId);
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error while cancelling the experiment: " + e.getMessage(), e);
        }
    }

    public String cloneExperiment(
            RequestContext ctx,
            String existingExperimentId,
            String newExperimentName,
            String newExperimentProjectId,
            boolean adminMode)
            throws ServiceException {
        try {
            ExperimentModel existingExperiment;
            if (adminMode) {
                existingExperiment = getExperimentByAdmin(ctx, existingExperimentId);
            } else {
                existingExperiment = getExperiment(ctx, existingExperimentId);
            }

            if (existingExperiment == null) {
                throw new ServiceNotFoundException("Experiment " + existingExperimentId + " does not exist");
            }

            if (newExperimentProjectId != null) {
                existingExperiment = existingExperiment.toBuilder()
                        .setProjectId(newExperimentProjectId)
                        .build();
            }

            // Verify write access to target project
            String qualifiedUserId = ctx.getUserId() + "@" + ctx.getGatewayId();
            if (!sharingHandler.userHasAccess(
                    ctx.getGatewayId(),
                    qualifiedUserId,
                    existingExperiment.getProjectId(),
                    ctx.getGatewayId() + ":WRITE")) {
                throw new ServiceAuthorizationException(
                        "User does not have permission to clone an experiment in this project");
            }

            ExperimentModel.Builder expBuilder =
                    existingExperiment.toBuilder().setCreationTime(System.currentTimeMillis());
            if (!existingExperiment.getExecutionId().isEmpty()) {
                List<OutputDataObjectType> appOutputs =
                        appCatalogRegistry.getApplicationOutputs(existingExperiment.getExecutionId());
                expBuilder.clearExperimentOutputs().addAllExperimentOutputs(appOutputs);
            }
            if (newExperimentName != null && !newExperimentName.isEmpty()) {
                expBuilder.setExperimentName(newExperimentName);
            }
            expBuilder.clearErrors().clearProcesses().clearExperimentStatus();
            existingExperiment = expBuilder.build();

            return createExperiment(ctx, existingExperiment);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error while cloning experiment: " + e.getMessage(), e);
        }
    }

    public ExperimentStatistics getExperimentStatistics(
            RequestContext ctx,
            String gatewayId,
            long fromTime,
            long toTime,
            String userName,
            String applicationName,
            String resourceHostName,
            int limit,
            int offset)
            throws ServiceException {
        try {
            return experimentRegistry.getExperimentStatistics(
                    gatewayId, fromTime, toTime, userName, applicationName, resourceHostName, null, limit, offset);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving experiment statistics: " + e.getMessage(), e);
        }
    }

    public List<ExperimentModel> getExperimentsInProject(RequestContext ctx, String projectId, int limit, int offset)
            throws ServiceException {
        try {
            Project project = projectRegistry.getProject(projectId);
            if (SharingHelper.isSharingEnabled()
                    && (!ctx.getUserId().equals(project.getOwner())
                            || !ctx.getGatewayId().equals(project.getGatewayId()))) {
                String qualifiedUserId = ctx.getUserId() + "@" + ctx.getGatewayId();
                if (!sharingHandler.userHasAccess(
                        ctx.getGatewayId(), qualifiedUserId, projectId, ctx.getGatewayId() + ":READ")) {
                    throw new ServiceAuthorizationException("User does not have permission to access this resource");
                }
            }
            return experimentRegistry.getExperimentsInProject(ctx.getGatewayId(), projectId, limit, offset);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving experiments in project: " + e.getMessage(), e);
        }
    }

    public List<ExperimentModel> getUserExperiments(
            RequestContext ctx, String gatewayId, String userName, int limit, int offset) throws ServiceException {
        try {
            return experimentRegistry.getUserExperiments(gatewayId, userName, limit, offset);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving user experiments: " + e.getMessage(), e);
        }
    }

    public ExperimentModel getDetailedExperimentTree(RequestContext ctx, String experimentId) throws ServiceException {
        try {
            return experimentRegistry.getDetailedExperimentTree(experimentId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving experiment tree: " + e.getMessage(), e);
        }
    }

    public void updateExperiment(RequestContext ctx, String experimentId, ExperimentModel experiment)
            throws ServiceException {
        try {
            ExperimentModel existing = experimentRegistry.getExperiment(experimentId);
            if (SharingHelper.isSharingEnabled()
                    && (!ctx.getUserId().equals(existing.getUserName())
                            || !ctx.getGatewayId().equals(existing.getGatewayId()))) {
                String qualifiedUserId = ctx.getUserId() + "@" + ctx.getGatewayId();
                if (!sharingHandler.userHasAccess(
                        ctx.getGatewayId(), qualifiedUserId, experimentId, ctx.getGatewayId() + ":WRITE")) {
                    throw new ServiceAuthorizationException("User does not have permission to update this resource");
                }
            }
            if (SharingHelper.isSharingEnabled()) {
                try {
                    sharingHandler.updateEntityMetadata(
                            ctx.getGatewayId(),
                            experimentId,
                            experiment.getExperimentName(),
                            experiment.getDescription(),
                            experiment.getProjectId());
                } catch (Exception e) {
                    throw new ServiceException("Failed to update entity in sharing registry", e);
                }
            }
            experimentRegistry.updateExperiment(experimentId, experiment);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error while updating experiment: " + e.getMessage(), e);
        }
    }

    public void updateExperimentConfiguration(
            RequestContext ctx, String experimentId, UserConfigurationDataModel userConfiguration)
            throws ServiceException {
        try {
            experimentRegistry.updateExperimentConfiguration(experimentId, userConfiguration);
        } catch (Exception e) {
            throw new ServiceException("Error while updating experiment configuration: " + e.getMessage(), e);
        }
    }

    public void updateResourceScheduleing(
            RequestContext ctx, String experimentId, ComputationalResourceSchedulingModel resourceScheduling)
            throws ServiceException {
        try {
            experimentRegistry.updateResourceScheduleing(experimentId, resourceScheduling);
        } catch (Exception e) {
            throw new ServiceException("Error while updating resource scheduling: " + e.getMessage(), e);
        }
    }

    public boolean validateExperiment(RequestContext ctx, String experimentId) throws ServiceException {
        // TODO: call validation module and validate experiment
        return true;
    }

    public Map<String, JobStatus> getJobStatuses(RequestContext ctx, String experimentId) throws ServiceException {
        try {
            return experimentRegistry.getJobStatuses(experimentId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving job statuses: " + e.getMessage(), e);
        }
    }

    public List<JobModel> getJobDetails(RequestContext ctx, String experimentId) throws ServiceException {
        try {
            return experimentRegistry.getJobDetails(experimentId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving job details: " + e.getMessage(), e);
        }
    }

    public void fetchIntermediateOutputs(RequestContext ctx, String experimentId, List<String> outputNames)
            throws ServiceException {
        try {
            // Verify that user has WRITE access to experiment
            if (!userHasWriteAccess(ctx, experimentId)) {
                throw new ServiceAuthorizationException("User does not have WRITE access to this experiment");
            }

            // Verify that the experiment's job is currently ACTIVE
            ExperimentModel existingExperiment = experimentRegistry.getExperiment(experimentId);
            List<JobModel> jobs = experimentRegistry.getJobDetails(experimentId);
            boolean anyJobIsActive = jobs.stream().anyMatch(j -> {
                if (j.getJobStatusesCount() > 0) {
                    return j.getJobStatusesList()
                                    .get(j.getJobStatusesCount() - 1)
                                    .getJobState()
                            == JobState.ACTIVE;
                }
                return false;
            });
            if (!anyJobIsActive) {
                throw new ServiceException("Experiment does not have currently ACTIVE job");
            }

            // Check if there are already running intermediate output fetching processes for outputNames
            List<ProcessModel> intermediateOutputFetchProcesses = existingExperiment.getProcessesList().stream()
                    .filter(p -> {
                        if (p.getProcessStatusesCount() > 0) {
                            ProcessStatus latestStatus =
                                    p.getProcessStatusesList().get(p.getProcessStatusesCount() - 1);
                            if (latestStatus.getState() == ProcessState.PROCESS_STATE_COMPLETED
                                    || latestStatus.getState() == ProcessState.PROCESS_STATE_FAILED) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .filter(p -> p.getTasksList().stream().allMatch(t -> t.getTaskType() == TaskTypes.OUTPUT_FETCHING))
                    .filter(p -> p.getProcessOutputsList().stream().anyMatch(o -> outputNames.contains(o.getName())))
                    .collect(Collectors.toList());
            if (!intermediateOutputFetchProcesses.isEmpty()) {
                throw new ServiceException(
                        "There are already intermediate output fetching tasks running for those outputs.");
            }

            eventPublisher.publishIntermediateOutputs(experimentId, ctx.getGatewayId(), outputNames);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(
                    "Error while processing request to fetch intermediate outputs for experiment: " + experimentId
                            + ": " + e.getMessage(),
                    e);
        }
    }

    public ProcessStatus getIntermediateOutputProcessStatus(
            RequestContext ctx, String experimentId, List<String> outputNames) throws ServiceException {
        try {
            // Verify that user has READ access to experiment
            if (!userHasReadAccess(ctx, experimentId)) {
                throw new ServiceAuthorizationException("User does not have READ access to this experiment");
            }

            ExperimentModel existingExperiment = experimentRegistry.getExperiment(experimentId);

            // Find the most recent intermediate output fetching process for the outputNames
            Optional<ProcessModel> mostRecentOutputFetchProcess = existingExperiment.getProcessesList().stream()
                    .filter(p -> p.getTasksList().stream().allMatch(t -> t.getTaskType() == TaskTypes.OUTPUT_FETCHING))
                    .filter(p -> {
                        List<String> names = p.getProcessOutputsList().stream()
                                .map(o -> o.getName())
                                .collect(Collectors.toList());
                        return new HashSet<>(names).equals(new HashSet<>(outputNames));
                    })
                    .sorted(Comparator.comparing(ProcessModel::getLastUpdateTime)
                            .reversed())
                    .findFirst();

            if (!mostRecentOutputFetchProcess.isPresent()) {
                throw new ServiceException("No matching intermediate output fetching process found.");
            }

            ProcessModel process = mostRecentOutputFetchProcess.get();
            if (process.getProcessStatusesCount() > 0) {
                return process.getProcessStatusesList().get(process.getProcessStatusesCount() - 1);
            } else {
                return ProcessStatus.newBuilder()
                        .setState(ProcessState.PROCESS_STATE_CREATED)
                        .build();
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(
                    "Error while getting intermediate output process status for experiment: " + experimentId + ": "
                            + e.getMessage(),
                    e);
        }
    }

    public void launchExperiment(RequestContext ctx, String experimentId, String gatewayId) throws ServiceException {
        try {
            ExperimentModel experiment = experimentRegistry.getExperiment(experimentId);
            if (experiment == null) {
                throw new ServiceException("Experiment " + experimentId + " does not exist");
            }

            // For backwards compatibility, if there is no groupResourceProfileId, pick one
            if (experiment
                    .getUserConfigurationData()
                    .getGroupResourceProfileId()
                    .isEmpty()) {
                List<org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupResourceProfile>
                        accessibleGroupResourceProfiles = groupResourceProfileListProvider != null
                                ? groupResourceProfileListProvider.getGroupResourceList(ctx, gatewayId)
                                : List.of();
                if (!accessibleGroupResourceProfiles.isEmpty()) {
                    final String groupResourceProfileId =
                            accessibleGroupResourceProfiles.get(0).getGroupResourceProfileId();
                    logger.warn(
                            "Experiment {} doesn't have groupResourceProfileId, picking first one user has access to: {}",
                            experimentId,
                            groupResourceProfileId);
                    UserConfigurationDataModel updatedConfig = experiment.getUserConfigurationData().toBuilder()
                            .setGroupResourceProfileId(groupResourceProfileId)
                            .build();
                    experimentRegistry.updateExperimentConfiguration(experimentId, updatedConfig);
                    experiment = experiment.toBuilder()
                            .setUserConfigurationData(updatedConfig)
                            .build();
                } else {
                    throw new ServiceAuthorizationException("User " + ctx.getUserId() + " in gateway " + gatewayId
                            + " doesn't have access to any group resource profiles.");
                }
            }

            // Verify user has READ access to groupResourceProfileId
            String qualifiedUserId = ctx.getUserId() + "@" + gatewayId;
            if (!sharingHandler.userHasAccess(
                    gatewayId,
                    qualifiedUserId,
                    experiment.getUserConfigurationData().getGroupResourceProfileId(),
                    gatewayId + ":READ")) {
                throw new ServiceAuthorizationException("User " + ctx.getUserId() + " in gateway " + gatewayId
                        + " doesn't have access to group resource profile "
                        + experiment.getUserConfigurationData().getGroupResourceProfileId());
            }

            // Verify user has READ access to Application Deployment
            final String appInterfaceId = experiment.getExecutionId();
            org.apache.airavata.model.appcatalog.appinterface.proto.ApplicationInterfaceDescription
                    applicationInterfaceDescription = appCatalogRegistry.getApplicationInterface(appInterfaceId);
            List<String> appModuleIds = applicationInterfaceDescription.getApplicationModulesList();
            String appModuleId = appModuleIds.get(0);
            List<org.apache.airavata.model.appcatalog.appdeployment.proto.ApplicationDeploymentDescription>
                    applicationDeploymentDescriptions = appCatalogRegistry.getApplicationDeployments(appModuleId);

            if (!experiment.getUserConfigurationData().getAiravataAutoSchedule()) {
                final String resourceHostId = experiment
                        .getUserConfigurationData()
                        .getComputationalResourceScheduling()
                        .getResourceHostId();
                Optional<org.apache.airavata.model.appcatalog.appdeployment.proto.ApplicationDeploymentDescription>
                        appDeployment = applicationDeploymentDescriptions.stream()
                                .filter(dep -> dep.getComputeHostId().equals(resourceHostId))
                                .findFirst();
                if (appDeployment.isPresent()) {
                    final String appDeploymentId = appDeployment.get().getAppDeploymentId();
                    if (!sharingHandler.userHasAccess(
                            gatewayId, qualifiedUserId, appDeploymentId, gatewayId + ":READ")) {
                        throw new ServiceAuthorizationException("User " + ctx.getUserId() + " in gateway " + gatewayId
                                + " doesn't have access to app deployment " + appDeploymentId);
                    }
                } else {
                    throw new ServiceException("Application deployment doesn't exist for application interface "
                            + appInterfaceId + " and host " + resourceHostId + " in gateway " + gatewayId);
                }
            } else if (experiment.getUserConfigurationData().getAutoScheduledCompResourceSchedulingListList() != null
                    && !experiment
                            .getUserConfigurationData()
                            .getAutoScheduledCompResourceSchedulingListList()
                            .isEmpty()) {
                for (org.apache.airavata.model.scheduling.proto.ComputationalResourceSchedulingModel crScheduling :
                        experiment.getUserConfigurationData().getAutoScheduledCompResourceSchedulingListList()) {
                    Optional<org.apache.airavata.model.appcatalog.appdeployment.proto.ApplicationDeploymentDescription>
                            appDeployment = applicationDeploymentDescriptions.stream()
                                    .filter(dep -> dep.getComputeHostId().equals(crScheduling.getResourceHostId()))
                                    .findFirst();
                    if (appDeployment.isPresent()) {
                        final String appDeploymentId = appDeployment.get().getAppDeploymentId();
                        if (!sharingHandler.userHasAccess(
                                gatewayId, qualifiedUserId, appDeploymentId, gatewayId + ":READ")) {
                            throw new ServiceAuthorizationException("User " + ctx.getUserId() + " in gateway "
                                    + gatewayId + " doesn't have access to app deployment " + appDeploymentId);
                        }
                    }
                }
            }

            eventPublisher.publishExperimentLaunch(experimentId, gatewayId);
            logger.info("Experiment with ExpId: {} was submitted in gateway: {}", experimentId, gatewayId);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error while launching experiment: " + e.getMessage(), e);
        }
    }

    private boolean userHasWriteAccess(RequestContext ctx, String entityId) {
        String domainId = ctx.getGatewayId();
        String qualifiedUserId = ctx.getUserId() + "@" + domainId;
        try {
            boolean isOwner = sharingHandler.userHasAccess(domainId, qualifiedUserId, entityId, domainId + ":OWNER");
            return isOwner || sharingHandler.userHasAccess(domainId, qualifiedUserId, entityId, domainId + ":WRITE");
        } catch (Exception e) {
            throw new RuntimeException("Unable to check if user has access", e);
        }
    }

    private boolean userHasReadAccess(RequestContext ctx, String entityId) {
        String domainId = ctx.getGatewayId();
        String qualifiedUserId = ctx.getUserId() + "@" + domainId;
        try {
            boolean isOwner = sharingHandler.userHasAccess(domainId, qualifiedUserId, entityId, domainId + ":OWNER");
            return isOwner || sharingHandler.userHasAccess(domainId, qualifiedUserId, entityId, domainId + ":READ");
        } catch (Exception e) {
            throw new RuntimeException("Unable to check if user has access", e);
        }
    }
}
