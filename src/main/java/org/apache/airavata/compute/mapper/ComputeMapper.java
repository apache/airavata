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
import org.apache.airavata.common.CommonMapperConversions;
import org.apache.airavata.models.appcatalog.computeresource.BatchQueue;
import org.apache.airavata.models.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.models.appcatalog.computeresource.FileSystems;
import org.apache.airavata.models.appcatalog.computeresource.JobManagerCommand;
import org.apache.airavata.models.appcatalog.computeresource.ResourceJobManager;
import org.apache.airavata.models.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.models.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.models.appcatalog.groupresourceprofile.BatchQueueResourcePolicy;
import org.apache.airavata.models.appcatalog.groupresourceprofile.ComputeResourcePolicy;
import org.apache.airavata.models.appcatalog.groupresourceprofile.ComputeResourceReservation;
import org.apache.airavata.models.appcatalog.groupresourceprofile.EnvironmentSpecificPreferences;
import org.apache.airavata.models.appcatalog.groupresourceprofile.GroupAccountSSHProvisionerConfig;
import org.apache.airavata.models.appcatalog.groupresourceprofile.GroupComputeResourcePreference;
import org.apache.airavata.models.appcatalog.groupresourceprofile.GroupResourceProfile;
import org.apache.airavata.models.appcatalog.groupresourceprofile.ResourceType;
import org.apache.airavata.models.appcatalog.groupresourceprofile.SlurmComputeResourcePreference;
import org.apache.airavata.models.appcatalog.userresourceprofile.UserComputeResourcePreference;
import org.apache.airavata.models.appcatalog.userresourceprofile.UserResourceProfile;
import org.apache.airavata.models.appcatalog.userresourceprofile.UserStoragePreference;
import org.apache.airavata.models.status.QueueStatusModel;
import org.apache.airavata.models.workspace.GatewayUsageReportingCommand;
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
        if (entity == null)
            return null;
        ComputeResourceDescription.Builder b = ComputeResourceDescription.newBuilder();
        if (entity.getComputeResourceId() != null)
            b.setComputeResourceId(entity.getComputeResourceId());
        if (entity.getHostName() != null)
            b.setHostName(entity.getHostName());
        if (entity.getResourceDescription() != null)
            b.setResourceDescription(entity.getResourceDescription());
        b.setEnabled(entity.getEnabled() != 0);
        b.setMaxMemoryPerNode(entity.getMaxMemoryPerNode());
        b.setGatewayUsageReporting(entity.isGatewayUsageReporting());
        b.setSshPort(entity.getSshPort());
        if (entity.getResourceJobManager() != null)
            b.setResourceJobManager(resourceJobManagerToModel(entity.getResourceJobManager()));
        if (entity.getGatewayUsageModuleLoadCommand() != null)
            b.setGatewayUsageModuleLoadCommand(entity.getGatewayUsageModuleLoadCommand());
        if (entity.getGatewayUsageExecutable() != null)
            b.setGatewayUsageExecutable(entity.getGatewayUsageExecutable());
        if (entity.getCpusPerNode() != null)
            b.setCpusPerNode(entity.getCpusPerNode());
        if (entity.getDefaultNodeCount() != null)
            b.setDefaultNodeCount(entity.getDefaultNodeCount());
        if (entity.getDefaultCPUCount() != null)
            b.setDefaultCpuCount(entity.getDefaultCPUCount());
        if (entity.getDefaultWalltime() != null)
            b.setDefaultWalltime(entity.getDefaultWalltime());
        if (entity.getHostAliases() != null)
            b.addAllHostAliases(entity.getHostAliases());
        if (entity.getIpAddresses() != null)
            b.addAllIpAddresses(entity.getIpAddresses());
        if (entity.getBatchQueues() != null) {
            entity.getBatchQueues().forEach(q -> b.addBatchQueues(batchQueueToModel(q)));
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
        if (model == null)
            return null;
        ComputeResourceEntity entity = new ComputeResourceEntity();
        entity.setComputeResourceId(model.computeResourceId());
        entity.setHostName(model.hostName());
        entity.setResourceDescription(model.resourceDescription());
        entity.setEnabled(model.enabled() ? (short) 1 : (short) 0);
        entity.setMaxMemoryPerNode(model.maxMemoryPerNode());
        entity.setGatewayUsageReporting(model.gatewayUsageReporting());
        entity.setSshPort(model.sshPort());
        if (model.hasResourceJobManager())
            entity.setResourceJobManager(resourceJobManagerToEntity(model.resourceJobManager()));
        entity.setGatewayUsageModuleLoadCommand(model.gatewayUsageModuleLoadCommand());
        entity.setGatewayUsageExecutable(model.gatewayUsageExecutable());
        entity.setCpusPerNode(model.cpusPerNode());
        entity.setDefaultNodeCount(model.defaultNodeCount());
        entity.setDefaultCPUCount(model.defaultCpuCount());
        entity.setDefaultWalltime(model.defaultWalltime());
        if (!model.hostAliases().isEmpty())
            entity.setHostAliases(model.hostAliases());
        if (!model.ipAddresses().isEmpty())
            entity.setIpAddresses(model.ipAddresses());
        if (!model.batchQueues().isEmpty()) {
            entity.setBatchQueues(model.batchQueues().stream()
                    .map(this::batchQueueToEntity)
                    .toList());
        }
        if (!model.fileSystems().isEmpty()) {
            List<Map<String, Object>> fsList = new ArrayList<>();
            model.fileSystems().forEach((fsNumber, path) -> {
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
    // The entity property is defaultCPUCount (acronym "CPU") but the proto property
    // is
    // defaultCpuCount ("Cpu"); MapStruct's case-sensitive name matching would
    // otherwise drop
    // it silently (unmappedTargetPolicy=IGNORE), leaving the queue's default core
    // count as 0.
    @Mapping(source = "maxRuntime", target = "maxRunTime")
    @Mapping(source = "defaultCPUCount", target = "defaultCpuCount")
    BatchQueue batchQueueToModel(BatchQueueEntity entity);

    @Mapping(source = "maxRunTime", target = "maxRuntime")
    @Mapping(source = "defaultCpuCount", target = "defaultCPUCount")
    BatchQueueEntity batchQueueToEntity(BatchQueue model);

    // --- ResourceJobManager ---
    // Hand-written so the job_manager_commands map (proto map<int32,string> keyed
    // by the
    // JobManagerCommand enum number) round-trips through the JOB_MANAGER_COMMAND
    // child table.
    // MapStruct cannot match the proto map accessor to the entity's
    // List<JobManagerCommandEntity>
    // and previously dropped the commands silently (sbatch/squeue/scancel were
    // never persisted).
    default ResourceJobManager resourceJobManagerToModel(ResourceJobManagerEntity entity) {
        if (entity == null)
            return null;
        ResourceJobManager.Builder b = ResourceJobManager.newBuilder();
        if (entity.getResourceJobManagerId() != null)
            b.setResourceJobManagerId(entity.getResourceJobManagerId());
        if (entity.getResourceJobManagerType() != null)
            b.setResourceJobManagerType(entity.getResourceJobManagerType());
        if (entity.getJobManagerBinPath() != null)
            b.setJobManagerBinPath(entity.getJobManagerBinPath());
        if (entity.getPushMonitoringEndpoint() != null)
            b.setPushMonitoringEndpoint(entity.getPushMonitoringEndpoint());
        if (entity.getJobManagerCommands() != null) {
            for (JobManagerCommandEntity cmd : entity.getJobManagerCommands()) {
                if (cmd.getCommandType() != null && cmd.getCommand() != null) {
                    b.putJobManagerCommands(cmd.getCommandType().getNumber(), cmd.getCommand());
                }
            }
        }
        return b.build();
    }

    default ResourceJobManagerEntity resourceJobManagerToEntity(ResourceJobManager model) {
        if (model == null)
            return null;
        ResourceJobManagerEntity entity = new ResourceJobManagerEntity();
        String rjmId = model.resourceJobManagerId();
        if (rjmId == null || rjmId.isEmpty()) {
            rjmId = "RJM_" + java.util.UUID.randomUUID();
        }
        entity.setResourceJobManagerId(rjmId);
        if (model.resourceJobManagerType() != null)
            entity.setResourceJobManagerType(model.resourceJobManagerType());
        entity.setJobManagerBinPath(model.jobManagerBinPath());
        entity.setPushMonitoringEndpoint(model.pushMonitoringEndpoint());
        if (!model.jobManagerCommands().isEmpty()) {
            List<JobManagerCommandEntity> cmds = new ArrayList<>();
            for (Map.Entry<Integer, String> e : model.jobManagerCommands().entrySet()) {
                JobManagerCommand type = JobManagerCommand.forNumber(e.getKey());
                if (type == null)
                    continue;
                JobManagerCommandEntity cmd = new JobManagerCommandEntity();
                cmd.setResourceJobManagerId(rjmId);
                cmd.setCommandType(type);
                cmd.setCommand(e.getValue());
                cmd.setResourceJobManager(entity);
                cmds.add(cmd);
            }
            entity.setJobManagerCommands(cmds);
        }
        return entity;
    }

    // --- GatewayResourceProfile / GatewayProfileEntity ---
    // MapStruct auto-maps the scalar tokens/IDs; the @AfterMapping hooks below add
    // the repeated
    // compute_resource_preferences child list, which MapStruct does not match
    // (proto's
    // getComputeResourcePreferencesList() vs entity property
    // `computeResourcePreferences`).
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
    default void afterGatewayProfileToEntity(GatewayResourceProfile model, @MappingTarget GatewayProfileEntity entity) {
        if (!model.computeResourcePreferences().isEmpty()) {
            List<ComputeResourcePreferenceEntity> prefs = new ArrayList<>();
            for (ComputeResourcePreference pref : model.computeResourcePreferences()) {
                prefs.add(computeResourcePrefToEntity(pref));
            }
            entity.setComputeResourcePreferences(prefs);
        }
    }

    // --- ComputeResourcePreference ---
    default ComputeResourcePreference computeResourcePrefToModel(ComputeResourcePreferenceEntity entity) {
        if (entity == null)
            return null;
        ComputeResourcePreference.Builder b = ComputeResourcePreference.newBuilder();
        if (entity.getComputeResourceId() != null)
            b.setComputeResourceId(entity.getComputeResourceId());
        if (entity.getLoginUserName() != null)
            b.setLoginUserName(entity.getLoginUserName());
        b.setOverrideByAiravata(entity.isOverridebyAiravata());
        if (entity.getPreferredBatchQueue() != null)
            b.setPreferredBatchQueue(entity.getPreferredBatchQueue());
        if (entity.getQualityOfService() != null)
            b.setQualityOfService(entity.getQualityOfService());
        if (entity.getReservation() != null)
            b.setReservation(entity.getReservation());
        if (entity.getReservationStartTime() != null)
            b.setReservationStartTime(entity.getReservationStartTime().getTime());
        if (entity.getReservationEndTime() != null)
            b.setReservationEndTime(entity.getReservationEndTime().getTime());
        if (entity.getResourceSpecificCredentialStoreToken() != null)
            b.setResourceSpecificCredentialStoreToken(entity.getResourceSpecificCredentialStoreToken());
        if (entity.getScratchLocation() != null)
            b.setScratchLocation(entity.getScratchLocation());
        if (entity.getUsageReportingGatewayId() != null)
            b.setUsageReportingGatewayId(entity.getUsageReportingGatewayId());
        if (entity.getSshAccountProvisioner() != null)
            b.setSshAccountProvisioner(entity.getSshAccountProvisioner());
        if (entity.getSshAccountProvisionerAdditionalInfo() != null)
            b.setSshAccountProvisionerAdditionalInfo(entity.getSshAccountProvisionerAdditionalInfo());
        if (entity.getSshAccountProvisionerConfigurations() != null) {
            for (Map<String, Object> entry : entity.getSshAccountProvisionerConfigurations()) {
                String name = (String) entry.get("configName");
                String value = (String) entry.getOrDefault("configValue", "");
                if (name != null)
                    b.putSshAccountProvisionerConfig(name, value);
            }
        }
        return b.build();
    }

    default ComputeResourcePreferenceEntity computeResourcePrefToEntity(ComputeResourcePreference model) {
        if (model == null)
            return null;
        ComputeResourcePreferenceEntity entity = new ComputeResourcePreferenceEntity();
        entity.setComputeResourceId(model.computeResourceId());
        entity.setLoginUserName(model.loginUserName());
        entity.setOverridebyAiravata(model.overrideByAiravata());
        entity.setPreferredBatchQueue(model.preferredBatchQueue());
        entity.setQualityOfService(model.qualityOfService());
        entity.setReservation(model.reservation());
        if (model.reservationStartTime() != 0)
            entity.setReservationStartTime(new java.sql.Timestamp(model.reservationStartTime()));
        if (model.reservationEndTime() != 0)
            entity.setReservationEndTime(new java.sql.Timestamp(model.reservationEndTime()));
        entity.setResourceSpecificCredentialStoreToken(model.resourceSpecificCredentialStoreToken());
        entity.setScratchLocation(model.scratchLocation());
        entity.setUsageReportingGatewayId(model.usageReportingGatewayId());
        entity.setSshAccountProvisioner(model.sshAccountProvisioner());
        entity.setSshAccountProvisionerAdditionalInfo(model.sshAccountProvisionerAdditionalInfo());
        if (!model.sshAccountProvisionerConfig().isEmpty()) {
            List<Map<String, Object>> configs = new ArrayList<>();
            model.sshAccountProvisionerConfig().forEach((name, value) -> {
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
    // Hand-written so the repeated child collections (compute preferences and the
    // two policy
    // lists) are mapped: MapStruct does not match protobuf's repeated accessors
    // (getComputePreferencesList()) to the entity's `computePreferences` property
    // and would
    // silently drop them in both directions.
    default GroupResourceProfile groupResourceProfileToModel(GroupResourceProfileEntity entity) {
        if (entity == null)
            return null;
        GroupResourceProfile.Builder b = GroupResourceProfile.newBuilder();
        if (entity.getGatewayId() != null)
            b.setGatewayId(entity.getGatewayId());
        if (entity.getGroupResourceProfileId() != null)
            b.setGroupResourceProfileId(entity.getGroupResourceProfileId());
        if (entity.getGroupResourceProfileName() != null)
            b.setGroupResourceProfileName(entity.getGroupResourceProfileName());
        if (entity.getCreationTime() != null)
            b.setCreationTime(entity.getCreationTime());
        if (entity.getUpdatedTime() != null)
            b.setUpdatedTime(entity.getUpdatedTime());
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
        if (model == null)
            return null;
        GroupResourceProfileEntity entity = new GroupResourceProfileEntity();
        entity.setGatewayId(model.gatewayId());
        entity.setGroupResourceProfileId(model.groupResourceProfileId());
        entity.setGroupResourceProfileName(model.groupResourceProfileName());
        entity.setCreationTime(model.creationTime());
        entity.setUpdatedTime(model.updatedTime());
        entity.setDefaultCredentialStoreToken(model.defaultCredentialStoreToken());
        if (!model.computePreferences().isEmpty()) {
            List<GroupComputeResourcePrefEntity> prefs = new ArrayList<>();
            for (GroupComputeResourcePreference pref : model.computePreferences()) {
                prefs.add(groupComputePrefToEntity(pref));
            }
            entity.setComputePreferences(prefs);
        }
        if (!model.computeResourcePolicies().isEmpty()) {
            List<ComputeResourcePolicyEntity> policies = new ArrayList<>();
            for (ComputeResourcePolicy policy : model.computeResourcePolicies()) {
                policies.add(computeResourcePolicyToEntity(policy));
            }
            entity.setComputeResourcePolicies(policies);
        }
        if (!model.batchQueueResourcePolicies().isEmpty()) {
            List<BatchQueueResourcePolicyEntity> policies = new ArrayList<>();
            for (BatchQueueResourcePolicy policy : model.batchQueueResourcePolicies()) {
                policies.add(batchQueuePolicyToEntity(policy));
            }
            entity.setBatchQueueResourcePolicies(policies);
        }
        return entity;
    }

    // --- GroupComputeResourcePreference ---
    // Hand-written so the Slurm-variant nested message (specific_preferences oneof)
    // and its repeated
    // child lists (reservations, group_ssh_account_provisioner_configs) are mapped:
    // MapStruct maps
    // only the base scalars and silently drops the nested
    // EnvironmentSpecificPreferences plus the
    // repeated children that live on the SlurmGroupComputeResourcePrefEntity
    // subclass.
    default GroupComputeResourcePreference groupComputePrefToModel(GroupComputeResourcePrefEntity entity) {
        if (entity == null)
            return null;
        GroupComputeResourcePreference.Builder b = GroupComputeResourcePreference.newBuilder();
        if (entity.getComputeResourceId() != null)
            b.setComputeResourceId(entity.getComputeResourceId());
        if (entity.getGroupResourceProfileId() != null)
            b.setGroupResourceProfileId(entity.getGroupResourceProfileId());
        b.setOverrideByAiravata(entity.getOverridebyAiravata() != 0);
        if (entity.getLoginUserName() != null)
            b.setLoginUserName(entity.getLoginUserName());
        if (entity.getScratchLocation() != null)
            b.setScratchLocation(entity.getScratchLocation());
        if (entity.getResourceSpecificCredentialStoreToken() != null)
            b.setResourceSpecificCredentialStoreToken(entity.getResourceSpecificCredentialStoreToken());
        if (entity instanceof SlurmGroupComputeResourcePrefEntity slurm) {
            b.setResourceType(ResourceType.SLURM);
            SlurmComputeResourcePreference.Builder s = SlurmComputeResourcePreference.newBuilder();
            if (slurm.getAllocationProjectNumber() != null)
                s.setAllocationProjectNumber(slurm.getAllocationProjectNumber());
            if (slurm.getPreferredBatchQueue() != null)
                s.setPreferredBatchQueue(slurm.getPreferredBatchQueue());
            if (slurm.getQualityOfService() != null)
                s.setQualityOfService(slurm.getQualityOfService());
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
                    if (resourceId != null)
                        c.setResourceId((String) resourceId);
                    if (groupResourceProfileId != null)
                        c.setGroupResourceProfileId((String) groupResourceProfileId);
                    if (configName != null)
                        c.setConfigName((String) configName);
                    if (configValue != null)
                        c.setConfigValue((String) configValue);
                    s.addGroupSshAccountProvisionerConfigs(c.build());
                }
            }
            if (slurm.getReservations() != null) {
                for (ComputeResourceReservationEntity r : slurm.getReservations()) {
                    s.addReservations(reservationToModel(r));
                }
            }
            b.setSpecificPreferences(new EnvironmentSpecificPreferences.Slurm(s.build()));
        }
        return b.build();
    }

    // Uses SlurmGroupComputeResourcePrefEntity as the default concrete type
    default SlurmGroupComputeResourcePrefEntity groupComputePrefToEntity(GroupComputeResourcePreference model) {
        if (model == null)
            return null;
        SlurmGroupComputeResourcePrefEntity entity = new SlurmGroupComputeResourcePrefEntity();
        entity.setComputeResourceId(model.computeResourceId());
        entity.setGroupResourceProfileId(model.groupResourceProfileId());
        entity.setOverridebyAiravata(model.overrideByAiravata() ? (short) 1 : (short) 0);
        entity.setLoginUserName(model.loginUserName());
        entity.setScratchLocation(model.scratchLocation());
        entity.setResourceSpecificCredentialStoreToken(model.resourceSpecificCredentialStoreToken());
        if (model.hasSpecificPreferences()
                && model.specificPreferences() instanceof EnvironmentSpecificPreferences.Slurm slurmVariant) {
            SlurmComputeResourcePreference slurm = slurmVariant.slurm();
            entity.setAllocationProjectNumber(slurm.allocationProjectNumber());
            entity.setPreferredBatchQueue(slurm.preferredBatchQueue());
            entity.setQualityOfService(slurm.qualityOfService());
            entity.setUsageReportingGatewayId(slurm.usageReportingGatewayId());
            entity.setSshAccountProvisioner(slurm.sshAccountProvisioner());
            entity.setSshAccountProvisionerAdditionalInfo(slurm.sshAccountProvisionerAdditionalInfo());
            if (!slurm.groupSshAccountProvisionerConfigs().isEmpty()) {
                List<Map<String, Object>> configs = new ArrayList<>();
                for (GroupAccountSSHProvisionerConfig c : slurm.groupSshAccountProvisionerConfigs()) {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("resourceId", c.resourceId());
                    entry.put("groupResourceProfileId", c.groupResourceProfileId());
                    entry.put("configName", c.configName());
                    entry.put("configValue", c.configValue());
                    configs.add(entry);
                }
                entity.setGroupSSHAccountProvisionerConfigs(configs);
            }
            if (!slurm.reservations().isEmpty()) {
                List<ComputeResourceReservationEntity> reservations = new ArrayList<>();
                for (ComputeResourceReservation r : slurm.reservations()) {
                    reservations.add(reservationToEntity(r));
                }
                entity.setReservations(reservations);
            }
        }
        return entity;
    }

    // --- ComputeResourceReservation ---
    default ComputeResourceReservation reservationToModel(ComputeResourceReservationEntity entity) {
        if (entity == null)
            return null;
        ComputeResourceReservation.Builder b = ComputeResourceReservation.newBuilder();
        if (entity.getReservationId() != null)
            b.setReservationId(entity.getReservationId());
        if (entity.getReservationName() != null)
            b.setReservationName(entity.getReservationName());
        if (entity.getQueueNames() != null)
            b.addAllQueueNames(entity.getQueueNames());
        if (entity.getStartTime() != null)
            b.setStartTime(entity.getStartTime().getTime());
        if (entity.getEndTime() != null)
            b.setEndTime(entity.getEndTime().getTime());
        return b.build();
    }

    default ComputeResourceReservationEntity reservationToEntity(ComputeResourceReservation model) {
        if (model == null)
            return null;
        ComputeResourceReservationEntity entity = new ComputeResourceReservationEntity();
        entity.setReservationId(model.reservationId());
        entity.setReservationName(model.reservationName());
        if (!model.queueNames().isEmpty()) entity.setQueueNames(new ArrayList<>(model.queueNames()));
        if (model.startTime() != 0) entity.setStartTime(new java.sql.Timestamp(model.startTime()));
        if (model.endTime() != 0) entity.setEndTime(new java.sql.Timestamp(model.endTime()));
        return entity;
    }

    // --- ComputeResourcePolicy ---
    // MapStruct maps the scalar IDs; the @AfterMapping hooks add the repeated
    // allowed_batch_queues
    // list, which it does not match (proto's getAllowedBatchQueuesList() vs entity
    // collection).
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
        if (!model.allowedBatchQueues().isEmpty()) {
            entity.setAllowedBatchQueues(new ArrayList<>(model.allowedBatchQueues()));
        }
    }

    // --- BatchQueueResourcePolicy ---
    BatchQueueResourcePolicy batchQueuePolicyToModel(BatchQueueResourcePolicyEntity entity);

    BatchQueueResourcePolicyEntity batchQueuePolicyToEntity(BatchQueueResourcePolicy model);

    // --- UserResourceProfile ---
    // MapStruct auto-maps the scalar tokens/IDs; the @AfterMapping hooks below add
    // the repeated
    // user_compute_resource_preferences child list, which MapStruct does not match
    // (proto's
    // getUserComputeResourcePreferencesList() vs entity property
    // `userComputeResourcePreferences`).
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
        if (!model.userComputeResourcePreferences().isEmpty()) {
            List<UserComputeResourcePreferenceEntity> prefs = new ArrayList<>();
            for (UserComputeResourcePreference pref : model.userComputeResourcePreferences()) {
                prefs.add(userComputeResourcePrefToEntity(pref));
            }
            entity.setUserComputeResourcePreferences(prefs);
        }
    }

    // --- UserComputeResourcePreference ---
    UserComputeResourcePreference userComputeResourcePrefToModel(UserComputeResourcePreferenceEntity entity);

    UserComputeResourcePreferenceEntity userComputeResourcePrefToEntity(UserComputeResourcePreference model);

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
