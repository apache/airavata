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
package org.apache.airavata.compute.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import java.util.List;
import org.apache.airavata.api.userprofile.*;
import org.apache.airavata.compute.service.UserResourceProfileService;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.grpc.GrpcRequestContext;
import org.apache.airavata.grpc.GrpcStatusMapper;
import org.apache.airavata.model.appcatalog.userresourceprofile.proto.UserComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.proto.UserResourceProfile;
import org.apache.airavata.model.appcatalog.userresourceprofile.proto.UserStoragePreference;
import org.apache.airavata.model.status.proto.QueueStatusModel;
import org.springframework.stereotype.Component;

@Component
public class UserResourceProfileGrpcService extends UserResourceProfileServiceGrpc.UserResourceProfileServiceImplBase {

    private final UserResourceProfileService userResourceProfileService;

    public UserResourceProfileGrpcService(UserResourceProfileService userResourceProfileService) {
        this.userResourceProfileService = userResourceProfileService;
    }

    // --- User Resource Profiles ---

    @Override
    public void registerUserResourceProfile(
            RegisterUserResourceProfileRequest request, StreamObserver<RegisterUserResourceProfileResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            String id = userResourceProfileService.registerUserResourceProfile(ctx, request.getUserResourceProfile());
            observer.onNext(RegisterUserResourceProfileResponse.newBuilder()
                    .setUserId(id)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void isUserResourceProfileExists(
            IsUserResourceProfileExistsRequest request, StreamObserver<IsUserResourceProfileExistsResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            boolean exists = userResourceProfileService.isUserResourceProfileExists(
                    ctx, request.getUserId(), request.getGatewayId());
            observer.onNext(IsUserResourceProfileExistsResponse.newBuilder()
                    .setExists(exists)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getUserResourceProfile(
            GetUserResourceProfileRequest request, StreamObserver<UserResourceProfile> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            UserResourceProfile result =
                    userResourceProfileService.getUserResourceProfile(ctx, request.getUserId(), request.getGatewayId());
            observer.onNext(result);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void updateUserResourceProfile(UpdateUserResourceProfileRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            userResourceProfileService.updateUserResourceProfile(
                    ctx, request.getUserId(), request.getGatewayId(), request.getUserResourceProfile());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void deleteUserResourceProfile(DeleteUserResourceProfileRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            userResourceProfileService.deleteUserResourceProfile(ctx, request.getUserId(), request.getGatewayId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getAllUserResourceProfiles(
            GetAllUserResourceProfilesRequest request, StreamObserver<GetAllUserResourceProfilesResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<UserResourceProfile> profiles = userResourceProfileService.getAllUserResourceProfiles(ctx);
            observer.onNext(GetAllUserResourceProfilesResponse.newBuilder()
                    .addAllUserResourceProfiles(profiles)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    // --- User Compute Preferences ---

    @Override
    public void addUserComputePreference(AddUserComputePreferenceRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            userResourceProfileService.addUserComputeResourcePreference(
                    ctx,
                    request.getUserId(),
                    request.getGatewayId(),
                    request.getComputeResourceId(),
                    request.getUserComputeResourcePreference());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getUserComputePreference(
            GetUserComputePreferenceRequest request, StreamObserver<UserComputeResourcePreference> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            UserComputeResourcePreference result = userResourceProfileService.getUserComputeResourcePreference(
                    ctx, request.getUserId(), request.getGatewayId(), request.getComputeResourceId());
            observer.onNext(result);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void updateUserComputePreference(
            UpdateUserComputePreferenceRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            userResourceProfileService.updateUserComputeResourcePreference(
                    ctx,
                    request.getUserId(),
                    request.getGatewayId(),
                    request.getComputeResourceId(),
                    request.getUserComputeResourcePreference());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void deleteUserComputePreference(
            DeleteUserComputePreferenceRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            userResourceProfileService.deleteUserComputeResourcePreference(
                    ctx, request.getUserId(), request.getGatewayId(), request.getComputeResourceId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getAllUserComputePreferences(
            GetAllUserComputePreferencesRequest request,
            StreamObserver<GetAllUserComputePreferencesResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<UserComputeResourcePreference> prefs = userResourceProfileService.getAllUserComputeResourcePreferences(
                    ctx, request.getUserId(), request.getGatewayId());
            observer.onNext(GetAllUserComputePreferencesResponse.newBuilder()
                    .addAllUserComputeResourcePreferences(prefs)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    // --- User Storage Preferences ---

    @Override
    public void addUserStoragePreference(AddUserStoragePreferenceRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            userResourceProfileService.addUserStoragePreference(
                    ctx,
                    request.getUserId(),
                    request.getGatewayId(),
                    request.getStorageResourceId(),
                    request.getUserStoragePreference());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getUserStoragePreference(
            GetUserStoragePreferenceRequest request, StreamObserver<UserStoragePreference> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            UserStoragePreference result = userResourceProfileService.getUserStoragePreference(
                    ctx, request.getUserId(), request.getGatewayId(), request.getStorageResourceId());
            observer.onNext(result);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void updateUserStoragePreference(
            UpdateUserStoragePreferenceRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            userResourceProfileService.updateUserStoragePreference(
                    ctx,
                    request.getUserId(),
                    request.getGatewayId(),
                    request.getStorageResourceId(),
                    request.getUserStoragePreference());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void deleteUserStoragePreference(
            DeleteUserStoragePreferenceRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            userResourceProfileService.deleteUserStoragePreference(
                    ctx, request.getUserId(), request.getGatewayId(), request.getStorageResourceId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getAllUserStoragePreferences(
            GetAllUserStoragePreferencesRequest request,
            StreamObserver<GetAllUserStoragePreferencesResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<UserStoragePreference> prefs = userResourceProfileService.getAllUserStoragePreferences(
                    ctx, request.getUserId(), request.getGatewayId());
            observer.onNext(GetAllUserStoragePreferencesResponse.newBuilder()
                    .addAllUserStoragePreferences(prefs)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    // --- Queue Statuses ---

    @Override
    public void getLatestQueueStatuses(
            GetLatestQueueStatusesRequest request, StreamObserver<GetLatestQueueStatusesResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<QueueStatusModel> statuses = userResourceProfileService.getLatestQueueStatuses(ctx);
            observer.onNext(GetLatestQueueStatusesResponse.newBuilder()
                    .addAllQueueStatuses(statuses)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }
}
