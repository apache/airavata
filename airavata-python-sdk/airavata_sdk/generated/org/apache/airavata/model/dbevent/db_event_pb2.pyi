from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class CrudType(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    CRUD_TYPE_UNKNOWN: _ClassVar[CrudType]
    CREATE: _ClassVar[CrudType]
    READ: _ClassVar[CrudType]
    UPDATE: _ClassVar[CrudType]
    DELETE: _ClassVar[CrudType]

class EntityType(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    ENTITY_TYPE_UNKNOWN: _ClassVar[EntityType]
    USER_PROFILE: _ClassVar[EntityType]
    TENANT: _ClassVar[EntityType]
    GROUP: _ClassVar[EntityType]
    PROJECT: _ClassVar[EntityType]
    EXPERIMENT: _ClassVar[EntityType]
    APPLICATION: _ClassVar[EntityType]
    SHARING: _ClassVar[EntityType]
    REGISTRY: _ClassVar[EntityType]

class DBEventType(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    DB_EVENT_TYPE_UNKNOWN: _ClassVar[DBEventType]
    PUBLISHER: _ClassVar[DBEventType]
    SUBSCRIBER: _ClassVar[DBEventType]
CRUD_TYPE_UNKNOWN: CrudType
CREATE: CrudType
READ: CrudType
UPDATE: CrudType
DELETE: CrudType
ENTITY_TYPE_UNKNOWN: EntityType
USER_PROFILE: EntityType
TENANT: EntityType
GROUP: EntityType
PROJECT: EntityType
EXPERIMENT: EntityType
APPLICATION: EntityType
SHARING: EntityType
REGISTRY: EntityType
DB_EVENT_TYPE_UNKNOWN: DBEventType
PUBLISHER: DBEventType
SUBSCRIBER: DBEventType

class DBEventPublisherContext(_message.Message):
    __slots__ = ("crud_type", "entity_type", "entity_data_model")
    CRUD_TYPE_FIELD_NUMBER: _ClassVar[int]
    ENTITY_TYPE_FIELD_NUMBER: _ClassVar[int]
    ENTITY_DATA_MODEL_FIELD_NUMBER: _ClassVar[int]
    crud_type: CrudType
    entity_type: EntityType
    entity_data_model: bytes
    def __init__(self, crud_type: _Optional[_Union[CrudType, str]] = ..., entity_type: _Optional[_Union[EntityType, str]] = ..., entity_data_model: _Optional[bytes] = ...) -> None: ...

class DBEventPublisher(_message.Message):
    __slots__ = ("publisher_context",)
    PUBLISHER_CONTEXT_FIELD_NUMBER: _ClassVar[int]
    publisher_context: DBEventPublisherContext
    def __init__(self, publisher_context: _Optional[_Union[DBEventPublisherContext, _Mapping]] = ...) -> None: ...

class DBEventSubscriber(_message.Message):
    __slots__ = ("subscriber_service",)
    SUBSCRIBER_SERVICE_FIELD_NUMBER: _ClassVar[int]
    subscriber_service: str
    def __init__(self, subscriber_service: _Optional[str] = ...) -> None: ...

class DBEventMessageContext(_message.Message):
    __slots__ = ("publisher", "subscriber")
    PUBLISHER_FIELD_NUMBER: _ClassVar[int]
    SUBSCRIBER_FIELD_NUMBER: _ClassVar[int]
    publisher: DBEventPublisher
    subscriber: DBEventSubscriber
    def __init__(self, publisher: _Optional[_Union[DBEventPublisher, _Mapping]] = ..., subscriber: _Optional[_Union[DBEventSubscriber, _Mapping]] = ...) -> None: ...

class DBEventMessage(_message.Message):
    __slots__ = ("db_event_type", "message_context", "publisher_service")
    DB_EVENT_TYPE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_CONTEXT_FIELD_NUMBER: _ClassVar[int]
    PUBLISHER_SERVICE_FIELD_NUMBER: _ClassVar[int]
    db_event_type: DBEventType
    message_context: DBEventMessageContext
    publisher_service: str
    def __init__(self, db_event_type: _Optional[_Union[DBEventType, str]] = ..., message_context: _Optional[_Union[DBEventMessageContext, _Mapping]] = ..., publisher_service: _Optional[str] = ...) -> None: ...
