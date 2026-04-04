from google.api import annotations_pb2 as _annotations_pb2
from google.protobuf import empty_pb2 as _empty_pb2
from org.apache.airavata.model.workspace import workspace_pb2 as _workspace_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class CreateNotificationRequest(_message.Message):
    __slots__ = ("gateway_id", "notification")
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    NOTIFICATION_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    notification: _workspace_pb2.Notification
    def __init__(self, gateway_id: _Optional[str] = ..., notification: _Optional[_Union[_workspace_pb2.Notification, _Mapping]] = ...) -> None: ...

class CreateNotificationResponse(_message.Message):
    __slots__ = ("notification_id",)
    NOTIFICATION_ID_FIELD_NUMBER: _ClassVar[int]
    notification_id: str
    def __init__(self, notification_id: _Optional[str] = ...) -> None: ...

class UpdateNotificationRequest(_message.Message):
    __slots__ = ("notification_id", "notification")
    NOTIFICATION_ID_FIELD_NUMBER: _ClassVar[int]
    NOTIFICATION_FIELD_NUMBER: _ClassVar[int]
    notification_id: str
    notification: _workspace_pb2.Notification
    def __init__(self, notification_id: _Optional[str] = ..., notification: _Optional[_Union[_workspace_pb2.Notification, _Mapping]] = ...) -> None: ...

class DeleteNotificationRequest(_message.Message):
    __slots__ = ("gateway_id", "notification_id")
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    NOTIFICATION_ID_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    notification_id: str
    def __init__(self, gateway_id: _Optional[str] = ..., notification_id: _Optional[str] = ...) -> None: ...

class GetNotificationRequest(_message.Message):
    __slots__ = ("gateway_id", "notification_id")
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    NOTIFICATION_ID_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    notification_id: str
    def __init__(self, gateway_id: _Optional[str] = ..., notification_id: _Optional[str] = ...) -> None: ...

class GetAllNotificationsRequest(_message.Message):
    __slots__ = ("gateway_id",)
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    def __init__(self, gateway_id: _Optional[str] = ...) -> None: ...

class GetAllNotificationsResponse(_message.Message):
    __slots__ = ("notifications",)
    NOTIFICATIONS_FIELD_NUMBER: _ClassVar[int]
    notifications: _containers.RepeatedCompositeFieldContainer[_workspace_pb2.Notification]
    def __init__(self, notifications: _Optional[_Iterable[_Union[_workspace_pb2.Notification, _Mapping]]] = ...) -> None: ...
