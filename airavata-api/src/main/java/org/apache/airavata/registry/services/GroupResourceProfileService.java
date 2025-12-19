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
import org.apache.airavata.common.model.AiravataCommonsConstants;
import org.apache.airavata.common.model.AwsComputeResourcePreference;
import org.apache.airavata.common.model.BatchQueueResourcePolicy;
import org.apache.airavata.common.model.ComputeResourcePolicy;
import org.apache.airavata.common.model.ComputeResourceType;
import org.apache.airavata.common.model.GroupComputeResourcePreference;
import org.apache.airavata.common.model.GroupResourceProfile;
import org.apache.airavata.common.model.SlurmComputeResourcePreference;
import org.apache.airavata.common.utils.AiravataUtils;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GroupResourceProfileService {
    private final GroupResourceProfileRepository groupResourceProfileRepository;
    private final GrpComputePrefRepository grpComputePrefRepository;
    private final ComputeResourcePolicyRepository computeResourcePolicyRepository;
    private final Mapper mapper;
    private final BatchQueuePolicyRepository batchQueuePolicyRepository;

    public GroupResourceProfileService(
            GroupResourceProfileRepository groupResourceProfileRepository,
            GrpComputePrefRepository grpComputePrefRepository,
            ComputeResourcePolicyRepository computeResourcePolicyRepository,
            Mapper mapper,
            BatchQueuePolicyRepository batchQueuePolicyRepository) {
        this.groupResourceProfileRepository = groupResourceProfileRepository;
        this.grpComputePrefRepository = grpComputePrefRepository;
        this.computeResourcePolicyRepository = computeResourcePolicyRepository;
        this.mapper = mapper;
        this.batchQueuePolicyRepository = batchQueuePolicyRepository;
    }

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

                if (gcrPref.getResourceType() == ComputeResourceType.SLURM
                        && gcrPref.getSpecificPreferences() != null
                        && gcrPref.getSpecificPreferences().isSlurm()) {

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
                                    || res.getReservationId().equals(AiravataCommonsConstants.DEFAULT_ID)) {
                                res.setReservationId(AiravataUtils.getId(res.getReservationName()));
                            }
                        });
                    }
                }
            }
        }
        if (groupResourceProfile.getBatchQueueResourcePolicies() != null) {
            groupResourceProfile.getBatchQueueResourcePolicies().forEach(bq -> {
                if (bq.getResourcePolicyId() == null || bq.getResourcePolicyId().trim().isEmpty()
                        || bq.getResourcePolicyId().equals(AiravataCommonsConstants.DEFAULT_ID)) {
                    bq.setResourcePolicyId(UUID.randomUUID().toString());
                }
                bq.setGroupResourceProfileId(groupResourceProfileId);
            });
        }
        if (groupResourceProfile.getComputeResourcePolicies() != null) {
            groupResourceProfile.getComputeResourcePolicies().forEach(cr -> {
                if (cr.getResourcePolicyId() == null || cr.getResourcePolicyId().trim().isEmpty()
                        || cr.getResourcePolicyId().equals(AiravataCommonsConstants.DEFAULT_ID)) {
                    cr.setResourcePolicyId(UUID.randomUUID().toString());
                }
                cr.setGroupResourceProfileId(groupResourceProfileId);
            });
        }
    }

    public String updateGroupResourceProfile(GroupResourceProfile updatedGroupResourceProfile) {
        updatedGroupResourceProfile.setUpdatedTime(System.currentTimeMillis());
        updateChildren(updatedGroupResourceProfile, updatedGroupResourceProfile.getGroupResourceProfileId());
        // Preserve creationTime if not set in the update
        GroupResourceProfileEntity existingEntity = groupResourceProfileRepository
                .findById(updatedGroupResourceProfile.getGroupResourceProfileId())
                .orElse(null);
        boolean isNewEntity = (existingEntity == null);
        if (existingEntity != null && existingEntity.getCreationTime() != null) {
            // Preserve creationTime from existing entity
            updatedGroupResourceProfile.setCreationTime(existingEntity.getCreationTime());
        }
        // Temporarily remove computePreferences and policies to avoid Dozer trying to instantiate abstract class
        // and to prevent duplicate entity issues
        List<GroupComputeResourcePreference> computePreferences = updatedGroupResourceProfile.getComputePreferences();
        List<BatchQueueResourcePolicy> batchQueueResourcePolicies = updatedGroupResourceProfile.getBatchQueueResourcePolicies();
        List<ComputeResourcePolicy> computeResourcePolicies = updatedGroupResourceProfile.getComputeResourcePolicies();
        updatedGroupResourceProfile.setComputePreferences(null);
        updatedGroupResourceProfile.setBatchQueueResourcePolicies(null);
        updatedGroupResourceProfile.setComputeResourcePolicies(null);
        // Preserve existing lists to avoid orphan deletion issues
        List<GroupComputeResourcePrefEntity> existingPrefList = null;
        List<BatchQueueResourcePolicyEntity> existingBqList = null;
        List<ComputeResourcePolicyEntity> existingCrList = null;
        if (!isNewEntity && existingEntity != null) {
            existingPrefList = existingEntity.getComputePreferences();
            existingBqList = existingEntity.getBatchQueueResourcePolicies();
            existingCrList = existingEntity.getComputeResourcePolicies();
        }
        GroupResourceProfileEntity groupResourceProfileEntity;
        if (isNewEntity) {
            groupResourceProfileEntity = mapper.map(updatedGroupResourceProfile, GroupResourceProfileEntity.class);
        } else {
            // Update existing entity - preserve lists to avoid orphan deletion
            existingEntity.setComputePreferences(null);
            existingEntity.setBatchQueueResourcePolicies(null);
            existingEntity.setComputeResourcePolicies(null);
            mapper.map(updatedGroupResourceProfile, existingEntity);
            groupResourceProfileEntity = existingEntity;
            // Restore lists
            groupResourceProfileEntity.setComputePreferences(existingPrefList);
            groupResourceProfileEntity.setBatchQueueResourcePolicies(existingBqList);
            groupResourceProfileEntity.setComputeResourcePolicies(existingCrList);
        }
        // Restore on model
        updatedGroupResourceProfile.setComputePreferences(computePreferences);
        updatedGroupResourceProfile.setBatchQueueResourcePolicies(batchQueueResourcePolicies);
        updatedGroupResourceProfile.setComputeResourcePolicies(computeResourcePolicies);
        // Ensure creationTime is preserved (updatable=false should prevent update, but set it explicitly)
        if (!isNewEntity && existingEntity.getCreationTime() != null) {
            groupResourceProfileEntity.setCreationTime(existingEntity.getCreationTime());
        }
        // Manually map computePreferences using existing entities or create new ones
        // Use existing list to avoid orphan deletion issues
        if (computePreferences != null && !computePreferences.isEmpty()) {
            List<GroupComputeResourcePrefEntity> prefList = groupResourceProfileEntity.getComputePreferences();
            if (prefList == null) {
                prefList = new ArrayList<>();
                groupResourceProfileEntity.setComputePreferences(prefList);
            }
            Map<GroupComputeResourcePrefPK, GroupComputeResourcePrefEntity> existingPrefs = new HashMap<>();
            for (GroupComputeResourcePrefEntity existing : prefList) {
                GroupComputeResourcePrefPK pk = new GroupComputeResourcePrefPK();
                pk.setComputeResourceId(existing.getComputeResourceId());
                pk.setGroupResourceProfileId(existing.getGroupResourceProfileId());
                existingPrefs.put(pk, existing);
            }
            // Clear and rebuild the list
            prefList.clear();
            for (GroupComputeResourcePreference pref : computePreferences) {
                GroupComputeResourcePrefPK pk = new GroupComputeResourcePrefPK();
                pk.setComputeResourceId(pref.getComputeResourceId());
                pk.setGroupResourceProfileId(updatedGroupResourceProfile.getGroupResourceProfileId());
                GroupComputeResourcePrefEntity prefEntity = existingPrefs.get(pk);
                if (prefEntity == null) {
                    // Create new entity based on resource type
                    ComputeResourceType resourceType = pref.getResourceType();
                    if (resourceType == ComputeResourceType.AWS) {
                        prefEntity = new AWSGroupComputeResourcePrefEntity();
                    } else {
                        prefEntity = new SlurmGroupComputeResourcePrefEntity();
                    }
                    mapper.map(pref, prefEntity);
                } else {
                    // Update existing entity
                    mapper.map(pref, prefEntity);
                }
                prefEntity.setGroupResourceProfile(groupResourceProfileEntity);
                // Ensure groupResourceProfileId is set from the relationship (it's part of @Id)
                prefEntity.setGroupResourceProfileId(updatedGroupResourceProfile.getGroupResourceProfileId());
                prefList.add(prefEntity);
            }
        } else {
            if (groupResourceProfileEntity.getComputePreferences() != null) {
                groupResourceProfileEntity.getComputePreferences().clear();
            } else {
                groupResourceProfileEntity.setComputePreferences(new ArrayList<>());
            }
        }
        patchComputePrefEntities(groupResourceProfileEntity, updatedGroupResourceProfile);
        // Manually map BatchQueueResourcePolicies to avoid duplicate entity issues
        // Use existing list to avoid orphan deletion issues
        if (batchQueueResourcePolicies != null) {
            List<BatchQueueResourcePolicyEntity> bqList = groupResourceProfileEntity.getBatchQueueResourcePolicies();
            if (bqList == null) {
                bqList = new ArrayList<>();
                groupResourceProfileEntity.setBatchQueueResourcePolicies(bqList);
            }
            Map<String, BatchQueueResourcePolicyEntity> existingBqPolicies = new HashMap<>();
            for (BatchQueueResourcePolicyEntity existing : bqList) {
                existingBqPolicies.put(existing.getResourcePolicyId(), existing);
            }
            // Clear and rebuild the list
            bqList.clear();
            for (BatchQueueResourcePolicy bqPolicy : batchQueueResourcePolicies) {
                BatchQueueResourcePolicyEntity bqEntity = existingBqPolicies.get(bqPolicy.getResourcePolicyId());
                if (bqEntity == null) {
                    bqEntity = mapper.map(bqPolicy, BatchQueueResourcePolicyEntity.class);
                } else {
                    mapper.map(bqPolicy, bqEntity);
                }
                bqEntity.setGroupResourceProfile(groupResourceProfileEntity);
                bqList.add(bqEntity);
            }
        } else {
            if (groupResourceProfileEntity.getBatchQueueResourcePolicies() != null) {
                groupResourceProfileEntity.getBatchQueueResourcePolicies().clear();
            } else {
                groupResourceProfileEntity.setBatchQueueResourcePolicies(new ArrayList<>());
            }
        }
        // Manually map ComputeResourcePolicies to avoid duplicate entity issues
        // Use existing list to avoid orphan deletion issues
        if (computeResourcePolicies != null) {
            List<ComputeResourcePolicyEntity> crList = groupResourceProfileEntity.getComputeResourcePolicies();
            if (crList == null) {
                crList = new ArrayList<>();
                groupResourceProfileEntity.setComputeResourcePolicies(crList);
            }
            Map<String, ComputeResourcePolicyEntity> existingCrPolicies = new HashMap<>();
            for (ComputeResourcePolicyEntity existing : crList) {
                existingCrPolicies.put(existing.getResourcePolicyId(), existing);
            }
            // Clear and rebuild the list
            crList.clear();
            for (ComputeResourcePolicy crPolicy : computeResourcePolicies) {
                ComputeResourcePolicyEntity crEntity = existingCrPolicies.get(crPolicy.getResourcePolicyId());
                if (crEntity == null) {
                    crEntity = mapper.map(crPolicy, ComputeResourcePolicyEntity.class);
                } else {
                    mapper.map(crPolicy, crEntity);
                }
                crEntity.setGroupResourceProfile(groupResourceProfileEntity);
                crList.add(crEntity);
            }
        } else {
            if (groupResourceProfileEntity.getComputeResourcePolicies() != null) {
                groupResourceProfileEntity.getComputeResourcePolicies().clear();
            } else {
                groupResourceProfileEntity.setComputeResourcePolicies(new ArrayList<>());
            }
        }
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
        // Ensure computeResourceId is set on BatchQueueResourcePolicyEntity and ComputeResourcePolicyEntity
        // (even though marked as insertable=false, we need to set it for the constraint)
        if (groupResourceProfileEntity.getBatchQueueResourcePolicies() != null) {
            for (BatchQueueResourcePolicyEntity bqPolicy : groupResourceProfileEntity.getBatchQueueResourcePolicies()) {
                bqPolicy.setGroupResourceProfile(groupResourceProfileEntity);
                // computeResourceId is marked insertable=false, but we need to set it for the constraint
                // The value should already be set from Dozer mapping, but ensure it's not null
                if (bqPolicy.getComputeResourceId() == null) {
                    // Try to get it from the model if available
                    // This is a workaround for the insertable=false constraint
                }
            }
        }
        if (groupResourceProfileEntity.getComputeResourcePolicies() != null) {
            for (ComputeResourcePolicyEntity crPolicy : groupResourceProfileEntity.getComputeResourcePolicies()) {
                crPolicy.setGroupResourceProfile(groupResourceProfileEntity);
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
            if (sourcePref == null || sourcePref.getSpecificPreferences() == null) {
                continue;
            }
            if (prefEntity instanceof SlurmGroupComputeResourcePrefEntity slurmEntity) {
                if (sourcePref.getSpecificPreferences().isSlurm()) {
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
                if (sourcePref.getSpecificPreferences().isAws()) {
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
        GroupResourceProfile groupResourceProfile = mapper.map(entity, GroupResourceProfile.class);
        // Ensure computePreferences is initialized
        if (groupResourceProfile.getComputePreferences() == null) {
            groupResourceProfile.setComputePreferences(new ArrayList<>());
        }

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
        return computeResourcePolicyRepository
                .findById(resourcePolicyId)
                .map(entity -> mapper.map(entity, ComputeResourcePolicy.class))
                .orElse(null);
    }

    public BatchQueueResourcePolicy getBatchQueueResourcePolicy(String resourcePolicyId) {
        return batchQueuePolicyRepository
                .findById(resourcePolicyId)
                .map(entity -> mapper.map(entity, BatchQueueResourcePolicy.class))
                .orElse(null);
    }

    public List<GroupComputeResourcePreference> getAllGroupComputeResourcePreferences(String groupResourceProfileId) {
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
        List<BatchQueueResourcePolicyEntity> entities =
                batchQueuePolicyRepository.findByGroupResourceProfileId(groupResourceProfileId);
        return entities.stream()
                .map(entity -> mapper.map(entity, BatchQueueResourcePolicy.class))
                .collect(Collectors.toList());
    }

    public List<ComputeResourcePolicy> getAllGroupComputeResourcePolicies(String groupResourceProfileId) {
        List<ComputeResourcePolicyEntity> entities =
                computeResourcePolicyRepository.findByGroupResourceProfileId(groupResourceProfileId);
        return entities.stream()
                .map(entity -> mapper.map(entity, ComputeResourcePolicy.class))
                .collect(Collectors.toList());
    }
}
