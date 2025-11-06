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
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.registry.api.service.handler.RegistryServerHandler;
import org.apache.airavata.service.security.IdentityContext;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class StorageResourceService {

    private static final Logger logger = LoggerFactory.getLogger(StorageResourceService.class);
    private final RegistryServerHandler registryServerHandler;

    public StorageResourceService() {
        this.registryServerHandler = new RegistryServerHandler();
    }

    public String registerStorageResource(StorageResourceDescription storageResourceDescription)
            throws RegistryServiceException, TException {
        return registryServerHandler.registerStorageResource(storageResourceDescription);
    }

    public StorageResourceDescription getStorageResource(String storageResourceId)
            throws RegistryServiceException, TException {
        return registryServerHandler.getStorageResource(storageResourceId);
    }

    public Map<String, String> getAllStorageResourceNames() throws RegistryServiceException, TException {
        return registryServerHandler.getAllStorageResourceNames();
    }

    public boolean updateStorageResource(String storageResourceId, StorageResourceDescription storageResourceDescription)
            throws RegistryServiceException, TException {
        return registryServerHandler.updateStorageResource(storageResourceId, storageResourceDescription);
    }

    public boolean deleteStorageResource(String storageResourceId) throws RegistryServiceException, TException {
        return registryServerHandler.deleteStorageResource(storageResourceId);
    }

    private AuthzToken getAuthzToken() {
        AuthzToken authzToken = IdentityContext.get();
        if (authzToken == null) {
            throw new IllegalStateException("AuthzToken not found in IdentityContext");
        }
        return authzToken;
    }
}

