from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class GroupCardinality(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    GROUP_CARDINALITY_UNKNOWN: _ClassVar[GroupCardinality]
    SINGLE_USER: _ClassVar[GroupCardinality]
    MULTI_USER: _ClassVar[GroupCardinality]

class GroupType(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    GROUP_TYPE_UNKNOWN: _ClassVar[GroupType]
    DOMAIN_LEVEL_GROUP: _ClassVar[GroupType]
    USER_LEVEL_GROUP: _ClassVar[GroupType]

class GroupChildType(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    GROUP_CHILD_TYPE_UNKNOWN: _ClassVar[GroupChildType]
    USER: _ClassVar[GroupChildType]
    GROUP: _ClassVar[GroupChildType]

class EntitySearchField(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    ENTITY_SEARCH_FIELD_UNKNOWN: _ClassVar[EntitySearchField]
    NAME: _ClassVar[EntitySearchField]
    DESCRIPTION: _ClassVar[EntitySearchField]
    FULL_TEXT: _ClassVar[EntitySearchField]
    PARRENT_ENTITY_ID: _ClassVar[EntitySearchField]
    OWNER_ID: _ClassVar[EntitySearchField]
    PERMISSION_TYPE_ID: _ClassVar[EntitySearchField]
    CREATED_TIME: _ClassVar[EntitySearchField]
    UPDATED_TIME: _ClassVar[EntitySearchField]
    ENTITY_TYPE_ID: _ClassVar[EntitySearchField]
    SHARED_COUNT: _ClassVar[EntitySearchField]

class SearchCondition(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    SEARCH_CONDITION_UNKNOWN: _ClassVar[SearchCondition]
    EQUAL: _ClassVar[SearchCondition]
    LIKE: _ClassVar[SearchCondition]
    FULL_TEXT_SEARCH: _ClassVar[SearchCondition]
    GTE: _ClassVar[SearchCondition]
    LTE: _ClassVar[SearchCondition]
    NOT: _ClassVar[SearchCondition]

class SharingType(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    SHARING_TYPE_UNKNOWN: _ClassVar[SharingType]
    DIRECT_NON_CASCADING: _ClassVar[SharingType]
    DIRECT_CASCADING: _ClassVar[SharingType]
    INDIRECT_CASCADING: _ClassVar[SharingType]
GROUP_CARDINALITY_UNKNOWN: GroupCardinality
SINGLE_USER: GroupCardinality
MULTI_USER: GroupCardinality
GROUP_TYPE_UNKNOWN: GroupType
DOMAIN_LEVEL_GROUP: GroupType
USER_LEVEL_GROUP: GroupType
GROUP_CHILD_TYPE_UNKNOWN: GroupChildType
USER: GroupChildType
GROUP: GroupChildType
ENTITY_SEARCH_FIELD_UNKNOWN: EntitySearchField
NAME: EntitySearchField
DESCRIPTION: EntitySearchField
FULL_TEXT: EntitySearchField
PARRENT_ENTITY_ID: EntitySearchField
OWNER_ID: EntitySearchField
PERMISSION_TYPE_ID: EntitySearchField
CREATED_TIME: EntitySearchField
UPDATED_TIME: EntitySearchField
ENTITY_TYPE_ID: EntitySearchField
SHARED_COUNT: EntitySearchField
SEARCH_CONDITION_UNKNOWN: SearchCondition
EQUAL: SearchCondition
LIKE: SearchCondition
FULL_TEXT_SEARCH: SearchCondition
GTE: SearchCondition
LTE: SearchCondition
NOT: SearchCondition
SHARING_TYPE_UNKNOWN: SharingType
DIRECT_NON_CASCADING: SharingType
DIRECT_CASCADING: SharingType
INDIRECT_CASCADING: SharingType

class Domain(_message.Message):
    __slots__ = ("domain_id", "name", "description", "created_time", "updated_time", "initial_user_group_id")
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    NAME_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    CREATED_TIME_FIELD_NUMBER: _ClassVar[int]
    UPDATED_TIME_FIELD_NUMBER: _ClassVar[int]
    INITIAL_USER_GROUP_ID_FIELD_NUMBER: _ClassVar[int]
    domain_id: str
    name: str
    description: str
    created_time: int
    updated_time: int
    initial_user_group_id: str
    def __init__(self, domain_id: _Optional[str] = ..., name: _Optional[str] = ..., description: _Optional[str] = ..., created_time: _Optional[int] = ..., updated_time: _Optional[int] = ..., initial_user_group_id: _Optional[str] = ...) -> None: ...

class User(_message.Message):
    __slots__ = ("user_id", "domain_id", "user_name", "first_name", "last_name", "email", "icon", "created_time", "updated_time")
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    USER_NAME_FIELD_NUMBER: _ClassVar[int]
    FIRST_NAME_FIELD_NUMBER: _ClassVar[int]
    LAST_NAME_FIELD_NUMBER: _ClassVar[int]
    EMAIL_FIELD_NUMBER: _ClassVar[int]
    ICON_FIELD_NUMBER: _ClassVar[int]
    CREATED_TIME_FIELD_NUMBER: _ClassVar[int]
    UPDATED_TIME_FIELD_NUMBER: _ClassVar[int]
    user_id: str
    domain_id: str
    user_name: str
    first_name: str
    last_name: str
    email: str
    icon: bytes
    created_time: int
    updated_time: int
    def __init__(self, user_id: _Optional[str] = ..., domain_id: _Optional[str] = ..., user_name: _Optional[str] = ..., first_name: _Optional[str] = ..., last_name: _Optional[str] = ..., email: _Optional[str] = ..., icon: _Optional[bytes] = ..., created_time: _Optional[int] = ..., updated_time: _Optional[int] = ...) -> None: ...

class GroupAdmin(_message.Message):
    __slots__ = ("group_id", "domain_id", "admin_id")
    GROUP_ID_FIELD_NUMBER: _ClassVar[int]
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    ADMIN_ID_FIELD_NUMBER: _ClassVar[int]
    group_id: str
    domain_id: str
    admin_id: str
    def __init__(self, group_id: _Optional[str] = ..., domain_id: _Optional[str] = ..., admin_id: _Optional[str] = ...) -> None: ...

class UserGroup(_message.Message):
    __slots__ = ("group_id", "domain_id", "name", "description", "owner_id", "group_type", "group_cardinality", "created_time", "updated_time", "group_admins")
    GROUP_ID_FIELD_NUMBER: _ClassVar[int]
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    NAME_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    OWNER_ID_FIELD_NUMBER: _ClassVar[int]
    GROUP_TYPE_FIELD_NUMBER: _ClassVar[int]
    GROUP_CARDINALITY_FIELD_NUMBER: _ClassVar[int]
    CREATED_TIME_FIELD_NUMBER: _ClassVar[int]
    UPDATED_TIME_FIELD_NUMBER: _ClassVar[int]
    GROUP_ADMINS_FIELD_NUMBER: _ClassVar[int]
    group_id: str
    domain_id: str
    name: str
    description: str
    owner_id: str
    group_type: GroupType
    group_cardinality: GroupCardinality
    created_time: int
    updated_time: int
    group_admins: _containers.RepeatedCompositeFieldContainer[GroupAdmin]
    def __init__(self, group_id: _Optional[str] = ..., domain_id: _Optional[str] = ..., name: _Optional[str] = ..., description: _Optional[str] = ..., owner_id: _Optional[str] = ..., group_type: _Optional[_Union[GroupType, str]] = ..., group_cardinality: _Optional[_Union[GroupCardinality, str]] = ..., created_time: _Optional[int] = ..., updated_time: _Optional[int] = ..., group_admins: _Optional[_Iterable[_Union[GroupAdmin, _Mapping]]] = ...) -> None: ...

class GroupMembership(_message.Message):
    __slots__ = ("parent_id", "child_id", "domain_id", "child_type", "created_time", "updated_time")
    PARENT_ID_FIELD_NUMBER: _ClassVar[int]
    CHILD_ID_FIELD_NUMBER: _ClassVar[int]
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    CHILD_TYPE_FIELD_NUMBER: _ClassVar[int]
    CREATED_TIME_FIELD_NUMBER: _ClassVar[int]
    UPDATED_TIME_FIELD_NUMBER: _ClassVar[int]
    parent_id: str
    child_id: str
    domain_id: str
    child_type: GroupChildType
    created_time: int
    updated_time: int
    def __init__(self, parent_id: _Optional[str] = ..., child_id: _Optional[str] = ..., domain_id: _Optional[str] = ..., child_type: _Optional[_Union[GroupChildType, str]] = ..., created_time: _Optional[int] = ..., updated_time: _Optional[int] = ...) -> None: ...

class EntityType(_message.Message):
    __slots__ = ("entity_type_id", "domain_id", "name", "description", "created_time", "updated_time")
    ENTITY_TYPE_ID_FIELD_NUMBER: _ClassVar[int]
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    NAME_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    CREATED_TIME_FIELD_NUMBER: _ClassVar[int]
    UPDATED_TIME_FIELD_NUMBER: _ClassVar[int]
    entity_type_id: str
    domain_id: str
    name: str
    description: str
    created_time: int
    updated_time: int
    def __init__(self, entity_type_id: _Optional[str] = ..., domain_id: _Optional[str] = ..., name: _Optional[str] = ..., description: _Optional[str] = ..., created_time: _Optional[int] = ..., updated_time: _Optional[int] = ...) -> None: ...

class SearchCriteria(_message.Message):
    __slots__ = ("search_field", "value", "search_condition")
    SEARCH_FIELD_FIELD_NUMBER: _ClassVar[int]
    VALUE_FIELD_NUMBER: _ClassVar[int]
    SEARCH_CONDITION_FIELD_NUMBER: _ClassVar[int]
    search_field: EntitySearchField
    value: str
    search_condition: SearchCondition
    def __init__(self, search_field: _Optional[_Union[EntitySearchField, str]] = ..., value: _Optional[str] = ..., search_condition: _Optional[_Union[SearchCondition, str]] = ...) -> None: ...

class Entity(_message.Message):
    __slots__ = ("entity_id", "domain_id", "entity_type_id", "owner_id", "parent_entity_id", "name", "description", "binary_data", "full_text", "shared_count", "original_entity_creation_time", "created_time", "updated_time")
    ENTITY_ID_FIELD_NUMBER: _ClassVar[int]
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    ENTITY_TYPE_ID_FIELD_NUMBER: _ClassVar[int]
    OWNER_ID_FIELD_NUMBER: _ClassVar[int]
    PARENT_ENTITY_ID_FIELD_NUMBER: _ClassVar[int]
    NAME_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    BINARY_DATA_FIELD_NUMBER: _ClassVar[int]
    FULL_TEXT_FIELD_NUMBER: _ClassVar[int]
    SHARED_COUNT_FIELD_NUMBER: _ClassVar[int]
    ORIGINAL_ENTITY_CREATION_TIME_FIELD_NUMBER: _ClassVar[int]
    CREATED_TIME_FIELD_NUMBER: _ClassVar[int]
    UPDATED_TIME_FIELD_NUMBER: _ClassVar[int]
    entity_id: str
    domain_id: str
    entity_type_id: str
    owner_id: str
    parent_entity_id: str
    name: str
    description: str
    binary_data: bytes
    full_text: str
    shared_count: int
    original_entity_creation_time: int
    created_time: int
    updated_time: int
    def __init__(self, entity_id: _Optional[str] = ..., domain_id: _Optional[str] = ..., entity_type_id: _Optional[str] = ..., owner_id: _Optional[str] = ..., parent_entity_id: _Optional[str] = ..., name: _Optional[str] = ..., description: _Optional[str] = ..., binary_data: _Optional[bytes] = ..., full_text: _Optional[str] = ..., shared_count: _Optional[int] = ..., original_entity_creation_time: _Optional[int] = ..., created_time: _Optional[int] = ..., updated_time: _Optional[int] = ...) -> None: ...

class PermissionType(_message.Message):
    __slots__ = ("permission_type_id", "domain_id", "name", "description", "created_time", "updated_time")
    PERMISSION_TYPE_ID_FIELD_NUMBER: _ClassVar[int]
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    NAME_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    CREATED_TIME_FIELD_NUMBER: _ClassVar[int]
    UPDATED_TIME_FIELD_NUMBER: _ClassVar[int]
    permission_type_id: str
    domain_id: str
    name: str
    description: str
    created_time: int
    updated_time: int
    def __init__(self, permission_type_id: _Optional[str] = ..., domain_id: _Optional[str] = ..., name: _Optional[str] = ..., description: _Optional[str] = ..., created_time: _Optional[int] = ..., updated_time: _Optional[int] = ...) -> None: ...

class Sharing(_message.Message):
    __slots__ = ("permission_type_id", "entity_id", "group_id", "sharing_type", "domain_id", "inherited_parent_id", "created_time", "updated_time")
    PERMISSION_TYPE_ID_FIELD_NUMBER: _ClassVar[int]
    ENTITY_ID_FIELD_NUMBER: _ClassVar[int]
    GROUP_ID_FIELD_NUMBER: _ClassVar[int]
    SHARING_TYPE_FIELD_NUMBER: _ClassVar[int]
    DOMAIN_ID_FIELD_NUMBER: _ClassVar[int]
    INHERITED_PARENT_ID_FIELD_NUMBER: _ClassVar[int]
    CREATED_TIME_FIELD_NUMBER: _ClassVar[int]
    UPDATED_TIME_FIELD_NUMBER: _ClassVar[int]
    permission_type_id: str
    entity_id: str
    group_id: str
    sharing_type: SharingType
    domain_id: str
    inherited_parent_id: str
    created_time: int
    updated_time: int
    def __init__(self, permission_type_id: _Optional[str] = ..., entity_id: _Optional[str] = ..., group_id: _Optional[str] = ..., sharing_type: _Optional[_Union[SharingType, str]] = ..., domain_id: _Optional[str] = ..., inherited_parent_id: _Optional[str] = ..., created_time: _Optional[int] = ..., updated_time: _Optional[int] = ...) -> None: ...
