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

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.api.sharing.*;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.grpc.GrpcRequestContext;
import org.apache.airavata.grpc.GrpcStatusMapper;
import org.apache.airavata.model.group.proto.ResourcePermissionType;
import org.apache.airavata.sharing.service.ResourceSharingService;
import org.apache.airavata.sharing.service.SharingService;
import org.springframework.stereotype.Component;

@Component
public class SharingGrpcService extends SharingServiceGrpc.SharingServiceImplBase {

    private final ResourceSharingService resourceSharingService;
    private final SharingService sharingHandler;

    public SharingGrpcService(ResourceSharingService resourceSharingService, SharingService sharingHandler) {
        this.resourceSharingService = resourceSharingService;
        this.sharingHandler = sharingHandler;
    }

    // ========================================================================
    // Resource sharing RPCs (existing)
    // ========================================================================

    @Override
    public void shareResourceWithUsers(ShareResourceWithUsersRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            Map<String, ResourcePermissionType> permissions = toResourcePermissionMap(request.getUserPermissionsMap());
            resourceSharingService.shareResourceWithUsers(ctx, request.getResourceId(), permissions);
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void shareResourceWithGroups(ShareResourceWithGroupsRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            Map<String, ResourcePermissionType> permissions = toResourcePermissionMap(request.getGroupPermissionsMap());
            resourceSharingService.shareResourceWithGroups(ctx, request.getResourceId(), permissions);
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void revokeFromUsers(RevokeFromUsersRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            Map<String, ResourcePermissionType> permissions = toResourcePermissionMap(request.getUserPermissionsMap());
            resourceSharingService.revokeSharingOfResourceFromUsers(ctx, request.getResourceId(), permissions);
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void revokeFromGroups(RevokeFromGroupsRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            Map<String, ResourcePermissionType> permissions = toResourcePermissionMap(request.getGroupPermissionsMap());
            resourceSharingService.revokeSharingOfResourceFromGroups(ctx, request.getResourceId(), permissions);
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getAllAccessibleUsers(
            GetAllAccessibleUsersRequest request, StreamObserver<GetAllAccessibleUsersResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            ResourcePermissionType permType = ResourcePermissionType.valueOf(request.getPermissionType());
            List<String> users = resourceSharingService.getAllAccessibleUsers(ctx, request.getResourceId(), permType);
            observer.onNext(GetAllAccessibleUsersResponse.newBuilder()
                    .addAllUserIds(users)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getAllDirectlyAccessibleUsers(
            GetAllDirectlyAccessibleUsersRequest request,
            StreamObserver<GetAllDirectlyAccessibleUsersResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            ResourcePermissionType permType = ResourcePermissionType.valueOf(request.getPermissionType());
            List<String> users =
                    resourceSharingService.getAllDirectlyAccessibleUsers(ctx, request.getResourceId(), permType);
            observer.onNext(GetAllDirectlyAccessibleUsersResponse.newBuilder()
                    .addAllUserIds(users)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getAllAccessibleGroups(
            GetAllAccessibleGroupsRequest request, StreamObserver<GetAllAccessibleGroupsResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            ResourcePermissionType permType = ResourcePermissionType.valueOf(request.getPermissionType());
            List<String> groups = resourceSharingService.getAllAccessibleGroups(ctx, request.getResourceId(), permType);
            observer.onNext(GetAllAccessibleGroupsResponse.newBuilder()
                    .addAllGroupIds(groups)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getAllDirectlyAccessibleGroups(
            GetAllDirectlyAccessibleGroupsRequest request,
            StreamObserver<GetAllDirectlyAccessibleGroupsResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            ResourcePermissionType permType = ResourcePermissionType.valueOf(request.getPermissionType());
            List<String> groups =
                    resourceSharingService.getAllDirectlyAccessibleGroups(ctx, request.getResourceId(), permType);
            observer.onNext(GetAllDirectlyAccessibleGroupsResponse.newBuilder()
                    .addAllGroupIds(groups)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void userHasAccess(UserHasAccessRequest request, StreamObserver<UserHasAccessResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            ResourcePermissionType permType = ResourcePermissionType.valueOf(request.getPermissionType());
            boolean hasAccess = resourceSharingService.userHasAccess(ctx, request.getResourceId(), permType);
            observer.onNext(
                    UserHasAccessResponse.newBuilder().setHasAccess(hasAccess).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    // ========================================================================
    // Domain CRUD
    // ========================================================================

    @Override
    public void createDomain(CreateDomainRequest request, StreamObserver<CreateDomainResponse> observer) {
        try {
            var domain = toThriftDomain(request.getDomain());
            String id = sharingHandler.createDomain(domain);
            observer.onNext(CreateDomainResponse.newBuilder().setDomainId(id).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void updateDomain(UpdateDomainRequest request, StreamObserver<Empty> observer) {
        try {
            sharingHandler.updateDomain(toThriftDomain(request.getDomain()));
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void isDomainExists(IsDomainExistsRequest request, StreamObserver<IsDomainExistsResponse> observer) {
        try {
            boolean exists = sharingHandler.isDomainExists(request.getDomainId());
            observer.onNext(
                    IsDomainExistsResponse.newBuilder().setExists(exists).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void deleteDomain(DeleteDomainRequest request, StreamObserver<Empty> observer) {
        try {
            sharingHandler.deleteDomain(request.getDomainId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getDomain(
            GetDomainRequest request,
            StreamObserver<org.apache.airavata.sharing.registry.models.proto.Domain> observer) {
        try {
            var domain = sharingHandler.getDomain(request.getDomainId());
            observer.onNext(toProtoDomain(domain));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getDomains(GetDomainsRequest request, StreamObserver<GetDomainsResponse> observer) {
        try {
            var domains = sharingHandler.getDomains(request.getOffset(), request.getLimit());
            var builder = GetDomainsResponse.newBuilder();
            domains.forEach(d -> builder.addDomains(toProtoDomain(d)));
            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    // ========================================================================
    // User CRUD
    // ========================================================================

    @Override
    public void createUser(CreateUserRequest request, StreamObserver<CreateUserResponse> observer) {
        try {
            String id = sharingHandler.createUser(toThriftUser(request.getUser()));
            observer.onNext(CreateUserResponse.newBuilder().setUserId(id).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void updateUser(UpdateUserRequest request, StreamObserver<Empty> observer) {
        try {
            sharingHandler.updatedUser(toThriftUser(request.getUser()));
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void isUserExists(IsUserExistsRequest request, StreamObserver<IsUserExistsResponse> observer) {
        try {
            boolean exists = sharingHandler.isUserExists(request.getDomainId(), request.getUserId());
            observer.onNext(IsUserExistsResponse.newBuilder().setExists(exists).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void deleteUser(DeleteUserRequest request, StreamObserver<Empty> observer) {
        try {
            sharingHandler.deleteUser(request.getDomainId(), request.getUserId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getUser(
            GetUserRequest request, StreamObserver<org.apache.airavata.sharing.registry.models.proto.User> observer) {
        try {
            var user = sharingHandler.getUser(request.getDomainId(), request.getUserId());
            observer.onNext(toProtoUser(user));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getUsers(GetUsersRequest request, StreamObserver<GetUsersResponse> observer) {
        try {
            var users = sharingHandler.getUsers(request.getDomainId(), request.getOffset(), request.getLimit());
            var builder = GetUsersResponse.newBuilder();
            users.forEach(u -> builder.addUsers(toProtoUser(u)));
            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    // ========================================================================
    // Group CRUD
    // ========================================================================

    @Override
    public void createGroup(CreateGroupRequest request, StreamObserver<CreateGroupResponse> observer) {
        try {
            String id = sharingHandler.createGroup(toThriftUserGroup(request.getGroup()));
            observer.onNext(CreateGroupResponse.newBuilder().setGroupId(id).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void updateGroup(UpdateGroupRequest request, StreamObserver<Empty> observer) {
        try {
            sharingHandler.updateGroup(toThriftUserGroup(request.getGroup()));
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void isGroupExists(IsGroupExistsRequest request, StreamObserver<IsGroupExistsResponse> observer) {
        try {
            boolean exists = sharingHandler.isGroupExists(request.getDomainId(), request.getGroupId());
            observer.onNext(IsGroupExistsResponse.newBuilder().setExists(exists).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void deleteGroup(DeleteGroupRequest request, StreamObserver<Empty> observer) {
        try {
            sharingHandler.deleteGroup(request.getDomainId(), request.getGroupId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getGroup(
            GetGroupRequest request,
            StreamObserver<org.apache.airavata.sharing.registry.models.proto.UserGroup> observer) {
        try {
            var group = sharingHandler.getGroup(request.getDomainId(), request.getGroupId());
            observer.onNext(toProtoUserGroup(group));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getGroups(GetGroupsRequest request, StreamObserver<GetGroupsResponse> observer) {
        try {
            var groups = sharingHandler.getGroups(request.getDomainId(), request.getOffset(), request.getLimit());
            var builder = GetGroupsResponse.newBuilder();
            groups.forEach(g -> builder.addGroups(toProtoUserGroup(g)));
            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    // ========================================================================
    // Group membership
    // ========================================================================

    @Override
    public void addUsersToGroup(AddUsersToGroupRequest request, StreamObserver<Empty> observer) {
        try {
            sharingHandler.addUsersToGroup(request.getDomainId(), request.getUserIdsList(), request.getGroupId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void removeUsersFromGroup(RemoveUsersFromGroupRequest request, StreamObserver<Empty> observer) {
        try {
            sharingHandler.removeUsersFromGroup(request.getDomainId(), request.getUserIdsList(), request.getGroupId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void transferGroupOwnership(TransferGroupOwnershipRequest request, StreamObserver<Empty> observer) {
        try {
            sharingHandler.transferGroupOwnership(request.getDomainId(), request.getGroupId(), request.getNewOwnerId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void addGroupAdmins(AddGroupAdminsRequest request, StreamObserver<Empty> observer) {
        try {
            sharingHandler.addGroupAdmins(request.getDomainId(), request.getGroupId(), request.getAdminIdsList());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void removeGroupAdmins(RemoveGroupAdminsRequest request, StreamObserver<Empty> observer) {
        try {
            sharingHandler.removeGroupAdmins(request.getDomainId(), request.getGroupId(), request.getAdminIdsList());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void hasAdminAccess(HasAdminAccessRequest request, StreamObserver<HasAdminAccessResponse> observer) {
        try {
            boolean result =
                    sharingHandler.hasAdminAccess(request.getDomainId(), request.getGroupId(), request.getAdminId());
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
            boolean result =
                    sharingHandler.hasOwnerAccess(request.getDomainId(), request.getGroupId(), request.getOwnerId());
            observer.onNext(
                    HasOwnerAccessResponse.newBuilder().setHasAccess(result).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getGroupMembersOfTypeUser(
            GetGroupMembersOfTypeUserRequest request, StreamObserver<GetGroupMembersOfTypeUserResponse> observer) {
        try {
            var users = sharingHandler.getGroupMembersOfTypeUser(
                    request.getDomainId(), request.getGroupId(), request.getOffset(), request.getLimit());
            var builder = GetGroupMembersOfTypeUserResponse.newBuilder();
            users.forEach(u -> builder.addUsers(toProtoUser(u)));
            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getGroupMembersOfTypeGroup(
            GetGroupMembersOfTypeGroupRequest request, StreamObserver<GetGroupMembersOfTypeGroupResponse> observer) {
        try {
            var groups = sharingHandler.getGroupMembersOfTypeGroup(
                    request.getDomainId(), request.getGroupId(), request.getOffset(), request.getLimit());
            var builder = GetGroupMembersOfTypeGroupResponse.newBuilder();
            groups.forEach(g -> builder.addGroups(toProtoUserGroup(g)));
            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void addChildGroupsToParentGroup(
            AddChildGroupsToParentGroupRequest request, StreamObserver<Empty> observer) {
        try {
            sharingHandler.addChildGroupsToParentGroup(
                    request.getDomainId(), request.getChildIdsList(), request.getGroupId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void removeChildGroupFromParentGroup(
            RemoveChildGroupFromParentGroupRequest request, StreamObserver<Empty> observer) {
        try {
            sharingHandler.removeChildGroupFromParentGroup(
                    request.getDomainId(), request.getChildId(), request.getGroupId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getAllMemberGroupsForUser(
            GetAllMemberGroupsForUserRequest request, StreamObserver<GetAllMemberGroupsForUserResponse> observer) {
        try {
            var groups = sharingHandler.getAllMemberGroupEntitiesForUser(request.getDomainId(), request.getUserId());
            var builder = GetAllMemberGroupsForUserResponse.newBuilder();
            groups.forEach(g -> builder.addGroups(toProtoUserGroup(g)));
            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    // ========================================================================
    // Entity type CRUD
    // ========================================================================

    @Override
    public void createEntityType(CreateEntityTypeRequest request, StreamObserver<CreateEntityTypeResponse> observer) {
        try {
            String id = sharingHandler.createEntityType(toThriftEntityType(request.getEntityType()));
            observer.onNext(
                    CreateEntityTypeResponse.newBuilder().setEntityTypeId(id).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void updateEntityType(UpdateEntityTypeRequest request, StreamObserver<Empty> observer) {
        try {
            sharingHandler.updateEntityType(toThriftEntityType(request.getEntityType()));
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void isEntityTypeExists(
            IsEntityTypeExistsRequest request, StreamObserver<IsEntityTypeExistsResponse> observer) {
        try {
            boolean exists = sharingHandler.isEntityTypeExists(request.getDomainId(), request.getEntityTypeId());
            observer.onNext(
                    IsEntityTypeExistsResponse.newBuilder().setExists(exists).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void deleteEntityType(DeleteEntityTypeRequest request, StreamObserver<Empty> observer) {
        try {
            sharingHandler.deleteEntityType(request.getDomainId(), request.getEntityTypeId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getEntityType(
            GetEntityTypeRequest request,
            StreamObserver<org.apache.airavata.sharing.registry.models.proto.EntityType> observer) {
        try {
            var et = sharingHandler.getEntityType(request.getDomainId(), request.getEntityTypeId());
            observer.onNext(toProtoEntityType(et));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getEntityTypes(GetEntityTypesRequest request, StreamObserver<GetEntityTypesResponse> observer) {
        try {
            var types = sharingHandler.getEntityTypes(request.getDomainId(), request.getOffset(), request.getLimit());
            var builder = GetEntityTypesResponse.newBuilder();
            types.forEach(t -> builder.addEntityTypes(toProtoEntityType(t)));
            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    // ========================================================================
    // Entity CRUD
    // ========================================================================

    @Override
    public void createEntity(CreateEntityRequest request, StreamObserver<CreateEntityResponse> observer) {
        try {
            String id = sharingHandler.createEntity(toThriftEntity(request.getEntity()));
            observer.onNext(CreateEntityResponse.newBuilder().setEntityId(id).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void updateEntity(UpdateEntityRequest request, StreamObserver<Empty> observer) {
        try {
            sharingHandler.updateEntity(toThriftEntity(request.getEntity()));
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void isEntityExists(IsEntityExistsRequest request, StreamObserver<IsEntityExistsResponse> observer) {
        try {
            boolean exists = sharingHandler.isEntityExists(request.getDomainId(), request.getEntityId());
            observer.onNext(
                    IsEntityExistsResponse.newBuilder().setExists(exists).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void deleteEntity(DeleteEntityRequest request, StreamObserver<Empty> observer) {
        try {
            sharingHandler.deleteEntity(request.getDomainId(), request.getEntityId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getEntity(
            GetEntityRequest request,
            StreamObserver<org.apache.airavata.sharing.registry.models.proto.Entity> observer) {
        try {
            var entity = sharingHandler.getEntity(request.getDomainId(), request.getEntityId());
            observer.onNext(toProtoEntity(entity));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void searchEntities(SearchEntitiesRequest request, StreamObserver<SearchEntitiesResponse> observer) {
        try {
            var entities = sharingHandler.searchEntities(
                    request.getDomainId(),
                    request.getUserId(),
                    request.getFiltersList(),
                    request.getOffset(),
                    request.getLimit());
            var builder = SearchEntitiesResponse.newBuilder();
            entities.forEach(e -> builder.addEntities(toProtoEntity(e)));
            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getListOfSharedUsers(
            GetListOfSharedUsersRequest request, StreamObserver<GetListOfSharedUsersResponse> observer) {
        try {
            var users = sharingHandler.getListOfSharedUsers(
                    request.getDomainId(), request.getEntityId(), request.getPermissionTypeId());
            var builder = GetListOfSharedUsersResponse.newBuilder();
            users.forEach(u -> builder.addUsers(toProtoUser(u)));
            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getListOfDirectlySharedUsers(
            GetListOfDirectlySharedUsersRequest request,
            StreamObserver<GetListOfDirectlySharedUsersResponse> observer) {
        try {
            var users = sharingHandler.getListOfDirectlySharedUsers(
                    request.getDomainId(), request.getEntityId(), request.getPermissionTypeId());
            var builder = GetListOfDirectlySharedUsersResponse.newBuilder();
            users.forEach(u -> builder.addUsers(toProtoUser(u)));
            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getListOfSharedGroups(
            GetListOfSharedGroupsRequest request, StreamObserver<GetListOfSharedGroupsResponse> observer) {
        try {
            var groups = sharingHandler.getListOfSharedGroups(
                    request.getDomainId(), request.getEntityId(), request.getPermissionTypeId());
            var builder = GetListOfSharedGroupsResponse.newBuilder();
            groups.forEach(g -> builder.addGroups(toProtoUserGroup(g)));
            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getListOfDirectlySharedGroups(
            GetListOfDirectlySharedGroupsRequest request,
            StreamObserver<GetListOfDirectlySharedGroupsResponse> observer) {
        try {
            var groups = sharingHandler.getListOfDirectlySharedGroups(
                    request.getDomainId(), request.getEntityId(), request.getPermissionTypeId());
            var builder = GetListOfDirectlySharedGroupsResponse.newBuilder();
            groups.forEach(g -> builder.addGroups(toProtoUserGroup(g)));
            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    // ========================================================================
    // Permission type CRUD
    // ========================================================================

    @Override
    public void createPermissionType(
            CreatePermissionTypeRequest request, StreamObserver<CreatePermissionTypeResponse> observer) {
        try {
            String id = sharingHandler.createPermissionType(toThriftPermissionType(request.getPermissionType()));
            observer.onNext(CreatePermissionTypeResponse.newBuilder()
                    .setPermissionTypeId(id)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void updatePermissionType(UpdatePermissionTypeRequest request, StreamObserver<Empty> observer) {
        try {
            sharingHandler.updatePermissionType(toThriftPermissionType(request.getPermissionType()));
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void isPermissionExists(
            IsPermissionExistsRequest request, StreamObserver<IsPermissionExistsResponse> observer) {
        try {
            boolean exists = sharingHandler.isPermissionExists(request.getDomainId(), request.getPermissionId());
            observer.onNext(
                    IsPermissionExistsResponse.newBuilder().setExists(exists).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void deletePermissionType(DeletePermissionTypeRequest request, StreamObserver<Empty> observer) {
        try {
            sharingHandler.deletePermissionType(request.getDomainId(), request.getPermissionTypeId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getPermissionType(
            GetPermissionTypeRequest request,
            StreamObserver<org.apache.airavata.sharing.registry.models.proto.PermissionType> observer) {
        try {
            var pt = sharingHandler.getPermissionType(request.getDomainId(), request.getPermissionTypeId());
            observer.onNext(toProtoPermissionType(pt));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getPermissionTypes(
            GetPermissionTypesRequest request, StreamObserver<GetPermissionTypesResponse> observer) {
        try {
            var types =
                    sharingHandler.getPermissionTypes(request.getDomainId(), request.getOffset(), request.getLimit());
            var builder = GetPermissionTypesResponse.newBuilder();
            types.forEach(t -> builder.addPermissionTypes(toProtoPermissionType(t)));
            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    // ========================================================================
    // Entity sharing (Thrift-compatible)
    // ========================================================================

    @Override
    public void shareEntityWithUsers(ShareEntityWithUsersRequest request, StreamObserver<Empty> observer) {
        try {
            sharingHandler.shareEntityWithUsers(
                    request.getDomainId(),
                    request.getEntityId(),
                    request.getUserListList(),
                    request.getPermissionTypeId(),
                    request.getCascadePermission());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void revokeEntitySharingFromUsers(
            RevokeEntitySharingFromUsersRequest request, StreamObserver<Empty> observer) {
        try {
            sharingHandler.revokeEntitySharingFromUsers(
                    request.getDomainId(), request.getEntityId(),
                    request.getUserListList(), request.getPermissionTypeId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void shareEntityWithGroups(ShareEntityWithGroupsRequest request, StreamObserver<Empty> observer) {
        try {
            sharingHandler.shareEntityWithGroups(
                    request.getDomainId(),
                    request.getEntityId(),
                    request.getGroupListList(),
                    request.getPermissionTypeId(),
                    request.getCascadePermission());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void revokeEntitySharingFromGroups(
            RevokeEntitySharingFromGroupsRequest request, StreamObserver<Empty> observer) {
        try {
            sharingHandler.revokeEntitySharingFromGroups(
                    request.getDomainId(), request.getEntityId(),
                    request.getGroupListList(), request.getPermissionTypeId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    // ========================================================================
    // Proto <-> Thrift conversion helpers
    // ========================================================================

    private static Map<String, ResourcePermissionType> toResourcePermissionMap(Map<String, String> protoMap) {
        Map<String, ResourcePermissionType> result = new HashMap<>();
        for (Map.Entry<String, String> entry : protoMap.entrySet()) {
            result.put(entry.getKey(), ResourcePermissionType.valueOf(entry.getValue()));
        }
        return result;
    }

    // --- Domain ---

    private static org.apache.airavata.sharing.model.DomainEntity toThriftDomain(
            org.apache.airavata.sharing.registry.models.proto.Domain proto) {
        var t = new org.apache.airavata.sharing.model.DomainEntity();
        if (!proto.getDomainId().isEmpty()) t.setDomainId(proto.getDomainId());
        if (!proto.getName().isEmpty()) t.setName(proto.getName());
        if (!proto.getDescription().isEmpty()) t.setDescription(proto.getDescription());
        if (proto.getCreatedTime() != 0) t.setCreatedTime(proto.getCreatedTime());
        if (proto.getUpdatedTime() != 0) t.setUpdatedTime(proto.getUpdatedTime());
        if (!proto.getInitialUserGroupId().isEmpty()) t.setInitialUserGroupId(proto.getInitialUserGroupId());
        return t;
    }

    private static org.apache.airavata.sharing.registry.models.proto.Domain toProtoDomain(
            org.apache.airavata.sharing.model.DomainEntity t) {
        var b = org.apache.airavata.sharing.registry.models.proto.Domain.newBuilder();
        if (t.getDomainId() != null) b.setDomainId(t.getDomainId());
        if (t.getName() != null) b.setName(t.getName());
        if (t.getDescription() != null) b.setDescription(t.getDescription());
        b.setCreatedTime(t.getCreatedTime());
        b.setUpdatedTime(t.getUpdatedTime());
        if (t.getInitialUserGroupId() != null) b.setInitialUserGroupId(t.getInitialUserGroupId());
        return b.build();
    }

    // --- User ---

    private static org.apache.airavata.sharing.model.UserEntity toThriftUser(
            org.apache.airavata.sharing.registry.models.proto.User proto) {
        var t = new org.apache.airavata.sharing.model.UserEntity();
        if (!proto.getUserId().isEmpty()) t.setUserId(proto.getUserId());
        if (!proto.getDomainId().isEmpty()) t.setDomainId(proto.getDomainId());
        if (!proto.getUserName().isEmpty()) t.setUserName(proto.getUserName());
        if (proto.getCreatedTime() != 0) t.setCreatedTime(proto.getCreatedTime());
        if (proto.getUpdatedTime() != 0) t.setUpdatedTime(proto.getUpdatedTime());
        return t;
    }

    private static org.apache.airavata.sharing.registry.models.proto.User toProtoUser(
            org.apache.airavata.sharing.model.UserEntity t) {
        var b = org.apache.airavata.sharing.registry.models.proto.User.newBuilder();
        if (t.getUserId() != null) b.setUserId(t.getUserId());
        if (t.getDomainId() != null) b.setDomainId(t.getDomainId());
        if (t.getUserName() != null) b.setUserName(t.getUserName());
        b.setCreatedTime(t.getCreatedTime());
        b.setUpdatedTime(t.getUpdatedTime());
        return b.build();
    }

    // --- UserGroup ---

    private static org.apache.airavata.sharing.model.UserGroupEntity toThriftUserGroup(
            org.apache.airavata.sharing.registry.models.proto.UserGroup proto) {
        var t = new org.apache.airavata.sharing.model.UserGroupEntity();
        if (!proto.getGroupId().isEmpty()) t.setGroupId(proto.getGroupId());
        if (!proto.getDomainId().isEmpty()) t.setDomainId(proto.getDomainId());
        if (!proto.getName().isEmpty()) t.setName(proto.getName());
        if (!proto.getDescription().isEmpty()) t.setDescription(proto.getDescription());
        if (!proto.getOwnerId().isEmpty()) t.setOwnerId(proto.getOwnerId());
        if (proto.getGroupTypeValue() != 0) {
            t.setGroupType(
                    org.apache.airavata.sharing.registry.models.proto.GroupType.forNumber(proto.getGroupTypeValue())
                            .name());
        }
        if (proto.getGroupCardinalityValue() != 0) {
            t.setGroupCardinality(org.apache.airavata.sharing.registry.models.proto.GroupCardinality.forNumber(
                            proto.getGroupCardinalityValue())
                    .name());
        }
        if (proto.getCreatedTime() != 0) t.setCreatedTime(proto.getCreatedTime());
        if (proto.getUpdatedTime() != 0) t.setUpdatedTime(proto.getUpdatedTime());
        return t;
    }

    private static org.apache.airavata.sharing.registry.models.proto.UserGroup toProtoUserGroup(
            org.apache.airavata.sharing.model.UserGroupEntity t) {
        var b = org.apache.airavata.sharing.registry.models.proto.UserGroup.newBuilder();
        if (t.getGroupId() != null) b.setGroupId(t.getGroupId());
        if (t.getDomainId() != null) b.setDomainId(t.getDomainId());
        if (t.getName() != null) b.setName(t.getName());
        if (t.getDescription() != null) b.setDescription(t.getDescription());
        if (t.getOwnerId() != null) b.setOwnerId(t.getOwnerId());
        if (t.getGroupType() != null)
            b.setGroupTypeValue(org.apache.airavata.sharing.registry.models.proto.GroupType.valueOf(t.getGroupType())
                    .getNumber());
        if (t.getGroupCardinality() != null)
            b.setGroupCardinalityValue(
                    org.apache.airavata.sharing.registry.models.proto.GroupCardinality.valueOf(t.getGroupCardinality())
                            .getNumber());
        b.setCreatedTime(t.getCreatedTime());
        b.setUpdatedTime(t.getUpdatedTime());
        return b.build();
    }

    // --- EntityType ---

    private static org.apache.airavata.sharing.model.EntityTypeEntity toThriftEntityType(
            org.apache.airavata.sharing.registry.models.proto.EntityType proto) {
        var t = new org.apache.airavata.sharing.model.EntityTypeEntity();
        if (!proto.getEntityTypeId().isEmpty()) t.setEntityTypeId(proto.getEntityTypeId());
        if (!proto.getDomainId().isEmpty()) t.setDomainId(proto.getDomainId());
        if (!proto.getName().isEmpty()) t.setName(proto.getName());
        if (!proto.getDescription().isEmpty()) t.setDescription(proto.getDescription());
        if (proto.getCreatedTime() != 0) t.setCreatedTime(proto.getCreatedTime());
        if (proto.getUpdatedTime() != 0) t.setUpdatedTime(proto.getUpdatedTime());
        return t;
    }

    private static org.apache.airavata.sharing.registry.models.proto.EntityType toProtoEntityType(
            org.apache.airavata.sharing.model.EntityTypeEntity t) {
        var b = org.apache.airavata.sharing.registry.models.proto.EntityType.newBuilder();
        if (t.getEntityTypeId() != null) b.setEntityTypeId(t.getEntityTypeId());
        if (t.getDomainId() != null) b.setDomainId(t.getDomainId());
        if (t.getName() != null) b.setName(t.getName());
        if (t.getDescription() != null) b.setDescription(t.getDescription());
        b.setCreatedTime(t.getCreatedTime());
        b.setUpdatedTime(t.getUpdatedTime());
        return b.build();
    }

    // --- Entity ---

    private static org.apache.airavata.sharing.model.EntityEntity toThriftEntity(
            org.apache.airavata.sharing.registry.models.proto.Entity proto) {
        var t = new org.apache.airavata.sharing.model.EntityEntity();
        if (!proto.getEntityId().isEmpty()) t.setEntityId(proto.getEntityId());
        if (!proto.getDomainId().isEmpty()) t.setDomainId(proto.getDomainId());
        if (!proto.getEntityTypeId().isEmpty()) t.setEntityTypeId(proto.getEntityTypeId());
        if (!proto.getOwnerId().isEmpty()) t.setOwnerId(proto.getOwnerId());
        if (!proto.getParentEntityId().isEmpty()) t.setParentEntityId(proto.getParentEntityId());
        if (!proto.getName().isEmpty()) t.setName(proto.getName());
        if (!proto.getDescription().isEmpty()) t.setDescription(proto.getDescription());
        if (!proto.getBinaryData().isEmpty())
            t.setBinaryData(proto.getBinaryData().toByteArray());
        if (!proto.getFullText().isEmpty()) t.setFullText(proto.getFullText());
        if (proto.getSharedCount() != 0) t.setSharedCount(proto.getSharedCount());
        if (proto.getOriginalEntityCreationTime() != 0)
            t.setOriginalEntityCreationTime(proto.getOriginalEntityCreationTime());
        if (proto.getCreatedTime() != 0) t.setCreatedTime(proto.getCreatedTime());
        if (proto.getUpdatedTime() != 0) t.setUpdatedTime(proto.getUpdatedTime());
        return t;
    }

    private static org.apache.airavata.sharing.registry.models.proto.Entity toProtoEntity(
            org.apache.airavata.sharing.model.EntityEntity t) {
        var b = org.apache.airavata.sharing.registry.models.proto.Entity.newBuilder();
        if (t.getEntityId() != null) b.setEntityId(t.getEntityId());
        if (t.getDomainId() != null) b.setDomainId(t.getDomainId());
        if (t.getEntityTypeId() != null) b.setEntityTypeId(t.getEntityTypeId());
        if (t.getOwnerId() != null) b.setOwnerId(t.getOwnerId());
        if (t.getParentEntityId() != null) b.setParentEntityId(t.getParentEntityId());
        if (t.getName() != null) b.setName(t.getName());
        if (t.getDescription() != null) b.setDescription(t.getDescription());
        if (t.getBinaryData() != null) b.setBinaryData(ByteString.copyFrom(t.getBinaryData()));
        if (t.getFullText() != null) b.setFullText(t.getFullText());
        b.setSharedCount(t.getSharedCount());
        b.setOriginalEntityCreationTime(t.getOriginalEntityCreationTime());
        b.setCreatedTime(t.getCreatedTime());
        b.setUpdatedTime(t.getUpdatedTime());
        return b.build();
    }

    // --- PermissionType ---

    private static org.apache.airavata.sharing.model.PermissionTypeEntity toThriftPermissionType(
            org.apache.airavata.sharing.registry.models.proto.PermissionType proto) {
        var t = new org.apache.airavata.sharing.model.PermissionTypeEntity();
        if (!proto.getPermissionTypeId().isEmpty()) t.setPermissionTypeId(proto.getPermissionTypeId());
        if (!proto.getDomainId().isEmpty()) t.setDomainId(proto.getDomainId());
        if (!proto.getName().isEmpty()) t.setName(proto.getName());
        if (!proto.getDescription().isEmpty()) t.setDescription(proto.getDescription());
        if (proto.getCreatedTime() != 0) t.setCreatedTime(proto.getCreatedTime());
        if (proto.getUpdatedTime() != 0) t.setUpdatedTime(proto.getUpdatedTime());
        return t;
    }

    private static org.apache.airavata.sharing.registry.models.proto.PermissionType toProtoPermissionType(
            org.apache.airavata.sharing.model.PermissionTypeEntity t) {
        var b = org.apache.airavata.sharing.registry.models.proto.PermissionType.newBuilder();
        if (t.getPermissionTypeId() != null) b.setPermissionTypeId(t.getPermissionTypeId());
        if (t.getDomainId() != null) b.setDomainId(t.getDomainId());
        if (t.getName() != null) b.setName(t.getName());
        if (t.getDescription() != null) b.setDescription(t.getDescription());
        b.setCreatedTime(t.getCreatedTime());
        b.setUpdatedTime(t.getUpdatedTime());
        return b.build();
    }
}
