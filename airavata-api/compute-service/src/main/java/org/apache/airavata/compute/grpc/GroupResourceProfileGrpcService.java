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
import org.apache.airavata.api.groupprofile.*;
import org.apache.airavata.compute.service.GroupResourceProfileService;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.grpc.GrpcRequestContext;
import org.apache.airavata.grpc.GrpcStatusMapper;
import org.apache.airavata.model.appcatalog.gatewaygroups.proto.GatewayGroups;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.BatchQueueResourcePolicy;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.ComputeResourcePolicy;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupResourceProfile;
import org.springframework.stereotype.Component;

@Component
public class GroupResourceProfileGrpcService
        extends GroupResourceProfileServiceGrpc.GroupResourceProfileServiceImplBase {

    private final GroupResourceProfileService groupResourceProfileService;

    public GroupResourceProfileGrpcService(GroupResourceProfileService groupResourceProfileService) {
        this.groupResourceProfileService = groupResourceProfileService;
    }

    // --- Group Resource Profiles ---

    @Override
    public void createGroupResourceProfile(
            CreateGroupResourceProfileRequest request, StreamObserver<GroupResourceProfile> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            String id = groupResourceProfileService.createGroupResourceProfile(ctx, request.getGroupResourceProfile());
            // Return the profile with the generated ID
            GroupResourceProfile created = groupResourceProfileService.getGroupResourceProfile(ctx, id);
            observer.onNext(created);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getGroupResourceProfile(
            GetGroupResourceProfileRequest request, StreamObserver<GroupResourceProfile> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            GroupResourceProfile result =
                    groupResourceProfileService.getGroupResourceProfile(ctx, request.getGroupResourceProfileId());
            observer.onNext(result);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void updateGroupResourceProfile(UpdateGroupResourceProfileRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            groupResourceProfileService.updateGroupResourceProfile(ctx, request.getGroupResourceProfile());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void removeGroupResourceProfile(RemoveGroupResourceProfileRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            groupResourceProfileService.removeGroupResourceProfile(ctx, request.getGroupResourceProfileId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getGroupResourceList(
            GetGroupResourceListRequest request, StreamObserver<GetGroupResourceListResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<GroupResourceProfile> profiles =
                    groupResourceProfileService.getGroupResourceList(ctx, ctx.getGatewayId());
            observer.onNext(GetGroupResourceListResponse.newBuilder()
                    .addAllGroupResourceProfiles(profiles)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    // --- Group Compute Preferences ---

    @Override
    public void getGroupComputePreference(
            GetGroupComputePreferenceRequest request, StreamObserver<GroupComputeResourcePreference> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            GroupComputeResourcePreference result = groupResourceProfileService.getGroupComputeResourcePreference(
                    ctx, request.getComputeResourceId(), request.getGroupResourceProfileId());
            observer.onNext(result);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void removeGroupComputePrefs(RemoveGroupComputePrefsRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            groupResourceProfileService.removeGroupComputePrefs(
                    ctx, request.getComputeResourceId(), request.getGroupResourceProfileId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getGroupComputePrefList(
            GetGroupComputePrefListRequest request, StreamObserver<GetGroupComputePrefListResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<GroupComputeResourcePreference> prefs = groupResourceProfileService.getGroupComputeResourcePrefList(
                    ctx, request.getGroupResourceProfileId());
            observer.onNext(GetGroupComputePrefListResponse.newBuilder()
                    .addAllGroupComputeResourcePreferences(prefs)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    // --- Compute Resource Policies ---

    @Override
    public void getGroupComputeResourcePolicy(
            GetGroupComputeResourcePolicyRequest request, StreamObserver<ComputeResourcePolicy> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            ComputeResourcePolicy result =
                    groupResourceProfileService.getGroupComputeResourcePolicy(ctx, request.getResourcePolicyId());
            observer.onNext(result);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void removeGroupComputeResourcePolicy(
            RemoveGroupComputeResourcePolicyRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            groupResourceProfileService.removeGroupComputeResourcePolicy(ctx, request.getResourcePolicyId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getGroupComputeResourcePolicyList(
            GetGroupComputeResourcePolicyListRequest request,
            StreamObserver<GetGroupComputeResourcePolicyListResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<ComputeResourcePolicy> policies = groupResourceProfileService.getGroupComputeResourcePolicyList(
                    ctx, request.getGroupResourceProfileId());
            observer.onNext(GetGroupComputeResourcePolicyListResponse.newBuilder()
                    .addAllComputeResourcePolicies(policies)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    // --- Batch Queue Resource Policies ---

    @Override
    public void getBatchQueueResourcePolicy(
            GetBatchQueueResourcePolicyRequest request, StreamObserver<BatchQueueResourcePolicy> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            BatchQueueResourcePolicy result =
                    groupResourceProfileService.getBatchQueueResourcePolicy(ctx, request.getResourcePolicyId());
            observer.onNext(result);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void removeGroupBatchQueueResourcePolicy(
            RemoveGroupBatchQueueResourcePolicyRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            groupResourceProfileService.removeGroupBatchQueueResourcePolicy(ctx, request.getResourcePolicyId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getGroupBatchQueuePolicyList(
            GetGroupBatchQueuePolicyListRequest request,
            StreamObserver<GetGroupBatchQueuePolicyListResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<BatchQueueResourcePolicy> policies = groupResourceProfileService.getGroupBatchQueueResourcePolicyList(
                    ctx, request.getGroupResourceProfileId());
            observer.onNext(GetGroupBatchQueuePolicyListResponse.newBuilder()
                    .addAllBatchQueueResourcePolicies(policies)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    // --- Gateway Groups ---

    @Override
    public void getGatewayGroups(GetGatewayGroupsRequest request, StreamObserver<GatewayGroups> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            GatewayGroups result = groupResourceProfileService.getGatewayGroups(ctx);
            observer.onNext(result);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }
}
