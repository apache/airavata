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
import org.apache.airavata.models.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.models.appcatalog.appdeployment.ApplicationModule;
import org.apache.airavata.models.appcatalog.appdeployment.CommandObject;
import org.apache.airavata.models.appcatalog.appdeployment.SetEnvPaths;
import org.apache.airavata.models.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.models.application.io.DataType;
import org.apache.airavata.models.application.io.InputDataObjectType;
import org.apache.airavata.models.application.io.OutputDataObjectType;
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
        if (!model.applicationModules().isEmpty()) {
            entity.setApplicationModules(new ArrayList<>(model.applicationModules()));
        }
        if (!model.applicationInputs().isEmpty()) {
            List<AppIoParamEntity> inputs = new ArrayList<>();
            for (InputDataObjectType input : model.applicationInputs()) {
                inputs.add(appInputToEntity(input));
            }
            entity.setApplicationInputs(inputs);
        }
        if (!model.applicationOutputs().isEmpty()) {
            List<AppIoParamEntity> outputs = new ArrayList<>();
            for (OutputDataObjectType output : model.applicationOutputs()) {
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
        entity.setAppDeploymentId(model.appDeploymentId());
        entity.setAppDeploymentDescription(model.appDeploymentDescription());
        entity.setExecutablePath(model.executablePath());
        entity.setComputeHostId(model.computeHostId());
        entity.setAppModuleId(model.appModuleId());
        entity.setParallelism(model.parallelism());
        entity.setDefaultQueueName(model.defaultQueueName());
        entity.setDefaultNodeCount(model.defaultNodeCount());
        entity.setDefaultCPUCount(model.defaultCpuCount());
        entity.setDefaultWallTime(model.defaultWalltime());
        entity.setEditableByUser(model.editableByUser());
        if (!model.moduleLoadCmds().isEmpty()) {
            entity.setModuleLoadCmds(model.moduleLoadCmds().stream()
                    .map(cmd -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("command", cmd.command());
                        m.put("commandOrder", cmd.commandOrder());
                        return m;
                    })
                    .toList());
        }
        if (!model.preJobCommands().isEmpty()) {
            entity.setPreJobCommands(model.preJobCommands().stream()
                    .map(cmd -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("command", cmd.command());
                        m.put("commandOrder", cmd.commandOrder());
                        return m;
                    })
                    .toList());
        }
        if (!model.postJobCommands().isEmpty()) {
            entity.setPostJobCommands(model.postJobCommands().stream()
                    .map(cmd -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("command", cmd.command());
                        m.put("commandOrder", cmd.commandOrder());
                        return m;
                    })
                    .toList());
        }
        if (!model.libPrependPaths().isEmpty()) {
            entity.setLibPrependPaths(model.libPrependPaths().stream()
                    .map(p -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("name", p.name());
                        m.put("value", p.value());
                        m.put("envPathOrder", p.envPathOrder());
                        return m;
                    })
                    .toList());
        }
        if (!model.libAppendPaths().isEmpty()) {
            entity.setLibAppendPaths(model.libAppendPaths().stream()
                    .map(p -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("name", p.name());
                        m.put("value", p.value());
                        m.put("envPathOrder", p.envPathOrder());
                        return m;
                    })
                    .toList());
        }
        if (!model.setEnvironment().isEmpty()) {
            entity.setSetEnvironment(model.setEnvironment().stream()
                    .map(p -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("name", p.name());
                        m.put("value", p.value());
                        m.put("envPathOrder", p.envPathOrder());
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
                .setType(entity.getType() != null ? entity.getType() : DataType.DATA_TYPE_UNKNOWN)
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
        entity.setName(model.name());
        entity.setValue(model.value());
        entity.setType(model.type());
        entity.setApplicationArgument(model.applicationArgument());
        entity.setStandardInput(model.standardInput());
        entity.setUserFriendlyDescription(model.userFriendlyDescription());
        entity.setMetaData(model.metaData());
        entity.setInputOrder(model.inputOrder());
        entity.setIsRequired(model.isRequired());
        entity.setRequiredToAddedToCommandLine(model.requiredToAddedToCommandLine());
        entity.setDataStaged(model.dataStaged());
        entity.setReadOnly(model.isReadOnly());
        entity.setOverrideFilename(model.overrideFilename());
        return entity;
    }

    // --- ApplicationOutput (AppIoParamEntity, DIRECTION=OUTPUT) ---
    default OutputDataObjectType appOutputToModel(AppIoParamEntity entity) {
        if (entity == null)
            return null;
        return OutputDataObjectType.newBuilder()
                .setName(entity.getName() != null ? entity.getName() : "")
                .setValue(entity.getValue() != null ? entity.getValue() : "")
                .setType(entity.getType() != null ? entity.getType() : DataType.DATA_TYPE_UNKNOWN)
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
        entity.setName(model.name());
        entity.setValue(model.value());
        entity.setType(model.type());
        entity.setApplicationArgument(model.applicationArgument());
        entity.setIsRequired(model.isRequired());
        entity.setRequiredToAddedToCommandLine(model.requiredToAddedToCommandLine());
        entity.setDataMovement(model.dataMovement());
        entity.setLocation(model.location());
        entity.setSearchQuery(model.searchQuery());
        entity.setOutputStreaming(model.outputStreaming());
        entity.setMetaData(model.metaData());
        return entity;
    }
}
