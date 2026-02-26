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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.airavata.compute.resource.model.JobSubmissionProtocol;
import org.apache.airavata.storage.resource.model.DataMovementProtocol;

/**
 * In-memory cache of agent adapters (job submission and data movement) per compute resource and credential.
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class AdapterCache {

    // compute resource: Job submission protocol: auth token: user: adapter
    private final Map<String, Map<JobSubmissionProtocol, Map<String, Map<String, AgentAdapter>>>> agentAdapterCache =
            new HashMap<>();
    private final Map<String, Map<DataMovementProtocol, Map<String, Map<String, StorageResourceAdapter>>>>
            storageAdapterCache = new HashMap<>();
    // SSH adapter cache: resourceId (with compute/storage prefix): auth token: gatewayUserId: loginUserName: adapter
    private final Map<String, Map<String, Map<String, Map<String, AgentAdapter>>>> sshAdapterCache = new HashMap<>();

    public Optional<AgentAdapter> getAgentAdapter(
            String computeResource, JobSubmissionProtocol submissionProtocol, String authToken, String userId) {
        var protoToTokenMap = agentAdapterCache.get(computeResource);
        if (protoToTokenMap != null) {
            var tokenToUserMap = protoToTokenMap.get(submissionProtocol);
            if (tokenToUserMap != null) {
                var userToAdapterMap = tokenToUserMap.get(authToken);
                if (userToAdapterMap != null) {
                    return Optional.ofNullable(userToAdapterMap.get(userId));
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

    public void putAgentAdapter(
            String computeResource,
            JobSubmissionProtocol submissionProtocol,
            String authToken,
            String userId,
            AgentAdapter agentAdapter) {
        var protoToTokenMap = agentAdapterCache.computeIfAbsent(computeResource, k -> new HashMap<>());
        var tokenToUserMap = protoToTokenMap.computeIfAbsent(submissionProtocol, k -> new HashMap<>());
        var userToAdapterMap = tokenToUserMap.computeIfAbsent(authToken, k -> new HashMap<>());
        userToAdapterMap.put(userId, agentAdapter);
    }

    public Optional<StorageResourceAdapter> getStorageAdapter(
            String computeResource, DataMovementProtocol dataMovementProtocol, String authToken, String userId) {
        var protoToTokenMap = storageAdapterCache.get(computeResource);
        if (protoToTokenMap != null) {
            var tokenToUserMap = protoToTokenMap.get(dataMovementProtocol);
            if (tokenToUserMap != null) {
                var userToAdapterMap = tokenToUserMap.get(authToken);
                if (userToAdapterMap != null) {
                    return Optional.ofNullable(userToAdapterMap.get(userId));
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

    public void putStorageAdapter(
            String computeResource,
            DataMovementProtocol dataMovementProtocol,
            String authToken,
            String userId,
            StorageResourceAdapter storageResourceAdapter) {
        var protoToTokenMap = storageAdapterCache.computeIfAbsent(computeResource, k -> new HashMap<>());
        var tokenToUserMap = protoToTokenMap.computeIfAbsent(dataMovementProtocol, k -> new HashMap<>());
        var userToAdapterMap = tokenToUserMap.computeIfAbsent(authToken, k -> new HashMap<>());
        userToAdapterMap.put(userId, storageResourceAdapter);
    }

    public Optional<AgentAdapter> getSSHAdapter(
            String resourceId, String authToken, String gatewayUserId, String loginUserName) {
        var tokenToGatewayUserMap = sshAdapterCache.get(resourceId);

        if (tokenToGatewayUserMap != null) {
            var gatewayUserToLoginUserMap = tokenToGatewayUserMap.get(authToken);

            if (gatewayUserToLoginUserMap != null) {
                var loginUserToAdapterMap = gatewayUserToLoginUserMap.get(gatewayUserId);

                if (loginUserToAdapterMap != null) {
                    return Optional.ofNullable(loginUserToAdapterMap.get(loginUserName));
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

    public void putSSHAdapter(
            String resourceId, String authToken, String gatewayUserId, String loginUserName, AgentAdapter adapter) {

        var tokenToGatewayUserMap = sshAdapterCache.computeIfAbsent(resourceId, k -> new HashMap<>());
        var gatewayUserToLoginUserMap = tokenToGatewayUserMap.computeIfAbsent(authToken, k -> new HashMap<>());
        var loginUserToAdapterMap = gatewayUserToLoginUserMap.computeIfAbsent(gatewayUserId, k -> new HashMap<>());

        loginUserToAdapterMap.put(loginUserName, adapter);
    }
}
