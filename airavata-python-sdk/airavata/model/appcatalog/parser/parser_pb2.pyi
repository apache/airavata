from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class IOType(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    IO_TYPE_UNKNOWN: _ClassVar[IOType]
    FILE: _ClassVar[IOType]
    PROPERTY: _ClassVar[IOType]
IO_TYPE_UNKNOWN: IOType
FILE: IOType
PROPERTY: IOType

class ParserInput(_message.Message):
    __slots__ = ("id", "name", "required_input", "parser_id", "type")
    ID_FIELD_NUMBER: _ClassVar[int]
    NAME_FIELD_NUMBER: _ClassVar[int]
    REQUIRED_INPUT_FIELD_NUMBER: _ClassVar[int]
    PARSER_ID_FIELD_NUMBER: _ClassVar[int]
    TYPE_FIELD_NUMBER: _ClassVar[int]
    id: str
    name: str
    required_input: bool
    parser_id: str
    type: IOType
    def __init__(self, id: _Optional[str] = ..., name: _Optional[str] = ..., required_input: bool = ..., parser_id: _Optional[str] = ..., type: _Optional[_Union[IOType, str]] = ...) -> None: ...

class ParserOutput(_message.Message):
    __slots__ = ("id", "name", "required_output", "parser_id", "type")
    ID_FIELD_NUMBER: _ClassVar[int]
    NAME_FIELD_NUMBER: _ClassVar[int]
    REQUIRED_OUTPUT_FIELD_NUMBER: _ClassVar[int]
    PARSER_ID_FIELD_NUMBER: _ClassVar[int]
    TYPE_FIELD_NUMBER: _ClassVar[int]
    id: str
    name: str
    required_output: bool
    parser_id: str
    type: IOType
    def __init__(self, id: _Optional[str] = ..., name: _Optional[str] = ..., required_output: bool = ..., parser_id: _Optional[str] = ..., type: _Optional[_Union[IOType, str]] = ...) -> None: ...

class Parser(_message.Message):
    __slots__ = ("id", "image_name", "output_dir_path", "input_dir_path", "execution_command", "input_files", "output_files", "gateway_id")
    ID_FIELD_NUMBER: _ClassVar[int]
    IMAGE_NAME_FIELD_NUMBER: _ClassVar[int]
    OUTPUT_DIR_PATH_FIELD_NUMBER: _ClassVar[int]
    INPUT_DIR_PATH_FIELD_NUMBER: _ClassVar[int]
    EXECUTION_COMMAND_FIELD_NUMBER: _ClassVar[int]
    INPUT_FILES_FIELD_NUMBER: _ClassVar[int]
    OUTPUT_FILES_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    id: str
    image_name: str
    output_dir_path: str
    input_dir_path: str
    execution_command: str
    input_files: _containers.RepeatedCompositeFieldContainer[ParserInput]
    output_files: _containers.RepeatedCompositeFieldContainer[ParserOutput]
    gateway_id: str
    def __init__(self, id: _Optional[str] = ..., image_name: _Optional[str] = ..., output_dir_path: _Optional[str] = ..., input_dir_path: _Optional[str] = ..., execution_command: _Optional[str] = ..., input_files: _Optional[_Iterable[_Union[ParserInput, _Mapping]]] = ..., output_files: _Optional[_Iterable[_Union[ParserOutput, _Mapping]]] = ..., gateway_id: _Optional[str] = ...) -> None: ...

class ParserConnectorInput(_message.Message):
    __slots__ = ("id", "input_id", "parent_output_id", "value", "parser_connector_id")
    ID_FIELD_NUMBER: _ClassVar[int]
    INPUT_ID_FIELD_NUMBER: _ClassVar[int]
    PARENT_OUTPUT_ID_FIELD_NUMBER: _ClassVar[int]
    VALUE_FIELD_NUMBER: _ClassVar[int]
    PARSER_CONNECTOR_ID_FIELD_NUMBER: _ClassVar[int]
    id: str
    input_id: str
    parent_output_id: str
    value: str
    parser_connector_id: str
    def __init__(self, id: _Optional[str] = ..., input_id: _Optional[str] = ..., parent_output_id: _Optional[str] = ..., value: _Optional[str] = ..., parser_connector_id: _Optional[str] = ...) -> None: ...

class ParserConnector(_message.Message):
    __slots__ = ("id", "parent_parser_id", "child_parser_id", "connector_inputs", "parsing_template_id")
    ID_FIELD_NUMBER: _ClassVar[int]
    PARENT_PARSER_ID_FIELD_NUMBER: _ClassVar[int]
    CHILD_PARSER_ID_FIELD_NUMBER: _ClassVar[int]
    CONNECTOR_INPUTS_FIELD_NUMBER: _ClassVar[int]
    PARSING_TEMPLATE_ID_FIELD_NUMBER: _ClassVar[int]
    id: str
    parent_parser_id: str
    child_parser_id: str
    connector_inputs: _containers.RepeatedCompositeFieldContainer[ParserConnectorInput]
    parsing_template_id: str
    def __init__(self, id: _Optional[str] = ..., parent_parser_id: _Optional[str] = ..., child_parser_id: _Optional[str] = ..., connector_inputs: _Optional[_Iterable[_Union[ParserConnectorInput, _Mapping]]] = ..., parsing_template_id: _Optional[str] = ...) -> None: ...

class ParsingTemplateInput(_message.Message):
    __slots__ = ("id", "target_input_id", "application_output_name", "value", "parsing_template_id")
    ID_FIELD_NUMBER: _ClassVar[int]
    TARGET_INPUT_ID_FIELD_NUMBER: _ClassVar[int]
    APPLICATION_OUTPUT_NAME_FIELD_NUMBER: _ClassVar[int]
    VALUE_FIELD_NUMBER: _ClassVar[int]
    PARSING_TEMPLATE_ID_FIELD_NUMBER: _ClassVar[int]
    id: str
    target_input_id: str
    application_output_name: str
    value: str
    parsing_template_id: str
    def __init__(self, id: _Optional[str] = ..., target_input_id: _Optional[str] = ..., application_output_name: _Optional[str] = ..., value: _Optional[str] = ..., parsing_template_id: _Optional[str] = ...) -> None: ...

class ParsingTemplate(_message.Message):
    __slots__ = ("id", "application_interface", "initial_inputs", "parser_connections", "gateway_id")
    ID_FIELD_NUMBER: _ClassVar[int]
    APPLICATION_INTERFACE_FIELD_NUMBER: _ClassVar[int]
    INITIAL_INPUTS_FIELD_NUMBER: _ClassVar[int]
    PARSER_CONNECTIONS_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    id: str
    application_interface: str
    initial_inputs: _containers.RepeatedCompositeFieldContainer[ParsingTemplateInput]
    parser_connections: _containers.RepeatedCompositeFieldContainer[ParserConnector]
    gateway_id: str
    def __init__(self, id: _Optional[str] = ..., application_interface: _Optional[str] = ..., initial_inputs: _Optional[_Iterable[_Union[ParsingTemplateInput, _Mapping]]] = ..., parser_connections: _Optional[_Iterable[_Union[ParserConnector, _Mapping]]] = ..., gateway_id: _Optional[str] = ...) -> None: ...
