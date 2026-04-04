from org.apache.airavata.model.application.io import application_io_pb2 as _application_io_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class ApplicationInterfaceDescription(_message.Message):
    __slots__ = ("application_interface_id", "application_name", "application_description", "application_modules", "application_inputs", "application_outputs", "archive_working_directory", "has_optional_file_inputs", "clean_after_staged")
    APPLICATION_INTERFACE_ID_FIELD_NUMBER: _ClassVar[int]
    APPLICATION_NAME_FIELD_NUMBER: _ClassVar[int]
    APPLICATION_DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    APPLICATION_MODULES_FIELD_NUMBER: _ClassVar[int]
    APPLICATION_INPUTS_FIELD_NUMBER: _ClassVar[int]
    APPLICATION_OUTPUTS_FIELD_NUMBER: _ClassVar[int]
    ARCHIVE_WORKING_DIRECTORY_FIELD_NUMBER: _ClassVar[int]
    HAS_OPTIONAL_FILE_INPUTS_FIELD_NUMBER: _ClassVar[int]
    CLEAN_AFTER_STAGED_FIELD_NUMBER: _ClassVar[int]
    application_interface_id: str
    application_name: str
    application_description: str
    application_modules: _containers.RepeatedScalarFieldContainer[str]
    application_inputs: _containers.RepeatedCompositeFieldContainer[_application_io_pb2.InputDataObjectType]
    application_outputs: _containers.RepeatedCompositeFieldContainer[_application_io_pb2.OutputDataObjectType]
    archive_working_directory: bool
    has_optional_file_inputs: bool
    clean_after_staged: bool
    def __init__(self, application_interface_id: _Optional[str] = ..., application_name: _Optional[str] = ..., application_description: _Optional[str] = ..., application_modules: _Optional[_Iterable[str]] = ..., application_inputs: _Optional[_Iterable[_Union[_application_io_pb2.InputDataObjectType, _Mapping]]] = ..., application_outputs: _Optional[_Iterable[_Union[_application_io_pb2.OutputDataObjectType, _Mapping]]] = ..., archive_working_directory: bool = ..., has_optional_file_inputs: bool = ..., clean_after_staged: bool = ...) -> None: ...
