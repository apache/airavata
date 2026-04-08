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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.airavata.config.UserContext;
import org.apache.airavata.grpc.GrpcStatusMapper;
import org.apache.airavata.research.*;
import org.apache.airavata.research.model.SessionEntity;
import org.apache.airavata.research.model.SessionStatusEnum;
import org.apache.airavata.research.service.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ResearchSessionGrpcHandler extends ResearchSessionServiceGrpc.ResearchSessionServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(ResearchSessionGrpcHandler.class);
    private final SessionService sessionService;
    private final ObjectMapper objectMapper;

    public ResearchSessionGrpcHandler(SessionService sessionService, ObjectMapper objectMapper) {
        this.sessionService = sessionService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void getSessions(GetSessionsRequest request, StreamObserver<JsonListResponse> responseObserver) {
        try {
            String userId = UserContext.userId();
            List<SessionEntity> sessions;
            if (request.getStatus().isEmpty()) {
                sessions = sessionService.findAllByUserId(userId);
            } else {
                SessionStatusEnum status = SessionStatusEnum.valueOf(request.getStatus());
                sessions = sessionService.findAllByUserIdAndStatus(userId, status);
            }
            List<String> jsonList = sessions.stream().map(this::writeJson).collect(Collectors.toList());
            responseObserver.onNext(
                    JsonListResponse.newBuilder().addAllItems(jsonList).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error getting sessions", e);
            responseObserver.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void updateSessionStatus(UpdateSessionStatusRequest request, StreamObserver<JsonResponse> responseObserver) {
        try {
            SessionStatusEnum status = SessionStatusEnum.valueOf(request.getStatus());
            SessionEntity session = sessionService.updateSessionStatus(request.getSessionId(), status);
            responseObserver.onNext(
                    JsonResponse.newBuilder().setJson(writeJson(session)).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error updating session status for {}", request.getSessionId(), e);
            responseObserver.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void deleteSessions(DeleteSessionsRequest request, StreamObserver<BoolResponse> responseObserver) {
        try {
            for (String id : request.getSessionIdsList()) {
                sessionService.updateSessionStatus(id, SessionStatusEnum.TERMINATED);
                sessionService.deleteSession(id);
            }
            responseObserver.onNext(BoolResponse.newBuilder().setValue(true).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error deleting sessions", e);
            responseObserver.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void deleteSession(DeleteSessionRequest request, StreamObserver<BoolResponse> responseObserver) {
        try {
            sessionService.updateSessionStatus(request.getSessionId(), SessionStatusEnum.TERMINATED);
            sessionService.deleteSession(request.getSessionId());
            responseObserver.onNext(BoolResponse.newBuilder().setValue(true).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error deleting session {}", request.getSessionId(), e);
            responseObserver.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    private String writeJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize to JSON", e);
        }
    }
}
