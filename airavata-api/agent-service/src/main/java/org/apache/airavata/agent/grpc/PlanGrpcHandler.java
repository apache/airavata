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

import com.google.protobuf.util.JsonFormat;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.airavata.agent.model.PlanEntity;
import org.apache.airavata.agent.service.*;
import org.apache.airavata.agent.service.PlanService;
import org.apache.airavata.config.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PlanGrpcHandler extends PlanServiceGrpc.PlanServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(PlanGrpcHandler.class);

    private final PlanService planHandler;

    public PlanGrpcHandler(PlanService planHandler) {
        this.planHandler = planHandler;
    }

    @Override
    public void savePlan(SavePlanRequest request, StreamObserver<PlanMessage> responseObserver) {
        try {
            String dataAsString = JsonFormat.printer().print(request.getData());

            PlanEntity plan = new PlanEntity();
            plan.setId(request.getId());
            plan.setUserId(UserContext.userId());
            plan.setGatewayId(UserContext.gatewayId());
            plan.setData(dataAsString);

            PlanEntity savedPlan = planHandler.savePlan(plan);
            responseObserver.onNext(toPlanMessage(savedPlan));
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error while saving plan", e);
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getPlansByUser(GetPlansByUserRequest request, StreamObserver<GetPlansByUserResponse> responseObserver) {
        try {
            List<PlanEntity> plans = planHandler.getAllPlansByUserId(UserContext.userId(), UserContext.gatewayId());
            GetPlansByUserResponse response = GetPlansByUserResponse.newBuilder()
                    .addAllPlans(plans.stream().map(this::toPlanMessage).collect(Collectors.toList()))
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error while getting plans by user", e);
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getPlan(GetPlanRequest request, StreamObserver<PlanMessage> responseObserver) {
        try {
            PlanEntity plan = planHandler.getPlanById(request.getPlanId());
            responseObserver.onNext(toPlanMessage(plan));
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error while getting plan {}", request.getPlanId(), e);
            responseObserver.onError(
                    Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void updatePlan(UpdatePlanRequest request, StreamObserver<PlanMessage> responseObserver) {
        try {
            PlanEntity existingPlan = planHandler.getPlanById(request.getPlanId());

            String dataAsString = JsonFormat.printer().print(request.getData());
            existingPlan.setData(dataAsString);
            PlanEntity updatedPlan = planHandler.savePlan(existingPlan);
            responseObserver.onNext(toPlanMessage(updatedPlan));
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error while updating plan {}", request.getPlanId(), e);
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    private PlanMessage toPlanMessage(PlanEntity plan) {
        return PlanMessage.newBuilder()
                .setId(plan.getId())
                .setUserId(plan.getUserId())
                .setGatewayId(plan.getGatewayId())
                .setData(plan.getData() != null ? plan.getData() : "")
                .build();
    }
}
