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
package org.apache.airavata.agent.connection.service.handlers;

import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.airavata.agent.*;
import org.apache.airavata.agent.AsyncCommand;
import org.apache.airavata.agent.connection.service.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

@GrpcService
public class AgentConnectionHandler extends AgentCommunicationServiceGrpc.AgentCommunicationServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(AgentConnectionHandler.class);

    // <streamId, StreamObserver>
    private final Map<String, StreamObserver<ServerMessage>> ACTIVE_STREAMS = new ConcurrentHashMap<>();

    // <agentId, streamId>
    private final Map<String, String> AGENT_STREAM_MAPPING = new ConcurrentHashMap<>();

    private final Map<String, EnvSetupResponse> ENV_SETUP_RESPONSE_CACHE = new ConcurrentHashMap<>();
    private final Map<String, CommandExecutionResponse> COMMAND_EXECUTION_RESPONSE_CACHE = new ConcurrentHashMap<>();
    private final Map<String, AsyncCommandExecutionResponse> ASYNC_COMMAND_EXECUTION_RESPONSE_CACHE =
            new ConcurrentHashMap<>();
    private final Map<String, AsyncCommandListResponse> ASYNC_COMMAND_LIST_RESPONSE_CACHE = new ConcurrentHashMap<>();
    private final Map<String, AsyncCommandTerminateResponse> ASYNC_COMMAND_TERMINATE_RESPONSE_CACHE =
            new ConcurrentHashMap<>();
    private final Map<String, JupyterExecutionResponse> JUPYTER_EXECUTION_RESPONSE_CACHE = new ConcurrentHashMap<>();
    private final Map<String, KernelRestartResponse> KERNEL_RESTART_RESPONSE_CACHE = new ConcurrentHashMap<>();
    private final Map<String, PythonExecutionResponse> PYTHON_EXECUTION_RESPONSE_CACHE = new ConcurrentHashMap<>();
    private final Map<String, TunnelCreationResponse> TUNNEL_CREATION_RESPONSE_CACHE = new ConcurrentHashMap<>();

    private final AgentWorkService agentWorkService;

    @Value("${airavata.tunnel.serverHost}")
    private String tunnelServerHost;

    @Value("${airavata.tunnel.serverPort}")
    private int tunnelServerPort;

    @Value("${airavata.tunnel.serverApiUrl}")
    private String tunnelServerApiUrl;

    @Value("${airavata.tunnel.serverToken}")
    private String tunnelServerToken;

    public AgentConnectionHandler(AgentWorkService agentWorkService) {
        this.agentWorkService = agentWorkService;
    }

    // response handling
    public AgentInfoResponse isAgentUp(String agentId) {
        if (AGENT_STREAM_MAPPING.containsKey(agentId)
                && ACTIVE_STREAMS.containsKey(AGENT_STREAM_MAPPING.get(agentId))) {
            return new AgentInfoResponse(agentId, true);
        } else {
            return new AgentInfoResponse(agentId, false);
        }
    }

    public AgentEnvSetupResponse getEnvSetupResponse(String executionId) {
        AgentEnvSetupResponse envCreationResponse = new AgentEnvSetupResponse();
        if (ENV_SETUP_RESPONSE_CACHE.containsKey(executionId)) {
            envCreationResponse.setStatus(
                    ENV_SETUP_RESPONSE_CACHE.get(executionId).getStatus());
            envCreationResponse.setExecutionId(executionId);
            envCreationResponse.setSetup(true);
            ENV_SETUP_RESPONSE_CACHE.remove(executionId);
        } else {
            envCreationResponse.setSetup(false);
        }
        return envCreationResponse;
    }

    public AgentCommandExecutionResponse getCommandExecutionResponse(String executionId) {
        AgentCommandExecutionResponse agentCommandResponse = new AgentCommandExecutionResponse();
        if (COMMAND_EXECUTION_RESPONSE_CACHE.containsKey(executionId)) {
            agentCommandResponse.setResponseString(
                    COMMAND_EXECUTION_RESPONSE_CACHE.get(executionId).getResponseString());
            agentCommandResponse.setExecutionId(executionId);
            agentCommandResponse.setExecuted(true);
            COMMAND_EXECUTION_RESPONSE_CACHE.remove(executionId);
        } else {
            agentCommandResponse.setExecuted(false);
        }
        return agentCommandResponse;
    }

    public AgentAsyncCommandExecutionResponse getAsyncCommandExecutionResponse(String executionId) {
        AgentAsyncCommandExecutionResponse agentCommandResponse = new AgentAsyncCommandExecutionResponse();
        if (ASYNC_COMMAND_EXECUTION_RESPONSE_CACHE.containsKey(executionId)) {
            AsyncCommandExecutionResponse asyncCommandExecutionResponse =
                    ASYNC_COMMAND_EXECUTION_RESPONSE_CACHE.get(executionId);
            agentCommandResponse.setProcessId(asyncCommandExecutionResponse.getProcessId());
            agentCommandResponse.setExecutionId(executionId);
            agentCommandResponse.setErrorMessage(asyncCommandExecutionResponse.getErrorMessage());
            ASYNC_COMMAND_EXECUTION_RESPONSE_CACHE.remove(executionId);
        } else {
            agentCommandResponse.setErrorMessage("Not Ready");
            agentCommandResponse.setProcessId(-1);
        }
        return agentCommandResponse;
    }

    public AgentAsyncCommandListResponse getAsyncCommandListResponse(String executionId) {
        AgentAsyncCommandListResponse agentCommandListResponse = new AgentAsyncCommandListResponse();
        if (ASYNC_COMMAND_LIST_RESPONSE_CACHE.containsKey(executionId)) {
            AsyncCommandListResponse asyncCommandListResponse = ASYNC_COMMAND_LIST_RESPONSE_CACHE.get(executionId);
            agentCommandListResponse.setExecutionId(executionId);
            List<AsyncCommand> commandsList = asyncCommandListResponse.getCommandsList();

            agentCommandListResponse.setCommands(commandsList.stream()
                    .map(c -> {
                        org.apache.airavata.agent.connection.service.models.AsyncCommand cmd =
                                new org.apache.airavata.agent.connection.service.models.AsyncCommand();
                        cmd.setProcessId(c.getProcessId());
                        cmd.setArguments(c.getArgumentsList());
                        return cmd;
                    })
                    .collect(Collectors.toList()));
            ASYNC_COMMAND_LIST_RESPONSE_CACHE.remove(executionId);
        } else {
            agentCommandListResponse.setError("Not Ready");
        }
        return agentCommandListResponse;
    }

    public AgentAsyncCommandTerminateResponse getAsyncCommandTerminateResponse(String executionId) {
        AgentAsyncCommandTerminateResponse agentAsyncCommandTerminateResponse =
                new AgentAsyncCommandTerminateResponse();
        if (ASYNC_COMMAND_TERMINATE_RESPONSE_CACHE.containsKey(executionId)) {
            AsyncCommandTerminateResponse asyncCommandTerminateResponse =
                    ASYNC_COMMAND_TERMINATE_RESPONSE_CACHE.get(executionId);
            agentAsyncCommandTerminateResponse.setExecutionId(executionId);
            agentAsyncCommandTerminateResponse.setStatus(asyncCommandTerminateResponse.getStatus());
            ASYNC_COMMAND_TERMINATE_RESPONSE_CACHE.remove(executionId);
        } else {
            agentAsyncCommandTerminateResponse.setStatus("Not Ready");
        }
        return agentAsyncCommandTerminateResponse;
    }

    public AgentJupyterExecutionResponse getJupyterExecutionResponse(String executionId) {
        AgentJupyterExecutionResponse executionResponse = new AgentJupyterExecutionResponse();
        if (JUPYTER_EXECUTION_RESPONSE_CACHE.containsKey(executionId)) {
            executionResponse.setResponseString(
                    JUPYTER_EXECUTION_RESPONSE_CACHE.get(executionId).getResponseString());
            executionResponse.setExecutionId(executionId);
            executionResponse.setExecuted(true);
            JUPYTER_EXECUTION_RESPONSE_CACHE.remove(executionId);
        } else {
            executionResponse.setExecuted(false);
        }
        return executionResponse;
    }

    public AgentKernelRestartResponse getKernelRestartResponse(String executionId) {
        AgentKernelRestartResponse kernelRestartResponse = new AgentKernelRestartResponse();
        if (KERNEL_RESTART_RESPONSE_CACHE.containsKey(executionId)) {
            kernelRestartResponse.setStatus(
                    KERNEL_RESTART_RESPONSE_CACHE.get(executionId).getStatus());
            kernelRestartResponse.setExecutionId(executionId);
            kernelRestartResponse.setRestarted(true);
            KERNEL_RESTART_RESPONSE_CACHE.remove(executionId);
        } else {
            kernelRestartResponse.setRestarted(false);
        }
        return kernelRestartResponse;
    }

    public AgentPythonExecutionResponse getPythonExecutionResponse(String executionId) {
        AgentPythonExecutionResponse runResponse = new AgentPythonExecutionResponse();
        if (PYTHON_EXECUTION_RESPONSE_CACHE.containsKey(executionId)) {
            runResponse.setExecutionId(executionId);
            runResponse.setExecuted(true);
            runResponse.setResponseString(
                    PYTHON_EXECUTION_RESPONSE_CACHE.get(executionId).getResponseString());
            PYTHON_EXECUTION_RESPONSE_CACHE.remove(executionId);
        } else {
            runResponse.setExecuted(false);
        }
        return runResponse;
    }

    // request handling
    public AgentEnvSetupAck runEnvSetupOnAgent(AgentEnvSetupRequest envSetupRequest) {
        String executionId = UUID.randomUUID().toString();
        AgentEnvSetupAck ack = new AgentEnvSetupAck();
        ack.setExecutionId(executionId);
        Optional<StreamObserver<ServerMessage>> agentStreamObserver =
                getAgentStreamObserver(envSetupRequest.getAgentId());
        if (agentStreamObserver.isPresent()) {
            try {
                logger.info("Running an env setup on agent {}", envSetupRequest.getAgentId());
                agentStreamObserver
                        .get()
                        .onNext(ServerMessage.newBuilder()
                                .setEnvSetupRequest(org.apache.airavata.agent.EnvSetupRequest.newBuilder()
                                        .setExecutionId(executionId)
                                        .setEnvName(envSetupRequest.getEnvName())
                                        .addAllLibraries(envSetupRequest.getLibraries())
                                        .addAllPip(envSetupRequest.getPip())
                                        .build())
                                .build());

            } catch (Exception e) {
                logger.error(
                        "Failed to submit env setup request {} on agent {}",
                        executionId,
                        envSetupRequest.getAgentId(),
                        e);
                ack.setError(e.getMessage());
            }
        } else {
            logger.warn("No agent found to run the env setup on agent {}", envSetupRequest.getAgentId());
            ack.setError("No agent found to run the env setup on agent " + envSetupRequest.getAgentId());
        }
        return ack;
    }

    public AgentCommandExecutionAck runCommandOnAgent(AgentCommandExecutionRequest commandRequest) {
        String executionId = UUID.randomUUID().toString();
        AgentCommandExecutionAck ack = new AgentCommandExecutionAck();
        ack.setExecutionId(executionId);
        if (AGENT_STREAM_MAPPING.containsKey(commandRequest.getAgentId())
                && ACTIVE_STREAMS.containsKey(AGENT_STREAM_MAPPING.get(commandRequest.getAgentId()))) {
            String streamId = AGENT_STREAM_MAPPING.get(commandRequest.getAgentId());
            StreamObserver<ServerMessage> streamObserver = ACTIVE_STREAMS.get(streamId);
            try {
                logger.info("Running a command on agent {}", commandRequest.getAgentId());
                streamObserver.onNext(ServerMessage.newBuilder()
                        .setCommandExecutionRequest(CommandExecutionRequest.newBuilder()
                                .setExecutionId(executionId)
                                .setEnvName(commandRequest.getEnvName())
                                .setWorkingDir(commandRequest.getWorkingDir())
                                .addAllArguments(commandRequest.getArguments())
                                .build())
                        .build());
            } catch (Exception e) {
                logger.error(
                        "Failed to submit command execution request {} on agent {}",
                        executionId,
                        commandRequest.getAgentId(),
                        e);
                ack.setError(e.getMessage());
            }
        } else {
            logger.warn("No agent found to run the command on agent {}", commandRequest.getAgentId());
            ack.setError("No agent found to run the command on agent " + commandRequest.getAgentId());
        }
        return ack;
    }

    public AgentCommandExecutionAck runAsyncCommandOnAgent(AgentAsyncCommandExecutionRequest commandRequest) {
        String executionId = UUID.randomUUID().toString();
        AgentCommandExecutionAck ack = new AgentCommandExecutionAck();
        ack.setExecutionId(executionId);
        if (AGENT_STREAM_MAPPING.containsKey(commandRequest.getAgentId())
                && ACTIVE_STREAMS.containsKey(AGENT_STREAM_MAPPING.get(commandRequest.getAgentId()))) {
            String streamId = AGENT_STREAM_MAPPING.get(commandRequest.getAgentId());
            StreamObserver<ServerMessage> streamObserver = ACTIVE_STREAMS.get(streamId);
            try {
                logger.info("Running an async command on agent {}", commandRequest.getAgentId());
                streamObserver.onNext(ServerMessage.newBuilder()
                        .setAsyncCommandExecutionRequest(AsyncCommandExecutionRequest.newBuilder()
                                .setExecutionId(executionId)
                                .setEnvName(commandRequest.getEnvName())
                                .setWorkingDir(commandRequest.getWorkingDir())
                                .addAllArguments(commandRequest.getArguments())
                                .build())
                        .build());
            } catch (Exception e) {
                logger.error(
                        "Failed to submit async command execution request {} on agent {}",
                        executionId,
                        commandRequest.getAgentId(),
                        e);
                ack.setError(e.getMessage());
            }
        } else {
            logger.warn("No agent found to run the async command on agent {}", commandRequest.getAgentId());
            ack.setError("No agent found to run the async command on agent " + commandRequest.getAgentId());
        }
        return ack;
    }

    public AgentCommandExecutionAck runAsyncCommandListOnAgent(AgentAsyncCommandListRequest commandRequest) {
        String executionId = UUID.randomUUID().toString();
        AgentCommandExecutionAck ack = new AgentCommandExecutionAck();
        ack.setExecutionId(executionId);
        if (AGENT_STREAM_MAPPING.containsKey(commandRequest.getAgentId())
                && ACTIVE_STREAMS.containsKey(AGENT_STREAM_MAPPING.get(commandRequest.getAgentId()))) {
            String streamId = AGENT_STREAM_MAPPING.get(commandRequest.getAgentId());
            StreamObserver<ServerMessage> streamObserver = ACTIVE_STREAMS.get(streamId);
            try {
                logger.info("Running an async command list on agent {}", commandRequest.getAgentId());
                streamObserver.onNext(ServerMessage.newBuilder()
                        .setAsyncCommandListRequest(AsyncCommandListRequest.newBuilder()
                                .setExecutionId(executionId)
                                .build())
                        .build());
            } catch (Exception e) {
                logger.error(
                        "Failed to submit async command list execution request {} on agent {}",
                        executionId,
                        commandRequest.getAgentId(),
                        e);
                ack.setError(e.getMessage());
            }
        } else {
            logger.warn("No agent found to run the async command list on agent {}", commandRequest.getAgentId());
            ack.setError("No agent found to run the async command list on agent " + commandRequest.getAgentId());
        }
        return ack;
    }

    public AgentCommandExecutionAck runAsyncCommandTerminateOnAgent(AgentAsyncCommandTerminateRequest commandRequest) {
        String executionId = UUID.randomUUID().toString();
        AgentCommandExecutionAck ack = new AgentCommandExecutionAck();
        ack.setExecutionId(executionId);
        if (AGENT_STREAM_MAPPING.containsKey(commandRequest.getAgentId())
                && ACTIVE_STREAMS.containsKey(AGENT_STREAM_MAPPING.get(commandRequest.getAgentId()))) {
            String streamId = AGENT_STREAM_MAPPING.get(commandRequest.getAgentId());
            StreamObserver<ServerMessage> streamObserver = ACTIVE_STREAMS.get(streamId);
            try {
                logger.info("Running an async command terminate on agent {}", commandRequest.getAgentId());
                streamObserver.onNext(ServerMessage.newBuilder()
                        .setAsyncCommandTerminateRequest(AsyncCommandTerminateRequest.newBuilder()
                                .setExecutionId(executionId)
                                .setProcessId(commandRequest.getProcessId())
                                .build())
                        .build());
            } catch (Exception e) {
                logger.error(
                        "Failed to submit async command terminate execution request {} on agent {}",
                        executionId,
                        commandRequest.getAgentId(),
                        e);
                ack.setError(e.getMessage());
            }
        } else {
            logger.warn("No agent found to run the async command terminate on agent {}", commandRequest.getAgentId());
            ack.setError("No agent found to run the async command terminate on agent " + commandRequest.getAgentId());
        }
        return ack;
    }

    public AgentJupyterExecutionAck runJupyterOnAgent(AgentJupyterExecutionRequest jupyterExecutionRequest) {
        String executionId = UUID.randomUUID().toString();
        AgentJupyterExecutionAck ack = new AgentJupyterExecutionAck();
        ack.setExecutionId(executionId);
        Optional<StreamObserver<ServerMessage>> agentStreamObserver =
                getAgentStreamObserver(jupyterExecutionRequest.getAgentId());
        if (agentStreamObserver.isPresent()) {
            try {
                logger.info("Running a jupyter on agent {}", jupyterExecutionRequest.getAgentId());
                agentStreamObserver
                        .get()
                        .onNext(ServerMessage.newBuilder()
                                .setJupyterExecutionRequest(
                                        org.apache.airavata.agent.JupyterExecutionRequest.newBuilder()
                                                .setExecutionId(executionId)
                                                .setEnvName(jupyterExecutionRequest.getEnvName())
                                                .setCode(jupyterExecutionRequest.getCode())
                                                .build())
                                .build());
            } catch (Exception e) {
                logger.error(
                        "Failed to submit jupyter execution request {} on agent {}",
                        executionId,
                        jupyterExecutionRequest.getAgentId(),
                        e);
                ack.setError(e.getMessage());
            }
        } else {
            logger.warn("No agent found to run jupyter execution on agent {}", jupyterExecutionRequest.getAgentId());
            ack.setError("No agent found to run jupyter execution on agent " + jupyterExecutionRequest.getAgentId());
        }
        return ack;
    }

    public AgentPythonExecutionAck runPythonOnAgent(AgentPythonExecutionRequest pythonRunRequest) {
        String executionId = UUID.randomUUID().toString();
        AgentPythonExecutionAck ack = new AgentPythonExecutionAck();
        ack.setExecutionId(executionId);
        Optional<StreamObserver<ServerMessage>> agentStreamObserver =
                getAgentStreamObserver(pythonRunRequest.getAgentId());
        if (agentStreamObserver.isPresent()) {
            try {
                logger.info("Running a python on agent {}", pythonRunRequest.getAgentId());
                agentStreamObserver
                        .get()
                        .onNext(ServerMessage.newBuilder()
                                .setPythonExecutionRequest(PythonExecutionRequest.newBuilder()
                                        .setExecutionId(executionId)
                                        .setEnvName(pythonRunRequest.getEnvName())
                                        .setWorkingDir(pythonRunRequest.getWorkingDir())
                                        .setCode(pythonRunRequest.getCode())
                                        .build())
                                .build());
            } catch (Exception e) {
                logger.error(
                        "Failed to submit python execution request {} on agent {}",
                        executionId,
                        pythonRunRequest.getAgentId(),
                        e);
                ack.setError(e.getMessage());
            }
        } else {
            logger.warn("No agent found to run python execution on agent {}", pythonRunRequest.getAgentId());
            ack.setError("No agent found to run python execution on agent " + pythonRunRequest.getAgentId());
        }
        return ack;
    }

    public AgentTunnelAck terminateTunnelOnAgent(AgentTunnelTerminateRequest tunnelTerminateRequest) {
        String executionId = UUID.randomUUID().toString();

        AgentTunnelAck ack = new AgentTunnelAck();
        ack.setExecutionId(executionId);
        if (AGENT_STREAM_MAPPING.containsKey(tunnelTerminateRequest.getAgentId())
                && ACTIVE_STREAMS.containsKey(AGENT_STREAM_MAPPING.get(tunnelTerminateRequest.getAgentId()))) {

            String agentId = AGENT_STREAM_MAPPING.get(tunnelTerminateRequest.getAgentId());
            StreamObserver<ServerMessage> streamObserver = ACTIVE_STREAMS.get(agentId);
            try {
                streamObserver.onNext(ServerMessage.newBuilder()
                        .setTunnelTerminationRequest(TunnelTerminationRequest.newBuilder()
                                .setTunnelId(tunnelTerminateRequest.getTunnelId())
                                .setExecutionId(executionId)
                                .build())
                        .build());
            } catch (Exception e) {
                logger.error("Failed to submit tunnel termination request on agent {}", agentId, e);
                ack.setError(e.getMessage());
            }

        } else {
            logger.warn("No agent found to terminate the tunnel for agent id ", tunnelTerminateRequest.getAgentId());
            ack.setError("No agent found to terminate the tunnel for agent id " + tunnelTerminateRequest.getAgentId());
        }

        return ack;
    }

    public AgentTunnelAck runTunnelOnAgent(AgentTunnelCreateRequest tunnelRequest) {
        String executionId = UUID.randomUUID().toString();

        AgentTunnelAck ack = new AgentTunnelAck();
        ack.setExecutionId(executionId);
        if (AGENT_STREAM_MAPPING.containsKey(tunnelRequest.getAgentId())
                && ACTIVE_STREAMS.containsKey(AGENT_STREAM_MAPPING.get(tunnelRequest.getAgentId()))) {
            String agentId = AGENT_STREAM_MAPPING.get(tunnelRequest.getAgentId());
            StreamObserver<ServerMessage> streamObserver = ACTIVE_STREAMS.get(agentId);
            try {
                streamObserver.onNext(ServerMessage.newBuilder()
                        .setTunnelCreationRequest(TunnelCreationRequest.newBuilder()
                                .setExecutionId(executionId)
                                .setLocalPort(tunnelRequest.getLocalPort())
                                .setLocalBindHost(tunnelRequest.getLocalBindHost())
                                .setTunnelServerHost(tunnelServerHost)
                                .setTunnelServerPort(tunnelServerPort)
                                .setTunnelServerApiUrl(tunnelServerApiUrl)
                                .setTunnelServerToken(tunnelServerToken)
                                .build())
                        .build());
            } catch (Exception e) {
                logger.error("Failed to submit tunnel creation request on agent {}", agentId, e);
                ack.setError(e.getMessage());
            }
        } else {
            logger.warn("No agent found to run the tunnel for agent id ", tunnelRequest.getAgentId());
            ack.setError("No agent found to run the tunnel for agent id " + tunnelRequest.getAgentId());
        }
        return ack;
    }

    public AgentTunnelCreateResponse getTunnelCreateResponse(String executionId) {
        AgentTunnelCreateResponse tunnelResponse = new AgentTunnelCreateResponse();
        if (TUNNEL_CREATION_RESPONSE_CACHE.containsKey(executionId)) {
            TunnelCreationResponse tunnelCreationResponse = TUNNEL_CREATION_RESPONSE_CACHE.get(executionId);
            tunnelResponse.setTunnelId(tunnelCreationResponse.getTunnelId());
            tunnelResponse.setExecutionId(executionId);
            tunnelResponse.setProxyHost(tunnelCreationResponse.getTunnelHost());
            tunnelResponse.setPoxyPort(tunnelCreationResponse.getTunnelPort());
            tunnelResponse.setStatus(tunnelCreationResponse.getStatus());
            TUNNEL_CREATION_RESPONSE_CACHE.remove(executionId);
        } else {
            tunnelResponse.setStatus("Pending");
        }
        return tunnelResponse;
    }

    public AgentKernelRestartAck runKernelRestartOnAgent(AgentKernelRestartRequest kernelRestartRequest) {
        String executionId = UUID.randomUUID().toString();
        AgentKernelRestartAck ack = new AgentKernelRestartAck();
        ack.setExecutionId(executionId);
        Optional<StreamObserver<ServerMessage>> agentStreamObserver =
                getAgentStreamObserver(kernelRestartRequest.getAgentId());
        if (agentStreamObserver.isPresent()) {
            try {
                logger.info("restarting kernel on env {}...", kernelRestartRequest.getEnvName());
                agentStreamObserver
                        .get()
                        .onNext(ServerMessage.newBuilder()
                                .setKernelRestartRequest(KernelRestartRequest.newBuilder()
                                        .setExecutionId(executionId)
                                        .setEnvName(kernelRestartRequest.getEnvName())
                                        .build())
                                .build());
            } catch (Exception e) {
                logger.error(
                        "{} Failed to restart kernel on env {}!", executionId, kernelRestartRequest.getEnvName(), e);
                ack.setError(e.getMessage());
            }
        } else {
            logger.warn("No agent found to run the kernel restart on agent {}", kernelRestartRequest.getAgentId());
            ack.setError("No agent found to run the kernel restart on agent " + kernelRestartRequest.getAgentId());
        }
        return ack;
    }

    // internal handlers
    private void handleAgentPing(AgentPing agentPing, String streamId) {
        logger.info("Received agent ping for agent id {}", agentPing.getAgentId());
        AGENT_STREAM_MAPPING.put(agentPing.getAgentId(), streamId);
    }

    private void handleEnvSetupResponse(EnvSetupResponse envSetupResponse) {
        logger.info("Received env setup response for execution id {}", envSetupResponse.getExecutionId());
        ENV_SETUP_RESPONSE_CACHE.put(envSetupResponse.getExecutionId(), envSetupResponse);
    }

    private void handleTunnelCreationResponse(TunnelCreationResponse tunnelCreationResponse) {
        logger.info("Received tunnel creation response for execution id {}", tunnelCreationResponse.getExecutionId());
        TUNNEL_CREATION_RESPONSE_CACHE.put(tunnelCreationResponse.getExecutionId(), tunnelCreationResponse);
    }

    private void handleTunnelTerminationResponse(TunnelTerminationResponse tunnelTerminationResponse) {
        logger.info(
                "Received tunnel termination response for execution id {}", tunnelTerminationResponse.getExecutionId());
    }

    private void handleCommandExecutionResponse(CommandExecutionResponse commandExecutionResponse) {
        logger.info(
                "Received command execution response for execution id {}", commandExecutionResponse.getExecutionId());
        COMMAND_EXECUTION_RESPONSE_CACHE.put(commandExecutionResponse.getExecutionId(), commandExecutionResponse);
    }

    private void handleAsyncCommandExecutionResponse(AsyncCommandExecutionResponse commandExecutionResponse) {
        logger.info(
                "Received async command execution response for execution id {}",
                commandExecutionResponse.getExecutionId());
        ASYNC_COMMAND_EXECUTION_RESPONSE_CACHE.put(commandExecutionResponse.getExecutionId(), commandExecutionResponse);
    }

    private void handleAsyncCommandListResponse(AsyncCommandListResponse commandListResponse) {
        logger.info("Received async command list response for execution id {}", commandListResponse.getExecutionId());
        ASYNC_COMMAND_LIST_RESPONSE_CACHE.put(commandListResponse.getExecutionId(), commandListResponse);
    }

    private void handleAsyncCommandTerminateResponse(AsyncCommandTerminateResponse commandTerminateResponse) {
        logger.info(
                "Received async command terminate response for execution id {}",
                commandTerminateResponse.getExecutionId());
        ASYNC_COMMAND_TERMINATE_RESPONSE_CACHE.put(commandTerminateResponse.getExecutionId(), commandTerminateResponse);
    }

    private void handleJupyterExecutionResponse(JupyterExecutionResponse executionResponse) {
        logger.info("Received jupyter execution response for execution id {}", executionResponse.getExecutionId());
        JUPYTER_EXECUTION_RESPONSE_CACHE.put(executionResponse.getExecutionId(), executionResponse);
    }

    private void handleKernelRestartResponse(KernelRestartResponse kernelRestartResponse) {
        logger.info("Received kernel restart response for execution id {}", kernelRestartResponse.getExecutionId());
        KERNEL_RESTART_RESPONSE_CACHE.put(kernelRestartResponse.getExecutionId(), kernelRestartResponse);
    }

    private void handlePythonExecutionResponse(PythonExecutionResponse executionResponse) {
        logger.info("Received python execution response for execution id {}", executionResponse.getExecutionId());
        PYTHON_EXECUTION_RESPONSE_CACHE.put(executionResponse.getExecutionId(), executionResponse);
    }

    private void handleNextJobUnitRequest(String agentId, StreamObserver<ServerMessage> responseObserver) {
        logger.info("Received next job unit request for agent id {}", agentId);
        AssignResult assignResult = agentWorkService.assignNextForAgent(agentId);

        try {
            if (assignResult instanceof AssignResult.Assigned assigned) {
                responseObserver.onNext(ServerMessage.newBuilder()
                        .setAssignJobUnit(AssignJobUnit.newBuilder()
                                .setJobUnitId(assigned.jobUnitId())
                                .setResolvedCommand(assigned.resolvedCommand())
                                .build())
                        .build());
                logger.info("Assigned job unit id {} to agent id {}", assigned.jobUnitId(), agentId);

            } else if (assignResult instanceof AssignResult.NoWork noWork) {
                responseObserver.onNext(ServerMessage.newBuilder()
                        .setNoJobUnitAvailable(NoJobUnitAvailable.newBuilder()
                                .setReason(noWork.reason())
                                .build())
                        .build());
                logger.info("No job unit available for agent id {}, reason: {}", agentId, noWork.reason());
            }
        } catch (Exception e) {
            logger.error("Failed to send reply to agent {}", agentId, e);
        }
    }

    private void handleJobUnitCompletion(String jobUnitId) {
        agentWorkService.markCompleted(jobUnitId);
    }

    // routing
    private Optional<StreamObserver<ServerMessage>> getAgentStreamObserver(String agentId) {
        if (AGENT_STREAM_MAPPING.containsKey(agentId)
                && ACTIVE_STREAMS.containsKey(AGENT_STREAM_MAPPING.get(agentId))) {
            String streamId = AGENT_STREAM_MAPPING.get(agentId);
            StreamObserver<ServerMessage> streamObserver = ACTIVE_STREAMS.get(streamId);
            return Optional.ofNullable(streamObserver);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public StreamObserver<AgentMessage> createMessageBus(StreamObserver<ServerMessage> responseObserver) {

        String streamId = UUID.randomUUID().toString();
        ACTIVE_STREAMS.put(streamId, responseObserver);

        return new StreamObserver<AgentMessage>() {
            @Override
            public void onNext(AgentMessage request) {

                switch (request.getMessageCase()) {
                    case AGENTPING -> {
                        handleAgentPing(request.getAgentPing(), streamId);
                    }
                    case COMMANDEXECUTIONRESPONSE -> {
                        handleCommandExecutionResponse(request.getCommandExecutionResponse());
                    }
                    case JUPYTEREXECUTIONRESPONSE -> {
                        handleJupyterExecutionResponse(request.getJupyterExecutionResponse());
                    }
                    case KERNELRESTARTRESPONSE -> {
                        handleKernelRestartResponse(request.getKernelRestartResponse());
                    }
                    case PYTHONEXECUTIONRESPONSE -> {
                        handlePythonExecutionResponse(request.getPythonExecutionResponse());
                    }
                    case ENVSETUPRESPONSE -> {
                        handleEnvSetupResponse(request.getEnvSetupResponse());
                    }
                    case TUNNELCREATIONRESPONSE -> {
                        handleTunnelCreationResponse(request.getTunnelCreationResponse());
                    }
                    case TUNNELTERMINATIONRESPONSE -> {}

                    case ASYNCCOMMANDEXECUTIONRESPONSE -> {
                        handleAsyncCommandExecutionResponse(request.getAsyncCommandExecutionResponse());
                    }

                    case ASYNCCOMMANDLISTRESPONSE -> {
                        handleAsyncCommandListResponse(request.getAsyncCommandListResponse());
                    }

                    case ASYNCCOMMANDTERMINATERESPONSE -> {
                        handleAsyncCommandTerminateResponse(request.getAsyncCommandTerminateResponse());
                    }

                    case REQUESTNEXTJOBUNIT -> {
                        handleNextJobUnitRequest(request.getRequestNextJobUnit().getAgentId(), responseObserver);
                    }

                    case JOBUNITCOMPLETED -> {
                        handleJobUnitCompletion(request.getJobUnitCompleted().getJobUnitId());
                    }
                }
            }

            @Override
            public void onError(Throwable t) {
                logger.warn("Error in processing stream {}. Removing the stream tracking from cache", streamId, t);
                ACTIVE_STREAMS.remove(streamId);
            }

            @Override
            public void onCompleted() {
                logger.info("Stream {} is completed", streamId);
                responseObserver.onCompleted();
                ACTIVE_STREAMS.remove(streamId);
            }
        };
    }
}
