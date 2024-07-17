package org.apache.airavata.agent.connection.service.handlers;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.airavata.agent.*;
import org.apache.airavata.agent.connection.service.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


@GrpcService
public class AgentHandler extends AgentCommunicationServiceGrpc.AgentCommunicationServiceImplBase {

    private final static Logger logger = LoggerFactory.getLogger(AgentHandler.class);

    // <streamId, StreamObserver>
    private final Map<String, StreamObserver<ServerMessage>> ACTIVE_STREAMS = new ConcurrentHashMap<>();

    // <agentId, streamId>
    private final Map<String, String> AGENT_STREAM_MAPPING = new ConcurrentHashMap<>();

    public AgentInfoResponse isAgentUp(String agentId) {

        if (AGENT_STREAM_MAPPING.containsKey(agentId) &&
                ACTIVE_STREAMS.containsKey(AGENT_STREAM_MAPPING.get(agentId))) {
            return new AgentInfoResponse(agentId, true);
        } else {
            return new AgentInfoResponse(agentId, false);
        }
    }

    public AgentTunnelAck runTunnelOnAgent(AgentTunnelCreationRequest tunnelRequest) {
        AgentTunnelAck ack = new AgentTunnelAck();

        if (AGENT_STREAM_MAPPING.containsKey(tunnelRequest.getAgentId()) &&
                ACTIVE_STREAMS.containsKey(AGENT_STREAM_MAPPING.get(tunnelRequest.getAgentId()))) {
            String agentId = AGENT_STREAM_MAPPING.get(tunnelRequest.getAgentId());
            StreamObserver<ServerMessage> streamObserver = ACTIVE_STREAMS.get(agentId);

            try {
                streamObserver.onNext(ServerMessage.newBuilder().setTunnelCreationRequest(TunnelCreationRequest.newBuilder()
                        .setDestinationHost(tunnelRequest.getDestinationHost())
                        .setDestinationPort(tunnelRequest.getDestinationPort())
                        .setSourcePort(tunnelRequest.getSourcePort())
                        .setSshUserName(tunnelRequest.getSshUserName())
                        .setPassword(Optional.ofNullable(tunnelRequest.getPassword()).orElse(""))
                        .setSshKeyPath(Optional.ofNullable(tunnelRequest.getSshKeyPath()).orElse(""))
                        .build()).build());
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
    public AgentCommandAck runCommandOnAgent(AgentCommandRequest commandRequest) {

        String executionId = UUID.randomUUID().toString();
        AgentCommandAck ack = new AgentCommandAck();
        ack.setExecutionId(executionId);

        if (AGENT_STREAM_MAPPING.containsKey(commandRequest.getAgentId()) &&
                ACTIVE_STREAMS.containsKey(AGENT_STREAM_MAPPING.get(commandRequest.getAgentId()))) {
            String streamId = AGENT_STREAM_MAPPING.get(commandRequest.getAgentId());
            StreamObserver<ServerMessage> streamObserver = ACTIVE_STREAMS.get(streamId);

            try {
                logger.info("Running a command on agent {}", commandRequest.getAgentId());
                streamObserver.onNext(ServerMessage.newBuilder().setCommandExecutionRequest(
                        CommandExecutionRequest.newBuilder()
                                .setExecutionId(executionId)
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

    private void handleAgentPing(AgentPing agentPing, String streamId) {
        logger.info("Received agent ping for agent id {}", agentPing.getAgentId());
        AGENT_STREAM_MAPPING.put(agentPing.getAgentId(), streamId);
    }

    private void handleCommandExecutionResponse (CommandExecutionResponse commandExecutionResponse) {

    }

    private void handleContainerExecutionResponse (ContainerExecutionResponse containerExecutionResponse) {

    }

    private void handleAgentTerminationResponse (TerminateExecutionResponse terminateExecutionResponse) {

    }

    private String generateStreamId() {
        // Generate a unique ID for each stream
        return java.util.UUID.randomUUID().toString();
    }
    @Override
    public StreamObserver<AgentMessage> createMessageBus(StreamObserver<ServerMessage> responseObserver) {

        String streamId = generateStreamId();
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
                    case CONTAINEREXECUTIONRESPONSE -> {
                        handleContainerExecutionResponse(request.getContainerExecutionResponse());
                    }
                    case TERMINATEEXECUTIONRESPONSE -> {
                        handleAgentTerminationResponse(request.getTerminateExecutionResponse());
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
