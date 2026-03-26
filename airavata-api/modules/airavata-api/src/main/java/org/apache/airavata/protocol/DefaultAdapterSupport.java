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
package org.apache.airavata.protocol;

import java.util.concurrent.ConcurrentHashMap;
import org.apache.airavata.compute.resource.model.JobSubmissionProtocol;
import org.apache.airavata.iam.service.CredentialStoreService;
import org.apache.airavata.protocol.AgentAdapter.AgentException;
import org.apache.airavata.protocol.ssh.SSHJAgentAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Default implementation of AdapterSupport; resolves and caches agent adapters.
 */
@Component
@Profile({"!test", "orchestrator-integration"})
@Primary
public class DefaultAdapterSupport implements AdapterSupport {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAdapterSupport.class);

    private final ConcurrentHashMap<String, AgentAdapter> cache = new ConcurrentHashMap<>();
    private final ResourceLookup resourceLookup;
    private final CredentialStoreService credentialStoreService;

    public DefaultAdapterSupport(ResourceLookup resourceLookup, CredentialStoreService credentialStoreService) {
        this.resourceLookup = resourceLookup;
        this.credentialStoreService = credentialStoreService;
    }

    @Override
    public AgentAdapter fetchAdapter(
            String gatewayId, String computeResourceId, JobSubmissionProtocol protocol, String authToken, String userId)
            throws AgentException {

        switch (protocol) {
            case SSH:
            case SSH_FORK:
                break;
            default:
                throw new AgentException(
                        "Unsupported protocol " + protocol + " for compute resource " + computeResourceId);
        }

        var cacheKey = cacheKey("compute", computeResourceId, protocol.name(), authToken, userId);
        return getOrCreate(cacheKey, computeResourceId, gatewayId, userId, authToken);
    }

    @Override
    public AgentAdapter fetchStorageAdapter(String gatewayId, String storageResourceId, String authToken, String userId)
            throws AgentException {

        var cacheKey = cacheKey("storage", storageResourceId, authToken, userId);
        return getOrCreate(cacheKey, storageResourceId, gatewayId, userId, authToken);
    }

    @Override
    public AgentAdapter fetchSSHAdapter(
            String gatewayId, String resourceId, String authToken, String gatewayUserId, String loginUserName)
            throws AgentException {

        var cacheKey = cacheKey("ssh", resourceId, authToken, gatewayUserId, loginUserName);
        return getOrCreate(cacheKey, resourceId, gatewayId, loginUserName, authToken);
    }

    private static String cacheKey(String... parts) {
        return String.join("|", parts);
    }

    private AgentAdapter getOrCreate(
            String cacheKey, String resourceId, String gatewayId, String userId, String authToken)
            throws AgentException {

        var existing = cache.get(cacheKey);
        if (existing != null) {
            logger.debug("Reusing adapter for resource {}, user {}", resourceId, userId);
            return existing;
        }

        synchronized (this) {
            existing = cache.get(cacheKey);
            if (existing != null) {
                return existing;
            }

            logger.debug("Creating new adapter for resource {}, user {}", resourceId, userId);
            var adapter = new SSHJAgentAdapter(resourceLookup, credentialStoreService);
            adapter.init(resourceId, gatewayId, userId, authToken);
            cache.put(cacheKey, adapter);
            return adapter;
        }
    }
}
