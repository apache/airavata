package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupResourceProfile;
import org.apache.airavata.registry.core.entities.appcatalog.*;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.core.utils.QueryConstants;
import org.dozer.Mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by skariyat on 2/8/18.
 */
public class GroupResourceProfileRepository extends AppCatAbstractRepository<GroupResourceProfile, GroupResourceProfileEntity, GroupResourceProfilePK> {

    public GroupResourceProfileRepository() {
        super(GroupResourceProfile.class, GroupResourceProfileEntity.class);
    }

    public void addGroupResourceProfile(GroupResourceProfile groupResourceProfile) {

        groupResourceProfile.setCreationTime(System.currentTimeMillis());
        updateGroupResourceProfile(groupResourceProfile);
    }

    public String updateGroupResourceProfile(GroupResourceProfile updatedGroupResourceProfile) {

        updatedGroupResourceProfile.setUpdatedTime(System.currentTimeMillis());
        GroupResourceProfile groupResourceProfile = update(updatedGroupResourceProfile);

        List<GroupComputeResourcePreference> computeResourcePreferences = updatedGroupResourceProfile.getComputePreferences();
        if (computeResourcePreferences != null && !computeResourcePreferences.isEmpty()) {
            addSSHProvConfig(computeResourcePreferences);
        }
        return groupResourceProfile.getGroupResourceProfileId();
    }

    public GroupResourceProfile getGroupResourceProfile(String gatewayId, String groupResourceProfileId) {
        GroupResourceProfilePK groupResourceProfilePK = new GroupResourceProfilePK();
        groupResourceProfilePK.setGatewayID(gatewayId);
        groupResourceProfilePK.setGroupResourceProfileId(groupResourceProfileId);
        GroupResourceProfile groupResourceProfile = get(groupResourceProfilePK);

        if (groupResourceProfile.getComputePreferences()!= null && !groupResourceProfile.getComputePreferences().isEmpty()){
            for (GroupComputeResourcePreference preference: groupResourceProfile.getComputePreferences()){
                preference.setSshAccountProvisionerConfig(getSSHProvConfig(gatewayId, preference.getComputeResourceId(), groupResourceProfileId));
            }
        }
        return groupResourceProfile;
    }

    public boolean removeGroupResourceProfile(String gatewayId, String groupResourceProfileId) {
        GroupResourceProfilePK groupResourceProfilePK = new GroupResourceProfilePK();
        groupResourceProfilePK.setGatewayID(gatewayId);
        groupResourceProfilePK.setGroupResourceProfileId(groupResourceProfileId);
        return delete(groupResourceProfilePK);
    }

    public boolean isGroupResourceProfileExists(String gatewayId, String groupResourceProfileId) {
        GroupResourceProfilePK groupResourceProfilePK = new GroupResourceProfilePK();
        groupResourceProfilePK.setGatewayID(gatewayId);
        groupResourceProfilePK.setGroupResourceProfileId(groupResourceProfileId);
        return isExists(groupResourceProfilePK);
    }

    public List<GroupResourceProfile> getAllGroupResourceProfiles(String gatewayId) {
        Map<String,Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.GroupResourceProfile.GATEWAY_ID, gatewayId);
        List<GroupResourceProfile> groupResourceProfileList = select(QueryConstants.FIND_ALL_GROUP_RESOURCE_PROFILES, -1, 0, queryParameters);
        if (groupResourceProfileList != null && !groupResourceProfileList.isEmpty()) {
            for (GroupResourceProfile groupResourceProfile: groupResourceProfileList) {
                if (groupResourceProfile.getComputePreferences() != null && !groupResourceProfile.getComputePreferences().isEmpty()) {
                    for (GroupComputeResourcePreference computeResourcePreference: groupResourceProfile.getComputePreferences()) {
                        computeResourcePreference.setSshAccountProvisionerConfig(getSSHProvConfig(gatewayId, computeResourcePreference.getComputeResourceId(), groupResourceProfile.getGroupResourceProfileId()));
                    }
                }
            }
        }
        return groupResourceProfileList;
    }

    public boolean removeGroupComputeResourcePreference(String gatewayId, String computeResourceId, String groupResourceProfileId) {
        GroupComputeResourcePrefPK groupComputeResourcePrefPK = new GroupComputeResourcePrefPK();
        groupComputeResourcePrefPK.setGatewayID(gatewayId);
        groupComputeResourcePrefPK.setComputeResourceId(computeResourceId);
        groupComputeResourcePrefPK.setGroupResourceProfileId(groupResourceProfileId);

        execute(entityManager -> {
            GroupComputeResourcePrefEntity groupComputeResourcePrefEntity = entityManager.find(GroupComputeResourcePrefEntity.class, groupComputeResourcePrefPK);
            entityManager.remove(groupComputeResourcePrefEntity);
            return groupComputeResourcePrefEntity;
        });
        return true;
    }

    public boolean removeComputeResourcePolicy(String resourcePolicyId, String computeResourceId, String groupResourceProfileId) {
        ComputeResourcePolicyPK computeResourcePolicyPK = new ComputeResourcePolicyPK();
        computeResourcePolicyPK.setComputeResourceId(computeResourceId);
        computeResourcePolicyPK.setGroupResourceProfileId(groupResourceProfileId);
        computeResourcePolicyPK.setResourcePolicyId(resourcePolicyId);

        execute(entityManager -> {
            ComputeResourcePolicyEntity computeResourcePolicyEntity = entityManager.find(ComputeResourcePolicyEntity.class, computeResourcePolicyPK);
            entityManager.remove(computeResourcePolicyEntity);
            return computeResourcePolicyEntity;
        });
        return true;
    }

    public boolean removeBatchQueueResourcePolicy(String resourcePolicyId, String computeResourceId, String groupResourceProfileId, String queueName) {
        BatchQueueResourcePolicyPK batchQueueResourcePolicyPK = new BatchQueueResourcePolicyPK();
        batchQueueResourcePolicyPK.setComputeResourceId(computeResourceId);
        batchQueueResourcePolicyPK.setGroupResourceProfileId(groupResourceProfileId);
        batchQueueResourcePolicyPK.setResourcePolicyId(resourcePolicyId);
        batchQueueResourcePolicyPK.setQueuename(queueName);

        execute(entityManager -> {
            BatchQueueResourcePolicyEntity batchQueueResourcePolicyEntity = entityManager.find(BatchQueueResourcePolicyEntity.class, batchQueueResourcePolicyPK);
            entityManager.remove(batchQueueResourcePolicyEntity);
            return batchQueueResourcePolicyEntity;
        });
        return true;
    }

    private void addSSHProvConfig(List<GroupComputeResourcePreference> computeResourcePreferences) {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        for (GroupComputeResourcePreference preference : computeResourcePreferences ) {
            if (preference.getSshAccountProvisionerConfig() != null && !preference.getSshAccountProvisionerConfig().isEmpty()){
                GroupComputeResourcePrefEntity computeResourcePreferenceEntity = mapper.map(preference, GroupComputeResourcePrefEntity.class);
                List<GroupSSHAccountProvisionerConfig> configurations = new ArrayList<>();
                for (String sshAccountProvisionerConfigName : preference.getSshAccountProvisionerConfig().keySet()) {
                    String value = preference.getSshAccountProvisionerConfig().get(sshAccountProvisionerConfigName);
                    configurations.add(new GroupSSHAccountProvisionerConfig(sshAccountProvisionerConfigName, value, computeResourcePreferenceEntity));
                }
                computeResourcePreferenceEntity.setGroupSSHAccountProvisionerConfigs(configurations);
                execute(entityManager -> entityManager.merge(computeResourcePreferenceEntity));
            }
        }
    }

    private Map<String,String> getSSHProvConfig(String gatewayId, String computeResourceId, String groupResourceProfileId) {
        GroupComputeResourcePrefPK groupComputeResourcePrefPK = new GroupComputeResourcePrefPK();
        groupComputeResourcePrefPK.setComputeResourceId(computeResourceId);
        groupComputeResourcePrefPK.setGatewayID(gatewayId);
        groupComputeResourcePrefPK.setGroupResourceProfileId(groupResourceProfileId);

        GroupComputeResourcePrefEntity computeResourcePreferenceEntity = execute(entityManager -> entityManager
                .find(GroupComputeResourcePrefEntity.class, groupComputeResourcePrefPK));

        if (computeResourcePreferenceEntity.getGroupSSHAccountProvisionerConfigs()!= null && !computeResourcePreferenceEntity.getGroupSSHAccountProvisionerConfigs().isEmpty()){
            Map<String,String> sshAccountProvisionerConfigurations = new HashMap<>();
            for (GroupSSHAccountProvisionerConfig config : computeResourcePreferenceEntity.getGroupSSHAccountProvisionerConfigs()){
                sshAccountProvisionerConfigurations.put(config.getConfigName(), config.getConfigValue());
            }
            return sshAccountProvisionerConfigurations;
        }
        return null;
    }

}
