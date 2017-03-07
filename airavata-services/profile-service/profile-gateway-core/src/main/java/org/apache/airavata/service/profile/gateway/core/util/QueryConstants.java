package org.apache.airavata.service.profile.gateway.core.util;

import org.apache.airavata.model.user.UserProfile;

/**
 * Created by goshenoy on 11/11/2016.
 */
public class QueryConstants {

    public static final String FIND_GATEWAY_BY_ID = "SELECT g FROM Gateway g where g.gatewayId='{}'";

    public static final String GET_ALL_GATEWAYS = "SELECT g FROM Gateway g";
}
