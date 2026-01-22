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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.agents.api.StorageResourceAdaptor;
import org.apache.airavata.common.model.DataMovementProtocol;
import org.apache.airavata.common.model.JobSubmissionProtocol;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class AgentStore {

    // compute resource: Job submission protocol: auth token: user: adaptor
    private final Map<String, Map<JobSubmissionProtocol, Map<String, Map<String, AgentAdaptor>>>> agentAdaptorCache =
            new HashMap<>();
    private final Map<String, Map<DataMovementProtocol, Map<String, Map<String, StorageResourceAdaptor>>>>
            storageAdaptorCache = new HashMap<>();
    // SSH adaptor cache: resourceId (with compute/storage prefix): auth token: gatewayUserId: loginUserName: adaptor
    private final Map<String, Map<String, Map<String, Map<String, AgentAdaptor>>>> sshAdaptorCache = new HashMap<>();

    public Optional<AgentAdaptor> getAgentAdaptor(
            String computeResource, JobSubmissionProtocol submissionProtocol, String authToken, String userId) {
        var protoToTokenMap = agentAdaptorCache.get(computeResource);
        if (protoToTokenMap != null) {
            var tokenToUserMap = protoToTokenMap.get(submissionProtocol);
            if (tokenToUserMap != null) {
                var userToAdaptorMap = tokenToUserMap.get(authToken);
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

    public void putAgentAdaptor(
            String computeResource,
            JobSubmissionProtocol submissionProtocol,
            String authToken,
            String userId,
            AgentAdaptor agentAdaptor) {
        var protoToTokenMap = agentAdaptorCache.computeIfAbsent(computeResource, k -> new HashMap<>());
        var tokenToUserMap = protoToTokenMap.computeIfAbsent(submissionProtocol, k -> new HashMap<>());
        var userToAdaptorMap = tokenToUserMap.computeIfAbsent(authToken, k -> new HashMap<>());
        userToAdaptorMap.put(userId, agentAdaptor);
    }

    public Optional<StorageResourceAdaptor> getStorageAdaptor(
            String computeResource, DataMovementProtocol dataMovementProtocol, String authToken, String userId) {
        var protoToTokenMap = storageAdaptorCache.get(computeResource);
        if (protoToTokenMap != null) {
            var tokenToUserMap = protoToTokenMap.get(dataMovementProtocol);
            if (tokenToUserMap != null) {
                var userToAdaptorMap = tokenToUserMap.get(authToken);
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

    public void putStorageAdaptor(
            String computeResource,
            DataMovementProtocol dataMovementProtocol,
            String authToken,
            String userId,
            StorageResourceAdaptor storageResourceAdaptor) {
        var protoToTokenMap = storageAdaptorCache.computeIfAbsent(computeResource, k -> new HashMap<>());
        var tokenToUserMap = protoToTokenMap.computeIfAbsent(dataMovementProtocol, k -> new HashMap<>());
        var userToAdaptorMap = tokenToUserMap.computeIfAbsent(authToken, k -> new HashMap<>());
        userToAdaptorMap.put(userId, storageResourceAdaptor);
    }

    public Optional<AgentAdaptor> getSSHAdaptor(
            String resourceId, String authToken, String gatewayUserId, String loginUserName) {
        var tokenToGatewayUserMap = sshAdaptorCache.get(resourceId);

        if (tokenToGatewayUserMap != null) {
            var gatewayUserToLoginUserMap = tokenToGatewayUserMap.get(authToken);

            if (gatewayUserToLoginUserMap != null) {
                var loginUserToAdaptorMap = gatewayUserToLoginUserMap.get(gatewayUserId);

                if (loginUserToAdaptorMap != null) {
                    return Optional.ofNullable(loginUserToAdaptorMap.get(loginUserName));
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

    public void putSSHAdaptor(
            String resourceId, String authToken, String gatewayUserId, String loginUserName, AgentAdaptor adaptor) {

        var tokenToGatewayUserMap = sshAdaptorCache.computeIfAbsent(resourceId, k -> new HashMap<>());
        var gatewayUserToLoginUserMap = tokenToGatewayUserMap.computeIfAbsent(authToken, k -> new HashMap<>());
        var loginUserToAdaptorMap = gatewayUserToLoginUserMap.computeIfAbsent(gatewayUserId, k -> new HashMap<>());

        loginUserToAdaptorMap.put(loginUserName, adaptor);
    }
}
