package org.apache.airavata.service.profile.gateway.core.util;

import org.apache.airavata.model.user.UserProfile;

/**
 * Created by goshenoy on 11/11/2016.
 */
public interface QueryConstants {



    String FIND_GATEWAY_PROFILE_BY_ID = "SELECT gp FROM GatewayProfile gp " +
            "where gp.gatewayID='{}'";

    String GET_ALL_GATEWAY_PROFILES = "SELECT gp FROM GatewayProfile gp";
}
