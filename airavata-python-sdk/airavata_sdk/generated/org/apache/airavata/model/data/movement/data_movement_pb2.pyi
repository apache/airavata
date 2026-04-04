from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class DMType(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    DM_TYPE_UNKNOWN: _ClassVar[DMType]
    COMPUTE_RESOURCE: _ClassVar[DMType]
    STORAGE_RESOURCE: _ClassVar[DMType]

class SecurityProtocol(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    SECURITY_PROTOCOL_UNKNOWN: _ClassVar[SecurityProtocol]
    USERNAME_PASSWORD: _ClassVar[SecurityProtocol]
    SSH_KEYS: _ClassVar[SecurityProtocol]
    GSI: _ClassVar[SecurityProtocol]
    KERBEROS: _ClassVar[SecurityProtocol]
    OAUTH: _ClassVar[SecurityProtocol]
    LOCAL: _ClassVar[SecurityProtocol]

class DataMovementProtocol(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    DATA_MOVEMENT_PROTOCOL_UNKNOWN: _ClassVar[DataMovementProtocol]
    DATA_MOVEMENT_PROTOCOL_LOCAL: _ClassVar[DataMovementProtocol]
    SCP: _ClassVar[DataMovementProtocol]
    SFTP: _ClassVar[DataMovementProtocol]
    GRID_FTP: _ClassVar[DataMovementProtocol]
    UNICORE_STORAGE_SERVICE: _ClassVar[DataMovementProtocol]
DM_TYPE_UNKNOWN: DMType
COMPUTE_RESOURCE: DMType
STORAGE_RESOURCE: DMType
SECURITY_PROTOCOL_UNKNOWN: SecurityProtocol
USERNAME_PASSWORD: SecurityProtocol
SSH_KEYS: SecurityProtocol
GSI: SecurityProtocol
KERBEROS: SecurityProtocol
OAUTH: SecurityProtocol
LOCAL: SecurityProtocol
DATA_MOVEMENT_PROTOCOL_UNKNOWN: DataMovementProtocol
DATA_MOVEMENT_PROTOCOL_LOCAL: DataMovementProtocol
SCP: DataMovementProtocol
SFTP: DataMovementProtocol
GRID_FTP: DataMovementProtocol
UNICORE_STORAGE_SERVICE: DataMovementProtocol

class SCPDataMovement(_message.Message):
    __slots__ = ("data_movement_interface_id", "security_protocol", "alternative_scp_host_name", "ssh_port")
    DATA_MOVEMENT_INTERFACE_ID_FIELD_NUMBER: _ClassVar[int]
    SECURITY_PROTOCOL_FIELD_NUMBER: _ClassVar[int]
    ALTERNATIVE_SCP_HOST_NAME_FIELD_NUMBER: _ClassVar[int]
    SSH_PORT_FIELD_NUMBER: _ClassVar[int]
    data_movement_interface_id: str
    security_protocol: SecurityProtocol
    alternative_scp_host_name: str
    ssh_port: int
    def __init__(self, data_movement_interface_id: _Optional[str] = ..., security_protocol: _Optional[_Union[SecurityProtocol, str]] = ..., alternative_scp_host_name: _Optional[str] = ..., ssh_port: _Optional[int] = ...) -> None: ...

class GridFTPDataMovement(_message.Message):
    __slots__ = ("data_movement_interface_id", "security_protocol", "grid_ftp_end_points")
    DATA_MOVEMENT_INTERFACE_ID_FIELD_NUMBER: _ClassVar[int]
    SECURITY_PROTOCOL_FIELD_NUMBER: _ClassVar[int]
    GRID_FTP_END_POINTS_FIELD_NUMBER: _ClassVar[int]
    data_movement_interface_id: str
    security_protocol: SecurityProtocol
    grid_ftp_end_points: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, data_movement_interface_id: _Optional[str] = ..., security_protocol: _Optional[_Union[SecurityProtocol, str]] = ..., grid_ftp_end_points: _Optional[_Iterable[str]] = ...) -> None: ...

class UnicoreDataMovement(_message.Message):
    __slots__ = ("data_movement_interface_id", "security_protocol", "unicore_end_point_url")
    DATA_MOVEMENT_INTERFACE_ID_FIELD_NUMBER: _ClassVar[int]
    SECURITY_PROTOCOL_FIELD_NUMBER: _ClassVar[int]
    UNICORE_END_POINT_URL_FIELD_NUMBER: _ClassVar[int]
    data_movement_interface_id: str
    security_protocol: SecurityProtocol
    unicore_end_point_url: str
    def __init__(self, data_movement_interface_id: _Optional[str] = ..., security_protocol: _Optional[_Union[SecurityProtocol, str]] = ..., unicore_end_point_url: _Optional[str] = ...) -> None: ...

class LOCALDataMovement(_message.Message):
    __slots__ = ("data_movement_interface_id",)
    DATA_MOVEMENT_INTERFACE_ID_FIELD_NUMBER: _ClassVar[int]
    data_movement_interface_id: str
    def __init__(self, data_movement_interface_id: _Optional[str] = ...) -> None: ...

class DataMovementInterface(_message.Message):
    __slots__ = ("data_movement_interface_id", "data_movement_protocol", "priority_order", "creation_time", "update_time", "storage_resource_id")
    DATA_MOVEMENT_INTERFACE_ID_FIELD_NUMBER: _ClassVar[int]
    DATA_MOVEMENT_PROTOCOL_FIELD_NUMBER: _ClassVar[int]
    PRIORITY_ORDER_FIELD_NUMBER: _ClassVar[int]
    CREATION_TIME_FIELD_NUMBER: _ClassVar[int]
    UPDATE_TIME_FIELD_NUMBER: _ClassVar[int]
    STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    data_movement_interface_id: str
    data_movement_protocol: DataMovementProtocol
    priority_order: int
    creation_time: int
    update_time: int
    storage_resource_id: str
    def __init__(self, data_movement_interface_id: _Optional[str] = ..., data_movement_protocol: _Optional[_Union[DataMovementProtocol, str]] = ..., priority_order: _Optional[int] = ..., creation_time: _Optional[int] = ..., update_time: _Optional[int] = ..., storage_resource_id: _Optional[str] = ...) -> None: ...
