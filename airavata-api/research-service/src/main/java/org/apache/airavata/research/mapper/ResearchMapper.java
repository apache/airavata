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
package org.apache.airavata.research.mapper;

import org.apache.airavata.mapper.CommonMapperConversions;
import org.apache.airavata.model.application.io.proto.InputDataObjectType;
import org.apache.airavata.model.application.io.proto.OutputDataObjectType;
import org.apache.airavata.model.commons.proto.ErrorModel;
import org.apache.airavata.model.experiment.proto.ExperimentModel;
import org.apache.airavata.model.experiment.proto.ExperimentSummaryModel;
import org.apache.airavata.model.status.proto.ExperimentStatus;
import org.apache.airavata.model.workspace.proto.Notification;
import org.apache.airavata.model.workspace.proto.Project;
import org.apache.airavata.research.model.*;
import org.apache.airavata.research.model.NotificationEntity;
import org.apache.airavata.research.model.ProjectEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ResearchMapper extends CommonMapperConversions {

    ResearchMapper INSTANCE = Mappers.getMapper(ResearchMapper.class);

    // --- Experiment ---
    @Mapping(target = "emailAddressesList", ignore = true)
    ExperimentModel experimentToModel(ExperimentEntity entity);

    @Mapping(target = "emailAddresses", expression = "java(listToCsv(model.getEmailAddressesList()))")
    ExperimentEntity experimentToEntity(ExperimentModel model);

    // --- ExperimentSummary ---
    ExperimentSummaryModel experimentSummaryToModel(ExperimentSummaryEntity entity);

    ExperimentSummaryEntity experimentSummaryToEntity(ExperimentSummaryModel model);

    // --- ExperimentStatus ---
    ExperimentStatus experimentStatusToModel(ExperimentStatusEntity entity);

    ExperimentStatusEntity experimentStatusToEntity(ExperimentStatus model);

    // --- ExperimentError ---
    ErrorModel experimentErrorToModel(ExperimentErrorEntity entity);

    ExperimentErrorEntity experimentErrorToEntity(ErrorModel model);

    // --- ExperimentInput ---
    InputDataObjectType experimentInputToModel(ExperimentInputEntity entity);

    ExperimentInputEntity experimentInputToEntity(InputDataObjectType model);

    // --- ExperimentOutput ---
    OutputDataObjectType experimentOutputToModel(ExperimentOutputEntity entity);

    ExperimentOutputEntity experimentOutputToEntity(OutputDataObjectType model);

    // --- Project ---
    Project projectToModel(ProjectEntity entity);

    ProjectEntity projectToEntity(Project model);

    // --- Notification ---
    Notification notificationToModel(NotificationEntity entity);

    NotificationEntity notificationToEntity(Notification model);
}
