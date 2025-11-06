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
package org.apache.airavata.rest.api.service;

import java.util.Map;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManager;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.registry.api.service.handler.RegistryServerHandler;
import org.apache.airavata.service.security.IdentityContext;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ComputeResourceService {

    private static final Logger logger = LoggerFactory.getLogger(ComputeResourceService.class);
    private final RegistryServerHandler registryServerHandler;

    public ComputeResourceService() {
        this.registryServerHandler = new RegistryServerHandler();
    }

    public String registerComputeResource(ComputeResourceDescription computeResourceDescription)
            throws RegistryServiceException, TException {
        return registryServerHandler.registerComputeResource(computeResourceDescription);
    }

    public ComputeResourceDescription getComputeResource(String computeResourceId)
            throws RegistryServiceException, TException {
        return registryServerHandler.getComputeResource(computeResourceId);
    }

    public Map<String, String> getAllComputeResourceNames() throws RegistryServiceException, TException {
        return registryServerHandler.getAllComputeResourceNames();
    }

    public boolean updateComputeResource(String computeResourceId, ComputeResourceDescription computeResourceDescription)
            throws RegistryServiceException, TException {
        return registryServerHandler.updateComputeResource(computeResourceId, computeResourceDescription);
    }

    public boolean deleteComputeResource(String computeResourceId) throws RegistryServiceException, TException {
        return registryServerHandler.deleteComputeResource(computeResourceId);
    }

    public String registerResourceJobManager(ResourceJobManager resourceJobManager)
            throws RegistryServiceException, TException {
        return registryServerHandler.registerResourceJobManager(resourceJobManager);
    }

    public ResourceJobManager getResourceJobManager(String resourceJobManagerId)
            throws RegistryServiceException, TException {
        return registryServerHandler.getResourceJobManager(resourceJobManagerId);
    }

    public boolean updateResourceJobManager(String resourceJobManagerId, ResourceJobManager updatedResourceJobManager)
            throws RegistryServiceException, TException {
        return registryServerHandler.updateResourceJobManager(resourceJobManagerId, updatedResourceJobManager);
    }

    public boolean deleteResourceJobManager(String resourceJobManagerId) throws RegistryServiceException, TException {
        return registryServerHandler.deleteResourceJobManager(resourceJobManagerId);
    }

    public boolean deleteBatchQueue(String computeResourceId, String queueName)
            throws RegistryServiceException, TException {
        return registryServerHandler.deleteBatchQueue(computeResourceId, queueName);
    }

    private AuthzToken getAuthzToken() {
        AuthzToken authzToken = IdentityContext.get();
        if (authzToken == null) {
            throw new IllegalStateException("AuthzToken not found in IdentityContext");
        }
        return authzToken;
    }
}

