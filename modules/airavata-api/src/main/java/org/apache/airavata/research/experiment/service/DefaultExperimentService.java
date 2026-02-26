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
package org.apache.airavata.research.experiment.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.airavata.compute.resource.adapter.ComputeResourceAdapter;
import org.apache.airavata.compute.resource.adapter.ResourceProfileAdapter;
import org.apache.airavata.compute.resource.model.ComputationalResourceSchedulingModel;
import org.apache.airavata.compute.resource.model.Resource;
import org.apache.airavata.config.ServerProperties;
import org.apache.airavata.core.exception.CoreExceptions.AiravataSystemException;
import org.apache.airavata.core.exception.CoreExceptions.InvalidRequestException;
import org.apache.airavata.core.exception.DuplicateEntryException;
import org.apache.airavata.core.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.core.exception.ValidationExceptions.ExceptionHandlerUtil;
import org.apache.airavata.core.model.EntitySearchField;
import org.apache.airavata.core.model.SearchCondition;
import org.apache.airavata.core.model.SearchCriteria;
import org.apache.airavata.core.util.Constants;
import org.apache.airavata.core.util.IdGenerator;
import org.apache.airavata.core.util.PaginationUtil;
import org.apache.airavata.execution.orchestration.OrchestratorException;
import org.apache.airavata.execution.orchestration.OrchestratorService;
import org.apache.airavata.gateway.model.GatewayGroups;
import org.apache.airavata.gateway.service.GatewayService;
import org.apache.airavata.iam.exception.AuthExceptions.AuthorizationException;
import org.apache.airavata.iam.exception.SharingRegistryException;
import org.apache.airavata.iam.model.AuthzToken;
import org.apache.airavata.iam.model.PermissionType;
import org.apache.airavata.iam.model.SharingEntity;
import org.apache.airavata.iam.model.SharingResourceType;
import org.apache.airavata.iam.service.AuthorizationService;
import org.apache.airavata.iam.service.GatewayGroupsInitializer;
import org.apache.airavata.iam.service.SharingService;
import org.apache.airavata.research.application.adapter.ApplicationAdapter;
import org.apache.airavata.research.application.model.ApplicationDeploymentDescription;
import org.apache.airavata.research.experiment.entity.ExperimentEntity;
import org.apache.airavata.research.experiment.exception.ExperimentExceptions.ExperimentNotFoundException;
import org.apache.airavata.research.experiment.exception.ExperimentExceptions.ProjectNotFoundException;
import org.apache.airavata.research.experiment.mapper.ExperimentMapper;
import org.apache.airavata.research.experiment.model.ExperimentModel;
import org.apache.airavata.research.experiment.model.ExperimentState;
import org.apache.airavata.research.project.model.Project;
import org.apache.airavata.research.project.service.ProjectService;
import org.apache.airavata.research.experiment.model.UserConfigurationDataModel;
import org.apache.airavata.research.experiment.repository.ExperimentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Unified service for all experiment operations: CRUD, lifecycle (launch/clone/terminate),
 * project management, and sharing registry integration.
 */
@Service("experimentServiceFacade")
@Transactional
public class DefaultExperimentService implements ExperimentService {

    private static final Logger logger = LoggerFactory.getLogger(ExperimentService.class);

    // Core experiment persistence
    private final ExperimentRepository experimentRepository;
    private final ExperimentMapper mapper;

    // Orchestration and lifecycle
    private final ServerProperties properties;
    private final ProjectService projectService;
    private final ApplicationAdapter applicationAdapter;
    private final ComputeResourceAdapter computeResourceAdapter;
    private final ResourceProfileAdapter resourceProfileAdapter;
    private final SharingService sharingService;
    private final AuthorizationService authorizationService;
    private final ExecutorService launchExecutor = Executors.newCachedThreadPool();

    // Sharing registry
    private final GatewayService gatewayGroupsService;
    private final GatewayGroupsInitializer gatewayGroupsInitializer;

    @Autowired(required = false)
    private OrchestratorService orchestratorService;

    public DefaultExperimentService(
            ExperimentRepository experimentRepository,
            ExperimentMapper mapper,
            ServerProperties properties,
            ProjectService projectService,
            ApplicationAdapter applicationAdapter,
            ComputeResourceAdapter computeResourceAdapter,
            ResourceProfileAdapter resourceProfileAdapter,
            SharingService sharingService,
            AuthorizationService authorizationService,
            GatewayService gatewayGroupsService,
            GatewayGroupsInitializer gatewayGroupsInitializer) {
        this.experimentRepository = experimentRepository;
        this.mapper = mapper;
        this.properties = properties;
        this.projectService = projectService;
        this.applicationAdapter = applicationAdapter;
        this.computeResourceAdapter = computeResourceAdapter;
        this.resourceProfileAdapter = resourceProfileAdapter;
        this.sharingService = sharingService;
        this.authorizationService = authorizationService;
        this.gatewayGroupsService = gatewayGroupsService;
        this.gatewayGroupsInitializer = gatewayGroupsInitializer;
    }

    // -------------------------------------------------------------------------
    // Core CRUD
    // -------------------------------------------------------------------------

    /**
     * Creates and persists an experiment, fires a CREATED status event, and registers it
     * in the sharing registry. This is the primary public entry point for experiment creation.
     */
    public String createExperiment(String gatewayId, ExperimentModel experiment) throws AiravataSystemException {
        if (experiment.getGatewayId() == null) {
            experiment.setGatewayId(gatewayId);
        }
        var experimentId = createExperimentInternal(gatewayId, experiment);
        try {
            createExperimentSharingEntity(experimentId, experiment);
        } catch (AiravataSystemException ex) {
            logger.error(ex.getMessage(), ex);
            logger.error("Rolling back experiment creation Exp ID : " + experimentId);
            deleteExperiment(experimentId);
            throw ex;
        }
        logger.info(
                "Created new experiment with experiment name {} and id {}",
                experiment.getExperimentName(),
                experimentId);
        return experimentId;
    }

    @Transactional(readOnly = true)
    public ExperimentModel getExperiment(String airavataExperimentId) throws AiravataSystemException {
        try {
            return experimentRepository
                    .findById(airavataExperimentId)
                    .map(mapper::toModel)
                    .orElse(null);
        } catch (Exception e) {
            String msg = "Error while retrieving experiment: " + e.getMessage();
            logger.error(msg, e);
            throw ExceptionHandlerUtil.wrapAsAiravataException(msg, e);
        }
    }

    public void updateExperiment(String airavataExperimentId, ExperimentModel experiment)
            throws AiravataSystemException {
        try {
            experiment.setExperimentId(airavataExperimentId);
            ExperimentEntity entity = mapper.toEntity(experiment);
            experimentRepository.save(entity);
        } catch (Exception e) {
            String msg = "Error while updating experiment: " + e.getMessage();
            logger.error(msg, e);
            throw ExceptionHandlerUtil.wrapAsAiravataException(msg, e);
        }
    }

    public boolean deleteExperiment(String experimentId) throws AiravataSystemException {
        try {
            if (experimentRepository.existsById(experimentId)) {
                experimentRepository.deleteById(experimentId);
                return true;
            }
            return false;
        } catch (Exception e) {
            String msg = "Error while deleting experiment: " + e.getMessage();
            logger.error(msg, e);
            throw ExceptionHandlerUtil.wrapAsAiravataException(msg, e);
        }
    }

    /**
     * Simple clone without authorization check. For permission-checked cloning use
     * {@link #cloneExperiment(AuthzToken, String, String, String, ExperimentModel)}.
     */
    public String cloneExperiment(String existingExperimentId, String newExperimentName)
            throws AiravataSystemException {
        try {
            ExperimentModel existing = getExperiment(existingExperimentId);
            if (existing == null) {
                throw ExceptionHandlerUtil.wrapAsAiravataException(
                        "Experiment not found: " + existingExperimentId, null);
            }
            existing.setExperimentId(IdGenerator.ensureId(null));
            existing.setExperimentName(newExperimentName);
            existing.setCreationTime(System.currentTimeMillis());
            existing.setState(null);
            existing.setProcesses(null);
            return createExperiment(existing.getGatewayId(), existing);
        } catch (AiravataSystemException e) {
            throw e;
        } catch (Exception e) {
            String msg = "Error while cloning experiment " + existingExperimentId + ": " + e.getMessage();
            logger.error(msg, e);
            throw ExceptionHandlerUtil.wrapAsAiravataException(msg, e);
        }
    }

    /**
     * Update experiment configuration (scheduling data).
     */
    public void updateExperimentConfiguration(String airavataExperimentId, UserConfigurationDataModel userConfiguration)
            throws AiravataSystemException {
        try {
            ExperimentEntity entity =
                    experimentRepository.findById(airavataExperimentId).orElse(null);
            if (entity == null) {
                throw new AiravataSystemException("Experiment not found: " + airavataExperimentId);
            }
            if (userConfiguration != null && userConfiguration.getComputationalResourceScheduling() != null) {
                var objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> schedulingMap = objectMapper.convertValue(
                        userConfiguration.getComputationalResourceScheduling(), java.util.Map.class);
                entity.setScheduling(schedulingMap);
            }
            experimentRepository.save(entity);
        } catch (AiravataSystemException e) {
            throw e;
        } catch (Exception e) {
            String msg = "Error while updating experiment configuration: " + e.getMessage();
            logger.error(msg, e);
            throw ExceptionHandlerUtil.wrapAsAiravataException(msg, e);
        }
    }

    @Transactional(readOnly = true)
    public List<ExperimentModel> getUserExperiments(String gatewayId, String userName, int limit, int offset)
            throws AiravataSystemException {
        try {
            List<ExperimentEntity> entities = experimentRepository.findByGatewayIdAndUserNameOrderByCreatedAtDesc(
                    gatewayId, userName, PaginationUtil.toPageRequest(limit, offset));
            return mapper.toModelList(entities);
        } catch (Exception e) {
            String msg = "Error while getting user experiments: " + e.getMessage();
            logger.error(msg, e);
            throw ExceptionHandlerUtil.wrapAsAiravataException(msg, e);
        }
    }

    @Transactional(readOnly = true)
    public List<ExperimentModel> getExperimentsInProject(String gatewayId, String projectId, int limit, int offset)
            throws AiravataSystemException {
        try {
            List<ExperimentEntity> entities =
                    experimentRepository.findByProjectIdOrderByCreatedAtDesc(
                            projectId, PaginationUtil.toPageRequest(limit, offset));
            return mapper.toModelList(entities);
        } catch (Exception e) {
            String msg = "Error while retrieving the experiments: " + e.getMessage();
            logger.error(msg, e);
            throw ExceptionHandlerUtil.wrapAsAiravataException(msg, e);
        }
    }

    // -------------------------------------------------------------------------
    // Lifecycle operations
    // -------------------------------------------------------------------------

    /**
     * Get experiment with authorization check.
     */
    public ExperimentModel getExperiment(AuthzToken authzToken, String airavataExperimentId)
            throws AuthorizationException, InvalidRequestException, AiravataSystemException {
        var existingExperiment = getExperiment(airavataExperimentId);
        authorizationService.validateExperimentReadAccess(
                authzToken, airavataExperimentId, existingExperiment.getUserName(), existingExperiment.getGatewayId());
        return existingExperiment;
    }

    public void launchExperiment(AuthzToken authzToken, String gatewayId, String airavataExperimentId)
            throws InvalidRequestException, AiravataSystemException, AuthorizationException,
                    ExperimentNotFoundException, ProjectNotFoundException, SharingRegistryException {
        try {
            logger.info("Launching experiment {}", airavataExperimentId);
            ExperimentModel experiment = getExperiment(airavataExperimentId);

            if (experiment == null) {
                throw new ExperimentNotFoundException(
                        "Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            String username = authzToken.getClaimsMap().get(Constants.USER_NAME);

            if (experiment.getUserConfigurationData().getGroupResourceProfileId() == null) {
                List<String> accessibleProfileIds = getAccessibleGroupResourceProfileIds(authzToken, gatewayId);
                if (accessibleProfileIds != null && !accessibleProfileIds.isEmpty()) {
                    final String groupResourceProfileId = accessibleProfileIds.get(0);
                    logger.warn(
                            "Experiment {} doesn't have groupResourceProfileId, picking first one user has access to: {}",
                            airavataExperimentId,
                            groupResourceProfileId);
                    experiment.getUserConfigurationData().setGroupResourceProfileId(groupResourceProfileId);
                    updateExperimentConfiguration(airavataExperimentId, experiment.getUserConfigurationData());
                } else {
                    logger.info(
                            "User {} in gateway {} doesn't have access to any group resource profiles. Creating default one.",
                            username,
                            gatewayId);
                    try {
                        String defaultGroupResourceProfileId =
                                createDefaultGroupResourceProfileForUser(authzToken, gatewayId, username);
                        logger.info(
                                "Created default group resource profile {} for user {} in gateway {}",
                                defaultGroupResourceProfileId,
                                username,
                                gatewayId);
                        experiment.getUserConfigurationData().setGroupResourceProfileId(defaultGroupResourceProfileId);
                        updateExperimentConfiguration(airavataExperimentId, experiment.getUserConfigurationData());
                    } catch (Exception e) {
                        String msg = "User " + username + " in gateway " + gatewayId
                                + " doesn't have access to any group resource profiles, and failed to create default one: "
                                + e.getMessage();
                        logger.error(msg, e);
                        throw airavataSystemException(msg, e);
                    }
                }
            }

            validateLaunchExperimentAccess(authzToken, gatewayId, experiment);
            if (orchestratorService != null) {
                orchestratorService.launchExperiment(airavataExperimentId, gatewayId, launchExecutor);
            } else {
                throw airavataSystemException(
                        "OrchestratorService is not available. Enable orchestrator services to launch experiments.",
                        null);
            }
        } catch (InvalidRequestException
                | AiravataSystemException
                | AuthorizationException
                | ExperimentNotFoundException e) {
            throw e;
        } catch (OrchestratorException e) {
            String msg = "Experiment launch failed: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(msg, e);
        }
    }

    public String cloneExperiment(
            AuthzToken authzToken,
            String existingExperimentID,
            String newExperimentName,
            String newExperimentProjectId,
            ExperimentModel existingExperiment)
            throws ExperimentNotFoundException, ProjectNotFoundException, AuthorizationException,
                    AiravataSystemException, InvalidRequestException {
        try {
            if (existingExperiment == null) {
                throw new ExperimentNotFoundException(
                        "Requested experiment id " + existingExperimentID + " does not exist in the system..");
            }
            if (newExperimentProjectId != null) {
                var project = projectService.getProject(newExperimentProjectId);
                if (project == null) {
                    throw new ProjectNotFoundException(
                            "Requested project id " + newExperimentProjectId + " does not exist in the system..");
                }
                existingExperiment.setProjectId(project.getProjectId());
            }

            var claimsGatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            var userId = authzToken.getClaimsMap().get(Constants.USER_NAME);
            if (!userHasAccess(
                    claimsGatewayId,
                    userId + "@" + claimsGatewayId,
                    existingExperiment.getProjectId(),
                    claimsGatewayId + ":WRITE")) {
                throw new AuthorizationException(
                        "User does not have permission to clone an experiment in this project");
            }

            existingExperiment.setCreationTime(
                    IdGenerator.getCurrentTimestamp().getTime());
            if (newExperimentName != null && !newExperimentName.isBlank()) {
                existingExperiment.setExperimentName(newExperimentName);
            }
            existingExperiment.setProcesses(null);
            existingExperiment.setState(null);
            if (existingExperiment.getUserConfigurationData() != null
                    && existingExperiment.getUserConfigurationData().getComputationalResourceScheduling() != null
                    && existingExperiment
                                    .getUserConfigurationData()
                                    .getComputationalResourceScheduling()
                                    .getResourceHostId()
                            != null) {
                var compResourceId = existingExperiment
                        .getUserConfigurationData()
                        .getComputationalResourceScheduling()
                        .getResourceHostId();
                try {
                    var computeResource = getComputeResource(compResourceId);
                    if (computeResource == null) {
                        existingExperiment.getUserConfigurationData().setComputationalResourceScheduling(null);
                    }
                } catch (AiravataSystemException e) {
                    logger.warn("Error getting compute resource for experiment clone: " + e.getMessage());
                }
            }
            existingExperiment.setUserName(userId);

            var expId = createExperimentInternal(claimsGatewayId, existingExperiment);
            try {
                createExperimentSharingEntity(expId, existingExperiment);
            } catch (AiravataSystemException ex) {
                logger.error(ex.getMessage(), ex);
                logger.error("rolling back experiment creation Exp ID : " + expId);
                try {
                    deleteExperiment(expId);
                } catch (AiravataSystemException e) {
                    logger.error("Error deleting experiment during rollback: " + e.getMessage());
                }
                throw ex;
            }
            return expId;
        } catch (ExperimentNotFoundException
                | ProjectNotFoundException
                | AuthorizationException
                | AiravataSystemException e) {
            throw e;
        }
    }

    public void terminateExperiment(String airavataExperimentId, String gatewayId)
            throws ExperimentNotFoundException, AiravataSystemException {
        try {
            var existingExperiment = getExperiment(airavataExperimentId);
            if (existingExperiment == null) {
                throw new ExperimentNotFoundException(
                        "Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            var currentState = existingExperiment.getState();
            switch (currentState) {
                case COMPLETED, CANCELED, FAILED, CANCELING ->
                    logger.warn("Can't terminate already {} experiment", currentState.name());
                case CREATED -> logger.warn("Experiment termination is only allowed for launched experiments.");
                default -> {
                    if (orchestratorService != null) {
                        orchestratorService.terminateExperiment(airavataExperimentId, gatewayId);
                        logger.debug("Cancelled experiment with experiment id : " + airavataExperimentId);
                    } else {
                        logger.error("OrchestratorService is not available. Cannot terminate experiment.");
                        throw airavataSystemException(
                                "OrchestratorService is not available. Enable orchestrator services to terminate experiments.",
                                null);
                    }
                }
            }
        } catch (ExperimentNotFoundException e) {
            throw e;
        } catch (AiravataSystemException e) {
            String msg = "Error occurred while cancelling the experiment: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred while cancelling the experiment: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(msg, e);
        }
    }

    // -------------------------------------------------------------------------
    // Project operations
    // -------------------------------------------------------------------------

    public String createProject(String gatewayId, Project project) throws AiravataSystemException {
        if (project.getGatewayId() == null) {
            project.setGatewayId(gatewayId);
        }
        var projectId = projectService.createProject(gatewayId, project);
        try {
            createProjectSharingEntity(projectId, project);
        } catch (AiravataSystemException ex) {
            logger.error(ex.getMessage(), ex);
            logger.error("Rolling back project creation Proj ID : " + projectId);
            projectService.deleteProject(projectId);
            throw ex;
        }
        logger.debug("Created project with project Id : {} for gateway Id : {}", projectId, gatewayId);
        return projectId;
    }

    public List<Project> getUserProjects(
            AuthzToken authzToken, String gatewayId, String userName, int limit, int offset)
            throws AiravataSystemException {
        try {
            if (properties.isSharingEnabled()) {
                var accessibleProjectIds = new ArrayList<String>();
                var filters = new ArrayList<SearchCriteria>();
                var searchCriteria = new SearchCriteria();
                searchCriteria.setSearchField(EntitySearchField.ENTITY_TYPE_ID);
                searchCriteria.setSearchCondition(SearchCondition.EQUAL);
                searchCriteria.setValue(gatewayId + ":PROJECT");
                filters.add(searchCriteria);
                sharingService
                        .searchEntities(
                                authzToken.getClaimsMap().get(Constants.GATEWAY_ID),
                                userName + "@" + gatewayId,
                                filters,
                                0,
                                -1)
                        .forEach(p -> accessibleProjectIds.add(p.getEntityId()));
                if (accessibleProjectIds.isEmpty()) {
                    return Collections.emptyList();
                }
                return projectService.searchProjects(gatewayId, userName, accessibleProjectIds, limit, offset);
            } else {
                return projectService.searchProjects(gatewayId, userName, null, limit, offset);
            }
        } catch (SharingRegistryException e) {
            String msg = "Error while retrieving user projects: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(msg, e);
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers — experiment persistence (no sharing)
    // -------------------------------------------------------------------------

    /**
     * Persists an experiment and fires a CREATED status event. Does NOT register sharing.
     * All callers that need sharing must call {@link #createExperimentSharingEntity} themselves,
     * or use the public {@link #createExperiment} entry point.
     */
    private String createExperimentInternal(String gatewayId, ExperimentModel experiment)
            throws AiravataSystemException {
        try {
            experiment.setExperimentId(IdGenerator.ensureId(experiment.getExperimentId()));
            experiment.setGatewayId(gatewayId);
            if (experiment.getCreationTime() <= 0) {
                experiment.setCreationTime(System.currentTimeMillis());
            }

            ExperimentEntity entity = mapper.toEntity(experiment);
            experimentRepository.save(entity);

            return experiment.getExperimentId();
        } catch (Exception e) {
            String msg = "Error while creating experiment: " + e.getMessage();
            logger.error(msg, e);
            throw ExceptionHandlerUtil.wrapAsAiravataException(msg, e);
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers — sharing registry
    // -------------------------------------------------------------------------

    /**
     * Creates a sharing entity for an experiment and shares it with admin gateway groups.
     */
    private String createExperimentSharingEntity(String experimentId, ExperimentModel experiment)
            throws AiravataSystemException {
        if (!properties.isSharingEnabled()) {
            return experimentId;
        }

        try {
            var entity = new SharingEntity();
            entity.setEntityId(experimentId);
            final String domainId = experiment.getGatewayId();
            entity.setDomainId(domainId);
            entity.setEntityTypeId(domainId + ":" + "EXPERIMENT");
            entity.setOwnerId(experiment.getUserName() + "@" + domainId);
            entity.setName(experiment.getExperimentName());
            entity.setDescription(experiment.getDescription());
            entity.setParentEntityId(experiment.getProjectId());

            sharingService.createEntity(entity);
            shareEntityWithAdminGatewayGroups(entity);
            return experimentId;
        } catch (SharingRegistryException
                | org.apache.airavata.core.exception.DuplicateEntryException
                | InvalidRequestException
                | AuthorizationException ex) {
            logger.error(ex.getMessage(), ex);
            throw airavataSystemException("Failed to create sharing registry record. " + ex.getMessage(), ex);
        }
    }

    /**
     * Creates a sharing entity for a project.
     */
    private String createProjectSharingEntity(String projectId, Project project) throws AiravataSystemException {
        if (!properties.isSharingEnabled()) {
            return projectId;
        }

        try {
            var entity = new SharingEntity();
            entity.setEntityId(projectId);
            final String domainId = project.getGatewayId();
            entity.setDomainId(domainId);
            entity.setEntityTypeId(domainId + ":" + "PROJECT");
            entity.setOwnerId(project.getUserName() + "@" + domainId);
            entity.setName(project.getProjectName());
            entity.setDescription(project.getDescription());

            sharingService.createEntity(entity);
            return projectId;
        } catch (SharingRegistryException
                | org.apache.airavata.core.exception.DuplicateEntryException ex) {
            logger.error(ex.getMessage(), ex);
            throw airavataSystemException(
                    "Failed to create entry for project in Sharing Registry. More info : " + ex.getMessage(), ex);
        }
    }

    /**
     * Updates sharing entity metadata for an experiment.
     */
    private void updateExperimentSharingEntity(String experimentId, ExperimentModel experiment)
            throws AiravataSystemException {
        if (!properties.isSharingEnabled()) {
            return;
        }

        try {
            var entity = sharingService.getEntity(experiment.getGatewayId(), experimentId);
            entity.setName(experiment.getExperimentName());
            entity.setDescription(experiment.getDescription());
            entity.setParentEntityId(experiment.getProjectId());
            sharingService.updateEntity(entity);
        } catch (SharingRegistryException e) {
            String msg = "Error while updating experiment entity: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(msg, e);
        }
    }

    private void shareEntityWithAdminGatewayGroups(SharingEntity entity)
            throws SharingRegistryException, InvalidRequestException, AuthorizationException {
        final String domainId = entity.getDomainId();
        try {
            GatewayGroups gatewayGroups = retrieveGatewayGroups(domainId);
            createManageSharingPermissionTypeIfMissing(domainId);
            sharingService.shareEntityWithGroups(
                    domainId,
                    entity.getEntityId(),
                    Arrays.asList(gatewayGroups.getAdminsGroupId()),
                    domainId + ":MANAGE_SHARING",
                    true);
            sharingService.shareEntityWithGroups(
                    domainId,
                    entity.getEntityId(),
                    Arrays.asList(gatewayGroups.getAdminsGroupId()),
                    domainId + ":WRITE",
                    true);
            sharingService.shareEntityWithGroups(
                    domainId,
                    entity.getEntityId(),
                    Arrays.asList(gatewayGroups.getAdminsGroupId(), gatewayGroups.getReadOnlyAdminsGroupId()),
                    domainId + ":READ",
                    true);
        } catch (SharingRegistryException | RegistryException e) {
            logger.error("Error sharing entity with admin gateway groups: " + e.getMessage(), e);
            throw new SharingRegistryException("Error sharing entity with admin gateway groups: " + e.getMessage());
        }
    }

    private GatewayGroups retrieveGatewayGroups(String gatewayId) throws RegistryException, SharingRegistryException {
        try {
            if (isGatewayGroupsExists(gatewayId)) {
                return gatewayGroupsService.getGatewayGroups(gatewayId);
            } else {
                return gatewayGroupsInitializer.initialize(gatewayId);
            }
        } catch (Exception e) {
            String msg = "Error while initializing gateway groups: " + gatewayId + " " + e.getMessage();
            logger.error(msg, e);
            throw new RegistryException(msg);
        }
    }

    private boolean isGatewayGroupsExists(String gatewayId) {
        try {
            return gatewayGroupsService.isGatewayGroupsExists(gatewayId);
        } catch (Exception e) {
            return false;
        }
    }

    private void createManageSharingPermissionTypeIfMissing(String domainId) {
        try {
            var permissionTypeId = domainId + ":MANAGE_SHARING";
            try {
                PermissionType existing = sharingService.getPermissionType(domainId, permissionTypeId);
                if (existing == null) {
                    throw new SharingRegistryException("Permission type not found, will create it");
                }
            } catch (SharingRegistryException e) {
                // Permission type doesn't exist, create it
                var permissionType = new PermissionType();
                permissionType.setPermissionTypeId(permissionTypeId);
                permissionType.setDomainId(domainId);
                permissionType.setName("MANAGE_SHARING");
                permissionType.setDescription("Sharing permission type");
                permissionType.setCreatedTime(IdGenerator.getUniqueTimestamp().getTime());
                permissionType.setUpdatedTime(IdGenerator.getUniqueTimestamp().getTime());
                sharingService.createPermissionType(permissionType);
            }
        } catch (SharingRegistryException | org.apache.airavata.core.exception.DuplicateEntryException e) {
            logger.warn("Error creating/managing MANAGE_SHARING permission type: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers — group resource profile operations
    // -------------------------------------------------------------------------

    /**
     * Returns the list of group resource profile IDs accessible to the authenticated user in the given gateway.
     * When sharing is disabled, an empty list is returned.
     */
    private List<String> getAccessibleGroupResourceProfileIds(AuthzToken authzToken, String gatewayId)
            throws AiravataSystemException {
        try {
            var accessibleGroupResProfileIds = new ArrayList<String>();
            if (properties.isSharingEnabled()) {
                String userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
                var filters = new ArrayList<SearchCriteria>();
                var searchCriteria = new SearchCriteria();
                searchCriteria.setSearchField(EntitySearchField.ENTITY_TYPE_ID);
                searchCriteria.setSearchCondition(SearchCondition.EQUAL);
                searchCriteria.setValue(gatewayId + ":" + SharingResourceType.GROUP_RESOURCE_PROFILE.name());
                filters.add(searchCriteria);
                sharingService
                        .searchEntities(
                                authzToken.getClaimsMap().get(Constants.GATEWAY_ID),
                                userName + "@" + gatewayId,
                                filters,
                                0,
                                -1)
                        .forEach(p -> accessibleGroupResProfileIds.add(p.getEntityId()));
            }
            return accessibleGroupResProfileIds;
        } catch (SharingRegistryException e) {
            String msg = "Error occurred while getting group resource list: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(msg, e);
        } catch (Exception e) {
            String msg = "Error while retrieving group resource list: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(msg, e);
        }
    }

    /**
     * Creates a default group resource profile entry for the user by registering a sharing entity
     * with a generated ID. Returns the generated profile ID, or {@code null} when sharing is disabled.
     */
    private String createDefaultGroupResourceProfileForUser(AuthzToken authzToken, String gatewayId, String username)
            throws AiravataSystemException {
        String userId = username + "@" + gatewayId;
        String profileName = "Default Resource Profile for " + username;
        String groupResourceProfileId = IdGenerator.ensureId(null);

        if (properties.isSharingEnabled()) {
            try {
                var entity = new SharingEntity();
                entity.setEntityId(groupResourceProfileId);
                entity.setDomainId(gatewayId);
                entity.setEntityTypeId(gatewayId + ":GROUP_RESOURCE_PROFILE");
                entity.setOwnerId(authzToken.getClaimsMap().get(Constants.USER_NAME) + "@" + gatewayId);
                entity.setName(profileName);
                sharingService.createEntity(entity);

                try {
                    sharingService.shareEntityWithUsers(
                            gatewayId, groupResourceProfileId, Arrays.asList(userId), gatewayId + ":READ", true);
                    logger.info("Shared default group resource profile {} with user {}", groupResourceProfileId, userId);
                } catch (SharingRegistryException e) {
                    logger.warn(
                            "Failed to share default group resource profile with user {}: {}. Profile was created but may not be accessible.",
                            userId,
                            e.getMessage());
                }
            } catch (SharingRegistryException | DuplicateEntryException ex) {
                String msg = "Error while creating group resource profile sharing entity: " + ex.getMessage();
                logger.error(msg, ex);
                throw airavataSystemException(msg, ex);
            }
        }

        return groupResourceProfileId;
    }

    // -------------------------------------------------------------------------
    // Private helpers — authorization and resource lookup
    // -------------------------------------------------------------------------

    /**
     * Validates launch experiment access, checking group resource profile and application deployment permissions.
     * This logic lives here rather than in AuthorizationService because it inspects research domain models
     * (ExperimentModel, ApplicationDeploymentDescription) that must not be imported by iam/.
     */
    private void validateLaunchExperimentAccess(AuthzToken authzToken, String gatewayId, ExperimentModel experiment)
            throws InvalidRequestException, AuthorizationException, AiravataSystemException {
        String username = authzToken.getClaimsMap().get(Constants.USER_NAME);

        if (experiment.getUserConfigurationData().getGroupResourceProfileId() == null) {
            throw new InvalidRequestException("Experiment doesn't have groupResourceProfileId");
        }

        // Verify user has READ access to groupResourceProfileId
        if (!userHasAccess(
                gatewayId,
                username + "@" + gatewayId,
                experiment.getUserConfigurationData().getGroupResourceProfileId(),
                gatewayId + ":READ")) {
            throw new AuthorizationException("User " + username + " in gateway " + gatewayId
                    + " doesn't have access to group resource profile "
                    + experiment.getUserConfigurationData().getGroupResourceProfileId());
        }

        // Verify user has READ access to Application Deployment
        final String appInterfaceId = experiment.getApplicationId();
        List<ApplicationDeploymentDescription> applicationDeploymentDescriptions;
        try {
            // Look up deployments by interface ID (APPLICATION_DEPLOYMENT.APPLICATION_ID
            // references the APPLICATION table which stores interfaces)
            applicationDeploymentDescriptions = applicationAdapter.getApplicationDeployments(appInterfaceId);
        } catch (Exception e) {
            throw new AiravataSystemException("Error resolving application for authorization check: " + e.getMessage());
        }

        if (!experiment.getUserConfigurationData().getAiravataAutoSchedule()) {
            final String resourceHostId = experiment
                    .getUserConfigurationData()
                    .getComputationalResourceScheduling()
                    .getResourceHostId();

            Optional<ApplicationDeploymentDescription> applicationDeploymentDescription =
                    applicationDeploymentDescriptions.stream()
                            .filter(dep -> dep.getComputeResourceId().equals(resourceHostId))
                            .findFirst();
            if (applicationDeploymentDescription.isPresent()) {
                final String appDeploymentId =
                        applicationDeploymentDescription.get().getAppDeploymentId();
                if (!userHasAccess(gatewayId, username + "@" + gatewayId, appDeploymentId, gatewayId + ":READ")) {
                    throw new AuthorizationException("User " + username + " in gateway " + gatewayId
                            + " doesn't have access to app deployment " + appDeploymentId);
                }
            } else {
                throw new InvalidRequestException("Application deployment doesn't exist for application interface "
                        + appInterfaceId + " and host " + resourceHostId + " in gateway " + gatewayId);
            }
        } else if (experiment.getUserConfigurationData().getAutoScheduledCompResourceSchedulingList() != null
                && !experiment
                        .getUserConfigurationData()
                        .getAutoScheduledCompResourceSchedulingList()
                        .isEmpty()) {
            List<ComputationalResourceSchedulingModel> compResourceSchedulingList =
                    experiment.getUserConfigurationData().getAutoScheduledCompResourceSchedulingList();
            for (ComputationalResourceSchedulingModel crScheduling : compResourceSchedulingList) {
                Optional<ApplicationDeploymentDescription> applicationDeploymentDescription =
                        applicationDeploymentDescriptions.stream()
                                .filter(dep -> dep.getComputeResourceId().equals(crScheduling.getResourceHostId()))
                                .findFirst();
                if (applicationDeploymentDescription.isPresent()) {
                    final String appDeploymentId =
                            applicationDeploymentDescription.get().getAppDeploymentId();
                    if (!userHasAccess(gatewayId, username + "@" + gatewayId, appDeploymentId, gatewayId + ":READ")) {
                        throw new AuthorizationException("User " + username + " in gateway " + gatewayId
                                + " doesn't have access to app deployment " + appDeploymentId);
                    }
                }
            }
        }
    }

    private boolean userHasAccess(String gatewayId, String userId, String entityId, String permissionTypeId) {
        try {
            if (sharingService.userHasAccess(gatewayId, userId, entityId, gatewayId + ":OWNER")) {
                return true;
            }
            return sharingService.userHasAccess(gatewayId, userId, entityId, permissionTypeId);
        } catch (SharingRegistryException e) {
            logger.warn(
                    "Error while checking if user has access: {} {} {}", entityId, permissionTypeId, e.getMessage());
            return false;
        }
    }

    private Resource getComputeResource(String computeResourceId) throws AiravataSystemException {
        try {
            return computeResourceAdapter.getResource(computeResourceId);
        } catch (Exception e) {
            String msg = "Error while retrieving compute resource: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(msg, e);
        }
    }

    private AiravataSystemException airavataSystemException(String message, Throwable cause) {
        return ExceptionHandlerUtil.wrapAsAiravataException(message, cause);
    }
}
