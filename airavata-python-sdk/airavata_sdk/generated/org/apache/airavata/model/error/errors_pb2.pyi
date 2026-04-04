from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class AiravataErrorType(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    AIRAVATA_ERROR_TYPE_UNKNOWN: _ClassVar[AiravataErrorType]
    PERMISSION_DENIED: _ClassVar[AiravataErrorType]
    INTERNAL_ERROR: _ClassVar[AiravataErrorType]
    AUTHENTICATION_FAILURE: _ClassVar[AiravataErrorType]
    INVALID_AUTHORIZATION: _ClassVar[AiravataErrorType]
    AUTHORIZATION_EXPIRED: _ClassVar[AiravataErrorType]
    UNKNOWN_GATEWAY_ID: _ClassVar[AiravataErrorType]
    UNSUPPORTED_OPERATION: _ClassVar[AiravataErrorType]
AIRAVATA_ERROR_TYPE_UNKNOWN: AiravataErrorType
PERMISSION_DENIED: AiravataErrorType
INTERNAL_ERROR: AiravataErrorType
AUTHENTICATION_FAILURE: AiravataErrorType
INVALID_AUTHORIZATION: AiravataErrorType
AUTHORIZATION_EXPIRED: AiravataErrorType
UNKNOWN_GATEWAY_ID: AiravataErrorType
UNSUPPORTED_OPERATION: AiravataErrorType

class ExperimentNotFoundException(_message.Message):
    __slots__ = ("message",)
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    message: str
    def __init__(self, message: _Optional[str] = ...) -> None: ...

class ProjectNotFoundException(_message.Message):
    __slots__ = ("message",)
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    message: str
    def __init__(self, message: _Optional[str] = ...) -> None: ...

class InvalidRequestException(_message.Message):
    __slots__ = ("message",)
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    message: str
    def __init__(self, message: _Optional[str] = ...) -> None: ...

class TimedOutException(_message.Message):
    __slots__ = ()
    def __init__(self) -> None: ...

class AuthenticationException(_message.Message):
    __slots__ = ("message",)
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    message: str
    def __init__(self, message: _Optional[str] = ...) -> None: ...

class AuthorizationException(_message.Message):
    __slots__ = ("message",)
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    message: str
    def __init__(self, message: _Optional[str] = ...) -> None: ...

class DuplicateEntryException(_message.Message):
    __slots__ = ("message",)
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    message: str
    def __init__(self, message: _Optional[str] = ...) -> None: ...

class AiravataClientException(_message.Message):
    __slots__ = ("airavata_error_type", "parameter")
    AIRAVATA_ERROR_TYPE_FIELD_NUMBER: _ClassVar[int]
    PARAMETER_FIELD_NUMBER: _ClassVar[int]
    airavata_error_type: AiravataErrorType
    parameter: str
    def __init__(self, airavata_error_type: _Optional[_Union[AiravataErrorType, str]] = ..., parameter: _Optional[str] = ...) -> None: ...

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

class LaunchValidationException(_message.Message):
    __slots__ = ("validation_result", "error_message")
    VALIDATION_RESULT_FIELD_NUMBER: _ClassVar[int]
    ERROR_MESSAGE_FIELD_NUMBER: _ClassVar[int]
    validation_result: ValidationResults
    error_message: str
    def __init__(self, validation_result: _Optional[_Union[ValidationResults, _Mapping]] = ..., error_message: _Optional[str] = ...) -> None: ...

class AiravataSystemException(_message.Message):
    __slots__ = ("airavata_error_type", "message")
    AIRAVATA_ERROR_TYPE_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    airavata_error_type: AiravataErrorType
    message: str
    def __init__(self, airavata_error_type: _Optional[_Union[AiravataErrorType, str]] = ..., message: _Optional[str] = ...) -> None: ...
