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
import org.apache.airavata.api.notification.*;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.grpc.GrpcRequestContext;
import org.apache.airavata.grpc.GrpcStatusMapper;
import org.apache.airavata.model.workspace.proto.Notification;
import org.apache.airavata.research.service.NotificationService;
import org.springframework.stereotype.Component;

@Component
public class NotificationGrpcService extends NotificationServiceGrpc.NotificationServiceImplBase {

    private final NotificationService notificationService;

    public NotificationGrpcService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void createNotification(
            CreateNotificationRequest request, StreamObserver<CreateNotificationResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            String id = notificationService.createNotification(ctx, request.getNotification());
            observer.onNext(CreateNotificationResponse.newBuilder()
                    .setNotificationId(id)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void updateNotification(UpdateNotificationRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            notificationService.updateNotification(ctx, request.getNotification());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void deleteNotification(DeleteNotificationRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            notificationService.deleteNotification(ctx, request.getGatewayId(), request.getNotificationId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getNotification(GetNotificationRequest request, StreamObserver<Notification> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            Notification result =
                    notificationService.getNotification(ctx, request.getGatewayId(), request.getNotificationId());
            observer.onNext(result);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getAllNotifications(
            GetAllNotificationsRequest request, StreamObserver<GetAllNotificationsResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<Notification> results = notificationService.getAllNotifications(ctx, request.getGatewayId());
            observer.onNext(GetAllNotificationsResponse.newBuilder()
                    .addAllNotifications(results)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }
}
