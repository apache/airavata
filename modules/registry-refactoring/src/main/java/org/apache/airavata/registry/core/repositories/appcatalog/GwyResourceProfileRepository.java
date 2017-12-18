package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.registry.core.entities.appcatalog.GatewayProfileEntity;
import org.apache.airavata.registry.core.repositories.AbstractRepository;
import org.apache.airavata.registry.core.utils.JPAUtils;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class GwyResourceProfileRepository extends AbstractRepository<GatewayResourceProfile, GatewayProfileEntity, String>{

    private final static Logger logger = LoggerFactory.getLogger(GwyResourceProfileRepository.class);

    public GwyResourceProfileRepository(Class<GatewayResourceProfile> thriftGenericClass, Class<GatewayProfileEntity> dbEntityGenericClass) {
        super(thriftGenericClass, dbEntityGenericClass);
    }

    public String addGatewayResourceProfile(GatewayResourceProfile gatewayResourceProfile) {
            return updateGatewayResourceProfile(gatewayResourceProfile);
    }

    public String updateGatewayResourceProfile(GatewayResourceProfile gatewayResourceProfile) {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        GatewayProfileEntity gatewayProfileEntity = mapper.map(gatewayResourceProfile, GatewayProfileEntity.class);
        gatewayProfileEntity.setUpdateTime(AiravataUtils.getCurrentTimestamp());
        GatewayProfileEntity persistedCopy = JPAUtils.execute(entityManager -> entityManager.merge(gatewayProfileEntity));

        //TODO ComputeResourcePreference
        //TODO StoragePreference

        return persistedCopy.getGatewayId();

    }

    public GatewayResourceProfile getGatewayProfile(String gatewayId) {
        //TODO ComputeResourcePreference
        //TODO StoragePreference
    }

    public boolean removeGatewayResourceProfile(String gatewayId) {
        // TODO - Call from handler
    }

    public boolean isGatewayResourceProfileExists(String gatewayId) {
        // TODO - Call isExists from handler
    }

    public List<String> getGatewayProfileIds(String gatewayName) {
        // TODO - Not used anywhere (dev list??)
    }

    public List<GatewayResourceProfile> getAllGatewayProfiles() {

        String queryString = "SELECT * FROM GATEWAY_PROFILE";

        List<GatewayResourceProfile> gatewayResourceProfileList = select(queryString,-1, 0);

        //TODO ComputeResourcePreference
        //TODO StoragePreference
    }




}
