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
package org.apache.airavata.sharing.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.airavata.api.groupmanager.*;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.grpc.GrpcRequestContext;
import org.apache.airavata.grpc.GrpcStatusMapper;
import org.apache.airavata.model.group.proto.GroupModel;
import org.apache.airavata.sharing.model.GroupAdminEntity;
import org.apache.airavata.sharing.model.UserGroupEntity;
import org.apache.airavata.sharing.service.SharingService;
import org.springframework.stereotype.Component;

@Component
public class GroupManagerGrpcService extends GroupManagerServiceGrpc.GroupManagerServiceImplBase {

    private final SharingService sharingHandler;

    public GroupManagerGrpcService(SharingService sharingHandler) {
        this.sharingHandler = sharingHandler;
    }

    @Override
    public void createGroup(CreateGroupRequest request, StreamObserver<CreateGroupResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            GroupModel group = request.getGroup();
            UserGroupEntity entity = toEntity(group, ctx.getGatewayId());
            String groupId = sharingHandler.createGroup(entity);
            observer.onNext(CreateGroupResponse.newBuilder().setGroupId(groupId).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void updateGroup(UpdateGroupRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            GroupModel group = request.getGroup();
            UserGroupEntity entity = toEntity(group, ctx.getGatewayId());
            sharingHandler.updateGroup(entity);
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void deleteGroup(DeleteGroupRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            sharingHandler.deleteGroup(ctx.getGatewayId(), request.getGroupId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getGroup(GetGroupRequest request, StreamObserver<GroupModel> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            UserGroupEntity entity = sharingHandler.getGroup(ctx.getGatewayId(), request.getGroupId());
            observer.onNext(toGroupModel(entity));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getGroups(GetGroupsRequest request, StreamObserver<GetGroupsResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<UserGroupEntity> groups = sharingHandler.getGroups(ctx.getGatewayId(), 0, -1);
            GetGroupsResponse.Builder builder = GetGroupsResponse.newBuilder();
            groups.forEach(g -> builder.addGroups(toGroupModel(g)));
            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getAllGroupsUserBelongs(
            GetAllGroupsUserBelongsRequest request, StreamObserver<GetAllGroupsUserBelongsResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<UserGroupEntity> groups =
                    sharingHandler.getAllMemberGroupEntitiesForUser(ctx.getGatewayId(), request.getUserName());
            GetAllGroupsUserBelongsResponse.Builder builder = GetAllGroupsUserBelongsResponse.newBuilder();
            groups.forEach(g -> builder.addGroups(toGroupModel(g)));
            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void addUsersToGroup(AddUsersToGroupRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            sharingHandler.addUsersToGroup(ctx.getGatewayId(), request.getUserIdsList(), request.getGroupId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void removeUsersFromGroup(RemoveUsersFromGroupRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            sharingHandler.removeUsersFromGroup(ctx.getGatewayId(), request.getUserIdsList(), request.getGroupId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void transferGroupOwnership(TransferGroupOwnershipRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            sharingHandler.transferGroupOwnership(ctx.getGatewayId(), request.getGroupId(), request.getNewOwnerId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void addGroupAdmins(AddGroupAdminsRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            sharingHandler.addGroupAdmins(ctx.getGatewayId(), request.getGroupId(), request.getAdminIdsList());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void removeGroupAdmins(RemoveGroupAdminsRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            sharingHandler.removeGroupAdmins(ctx.getGatewayId(), request.getGroupId(), request.getAdminIdsList());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void hasAdminAccess(HasAdminAccessRequest request, StreamObserver<HasAdminAccessResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            boolean result =
                    sharingHandler.hasAdminAccess(ctx.getGatewayId(), request.getGroupId(), request.getAdminId());
            observer.onNext(
                    HasAdminAccessResponse.newBuilder().setHasAccess(result).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void hasOwnerAccess(HasOwnerAccessRequest request, StreamObserver<HasOwnerAccessResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            boolean result =
                    sharingHandler.hasOwnerAccess(ctx.getGatewayId(), request.getGroupId(), request.getOwnerId());
            observer.onNext(
                    HasOwnerAccessResponse.newBuilder().setHasAccess(result).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    // --- Mapping helpers ---

    private static UserGroupEntity toEntity(GroupModel model, String domainId) {
        UserGroupEntity entity = new UserGroupEntity();
        if (!model.getId().isEmpty()) entity.setGroupId(model.getId());
        entity.setDomainId(domainId);
        if (!model.getName().isEmpty()) entity.setName(model.getName());
        if (!model.getDescription().isEmpty()) entity.setDescription(model.getDescription());
        if (!model.getOwnerId().isEmpty()) entity.setOwnerId(model.getOwnerId());
        return entity;
    }

    private static GroupModel toGroupModel(UserGroupEntity entity) {
        GroupModel.Builder b = GroupModel.newBuilder();
        if (entity.getGroupId() != null) b.setId(entity.getGroupId());
        if (entity.getName() != null) b.setName(entity.getName());
        if (entity.getOwnerId() != null) b.setOwnerId(entity.getOwnerId());
        if (entity.getDescription() != null) b.setDescription(entity.getDescription());
        if (entity.getGroupAdmins() != null) {
            b.addAllAdmins(entity.getGroupAdmins().stream()
                    .map(GroupAdminEntity::getAdminId)
                    .collect(Collectors.toList()));
        }
        return b.build();
    }
}
