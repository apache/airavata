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
package org.apache.airavata.compute.mapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.compute.model.*;
import org.apache.airavata.mapper.CommonMapperConversions;
import org.apache.airavata.model.appcatalog.computeresource.proto.BatchQueue;
import org.apache.airavata.model.appcatalog.computeresource.proto.CloudJobSubmission;
import org.apache.airavata.model.appcatalog.computeresource.proto.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.proto.FileSystems;
import org.apache.airavata.model.appcatalog.computeresource.proto.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.proto.LOCALSubmission;
import org.apache.airavata.model.appcatalog.computeresource.proto.MonitorMode;
import org.apache.airavata.model.appcatalog.computeresource.proto.ProviderName;
import org.apache.airavata.model.appcatalog.computeresource.proto.ResourceJobManager;
import org.apache.airavata.model.appcatalog.computeresource.proto.SSHJobSubmission;
import org.apache.airavata.model.appcatalog.computeresource.proto.UnicoreJobSubmission;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.GatewayResourceProfile;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.BatchQueueResourcePolicy;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.ComputeResourcePolicy;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.ComputeResourceReservation;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.EnvironmentSpecificPreferences;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupAccountSSHProvisionerConfig;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupResourceProfile;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.ResourceType;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.SlurmComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.proto.UserComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.proto.UserResourceProfile;
import org.apache.airavata.model.appcatalog.userresourceprofile.proto.UserStoragePreference;
import org.apache.airavata.model.status.proto.QueueStatusModel;
import org.apache.airavata.model.workspace.proto.GatewayUsageReportingCommand;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ComputeMapper extends CommonMapperConversions {

    ComputeMapper INSTANCE = Mappers.getMapper(ComputeMapper.class);

    // --- ComputeResourceDescription ---
    default ComputeResourceDescription computeResourceToModel(ComputeResourceEntity entity) {
        if (entity == null) return null;
        ComputeResourceDescription.Builder b = ComputeResourceDescription.newBuilder();
        if (entity.getComputeResourceId() != null) b.setComputeResourceId(entity.getComputeResourceId());
        if (entity.getHostName() != null) b.setHostName(entity.getHostName());
        if (entity.getResourceDescription() != null) b.setResourceDescription(entity.getResourceDescription());
        b.setEnabled(entity.getEnabled() != 0);
        b.setMaxMemoryPerNode(entity.getMaxMemoryPerNode());
        b.setGatewayUsageReporting(entity.isGatewayUsageReporting());
        if (entity.getGatewayUsageModuleLoadCommand() != null)
            b.setGatewayUsageModuleLoadCommand(entity.getGatewayUsageModuleLoadCommand());
        if (entity.getGatewayUsageExecutable() != null) b.setGatewayUsageExecutable(entity.getGatewayUsageExecutable());
        if (entity.getCpusPerNode() != null) b.setCpusPerNode(entity.getCpusPerNode());
        if (entity.getDefaultNodeCount() != null) b.setDefaultNodeCount(entity.getDefaultNodeCount());
        if (entity.getDefaultCPUCount() != null) b.setDefaultCpuCount(entity.getDefaultCPUCount());
        if (entity.getDefaultWalltime() != null) b.setDefaultWalltime(entity.getDefaultWalltime());
        if (entity.getHostAliases() != null) b.addAllHostAliases(entity.getHostAliases());
        if (entity.getIpAddresses() != null) b.addAllIpAddresses(entity.getIpAddresses());
        if (entity.getBatchQueues() != null) {
            entity.getBatchQueues().forEach(q -> b.addBatchQueues(batchQueueToModel(q)));
        }
        if (entity.getJobSubmissionInterfaces() != null) {
            entity.getJobSubmissionInterfaces()
                    .forEach(j -> b.addJobSubmissionInterfaces(jobSubmissionInterfaceToModel(j)));
        }
        if (entity.getFileSystems() != null) {
            for (Map<String, Object> entry : entity.getFileSystems()) {
                String fsName = (String) entry.get("fileSystem");
                String path = (String) entry.getOrDefault("path", "");
                if (fsName != null) {
                    try {
                        FileSystems fs = FileSystems.valueOf(fsName);
                        b.putFileSystems(fs.getNumber(), path);
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        }
        return b.build();
    }

    default ComputeResourceEntity computeResourceToEntity(ComputeResourceDescription model) {
        if (model == null) return null;
        ComputeResourceEntity entity = new ComputeResourceEntity();
        entity.setComputeResourceId(model.getComputeResourceId());
        entity.setHostName(model.getHostName());
        entity.setResourceDescription(model.getResourceDescription());
        entity.setEnabled(model.getEnabled() ? (short) 1 : (short) 0);
        entity.setMaxMemoryPerNode(model.getMaxMemoryPerNode());
        entity.setGatewayUsageReporting(model.getGatewayUsageReporting());
        entity.setGatewayUsageModuleLoadCommand(model.getGatewayUsageModuleLoadCommand());
        entity.setGatewayUsageExecutable(model.getGatewayUsageExecutable());
        entity.setCpusPerNode(model.getCpusPerNode());
        entity.setDefaultNodeCount(model.getDefaultNodeCount());
        entity.setDefaultCPUCount(model.getDefaultCpuCount());
        entity.setDefaultWalltime(model.getDefaultWalltime());
        if (!model.getHostAliasesList().isEmpty()) entity.setHostAliases(model.getHostAliasesList());
        if (!model.getIpAddressesList().isEmpty()) entity.setIpAddresses(model.getIpAddressesList());
        if (!model.getBatchQueuesList().isEmpty()) {
            entity.setBatchQueues(model.getBatchQueuesList().stream()
                    .map(this::batchQueueToEntity)
                    .toList());
        }
        if (!model.getJobSubmissionInterfacesList().isEmpty()) {
            entity.setJobSubmissionInterfaces(model.getJobSubmissionInterfacesList().stream()
                    .map(this::jobSubmissionInterfaceToEntity)
                    .toList());
        }
        if (!model.getFileSystemsMap().isEmpty()) {
            List<Map<String, Object>> fsList = new ArrayList<>();
            model.getFileSystemsMap().forEach((fsNumber, path) -> {
                FileSystems fs = FileSystems.forNumber(fsNumber);
                if (fs != null) {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("fileSystem", fs.name());
                    entry.put("path", path);
                    fsList.add(entry);
                }
            });
            entity.setFileSystems(fsList);
        }
        return entity;
    }

    // --- BatchQueue ---
    @Mapping(source = "maxRuntime", target = "maxRunTime")
    BatchQueue batchQueueToModel(BatchQueueEntity entity);

    @Mapping(source = "maxRunTime", target = "maxRuntime")
    BatchQueueEntity batchQueueToEntity(BatchQueue model);

    // --- ResourceJobManager ---
    @Mapping(target = "jobManagerCommands", ignore = true)
    @Mapping(target = "parallelismPrefix", ignore = true)
    ResourceJobManager resourceJobManagerToModel(ResourceJobManagerEntity entity);

    @Mapping(target = "jobManagerCommands", ignore = true)
    @Mapping(target = "parallelismCommands", ignore = true)
    ResourceJobManagerEntity resourceJobManagerToEntity(ResourceJobManager model);

    // --- JobSubmissionInterface ---
    JobSubmissionInterface jobSubmissionInterfaceToModel(JobSubmissionInterfaceEntity entity);

    JobSubmissionInterfaceEntity jobSubmissionInterfaceToEntity(JobSubmissionInterface model);

    // --- GatewayResourceProfile / GatewayProfileEntity ---
    // MapStruct auto-maps the scalar tokens/IDs; the @AfterMapping hooks below add the repeated
    // compute_resource_preferences child list, which MapStruct does not match (proto's
    // getComputeResourcePreferencesList() vs entity property `computeResourcePreferences`).
    GatewayResourceProfile gatewayProfileToModel(GatewayProfileEntity entity);

    @AfterMapping
    default void afterGatewayProfileToModel(
            GatewayProfileEntity entity, @MappingTarget GatewayResourceProfile.Builder builder) {
        if (entity.getComputeResourcePreferences() != null) {
            for (ComputeResourcePreferenceEntity pref : entity.getComputeResourcePreferences()) {
                builder.addComputeResourcePreferences(computeResourcePrefToModel(pref));
            }
        }
    }

    GatewayProfileEntity gatewayProfileToEntity(GatewayResourceProfile model);

    @AfterMapping
    default void afterGatewayProfileToEntity(
            GatewayResourceProfile model, @MappingTarget GatewayProfileEntity entity) {
        if (!model.getComputeResourcePreferencesList().isEmpty()) {
            List<ComputeResourcePreferenceEntity> prefs = new ArrayList<>();
            for (ComputeResourcePreference pref : model.getComputeResourcePreferencesList()) {
                prefs.add(computeResourcePrefToEntity(pref));
            }
            entity.setComputeResourcePreferences(prefs);
        }
    }

    // --- ComputeResourcePreference ---
    default ComputeResourcePreference computeResourcePrefToModel(ComputeResourcePreferenceEntity entity) {
        if (entity == null) return null;
        ComputeResourcePreference.Builder b = ComputeResourcePreference.newBuilder();
        if (entity.getComputeResourceId() != null) b.setComputeResourceId(entity.getComputeResourceId());
        if (entity.getLoginUserName() != null) b.setLoginUserName(entity.getLoginUserName());
        b.setOverrideByAiravata(entity.isOverridebyAiravata());
        if (entity.getPreferredBatchQueue() != null) b.setPreferredBatchQueue(entity.getPreferredBatchQueue());
        if (entity.getPreferredDataMovementProtocol() != null)
            b.setPreferredDataMovementProtocol(entity.getPreferredDataMovementProtocol());
        if (entity.getPreferredJobSubmissionProtocol() != null)
            b.setPreferredJobSubmissionProtocol(entity.getPreferredJobSubmissionProtocol());
        if (entity.getQualityOfService() != null) b.setQualityOfService(entity.getQualityOfService());
        if (entity.getReservation() != null) b.setReservation(entity.getReservation());
        if (entity.getReservationStartTime() != null)
            b.setReservationStartTime(entity.getReservationStartTime().getTime());
        if (entity.getReservationEndTime() != null)
            b.setReservationEndTime(entity.getReservationEndTime().getTime());
        if (entity.getResourceSpecificCredentialStoreToken() != null)
            b.setResourceSpecificCredentialStoreToken(entity.getResourceSpecificCredentialStoreToken());
        if (entity.getScratchLocation() != null) b.setScratchLocation(entity.getScratchLocation());
        if (entity.getUsageReportingGatewayId() != null)
            b.setUsageReportingGatewayId(entity.getUsageReportingGatewayId());
        if (entity.getSshAccountProvisioner() != null) b.setSshAccountProvisioner(entity.getSshAccountProvisioner());
        if (entity.getSshAccountProvisionerAdditionalInfo() != null)
            b.setSshAccountProvisionerAdditionalInfo(entity.getSshAccountProvisionerAdditionalInfo());
        if (entity.getSshAccountProvisionerConfigurations() != null) {
            for (Map<String, Object> entry : entity.getSshAccountProvisionerConfigurations()) {
                String name = (String) entry.get("configName");
                String value = (String) entry.getOrDefault("configValue", "");
                if (name != null) b.putSshAccountProvisionerConfig(name, value);
            }
        }
        return b.build();
    }

    default ComputeResourcePreferenceEntity computeResourcePrefToEntity(ComputeResourcePreference model) {
        if (model == null) return null;
        ComputeResourcePreferenceEntity entity = new ComputeResourcePreferenceEntity();
        entity.setComputeResourceId(model.getComputeResourceId());
        entity.setLoginUserName(model.getLoginUserName());
        entity.setOverridebyAiravata(model.getOverrideByAiravata());
        entity.setPreferredBatchQueue(model.getPreferredBatchQueue());
        entity.setPreferredDataMovementProtocol(model.getPreferredDataMovementProtocol());
        entity.setPreferredJobSubmissionProtocol(model.getPreferredJobSubmissionProtocol());
        entity.setQualityOfService(model.getQualityOfService());
        entity.setReservation(model.getReservation());
        if (model.getReservationStartTime() != 0)
            entity.setReservationStartTime(new java.sql.Timestamp(model.getReservationStartTime()));
        if (model.getReservationEndTime() != 0)
            entity.setReservationEndTime(new java.sql.Timestamp(model.getReservationEndTime()));
        entity.setResourceSpecificCredentialStoreToken(model.getResourceSpecificCredentialStoreToken());
        entity.setScratchLocation(model.getScratchLocation());
        entity.setUsageReportingGatewayId(model.getUsageReportingGatewayId());
        entity.setSshAccountProvisioner(model.getSshAccountProvisioner());
        entity.setSshAccountProvisionerAdditionalInfo(model.getSshAccountProvisionerAdditionalInfo());
        if (!model.getSshAccountProvisionerConfigMap().isEmpty()) {
            List<Map<String, Object>> configs = new ArrayList<>();
            model.getSshAccountProvisionerConfigMap().forEach((name, value) -> {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("configName", name);
                entry.put("configValue", value);
                configs.add(entry);
            });
            entity.setSshAccountProvisionerConfigurations(configs);
        }
        return entity;
    }

    // --- GroupResourceProfile ---
    // Hand-written so the repeated child collections (compute preferences and the two policy
    // lists) are mapped: MapStruct does not match protobuf's repeated accessors
    // (getComputePreferencesList()) to the entity's `computePreferences` property and would
    // silently drop them in both directions.
    default GroupResourceProfile groupResourceProfileToModel(GroupResourceProfileEntity entity) {
        if (entity == null) return null;
        GroupResourceProfile.Builder b = GroupResourceProfile.newBuilder();
        if (entity.getGatewayId() != null) b.setGatewayId(entity.getGatewayId());
        if (entity.getGroupResourceProfileId() != null) b.setGroupResourceProfileId(entity.getGroupResourceProfileId());
        if (entity.getGroupResourceProfileName() != null)
            b.setGroupResourceProfileName(entity.getGroupResourceProfileName());
        if (entity.getCreationTime() != null) b.setCreationTime(entity.getCreationTime());
        if (entity.getUpdatedTime() != null) b.setUpdatedTime(entity.getUpdatedTime());
        if (entity.getDefaultCredentialStoreToken() != null)
            b.setDefaultCredentialStoreToken(entity.getDefaultCredentialStoreToken());
        if (entity.getComputePreferences() != null) {
            for (GroupComputeResourcePrefEntity pref : entity.getComputePreferences()) {
                b.addComputePreferences(groupComputePrefToModel(pref));
            }
        }
        if (entity.getComputeResourcePolicies() != null) {
            for (ComputeResourcePolicyEntity policy : entity.getComputeResourcePolicies()) {
                b.addComputeResourcePolicies(computeResourcePolicyToModel(policy));
            }
        }
        if (entity.getBatchQueueResourcePolicies() != null) {
            for (BatchQueueResourcePolicyEntity policy : entity.getBatchQueueResourcePolicies()) {
                b.addBatchQueueResourcePolicies(batchQueuePolicyToModel(policy));
            }
        }
        return b.build();
    }

    default GroupResourceProfileEntity groupResourceProfileToEntity(GroupResourceProfile model) {
        if (model == null) return null;
        GroupResourceProfileEntity entity = new GroupResourceProfileEntity();
        entity.setGatewayId(model.getGatewayId());
        entity.setGroupResourceProfileId(model.getGroupResourceProfileId());
        entity.setGroupResourceProfileName(model.getGroupResourceProfileName());
        entity.setCreationTime(model.getCreationTime());
        entity.setUpdatedTime(model.getUpdatedTime());
        entity.setDefaultCredentialStoreToken(model.getDefaultCredentialStoreToken());
        if (!model.getComputePreferencesList().isEmpty()) {
            List<GroupComputeResourcePrefEntity> prefs = new ArrayList<>();
            for (GroupComputeResourcePreference pref : model.getComputePreferencesList()) {
                prefs.add(groupComputePrefToEntity(pref));
            }
            entity.setComputePreferences(prefs);
        }
        if (!model.getComputeResourcePoliciesList().isEmpty()) {
            List<ComputeResourcePolicyEntity> policies = new ArrayList<>();
            for (ComputeResourcePolicy policy : model.getComputeResourcePoliciesList()) {
                policies.add(computeResourcePolicyToEntity(policy));
            }
            entity.setComputeResourcePolicies(policies);
        }
        if (!model.getBatchQueueResourcePoliciesList().isEmpty()) {
            List<BatchQueueResourcePolicyEntity> policies = new ArrayList<>();
            for (BatchQueueResourcePolicy policy : model.getBatchQueueResourcePoliciesList()) {
                policies.add(batchQueuePolicyToEntity(policy));
            }
            entity.setBatchQueueResourcePolicies(policies);
        }
        return entity;
    }

    // --- GroupComputeResourcePreference ---
    // Hand-written so the Slurm-variant nested message (specific_preferences oneof) and its repeated
    // child lists (reservations, group_ssh_account_provisioner_configs) are mapped: MapStruct maps
    // only the base scalars and silently drops the nested EnvironmentSpecificPreferences plus the
    // repeated children that live on the SlurmGroupComputeResourcePrefEntity subclass.
    default GroupComputeResourcePreference groupComputePrefToModel(GroupComputeResourcePrefEntity entity) {
        if (entity == null) return null;
        GroupComputeResourcePreference.Builder b = GroupComputeResourcePreference.newBuilder();
        if (entity.getComputeResourceId() != null) b.setComputeResourceId(entity.getComputeResourceId());
        if (entity.getGroupResourceProfileId() != null)
            b.setGroupResourceProfileId(entity.getGroupResourceProfileId());
        b.setOverrideByAiravata(entity.getOverridebyAiravata() != 0);
        if (entity.getLoginUserName() != null) b.setLoginUserName(entity.getLoginUserName());
        if (entity.getScratchLocation() != null) b.setScratchLocation(entity.getScratchLocation());
        if (entity.getPreferredJobSubmissionProtocol() != null)
            b.setPreferredJobSubmissionProtocol(entity.getPreferredJobSubmissionProtocol());
        if (entity.getPreferredDataMovementProtocol() != null)
            b.setPreferredDataMovementProtocol(entity.getPreferredDataMovementProtocol());
        if (entity.getResourceSpecificCredentialStoreToken() != null)
            b.setResourceSpecificCredentialStoreToken(entity.getResourceSpecificCredentialStoreToken());
        if (entity instanceof SlurmGroupComputeResourcePrefEntity slurm) {
            b.setResourceType(ResourceType.SLURM);
            SlurmComputeResourcePreference.Builder s = SlurmComputeResourcePreference.newBuilder();
            if (slurm.getAllocationProjectNumber() != null)
                s.setAllocationProjectNumber(slurm.getAllocationProjectNumber());
            if (slurm.getPreferredBatchQueue() != null) s.setPreferredBatchQueue(slurm.getPreferredBatchQueue());
            if (slurm.getQualityOfService() != null) s.setQualityOfService(slurm.getQualityOfService());
            if (slurm.getUsageReportingGatewayId() != null)
                s.setUsageReportingGatewayId(slurm.getUsageReportingGatewayId());
            if (slurm.getSshAccountProvisioner() != null)
                s.setSshAccountProvisioner(slurm.getSshAccountProvisioner());
            if (slurm.getSshAccountProvisionerAdditionalInfo() != null)
                s.setSshAccountProvisionerAdditionalInfo(slurm.getSshAccountProvisionerAdditionalInfo());
            if (slurm.getGroupSSHAccountProvisionerConfigs() != null) {
                for (Map<String, Object> entry : slurm.getGroupSSHAccountProvisionerConfigs()) {
                    GroupAccountSSHProvisionerConfig.Builder c = GroupAccountSSHProvisionerConfig.newBuilder();
                    Object resourceId = entry.get("resourceId");
                    Object groupResourceProfileId = entry.get("groupResourceProfileId");
                    Object configName = entry.get("configName");
                    Object configValue = entry.get("configValue");
                    if (resourceId != null) c.setResourceId((String) resourceId);
                    if (groupResourceProfileId != null) c.setGroupResourceProfileId((String) groupResourceProfileId);
                    if (configName != null) c.setConfigName((String) configName);
                    if (configValue != null) c.setConfigValue((String) configValue);
                    s.addGroupSshAccountProvisionerConfigs(c.build());
                }
            }
            if (slurm.getReservations() != null) {
                for (ComputeResourceReservationEntity r : slurm.getReservations()) {
                    s.addReservations(reservationToModel(r));
                }
            }
            b.setSpecificPreferences(
                    EnvironmentSpecificPreferences.newBuilder().setSlurm(s).build());
        }
        return b.build();
    }

    // Uses SlurmGroupComputeResourcePrefEntity as the default concrete type
    default SlurmGroupComputeResourcePrefEntity groupComputePrefToEntity(GroupComputeResourcePreference model) {
        if (model == null) return null;
        SlurmGroupComputeResourcePrefEntity entity = new SlurmGroupComputeResourcePrefEntity();
        entity.setComputeResourceId(model.getComputeResourceId());
        entity.setGroupResourceProfileId(model.getGroupResourceProfileId());
        entity.setOverridebyAiravata(model.getOverrideByAiravata() ? (short) 1 : (short) 0);
        entity.setLoginUserName(model.getLoginUserName());
        entity.setScratchLocation(model.getScratchLocation());
        entity.setPreferredDataMovementProtocol(model.getPreferredDataMovementProtocol());
        entity.setPreferredJobSubmissionProtocol(model.getPreferredJobSubmissionProtocol());
        entity.setResourceSpecificCredentialStoreToken(model.getResourceSpecificCredentialStoreToken());
        if (model.hasSpecificPreferences()
                && model.getSpecificPreferences().getPreferencesCase()
                        == EnvironmentSpecificPreferences.PreferencesCase.SLURM) {
            SlurmComputeResourcePreference slurm = model.getSpecificPreferences().getSlurm();
            entity.setAllocationProjectNumber(slurm.getAllocationProjectNumber());
            entity.setPreferredBatchQueue(slurm.getPreferredBatchQueue());
            entity.setQualityOfService(slurm.getQualityOfService());
            entity.setUsageReportingGatewayId(slurm.getUsageReportingGatewayId());
            entity.setSshAccountProvisioner(slurm.getSshAccountProvisioner());
            entity.setSshAccountProvisionerAdditionalInfo(slurm.getSshAccountProvisionerAdditionalInfo());
            if (!slurm.getGroupSshAccountProvisionerConfigsList().isEmpty()) {
                List<Map<String, Object>> configs = new ArrayList<>();
                for (GroupAccountSSHProvisionerConfig c : slurm.getGroupSshAccountProvisionerConfigsList()) {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("resourceId", c.getResourceId());
                    entry.put("groupResourceProfileId", c.getGroupResourceProfileId());
                    entry.put("configName", c.getConfigName());
                    entry.put("configValue", c.getConfigValue());
                    configs.add(entry);
                }
                entity.setGroupSSHAccountProvisionerConfigs(configs);
            }
            if (!slurm.getReservationsList().isEmpty()) {
                List<ComputeResourceReservationEntity> reservations = new ArrayList<>();
                for (ComputeResourceReservation r : slurm.getReservationsList()) {
                    reservations.add(reservationToEntity(r));
                }
                entity.setReservations(reservations);
            }
        }
        return entity;
    }

    // --- ComputeResourceReservation ---
    default ComputeResourceReservation reservationToModel(ComputeResourceReservationEntity entity) {
        if (entity == null) return null;
        ComputeResourceReservation.Builder b = ComputeResourceReservation.newBuilder();
        if (entity.getReservationId() != null) b.setReservationId(entity.getReservationId());
        if (entity.getReservationName() != null) b.setReservationName(entity.getReservationName());
        if (entity.getQueueNames() != null) b.addAllQueueNames(entity.getQueueNames());
        if (entity.getStartTime() != null) b.setStartTime(entity.getStartTime().getTime());
        if (entity.getEndTime() != null) b.setEndTime(entity.getEndTime().getTime());
        return b.build();
    }

    default ComputeResourceReservationEntity reservationToEntity(ComputeResourceReservation model) {
        if (model == null) return null;
        ComputeResourceReservationEntity entity = new ComputeResourceReservationEntity();
        entity.setReservationId(model.getReservationId());
        entity.setReservationName(model.getReservationName());
        if (!model.getQueueNamesList().isEmpty()) entity.setQueueNames(new ArrayList<>(model.getQueueNamesList()));
        if (model.getStartTime() != 0) entity.setStartTime(new java.sql.Timestamp(model.getStartTime()));
        if (model.getEndTime() != 0) entity.setEndTime(new java.sql.Timestamp(model.getEndTime()));
        return entity;
    }

    // --- ComputeResourcePolicy ---
    // MapStruct maps the scalar IDs; the @AfterMapping hooks add the repeated allowed_batch_queues
    // list, which it does not match (proto's getAllowedBatchQueuesList() vs entity collection).
    ComputeResourcePolicy computeResourcePolicyToModel(ComputeResourcePolicyEntity entity);

    @AfterMapping
    default void afterComputeResourcePolicyToModel(
            ComputeResourcePolicyEntity entity, @MappingTarget ComputeResourcePolicy.Builder builder) {
        if (entity.getAllowedBatchQueues() != null) {
            builder.addAllAllowedBatchQueues(entity.getAllowedBatchQueues());
        }
    }

    ComputeResourcePolicyEntity computeResourcePolicyToEntity(ComputeResourcePolicy model);

    @AfterMapping
    default void afterComputeResourcePolicyToEntity(
            ComputeResourcePolicy model, @MappingTarget ComputeResourcePolicyEntity entity) {
        if (!model.getAllowedBatchQueuesList().isEmpty()) {
            entity.setAllowedBatchQueues(new ArrayList<>(model.getAllowedBatchQueuesList()));
        }
    }

    // --- BatchQueueResourcePolicy ---
    BatchQueueResourcePolicy batchQueuePolicyToModel(BatchQueueResourcePolicyEntity entity);

    BatchQueueResourcePolicyEntity batchQueuePolicyToEntity(BatchQueueResourcePolicy model);

    // --- UserResourceProfile ---
    // MapStruct auto-maps the scalar tokens/IDs; the @AfterMapping hooks below add the repeated
    // user_compute_resource_preferences child list, which MapStruct does not match (proto's
    // getUserComputeResourcePreferencesList() vs entity property `userComputeResourcePreferences`).
    UserResourceProfile userResourceProfileToModel(UserResourceProfileEntity entity);

    @AfterMapping
    default void afterUserResourceProfileToModel(
            UserResourceProfileEntity entity, @MappingTarget UserResourceProfile.Builder builder) {
        if (entity.getUserComputeResourcePreferences() != null) {
            for (UserComputeResourcePreferenceEntity pref : entity.getUserComputeResourcePreferences()) {
                builder.addUserComputeResourcePreferences(userComputeResourcePrefToModel(pref));
            }
        }
    }

    UserResourceProfileEntity userResourceProfileToEntity(UserResourceProfile model);

    @AfterMapping
    default void afterUserResourceProfileToEntity(
            UserResourceProfile model, @MappingTarget UserResourceProfileEntity entity) {
        if (!model.getUserComputeResourcePreferencesList().isEmpty()) {
            List<UserComputeResourcePreferenceEntity> prefs = new ArrayList<>();
            for (UserComputeResourcePreference pref : model.getUserComputeResourcePreferencesList()) {
                prefs.add(userComputeResourcePrefToEntity(pref));
            }
            entity.setUserComputeResourcePreferences(prefs);
        }
    }

    // --- UserComputeResourcePreference ---
    UserComputeResourcePreference userComputeResourcePrefToModel(UserComputeResourcePreferenceEntity entity);

    UserComputeResourcePreferenceEntity userComputeResourcePrefToEntity(UserComputeResourcePreference model);

    // --- Consolidated ComputeJobSubmissionEntity mappings ---

    // SSHJobSubmission
    default SSHJobSubmission sshJobSubmissionToModel(ComputeJobSubmissionEntity entity) {
        if (entity == null) return null;
        SSHJobSubmission.Builder b = SSHJobSubmission.newBuilder();
        b.setJobSubmissionInterfaceId(entity.getSubmissionId());
        if (entity.getSecurityProtocol() != null) b.setSecurityProtocol(entity.getSecurityProtocol());
        if (entity.getResourceJobManager() != null)
            b.setResourceJobManager(resourceJobManagerToModel(entity.getResourceJobManager()));
        Map<String, Object> cfg = entity.getConfig();
        if (cfg != null) {
            b.setSshPort(((Number) cfg.getOrDefault("sshPort", 22)).intValue());
            if (cfg.get("alternativeSshHostname") != null)
                b.setAlternativeSshHostName((String) cfg.get("alternativeSshHostname"));
            if (cfg.get("monitorMode") != null) b.setMonitorMode(MonitorMode.valueOf((String) cfg.get("monitorMode")));
        }
        return b.build();
    }

    default ComputeJobSubmissionEntity sshJobSubmissionToEntity(SSHJobSubmission model) {
        if (model == null) return null;
        ComputeJobSubmissionEntity entity = new ComputeJobSubmissionEntity();
        entity.setSubmissionId(model.getJobSubmissionInterfaceId());
        entity.setSubmissionType("SSH");
        entity.setSecurityProtocol(model.getSecurityProtocol());
        if (model.hasResourceJobManager())
            entity.setResourceJobManager(resourceJobManagerToEntity(model.getResourceJobManager()));
        Map<String, Object> cfg = new LinkedHashMap<>();
        cfg.put("sshPort", model.getSshPort());
        if (!model.getAlternativeSshHostName().isEmpty())
            cfg.put("alternativeSshHostname", model.getAlternativeSshHostName());
        if (model.getMonitorMode() != null)
            cfg.put("monitorMode", model.getMonitorMode().name());
        entity.setConfig(cfg);
        return entity;
    }

    // LOCALSubmission
    default LOCALSubmission localSubmissionToModel(ComputeJobSubmissionEntity entity) {
        if (entity == null) return null;
        LOCALSubmission.Builder b = LOCALSubmission.newBuilder();
        b.setJobSubmissionInterfaceId(entity.getSubmissionId());
        if (entity.getSecurityProtocol() != null) b.setSecurityProtocol(entity.getSecurityProtocol());
        if (entity.getResourceJobManager() != null)
            b.setResourceJobManager(resourceJobManagerToModel(entity.getResourceJobManager()));
        return b.build();
    }

    default ComputeJobSubmissionEntity localSubmissionToEntity(LOCALSubmission model) {
        if (model == null) return null;
        ComputeJobSubmissionEntity entity = new ComputeJobSubmissionEntity();
        entity.setSubmissionId(model.getJobSubmissionInterfaceId());
        entity.setSubmissionType("LOCAL");
        entity.setSecurityProtocol(model.getSecurityProtocol());
        if (model.hasResourceJobManager())
            entity.setResourceJobManager(resourceJobManagerToEntity(model.getResourceJobManager()));
        return entity;
    }

    // CloudJobSubmission
    default CloudJobSubmission cloudJobSubmissionToModel(ComputeJobSubmissionEntity entity) {
        if (entity == null) return null;
        CloudJobSubmission.Builder b = CloudJobSubmission.newBuilder();
        b.setJobSubmissionInterfaceId(entity.getSubmissionId());
        if (entity.getSecurityProtocol() != null) b.setSecurityProtocol(entity.getSecurityProtocol());
        Map<String, Object> cfg = entity.getConfig();
        if (cfg != null) {
            if (cfg.get("nodeId") != null) b.setNodeId((String) cfg.get("nodeId"));
            if (cfg.get("executableType") != null) b.setExecutableType((String) cfg.get("executableType"));
            if (cfg.get("providerName") != null)
                b.setProviderName(ProviderName.valueOf((String) cfg.get("providerName")));
            if (cfg.get("userAccountName") != null) b.setUserAccountName((String) cfg.get("userAccountName"));
        }
        return b.build();
    }

    default ComputeJobSubmissionEntity cloudJobSubmissionToEntity(CloudJobSubmission model) {
        if (model == null) return null;
        ComputeJobSubmissionEntity entity = new ComputeJobSubmissionEntity();
        entity.setSubmissionId(model.getJobSubmissionInterfaceId());
        entity.setSubmissionType("CLOUD");
        entity.setSecurityProtocol(model.getSecurityProtocol());
        Map<String, Object> cfg = new LinkedHashMap<>();
        if (!model.getNodeId().isEmpty()) cfg.put("nodeId", model.getNodeId());
        if (!model.getExecutableType().isEmpty()) cfg.put("executableType", model.getExecutableType());
        if (model.getProviderName() != null)
            cfg.put("providerName", model.getProviderName().name());
        if (!model.getUserAccountName().isEmpty()) cfg.put("userAccountName", model.getUserAccountName());
        entity.setConfig(cfg);
        return entity;
    }

    // UnicoreJobSubmission
    default UnicoreJobSubmission unicoreSubmissionToModel(ComputeJobSubmissionEntity entity) {
        if (entity == null) return null;
        UnicoreJobSubmission.Builder b = UnicoreJobSubmission.newBuilder();
        b.setJobSubmissionInterfaceId(entity.getSubmissionId());
        if (entity.getSecurityProtocol() != null) b.setSecurityProtocol(entity.getSecurityProtocol());
        Map<String, Object> cfg = entity.getConfig();
        if (cfg != null && cfg.get("unicoreEndPointURL") != null)
            b.setUnicoreEndPointUrl((String) cfg.get("unicoreEndPointURL"));
        return b.build();
    }

    default ComputeJobSubmissionEntity unicoreSubmissionToEntity(UnicoreJobSubmission model) {
        if (model == null) return null;
        ComputeJobSubmissionEntity entity = new ComputeJobSubmissionEntity();
        entity.setSubmissionId(model.getJobSubmissionInterfaceId());
        entity.setSubmissionType("UNICORE");
        entity.setSecurityProtocol(model.getSecurityProtocol());
        Map<String, Object> cfg = new LinkedHashMap<>();
        if (!model.getUnicoreEndPointUrl().isEmpty()) cfg.put("unicoreEndPointURL", model.getUnicoreEndPointUrl());
        entity.setConfig(cfg);
        return entity;
    }

    // --- QueueStatus ---
    QueueStatusModel queueStatusToModel(QueueStatusEntity entity);

    QueueStatusEntity queueStatusToEntity(QueueStatusModel model);

    // --- GatewayUsageReportingCommand ---
    GatewayUsageReportingCommand gatewayUsageReportingCommandToModel(GatewayUsageReportingCommandEntity entity);

    GatewayUsageReportingCommandEntity gatewayUsageReportingCommandToEntity(GatewayUsageReportingCommand model);

    // --- UserStoragePreference ---
    UserStoragePreference userStoragePrefToModel(UserStoragePreferenceEntity entity);

    UserStoragePreferenceEntity userStoragePrefToEntity(UserStoragePreference model);
}
