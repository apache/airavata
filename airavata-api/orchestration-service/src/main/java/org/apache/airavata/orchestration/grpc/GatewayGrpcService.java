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
package org.apache.airavata.orchestration.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import java.util.List;
import org.apache.airavata.api.gateway.*;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.grpc.GrpcRequestContext;
import org.apache.airavata.grpc.GrpcStatusMapper;
import org.apache.airavata.model.workspace.proto.Gateway;
import org.apache.airavata.orchestration.service.GatewayService;
import org.springframework.stereotype.Component;

@Component
public class GatewayGrpcService extends GatewayServiceGrpc.GatewayServiceImplBase {

    private final GatewayService gatewayService;

    public GatewayGrpcService(GatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    @Override
    public void addGateway(AddGatewayRequest request, StreamObserver<AddGatewayResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            String id = gatewayService.addGateway(ctx, request.getGateway());
            observer.onNext(AddGatewayResponse.newBuilder().setGatewayId(id).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getGateway(GetGatewayRequest request, StreamObserver<Gateway> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            Gateway result = gatewayService.getGateway(ctx, request.getGatewayId());
            observer.onNext(result);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void updateGateway(UpdateGatewayRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            gatewayService.updateGateway(ctx, request.getGatewayId(), request.getGateway());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void deleteGateway(DeleteGatewayRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            gatewayService.deleteGateway(ctx, request.getGatewayId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getAllGateways(GetAllGatewaysRequest request, StreamObserver<GetAllGatewaysResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<Gateway> gateways = gatewayService.getAllGateways(ctx);
            observer.onNext(
                    GetAllGatewaysResponse.newBuilder().addAllGateways(gateways).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void isGatewayExist(IsGatewayExistRequest request, StreamObserver<IsGatewayExistResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            boolean exists = gatewayService.isGatewayExist(ctx, request.getGatewayId());
            observer.onNext(
                    IsGatewayExistResponse.newBuilder().setExists(exists).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getAllUsersInGateway(
            GetAllUsersInGatewayRequest request, StreamObserver<GetAllUsersInGatewayResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<String> users = gatewayService.getAllUsersInGateway(ctx, request.getGatewayId());
            observer.onNext(GetAllUsersInGatewayResponse.newBuilder()
                    .addAllUserNames(users)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void isUserExists(IsUserExistsRequest request, StreamObserver<IsUserExistsResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            boolean exists = gatewayService.isUserExists(ctx, request.getGatewayId(), request.getUserName());
            observer.onNext(IsUserExistsResponse.newBuilder().setExists(exists).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }
}
