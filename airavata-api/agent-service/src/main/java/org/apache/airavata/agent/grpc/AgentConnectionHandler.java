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
package org.apache.airavata.agent.grpc;

import io.grpc.stub.StreamObserver;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.apache.airavata.agent.*;
import org.apache.airavata.agent.config.AgentProperties;
import org.apache.airavata.agent.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AgentConnectionHandler extends AgentCommunicationServiceGrpc.AgentCommunicationServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(AgentConnectionHandler.class);

    private final AgentProperties agentProperties;

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

    public AgentConnectionHandler(AgentProperties agentProperties) {
        this.agentProperties = agentProperties;
    }

    // response handling
    public AgentInfoResponse isAgentUp(String agentId) {
        boolean up = AGENT_STREAM_MAPPING.containsKey(agentId)
                && ACTIVE_STREAMS.containsKey(AGENT_STREAM_MAPPING.get(agentId));
        return AgentInfoResponse.newBuilder()
                .setAgentId(agentId)
                .setIsAgentUp(up)
                .build();
    }

    public AgentEnvSetupResponse getEnvSetupResponse(String executionId) {
        if (ENV_SETUP_RESPONSE_CACHE.containsKey(executionId)) {
            AgentEnvSetupResponse response = AgentEnvSetupResponse.newBuilder()
                    .setStatus(ENV_SETUP_RESPONSE_CACHE.get(executionId).getStatus())
                    .setExecutionId(executionId)
                    .setSetup(true)
                    .build();
            ENV_SETUP_RESPONSE_CACHE.remove(executionId);
            return response;
        } else {
            return AgentEnvSetupResponse.newBuilder().setSetup(false).build();
        }
    }

    public AgentCommandExecutionResponse getCommandExecutionResponse(String executionId) {
        if (COMMAND_EXECUTION_RESPONSE_CACHE.containsKey(executionId)) {
            AgentCommandExecutionResponse response = AgentCommandExecutionResponse.newBuilder()
                    .setResponseString(
                            COMMAND_EXECUTION_RESPONSE_CACHE.get(executionId).getResponseString())
                    .setExecutionId(executionId)
                    .setExecuted(true)
                    .build();
            COMMAND_EXECUTION_RESPONSE_CACHE.remove(executionId);
            return response;
        } else {
            return AgentCommandExecutionResponse.newBuilder().setExecuted(false).build();
        }
    }

    public AgentAsyncCommandExecutionResponse getAsyncCommandExecutionResponse(String executionId) {
        if (ASYNC_COMMAND_EXECUTION_RESPONSE_CACHE.containsKey(executionId)) {
            AsyncCommandExecutionResponse cached = ASYNC_COMMAND_EXECUTION_RESPONSE_CACHE.get(executionId);
            AgentAsyncCommandExecutionResponse response = AgentAsyncCommandExecutionResponse.newBuilder()
                    .setProcessId(cached.getProcessId())
                    .setExecutionId(executionId)
                    .setErrorMessage(cached.getErrorMessage())
                    .build();
            ASYNC_COMMAND_EXECUTION_RESPONSE_CACHE.remove(executionId);
            return response;
        } else {
            return AgentAsyncCommandExecutionResponse.newBuilder()
                    .setErrorMessage("Not Ready")
                    .setProcessId(-1)
                    .build();
        }
    }

    public AgentAsyncCommandListResponse getAsyncCommandListResponse(String executionId) {
        if (ASYNC_COMMAND_LIST_RESPONSE_CACHE.containsKey(executionId)) {
            AsyncCommandListResponse cached = ASYNC_COMMAND_LIST_RESPONSE_CACHE.get(executionId);
            AgentAsyncCommandListResponse response = AgentAsyncCommandListResponse.newBuilder()
                    .setExecutionId(executionId)
                    .addAllCommands(cached.getCommandsList().stream()
                            .map(c -> AgentAsyncCommand.newBuilder()
                                    .setProcessId(c.getProcessId())
                                    .addAllArguments(c.getArgumentsList())
                                    .build())
                            .collect(Collectors.toList()))
                    .build();
            ASYNC_COMMAND_LIST_RESPONSE_CACHE.remove(executionId);
            return response;
        } else {
            return AgentAsyncCommandListResponse.newBuilder()
                    .setError("Not Ready")
                    .build();
        }
    }

    public AgentAsyncCommandTerminateResponse getAsyncCommandTerminateResponse(String executionId) {
        if (ASYNC_COMMAND_TERMINATE_RESPONSE_CACHE.containsKey(executionId)) {
            AsyncCommandTerminateResponse cached = ASYNC_COMMAND_TERMINATE_RESPONSE_CACHE.get(executionId);
            AgentAsyncCommandTerminateResponse response = AgentAsyncCommandTerminateResponse.newBuilder()
                    .setExecutionId(executionId)
                    .setStatus(cached.getStatus())
                    .build();
            ASYNC_COMMAND_TERMINATE_RESPONSE_CACHE.remove(executionId);
            return response;
        } else {
            return AgentAsyncCommandTerminateResponse.newBuilder()
                    .setStatus("Not Ready")
                    .build();
        }
    }

    public AgentJupyterExecutionResponse getJupyterExecutionResponse(String executionId) {
        if (JUPYTER_EXECUTION_RESPONSE_CACHE.containsKey(executionId)) {
            AgentJupyterExecutionResponse response = AgentJupyterExecutionResponse.newBuilder()
                    .setResponseString(
                            JUPYTER_EXECUTION_RESPONSE_CACHE.get(executionId).getResponseString())
                    .setExecutionId(executionId)
                    .setExecuted(true)
                    .build();
            JUPYTER_EXECUTION_RESPONSE_CACHE.remove(executionId);
            return response;
        } else {
            return AgentJupyterExecutionResponse.newBuilder().setExecuted(false).build();
        }
    }

    public AgentKernelRestartResponse getKernelRestartResponse(String executionId) {
        if (KERNEL_RESTART_RESPONSE_CACHE.containsKey(executionId)) {
            AgentKernelRestartResponse response = AgentKernelRestartResponse.newBuilder()
                    .setStatus(KERNEL_RESTART_RESPONSE_CACHE.get(executionId).getStatus())
                    .setExecutionId(executionId)
                    .setRestarted(true)
                    .build();
            KERNEL_RESTART_RESPONSE_CACHE.remove(executionId);
            return response;
        } else {
            return AgentKernelRestartResponse.newBuilder().setRestarted(false).build();
        }
    }

    public AgentPythonExecutionResponse getPythonExecutionResponse(String executionId) {
        if (PYTHON_EXECUTION_RESPONSE_CACHE.containsKey(executionId)) {
            AgentPythonExecutionResponse response = AgentPythonExecutionResponse.newBuilder()
                    .setExecutionId(executionId)
                    .setExecuted(true)
                    .setResponseString(
                            PYTHON_EXECUTION_RESPONSE_CACHE.get(executionId).getResponseString())
                    .build();
            PYTHON_EXECUTION_RESPONSE_CACHE.remove(executionId);
            return response;
        } else {
            return AgentPythonExecutionResponse.newBuilder().setExecuted(false).build();
        }
    }

    // request handling
    public AgentExecutionAck runEnvSetupOnAgent(AgentEnvSetupRequest envSetupRequest) {
        String executionId = UUID.randomUUID().toString();
        AgentExecutionAck.Builder ackBuilder = AgentExecutionAck.newBuilder().setExecutionId(executionId);
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
                                        .addAllLibraries(envSetupRequest.getLibrariesList())
                                        .addAllPip(envSetupRequest.getPipList())
                                        .build())
                                .build());

            } catch (Exception e) {
                logger.error(
                        "Failed to submit env setup request {} on agent {}",
                        executionId,
                        envSetupRequest.getAgentId(),
                        e);
                ackBuilder.setError(e.getMessage());
            }
        } else {
            logger.warn("No agent found to run the env setup on agent {}", envSetupRequest.getAgentId());
            ackBuilder.setError("No agent found to run the env setup on agent " + envSetupRequest.getAgentId());
        }
        return ackBuilder.build();
    }

    public AgentExecutionAck runCommandOnAgent(AgentCommandExecutionRequest commandRequest) {
        String executionId = UUID.randomUUID().toString();
        AgentExecutionAck.Builder ackBuilder = AgentExecutionAck.newBuilder().setExecutionId(executionId);
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
                                .addAllArguments(commandRequest.getArgumentsList())
                                .build())
                        .build());
            } catch (Exception e) {
                logger.error(
                        "Failed to submit command execution request {} on agent {}",
                        executionId,
                        commandRequest.getAgentId(),
                        e);
                ackBuilder.setError(e.getMessage());
            }
        } else {
            logger.warn("No agent found to run the command on agent {}", commandRequest.getAgentId());
            ackBuilder.setError("No agent found to run the command on agent " + commandRequest.getAgentId());
        }
        return ackBuilder.build();
    }

    public AgentExecutionAck runAsyncCommandOnAgent(AgentAsyncCommandExecutionRequest commandRequest) {
        String executionId = UUID.randomUUID().toString();
        AgentExecutionAck.Builder ackBuilder = AgentExecutionAck.newBuilder().setExecutionId(executionId);
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
                                .addAllArguments(commandRequest.getArgumentsList())
                                .build())
                        .build());
            } catch (Exception e) {
                logger.error(
                        "Failed to submit async command execution request {} on agent {}",
                        executionId,
                        commandRequest.getAgentId(),
                        e);
                ackBuilder.setError(e.getMessage());
            }
        } else {
            logger.warn("No agent found to run the async command on agent {}", commandRequest.getAgentId());
            ackBuilder.setError("No agent found to run the async command on agent " + commandRequest.getAgentId());
        }
        return ackBuilder.build();
    }

    public AgentExecutionAck runAsyncCommandListOnAgent(AgentAsyncCommandListRequest commandRequest) {
        String executionId = UUID.randomUUID().toString();
        AgentExecutionAck.Builder ackBuilder = AgentExecutionAck.newBuilder().setExecutionId(executionId);
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
                ackBuilder.setError(e.getMessage());
            }
        } else {
            logger.warn("No agent found to run the async command list on agent {}", commandRequest.getAgentId());
            ackBuilder.setError("No agent found to run the async command list on agent " + commandRequest.getAgentId());
        }
        return ackBuilder.build();
    }

    public AgentExecutionAck runAsyncCommandTerminateOnAgent(AgentAsyncCommandTerminateRequest commandRequest) {
        String executionId = UUID.randomUUID().toString();
        AgentExecutionAck.Builder ackBuilder = AgentExecutionAck.newBuilder().setExecutionId(executionId);
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
                ackBuilder.setError(e.getMessage());
            }
        } else {
            logger.warn("No agent found to run the async command terminate on agent {}", commandRequest.getAgentId());
            ackBuilder.setError(
                    "No agent found to run the async command terminate on agent " + commandRequest.getAgentId());
        }
        return ackBuilder.build();
    }

    public AgentExecutionAck runJupyterOnAgent(AgentJupyterExecutionRequest jupyterExecutionRequest) {
        String executionId = UUID.randomUUID().toString();
        AgentExecutionAck.Builder ackBuilder = AgentExecutionAck.newBuilder().setExecutionId(executionId);
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
                ackBuilder.setError(e.getMessage());
            }
        } else {
            logger.warn("No agent found to run jupyter execution on agent {}", jupyterExecutionRequest.getAgentId());
            ackBuilder.setError(
                    "No agent found to run jupyter execution on agent " + jupyterExecutionRequest.getAgentId());
        }
        return ackBuilder.build();
    }

    public AgentExecutionAck runPythonOnAgent(AgentPythonExecutionRequest pythonRunRequest) {
        String executionId = UUID.randomUUID().toString();
        AgentExecutionAck.Builder ackBuilder = AgentExecutionAck.newBuilder().setExecutionId(executionId);
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
                ackBuilder.setError(e.getMessage());
            }
        } else {
            logger.warn("No agent found to run python execution on agent {}", pythonRunRequest.getAgentId());
            ackBuilder.setError("No agent found to run python execution on agent " + pythonRunRequest.getAgentId());
        }
        return ackBuilder.build();
    }

    public AgentTunnelAck terminateTunnelOnAgent(AgentTunnelTerminateRequest tunnelTerminateRequest) {
        String executionId = UUID.randomUUID().toString();

        AgentTunnelAck.Builder ackBuilder = AgentTunnelAck.newBuilder().setExecutionId(executionId);
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
                ackBuilder.setError(e.getMessage());
            }

        } else {
            logger.warn("No agent found to terminate the tunnel for agent id ", tunnelTerminateRequest.getAgentId());
            ackBuilder.setError(
                    "No agent found to terminate the tunnel for agent id " + tunnelTerminateRequest.getAgentId());
        }

        return ackBuilder.build();
    }

    public AgentTunnelAck runTunnelOnAgent(AgentTunnelCreateRequest tunnelRequest) {
        String executionId = UUID.randomUUID().toString();

        AgentTunnelAck.Builder ackBuilder = AgentTunnelAck.newBuilder().setExecutionId(executionId);
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
                                .setTunnelServerHost(agentProperties.getTunnel().getServerHost())
                                .setTunnelServerPort(agentProperties.getTunnel().getServerPort())
                                .setTunnelServerApiUrl(
                                        agentProperties.getTunnel().getServerApiUrl())
                                .setTunnelServerToken(
                                        agentProperties.getTunnel().getServerToken())
                                .build())
                        .build());
            } catch (Exception e) {
                logger.error("Failed to submit tunnel creation request on agent {}", agentId, e);
                ackBuilder.setError(e.getMessage());
            }
        } else {
            logger.warn("No agent found to run the tunnel for agent id ", tunnelRequest.getAgentId());
            ackBuilder.setError("No agent found to run the tunnel for agent id " + tunnelRequest.getAgentId());
        }
        return ackBuilder.build();
    }

    public AgentTunnelCreateResponse getTunnelCreateResponse(String executionId) {
        if (TUNNEL_CREATION_RESPONSE_CACHE.containsKey(executionId)) {
            TunnelCreationResponse cached = TUNNEL_CREATION_RESPONSE_CACHE.get(executionId);
            AgentTunnelCreateResponse response = AgentTunnelCreateResponse.newBuilder()
                    .setTunnelId(cached.getTunnelId())
                    .setExecutionId(executionId)
                    .setProxyHost(cached.getTunnelHost())
                    .setProxyPort(cached.getTunnelPort())
                    .setStatus(cached.getStatus())
                    .build();
            TUNNEL_CREATION_RESPONSE_CACHE.remove(executionId);
            return response;
        } else {
            return AgentTunnelCreateResponse.newBuilder().setStatus("Pending").build();
        }
    }

    public AgentExecutionAck runKernelRestartOnAgent(AgentKernelRestartRequest kernelRestartRequest) {
        String executionId = UUID.randomUUID().toString();
        AgentExecutionAck.Builder ackBuilder = AgentExecutionAck.newBuilder().setExecutionId(executionId);
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
                ackBuilder.setError(e.getMessage());
            }
        } else {
            logger.warn("No agent found to run the kernel restart on agent {}", kernelRestartRequest.getAgentId());
            ackBuilder.setError(
                    "No agent found to run the kernel restart on agent " + kernelRestartRequest.getAgentId());
        }
        return ackBuilder.build();
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
