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

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.api.project.*;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.grpc.GrpcRequestContext;
import org.apache.airavata.grpc.GrpcStatusMapper;
import org.apache.airavata.model.experiment.proto.ProjectSearchFields;
import org.apache.airavata.model.workspace.proto.Project;
import org.apache.airavata.research.service.ProjectService;
import org.springframework.stereotype.Component;

@Component
public class ProjectGrpcService extends ProjectServiceGrpc.ProjectServiceImplBase {

    private final ProjectService projectService;

    public ProjectGrpcService(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public void createProject(CreateProjectRequest request, StreamObserver<CreateProjectResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            String id = projectService.createProject(ctx, request.getGatewayId(), request.getProject());
            observer.onNext(CreateProjectResponse.newBuilder().setProjectId(id).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getProject(GetProjectRequest request, StreamObserver<Project> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            Project result = projectService.getProject(ctx, request.getProjectId());
            observer.onNext(result);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void updateProject(UpdateProjectRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            projectService.updateProject(ctx, request.getProjectId(), request.getProject());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void deleteProject(DeleteProjectRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            projectService.deleteProject(ctx, request.getProjectId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getUserProjects(GetUserProjectsRequest request, StreamObserver<GetUserProjectsResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<Project> results = projectService.getUserProjects(
                    ctx, request.getGatewayId(), request.getUserName(), request.getLimit(), request.getOffset());
            observer.onNext(
                    GetUserProjectsResponse.newBuilder().addAllProjects(results).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void searchProjects(SearchProjectsRequest request, StreamObserver<SearchProjectsResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            // TODO: Map string filter keys to ProjectSearchFields enum — needs mapper
            Map<ProjectSearchFields, String> filters = new HashMap<>();
            for (Map.Entry<String, String> entry : request.getFiltersMap().entrySet()) {
                filters.put(ProjectSearchFields.valueOf(entry.getKey()), entry.getValue());
            }
            List<Project> results = projectService.searchProjects(
                    ctx,
                    request.getGatewayId(),
                    request.getUserName(),
                    filters,
                    request.getLimit(),
                    request.getOffset());
            observer.onNext(
                    SearchProjectsResponse.newBuilder().addAllProjects(results).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }
}
