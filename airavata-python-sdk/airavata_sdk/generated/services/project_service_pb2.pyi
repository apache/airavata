from google.api import annotations_pb2 as _annotations_pb2
from google.protobuf import empty_pb2 as _empty_pb2
from org.apache.airavata.model.workspace import workspace_pb2 as _workspace_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class CreateProjectRequest(_message.Message):
    __slots__ = ("gateway_id", "project")
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    PROJECT_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    project: _workspace_pb2.Project
    def __init__(self, gateway_id: _Optional[str] = ..., project: _Optional[_Union[_workspace_pb2.Project, _Mapping]] = ...) -> None: ...

class CreateProjectResponse(_message.Message):
    __slots__ = ("project_id",)
    PROJECT_ID_FIELD_NUMBER: _ClassVar[int]
    project_id: str
    def __init__(self, project_id: _Optional[str] = ...) -> None: ...

class GetProjectRequest(_message.Message):
    __slots__ = ("project_id",)
    PROJECT_ID_FIELD_NUMBER: _ClassVar[int]
    project_id: str
    def __init__(self, project_id: _Optional[str] = ...) -> None: ...

class UpdateProjectRequest(_message.Message):
    __slots__ = ("project_id", "project")
    PROJECT_ID_FIELD_NUMBER: _ClassVar[int]
    PROJECT_FIELD_NUMBER: _ClassVar[int]
    project_id: str
    project: _workspace_pb2.Project
    def __init__(self, project_id: _Optional[str] = ..., project: _Optional[_Union[_workspace_pb2.Project, _Mapping]] = ...) -> None: ...

class DeleteProjectRequest(_message.Message):
    __slots__ = ("project_id",)
    PROJECT_ID_FIELD_NUMBER: _ClassVar[int]
    project_id: str
    def __init__(self, project_id: _Optional[str] = ...) -> None: ...

class GetUserProjectsRequest(_message.Message):
    __slots__ = ("gateway_id", "user_name", "limit", "offset")
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    USER_NAME_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    OFFSET_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    user_name: str
    limit: int
    offset: int
    def __init__(self, gateway_id: _Optional[str] = ..., user_name: _Optional[str] = ..., limit: _Optional[int] = ..., offset: _Optional[int] = ...) -> None: ...

class GetUserProjectsResponse(_message.Message):
    __slots__ = ("projects",)
    PROJECTS_FIELD_NUMBER: _ClassVar[int]
    projects: _containers.RepeatedCompositeFieldContainer[_workspace_pb2.Project]
    def __init__(self, projects: _Optional[_Iterable[_Union[_workspace_pb2.Project, _Mapping]]] = ...) -> None: ...

class SearchProjectsRequest(_message.Message):
    __slots__ = ("gateway_id", "user_name", "filters", "limit", "offset")
    class FiltersEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: str
        def __init__(self, key: _Optional[str] = ..., value: _Optional[str] = ...) -> None: ...
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    USER_NAME_FIELD_NUMBER: _ClassVar[int]
    FILTERS_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    OFFSET_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    user_name: str
    filters: _containers.ScalarMap[str, str]
    limit: int
    offset: int
    def __init__(self, gateway_id: _Optional[str] = ..., user_name: _Optional[str] = ..., filters: _Optional[_Mapping[str, str]] = ..., limit: _Optional[int] = ..., offset: _Optional[int] = ...) -> None: ...

class SearchProjectsResponse(_message.Message):
    __slots__ = ("projects",)
    PROJECTS_FIELD_NUMBER: _ClassVar[int]
    projects: _containers.RepeatedCompositeFieldContainer[_workspace_pb2.Project]
    def __init__(self, projects: _Optional[_Iterable[_Union[_workspace_pb2.Project, _Mapping]]] = ...) -> None: ...
