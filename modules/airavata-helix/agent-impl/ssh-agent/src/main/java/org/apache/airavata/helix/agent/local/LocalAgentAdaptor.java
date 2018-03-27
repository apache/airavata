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
package org.apache.airavata.helix.agent.local;

import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.agents.api.AgentException;
import org.apache.airavata.agents.api.CommandOutput;

import java.util.List;

public class LocalAgentAdaptor implements AgentAdaptor {

    public void init(Object agentPams) throws AgentException {
        throw new AgentException("Operation not implemented");
    }

    @Override
    public void init(String computeResource, String gatewayId, String userId, String token) throws AgentException {
        throw new AgentException("Operation not implemented");
    }

    @Override
    public CommandOutput executeCommand(String command, String workingDirectory) throws AgentException {
        throw new AgentException("Operation not implemented");
    }

    @Override
    public void createDirectory(String path) throws AgentException {
        throw new AgentException("Operation not implemented");
    }

    @Override
    public void copyFileTo(String localFile, String remoteFile) throws AgentException {
        throw new AgentException("Operation not implemented");
    }

    @Override
    public void copyFileFrom(String remoteFile, String localFile) throws AgentException {
        throw new AgentException("Operation not implemented");
    }


    @Override
    public List<String> listDirectory(String path) throws AgentException {
        throw new AgentException("Operation not implemented");
    }

    @Override
    public List<String> getFileNameFromExtension(String fileName, String parentPath) throws AgentException {
        throw new AgentException("Operation not implemented");
    }
}
