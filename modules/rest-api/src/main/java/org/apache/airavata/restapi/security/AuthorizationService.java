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
package org.apache.airavata.restapi.security;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.airavata.core.util.Constants;
import org.apache.airavata.gateway.service.GatewayService;
import org.apache.airavata.iam.model.AuthzToken;
import org.apache.airavata.iam.service.IamAdminService;
import org.apache.airavata.iam.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service for checking user authorization and gateway access.
 * Queries DB directly — user profile data is already DB-backed.
 */
@Service("restApiAuthorizationService")
public class AuthorizationService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AuthorizationService.class);

    @Value("${airavata.portal.root-admin-username:default-admin}")
    private String rootUserId;

    private final UserService userProfileService;
    private final IamAdminService iamAdminService;
    private final GatewayService gatewayService;

    public AuthorizationService(
            UserService userProfileService, IamAdminService iamAdminService, GatewayService gatewayService) {
        this.userProfileService = userProfileService;
        this.iamAdminService = iamAdminService;
        this.gatewayService = gatewayService;
    }

    /**
     * Check if user is the root/admin user.
     */
    public boolean isRootUser(AuthzToken authzToken) {
        String userId = authzToken.getClaimsMap().get("userId");
        String userName = authzToken.getClaimsMap().get("userName");
        // Check both userId (which could be UUID) and userName (which is the username)
        return rootUserId.equals(userId) || rootUserId.equals(userName);
    }

    /**
     * Check if user has access to a specific gateway.
     * Queries user profile DB directly.
     */
    public boolean hasGatewayAccess(AuthzToken authzToken, String gatewayId) {
        // Root user has access to all gateways
        if (isRootUser(authzToken)) {
            return true;
        }

        String userId = authzToken.getClaimsMap().get("userId");
        if (userId == null) {
            return false;
        }

        try {
            // Check if user has a profile for this gateway (DB query)
            var userProfile = userProfileService.getUserProfileById(authzToken, userId, gatewayId);
            return userProfile != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get list of gateway IDs the user has access to.
     * Queries DB directly.
     */
    public List<String> getAccessibleGateways(AuthzToken authzToken) {
        String userId = authzToken.getClaimsMap().get("userId");

        if (userId == null) {
            return List.of();
        }

        // Root user can access all gateways - return all
        if (isRootUser(authzToken)) {
            try {
                var allGateways = gatewayService.getAllGateways();
                return allGateways.stream().map(g -> g.getGatewayId()).collect(Collectors.toList());
            } catch (Exception e) {
                return List.of("default");
            }
        }

        // For regular users, get gateways from their token
        try {
            String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            return gatewayId != null ? List.of(gatewayId) : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Require that user is root user, throw exception if not.
     */
    public void requireRootUser(AuthzToken authzToken) {
        if (!isRootUser(authzToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This operation requires root/admin privileges");
        }
    }

    /**
     * Require that user has access to the specified gateway, throw exception if not.
     */
    public void requireGatewayAccess(AuthzToken authzToken, String gatewayId) {
        if (!hasGatewayAccess(authzToken, gatewayId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "User does not have access to gateway: " + gatewayId);
        }
    }

    /**
     * Validate and scope gateway ID - ensures user has access and returns the scoped gateway ID.
     */
    public String validateAndScopeGateway(AuthzToken authzToken, String requestedGatewayId) {
        // Clean up the gateway ID - remove any whitespace or duplicates
        if (requestedGatewayId != null) {
            requestedGatewayId = requestedGatewayId.trim();
            // If it contains commas, take the first part (in case of accidental duplication)
            if (requestedGatewayId.contains(",")) {
                logger.warn("Gateway ID contains comma, using first part: {}", requestedGatewayId);
                requestedGatewayId = requestedGatewayId.split(",")[0].trim();
            }
        }

        if (requestedGatewayId == null || requestedGatewayId.isEmpty()) {
            // Use gateway from token if not specified
            requestedGatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            if (requestedGatewayId != null) {
                requestedGatewayId = requestedGatewayId.trim();
                // Handle comma-separated values from token as well
                if (requestedGatewayId.contains(",")) {
                    logger.warn("Gateway ID from token contains comma, using first part: {}", requestedGatewayId);
                    requestedGatewayId = requestedGatewayId.split(",")[0].trim();
                }
            }
        }

        if (requestedGatewayId == null || requestedGatewayId.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gateway ID is required");
        }

        requireGatewayAccess(authzToken, requestedGatewayId);
        return requestedGatewayId;
    }
}
