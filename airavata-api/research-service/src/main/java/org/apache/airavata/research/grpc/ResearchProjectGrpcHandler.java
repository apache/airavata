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
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.airavata.grpc.GrpcStatusMapper;
import org.apache.airavata.research.*;
import org.apache.airavata.research.model.ResearchProjectEntity;
import org.apache.airavata.research.service.ResearchProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ResearchProjectGrpcHandler extends ResearchProjectServiceGrpc.ResearchProjectServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(ResearchProjectGrpcHandler.class);
    private final ResearchProjectService researchProjectService;
    private final ObjectMapper objectMapper;

    public ResearchProjectGrpcHandler(ResearchProjectService researchProjectService, ObjectMapper objectMapper) {
        this.researchProjectService = researchProjectService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void getAllProjects(GetAllProjectsRequest request, StreamObserver<JsonListResponse> responseObserver) {
        try {
            List<ResearchProjectEntity> projects = researchProjectService.getAllProjects();
            List<String> jsonList = projects.stream().map(p -> writeJson(p)).collect(Collectors.toList());
            responseObserver.onNext(
                    JsonListResponse.newBuilder().addAllItems(jsonList).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error getting all projects", e);
            responseObserver.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getProjectsByOwner(
            GetProjectsByOwnerRequest request, StreamObserver<JsonListResponse> responseObserver) {
        try {
            List<ResearchProjectEntity> projects = researchProjectService.getAllProjectsByOwnerId(request.getOwnerId());
            List<String> jsonList = projects.stream().map(p -> writeJson(p)).collect(Collectors.toList());
            responseObserver.onNext(
                    JsonListResponse.newBuilder().addAllItems(jsonList).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error getting projects for owner {}", request.getOwnerId(), e);
            responseObserver.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void createProject(CreateResearchProjectRequest request, StreamObserver<JsonResponse> responseObserver) {
        try {
            ResearchProjectEntity project = researchProjectService.createProject(
                    request.getName(),
                    request.getOwnerId(),
                    request.getRepositoryId(),
                    new HashSet<>(request.getDatasetIdsList()));
            responseObserver.onNext(
                    JsonResponse.newBuilder().setJson(writeJson(project)).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error creating project", e);
            responseObserver.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void deleteProject(DeleteProjectRequest request, StreamObserver<BoolResponse> responseObserver) {
        try {
            boolean result = researchProjectService.deleteProject(request.getProjectId());
            responseObserver.onNext(BoolResponse.newBuilder().setValue(result).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error deleting project {}", request.getProjectId(), e);
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
