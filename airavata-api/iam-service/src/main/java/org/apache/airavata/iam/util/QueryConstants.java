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
package org.apache.airavata.iam.util;

/**
 * Created by goshenoy on 03/08/2017.
 */
public class QueryConstants {

    // Field name constants (formerly derived from Thrift _Fields enums)
    public static final String USER_ID = "userId";
    public static final String GATEWAY_ID = "gatewayId";
    public static final String AIRAVATA_INTERNAL_GATEWAY_ID = "airavataInternalGatewayId";
    public static final String GATEWAY_APPROVAL_STATUS = "gatewayApprovalStatus";
    public static final String GATEWAY_NAME = "gatewayName";
    public static final String GATEWAY_URL = "gatewayUrl";
    public static final String REQUESTER_USERNAME = "requesterUsername";

    public static final String FIND_USER_PROFILE_BY_USER_ID =
            "SELECT u FROM UserProfileEntity u " + "where u.userId LIKE :"
                    + USER_ID + " " + "AND u.gatewayId LIKE :"
                    + GATEWAY_ID + "";

    public static final String FIND_ALL_USER_PROFILES_BY_GATEWAY_ID =
            "SELECT u FROM UserProfileEntity u " + "where u.gatewayId LIKE :" + GATEWAY_ID + "";

    public static final String FIND_GATEWAY_BY_INTERNAL_ID = "SELECT g FROM GatewayEntity g "
            + "where g.airavataInternalGatewayId LIKE :" + AIRAVATA_INTERNAL_GATEWAY_ID;

    public static final String FIND_DUPLICATE_GATEWAY =
            "SELECT g FROM GatewayEntity g " + "where g.gatewayApprovalStatus IN :"
                    + GATEWAY_APPROVAL_STATUS + " " + "and (g.gatewayId LIKE :"
                    + GATEWAY_ID + " " + "     or g.gatewayName LIKE :"
                    + GATEWAY_NAME + " " + "     or g.gatewayUrl LIKE :"
                    + GATEWAY_URL + " " + "    )";

    public static final String GET_ALL_GATEWAYS = "SELECT g FROM GatewayEntity g";

    public static final String GET_USER_GATEWAYS =
            "SELECT g from GatewayEntity g " + "where g.requesterUsername LIKE :" + REQUESTER_USERNAME + "";
}
