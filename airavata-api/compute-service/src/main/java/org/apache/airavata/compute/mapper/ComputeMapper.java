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
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupResourceProfile;
import org.apache.airavata.model.appcatalog.userresourceprofile.proto.UserComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.proto.UserResourceProfile;
import org.apache.airavata.model.appcatalog.userresourceprofile.proto.UserStoragePreference;
import org.apache.airavata.model.status.proto.QueueStatusModel;
import org.apache.airavata.model.workspace.proto.GatewayUsageReportingCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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
    GatewayResourceProfile gatewayProfileToModel(GatewayProfileEntity entity);

    GatewayProfileEntity gatewayProfileToEntity(GatewayResourceProfile model);

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
    GroupResourceProfile groupResourceProfileToModel(GroupResourceProfileEntity entity);

    GroupResourceProfileEntity groupResourceProfileToEntity(GroupResourceProfile model);

    // --- GroupComputeResourcePreference ---
    GroupComputeResourcePreference groupComputePrefToModel(GroupComputeResourcePrefEntity entity);
    // Uses SlurmGroupComputeResourcePrefEntity as the default concrete type
    SlurmGroupComputeResourcePrefEntity groupComputePrefToEntity(GroupComputeResourcePreference model);

    // --- ComputeResourcePolicy ---
    ComputeResourcePolicy computeResourcePolicyToModel(ComputeResourcePolicyEntity entity);

    ComputeResourcePolicyEntity computeResourcePolicyToEntity(ComputeResourcePolicy model);

    // --- BatchQueueResourcePolicy ---
    BatchQueueResourcePolicy batchQueuePolicyToModel(BatchQueueResourcePolicyEntity entity);

    BatchQueueResourcePolicyEntity batchQueuePolicyToEntity(BatchQueueResourcePolicy model);

    // --- UserResourceProfile ---
    UserResourceProfile userResourceProfileToModel(UserResourceProfileEntity entity);

    UserResourceProfileEntity userResourceProfileToEntity(UserResourceProfile model);

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
