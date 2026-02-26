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
package org.apache.airavata.iam.service;

import org.apache.airavata.config.ServerProperties;
import org.apache.airavata.core.util.Constants;
import org.apache.airavata.iam.exception.AuthExceptions.AuthorizationException;
import org.apache.airavata.iam.model.AuthzToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

/**
 * Service for authorization and access control operations.
 */
@Service
@ConditionalOnBean(SharingService.class)
public class DefaultAuthorizationService implements AuthorizationService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultAuthorizationService.class);

    private final ServerProperties properties;
    private final SharingService sharingService;

    public DefaultAuthorizationService(ServerProperties properties, SharingService sharingService) {
        this.properties = properties;
        this.sharingService = sharingService;
    }

    private boolean userHasAccess(String gatewayId, String userId, String entityId, String permissionTypeId) {
        try {
            return sharingService.userHasAccess(gatewayId, userId, entityId, permissionTypeId);
        } catch (Exception e) {
            logger.error("Error checking user access: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Validates that a user has access to an experiment for reading.
     */
    public void validateExperimentReadAccess(
            AuthzToken authzToken, String experimentId, String experimentOwner, String experimentGatewayId)
            throws AuthorizationException {
        String username = authzToken.getClaimsMap().get(Constants.USER_NAME);
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);

        if (username.equals(experimentOwner) && gatewayId.equals(experimentGatewayId)) {
            return; // Owner has access
        }

        if (properties.isSharingEnabled()) {
            String userId = username + "@" + gatewayId;
            if (!userHasAccess(gatewayId, userId, experimentId, gatewayId + ":READ")) {
                throw new AuthorizationException("User does not have permission to access this resource");
            }
        } else {
            throw new AuthorizationException("User does not have permission to access this resource");
        }
    }

    /**
     * Validates that a user has access to an experiment for writing.
     */
    public void validateExperimentWriteAccess(
            AuthzToken authzToken, String experimentId, String experimentOwner, String experimentGatewayId)
            throws AuthorizationException {
        String username = authzToken.getClaimsMap().get(Constants.USER_NAME);
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);

        if (properties.isSharingEnabled()
                && (!username.equals(experimentOwner) || !gatewayId.equals(experimentGatewayId))) {
            String userId = username + "@" + gatewayId;
            if (!userHasAccess(gatewayId, userId, experimentId, gatewayId + ":WRITE")) {
                throw new AuthorizationException("User does not have permission to access this resource");
            }
        }
    }

    /**
     * Validates that a user has access to a project for reading.
     */
    public void validateProjectReadAccess(
            AuthzToken authzToken, String projectId, String projectOwner, String projectGatewayId)
            throws AuthorizationException {
        String username = authzToken.getClaimsMap().get(Constants.USER_NAME);
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);

        if (username.equals(projectOwner) && gatewayId.equals(projectGatewayId)) {
            return; // Owner has access
        }

        if (properties.isSharingEnabled()) {
            String userId = username + "@" + gatewayId;
            if (!userHasAccess(gatewayId, userId, projectId, gatewayId + ":READ")) {
                throw new AuthorizationException("User does not have permission to access this resource");
            }
        }
    }

    /**
     * Validates that a user has access to a project for writing.
     */
    public void validateProjectWriteAccess(
            AuthzToken authzToken, String projectId, String projectOwner, String projectGatewayId)
            throws AuthorizationException {
        String username = authzToken.getClaimsMap().get(Constants.USER_NAME);
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);

        if (properties.isSharingEnabled() && (!username.equals(projectOwner) || !gatewayId.equals(projectGatewayId))) {
            String userId = username + "@" + gatewayId;
            if (!userHasAccess(gatewayId, userId, projectId, gatewayId + ":WRITE")) {
                throw new AuthorizationException("User does not have permission to access this resource");
            }
        }
    }
}
