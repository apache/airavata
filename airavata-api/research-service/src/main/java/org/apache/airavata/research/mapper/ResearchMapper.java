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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.mapper.CommonMapperConversions;
import org.apache.airavata.model.appcatalog.appdeployment.proto.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.proto.ApplicationModule;
import org.apache.airavata.model.appcatalog.appdeployment.proto.CommandObject;
import org.apache.airavata.model.appcatalog.appdeployment.proto.SetEnvPaths;
import org.apache.airavata.model.appcatalog.appinterface.proto.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.parser.proto.IOType;
import org.apache.airavata.model.appcatalog.parser.proto.Parser;
import org.apache.airavata.model.appcatalog.parser.proto.ParserInput;
import org.apache.airavata.model.appcatalog.parser.proto.ParserOutput;
import org.apache.airavata.model.appcatalog.parser.proto.ParsingTemplate;
import org.apache.airavata.model.appcatalog.parser.proto.ParsingTemplateInput;
import org.apache.airavata.model.application.io.proto.InputDataObjectType;
import org.apache.airavata.model.application.io.proto.OutputDataObjectType;
import org.apache.airavata.model.commons.proto.ErrorModel;
import org.apache.airavata.model.data.replica.proto.DataProductModel;
import org.apache.airavata.model.data.replica.proto.DataReplicaLocationModel;
import org.apache.airavata.model.experiment.proto.ExperimentModel;
import org.apache.airavata.model.experiment.proto.ExperimentSummaryModel;
import org.apache.airavata.model.status.proto.ExperimentStatus;
import org.apache.airavata.model.workspace.proto.Notification;
import org.apache.airavata.model.workspace.proto.Project;
import org.apache.airavata.research.model.*;
import org.apache.airavata.research.model.AppIoParamEntity;
import org.apache.airavata.research.model.ApplicationDeploymentEntity;
import org.apache.airavata.research.model.ApplicationInterfaceEntity;
import org.apache.airavata.research.model.ApplicationModuleEntity;
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

    // --- Project ---
    @Mapping(target = "projectId", source = "projectID")
    Project projectToModel(ProjectEntity entity);

    @Mapping(target = "projectID", source = "projectId")
    ProjectEntity projectToEntity(Project model);

    // --- Notification ---
    Notification notificationToModel(NotificationEntity entity);

    NotificationEntity notificationToEntity(Notification model);

    // --- DataProductModel ---
    DataProductModel dataProductToModel(DataProductEntity entity);

    DataProductEntity dataProductToEntity(DataProductModel model);

    // --- DataReplicaLocationModel ---
    DataReplicaLocationModel dataReplicaToModel(DataReplicaLocationEntity entity);

    DataReplicaLocationEntity dataReplicaToEntity(DataReplicaLocationModel model);

    // --- ApplicationInterfaceDescription ---
    ApplicationInterfaceDescription appInterfaceToModel(ApplicationInterfaceEntity entity);

    ApplicationInterfaceEntity appInterfaceToEntity(ApplicationInterfaceDescription model);

    // --- ApplicationModule ---
    ApplicationModule appModuleToModel(ApplicationModuleEntity entity);

    ApplicationModuleEntity appModuleToEntity(ApplicationModule model);

    // --- ApplicationDeploymentDescription ---
    default ApplicationDeploymentDescription appDeploymentToModel(ApplicationDeploymentEntity entity) {
        if (entity == null) return null;
        ApplicationDeploymentDescription.Builder builder = ApplicationDeploymentDescription.newBuilder();
        if (entity.getAppDeploymentId() != null) builder.setAppDeploymentId(entity.getAppDeploymentId());
        if (entity.getAppDeploymentDescription() != null)
            builder.setAppDeploymentDescription(entity.getAppDeploymentDescription());
        if (entity.getExecutablePath() != null) builder.setExecutablePath(entity.getExecutablePath());
        if (entity.getComputeHostId() != null) builder.setComputeHostId(entity.getComputeHostId());
        if (entity.getAppModuleId() != null) builder.setAppModuleId(entity.getAppModuleId());
        if (entity.getParallelism() != null) builder.setParallelism(entity.getParallelism());
        if (entity.getDefaultQueueName() != null) builder.setDefaultQueueName(entity.getDefaultQueueName());
        builder.setDefaultNodeCount(entity.getDefaultNodeCount());
        builder.setDefaultCpuCount(entity.getDefaultCPUCount());
        builder.setDefaultWalltime(entity.getDefaultWallTime());
        builder.setEditableByUser(entity.getEditableByUser());
        if (entity.getModuleLoadCmds() != null) {
            builder.addAllModuleLoadCmds(entity.getModuleLoadCmds().stream()
                    .map(m -> CommandObject.newBuilder()
                            .setCommand((String) m.getOrDefault("command", ""))
                            .setCommandOrder(((Number) m.getOrDefault("commandOrder", 0)).intValue())
                            .build())
                    .toList());
        }
        if (entity.getPreJobCommands() != null) {
            builder.addAllPreJobCommands(entity.getPreJobCommands().stream()
                    .map(m -> CommandObject.newBuilder()
                            .setCommand((String) m.getOrDefault("command", ""))
                            .setCommandOrder(((Number) m.getOrDefault("commandOrder", 0)).intValue())
                            .build())
                    .toList());
        }
        if (entity.getPostJobCommands() != null) {
            builder.addAllPostJobCommands(entity.getPostJobCommands().stream()
                    .map(m -> CommandObject.newBuilder()
                            .setCommand((String) m.getOrDefault("command", ""))
                            .setCommandOrder(((Number) m.getOrDefault("commandOrder", 0)).intValue())
                            .build())
                    .toList());
        }
        if (entity.getLibPrependPaths() != null) {
            builder.addAllLibPrependPaths(entity.getLibPrependPaths().stream()
                    .map(m -> SetEnvPaths.newBuilder()
                            .setName((String) m.getOrDefault("name", ""))
                            .setValue((String) m.getOrDefault("value", ""))
                            .setEnvPathOrder(((Number) m.getOrDefault("envPathOrder", 0)).intValue())
                            .build())
                    .toList());
        }
        if (entity.getLibAppendPaths() != null) {
            builder.addAllLibAppendPaths(entity.getLibAppendPaths().stream()
                    .map(m -> SetEnvPaths.newBuilder()
                            .setName((String) m.getOrDefault("name", ""))
                            .setValue((String) m.getOrDefault("value", ""))
                            .setEnvPathOrder(((Number) m.getOrDefault("envPathOrder", 0)).intValue())
                            .build())
                    .toList());
        }
        if (entity.getSetEnvironment() != null) {
            builder.addAllSetEnvironment(entity.getSetEnvironment().stream()
                    .map(m -> SetEnvPaths.newBuilder()
                            .setName((String) m.getOrDefault("name", ""))
                            .setValue((String) m.getOrDefault("value", ""))
                            .setEnvPathOrder(((Number) m.getOrDefault("envPathOrder", 0)).intValue())
                            .build())
                    .toList());
        }
        return builder.build();
    }

    default ApplicationDeploymentEntity appDeploymentToEntity(ApplicationDeploymentDescription model) {
        if (model == null) return null;
        ApplicationDeploymentEntity entity = new ApplicationDeploymentEntity();
        entity.setAppDeploymentId(model.getAppDeploymentId());
        entity.setAppDeploymentDescription(model.getAppDeploymentDescription());
        entity.setExecutablePath(model.getExecutablePath());
        entity.setComputeHostId(model.getComputeHostId());
        entity.setAppModuleId(model.getAppModuleId());
        entity.setParallelism(model.getParallelism());
        entity.setDefaultQueueName(model.getDefaultQueueName());
        entity.setDefaultNodeCount(model.getDefaultNodeCount());
        entity.setDefaultCPUCount(model.getDefaultCpuCount());
        entity.setDefaultWallTime(model.getDefaultWalltime());
        entity.setEditableByUser(model.getEditableByUser());
        if (!model.getModuleLoadCmdsList().isEmpty()) {
            entity.setModuleLoadCmds(model.getModuleLoadCmdsList().stream()
                    .map(cmd -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("command", cmd.getCommand());
                        m.put("commandOrder", cmd.getCommandOrder());
                        return m;
                    })
                    .toList());
        }
        if (!model.getPreJobCommandsList().isEmpty()) {
            entity.setPreJobCommands(model.getPreJobCommandsList().stream()
                    .map(cmd -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("command", cmd.getCommand());
                        m.put("commandOrder", cmd.getCommandOrder());
                        return m;
                    })
                    .toList());
        }
        if (!model.getPostJobCommandsList().isEmpty()) {
            entity.setPostJobCommands(model.getPostJobCommandsList().stream()
                    .map(cmd -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("command", cmd.getCommand());
                        m.put("commandOrder", cmd.getCommandOrder());
                        return m;
                    })
                    .toList());
        }
        if (!model.getLibPrependPathsList().isEmpty()) {
            entity.setLibPrependPaths(model.getLibPrependPathsList().stream()
                    .map(p -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("name", p.getName());
                        m.put("value", p.getValue());
                        m.put("envPathOrder", p.getEnvPathOrder());
                        return m;
                    })
                    .toList());
        }
        if (!model.getLibAppendPathsList().isEmpty()) {
            entity.setLibAppendPaths(model.getLibAppendPathsList().stream()
                    .map(p -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("name", p.getName());
                        m.put("value", p.getValue());
                        m.put("envPathOrder", p.getEnvPathOrder());
                        return m;
                    })
                    .toList());
        }
        if (!model.getSetEnvironmentList().isEmpty()) {
            entity.setSetEnvironment(model.getSetEnvironmentList().stream()
                    .map(p -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("name", p.getName());
                        m.put("value", p.getValue());
                        m.put("envPathOrder", p.getEnvPathOrder());
                        return m;
                    })
                    .toList());
        }
        return entity;
    }

    // --- ApplicationInput (AppIoParamEntity, DIRECTION=INPUT) ---
    default InputDataObjectType appInputToModel(AppIoParamEntity entity) {
        if (entity == null) return null;
        return InputDataObjectType.newBuilder()
                .setName(entity.getName() != null ? entity.getName() : "")
                .setValue(entity.getValue() != null ? entity.getValue() : "")
                .setType(
                        entity.getType() != null
                                ? entity.getType()
                                : org.apache.airavata.model.application.io.proto.DataType.DATA_TYPE_UNKNOWN)
                .setApplicationArgument(entity.getApplicationArgument() != null ? entity.getApplicationArgument() : "")
                .setStandardInput(entity.isStandardInput())
                .setUserFriendlyDescription(
                        entity.getUserFriendlyDescription() != null ? entity.getUserFriendlyDescription() : "")
                .setMetaData(entity.getMetaData() != null ? entity.getMetaData() : "")
                .setInputOrder(entity.getInputOrder())
                .setIsRequired(entity.isIsRequired())
                .setRequiredToAddedToCommandLine(entity.isRequiredToAddedToCommandLine())
                .setDataStaged(entity.isDataStaged())
                .setIsReadOnly(entity.isReadOnly())
                .setOverrideFilename(entity.getOverrideFilename() != null ? entity.getOverrideFilename() : "")
                .build();
    }

    default AppIoParamEntity appInputToEntity(InputDataObjectType model) {
        if (model == null) return null;
        AppIoParamEntity entity = new AppIoParamEntity();
        entity.setDirection("INPUT");
        entity.setName(model.getName());
        entity.setValue(model.getValue());
        entity.setType(model.getType());
        entity.setApplicationArgument(model.getApplicationArgument());
        entity.setStandardInput(model.getStandardInput());
        entity.setUserFriendlyDescription(model.getUserFriendlyDescription());
        entity.setMetaData(model.getMetaData());
        entity.setInputOrder(model.getInputOrder());
        entity.setIsRequired(model.getIsRequired());
        entity.setRequiredToAddedToCommandLine(model.getRequiredToAddedToCommandLine());
        entity.setDataStaged(model.getDataStaged());
        entity.setReadOnly(model.getIsReadOnly());
        entity.setOverrideFilename(model.getOverrideFilename());
        return entity;
    }

    // --- ApplicationOutput (AppIoParamEntity, DIRECTION=OUTPUT) ---
    default OutputDataObjectType appOutputToModel(AppIoParamEntity entity) {
        if (entity == null) return null;
        return OutputDataObjectType.newBuilder()
                .setName(entity.getName() != null ? entity.getName() : "")
                .setValue(entity.getValue() != null ? entity.getValue() : "")
                .setType(
                        entity.getType() != null
                                ? entity.getType()
                                : org.apache.airavata.model.application.io.proto.DataType.DATA_TYPE_UNKNOWN)
                .setApplicationArgument(entity.getApplicationArgument() != null ? entity.getApplicationArgument() : "")
                .setIsRequired(entity.isIsRequired())
                .setRequiredToAddedToCommandLine(entity.isRequiredToAddedToCommandLine())
                .setDataMovement(entity.isDataMovement())
                .setLocation(entity.getLocation() != null ? entity.getLocation() : "")
                .setSearchQuery(entity.getSearchQuery() != null ? entity.getSearchQuery() : "")
                .setOutputStreaming(entity.isOutputStreaming())
                .setMetaData(entity.getMetaData() != null ? entity.getMetaData() : "")
                .build();
    }

    default AppIoParamEntity appOutputToEntity(OutputDataObjectType model) {
        if (model == null) return null;
        AppIoParamEntity entity = new AppIoParamEntity();
        entity.setDirection("OUTPUT");
        entity.setName(model.getName());
        entity.setValue(model.getValue());
        entity.setType(model.getType());
        entity.setApplicationArgument(model.getApplicationArgument());
        entity.setIsRequired(model.getIsRequired());
        entity.setRequiredToAddedToCommandLine(model.getRequiredToAddedToCommandLine());
        entity.setDataMovement(model.getDataMovement());
        entity.setLocation(model.getLocation());
        entity.setSearchQuery(model.getSearchQuery());
        entity.setOutputStreaming(model.getOutputStreaming());
        entity.setMetaData(model.getMetaData());
        return entity;
    }

    // --- Parser ---
    default Parser parserToModel(ParserEntity entity) {
        if (entity == null) return null;
        Parser.Builder b = Parser.newBuilder();
        if (entity.getId() != null) b.setId(entity.getId());
        if (entity.getImageName() != null) b.setImageName(entity.getImageName());
        if (entity.getOutputDirPath() != null) b.setOutputDirPath(entity.getOutputDirPath());
        if (entity.getInputDirPath() != null) b.setInputDirPath(entity.getInputDirPath());
        if (entity.getExecutionCommand() != null) b.setExecutionCommand(entity.getExecutionCommand());
        if (entity.getGatewayId() != null) b.setGatewayId(entity.getGatewayId());
        if (entity.getInputFiles() != null) {
            for (Map<String, Object> m : entity.getInputFiles()) {
                ParserInput.Builder ib = ParserInput.newBuilder();
                if (m.get("id") != null) ib.setId((String) m.get("id"));
                if (m.get("name") != null) ib.setName((String) m.get("name"));
                if (m.get("requiredInput") != null) ib.setRequiredInput((Boolean) m.get("requiredInput"));
                if (m.get("parserId") != null) ib.setParserId((String) m.get("parserId"));
                if (m.get("type") != null) {
                    try {
                        ib.setType(IOType.valueOf((String) m.get("type")));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                b.addInputFiles(ib.build());
            }
        }
        if (entity.getOutputFiles() != null) {
            for (Map<String, Object> m : entity.getOutputFiles()) {
                ParserOutput.Builder ob = ParserOutput.newBuilder();
                if (m.get("id") != null) ob.setId((String) m.get("id"));
                if (m.get("name") != null) ob.setName((String) m.get("name"));
                if (m.get("requiredOutput") != null) ob.setRequiredOutput((Boolean) m.get("requiredOutput"));
                if (m.get("parserId") != null) ob.setParserId((String) m.get("parserId"));
                if (m.get("type") != null) {
                    try {
                        ob.setType(IOType.valueOf((String) m.get("type")));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                b.addOutputFiles(ob.build());
            }
        }
        return b.build();
    }

    default ParserEntity parserToEntity(Parser model) {
        if (model == null) return null;
        ParserEntity entity = new ParserEntity();
        entity.setId(model.getId());
        entity.setImageName(model.getImageName());
        entity.setOutputDirPath(model.getOutputDirPath());
        entity.setInputDirPath(model.getInputDirPath());
        entity.setExecutionCommand(model.getExecutionCommand());
        entity.setGatewayId(model.getGatewayId());
        if (!model.getInputFilesList().isEmpty()) {
            List<Map<String, Object>> inputs = new ArrayList<>();
            for (ParserInput pi : model.getInputFilesList()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", pi.getId());
                m.put("name", pi.getName());
                m.put("requiredInput", pi.getRequiredInput());
                m.put("parserId", pi.getParserId());
                m.put("type", pi.getType().name());
                inputs.add(m);
            }
            entity.setInputFiles(inputs);
        }
        if (!model.getOutputFilesList().isEmpty()) {
            List<Map<String, Object>> outputs = new ArrayList<>();
            for (ParserOutput po : model.getOutputFilesList()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", po.getId());
                m.put("name", po.getName());
                m.put("requiredOutput", po.getRequiredOutput());
                m.put("parserId", po.getParserId());
                m.put("type", po.getType().name());
                outputs.add(m);
            }
            entity.setOutputFiles(outputs);
        }
        return entity;
    }

    // --- ParsingTemplate ---
    default ParsingTemplate parsingTemplateToModel(ParsingTemplateEntity entity) {
        if (entity == null) return null;
        ParsingTemplate.Builder b = ParsingTemplate.newBuilder();
        if (entity.getId() != null) b.setId(entity.getId());
        if (entity.getApplicationInterface() != null) b.setApplicationInterface(entity.getApplicationInterface());
        if (entity.getGatewayId() != null) b.setGatewayId(entity.getGatewayId());
        if (entity.getInitialInputs() != null) {
            for (Map<String, Object> m : entity.getInitialInputs()) {
                ParsingTemplateInput.Builder ib = ParsingTemplateInput.newBuilder();
                if (m.get("id") != null) ib.setId((String) m.get("id"));
                if (m.get("targetInputId") != null) ib.setTargetInputId((String) m.get("targetInputId"));
                if (m.get("applicationOutputName") != null)
                    ib.setApplicationOutputName((String) m.get("applicationOutputName"));
                if (m.get("value") != null) ib.setValue((String) m.get("value"));
                if (m.get("parsingTemplateId") != null) ib.setParsingTemplateId((String) m.get("parsingTemplateId"));
                b.addInitialInputs(ib.build());
            }
        }
        return b.build();
    }

    default ParsingTemplateEntity parsingTemplateToEntity(ParsingTemplate model) {
        if (model == null) return null;
        ParsingTemplateEntity entity = new ParsingTemplateEntity();
        entity.setId(model.getId());
        entity.setApplicationInterface(model.getApplicationInterface());
        entity.setGatewayId(model.getGatewayId());
        if (!model.getInitialInputsList().isEmpty()) {
            List<Map<String, Object>> inputs = new ArrayList<>();
            for (ParsingTemplateInput pti : model.getInitialInputsList()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", pti.getId());
                m.put("targetInputId", pti.getTargetInputId());
                m.put("applicationOutputName", pti.getApplicationOutputName());
                m.put("value", pti.getValue());
                m.put("parsingTemplateId", pti.getParsingTemplateId());
                inputs.add(m);
            }
            entity.setInitialInputs(inputs);
        }
        return entity;
    }
}
