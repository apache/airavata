from org.apache.airavata.model.commons import commons_pb2 as _commons_pb2
from org.apache.airavata.model.application.io import application_io_pb2 as _application_io_pb2
from org.apache.airavata.model.scheduling import scheduling_pb2 as _scheduling_pb2
from org.apache.airavata.model.status import status_pb2 as _status_pb2
from org.apache.airavata.model.task import task_pb2 as _task_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class ProcessWorkflow(_message.Message):
    __slots__ = ("process_id", "workflow_id", "creation_time", "type")
    PROCESS_ID_FIELD_NUMBER: _ClassVar[int]
    WORKFLOW_ID_FIELD_NUMBER: _ClassVar[int]
    CREATION_TIME_FIELD_NUMBER: _ClassVar[int]
    TYPE_FIELD_NUMBER: _ClassVar[int]
    process_id: str
    workflow_id: str
    creation_time: int
    type: str
    def __init__(self, process_id: _Optional[str] = ..., workflow_id: _Optional[str] = ..., creation_time: _Optional[int] = ..., type: _Optional[str] = ...) -> None: ...

class ProcessModel(_message.Message):
    __slots__ = ("process_id", "experiment_id", "creation_time", "last_update_time", "process_statuses", "process_detail", "application_interface_id", "application_deployment_id", "compute_resource_id", "process_inputs", "process_outputs", "process_resource_schedule", "tasks", "task_dag", "process_errors", "gateway_execution_id", "enable_email_notification", "email_addresses", "input_storage_resource_id", "output_storage_resource_id", "user_dn", "generate_cert", "experiment_data_dir", "user_name", "use_user_cr_pref", "group_resource_profile_id", "process_workflows")
    PROCESS_ID_FIELD_NUMBER: _ClassVar[int]
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    CREATION_TIME_FIELD_NUMBER: _ClassVar[int]
    LAST_UPDATE_TIME_FIELD_NUMBER: _ClassVar[int]
    PROCESS_STATUSES_FIELD_NUMBER: _ClassVar[int]
    PROCESS_DETAIL_FIELD_NUMBER: _ClassVar[int]
    APPLICATION_INTERFACE_ID_FIELD_NUMBER: _ClassVar[int]
    APPLICATION_DEPLOYMENT_ID_FIELD_NUMBER: _ClassVar[int]
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    PROCESS_INPUTS_FIELD_NUMBER: _ClassVar[int]
    PROCESS_OUTPUTS_FIELD_NUMBER: _ClassVar[int]
    PROCESS_RESOURCE_SCHEDULE_FIELD_NUMBER: _ClassVar[int]
    TASKS_FIELD_NUMBER: _ClassVar[int]
    TASK_DAG_FIELD_NUMBER: _ClassVar[int]
    PROCESS_ERRORS_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_EXECUTION_ID_FIELD_NUMBER: _ClassVar[int]
    ENABLE_EMAIL_NOTIFICATION_FIELD_NUMBER: _ClassVar[int]
    EMAIL_ADDRESSES_FIELD_NUMBER: _ClassVar[int]
    INPUT_STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    OUTPUT_STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    USER_DN_FIELD_NUMBER: _ClassVar[int]
    GENERATE_CERT_FIELD_NUMBER: _ClassVar[int]
    EXPERIMENT_DATA_DIR_FIELD_NUMBER: _ClassVar[int]
    USER_NAME_FIELD_NUMBER: _ClassVar[int]
    USE_USER_CR_PREF_FIELD_NUMBER: _ClassVar[int]
    GROUP_RESOURCE_PROFILE_ID_FIELD_NUMBER: _ClassVar[int]
    PROCESS_WORKFLOWS_FIELD_NUMBER: _ClassVar[int]
    process_id: str
    experiment_id: str
    creation_time: int
    last_update_time: int
    process_statuses: _containers.RepeatedCompositeFieldContainer[_status_pb2.ProcessStatus]
    process_detail: str
    application_interface_id: str
    application_deployment_id: str
    compute_resource_id: str
    process_inputs: _containers.RepeatedCompositeFieldContainer[_application_io_pb2.InputDataObjectType]
    process_outputs: _containers.RepeatedCompositeFieldContainer[_application_io_pb2.OutputDataObjectType]
    process_resource_schedule: _scheduling_pb2.ComputationalResourceSchedulingModel
    tasks: _containers.RepeatedCompositeFieldContainer[_task_pb2.TaskModel]
    task_dag: str
    process_errors: _containers.RepeatedCompositeFieldContainer[_commons_pb2.ErrorModel]
    gateway_execution_id: str
    enable_email_notification: bool
    email_addresses: _containers.RepeatedScalarFieldContainer[str]
    input_storage_resource_id: str
    output_storage_resource_id: str
    user_dn: str
    generate_cert: bool
    experiment_data_dir: str
    user_name: str
    use_user_cr_pref: bool
    group_resource_profile_id: str
    process_workflows: _containers.RepeatedCompositeFieldContainer[ProcessWorkflow]
    def __init__(self, process_id: _Optional[str] = ..., experiment_id: _Optional[str] = ..., creation_time: _Optional[int] = ..., last_update_time: _Optional[int] = ..., process_statuses: _Optional[_Iterable[_Union[_status_pb2.ProcessStatus, _Mapping]]] = ..., process_detail: _Optional[str] = ..., application_interface_id: _Optional[str] = ..., application_deployment_id: _Optional[str] = ..., compute_resource_id: _Optional[str] = ..., process_inputs: _Optional[_Iterable[_Union[_application_io_pb2.InputDataObjectType, _Mapping]]] = ..., process_outputs: _Optional[_Iterable[_Union[_application_io_pb2.OutputDataObjectType, _Mapping]]] = ..., process_resource_schedule: _Optional[_Union[_scheduling_pb2.ComputationalResourceSchedulingModel, _Mapping]] = ..., tasks: _Optional[_Iterable[_Union[_task_pb2.TaskModel, _Mapping]]] = ..., task_dag: _Optional[str] = ..., process_errors: _Optional[_Iterable[_Union[_commons_pb2.ErrorModel, _Mapping]]] = ..., gateway_execution_id: _Optional[str] = ..., enable_email_notification: bool = ..., email_addresses: _Optional[_Iterable[str]] = ..., input_storage_resource_id: _Optional[str] = ..., output_storage_resource_id: _Optional[str] = ..., user_dn: _Optional[str] = ..., generate_cert: bool = ..., experiment_data_dir: _Optional[str] = ..., user_name: _Optional[str] = ..., use_user_cr_pref: bool = ..., group_resource_profile_id: _Optional[str] = ..., process_workflows: _Optional[_Iterable[_Union[ProcessWorkflow, _Mapping]]] = ...) -> None: ...
