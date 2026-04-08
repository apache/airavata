from org.apache.airavata.model.status import status_pb2 as _status_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class JobModel(_message.Message):
    __slots__ = ("job_id", "task_id", "process_id", "job_description", "creation_time", "job_statuses", "compute_resource_consumed", "job_name", "working_dir", "std_out", "std_err", "exit_code")
    JOB_ID_FIELD_NUMBER: _ClassVar[int]
    TASK_ID_FIELD_NUMBER: _ClassVar[int]
    PROCESS_ID_FIELD_NUMBER: _ClassVar[int]
    JOB_DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    CREATION_TIME_FIELD_NUMBER: _ClassVar[int]
    JOB_STATUSES_FIELD_NUMBER: _ClassVar[int]
    COMPUTE_RESOURCE_CONSUMED_FIELD_NUMBER: _ClassVar[int]
    JOB_NAME_FIELD_NUMBER: _ClassVar[int]
    WORKING_DIR_FIELD_NUMBER: _ClassVar[int]
    STD_OUT_FIELD_NUMBER: _ClassVar[int]
    STD_ERR_FIELD_NUMBER: _ClassVar[int]
    EXIT_CODE_FIELD_NUMBER: _ClassVar[int]
    job_id: str
    task_id: str
    process_id: str
    job_description: str
    creation_time: int
    job_statuses: _containers.RepeatedCompositeFieldContainer[_status_pb2.JobStatus]
    compute_resource_consumed: str
    job_name: str
    working_dir: str
    std_out: str
    std_err: str
    exit_code: int
    def __init__(self, job_id: _Optional[str] = ..., task_id: _Optional[str] = ..., process_id: _Optional[str] = ..., job_description: _Optional[str] = ..., creation_time: _Optional[int] = ..., job_statuses: _Optional[_Iterable[_Union[_status_pb2.JobStatus, _Mapping]]] = ..., compute_resource_consumed: _Optional[str] = ..., job_name: _Optional[str] = ..., working_dir: _Optional[str] = ..., std_out: _Optional[str] = ..., std_err: _Optional[str] = ..., exit_code: _Optional[int] = ...) -> None: ...
