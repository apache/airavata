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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.airavata.config.ServiceConditionals.ConditionalOnParticipant;
import org.apache.airavata.core.model.DagTaskResult;
import org.apache.airavata.research.experiment.entity.ExperimentEntity;
import org.apache.airavata.research.experiment.entity.ExperimentOutputEntity;
import org.apache.airavata.research.experiment.repository.ExperimentRepository;
import org.apache.airavata.storage.resource.model.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persists experiment output URIs collected by the output staging task.
 *
 * <p>Reads all DAG state entries with the {@value #OUTPUT_KEY_PREFIX} prefix
 * and saves them as experiment output values. This decouples the storage
 * layer from experiment persistence.
 */
@Component("saveExperimentOutputsTask")
@ConditionalOnParticipant
@Transactional
public class SaveExperimentOutputsTask implements DagTask {

    private static final Logger logger = LoggerFactory.getLogger(SaveExperimentOutputsTask.class);

    static final String OUTPUT_KEY_PREFIX = "experimentOutput.";

    private final ExperimentRepository experimentRepository;

    public SaveExperimentOutputsTask(ExperimentRepository experimentRepository) {
        this.experimentRepository = experimentRepository;
    }

    @Override
    public DagTaskResult execute(TaskContext context) {
        String experimentId = context.getExperimentId();
        Map<String, String> dagState = context.getDagState();

        List<Map.Entry<String, String>> outputEntries = dagState.entrySet().stream()
                .filter(e -> e.getKey().startsWith(OUTPUT_KEY_PREFIX))
                .toList();

        if (outputEntries.isEmpty()) {
            logger.info("No experiment outputs to persist for experiment {}", experimentId);
            return new DagTaskResult.Success("No experiment outputs to persist");
        }

        ExperimentEntity entity = experimentRepository.findById(experimentId).orElse(null);
        if (entity == null) {
            logger.warn("Experiment {} not found, skipping output persistence", experimentId);
            return new DagTaskResult.Success("Experiment not found, skipped output persistence");
        }

        List<ExperimentOutputEntity> outputs = entity.getOutputs();
        if (outputs == null) {
            outputs = new ArrayList<>();
            entity.setOutputs(outputs);
        }

        Map<String, ExperimentOutputEntity> outputsByName =
                outputs.stream().collect(Collectors.toMap(ExperimentOutputEntity::getName, Function.identity()));

        for (Map.Entry<String, String> entry : outputEntries) {
            String outputName = entry.getKey().substring(OUTPUT_KEY_PREFIX.length());
            String outputValue = entry.getValue();

            ExperimentOutputEntity existing = outputsByName.get(outputName);
            if (existing != null) {
                existing.setValue(outputValue);
            } else {
                var newOutput = new ExperimentOutputEntity();
                newOutput.setOutputId(UUID.randomUUID().toString());
                newOutput.setName(outputName);
                newOutput.setValue(outputValue);
                newOutput.setType(DataType.STRING);
                newOutput.setExperiment(entity);
                outputs.add(newOutput);
            }
        }

        experimentRepository.save(entity);
        logger.info("Persisted {} experiment output(s) for experiment {}", outputEntries.size(), experimentId);
        return new DagTaskResult.Success(
                "Persisted " + outputEntries.size() + " experiment output(s) for experiment " + experimentId);
    }
}
