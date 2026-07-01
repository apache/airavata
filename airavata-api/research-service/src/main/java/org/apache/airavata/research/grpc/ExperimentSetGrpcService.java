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
import java.util.List;
import org.apache.airavata.api.experimentset.*;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.grpc.GrpcRequestContext;
import org.apache.airavata.grpc.GrpcStatusMapper;
import org.apache.airavata.research.service.ExperimentSetService;
import org.springframework.stereotype.Component;

@Component
public class ExperimentSetGrpcService extends ExperimentSetServiceGrpc.ExperimentSetServiceImplBase {

    private final ExperimentSetService experimentSetService;

    public ExperimentSetGrpcService(ExperimentSetService experimentSetService) {
        this.experimentSetService = experimentSetService;
    }

    @Override
    public void createExperimentSet(CreateExperimentSetRequest request, StreamObserver<ExperimentSet> responseObserver) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            ExperimentSet r = experimentSetService.createExperimentSet(ctx, request);
            responseObserver.onNext(r);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void launchExperimentSet(LaunchExperimentSetRequest request, StreamObserver<ExperimentSet> responseObserver) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            ExperimentSet r = experimentSetService.launchExperimentSet(ctx, request.getExperimentSetId(), request.getNotificationEmail());
            responseObserver.onNext(r);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getExperimentSet(GetExperimentSetRequest request, StreamObserver<ExperimentSet> responseObserver) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            ExperimentSet r = experimentSetService.getExperimentSet(ctx, request.getExperimentSetId());
            responseObserver.onNext(r);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void listExperimentSets(ListExperimentSetsRequest request, StreamObserver<ListExperimentSetsResponse> responseObserver) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<ExperimentSet> sets = experimentSetService.listExperimentSets(ctx, request.getLimit(), request.getOffset());
            responseObserver.onNext(ListExperimentSetsResponse.newBuilder()
                    .addAllExperimentSets(sets)
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getExperimentSetStatus(GetExperimentSetRequest request, StreamObserver<ExperimentSetStatus> responseObserver) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            ExperimentSetStatus r = experimentSetService.getExperimentSetStatus(ctx, request.getExperimentSetId());
            responseObserver.onNext(r);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void deleteExperimentSet(DeleteExperimentSetRequest request, StreamObserver<Empty> responseObserver) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            experimentSetService.deleteExperimentSet(ctx, request.getExperimentSetId());
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(GrpcStatusMapper.toStatusException(e));
        }
    }
}
