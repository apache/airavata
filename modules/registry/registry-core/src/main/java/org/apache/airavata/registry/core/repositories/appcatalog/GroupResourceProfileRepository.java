/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.model.appcatalog.groupresourceprofile.BatchQueueResourcePolicy;
import org.apache.airavata.model.appcatalog.groupresourceprofile.ComputeResourcePolicy;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupResourceProfile;
import org.apache.airavata.model.commons.airavata_commonsConstants;
import org.apache.airavata.registry.core.entities.appcatalog.ComputeResourceReservationEntity;
import org.apache.airavata.registry.core.entities.appcatalog.GroupComputeResourcePrefEntity;
import org.apache.airavata.registry.core.entities.appcatalog.GroupComputeResourcePrefPK;
import org.apache.airavata.registry.core.entities.appcatalog.GroupResourceProfileEntity;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.core.utils.QueryConstants;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by skariyat on 2/8/18.
 */
public class GroupResourceProfileRepository extends AppCatAbstractRepository<GroupResourceProfile, GroupResourceProfileEntity, String> {

    public GroupResourceProfileRepository() {
        super(GroupResourceProfile.class, GroupResourceProfileEntity.class);
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
            for (GroupComputeResourcePreference groupComputeResourcePreference: groupResourceProfile.getComputePreferences()) {
                groupComputeResourcePreference.setGroupResourceProfileId(groupResourceProfileId);
                if (groupComputeResourcePreference.getGroupSSHAccountProvisionerConfigs() != null) {
                    groupComputeResourcePreference.getGroupSSHAccountProvisionerConfigs().forEach(gssh -> gssh.setGroupResourceProfileId(groupResourceProfileId));
                }
                if (groupComputeResourcePreference.getReservations() != null) {
                    groupComputeResourcePreference.getReservations().forEach(reservation -> {
                        if (reservation.getReservationId().trim().isEmpty() || reservation.getReservationId().equals(airavata_commonsConstants.DEFAULT_ID)) {
                            reservation.setReservationId(AiravataUtils.getId(reservation.getReservationName()));
                        }
                    });
                }
            }
        }
        if (groupResourceProfile.getBatchQueueResourcePolicies() != null) {
            groupResourceProfile.getBatchQueueResourcePolicies().forEach(bq -> {
                if (bq.getResourcePolicyId().trim().isEmpty() || bq.getResourcePolicyId().equals(airavata_commonsConstants.DEFAULT_ID)) {
                    bq.setResourcePolicyId(UUID.randomUUID().toString());
                }
                bq.setGroupResourceProfileId(groupResourceProfileId);
            });
        }
        if (groupResourceProfile.getComputeResourcePolicies() != null) {
            groupResourceProfile.getComputeResourcePolicies().forEach(cr -> {
                if (cr.getResourcePolicyId().trim().isEmpty() || cr.getResourcePolicyId().equals(airavata_commonsConstants.DEFAULT_ID)) {
                    cr.setResourcePolicyId(UUID.randomUUID().toString());
                }
                cr.setGroupResourceProfileId(groupResourceProfileId);
            });
        }
    }

    public String updateGroupResourceProfile(GroupResourceProfile updatedGroupResourceProfile) {

        updatedGroupResourceProfile.setUpdatedTime(System.currentTimeMillis());
        updateChildren(updatedGroupResourceProfile, updatedGroupResourceProfile.getGroupResourceProfileId());
        GroupResourceProfileEntity groupResourceProfileEntity = mapToEntity(updatedGroupResourceProfile);
        updateChildrenEntities(groupResourceProfileEntity);
        GroupResourceProfile groupResourceProfile = mergeEntity(groupResourceProfileEntity);
        return groupResourceProfile.getGroupResourceProfileId();
    }

    private void updateChildrenEntities(GroupResourceProfileEntity groupResourceProfileEntity) {
        if (groupResourceProfileEntity.getComputePreferences() != null) {
            for (GroupComputeResourcePrefEntity groupComputeResourcePrefEntity : groupResourceProfileEntity.getComputePreferences()) {
                // For some reason next line is needed to get OpenJPA to persist
                // GroupResourceProfileEntity before GroupComputeResourcePrefEntity
                groupComputeResourcePrefEntity.setGroupResourceProfile(groupResourceProfileEntity);
                if (groupComputeResourcePrefEntity.getReservations() != null) {
                    for (ComputeResourceReservationEntity reservationEntity : groupComputeResourcePrefEntity.getReservations()) {
                        reservationEntity.setGroupComputeResourcePref(groupComputeResourcePrefEntity);
                    }
                }
            }
        }
    }

    public GroupResourceProfile getGroupResourceProfile(String groupResourceProfileId) {
        GroupResourceProfile groupResourceProfile = get(groupResourceProfileId);
        return groupResourceProfile;
    }

    public boolean removeGroupResourceProfile(String groupResourceProfileId) {
        return delete(groupResourceProfileId);
    }

    public boolean isGroupResourceProfileExists(String groupResourceProfileId) {
        return isExists(groupResourceProfileId);
    }

    public List<GroupResourceProfile> getAllGroupResourceProfiles(String gatewayId, List<String> accessibleGroupResProfileIds) {
        Map<String,Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.GroupResourceProfile.GATEWAY_ID, gatewayId);

        if (accessibleGroupResProfileIds != null && !accessibleGroupResProfileIds.isEmpty()) {
            queryParameters.put(DBConstants.GroupResourceProfile.ACCESSIBLE_GROUP_RESOURCE_IDS, accessibleGroupResProfileIds);
            return select(QueryConstants.FIND_ACCESSIBLE_GROUP_RESOURCE_PROFILES, -1, 0, queryParameters);
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

    public GroupComputeResourcePreference getGroupComputeResourcePreference(String computeResourceId, String groupResourceProfileId) {
        GroupComputeResourcePrefPK groupComputeResourcePrefPK = new GroupComputeResourcePrefPK();
        groupComputeResourcePrefPK.setGroupResourceProfileId(groupResourceProfileId);
        groupComputeResourcePrefPK.setComputeResourceId(computeResourceId);

        return (new GrpComputePrefRepository().get(groupComputeResourcePrefPK));
    }

    public ComputeResourcePolicy getComputeResourcePolicy(String resourcePolicyId) {
        return (new ComputeResourcePolicyRepository().get(resourcePolicyId));
    }

    public BatchQueueResourcePolicy getBatchQueueResourcePolicy(String resourcePolicyId) {
        return (new BatchQueuePolicyRepository().get(resourcePolicyId));
    }

    public List<GroupComputeResourcePreference> getAllGroupComputeResourcePreferences(String groupResourceProfileId) {
        Map<String,Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.GroupResourceProfile.GROUP_RESOURCE_PROFILE_ID, groupResourceProfileId);
        List<GroupComputeResourcePreference> groupComputeResourcePreferenceList = (new GrpComputePrefRepository().select(QueryConstants.FIND_ALL_GROUP_COMPUTE_PREFERENCES, -1, 0, queryParameters));

        return groupComputeResourcePreferenceList;
    }

    public List<BatchQueueResourcePolicy> getAllGroupBatchQueueResourcePolicies(String groupResourceProfileId) {
        Map<String,Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.GroupResourceProfile.GROUP_RESOURCE_PROFILE_ID, groupResourceProfileId);
        return (new BatchQueuePolicyRepository().select(QueryConstants.FIND_ALL_GROUP_BATCH_QUEUE_RESOURCE_POLICY, -1, 0, queryParameters));
    }

    public List<ComputeResourcePolicy> getAllGroupComputeResourcePolicies(String groupResourceProfileId) {
        Map<String,Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.GroupResourceProfile.GROUP_RESOURCE_PROFILE_ID, groupResourceProfileId);
        return (new ComputeResourcePolicyRepository().select(QueryConstants.FIND_ALL_GROUP_COMPUTE_RESOURCE_POLICY, -1, 0, queryParameters));
    }

}
