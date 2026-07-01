from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Optional as _Optional

DESCRIPTOR: _descriptor.FileDescriptor

class GatewayGroups(_message.Message):
    __slots__ = ("gateway_id", "admins_group_id", "read_only_admins_group_id", "default_gateway_users_group_id")
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    ADMINS_GROUP_ID_FIELD_NUMBER: _ClassVar[int]
    READ_ONLY_ADMINS_GROUP_ID_FIELD_NUMBER: _ClassVar[int]
    DEFAULT_GATEWAY_USERS_GROUP_ID_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    admins_group_id: str
    read_only_admins_group_id: str
    default_gateway_users_group_id: str
    def __init__(self, gateway_id: _Optional[str] = ..., admins_group_id: _Optional[str] = ..., read_only_admins_group_id: _Optional[str] = ..., default_gateway_users_group_id: _Optional[str] = ...) -> None: ...
