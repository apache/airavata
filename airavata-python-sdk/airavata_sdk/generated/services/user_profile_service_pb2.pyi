from google.api import annotations_pb2 as _annotations_pb2
from google.protobuf import empty_pb2 as _empty_pb2
from org.apache.airavata.model.user import user_profile_pb2 as _user_profile_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class AddUserProfileRequest(_message.Message):
    __slots__ = ("user_profile",)
    USER_PROFILE_FIELD_NUMBER: _ClassVar[int]
    user_profile: _user_profile_pb2.UserProfile
    def __init__(self, user_profile: _Optional[_Union[_user_profile_pb2.UserProfile, _Mapping]] = ...) -> None: ...

class AddUserProfileResponse(_message.Message):
    __slots__ = ("user_id",)
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    user_id: str
    def __init__(self, user_id: _Optional[str] = ...) -> None: ...

class UpdateUserProfileRequest(_message.Message):
    __slots__ = ("user_profile",)
    USER_PROFILE_FIELD_NUMBER: _ClassVar[int]
    user_profile: _user_profile_pb2.UserProfile
    def __init__(self, user_profile: _Optional[_Union[_user_profile_pb2.UserProfile, _Mapping]] = ...) -> None: ...

class GetUserProfileByIdRequest(_message.Message):
    __slots__ = ("user_id", "gateway_id")
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    user_id: str
    gateway_id: str
    def __init__(self, user_id: _Optional[str] = ..., gateway_id: _Optional[str] = ...) -> None: ...

class GetUserProfileByNameRequest(_message.Message):
    __slots__ = ("user_name", "gateway_id")
    USER_NAME_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    user_name: str
    gateway_id: str
    def __init__(self, user_name: _Optional[str] = ..., gateway_id: _Optional[str] = ...) -> None: ...

class DeleteUserProfileRequest(_message.Message):
    __slots__ = ("user_id",)
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    user_id: str
    def __init__(self, user_id: _Optional[str] = ...) -> None: ...

class GetAllUserProfilesInGatewayRequest(_message.Message):
    __slots__ = ("gateway_id", "offset", "limit")
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    OFFSET_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    offset: int
    limit: int
    def __init__(self, gateway_id: _Optional[str] = ..., offset: _Optional[int] = ..., limit: _Optional[int] = ...) -> None: ...

class GetAllUserProfilesInGatewayResponse(_message.Message):
    __slots__ = ("user_profiles",)
    USER_PROFILES_FIELD_NUMBER: _ClassVar[int]
    user_profiles: _containers.RepeatedCompositeFieldContainer[_user_profile_pb2.UserProfile]
    def __init__(self, user_profiles: _Optional[_Iterable[_Union[_user_profile_pb2.UserProfile, _Mapping]]] = ...) -> None: ...

class DoesUserExistRequest(_message.Message):
    __slots__ = ("user_name", "gateway_id")
    USER_NAME_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    user_name: str
    gateway_id: str
    def __init__(self, user_name: _Optional[str] = ..., gateway_id: _Optional[str] = ...) -> None: ...

class DoesUserExistResponse(_message.Message):
    __slots__ = ("exists",)
    EXISTS_FIELD_NUMBER: _ClassVar[int]
    exists: bool
    def __init__(self, exists: bool = ...) -> None: ...
