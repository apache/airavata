from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class UserComputeResourcePreference(_message.Message):
    __slots__ = ("compute_resource_id", "login_user_name", "preferred_batch_queue", "scratch_location", "allocation_project_number", "resource_specific_credential_store_token", "quality_of_service", "reservation", "reservation_start_time", "reservation_end_time", "validated")
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    LOGIN_USER_NAME_FIELD_NUMBER: _ClassVar[int]
    PREFERRED_BATCH_QUEUE_FIELD_NUMBER: _ClassVar[int]
    SCRATCH_LOCATION_FIELD_NUMBER: _ClassVar[int]
    ALLOCATION_PROJECT_NUMBER_FIELD_NUMBER: _ClassVar[int]
    RESOURCE_SPECIFIC_CREDENTIAL_STORE_TOKEN_FIELD_NUMBER: _ClassVar[int]
    QUALITY_OF_SERVICE_FIELD_NUMBER: _ClassVar[int]
    RESERVATION_FIELD_NUMBER: _ClassVar[int]
    RESERVATION_START_TIME_FIELD_NUMBER: _ClassVar[int]
    RESERVATION_END_TIME_FIELD_NUMBER: _ClassVar[int]
    VALIDATED_FIELD_NUMBER: _ClassVar[int]
    compute_resource_id: str
    login_user_name: str
    preferred_batch_queue: str
    scratch_location: str
    allocation_project_number: str
    resource_specific_credential_store_token: str
    quality_of_service: str
    reservation: str
    reservation_start_time: int
    reservation_end_time: int
    validated: bool
    def __init__(self, compute_resource_id: _Optional[str] = ..., login_user_name: _Optional[str] = ..., preferred_batch_queue: _Optional[str] = ..., scratch_location: _Optional[str] = ..., allocation_project_number: _Optional[str] = ..., resource_specific_credential_store_token: _Optional[str] = ..., quality_of_service: _Optional[str] = ..., reservation: _Optional[str] = ..., reservation_start_time: _Optional[int] = ..., reservation_end_time: _Optional[int] = ..., validated: bool = ...) -> None: ...

class UserStoragePreference(_message.Message):
    __slots__ = ("storage_resource_id", "login_user_name", "file_system_root_location", "resource_specific_credential_store_token")
    STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    LOGIN_USER_NAME_FIELD_NUMBER: _ClassVar[int]
    FILE_SYSTEM_ROOT_LOCATION_FIELD_NUMBER: _ClassVar[int]
    RESOURCE_SPECIFIC_CREDENTIAL_STORE_TOKEN_FIELD_NUMBER: _ClassVar[int]
    storage_resource_id: str
    login_user_name: str
    file_system_root_location: str
    resource_specific_credential_store_token: str
    def __init__(self, storage_resource_id: _Optional[str] = ..., login_user_name: _Optional[str] = ..., file_system_root_location: _Optional[str] = ..., resource_specific_credential_store_token: _Optional[str] = ...) -> None: ...

class UserResourceProfile(_message.Message):
    __slots__ = ("user_id", "gateway_id", "credential_store_token", "user_compute_resource_preferences", "user_storage_preferences", "identity_server_tenant", "identity_server_pwd_cred_token")
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    CREDENTIAL_STORE_TOKEN_FIELD_NUMBER: _ClassVar[int]
    USER_COMPUTE_RESOURCE_PREFERENCES_FIELD_NUMBER: _ClassVar[int]
    USER_STORAGE_PREFERENCES_FIELD_NUMBER: _ClassVar[int]
    IDENTITY_SERVER_TENANT_FIELD_NUMBER: _ClassVar[int]
    IDENTITY_SERVER_PWD_CRED_TOKEN_FIELD_NUMBER: _ClassVar[int]
    user_id: str
    gateway_id: str
    credential_store_token: str
    user_compute_resource_preferences: _containers.RepeatedCompositeFieldContainer[UserComputeResourcePreference]
    user_storage_preferences: _containers.RepeatedCompositeFieldContainer[UserStoragePreference]
    identity_server_tenant: str
    identity_server_pwd_cred_token: str
    def __init__(self, user_id: _Optional[str] = ..., gateway_id: _Optional[str] = ..., credential_store_token: _Optional[str] = ..., user_compute_resource_preferences: _Optional[_Iterable[_Union[UserComputeResourcePreference, _Mapping]]] = ..., user_storage_preferences: _Optional[_Iterable[_Union[UserStoragePreference, _Mapping]]] = ..., identity_server_tenant: _Optional[str] = ..., identity_server_pwd_cred_token: _Optional[str] = ...) -> None: ...
