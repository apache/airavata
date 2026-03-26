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

    private boolean isSharingEnabled() {
        try {
            return ServerSettings.isEnableSharing();
        } catch (Exception e) {
            return false;
        }
    }
}
