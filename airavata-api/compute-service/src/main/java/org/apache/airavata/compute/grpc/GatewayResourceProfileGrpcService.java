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
import org.apache.airavata.api.gatewayprofile.*;
import org.apache.airavata.compute.service.GatewayResourceProfileService;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.grpc.GrpcRequestContext;
import org.apache.airavata.grpc.GrpcStatusMapper;
import org.apache.airavata.model.appcatalog.accountprovisioning.proto.SSHAccountProvisioner;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.GatewayResourceProfile;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.StoragePreference;
import org.springframework.stereotype.Component;

@Component
public class GatewayResourceProfileGrpcService
        extends GatewayResourceProfileServiceGrpc.GatewayResourceProfileServiceImplBase {

    private final GatewayResourceProfileService gatewayResourceProfileService;

    public GatewayResourceProfileGrpcService(GatewayResourceProfileService gatewayResourceProfileService) {
        this.gatewayResourceProfileService = gatewayResourceProfileService;
    }

    // --- Gateway Resource Profiles ---

    @Override
    public void registerGatewayResourceProfile(
            RegisterGatewayResourceProfileRequest request,
            StreamObserver<RegisterGatewayResourceProfileResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            String id = gatewayResourceProfileService.registerGatewayResourceProfile(
                    ctx, request.getGatewayResourceProfile());
            observer.onNext(RegisterGatewayResourceProfileResponse.newBuilder()
                    .setGatewayId(id)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getGatewayResourceProfile(
            GetGatewayResourceProfileRequest request, StreamObserver<GatewayResourceProfile> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            GatewayResourceProfile result =
                    gatewayResourceProfileService.getGatewayResourceProfile(ctx, request.getGatewayId());
            observer.onNext(result);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void updateGatewayResourceProfile(
            UpdateGatewayResourceProfileRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            gatewayResourceProfileService.updateGatewayResourceProfile(
                    ctx, request.getGatewayId(), request.getGatewayResourceProfile());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void deleteGatewayResourceProfile(
            DeleteGatewayResourceProfileRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            gatewayResourceProfileService.deleteGatewayResourceProfile(ctx, request.getGatewayId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getAllGatewayResourceProfiles(
            GetAllGatewayResourceProfilesRequest request,
            StreamObserver<GetAllGatewayResourceProfilesResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<GatewayResourceProfile> profiles = gatewayResourceProfileService.getAllGatewayResourceProfiles(ctx);
            observer.onNext(GetAllGatewayResourceProfilesResponse.newBuilder()
                    .addAllGatewayResourceProfiles(profiles)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    // --- Compute Preferences ---

    @Override
    public void addComputePreference(AddComputePreferenceRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            gatewayResourceProfileService.addGatewayComputeResourcePreference(
                    ctx,
                    request.getGatewayId(),
                    request.getComputeResourceId(),
                    request.getComputeResourcePreference());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getComputePreference(
            GetComputePreferenceRequest request, StreamObserver<ComputeResourcePreference> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            ComputeResourcePreference result = gatewayResourceProfileService.getGatewayComputeResourcePreference(
                    ctx, request.getGatewayId(), request.getComputeResourceId());
            observer.onNext(result);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void updateComputePreference(UpdateComputePreferenceRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            gatewayResourceProfileService.updateGatewayComputeResourcePreference(
                    ctx,
                    request.getGatewayId(),
                    request.getComputeResourceId(),
                    request.getComputeResourcePreference());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void deleteComputePreference(DeleteComputePreferenceRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            gatewayResourceProfileService.deleteGatewayComputeResourcePreference(
                    ctx, request.getGatewayId(), request.getComputeResourceId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getAllComputePreferences(
            GetAllComputePreferencesRequest request, StreamObserver<GetAllComputePreferencesResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<ComputeResourcePreference> prefs =
                    gatewayResourceProfileService.getAllGatewayComputeResourcePreferences(ctx, request.getGatewayId());
            observer.onNext(GetAllComputePreferencesResponse.newBuilder()
                    .addAllComputeResourcePreferences(prefs)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    // --- Storage Preferences ---

    @Override
    public void addStoragePreference(AddStoragePreferenceRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            gatewayResourceProfileService.addGatewayStoragePreference(
                    ctx, request.getGatewayId(), request.getStorageResourceId(), request.getStoragePreference());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getStoragePreference(GetStoragePreferenceRequest request, StreamObserver<StoragePreference> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            StoragePreference result = gatewayResourceProfileService.getGatewayStoragePreference(
                    ctx, request.getGatewayId(), request.getStorageResourceId());
            observer.onNext(result);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void updateStoragePreference(UpdateStoragePreferenceRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            gatewayResourceProfileService.updateGatewayStoragePreference(
                    ctx, request.getGatewayId(), request.getStorageResourceId(), request.getStoragePreference());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void deleteStoragePreference(DeleteStoragePreferenceRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            gatewayResourceProfileService.deleteGatewayStoragePreference(
                    ctx, request.getGatewayId(), request.getStorageResourceId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getAllStoragePreferences(
            GetAllStoragePreferencesRequest request, StreamObserver<GetAllStoragePreferencesResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<StoragePreference> prefs =
                    gatewayResourceProfileService.getAllGatewayStoragePreferences(ctx, request.getGatewayId());
            observer.onNext(GetAllStoragePreferencesResponse.newBuilder()
                    .addAllStoragePreferences(prefs)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    // --- SSH Account Provisioners ---

    @Override
    public void getSSHAccountProvisioners(
            GetSSHAccountProvisionersRequest request, StreamObserver<GetSSHAccountProvisionersResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<SSHAccountProvisioner> provisioners = gatewayResourceProfileService.getSSHAccountProvisioners(ctx);
            observer.onNext(GetSSHAccountProvisionersResponse.newBuilder()
                    .addAllSshAccountProvisioners(provisioners)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }
}
