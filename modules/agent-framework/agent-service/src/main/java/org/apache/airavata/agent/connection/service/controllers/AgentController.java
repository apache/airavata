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
package org.apache.airavata.agent.connection.service.controllers;

import org.apache.airavata.agent.connection.service.handlers.AgentConnectionHandler;
import org.apache.airavata.agent.connection.service.models.AgentAsyncCommandExecutionRequest;
import org.apache.airavata.agent.connection.service.models.AgentAsyncCommandExecutionResponse;
import org.apache.airavata.agent.connection.service.models.AgentAsyncCommandListRequest;
import org.apache.airavata.agent.connection.service.models.AgentAsyncCommandListResponse;
import org.apache.airavata.agent.connection.service.models.AgentAsyncCommandTerminateRequest;
import org.apache.airavata.agent.connection.service.models.AgentAsyncCommandTerminateResponse;
import org.apache.airavata.agent.connection.service.models.AgentCommandExecutionAck;
import org.apache.airavata.agent.connection.service.models.AgentCommandExecutionRequest;
import org.apache.airavata.agent.connection.service.models.AgentCommandExecutionResponse;
import org.apache.airavata.agent.connection.service.models.AgentEnvSetupAck;
import org.apache.airavata.agent.connection.service.models.AgentEnvSetupRequest;
import org.apache.airavata.agent.connection.service.models.AgentEnvSetupResponse;
import org.apache.airavata.agent.connection.service.models.AgentInfoResponse;
import org.apache.airavata.agent.connection.service.models.AgentJupyterExecutionAck;
import org.apache.airavata.agent.connection.service.models.AgentJupyterExecutionRequest;
import org.apache.airavata.agent.connection.service.models.AgentJupyterExecutionResponse;
import org.apache.airavata.agent.connection.service.models.AgentKernelRestartAck;
import org.apache.airavata.agent.connection.service.models.AgentKernelRestartRequest;
import org.apache.airavata.agent.connection.service.models.AgentKernelRestartResponse;
import org.apache.airavata.agent.connection.service.models.AgentPythonExecutionAck;
import org.apache.airavata.agent.connection.service.models.AgentPythonExecutionRequest;
import org.apache.airavata.agent.connection.service.models.AgentPythonExecutionResponse;
import org.apache.airavata.agent.connection.service.models.AgentTunnelAck;
import org.apache.airavata.agent.connection.service.models.AgentTunnelCreateRequest;
import org.apache.airavata.agent.connection.service.models.AgentTunnelCreateResponse;
import org.apache.airavata.agent.connection.service.models.AgentTunnelTerminateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Agent API Controller - Part of the unified HTTP server.
 *
 * <p>This controller provides HTTP endpoints for agent management and operations,
 * running as part of the unified HTTP server on port 8080 (configurable via
 * {@code airavata.services.http.server.port}).
 *
 * <p><b>External API:</b> This is part of one of four external API layers in Airavata:
 * <ul>
 *   <li>Thrift Server (port 8930) - Thrift Endpoints for Airavata API functions</li>
 *   <li>HTTP Server (port 8080):
 *       <ul>
 *         <li>Airavata API - HTTP Endpoints for Airavata API functions</li>
 *         <li>File API - HTTP Endpoints for file upload/download</li>
 *         <li>Agent API (this controller) - HTTP Endpoints for interactive job contexts</li>
 *         <li>Research API - HTTP Endpoints for use by research hub</li>
 *       </ul>
 *   </li>
 *   <li>gRPC Server (port 9090) - For airavata binaries to open persistent channels with airavata APIs</li>
 *   <li>Dapr gRPC (port 50001) - Sidecar for pub/sub, state, and workflow execution</li>
 * </ul>
 *
 * <p><b>Endpoints:</b> All endpoints are prefixed with {@code /api/v1/agent}:
 * <ul>
 *   <li>{@code GET /{agentId}} - Get agent information</li>
 *   <li>{@code POST /setup/tunnel} - Create TCP tunnel</li>
 *   <li>{@code GET /setup/tunnel/{executionId}} - Get tunnel creation response</li>
 *   <li>{@code POST /terminate/tunnel} - Terminate TCP tunnel</li>
 *   <li>{@code POST /setup/env} - Setup environment (conda/pip)</li>
 *   <li>{@code GET /setup/env/{executionId}} - Get environment setup response</li>
 *   <li>{@code POST /setup/restart} - Restart Jupyter kernel</li>
 *   <li>{@code GET /setup/restart/{executionId}} - Get kernel restart response</li>
 *   <li>{@code POST /execute/shell} - Execute shell command</li>
 *   <li>{@code GET /execute/shell/{executionId}} - Get command execution response</li>
 *   <li>{@code POST /execute/asyncshell} - Execute async shell command</li>
 *   <li>{@code GET /execute/asyncshell/{executionId}} - Get async command execution response</li>
 *   <li>{@code POST /list/asyncshell} - List async commands</li>
 *   <li>{@code GET /list/asyncshell/{executionId}} - Get async command list response</li>
 *   <li>{@code POST /terminate/asyncshell} - Terminate async command</li>
 *   <li>{@code GET /terminate/asyncshell/{executionId}} - Get terminate response</li>
 *   <li>{@code POST /execute/jupyter} - Execute Jupyter notebook cell</li>
 *   <li>{@code GET /execute/jupyter/{executionId}} - Get Jupyter execution response</li>
 *   <li>{@code POST /execute/python} - Execute Python script</li>
 *   <li>{@code GET /execute/python/{executionId}} - Get Python execution response</li>
 * </ul>
 *
 * <p><b>Relationship to gRPC:</b> This HTTP controller provides a RESTful interface to
 * agent operations. The primary agent communication protocol is gRPC (bidirectional streaming)
 * via {@link org.apache.airavata.agent.connection.service.handlers.AgentConnectionHandler},
 * which runs on the unified gRPC server (port 9090). This HTTP controller provides
 * synchronous request/response alternatives for some operations.
 *
 * <p><b>Configuration:</b>
 * <ul>
 *   <li>{@code airavata.services.agent.enabled} - Enable/disable Agent Service (default: false)</li>
 *   <li>{@code airavata.services.http.server.port} - Unified HTTP server port (default: 8080)</li>
 * </ul>
 *
 * @see org.apache.airavata.agent.connection.service.handlers.AgentConnectionHandler
 */
@RestController
@RequestMapping("/api/v1/agent")
public class AgentController {

    private static final Logger logger = LoggerFactory.getLogger(AgentController.class);
    private final AgentConnectionHandler agentConnectionHandler;

    public AgentController(AgentConnectionHandler agentConnectionHandler) {
        this.agentConnectionHandler = agentConnectionHandler;
    }

    @GetMapping("/{agentId}")
    public ResponseEntity<AgentInfoResponse> getAgentInfo(@PathVariable("agentId") String agentId) {
        logger.info("Received agent info request for agent {}", agentId);
        return ResponseEntity.accepted().body(agentConnectionHandler.isAgentUp(agentId));
    }

    @GetMapping("/setup/tunnel/{executionId}")
    public ResponseEntity<AgentTunnelCreateResponse> getTunnelCreteResponse(
            @PathVariable("executionId") String executionId) {
        logger.info("Received tunnel creation response for execution id {}", executionId);
        return ResponseEntity.accepted().body(agentConnectionHandler.getTunnelCreateResponse(executionId));
    }

    @PostMapping("/setup/tunnel")
    public ResponseEntity<AgentTunnelAck> runTunnelCreationOnAgent(
            @Validated @RequestBody AgentTunnelCreateRequest tunnelRequest) {
        logger.info("Received tunnel creation request to run on agent {}", tunnelRequest.getAgentId());
        return ResponseEntity.accepted().body(agentConnectionHandler.runTunnelOnAgent(tunnelRequest));
    }

    @PostMapping("/terminate/tunnel")
    public ResponseEntity<AgentTunnelAck> runTunnelCreationOnAgent(
            @Validated @RequestBody AgentTunnelTerminateRequest terminateRequest) {
        logger.info("Received tunnel termination request to run on agent {}", terminateRequest.getAgentId());
        return ResponseEntity.accepted().body(agentConnectionHandler.terminateTunnelOnAgent(terminateRequest));
    }

    @PostMapping("/setup/env")
    public ResponseEntity<AgentEnvSetupAck> runEnvSetupOnAgent(
            @Validated @RequestBody AgentEnvSetupRequest envSetupRequest) {
        logger.info("Received env setup request to run on agent {}", envSetupRequest.getAgentId());
        if (agentConnectionHandler.isAgentUp(envSetupRequest.getAgentId()).isAgentUp()) {
            return ResponseEntity.accepted().body(agentConnectionHandler.runEnvSetupOnAgent(envSetupRequest));
        } else {
            logger.warn("No agent is available to run on agent {}", envSetupRequest.getAgentId());
            AgentEnvSetupAck ack = new AgentEnvSetupAck();
            ack.setError("Agent not found");
            return ResponseEntity.accepted().body(ack);
        }
    }

    @GetMapping("/setup/env/{executionId}")
    public ResponseEntity<AgentEnvSetupResponse> getEnvSetupResponse(@PathVariable("executionId") String executionId) {
        logger.info("Received env setup response for execution id {}", executionId);
        return ResponseEntity.accepted().body(agentConnectionHandler.getEnvSetupResponse(executionId));
    }

    @PostMapping("/setup/restart")
    public ResponseEntity<AgentKernelRestartAck> runKernelRestartOnAgent(
            @Validated @RequestBody AgentKernelRestartRequest kernelRestartRequest) {
        logger.info("Received kernel restart request to run on agent {}", kernelRestartRequest.getAgentId());
        if (agentConnectionHandler.isAgentUp(kernelRestartRequest.getAgentId()).isAgentUp()) {
            return ResponseEntity.accepted().body(agentConnectionHandler.runKernelRestartOnAgent(kernelRestartRequest));
        } else {
            logger.warn("No agent is available to run on agent {}", kernelRestartRequest.getAgentId());
            AgentKernelRestartAck ack = new AgentKernelRestartAck();
            ack.setError("Agent not found");
            return ResponseEntity.accepted().body(ack);
        }
    }

    @GetMapping("/setup/restart/{executionId}")
    public ResponseEntity<AgentKernelRestartResponse> getKernelRestartResponse(
            @PathVariable("executionId") String executionId) {
        logger.info("Received kernel restart response for execution id {}", executionId);
        return ResponseEntity.accepted().body(agentConnectionHandler.getKernelRestartResponse(executionId));
    }

    @PostMapping("/execute/shell")
    public ResponseEntity<AgentCommandExecutionAck> runCommandOnAgent(
            @Validated @RequestBody AgentCommandExecutionRequest commandRequest) {
        logger.info("Received command request to run on agent {}", commandRequest.getAgentId());
        if (agentConnectionHandler.isAgentUp(commandRequest.getAgentId()).isAgentUp()) {
            return ResponseEntity.accepted().body(agentConnectionHandler.runCommandOnAgent(commandRequest));
        } else {
            logger.warn("No agent is available to run on agent {}", commandRequest.getAgentId());
            AgentCommandExecutionAck ack = new AgentCommandExecutionAck();
            ack.setError("Agent not found");
            return ResponseEntity.accepted().body(ack);
        }
    }

    @GetMapping("/execute/shell/{executionId}")
    public ResponseEntity<AgentCommandExecutionResponse> getExecutionResponse(
            @PathVariable("executionId") String executionId) {
        logger.info("Received command response for execution id {}", executionId);
        return ResponseEntity.accepted().body(agentConnectionHandler.getCommandExecutionResponse(executionId));
    }

    @PostMapping("/execute/asyncshell")
    public ResponseEntity<AgentCommandExecutionAck> runAsyncCommandOnAgent(
            @Validated @RequestBody AgentAsyncCommandExecutionRequest commandRequest) {
        logger.info("Received async command request to run on agent {}", commandRequest.getAgentId());
        if (agentConnectionHandler.isAgentUp(commandRequest.getAgentId()).isAgentUp()) {
            return ResponseEntity.accepted().body(agentConnectionHandler.runAsyncCommandOnAgent(commandRequest));
        } else {
            logger.warn("No agent is available to run on agent {}", commandRequest.getAgentId());
            AgentCommandExecutionAck ack = new AgentCommandExecutionAck();
            ack.setError("Agent not found");
            return ResponseEntity.accepted().body(ack);
        }
    }

    @GetMapping("/execute/asyncshell/{executionId}")
    public ResponseEntity<AgentAsyncCommandExecutionResponse> getAsyncExecutionResponse(
            @PathVariable("executionId") String executionId) {
        logger.info("Received async command response for execution id {}", executionId);
        return ResponseEntity.accepted().body(agentConnectionHandler.getAsyncCommandExecutionResponse(executionId));
    }

    @PostMapping("/list/asyncshell")
    public ResponseEntity<AgentCommandExecutionAck> listAsyncCommandOnAgent(
            @Validated @RequestBody AgentAsyncCommandListRequest commandListRequest) {
        logger.info("Received list async command request to run on agent {}", commandListRequest.getAgentId());
        if (agentConnectionHandler.isAgentUp(commandListRequest.getAgentId()).isAgentUp()) {
            return ResponseEntity.accepted()
                    .body(agentConnectionHandler.runAsyncCommandListOnAgent(commandListRequest));
        } else {
            logger.warn("No agent is available to run on agent {}", commandListRequest.getAgentId());
            AgentCommandExecutionAck ack = new AgentCommandExecutionAck();
            ack.setError("Agent not found");
            return ResponseEntity.accepted().body(ack);
        }
    }

    @GetMapping("/list/asyncshell/{executionId}")
    public ResponseEntity<AgentAsyncCommandListResponse> getAsyncCommandListResponse(
            @PathVariable("executionId") String executionId) {
        logger.info("Received list async command response for execution id {}", executionId);
        return ResponseEntity.accepted().body(agentConnectionHandler.getAsyncCommandListResponse(executionId));
    }

    @PostMapping("/terminate/asyncshell")
    public ResponseEntity<AgentCommandExecutionAck> terminateAsyncCommandOnAgent(
            @Validated @RequestBody AgentAsyncCommandTerminateRequest commandTerminateRequest) {
        logger.info(
                "Received terminate async command request to run on agent {}", commandTerminateRequest.getAgentId());
        if (agentConnectionHandler
                .isAgentUp(commandTerminateRequest.getAgentId())
                .isAgentUp()) {
            return ResponseEntity.accepted()
                    .body(agentConnectionHandler.runAsyncCommandTerminateOnAgent(commandTerminateRequest));
        } else {
            logger.warn("No agent is available to run on agent {}", commandTerminateRequest.getAgentId());
            AgentCommandExecutionAck ack = new AgentCommandExecutionAck();
            ack.setError("Agent not found");
            return ResponseEntity.accepted().body(ack);
        }
    }

    @GetMapping("/terminate/asyncshell/{executionId}")
    public ResponseEntity<AgentAsyncCommandTerminateResponse> getAsyncCommandTerminateResponse(
            @PathVariable("executionId") String executionId) {
        logger.info("Received terminate async command response for execution id {}", executionId);
        return ResponseEntity.accepted().body(agentConnectionHandler.getAsyncCommandTerminateResponse(executionId));
    }

    @PostMapping("/execute/jupyter")
    public ResponseEntity<AgentJupyterExecutionAck> runJupyterOnAgent(
            @Validated @RequestBody AgentJupyterExecutionRequest executionRequest) {
        logger.info("Received jupyter execution request to run on agent {}", executionRequest.getAgentId());
        if (agentConnectionHandler.isAgentUp(executionRequest.getAgentId()).isAgentUp()) {
            return ResponseEntity.accepted().body(agentConnectionHandler.runJupyterOnAgent(executionRequest));
        } else {
            logger.warn("No agent is available to run on agent {}", executionRequest.getAgentId());
            AgentJupyterExecutionAck ack = new AgentJupyterExecutionAck();
            ack.setError("Agent not found");
            return ResponseEntity.accepted().body(ack);
        }
    }

    @GetMapping("/execute/jupyter/{executionId}")
    public ResponseEntity<AgentJupyterExecutionResponse> getJupyterResponse(
            @PathVariable("executionId") String executionId) {
        logger.info("Received jupyter execution response for execution id {}", executionId);
        return ResponseEntity.accepted().body(agentConnectionHandler.getJupyterExecutionResponse(executionId));
    }

    @PostMapping("/execute/python")
    public ResponseEntity<AgentPythonExecutionAck> runPythonOnAgent(
            @Validated @RequestBody AgentPythonExecutionRequest pythonRunRequest) {
        logger.info("Received python execution request to run on agent {}", pythonRunRequest.getAgentId());
        if (agentConnectionHandler.isAgentUp(pythonRunRequest.getAgentId()).isAgentUp()) {
            return ResponseEntity.accepted().body(agentConnectionHandler.runPythonOnAgent(pythonRunRequest));
        } else {
            logger.warn("No agent is available to run on agent {}", pythonRunRequest.getAgentId());
            AgentPythonExecutionAck ack = new AgentPythonExecutionAck();
            ack.setError("Agent not found");
            return ResponseEntity.accepted().body(ack);
        }
    }

    @GetMapping("/execute/python/{executionId}")
    public ResponseEntity<AgentPythonExecutionResponse> getPythonResponse(
            @PathVariable("executionId") String executionId) {
        logger.info("Received python execution response for execution id {}", executionId);
        return ResponseEntity.accepted().body(agentConnectionHandler.getPythonExecutionResponse(executionId));
    }
}
