from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable
from typing import ClassVar as _ClassVar, Optional as _Optional

DESCRIPTOR: _descriptor.FileDescriptor

class ResourceType(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    RESOURCE_TYPE_UNKNOWN: _ClassVar[ResourceType]
    PROJECT: _ClassVar[ResourceType]
    EXPERIMENT: _ClassVar[ResourceType]
    DATA: _ClassVar[ResourceType]
    APPLICATION_DEPLOYMENT: _ClassVar[ResourceType]
    GROUP_RESOURCE_PROFILE: _ClassVar[ResourceType]
    CREDENTIAL_TOKEN: _ClassVar[ResourceType]
    OTHER: _ClassVar[ResourceType]

class ResourcePermissionType(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    RESOURCE_PERMISSION_TYPE_UNKNOWN: _ClassVar[ResourcePermissionType]
    WRITE: _ClassVar[ResourcePermissionType]
    READ: _ClassVar[ResourcePermissionType]
    OWNER: _ClassVar[ResourcePermissionType]
    MANAGE_SHARING: _ClassVar[ResourcePermissionType]
RESOURCE_TYPE_UNKNOWN: ResourceType
PROJECT: ResourceType
EXPERIMENT: ResourceType
DATA: ResourceType
APPLICATION_DEPLOYMENT: ResourceType
GROUP_RESOURCE_PROFILE: ResourceType
CREDENTIAL_TOKEN: ResourceType
OTHER: ResourceType
RESOURCE_PERMISSION_TYPE_UNKNOWN: ResourcePermissionType
WRITE: ResourcePermissionType
READ: ResourcePermissionType
OWNER: ResourcePermissionType
MANAGE_SHARING: ResourcePermissionType

class GroupModel(_message.Message):
    __slots__ = ("id", "name", "owner_id", "description", "members", "admins")
    ID_FIELD_NUMBER: _ClassVar[int]
    NAME_FIELD_NUMBER: _ClassVar[int]
    OWNER_ID_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    MEMBERS_FIELD_NUMBER: _ClassVar[int]
    ADMINS_FIELD_NUMBER: _ClassVar[int]
    id: str
    name: str
    owner_id: str
    description: str
    members: _containers.RepeatedScalarFieldContainer[str]
    admins: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, id: _Optional[str] = ..., name: _Optional[str] = ..., owner_id: _Optional[str] = ..., description: _Optional[str] = ..., members: _Optional[_Iterable[str]] = ..., admins: _Optional[_Iterable[str]] = ...) -> None: ...
