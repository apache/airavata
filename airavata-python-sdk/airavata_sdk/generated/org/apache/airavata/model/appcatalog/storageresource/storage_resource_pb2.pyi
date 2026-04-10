from org.apache.airavata.model.data.movement import data_movement_pb2 as _data_movement_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class StorageResourceDescription(_message.Message):
    __slots__ = ("storage_resource_id", "host_name", "storage_resource_description", "enabled", "data_movement_interfaces", "creation_time", "update_time")
    STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    HOST_NAME_FIELD_NUMBER: _ClassVar[int]
    STORAGE_RESOURCE_DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    ENABLED_FIELD_NUMBER: _ClassVar[int]
    DATA_MOVEMENT_INTERFACES_FIELD_NUMBER: _ClassVar[int]
    CREATION_TIME_FIELD_NUMBER: _ClassVar[int]
    UPDATE_TIME_FIELD_NUMBER: _ClassVar[int]
    storage_resource_id: str
    host_name: str
    storage_resource_description: str
    enabled: bool
    data_movement_interfaces: _containers.RepeatedCompositeFieldContainer[_data_movement_pb2.DataMovementInterface]
    creation_time: int
    update_time: int
    def __init__(self, storage_resource_id: _Optional[str] = ..., host_name: _Optional[str] = ..., storage_resource_description: _Optional[str] = ..., enabled: bool = ..., data_movement_interfaces: _Optional[_Iterable[_Union[_data_movement_pb2.DataMovementInterface, _Mapping]]] = ..., creation_time: _Optional[int] = ..., update_time: _Optional[int] = ...) -> None: ...

class StorageVolumeInfo(_message.Message):
    __slots__ = ("total_size", "used_size", "available_size", "total_size_byte_count", "used_size_byte_count", "available_size_byte_count", "percentage_used", "mount_point", "filesystem_type")
    TOTAL_SIZE_FIELD_NUMBER: _ClassVar[int]
    USED_SIZE_FIELD_NUMBER: _ClassVar[int]
    AVAILABLE_SIZE_FIELD_NUMBER: _ClassVar[int]
    TOTAL_SIZE_BYTE_COUNT_FIELD_NUMBER: _ClassVar[int]
    USED_SIZE_BYTE_COUNT_FIELD_NUMBER: _ClassVar[int]
    AVAILABLE_SIZE_BYTE_COUNT_FIELD_NUMBER: _ClassVar[int]
    PERCENTAGE_USED_FIELD_NUMBER: _ClassVar[int]
    MOUNT_POINT_FIELD_NUMBER: _ClassVar[int]
    FILESYSTEM_TYPE_FIELD_NUMBER: _ClassVar[int]
    total_size: str
    used_size: str
    available_size: str
    total_size_byte_count: int
    used_size_byte_count: int
    available_size_byte_count: int
    percentage_used: float
    mount_point: str
    filesystem_type: str
    def __init__(self, total_size: _Optional[str] = ..., used_size: _Optional[str] = ..., available_size: _Optional[str] = ..., total_size_byte_count: _Optional[int] = ..., used_size_byte_count: _Optional[int] = ..., available_size_byte_count: _Optional[int] = ..., percentage_used: _Optional[float] = ..., mount_point: _Optional[str] = ..., filesystem_type: _Optional[str] = ...) -> None: ...

class StorageDirectoryInfo(_message.Message):
    __slots__ = ("total_size", "total_size_byte_count")
    TOTAL_SIZE_FIELD_NUMBER: _ClassVar[int]
    TOTAL_SIZE_BYTE_COUNT_FIELD_NUMBER: _ClassVar[int]
    total_size: str
    total_size_byte_count: int
    def __init__(self, total_size: _Optional[str] = ..., total_size_byte_count: _Optional[int] = ...) -> None: ...
