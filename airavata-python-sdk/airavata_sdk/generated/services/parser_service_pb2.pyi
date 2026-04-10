from google.api import annotations_pb2 as _annotations_pb2
from google.protobuf import empty_pb2 as _empty_pb2
from org.apache.airavata.model.appcatalog.parser import parser_pb2 as _parser_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class SaveParserRequest(_message.Message):
    __slots__ = ("parser",)
    PARSER_FIELD_NUMBER: _ClassVar[int]
    parser: _parser_pb2.Parser
    def __init__(self, parser: _Optional[_Union[_parser_pb2.Parser, _Mapping]] = ...) -> None: ...

class SaveParserResponse(_message.Message):
    __slots__ = ("parser_id",)
    PARSER_ID_FIELD_NUMBER: _ClassVar[int]
    parser_id: str
    def __init__(self, parser_id: _Optional[str] = ...) -> None: ...

class GetParserRequest(_message.Message):
    __slots__ = ("gateway_id", "parser_id")
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    PARSER_ID_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    parser_id: str
    def __init__(self, gateway_id: _Optional[str] = ..., parser_id: _Optional[str] = ...) -> None: ...

class ListAllParsersRequest(_message.Message):
    __slots__ = ("gateway_id",)
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    def __init__(self, gateway_id: _Optional[str] = ...) -> None: ...

class ListAllParsersResponse(_message.Message):
    __slots__ = ("parsers",)
    PARSERS_FIELD_NUMBER: _ClassVar[int]
    parsers: _containers.RepeatedCompositeFieldContainer[_parser_pb2.Parser]
    def __init__(self, parsers: _Optional[_Iterable[_Union[_parser_pb2.Parser, _Mapping]]] = ...) -> None: ...

class RemoveParserRequest(_message.Message):
    __slots__ = ("gateway_id", "parser_id")
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    PARSER_ID_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    parser_id: str
    def __init__(self, gateway_id: _Optional[str] = ..., parser_id: _Optional[str] = ...) -> None: ...

class SaveParsingTemplateRequest(_message.Message):
    __slots__ = ("parsing_template",)
    PARSING_TEMPLATE_FIELD_NUMBER: _ClassVar[int]
    parsing_template: _parser_pb2.ParsingTemplate
    def __init__(self, parsing_template: _Optional[_Union[_parser_pb2.ParsingTemplate, _Mapping]] = ...) -> None: ...

class SaveParsingTemplateResponse(_message.Message):
    __slots__ = ("template_id",)
    TEMPLATE_ID_FIELD_NUMBER: _ClassVar[int]
    template_id: str
    def __init__(self, template_id: _Optional[str] = ...) -> None: ...

class GetParsingTemplateRequest(_message.Message):
    __slots__ = ("gateway_id", "template_id")
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    TEMPLATE_ID_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    template_id: str
    def __init__(self, gateway_id: _Optional[str] = ..., template_id: _Optional[str] = ...) -> None: ...

class GetParsingTemplatesForExperimentRequest(_message.Message):
    __slots__ = ("gateway_id", "experiment_id")
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    experiment_id: str
    def __init__(self, gateway_id: _Optional[str] = ..., experiment_id: _Optional[str] = ...) -> None: ...

class GetParsingTemplatesForExperimentResponse(_message.Message):
    __slots__ = ("parsing_templates",)
    PARSING_TEMPLATES_FIELD_NUMBER: _ClassVar[int]
    parsing_templates: _containers.RepeatedCompositeFieldContainer[_parser_pb2.ParsingTemplate]
    def __init__(self, parsing_templates: _Optional[_Iterable[_Union[_parser_pb2.ParsingTemplate, _Mapping]]] = ...) -> None: ...

class ListAllParsingTemplatesRequest(_message.Message):
    __slots__ = ("gateway_id",)
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    def __init__(self, gateway_id: _Optional[str] = ...) -> None: ...

class ListAllParsingTemplatesResponse(_message.Message):
    __slots__ = ("parsing_templates",)
    PARSING_TEMPLATES_FIELD_NUMBER: _ClassVar[int]
    parsing_templates: _containers.RepeatedCompositeFieldContainer[_parser_pb2.ParsingTemplate]
    def __init__(self, parsing_templates: _Optional[_Iterable[_Union[_parser_pb2.ParsingTemplate, _Mapping]]] = ...) -> None: ...

class RemoveParsingTemplateRequest(_message.Message):
    __slots__ = ("gateway_id", "template_id")
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    TEMPLATE_ID_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    template_id: str
    def __init__(self, gateway_id: _Optional[str] = ..., template_id: _Optional[str] = ...) -> None: ...
