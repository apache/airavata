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
package org.apache.airavata.experiment.mapper;

import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.common.CommonMapperConversions;
import org.apache.airavata.model.application.io.proto.InputDataObjectType;
import org.apache.airavata.model.application.io.proto.OutputDataObjectType;
import org.apache.airavata.model.commons.proto.ErrorModel;
import org.apache.airavata.model.experiment.proto.ExperimentModel;
import org.apache.airavata.model.experiment.proto.ExperimentSummaryModel;
import org.apache.airavata.model.status.proto.ExperimentStatus;
import org.apache.airavata.model.workspace.proto.Project;
import org.apache.airavata.experiment.model.*;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ExperimentMapper extends CommonMapperConversions {

    ExperimentMapper INSTANCE = Mappers.getMapper(ExperimentMapper.class);

    // --- Project ---
    @Mapping(target = "projectId", source = "projectID")
    Project projectToModel(ProjectEntity entity);

    @Mapping(target = "projectID", source = "projectId")
    ProjectEntity projectToEntity(Project model);

    // --- Experiment ---
    @Mapping(target = "emailAddressesList", ignore = true)
    ExperimentModel experimentToModel(ExperimentEntity entity);

    @Mapping(target = "emailAddresses", expression = "java(listToCsv(model.getEmailAddressesList()))")
    ExperimentEntity experimentToEntity(ExperimentModel model);

    // MapStruct does not match protobuf's repeated accessors
    // (getExperimentInputsList(), etc.)
    // to the entity's list properties, so it silently drops these child
    // collections. Map them here.
    @AfterMapping
    default void afterExperimentToModel(ExperimentEntity entity, @MappingTarget ExperimentModel.Builder builder) {
        if (entity.getExperimentInputs() != null) {
            for (ResearchIoParamEntity input : entity.getExperimentInputs()) {
                builder.addExperimentInputs(experimentInputToModel(input));
            }
        }
        if (entity.getExperimentOutputs() != null) {
            for (ResearchIoParamEntity output : entity.getExperimentOutputs()) {
                builder.addExperimentOutputs(experimentOutputToModel(output));
            }
        }
        if (entity.getExperimentStatus() != null) {
            for (ExperimentStatusEntity status : entity.getExperimentStatus()) {
                builder.addExperimentStatus(experimentStatusToModel(status));
            }
        }
        if (entity.getErrors() != null) {
            for (ExperimentErrorEntity error : entity.getErrors()) {
                builder.addErrors(experimentErrorToModel(error));
            }
        }
        // emailAddresses is stored as a CSV column; restore the proto repeated field
        // (MapStruct does not map getEmailAddressesList(), so it would otherwise be
        // dropped on read).
        if (entity.getEmailAddresses() != null) {
            java.util.List<String> emails = csvToList(entity.getEmailAddresses());
            if (emails != null)
                builder.addAllEmailAddresses(emails);
        }
    }

    @AfterMapping
    default void afterExperimentToEntity(ExperimentModel model, @MappingTarget ExperimentEntity entity) {
        if (!model.getExperimentInputsList().isEmpty()) {
            List<ResearchIoParamEntity> inputs = new ArrayList<>();
            for (InputDataObjectType input : model.getExperimentInputsList()) {
                inputs.add(experimentInputToEntity(input));
            }
            entity.setExperimentInputs(inputs);
        }
        if (!model.getExperimentOutputsList().isEmpty()) {
            List<ResearchIoParamEntity> outputs = new ArrayList<>();
            for (OutputDataObjectType output : model.getExperimentOutputsList()) {
                outputs.add(experimentOutputToEntity(output));
            }
            entity.setExperimentOutputs(outputs);
        }
        if (!model.getExperimentStatusList().isEmpty()) {
            List<ExperimentStatusEntity> statuses = new ArrayList<>();
            for (ExperimentStatus status : model.getExperimentStatusList()) {
                ExperimentStatusEntity statusEntity = experimentStatusToEntity(status);
                // Set the @ManyToOne back-reference so the child persists with its parent.
                statusEntity.setExperiment(entity);
                statuses.add(statusEntity);
            }
            entity.setExperimentStatus(statuses);
        }
        if (!model.getErrorsList().isEmpty()) {
            List<ExperimentErrorEntity> errors = new ArrayList<>();
            for (ErrorModel error : model.getErrorsList()) {
                ExperimentErrorEntity errorEntity = experimentErrorToEntity(error);
                // Set the @ManyToOne back-reference so the child persists with its parent.
                errorEntity.setExperiment(entity);
                errors.add(errorEntity);
            }
            entity.setErrors(errors);
        }
    }

    // --- ExperimentSummary ---
    ExperimentSummaryModel experimentSummaryToModel(ExperimentSummaryEntity entity);

    ExperimentSummaryEntity experimentSummaryToEntity(ExperimentSummaryModel model);

    // --- ExperimentStatus ---
    ExperimentStatus experimentStatusToModel(ExperimentStatusEntity entity);

    ExperimentStatusEntity experimentStatusToEntity(ExperimentStatus model);

    // --- ExperimentError ---
    ErrorModel experimentErrorToModel(ExperimentErrorEntity entity);

    ExperimentErrorEntity experimentErrorToEntity(ErrorModel model);

    // The proto repeated root_cause_error_id_list is stored as a CSV String on the
    // entity, so
    // MapStruct cannot map it automatically. Bridge it with the shared CSV
    // converters.
    @AfterMapping
    default void afterExperimentErrorToModel(ExperimentErrorEntity entity, @MappingTarget ErrorModel.Builder builder) {
        List<String> rootCauses = csvToList(entity.getRootCauseErrorIdList());
        if (rootCauses != null) {
            builder.addAllRootCauseErrorIdList(rootCauses);
        }
    }

    @AfterMapping
    default void afterExperimentErrorToEntity(ErrorModel model, @MappingTarget ExperimentErrorEntity entity) {
        entity.setRootCauseErrorIdList(listToCsv(model.getRootCauseErrorIdListList()));
    }

    // --- ExperimentInput ---
    InputDataObjectType experimentInputToModel(ResearchIoParamEntity entity);

    @Mapping(target = "direction", ignore = true)
    ResearchIoParamEntity ioParamFromInput(InputDataObjectType model);

    default ResearchIoParamEntity experimentInputToEntity(InputDataObjectType model) {
        ResearchIoParamEntity entity = ioParamFromInput(model);
        entity.setDirection("INPUT");
        return entity;
    }

    // --- ExperimentOutput ---
    OutputDataObjectType experimentOutputToModel(ResearchIoParamEntity entity);

    @Mapping(target = "direction", ignore = true)
    ResearchIoParamEntity ioParamFromOutput(OutputDataObjectType model);

    default ResearchIoParamEntity experimentOutputToEntity(OutputDataObjectType model) {
        ResearchIoParamEntity entity = ioParamFromOutput(model);
        entity.setDirection("OUTPUT");
        return entity;
    }
}
