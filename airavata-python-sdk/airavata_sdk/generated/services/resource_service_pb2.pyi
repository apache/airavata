from google.api import annotations_pb2 as _annotations_pb2
from google.protobuf import empty_pb2 as _empty_pb2
from org.apache.airavata.model.appcatalog.computeresource import compute_resource_pb2 as _compute_resource_pb2
from org.apache.airavata.model.appcatalog.storageresource import storage_resource_pb2 as _storage_resource_pb2
from org.apache.airavata.model.data.movement import data_movement_pb2 as _data_movement_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class RegisterComputeResourceRequest(_message.Message):
    __slots__ = ("compute_resource",)
    COMPUTE_RESOURCE_FIELD_NUMBER: _ClassVar[int]
    compute_resource: _compute_resource_pb2.ComputeResourceDescription
    def __init__(self, compute_resource: _Optional[_Union[_compute_resource_pb2.ComputeResourceDescription, _Mapping]] = ...) -> None: ...

class RegisterComputeResourceResponse(_message.Message):
    __slots__ = ("compute_resource_id",)
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    compute_resource_id: str
    def __init__(self, compute_resource_id: _Optional[str] = ...) -> None: ...

class GetComputeResourceRequest(_message.Message):
    __slots__ = ("compute_resource_id",)
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    compute_resource_id: str
    def __init__(self, compute_resource_id: _Optional[str] = ...) -> None: ...

class UpdateComputeResourceRequest(_message.Message):
    __slots__ = ("compute_resource_id", "compute_resource")
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    COMPUTE_RESOURCE_FIELD_NUMBER: _ClassVar[int]
    compute_resource_id: str
    compute_resource: _compute_resource_pb2.ComputeResourceDescription
    def __init__(self, compute_resource_id: _Optional[str] = ..., compute_resource: _Optional[_Union[_compute_resource_pb2.ComputeResourceDescription, _Mapping]] = ...) -> None: ...

class DeleteComputeResourceRequest(_message.Message):
    __slots__ = ("compute_resource_id",)
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    compute_resource_id: str
    def __init__(self, compute_resource_id: _Optional[str] = ...) -> None: ...

class GetAllComputeResourceNamesRequest(_message.Message):
    __slots__ = ()
    def __init__(self) -> None: ...

class GetAllComputeResourceNamesResponse(_message.Message):
    __slots__ = ("compute_resource_names",)
    class ComputeResourceNamesEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: str
        def __init__(self, key: _Optional[str] = ..., value: _Optional[str] = ...) -> None: ...
    COMPUTE_RESOURCE_NAMES_FIELD_NUMBER: _ClassVar[int]
    compute_resource_names: _containers.ScalarMap[str, str]
    def __init__(self, compute_resource_names: _Optional[_Mapping[str, str]] = ...) -> None: ...

class RegisterStorageResourceRequest(_message.Message):
    __slots__ = ("storage_resource",)
    STORAGE_RESOURCE_FIELD_NUMBER: _ClassVar[int]
    storage_resource: _storage_resource_pb2.StorageResourceDescription
    def __init__(self, storage_resource: _Optional[_Union[_storage_resource_pb2.StorageResourceDescription, _Mapping]] = ...) -> None: ...

class RegisterStorageResourceResponse(_message.Message):
    __slots__ = ("storage_resource_id",)
    STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    storage_resource_id: str
    def __init__(self, storage_resource_id: _Optional[str] = ...) -> None: ...

class GetStorageResourceRequest(_message.Message):
    __slots__ = ("storage_resource_id",)
    STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    storage_resource_id: str
    def __init__(self, storage_resource_id: _Optional[str] = ...) -> None: ...

class UpdateStorageResourceRequest(_message.Message):
    __slots__ = ("storage_resource_id", "storage_resource")
    STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    STORAGE_RESOURCE_FIELD_NUMBER: _ClassVar[int]
    storage_resource_id: str
    storage_resource: _storage_resource_pb2.StorageResourceDescription
    def __init__(self, storage_resource_id: _Optional[str] = ..., storage_resource: _Optional[_Union[_storage_resource_pb2.StorageResourceDescription, _Mapping]] = ...) -> None: ...

class DeleteStorageResourceRequest(_message.Message):
    __slots__ = ("storage_resource_id",)
    STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    storage_resource_id: str
    def __init__(self, storage_resource_id: _Optional[str] = ...) -> None: ...

class GetAllStorageResourceNamesRequest(_message.Message):
    __slots__ = ()
    def __init__(self) -> None: ...

class GetAllStorageResourceNamesResponse(_message.Message):
    __slots__ = ("storage_resource_names",)
    class StorageResourceNamesEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: str
        def __init__(self, key: _Optional[str] = ..., value: _Optional[str] = ...) -> None: ...
    STORAGE_RESOURCE_NAMES_FIELD_NUMBER: _ClassVar[int]
    storage_resource_names: _containers.ScalarMap[str, str]
    def __init__(self, storage_resource_names: _Optional[_Mapping[str, str]] = ...) -> None: ...

class AddLocalSubmissionRequest(_message.Message):
    __slots__ = ("compute_resource_id", "priority", "local_submission")
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    PRIORITY_FIELD_NUMBER: _ClassVar[int]
    LOCAL_SUBMISSION_FIELD_NUMBER: _ClassVar[int]
    compute_resource_id: str
    priority: int
    local_submission: _compute_resource_pb2.LOCALSubmission
    def __init__(self, compute_resource_id: _Optional[str] = ..., priority: _Optional[int] = ..., local_submission: _Optional[_Union[_compute_resource_pb2.LOCALSubmission, _Mapping]] = ...) -> None: ...

class AddLocalSubmissionResponse(_message.Message):
    __slots__ = ("submission_id",)
    SUBMISSION_ID_FIELD_NUMBER: _ClassVar[int]
    submission_id: str
    def __init__(self, submission_id: _Optional[str] = ...) -> None: ...

class UpdateLocalSubmissionRequest(_message.Message):
    __slots__ = ("submission_id", "local_submission")
    SUBMISSION_ID_FIELD_NUMBER: _ClassVar[int]
    LOCAL_SUBMISSION_FIELD_NUMBER: _ClassVar[int]
    submission_id: str
    local_submission: _compute_resource_pb2.LOCALSubmission
    def __init__(self, submission_id: _Optional[str] = ..., local_submission: _Optional[_Union[_compute_resource_pb2.LOCALSubmission, _Mapping]] = ...) -> None: ...

class GetLocalJobSubmissionRequest(_message.Message):
    __slots__ = ("submission_id",)
    SUBMISSION_ID_FIELD_NUMBER: _ClassVar[int]
    submission_id: str
    def __init__(self, submission_id: _Optional[str] = ...) -> None: ...

class AddSSHJobSubmissionRequest(_message.Message):
    __slots__ = ("compute_resource_id", "priority", "ssh_job_submission")
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    PRIORITY_FIELD_NUMBER: _ClassVar[int]
    SSH_JOB_SUBMISSION_FIELD_NUMBER: _ClassVar[int]
    compute_resource_id: str
    priority: int
    ssh_job_submission: _compute_resource_pb2.SSHJobSubmission
    def __init__(self, compute_resource_id: _Optional[str] = ..., priority: _Optional[int] = ..., ssh_job_submission: _Optional[_Union[_compute_resource_pb2.SSHJobSubmission, _Mapping]] = ...) -> None: ...

class AddSSHJobSubmissionResponse(_message.Message):
    __slots__ = ("submission_id",)
    SUBMISSION_ID_FIELD_NUMBER: _ClassVar[int]
    submission_id: str
    def __init__(self, submission_id: _Optional[str] = ...) -> None: ...

class AddSSHForkJobSubmissionRequest(_message.Message):
    __slots__ = ("compute_resource_id", "priority", "ssh_job_submission")
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    PRIORITY_FIELD_NUMBER: _ClassVar[int]
    SSH_JOB_SUBMISSION_FIELD_NUMBER: _ClassVar[int]
    compute_resource_id: str
    priority: int
    ssh_job_submission: _compute_resource_pb2.SSHJobSubmission
    def __init__(self, compute_resource_id: _Optional[str] = ..., priority: _Optional[int] = ..., ssh_job_submission: _Optional[_Union[_compute_resource_pb2.SSHJobSubmission, _Mapping]] = ...) -> None: ...

class AddSSHForkJobSubmissionResponse(_message.Message):
    __slots__ = ("submission_id",)
    SUBMISSION_ID_FIELD_NUMBER: _ClassVar[int]
    submission_id: str
    def __init__(self, submission_id: _Optional[str] = ...) -> None: ...

class GetSSHJobSubmissionRequest(_message.Message):
    __slots__ = ("submission_id",)
    SUBMISSION_ID_FIELD_NUMBER: _ClassVar[int]
    submission_id: str
    def __init__(self, submission_id: _Optional[str] = ...) -> None: ...

class UpdateSSHJobSubmissionRequest(_message.Message):
    __slots__ = ("submission_id", "ssh_job_submission")
    SUBMISSION_ID_FIELD_NUMBER: _ClassVar[int]
    SSH_JOB_SUBMISSION_FIELD_NUMBER: _ClassVar[int]
    submission_id: str
    ssh_job_submission: _compute_resource_pb2.SSHJobSubmission
    def __init__(self, submission_id: _Optional[str] = ..., ssh_job_submission: _Optional[_Union[_compute_resource_pb2.SSHJobSubmission, _Mapping]] = ...) -> None: ...

class AddCloudJobSubmissionRequest(_message.Message):
    __slots__ = ("compute_resource_id", "priority", "cloud_job_submission")
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    PRIORITY_FIELD_NUMBER: _ClassVar[int]
    CLOUD_JOB_SUBMISSION_FIELD_NUMBER: _ClassVar[int]
    compute_resource_id: str
    priority: int
    cloud_job_submission: _compute_resource_pb2.CloudJobSubmission
    def __init__(self, compute_resource_id: _Optional[str] = ..., priority: _Optional[int] = ..., cloud_job_submission: _Optional[_Union[_compute_resource_pb2.CloudJobSubmission, _Mapping]] = ...) -> None: ...

class AddCloudJobSubmissionResponse(_message.Message):
    __slots__ = ("submission_id",)
    SUBMISSION_ID_FIELD_NUMBER: _ClassVar[int]
    submission_id: str
    def __init__(self, submission_id: _Optional[str] = ...) -> None: ...

class GetCloudJobSubmissionRequest(_message.Message):
    __slots__ = ("submission_id",)
    SUBMISSION_ID_FIELD_NUMBER: _ClassVar[int]
    submission_id: str
    def __init__(self, submission_id: _Optional[str] = ...) -> None: ...

class UpdateCloudJobSubmissionRequest(_message.Message):
    __slots__ = ("submission_id", "cloud_job_submission")
    SUBMISSION_ID_FIELD_NUMBER: _ClassVar[int]
    CLOUD_JOB_SUBMISSION_FIELD_NUMBER: _ClassVar[int]
    submission_id: str
    cloud_job_submission: _compute_resource_pb2.CloudJobSubmission
    def __init__(self, submission_id: _Optional[str] = ..., cloud_job_submission: _Optional[_Union[_compute_resource_pb2.CloudJobSubmission, _Mapping]] = ...) -> None: ...

class AddUnicoreJobSubmissionRequest(_message.Message):
    __slots__ = ("compute_resource_id", "priority", "unicore_job_submission")
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    PRIORITY_FIELD_NUMBER: _ClassVar[int]
    UNICORE_JOB_SUBMISSION_FIELD_NUMBER: _ClassVar[int]
    compute_resource_id: str
    priority: int
    unicore_job_submission: _compute_resource_pb2.UnicoreJobSubmission
    def __init__(self, compute_resource_id: _Optional[str] = ..., priority: _Optional[int] = ..., unicore_job_submission: _Optional[_Union[_compute_resource_pb2.UnicoreJobSubmission, _Mapping]] = ...) -> None: ...

class AddUnicoreJobSubmissionResponse(_message.Message):
    __slots__ = ("submission_id",)
    SUBMISSION_ID_FIELD_NUMBER: _ClassVar[int]
    submission_id: str
    def __init__(self, submission_id: _Optional[str] = ...) -> None: ...

class GetUnicoreJobSubmissionRequest(_message.Message):
    __slots__ = ("submission_id",)
    SUBMISSION_ID_FIELD_NUMBER: _ClassVar[int]
    submission_id: str
    def __init__(self, submission_id: _Optional[str] = ...) -> None: ...

class UpdateUnicoreJobSubmissionRequest(_message.Message):
    __slots__ = ("submission_id", "unicore_job_submission")
    SUBMISSION_ID_FIELD_NUMBER: _ClassVar[int]
    UNICORE_JOB_SUBMISSION_FIELD_NUMBER: _ClassVar[int]
    submission_id: str
    unicore_job_submission: _compute_resource_pb2.UnicoreJobSubmission
    def __init__(self, submission_id: _Optional[str] = ..., unicore_job_submission: _Optional[_Union[_compute_resource_pb2.UnicoreJobSubmission, _Mapping]] = ...) -> None: ...

class DeleteJobSubmissionInterfaceRequest(_message.Message):
    __slots__ = ("compute_resource_id", "submission_id")
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    SUBMISSION_ID_FIELD_NUMBER: _ClassVar[int]
    compute_resource_id: str
    submission_id: str
    def __init__(self, compute_resource_id: _Optional[str] = ..., submission_id: _Optional[str] = ...) -> None: ...

class AddLocalDataMovementRequest(_message.Message):
    __slots__ = ("compute_resource_id", "priority", "dm_type", "local_data_movement")
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    PRIORITY_FIELD_NUMBER: _ClassVar[int]
    DM_TYPE_FIELD_NUMBER: _ClassVar[int]
    LOCAL_DATA_MOVEMENT_FIELD_NUMBER: _ClassVar[int]
    compute_resource_id: str
    priority: int
    dm_type: str
    local_data_movement: _data_movement_pb2.LOCALDataMovement
    def __init__(self, compute_resource_id: _Optional[str] = ..., priority: _Optional[int] = ..., dm_type: _Optional[str] = ..., local_data_movement: _Optional[_Union[_data_movement_pb2.LOCALDataMovement, _Mapping]] = ...) -> None: ...

class AddLocalDataMovementResponse(_message.Message):
    __slots__ = ("data_movement_id",)
    DATA_MOVEMENT_ID_FIELD_NUMBER: _ClassVar[int]
    data_movement_id: str
    def __init__(self, data_movement_id: _Optional[str] = ...) -> None: ...

class UpdateLocalDataMovementRequest(_message.Message):
    __slots__ = ("data_movement_id", "local_data_movement")
    DATA_MOVEMENT_ID_FIELD_NUMBER: _ClassVar[int]
    LOCAL_DATA_MOVEMENT_FIELD_NUMBER: _ClassVar[int]
    data_movement_id: str
    local_data_movement: _data_movement_pb2.LOCALDataMovement
    def __init__(self, data_movement_id: _Optional[str] = ..., local_data_movement: _Optional[_Union[_data_movement_pb2.LOCALDataMovement, _Mapping]] = ...) -> None: ...

class GetLocalDataMovementRequest(_message.Message):
    __slots__ = ("data_movement_id",)
    DATA_MOVEMENT_ID_FIELD_NUMBER: _ClassVar[int]
    data_movement_id: str
    def __init__(self, data_movement_id: _Optional[str] = ...) -> None: ...

class AddSCPDataMovementRequest(_message.Message):
    __slots__ = ("compute_resource_id", "priority", "dm_type", "scp_data_movement")
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    PRIORITY_FIELD_NUMBER: _ClassVar[int]
    DM_TYPE_FIELD_NUMBER: _ClassVar[int]
    SCP_DATA_MOVEMENT_FIELD_NUMBER: _ClassVar[int]
    compute_resource_id: str
    priority: int
    dm_type: str
    scp_data_movement: _data_movement_pb2.SCPDataMovement
    def __init__(self, compute_resource_id: _Optional[str] = ..., priority: _Optional[int] = ..., dm_type: _Optional[str] = ..., scp_data_movement: _Optional[_Union[_data_movement_pb2.SCPDataMovement, _Mapping]] = ...) -> None: ...

class AddSCPDataMovementResponse(_message.Message):
    __slots__ = ("data_movement_id",)
    DATA_MOVEMENT_ID_FIELD_NUMBER: _ClassVar[int]
    data_movement_id: str
    def __init__(self, data_movement_id: _Optional[str] = ...) -> None: ...

class UpdateSCPDataMovementRequest(_message.Message):
    __slots__ = ("data_movement_id", "scp_data_movement")
    DATA_MOVEMENT_ID_FIELD_NUMBER: _ClassVar[int]
    SCP_DATA_MOVEMENT_FIELD_NUMBER: _ClassVar[int]
    data_movement_id: str
    scp_data_movement: _data_movement_pb2.SCPDataMovement
    def __init__(self, data_movement_id: _Optional[str] = ..., scp_data_movement: _Optional[_Union[_data_movement_pb2.SCPDataMovement, _Mapping]] = ...) -> None: ...

class GetSCPDataMovementRequest(_message.Message):
    __slots__ = ("data_movement_id",)
    DATA_MOVEMENT_ID_FIELD_NUMBER: _ClassVar[int]
    data_movement_id: str
    def __init__(self, data_movement_id: _Optional[str] = ...) -> None: ...

class AddGridFTPDataMovementRequest(_message.Message):
    __slots__ = ("compute_resource_id", "priority", "dm_type", "gridftp_data_movement")
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    PRIORITY_FIELD_NUMBER: _ClassVar[int]
    DM_TYPE_FIELD_NUMBER: _ClassVar[int]
    GRIDFTP_DATA_MOVEMENT_FIELD_NUMBER: _ClassVar[int]
    compute_resource_id: str
    priority: int
    dm_type: str
    gridftp_data_movement: _data_movement_pb2.GridFTPDataMovement
    def __init__(self, compute_resource_id: _Optional[str] = ..., priority: _Optional[int] = ..., dm_type: _Optional[str] = ..., gridftp_data_movement: _Optional[_Union[_data_movement_pb2.GridFTPDataMovement, _Mapping]] = ...) -> None: ...

class AddGridFTPDataMovementResponse(_message.Message):
    __slots__ = ("data_movement_id",)
    DATA_MOVEMENT_ID_FIELD_NUMBER: _ClassVar[int]
    data_movement_id: str
    def __init__(self, data_movement_id: _Optional[str] = ...) -> None: ...

class UpdateGridFTPDataMovementRequest(_message.Message):
    __slots__ = ("data_movement_id", "gridftp_data_movement")
    DATA_MOVEMENT_ID_FIELD_NUMBER: _ClassVar[int]
    GRIDFTP_DATA_MOVEMENT_FIELD_NUMBER: _ClassVar[int]
    data_movement_id: str
    gridftp_data_movement: _data_movement_pb2.GridFTPDataMovement
    def __init__(self, data_movement_id: _Optional[str] = ..., gridftp_data_movement: _Optional[_Union[_data_movement_pb2.GridFTPDataMovement, _Mapping]] = ...) -> None: ...

class GetGridFTPDataMovementRequest(_message.Message):
    __slots__ = ("data_movement_id",)
    DATA_MOVEMENT_ID_FIELD_NUMBER: _ClassVar[int]
    data_movement_id: str
    def __init__(self, data_movement_id: _Optional[str] = ...) -> None: ...

class DeleteDataMovementInterfaceRequest(_message.Message):
    __slots__ = ("compute_resource_id", "data_movement_id")
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    DATA_MOVEMENT_ID_FIELD_NUMBER: _ClassVar[int]
    compute_resource_id: str
    data_movement_id: str
    def __init__(self, compute_resource_id: _Optional[str] = ..., data_movement_id: _Optional[str] = ...) -> None: ...

class DeleteBatchQueueRequest(_message.Message):
    __slots__ = ("compute_resource_id", "queue_name")
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    QUEUE_NAME_FIELD_NUMBER: _ClassVar[int]
    compute_resource_id: str
    queue_name: str
    def __init__(self, compute_resource_id: _Optional[str] = ..., queue_name: _Optional[str] = ...) -> None: ...
