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

import io.grpc.stub.StreamObserver;
import org.apache.airavata.grpc.GrpcStatusMapper;
import org.apache.airavata.research.*;
import org.apache.airavata.research.service.ResearchHubService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ResearchHubGrpcHandler extends ResearchHubServiceGrpc.ResearchHubServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(ResearchHubGrpcHandler.class);
    private final ResearchHubService researchHubService;

    public ResearchHubGrpcHandler(ResearchHubService researchHubService) {
        this.researchHubService = researchHubService;
    }

    @Override
    public void startProjectSession(
            StartProjectSessionRequest request, StreamObserver<RedirectResponse> responseObserver) {
        try {
            String spawnUrl = researchHubService.spinRHubSession(request.getProjectId(), request.getSessionName());
            responseObserver.onNext(
                    RedirectResponse.newBuilder().setRedirectUrl(spawnUrl).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error starting project session for {}", request.getProjectId(), e);
            responseObserver.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void resumeSession(ResumeSessionRequest request, StreamObserver<RedirectResponse> responseObserver) {
        try {
            String sessionUrl = researchHubService.resolveRHubExistingSession(request.getSessionId());
            responseObserver.onNext(
                    RedirectResponse.newBuilder().setRedirectUrl(sessionUrl).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error resuming session {}", request.getSessionId(), e);
            responseObserver.onError(GrpcStatusMapper.toStatusException(e));
        }
    }
}
