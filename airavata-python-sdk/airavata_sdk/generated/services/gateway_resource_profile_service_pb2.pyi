from google.api import annotations_pb2 as _annotations_pb2
from google.protobuf import empty_pb2 as _empty_pb2
from org.apache.airavata.model.appcatalog.gatewayprofile import gateway_profile_pb2 as _gateway_profile_pb2
from org.apache.airavata.model.appcatalog.accountprovisioning import account_provisioning_pb2 as _account_provisioning_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class RegisterGatewayResourceProfileRequest(_message.Message):
    __slots__ = ("gateway_resource_profile",)
    GATEWAY_RESOURCE_PROFILE_FIELD_NUMBER: _ClassVar[int]
    gateway_resource_profile: _gateway_profile_pb2.GatewayResourceProfile
    def __init__(self, gateway_resource_profile: _Optional[_Union[_gateway_profile_pb2.GatewayResourceProfile, _Mapping]] = ...) -> None: ...

class RegisterGatewayResourceProfileResponse(_message.Message):
    __slots__ = ("gateway_id",)
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    def __init__(self, gateway_id: _Optional[str] = ...) -> None: ...

class GetGatewayResourceProfileRequest(_message.Message):
    __slots__ = ("gateway_id",)
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    def __init__(self, gateway_id: _Optional[str] = ...) -> None: ...

class UpdateGatewayResourceProfileRequest(_message.Message):
    __slots__ = ("gateway_id", "gateway_resource_profile")
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_RESOURCE_PROFILE_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    gateway_resource_profile: _gateway_profile_pb2.GatewayResourceProfile
    def __init__(self, gateway_id: _Optional[str] = ..., gateway_resource_profile: _Optional[_Union[_gateway_profile_pb2.GatewayResourceProfile, _Mapping]] = ...) -> None: ...

class DeleteGatewayResourceProfileRequest(_message.Message):
    __slots__ = ("gateway_id",)
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    def __init__(self, gateway_id: _Optional[str] = ...) -> None: ...

class GetAllGatewayResourceProfilesRequest(_message.Message):
    __slots__ = ()
    def __init__(self) -> None: ...

class GetAllGatewayResourceProfilesResponse(_message.Message):
    __slots__ = ("gateway_resource_profiles",)
    GATEWAY_RESOURCE_PROFILES_FIELD_NUMBER: _ClassVar[int]
    gateway_resource_profiles: _containers.RepeatedCompositeFieldContainer[_gateway_profile_pb2.GatewayResourceProfile]
    def __init__(self, gateway_resource_profiles: _Optional[_Iterable[_Union[_gateway_profile_pb2.GatewayResourceProfile, _Mapping]]] = ...) -> None: ...

class AddComputePreferenceRequest(_message.Message):
    __slots__ = ("gateway_id", "compute_resource_id", "compute_resource_preference")
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    COMPUTE_RESOURCE_PREFERENCE_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    compute_resource_id: str
    compute_resource_preference: _gateway_profile_pb2.ComputeResourcePreference
    def __init__(self, gateway_id: _Optional[str] = ..., compute_resource_id: _Optional[str] = ..., compute_resource_preference: _Optional[_Union[_gateway_profile_pb2.ComputeResourcePreference, _Mapping]] = ...) -> None: ...

class GetComputePreferenceRequest(_message.Message):
    __slots__ = ("gateway_id", "compute_resource_id")
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    compute_resource_id: str
    def __init__(self, gateway_id: _Optional[str] = ..., compute_resource_id: _Optional[str] = ...) -> None: ...

class UpdateComputePreferenceRequest(_message.Message):
    __slots__ = ("gateway_id", "compute_resource_id", "compute_resource_preference")
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    COMPUTE_RESOURCE_PREFERENCE_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    compute_resource_id: str
    compute_resource_preference: _gateway_profile_pb2.ComputeResourcePreference
    def __init__(self, gateway_id: _Optional[str] = ..., compute_resource_id: _Optional[str] = ..., compute_resource_preference: _Optional[_Union[_gateway_profile_pb2.ComputeResourcePreference, _Mapping]] = ...) -> None: ...

class DeleteComputePreferenceRequest(_message.Message):
    __slots__ = ("gateway_id", "compute_resource_id")
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    compute_resource_id: str
    def __init__(self, gateway_id: _Optional[str] = ..., compute_resource_id: _Optional[str] = ...) -> None: ...

class GetAllComputePreferencesRequest(_message.Message):
    __slots__ = ("gateway_id",)
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    def __init__(self, gateway_id: _Optional[str] = ...) -> None: ...

class GetAllComputePreferencesResponse(_message.Message):
    __slots__ = ("compute_resource_preferences",)
    COMPUTE_RESOURCE_PREFERENCES_FIELD_NUMBER: _ClassVar[int]
    compute_resource_preferences: _containers.RepeatedCompositeFieldContainer[_gateway_profile_pb2.ComputeResourcePreference]
    def __init__(self, compute_resource_preferences: _Optional[_Iterable[_Union[_gateway_profile_pb2.ComputeResourcePreference, _Mapping]]] = ...) -> None: ...

class AddStoragePreferenceRequest(_message.Message):
    __slots__ = ("gateway_id", "storage_resource_id", "storage_preference")
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    STORAGE_PREFERENCE_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    storage_resource_id: str
    storage_preference: _gateway_profile_pb2.StoragePreference
    def __init__(self, gateway_id: _Optional[str] = ..., storage_resource_id: _Optional[str] = ..., storage_preference: _Optional[_Union[_gateway_profile_pb2.StoragePreference, _Mapping]] = ...) -> None: ...

class GetStoragePreferenceRequest(_message.Message):
    __slots__ = ("gateway_id", "storage_resource_id")
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    storage_resource_id: str
    def __init__(self, gateway_id: _Optional[str] = ..., storage_resource_id: _Optional[str] = ...) -> None: ...

class UpdateStoragePreferenceRequest(_message.Message):
    __slots__ = ("gateway_id", "storage_resource_id", "storage_preference")
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    STORAGE_PREFERENCE_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    storage_resource_id: str
    storage_preference: _gateway_profile_pb2.StoragePreference
    def __init__(self, gateway_id: _Optional[str] = ..., storage_resource_id: _Optional[str] = ..., storage_preference: _Optional[_Union[_gateway_profile_pb2.StoragePreference, _Mapping]] = ...) -> None: ...

class DeleteStoragePreferenceRequest(_message.Message):
    __slots__ = ("gateway_id", "storage_resource_id")
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    storage_resource_id: str
    def __init__(self, gateway_id: _Optional[str] = ..., storage_resource_id: _Optional[str] = ...) -> None: ...

class GetAllStoragePreferencesRequest(_message.Message):
    __slots__ = ("gateway_id",)
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    def __init__(self, gateway_id: _Optional[str] = ...) -> None: ...

class GetAllStoragePreferencesResponse(_message.Message):
    __slots__ = ("storage_preferences",)
    STORAGE_PREFERENCES_FIELD_NUMBER: _ClassVar[int]
    storage_preferences: _containers.RepeatedCompositeFieldContainer[_gateway_profile_pb2.StoragePreference]
    def __init__(self, storage_preferences: _Optional[_Iterable[_Union[_gateway_profile_pb2.StoragePreference, _Mapping]]] = ...) -> None: ...

class GetSSHAccountProvisionersRequest(_message.Message):
    __slots__ = ()
    def __init__(self) -> None: ...

class GetSSHAccountProvisionersResponse(_message.Message):
    __slots__ = ("ssh_account_provisioners",)
    SSH_ACCOUNT_PROVISIONERS_FIELD_NUMBER: _ClassVar[int]
    ssh_account_provisioners: _containers.RepeatedCompositeFieldContainer[_account_provisioning_pb2.SSHAccountProvisioner]
    def __init__(self, ssh_account_provisioners: _Optional[_Iterable[_Union[_account_provisioning_pb2.SSHAccountProvisioner, _Mapping]]] = ...) -> None: ...
