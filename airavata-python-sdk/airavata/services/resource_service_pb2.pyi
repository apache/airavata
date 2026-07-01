from google.api import annotations_pb2 as _annotations_pb2
from google.protobuf import empty_pb2 as _empty_pb2
from airavata.model.appcatalog.computeresource import compute_resource_pb2 as _compute_resource_pb2
from airavata.model.appcatalog.storageresource import storage_resource_pb2 as _storage_resource_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class RegisterComputeResourceRequest(_message.Message):
    __slots__ = ("compute_resource",)
    COMPUTE_RESOURCE_FIELD_NUMBER: _ClassVar[int]
    compute_resource: _compute_resource_pb2.ComputeResourceDescription
    def __init__(self, compute_resource: _Optional[_Union[_compute_resource_pb2.ComputeResourceDescription, _Mapping]] = ...) -> None: ...

class RegisterComputeResourceResponse(_message.Message):
    __slots__ = ("compute_resource_id",)
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    compute_resource_id: str
    def __init__(self, compute_resource_id: _Optional[str] = ...) -> None: ...

class GetComputeResourceRequest(_message.Message):
    __slots__ = ("compute_resource_id",)
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    compute_resource_id: str
    def __init__(self, compute_resource_id: _Optional[str] = ...) -> None: ...

class UpdateComputeResourceRequest(_message.Message):
    __slots__ = ("compute_resource_id", "compute_resource")
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    COMPUTE_RESOURCE_FIELD_NUMBER: _ClassVar[int]
    compute_resource_id: str
    compute_resource: _compute_resource_pb2.ComputeResourceDescription
    def __init__(self, compute_resource_id: _Optional[str] = ..., compute_resource: _Optional[_Union[_compute_resource_pb2.ComputeResourceDescription, _Mapping]] = ...) -> None: ...

class DeleteComputeResourceRequest(_message.Message):
    __slots__ = ("compute_resource_id",)
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    compute_resource_id: str
    def __init__(self, compute_resource_id: _Optional[str] = ...) -> None: ...

class GetAllComputeResourceNamesRequest(_message.Message):
    __slots__ = ()
    def __init__(self) -> None: ...

class GetAllComputeResourceNamesResponse(_message.Message):
    __slots__ = ("compute_resource_names",)
    class ComputeResourceNamesEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: str
        def __init__(self, key: _Optional[str] = ..., value: _Optional[str] = ...) -> None: ...
    COMPUTE_RESOURCE_NAMES_FIELD_NUMBER: _ClassVar[int]
    compute_resource_names: _containers.ScalarMap[str, str]
    def __init__(self, compute_resource_names: _Optional[_Mapping[str, str]] = ...) -> None: ...

class RegisterStorageResourceRequest(_message.Message):
    __slots__ = ("storage_resource",)
    STORAGE_RESOURCE_FIELD_NUMBER: _ClassVar[int]
    storage_resource: _storage_resource_pb2.StorageResourceDescription
    def __init__(self, storage_resource: _Optional[_Union[_storage_resource_pb2.StorageResourceDescription, _Mapping]] = ...) -> None: ...

class RegisterStorageResourceResponse(_message.Message):
    __slots__ = ("storage_resource_id",)
    STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    storage_resource_id: str
    def __init__(self, storage_resource_id: _Optional[str] = ...) -> None: ...

class GetStorageResourceRequest(_message.Message):
    __slots__ = ("storage_resource_id",)
    STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    storage_resource_id: str
    def __init__(self, storage_resource_id: _Optional[str] = ...) -> None: ...

class UpdateStorageResourceRequest(_message.Message):
    __slots__ = ("storage_resource_id", "storage_resource")
    STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    STORAGE_RESOURCE_FIELD_NUMBER: _ClassVar[int]
    storage_resource_id: str
    storage_resource: _storage_resource_pb2.StorageResourceDescription
    def __init__(self, storage_resource_id: _Optional[str] = ..., storage_resource: _Optional[_Union[_storage_resource_pb2.StorageResourceDescription, _Mapping]] = ...) -> None: ...

class DeleteStorageResourceRequest(_message.Message):
    __slots__ = ("storage_resource_id",)
    STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    storage_resource_id: str
    def __init__(self, storage_resource_id: _Optional[str] = ...) -> None: ...

class GetAllStorageResourceNamesRequest(_message.Message):
    __slots__ = ()
    def __init__(self) -> None: ...

class GetAllStorageResourceNamesResponse(_message.Message):
    __slots__ = ("storage_resource_names",)
    class StorageResourceNamesEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: str
        def __init__(self, key: _Optional[str] = ..., value: _Optional[str] = ...) -> None: ...
    STORAGE_RESOURCE_NAMES_FIELD_NUMBER: _ClassVar[int]
    storage_resource_names: _containers.ScalarMap[str, str]
    def __init__(self, storage_resource_names: _Optional[_Mapping[str, str]] = ...) -> None: ...

class DeleteBatchQueueRequest(_message.Message):
    __slots__ = ("compute_resource_id", "queue_name")
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    QUEUE_NAME_FIELD_NUMBER: _ClassVar[int]
    compute_resource_id: str
    queue_name: str
    def __init__(self, compute_resource_id: _Optional[str] = ..., queue_name: _Optional[str] = ...) -> None: ...
