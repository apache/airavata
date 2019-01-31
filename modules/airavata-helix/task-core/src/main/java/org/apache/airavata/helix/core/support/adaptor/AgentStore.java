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

import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.agents.api.StorageResourceAdaptor;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.data.movement.DataMovementProtocol;
import org.apache.zookeeper.Op;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class AgentStore {

    // compute resource: Job submission protocol: auth token: user: adaptor
    private final Map<String, Map<JobSubmissionProtocol, Map<String, Map<String, AgentAdaptor>>>> agentAdaptorCache = new HashMap<>();
    private final Map<String, Map<DataMovementProtocol, Map<String, Map<String, StorageResourceAdaptor>>>> storageAdaptorCache = new HashMap<>();

    public Optional<AgentAdaptor> getAgentAdaptor(String computeResource, JobSubmissionProtocol submissionProtocol, String authToken, String userId) {
        Map<JobSubmissionProtocol, Map<String, Map<String, AgentAdaptor>>> protoToTokenMap = agentAdaptorCache.get(computeResource);
        if (protoToTokenMap != null) {
            Map<String, Map<String,AgentAdaptor>> tokenToUserMap = protoToTokenMap.get(submissionProtocol);
            if (tokenToUserMap != null) {
                Map<String, AgentAdaptor> userToAdaptorMap = tokenToUserMap.get(authToken);
                if (userToAdaptorMap != null) {
                    return Optional.ofNullable(userToAdaptorMap.get(userId));
                } else {
                    return Optional.empty();
                }
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    public void putAgentAdaptor(String computeResource, JobSubmissionProtocol submissionProtocol, String authToken, String userId, AgentAdaptor agentAdaptor) {
        Map<JobSubmissionProtocol, Map<String, Map<String,AgentAdaptor>>> protoToTokenMap = agentAdaptorCache.computeIfAbsent(computeResource, k -> new HashMap<>());
        Map<String, Map<String,AgentAdaptor>> tokenToUserMap = protoToTokenMap.computeIfAbsent(submissionProtocol, k -> new HashMap<>());
        Map<String, AgentAdaptor> userToAdaptorMap = tokenToUserMap.computeIfAbsent(authToken, k-> new HashMap<>());
        userToAdaptorMap.put(userId, agentAdaptor);
    }

    public Optional<StorageResourceAdaptor> getStorageAdaptor(String computeResource, DataMovementProtocol dataMovementProtocol, String authToken, String userId) {
        Map<DataMovementProtocol, Map<String, Map<String, StorageResourceAdaptor>>> protoToTokenMap = storageAdaptorCache.get(computeResource);
        if (protoToTokenMap != null) {
            Map<String, Map<String, StorageResourceAdaptor>> tokenToUserMap = protoToTokenMap.get(dataMovementProtocol);
            if (tokenToUserMap != null) {
                Map<String, StorageResourceAdaptor> userToAdaptorMap = tokenToUserMap.get(authToken);
                if (userToAdaptorMap != null) {
                    return Optional.ofNullable(userToAdaptorMap.get(userId));
                } else {
                    return Optional.empty();
                }
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    public void putStorageAdaptor(String computeResource, DataMovementProtocol dataMovementProtocol, String authToken, String userId, StorageResourceAdaptor storageResourceAdaptor) {
        Map<DataMovementProtocol, Map<String, Map<String, StorageResourceAdaptor>>> protoToTokenMap = storageAdaptorCache.computeIfAbsent(computeResource, k -> new HashMap<>());
        Map<String, Map<String, StorageResourceAdaptor>> tokenToUserMap = protoToTokenMap.computeIfAbsent(dataMovementProtocol, k -> new HashMap<>());
        Map<String, StorageResourceAdaptor> userToAdaptorMap = tokenToUserMap.computeIfAbsent(authToken, k -> new HashMap<>());
        userToAdaptorMap.put(userId, storageResourceAdaptor);
    }

}
