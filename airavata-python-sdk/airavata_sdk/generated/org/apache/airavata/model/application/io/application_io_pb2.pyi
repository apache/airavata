from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class DataType(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    DATA_TYPE_UNKNOWN: _ClassVar[DataType]
    STRING: _ClassVar[DataType]
    INTEGER: _ClassVar[DataType]
    FLOAT: _ClassVar[DataType]
    URI: _ClassVar[DataType]
    URI_COLLECTION: _ClassVar[DataType]
    STDOUT: _ClassVar[DataType]
    STDERR: _ClassVar[DataType]
DATA_TYPE_UNKNOWN: DataType
STRING: DataType
INTEGER: DataType
FLOAT: DataType
URI: DataType
URI_COLLECTION: DataType
STDOUT: DataType
STDERR: DataType

class InputDataObjectType(_message.Message):
    __slots__ = ("name", "value", "type", "application_argument", "standard_input", "user_friendly_description", "meta_data", "input_order", "is_required", "required_to_added_to_command_line", "data_staged", "storage_resource_id", "is_read_only", "override_filename")
    NAME_FIELD_NUMBER: _ClassVar[int]
    VALUE_FIELD_NUMBER: _ClassVar[int]
    TYPE_FIELD_NUMBER: _ClassVar[int]
    APPLICATION_ARGUMENT_FIELD_NUMBER: _ClassVar[int]
    STANDARD_INPUT_FIELD_NUMBER: _ClassVar[int]
    USER_FRIENDLY_DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    META_DATA_FIELD_NUMBER: _ClassVar[int]
    INPUT_ORDER_FIELD_NUMBER: _ClassVar[int]
    IS_REQUIRED_FIELD_NUMBER: _ClassVar[int]
    REQUIRED_TO_ADDED_TO_COMMAND_LINE_FIELD_NUMBER: _ClassVar[int]
    DATA_STAGED_FIELD_NUMBER: _ClassVar[int]
    STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    IS_READ_ONLY_FIELD_NUMBER: _ClassVar[int]
    OVERRIDE_FILENAME_FIELD_NUMBER: _ClassVar[int]
    name: str
    value: str
    type: DataType
    application_argument: str
    standard_input: bool
    user_friendly_description: str
    meta_data: str
    input_order: int
    is_required: bool
    required_to_added_to_command_line: bool
    data_staged: bool
    storage_resource_id: str
    is_read_only: bool
    override_filename: str
    def __init__(self, name: _Optional[str] = ..., value: _Optional[str] = ..., type: _Optional[_Union[DataType, str]] = ..., application_argument: _Optional[str] = ..., standard_input: bool = ..., user_friendly_description: _Optional[str] = ..., meta_data: _Optional[str] = ..., input_order: _Optional[int] = ..., is_required: bool = ..., required_to_added_to_command_line: bool = ..., data_staged: bool = ..., storage_resource_id: _Optional[str] = ..., is_read_only: bool = ..., override_filename: _Optional[str] = ...) -> None: ...

class OutputDataObjectType(_message.Message):
    __slots__ = ("name", "value", "type", "application_argument", "is_required", "required_to_added_to_command_line", "data_movement", "location", "search_query", "output_streaming", "storage_resource_id", "meta_data")
    NAME_FIELD_NUMBER: _ClassVar[int]
    VALUE_FIELD_NUMBER: _ClassVar[int]
    TYPE_FIELD_NUMBER: _ClassVar[int]
    APPLICATION_ARGUMENT_FIELD_NUMBER: _ClassVar[int]
    IS_REQUIRED_FIELD_NUMBER: _ClassVar[int]
    REQUIRED_TO_ADDED_TO_COMMAND_LINE_FIELD_NUMBER: _ClassVar[int]
    DATA_MOVEMENT_FIELD_NUMBER: _ClassVar[int]
    LOCATION_FIELD_NUMBER: _ClassVar[int]
    SEARCH_QUERY_FIELD_NUMBER: _ClassVar[int]
    OUTPUT_STREAMING_FIELD_NUMBER: _ClassVar[int]
    STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    META_DATA_FIELD_NUMBER: _ClassVar[int]
    name: str
    value: str
    type: DataType
    application_argument: str
    is_required: bool
    required_to_added_to_command_line: bool
    data_movement: bool
    location: str
    search_query: str
    output_streaming: bool
    storage_resource_id: str
    meta_data: str
    def __init__(self, name: _Optional[str] = ..., value: _Optional[str] = ..., type: _Optional[_Union[DataType, str]] = ..., application_argument: _Optional[str] = ..., is_required: bool = ..., required_to_added_to_command_line: bool = ..., data_movement: bool = ..., location: _Optional[str] = ..., search_query: _Optional[str] = ..., output_streaming: bool = ..., storage_resource_id: _Optional[str] = ..., meta_data: _Optional[str] = ...) -> None: ...
