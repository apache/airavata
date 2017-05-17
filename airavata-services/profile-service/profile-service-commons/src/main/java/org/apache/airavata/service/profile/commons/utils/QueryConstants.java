package org.apache.airavata.service.profile.commons.utils;

import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.model.workspace.Gateway;

/**
 * Created by goshenoy on 03/08/2017.
 */
public class QueryConstants {


    public static final String FIND_USER_PROFILE_BY_USER_ID = "SELECT u FROM UserProfileEntity u " +
            "where u.userId LIKE :" + UserProfile._Fields.USER_ID.getFieldName() + " " +
            "AND u.gatewayId LIKE :" + UserProfile._Fields.GATEWAY_ID.getFieldName() + "";

    public static final String FIND_ALL_USER_PROFILES_BY_GATEWAY_ID = "SELECT u FROM UserProfileEntity u " +
            "where u.gatewayId LIKE :" + UserProfile._Fields.GATEWAY_ID.getFieldName() + "";

    public static final String FIND_GATEWAY_BY_ID = "SELECT g FROM GatewayEntity g " +
            "where g.gatewayId LIKE :" + Gateway._Fields.GATEWAY_ID.getFieldName() + "";

    public static final String GET_ALL_GATEWAYS = "SELECT g FROM GatewayEntity g";
}
