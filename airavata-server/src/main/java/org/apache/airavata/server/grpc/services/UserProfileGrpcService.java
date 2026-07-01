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
package org.apache.airavata.server.grpc.services;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import java.util.List;
import org.apache.airavata.api.iam.userprofile.*;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.grpc.GrpcRequestContext;
import org.apache.airavata.grpc.GrpcStatusMapper;
import org.apache.airavata.iam.repository.UserProfileRepository;
import org.apache.airavata.iam.service.UserPreferencesService;
import org.apache.airavata.model.user.proto.UserProfile;
import org.springframework.stereotype.Component;

@Component
public class UserProfileGrpcService extends UserProfileServiceGrpc.UserProfileServiceImplBase {

    private final UserProfileRepository userProfileRepository;
    private final UserPreferencesService userPreferencesService;

    public UserProfileGrpcService(UserPreferencesService userPreferencesService) {
        this.userProfileRepository = new UserProfileRepository();
        this.userPreferencesService = userPreferencesService;
    }

    @Override
    public void addUserProfile(AddUserProfileRequest request, StreamObserver<AddUserProfileResponse> observer) {
        try {
            UserProfile created = userProfileRepository.createUserProfile(request.getUserProfile());
            observer.onNext(AddUserProfileResponse.newBuilder()
                    .setUserId(created.getUserId())
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void updateUserProfile(UpdateUserProfileRequest request, StreamObserver<Empty> observer) {
        try {
            userProfileRepository.updateUserProfile(request.getUserProfile(), null);
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getUserProfileById(GetUserProfileByIdRequest request, StreamObserver<UserProfile> observer) {
        try {
            UserProfile profile =
                    userProfileRepository.getUserProfileByIdAndGateWay(request.getUserId(), request.getGatewayId());
            if (profile == null) {
                observer.onError(io.grpc.Status.NOT_FOUND
                        .withDescription("User profile not found for userId=" + request.getUserId())
                        .asRuntimeException());
                return;
            }
            observer.onNext(profile);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getUserProfileByName(GetUserProfileByNameRequest request, StreamObserver<UserProfile> observer) {
        try {
            UserProfile profile =
                    userProfileRepository.getUserProfileByIdAndGateWay(request.getUserName(), request.getGatewayId());
            if (profile == null) {
                observer.onError(io.grpc.Status.NOT_FOUND
                        .withDescription("User profile not found for userName=" + request.getUserName())
                        .asRuntimeException());
                return;
            }
            observer.onNext(profile);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void deleteUserProfile(DeleteUserProfileRequest request, StreamObserver<Empty> observer) {
        try {
            userProfileRepository.delete(request.getUserId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getAllUserProfilesInGateway(
            GetAllUserProfilesInGatewayRequest request, StreamObserver<GetAllUserProfilesInGatewayResponse> observer) {
        try {
            List<UserProfile> profiles = userProfileRepository.getAllUserProfilesInGateway(
                    request.getGatewayId(), request.getOffset(), request.getLimit());
            observer.onNext(GetAllUserProfilesInGatewayResponse.newBuilder()
                    .addAllUserProfiles(profiles)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void doesUserExist(DoesUserExistRequest request, StreamObserver<DoesUserExistResponse> observer) {
        try {
            UserProfile profile =
                    userProfileRepository.getUserProfileByIdAndGateWay(request.getUserName(), request.getGatewayId());
            observer.onNext(DoesUserExistResponse.newBuilder()
                    .setExists(profile != null)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getUserPreferences(Empty request, StreamObserver<UserPreferences> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            observer.onNext(userPreferencesService.getUserPreferences(ctx));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void updateUserPreferences(UserPreferences request, StreamObserver<UserPreferences> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            observer.onNext(userPreferencesService.updateUserPreferences(ctx, request));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }
}
