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
package org.apache.airavata.execution.dag;

import org.apache.airavata.compute.resource.model.JobSubmissionProtocol;
import org.apache.airavata.compute.resource.model.Resource;
import org.apache.airavata.compute.resource.model.ResourceBinding;
import org.apache.airavata.compute.resource.service.ResourceService;
import org.apache.airavata.execution.entity.ProcessEntity;
import org.apache.airavata.execution.model.ProcessModel;
import org.apache.airavata.execution.repository.ProcessRepository;
import org.apache.airavata.execution.task.TaskContext;
import org.apache.airavata.research.experiment.entity.ExperimentEntity;
import org.apache.airavata.research.experiment.entity.ExperimentInputEntity;
import org.apache.airavata.research.experiment.entity.ExperimentOutputEntity;
import org.apache.airavata.research.experiment.model.ExperimentInput;
import org.apache.airavata.research.experiment.model.ExperimentModel;
import org.apache.airavata.research.experiment.model.ExperimentOutput;
import org.apache.airavata.research.experiment.repository.ExperimentRepository;
import org.apache.airavata.research.experiment.util.ExperimentModelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Builds a fully-enriched {@link TaskContext} from database entities.
 *
 * <p>Extracts the context-loading logic that was previously embedded in
 * {@code AbstractTask.loadContext()} into a standalone Spring bean. The DAG
 * engine calls this once per execution to create the shared context that
 * all tasks in the DAG operate on.
 *
 * <p>In addition to the core process and experiment models, this factory
 * resolves and sets:
 * <ul>
 *   <li>Experiment inputs/outputs (converted to {@link org.apache.airavata.research.application.model.ApplicationInput}
 *       / {@link org.apache.airavata.research.application.model.ApplicationOutput}) on the ProcessModel</li>
 *   <li>Experiment data directory from the process resource schedule</li>
 *   <li>Compute {@link Resource} and its {@link JobSubmissionProtocol}</li>
 *   <li>Credential {@link ResourceBinding}</li>
 * </ul>
 */
@Component
public class TaskContextFactory {

    private static final Logger logger = LoggerFactory.getLogger(TaskContextFactory.class);

    private final ProcessRepository processRepository;
    private final ExperimentRepository experimentRepository;
    private final ResourceService resourceService;

    public TaskContextFactory(
            ProcessRepository processRepository,
            ExperimentRepository experimentRepository,
            ResourceService resourceService) {
        this.processRepository = processRepository;
        this.experimentRepository = experimentRepository;
        this.resourceService = resourceService;
    }

    /**
     * Builds a fully-populated {@link TaskContext} for the given process.
     *
     * <p>Loads the process and experiment entities, converts experiment inputs/outputs
     * to {@code ApplicationInput}/{@code ApplicationOutput} on the ProcessModel, resolves
     * the compute resource and credential binding, and derives the job submission protocol
     * from the resource capabilities.
     *
     * @param processId  the process to load
     * @param gatewayId  the owning gateway
     * @param taskId     a unique task identifier for this execution
     * @return a new TaskContext with all resources fully populated
     * @throws IllegalStateException if the process or experiment cannot be found
     */
    public TaskContext buildContext(String processId, String gatewayId, String taskId) {
        // Load process entity
        ProcessEntity processEntity = processRepository.findById(processId)
                .orElseThrow(() -> new IllegalStateException("Process not found: " + processId));
        ProcessModel processModel = toProcessModel(processEntity);

        // Load experiment entity
        ExperimentEntity experimentEntity = experimentRepository.findById(processEntity.getExperimentId())
                .orElseThrow(() -> new IllegalStateException(
                        "Experiment not found: " + processEntity.getExperimentId()));
        ExperimentModel experimentModel = toExperimentModel(experimentEntity);

        // Enrich processModel with experiment inputs converted to ApplicationInput
        if (experimentEntity.getInputs() != null) {
            processModel.setProcessInputs(experimentEntity.getInputs().stream()
                    .map(e -> ExperimentModelUtil.toApplicationInput(toExperimentInput(e)))
                    .toList());
        }

        // Enrich processModel with experiment outputs converted to ApplicationOutput
        if (experimentEntity.getOutputs() != null) {
            processModel.setProcessOutputs(experimentEntity.getOutputs().stream()
                    .map(e -> ExperimentModelUtil.toApplicationOutput(toExperimentOutput(e)))
                    .toList());
        }

        // Set experiment data dir from process resource schedule if available
        if (processModel.getResourceSchedule() != null) {
            Object dataDir = processModel.getResourceSchedule().get("experimentDataDir");
            if (dataDir != null && !dataDir.toString().isBlank()) {
                processModel.setExperimentDataDir(dataDir.toString());
            }
        }

        // Build the base TaskContext
        TaskContext context = new TaskContext(processId, gatewayId, taskId, processModel);
        context.setExperimentModel(experimentModel);

        // Load and set compute resource
        if (processModel.getResourceId() != null) {
            try {
                Resource resource = resourceService.getResource(processModel.getResourceId());
                context.setComputeResource(resource);

                // Derive job submission protocol from resource capabilities
                if (resource != null
                        && resource.getCapabilities() != null
                        && resource.getCapabilities().getCompute() != null) {
                    String protocolStr = resource.getCapabilities().getCompute().getProtocol();
                    if (protocolStr != null && !protocolStr.isBlank()) {
                        try {
                            context.setJobSubmissionProtocol(
                                    JobSubmissionProtocol.valueOf(protocolStr.toUpperCase()));
                        } catch (IllegalArgumentException ex) {
                            logger.warn("Unknown job submission protocol '{}' on resource {}; skipping protocol set",
                                    protocolStr, processModel.getResourceId());
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("Could not load compute resource {}: {}", processModel.getResourceId(), e.getMessage());
            }
        }

        // Load and set credential resource binding
        if (processModel.getBindingId() != null) {
            try {
                ResourceBinding binding = resourceService.getBinding(processModel.getBindingId());
                context.setCredentialResourceBinding(binding);
            } catch (Exception e) {
                logger.warn("Could not load resource binding {}: {}", processModel.getBindingId(), e.getMessage());
            }
        }

        logger.info("Built TaskContext for process {} (experiment {})", processId, experimentModel.getExperimentId());
        return context;
    }

    // -------------------------------------------------------------------------
    // Entity-to-model converters
    // -------------------------------------------------------------------------

    private ProcessModel toProcessModel(ProcessEntity entity) {
        ProcessModel model = new ProcessModel();
        model.setProcessId(entity.getProcessId());
        model.setExperimentId(entity.getExperimentId());
        model.setApplicationId(entity.getApplicationId());
        model.setResourceId(entity.getResourceId());
        model.setBindingId(entity.getBindingId());
        model.setResourceSchedule(entity.getResourceSchedule());
        model.setProviderContext(entity.getProviderContext());
        return model;
    }

    private ExperimentModel toExperimentModel(ExperimentEntity entity) {
        ExperimentModel model = new ExperimentModel();
        model.setExperimentId(entity.getExperimentId());
        model.setExperimentName(entity.getExperimentName());
        model.setProjectId(entity.getProjectId());
        model.setGatewayId(entity.getGatewayId());
        model.setUserName(entity.getUserName());
        model.setDescription(entity.getDescription());
        model.setApplicationId(entity.getApplicationId());
        model.setBindingId(entity.getBindingId());
        return model;
    }

    /**
     * Converts an {@link ExperimentInputEntity} to its domain model {@link ExperimentInput}.
     * The domain model is then passed to {@link ExperimentModelUtil#toApplicationInput(ExperimentInput)}
     * to produce the {@code ApplicationInput} used by the execution pipeline.
     */
    private ExperimentInput toExperimentInput(ExperimentInputEntity entity) {
        ExperimentInput input = new ExperimentInput();
        input.setInputId(entity.getInputId());
        input.setName(entity.getName());
        input.setType(entity.getType());
        input.setArtifactId(entity.getArtifactId());
        input.setValue(entity.getValue());
        input.setCommandLineArg(entity.getCommandLineArg());
        input.setRequired(entity.isRequired());
        input.setAddToCommandLine(entity.isAddToCommandLine());
        input.setOrderIndex(entity.getOrderIndex());
        input.setDescription(entity.getDescription());
        return input;
    }

    /**
     * Converts an {@link ExperimentOutputEntity} to its domain model {@link ExperimentOutput}.
     * The domain model is then passed to {@link ExperimentModelUtil#toApplicationOutput(ExperimentOutput)}
     * to produce the {@code ApplicationOutput} used by the execution pipeline.
     */
    private ExperimentOutput toExperimentOutput(ExperimentOutputEntity entity) {
        ExperimentOutput output = new ExperimentOutput();
        output.setOutputId(entity.getOutputId());
        output.setName(entity.getName());
        output.setType(entity.getType());
        output.setArtifactId(entity.getArtifactId());
        output.setValue(entity.getValue());
        output.setCommandLineArg(entity.getCommandLineArg());
        output.setRequired(entity.isRequired());
        output.setDataMovement(entity.isDataMovement());
        output.setOrderIndex(entity.getOrderIndex());
        output.setDescription(entity.getDescription());
        output.setLocation(entity.getLocation());
        return output;
    }
}
