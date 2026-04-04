from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class ErrorModel(_message.Message):
    __slots__ = ("error_id", "creation_time", "actual_error_message", "user_friendly_message", "transient_or_persistent", "root_cause_error_id_list")
    ERROR_ID_FIELD_NUMBER: _ClassVar[int]
    CREATION_TIME_FIELD_NUMBER: _ClassVar[int]
    ACTUAL_ERROR_MESSAGE_FIELD_NUMBER: _ClassVar[int]
    USER_FRIENDLY_MESSAGE_FIELD_NUMBER: _ClassVar[int]
    TRANSIENT_OR_PERSISTENT_FIELD_NUMBER: _ClassVar[int]
    ROOT_CAUSE_ERROR_ID_LIST_FIELD_NUMBER: _ClassVar[int]
    error_id: str
    creation_time: int
    actual_error_message: str
    user_friendly_message: str
    transient_or_persistent: bool
    root_cause_error_id_list: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, error_id: _Optional[str] = ..., creation_time: _Optional[int] = ..., actual_error_message: _Optional[str] = ..., user_friendly_message: _Optional[str] = ..., transient_or_persistent: bool = ..., root_cause_error_id_list: _Optional[_Iterable[str]] = ...) -> None: ...

class ValidatorResult(_message.Message):
    __slots__ = ("result", "error_details")
    RESULT_FIELD_NUMBER: _ClassVar[int]
    ERROR_DETAILS_FIELD_NUMBER: _ClassVar[int]
    result: bool
    error_details: str
    def __init__(self, result: bool = ..., error_details: _Optional[str] = ...) -> None: ...

class ValidationResults(_message.Message):
    __slots__ = ("validation_state", "validation_result_list")
    VALIDATION_STATE_FIELD_NUMBER: _ClassVar[int]
    VALIDATION_RESULT_LIST_FIELD_NUMBER: _ClassVar[int]
    validation_state: bool
    validation_result_list: _containers.RepeatedCompositeFieldContainer[ValidatorResult]
    def __init__(self, validation_state: bool = ..., validation_result_list: _Optional[_Iterable[_Union[ValidatorResult, _Mapping]]] = ...) -> None: ...
