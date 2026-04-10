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
package org.apache.airavata.research.grpc;

import com.google.protobuf.util.JsonFormat;
import io.grpc.stub.StreamObserver;
import java.util.List;
import org.apache.airavata.grpc.GrpcStatusMapper;
import org.apache.airavata.model.experiment.proto.ExperimentModel;
import org.apache.airavata.model.process.proto.ProcessModel;
import org.apache.airavata.research.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ExperimentManagementGrpcHandler
        extends ExperimentManagementServiceGrpc.ExperimentManagementServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(ExperimentManagementGrpcHandler.class);

    private final AgentManagementService agentManagementHandler;

    public ExperimentManagementGrpcHandler(AgentManagementService agentManagementHandler) {
        this.agentManagementHandler = agentManagementHandler;
    }

    @Override
    public void getExperiment(
            GetAgentExperimentRequest request, StreamObserver<AgentExperimentResponse> responseObserver) {
        try {
            ExperimentModel experiment = agentManagementHandler.getExperiment(request.getExperimentId());
            String json = JsonFormat.printer().print(experiment);
            responseObserver.onNext(
                    AgentExperimentResponse.newBuilder().setExperimentJson(json).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error getting experiment {}", request.getExperimentId(), e);
            responseObserver.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void launchExperiment(AgentLaunchRequest request, StreamObserver<AgentLaunchResponse> responseObserver) {
        try {
            AgentLaunchResponse response = agentManagementHandler.createAndLaunchExperiment(request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error launching experiment {}", request.getExperimentName(), e);
            responseObserver.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void launchOptimizedExperiment(
            LaunchOptimizedExperimentRequest request, StreamObserver<AgentLaunchResponse> responseObserver) {
        try {
            List<AgentLaunchRequest> requests = request.getRequestsList();
            AgentLaunchRequest optimum = agentManagementHandler.filterOptimumLaunchRequest(requests);
            AgentLaunchResponse response = agentManagementHandler.createAndLaunchExperiment(optimum);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed to launch optimized experiment", e);
            responseObserver.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void terminateExperiment(
            TerminateAgentExperimentRequest request, StreamObserver<AgentTerminateResponse> responseObserver) {
        try {
            AgentTerminateResponse response = agentManagementHandler.terminateExperiment(request.getExperimentId());
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error terminating experiment {}", request.getExperimentId(), e);
            responseObserver.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getProcessModel(GetProcessModelRequest request, StreamObserver<ProcessModelResponse> responseObserver) {
        try {
            ProcessModel processModel = agentManagementHandler.getEnvProcessModel(request.getExperimentId());
            if (processModel == null) {
                responseObserver.onNext(
                        ProcessModelResponse.newBuilder().setFound(false).build());
            } else {
                String json = JsonFormat.printer().print(processModel);
                responseObserver.onNext(ProcessModelResponse.newBuilder()
                        .setProcessJson(json)
                        .setFound(true)
                        .build());
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error getting process model for experiment {}", request.getExperimentId(), e);
            responseObserver.onError(GrpcStatusMapper.toStatusException(e));
        }
    }
}
