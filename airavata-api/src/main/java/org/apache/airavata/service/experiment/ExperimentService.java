package org.apache.airavata.service.experiment;

import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentStatistics;
import org.apache.airavata.model.experiment.ExperimentSummaryModel;
import org.apache.airavata.model.experiment.ExperimentSearchFields;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.model.status.ExperimentStatus;
import org.apache.airavata.model.status.JobState;
import org.apache.airavata.model.status.JobStatus;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.model.task.TaskTypes;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.registry.api.service.handler.RegistryServerHandler;
import org.apache.airavata.service.context.RequestContext;
import org.apache.airavata.service.exception.ServiceAuthorizationException;
import org.apache.airavata.service.exception.ServiceException;
import org.apache.airavata.service.exception.ServiceNotFoundException;
import org.apache.airavata.service.messaging.EventPublisher;
import org.apache.airavata.sharing.registry.models.Entity;
import org.apache.airavata.sharing.registry.models.SearchCriteria;
import org.apache.airavata.sharing.registry.models.EntitySearchField;
import org.apache.airavata.sharing.registry.models.SearchCondition;
import org.apache.airavata.sharing.registry.server.SharingRegistryServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ExperimentService {

    private static final Logger logger = LoggerFactory.getLogger(ExperimentService.class);

    private final RegistryServerHandler registryHandler;
    private final SharingRegistryServerHandler sharingHandler;
    private final EventPublisher eventPublisher;

    public ExperimentService(
            RegistryServerHandler registryHandler,
            SharingRegistryServerHandler sharingHandler,
            EventPublisher eventPublisher) {
        this.registryHandler = registryHandler;
        this.sharingHandler = sharingHandler;
        this.eventPublisher = eventPublisher;
    }

    public String createExperiment(RequestContext ctx, ExperimentModel experiment) throws ServiceException {
        try {
            String experimentId = registryHandler.createExperiment(ctx.getGatewayId(), experiment);

            if (isSharingEnabled()) {
                try {
                    Entity entity = new Entity();
                    entity.setEntityId(experimentId);
                    String domainId = experiment.getGatewayId();
                    entity.setDomainId(domainId);
                    entity.setEntityTypeId(domainId + ":" + "EXPERIMENT");
                    entity.setOwnerId(experiment.getUserName() + "@" + domainId);
                    entity.setName(experiment.getExperimentName());
                    entity.setDescription(experiment.getDescription());
                    entity.setParentEntityId(experiment.getProjectId());
                    sharingHandler.createEntity(entity);
                } catch (Exception ex) {
                    logger.error("Rolling back experiment creation Exp ID : {}", experimentId, ex);
                    registryHandler.deleteExperiment(experimentId);
                    throw new ServiceException("Failed to create sharing registry record", ex);
                }
            }

            eventPublisher.publishExperimentStatus(experimentId, ctx.getGatewayId(), ExperimentState.CREATED);
            logger.info("Created new experiment with name {} and id {}",
                    experiment.getExperimentName(), experimentId);
            return experimentId;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error while creating the experiment: " + e.getMessage(), e);
        }
    }

    public ExperimentModel getExperiment(RequestContext ctx, String experimentId) throws ServiceException {
        try {
            ExperimentModel experiment = registryHandler.getExperiment(experimentId);
            if (experiment == null) {
                throw new ServiceNotFoundException("Experiment " + experimentId + " does not exist");
            }

            // Owner always has access
            if (ctx.getUserId().equals(experiment.getUserName())
                    && ctx.getGatewayId().equals(experiment.getGatewayId())) {
                return experiment;
            }

            // Check sharing permissions
            if (isSharingEnabled()) {
                String qualifiedUserId = ctx.getUserId() + "@" + ctx.getGatewayId();
                if (!sharingHandler.userHasAccess(
                        ctx.getGatewayId(), qualifiedUserId, experimentId, ctx.getGatewayId() + ":READ")) {
                    throw new ServiceAuthorizationException(
                            "User does not have permission to access this resource");
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
            ExperimentModel experiment = registryHandler.getExperiment(experimentId);

            if (!ctx.getUserId().equals(experiment.getUserName())
                    || !ctx.getGatewayId().equals(experiment.getGatewayId())) {
                if (isSharingEnabled()) {
                    String qualifiedUserId = ctx.getUserId() + "@" + ctx.getGatewayId();
                    if (!sharingHandler.userHasAccess(
                            ctx.getGatewayId(), qualifiedUserId, experimentId,
                            ctx.getGatewayId() + ":WRITE")) {
                        throw new ServiceAuthorizationException(
                                "User does not have permission to delete this resource");
                    }
                }
            }

            if (experiment.getExperimentStatus().get(0).getState() != ExperimentState.CREATED) {
                throw new ServiceException(
                        "Experiment is not in CREATED state. Cannot be deleted. ID: " + experimentId);
            }

            return registryHandler.deleteExperiment(experimentId);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error while deleting the experiment: " + e.getMessage(), e);
        }
    }

    public ExperimentModel getExperimentByAdmin(RequestContext ctx, String experimentId)
            throws ServiceException {
        try {
            ExperimentModel experiment = registryHandler.getExperiment(experimentId);
            if (ctx.getGatewayId().equals(experiment.getGatewayId())) {
                return experiment;
            }
            throw new ServiceAuthorizationException(
                    "User does not have permission to access this resource");
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error while getting the experiment: " + e.getMessage(), e);
        }
    }

    public List<ExperimentSummaryModel> searchExperiments(
            RequestContext ctx, String gatewayId, String userName,
            Map<ExperimentSearchFields, String> filters, int limit, int offset)
            throws ServiceException {
        try {
            List<String> accessibleExpIds = new ArrayList<>();
            Map<ExperimentSearchFields, String> filtersCopy = new HashMap<>(filters);
            List<SearchCriteria> sharingFilters = new ArrayList<>();

            SearchCriteria entityTypeCriteria = new SearchCriteria();
            entityTypeCriteria.setSearchField(EntitySearchField.ENTITY_TYPE_ID);
            entityTypeCriteria.setSearchCondition(SearchCondition.EQUAL);
            entityTypeCriteria.setValue(gatewayId + ":EXPERIMENT");
            sharingFilters.add(entityTypeCriteria);

            if (filtersCopy.containsKey(ExperimentSearchFields.FROM_DATE)) {
                String fromTime = filtersCopy.remove(ExperimentSearchFields.FROM_DATE);
                SearchCriteria c = new SearchCriteria();
                c.setSearchField(EntitySearchField.CREATED_TIME);
                c.setSearchCondition(SearchCondition.GTE);
                c.setValue(fromTime);
                sharingFilters.add(c);
            }
            if (filtersCopy.containsKey(ExperimentSearchFields.TO_DATE)) {
                String toTime = filtersCopy.remove(ExperimentSearchFields.TO_DATE);
                SearchCriteria c = new SearchCriteria();
                c.setSearchField(EntitySearchField.CREATED_TIME);
                c.setSearchCondition(SearchCondition.LTE);
                c.setValue(toTime);
                sharingFilters.add(c);
            }
            if (filtersCopy.containsKey(ExperimentSearchFields.PROJECT_ID)) {
                String projectId = filtersCopy.remove(ExperimentSearchFields.PROJECT_ID);
                SearchCriteria c = new SearchCriteria();
                c.setSearchField(EntitySearchField.PARRENT_ENTITY_ID);
                c.setSearchCondition(SearchCondition.EQUAL);
                c.setValue(projectId);
                sharingFilters.add(c);
            }
            if (filtersCopy.containsKey(ExperimentSearchFields.USER_NAME)) {
                String username = filtersCopy.remove(ExperimentSearchFields.USER_NAME);
                SearchCriteria c = new SearchCriteria();
                c.setSearchField(EntitySearchField.OWNER_ID);
                c.setSearchCondition(SearchCondition.EQUAL);
                c.setValue(username + "@" + gatewayId);
                sharingFilters.add(c);
            }
            if (filtersCopy.containsKey(ExperimentSearchFields.EXPERIMENT_NAME)) {
                String name = filtersCopy.remove(ExperimentSearchFields.EXPERIMENT_NAME);
                SearchCriteria c = new SearchCriteria();
                c.setSearchField(EntitySearchField.NAME);
                c.setSearchCondition(SearchCondition.LIKE);
                c.setValue(name);
                sharingFilters.add(c);
            }
            if (filtersCopy.containsKey(ExperimentSearchFields.EXPERIMENT_DESC)) {
                String desc = filtersCopy.remove(ExperimentSearchFields.EXPERIMENT_DESC);
                SearchCriteria c = new SearchCriteria();
                c.setSearchField(EntitySearchField.DESCRIPTION);
                c.setSearchCondition(SearchCondition.LIKE);
                c.setValue(desc);
                sharingFilters.add(c);
            }

            int searchOffset = 0;
            int searchLimit = Integer.MAX_VALUE;
            boolean filteredInSharing = filtersCopy.isEmpty();
            if (filteredInSharing) {
                searchOffset = offset;
                searchLimit = limit;
            }

            sharingHandler.searchEntities(
                    gatewayId, userName + "@" + gatewayId,
                    sharingFilters, searchOffset, searchLimit)
                    .forEach(e -> accessibleExpIds.add(e.getEntityId()));

            int finalOffset = filteredInSharing ? 0 : offset;
            return registryHandler.searchExperiments(
                    gatewayId, userName, accessibleExpIds, filtersCopy, limit, finalOffset);
        } catch (Exception e) {
            throw new ServiceException("Error while searching experiments: " + e.getMessage(), e);
        }
    }

    public ExperimentStatus getExperimentStatus(RequestContext ctx, String experimentId)
            throws ServiceException {
        try {
            return registryHandler.getExperimentStatus(experimentId);
        } catch (Exception e) {
            throw new ServiceException("Error while getting experiment status: " + e.getMessage(), e);
        }
    }

    public List<OutputDataObjectType> getExperimentOutputs(RequestContext ctx, String experimentId)
            throws ServiceException {
        try {
            return registryHandler.getExperimentOutputs(experimentId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving experiment outputs: " + e.getMessage(), e);
        }
    }

    public void terminateExperiment(RequestContext ctx, String experimentId)
            throws ServiceException {
        try {
            ExperimentModel experiment = registryHandler.getExperiment(experimentId);
            if (experiment == null) {
                throw new ServiceNotFoundException("Experiment " + experimentId + " does not exist");
            }
            ExperimentStatus status = registryHandler.getExperimentStatus(experimentId);
            switch (status.getState()) {
                case COMPLETED:
                case CANCELED:
                case FAILED:
                case CANCELING:
                    logger.warn("Can't terminate already {} experiment", status.getState().name());
                    return;
                case CREATED:
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

    public String cloneExperiment(RequestContext ctx, String existingExperimentId,
                                   String newExperimentName, String newExperimentProjectId,
                                   boolean adminMode) throws ServiceException {
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
                existingExperiment.setProjectId(newExperimentProjectId);
            }

            // Verify write access to target project
            String qualifiedUserId = ctx.getUserId() + "@" + ctx.getGatewayId();
            if (!sharingHandler.userHasAccess(
                    ctx.getGatewayId(), qualifiedUserId,
                    existingExperiment.getProjectId(), ctx.getGatewayId() + ":WRITE")) {
                throw new ServiceAuthorizationException(
                        "User does not have permission to clone an experiment in this project");
            }

            existingExperiment.setCreationTime(System.currentTimeMillis());
            if (existingExperiment.getExecutionId() != null) {
                List<OutputDataObjectType> appOutputs =
                        registryHandler.getApplicationOutputs(existingExperiment.getExecutionId());
                existingExperiment.setExperimentOutputs(appOutputs);
            }
            if (newExperimentName != null && !newExperimentName.isEmpty()) {
                existingExperiment.setExperimentName(newExperimentName);
            }
            existingExperiment.unsetErrors();
            existingExperiment.unsetProcesses();
            existingExperiment.unsetExperimentStatus();

            return createExperiment(ctx, existingExperiment);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error while cloning experiment: " + e.getMessage(), e);
        }
    }

    public ExperimentStatistics getExperimentStatistics(
            RequestContext ctx, String gatewayId, long fromTime, long toTime,
            String userName, String applicationName, String resourceHostName,
            int limit, int offset) throws ServiceException {
        try {
            return registryHandler.getExperimentStatistics(
                    gatewayId, fromTime, toTime, userName, applicationName,
                    resourceHostName, null, limit, offset);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving experiment statistics: " + e.getMessage(), e);
        }
    }

    public List<ExperimentModel> getExperimentsInProject(
            RequestContext ctx, String projectId, int limit, int offset) throws ServiceException {
        try {
            Project project = registryHandler.getProject(projectId);
            if (isSharingEnabled()
                    && (!ctx.getUserId().equals(project.getOwner())
                        || !ctx.getGatewayId().equals(project.getGatewayId()))) {
                String qualifiedUserId = ctx.getUserId() + "@" + ctx.getGatewayId();
                if (!sharingHandler.userHasAccess(
                        ctx.getGatewayId(), qualifiedUserId, projectId, ctx.getGatewayId() + ":READ")) {
                    throw new ServiceAuthorizationException(
                            "User does not have permission to access this resource");
                }
            }
            return registryHandler.getExperimentsInProject(ctx.getGatewayId(), projectId, limit, offset);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving experiments in project: " + e.getMessage(), e);
        }
    }

    public List<ExperimentModel> getUserExperiments(
            RequestContext ctx, String gatewayId, String userName, int limit, int offset) throws ServiceException {
        try {
            return registryHandler.getUserExperiments(gatewayId, userName, limit, offset);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving user experiments: " + e.getMessage(), e);
        }
    }

    public ExperimentModel getDetailedExperimentTree(RequestContext ctx, String experimentId)
            throws ServiceException {
        try {
            return registryHandler.getDetailedExperimentTree(experimentId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving experiment tree: " + e.getMessage(), e);
        }
    }

    public void updateExperiment(RequestContext ctx, String experimentId, ExperimentModel experiment)
            throws ServiceException {
        try {
            ExperimentModel existing = registryHandler.getExperiment(experimentId);
            if (isSharingEnabled()
                    && (!ctx.getUserId().equals(existing.getUserName())
                        || !ctx.getGatewayId().equals(existing.getGatewayId()))) {
                String qualifiedUserId = ctx.getUserId() + "@" + ctx.getGatewayId();
                if (!sharingHandler.userHasAccess(
                        ctx.getGatewayId(), qualifiedUserId, experimentId,
                        ctx.getGatewayId() + ":WRITE")) {
                    throw new ServiceAuthorizationException(
                            "User does not have permission to update this resource");
                }
            }
            if (isSharingEnabled()) {
                try {
                    Entity entity = sharingHandler.getEntity(ctx.getGatewayId(), experimentId);
                    entity.setName(experiment.getExperimentName());
                    entity.setDescription(experiment.getDescription());
                    entity.setParentEntityId(experiment.getProjectId());
                    sharingHandler.updateEntity(entity);
                } catch (Exception e) {
                    throw new ServiceException("Failed to update entity in sharing registry", e);
                }
            }
            registryHandler.updateExperiment(experimentId, experiment);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error while updating experiment: " + e.getMessage(), e);
        }
    }

    public void updateExperimentConfiguration(RequestContext ctx, String experimentId,
            UserConfigurationDataModel userConfiguration) throws ServiceException {
        try {
            registryHandler.updateExperimentConfiguration(experimentId, userConfiguration);
        } catch (Exception e) {
            throw new ServiceException("Error while updating experiment configuration: " + e.getMessage(), e);
        }
    }

    public void updateResourceScheduleing(RequestContext ctx, String experimentId,
            ComputationalResourceSchedulingModel resourceScheduling) throws ServiceException {
        try {
            registryHandler.updateResourceScheduleing(experimentId, resourceScheduling);
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
            return registryHandler.getJobStatuses(experimentId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving job statuses: " + e.getMessage(), e);
        }
    }

    public List<JobModel> getJobDetails(RequestContext ctx, String experimentId) throws ServiceException {
        try {
            return registryHandler.getJobDetails(experimentId);
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
            ExperimentModel existingExperiment = registryHandler.getExperiment(experimentId);
            List<JobModel> jobs = registryHandler.getJobDetails(experimentId);
            boolean anyJobIsActive = jobs.stream().anyMatch(j -> {
                if (j.getJobStatusesSize() > 0) {
                    return j.getJobStatuses().get(j.getJobStatusesSize() - 1).getJobState() == JobState.ACTIVE;
                }
                return false;
            });
            if (!anyJobIsActive) {
                throw new ServiceException("Experiment does not have currently ACTIVE job");
            }

            // Check if there are already running intermediate output fetching processes for outputNames
            List<ProcessModel> intermediateOutputFetchProcesses = existingExperiment.getProcesses().stream()
                    .filter(p -> {
                        if (p.getProcessStatusesSize() > 0) {
                            ProcessStatus latestStatus = p.getProcessStatuses().get(p.getProcessStatusesSize() - 1);
                            if (latestStatus.getState() == ProcessState.COMPLETED
                                    || latestStatus.getState() == ProcessState.FAILED) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .filter(p -> p.getTasks().stream().allMatch(t -> t.getTaskType() == TaskTypes.OUTPUT_FETCHING))
                    .filter(p -> p.getProcessOutputs().stream().anyMatch(o -> outputNames.contains(o.getName())))
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
                    "Error while processing request to fetch intermediate outputs for experiment: "
                            + experimentId + ": " + e.getMessage(), e);
        }
    }

    public ProcessStatus getIntermediateOutputProcessStatus(RequestContext ctx, String experimentId,
            List<String> outputNames) throws ServiceException {
        try {
            // Verify that user has READ access to experiment
            if (!userHasReadAccess(ctx, experimentId)) {
                throw new ServiceAuthorizationException("User does not have READ access to this experiment");
            }

            ExperimentModel existingExperiment = registryHandler.getExperiment(experimentId);

            // Find the most recent intermediate output fetching process for the outputNames
            Optional<ProcessModel> mostRecentOutputFetchProcess = existingExperiment.getProcesses().stream()
                    .filter(p -> p.getTasks().stream().allMatch(t -> t.getTaskType() == TaskTypes.OUTPUT_FETCHING))
                    .filter(p -> {
                        List<String> names = p.getProcessOutputs().stream()
                                .map(o -> o.getName())
                                .collect(Collectors.toList());
                        return new HashSet<>(names).equals(new HashSet<>(outputNames));
                    })
                    .sorted(Comparator.comparing(ProcessModel::getLastUpdateTime).reversed())
                    .findFirst();

            if (!mostRecentOutputFetchProcess.isPresent()) {
                throw new ServiceException("No matching intermediate output fetching process found.");
            }

            ProcessModel process = mostRecentOutputFetchProcess.get();
            if (process.getProcessStatusesSize() > 0) {
                return process.getProcessStatuses().get(process.getProcessStatusesSize() - 1);
            } else {
                return new ProcessStatus(ProcessState.CREATED);
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(
                    "Error while getting intermediate output process status for experiment: "
                            + experimentId + ": " + e.getMessage(), e);
        }
    }

    public void launchExperiment(RequestContext ctx, String experimentId, String gatewayId,
            List<org.apache.airavata.model.appcatalog.groupresourceprofile.GroupResourceProfile> accessibleGroupResourceProfiles)
            throws ServiceException {
        try {
            ExperimentModel experiment = registryHandler.getExperiment(experimentId);
            if (experiment == null) {
                throw new ServiceException("Experiment " + experimentId + " does not exist");
            }

            // For backwards compatibility, if there is no groupResourceProfileId, pick one
            if (!experiment.getUserConfigurationData().isSetGroupResourceProfileId()) {
                if (!accessibleGroupResourceProfiles.isEmpty()) {
                    final String groupResourceProfileId =
                            accessibleGroupResourceProfiles.get(0).getGroupResourceProfileId();
                    logger.warn(
                            "Experiment {} doesn't have groupResourceProfileId, picking first one user has access to: {}",
                            experimentId, groupResourceProfileId);
                    experiment.getUserConfigurationData().setGroupResourceProfileId(groupResourceProfileId);
                    registryHandler.updateExperimentConfiguration(
                            experimentId, experiment.getUserConfigurationData());
                } else {
                    throw new ServiceAuthorizationException("User " + ctx.getUserId() + " in gateway " + gatewayId
                            + " doesn't have access to any group resource profiles.");
                }
            }

            // Verify user has READ access to groupResourceProfileId
            String qualifiedUserId = ctx.getUserId() + "@" + gatewayId;
            if (!sharingHandler.userHasAccess(
                    gatewayId, qualifiedUserId,
                    experiment.getUserConfigurationData().getGroupResourceProfileId(),
                    gatewayId + ":READ")) {
                throw new ServiceAuthorizationException("User " + ctx.getUserId() + " in gateway " + gatewayId
                        + " doesn't have access to group resource profile "
                        + experiment.getUserConfigurationData().getGroupResourceProfileId());
            }

            // Verify user has READ access to Application Deployment
            final String appInterfaceId = experiment.getExecutionId();
            org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription applicationInterfaceDescription =
                    registryHandler.getApplicationInterface(appInterfaceId);
            List<String> appModuleIds = applicationInterfaceDescription.getApplicationModules();
            String appModuleId = appModuleIds.get(0);
            List<org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription> applicationDeploymentDescriptions =
                    registryHandler.getApplicationDeployments(appModuleId);

            if (!experiment.getUserConfigurationData().isAiravataAutoSchedule()) {
                final String resourceHostId = experiment
                        .getUserConfigurationData()
                        .getComputationalResourceScheduling()
                        .getResourceHostId();
                Optional<org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription> appDeployment =
                        applicationDeploymentDescriptions.stream()
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
            } else if (experiment.getUserConfigurationData().getAutoScheduledCompResourceSchedulingList() != null
                    && !experiment.getUserConfigurationData().getAutoScheduledCompResourceSchedulingList().isEmpty()) {
                for (org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel crScheduling
                        : experiment.getUserConfigurationData().getAutoScheduledCompResourceSchedulingList()) {
                    Optional<org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription> appDeployment =
                            applicationDeploymentDescriptions.stream()
                                    .filter(dep -> dep.getComputeHostId().equals(crScheduling.getResourceHostId()))
                                    .findFirst();
                    if (appDeployment.isPresent()) {
                        final String appDeploymentId = appDeployment.get().getAppDeploymentId();
                        if (!sharingHandler.userHasAccess(
                                gatewayId, qualifiedUserId, appDeploymentId, gatewayId + ":READ")) {
                            throw new ServiceAuthorizationException("User " + ctx.getUserId() + " in gateway " + gatewayId
                                    + " doesn't have access to app deployment " + appDeploymentId);
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

    private boolean isSharingEnabled() {
        try {
            return ServerSettings.isEnableSharing();
        } catch (Exception e) {
            return false;
        }
    }
}
