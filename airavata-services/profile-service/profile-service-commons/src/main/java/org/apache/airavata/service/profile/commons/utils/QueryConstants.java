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

    public static final String FIND_GATEWAY_BY_INTERNAL_ID = "SELECT g FROM GatewayEntity g " +
            "where g.airavataInternalGatewayId LIKE :" + Gateway._Fields.AIRAVATA_INTERNAL_GATEWAY_ID.getFieldName();

    public static final String FIND_DUPLICATE_GATEWAY = "SELECT g FROM GatewayEntity g " +
            "where g.gatewayApprovalStatus IN :" + Gateway._Fields.GATEWAY_APPROVAL_STATUS.getFieldName() + " " +
            "and (g.gatewayId LIKE :" + Gateway._Fields.GATEWAY_ID.getFieldName() + " " +
            "     or g.gatewayName LIKE :" + Gateway._Fields.GATEWAY_NAME.getFieldName() + " " +
            "     or g.gatewayUrl LIKE :" + Gateway._Fields.GATEWAY_URL.getFieldName() + " " +
            "    )";

    public static final String GET_ALL_GATEWAYS = "SELECT g FROM GatewayEntity g";

    public static final String GET_USER_GATEWAYS = "SELECT g from GatewayEntity g " +
            "where g.requesterUsername LIKE :" + Gateway._Fields.REQUESTER_USERNAME.getFieldName() + "";
}
