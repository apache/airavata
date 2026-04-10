from google.api import annotations_pb2 as _annotations_pb2
from google.protobuf import empty_pb2 as _empty_pb2
from org.apache.airavata.model.sharing import sharing_pb2 as _sharing_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class ShareResourceWithUsersRequest(_message.Message):
    __slots__ = ("resource_id", "user_permissions")
    class UserPermissionsEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: str
        def __init__(self, key: _Optional[str] = ..., value: _Optional[str] = ...) -> None: ...
    RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    USER_PERMISSIONS_FIELD_NUMBER: _ClassVar[int]
    resource_id: str
    user_permissions: _containers.ScalarMap[str, str]
    def __init__(self, resource_id: _Optional[str] = ..., user_permissions: _Optional[_Mapping[str, str]] = ...) -> None: ...

class ShareResourceWithGroupsRequest(_message.Message):
    __slots__ = ("resource_id", "group_permissions")
    class GroupPermissionsEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: str
        def __init__(self, key: _Optional[str] = ..., value: _Optional[str] = ...) -> None: ...
    RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    GROUP_PERMISSIONS_FIELD_NUMBER: _ClassVar[int]
    resource_id: str
    group_permissions: _containers.ScalarMap[str, str]
    def __init__(self, resource_id: _Optional[str] = ..., group_permissions: _Optional[_Mapping[str, str]] = ...) -> None: ...

class RevokeFromUsersRequest(_message.Message):
    __slots__ = ("resource_id", "user_permissions")
    class UserPermissionsEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: str
        def __init__(self, key: _Optional[str] = ..., value: _Optional[str] = ...) -> None: ...
    RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    USER_PERMISSIONS_FIELD_NUMBER: _ClassVar[int]
    resource_id: str
    user_permissions: _containers.ScalarMap[str, str]
    def __init__(self, resource_id: _Optional[str] = ..., user_permissions: _Optional[_Mapping[str, str]] = ...) -> None: ...

class RevokeFromGroupsRequest(_message.Message):
    __slots__ = ("resource_id", "group_permissions")
    class GroupPermissionsEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: str
        def __init__(self, key: _Optional[str] = ..., value: _Optional[str] = ...) -> None: ...
    RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    GROUP_PERMISSIONS_FIELD_NUMBER: _ClassVar[int]
    resource_id: str
    group_permissions: _containers.ScalarMap[str, str]
    def __init__(self, resource_id: _Optional[str] = ..., group_permissions: _Optional[_Mapping[str, str]] = ...) -> None: ...

class GetAllAccessibleUsersRequest(_message.Message):
    __slots__ = ("resource_id", "permission_type")
    RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    PERMISSION_TYPE_FIELD_NUMBER: _ClassVar[int]
    resource_id: str
    permission_type: str
    def __init__(self, resource_id: _Optional[str] = ..., permission_type: _Optional[str] = ...) -> None: ...

class GetAllAccessibleUsersResponse(_message.Message):
    __slots__ = ("user_ids",)
    USER_IDS_FIELD_NUMBER: _ClassVar[int]
    user_ids: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, user_ids: _Optional[_Iterable[str]] = ...) -> None: ...

class GetAllDirectlyAccessibleUsersRequest(_message.Message):
    __slots__ = ("resource_id", "permission_type")
    RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    PERMISSION_TYPE_FIELD_NUMBER: _ClassVar[int]
    resource_id: str
    permission_type: str
    def __init__(self, resource_id: _Optional[str] = ..., permission_type: _Optional[str] = ...) -> None: ...

class GetAllDirectlyAccessibleUsersResponse(_message.Message):
    __slots__ = ("user_ids",)
    USER_IDS_FIELD_NUMBER: _ClassVar[int]
    user_ids: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, user_ids: _Optional[_Iterable[str]] = ...) -> None: ...

class GetAllAccessibleGroupsRequest(_message.Message):
    __slots__ = ("resource_id", "permission_type")
    RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    PERMISSION_TYPE_FIELD_NUMBER: _ClassVar[int]
    resource_id: str
    permission_type: str
    def __init__(self, resource_id: _Optional[str] = ..., permission_type: _Optional[str] = ...) -> None: ...

class GetAllAccessibleGroupsResponse(_message.Message):
    __slots__ = ("group_ids",)
    GROUP_IDS_FIELD_NUMBER: _ClassVar[int]
    group_ids: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, group_ids: _Optional[_Iterable[str]] = ...) -> None: ...

class GetAllDirectlyAccessibleGroupsRequest(_message.Message):
    __slots__ = ("resource_id", "permission_type")
    RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    PERMISSION_TYPE_FIELD_NUMBER: _ClassVar[int]
    resource_id: str
    permission_type: str
    def __init__(self, resource_id: _Optional[str] = ..., permission_type: _Optional[str] = ...) -> None: ...

class GetAllDirectlyAccessibleGroupsResponse(_message.Message):
    __slots__ = ("group_ids",)
    GROUP_IDS_FIELD_NUMBER: _ClassVar[int]
    group_ids: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, group_ids: _Optional[_Iterable[str]] = ...) -> None: ...

class UserHasAccessRequest(_message.Message):
    __slots__ = ("resource_id", "user_id", "permission_type")
    RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    PERMISSION_TYPE_FIELD_NUMBER: _ClassVar[int]
    resource_id: str
    user_id: str
    permission_type: str
    def __init__(self, resource_id: _Optional[str] = ..., user_id: _Optional[str] = ..., permission_type: _Optional[str] = ...) -> None: ...

class UserHasAccessResponse(_message.Message):
    __slots__ = ("has_access",)
    HAS_ACCESS_FIELD_NUMBER: _ClassVar[int]
    has_access: bool
    def __init__(self, has_access: bool = ...) -> None: ...

class CreateDomainRequest(_message.Message):
    __slots__ = ("domain",)
    DOMAIN_FIELD_NUMBER: _ClassVar[int]
    domain: _sharing_pb2.Domain
    def __init__(self, domain: _Optional[_Union[_sharing_pb2.Domain, _Mapping]] = ...) -> None: ...

class CreateDomainResponse(_message.Message):
    __slots__ = ("domain_id",)
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    def __init__(self, domain_id: _Optional[str] = ...) -> None: ...

class UpdateDomainRequest(_message.Message):
    __slots__ = ("domain",)
    DOMAIN_FIELD_NUMBER: _ClassVar[int]
    domain: _sharing_pb2.Domain
    def __init__(self, domain: _Optional[_Union[_sharing_pb2.Domain, _Mapping]] = ...) -> None: ...

class IsDomainExistsRequest(_message.Message):
    __slots__ = ("domain_id",)
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    def __init__(self, domain_id: _Optional[str] = ...) -> None: ...

class IsDomainExistsResponse(_message.Message):
    __slots__ = ("exists",)
    EXISTS_FIELD_NUMBER: _ClassVar[int]
    exists: bool
    def __init__(self, exists: bool = ...) -> None: ...

class DeleteDomainRequest(_message.Message):
    __slots__ = ("domain_id",)
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    def __init__(self, domain_id: _Optional[str] = ...) -> None: ...

class GetDomainRequest(_message.Message):
    __slots__ = ("domain_id",)
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    def __init__(self, domain_id: _Optional[str] = ...) -> None: ...

class GetDomainsRequest(_message.Message):
    __slots__ = ("offset", "limit")
    OFFSET_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    offset: int
    limit: int
    def __init__(self, offset: _Optional[int] = ..., limit: _Optional[int] = ...) -> None: ...

class GetDomainsResponse(_message.Message):
    __slots__ = ("domains",)
    DOMAINS_FIELD_NUMBER: _ClassVar[int]
    domains: _containers.RepeatedCompositeFieldContainer[_sharing_pb2.Domain]
    def __init__(self, domains: _Optional[_Iterable[_Union[_sharing_pb2.Domain, _Mapping]]] = ...) -> None: ...

class CreateUserRequest(_message.Message):
    __slots__ = ("user",)
    USER_FIELD_NUMBER: _ClassVar[int]
    user: _sharing_pb2.User
    def __init__(self, user: _Optional[_Union[_sharing_pb2.User, _Mapping]] = ...) -> None: ...

class CreateUserResponse(_message.Message):
    __slots__ = ("user_id",)
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    user_id: str
    def __init__(self, user_id: _Optional[str] = ...) -> None: ...

class UpdateUserRequest(_message.Message):
    __slots__ = ("user",)
    USER_FIELD_NUMBER: _ClassVar[int]
    user: _sharing_pb2.User
    def __init__(self, user: _Optional[_Union[_sharing_pb2.User, _Mapping]] = ...) -> None: ...

class IsUserExistsRequest(_message.Message):
    __slots__ = ("domain_id", "user_id")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    user_id: str
    def __init__(self, domain_id: _Optional[str] = ..., user_id: _Optional[str] = ...) -> None: ...

class IsUserExistsResponse(_message.Message):
    __slots__ = ("exists",)
    EXISTS_FIELD_NUMBER: _ClassVar[int]
    exists: bool
    def __init__(self, exists: bool = ...) -> None: ...

class DeleteUserRequest(_message.Message):
    __slots__ = ("domain_id", "user_id")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    user_id: str
    def __init__(self, domain_id: _Optional[str] = ..., user_id: _Optional[str] = ...) -> None: ...

class GetUserRequest(_message.Message):
    __slots__ = ("domain_id", "user_id")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    user_id: str
    def __init__(self, domain_id: _Optional[str] = ..., user_id: _Optional[str] = ...) -> None: ...

class GetUsersRequest(_message.Message):
    __slots__ = ("domain_id", "offset", "limit")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    OFFSET_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    offset: int
    limit: int
    def __init__(self, domain_id: _Optional[str] = ..., offset: _Optional[int] = ..., limit: _Optional[int] = ...) -> None: ...

class GetUsersResponse(_message.Message):
    __slots__ = ("users",)
    USERS_FIELD_NUMBER: _ClassVar[int]
    users: _containers.RepeatedCompositeFieldContainer[_sharing_pb2.User]
    def __init__(self, users: _Optional[_Iterable[_Union[_sharing_pb2.User, _Mapping]]] = ...) -> None: ...

class CreateGroupRequest(_message.Message):
    __slots__ = ("group",)
    GROUP_FIELD_NUMBER: _ClassVar[int]
    group: _sharing_pb2.UserGroup
    def __init__(self, group: _Optional[_Union[_sharing_pb2.UserGroup, _Mapping]] = ...) -> None: ...

class CreateGroupResponse(_message.Message):
    __slots__ = ("group_id",)
    GROUP_ID_FIELD_NUMBER: _ClassVar[int]
    group_id: str
    def __init__(self, group_id: _Optional[str] = ...) -> None: ...

class UpdateGroupRequest(_message.Message):
    __slots__ = ("group",)
    GROUP_FIELD_NUMBER: _ClassVar[int]
    group: _sharing_pb2.UserGroup
    def __init__(self, group: _Optional[_Union[_sharing_pb2.UserGroup, _Mapping]] = ...) -> None: ...

class IsGroupExistsRequest(_message.Message):
    __slots__ = ("domain_id", "group_id")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    GROUP_ID_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    group_id: str
    def __init__(self, domain_id: _Optional[str] = ..., group_id: _Optional[str] = ...) -> None: ...

class IsGroupExistsResponse(_message.Message):
    __slots__ = ("exists",)
    EXISTS_FIELD_NUMBER: _ClassVar[int]
    exists: bool
    def __init__(self, exists: bool = ...) -> None: ...

class DeleteGroupRequest(_message.Message):
    __slots__ = ("domain_id", "group_id")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    GROUP_ID_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    group_id: str
    def __init__(self, domain_id: _Optional[str] = ..., group_id: _Optional[str] = ...) -> None: ...

class GetGroupRequest(_message.Message):
    __slots__ = ("domain_id", "group_id")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    GROUP_ID_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    group_id: str
    def __init__(self, domain_id: _Optional[str] = ..., group_id: _Optional[str] = ...) -> None: ...

class GetGroupsRequest(_message.Message):
    __slots__ = ("domain_id", "offset", "limit")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    OFFSET_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    offset: int
    limit: int
    def __init__(self, domain_id: _Optional[str] = ..., offset: _Optional[int] = ..., limit: _Optional[int] = ...) -> None: ...

class GetGroupsResponse(_message.Message):
    __slots__ = ("groups",)
    GROUPS_FIELD_NUMBER: _ClassVar[int]
    groups: _containers.RepeatedCompositeFieldContainer[_sharing_pb2.UserGroup]
    def __init__(self, groups: _Optional[_Iterable[_Union[_sharing_pb2.UserGroup, _Mapping]]] = ...) -> None: ...

class AddUsersToGroupRequest(_message.Message):
    __slots__ = ("domain_id", "user_ids", "group_id")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    USER_IDS_FIELD_NUMBER: _ClassVar[int]
    GROUP_ID_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    user_ids: _containers.RepeatedScalarFieldContainer[str]
    group_id: str
    def __init__(self, domain_id: _Optional[str] = ..., user_ids: _Optional[_Iterable[str]] = ..., group_id: _Optional[str] = ...) -> None: ...

class RemoveUsersFromGroupRequest(_message.Message):
    __slots__ = ("domain_id", "user_ids", "group_id")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    USER_IDS_FIELD_NUMBER: _ClassVar[int]
    GROUP_ID_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    user_ids: _containers.RepeatedScalarFieldContainer[str]
    group_id: str
    def __init__(self, domain_id: _Optional[str] = ..., user_ids: _Optional[_Iterable[str]] = ..., group_id: _Optional[str] = ...) -> None: ...

class TransferGroupOwnershipRequest(_message.Message):
    __slots__ = ("domain_id", "group_id", "new_owner_id")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    GROUP_ID_FIELD_NUMBER: _ClassVar[int]
    NEW_OWNER_ID_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    group_id: str
    new_owner_id: str
    def __init__(self, domain_id: _Optional[str] = ..., group_id: _Optional[str] = ..., new_owner_id: _Optional[str] = ...) -> None: ...

class AddGroupAdminsRequest(_message.Message):
    __slots__ = ("domain_id", "group_id", "admin_ids")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    GROUP_ID_FIELD_NUMBER: _ClassVar[int]
    ADMIN_IDS_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    group_id: str
    admin_ids: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, domain_id: _Optional[str] = ..., group_id: _Optional[str] = ..., admin_ids: _Optional[_Iterable[str]] = ...) -> None: ...

class RemoveGroupAdminsRequest(_message.Message):
    __slots__ = ("domain_id", "group_id", "admin_ids")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    GROUP_ID_FIELD_NUMBER: _ClassVar[int]
    ADMIN_IDS_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    group_id: str
    admin_ids: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, domain_id: _Optional[str] = ..., group_id: _Optional[str] = ..., admin_ids: _Optional[_Iterable[str]] = ...) -> None: ...

class HasAdminAccessRequest(_message.Message):
    __slots__ = ("domain_id", "group_id", "admin_id")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    GROUP_ID_FIELD_NUMBER: _ClassVar[int]
    ADMIN_ID_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    group_id: str
    admin_id: str
    def __init__(self, domain_id: _Optional[str] = ..., group_id: _Optional[str] = ..., admin_id: _Optional[str] = ...) -> None: ...

class HasAdminAccessResponse(_message.Message):
    __slots__ = ("has_access",)
    HAS_ACCESS_FIELD_NUMBER: _ClassVar[int]
    has_access: bool
    def __init__(self, has_access: bool = ...) -> None: ...

class HasOwnerAccessRequest(_message.Message):
    __slots__ = ("domain_id", "group_id", "owner_id")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    GROUP_ID_FIELD_NUMBER: _ClassVar[int]
    OWNER_ID_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    group_id: str
    owner_id: str
    def __init__(self, domain_id: _Optional[str] = ..., group_id: _Optional[str] = ..., owner_id: _Optional[str] = ...) -> None: ...

class HasOwnerAccessResponse(_message.Message):
    __slots__ = ("has_access",)
    HAS_ACCESS_FIELD_NUMBER: _ClassVar[int]
    has_access: bool
    def __init__(self, has_access: bool = ...) -> None: ...

class GetGroupMembersOfTypeUserRequest(_message.Message):
    __slots__ = ("domain_id", "group_id", "offset", "limit")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    GROUP_ID_FIELD_NUMBER: _ClassVar[int]
    OFFSET_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    group_id: str
    offset: int
    limit: int
    def __init__(self, domain_id: _Optional[str] = ..., group_id: _Optional[str] = ..., offset: _Optional[int] = ..., limit: _Optional[int] = ...) -> None: ...

class GetGroupMembersOfTypeUserResponse(_message.Message):
    __slots__ = ("users",)
    USERS_FIELD_NUMBER: _ClassVar[int]
    users: _containers.RepeatedCompositeFieldContainer[_sharing_pb2.User]
    def __init__(self, users: _Optional[_Iterable[_Union[_sharing_pb2.User, _Mapping]]] = ...) -> None: ...

class GetGroupMembersOfTypeGroupRequest(_message.Message):
    __slots__ = ("domain_id", "group_id", "offset", "limit")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    GROUP_ID_FIELD_NUMBER: _ClassVar[int]
    OFFSET_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    group_id: str
    offset: int
    limit: int
    def __init__(self, domain_id: _Optional[str] = ..., group_id: _Optional[str] = ..., offset: _Optional[int] = ..., limit: _Optional[int] = ...) -> None: ...

class GetGroupMembersOfTypeGroupResponse(_message.Message):
    __slots__ = ("groups",)
    GROUPS_FIELD_NUMBER: _ClassVar[int]
    groups: _containers.RepeatedCompositeFieldContainer[_sharing_pb2.UserGroup]
    def __init__(self, groups: _Optional[_Iterable[_Union[_sharing_pb2.UserGroup, _Mapping]]] = ...) -> None: ...

class AddChildGroupsToParentGroupRequest(_message.Message):
    __slots__ = ("domain_id", "child_ids", "group_id")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    CHILD_IDS_FIELD_NUMBER: _ClassVar[int]
    GROUP_ID_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    child_ids: _containers.RepeatedScalarFieldContainer[str]
    group_id: str
    def __init__(self, domain_id: _Optional[str] = ..., child_ids: _Optional[_Iterable[str]] = ..., group_id: _Optional[str] = ...) -> None: ...

class RemoveChildGroupFromParentGroupRequest(_message.Message):
    __slots__ = ("domain_id", "child_id", "group_id")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    CHILD_ID_FIELD_NUMBER: _ClassVar[int]
    GROUP_ID_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    child_id: str
    group_id: str
    def __init__(self, domain_id: _Optional[str] = ..., child_id: _Optional[str] = ..., group_id: _Optional[str] = ...) -> None: ...

class GetAllMemberGroupsForUserRequest(_message.Message):
    __slots__ = ("domain_id", "user_id")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    user_id: str
    def __init__(self, domain_id: _Optional[str] = ..., user_id: _Optional[str] = ...) -> None: ...

class GetAllMemberGroupsForUserResponse(_message.Message):
    __slots__ = ("groups",)
    GROUPS_FIELD_NUMBER: _ClassVar[int]
    groups: _containers.RepeatedCompositeFieldContainer[_sharing_pb2.UserGroup]
    def __init__(self, groups: _Optional[_Iterable[_Union[_sharing_pb2.UserGroup, _Mapping]]] = ...) -> None: ...

class CreateEntityTypeRequest(_message.Message):
    __slots__ = ("entity_type",)
    ENTITY_TYPE_FIELD_NUMBER: _ClassVar[int]
    entity_type: _sharing_pb2.EntityType
    def __init__(self, entity_type: _Optional[_Union[_sharing_pb2.EntityType, _Mapping]] = ...) -> None: ...

class CreateEntityTypeResponse(_message.Message):
    __slots__ = ("entity_type_id",)
    ENTITY_TYPE_ID_FIELD_NUMBER: _ClassVar[int]
    entity_type_id: str
    def __init__(self, entity_type_id: _Optional[str] = ...) -> None: ...

class UpdateEntityTypeRequest(_message.Message):
    __slots__ = ("entity_type",)
    ENTITY_TYPE_FIELD_NUMBER: _ClassVar[int]
    entity_type: _sharing_pb2.EntityType
    def __init__(self, entity_type: _Optional[_Union[_sharing_pb2.EntityType, _Mapping]] = ...) -> None: ...

class IsEntityTypeExistsRequest(_message.Message):
    __slots__ = ("domain_id", "entity_type_id")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    ENTITY_TYPE_ID_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    entity_type_id: str
    def __init__(self, domain_id: _Optional[str] = ..., entity_type_id: _Optional[str] = ...) -> None: ...

class IsEntityTypeExistsResponse(_message.Message):
    __slots__ = ("exists",)
    EXISTS_FIELD_NUMBER: _ClassVar[int]
    exists: bool
    def __init__(self, exists: bool = ...) -> None: ...

class DeleteEntityTypeRequest(_message.Message):
    __slots__ = ("domain_id", "entity_type_id")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    ENTITY_TYPE_ID_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    entity_type_id: str
    def __init__(self, domain_id: _Optional[str] = ..., entity_type_id: _Optional[str] = ...) -> None: ...

class GetEntityTypeRequest(_message.Message):
    __slots__ = ("domain_id", "entity_type_id")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    ENTITY_TYPE_ID_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    entity_type_id: str
    def __init__(self, domain_id: _Optional[str] = ..., entity_type_id: _Optional[str] = ...) -> None: ...

class GetEntityTypesRequest(_message.Message):
    __slots__ = ("domain_id", "offset", "limit")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    OFFSET_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    offset: int
    limit: int
    def __init__(self, domain_id: _Optional[str] = ..., offset: _Optional[int] = ..., limit: _Optional[int] = ...) -> None: ...

class GetEntityTypesResponse(_message.Message):
    __slots__ = ("entity_types",)
    ENTITY_TYPES_FIELD_NUMBER: _ClassVar[int]
    entity_types: _containers.RepeatedCompositeFieldContainer[_sharing_pb2.EntityType]
    def __init__(self, entity_types: _Optional[_Iterable[_Union[_sharing_pb2.EntityType, _Mapping]]] = ...) -> None: ...

class CreateEntityRequest(_message.Message):
    __slots__ = ("entity",)
    ENTITY_FIELD_NUMBER: _ClassVar[int]
    entity: _sharing_pb2.Entity
    def __init__(self, entity: _Optional[_Union[_sharing_pb2.Entity, _Mapping]] = ...) -> None: ...

class CreateEntityResponse(_message.Message):
    __slots__ = ("entity_id",)
    ENTITY_ID_FIELD_NUMBER: _ClassVar[int]
    entity_id: str
    def __init__(self, entity_id: _Optional[str] = ...) -> None: ...

class UpdateEntityRequest(_message.Message):
    __slots__ = ("entity",)
    ENTITY_FIELD_NUMBER: _ClassVar[int]
    entity: _sharing_pb2.Entity
    def __init__(self, entity: _Optional[_Union[_sharing_pb2.Entity, _Mapping]] = ...) -> None: ...

class IsEntityExistsRequest(_message.Message):
    __slots__ = ("domain_id", "entity_id")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    ENTITY_ID_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    entity_id: str
    def __init__(self, domain_id: _Optional[str] = ..., entity_id: _Optional[str] = ...) -> None: ...

class IsEntityExistsResponse(_message.Message):
    __slots__ = ("exists",)
    EXISTS_FIELD_NUMBER: _ClassVar[int]
    exists: bool
    def __init__(self, exists: bool = ...) -> None: ...

class DeleteEntityRequest(_message.Message):
    __slots__ = ("domain_id", "entity_id")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    ENTITY_ID_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    entity_id: str
    def __init__(self, domain_id: _Optional[str] = ..., entity_id: _Optional[str] = ...) -> None: ...

class GetEntityRequest(_message.Message):
    __slots__ = ("domain_id", "entity_id")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    ENTITY_ID_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    entity_id: str
    def __init__(self, domain_id: _Optional[str] = ..., entity_id: _Optional[str] = ...) -> None: ...

class SearchEntitiesRequest(_message.Message):
    __slots__ = ("domain_id", "user_id", "filters", "offset", "limit")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    FILTERS_FIELD_NUMBER: _ClassVar[int]
    OFFSET_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    user_id: str
    filters: _containers.RepeatedCompositeFieldContainer[_sharing_pb2.SearchCriteria]
    offset: int
    limit: int
    def __init__(self, domain_id: _Optional[str] = ..., user_id: _Optional[str] = ..., filters: _Optional[_Iterable[_Union[_sharing_pb2.SearchCriteria, _Mapping]]] = ..., offset: _Optional[int] = ..., limit: _Optional[int] = ...) -> None: ...

class SearchEntitiesResponse(_message.Message):
    __slots__ = ("entities",)
    ENTITIES_FIELD_NUMBER: _ClassVar[int]
    entities: _containers.RepeatedCompositeFieldContainer[_sharing_pb2.Entity]
    def __init__(self, entities: _Optional[_Iterable[_Union[_sharing_pb2.Entity, _Mapping]]] = ...) -> None: ...

class GetListOfSharedUsersRequest(_message.Message):
    __slots__ = ("domain_id", "entity_id", "permission_type_id")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    ENTITY_ID_FIELD_NUMBER: _ClassVar[int]
    PERMISSION_TYPE_ID_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    entity_id: str
    permission_type_id: str
    def __init__(self, domain_id: _Optional[str] = ..., entity_id: _Optional[str] = ..., permission_type_id: _Optional[str] = ...) -> None: ...

class GetListOfSharedUsersResponse(_message.Message):
    __slots__ = ("users",)
    USERS_FIELD_NUMBER: _ClassVar[int]
    users: _containers.RepeatedCompositeFieldContainer[_sharing_pb2.User]
    def __init__(self, users: _Optional[_Iterable[_Union[_sharing_pb2.User, _Mapping]]] = ...) -> None: ...

class GetListOfDirectlySharedUsersRequest(_message.Message):
    __slots__ = ("domain_id", "entity_id", "permission_type_id")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    ENTITY_ID_FIELD_NUMBER: _ClassVar[int]
    PERMISSION_TYPE_ID_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    entity_id: str
    permission_type_id: str
    def __init__(self, domain_id: _Optional[str] = ..., entity_id: _Optional[str] = ..., permission_type_id: _Optional[str] = ...) -> None: ...

class GetListOfDirectlySharedUsersResponse(_message.Message):
    __slots__ = ("users",)
    USERS_FIELD_NUMBER: _ClassVar[int]
    users: _containers.RepeatedCompositeFieldContainer[_sharing_pb2.User]
    def __init__(self, users: _Optional[_Iterable[_Union[_sharing_pb2.User, _Mapping]]] = ...) -> None: ...

class GetListOfSharedGroupsRequest(_message.Message):
    __slots__ = ("domain_id", "entity_id", "permission_type_id")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    ENTITY_ID_FIELD_NUMBER: _ClassVar[int]
    PERMISSION_TYPE_ID_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    entity_id: str
    permission_type_id: str
    def __init__(self, domain_id: _Optional[str] = ..., entity_id: _Optional[str] = ..., permission_type_id: _Optional[str] = ...) -> None: ...

class GetListOfSharedGroupsResponse(_message.Message):
    __slots__ = ("groups",)
    GROUPS_FIELD_NUMBER: _ClassVar[int]
    groups: _containers.RepeatedCompositeFieldContainer[_sharing_pb2.UserGroup]
    def __init__(self, groups: _Optional[_Iterable[_Union[_sharing_pb2.UserGroup, _Mapping]]] = ...) -> None: ...

class GetListOfDirectlySharedGroupsRequest(_message.Message):
    __slots__ = ("domain_id", "entity_id", "permission_type_id")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    ENTITY_ID_FIELD_NUMBER: _ClassVar[int]
    PERMISSION_TYPE_ID_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    entity_id: str
    permission_type_id: str
    def __init__(self, domain_id: _Optional[str] = ..., entity_id: _Optional[str] = ..., permission_type_id: _Optional[str] = ...) -> None: ...

class GetListOfDirectlySharedGroupsResponse(_message.Message):
    __slots__ = ("groups",)
    GROUPS_FIELD_NUMBER: _ClassVar[int]
    groups: _containers.RepeatedCompositeFieldContainer[_sharing_pb2.UserGroup]
    def __init__(self, groups: _Optional[_Iterable[_Union[_sharing_pb2.UserGroup, _Mapping]]] = ...) -> None: ...

class CreatePermissionTypeRequest(_message.Message):
    __slots__ = ("permission_type",)
    PERMISSION_TYPE_FIELD_NUMBER: _ClassVar[int]
    permission_type: _sharing_pb2.PermissionType
    def __init__(self, permission_type: _Optional[_Union[_sharing_pb2.PermissionType, _Mapping]] = ...) -> None: ...

class CreatePermissionTypeResponse(_message.Message):
    __slots__ = ("permission_type_id",)
    PERMISSION_TYPE_ID_FIELD_NUMBER: _ClassVar[int]
    permission_type_id: str
    def __init__(self, permission_type_id: _Optional[str] = ...) -> None: ...

class UpdatePermissionTypeRequest(_message.Message):
    __slots__ = ("permission_type",)
    PERMISSION_TYPE_FIELD_NUMBER: _ClassVar[int]
    permission_type: _sharing_pb2.PermissionType
    def __init__(self, permission_type: _Optional[_Union[_sharing_pb2.PermissionType, _Mapping]] = ...) -> None: ...

class IsPermissionExistsRequest(_message.Message):
    __slots__ = ("domain_id", "permission_id")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    PERMISSION_ID_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    permission_id: str
    def __init__(self, domain_id: _Optional[str] = ..., permission_id: _Optional[str] = ...) -> None: ...

class IsPermissionExistsResponse(_message.Message):
    __slots__ = ("exists",)
    EXISTS_FIELD_NUMBER: _ClassVar[int]
    exists: bool
    def __init__(self, exists: bool = ...) -> None: ...

class DeletePermissionTypeRequest(_message.Message):
    __slots__ = ("domain_id", "permission_type_id")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    PERMISSION_TYPE_ID_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    permission_type_id: str
    def __init__(self, domain_id: _Optional[str] = ..., permission_type_id: _Optional[str] = ...) -> None: ...

class GetPermissionTypeRequest(_message.Message):
    __slots__ = ("domain_id", "permission_type_id")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    PERMISSION_TYPE_ID_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    permission_type_id: str
    def __init__(self, domain_id: _Optional[str] = ..., permission_type_id: _Optional[str] = ...) -> None: ...

class GetPermissionTypesRequest(_message.Message):
    __slots__ = ("domain_id", "offset", "limit")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    OFFSET_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    offset: int
    limit: int
    def __init__(self, domain_id: _Optional[str] = ..., offset: _Optional[int] = ..., limit: _Optional[int] = ...) -> None: ...

class GetPermissionTypesResponse(_message.Message):
    __slots__ = ("permission_types",)
    PERMISSION_TYPES_FIELD_NUMBER: _ClassVar[int]
    permission_types: _containers.RepeatedCompositeFieldContainer[_sharing_pb2.PermissionType]
    def __init__(self, permission_types: _Optional[_Iterable[_Union[_sharing_pb2.PermissionType, _Mapping]]] = ...) -> None: ...

class ShareEntityWithUsersRequest(_message.Message):
    __slots__ = ("domain_id", "entity_id", "user_list", "permission_type_id", "cascade_permission")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    ENTITY_ID_FIELD_NUMBER: _ClassVar[int]
    USER_LIST_FIELD_NUMBER: _ClassVar[int]
    PERMISSION_TYPE_ID_FIELD_NUMBER: _ClassVar[int]
    CASCADE_PERMISSION_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    entity_id: str
    user_list: _containers.RepeatedScalarFieldContainer[str]
    permission_type_id: str
    cascade_permission: bool
    def __init__(self, domain_id: _Optional[str] = ..., entity_id: _Optional[str] = ..., user_list: _Optional[_Iterable[str]] = ..., permission_type_id: _Optional[str] = ..., cascade_permission: bool = ...) -> None: ...

class RevokeEntitySharingFromUsersRequest(_message.Message):
    __slots__ = ("domain_id", "entity_id", "user_list", "permission_type_id")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    ENTITY_ID_FIELD_NUMBER: _ClassVar[int]
    USER_LIST_FIELD_NUMBER: _ClassVar[int]
    PERMISSION_TYPE_ID_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    entity_id: str
    user_list: _containers.RepeatedScalarFieldContainer[str]
    permission_type_id: str
    def __init__(self, domain_id: _Optional[str] = ..., entity_id: _Optional[str] = ..., user_list: _Optional[_Iterable[str]] = ..., permission_type_id: _Optional[str] = ...) -> None: ...

class ShareEntityWithGroupsRequest(_message.Message):
    __slots__ = ("domain_id", "entity_id", "group_list", "permission_type_id", "cascade_permission")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    ENTITY_ID_FIELD_NUMBER: _ClassVar[int]
    GROUP_LIST_FIELD_NUMBER: _ClassVar[int]
    PERMISSION_TYPE_ID_FIELD_NUMBER: _ClassVar[int]
    CASCADE_PERMISSION_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    entity_id: str
    group_list: _containers.RepeatedScalarFieldContainer[str]
    permission_type_id: str
    cascade_permission: bool
    def __init__(self, domain_id: _Optional[str] = ..., entity_id: _Optional[str] = ..., group_list: _Optional[_Iterable[str]] = ..., permission_type_id: _Optional[str] = ..., cascade_permission: bool = ...) -> None: ...

class RevokeEntitySharingFromGroupsRequest(_message.Message):
    __slots__ = ("domain_id", "entity_id", "group_list", "permission_type_id")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    ENTITY_ID_FIELD_NUMBER: _ClassVar[int]
    GROUP_LIST_FIELD_NUMBER: _ClassVar[int]
    PERMISSION_TYPE_ID_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    entity_id: str
    group_list: _containers.RepeatedScalarFieldContainer[str]
    permission_type_id: str
    def __init__(self, domain_id: _Optional[str] = ..., entity_id: _Optional[str] = ..., group_list: _Optional[_Iterable[str]] = ..., permission_type_id: _Optional[str] = ...) -> None: ...
