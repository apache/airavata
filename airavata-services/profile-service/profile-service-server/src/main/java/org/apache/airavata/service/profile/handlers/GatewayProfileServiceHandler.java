package org.apache.airavata.service.profile.handlers;

import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.service.profile.gateway.cpi.GatewayProfileService;
import org.apache.airavata.service.profile.gateway.cpi.exception.GatewayProfileServiceException;
import org.apache.airavata.service.profile.gateway.cpi.profile_gateway_cpiConstants;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by goshenoy on 3/6/17.
 */
public class GatewayProfileServiceHandler implements GatewayProfileService.Iface {

    private final static Logger logger = LoggerFactory.getLogger(GatewayProfileServiceHandler.class);

    @Override
    public String getAPIVersion() throws GatewayProfileServiceException {
        try {
            return profile_gateway_cpiConstants.GATEWAY_PROFILE_CPI_VERSION;
        } catch (Exception ex) {
            logger.error("Error getting API version, reason: " + ex.getMessage(), ex);
            GatewayProfileServiceException exception = new GatewayProfileServiceException();
            exception.setMessage("Error getting API version, reason: " + ex.getMessage());
            throw ex;
        }
    }

    @Override
    public String addGateway(Gateway gateway) throws GatewayProfileServiceException {
        return null;
    }

    @Override
    public boolean updateGateway(String gatewayId, Gateway updatedGateway) throws GatewayProfileServiceException {
        return false;
    }

    @Override
    public Gateway getGateway(String gatewayId) throws GatewayProfileServiceException {
        return null;
    }

    @Override
    public boolean deleteGateway(String gatewayId) throws GatewayProfileServiceException {
        return false;
    }

    @Override
    public List<Gateway> getAllGateways() throws GatewayProfileServiceException {
        return null;
    }

    @Override
    public boolean isGatewayExist(String gatewayId) throws GatewayProfileServiceException {
        return false;
    }
}
