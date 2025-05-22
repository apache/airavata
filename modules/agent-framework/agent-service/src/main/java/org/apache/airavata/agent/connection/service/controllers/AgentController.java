/*
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
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.airavata.agent.connection.service.controllers;

import javax.validation.Valid;

import org.apache.airavata.agent.TunnelCreationResponse;
import org.apache.airavata.agent.connection.service.handlers.AgentConnectionHandler;
import org.apache.airavata.agent.connection.service.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/agent")
public class AgentController {

    private final static Logger logger = LoggerFactory.getLogger(AgentController.class);
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
    public ResponseEntity<AgentTunnelCreateResponse> getTunnelCreteResponse(@PathVariable("executionId") String executionId) {
        logger.info("Received tunnel creation response for execution id {}", executionId);
        return ResponseEntity.accepted().body(agentConnectionHandler.getTunnelCreateResponse(executionId));
    }

    @PostMapping("/setup/tunnel")
    public ResponseEntity<AgentTunnelAck> runTunnelCreationOnAgent(@Valid @RequestBody AgentTunnelCreateRequest tunnelRequest) {
        logger.info("Received tunnel creation request to run on agent {}", tunnelRequest.getAgentId());
        return ResponseEntity.accepted().body(agentConnectionHandler.runTunnelOnAgent(tunnelRequest));
    }

    @PostMapping("/terminate/tunnel")
    public ResponseEntity<AgentTunnelAck> runTunnelCreationOnAgent(@Valid @RequestBody AgentTunnelTerminateRequest terminateRequest) {
        logger.info("Received tunnel termination request to run on agent {}", terminateRequest.getAgentId());
        return ResponseEntity.accepted().body(agentConnectionHandler.terminateTunnelOnAgent(terminateRequest));
    }

    @PostMapping("/setup/env")
    public ResponseEntity<AgentEnvSetupAck> runEnvSetupOnAgent(@Valid @RequestBody AgentEnvSetupRequest envSetupRequest) {
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
    public ResponseEntity<AgentKernelRestartAck> runKernelRestartOnAgent(@Valid @RequestBody AgentKernelRestartRequest kernelRestartRequest) {
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
    public ResponseEntity<AgentKernelRestartResponse> getKernelRestartResponse(@PathVariable("executionId") String executionId) {
        logger.info("Received kernel restart response for execution id {}", executionId);
        return ResponseEntity.accepted().body(agentConnectionHandler.getKernelRestartResponse(executionId));
    }

    @PostMapping("/execute/shell")
    public ResponseEntity<AgentCommandExecutionAck> runCommandOnAgent(@Valid @RequestBody AgentCommandExecutionRequest commandRequest) {
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
    public ResponseEntity<AgentCommandExecutionResponse> getExecutionResponse(@PathVariable("executionId") String executionId) {
        logger.info("Received command response for execution id {}", executionId);
        return ResponseEntity.accepted().body(agentConnectionHandler.getCommandExecutionResponse(executionId));
    }


    @PostMapping("/execute/asyncshell")
    public ResponseEntity<AgentCommandExecutionAck> runAsyncCommandOnAgent(@Valid @RequestBody AgentAsyncCommandExecutionRequest commandRequest) {
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
    public ResponseEntity<AgentAsyncCommandExecutionResponse> getAsyncExecutionResponse(@PathVariable("executionId") String executionId) {
        logger.info("Received async command response for execution id {}", executionId);
        return ResponseEntity.accepted().body(agentConnectionHandler.getAsyncCommandExecutionResponse(executionId));
    }

    @PostMapping("/list/asyncshell")
    public ResponseEntity<AgentCommandExecutionAck> listAsyncCommandOnAgent(
            @Valid @RequestBody AgentAsyncCommandListRequest commandListRequest) {
        logger.info("Received list async command request to run on agent {}", commandListRequest.getAgentId());
        if (agentConnectionHandler.isAgentUp(commandListRequest.getAgentId()).isAgentUp()) {
            return ResponseEntity.accepted().body(agentConnectionHandler.runAsyncCommandListOnAgent(commandListRequest));
        } else {
            logger.warn("No agent is available to run on agent {}", commandListRequest.getAgentId());
            AgentCommandExecutionAck ack = new AgentCommandExecutionAck();
            ack.setError("Agent not found");
            return ResponseEntity.accepted().body(ack);
        }
    }

    @GetMapping("/list/asyncshell/{executionId}")
    public ResponseEntity<AgentAsyncCommandListResponse> getAsyncCommandListResponse(@PathVariable("executionId") String executionId) {
        logger.info("Received list async command response for execution id {}", executionId);
        return ResponseEntity.accepted().body(agentConnectionHandler.getAsyncCommandListResponse(executionId));
    }

    @PostMapping("/terminate/asyncshell")
    public ResponseEntity<AgentCommandExecutionAck> terminateAsyncCommandOnAgent(
            @Valid @RequestBody AgentAsyncCommandTerminateRequest commandTerminateRequest) {
        logger.info("Received terminate async command request to run on agent {}", commandTerminateRequest.getAgentId());
        if (agentConnectionHandler.isAgentUp(commandTerminateRequest.getAgentId()).isAgentUp()) {
            return ResponseEntity.accepted().body(agentConnectionHandler.runAsyncCommandTerminateOnAgent(commandTerminateRequest));
        } else {
            logger.warn("No agent is available to run on agent {}", commandTerminateRequest.getAgentId());
            AgentCommandExecutionAck ack = new AgentCommandExecutionAck();
            ack.setError("Agent not found");
            return ResponseEntity.accepted().body(ack);
        }
    }

    @GetMapping("/terminate/asyncshell/{executionId}")
    public ResponseEntity<AgentAsyncCommandTerminateResponse> getAsyncCommandTerminateResponse(@PathVariable("executionId") String executionId) {
        logger.info("Received terminate async command response for execution id {}", executionId);
        return ResponseEntity.accepted().body(agentConnectionHandler.getAsyncCommandTerminateResponse(executionId));
    }

    @PostMapping("/execute/jupyter")
    public ResponseEntity<AgentJupyterExecutionAck> runJupyterOnAgent(@Valid @RequestBody AgentJupyterExecutionRequest executionRequest) {
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
    public ResponseEntity<AgentJupyterExecutionResponse> getJupyterResponse(@PathVariable("executionId") String executionId) {
        logger.info("Received jupyter execution response for execution id {}", executionId);
        return ResponseEntity.accepted().body(agentConnectionHandler.getJupyterExecutionResponse(executionId));
    }

    @PostMapping("/execute/python")
    public ResponseEntity<AgentPythonExecutionAck> runPythonOnAgent(@Valid @RequestBody AgentPythonExecutionRequest pythonRunRequest) {
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
    public ResponseEntity<AgentPythonExecutionResponse> getPythonResponse(@PathVariable("executionId") String executionId) {
        logger.info("Received python execution response for execution id {}", executionId);
        return ResponseEntity.accepted().body(agentConnectionHandler.getPythonExecutionResponse(executionId));
    }

}
