from google.api import annotations_pb2 as _annotations_pb2
from google.protobuf import struct_pb2 as _struct_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class CreateUserRequest(_message.Message):
    __slots__ = ("userName", "firstName", "lastName", "email", "avatar")
    USERNAME_FIELD_NUMBER: _ClassVar[int]
    FIRSTNAME_FIELD_NUMBER: _ClassVar[int]
    LASTNAME_FIELD_NUMBER: _ClassVar[int]
    EMAIL_FIELD_NUMBER: _ClassVar[int]
    AVATAR_FIELD_NUMBER: _ClassVar[int]
    userName: str
    firstName: str
    lastName: str
    email: str
    avatar: str
    def __init__(self, userName: _Optional[str] = ..., firstName: _Optional[str] = ..., lastName: _Optional[str] = ..., email: _Optional[str] = ..., avatar: _Optional[str] = ...) -> None: ...

class BoolResponse(_message.Message):
    __slots__ = ("value",)
    VALUE_FIELD_NUMBER: _ClassVar[int]
    value: bool
    def __init__(self, value: bool = ...) -> None: ...

class JsonResponse(_message.Message):
    __slots__ = ("json",)
    JSON_FIELD_NUMBER: _ClassVar[int]
    json: str
    def __init__(self, json: _Optional[str] = ...) -> None: ...

class JsonListResponse(_message.Message):
    __slots__ = ("items",)
    ITEMS_FIELD_NUMBER: _ClassVar[int]
    items: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, items: _Optional[_Iterable[str]] = ...) -> None: ...

class CreateResearchProjectRequest(_message.Message):
    __slots__ = ("name", "owner_id", "repository_id", "dataset_ids")
    NAME_FIELD_NUMBER: _ClassVar[int]
    OWNER_ID_FIELD_NUMBER: _ClassVar[int]
    REPOSITORY_ID_FIELD_NUMBER: _ClassVar[int]
    DATASET_IDS_FIELD_NUMBER: _ClassVar[int]
    name: str
    owner_id: str
    repository_id: str
    dataset_ids: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, name: _Optional[str] = ..., owner_id: _Optional[str] = ..., repository_id: _Optional[str] = ..., dataset_ids: _Optional[_Iterable[str]] = ...) -> None: ...

class GetProjectsByOwnerRequest(_message.Message):
    __slots__ = ("owner_id",)
    OWNER_ID_FIELD_NUMBER: _ClassVar[int]
    owner_id: str
    def __init__(self, owner_id: _Optional[str] = ...) -> None: ...

class DeleteProjectRequest(_message.Message):
    __slots__ = ("project_id",)
    PROJECT_ID_FIELD_NUMBER: _ClassVar[int]
    project_id: str
    def __init__(self, project_id: _Optional[str] = ...) -> None: ...

class GetAllProjectsRequest(_message.Message):
    __slots__ = ()
    def __init__(self) -> None: ...

class CreateResourceRequest(_message.Message):
    __slots__ = ("name", "description", "header_image", "tags", "authors", "privacy")
    NAME_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    HEADER_IMAGE_FIELD_NUMBER: _ClassVar[int]
    TAGS_FIELD_NUMBER: _ClassVar[int]
    AUTHORS_FIELD_NUMBER: _ClassVar[int]
    PRIVACY_FIELD_NUMBER: _ClassVar[int]
    name: str
    description: str
    header_image: str
    tags: _containers.RepeatedScalarFieldContainer[str]
    authors: _containers.RepeatedScalarFieldContainer[str]
    privacy: str
    def __init__(self, name: _Optional[str] = ..., description: _Optional[str] = ..., header_image: _Optional[str] = ..., tags: _Optional[_Iterable[str]] = ..., authors: _Optional[_Iterable[str]] = ..., privacy: _Optional[str] = ...) -> None: ...

class ModifyResourceRequest(_message.Message):
    __slots__ = ("id", "name", "description", "header_image", "tags", "authors", "privacy")
    ID_FIELD_NUMBER: _ClassVar[int]
    NAME_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    HEADER_IMAGE_FIELD_NUMBER: _ClassVar[int]
    TAGS_FIELD_NUMBER: _ClassVar[int]
    AUTHORS_FIELD_NUMBER: _ClassVar[int]
    PRIVACY_FIELD_NUMBER: _ClassVar[int]
    id: str
    name: str
    description: str
    header_image: str
    tags: _containers.RepeatedScalarFieldContainer[str]
    authors: _containers.RepeatedScalarFieldContainer[str]
    privacy: str
    def __init__(self, id: _Optional[str] = ..., name: _Optional[str] = ..., description: _Optional[str] = ..., header_image: _Optional[str] = ..., tags: _Optional[_Iterable[str]] = ..., authors: _Optional[_Iterable[str]] = ..., privacy: _Optional[str] = ...) -> None: ...

class CreateRepositoryResourceRequest(_message.Message):
    __slots__ = ("resource", "github_url")
    RESOURCE_FIELD_NUMBER: _ClassVar[int]
    GITHUB_URL_FIELD_NUMBER: _ClassVar[int]
    resource: CreateResourceRequest
    github_url: str
    def __init__(self, resource: _Optional[_Union[CreateResourceRequest, _Mapping]] = ..., github_url: _Optional[str] = ...) -> None: ...

class ResourceIdRequest(_message.Message):
    __slots__ = ("id",)
    ID_FIELD_NUMBER: _ClassVar[int]
    id: str
    def __init__(self, id: _Optional[str] = ...) -> None: ...

class GetAllResourcesRequest(_message.Message):
    __slots__ = ("page_number", "page_size", "name_search", "types", "tags")
    PAGE_NUMBER_FIELD_NUMBER: _ClassVar[int]
    PAGE_SIZE_FIELD_NUMBER: _ClassVar[int]
    NAME_SEARCH_FIELD_NUMBER: _ClassVar[int]
    TYPES_FIELD_NUMBER: _ClassVar[int]
    TAGS_FIELD_NUMBER: _ClassVar[int]
    page_number: int
    page_size: int
    name_search: str
    types: _containers.RepeatedScalarFieldContainer[str]
    tags: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, page_number: _Optional[int] = ..., page_size: _Optional[int] = ..., name_search: _Optional[str] = ..., types: _Optional[_Iterable[str]] = ..., tags: _Optional[_Iterable[str]] = ...) -> None: ...

class SearchResourceRequest(_message.Message):
    __slots__ = ("type", "name")
    TYPE_FIELD_NUMBER: _ClassVar[int]
    NAME_FIELD_NUMBER: _ClassVar[int]
    type: str
    name: str
    def __init__(self, type: _Optional[str] = ..., name: _Optional[str] = ...) -> None: ...

class StarResourceRequest(_message.Message):
    __slots__ = ("id",)
    ID_FIELD_NUMBER: _ClassVar[int]
    id: str
    def __init__(self, id: _Optional[str] = ...) -> None: ...

class GetStarredResourcesRequest(_message.Message):
    __slots__ = ("user_id",)
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    user_id: str
    def __init__(self, user_id: _Optional[str] = ...) -> None: ...

class GetResourceStarCountRequest(_message.Message):
    __slots__ = ("id",)
    ID_FIELD_NUMBER: _ClassVar[int]
    id: str
    def __init__(self, id: _Optional[str] = ...) -> None: ...

class StarCountResponse(_message.Message):
    __slots__ = ("count",)
    COUNT_FIELD_NUMBER: _ClassVar[int]
    count: int
    def __init__(self, count: _Optional[int] = ...) -> None: ...

class GetSessionsRequest(_message.Message):
    __slots__ = ("status",)
    STATUS_FIELD_NUMBER: _ClassVar[int]
    status: str
    def __init__(self, status: _Optional[str] = ...) -> None: ...

class UpdateSessionStatusRequest(_message.Message):
    __slots__ = ("session_id", "status")
    SESSION_ID_FIELD_NUMBER: _ClassVar[int]
    STATUS_FIELD_NUMBER: _ClassVar[int]
    session_id: str
    status: str
    def __init__(self, session_id: _Optional[str] = ..., status: _Optional[str] = ...) -> None: ...

class DeleteSessionRequest(_message.Message):
    __slots__ = ("session_id",)
    SESSION_ID_FIELD_NUMBER: _ClassVar[int]
    session_id: str
    def __init__(self, session_id: _Optional[str] = ...) -> None: ...

class DeleteSessionsRequest(_message.Message):
    __slots__ = ("session_ids",)
    SESSION_IDS_FIELD_NUMBER: _ClassVar[int]
    session_ids: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, session_ids: _Optional[_Iterable[str]] = ...) -> None: ...

class StartProjectSessionRequest(_message.Message):
    __slots__ = ("project_id", "session_name")
    PROJECT_ID_FIELD_NUMBER: _ClassVar[int]
    SESSION_NAME_FIELD_NUMBER: _ClassVar[int]
    project_id: str
    session_name: str
    def __init__(self, project_id: _Optional[str] = ..., session_name: _Optional[str] = ...) -> None: ...

class ResumeSessionRequest(_message.Message):
    __slots__ = ("session_id",)
    SESSION_ID_FIELD_NUMBER: _ClassVar[int]
    session_id: str
    def __init__(self, session_id: _Optional[str] = ...) -> None: ...

class RedirectResponse(_message.Message):
    __slots__ = ("redirect_url",)
    REDIRECT_URL_FIELD_NUMBER: _ClassVar[int]
    redirect_url: str
    def __init__(self, redirect_url: _Optional[str] = ...) -> None: ...
