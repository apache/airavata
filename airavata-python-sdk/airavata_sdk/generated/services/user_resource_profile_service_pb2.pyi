from google.api import annotations_pb2 as _annotations_pb2
from google.protobuf import empty_pb2 as _empty_pb2
from org.apache.airavata.model.appcatalog.userresourceprofile import user_resource_profile_pb2 as _user_resource_profile_pb2
from org.apache.airavata.model.status import status_pb2 as _status_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class RegisterUserResourceProfileRequest(_message.Message):
    __slots__ = ("user_resource_profile",)
    USER_RESOURCE_PROFILE_FIELD_NUMBER: _ClassVar[int]
    user_resource_profile: _user_resource_profile_pb2.UserResourceProfile
    def __init__(self, user_resource_profile: _Optional[_Union[_user_resource_profile_pb2.UserResourceProfile, _Mapping]] = ...) -> None: ...

class RegisterUserResourceProfileResponse(_message.Message):
    __slots__ = ("user_id",)
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    user_id: str
    def __init__(self, user_id: _Optional[str] = ...) -> None: ...

class IsUserResourceProfileExistsRequest(_message.Message):
    __slots__ = ("user_id", "gateway_id")
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    user_id: str
    gateway_id: str
    def __init__(self, user_id: _Optional[str] = ..., gateway_id: _Optional[str] = ...) -> None: ...

class IsUserResourceProfileExistsResponse(_message.Message):
    __slots__ = ("exists",)
    EXISTS_FIELD_NUMBER: _ClassVar[int]
    exists: bool
    def __init__(self, exists: bool = ...) -> None: ...

class GetUserResourceProfileRequest(_message.Message):
    __slots__ = ("user_id", "gateway_id")
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    user_id: str
    gateway_id: str
    def __init__(self, user_id: _Optional[str] = ..., gateway_id: _Optional[str] = ...) -> None: ...

class UpdateUserResourceProfileRequest(_message.Message):
    __slots__ = ("user_id", "gateway_id", "user_resource_profile")
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    USER_RESOURCE_PROFILE_FIELD_NUMBER: _ClassVar[int]
    user_id: str
    gateway_id: str
    user_resource_profile: _user_resource_profile_pb2.UserResourceProfile
    def __init__(self, user_id: _Optional[str] = ..., gateway_id: _Optional[str] = ..., user_resource_profile: _Optional[_Union[_user_resource_profile_pb2.UserResourceProfile, _Mapping]] = ...) -> None: ...

class DeleteUserResourceProfileRequest(_message.Message):
    __slots__ = ("user_id", "gateway_id")
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    user_id: str
    gateway_id: str
    def __init__(self, user_id: _Optional[str] = ..., gateway_id: _Optional[str] = ...) -> None: ...

class GetAllUserResourceProfilesRequest(_message.Message):
    __slots__ = ()
    def __init__(self) -> None: ...

class GetAllUserResourceProfilesResponse(_message.Message):
    __slots__ = ("user_resource_profiles",)
    USER_RESOURCE_PROFILES_FIELD_NUMBER: _ClassVar[int]
    user_resource_profiles: _containers.RepeatedCompositeFieldContainer[_user_resource_profile_pb2.UserResourceProfile]
    def __init__(self, user_resource_profiles: _Optional[_Iterable[_Union[_user_resource_profile_pb2.UserResourceProfile, _Mapping]]] = ...) -> None: ...

class AddUserComputePreferenceRequest(_message.Message):
    __slots__ = ("user_id", "gateway_id", "compute_resource_id", "user_compute_resource_preference")
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    USER_COMPUTE_RESOURCE_PREFERENCE_FIELD_NUMBER: _ClassVar[int]
    user_id: str
    gateway_id: str
    compute_resource_id: str
    user_compute_resource_preference: _user_resource_profile_pb2.UserComputeResourcePreference
    def __init__(self, user_id: _Optional[str] = ..., gateway_id: _Optional[str] = ..., compute_resource_id: _Optional[str] = ..., user_compute_resource_preference: _Optional[_Union[_user_resource_profile_pb2.UserComputeResourcePreference, _Mapping]] = ...) -> None: ...

class GetUserComputePreferenceRequest(_message.Message):
    __slots__ = ("user_id", "gateway_id", "compute_resource_id")
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    user_id: str
    gateway_id: str
    compute_resource_id: str
    def __init__(self, user_id: _Optional[str] = ..., gateway_id: _Optional[str] = ..., compute_resource_id: _Optional[str] = ...) -> None: ...

class UpdateUserComputePreferenceRequest(_message.Message):
    __slots__ = ("user_id", "gateway_id", "compute_resource_id", "user_compute_resource_preference")
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    USER_COMPUTE_RESOURCE_PREFERENCE_FIELD_NUMBER: _ClassVar[int]
    user_id: str
    gateway_id: str
    compute_resource_id: str
    user_compute_resource_preference: _user_resource_profile_pb2.UserComputeResourcePreference
    def __init__(self, user_id: _Optional[str] = ..., gateway_id: _Optional[str] = ..., compute_resource_id: _Optional[str] = ..., user_compute_resource_preference: _Optional[_Union[_user_resource_profile_pb2.UserComputeResourcePreference, _Mapping]] = ...) -> None: ...

class DeleteUserComputePreferenceRequest(_message.Message):
    __slots__ = ("user_id", "gateway_id", "compute_resource_id")
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    user_id: str
    gateway_id: str
    compute_resource_id: str
    def __init__(self, user_id: _Optional[str] = ..., gateway_id: _Optional[str] = ..., compute_resource_id: _Optional[str] = ...) -> None: ...

class GetAllUserComputePreferencesRequest(_message.Message):
    __slots__ = ("user_id", "gateway_id")
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    user_id: str
    gateway_id: str
    def __init__(self, user_id: _Optional[str] = ..., gateway_id: _Optional[str] = ...) -> None: ...

class GetAllUserComputePreferencesResponse(_message.Message):
    __slots__ = ("user_compute_resource_preferences",)
    USER_COMPUTE_RESOURCE_PREFERENCES_FIELD_NUMBER: _ClassVar[int]
    user_compute_resource_preferences: _containers.RepeatedCompositeFieldContainer[_user_resource_profile_pb2.UserComputeResourcePreference]
    def __init__(self, user_compute_resource_preferences: _Optional[_Iterable[_Union[_user_resource_profile_pb2.UserComputeResourcePreference, _Mapping]]] = ...) -> None: ...

class AddUserStoragePreferenceRequest(_message.Message):
    __slots__ = ("user_id", "gateway_id", "storage_resource_id", "user_storage_preference")
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    USER_STORAGE_PREFERENCE_FIELD_NUMBER: _ClassVar[int]
    user_id: str
    gateway_id: str
    storage_resource_id: str
    user_storage_preference: _user_resource_profile_pb2.UserStoragePreference
    def __init__(self, user_id: _Optional[str] = ..., gateway_id: _Optional[str] = ..., storage_resource_id: _Optional[str] = ..., user_storage_preference: _Optional[_Union[_user_resource_profile_pb2.UserStoragePreference, _Mapping]] = ...) -> None: ...

class GetUserStoragePreferenceRequest(_message.Message):
    __slots__ = ("user_id", "gateway_id", "storage_resource_id")
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    user_id: str
    gateway_id: str
    storage_resource_id: str
    def __init__(self, user_id: _Optional[str] = ..., gateway_id: _Optional[str] = ..., storage_resource_id: _Optional[str] = ...) -> None: ...

class UpdateUserStoragePreferenceRequest(_message.Message):
    __slots__ = ("user_id", "gateway_id", "storage_resource_id", "user_storage_preference")
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    USER_STORAGE_PREFERENCE_FIELD_NUMBER: _ClassVar[int]
    user_id: str
    gateway_id: str
    storage_resource_id: str
    user_storage_preference: _user_resource_profile_pb2.UserStoragePreference
    def __init__(self, user_id: _Optional[str] = ..., gateway_id: _Optional[str] = ..., storage_resource_id: _Optional[str] = ..., user_storage_preference: _Optional[_Union[_user_resource_profile_pb2.UserStoragePreference, _Mapping]] = ...) -> None: ...

class DeleteUserStoragePreferenceRequest(_message.Message):
    __slots__ = ("user_id", "gateway_id", "storage_resource_id")
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    user_id: str
    gateway_id: str
    storage_resource_id: str
    def __init__(self, user_id: _Optional[str] = ..., gateway_id: _Optional[str] = ..., storage_resource_id: _Optional[str] = ...) -> None: ...

class GetAllUserStoragePreferencesRequest(_message.Message):
    __slots__ = ("user_id", "gateway_id")
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    user_id: str
    gateway_id: str
    def __init__(self, user_id: _Optional[str] = ..., gateway_id: _Optional[str] = ...) -> None: ...

class GetAllUserStoragePreferencesResponse(_message.Message):
    __slots__ = ("user_storage_preferences",)
    USER_STORAGE_PREFERENCES_FIELD_NUMBER: _ClassVar[int]
    user_storage_preferences: _containers.RepeatedCompositeFieldContainer[_user_resource_profile_pb2.UserStoragePreference]
    def __init__(self, user_storage_preferences: _Optional[_Iterable[_Union[_user_resource_profile_pb2.UserStoragePreference, _Mapping]]] = ...) -> None: ...

class GetLatestQueueStatusesRequest(_message.Message):
    __slots__ = ()
    def __init__(self) -> None: ...

class GetLatestQueueStatusesResponse(_message.Message):
    __slots__ = ("queue_statuses",)
    QUEUE_STATUSES_FIELD_NUMBER: _ClassVar[int]
    queue_statuses: _containers.RepeatedCompositeFieldContainer[_status_pb2.QueueStatusModel]
    def __init__(self, queue_statuses: _Optional[_Iterable[_Union[_status_pb2.QueueStatusModel, _Mapping]]] = ...) -> None: ...
