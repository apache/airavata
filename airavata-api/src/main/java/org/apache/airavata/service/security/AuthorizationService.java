/**
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
package org.apache.airavata.service.security;

import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.model.error.AuthorizationException;
import org.apache.airavata.model.error.InvalidRequestException;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.security.AuthzToken;

/**
 * Service interface for authorization and access control operations.
 */
public interface AuthorizationService {
    /**
     * Validates that a user has access to an experiment for reading.
     */
    void validateExperimentReadAccess(AuthzToken authzToken, String experimentId, String experimentOwner, String experimentGatewayId) throws AuthorizationException;
    
    /**
     * Validates that a user has access to an experiment for writing.
     */
    void validateExperimentWriteAccess(AuthzToken authzToken, String experimentId, String experimentOwner, String experimentGatewayId) throws AuthorizationException;
    
    /**
     * Validates that a user has access to a project for reading.
     */
    void validateProjectReadAccess(AuthzToken authzToken, String projectId, String projectOwner, String projectGatewayId) throws AuthorizationException;
    
    /**
     * Validates that a user has access to a project for writing.
     */
    void validateProjectWriteAccess(AuthzToken authzToken, String projectId, String projectOwner, String projectGatewayId) throws AuthorizationException;
    
    /**
     * Validates launch experiment access, checking group resource profile and application deployment permissions.
     */
    void validateLaunchExperimentAccess(AuthzToken authzToken, String gatewayId, ExperimentModel experiment) throws InvalidRequestException, AuthorizationException, AiravataSystemException;
}
