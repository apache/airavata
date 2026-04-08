import importlib

from airavata_sdk.transport.utils import (
    create_sharing_service_stub,
    create_group_manager_service_stub,
)


def _snake_to_camel(name):
    """Convert snake_case to camelCase."""
    parts = name.split('_')
    return parts[0] + ''.join(p.capitalize() for p in parts[1:])


class SharingClient:
    """Sharing registry (domains, users, groups, entities, permissions, resource sharing)
    and group manager operations."""

    def __init__(self, channel, metadata, gateway_id):
        self._metadata = metadata
        self._gateway_id = gateway_id
        self._sharing = create_sharing_service_stub(channel)
        self._group_mgr = create_group_manager_service_stub(channel)

    @staticmethod
    def _svc(name):
        return importlib.import_module(f"airavata_sdk.generated.services.{name}")

    def _pb2(self):
        from airavata_sdk.generated.services import sharing_service_pb2
        return sharing_service_pb2

    def _sharing_pb2(self):
        from airavata_sdk.generated.org.apache.airavata.model.sharing import sharing_pb2
        return sharing_pb2

    # ================================================================
    # Resource sharing RPCs
    # ================================================================

    def share_resource_with_users(self, resource_id, user_permissions):
        pb2 = self._pb2()
        return self._sharing.ShareResourceWithUsers(
            pb2.ShareResourceWithUsersRequest(resource_id=resource_id, user_permissions=user_permissions),
            metadata=self._metadata,
        )

    def share_resource_with_groups(self, resource_id, group_permissions):
        pb2 = self._pb2()
        return self._sharing.ShareResourceWithGroups(
            pb2.ShareResourceWithGroupsRequest(resource_id=resource_id, group_permissions=group_permissions),
            metadata=self._metadata,
        )

    def revoke_sharing_of_resource_from_users(self, resource_id, user_permissions):
        pb2 = self._pb2()
        return self._sharing.RevokeFromUsers(
            pb2.RevokeFromUsersRequest(resource_id=resource_id, user_permissions=user_permissions),
            metadata=self._metadata,
        )

    def revoke_sharing_of_resource_from_groups(self, resource_id, group_permissions):
        pb2 = self._pb2()
        return self._sharing.RevokeFromGroups(
            pb2.RevokeFromGroupsRequest(resource_id=resource_id, group_permissions=group_permissions),
            metadata=self._metadata,
        )

    def get_all_accessible_users(self, resource_id, permission_type):
        pb2 = self._pb2()
        return self._sharing.GetAllAccessibleUsers(
            pb2.GetAllAccessibleUsersRequest(resource_id=resource_id, permission_type=permission_type),
            metadata=self._metadata,
        )

    def get_all_directly_accessible_users(self, resource_id, permission_type):
        pb2 = self._pb2()
        return self._sharing.GetAllDirectlyAccessibleUsers(
            pb2.GetAllDirectlyAccessibleUsersRequest(resource_id=resource_id, permission_type=permission_type),
            metadata=self._metadata,
        )

    def get_all_accessible_groups(self, resource_id, permission_type):
        pb2 = self._pb2()
        return self._sharing.GetAllAccessibleGroups(
            pb2.GetAllAccessibleGroupsRequest(resource_id=resource_id, permission_type=permission_type),
            metadata=self._metadata,
        )

    def get_all_directly_accessible_groups(self, resource_id, permission_type):
        pb2 = self._pb2()
        return self._sharing.GetAllDirectlyAccessibleGroups(
            pb2.GetAllDirectlyAccessibleGroupsRequest(resource_id=resource_id, permission_type=permission_type),
            metadata=self._metadata,
        )

    def user_has_access(self, resource_id, user_id, permission_type):
        pb2 = self._pb2()
        return self._sharing.UserHasAccess(
            pb2.UserHasAccessRequest(resource_id=resource_id, user_id=user_id, permission_type=permission_type),
            metadata=self._metadata,
        )

    def revoke_from_users(self, resource_id, user_permissions):
        pb2 = self._pb2()
        return self._sharing.RevokeFromUsers(
            pb2.RevokeFromUsersRequest(resource_id=resource_id, user_permissions=user_permissions),
            metadata=self._metadata,
        )

    def revoke_from_groups(self, resource_id, group_permissions):
        pb2 = self._pb2()
        return self._sharing.RevokeFromGroups(
            pb2.RevokeFromGroupsRequest(resource_id=resource_id, group_permissions=group_permissions),
            metadata=self._metadata,
        )

    # ================================================================
    # Domain CRUD
    # ================================================================

    def create_domain(self, domain):
        pb2 = self._pb2()
        proto_domain = self._to_proto_domain(domain)
        resp = self._sharing.CreateDomain(
            pb2.CreateDomainRequest(domain=proto_domain),
            metadata=self._metadata,
        )
        return resp.domain_id

    def update_domain(self, domain):
        pb2 = self._pb2()
        proto_domain = self._to_proto_domain(domain)
        self._sharing.UpdateDomain(
            pb2.UpdateDomainRequest(domain=proto_domain),
            metadata=self._metadata,
        )
        return True

    def is_domain_exists(self, domain_id):
        pb2 = self._pb2()
        resp = self._sharing.IsDomainExists(
            pb2.IsDomainExistsRequest(domain_id=domain_id),
            metadata=self._metadata,
        )
        return resp.exists

    def delete_domain(self, domain_id):
        pb2 = self._pb2()
        self._sharing.DeleteDomain(
            pb2.DeleteDomainRequest(domain_id=domain_id),
            metadata=self._metadata,
        )
        return True

    def get_domain(self, domain_id):
        pb2 = self._pb2()
        return self._sharing.GetDomain(
            pb2.GetDomainRequest(domain_id=domain_id),
            metadata=self._metadata,
        )

    def get_domains(self, offset, limit):
        pb2 = self._pb2()
        resp = self._sharing.GetDomains(
            pb2.GetDomainsRequest(offset=offset, limit=limit),
            metadata=self._metadata,
        )
        return list(resp.domains)

    # ================================================================
    # User CRUD
    # ================================================================

    def create_user(self, user):
        pb2 = self._pb2()
        proto_user = self._to_proto_user(user)
        resp = self._sharing.CreateUser(
            pb2.CreateUserRequest(user=proto_user),
            metadata=self._metadata,
        )
        return resp.user_id

    def updated_user(self, user):
        pb2 = self._pb2()
        proto_user = self._to_proto_user(user)
        self._sharing.UpdateUser(
            pb2.UpdateUserRequest(user=proto_user),
            metadata=self._metadata,
        )
        return True

    def update_user(self, user):
        """Correctly-named alias for updated_user."""
        return self.updated_user(user)

    def is_user_exists(self, domain_id, user_id):
        pb2 = self._pb2()
        resp = self._sharing.IsUserExists(
            pb2.IsUserExistsRequest(domain_id=domain_id, user_id=user_id),
            metadata=self._metadata,
        )
        return resp.exists

    def delete_user(self, domain_id, user_id):
        pb2 = self._pb2()
        self._sharing.DeleteUser(
            pb2.DeleteUserRequest(domain_id=domain_id, user_id=user_id),
            metadata=self._metadata,
        )
        return True

    def get_user(self, domain_id, user_id):
        pb2 = self._pb2()
        return self._sharing.GetUser(
            pb2.GetUserRequest(domain_id=domain_id, user_id=user_id),
            metadata=self._metadata,
        )

    def get_users(self, domain_id, offset, limit):
        pb2 = self._pb2()
        resp = self._sharing.GetUsers(
            pb2.GetUsersRequest(domain_id=domain_id, offset=offset, limit=limit),
            metadata=self._metadata,
        )
        return list(resp.users)

    # ================================================================
    # Group CRUD (sharing registry)
    # ================================================================

    def create_group(self, group):
        pb2 = self._pb2()
        proto_group = self._to_proto_user_group(group)
        resp = self._sharing.CreateGroup(
            pb2.CreateGroupRequest(group=proto_group),
            metadata=self._metadata,
        )
        return resp.group_id

    def update_group(self, group):
        pb2 = self._pb2()
        proto_group = self._to_proto_user_group(group)
        self._sharing.UpdateGroup(
            pb2.UpdateGroupRequest(group=proto_group),
            metadata=self._metadata,
        )
        return True

    def is_group_exists(self, domain_id, group_id):
        pb2 = self._pb2()
        resp = self._sharing.IsGroupExists(
            pb2.IsGroupExistsRequest(domain_id=domain_id, group_id=group_id),
            metadata=self._metadata,
        )
        return resp.exists

    def delete_group(self, domain_id, group_id):
        pb2 = self._pb2()
        self._sharing.DeleteGroup(
            pb2.DeleteGroupRequest(domain_id=domain_id, group_id=group_id),
            metadata=self._metadata,
        )
        return True

    def get_group(self, domain_id, group_id):
        pb2 = self._pb2()
        return self._sharing.GetGroup(
            pb2.GetGroupRequest(domain_id=domain_id, group_id=group_id),
            metadata=self._metadata,
        )

    def get_groups(self, domain_id, offset, limit):
        pb2 = self._pb2()
        resp = self._sharing.GetGroups(
            pb2.GetGroupsRequest(domain_id=domain_id, offset=offset, limit=limit),
            metadata=self._metadata,
        )
        return list(resp.groups)

    # ================================================================
    # Group membership (sharing registry)
    # ================================================================

    def add_users_to_group(self, domain_id, user_ids, group_id):
        pb2 = self._pb2()
        self._sharing.AddUsersToGroup(
            pb2.AddUsersToGroupRequest(domain_id=domain_id, user_ids=user_ids, group_id=group_id),
            metadata=self._metadata,
        )
        return True

    def remove_users_from_group(self, domain_id, user_ids, group_id):
        pb2 = self._pb2()
        self._sharing.RemoveUsersFromGroup(
            pb2.RemoveUsersFromGroupRequest(domain_id=domain_id, user_ids=user_ids, group_id=group_id),
            metadata=self._metadata,
        )
        return True

    def transfer_group_ownership(self, domain_id, group_id, new_owner_id):
        pb2 = self._pb2()
        self._sharing.TransferGroupOwnership(
            pb2.TransferGroupOwnershipRequest(domain_id=domain_id, group_id=group_id, new_owner_id=new_owner_id),
            metadata=self._metadata,
        )
        return True

    def add_group_admins(self, domain_id, group_id, admin_ids):
        pb2 = self._pb2()
        self._sharing.AddGroupAdmins(
            pb2.AddGroupAdminsRequest(domain_id=domain_id, group_id=group_id, admin_ids=admin_ids),
            metadata=self._metadata,
        )
        return True

    def remove_group_admins(self, domain_id, group_id, admin_ids):
        pb2 = self._pb2()
        self._sharing.RemoveGroupAdmins(
            pb2.RemoveGroupAdminsRequest(domain_id=domain_id, group_id=group_id, admin_ids=admin_ids),
            metadata=self._metadata,
        )
        return True

    def has_admin_access(self, domain_id, group_id, admin_id):
        pb2 = self._pb2()
        resp = self._sharing.HasAdminAccess(
            pb2.HasAdminAccessRequest(domain_id=domain_id, group_id=group_id, admin_id=admin_id),
            metadata=self._metadata,
        )
        return resp.has_access

    def has_owner_access(self, domain_id, group_id, owner_id):
        pb2 = self._pb2()
        resp = self._sharing.HasOwnerAccess(
            pb2.HasOwnerAccessRequest(domain_id=domain_id, group_id=group_id, owner_id=owner_id),
            metadata=self._metadata,
        )
        return resp.has_access

    def get_group_members_of_type_user(self, domain_id, group_id, offset, limit):
        pb2 = self._pb2()
        resp = self._sharing.GetGroupMembersOfTypeUser(
            pb2.GetGroupMembersOfTypeUserRequest(domain_id=domain_id, group_id=group_id, offset=offset, limit=limit),
            metadata=self._metadata,
        )
        return list(resp.users)

    def get_group_members_of_type_group(self, domain_id, group_id, offset, limit):
        pb2 = self._pb2()
        resp = self._sharing.GetGroupMembersOfTypeGroup(
            pb2.GetGroupMembersOfTypeGroupRequest(domain_id=domain_id, group_id=group_id, offset=offset, limit=limit),
            metadata=self._metadata,
        )
        return list(resp.groups)

    def add_child_groups_to_parent_group(self, domain_id, child_ids, group_id):
        pb2 = self._pb2()
        self._sharing.AddChildGroupsToParentGroup(
            pb2.AddChildGroupsToParentGroupRequest(domain_id=domain_id, child_ids=child_ids, group_id=group_id),
            metadata=self._metadata,
        )
        return True

    def remove_child_group_from_parent_group(self, domain_id, child_id, group_id):
        pb2 = self._pb2()
        self._sharing.RemoveChildGroupFromParentGroup(
            pb2.RemoveChildGroupFromParentGroupRequest(domain_id=domain_id, child_id=child_id, group_id=group_id),
            metadata=self._metadata,
        )
        return True

    def get_all_member_groups_for_user(self, domain_id, user_id):
        pb2 = self._pb2()
        resp = self._sharing.GetAllMemberGroupsForUser(
            pb2.GetAllMemberGroupsForUserRequest(domain_id=domain_id, user_id=user_id),
            metadata=self._metadata,
        )
        return list(resp.groups)

    # ================================================================
    # Entity type CRUD
    # ================================================================

    def create_entity_type(self, entity_type):
        pb2 = self._pb2()
        proto_et = self._to_proto_entity_type(entity_type)
        resp = self._sharing.CreateEntityType(
            pb2.CreateEntityTypeRequest(entity_type=proto_et),
            metadata=self._metadata,
        )
        return resp.entity_type_id

    def update_entity_type(self, entity_type):
        pb2 = self._pb2()
        proto_et = self._to_proto_entity_type(entity_type)
        self._sharing.UpdateEntityType(
            pb2.UpdateEntityTypeRequest(entity_type=proto_et),
            metadata=self._metadata,
        )
        return True

    def is_entity_type_exists(self, domain_id, entity_type_id):
        pb2 = self._pb2()
        resp = self._sharing.IsEntityTypeExists(
            pb2.IsEntityTypeExistsRequest(domain_id=domain_id, entity_type_id=entity_type_id),
            metadata=self._metadata,
        )
        return resp.exists

    def delete_entity_type(self, domain_id, entity_type_id):
        pb2 = self._pb2()
        self._sharing.DeleteEntityType(
            pb2.DeleteEntityTypeRequest(domain_id=domain_id, entity_type_id=entity_type_id),
            metadata=self._metadata,
        )
        return True

    def get_entity_type(self, domain_id, entity_type_id):
        pb2 = self._pb2()
        return self._sharing.GetEntityType(
            pb2.GetEntityTypeRequest(domain_id=domain_id, entity_type_id=entity_type_id),
            metadata=self._metadata,
        )

    def get_entity_types(self, domain_id, offset, limit):
        pb2 = self._pb2()
        resp = self._sharing.GetEntityTypes(
            pb2.GetEntityTypesRequest(domain_id=domain_id, offset=offset, limit=limit),
            metadata=self._metadata,
        )
        return list(resp.entity_types)

    # ================================================================
    # Entity CRUD
    # ================================================================

    def create_entity(self, entity):
        pb2 = self._pb2()
        proto_entity = self._to_proto_entity(entity)
        resp = self._sharing.CreateEntity(
            pb2.CreateEntityRequest(entity=proto_entity),
            metadata=self._metadata,
        )
        return resp.entity_id

    def update_entity(self, entity):
        pb2 = self._pb2()
        proto_entity = self._to_proto_entity(entity)
        self._sharing.UpdateEntity(
            pb2.UpdateEntityRequest(entity=proto_entity),
            metadata=self._metadata,
        )
        return True

    def is_entity_exists(self, domain_id, entity_id):
        pb2 = self._pb2()
        resp = self._sharing.IsEntityExists(
            pb2.IsEntityExistsRequest(domain_id=domain_id, entity_id=entity_id),
            metadata=self._metadata,
        )
        return resp.exists

    def delete_entity(self, domain_id, entity_id):
        pb2 = self._pb2()
        self._sharing.DeleteEntity(
            pb2.DeleteEntityRequest(domain_id=domain_id, entity_id=entity_id),
            metadata=self._metadata,
        )
        return True

    def get_entity(self, domain_id, entity_id):
        pb2 = self._pb2()
        return self._sharing.GetEntity(
            pb2.GetEntityRequest(domain_id=domain_id, entity_id=entity_id),
            metadata=self._metadata,
        )

    def search_entities(self, domain_id, user_id, filters, offset, limit):
        pb2 = self._pb2()
        proto_filters = [self._to_proto_search_criteria(f) for f in filters]
        resp = self._sharing.SearchEntities(
            pb2.SearchEntitiesRequest(
                domain_id=domain_id, user_id=user_id,
                filters=proto_filters, offset=offset, limit=limit,
            ),
            metadata=self._metadata,
        )
        return list(resp.entities)

    def get_list_of_shared_users(self, domain_id, entity_id, permission_type_id):
        pb2 = self._pb2()
        resp = self._sharing.GetListOfSharedUsers(
            pb2.GetListOfSharedUsersRequest(
                domain_id=domain_id, entity_id=entity_id, permission_type_id=permission_type_id,
            ),
            metadata=self._metadata,
        )
        return list(resp.users)

    def get_list_of_directly_shared_users(self, domain_id, entity_id, permission_type_id):
        pb2 = self._pb2()
        resp = self._sharing.GetListOfDirectlySharedUsers(
            pb2.GetListOfDirectlySharedUsersRequest(
                domain_id=domain_id, entity_id=entity_id, permission_type_id=permission_type_id,
            ),
            metadata=self._metadata,
        )
        return list(resp.users)

    def get_list_of_shared_groups(self, domain_id, entity_id, permission_type_id):
        pb2 = self._pb2()
        resp = self._sharing.GetListOfSharedGroups(
            pb2.GetListOfSharedGroupsRequest(
                domain_id=domain_id, entity_id=entity_id, permission_type_id=permission_type_id,
            ),
            metadata=self._metadata,
        )
        return list(resp.groups)

    def get_list_of_directly_shared_groups(self, domain_id, entity_id, permission_type_id):
        pb2 = self._pb2()
        resp = self._sharing.GetListOfDirectlySharedGroups(
            pb2.GetListOfDirectlySharedGroupsRequest(
                domain_id=domain_id, entity_id=entity_id, permission_type_id=permission_type_id,
            ),
            metadata=self._metadata,
        )
        return list(resp.groups)

    # ================================================================
    # Permission type CRUD
    # ================================================================

    def create_permission_type(self, permission_type):
        pb2 = self._pb2()
        proto_pt = self._to_proto_permission_type(permission_type)
        resp = self._sharing.CreatePermissionType(
            pb2.CreatePermissionTypeRequest(permission_type=proto_pt),
            metadata=self._metadata,
        )
        return resp.permission_type_id

    def update_permission_type(self, permission_type):
        pb2 = self._pb2()
        proto_pt = self._to_proto_permission_type(permission_type)
        self._sharing.UpdatePermissionType(
            pb2.UpdatePermissionTypeRequest(permission_type=proto_pt),
            metadata=self._metadata,
        )
        return True

    def is_permission_exists(self, domain_id, permission_id):
        pb2 = self._pb2()
        resp = self._sharing.IsPermissionExists(
            pb2.IsPermissionExistsRequest(domain_id=domain_id, permission_id=permission_id),
            metadata=self._metadata,
        )
        return resp.exists

    def delete_permission_type(self, domain_id, permission_type_id):
        pb2 = self._pb2()
        self._sharing.DeletePermissionType(
            pb2.DeletePermissionTypeRequest(domain_id=domain_id, permission_type_id=permission_type_id),
            metadata=self._metadata,
        )
        return True

    def get_permission_type(self, domain_id, permission_type_id):
        pb2 = self._pb2()
        return self._sharing.GetPermissionType(
            pb2.GetPermissionTypeRequest(domain_id=domain_id, permission_type_id=permission_type_id),
            metadata=self._metadata,
        )

    def get_permission_types(self, domain_id, offset, limit):
        pb2 = self._pb2()
        resp = self._sharing.GetPermissionTypes(
            pb2.GetPermissionTypesRequest(domain_id=domain_id, offset=offset, limit=limit),
            metadata=self._metadata,
        )
        return list(resp.permission_types)

    # ================================================================
    # Entity sharing (Thrift-compatible)
    # ================================================================

    def share_entity_with_users(self, domain_id, entity_id, user_list, permission_type_id, cascade_permission=True):
        pb2 = self._pb2()
        self._sharing.ShareEntityWithUsers(
            pb2.ShareEntityWithUsersRequest(
                domain_id=domain_id, entity_id=entity_id,
                user_list=user_list, permission_type_id=permission_type_id,
                cascade_permission=cascade_permission,
            ),
            metadata=self._metadata,
        )
        return True

    def revoke_entity_sharing_from_users(self, domain_id, entity_id, user_list, permission_type_id):
        pb2 = self._pb2()
        self._sharing.RevokeEntitySharingFromUsers(
            pb2.RevokeEntitySharingFromUsersRequest(
                domain_id=domain_id, entity_id=entity_id,
                user_list=user_list, permission_type_id=permission_type_id,
            ),
            metadata=self._metadata,
        )
        return True

    def share_entity_with_groups(self, domain_id, entity_id, group_list, permission_type_id, cascade_permission=True):
        pb2 = self._pb2()
        self._sharing.ShareEntityWithGroups(
            pb2.ShareEntityWithGroupsRequest(
                domain_id=domain_id, entity_id=entity_id,
                group_list=group_list, permission_type_id=permission_type_id,
                cascade_permission=cascade_permission,
            ),
            metadata=self._metadata,
        )
        return True

    def revoke_entity_sharing_from_groups(self, domain_id, entity_id, group_list, permission_type_id):
        pb2 = self._pb2()
        self._sharing.RevokeEntitySharingFromGroups(
            pb2.RevokeEntitySharingFromGroupsRequest(
                domain_id=domain_id, entity_id=entity_id,
                group_list=group_list, permission_type_id=permission_type_id,
            ),
            metadata=self._metadata,
        )
        return True

    # ================================================================
    # Group Manager Service (prefixed with gm_ where names collide)
    # ================================================================

    def gm_create_group(self, group):
        from airavata_sdk.generated.services import group_manager_service_pb2 as pb2
        resp = self._group_mgr.CreateGroup(
            pb2.CreateGroupRequest(group=group),
            metadata=self._metadata,
        )
        return resp.group_id

    def gm_update_group(self, group):
        from airavata_sdk.generated.services import group_manager_service_pb2 as pb2
        self._group_mgr.UpdateGroup(
            pb2.UpdateGroupRequest(group=group),
            metadata=self._metadata,
        )

    def gm_delete_group(self, group_id, owner_id):
        from airavata_sdk.generated.services import group_manager_service_pb2 as pb2
        self._group_mgr.DeleteGroup(
            pb2.DeleteGroupRequest(group_id=group_id, owner_id=owner_id),
            metadata=self._metadata,
        )

    def gm_get_group(self, group_id):
        from airavata_sdk.generated.services import group_manager_service_pb2 as pb2
        return self._group_mgr.GetGroup(
            pb2.GetGroupRequest(group_id=group_id),
            metadata=self._metadata,
        )

    def gm_get_groups(self):
        from airavata_sdk.generated.services import group_manager_service_pb2 as pb2
        resp = self._group_mgr.GetGroups(
            pb2.GetGroupsRequest(),
            metadata=self._metadata,
        )
        return list(resp.groups)

    def gm_get_all_groups_user_belongs(self, user_name):
        from airavata_sdk.generated.services import group_manager_service_pb2 as pb2
        resp = self._group_mgr.GetAllGroupsUserBelongs(
            pb2.GetAllGroupsUserBelongsRequest(user_name=user_name),
            metadata=self._metadata,
        )
        return list(resp.groups)

    def gm_add_users_to_group(self, user_ids, group_id):
        from airavata_sdk.generated.services import group_manager_service_pb2 as pb2
        self._group_mgr.AddUsersToGroup(
            pb2.AddUsersToGroupRequest(group_id=group_id, user_ids=user_ids),
            metadata=self._metadata,
        )

    def gm_remove_users_from_group(self, user_ids, group_id):
        from airavata_sdk.generated.services import group_manager_service_pb2 as pb2
        self._group_mgr.RemoveUsersFromGroup(
            pb2.RemoveUsersFromGroupRequest(group_id=group_id, user_ids=user_ids),
            metadata=self._metadata,
        )

    def gm_transfer_group_ownership(self, group_id, new_owner_id):
        from airavata_sdk.generated.services import group_manager_service_pb2 as pb2
        self._group_mgr.TransferGroupOwnership(
            pb2.TransferGroupOwnershipRequest(group_id=group_id, new_owner_id=new_owner_id),
            metadata=self._metadata,
        )

    def gm_add_group_admins(self, group_id, admin_ids):
        from airavata_sdk.generated.services import group_manager_service_pb2 as pb2
        self._group_mgr.AddGroupAdmins(
            pb2.AddGroupAdminsRequest(group_id=group_id, admin_ids=admin_ids),
            metadata=self._metadata,
        )

    def gm_remove_group_admins(self, group_id, admin_ids):
        from airavata_sdk.generated.services import group_manager_service_pb2 as pb2
        self._group_mgr.RemoveGroupAdmins(
            pb2.RemoveGroupAdminsRequest(group_id=group_id, admin_ids=admin_ids),
            metadata=self._metadata,
        )

    def gm_has_admin_access(self, group_id, admin_id):
        from airavata_sdk.generated.services import group_manager_service_pb2 as pb2
        resp = self._group_mgr.HasAdminAccess(
            pb2.HasAdminAccessRequest(group_id=group_id, admin_id=admin_id),
            metadata=self._metadata,
        )
        return resp.has_access

    def gm_has_owner_access(self, group_id, owner_id):
        from airavata_sdk.generated.services import group_manager_service_pb2 as pb2
        resp = self._group_mgr.HasOwnerAccess(
            pb2.HasOwnerAccessRequest(group_id=group_id, owner_id=owner_id),
            metadata=self._metadata,
        )
        return resp.has_access

    # ================================================================
    # Proto model builders
    # ================================================================

    def _to_proto_domain(self, domain):
        spb = self._sharing_pb2()
        if isinstance(domain, spb.Domain):
            return domain
        kwargs = self._extract_fields(domain, [
            'domain_id', 'name', 'description', 'created_time',
            'updated_time', 'initial_user_group_id',
        ])
        return spb.Domain(**kwargs)

    def _to_proto_user(self, user):
        spb = self._sharing_pb2()
        if isinstance(user, spb.User):
            return user
        kwargs = self._extract_fields(user, [
            'user_id', 'domain_id', 'user_name', 'first_name',
            'last_name', 'email', 'icon', 'created_time', 'updated_time',
        ])
        return spb.User(**kwargs)

    def _to_proto_user_group(self, group):
        spb = self._sharing_pb2()
        if isinstance(group, spb.UserGroup):
            return group
        kwargs = self._extract_fields(group, [
            'group_id', 'domain_id', 'name', 'description', 'owner_id',
            'group_type', 'group_cardinality', 'created_time', 'updated_time',
        ])
        return spb.UserGroup(**kwargs)

    def _to_proto_entity_type(self, entity_type):
        spb = self._sharing_pb2()
        if isinstance(entity_type, spb.EntityType):
            return entity_type
        kwargs = self._extract_fields(entity_type, [
            'entity_type_id', 'domain_id', 'name', 'description',
            'created_time', 'updated_time',
        ])
        return spb.EntityType(**kwargs)

    def _to_proto_entity(self, entity):
        spb = self._sharing_pb2()
        if isinstance(entity, spb.Entity):
            return entity
        kwargs = self._extract_fields(entity, [
            'entity_id', 'domain_id', 'entity_type_id', 'owner_id',
            'parent_entity_id', 'name', 'description', 'binary_data',
            'full_text', 'shared_count', 'original_entity_creation_time',
            'created_time', 'updated_time',
        ])
        return spb.Entity(**kwargs)

    def _to_proto_permission_type(self, permission_type):
        spb = self._sharing_pb2()
        if isinstance(permission_type, spb.PermissionType):
            return permission_type
        kwargs = self._extract_fields(permission_type, [
            'permission_type_id', 'domain_id', 'name', 'description',
            'created_time', 'updated_time',
        ])
        return spb.PermissionType(**kwargs)

    def _to_proto_search_criteria(self, criteria):
        spb = self._sharing_pb2()
        if isinstance(criteria, spb.SearchCriteria):
            return criteria
        kwargs = self._extract_fields(criteria, [
            'search_field', 'value', 'search_condition',
        ])
        return spb.SearchCriteria(**kwargs)

    @staticmethod
    def _extract_fields(obj, field_names):
        """Extract fields from a dict or Thrift-like object."""
        kwargs = {}
        for name in field_names:
            val = None
            if isinstance(obj, dict):
                val = obj.get(name)
                if val is None:
                    camel = _snake_to_camel(name)
                    val = obj.get(camel)
            else:
                val = getattr(obj, name, None)
                if val is None:
                    camel = _snake_to_camel(name)
                    val = getattr(obj, camel, None)
            if val is not None:
                kwargs[name] = val
        return kwargs
