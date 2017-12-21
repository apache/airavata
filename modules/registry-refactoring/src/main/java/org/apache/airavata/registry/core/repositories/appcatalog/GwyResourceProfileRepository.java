package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.appcatalog.gatewayprofile.StoragePreference;
import org.apache.airavata.registry.core.entities.appcatalog.ComputeResourcePreferencePK;
import org.apache.airavata.registry.core.entities.appcatalog.StoragePreferencePK;
import org.apache.airavata.registry.core.entities.appcatalog.GatewayProfileEntity;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.core.utils.QueryConstants;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GwyResourceProfileRepository extends AppCatAbstractRepository<GatewayResourceProfile, GatewayProfileEntity, String>{

    private final static Logger logger = LoggerFactory.getLogger(GwyResourceProfileRepository.class);

    public GwyResourceProfileRepository() {
        super(GatewayResourceProfile.class, GatewayProfileEntity.class);
    }

    public String addGatewayResourceProfile(GatewayResourceProfile gatewayResourceProfile) {

        return updateGatewayResourceProfile(gatewayResourceProfile);
    }

    public String updateGatewayResourceProfile(GatewayResourceProfile gatewayResourceProfile) {
        String gatewayId = gatewayResourceProfile.getGatewayID();
        Mapper mapper = ObjectMapperSingleton.getInstance();
        GatewayProfileEntity gatewayProfileEntity = mapper.map(gatewayResourceProfile, GatewayProfileEntity.class);
        if (get(gatewayId) != null) {
            gatewayProfileEntity.setUpdateTime(AiravataUtils.getCurrentTimestamp());
        }
        else {
            gatewayProfileEntity.setCreationTime(AiravataUtils.getCurrentTimestamp());
        }
        GatewayProfileEntity persistedCopy = execute(entityManager -> entityManager.merge(gatewayProfileEntity));

        List<ComputeResourcePreference> computeResourcePreferences = gatewayResourceProfile.getComputeResourcePreferences();
        if (computeResourcePreferences != null && !computeResourcePreferences.isEmpty()) {
            for (ComputeResourcePreference preference : computeResourcePreferences ) {
                (new ComputeResourceRepository()).create(preference);
            }
        }

        List<StoragePreference> dataStoragePreferences = gatewayResourceProfile.getStoragePreferences();
        if (dataStoragePreferences != null && !dataStoragePreferences.isEmpty()) {
            for (StoragePreference storagePreference : dataStoragePreferences) {
                (new StoragePrefRepository()).create(storagePreference);
            }
        }
        return persistedCopy.getGatewayId();
    }

    public GatewayResourceProfile getGatewayProfile(String gatewayId) {
        GatewayResourceProfile gatewayResourceProfile = get(gatewayId);
        gatewayResourceProfile.setComputeResourcePreferences(getAllComputeResourcePreferences(gatewayId));
        gatewayResourceProfile.setStoragePreferences(getAllStoragePreferences(gatewayId));
        return gatewayResourceProfile;
    }

    public List<GatewayResourceProfile> getAllGatewayProfiles() {

        List<GatewayResourceProfile> gwyResourceProfileList = new ArrayList<GatewayResourceProfile>();
        List<GatewayResourceProfile> gatewayResourceProfileList = select(QueryConstants.FIND_ALL_GATEWAY_PROFILES, 0);
        if (gatewayResourceProfileList != null && !gatewayResourceProfileList.isEmpty()) {
            for (GatewayResourceProfile gatewayResourceProfile: gatewayResourceProfileList) {
                gatewayResourceProfile.setComputeResourcePreferences(getAllComputeResourcePreferences(gatewayResourceProfile.getGatewayID()));
                gatewayResourceProfile.setStoragePreferences(getAllStoragePreferences(gatewayResourceProfile.getGatewayID()));
            }
        }
        return gatewayResourceProfileList;
    }

    public boolean removeComputeResourcePreferenceFromGateway(String gatewayId, String preferenceId) {
        ComputeResourcePreferencePK computeResourcePreferencePK = new ComputeResourcePreferencePK();
        computeResourcePreferencePK.setGatewayId(gatewayId);
        computeResourcePreferencePK.setComputeResourceId(preferenceId);
        (new ComputeResourceRepository()).delete(computeResourcePreferencePK);
        return true;
    }

    public boolean removeDataStoragePreferenceFromGateway(String gatewayId, String preferenceId) {
        StoragePreferencePK storagePreferencePK = new StoragePreferencePK();
        storagePreferencePK.setGatewayId(gatewayId);
        storagePreferencePK.setStorageResourceId(preferenceId);
        (new StoragePrefRepository()).delete(storagePreferencePK);
        return true;
    }

    public ComputeResourcePreference getComputeResourcePreference(String gatewayId, String hostId) {
        ComputeResourcePreferencePK computeResourcePreferencePK = new ComputeResourcePreferencePK();
        computeResourcePreferencePK.setGatewayId(gatewayId);
        computeResourcePreferencePK.setComputeResourceId(hostId);
        return (new ComputeResourceRepository()).get(computeResourcePreferencePK);
    }

    public StoragePreference getStoragePreference(String gatewayId, String storageId){
        StoragePreferencePK storagePreferencePK = new StoragePreferencePK();
        storagePreferencePK.setStorageResourceId(storageId);
        storagePreferencePK.setGatewayId(gatewayId);
        return (new StoragePrefRepository()).get(storagePreferencePK);
    }

    public List<ComputeResourcePreference> getAllComputeResourcePreferences(String gatewayId) {
        Map<String,Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.ComputeResourcePreference.GATEWAY_ID, gatewayId);
        return (new ComputeResourceRepository()).select(QueryConstants.FIND_ALL_COMPUTE_RESOURCE_PREFERENCES, -1, 0, queryParameters);
    }

    public List<StoragePreference> getAllStoragePreferences(String gatewayId) {
        Map<String,Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.StorageResourcePreference.GATEWAY_ID, gatewayId);
        return (new StoragePrefRepository()).select(QueryConstants.FIND_ALL_STORAGE_RESOURCE_PREFERENCES, -1, 0, queryParameters);
    }
}
