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
package org.apache.airavata.compute.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.airavata.compute.mapper.ComputeMapper;
import org.apache.airavata.compute.model.AWSGroupComputeResourcePrefEntity;
import org.apache.airavata.compute.model.ComputeResourceReservationEntity;
import org.apache.airavata.compute.model.GroupComputeResourcePrefEntity;
import org.apache.airavata.compute.model.GroupComputeResourcePrefPK;
import org.apache.airavata.compute.model.GroupResourceProfileEntity;
import org.apache.airavata.compute.model.SlurmGroupComputeResourcePrefEntity;
import org.apache.airavata.db.AbstractRepository;
import org.apache.airavata.db.DBConstants;
import org.apache.airavata.db.QueryConstants;
import org.apache.airavata.models.appcatalog.groupresourceprofile.AwsComputeResourcePreference;
import org.apache.airavata.models.appcatalog.groupresourceprofile.BatchQueueResourcePolicy;
import org.apache.airavata.models.appcatalog.groupresourceprofile.ComputeResourcePolicy;
import org.apache.airavata.models.appcatalog.groupresourceprofile.ComputeResourceReservation;
import org.apache.airavata.models.appcatalog.groupresourceprofile.EnvironmentSpecificPreferences;
import org.apache.airavata.models.appcatalog.groupresourceprofile.GroupAccountSSHProvisionerConfig;
import org.apache.airavata.models.appcatalog.groupresourceprofile.GroupComputeResourcePreference;
import org.apache.airavata.models.appcatalog.groupresourceprofile.GroupResourceProfile;
import org.apache.airavata.models.appcatalog.groupresourceprofile.ResourceType;
import org.apache.airavata.models.appcatalog.groupresourceprofile.SlurmComputeResourcePreference;
import org.apache.airavata.common.AiravataUtils;
import org.springframework.stereotype.Component;

/**
 * Created by skariyat on 2/8/18.
 */
@Component
public class GroupResourceProfileRepository
        extends AbstractRepository<GroupResourceProfile, GroupResourceProfileEntity, String> {

    public GroupResourceProfileRepository() {
        super(GroupResourceProfile.class, GroupResourceProfileEntity.class);
    }

    @Override
    protected GroupResourceProfile toModel(GroupResourceProfileEntity entity) {
        return ComputeMapper.INSTANCE.groupResourceProfileToModel(entity);
    }

    @Override
    protected GroupResourceProfileEntity toEntity(GroupResourceProfile model) {
        return ComputeMapper.INSTANCE.groupResourceProfileToEntity(model);
    }

    public String addGroupResourceProfile(GroupResourceProfile groupResourceProfile) {

        final String groupResourceProfileId = UUID.randomUUID().toString();
        groupResourceProfile = groupResourceProfile.toBuilder()
                .setGroupResourceProfileId(groupResourceProfileId)
                .setCreationTime(System.currentTimeMillis())
                .build();
        groupResourceProfile = updateChildren(groupResourceProfile, groupResourceProfileId);
        return updateGroupResourceProfile(groupResourceProfile);
    }

    private GroupResourceProfile updateChildren(
            GroupResourceProfile groupResourceProfile, String groupResourceProfileId) {
        GroupResourceProfile.Builder profileBuilder = groupResourceProfile.toBuilder();

        if (groupResourceProfile.computePreferences() != null) {
            List<GroupComputeResourcePreference> updatedPrefs = new ArrayList<>();
            for (GroupComputeResourcePreference gcrPref : groupResourceProfile.computePreferences()) {
                GroupComputeResourcePreference.Builder prefBuilder = gcrPref.toBuilder()
                        .setGroupResourceProfileId(groupResourceProfileId);

                if (gcrPref.resourceType() == ResourceType.SLURM
                        && gcrPref.hasSpecificPreferences()
                        && gcrPref.specificPreferences() instanceof EnvironmentSpecificPreferences.Slurm slurmVariant) {

                    SlurmComputeResourcePreference slurm = slurmVariant.slurm();
                    SlurmComputeResourcePreference.Builder slurmBuilder = slurm.toBuilder();

                    // update SSH provisioner configs
                    if (slurm.groupSshAccountProvisionerConfigs() != null) {
                        List<GroupAccountSSHProvisionerConfig> updatedConfigs = new ArrayList<>();
                        for (GroupAccountSSHProvisionerConfig gssh : slurm.groupSshAccountProvisionerConfigs()) {
                            updatedConfigs.add(gssh.toBuilder()
                                    .setGroupResourceProfileId(groupResourceProfileId)
                                    .build());
                        }
                        slurmBuilder
                                .clearGroupSshAccountProvisionerConfigs()
                                .addAllGroupSshAccountProvisionerConfigs(updatedConfigs);
                    }

                    // update reservations
                    if (slurm.reservations() != null) {
                        List<ComputeResourceReservation> updatedRes = new ArrayList<>();
                        for (ComputeResourceReservation res : slurm.reservations()) {
                            if (res.reservationId().trim().isEmpty()
                                    || res.reservationId().equals("DO_NOT_SET_AT_CLIENTS")) {
                                updatedRes.add(res.toBuilder()
                                        .setReservationId(AiravataUtils.getId(res.reservationName()))
                                        .build());
                            } else {
                                updatedRes.add(res);
                            }
                        }
                        slurmBuilder.clearReservations().addAllReservations(updatedRes);
                    }

                    prefBuilder.setSpecificPreferences(new EnvironmentSpecificPreferences.Slurm(slurmBuilder.build()));
                }
                updatedPrefs.add(prefBuilder.build());
            }
            profileBuilder.clearComputePreferences().addAllComputePreferences(updatedPrefs);
        }

        if (groupResourceProfile.batchQueueResourcePolicies() != null) {
            List<BatchQueueResourcePolicy> updatedBqs = new ArrayList<>();
            for (BatchQueueResourcePolicy bq : groupResourceProfile.batchQueueResourcePolicies()) {
                BatchQueueResourcePolicy.Builder bqBuilder = bq.toBuilder();
                if (bq.resourcePolicyId().trim().isEmpty()
                        || bq.resourcePolicyId().equals("DO_NOT_SET_AT_CLIENTS")) {
                    bqBuilder.setResourcePolicyId(UUID.randomUUID().toString());
                }
                bqBuilder.setGroupResourceProfileId(groupResourceProfileId);
                updatedBqs.add(bqBuilder.build());
            }
            profileBuilder.clearBatchQueueResourcePolicies().addAllBatchQueueResourcePolicies(updatedBqs);
        }

        if (groupResourceProfile.computeResourcePolicies() != null) {
            List<ComputeResourcePolicy> updatedCrs = new ArrayList<>();
            for (ComputeResourcePolicy cr : groupResourceProfile.computeResourcePolicies()) {
                ComputeResourcePolicy.Builder crBuilder = cr.toBuilder();
                if (cr.resourcePolicyId().trim().isEmpty()
                        || cr.resourcePolicyId().equals("DO_NOT_SET_AT_CLIENTS")) {
                    crBuilder.setResourcePolicyId(UUID.randomUUID().toString());
                }
                crBuilder.setGroupResourceProfileId(groupResourceProfileId);
                updatedCrs.add(crBuilder.build());
            }
            profileBuilder.clearComputeResourcePolicies().addAllComputeResourcePolicies(updatedCrs);
        }

        return profileBuilder.build();
    }

    public String updateGroupResourceProfile(GroupResourceProfile updatedGroupResourceProfile) {

        updatedGroupResourceProfile = updatedGroupResourceProfile.toBuilder()
                .setUpdatedTime(System.currentTimeMillis())
                .build();
        updatedGroupResourceProfile = updateChildren(updatedGroupResourceProfile,
                updatedGroupResourceProfile.groupResourceProfileId());
        GroupResourceProfileEntity groupResourceProfileEntity = mapToEntity(updatedGroupResourceProfile);
        patchComputePrefEntities(groupResourceProfileEntity, updatedGroupResourceProfile);
        updateChildrenEntities(groupResourceProfileEntity);
        GroupResourceProfile groupResourceProfile = mergeEntity(groupResourceProfileEntity);
        return groupResourceProfile.groupResourceProfileId();
    }

    private void updateChildrenEntities(GroupResourceProfileEntity groupResourceProfileEntity) {
        if (groupResourceProfileEntity.getComputePreferences() != null) {
            for (GroupComputeResourcePrefEntity gcrPref : groupResourceProfileEntity.getComputePreferences()) {
                // For some reason next line is needed to get OpenJPA to persist
                // GroupResourceProfileEntity before GroupComputeResourcePrefEntity
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
        if (destEntity.getComputePreferences() == null || sourceModel.computePreferences() == null) {
            return;
        }
        Map<String, GroupComputeResourcePreference> sourcePrefs = new HashMap<>();
        for (GroupComputeResourcePreference pref : sourceModel.computePreferences()) {
            sourcePrefs.put(pref.computeResourceId(), pref);
        }

        for (GroupComputeResourcePrefEntity prefEntity : destEntity.getComputePreferences()) {
            GroupComputeResourcePreference sourcePref = sourcePrefs.get(prefEntity.getComputeResourceId());
            if (sourcePref == null || !sourcePref.hasSpecificPreferences()) {
                continue;
            }
            if (prefEntity instanceof SlurmGroupComputeResourcePrefEntity slurmEntity) {
                if (sourcePref.specificPreferences() instanceof EnvironmentSpecificPreferences.Slurm slurmVariant) {
                    SlurmComputeResourcePreference slurm = slurmVariant.slurm();
                    slurmEntity.setAllocationProjectNumber(slurm.allocationProjectNumber());
                    slurmEntity.setPreferredBatchQueue(slurm.preferredBatchQueue());
                    slurmEntity.setQualityOfService(slurm.qualityOfService());
                    slurmEntity.setUsageReportingGatewayId(slurm.usageReportingGatewayId());
                    slurmEntity.setSshAccountProvisioner(slurm.sshAccountProvisioner());
                    slurmEntity.setSshAccountProvisionerAdditionalInfo(slurm.sshAccountProvisionerAdditionalInfo());
                    if (!slurm.groupSshAccountProvisionerConfigs().isEmpty()) {
                        List<Map<String, Object>> cfgList = new ArrayList<>();
                        for (GroupAccountSSHProvisionerConfig cfg : slurm.groupSshAccountProvisionerConfigs()) {
                            Map<String, Object> m = new LinkedHashMap<>();
                            m.put("resourceId", cfg.resourceId());
                            m.put("groupResourceProfileId", cfg.groupResourceProfileId());
                            m.put("configName", cfg.configName());
                            m.put("configValue", cfg.configValue());
                            cfgList.add(m);
                        }
                        slurmEntity.setGroupSSHAccountProvisionerConfigs(cfgList);
                    } else {
                        slurmEntity.setGroupSSHAccountProvisionerConfigs(null);
                    }
                }
            } else if (prefEntity instanceof AWSGroupComputeResourcePrefEntity awsEntity) {
                if (sourcePref.specificPreferences() instanceof EnvironmentSpecificPreferences.Aws awsVariant) {
                    AwsComputeResourcePreference aws = awsVariant.aws();
                    awsEntity.setRegion(aws.region());
                    awsEntity.setPreferredAmiId(aws.preferredAmiId());
                    awsEntity.setPreferredInstanceType(aws.preferredInstanceType());
                }
            }
        }
    }

    public GroupResourceProfile getGroupResourceProfile(String groupResourceProfileId) {
        GroupResourceProfile groupResourceProfile = get(groupResourceProfileId);

        GrpComputePrefRepository prefRepo = new GrpComputePrefRepository();
        List<GroupComputeResourcePreference> decoratedPrefs = new ArrayList<>();
        for (GroupComputeResourcePreference raw : groupResourceProfile.computePreferences()) {
            GroupComputeResourcePrefPK pk = new GroupComputeResourcePrefPK();
            pk.setComputeResourceId(raw.computeResourceId());
            pk.setGroupResourceProfileId(raw.groupResourceProfileId());
            decoratedPrefs.add(prefRepo.get(pk));
        }
        groupResourceProfile = groupResourceProfile.toBuilder()
                .clearComputePreferences()
                .addAllComputePreferences(decoratedPrefs)
                .build();

        return groupResourceProfile;
    }

    public boolean removeGroupResourceProfile(String groupResourceProfileId) {
        return delete(groupResourceProfileId);
    }

    public boolean isGroupResourceProfileExists(String groupResourceProfileId) {
        return isExists(groupResourceProfileId);
    }

    public List<GroupResourceProfile> getAllGroupResourceProfiles(
            String gatewayId, List<String> accessibleGroupResProfileIds) {
        if (accessibleGroupResProfileIds != null && !accessibleGroupResProfileIds.isEmpty()) {
            List<GroupResourceProfile> profiles = select(
                    QueryConstants.FIND_ACCESSIBLE_GROUP_RESOURCE_PROFILES,
                    -1,
                    0,
                    Map.of(
                            DBConstants.GroupResourceProfile.GATEWAY_ID,
                            gatewayId,
                            DBConstants.GroupResourceProfile.ACCESSIBLE_GROUP_RESOURCE_IDS,
                            accessibleGroupResProfileIds));

            GrpComputePrefRepository prefRepo = new GrpComputePrefRepository();
            List<GroupResourceProfile> decoratedProfiles = new ArrayList<>();
            for (GroupResourceProfile profile : profiles) {
                List<GroupComputeResourcePreference> decoratedPrefs = new ArrayList<>();

                for (GroupComputeResourcePreference rawPref : profile.computePreferences()) {
                    GroupComputeResourcePrefPK pk = new GroupComputeResourcePrefPK();
                    pk.setComputeResourceId(rawPref.computeResourceId());
                    pk.setGroupResourceProfileId(rawPref.groupResourceProfileId());

                    GroupComputeResourcePreference fullPref = prefRepo.get(pk);
                    decoratedPrefs.add(fullPref);
                }

                decoratedProfiles.add(profile.toBuilder()
                        .clearComputePreferences()
                        .addAllComputePreferences(decoratedPrefs)
                        .build());
            }
            return decoratedProfiles;

        } else {
            return Collections.emptyList();
        }
    }

    public boolean removeGroupComputeResourcePreference(String computeResourceId, String groupResourceProfileId) {
        GroupComputeResourcePrefPK groupComputeResourcePrefPK = new GroupComputeResourcePrefPK();
        groupComputeResourcePrefPK.setComputeResourceId(computeResourceId);
        groupComputeResourcePrefPK.setGroupResourceProfileId(groupResourceProfileId);

        return (new GrpComputePrefRepository().delete(groupComputeResourcePrefPK));
    }

    public boolean removeComputeResourcePolicy(String resourcePolicyId) {
        return (new ComputeResourcePolicyRepository().delete(resourcePolicyId));
    }

    public boolean removeBatchQueueResourcePolicy(String resourcePolicyId) {
        return (new BatchQueuePolicyRepository().delete(resourcePolicyId));
    }

    public GroupComputeResourcePreference getGroupComputeResourcePreference(
            String computeResourceId, String groupResourceProfileId) {
        GroupComputeResourcePrefPK groupComputeResourcePrefPK = new GroupComputeResourcePrefPK();
        groupComputeResourcePrefPK.setGroupResourceProfileId(groupResourceProfileId);
        groupComputeResourcePrefPK.setComputeResourceId(computeResourceId);

        return (new GrpComputePrefRepository().get(groupComputeResourcePrefPK));
    }

    public boolean isGroupComputeResourcePreferenceExists(String computeResourceId, String groupResourceProfileId) {
        GroupComputeResourcePrefPK groupComputeResourcePrefPK = new GroupComputeResourcePrefPK();
        groupComputeResourcePrefPK.setGroupResourceProfileId(groupResourceProfileId);
        groupComputeResourcePrefPK.setComputeResourceId(computeResourceId);

        return (new GrpComputePrefRepository().isExists(groupComputeResourcePrefPK));
    }

    public ComputeResourcePolicy getComputeResourcePolicy(String resourcePolicyId) {
        return (new ComputeResourcePolicyRepository().get(resourcePolicyId));
    }

    public BatchQueueResourcePolicy getBatchQueueResourcePolicy(String resourcePolicyId) {
        return (new BatchQueuePolicyRepository().get(resourcePolicyId));
    }

    public List<GroupComputeResourcePreference> getAllGroupComputeResourcePreferences(String groupResourceProfileId) {
        List<GroupComputeResourcePreference> rawPrefs = (new GrpComputePrefRepository()
                .select(
                        QueryConstants.FIND_ALL_GROUP_COMPUTE_PREFERENCES,
                        -1,
                        0,
                        Map.of(DBConstants.GroupResourceProfile.GROUP_RESOURCE_PROFILE_ID, groupResourceProfileId)));

        GrpComputePrefRepository prefRepo = new GrpComputePrefRepository();
        List<GroupComputeResourcePreference> decorated = new ArrayList<>();
        for (GroupComputeResourcePreference raw : rawPrefs) {
            GroupComputeResourcePrefPK pk = new GroupComputeResourcePrefPK();
            pk.setComputeResourceId(raw.computeResourceId());
            pk.setGroupResourceProfileId(raw.groupResourceProfileId());
            // this .get(...) will load the entity, detect SLURM, set resourceType, and
            // populate the specificPreferences
            // union
            GroupComputeResourcePreference full = prefRepo.get(pk);
            decorated.add(full);
        }

        return decorated;
    }

    public List<BatchQueueResourcePolicy> getAllGroupBatchQueueResourcePolicies(String groupResourceProfileId) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.GroupResourceProfile.GROUP_RESOURCE_PROFILE_ID, groupResourceProfileId);
        return (new BatchQueuePolicyRepository()
                .select(QueryConstants.FIND_ALL_GROUP_BATCH_QUEUE_RESOURCE_POLICY, -1, 0, queryParameters));
    }

    public List<ComputeResourcePolicy> getAllGroupComputeResourcePolicies(String groupResourceProfileId) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.GroupResourceProfile.GROUP_RESOURCE_PROFILE_ID, groupResourceProfileId);
        return (new ComputeResourcePolicyRepository()
                .select(QueryConstants.FIND_ALL_GROUP_COMPUTE_RESOURCE_POLICY, -1, 0, queryParameters));
    }
}
