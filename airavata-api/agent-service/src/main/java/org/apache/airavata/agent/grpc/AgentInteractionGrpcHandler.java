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

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.apache.airavata.agent.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AgentInteractionGrpcHandler extends AgentInteractionServiceGrpc.AgentInteractionServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(AgentInteractionGrpcHandler.class);

    private final AgentConnectionHandler agentConnectionHandler;

    public AgentInteractionGrpcHandler(AgentConnectionHandler agentConnectionHandler) {
        this.agentConnectionHandler = agentConnectionHandler;
    }

    @Override
    public void getAgentInfo(GetAgentInfoRequest request, StreamObserver<AgentInfoResponse> responseObserver) {
        try {
            responseObserver.onNext(agentConnectionHandler.isAgentUp(request.getAgentId()));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void setupTunnel(AgentTunnelCreateRequest request, StreamObserver<AgentTunnelAck> responseObserver) {
        try {
            responseObserver.onNext(agentConnectionHandler.runTunnelOnAgent(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getTunnelResponse(
            GetExecutionResponseRequest request, StreamObserver<AgentTunnelCreateResponse> responseObserver) {
        try {
            responseObserver.onNext(agentConnectionHandler.getTunnelCreateResponse(request.getExecutionId()));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void terminateTunnel(AgentTunnelTerminateRequest request, StreamObserver<AgentTunnelAck> responseObserver) {
        try {
            responseObserver.onNext(agentConnectionHandler.terminateTunnelOnAgent(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void setupEnv(AgentEnvSetupRequest request, StreamObserver<AgentExecutionAck> responseObserver) {
        try {
            if (agentConnectionHandler.isAgentUp(request.getAgentId()).getIsAgentUp()) {
                responseObserver.onNext(agentConnectionHandler.runEnvSetupOnAgent(request));
            } else {
                logger.warn("No agent is available to run on agent {}", request.getAgentId());
                responseObserver.onNext(AgentExecutionAck.newBuilder()
                        .setError("Agent not found")
                        .build());
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getEnvSetupResponse(
            GetExecutionResponseRequest request, StreamObserver<AgentEnvSetupResponse> responseObserver) {
        try {
            responseObserver.onNext(agentConnectionHandler.getEnvSetupResponse(request.getExecutionId()));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void restartKernel(AgentKernelRestartRequest request, StreamObserver<AgentExecutionAck> responseObserver) {
        try {
            if (agentConnectionHandler.isAgentUp(request.getAgentId()).getIsAgentUp()) {
                responseObserver.onNext(agentConnectionHandler.runKernelRestartOnAgent(request));
            } else {
                logger.warn("No agent is available to run on agent {}", request.getAgentId());
                responseObserver.onNext(AgentExecutionAck.newBuilder()
                        .setError("Agent not found")
                        .build());
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getKernelRestartResponse(
            GetExecutionResponseRequest request, StreamObserver<AgentKernelRestartResponse> responseObserver) {
        try {
            responseObserver.onNext(agentConnectionHandler.getKernelRestartResponse(request.getExecutionId()));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void executeCommand(
            AgentCommandExecutionRequest request, StreamObserver<AgentExecutionAck> responseObserver) {
        try {
            if (agentConnectionHandler.isAgentUp(request.getAgentId()).getIsAgentUp()) {
                responseObserver.onNext(agentConnectionHandler.runCommandOnAgent(request));
            } else {
                logger.warn("No agent is available to run on agent {}", request.getAgentId());
                responseObserver.onNext(AgentExecutionAck.newBuilder()
                        .setError("Agent not found")
                        .build());
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getCommandResponse(
            GetExecutionResponseRequest request, StreamObserver<AgentCommandExecutionResponse> responseObserver) {
        try {
            responseObserver.onNext(agentConnectionHandler.getCommandExecutionResponse(request.getExecutionId()));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void executeAsyncCommand(
            AgentAsyncCommandExecutionRequest request, StreamObserver<AgentExecutionAck> responseObserver) {
        try {
            if (agentConnectionHandler.isAgentUp(request.getAgentId()).getIsAgentUp()) {
                responseObserver.onNext(agentConnectionHandler.runAsyncCommandOnAgent(request));
            } else {
                logger.warn("No agent is available to run on agent {}", request.getAgentId());
                responseObserver.onNext(AgentExecutionAck.newBuilder()
                        .setError("Agent not found")
                        .build());
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getAsyncCommandResponse(
            GetExecutionResponseRequest request, StreamObserver<AgentAsyncCommandExecutionResponse> responseObserver) {
        try {
            responseObserver.onNext(agentConnectionHandler.getAsyncCommandExecutionResponse(request.getExecutionId()));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void listAsyncCommands(
            AgentAsyncCommandListRequest request, StreamObserver<AgentExecutionAck> responseObserver) {
        try {
            if (agentConnectionHandler.isAgentUp(request.getAgentId()).getIsAgentUp()) {
                responseObserver.onNext(agentConnectionHandler.runAsyncCommandListOnAgent(request));
            } else {
                logger.warn("No agent is available to run on agent {}", request.getAgentId());
                responseObserver.onNext(AgentExecutionAck.newBuilder()
                        .setError("Agent not found")
                        .build());
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getAsyncCommandListResponse(
            GetExecutionResponseRequest request, StreamObserver<AgentAsyncCommandListResponse> responseObserver) {
        try {
            responseObserver.onNext(agentConnectionHandler.getAsyncCommandListResponse(request.getExecutionId()));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void terminateAsyncCommand(
            AgentAsyncCommandTerminateRequest request, StreamObserver<AgentExecutionAck> responseObserver) {
        try {
            if (agentConnectionHandler.isAgentUp(request.getAgentId()).getIsAgentUp()) {
                responseObserver.onNext(agentConnectionHandler.runAsyncCommandTerminateOnAgent(request));
            } else {
                logger.warn("No agent is available to run on agent {}", request.getAgentId());
                responseObserver.onNext(AgentExecutionAck.newBuilder()
                        .setError("Agent not found")
                        .build());
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getAsyncCommandTerminateResponse(
            GetExecutionResponseRequest request, StreamObserver<AgentAsyncCommandTerminateResponse> responseObserver) {
        try {
            responseObserver.onNext(agentConnectionHandler.getAsyncCommandTerminateResponse(request.getExecutionId()));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void executeJupyter(
            AgentJupyterExecutionRequest request, StreamObserver<AgentExecutionAck> responseObserver) {
        try {
            if (agentConnectionHandler.isAgentUp(request.getAgentId()).getIsAgentUp()) {
                responseObserver.onNext(agentConnectionHandler.runJupyterOnAgent(request));
            } else {
                logger.warn("No agent is available to run on agent {}", request.getAgentId());
                responseObserver.onNext(AgentExecutionAck.newBuilder()
                        .setError("Agent not found")
                        .build());
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getJupyterResponse(
            GetExecutionResponseRequest request, StreamObserver<AgentJupyterExecutionResponse> responseObserver) {
        try {
            responseObserver.onNext(agentConnectionHandler.getJupyterExecutionResponse(request.getExecutionId()));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void executePython(AgentPythonExecutionRequest request, StreamObserver<AgentExecutionAck> responseObserver) {
        try {
            if (agentConnectionHandler.isAgentUp(request.getAgentId()).getIsAgentUp()) {
                responseObserver.onNext(agentConnectionHandler.runPythonOnAgent(request));
            } else {
                logger.warn("No agent is available to run on agent {}", request.getAgentId());
                responseObserver.onNext(AgentExecutionAck.newBuilder()
                        .setError("Agent not found")
                        .build());
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getPythonResponse(
            GetExecutionResponseRequest request, StreamObserver<AgentPythonExecutionResponse> responseObserver) {
        try {
            responseObserver.onNext(agentConnectionHandler.getPythonExecutionResponse(request.getExecutionId()));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
