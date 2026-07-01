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
package org.apache.airavata.iam.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import java.util.List;
import org.apache.airavata.api.iam.groupmanager.*;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.exception.ServiceNotFoundException;
import org.apache.airavata.grpc.GrpcRequestContext;
import org.apache.airavata.grpc.GrpcStatusMapper;
import org.apache.airavata.iam.model.UserGroupEntity;
import org.apache.airavata.iam.service.SharingService;
import org.apache.airavata.model.group.proto.GroupModel;
import org.springframework.stereotype.Component;

@Component
public class GroupManagerGrpcService extends GroupManagerServiceGrpc.GroupManagerServiceImplBase {

    private final SharingService sharingHandler;
    private final GroupWithAccessAssembler groupAssembler;

    public GroupManagerGrpcService(SharingService sharingHandler, GroupWithAccessAssembler groupAssembler) {
        this.sharingHandler = sharingHandler;
        this.groupAssembler = groupAssembler;
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
    public void createGroupReconciled(CreateGroupRequest request, StreamObserver<GroupWithAccess> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            GroupModel group = request.getGroup();
            UserGroupEntity entity = toEntity(group, ctx.getGatewayId());
            String groupId = sharingHandler.createGroup(entity);
            // New group: no current roster, so every desired non-owner member/admin is added.
            sharingHandler.reconcileGroupMembership(
                    ctx.getGatewayId(), groupId, group.getMembersList(), group.getAdminsList(), group.getOwnerId(), true);
            UserGroupEntity reloaded = sharingHandler.getGroup(ctx.getGatewayId(), groupId);
            if (reloaded == null) {
                throw new ServiceNotFoundException("Group " + groupId + " does not exist");
            }
            observer.onNext(groupAssembler.buildGroupWithAccess(ctx, reloaded));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void updateGroupReconciled(UpdateGroupRequest request, StreamObserver<GroupWithAccess> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            GroupModel group = request.getGroup();
            sharingHandler.reconcileGroupMembership(
                    ctx.getGatewayId(),
                    group.getId(),
                    group.getMembersList(),
                    group.getAdminsList(),
                    group.getOwnerId(),
                    false);
            UserGroupEntity entity = toEntity(group, ctx.getGatewayId());
            sharingHandler.updateGroup(entity);
            UserGroupEntity reloaded = sharingHandler.getGroup(ctx.getGatewayId(), group.getId());
            if (reloaded == null) {
                throw new ServiceNotFoundException("Group " + group.getId() + " does not exist");
            }
            observer.onNext(groupAssembler.buildGroupWithAccess(ctx, reloaded));
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
            if (entity == null) {
                throw new ServiceNotFoundException("Group " + request.getGroupId() + " does not exist");
            }
            observer.onNext(GroupWithAccessAssembler.toGroupModel(entity));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getGroupWithAccess(GetGroupRequest request, StreamObserver<GroupWithAccess> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            UserGroupEntity entity = sharingHandler.getGroup(ctx.getGatewayId(), request.getGroupId());
            if (entity == null) {
                throw new ServiceNotFoundException("Group " + request.getGroupId() + " does not exist");
            }
            observer.onNext(groupAssembler.buildGroupWithAccess(ctx, entity));
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
            groups.forEach(g -> builder.addGroups(GroupWithAccessAssembler.toGroupModel(g)));
            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getGroupsWithAccess(
            GetGroupsRequest request, StreamObserver<GetGroupsWithAccessResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<UserGroupEntity> groups = sharingHandler.getGroups(ctx.getGatewayId(), 0, -1);
            GetGroupsWithAccessResponse.Builder builder = GetGroupsWithAccessResponse.newBuilder();
            for (UserGroupEntity entity : groups) {
                builder.addGroups(groupAssembler.buildGroupWithAccess(ctx, entity));
            }
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
            groups.forEach(g -> builder.addGroups(GroupWithAccessAssembler.toGroupModel(g)));
            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getAllGroupsUserBelongsWithAccess(
            GetAllGroupsUserBelongsRequest request, StreamObserver<GetGroupsWithAccessResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<UserGroupEntity> groups =
                    sharingHandler.getAllMemberGroupEntitiesForUser(ctx.getGatewayId(), request.getUserName());
            GetGroupsWithAccessResponse.Builder builder = GetGroupsWithAccessResponse.newBuilder();
            for (UserGroupEntity entity : groups) {
                builder.addGroups(groupAssembler.buildGroupWithAccess(ctx, entity));
            }
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
}
