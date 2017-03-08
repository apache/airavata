package org.apache.airavata.service.profile.tenant.core.repositories;

import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.service.profile.commons.repositories.AbstractRepository;
import org.apache.airavata.service.profile.commons.tenant.entities.GatewayEntity;
import org.apache.airavata.service.profile.commons.utils.QueryConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by goshenoy on 3/8/17.
 */
public class TenantProfileRepository extends AbstractRepository<Gateway, GatewayEntity, String> {

    private final static Logger logger = LoggerFactory.getLogger(TenantProfileRepository.class);

    public TenantProfileRepository(Class<Gateway> thriftGenericClass, Class<GatewayEntity> dbEntityGenericClass) {
        super(thriftGenericClass, dbEntityGenericClass);
    }

    public Gateway getGateway (String gatewayId) throws Exception {
        Gateway gateway = null;
        try {
            Map<String, Object> queryParam = new HashMap<String, Object>();
            queryParam.put(Gateway._Fields.GATEWAY_ID.getFieldName(), gatewayId);
            List<Gateway> gatewayList = select(QueryConstants.FIND_GATEWAY_BY_ID, 1, 0, queryParam);
            if (!gatewayList.isEmpty()) {
                gateway = gatewayList.get(0);
            }
        } catch (Exception ex) {
            logger.error("Error while getting gateway, reason: " + ex.getMessage(), ex);
            throw ex;
        }
        return gateway;
    }

    public List<Gateway> getAllGateways () throws Exception {
        try {
            List<Gateway> gatewayList = select(QueryConstants.GET_ALL_GATEWAYS);
            return gatewayList;
        } catch (Exception e){
            logger.error("Error while getting all the gateways, reason: ", e);
            throw e;
        }
    }
}
