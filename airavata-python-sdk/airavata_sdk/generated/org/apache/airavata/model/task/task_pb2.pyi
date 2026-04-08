from org.apache.airavata.model.commons import commons_pb2 as _commons_pb2
from org.apache.airavata.model.application.io import application_io_pb2 as _application_io_pb2
from org.apache.airavata.model.appcatalog.computeresource import compute_resource_pb2 as _compute_resource_pb2
from org.apache.airavata.model.data.movement import data_movement_pb2 as _data_movement_pb2
from org.apache.airavata.model.job import job_pb2 as _job_pb2
from org.apache.airavata.model.status import status_pb2 as _status_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class TaskTypes(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    TASK_TYPES_UNKNOWN: _ClassVar[TaskTypes]
    ENV_SETUP: _ClassVar[TaskTypes]
    DATA_STAGING: _ClassVar[TaskTypes]
    JOB_SUBMISSION: _ClassVar[TaskTypes]
    ENV_CLEANUP: _ClassVar[TaskTypes]
    MONITORING: _ClassVar[TaskTypes]
    OUTPUT_FETCHING: _ClassVar[TaskTypes]

class DataStageType(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    DATA_STAGE_TYPE_UNKNOWN: _ClassVar[DataStageType]
    INPUT: _ClassVar[DataStageType]
    OUPUT: _ClassVar[DataStageType]
    ARCHIVE_OUTPUT: _ClassVar[DataStageType]
TASK_TYPES_UNKNOWN: TaskTypes
ENV_SETUP: TaskTypes
DATA_STAGING: TaskTypes
JOB_SUBMISSION: TaskTypes
ENV_CLEANUP: TaskTypes
MONITORING: TaskTypes
OUTPUT_FETCHING: TaskTypes
DATA_STAGE_TYPE_UNKNOWN: DataStageType
INPUT: DataStageType
OUPUT: DataStageType
ARCHIVE_OUTPUT: DataStageType

class TaskModel(_message.Message):
    __slots__ = ("task_id", "task_type", "parent_process_id", "creation_time", "last_update_time", "task_statuses", "task_detail", "sub_task_model", "task_errors", "jobs", "max_retry", "current_retry")
    TASK_ID_FIELD_NUMBER: _ClassVar[int]
    TASK_TYPE_FIELD_NUMBER: _ClassVar[int]
    PARENT_PROCESS_ID_FIELD_NUMBER: _ClassVar[int]
    CREATION_TIME_FIELD_NUMBER: _ClassVar[int]
    LAST_UPDATE_TIME_FIELD_NUMBER: _ClassVar[int]
    TASK_STATUSES_FIELD_NUMBER: _ClassVar[int]
    TASK_DETAIL_FIELD_NUMBER: _ClassVar[int]
    SUB_TASK_MODEL_FIELD_NUMBER: _ClassVar[int]
    TASK_ERRORS_FIELD_NUMBER: _ClassVar[int]
    JOBS_FIELD_NUMBER: _ClassVar[int]
    MAX_RETRY_FIELD_NUMBER: _ClassVar[int]
    CURRENT_RETRY_FIELD_NUMBER: _ClassVar[int]
    task_id: str
    task_type: TaskTypes
    parent_process_id: str
    creation_time: int
    last_update_time: int
    task_statuses: _containers.RepeatedCompositeFieldContainer[_status_pb2.TaskStatus]
    task_detail: str
    sub_task_model: bytes
    task_errors: _containers.RepeatedCompositeFieldContainer[_commons_pb2.ErrorModel]
    jobs: _containers.RepeatedCompositeFieldContainer[_job_pb2.JobModel]
    max_retry: int
    current_retry: int
    def __init__(self, task_id: _Optional[str] = ..., task_type: _Optional[_Union[TaskTypes, str]] = ..., parent_process_id: _Optional[str] = ..., creation_time: _Optional[int] = ..., last_update_time: _Optional[int] = ..., task_statuses: _Optional[_Iterable[_Union[_status_pb2.TaskStatus, _Mapping]]] = ..., task_detail: _Optional[str] = ..., sub_task_model: _Optional[bytes] = ..., task_errors: _Optional[_Iterable[_Union[_commons_pb2.ErrorModel, _Mapping]]] = ..., jobs: _Optional[_Iterable[_Union[_job_pb2.JobModel, _Mapping]]] = ..., max_retry: _Optional[int] = ..., current_retry: _Optional[int] = ...) -> None: ...

class DataStagingTaskModel(_message.Message):
    __slots__ = ("source", "destination", "type", "transfer_start_time", "transfer_end_time", "transfer_rate", "process_input", "process_output")
    SOURCE_FIELD_NUMBER: _ClassVar[int]
    DESTINATION_FIELD_NUMBER: _ClassVar[int]
    TYPE_FIELD_NUMBER: _ClassVar[int]
    TRANSFER_START_TIME_FIELD_NUMBER: _ClassVar[int]
    TRANSFER_END_TIME_FIELD_NUMBER: _ClassVar[int]
    TRANSFER_RATE_FIELD_NUMBER: _ClassVar[int]
    PROCESS_INPUT_FIELD_NUMBER: _ClassVar[int]
    PROCESS_OUTPUT_FIELD_NUMBER: _ClassVar[int]
    source: str
    destination: str
    type: DataStageType
    transfer_start_time: int
    transfer_end_time: int
    transfer_rate: str
    process_input: _application_io_pb2.InputDataObjectType
    process_output: _application_io_pb2.OutputDataObjectType
    def __init__(self, source: _Optional[str] = ..., destination: _Optional[str] = ..., type: _Optional[_Union[DataStageType, str]] = ..., transfer_start_time: _Optional[int] = ..., transfer_end_time: _Optional[int] = ..., transfer_rate: _Optional[str] = ..., process_input: _Optional[_Union[_application_io_pb2.InputDataObjectType, _Mapping]] = ..., process_output: _Optional[_Union[_application_io_pb2.OutputDataObjectType, _Mapping]] = ...) -> None: ...

class EnvironmentSetupTaskModel(_message.Message):
    __slots__ = ("location", "protocol")
    LOCATION_FIELD_NUMBER: _ClassVar[int]
    PROTOCOL_FIELD_NUMBER: _ClassVar[int]
    location: str
    protocol: _data_movement_pb2.SecurityProtocol
    def __init__(self, location: _Optional[str] = ..., protocol: _Optional[_Union[_data_movement_pb2.SecurityProtocol, str]] = ...) -> None: ...

class JobSubmissionTaskModel(_message.Message):
    __slots__ = ("job_submission_protocol", "monitor_mode", "wall_time")
    JOB_SUBMISSION_PROTOCOL_FIELD_NUMBER: _ClassVar[int]
    MONITOR_MODE_FIELD_NUMBER: _ClassVar[int]
    WALL_TIME_FIELD_NUMBER: _ClassVar[int]
    job_submission_protocol: _compute_resource_pb2.JobSubmissionProtocol
    monitor_mode: _compute_resource_pb2.MonitorMode
    wall_time: int
    def __init__(self, job_submission_protocol: _Optional[_Union[_compute_resource_pb2.JobSubmissionProtocol, str]] = ..., monitor_mode: _Optional[_Union[_compute_resource_pb2.MonitorMode, str]] = ..., wall_time: _Optional[int] = ...) -> None: ...

class MonitorTaskModel(_message.Message):
    __slots__ = ("monitor_mode",)
    MONITOR_MODE_FIELD_NUMBER: _ClassVar[int]
    monitor_mode: _compute_resource_pb2.MonitorMode
    def __init__(self, monitor_mode: _Optional[_Union[_compute_resource_pb2.MonitorMode, str]] = ...) -> None: ...
