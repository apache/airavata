from google.api import annotations_pb2 as _annotations_pb2
from google.protobuf import empty_pb2 as _empty_pb2
from org.apache.airavata.model.group import group_manager_pb2 as _group_manager_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class CreateGroupRequest(_message.Message):
    __slots__ = ("group",)
    GROUP_FIELD_NUMBER: _ClassVar[int]
    group: _group_manager_pb2.GroupModel
    def __init__(self, group: _Optional[_Union[_group_manager_pb2.GroupModel, _Mapping]] = ...) -> None: ...

class CreateGroupResponse(_message.Message):
    __slots__ = ("group_id",)
    GROUP_ID_FIELD_NUMBER: _ClassVar[int]
    group_id: str
    def __init__(self, group_id: _Optional[str] = ...) -> None: ...

class UpdateGroupRequest(_message.Message):
    __slots__ = ("group",)
    GROUP_FIELD_NUMBER: _ClassVar[int]
    group: _group_manager_pb2.GroupModel
    def __init__(self, group: _Optional[_Union[_group_manager_pb2.GroupModel, _Mapping]] = ...) -> None: ...

class DeleteGroupRequest(_message.Message):
    __slots__ = ("group_id", "owner_id")
    GROUP_ID_FIELD_NUMBER: _ClassVar[int]
    OWNER_ID_FIELD_NUMBER: _ClassVar[int]
    group_id: str
    owner_id: str
    def __init__(self, group_id: _Optional[str] = ..., owner_id: _Optional[str] = ...) -> None: ...

class GetGroupRequest(_message.Message):
    __slots__ = ("group_id",)
    GROUP_ID_FIELD_NUMBER: _ClassVar[int]
    group_id: str
    def __init__(self, group_id: _Optional[str] = ...) -> None: ...

class GetGroupsRequest(_message.Message):
    __slots__ = ()
    def __init__(self) -> None: ...

class GetGroupsResponse(_message.Message):
    __slots__ = ("groups",)
    GROUPS_FIELD_NUMBER: _ClassVar[int]
    groups: _containers.RepeatedCompositeFieldContainer[_group_manager_pb2.GroupModel]
    def __init__(self, groups: _Optional[_Iterable[_Union[_group_manager_pb2.GroupModel, _Mapping]]] = ...) -> None: ...

class GetAllGroupsUserBelongsRequest(_message.Message):
    __slots__ = ("user_name",)
    USER_NAME_FIELD_NUMBER: _ClassVar[int]
    user_name: str
    def __init__(self, user_name: _Optional[str] = ...) -> None: ...

class GetAllGroupsUserBelongsResponse(_message.Message):
    __slots__ = ("groups",)
    GROUPS_FIELD_NUMBER: _ClassVar[int]
    groups: _containers.RepeatedCompositeFieldContainer[_group_manager_pb2.GroupModel]
    def __init__(self, groups: _Optional[_Iterable[_Union[_group_manager_pb2.GroupModel, _Mapping]]] = ...) -> None: ...

class AddUsersToGroupRequest(_message.Message):
    __slots__ = ("group_id", "user_ids")
    GROUP_ID_FIELD_NUMBER: _ClassVar[int]
    USER_IDS_FIELD_NUMBER: _ClassVar[int]
    group_id: str
    user_ids: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, group_id: _Optional[str] = ..., user_ids: _Optional[_Iterable[str]] = ...) -> None: ...

class RemoveUsersFromGroupRequest(_message.Message):
    __slots__ = ("group_id", "user_ids")
    GROUP_ID_FIELD_NUMBER: _ClassVar[int]
    USER_IDS_FIELD_NUMBER: _ClassVar[int]
    group_id: str
    user_ids: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, group_id: _Optional[str] = ..., user_ids: _Optional[_Iterable[str]] = ...) -> None: ...

class TransferGroupOwnershipRequest(_message.Message):
    __slots__ = ("group_id", "new_owner_id")
    GROUP_ID_FIELD_NUMBER: _ClassVar[int]
    NEW_OWNER_ID_FIELD_NUMBER: _ClassVar[int]
    group_id: str
    new_owner_id: str
    def __init__(self, group_id: _Optional[str] = ..., new_owner_id: _Optional[str] = ...) -> None: ...

class AddGroupAdminsRequest(_message.Message):
    __slots__ = ("group_id", "admin_ids")
    GROUP_ID_FIELD_NUMBER: _ClassVar[int]
    ADMIN_IDS_FIELD_NUMBER: _ClassVar[int]
    group_id: str
    admin_ids: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, group_id: _Optional[str] = ..., admin_ids: _Optional[_Iterable[str]] = ...) -> None: ...

class RemoveGroupAdminsRequest(_message.Message):
    __slots__ = ("group_id", "admin_ids")
    GROUP_ID_FIELD_NUMBER: _ClassVar[int]
    ADMIN_IDS_FIELD_NUMBER: _ClassVar[int]
    group_id: str
    admin_ids: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, group_id: _Optional[str] = ..., admin_ids: _Optional[_Iterable[str]] = ...) -> None: ...

class HasAdminAccessRequest(_message.Message):
    __slots__ = ("group_id", "admin_id")
    GROUP_ID_FIELD_NUMBER: _ClassVar[int]
    ADMIN_ID_FIELD_NUMBER: _ClassVar[int]
    group_id: str
    admin_id: str
    def __init__(self, group_id: _Optional[str] = ..., admin_id: _Optional[str] = ...) -> None: ...

class HasAdminAccessResponse(_message.Message):
    __slots__ = ("has_access",)
    HAS_ACCESS_FIELD_NUMBER: _ClassVar[int]
    has_access: bool
    def __init__(self, has_access: bool = ...) -> None: ...

class HasOwnerAccessRequest(_message.Message):
    __slots__ = ("group_id", "owner_id")
    GROUP_ID_FIELD_NUMBER: _ClassVar[int]
    OWNER_ID_FIELD_NUMBER: _ClassVar[int]
    group_id: str
    owner_id: str
    def __init__(self, group_id: _Optional[str] = ..., owner_id: _Optional[str] = ...) -> None: ...

class HasOwnerAccessResponse(_message.Message):
    __slots__ = ("has_access",)
    HAS_ACCESS_FIELD_NUMBER: _ClassVar[int]
    has_access: bool
    def __init__(self, has_access: bool = ...) -> None: ...
