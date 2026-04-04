from org.apache.airavata.model.status import status_pb2 as _status_pb2
from org.apache.airavata.model.application.io import application_io_pb2 as _application_io_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class MessageLevel(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    MESSAGE_LEVEL_UNKNOWN: _ClassVar[MessageLevel]
    INFO: _ClassVar[MessageLevel]
    DEBUG: _ClassVar[MessageLevel]
    ERROR: _ClassVar[MessageLevel]
    ACK: _ClassVar[MessageLevel]

class MessageType(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    MESSAGE_TYPE_UNKNOWN: _ClassVar[MessageType]
    EXPERIMENT: _ClassVar[MessageType]
    EXPERIMENT_CANCEL: _ClassVar[MessageType]
    TASK: _ClassVar[MessageType]
    PROCESS: _ClassVar[MessageType]
    JOB: _ClassVar[MessageType]
    LAUNCHPROCESS: _ClassVar[MessageType]
    TERMINATEPROCESS: _ClassVar[MessageType]
    PROCESSOUTPUT: _ClassVar[MessageType]
    DB_EVENT: _ClassVar[MessageType]
    INTERMEDIATE_OUTPUTS: _ClassVar[MessageType]
MESSAGE_LEVEL_UNKNOWN: MessageLevel
INFO: MessageLevel
DEBUG: MessageLevel
ERROR: MessageLevel
ACK: MessageLevel
MESSAGE_TYPE_UNKNOWN: MessageType
EXPERIMENT: MessageType
EXPERIMENT_CANCEL: MessageType
TASK: MessageType
PROCESS: MessageType
JOB: MessageType
LAUNCHPROCESS: MessageType
TERMINATEPROCESS: MessageType
PROCESSOUTPUT: MessageType
DB_EVENT: MessageType
INTERMEDIATE_OUTPUTS: MessageType

class ExperimentStatusChangeEvent(_message.Message):
    __slots__ = ("state", "experiment_id", "gateway_id")
    STATE_FIELD_NUMBER: _ClassVar[int]
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    state: _status_pb2.ExperimentState
    experiment_id: str
    gateway_id: str
    def __init__(self, state: _Optional[_Union[_status_pb2.ExperimentState, str]] = ..., experiment_id: _Optional[str] = ..., gateway_id: _Optional[str] = ...) -> None: ...

class ProcessIdentifier(_message.Message):
    __slots__ = ("process_id", "experiment_id", "gateway_id")
    PROCESS_ID_FIELD_NUMBER: _ClassVar[int]
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    process_id: str
    experiment_id: str
    gateway_id: str
    def __init__(self, process_id: _Optional[str] = ..., experiment_id: _Optional[str] = ..., gateway_id: _Optional[str] = ...) -> None: ...

class TaskIdentifier(_message.Message):
    __slots__ = ("task_id", "process_id", "experiment_id", "gateway_id")
    TASK_ID_FIELD_NUMBER: _ClassVar[int]
    PROCESS_ID_FIELD_NUMBER: _ClassVar[int]
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    task_id: str
    process_id: str
    experiment_id: str
    gateway_id: str
    def __init__(self, task_id: _Optional[str] = ..., process_id: _Optional[str] = ..., experiment_id: _Optional[str] = ..., gateway_id: _Optional[str] = ...) -> None: ...

class TaskStatusChangeEvent(_message.Message):
    __slots__ = ("state", "task_identity")
    STATE_FIELD_NUMBER: _ClassVar[int]
    TASK_IDENTITY_FIELD_NUMBER: _ClassVar[int]
    state: _status_pb2.TaskState
    task_identity: TaskIdentifier
    def __init__(self, state: _Optional[_Union[_status_pb2.TaskState, str]] = ..., task_identity: _Optional[_Union[TaskIdentifier, _Mapping]] = ...) -> None: ...

class TaskStatusChangeRequestEvent(_message.Message):
    __slots__ = ("state", "task_identity")
    STATE_FIELD_NUMBER: _ClassVar[int]
    TASK_IDENTITY_FIELD_NUMBER: _ClassVar[int]
    state: _status_pb2.TaskState
    task_identity: TaskIdentifier
    def __init__(self, state: _Optional[_Union[_status_pb2.TaskState, str]] = ..., task_identity: _Optional[_Union[TaskIdentifier, _Mapping]] = ...) -> None: ...

class ProcessStatusChangeEvent(_message.Message):
    __slots__ = ("state", "process_identity")
    STATE_FIELD_NUMBER: _ClassVar[int]
    PROCESS_IDENTITY_FIELD_NUMBER: _ClassVar[int]
    state: _status_pb2.ProcessState
    process_identity: ProcessIdentifier
    def __init__(self, state: _Optional[_Union[_status_pb2.ProcessState, str]] = ..., process_identity: _Optional[_Union[ProcessIdentifier, _Mapping]] = ...) -> None: ...

class ProcessStatusChangeRequestEvent(_message.Message):
    __slots__ = ("state", "process_identity")
    STATE_FIELD_NUMBER: _ClassVar[int]
    PROCESS_IDENTITY_FIELD_NUMBER: _ClassVar[int]
    state: _status_pb2.ProcessState
    process_identity: ProcessIdentifier
    def __init__(self, state: _Optional[_Union[_status_pb2.ProcessState, str]] = ..., process_identity: _Optional[_Union[ProcessIdentifier, _Mapping]] = ...) -> None: ...

class TaskOutputChangeEvent(_message.Message):
    __slots__ = ("output", "task_identity")
    OUTPUT_FIELD_NUMBER: _ClassVar[int]
    TASK_IDENTITY_FIELD_NUMBER: _ClassVar[int]
    output: _containers.RepeatedCompositeFieldContainer[_application_io_pb2.OutputDataObjectType]
    task_identity: TaskIdentifier
    def __init__(self, output: _Optional[_Iterable[_Union[_application_io_pb2.OutputDataObjectType, _Mapping]]] = ..., task_identity: _Optional[_Union[TaskIdentifier, _Mapping]] = ...) -> None: ...

class JobIdentifier(_message.Message):
    __slots__ = ("job_id", "task_id", "process_id", "experiment_id", "gateway_id")
    JOB_ID_FIELD_NUMBER: _ClassVar[int]
    TASK_ID_FIELD_NUMBER: _ClassVar[int]
    PROCESS_ID_FIELD_NUMBER: _ClassVar[int]
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    job_id: str
    task_id: str
    process_id: str
    experiment_id: str
    gateway_id: str
    def __init__(self, job_id: _Optional[str] = ..., task_id: _Optional[str] = ..., process_id: _Optional[str] = ..., experiment_id: _Optional[str] = ..., gateway_id: _Optional[str] = ...) -> None: ...

class ExperimentSubmitEvent(_message.Message):
    __slots__ = ("experiment_id", "gateway_id")
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    experiment_id: str
    gateway_id: str
    def __init__(self, experiment_id: _Optional[str] = ..., gateway_id: _Optional[str] = ...) -> None: ...

class ProcessSubmitEvent(_message.Message):
    __slots__ = ("process_id", "gateway_id", "experiment_id", "token_id")
    PROCESS_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    TOKEN_ID_FIELD_NUMBER: _ClassVar[int]
    process_id: str
    gateway_id: str
    experiment_id: str
    token_id: str
    def __init__(self, process_id: _Optional[str] = ..., gateway_id: _Optional[str] = ..., experiment_id: _Optional[str] = ..., token_id: _Optional[str] = ...) -> None: ...

class ProcessTerminateEvent(_message.Message):
    __slots__ = ("process_id", "gateway_id", "token_id")
    PROCESS_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    TOKEN_ID_FIELD_NUMBER: _ClassVar[int]
    process_id: str
    gateway_id: str
    token_id: str
    def __init__(self, process_id: _Optional[str] = ..., gateway_id: _Optional[str] = ..., token_id: _Optional[str] = ...) -> None: ...

class JobStatusChangeEvent(_message.Message):
    __slots__ = ("state", "job_identity")
    STATE_FIELD_NUMBER: _ClassVar[int]
    JOB_IDENTITY_FIELD_NUMBER: _ClassVar[int]
    state: _status_pb2.JobState
    job_identity: JobIdentifier
    def __init__(self, state: _Optional[_Union[_status_pb2.JobState, str]] = ..., job_identity: _Optional[_Union[JobIdentifier, _Mapping]] = ...) -> None: ...

class JobStatusChangeRequestEvent(_message.Message):
    __slots__ = ("state", "job_identity")
    STATE_FIELD_NUMBER: _ClassVar[int]
    JOB_IDENTITY_FIELD_NUMBER: _ClassVar[int]
    state: _status_pb2.JobState
    job_identity: JobIdentifier
    def __init__(self, state: _Optional[_Union[_status_pb2.JobState, str]] = ..., job_identity: _Optional[_Union[JobIdentifier, _Mapping]] = ...) -> None: ...

class ExperimentIntermediateOutputsEvent(_message.Message):
    __slots__ = ("experiment_id", "gateway_id", "output_names")
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    OUTPUT_NAMES_FIELD_NUMBER: _ClassVar[int]
    experiment_id: str
    gateway_id: str
    output_names: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, experiment_id: _Optional[str] = ..., gateway_id: _Optional[str] = ..., output_names: _Optional[_Iterable[str]] = ...) -> None: ...

class Message(_message.Message):
    __slots__ = ("event", "message_id", "message_type", "updated_time", "message_level")
    EVENT_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_ID_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_TYPE_FIELD_NUMBER: _ClassVar[int]
    UPDATED_TIME_FIELD_NUMBER: _ClassVar[int]
    MESSAGE_LEVEL_FIELD_NUMBER: _ClassVar[int]
    event: bytes
    message_id: str
    message_type: MessageType
    updated_time: int
    message_level: MessageLevel
    def __init__(self, event: _Optional[bytes] = ..., message_id: _Optional[str] = ..., message_type: _Optional[_Union[MessageType, str]] = ..., updated_time: _Optional[int] = ..., message_level: _Optional[_Union[MessageLevel, str]] = ...) -> None: ...
