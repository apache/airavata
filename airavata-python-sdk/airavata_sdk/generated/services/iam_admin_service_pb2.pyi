from google.api import annotations_pb2 as _annotations_pb2
from google.protobuf import empty_pb2 as _empty_pb2
from org.apache.airavata.model.user import user_profile_pb2 as _user_profile_pb2
from org.apache.airavata.model.workspace import workspace_pb2 as _workspace_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class SetUpGatewayRequest(_message.Message):
    __slots__ = ("gateway",)
    GATEWAY_FIELD_NUMBER: _ClassVar[int]
    gateway: _workspace_pb2.Gateway
    def __init__(self, gateway: _Optional[_Union[_workspace_pb2.Gateway, _Mapping]] = ...) -> None: ...

class IsUsernameAvailableRequest(_message.Message):
    __slots__ = ("username",)
    USERNAME_FIELD_NUMBER: _ClassVar[int]
    username: str
    def __init__(self, username: _Optional[str] = ...) -> None: ...

class IsUsernameAvailableResponse(_message.Message):
    __slots__ = ("available",)
    AVAILABLE_FIELD_NUMBER: _ClassVar[int]
    available: bool
    def __init__(self, available: bool = ...) -> None: ...

class RegisterUserRequest(_message.Message):
    __slots__ = ("username", "email_address", "first_name", "last_name", "new_password")
    USERNAME_FIELD_NUMBER: _ClassVar[int]
    EMAIL_ADDRESS_FIELD_NUMBER: _ClassVar[int]
    FIRST_NAME_FIELD_NUMBER: _ClassVar[int]
    LAST_NAME_FIELD_NUMBER: _ClassVar[int]
    NEW_PASSWORD_FIELD_NUMBER: _ClassVar[int]
    username: str
    email_address: str
    first_name: str
    last_name: str
    new_password: str
    def __init__(self, username: _Optional[str] = ..., email_address: _Optional[str] = ..., first_name: _Optional[str] = ..., last_name: _Optional[str] = ..., new_password: _Optional[str] = ...) -> None: ...

class EnableUserRequest(_message.Message):
    __slots__ = ("username",)
    USERNAME_FIELD_NUMBER: _ClassVar[int]
    username: str
    def __init__(self, username: _Optional[str] = ...) -> None: ...

class IsUserEnabledRequest(_message.Message):
    __slots__ = ("username",)
    USERNAME_FIELD_NUMBER: _ClassVar[int]
    username: str
    def __init__(self, username: _Optional[str] = ...) -> None: ...

class IsUserEnabledResponse(_message.Message):
    __slots__ = ("enabled",)
    ENABLED_FIELD_NUMBER: _ClassVar[int]
    enabled: bool
    def __init__(self, enabled: bool = ...) -> None: ...

class IsUserExistRequest(_message.Message):
    __slots__ = ("username",)
    USERNAME_FIELD_NUMBER: _ClassVar[int]
    username: str
    def __init__(self, username: _Optional[str] = ...) -> None: ...

class IsUserExistResponse(_message.Message):
    __slots__ = ("exists",)
    EXISTS_FIELD_NUMBER: _ClassVar[int]
    exists: bool
    def __init__(self, exists: bool = ...) -> None: ...

class GetIamUserRequest(_message.Message):
    __slots__ = ("username",)
    USERNAME_FIELD_NUMBER: _ClassVar[int]
    username: str
    def __init__(self, username: _Optional[str] = ...) -> None: ...

class GetIamUsersRequest(_message.Message):
    __slots__ = ("offset", "limit", "search")
    OFFSET_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    SEARCH_FIELD_NUMBER: _ClassVar[int]
    offset: int
    limit: int
    search: str
    def __init__(self, offset: _Optional[int] = ..., limit: _Optional[int] = ..., search: _Optional[str] = ...) -> None: ...

class GetIamUsersResponse(_message.Message):
    __slots__ = ("users",)
    USERS_FIELD_NUMBER: _ClassVar[int]
    users: _containers.RepeatedCompositeFieldContainer[_user_profile_pb2.UserProfile]
    def __init__(self, users: _Optional[_Iterable[_Union[_user_profile_pb2.UserProfile, _Mapping]]] = ...) -> None: ...

class ResetUserPasswordRequest(_message.Message):
    __slots__ = ("username", "new_password")
    USERNAME_FIELD_NUMBER: _ClassVar[int]
    NEW_PASSWORD_FIELD_NUMBER: _ClassVar[int]
    username: str
    new_password: str
    def __init__(self, username: _Optional[str] = ..., new_password: _Optional[str] = ...) -> None: ...

class FindUsersRequest(_message.Message):
    __slots__ = ("email", "user_id")
    EMAIL_FIELD_NUMBER: _ClassVar[int]
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    email: str
    user_id: str
    def __init__(self, email: _Optional[str] = ..., user_id: _Optional[str] = ...) -> None: ...

class FindUsersResponse(_message.Message):
    __slots__ = ("users",)
    USERS_FIELD_NUMBER: _ClassVar[int]
    users: _containers.RepeatedCompositeFieldContainer[_user_profile_pb2.UserProfile]
    def __init__(self, users: _Optional[_Iterable[_Union[_user_profile_pb2.UserProfile, _Mapping]]] = ...) -> None: ...

class UpdateIamUserProfileRequest(_message.Message):
    __slots__ = ("user_details",)
    USER_DETAILS_FIELD_NUMBER: _ClassVar[int]
    user_details: _user_profile_pb2.UserProfile
    def __init__(self, user_details: _Optional[_Union[_user_profile_pb2.UserProfile, _Mapping]] = ...) -> None: ...

class DeleteUserRequest(_message.Message):
    __slots__ = ("username",)
    USERNAME_FIELD_NUMBER: _ClassVar[int]
    username: str
    def __init__(self, username: _Optional[str] = ...) -> None: ...

class AddRoleToUserRequest(_message.Message):
    __slots__ = ("username", "role_name")
    USERNAME_FIELD_NUMBER: _ClassVar[int]
    ROLE_NAME_FIELD_NUMBER: _ClassVar[int]
    username: str
    role_name: str
    def __init__(self, username: _Optional[str] = ..., role_name: _Optional[str] = ...) -> None: ...

class RemoveRoleFromUserRequest(_message.Message):
    __slots__ = ("username", "role_name")
    USERNAME_FIELD_NUMBER: _ClassVar[int]
    ROLE_NAME_FIELD_NUMBER: _ClassVar[int]
    username: str
    role_name: str
    def __init__(self, username: _Optional[str] = ..., role_name: _Optional[str] = ...) -> None: ...

class GetUsersWithRoleRequest(_message.Message):
    __slots__ = ("role_name",)
    ROLE_NAME_FIELD_NUMBER: _ClassVar[int]
    role_name: str
    def __init__(self, role_name: _Optional[str] = ...) -> None: ...

class GetUsersWithRoleResponse(_message.Message):
    __slots__ = ("users",)
    USERS_FIELD_NUMBER: _ClassVar[int]
    users: _containers.RepeatedCompositeFieldContainer[_user_profile_pb2.UserProfile]
    def __init__(self, users: _Optional[_Iterable[_Union[_user_profile_pb2.UserProfile, _Mapping]]] = ...) -> None: ...
