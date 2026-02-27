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
import org.apache.airavata.execution.process.ProcessEntity;
import org.apache.airavata.execution.process.ProcessMapper;
import org.apache.airavata.execution.process.ProcessModel;
import org.apache.airavata.execution.process.ProcessRepository;
import org.apache.airavata.research.experiment.entity.ExperimentEntity;
import org.apache.airavata.research.experiment.mapper.ExperimentMapper;
import org.apache.airavata.research.experiment.model.Experiment;
import org.apache.airavata.research.experiment.repository.ExperimentRepository;
import org.apache.airavata.research.experiment.util.ExperimentUtil;
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
    private final ProcessMapper processMapper;
    private final ExperimentMapper experimentMapper;

    public TaskContextFactory(
            ProcessRepository processRepository,
            ExperimentRepository experimentRepository,
            ResourceService resourceService,
            ProcessMapper processMapper,
            ExperimentMapper experimentMapper) {
        this.processRepository = processRepository;
        this.experimentRepository = experimentRepository;
        this.resourceService = resourceService;
        this.processMapper = processMapper;
        this.experimentMapper = experimentMapper;
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
        ProcessEntity processEntity = processRepository
                .findById(processId)
                .orElseThrow(() -> new IllegalStateException("Process not found: " + processId));
        ProcessModel processModel = processMapper.toModel(processEntity);

        // Load experiment entity
        ExperimentEntity experimentEntity = experimentRepository
                .findById(processEntity.getExperimentId())
                .orElseThrow(
                        () -> new IllegalStateException("Experiment not found: " + processEntity.getExperimentId()));
        Experiment experimentModel = experimentMapper.toModel(experimentEntity);

        // Set experiment data dir from process resource schedule if available
        if (processModel.getResourceSchedule() != null) {
            Object dataDir = processModel.getResourceSchedule().get("experimentDataDir");
            if (dataDir != null && !dataDir.toString().isBlank()) {
                processModel.setExperimentDataDir(dataDir.toString());
            }
        }

        // Build the base TaskContext
        TaskContext context = new TaskContext(processId, gatewayId, taskId, processModel);
        context.setExperiment(experimentModel);

        // Set experiment inputs/outputs on context (converted to ApplicationInput/ApplicationOutput)
        if (experimentEntity.getInputs() != null) {
            context.setProcessInputs(experimentEntity.getInputs().stream()
                    .map(e -> ExperimentUtil.toApplicationInput(ExperimentMapper.toInputModel(e)))
                    .toList());
        }
        if (experimentEntity.getOutputs() != null) {
            context.setProcessOutputs(experimentEntity.getOutputs().stream()
                    .map(e -> ExperimentUtil.toApplicationOutput(ExperimentMapper.toOutputModel(e)))
                    .toList());
        }

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
                            context.setJobSubmissionProtocol(JobSubmissionProtocol.valueOf(protocolStr.toUpperCase()));
                        } catch (IllegalArgumentException ex) {
                            logger.warn(
                                    "Unknown job submission protocol '{}' on resource {}; skipping protocol set",
                                    protocolStr,
                                    processModel.getResourceId());
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
}
