from google.api import annotations_pb2 as _annotations_pb2
from google.protobuf import empty_pb2 as _empty_pb2
from org.apache.airavata.model.appcatalog.groupresourceprofile import group_resource_profile_pb2 as _group_resource_profile_pb2
from org.apache.airavata.model.appcatalog.gatewaygroups import gateway_groups_pb2 as _gateway_groups_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class CreateGroupResourceProfileRequest(_message.Message):
    __slots__ = ("group_resource_profile",)
    GROUP_RESOURCE_PROFILE_FIELD_NUMBER: _ClassVar[int]
    group_resource_profile: _group_resource_profile_pb2.GroupResourceProfile
    def __init__(self, group_resource_profile: _Optional[_Union[_group_resource_profile_pb2.GroupResourceProfile, _Mapping]] = ...) -> None: ...

class GetGroupResourceProfileRequest(_message.Message):
    __slots__ = ("group_resource_profile_id",)
    GROUP_RESOURCE_PROFILE_ID_FIELD_NUMBER: _ClassVar[int]
    group_resource_profile_id: str
    def __init__(self, group_resource_profile_id: _Optional[str] = ...) -> None: ...

class UpdateGroupResourceProfileRequest(_message.Message):
    __slots__ = ("group_resource_profile_id", "group_resource_profile")
    GROUP_RESOURCE_PROFILE_ID_FIELD_NUMBER: _ClassVar[int]
    GROUP_RESOURCE_PROFILE_FIELD_NUMBER: _ClassVar[int]
    group_resource_profile_id: str
    group_resource_profile: _group_resource_profile_pb2.GroupResourceProfile
    def __init__(self, group_resource_profile_id: _Optional[str] = ..., group_resource_profile: _Optional[_Union[_group_resource_profile_pb2.GroupResourceProfile, _Mapping]] = ...) -> None: ...

class RemoveGroupResourceProfileRequest(_message.Message):
    __slots__ = ("group_resource_profile_id",)
    GROUP_RESOURCE_PROFILE_ID_FIELD_NUMBER: _ClassVar[int]
    group_resource_profile_id: str
    def __init__(self, group_resource_profile_id: _Optional[str] = ...) -> None: ...

class GetGroupResourceListRequest(_message.Message):
    __slots__ = ()
    def __init__(self) -> None: ...

class GetGroupResourceListResponse(_message.Message):
    __slots__ = ("group_resource_profiles",)
    GROUP_RESOURCE_PROFILES_FIELD_NUMBER: _ClassVar[int]
    group_resource_profiles: _containers.RepeatedCompositeFieldContainer[_group_resource_profile_pb2.GroupResourceProfile]
    def __init__(self, group_resource_profiles: _Optional[_Iterable[_Union[_group_resource_profile_pb2.GroupResourceProfile, _Mapping]]] = ...) -> None: ...

class GetGroupComputePreferenceRequest(_message.Message):
    __slots__ = ("group_resource_profile_id", "compute_resource_id")
    GROUP_RESOURCE_PROFILE_ID_FIELD_NUMBER: _ClassVar[int]
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    group_resource_profile_id: str
    compute_resource_id: str
    def __init__(self, group_resource_profile_id: _Optional[str] = ..., compute_resource_id: _Optional[str] = ...) -> None: ...

class RemoveGroupComputePrefsRequest(_message.Message):
    __slots__ = ("group_resource_profile_id", "compute_resource_id")
    GROUP_RESOURCE_PROFILE_ID_FIELD_NUMBER: _ClassVar[int]
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    group_resource_profile_id: str
    compute_resource_id: str
    def __init__(self, group_resource_profile_id: _Optional[str] = ..., compute_resource_id: _Optional[str] = ...) -> None: ...

class GetGroupComputePrefListRequest(_message.Message):
    __slots__ = ("group_resource_profile_id",)
    GROUP_RESOURCE_PROFILE_ID_FIELD_NUMBER: _ClassVar[int]
    group_resource_profile_id: str
    def __init__(self, group_resource_profile_id: _Optional[str] = ...) -> None: ...

class GetGroupComputePrefListResponse(_message.Message):
    __slots__ = ("group_compute_resource_preferences",)
    GROUP_COMPUTE_RESOURCE_PREFERENCES_FIELD_NUMBER: _ClassVar[int]
    group_compute_resource_preferences: _containers.RepeatedCompositeFieldContainer[_group_resource_profile_pb2.GroupComputeResourcePreference]
    def __init__(self, group_compute_resource_preferences: _Optional[_Iterable[_Union[_group_resource_profile_pb2.GroupComputeResourcePreference, _Mapping]]] = ...) -> None: ...

class GetGroupComputeResourcePolicyRequest(_message.Message):
    __slots__ = ("resource_policy_id",)
    RESOURCE_POLICY_ID_FIELD_NUMBER: _ClassVar[int]
    resource_policy_id: str
    def __init__(self, resource_policy_id: _Optional[str] = ...) -> None: ...

class RemoveGroupComputeResourcePolicyRequest(_message.Message):
    __slots__ = ("resource_policy_id",)
    RESOURCE_POLICY_ID_FIELD_NUMBER: _ClassVar[int]
    resource_policy_id: str
    def __init__(self, resource_policy_id: _Optional[str] = ...) -> None: ...

class GetGroupComputeResourcePolicyListRequest(_message.Message):
    __slots__ = ("group_resource_profile_id",)
    GROUP_RESOURCE_PROFILE_ID_FIELD_NUMBER: _ClassVar[int]
    group_resource_profile_id: str
    def __init__(self, group_resource_profile_id: _Optional[str] = ...) -> None: ...

class GetGroupComputeResourcePolicyListResponse(_message.Message):
    __slots__ = ("compute_resource_policies",)
    COMPUTE_RESOURCE_POLICIES_FIELD_NUMBER: _ClassVar[int]
    compute_resource_policies: _containers.RepeatedCompositeFieldContainer[_group_resource_profile_pb2.ComputeResourcePolicy]
    def __init__(self, compute_resource_policies: _Optional[_Iterable[_Union[_group_resource_profile_pb2.ComputeResourcePolicy, _Mapping]]] = ...) -> None: ...

class GetBatchQueueResourcePolicyRequest(_message.Message):
    __slots__ = ("resource_policy_id",)
    RESOURCE_POLICY_ID_FIELD_NUMBER: _ClassVar[int]
    resource_policy_id: str
    def __init__(self, resource_policy_id: _Optional[str] = ...) -> None: ...

class RemoveGroupBatchQueueResourcePolicyRequest(_message.Message):
    __slots__ = ("resource_policy_id",)
    RESOURCE_POLICY_ID_FIELD_NUMBER: _ClassVar[int]
    resource_policy_id: str
    def __init__(self, resource_policy_id: _Optional[str] = ...) -> None: ...

class GetGroupBatchQueuePolicyListRequest(_message.Message):
    __slots__ = ("group_resource_profile_id",)
    GROUP_RESOURCE_PROFILE_ID_FIELD_NUMBER: _ClassVar[int]
    group_resource_profile_id: str
    def __init__(self, group_resource_profile_id: _Optional[str] = ...) -> None: ...

class GetGroupBatchQueuePolicyListResponse(_message.Message):
    __slots__ = ("batch_queue_resource_policies",)
    BATCH_QUEUE_RESOURCE_POLICIES_FIELD_NUMBER: _ClassVar[int]
    batch_queue_resource_policies: _containers.RepeatedCompositeFieldContainer[_group_resource_profile_pb2.BatchQueueResourcePolicy]
    def __init__(self, batch_queue_resource_policies: _Optional[_Iterable[_Union[_group_resource_profile_pb2.BatchQueueResourcePolicy, _Mapping]]] = ...) -> None: ...

class GetGatewayGroupsRequest(_message.Message):
    __slots__ = ()
    def __init__(self) -> None: ...
