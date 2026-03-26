package org.apache.airavata.service.experiment;

import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentSummaryModel;
import org.apache.airavata.model.experiment.ExperimentSearchFields;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.model.status.ExperimentStatus;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private boolean isSharingEnabled() {
        try {
            return ServerSettings.isEnableSharing();
        } catch (Exception e) {
            return false;
        }
    }
}
