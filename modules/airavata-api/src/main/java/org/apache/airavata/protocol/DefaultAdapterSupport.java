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

import org.apache.airavata.compute.resource.model.JobSubmissionProtocol;
import org.apache.airavata.iam.service.CredentialStoreService;
import org.apache.airavata.protocol.ssh.SSHJAgentAdapter;
import org.apache.airavata.protocol.ssh.SSHJStorageAdapter;
import org.apache.airavata.storage.resource.model.DataMovementProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Default implementation of AdapterSupport; resolves and caches agent adapters for job submission and data movement.
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@Component
@Profile({"!test", "orchestrator-integration"})
@Primary
public class DefaultAdapterSupport implements AdapterSupport {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAdapterSupport.class);

    private final AdapterCache agentStore = new AdapterCache();
    private final ResourceLookup resourceLookup;
    private final CredentialStoreService credentialStoreService;

    public DefaultAdapterSupport(ResourceLookup resourceLookup, CredentialStoreService credentialStoreService) {
        this.resourceLookup = resourceLookup;
        this.credentialStoreService = credentialStoreService;
    }

    public AgentAdapter fetchAdapter(
            String gatewayId, String computeResourceId, JobSubmissionProtocol protocol, String authToken, String userId)
            throws AgentException {

        logger.debug("Fetching adapter for compute resource " + computeResourceId + " with token " + authToken
                + " with user " + userId + " with protocol" + protocol.name());

        var agentAdapterOp = agentStore.getAgentAdapter(computeResourceId, protocol, authToken, userId);
        if (agentAdapterOp.isPresent()) {
            logger.debug("Re using the adapter for gateway " + gatewayId + ", compute resource " + computeResourceId
                    + ", protocol " + protocol + " , user " + userId);
            return agentAdapterOp.get();
        } else {
            synchronized (this) {
                agentAdapterOp = agentStore.getAgentAdapter(computeResourceId, protocol, authToken, userId);
                if (agentAdapterOp.isPresent()) {
                    return agentAdapterOp.get();
                } else {
                    logger.debug("Could not find an adapter for gateway " + gatewayId + ", compute resource "
                            + computeResourceId + ", protocol " + protocol + " , user " + userId
                            + ". Creating new one");
                    switch (protocol) {
                        case SSH:
                        case SSH_FORK:
                            var agentAdapter = new SSHJAgentAdapter(resourceLookup, credentialStoreService);
                            agentAdapter.init(computeResourceId, gatewayId, userId, authToken);
                            agentStore.putAgentAdapter(computeResourceId, protocol, authToken, userId, agentAdapter);
                            return agentAdapter;
                        default:
                            throw new AgentException(
                                    "Could not find an agent adapter for gateway " + gatewayId + ", compute resource "
                                            + computeResourceId + ", protocol " + protocol + " , user " + userId);
                    }
                }
            }
        }
    }

    @Override
    public StorageResourceAdapter fetchStorageAdapter(
            String gatewayId, String storageResourceId, DataMovementProtocol protocol, String authToken, String userId)
            throws AgentException {

        logger.debug("Fetching adapter for storage resource " + storageResourceId + " with token " + authToken
                + " with user " + userId + " with protocol" + protocol.name());

        var agentAdapterOp = agentStore.getStorageAdapter(storageResourceId, protocol, authToken, userId);
        if (agentAdapterOp.isPresent()) {
            logger.debug("Re using the storage adapter for gateway " + gatewayId + ", storage resource "
                    + storageResourceId + ", protocol " + protocol + " , user " + userId);
            return agentAdapterOp.get();
        } else {
            synchronized (this) {
                agentAdapterOp = agentStore.getStorageAdapter(storageResourceId, protocol, authToken, userId);
                if (agentAdapterOp.isPresent()) {
                    return agentAdapterOp.get();
                } else {
                    logger.debug("Could not find a storage adapter for gateway " + gatewayId + ", storage resource "
                            + storageResourceId + ", protocol " + protocol + " , user " + userId
                            + ". Creating new one");
                    switch (protocol) {
                        case SCP:
                            var storageResourceAdapter = new SSHJStorageAdapter(resourceLookup, credentialStoreService);
                            storageResourceAdapter.init(storageResourceId, gatewayId, userId, authToken);
                            agentStore.putStorageAdapter(
                                    storageResourceId, protocol, authToken, userId, storageResourceAdapter);
                            return storageResourceAdapter;
                        default:
                            throw new AgentException(
                                    "Could not find an storage adapter for gateway " + gatewayId + ", storage resource "
                                            + storageResourceId + ", protocol " + protocol + " , user " + userId);
                    }
                }
            }
        }
    }

    @Override
    public AgentAdapter fetchComputeSSHAdapter(
            String gatewayId, String resourceId, String authToken, String gatewayUserId, String loginUserName)
            throws AgentException {
        var cacheKey = "compute-" + resourceId;

        logger.debug(
                "Fetching SSH adapter for compute resource {} with token {} for gateway user {} with login username {}",
                resourceId,
                authToken,
                gatewayUserId,
                loginUserName);

        var adapterOp = agentStore.getSSHAdapter(cacheKey, authToken, gatewayUserId, loginUserName);
        if (adapterOp.isPresent()) {
            logger.debug(
                    "Reusing SSH adapter for gateway {}, compute resource {}, gateway user {}, login username {}",
                    gatewayId,
                    resourceId,
                    gatewayUserId,
                    loginUserName);
            return adapterOp.get();

        } else {
            synchronized (this) {
                adapterOp = agentStore.getSSHAdapter(cacheKey, authToken, gatewayUserId, loginUserName);
                if (adapterOp.isPresent()) {
                    return adapterOp.get();

                } else {
                    logger.debug(
                            "Could not find SSH adapter for gateway {}, compute resource {}, gateway user {}, login username {}. Creating new one",
                            gatewayId,
                            resourceId,
                            gatewayUserId,
                            loginUserName);

                    var agentAdapter = new SSHJAgentAdapter(resourceLookup, credentialStoreService);
                    agentAdapter.init(resourceId, gatewayId, loginUserName, authToken);

                    agentStore.putSSHAdapter(cacheKey, authToken, gatewayUserId, loginUserName, agentAdapter);
                    return agentAdapter;
                }
            }
        }
    }

    @Override
    public StorageResourceAdapter fetchStorageSSHAdapter(
            String gatewayId, String resourceId, String authToken, String gatewayUserId, String loginUserName)
            throws AgentException {
        var cacheKey = "storage-" + resourceId;

        logger.debug(
                "Fetching SSH adapter for storage resource {} with token {} for gateway user {} with login username {}",
                resourceId,
                authToken,
                gatewayUserId,
                loginUserName);

        var adapterOp = agentStore.getSSHAdapter(cacheKey, authToken, gatewayUserId, loginUserName);
        if (adapterOp.isPresent()) {
            logger.debug(
                    "Reusing SSH adapter for gateway {}, storage resource {}, gateway user {}, login username {}",
                    gatewayId,
                    resourceId,
                    gatewayUserId,
                    loginUserName);
            return (StorageResourceAdapter) adapterOp.get();

        } else {
            synchronized (this) {
                adapterOp = agentStore.getSSHAdapter(cacheKey, authToken, gatewayUserId, loginUserName);
                if (adapterOp.isPresent()) {
                    return (StorageResourceAdapter) adapterOp.get();
                } else {
                    logger.debug(
                            "Could not find SSH adapter for gateway {}, storage resource {}, gateway user {}, login username {}. Creating new one",
                            gatewayId,
                            resourceId,
                            gatewayUserId,
                            loginUserName);

                    var storageAdapter = new SSHJStorageAdapter(resourceLookup, credentialStoreService);
                    storageAdapter.init(resourceId, gatewayId, loginUserName, authToken);

                    agentStore.putSSHAdapter(cacheKey, authToken, gatewayUserId, loginUserName, storageAdapter);
                    return storageAdapter;
                }
            }
        }
    }
}
