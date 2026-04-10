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
package org.apache.airavata.research.repository;

import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.model.application.io.proto.InputDataObjectType;
import org.apache.airavata.model.application.io.proto.OutputDataObjectType;
import org.apache.airavata.research.mapper.ResearchMapper;
import org.apache.airavata.research.model.ExperimentEntity;
import org.apache.airavata.research.model.ResearchIoParamEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Repository for experiment I/O parameters backed by the unified RESEARCH_IO_PARAM table.
 * Persistence is managed via cascade through {@link ExperimentEntity}.
 */
@Component
public class ResearchIoParamRepository {
    private static final Logger logger = LoggerFactory.getLogger(ResearchIoParamRepository.class);

    // --- Input operations ---

    public String addExperimentInputs(List<InputDataObjectType> experimentInputs, String experimentId)
            throws RegistryException {
        saveInputs(experimentInputs, experimentId);
        return experimentId;
    }

    public void updateExperimentInputs(List<InputDataObjectType> updatedExperimentInputs, String experimentId)
            throws RegistryException {
        saveInputs(updatedExperimentInputs, experimentId);
    }

    public List<InputDataObjectType> getExperimentInputs(String experimentId) throws RegistryException {
        return new ExperimentRepository().getExperiment(experimentId).getExperimentInputsList();
    }

    private void saveInputs(List<InputDataObjectType> inputs, String experimentId) throws RegistryException {
        ExperimentRepository experimentRepository = new ExperimentRepository();
        experimentRepository.execute(em -> {
            ExperimentEntity experimentEntity = em.find(ExperimentEntity.class, experimentId);
            if (experimentEntity == null) {
                throw new RuntimeException("Experiment not found: " + experimentId);
            }
            if (experimentEntity.getExperimentInputs() == null) {
                experimentEntity.setExperimentInputs(new ArrayList<>());
            }
            for (InputDataObjectType input : inputs) {
                ResearchIoParamEntity newEntity = ResearchMapper.INSTANCE.experimentInputToEntity(input);
                List<ResearchIoParamEntity> existing = experimentEntity.getExperimentInputs();
                boolean updated = false;
                for (int i = 0; i < existing.size(); i++) {
                    if (input.getName().equals(existing.get(i).getName())) {
                        newEntity.setParamId(existing.get(i).getParamId());
                        existing.set(i, newEntity);
                        updated = true;
                        break;
                    }
                }
                if (!updated) {
                    logger.debug("Adding new INPUT param '{}' for experiment {}", input.getName(), experimentId);
                    existing.add(newEntity);
                }
            }
            em.merge(experimentEntity);
            return null;
        });
    }

    // --- Output operations ---

    public String addExperimentOutputs(List<OutputDataObjectType> experimentOutputs, String experimentId)
            throws RegistryException {
        saveOutputs(experimentOutputs, experimentId);
        return experimentId;
    }

    public void updateExperimentOutputs(List<OutputDataObjectType> updatedExperimentOutputs, String experimentId)
            throws RegistryException {
        saveOutputs(updatedExperimentOutputs, experimentId);
    }

    public List<OutputDataObjectType> getExperimentOutputs(String experimentId) throws RegistryException {
        return new ExperimentRepository().getExperiment(experimentId).getExperimentOutputsList();
    }

    private void saveOutputs(List<OutputDataObjectType> outputs, String experimentId) throws RegistryException {
        ExperimentRepository experimentRepository = new ExperimentRepository();
        experimentRepository.execute(em -> {
            ExperimentEntity experimentEntity = em.find(ExperimentEntity.class, experimentId);
            if (experimentEntity == null) {
                throw new RuntimeException("Experiment not found: " + experimentId);
            }
            if (experimentEntity.getExperimentOutputs() == null) {
                experimentEntity.setExperimentOutputs(new ArrayList<>());
            }
            for (OutputDataObjectType output : outputs) {
                ResearchIoParamEntity newEntity = ResearchMapper.INSTANCE.experimentOutputToEntity(output);
                List<ResearchIoParamEntity> existing = experimentEntity.getExperimentOutputs();
                boolean updated = false;
                for (int i = 0; i < existing.size(); i++) {
                    if (output.getName().equals(existing.get(i).getName())) {
                        newEntity.setParamId(existing.get(i).getParamId());
                        existing.set(i, newEntity);
                        updated = true;
                        break;
                    }
                }
                if (!updated) {
                    logger.debug("Adding new OUTPUT param '{}' for experiment {}", output.getName(), experimentId);
                    existing.add(newEntity);
                }
            }
            em.merge(experimentEntity);
            return null;
        });
    }
}
