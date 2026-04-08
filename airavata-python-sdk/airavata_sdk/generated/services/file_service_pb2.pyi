from google.api import annotations_pb2 as _annotations_pb2
from google.protobuf import empty_pb2 as _empty_pb2
from org.apache.airavata.model.data.replica import replica_catalog_pb2 as _replica_catalog_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class FileUploadResponse(_message.Message):
    __slots__ = ("name", "uri", "type", "size")
    NAME_FIELD_NUMBER: _ClassVar[int]
    URI_FIELD_NUMBER: _ClassVar[int]
    TYPE_FIELD_NUMBER: _ClassVar[int]
    SIZE_FIELD_NUMBER: _ClassVar[int]
    name: str
    uri: str
    type: str
    size: int
    def __init__(self, name: _Optional[str] = ..., uri: _Optional[str] = ..., type: _Optional[str] = ..., size: _Optional[int] = ...) -> None: ...

class FileMetadataResponse(_message.Message):
    __slots__ = ("name", "path", "size", "created_time", "modified_time", "is_directory", "content_type", "data_product_uri")
    NAME_FIELD_NUMBER: _ClassVar[int]
    PATH_FIELD_NUMBER: _ClassVar[int]
    SIZE_FIELD_NUMBER: _ClassVar[int]
    CREATED_TIME_FIELD_NUMBER: _ClassVar[int]
    MODIFIED_TIME_FIELD_NUMBER: _ClassVar[int]
    IS_DIRECTORY_FIELD_NUMBER: _ClassVar[int]
    CONTENT_TYPE_FIELD_NUMBER: _ClassVar[int]
    DATA_PRODUCT_URI_FIELD_NUMBER: _ClassVar[int]
    name: str
    path: str
    size: int
    created_time: int
    modified_time: int
    is_directory: bool
    content_type: str
    data_product_uri: str
    def __init__(self, name: _Optional[str] = ..., path: _Optional[str] = ..., size: _Optional[int] = ..., created_time: _Optional[int] = ..., modified_time: _Optional[int] = ..., is_directory: bool = ..., content_type: _Optional[str] = ..., data_product_uri: _Optional[str] = ...) -> None: ...

class UploadFileRequest(_message.Message):
    __slots__ = ("storage_resource_id", "path", "name", "content_type", "content")
    STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    PATH_FIELD_NUMBER: _ClassVar[int]
    NAME_FIELD_NUMBER: _ClassVar[int]
    CONTENT_TYPE_FIELD_NUMBER: _ClassVar[int]
    CONTENT_FIELD_NUMBER: _ClassVar[int]
    storage_resource_id: str
    path: str
    name: str
    content_type: str
    content: bytes
    def __init__(self, storage_resource_id: _Optional[str] = ..., path: _Optional[str] = ..., name: _Optional[str] = ..., content_type: _Optional[str] = ..., content: _Optional[bytes] = ...) -> None: ...

class DownloadFileRequest(_message.Message):
    __slots__ = ("storage_resource_id", "path")
    STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    PATH_FIELD_NUMBER: _ClassVar[int]
    storage_resource_id: str
    path: str
    def __init__(self, storage_resource_id: _Optional[str] = ..., path: _Optional[str] = ...) -> None: ...

class DownloadFileResponse(_message.Message):
    __slots__ = ("content", "name", "content_type")
    CONTENT_FIELD_NUMBER: _ClassVar[int]
    NAME_FIELD_NUMBER: _ClassVar[int]
    CONTENT_TYPE_FIELD_NUMBER: _ClassVar[int]
    content: bytes
    name: str
    content_type: str
    def __init__(self, content: _Optional[bytes] = ..., name: _Optional[str] = ..., content_type: _Optional[str] = ...) -> None: ...

class FileExistsRequest(_message.Message):
    __slots__ = ("storage_resource_id", "path")
    STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    PATH_FIELD_NUMBER: _ClassVar[int]
    storage_resource_id: str
    path: str
    def __init__(self, storage_resource_id: _Optional[str] = ..., path: _Optional[str] = ...) -> None: ...

class FileExistsResponse(_message.Message):
    __slots__ = ("exists",)
    EXISTS_FIELD_NUMBER: _ClassVar[int]
    exists: bool
    def __init__(self, exists: bool = ...) -> None: ...

class DirExistsRequest(_message.Message):
    __slots__ = ("storage_resource_id", "path")
    STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    PATH_FIELD_NUMBER: _ClassVar[int]
    storage_resource_id: str
    path: str
    def __init__(self, storage_resource_id: _Optional[str] = ..., path: _Optional[str] = ...) -> None: ...

class DirExistsResponse(_message.Message):
    __slots__ = ("exists",)
    EXISTS_FIELD_NUMBER: _ClassVar[int]
    exists: bool
    def __init__(self, exists: bool = ...) -> None: ...

class ListDirRequest(_message.Message):
    __slots__ = ("storage_resource_id", "path")
    STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    PATH_FIELD_NUMBER: _ClassVar[int]
    storage_resource_id: str
    path: str
    def __init__(self, storage_resource_id: _Optional[str] = ..., path: _Optional[str] = ...) -> None: ...

class ListDirResponse(_message.Message):
    __slots__ = ("directories", "files")
    DIRECTORIES_FIELD_NUMBER: _ClassVar[int]
    FILES_FIELD_NUMBER: _ClassVar[int]
    directories: _containers.RepeatedCompositeFieldContainer[FileMetadataResponse]
    files: _containers.RepeatedCompositeFieldContainer[FileMetadataResponse]
    def __init__(self, directories: _Optional[_Iterable[_Union[FileMetadataResponse, _Mapping]]] = ..., files: _Optional[_Iterable[_Union[FileMetadataResponse, _Mapping]]] = ...) -> None: ...

class DeleteFileRequest(_message.Message):
    __slots__ = ("storage_resource_id", "path")
    STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    PATH_FIELD_NUMBER: _ClassVar[int]
    storage_resource_id: str
    path: str
    def __init__(self, storage_resource_id: _Optional[str] = ..., path: _Optional[str] = ...) -> None: ...

class DeleteDirRequest(_message.Message):
    __slots__ = ("storage_resource_id", "path")
    STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    PATH_FIELD_NUMBER: _ClassVar[int]
    storage_resource_id: str
    path: str
    def __init__(self, storage_resource_id: _Optional[str] = ..., path: _Optional[str] = ...) -> None: ...

class MoveFileRequest(_message.Message):
    __slots__ = ("storage_resource_id", "source_path", "destination_path")
    STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    SOURCE_PATH_FIELD_NUMBER: _ClassVar[int]
    DESTINATION_PATH_FIELD_NUMBER: _ClassVar[int]
    storage_resource_id: str
    source_path: str
    destination_path: str
    def __init__(self, storage_resource_id: _Optional[str] = ..., source_path: _Optional[str] = ..., destination_path: _Optional[str] = ...) -> None: ...

class CreateDirRequest(_message.Message):
    __slots__ = ("storage_resource_id", "path")
    STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    PATH_FIELD_NUMBER: _ClassVar[int]
    storage_resource_id: str
    path: str
    def __init__(self, storage_resource_id: _Optional[str] = ..., path: _Optional[str] = ...) -> None: ...

class CreateDirResponse(_message.Message):
    __slots__ = ("created_path",)
    CREATED_PATH_FIELD_NUMBER: _ClassVar[int]
    created_path: str
    def __init__(self, created_path: _Optional[str] = ...) -> None: ...

class CreateSymlinkRequest(_message.Message):
    __slots__ = ("storage_resource_id", "source_path", "target_path")
    STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    SOURCE_PATH_FIELD_NUMBER: _ClassVar[int]
    TARGET_PATH_FIELD_NUMBER: _ClassVar[int]
    storage_resource_id: str
    source_path: str
    target_path: str
    def __init__(self, storage_resource_id: _Optional[str] = ..., source_path: _Optional[str] = ..., target_path: _Optional[str] = ...) -> None: ...

class GetFileMetadataRequest(_message.Message):
    __slots__ = ("storage_resource_id", "path")
    STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    PATH_FIELD_NUMBER: _ClassVar[int]
    storage_resource_id: str
    path: str
    def __init__(self, storage_resource_id: _Optional[str] = ..., path: _Optional[str] = ...) -> None: ...

class ListExperimentDirRequest(_message.Message):
    __slots__ = ("experiment_id",)
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    experiment_id: str
    def __init__(self, experiment_id: _Optional[str] = ...) -> None: ...

class GetDefaultStorageResourceIdRequest(_message.Message):
    __slots__ = ()
    def __init__(self) -> None: ...

class GetDefaultStorageResourceIdResponse(_message.Message):
    __slots__ = ("storage_resource_id",)
    STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    storage_resource_id: str
    def __init__(self, storage_resource_id: _Optional[str] = ...) -> None: ...
