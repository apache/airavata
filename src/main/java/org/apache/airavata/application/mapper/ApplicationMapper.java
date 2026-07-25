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
package org.apache.airavata.application.mapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.common.CommonMapperConversions;
import org.apache.airavata.model.appcatalog.appdeployment.proto.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.proto.ApplicationModule;
import org.apache.airavata.model.appcatalog.appdeployment.proto.CommandObject;
import org.apache.airavata.model.appcatalog.appdeployment.proto.SetEnvPaths;
import org.apache.airavata.model.appcatalog.appinterface.proto.ApplicationInterfaceDescription;
import org.apache.airavata.model.application.io.proto.InputDataObjectType;
import org.apache.airavata.model.application.io.proto.OutputDataObjectType;
import org.apache.airavata.application.model.AppIoParamEntity;
import org.apache.airavata.application.model.ApplicationDeploymentEntity;
import org.apache.airavata.application.model.ApplicationInterfaceEntity;
import org.apache.airavata.application.model.ApplicationModuleEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ApplicationMapper extends CommonMapperConversions {

    ApplicationMapper INSTANCE = Mappers.getMapper(ApplicationMapper.class);

    // --- ApplicationInterfaceDescription ---
    ApplicationInterfaceDescription appInterfaceToModel(ApplicationInterfaceEntity entity);

    ApplicationInterfaceEntity appInterfaceToEntity(ApplicationInterfaceDescription model);

    // MapStruct does not match protobuf's repeated accessors to the entity's list
    // properties,
    // so it silently drops application_modules (plain strings) and the input/output
    // collections.
    @AfterMapping
    default void afterAppInterfaceToModel(
            ApplicationInterfaceEntity entity, @MappingTarget ApplicationInterfaceDescription.Builder builder) {
        if (entity.getApplicationModules() != null) {
            builder.addAllApplicationModules(entity.getApplicationModules());
        }
        if (entity.getApplicationInputs() != null) {
            for (AppIoParamEntity input : entity.getApplicationInputs()) {
                builder.addApplicationInputs(appInputToModel(input));
            }
        }
        if (entity.getApplicationOutputs() != null) {
            for (AppIoParamEntity output : entity.getApplicationOutputs()) {
                builder.addApplicationOutputs(appOutputToModel(output));
            }
        }
    }

    @AfterMapping
    default void afterAppInterfaceToEntity(
            ApplicationInterfaceDescription model, @MappingTarget ApplicationInterfaceEntity entity) {
        if (!model.getApplicationModulesList().isEmpty()) {
            entity.setApplicationModules(new ArrayList<>(model.getApplicationModulesList()));
        }
        if (!model.getApplicationInputsList().isEmpty()) {
            List<AppIoParamEntity> inputs = new ArrayList<>();
            for (InputDataObjectType input : model.getApplicationInputsList()) {
                inputs.add(appInputToEntity(input));
            }
            entity.setApplicationInputs(inputs);
        }
        if (!model.getApplicationOutputsList().isEmpty()) {
            List<AppIoParamEntity> outputs = new ArrayList<>();
            for (OutputDataObjectType output : model.getApplicationOutputsList()) {
                outputs.add(appOutputToEntity(output));
            }
            entity.setApplicationOutputs(outputs);
        }
    }

    // --- ApplicationModule ---
    ApplicationModule appModuleToModel(ApplicationModuleEntity entity);

    ApplicationModuleEntity appModuleToEntity(ApplicationModule model);

    // --- ApplicationDeploymentDescription ---
    default ApplicationDeploymentDescription appDeploymentToModel(ApplicationDeploymentEntity entity) {
        if (entity == null)
            return null;
        ApplicationDeploymentDescription.Builder builder = ApplicationDeploymentDescription.newBuilder();
        if (entity.getAppDeploymentId() != null)
            builder.setAppDeploymentId(entity.getAppDeploymentId());
        if (entity.getAppDeploymentDescription() != null)
            builder.setAppDeploymentDescription(entity.getAppDeploymentDescription());
        if (entity.getExecutablePath() != null)
            builder.setExecutablePath(entity.getExecutablePath());
        if (entity.getComputeHostId() != null)
            builder.setComputeHostId(entity.getComputeHostId());
        if (entity.getAppModuleId() != null)
            builder.setAppModuleId(entity.getAppModuleId());
        if (entity.getParallelism() != null)
            builder.setParallelism(entity.getParallelism());
        if (entity.getDefaultQueueName() != null)
            builder.setDefaultQueueName(entity.getDefaultQueueName());
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
        if (model == null)
            return null;
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
        if (entity == null)
            return null;
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
        if (model == null)
            return null;
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
        if (entity == null)
            return null;
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
        if (model == null)
            return null;
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
}
