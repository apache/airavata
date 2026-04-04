from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class ExperimentState(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    EXPERIMENT_STATE_UNKNOWN: _ClassVar[ExperimentState]
    EXPERIMENT_STATE_CREATED: _ClassVar[ExperimentState]
    EXPERIMENT_STATE_VALIDATED: _ClassVar[ExperimentState]
    EXPERIMENT_STATE_SCHEDULED: _ClassVar[ExperimentState]
    EXPERIMENT_STATE_LAUNCHED: _ClassVar[ExperimentState]
    EXPERIMENT_STATE_EXECUTING: _ClassVar[ExperimentState]
    EXPERIMENT_STATE_CANCELING: _ClassVar[ExperimentState]
    EXPERIMENT_STATE_CANCELED: _ClassVar[ExperimentState]
    EXPERIMENT_STATE_COMPLETED: _ClassVar[ExperimentState]
    EXPERIMENT_STATE_FAILED: _ClassVar[ExperimentState]

class TaskState(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    TASK_STATE_UNKNOWN: _ClassVar[TaskState]
    TASK_STATE_CREATED: _ClassVar[TaskState]
    TASK_STATE_EXECUTING: _ClassVar[TaskState]
    TASK_STATE_COMPLETED: _ClassVar[TaskState]
    TASK_STATE_FAILED: _ClassVar[TaskState]
    TASK_STATE_CANCELED: _ClassVar[TaskState]

class ProcessState(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    PROCESS_STATE_UNKNOWN: _ClassVar[ProcessState]
    PROCESS_STATE_CREATED: _ClassVar[ProcessState]
    PROCESS_STATE_VALIDATED: _ClassVar[ProcessState]
    PROCESS_STATE_STARTED: _ClassVar[ProcessState]
    PROCESS_STATE_PRE_PROCESSING: _ClassVar[ProcessState]
    PROCESS_STATE_CONFIGURING_WORKSPACE: _ClassVar[ProcessState]
    PROCESS_STATE_INPUT_DATA_STAGING: _ClassVar[ProcessState]
    PROCESS_STATE_EXECUTING: _ClassVar[ProcessState]
    PROCESS_STATE_MONITORING: _ClassVar[ProcessState]
    PROCESS_STATE_OUTPUT_DATA_STAGING: _ClassVar[ProcessState]
    PROCESS_STATE_POST_PROCESSING: _ClassVar[ProcessState]
    PROCESS_STATE_COMPLETED: _ClassVar[ProcessState]
    PROCESS_STATE_FAILED: _ClassVar[ProcessState]
    PROCESS_STATE_CANCELLING: _ClassVar[ProcessState]
    PROCESS_STATE_CANCELED: _ClassVar[ProcessState]
    PROCESS_STATE_QUEUED: _ClassVar[ProcessState]
    PROCESS_STATE_DEQUEUING: _ClassVar[ProcessState]
    PROCESS_STATE_REQUEUED: _ClassVar[ProcessState]

class JobState(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    JOB_STATE_UNKNOWN: _ClassVar[JobState]
    SUBMITTED: _ClassVar[JobState]
    QUEUED: _ClassVar[JobState]
    ACTIVE: _ClassVar[JobState]
    COMPLETE: _ClassVar[JobState]
    CANCELED: _ClassVar[JobState]
    FAILED: _ClassVar[JobState]
    SUSPENDED: _ClassVar[JobState]
    NON_CRITICAL_FAIL: _ClassVar[JobState]
EXPERIMENT_STATE_UNKNOWN: ExperimentState
EXPERIMENT_STATE_CREATED: ExperimentState
EXPERIMENT_STATE_VALIDATED: ExperimentState
EXPERIMENT_STATE_SCHEDULED: ExperimentState
EXPERIMENT_STATE_LAUNCHED: ExperimentState
EXPERIMENT_STATE_EXECUTING: ExperimentState
EXPERIMENT_STATE_CANCELING: ExperimentState
EXPERIMENT_STATE_CANCELED: ExperimentState
EXPERIMENT_STATE_COMPLETED: ExperimentState
EXPERIMENT_STATE_FAILED: ExperimentState
TASK_STATE_UNKNOWN: TaskState
TASK_STATE_CREATED: TaskState
TASK_STATE_EXECUTING: TaskState
TASK_STATE_COMPLETED: TaskState
TASK_STATE_FAILED: TaskState
TASK_STATE_CANCELED: TaskState
PROCESS_STATE_UNKNOWN: ProcessState
PROCESS_STATE_CREATED: ProcessState
PROCESS_STATE_VALIDATED: ProcessState
PROCESS_STATE_STARTED: ProcessState
PROCESS_STATE_PRE_PROCESSING: ProcessState
PROCESS_STATE_CONFIGURING_WORKSPACE: ProcessState
PROCESS_STATE_INPUT_DATA_STAGING: ProcessState
PROCESS_STATE_EXECUTING: ProcessState
PROCESS_STATE_MONITORING: ProcessState
PROCESS_STATE_OUTPUT_DATA_STAGING: ProcessState
PROCESS_STATE_POST_PROCESSING: ProcessState
PROCESS_STATE_COMPLETED: ProcessState
PROCESS_STATE_FAILED: ProcessState
PROCESS_STATE_CANCELLING: ProcessState
PROCESS_STATE_CANCELED: ProcessState
PROCESS_STATE_QUEUED: ProcessState
PROCESS_STATE_DEQUEUING: ProcessState
PROCESS_STATE_REQUEUED: ProcessState
JOB_STATE_UNKNOWN: JobState
SUBMITTED: JobState
QUEUED: JobState
ACTIVE: JobState
COMPLETE: JobState
CANCELED: JobState
FAILED: JobState
SUSPENDED: JobState
NON_CRITICAL_FAIL: JobState

class ExperimentStatus(_message.Message):
    __slots__ = ("state", "time_of_state_change", "reason", "status_id")
    STATE_FIELD_NUMBER: _ClassVar[int]
    TIME_OF_STATE_CHANGE_FIELD_NUMBER: _ClassVar[int]
    REASON_FIELD_NUMBER: _ClassVar[int]
    STATUS_ID_FIELD_NUMBER: _ClassVar[int]
    state: ExperimentState
    time_of_state_change: int
    reason: str
    status_id: str
    def __init__(self, state: _Optional[_Union[ExperimentState, str]] = ..., time_of_state_change: _Optional[int] = ..., reason: _Optional[str] = ..., status_id: _Optional[str] = ...) -> None: ...

class ProcessStatus(_message.Message):
    __slots__ = ("state", "time_of_state_change", "reason", "status_id", "process_id")
    STATE_FIELD_NUMBER: _ClassVar[int]
    TIME_OF_STATE_CHANGE_FIELD_NUMBER: _ClassVar[int]
    REASON_FIELD_NUMBER: _ClassVar[int]
    STATUS_ID_FIELD_NUMBER: _ClassVar[int]
    PROCESS_ID_FIELD_NUMBER: _ClassVar[int]
    state: ProcessState
    time_of_state_change: int
    reason: str
    status_id: str
    process_id: str
    def __init__(self, state: _Optional[_Union[ProcessState, str]] = ..., time_of_state_change: _Optional[int] = ..., reason: _Optional[str] = ..., status_id: _Optional[str] = ..., process_id: _Optional[str] = ...) -> None: ...

class TaskStatus(_message.Message):
    __slots__ = ("state", "time_of_state_change", "reason", "status_id")
    STATE_FIELD_NUMBER: _ClassVar[int]
    TIME_OF_STATE_CHANGE_FIELD_NUMBER: _ClassVar[int]
    REASON_FIELD_NUMBER: _ClassVar[int]
    STATUS_ID_FIELD_NUMBER: _ClassVar[int]
    state: TaskState
    time_of_state_change: int
    reason: str
    status_id: str
    def __init__(self, state: _Optional[_Union[TaskState, str]] = ..., time_of_state_change: _Optional[int] = ..., reason: _Optional[str] = ..., status_id: _Optional[str] = ...) -> None: ...

class JobStatus(_message.Message):
    __slots__ = ("job_state", "time_of_state_change", "reason", "status_id")
    JOB_STATE_FIELD_NUMBER: _ClassVar[int]
    TIME_OF_STATE_CHANGE_FIELD_NUMBER: _ClassVar[int]
    REASON_FIELD_NUMBER: _ClassVar[int]
    STATUS_ID_FIELD_NUMBER: _ClassVar[int]
    job_state: JobState
    time_of_state_change: int
    reason: str
    status_id: str
    def __init__(self, job_state: _Optional[_Union[JobState, str]] = ..., time_of_state_change: _Optional[int] = ..., reason: _Optional[str] = ..., status_id: _Optional[str] = ...) -> None: ...

class QueueStatusModel(_message.Message):
    __slots__ = ("host_name", "queue_name", "queue_up", "running_jobs", "queued_jobs", "time")
    HOST_NAME_FIELD_NUMBER: _ClassVar[int]
    QUEUE_NAME_FIELD_NUMBER: _ClassVar[int]
    QUEUE_UP_FIELD_NUMBER: _ClassVar[int]
    RUNNING_JOBS_FIELD_NUMBER: _ClassVar[int]
    QUEUED_JOBS_FIELD_NUMBER: _ClassVar[int]
    TIME_FIELD_NUMBER: _ClassVar[int]
    host_name: str
    queue_name: str
    queue_up: bool
    running_jobs: int
    queued_jobs: int
    time: int
    def __init__(self, host_name: _Optional[str] = ..., queue_name: _Optional[str] = ..., queue_up: bool = ..., running_jobs: _Optional[int] = ..., queued_jobs: _Optional[int] = ..., time: _Optional[int] = ...) -> None: ...
