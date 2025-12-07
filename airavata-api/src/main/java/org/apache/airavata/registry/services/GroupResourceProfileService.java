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
package org.apache.airavata.registry.services;

import com.github.dozermapper.core.Mapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.model.appcatalog.groupresourceprofile.AwsComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.BatchQueueResourcePolicy;
import org.apache.airavata.model.appcatalog.groupresourceprofile.ComputeResourcePolicy;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupResourceProfile;
import org.apache.airavata.model.appcatalog.groupresourceprofile.ResourceType;
import org.apache.airavata.model.appcatalog.groupresourceprofile.SlurmComputeResourcePreference;
import org.apache.airavata.model.commons.airavata_commonsConstants;
import org.apache.airavata.registry.entities.appcatalog.AWSGroupComputeResourcePrefEntity;
import org.apache.airavata.registry.entities.appcatalog.BatchQueueResourcePolicyEntity;
import org.apache.airavata.registry.entities.appcatalog.ComputeResourcePolicyEntity;
import org.apache.airavata.registry.entities.appcatalog.ComputeResourceReservationEntity;
import org.apache.airavata.registry.entities.appcatalog.GroupComputeResourcePrefEntity;
import org.apache.airavata.registry.entities.appcatalog.GroupComputeResourcePrefPK;
import org.apache.airavata.registry.entities.appcatalog.GroupResourceProfileEntity;
import org.apache.airavata.registry.entities.appcatalog.SlurmGroupComputeResourcePrefEntity;
import org.apache.airavata.registry.repositories.appcatalog.BatchQueuePolicyRepository;
import org.apache.airavata.registry.repositories.appcatalog.ComputeResourcePolicyRepository;
import org.apache.airavata.registry.repositories.appcatalog.GroupResourceProfileRepository;
import org.apache.airavata.registry.repositories.appcatalog.GrpComputePrefRepository;
import org.apache.airavata.registry.utils.ObjectMapperSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GroupResourceProfileService {
    @Autowired
    private GroupResourceProfileRepository groupResourceProfileRepository;

    @Autowired
    private GrpComputePrefRepository grpComputePrefRepository;

    @Autowired
    private ComputeResourcePolicyRepository computeResourcePolicyRepository;

    @Autowired
    private BatchQueuePolicyRepository batchQueuePolicyRepository;

    public String addGroupResourceProfile(GroupResourceProfile groupResourceProfile) {
        final String groupResourceProfileId = UUID.randomUUID().toString();
        groupResourceProfile.setGroupResourceProfileId(groupResourceProfileId);
        groupResourceProfile.setCreationTime(System.currentTimeMillis());
        updateChildren(groupResourceProfile, groupResourceProfileId);
        return updateGroupResourceProfile(groupResourceProfile);
    }

    private void updateChildren(GroupResourceProfile groupResourceProfile, String groupResourceProfileId) {
        if (groupResourceProfile.getComputePreferences() != null) {
            for (GroupComputeResourcePreference gcrPref : groupResourceProfile.getComputePreferences()) {
                gcrPref.setGroupResourceProfileId(groupResourceProfileId);

                if (gcrPref.getResourceType() == ResourceType.SLURM
                        && gcrPref.isSetSpecificPreferences()
                        && gcrPref.getSpecificPreferences().isSetSlurm()) {

                    SlurmComputeResourcePreference slurm =
                            gcrPref.getSpecificPreferences().getSlurm();

                    // update SSH provisioner configs
                    if (slurm.getGroupSSHAccountProvisionerConfigs() != null) {
                        slurm.getGroupSSHAccountProvisionerConfigs()
                                .forEach(gssh -> gssh.setGroupResourceProfileId(groupResourceProfileId));
                    }

                    // update reservations
                    if (slurm.getReservations() != null) {
                        slurm.getReservations().forEach(res -> {
                            if (res.getReservationId().trim().isEmpty()
                                    || res.getReservationId().equals(airavata_commonsConstants.DEFAULT_ID)) {
                                res.setReservationId(AiravataUtils.getId(res.getReservationName()));
                            }
                        });
                    }
                }
            }
        }
        if (groupResourceProfile.getBatchQueueResourcePolicies() != null) {
            groupResourceProfile.getBatchQueueResourcePolicies().forEach(bq -> {
                if (bq.getResourcePolicyId().trim().isEmpty()
                        || bq.getResourcePolicyId().equals(airavata_commonsConstants.DEFAULT_ID)) {
                    bq.setResourcePolicyId(UUID.randomUUID().toString());
                }
                bq.setGroupResourceProfileId(groupResourceProfileId);
            });
        }
        if (groupResourceProfile.getComputeResourcePolicies() != null) {
            groupResourceProfile.getComputeResourcePolicies().forEach(cr -> {
                if (cr.getResourcePolicyId().trim().isEmpty()
                        || cr.getResourcePolicyId().equals(airavata_commonsConstants.DEFAULT_ID)) {
                    cr.setResourcePolicyId(UUID.randomUUID().toString());
                }
                cr.setGroupResourceProfileId(groupResourceProfileId);
            });
        }
    }

    public String updateGroupResourceProfile(GroupResourceProfile updatedGroupResourceProfile) {
        updatedGroupResourceProfile.setUpdatedTime(System.currentTimeMillis());
        updateChildren(updatedGroupResourceProfile, updatedGroupResourceProfile.getGroupResourceProfileId());
        Mapper mapper = ObjectMapperSingleton.getInstance();
        GroupResourceProfileEntity groupResourceProfileEntity =
                mapper.map(updatedGroupResourceProfile, GroupResourceProfileEntity.class);
        patchComputePrefEntities(groupResourceProfileEntity, updatedGroupResourceProfile);
        updateChildrenEntities(groupResourceProfileEntity);
        GroupResourceProfileEntity saved = groupResourceProfileRepository.save(groupResourceProfileEntity);
        return saved.getGroupResourceProfileId();
    }

    private void updateChildrenEntities(GroupResourceProfileEntity groupResourceProfileEntity) {
        if (groupResourceProfileEntity.getComputePreferences() != null) {
            for (GroupComputeResourcePrefEntity gcrPref : groupResourceProfileEntity.getComputePreferences()) {
                gcrPref.setGroupResourceProfile(groupResourceProfileEntity);
                if (gcrPref instanceof SlurmGroupComputeResourcePrefEntity) {
                    SlurmGroupComputeResourcePrefEntity slurm = (SlurmGroupComputeResourcePrefEntity) gcrPref;
                    if (slurm.getReservations() != null) {
                        for (ComputeResourceReservationEntity r : slurm.getReservations()) {
                            r.setGroupComputeResourcePref(slurm);
                        }
                    }
                }
            }
        }
    }

    private void patchComputePrefEntities(GroupResourceProfileEntity destEntity, GroupResourceProfile sourceModel) {
        if (destEntity.getComputePreferences() == null || sourceModel.getComputePreferences() == null) {
            return;
        }
        Map<String, GroupComputeResourcePreference> sourcePrefs = new HashMap<>();
        for (GroupComputeResourcePreference pref : sourceModel.getComputePreferences()) {
            sourcePrefs.put(pref.getComputeResourceId(), pref);
        }

        for (GroupComputeResourcePrefEntity prefEntity : destEntity.getComputePreferences()) {
            GroupComputeResourcePreference sourcePref = sourcePrefs.get(prefEntity.getComputeResourceId());
            if (sourcePref == null || !sourcePref.isSetSpecificPreferences()) {
                continue;
            }
            if (prefEntity instanceof SlurmGroupComputeResourcePrefEntity slurmEntity) {
                if (sourcePref.getSpecificPreferences().isSetSlurm()) {
                    SlurmComputeResourcePreference slurm =
                            sourcePref.getSpecificPreferences().getSlurm();
                    slurmEntity.setAllocationProjectNumber(slurm.getAllocationProjectNumber());
                    slurmEntity.setPreferredBatchQueue(slurm.getPreferredBatchQueue());
                    slurmEntity.setQualityOfService(slurm.getQualityOfService());
                    slurmEntity.setUsageReportingGatewayId(slurm.getUsageReportingGatewayId());
                    slurmEntity.setSshAccountProvisioner(slurm.getSshAccountProvisioner());
                    slurmEntity.setSshAccountProvisionerAdditionalInfo(slurm.getSshAccountProvisionerAdditionalInfo());
                }
            } else if (prefEntity instanceof AWSGroupComputeResourcePrefEntity awsEntity) {
                if (sourcePref.getSpecificPreferences().isSetAws()) {
                    AwsComputeResourcePreference aws =
                            sourcePref.getSpecificPreferences().getAws();
                    awsEntity.setRegion(aws.getRegion());
                    awsEntity.setPreferredAmiId(aws.getPreferredAmiId());
                    awsEntity.setPreferredInstanceType(aws.getPreferredInstanceType());
                }
            }
        }
    }

    public GroupResourceProfile getGroupResourceProfile(String groupResourceProfileId) {
        GroupResourceProfileEntity entity =
                groupResourceProfileRepository.findById(groupResourceProfileId).orElse(null);
        if (entity == null) return null;
        Mapper mapper = ObjectMapperSingleton.getInstance();
        GroupResourceProfile groupResourceProfile = mapper.map(entity, GroupResourceProfile.class);

        List<GroupComputeResourcePreference> decoratedPrefs = new ArrayList<>();
        for (GroupComputeResourcePreference raw : groupResourceProfile.getComputePreferences()) {
            GroupComputeResourcePrefPK pk = new GroupComputeResourcePrefPK();
            pk.setComputeResourceId(raw.getComputeResourceId());
            pk.setGroupResourceProfileId(raw.getGroupResourceProfileId());
            decoratedPrefs.add(grpComputePrefRepository
                    .findById(pk)
                    .map(e -> mapper.map(e, GroupComputeResourcePreference.class))
                    .orElse(raw));
        }
        groupResourceProfile.setComputePreferences(decoratedPrefs);

        return groupResourceProfile;
    }

    public boolean removeGroupResourceProfile(String groupResourceProfileId) {
        if (!groupResourceProfileRepository.existsById(groupResourceProfileId)) {
            return false;
        }
        groupResourceProfileRepository.deleteById(groupResourceProfileId);
        return true;
    }

    public boolean isGroupResourceProfileExists(String groupResourceProfileId) {
        return groupResourceProfileRepository.existsById(groupResourceProfileId);
    }

    public List<GroupResourceProfile> getAllGroupResourceProfiles(
            String gatewayId, List<String> accessibleGroupResProfileIds) {
        if (accessibleGroupResProfileIds != null && !accessibleGroupResProfileIds.isEmpty()) {
            List<GroupResourceProfileEntity> entities =
                    groupResourceProfileRepository.findAccessibleGroupResourceProfiles(
                            gatewayId, accessibleGroupResProfileIds);
            Mapper mapper = ObjectMapperSingleton.getInstance();
            List<GroupResourceProfile> profiles = entities.stream()
                    .map(e -> mapper.map(e, GroupResourceProfile.class))
                    .collect(Collectors.toList());

            for (GroupResourceProfile profile : profiles) {
                List<GroupComputeResourcePreference> decoratedPrefs = new ArrayList<>();

                for (GroupComputeResourcePreference rawPref : profile.getComputePreferences()) {
                    GroupComputeResourcePrefPK pk = new GroupComputeResourcePrefPK();
                    pk.setComputeResourceId(rawPref.getComputeResourceId());
                    pk.setGroupResourceProfileId(rawPref.getGroupResourceProfileId());

                    GroupComputeResourcePreference fullPref = grpComputePrefRepository
                            .findById(pk)
                            .map(e -> mapper.map(e, GroupComputeResourcePreference.class))
                            .orElse(rawPref);
                    decoratedPrefs.add(fullPref);
                }

                profile.setComputePreferences(decoratedPrefs);
            }
            return profiles;

        } else {
            return Collections.emptyList();
        }
    }

    public boolean removeGroupComputeResourcePreference(String computeResourceId, String groupResourceProfileId) {
        GroupComputeResourcePrefPK groupComputeResourcePrefPK = new GroupComputeResourcePrefPK();
        groupComputeResourcePrefPK.setComputeResourceId(computeResourceId);
        groupComputeResourcePrefPK.setGroupResourceProfileId(groupResourceProfileId);

        grpComputePrefRepository.deleteById(groupComputeResourcePrefPK);
        return true;
    }

    public boolean removeComputeResourcePolicy(String resourcePolicyId) {
        computeResourcePolicyRepository.deleteById(resourcePolicyId);
        return true;
    }

    public boolean removeBatchQueueResourcePolicy(String resourcePolicyId) {
        batchQueuePolicyRepository.deleteById(resourcePolicyId);
        return true;
    }

    public GroupComputeResourcePreference getGroupComputeResourcePreference(
            String computeResourceId, String groupResourceProfileId) {
        GroupComputeResourcePrefPK groupComputeResourcePrefPK = new GroupComputeResourcePrefPK();
        groupComputeResourcePrefPK.setGroupResourceProfileId(groupResourceProfileId);
        groupComputeResourcePrefPK.setComputeResourceId(computeResourceId);

        Mapper mapper = ObjectMapperSingleton.getInstance();
        return grpComputePrefRepository
                .findById(groupComputeResourcePrefPK)
                .map(entity -> mapper.map(entity, GroupComputeResourcePreference.class))
                .orElse(null);
    }

    public boolean isGroupComputeResourcePreferenceExists(String computeResourceId, String groupResourceProfileId) {
        GroupComputeResourcePrefPK groupComputeResourcePrefPK = new GroupComputeResourcePrefPK();
        groupComputeResourcePrefPK.setGroupResourceProfileId(groupResourceProfileId);
        groupComputeResourcePrefPK.setComputeResourceId(computeResourceId);

        return grpComputePrefRepository.existsById(groupComputeResourcePrefPK);
    }

    public ComputeResourcePolicy getComputeResourcePolicy(String resourcePolicyId) {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return computeResourcePolicyRepository
                .findById(resourcePolicyId)
                .map(entity -> mapper.map(entity, ComputeResourcePolicy.class))
                .orElse(null);
    }

    public BatchQueueResourcePolicy getBatchQueueResourcePolicy(String resourcePolicyId) {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return batchQueuePolicyRepository
                .findById(resourcePolicyId)
                .map(entity -> mapper.map(entity, BatchQueueResourcePolicy.class))
                .orElse(null);
    }

    public List<GroupComputeResourcePreference> getAllGroupComputeResourcePreferences(String groupResourceProfileId) {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        List<GroupComputeResourcePrefEntity> entities =
                grpComputePrefRepository.findByGroupResourceProfileId(groupResourceProfileId);
        List<GroupComputeResourcePreference> decorated = new ArrayList<>();
        for (GroupComputeResourcePrefEntity entity : entities) {
            GroupComputeResourcePreference full = mapper.map(entity, GroupComputeResourcePreference.class);
            decorated.add(full);
        }

        return decorated;
    }

    public List<BatchQueueResourcePolicy> getAllGroupBatchQueueResourcePolicies(String groupResourceProfileId) {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        List<BatchQueueResourcePolicyEntity> entities =
                batchQueuePolicyRepository.findByGroupResourceProfileId(groupResourceProfileId);
        return entities.stream()
                .map(entity -> mapper.map(entity, BatchQueueResourcePolicy.class))
                .collect(Collectors.toList());
    }

    public List<ComputeResourcePolicy> getAllGroupComputeResourcePolicies(String groupResourceProfileId) {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        List<ComputeResourcePolicyEntity> entities =
                computeResourcePolicyRepository.findByGroupResourceProfileId(groupResourceProfileId);
        return entities.stream()
                .map(entity -> mapper.map(entity, ComputeResourcePolicy.class))
                .collect(Collectors.toList());
    }
}
