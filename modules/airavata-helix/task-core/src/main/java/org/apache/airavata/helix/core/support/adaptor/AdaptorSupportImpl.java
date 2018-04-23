/*
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
package org.apache.airavata.helix.core.support.adaptor;

import org.apache.airavata.agents.api.*;
import org.apache.airavata.helix.adaptor.SSHJAgentAdaptor;
import org.apache.airavata.helix.adaptor.SSHJStorageAdaptor;
import org.apache.airavata.helix.task.api.support.AdaptorSupport;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.data.movement.DataMovementProtocol;

import java.util.Optional;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class AdaptorSupportImpl implements AdaptorSupport {

    private static AdaptorSupportImpl INSTANCE;

    private final AgentStore agentStore = new AgentStore();

    private AdaptorSupportImpl() {}

    public synchronized static AdaptorSupportImpl getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AdaptorSupportImpl();
        }
        return INSTANCE;
    }

    public void initializeAdaptor() {
    }

    public AgentAdaptor fetchAdaptor(String gatewayId, String computeResource, JobSubmissionProtocol protocol, String authToken, String userId) throws AgentException {

        Optional<AgentAdaptor> agentAdaptorOp = agentStore.getAgentAdaptor(computeResource, protocol, authToken);
        if (agentAdaptorOp.isPresent()) {
            return agentAdaptorOp.get();
        } else {
            synchronized (this) {
                agentAdaptorOp = agentStore.getAgentAdaptor(computeResource, protocol, authToken);
                if (agentAdaptorOp.isPresent()) {
                    return agentAdaptorOp.get();
                } else {
                    switch (protocol) {
                        case SSH:
                            SSHJAgentAdaptor agentAdaptor = new SSHJAgentAdaptor();
                            agentAdaptor.init(computeResource, gatewayId, userId, authToken);
                            agentStore.putAgentAdaptor(computeResource, protocol, authToken, agentAdaptor);
                            return agentAdaptor;
                        default:
                            throw new AgentException("Could not find an agent adaptor for gateway " + gatewayId +
                                    ", compute resource " + computeResource + ", protocol " + protocol + " , user " + userId);
                    }
                }
            }
        }
    }

    @Override
    public StorageResourceAdaptor fetchStorageAdaptor(String gatewayId, String storageResourceId, DataMovementProtocol protocol, String authToken, String userId) throws AgentException {
        Optional<StorageResourceAdaptor> agentAdaptorOp = agentStore.getStorageAdaptor(storageResourceId, protocol, authToken);
        if (agentAdaptorOp.isPresent()) {
            return agentAdaptorOp.get();
        } else {
            synchronized (this) {
                agentAdaptorOp = agentStore.getStorageAdaptor(storageResourceId, protocol, authToken);
                if (agentAdaptorOp.isPresent()) {
                    return agentAdaptorOp.get();
                } else {
                    switch (protocol) {
                        case SCP:
                            SSHJStorageAdaptor storageResourceAdaptor = new SSHJStorageAdaptor();
                            storageResourceAdaptor.init(storageResourceId, gatewayId, userId, authToken);
                            agentStore.putStorageAdaptor(storageResourceId, protocol, authToken, storageResourceAdaptor);
                            return storageResourceAdaptor;
                        default:
                            throw new AgentException("Could not find an storage adaptor for gateway " + gatewayId +
                                    ", storage resource " + storageResourceId + ", protocol " + protocol + " , user " + userId);
                    }
                }
            }
        }
    }
}
