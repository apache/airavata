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
package org.apache.airavata.agent.service;

import org.apache.airavata.agent.model.AgentAsyncCommandExecutionRequest;
import org.apache.airavata.agent.model.AgentAsyncCommandExecutionResponse;
import org.apache.airavata.agent.model.AgentAsyncCommandListRequest;
import org.apache.airavata.agent.model.AgentAsyncCommandListResponse;
import org.apache.airavata.agent.model.AgentAsyncCommandTerminateRequest;
import org.apache.airavata.agent.model.AgentAsyncCommandTerminateResponse;
import org.apache.airavata.agent.model.AgentCommandExecutionAck;
import org.apache.airavata.agent.model.AgentCommandExecutionRequest;
import org.apache.airavata.agent.model.AgentCommandExecutionResponse;
import org.apache.airavata.agent.model.AgentEnvSetupAck;
import org.apache.airavata.agent.model.AgentEnvSetupRequest;
import org.apache.airavata.agent.model.AgentEnvSetupResponse;
import org.apache.airavata.agent.model.AgentInfoResponse;
import org.apache.airavata.agent.model.AgentJupyterExecutionAck;
import org.apache.airavata.agent.model.AgentJupyterExecutionRequest;
import org.apache.airavata.agent.model.AgentJupyterExecutionResponse;
import org.apache.airavata.agent.model.AgentKernelRestartAck;
import org.apache.airavata.agent.model.AgentKernelRestartRequest;
import org.apache.airavata.agent.model.AgentKernelRestartResponse;
import org.apache.airavata.agent.model.AgentPythonExecutionAck;
import org.apache.airavata.agent.model.AgentPythonExecutionRequest;
import org.apache.airavata.agent.model.AgentPythonExecutionResponse;
import org.apache.airavata.agent.model.AgentTunnelAck;
import org.apache.airavata.agent.model.AgentTunnelCreateRequest;
import org.apache.airavata.agent.model.AgentTunnelCreateResponse;
import org.apache.airavata.agent.model.AgentTunnelTerminateRequest;

/**
 * Interface for agent connection operations.
 *
 * <p>Defines the contract for managing agent communication — sending commands
 * to agents and retrieving responses. The gRPC-based implementation lives in
 * the {@code grpc-api} module; REST controllers in {@code rest-api} depend
 * only on this interface.
 */
public interface AgentConnectionService {

    AgentInfoResponse isAgentUp(String agentId);

    AgentEnvSetupResponse getEnvSetupResponse(String executionId);

    AgentCommandExecutionResponse getCommandExecutionResponse(String executionId);

    AgentAsyncCommandExecutionResponse getAsyncCommandExecutionResponse(String executionId);

    AgentAsyncCommandListResponse getAsyncCommandListResponse(String executionId);

    AgentAsyncCommandTerminateResponse getAsyncCommandTerminateResponse(String executionId);

    AgentJupyterExecutionResponse getJupyterExecutionResponse(String executionId);

    AgentKernelRestartResponse getKernelRestartResponse(String executionId);

    AgentPythonExecutionResponse getPythonExecutionResponse(String executionId);

    AgentTunnelCreateResponse getTunnelCreateResponse(String executionId);

    AgentEnvSetupAck runEnvSetupOnAgent(AgentEnvSetupRequest envSetupRequest);

    AgentCommandExecutionAck runCommandOnAgent(AgentCommandExecutionRequest commandRequest);

    AgentCommandExecutionAck runAsyncCommandOnAgent(AgentAsyncCommandExecutionRequest commandRequest);

    AgentCommandExecutionAck runAsyncCommandListOnAgent(AgentAsyncCommandListRequest commandRequest);

    AgentCommandExecutionAck runAsyncCommandTerminateOnAgent(AgentAsyncCommandTerminateRequest commandRequest);

    AgentJupyterExecutionAck runJupyterOnAgent(AgentJupyterExecutionRequest jupyterExecutionRequest);

    AgentPythonExecutionAck runPythonOnAgent(AgentPythonExecutionRequest pythonRunRequest);

    AgentTunnelAck terminateTunnelOnAgent(AgentTunnelTerminateRequest tunnelTerminateRequest);

    AgentTunnelAck runTunnelOnAgent(AgentTunnelCreateRequest tunnelRequest);

    AgentKernelRestartAck runKernelRestartOnAgent(AgentKernelRestartRequest kernelRestartRequest);
}
