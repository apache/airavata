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
import org.apache.airavata.api.iam.*;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.config.ServerSettings;
import org.apache.airavata.grpc.GrpcRequestContext;
import org.apache.airavata.grpc.GrpcStatusMapper;
import org.apache.airavata.iam.service.TenantManagementInterface;
import org.apache.airavata.iam.service.TenantManagementKeycloakImpl;
import org.apache.airavata.model.credential.store.proto.PasswordCredential;
import org.apache.airavata.model.user.proto.UserProfile;
import org.apache.airavata.model.workspace.proto.Gateway;
import org.springframework.stereotype.Component;

@Component
public class IamAdminGrpcService extends IamAdminServiceGrpc.IamAdminServiceImplBase {

    private final TenantManagementInterface tenantManager;

    public IamAdminGrpcService() {
        this.tenantManager = new TenantManagementKeycloakImpl();
    }

    @Override
    public void setUpGateway(SetUpGatewayRequest request, StreamObserver<Gateway> observer) {
        try {
            PasswordCredential adminCreds = getSuperAdminCredentials();
            Gateway gateway = tenantManager.addTenant(adminCreds, request.getGateway());
            tenantManager.createTenantAdminAccount(adminCreds, request.getGateway(), null);
            gateway = tenantManager.configureClient(adminCreds, gateway);
            observer.onNext(gateway);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void isUsernameAvailable(
            IsUsernameAvailableRequest request, StreamObserver<IsUsernameAvailableResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            boolean available =
                    tenantManager.isUsernameAvailable(ctx.getAccessToken(), ctx.getGatewayId(), request.getUsername());
            observer.onNext(IsUsernameAvailableResponse.newBuilder()
                    .setAvailable(available)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void registerUser(RegisterUserRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            tenantManager.createUser(
                    ctx.getAccessToken(),
                    ctx.getGatewayId(),
                    request.getUsername(),
                    request.getEmailAddress(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getNewPassword());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void enableUser(EnableUserRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            tenantManager.enableUserAccount(ctx.getAccessToken(), ctx.getGatewayId(), request.getUsername());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void isUserEnabled(IsUserEnabledRequest request, StreamObserver<IsUserEnabledResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            boolean enabled =
                    tenantManager.isUserAccountEnabled(ctx.getAccessToken(), ctx.getGatewayId(), request.getUsername());
            observer.onNext(
                    IsUserEnabledResponse.newBuilder().setEnabled(enabled).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void isUserExist(IsUserExistRequest request, StreamObserver<IsUserExistResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            boolean exists = tenantManager.isUserExist(ctx.getAccessToken(), ctx.getGatewayId(), request.getUsername());
            observer.onNext(IsUserExistResponse.newBuilder().setExists(exists).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getUser(GetIamUserRequest request, StreamObserver<UserProfile> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            UserProfile user = tenantManager.getUser(ctx.getAccessToken(), ctx.getGatewayId(), request.getUsername());
            observer.onNext(user);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getUsers(GetIamUsersRequest request, StreamObserver<GetIamUsersResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<UserProfile> users = tenantManager.getUsers(
                    ctx.getAccessToken(),
                    ctx.getGatewayId(),
                    request.getOffset(),
                    request.getLimit(),
                    request.getSearch());
            observer.onNext(GetIamUsersResponse.newBuilder().addAllUsers(users).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void resetUserPassword(ResetUserPasswordRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            tenantManager.resetUserPassword(
                    ctx.getAccessToken(), ctx.getGatewayId(), request.getUsername(), request.getNewPassword());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void findUsers(FindUsersRequest request, StreamObserver<FindUsersResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<UserProfile> users = tenantManager.findUser(
                    ctx.getAccessToken(), ctx.getGatewayId(),
                    request.getEmail(), request.getUserId());
            observer.onNext(FindUsersResponse.newBuilder().addAllUsers(users).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void updateUserProfile(UpdateIamUserProfileRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            UserProfile userDetails = request.getUserDetails();
            tenantManager.updateUserProfile(
                    ctx.getAccessToken(), ctx.getGatewayId(), userDetails.getUserId(), userDetails);
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void deleteUser(DeleteUserRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            tenantManager.deleteUser(ctx.getAccessToken(), ctx.getGatewayId(), request.getUsername());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void addRoleToUser(AddRoleToUserRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            PasswordCredential adminCreds = getSuperAdminCredentials();
            tenantManager.addRoleToUser(adminCreds, ctx.getGatewayId(), request.getUsername(), request.getRoleName());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void removeRoleFromUser(RemoveRoleFromUserRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            PasswordCredential adminCreds = getSuperAdminCredentials();
            tenantManager.removeRoleFromUser(
                    adminCreds, ctx.getGatewayId(), request.getUsername(), request.getRoleName());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getUsersWithRole(GetUsersWithRoleRequest request, StreamObserver<GetUsersWithRoleResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            PasswordCredential adminCreds = getSuperAdminCredentials();
            List<UserProfile> users =
                    tenantManager.getUsersWithRole(adminCreds, ctx.getGatewayId(), request.getRoleName());
            observer.onNext(
                    GetUsersWithRoleResponse.newBuilder().addAllUsers(users).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    // --- Helper ---

    private static PasswordCredential getSuperAdminCredentials() throws Exception {
        return PasswordCredential.newBuilder()
                .setLoginUserName(ServerSettings.getIamServerSuperAdminUsername())
                .setPassword(ServerSettings.getIamServerSuperAdminPassword())
                .build();
    }
}
