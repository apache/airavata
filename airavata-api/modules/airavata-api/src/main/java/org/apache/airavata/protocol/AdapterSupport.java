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
import org.apache.airavata.protocol.AgentAdapter.AgentException;

/**
 * Support for fetching agent adapters by gateway, resource, and protocol.
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public interface AdapterSupport {
    AgentAdapter fetchAdapter(
            String gatewayId, String computeResource, JobSubmissionProtocol protocol, String authToken, String userId)
            throws AgentException;

    AgentAdapter fetchStorageAdapter(String gatewayId, String storageResourceId, String authToken, String userId)
            throws AgentException;

    AgentAdapter fetchSSHAdapter(
            String gatewayId, String resourceId, String authToken, String gatewayUserId, String loginUserName)
            throws AgentException;
}
