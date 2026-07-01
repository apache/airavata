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
import org.apache.airavata.api.experiment.DataProductWithAccess;
import org.apache.airavata.api.experiment.ExperimentSpec;
import org.apache.airavata.api.experiment.ExperimentSummaryWithAccess;
import org.apache.airavata.api.experiment.ExperimentWithAccess;
import org.apache.airavata.api.experiment.FullExperiment;
import org.apache.airavata.api.project.ProjectWithAccess;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.exception.ServiceAuthorizationException;
import org.apache.airavata.exception.ServiceException;
import org.apache.airavata.exception.ServiceNotFoundException;
import org.apache.airavata.interfaces.AppCatalogRegistry;
import org.apache.airavata.interfaces.ComputeRegistry;
import org.apache.airavata.interfaces.DataProductInterface;
import org.apache.airavata.interfaces.DataReplicaLocationInterface;
import org.apache.airavata.interfaces.ExperimentRegistry;
import org.apache.airavata.interfaces.ExperimentStoragePrep;
import org.apache.airavata.interfaces.ProjectRegistry;
import org.apache.airavata.interfaces.SharingFacade;
import org.apache.airavata.model.appcatalog.appdeployment.proto.ApplicationModule;
import org.apache.airavata.model.appcatalog.appinterface.proto.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.computeresource.proto.ComputeResourceDescription;
import org.apache.airavata.model.application.io.proto.DataType;
import org.apache.airavata.model.application.io.proto.InputDataObjectType;
import org.apache.airavata.model.application.io.proto.OutputDataObjectType;
import org.apache.airavata.model.commons.proto.AccessFlags;
import org.apache.airavata.model.data.replica.proto.DataProductModel;
import org.apache.airavata.model.data.replica.proto.DataProductType;
import org.apache.airavata.model.data.replica.proto.DataReplicaLocationModel;
import org.apache.airavata.model.data.replica.proto.ReplicaLocationCategory;
import org.apache.airavata.model.data.replica.proto.ReplicaPersistentType;
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
import org.apache.airavata.util.AdminAccess;
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
    private final ComputeRegistry computeRegistry;
    private final DataProductInterface dataProductInterface;
    private final DataReplicaLocationInterface dataReplicaLocationInterface;
    private final SharingFacade sharingHandler;
    private final ProjectService projectService;
    private final java.util.Optional<org.apache.airavata.orchestration.service.LaunchOrchestrator> launchOrchestrator;
    private GroupResourceProfileListProvider groupResourceProfileListProvider;

    // Filesystem-only launch-prep SPI (storage-service impl, setter-injected via composition in
    // ServiceWiringConfig). Nullable: when unset, launchExperiment skips server-side prep so the
    // thick SDK can keep prepping client-side during the migration (additive + idempotent).
    private ExperimentStoragePrep storagePrep;

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
            ComputeRegistry computeRegistry,
            DataProductInterface dataProductInterface,
            DataReplicaLocationInterface dataReplicaLocationInterface,
            SharingFacade sharingHandler,
            ProjectService projectService,
            java.util.Optional<org.apache.airavata.orchestration.service.LaunchOrchestrator> launchOrchestrator) {
        this.experimentRegistry = experimentRegistry;
        this.appCatalogRegistry = appCatalogRegistry;
        this.projectRegistry = projectRegistry;
        this.computeRegistry = computeRegistry;
        this.dataProductInterface = dataProductInterface;
        this.dataReplicaLocationInterface = dataReplicaLocationInterface;
        this.sharingHandler = sharingHandler;
        this.projectService = projectService;
        this.launchOrchestrator = launchOrchestrator;
    }

    public void setGroupResourceProfileListProvider(GroupResourceProfileListProvider groupResourceProfileListProvider) {
        this.groupResourceProfileListProvider = groupResourceProfileListProvider;
    }

    public void setExperimentStoragePrep(ExperimentStoragePrep storagePrep) {
        this.storagePrep = storagePrep;
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

            logger.info("Created new experiment with name {} and id {}", experiment.getExperimentName(), experimentId);
            return experimentId;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error while creating the experiment: " + e.getMessage(), e);
        }
    }

    /**
     * {@link #createExperiment} that returns the created experiment joined with the caller's access
     * flags (additive). Delegates the create to {@code createExperiment}, then reuses {@code
     * getExperimentWithAccess} as the single source of truth for the flags — the caller is the owner
     * of what it just created, so {@code is_owner}/{@code user_has_write_access} are true by ownership.
     */
    public ExperimentWithAccess createExperimentWithAccess(RequestContext ctx, ExperimentModel experiment)
            throws ServiceException {
        String experimentId = createExperiment(ctx, experiment);
        return getExperimentWithAccess(ctx, experimentId);
    }

    /**
     * Creates a single experiment from a declarative {@link ExperimentSpec}. Fetches the app interface
     * to wire spec inputs against declared interface inputs (unknown input names → IllegalArgumentException),
     * copies the interface outputs, builds scheduling + user configuration, and delegates to
     * {@link #createExperiment} before returning the WithAccess wrapper.
     *
     * <p>Empty {@code project_id} or {@code application_interface_id} in the spec → IllegalArgumentException
     * (maps to INVALID_ARGUMENT via the gRPC adapter). Storage ids are left empty when the spec omits
     * them — launch-time prep defaults them.
     */
    public ExperimentWithAccess createExperimentFromSpec(RequestContext ctx, ExperimentSpec spec)
            throws ServiceException {
        if (spec.getProjectId().isEmpty()) {
            throw new IllegalArgumentException("project_id must not be empty");
        }
        if (spec.getApplicationInterfaceId().isEmpty()) {
            throw new IllegalArgumentException("application_interface_id must not be empty");
        }

        try {
            ApplicationInterfaceDescription appInterface =
                    appCatalogRegistry.getApplicationInterface(spec.getApplicationInterfaceId());

            // Build a name-indexed map of declared interface inputs for fast lookup and validation.
            Map<String, InputDataObjectType> declaredInputsByName = new java.util.LinkedHashMap<>();
            for (InputDataObjectType declared : appInterface.getApplicationInputsList()) {
                declaredInputsByName.put(declared.getName(), declared);
            }

            // Validate: every key in spec.inputs must be a declared input name.
            for (String specInputName : spec.getInputsMap().keySet()) {
                if (!declaredInputsByName.containsKey(specInputName)) {
                    throw new IllegalArgumentException(
                            "Input '" + specInputName + "' is not declared on application interface "
                                    + spec.getApplicationInterfaceId());
                }
            }

            // Wire spec input values onto declared inputs (declared order preserved; value only
            // overridden when the spec supplies one for this input name).
            List<InputDataObjectType> experimentInputs = new ArrayList<>();
            for (InputDataObjectType declared : appInterface.getApplicationInputsList()) {
                String specValue = spec.getInputsMap().get(declared.getName());
                if (specValue != null) {
                    experimentInputs.add(declared.toBuilder().setValue(specValue).build());
                } else {
                    experimentInputs.add(declared);
                }
            }

            // Outputs: copy directly from the interface.
            List<org.apache.airavata.model.application.io.proto.OutputDataObjectType> experimentOutputs =
                    new ArrayList<>(appInterface.getApplicationOutputsList());

            // Build scheduling model.
            ComputationalResourceSchedulingModel scheduling = ComputationalResourceSchedulingModel.newBuilder()
                    .setResourceHostId(spec.getResource().getComputeResourceId())
                    .setNodeCount(spec.getResource().getNodeCount())
                    .setTotalCpuCount(spec.getResource().getTotalCpuCount())
                    .setQueueName(spec.getResource().getQueueName())
                    .setWallTimeLimit(spec.getResource().getWallTimeLimit())
                    .build();

            // Build user configuration.
            UserConfigurationDataModel userConfig = UserConfigurationDataModel.newBuilder()
                    .setComputationalResourceScheduling(scheduling)
                    .setGroupResourceProfileId(spec.getResource().getGroupResourceProfileId())
                    .setInputStorageResourceId(spec.getResource().getInputStorageResourceId())
                    .setOutputStorageResourceId(spec.getResource().getOutputStorageResourceId())
                    .setAiravataAutoSchedule(spec.getResource().getAutoSchedule())
                    .build();

            // Build the experiment model.
            ExperimentModel experiment = ExperimentModel.newBuilder()
                    .setExperimentName(spec.getExperimentName())
                    .setProjectId(spec.getProjectId())
                    .setGatewayId(ctx.getGatewayId())
                    .setUserName(ctx.getUserId())
                    .setExecutionId(spec.getApplicationInterfaceId())
                    .setExperimentType(
                            org.apache.airavata.model.experiment.proto.ExperimentType.SINGLE_APPLICATION)
                    .setDescription(spec.getDescription())
                    .addAllExperimentInputs(experimentInputs)
                    .addAllExperimentOutputs(experimentOutputs)
                    .setUserConfigurationData(userConfig)
                    .build();

            String experimentId = createExperiment(ctx, experiment);
            return getExperimentWithAccess(ctx, experimentId);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error while creating experiment from spec: " + e.getMessage(), e);
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

    /**
     * {@link #getExperiment} plus the caller's server-computed access flags (additive). Reuses
     * {@code getExperiment} for READ enforcement so a caller can never self-authorize; the flags are
     * derived from the same owner-field and {@link #userHasWriteAccess} sharing WRITE check the
     * mutating operations use.
     */
    public ExperimentWithAccess getExperimentWithAccess(RequestContext ctx, String experimentId)
            throws ServiceException {
        ExperimentModel experiment = getExperiment(ctx, experimentId);
        if (experiment == null) {
            // Sharing disabled and the caller is not the owner: no access.
            throw new ServiceAuthorizationException("User does not have permission to access this resource");
        }
        try {
            boolean isOwner = ctx.getUserId().equals(experiment.getUserName())
                    && ctx.getGatewayId().equals(experiment.getGatewayId());
            boolean userHasWriteAccess = isOwner;
            if (!isOwner && SharingHelper.isSharingEnabled()) {
                userHasWriteAccess = userHasWriteAccess(ctx, experimentId);
            }
            return ExperimentWithAccess.newBuilder()
                    .setExperiment(experiment)
                    .setAccess(AccessFlags.newBuilder()
                            .setIsOwner(isOwner)
                            .setUserHasWriteAccess(userHasWriteAccess)
                            .build())
                    .build();
        } catch (Exception e) {
            throw new ServiceException("Error while computing experiment access: " + e.getMessage(), e);
        }
    }

    /**
     * The experiment joined with every related entity a viewer needs in one round-trip — access
     * flags, owning project, application module/interface, compute resource, resolved input/output
     * data products, and job details (additive). This server-side composition replaces the multi-RPC
     * join the Python SDK does today, so any client gets the same shaped result. READ enforcement and
     * the access flags are inherited from {@link #getExperimentWithAccess}; every related reference is
     * best-effort and left unset when it does not apply or cannot be resolved.
     */
    public FullExperiment getFullExperiment(RequestContext ctx, String experimentId) throws ServiceException {
        // READ enforcement + access flags (throws when the caller has no access).
        ExperimentWithAccess experimentEnv = getExperimentWithAccess(ctx, experimentId);
        ExperimentModel experiment = experimentEnv.getExperiment();
        try {
            FullExperiment.Builder full =
                    FullExperiment.newBuilder().setExperiment(experiment).setAccess(experimentEnv.getAccess());

            // Referenced data products: outputs first, then inputs (matches the SDK ordering).
            full.addAllOutputDataProducts(
                    resolveDataProducts(ctx, collectOutputUris(experiment.getExperimentOutputsList())));
            full.addAllInputDataProducts(
                    resolveDataProducts(ctx, collectInputUris(experiment.getExperimentInputsList())));

            // Application interface (execution id) → first module.
            ApplicationInterfaceDescription applicationInterface = null;
            try {
                applicationInterface = appCatalogRegistry.getApplicationInterface(experiment.getExecutionId());
            } catch (Exception e) {
                logger.debug(
                        "Could not resolve application interface {} for experiment {}",
                        experiment.getExecutionId(),
                        experimentId);
            }
            if (applicationInterface != null) {
                full.setApplicationInterface(applicationInterface);
                if (applicationInterface.getApplicationModulesCount() > 0) {
                    try {
                        ApplicationModule module =
                                appCatalogRegistry.getApplicationModule(applicationInterface.getApplicationModules(0));
                        if (module != null) {
                            full.setApplicationModule(module);
                        }
                    } catch (Exception e) {
                        logger.debug("Could not resolve application module for experiment {}", experimentId);
                    }
                }
            }

            // Compute resource from the scheduling host id, when set.
            if (experiment.hasUserConfigurationData()
                    && experiment.getUserConfigurationData().hasComputationalResourceScheduling()) {
                String resourceHostId = experiment
                        .getUserConfigurationData()
                        .getComputationalResourceScheduling()
                        .getResourceHostId();
                if (!resourceHostId.isEmpty()) {
                    try {
                        ComputeResourceDescription computeResource = computeRegistry.getComputeResource(resourceHostId);
                        if (computeResource != null) {
                            full.setComputeResource(computeResource);
                        }
                    } catch (Exception e) {
                        logger.debug(
                                "Could not resolve compute resource {} for experiment {}",
                                resourceHostId,
                                experimentId);
                    }
                }
            }

            // Owning project, only when the caller may READ it (best-effort).
            Project project = resolveReadableProject(ctx, experiment.getProjectId());
            if (project != null) {
                full.setProject(project);
            }

            // Flat job list.
            full.addAllJobs(experimentRegistry.getJobDetails(experimentId));

            return full.build();
        } catch (Exception e) {
            throw new ServiceException("Error while composing full experiment: " + e.getMessage(), e);
        }
    }

    private List<String> collectOutputUris(List<OutputDataObjectType> outputs) {
        List<String> single = new ArrayList<>();
        List<String> collection = new ArrayList<>();
        for (OutputDataObjectType o : outputs) {
            collectUri(single, collection, o.getValue(), o.getType());
        }
        single.addAll(collection);
        return single;
    }

    private List<String> collectInputUris(List<InputDataObjectType> inputs) {
        List<String> single = new ArrayList<>();
        List<String> collection = new ArrayList<>();
        for (InputDataObjectType i : inputs) {
            collectUri(single, collection, i.getValue(), i.getType());
        }
        single.addAll(collection);
        return single;
    }

    // airavata-dp URIs referenced by an IO proto: single-URI types (URI/STDOUT/STDERR) contribute
    // their value; URI_COLLECTION contributes each comma-separated member. Single URIs precede
    // collection members, matching the SDK ordering.
    private void collectUri(List<String> single, List<String> collection, String value, DataType type) {
        if (value == null || value.isEmpty() || !value.startsWith("airavata-dp")) {
            return;
        }
        if (type == DataType.URI || type == DataType.STDOUT || type == DataType.STDERR) {
            single.add(value);
        } else if (type == DataType.URI_COLLECTION) {
            for (String member : value.split(",")) {
                if (!member.isEmpty()) {
                    collection.add(member);
                }
            }
        }
    }

    // The project if the caller may READ it (owner or sharing READ), else null. Mirrors the
    // enforcement in getExperimentsInProject; swallows resolution errors for best-effort use.
    private Project resolveReadableProject(RequestContext ctx, String projectId) {
        try {
            Project project = projectRegistry.getProject(projectId);
            if (project == null) {
                return null;
            }
            boolean isOwner = ctx.getUserId().equals(project.getOwner())
                    && ctx.getGatewayId().equals(project.getGatewayId());
            if (isOwner || !SharingHelper.isSharingEnabled()) {
                return project;
            }
            String qualifiedUserId = ctx.getUserId() + "@" + ctx.getGatewayId();
            if (sharingHandler.userHasAccess(
                    ctx.getGatewayId(), qualifiedUserId, projectId, ctx.getGatewayId() + ":READ")) {
                return project;
            }
            return null;
        } catch (Exception e) {
            logger.debug("Could not resolve project {}: {}", projectId, e.getMessage());
            return null;
        }
    }

    private List<DataProductWithAccess> resolveDataProducts(RequestContext ctx, List<String> uris) {
        List<DataProductWithAccess> resolved = new ArrayList<>();
        for (String uri : uris) {
            try {
                DataProductModel product = dataProductInterface.getDataProduct(uri);
                if (product == null) {
                    continue;
                }
                // No sharing entity exists for data products, so access falls back to ownership.
                boolean isOwner = !product.getOwnerName().isEmpty()
                        && product.getOwnerName().equals(ctx.getUserId());
                resolved.add(DataProductWithAccess.newBuilder()
                        .setDataProduct(product)
                        .setAccess(AccessFlags.newBuilder()
                                .setIsOwner(isOwner)
                                .setUserHasWriteAccess(isOwner)
                                .build())
                        .build());
            } catch (Exception e) {
                logger.debug("Could not resolve data product {}: {}", uri, e.getMessage());
            }
        }
        return resolved;
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
        AdminAccess.requireAdminOrReadOnly(ctx);
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

    /**
     * {@link #searchExperiments} plus each item's caller-relative access flags (additive). Reuses
     * {@code searchExperiments} for READ enforcement (it restricts results to sharing-accessible ids),
     * then computes per-item flags against the CALLER ({@link RequestContext#getUserId()}/{@link
     * RequestContext#getGatewayId()}): {@code is_owner} via the summary's owner fields, {@code
     * user_has_write_access} via the same sharing WRITE check the mutating operations use. Per-item
     * sharing calls run server-side, replacing N client round-trips.
     */
    public List<ExperimentSummaryWithAccess> searchExperimentsWithAccess(
            RequestContext ctx,
            String gatewayId,
            String userName,
            Map<ExperimentSearchFields, String> filters,
            int limit,
            int offset)
            throws ServiceException {
        List<ExperimentSummaryModel> summaries = searchExperiments(ctx, gatewayId, userName, filters, limit, offset);
        try {
            List<ExperimentSummaryWithAccess> results = new ArrayList<>(summaries.size());
            for (ExperimentSummaryModel summary : summaries) {
                boolean isOwner = ctx.getUserId().equals(summary.getUserName())
                        && ctx.getGatewayId().equals(summary.getGatewayId());
                boolean userHasWriteAccess = isOwner;
                if (!isOwner && SharingHelper.isSharingEnabled()) {
                    userHasWriteAccess = userHasWriteAccess(ctx, summary.getExperimentId());
                }
                results.add(ExperimentSummaryWithAccess.newBuilder()
                        .setSummary(summary)
                        .setAccess(AccessFlags.newBuilder()
                                .setIsOwner(isOwner)
                                .setUserHasWriteAccess(userHasWriteAccess)
                                .build())
                        .build());
            }
            return results;
        } catch (Exception e) {
            throw new ServiceException("Error while computing experiment search access: " + e.getMessage(), e);
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

    /**
     * Composite clone for thin clients: resolve a writable target project, reuse {@link #cloneExperiment} for
     * the DB-level copy, copy each referenced input file into a fresh tmp upload (rewriting the input value,
     * dropping inputs whose source file is gone), null the data dir, persist, and return the new experiment
     * with the caller's access flags. Composes existing units (cloneExperiment / updateExperiment /
     * getExperimentWithAccess / storage copyFile / data-product register) — mirrors the Python SDK's
     * experiment_orchestration.clone().
     */
    public ExperimentWithAccess cloneExperimentWithInputFiles(
            RequestContext ctx, String experimentId, String newExperimentName, String newExperimentProjectId)
            throws ServiceException {
        try {
            ExperimentModel source = getExperiment(ctx, experimentId);
            if (source == null) {
                throw new ServiceNotFoundException("Experiment " + experimentId + " does not exist");
            }
            String targetProjectId = resolveWritableProjectId(ctx, source, newExperimentProjectId);
            String name = (newExperimentName != null && !newExperimentName.isEmpty())
                    ? newExperimentName
                    : "Clone of " + source.getExperimentName();

            String clonedId = cloneExperiment(ctx, experimentId, name, targetProjectId, false);

            ExperimentModel cloned = experimentRegistry.getExperiment(clonedId);
            ExperimentModel withCopiedInputs = copyClonedExperimentInputUris(ctx, cloned);
            // Null experiment_data_dir so a fresh one is created at launch time.
            ExperimentModel finalModel = withCopiedInputs.toBuilder()
                    .setUserConfigurationData(withCopiedInputs.getUserConfigurationData().toBuilder()
                            .setExperimentDataDir("")
                            .build())
                    .build();
            updateExperiment(ctx, clonedId, finalModel);
            return getExperimentWithAccess(ctx, clonedId);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error cloning experiment with input files: " + e.getMessage(), e);
        }
    }

    // The requested project when supplied (cloneExperiment verifies its WRITE access); else the source's
    // project when writable; else the caller's most-recent writable project. Mirrors the SDK's
    // _get_writeable_project.
    private String resolveWritableProjectId(RequestContext ctx, ExperimentModel source, String requestedProjectId)
            throws ServiceException {
        if (requestedProjectId != null && !requestedProjectId.isEmpty()) {
            return requestedProjectId;
        }
        String qualifiedUserId = ctx.getUserId() + "@" + ctx.getGatewayId();
        String sourceProjectId = source.getProjectId();
        try {
            if (sharingHandler.userHasAccess(
                    ctx.getGatewayId(), qualifiedUserId, sourceProjectId, ctx.getGatewayId() + ":WRITE")) {
                return sourceProjectId;
            }
        } catch (Exception e) {
            logger.debug("Write-access check on source project {} failed: {}", sourceProjectId, e.getMessage());
        }
        ProjectWithAccess writable =
                projectService.getMostRecentWritableProject(ctx, ctx.getGatewayId(), ctx.getUserId());
        return writable.getProject().getProjectId();
    }

    // Rebuild the experiment inputs with each URI / URI_COLLECTION value copied into a fresh tmp upload
    // (missing source files dropped for URI, omitted for URI_COLLECTION). Mirrors the SDK's
    // _copy_cloned_experiment_input_uris.
    private ExperimentModel copyClonedExperimentInputUris(RequestContext ctx, ExperimentModel cloned) {
        ExperimentModel.Builder builder = cloned.toBuilder().clearExperimentInputs();
        for (InputDataObjectType input : cloned.getExperimentInputsList()) {
            InputDataObjectType.Builder inputBuilder = input.toBuilder();
            String value = input.getValue();
            if (value != null && !value.isEmpty()) {
                if (input.getType() == DataType.URI) {
                    String copied = copyExperimentInputUri(ctx, value);
                    inputBuilder.setValue(copied != null ? copied : "");
                } else if (input.getType() == DataType.URI_COLLECTION) {
                    List<String> copiedUris = new java.util.ArrayList<>();
                    for (String uri : value.split(",")) {
                        if (uri.isEmpty()) {
                            continue;
                        }
                        String copied = copyExperimentInputUri(ctx, uri);
                        if (copied != null) {
                            copiedUris.add(copied);
                        }
                    }
                    inputBuilder.setValue(String.join(",", copiedUris));
                }
            }
            builder.addExperimentInputs(inputBuilder.build());
        }
        return builder.build();
    }

    // Copy one input data product's file into a fresh tmp upload (native storage copyFile, no
    // download/upload round-trip) and register a new data product, returning its URI; null when the source
    // file is gone. Mirrors the SDK's _copy_experiment_input_uri.
    private String copyExperimentInputUri(RequestContext ctx, String dataProductUri) {
        try {
            DataProductModel source = dataProductInterface.getDataProduct(dataProductUri);
            if (source == null) {
                return null;
            }
            DataReplicaLocationModel replica = gatewayDataStoreReplica(source);
            String srcPath = replicaFilesystemPath(replica);
            String storageId = replica != null ? replica.getStorageResourceId() : "";
            if (srcPath == null || !storagePrep.fileExists(storageId, srcPath)) {
                logger.warn("Could not find file for source data product {}; dropping cloned input", dataProductUri);
                return null;
            }
            String fileName = !source.getProductName().isEmpty() ? source.getProductName() : basename(srcPath);
            String destPath = joinStoragePath(TMP_INPUT_FILE_UPLOAD_DIR, fileName);
            storagePrep.copyFile(storageId, srcPath, destPath);

            DataReplicaLocationModel newReplica = DataReplicaLocationModel.newBuilder()
                    .setStorageResourceId(storageId == null ? "" : storageId)
                    .setReplicaName(fileName + " gateway data store copy")
                    .setReplicaLocationCategory(ReplicaLocationCategory.GATEWAY_DATA_STORE)
                    .setReplicaPersistentType(ReplicaPersistentType.TRANSIENT)
                    .setFilePath(destPath)
                    .build();
            DataProductModel copy = DataProductModel.newBuilder()
                    .setGatewayId(ctx.getGatewayId())
                    .setOwnerName(ctx.getUserId())
                    .setProductName(fileName)
                    .setDataProductType(DataProductType.FILE)
                    .putAllProductMetadata(source.getProductMetadataMap())
                    .addReplicaLocations(newReplica)
                    .build();
            return dataProductInterface.registerDataProduct(copy);
        } catch (Exception e) {
            logger.warn("Could not copy input data product {} for clone: {}", dataProductUri, e.getMessage());
            return null;
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
        AdminAccess.requireAdminOrReadOnly(ctx);
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
            if (project == null) {
                throw new ServiceNotFoundException("Project " + projectId + " does not exist");
            }
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

    /**
     * {@link #getExperimentsInProject} plus each item's caller-relative access flags (additive).
     * Reuses {@code getExperimentsInProject} for READ enforcement, then computes per-item flags
     * against the CALLER ({@link RequestContext#getUserId()}/{@link RequestContext#getGatewayId()}):
     * {@code is_owner} via the experiment's owner field, {@code user_has_write_access} via the same
     * sharing WRITE check the mutating operations use. Per-item sharing calls run server-side,
     * replacing N client round-trips; a batch optimization is a noted follow-up.
     */
    public List<ExperimentWithAccess> getExperimentsInProjectWithAccess(
            RequestContext ctx, String projectId, int limit, int offset) throws ServiceException {
        List<ExperimentModel> experiments = getExperimentsInProject(ctx, projectId, limit, offset);
        try {
            List<ExperimentWithAccess> results = new ArrayList<>(experiments.size());
            for (ExperimentModel experiment : experiments) {
                boolean isOwner = ctx.getUserId().equals(experiment.getUserName())
                        && ctx.getGatewayId().equals(experiment.getGatewayId());
                boolean userHasWriteAccess = isOwner;
                if (!isOwner && SharingHelper.isSharingEnabled()) {
                    userHasWriteAccess = userHasWriteAccess(ctx, experiment.getExperimentId());
                }
                results.add(ExperimentWithAccess.newBuilder()
                        .setExperiment(experiment)
                        .setAccess(AccessFlags.newBuilder()
                                .setIsOwner(isOwner)
                                .setUserHasWriteAccess(userHasWriteAccess)
                                .build())
                        .build());
            }
            return results;
        } catch (Exception e) {
            throw new ServiceException("Error while computing experiment access in project: " + e.getMessage(), e);
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

    /**
     * {@link #updateExperiment} that returns the updated experiment joined with the caller's access
     * flags (additive). Delegates the update (with its WRITE enforcement) to {@code updateExperiment},
     * then reuses {@code getExperimentWithAccess} as the single source of truth for the flags.
     */
    public ExperimentWithAccess updateExperimentWithAccess(
            RequestContext ctx, String experimentId, ExperimentModel experiment) throws ServiceException {
        updateExperiment(ctx, experimentId, experiment);
        return getExperimentWithAccess(ctx, experimentId);
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

            throw new ServiceException("Fetching intermediate outputs is not supported in this deployment");
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

    // Directory (relative to the user's storage root) where freshly uploaded input files land
    // before they are moved into a launched experiment's data directory. Mirrors the SDK's
    // TMP_INPUT_FILE_UPLOAD_DIR.
    private static final String TMP_INPUT_FILE_UPLOAD_DIR = "tmp";

    /**
     * Server-side port of the Python SDK's pre-launch preparation
     * ({@code _set_storage_id_and_data_dir} + {@code _move_tmp_input_file_uploads_to_data_dir}).
     * Finalizes input/output storage ids and the experiment data dir on the user configuration,
     * relocates any tmp-resident input uploads into the data dir (repointing the replica in place so
     * the data-product URI — and thus the input value string — is preserved), persists the updated
     * configuration, and returns the experiment refreshed with it. Additive and idempotent: empty
     * fields are filled, already-set values are kept, and an already-moved file (present at the
     * destination, absent at the source) is treated as a no-op move.
     */
    private ExperimentModel prepareExperimentForLaunch(String experimentId, ExperimentModel experiment)
            throws Exception {
        UserConfigurationDataModel.Builder configBuilder = experiment.getUserConfigurationData().toBuilder();

        // 3a STORAGE IDS: default input/output storage to the gateway default, but only when empty
        // (idempotent; never clobber a non-empty value). Resolve the default LAZILY — only when a
        // storage id is actually missing — so a fully pre-configured experiment can launch even on a
        // gateway with no default storage preference.
        if (configBuilder.getInputStorageResourceId().isEmpty()
                || configBuilder.getOutputStorageResourceId().isEmpty()) {
            String defaultId = storagePrep.getDefaultStorageResourceId();
            if (configBuilder.getInputStorageResourceId().isEmpty()) {
                configBuilder.setInputStorageResourceId(defaultId);
            }
            if (configBuilder.getOutputStorageResourceId().isEmpty()) {
                configBuilder.setOutputStorageResourceId(defaultId);
            }
        }

        // The experiment_data_dir and the relocated inputs live on the effective input storage.
        String storageId = configBuilder.getInputStorageResourceId();

        // 3b DATA DIR: derive "<project>/<experiment>" (sanitized) when unset, creating the directory
        // and storing the BARE-RELATIVE path (DataStagingTask anchors it at staging time); if already
        // set, ensure the existing dir exists and keep its value (never re-derive, never 2nd dir).
        if (configBuilder.getExperimentDataDir().isEmpty()) {
            Project project = projectRegistry.getProject(experiment.getProjectId());
            String relPath = sanitizePathComponent(project != null ? project.getName() : "") + "/"
                    + sanitizePathComponent(experiment.getExperimentName());
            String created = storagePrep.ensureDir(storageId, relPath);
            configBuilder.setExperimentDataDir(created);
        } else {
            storagePrep.ensureDir(storageId, configBuilder.getExperimentDataDir());
        }

        // 3c RELOCATE TMP UPLOADS: move any URI / URI_COLLECTION input that is a tmp upload into the
        // data dir, repointing the replica's file_path in place (preserving the data-product URI, so
        // the input value string is unchanged).
        String experimentDataDir = configBuilder.getExperimentDataDir();
        String inputStorageId = configBuilder.getInputStorageResourceId();
        for (InputDataObjectType input : experiment.getExperimentInputsList()) {
            String value = input.getValue();
            if (value == null || value.isEmpty()) {
                continue;
            }
            if (input.getType() == DataType.URI) {
                if (value.startsWith(DataProductInterface.schema)) {
                    relocateTmpInputUpload(value, experimentDataDir, inputStorageId);
                }
            } else if (input.getType() == DataType.URI_COLLECTION) {
                for (String member : value.split(",")) {
                    if (!member.isEmpty() && member.startsWith(DataProductInterface.schema)) {
                        relocateTmpInputUpload(member, experimentDataDir, inputStorageId);
                    }
                }
            }
        }

        // 3d PERSIST: store the updated configuration and refresh the in-memory experiment. Input
        // URI values are preserved (repoint-in-place), so no updateExperiment is needed.
        UserConfigurationDataModel updatedConfig = configBuilder.build();
        experimentRegistry.updateExperimentConfiguration(experimentId, updatedConfig);
        return experiment.toBuilder().setUserConfigurationData(updatedConfig).build();
    }

    /**
     * If the data product referenced by {@code dataProductUri} is a tmp upload, relocate its bytes
     * into the experiment data dir and repoint the replica's file_path in place. Best-effort: never
     * fails the launch (a missing/unidentifiable replica is logged and skipped).
     */
    private void relocateTmpInputUpload(String dataProductUri, String experimentDataDir, String storageId) {
        try {
            DataProductModel dataProduct = dataProductInterface.getDataProduct(dataProductUri);
            if (dataProduct == null) {
                return;
            }
            DataReplicaLocationModel replica = gatewayDataStoreReplica(dataProduct);
            String srcPath = replicaFilesystemPath(replica);
            if (srcPath == null) {
                logger.warn("No replica file path for data product {}; skipping move", dataProductUri);
                return;
            }
            // tmp upload iff the file's parent directory basename is the tmp upload dir.
            boolean isTmp = TMP_INPUT_FILE_UPLOAD_DIR.equals(basename(dirname(srcPath)));
            if (!isTmp) {
                return;
            }

            String fileName =
                    !dataProduct.getProductName().isEmpty() ? dataProduct.getProductName() : basename(srcPath);
            String dstPath = joinStoragePath(experimentDataDir, fileName);

            // Idempotency guard: if the destination already exists and the source no longer does,
            // the bytes were already moved by a prior (e.g. client-side) prep — just repoint.
            boolean alreadyMoved =
                    storagePrep.fileExists(storageId, dstPath) && !storagePrep.fileExists(storageId, srcPath);
            if (!alreadyMoved) {
                storagePrep.moveFile(storageId, srcPath, dstPath);
            }

            // Repoint the replica's file_path to the new location, preserving the data-product URI.
            if (!replica.getReplicaId().isEmpty()) {
                DataReplicaLocationModel updatedReplica =
                        replica.toBuilder().setFilePath(dstPath).build();
                dataReplicaLocationInterface.updateReplicaLocation(updatedReplica);
            } else {
                logger.warn(
                        "Replica for data product {} has no replica_id; moved bytes but could not repoint replica file_path",
                        dataProductUri);
            }
        } catch (Exception e) {
            // Never fail launch on a single input relocation: log and continue.
            logger.warn("Could not relocate tmp input upload {}: {}", dataProductUri, e.getMessage());
        }
    }

    private static String sanitizePathComponent(String name) {
        return (name == null ? "" : name).trim().replace(" ", "_");
    }

    // The GATEWAY_DATA_STORE replica, or the first replica, or null.
    private static DataReplicaLocationModel gatewayDataStoreReplica(DataProductModel dataProduct) {
        DataReplicaLocationModel first = null;
        for (DataReplicaLocationModel replica : dataProduct.getReplicaLocationsList()) {
            if (first == null) {
                first = replica;
            }
            if (replica.getReplicaLocationCategory() == ReplicaLocationCategory.GATEWAY_DATA_STORE) {
                return replica;
            }
        }
        return first;
    }

    // The plain filesystem path from a replica's file_path (which may be stored as
    // file://<host>:<path> or URL-encoded). Ports the SDK's unquote(urlparse(path).path).
    private static String replicaFilesystemPath(DataReplicaLocationModel replica) {
        if (replica == null || replica.getFilePath().isEmpty()) {
            return null;
        }
        return urlDecode(urlPath(replica.getFilePath()));
    }

    // Extract the path component the way Python's urlparse(...).path does: for a value with a
    // "scheme://netloc" prefix, drop the scheme and netloc (everything up to the first '/' after the
    // "//"); for a bare path, return it unchanged. Query/fragment are not expected on storage paths.
    private static String urlPath(String value) {
        int schemeSep = value.indexOf("://");
        if (schemeSep < 0) {
            return value;
        }
        int netlocStart = schemeSep + 3;
        int pathStart = value.indexOf('/', netlocStart);
        return pathStart < 0 ? "" : value.substring(pathStart);
    }

    // Percent-decode like Python's urllib.parse.unquote: only %XX escapes are decoded (UTF-8); a
    // literal '+' is preserved (unlike java.net.URLDecoder, which would turn it into a space and
    // corrupt filenames containing '+').
    private static String urlDecode(String value) {
        if (value.indexOf('%') < 0) {
            return value;
        }
        java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
        for (int i = 0; i < value.length(); ) {
            char c = value.charAt(i);
            if (c == '%' && i + 2 < value.length()) {
                int hi = Character.digit(value.charAt(i + 1), 16);
                int lo = Character.digit(value.charAt(i + 2), 16);
                if (hi >= 0 && lo >= 0) {
                    bytes.write((hi << 4) + lo);
                    i += 3;
                    continue;
                }
            }
            // Not a valid escape: emit the character's UTF-8 bytes verbatim.
            byte[] cb = String.valueOf(c).getBytes(java.nio.charset.StandardCharsets.UTF_8);
            bytes.write(cb, 0, cb.length);
            i++;
        }
        return new String(bytes.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
    }

    // basename/dirname matching Python's os.path semantics for the forward-slash paths used here.
    private static String basename(String path) {
        if (path == null) {
            return "";
        }
        int slash = path.lastIndexOf('/');
        return slash < 0 ? path : path.substring(slash + 1);
    }

    private static String dirname(String path) {
        if (path == null) {
            return "";
        }
        int slash = path.lastIndexOf('/');
        return slash < 0 ? "" : path.substring(0, slash);
    }

    private static String joinStoragePath(String directory, String name) {
        if (directory == null || directory.isEmpty()) {
            return name;
        }
        String trimmed = directory;
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed + "/" + name;
    }

    /**
     * Composite launch for thin clients: when the experiment has email notifications enabled, override its
     * recipients to the launching user's address ({@code notificationEmail}) before launching. The storage
     * setup (default storage ids, data-dir creation, tmp-upload relocation) and all authorization run inside
     * {@link #launchExperiment}, so this only adds the email override and delegates — replacing the portal's
     * get/update/launch round-trip (Python SDK experiment_orchestration.launch()).
     */
    public void launchExperimentWithStorageSetup(
            RequestContext ctx, String experimentId, String gatewayId, String notificationEmail)
            throws ServiceException {
        try {
            ExperimentModel experiment = experimentRegistry.getExperiment(experimentId);
            if (experiment == null) {
                throw new ServiceException("Experiment " + experimentId + " does not exist");
            }
            if (experiment.getEnableEmailNotification() && notificationEmail != null && !notificationEmail.isEmpty()) {
                experimentRegistry.updateExperiment(
                        experimentId,
                        experiment.toBuilder()
                                .clearEmailAddresses()
                                .addEmailAddresses(notificationEmail)
                                .build());
            }
            launchExperiment(ctx, experimentId, gatewayId);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error launching experiment " + experimentId + ": " + e.getMessage(), e);
        }
    }

    public void launchExperiment(RequestContext ctx, String experimentId, String gatewayId) throws ServiceException {
        try {
            ExperimentModel experiment = experimentRegistry.getExperiment(experimentId);
            if (experiment == null) {
                throw new ServiceException("Experiment " + experimentId + " does not exist");
            }

            // Server-side launch-prep: finalize storage ids + data dir and relocate tmp input
            // uploads into the data dir, mirroring the Python SDK's pre-launch preparation. Runs
            // before the GRP backfill so the orchestrator sees the finalized model. Guarded by a
            // null check: during the SDK migration the thick SDK still preps client-side, so this
            // must no-op (or repeat idempotently) when the work is already done.
            if (storagePrep != null) {
                experiment = prepareExperimentForLaunch(experimentId, experiment);
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

            if (launchOrchestrator.isEmpty()) {
                throw new ServiceException("No launch orchestrator is available to submit experiment " + experimentId);
            }
            launchOrchestrator.get().launchExperiment(experimentId, gatewayId);
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
