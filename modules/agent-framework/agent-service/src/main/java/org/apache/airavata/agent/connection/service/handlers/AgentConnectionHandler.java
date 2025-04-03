package org.apache.airavata.agent.connection.service.handlers;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.airavata.agent.AgentCommunicationServiceGrpc;
import org.apache.airavata.agent.AgentMessage;
import org.apache.airavata.agent.AgentPing;
import org.apache.airavata.agent.CommandExecutionRequest;
import org.apache.airavata.agent.CommandExecutionResponse;
import org.apache.airavata.agent.EnvSetupResponse;
import org.apache.airavata.agent.JupyterExecutionResponse;
import org.apache.airavata.agent.PythonExecutionRequest;
import org.apache.airavata.agent.PythonExecutionResponse;
import org.apache.airavata.agent.ServerMessage;
import org.apache.airavata.agent.TunnelCreationRequest;
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
import org.apache.airavata.agent.connection.service.models.AgentPythonExecutionAck;
import org.apache.airavata.agent.connection.service.models.AgentPythonExecutionRequest;
import org.apache.airavata.agent.connection.service.models.AgentPythonExecutionResponse;
import org.apache.airavata.agent.connection.service.models.AgentTunnelAck;
import org.apache.airavata.agent.connection.service.models.AgentTunnelRequest;
import org.apache.airavata.agent.connection.service.models.AgentTunnelResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class AgentConnectionHandler extends AgentCommunicationServiceGrpc.AgentCommunicationServiceImplBase {

    private final static Logger logger = LoggerFactory.getLogger(AgentConnectionHandler.class);

    // <streamId, StreamObserver>
    private final Map<String, StreamObserver<ServerMessage>> ACTIVE_STREAMS = new ConcurrentHashMap<>();

    // <agentId, streamId>
    private final Map<String, String> AGENT_STREAM_MAPPING = new ConcurrentHashMap<>();

    private final Map<String, EnvSetupResponse> ENV_SETUP_RESPONSE_CACHE = new ConcurrentHashMap<>();
    private final Map<String, CommandExecutionResponse> COMMAND_EXECUTION_RESPONSE_CACHE = new ConcurrentHashMap<>();
    private final Map<String, JupyterExecutionResponse> JUPYTER_EXECUTION_RESPONSE_CACHE = new ConcurrentHashMap<>();
    private final Map<String, PythonExecutionResponse> PYTHON_EXECUTION_RESPONSE_CACHE = new ConcurrentHashMap<>();

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
            envCreationResponse.setStatus(ENV_SETUP_RESPONSE_CACHE.get(executionId).getStatus());
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
            agentCommandResponse.setResponseString(COMMAND_EXECUTION_RESPONSE_CACHE.get(executionId).getResponseString());
            agentCommandResponse.setExecutionId(executionId);
            agentCommandResponse.setExecuted(true);
            COMMAND_EXECUTION_RESPONSE_CACHE.remove(executionId);
        } else {
            agentCommandResponse.setExecuted(false);
        }
        return agentCommandResponse;
    }

    public AgentJupyterExecutionResponse getJupyterExecutionResponse(String executionId) {
        AgentJupyterExecutionResponse executionResponse = new AgentJupyterExecutionResponse();
        if (JUPYTER_EXECUTION_RESPONSE_CACHE.containsKey(executionId)) {
            executionResponse.setResponseString(JUPYTER_EXECUTION_RESPONSE_CACHE.get(executionId).getResponseString());
            executionResponse.setExecutionId(executionId);
            executionResponse.setExecuted(true);
            JUPYTER_EXECUTION_RESPONSE_CACHE.remove(executionId);
        } else {
            executionResponse.setExecuted(false);
        }
        return executionResponse;
    }

    public AgentPythonExecutionResponse getPythonExecutionResponse(String executionId) {
        AgentPythonExecutionResponse runResponse = new AgentPythonExecutionResponse();
        if (PYTHON_EXECUTION_RESPONSE_CACHE.containsKey(executionId)) {
            runResponse.setExecutionId(executionId);
            runResponse.setExecuted(true);
            runResponse.setResponseString(PYTHON_EXECUTION_RESPONSE_CACHE.get(executionId).getResponseString());
            PYTHON_EXECUTION_RESPONSE_CACHE.remove(executionId);
        } else {
            runResponse.setExecuted(false);
        }
        return runResponse;
    }

    public AgentTunnelResponse getTunnelResponse(String executionId) {
        AgentTunnelResponse tunnelResponse = new AgentTunnelResponse();
        if (AGENT_STREAM_MAPPING.containsKey(executionId)
                && ACTIVE_STREAMS.containsKey(AGENT_STREAM_MAPPING.get(executionId))) {
            tunnelResponse.setExecutionId(executionId);
            tunnelResponse.setTunneled(true);
        } else {
            tunnelResponse.setTunneled(false);
        }
        return tunnelResponse;
    }

    // request handling
    public AgentEnvSetupAck runEnvSetupOnAgent(AgentEnvSetupRequest envSetupRequest) {
        String executionId = UUID.randomUUID().toString();
        AgentEnvSetupAck ack = new AgentEnvSetupAck();
        ack.setExecutionId(executionId);
        Optional<StreamObserver<ServerMessage>> agentStreamObserver = getAgentStreamObserver(envSetupRequest.getAgentId());
        if (agentStreamObserver.isPresent()) {
            try {
                logger.info("Running an env setup on agent {}", envSetupRequest.getAgentId());
                agentStreamObserver.get().onNext(ServerMessage.newBuilder().setEnvSetupRequest(
                        org.apache.airavata.agent.EnvSetupRequest.newBuilder()
                                .setExecutionId(executionId)
                                .setEnvName(envSetupRequest.getEnvName())
                                .addAllLibraries(envSetupRequest.getLibraries())
                                .addAllPip(envSetupRequest.getPip())
                                .build()
                ).build());

            } catch (Exception e) {
                logger.error("Failed to submit env setup request {} on agent {}",
                        executionId, envSetupRequest.getAgentId(), e);
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
                streamObserver.onNext(ServerMessage.newBuilder().setCommandExecutionRequest(
                        CommandExecutionRequest.newBuilder()
                                .setExecutionId(executionId)
                                .setEnvName(commandRequest.getEnvName())
                                .setWorkingDir(commandRequest.getWorkingDir())
                                .addAllArguments(commandRequest.getArguments()).build()).build());
            } catch (Exception e) {
                logger.error("Failed to submit command execution request {} on agent {}",
                        executionId, commandRequest.getAgentId(), e);
                ack.setError(e.getMessage());
            }
        } else {
            logger.warn("No agent found to run the command on agent {}", commandRequest.getAgentId());
            ack.setError("No agent found to run the command on agent " + commandRequest.getAgentId());
        }
        return ack;
    }

    public AgentJupyterExecutionAck runJupyterOnAgent(AgentJupyterExecutionRequest jupyterExecutionRequest) {
        String executionId = UUID.randomUUID().toString();
        AgentJupyterExecutionAck ack = new AgentJupyterExecutionAck();
        ack.setExecutionId(executionId);
        Optional<StreamObserver<ServerMessage>> agentStreamObserver = getAgentStreamObserver(jupyterExecutionRequest.getAgentId());
        if (agentStreamObserver.isPresent()) {
            try {
                logger.info("Running a jupyter on agent {}", jupyterExecutionRequest.getAgentId());
                agentStreamObserver.get().onNext(ServerMessage.newBuilder().setJupyterExecutionRequest(
                        org.apache.airavata.agent.JupyterExecutionRequest.newBuilder()
                                .setExecutionId(executionId)
                                .setEnvName(jupyterExecutionRequest.getEnvName())
                                .setCode(jupyterExecutionRequest.getCode())
                                .build()
                ).build());
            } catch (Exception e) {
                logger.error("Failed to submit jupyter execution request {} on agent {}",
                        executionId, jupyterExecutionRequest.getAgentId(), e);
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
        Optional<StreamObserver<ServerMessage>> agentStreamObserver = getAgentStreamObserver(pythonRunRequest.getAgentId());
        if (agentStreamObserver.isPresent()) {
            try {
                logger.info("Running a python on agent {}", pythonRunRequest.getAgentId());
                agentStreamObserver.get().onNext(ServerMessage.newBuilder().setPythonExecutionRequest(
                        PythonExecutionRequest.newBuilder()
                                .setExecutionId(executionId)
                                .setEnvName(pythonRunRequest.getEnvName())
                                .setWorkingDir(pythonRunRequest.getWorkingDir())
                                .setCode(pythonRunRequest.getCode())
                                .build()
                ).build());
            } catch (Exception e) {
                logger.error("Failed to submit python execution request {} on agent {}",
                        executionId, pythonRunRequest.getAgentId(), e);
                ack.setError(e.getMessage());
            }
        } else {
            logger.warn("No agent found to run python execution on agent {}", pythonRunRequest.getAgentId());
            ack.setError("No agent found to run python execution on agent " + pythonRunRequest.getAgentId());
        }
        return ack;
    }

    public AgentTunnelAck runTunnelOnAgent(AgentTunnelRequest tunnelRequest) {
        AgentTunnelAck ack = new AgentTunnelAck();
        if (AGENT_STREAM_MAPPING.containsKey(tunnelRequest.getAgentId())
                && ACTIVE_STREAMS.containsKey(AGENT_STREAM_MAPPING.get(tunnelRequest.getAgentId()))) {
            String agentId = AGENT_STREAM_MAPPING.get(tunnelRequest.getAgentId());
            StreamObserver<ServerMessage> streamObserver = ACTIVE_STREAMS.get(agentId);
            try {
                streamObserver.onNext(ServerMessage.newBuilder().setTunnelCreationRequest(
                        TunnelCreationRequest.newBuilder()
                                .setDestinationHost(tunnelRequest.getDestinationHost())
                                .setDestinationPort(tunnelRequest.getDestinationPort())
                                .setSourcePort(tunnelRequest.getSourcePort())
                                .setSshUserName(tunnelRequest.getSshUserName())
                                .setPassword(Optional.ofNullable(tunnelRequest.getPassword()).orElse(""))
                                .setSshKeyPath(Optional.ofNullable(tunnelRequest.getSshKeyPath()).orElse(""))
                                .build()
                ).build());
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

    // internal handlers
    private void handleAgentPing(AgentPing agentPing, String streamId) {
        logger.info("Received agent ping for agent id {}", agentPing.getAgentId());
        AGENT_STREAM_MAPPING.put(agentPing.getAgentId(), streamId);
    }

    private void handleEnvSetupResponse(EnvSetupResponse envSetupResponse) {
        logger.info("Received env setup response for execution id {}", envSetupResponse.getExecutionId());
        ENV_SETUP_RESPONSE_CACHE.put(envSetupResponse.getExecutionId(), envSetupResponse);
    }

    private void handleCommandExecutionResponse(CommandExecutionResponse commandExecutionResponse) {
        logger.info("Received command execution response for execution id {}", commandExecutionResponse.getExecutionId());
        COMMAND_EXECUTION_RESPONSE_CACHE.put(commandExecutionResponse.getExecutionId(), commandExecutionResponse);
    }

    private void handleJupyterExecutionResponse(JupyterExecutionResponse executionResponse) {
        logger.info("Received jupyter execution response for execution id {}", executionResponse.getExecutionId());
        JUPYTER_EXECUTION_RESPONSE_CACHE.put(executionResponse.getExecutionId(), executionResponse);
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
                    case PYTHONEXECUTIONRESPONSE -> {
                        handlePythonExecutionResponse(request.getPythonExecutionResponse());
                    }
                    case ENVSETUPRESPONSE -> {
                        handleEnvSetupResponse(request.getEnvSetupResponse());
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
