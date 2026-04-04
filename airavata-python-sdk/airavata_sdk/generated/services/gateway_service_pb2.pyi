from google.api import annotations_pb2 as _annotations_pb2
from google.protobuf import empty_pb2 as _empty_pb2
from org.apache.airavata.model.workspace import workspace_pb2 as _workspace_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class AddGatewayRequest(_message.Message):
    __slots__ = ("gateway",)
    GATEWAY_FIELD_NUMBER: _ClassVar[int]
    gateway: _workspace_pb2.Gateway
    def __init__(self, gateway: _Optional[_Union[_workspace_pb2.Gateway, _Mapping]] = ...) -> None: ...

class AddGatewayResponse(_message.Message):
    __slots__ = ("gateway_id",)
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    def __init__(self, gateway_id: _Optional[str] = ...) -> None: ...

class GetGatewayRequest(_message.Message):
    __slots__ = ("gateway_id",)
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    def __init__(self, gateway_id: _Optional[str] = ...) -> None: ...

class UpdateGatewayRequest(_message.Message):
    __slots__ = ("gateway_id", "gateway")
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    gateway: _workspace_pb2.Gateway
    def __init__(self, gateway_id: _Optional[str] = ..., gateway: _Optional[_Union[_workspace_pb2.Gateway, _Mapping]] = ...) -> None: ...

class DeleteGatewayRequest(_message.Message):
    __slots__ = ("gateway_id",)
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    def __init__(self, gateway_id: _Optional[str] = ...) -> None: ...

class GetAllGatewaysRequest(_message.Message):
    __slots__ = ()
    def __init__(self) -> None: ...

class GetAllGatewaysResponse(_message.Message):
    __slots__ = ("gateways",)
    GATEWAYS_FIELD_NUMBER: _ClassVar[int]
    gateways: _containers.RepeatedCompositeFieldContainer[_workspace_pb2.Gateway]
    def __init__(self, gateways: _Optional[_Iterable[_Union[_workspace_pb2.Gateway, _Mapping]]] = ...) -> None: ...

class IsGatewayExistRequest(_message.Message):
    __slots__ = ("gateway_id",)
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    def __init__(self, gateway_id: _Optional[str] = ...) -> None: ...

class IsGatewayExistResponse(_message.Message):
    __slots__ = ("exists",)
    EXISTS_FIELD_NUMBER: _ClassVar[int]
    exists: bool
    def __init__(self, exists: bool = ...) -> None: ...

class GetAllUsersInGatewayRequest(_message.Message):
    __slots__ = ("gateway_id",)
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    def __init__(self, gateway_id: _Optional[str] = ...) -> None: ...

class GetAllUsersInGatewayResponse(_message.Message):
    __slots__ = ("user_names",)
    USER_NAMES_FIELD_NUMBER: _ClassVar[int]
    user_names: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, user_names: _Optional[_Iterable[str]] = ...) -> None: ...

class IsUserExistsRequest(_message.Message):
    __slots__ = ("gateway_id", "user_name")
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    USER_NAME_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    user_name: str
    def __init__(self, gateway_id: _Optional[str] = ..., user_name: _Optional[str] = ...) -> None: ...

class IsUserExistsResponse(_message.Message):
    __slots__ = ("exists",)
    EXISTS_FIELD_NUMBER: _ClassVar[int]
    exists: bool
    def __init__(self, exists: bool = ...) -> None: ...
