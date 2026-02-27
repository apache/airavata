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
package org.apache.airavata.research.experiment.mapper;

import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.core.mapper.EntityMapper;
import org.apache.airavata.core.util.IdGenerator;
import org.apache.airavata.research.experiment.entity.ExperimentEntity;
import org.apache.airavata.research.experiment.entity.ExperimentInputEntity;
import org.apache.airavata.research.experiment.entity.ExperimentOutputEntity;
import org.apache.airavata.research.experiment.model.Experiment;
import org.apache.airavata.research.experiment.model.ExperimentInput;
import org.apache.airavata.research.experiment.model.ExperimentOutput;
import org.apache.airavata.research.experiment.model.ExperimentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Hand-written Spring component mapper for {@link ExperimentEntity} and {@link Experiment}.
 *
 * <p>MapStruct is not used here because the mapping requires:
 * <ul>
 *   <li>Bidirectional conversion between {@link ExperimentInputEntity}/{@link ExperimentOutputEntity}
 *       and {@link ExperimentInput}/{@link ExperimentOutput} domain models.</li>
 *   <li>Setting the back-reference ({@code experiment}) on each child entity during toEntity.</li>
 *   <li>{@code createdAt} on the entity is a nullable {@code Instant}; passed through directly.</li>
 *   <li>State conversion between String (entity) and {@link ExperimentState} enum (model).</li>
 * </ul>
 */
@Component
public class ExperimentMapper implements EntityMapper<ExperimentEntity, Experiment> {

    private static final Logger logger = LoggerFactory.getLogger(ExperimentMapper.class);

    @Override
    public Experiment toModel(ExperimentEntity entity) {
        var model = new Experiment();
        model.setExperimentId(entity.getExperimentId());
        model.setProjectId(entity.getProjectId());
        model.setGatewayId(entity.getGatewayId());
        model.setUserName(entity.getUserName());
        model.setExperimentName(entity.getExperimentName());
        model.setDescription(entity.getDescription());
        model.setApplicationId(entity.getApplicationId());
        model.setBindingId(entity.getBindingId());
        model.setScheduling(entity.getScheduling());
        model.setCreatedAt(entity.getCreatedAt());
        model.setParentExperimentId(entity.getParentExperimentId());
        model.setTags(entity.getTags());

        // State
        model.setState(entity.getState() != null ? entity.getState() : ExperimentState.CREATED);

        // Inputs
        if (entity.getInputs() != null) {
            model.setInputs(entity.getInputs().stream()
                    .map(ExperimentMapper::toInputModel)
                    .toList());
        } else {
            model.setInputs(new ArrayList<>());
        }

        // Outputs
        if (entity.getOutputs() != null) {
            model.setOutputs(entity.getOutputs().stream()
                    .map(ExperimentMapper::toOutputModel)
                    .toList());
        } else {
            model.setOutputs(new ArrayList<>());
        }

        // Initialise transient list so callers never encounter null.
        model.setProcesses(new ArrayList<>());
        return model;
    }

    @Override
    public ExperimentEntity toEntity(Experiment model) {
        var entity = new ExperimentEntity();
        entity.setExperimentId(model.getExperimentId());
        entity.setProjectId(model.getProjectId());
        entity.setGatewayId(model.getGatewayId());
        entity.setUserName(model.getUserName());
        entity.setExperimentName(model.getExperimentName());
        entity.setDescription(model.getDescription());
        entity.setApplicationId(model.getApplicationId());
        entity.setBindingId(model.getBindingId());
        entity.setScheduling(model.getScheduling());
        entity.setCreatedAt(model.getCreatedAt());
        entity.setParentExperimentId(model.getParentExperimentId());
        entity.setTags(model.getTags());

        // State
        entity.setState(model.getState());

        // Inputs — set back-reference to parent entity
        if (model.getInputs() != null) {
            List<ExperimentInputEntity> inputEntities = new ArrayList<>();
            for (ExperimentInput input : model.getInputs()) {
                var ie = toInputEntity(input);
                ie.setExperiment(entity);
                inputEntities.add(ie);
            }
            entity.setInputs(inputEntities);
        }

        // Outputs — set back-reference to parent entity
        if (model.getOutputs() != null) {
            List<ExperimentOutputEntity> outputEntities = new ArrayList<>();
            for (ExperimentOutput output : model.getOutputs()) {
                var oe = toOutputEntity(output);
                oe.setExperiment(entity);
                outputEntities.add(oe);
            }
            entity.setOutputs(outputEntities);
        }

        return entity;
    }

    // ---- Input conversion ----

    public static ExperimentInput toInputModel(ExperimentInputEntity entity) {
        var model = new ExperimentInput();
        model.setInputId(entity.getInputId());
        model.setName(entity.getName());
        model.setType(entity.getType());
        model.setArtifactId(entity.getArtifactId());
        model.setValue(entity.getValue());
        model.setCommandLineArg(entity.getCommandLineArg());
        model.setRequired(entity.isRequired());
        model.setAddToCommandLine(entity.isAddToCommandLine());
        model.setOrderIndex(entity.getOrderIndex());
        model.setDescription(entity.getDescription());
        return model;
    }

    private ExperimentInputEntity toInputEntity(ExperimentInput model) {
        var entity = new ExperimentInputEntity();
        entity.setInputId(IdGenerator.ensureId(model.getInputId()));
        entity.setName(model.getName());
        entity.setType(model.getType());
        entity.setArtifactId(model.getArtifactId());
        entity.setValue(model.getValue());
        entity.setCommandLineArg(model.getCommandLineArg());
        entity.setRequired(model.isRequired());
        entity.setAddToCommandLine(model.isAddToCommandLine());
        entity.setOrderIndex(model.getOrderIndex());
        entity.setDescription(model.getDescription());
        return entity;
    }

    // ---- Output conversion ----

    public static ExperimentOutput toOutputModel(ExperimentOutputEntity entity) {
        var model = new ExperimentOutput();
        model.setOutputId(entity.getOutputId());
        model.setName(entity.getName());
        model.setType(entity.getType());
        model.setArtifactId(entity.getArtifactId());
        model.setValue(entity.getValue());
        model.setCommandLineArg(entity.getCommandLineArg());
        model.setRequired(entity.isRequired());
        model.setDataMovement(entity.isDataMovement());
        model.setOrderIndex(entity.getOrderIndex());
        model.setDescription(entity.getDescription());
        model.setLocation(entity.getLocation());
        return model;
    }

    private ExperimentOutputEntity toOutputEntity(ExperimentOutput model) {
        var entity = new ExperimentOutputEntity();
        entity.setOutputId(IdGenerator.ensureId(model.getOutputId()));
        entity.setName(model.getName());
        entity.setType(model.getType());
        entity.setArtifactId(model.getArtifactId());
        entity.setValue(model.getValue());
        entity.setCommandLineArg(model.getCommandLineArg());
        entity.setRequired(model.isRequired());
        entity.setDataMovement(model.isDataMovement());
        entity.setOrderIndex(model.getOrderIndex());
        entity.setDescription(model.getDescription());
        entity.setLocation(model.getLocation());
        return entity;
    }
}
