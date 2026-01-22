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
package org.apache.airavata.agents.support;

import org.apache.airavata.agents.api.AdaptorSupport;
import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.agents.api.AgentException;
import org.apache.airavata.agents.api.StorageResourceAdaptor;
import org.apache.airavata.agents.ssh.SSHJAgentAdaptor;
import org.apache.airavata.agents.ssh.SSHJStorageAdaptor;
import org.apache.airavata.common.model.DataMovementProtocol;
import org.apache.airavata.common.model.JobSubmissionProtocol;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.service.security.CredentialStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@Component
@Profile("!test")
@Primary
public class AdaptorSupportImpl implements AdaptorSupport {

    private static final Logger logger = LoggerFactory.getLogger(AdaptorSupportImpl.class);

    private final AgentStore agentStore = new AgentStore();
    private final RegistryService registryService;
    private final CredentialStoreService credentialStoreService;

    public AdaptorSupportImpl(RegistryService registryService, CredentialStoreService credentialStoreService) {
        this.registryService = registryService;
        this.credentialStoreService = credentialStoreService;
    }

    public void initializeAdaptor() {}

    public AgentAdaptor fetchAdaptor(
            String gatewayId, String computeResourceId, JobSubmissionProtocol protocol, String authToken, String userId)
            throws AgentException {

        logger.debug("Fetching adaptor for compute resource " + computeResourceId + " with token " + authToken
                + " with user " + userId + " with protocol" + protocol.name());

        var agentAdaptorOp = agentStore.getAgentAdaptor(computeResourceId, protocol, authToken, userId);
        if (agentAdaptorOp.isPresent()) {
            logger.debug("Re using the adaptor for gateway " + gatewayId + ", compute resource " + computeResourceId
                    + ", protocol " + protocol + " , user " + userId);
            return agentAdaptorOp.get();
        } else {
            synchronized (this) {
                agentAdaptorOp = agentStore.getAgentAdaptor(computeResourceId, protocol, authToken, userId);
                if (agentAdaptorOp.isPresent()) {
                    return agentAdaptorOp.get();
                } else {
                    logger.debug("Could not find an adaptor for gateway " + gatewayId + ", compute resource "
                            + computeResourceId + ", protocol " + protocol + " , user " + userId
                            + ". Creating new one");
                    switch (protocol) {
                        case SSH:
                            var agentAdaptor = new SSHJAgentAdaptor(registryService, credentialStoreService);
                            agentAdaptor.init(computeResourceId, gatewayId, userId, authToken);
                            agentStore.putAgentAdaptor(computeResourceId, protocol, authToken, userId, agentAdaptor);
                            return agentAdaptor;
                        default:
                            throw new AgentException(
                                    "Could not find an agent adaptor for gateway " + gatewayId + ", compute resource "
                                            + computeResourceId + ", protocol " + protocol + " , user " + userId);
                    }
                }
            }
        }
    }

    @Override
    public StorageResourceAdaptor fetchStorageAdaptor(
            String gatewayId, String storageResourceId, DataMovementProtocol protocol, String authToken, String userId)
            throws AgentException {

        logger.debug("Fetching adaptor for storage resource " + storageResourceId + " with token " + authToken
                + " with user " + userId + " with protocol" + protocol.name());

        var agentAdaptorOp = agentStore.getStorageAdaptor(storageResourceId, protocol, authToken, userId);
        if (agentAdaptorOp.isPresent()) {
            logger.debug("Re using the storage adaptor for gateway " + gatewayId + ", storage resource "
                    + storageResourceId + ", protocol " + protocol + " , user " + userId);
            return agentAdaptorOp.get();
        } else {
            synchronized (this) {
                agentAdaptorOp = agentStore.getStorageAdaptor(storageResourceId, protocol, authToken, userId);
                if (agentAdaptorOp.isPresent()) {
                    return agentAdaptorOp.get();
                } else {
                    logger.debug("Could not find a storage adaptor for gateway " + gatewayId + ", storage resource "
                            + storageResourceId + ", protocol " + protocol + " , user " + userId
                            + ". Creating new one");
                    switch (protocol) {
                        case SCP:
                            var storageResourceAdaptor =
                                    new SSHJStorageAdaptor(registryService, credentialStoreService);
                            storageResourceAdaptor.init(storageResourceId, gatewayId, userId, authToken);
                            agentStore.putStorageAdaptor(
                                    storageResourceId, protocol, authToken, userId, storageResourceAdaptor);
                            return storageResourceAdaptor;
                        default:
                            throw new AgentException(
                                    "Could not find an storage adaptor for gateway " + gatewayId + ", storage resource "
                                            + storageResourceId + ", protocol " + protocol + " , user " + userId);
                    }
                }
            }
        }
    }

    @Override
    public AgentAdaptor fetchComputeSSHAdaptor(
            String gatewayId, String resourceId, String authToken, String gatewayUserId, String loginUserName)
            throws AgentException {
        var cacheKey = "compute-" + resourceId;

        logger.debug(
                "Fetching SSH adaptor for compute resource {} with token {} for gateway user {} with login username {}",
                resourceId,
                authToken,
                gatewayUserId,
                loginUserName);

        var adaptorOp = agentStore.getSSHAdaptor(cacheKey, authToken, gatewayUserId, loginUserName);
        if (adaptorOp.isPresent()) {
            logger.debug(
                    "Reusing SSH adaptor for gateway {}, compute resource {}, gateway user {}, login username {}",
                    gatewayId,
                    resourceId,
                    gatewayUserId,
                    loginUserName);
            return adaptorOp.get();

        } else {
            synchronized (this) {
                adaptorOp = agentStore.getSSHAdaptor(cacheKey, authToken, gatewayUserId, loginUserName);
                if (adaptorOp.isPresent()) {
                    return adaptorOp.get();

                } else {
                    logger.debug(
                            "Could not find SSH adaptor for gateway {}, compute resource {}, gateway user {}, login username {}. Creating new one",
                            gatewayId,
                            resourceId,
                            gatewayUserId,
                            loginUserName);

                    var agentAdaptor = new SSHJAgentAdaptor(registryService, credentialStoreService);
                    agentAdaptor.init(resourceId, gatewayId, loginUserName, authToken);

                    agentStore.putSSHAdaptor(cacheKey, authToken, gatewayUserId, loginUserName, agentAdaptor);
                    return agentAdaptor;
                }
            }
        }
    }

    @Override
    public StorageResourceAdaptor fetchStorageSSHAdaptor(
            String gatewayId, String resourceId, String authToken, String gatewayUserId, String loginUserName)
            throws AgentException {
        var cacheKey = "storage-" + resourceId;

        logger.debug(
                "Fetching SSH adaptor for storage resource {} with token {} for gateway user {} with login username {}",
                resourceId,
                authToken,
                gatewayUserId,
                loginUserName);

        var adaptorOp = agentStore.getSSHAdaptor(cacheKey, authToken, gatewayUserId, loginUserName);
        if (adaptorOp.isPresent()) {
            logger.debug(
                    "Reusing SSH adaptor for gateway {}, storage resource {}, gateway user {}, login username {}",
                    gatewayId,
                    resourceId,
                    gatewayUserId,
                    loginUserName);
            return (StorageResourceAdaptor) adaptorOp.get();

        } else {
            synchronized (this) {
                adaptorOp = agentStore.getSSHAdaptor(cacheKey, authToken, gatewayUserId, loginUserName);
                if (adaptorOp.isPresent()) {
                    return (StorageResourceAdaptor) adaptorOp.get();
                } else {
                    logger.debug(
                            "Could not find SSH adaptor for gateway {}, storage resource {}, gateway user {}, login username {}. Creating new one",
                            gatewayId,
                            resourceId,
                            gatewayUserId,
                            loginUserName);

                    var storageAdaptor = new SSHJStorageAdaptor(registryService, credentialStoreService);
                    storageAdaptor.init(resourceId, gatewayId, loginUserName, authToken);

                    agentStore.putSSHAdaptor(cacheKey, authToken, gatewayUserId, loginUserName, storageAdaptor);
                    return storageAdaptor;
                }
            }
        }
    }
}
