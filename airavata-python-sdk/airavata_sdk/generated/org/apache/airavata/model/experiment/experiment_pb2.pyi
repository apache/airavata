from org.apache.airavata.model.commons import commons_pb2 as _commons_pb2
from org.apache.airavata.model.application.io import application_io_pb2 as _application_io_pb2
from org.apache.airavata.model.scheduling import scheduling_pb2 as _scheduling_pb2
from org.apache.airavata.model.status import status_pb2 as _status_pb2
from org.apache.airavata.model.process import process_pb2 as _process_pb2
from org.apache.airavata.model.workflow import workflow_pb2 as _workflow_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class ExperimentType(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    EXPERIMENT_TYPE_UNKNOWN: _ClassVar[ExperimentType]
    SINGLE_APPLICATION: _ClassVar[ExperimentType]
    WORKFLOW: _ClassVar[ExperimentType]

class ExperimentSearchFields(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    EXPERIMENT_SEARCH_FIELDS_UNKNOWN: _ClassVar[ExperimentSearchFields]
    EXPERIMENT_NAME: _ClassVar[ExperimentSearchFields]
    EXPERIMENT_DESC: _ClassVar[ExperimentSearchFields]
    APPLICATION_ID: _ClassVar[ExperimentSearchFields]
    FROM_DATE: _ClassVar[ExperimentSearchFields]
    TO_DATE: _ClassVar[ExperimentSearchFields]
    STATUS: _ClassVar[ExperimentSearchFields]
    PROJECT_ID: _ClassVar[ExperimentSearchFields]
    USER_NAME: _ClassVar[ExperimentSearchFields]
    JOB_ID: _ClassVar[ExperimentSearchFields]

class ProjectSearchFields(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    PROJECT_SEARCH_FIELDS_UNKNOWN: _ClassVar[ProjectSearchFields]
    PROJECT_NAME: _ClassVar[ProjectSearchFields]
    PROJECT_DESCRIPTION: _ClassVar[ProjectSearchFields]

class ExperimentCleanupStrategy(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    EXPERIMENT_CLEANUP_STRATEGY_UNKNOWN: _ClassVar[ExperimentCleanupStrategy]
    NONE: _ClassVar[ExperimentCleanupStrategy]
    ALWAYS: _ClassVar[ExperimentCleanupStrategy]
    ONLY_COMPLETED: _ClassVar[ExperimentCleanupStrategy]
    ONLY_FAILED: _ClassVar[ExperimentCleanupStrategy]
EXPERIMENT_TYPE_UNKNOWN: ExperimentType
SINGLE_APPLICATION: ExperimentType
WORKFLOW: ExperimentType
EXPERIMENT_SEARCH_FIELDS_UNKNOWN: ExperimentSearchFields
EXPERIMENT_NAME: ExperimentSearchFields
EXPERIMENT_DESC: ExperimentSearchFields
APPLICATION_ID: ExperimentSearchFields
FROM_DATE: ExperimentSearchFields
TO_DATE: ExperimentSearchFields
STATUS: ExperimentSearchFields
PROJECT_ID: ExperimentSearchFields
USER_NAME: ExperimentSearchFields
JOB_ID: ExperimentSearchFields
PROJECT_SEARCH_FIELDS_UNKNOWN: ProjectSearchFields
PROJECT_NAME: ProjectSearchFields
PROJECT_DESCRIPTION: ProjectSearchFields
EXPERIMENT_CLEANUP_STRATEGY_UNKNOWN: ExperimentCleanupStrategy
NONE: ExperimentCleanupStrategy
ALWAYS: ExperimentCleanupStrategy
ONLY_COMPLETED: ExperimentCleanupStrategy
ONLY_FAILED: ExperimentCleanupStrategy

class UserConfigurationDataModel(_message.Message):
    __slots__ = ("airavata_auto_schedule", "override_manual_scheduled_params", "share_experiment_publicly", "computational_resource_scheduling", "throttle_resources", "user_dn", "generate_cert", "input_storage_resource_id", "output_storage_resource_id", "experiment_data_dir", "use_user_cr_pref", "group_resource_profile_id", "auto_scheduled_comp_resource_scheduling_list")
    AIRAVATA_AUTO_SCHEDULE_FIELD_NUMBER: _ClassVar[int]
    OVERRIDE_MANUAL_SCHEDULED_PARAMS_FIELD_NUMBER: _ClassVar[int]
    SHARE_EXPERIMENT_PUBLICLY_FIELD_NUMBER: _ClassVar[int]
    COMPUTATIONAL_RESOURCE_SCHEDULING_FIELD_NUMBER: _ClassVar[int]
    THROTTLE_RESOURCES_FIELD_NUMBER: _ClassVar[int]
    USER_DN_FIELD_NUMBER: _ClassVar[int]
    GENERATE_CERT_FIELD_NUMBER: _ClassVar[int]
    INPUT_STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    OUTPUT_STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    EXPERIMENT_DATA_DIR_FIELD_NUMBER: _ClassVar[int]
    USE_USER_CR_PREF_FIELD_NUMBER: _ClassVar[int]
    GROUP_RESOURCE_PROFILE_ID_FIELD_NUMBER: _ClassVar[int]
    AUTO_SCHEDULED_COMP_RESOURCE_SCHEDULING_LIST_FIELD_NUMBER: _ClassVar[int]
    airavata_auto_schedule: bool
    override_manual_scheduled_params: bool
    share_experiment_publicly: bool
    computational_resource_scheduling: _scheduling_pb2.ComputationalResourceSchedulingModel
    throttle_resources: bool
    user_dn: str
    generate_cert: bool
    input_storage_resource_id: str
    output_storage_resource_id: str
    experiment_data_dir: str
    use_user_cr_pref: bool
    group_resource_profile_id: str
    auto_scheduled_comp_resource_scheduling_list: _containers.RepeatedCompositeFieldContainer[_scheduling_pb2.ComputationalResourceSchedulingModel]
    def __init__(self, airavata_auto_schedule: bool = ..., override_manual_scheduled_params: bool = ..., share_experiment_publicly: bool = ..., computational_resource_scheduling: _Optional[_Union[_scheduling_pb2.ComputationalResourceSchedulingModel, _Mapping]] = ..., throttle_resources: bool = ..., user_dn: _Optional[str] = ..., generate_cert: bool = ..., input_storage_resource_id: _Optional[str] = ..., output_storage_resource_id: _Optional[str] = ..., experiment_data_dir: _Optional[str] = ..., use_user_cr_pref: bool = ..., group_resource_profile_id: _Optional[str] = ..., auto_scheduled_comp_resource_scheduling_list: _Optional[_Iterable[_Union[_scheduling_pb2.ComputationalResourceSchedulingModel, _Mapping]]] = ...) -> None: ...

class ExperimentModel(_message.Message):
    __slots__ = ("experiment_id", "project_id", "gateway_id", "experiment_type", "user_name", "experiment_name", "creation_time", "description", "execution_id", "gateway_execution_id", "gateway_instance_id", "enable_email_notification", "email_addresses", "user_configuration_data", "experiment_inputs", "experiment_outputs", "experiment_status", "errors", "processes", "workflow", "clean_up_strategy")
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    PROJECT_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    EXPERIMENT_TYPE_FIELD_NUMBER: _ClassVar[int]
    USER_NAME_FIELD_NUMBER: _ClassVar[int]
    EXPERIMENT_NAME_FIELD_NUMBER: _ClassVar[int]
    CREATION_TIME_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    EXECUTION_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_EXECUTION_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_INSTANCE_ID_FIELD_NUMBER: _ClassVar[int]
    ENABLE_EMAIL_NOTIFICATION_FIELD_NUMBER: _ClassVar[int]
    EMAIL_ADDRESSES_FIELD_NUMBER: _ClassVar[int]
    USER_CONFIGURATION_DATA_FIELD_NUMBER: _ClassVar[int]
    EXPERIMENT_INPUTS_FIELD_NUMBER: _ClassVar[int]
    EXPERIMENT_OUTPUTS_FIELD_NUMBER: _ClassVar[int]
    EXPERIMENT_STATUS_FIELD_NUMBER: _ClassVar[int]
    ERRORS_FIELD_NUMBER: _ClassVar[int]
    PROCESSES_FIELD_NUMBER: _ClassVar[int]
    WORKFLOW_FIELD_NUMBER: _ClassVar[int]
    CLEAN_UP_STRATEGY_FIELD_NUMBER: _ClassVar[int]
    experiment_id: str
    project_id: str
    gateway_id: str
    experiment_type: ExperimentType
    user_name: str
    experiment_name: str
    creation_time: int
    description: str
    execution_id: str
    gateway_execution_id: str
    gateway_instance_id: str
    enable_email_notification: bool
    email_addresses: _containers.RepeatedScalarFieldContainer[str]
    user_configuration_data: UserConfigurationDataModel
    experiment_inputs: _containers.RepeatedCompositeFieldContainer[_application_io_pb2.InputDataObjectType]
    experiment_outputs: _containers.RepeatedCompositeFieldContainer[_application_io_pb2.OutputDataObjectType]
    experiment_status: _containers.RepeatedCompositeFieldContainer[_status_pb2.ExperimentStatus]
    errors: _containers.RepeatedCompositeFieldContainer[_commons_pb2.ErrorModel]
    processes: _containers.RepeatedCompositeFieldContainer[_process_pb2.ProcessModel]
    workflow: _workflow_pb2.AiravataWorkflow
    clean_up_strategy: ExperimentCleanupStrategy
    def __init__(self, experiment_id: _Optional[str] = ..., project_id: _Optional[str] = ..., gateway_id: _Optional[str] = ..., experiment_type: _Optional[_Union[ExperimentType, str]] = ..., user_name: _Optional[str] = ..., experiment_name: _Optional[str] = ..., creation_time: _Optional[int] = ..., description: _Optional[str] = ..., execution_id: _Optional[str] = ..., gateway_execution_id: _Optional[str] = ..., gateway_instance_id: _Optional[str] = ..., enable_email_notification: bool = ..., email_addresses: _Optional[_Iterable[str]] = ..., user_configuration_data: _Optional[_Union[UserConfigurationDataModel, _Mapping]] = ..., experiment_inputs: _Optional[_Iterable[_Union[_application_io_pb2.InputDataObjectType, _Mapping]]] = ..., experiment_outputs: _Optional[_Iterable[_Union[_application_io_pb2.OutputDataObjectType, _Mapping]]] = ..., experiment_status: _Optional[_Iterable[_Union[_status_pb2.ExperimentStatus, _Mapping]]] = ..., errors: _Optional[_Iterable[_Union[_commons_pb2.ErrorModel, _Mapping]]] = ..., processes: _Optional[_Iterable[_Union[_process_pb2.ProcessModel, _Mapping]]] = ..., workflow: _Optional[_Union[_workflow_pb2.AiravataWorkflow, _Mapping]] = ..., clean_up_strategy: _Optional[_Union[ExperimentCleanupStrategy, str]] = ...) -> None: ...

class ExperimentSummaryModel(_message.Message):
    __slots__ = ("experiment_id", "project_id", "gateway_id", "creation_time", "user_name", "name", "description", "execution_id", "resource_host_id", "experiment_status", "status_update_time")
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    PROJECT_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    CREATION_TIME_FIELD_NUMBER: _ClassVar[int]
    USER_NAME_FIELD_NUMBER: _ClassVar[int]
    NAME_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    EXECUTION_ID_FIELD_NUMBER: _ClassVar[int]
    RESOURCE_HOST_ID_FIELD_NUMBER: _ClassVar[int]
    EXPERIMENT_STATUS_FIELD_NUMBER: _ClassVar[int]
    STATUS_UPDATE_TIME_FIELD_NUMBER: _ClassVar[int]
    experiment_id: str
    project_id: str
    gateway_id: str
    creation_time: int
    user_name: str
    name: str
    description: str
    execution_id: str
    resource_host_id: str
    experiment_status: str
    status_update_time: int
    def __init__(self, experiment_id: _Optional[str] = ..., project_id: _Optional[str] = ..., gateway_id: _Optional[str] = ..., creation_time: _Optional[int] = ..., user_name: _Optional[str] = ..., name: _Optional[str] = ..., description: _Optional[str] = ..., execution_id: _Optional[str] = ..., resource_host_id: _Optional[str] = ..., experiment_status: _Optional[str] = ..., status_update_time: _Optional[int] = ...) -> None: ...

class ExperimentStatistics(_message.Message):
    __slots__ = ("all_experiment_count", "completed_experiment_count", "cancelled_experiment_count", "failed_experiment_count", "created_experiment_count", "running_experiment_count", "all_experiments", "completed_experiments", "failed_experiments", "cancelled_experiments", "created_experiments", "running_experiments")
    ALL_EXPERIMENT_COUNT_FIELD_NUMBER: _ClassVar[int]
    COMPLETED_EXPERIMENT_COUNT_FIELD_NUMBER: _ClassVar[int]
    CANCELLED_EXPERIMENT_COUNT_FIELD_NUMBER: _ClassVar[int]
    FAILED_EXPERIMENT_COUNT_FIELD_NUMBER: _ClassVar[int]
    CREATED_EXPERIMENT_COUNT_FIELD_NUMBER: _ClassVar[int]
    RUNNING_EXPERIMENT_COUNT_FIELD_NUMBER: _ClassVar[int]
    ALL_EXPERIMENTS_FIELD_NUMBER: _ClassVar[int]
    COMPLETED_EXPERIMENTS_FIELD_NUMBER: _ClassVar[int]
    FAILED_EXPERIMENTS_FIELD_NUMBER: _ClassVar[int]
    CANCELLED_EXPERIMENTS_FIELD_NUMBER: _ClassVar[int]
    CREATED_EXPERIMENTS_FIELD_NUMBER: _ClassVar[int]
    RUNNING_EXPERIMENTS_FIELD_NUMBER: _ClassVar[int]
    all_experiment_count: int
    completed_experiment_count: int
    cancelled_experiment_count: int
    failed_experiment_count: int
    created_experiment_count: int
    running_experiment_count: int
    all_experiments: _containers.RepeatedCompositeFieldContainer[ExperimentSummaryModel]
    completed_experiments: _containers.RepeatedCompositeFieldContainer[ExperimentSummaryModel]
    failed_experiments: _containers.RepeatedCompositeFieldContainer[ExperimentSummaryModel]
    cancelled_experiments: _containers.RepeatedCompositeFieldContainer[ExperimentSummaryModel]
    created_experiments: _containers.RepeatedCompositeFieldContainer[ExperimentSummaryModel]
    running_experiments: _containers.RepeatedCompositeFieldContainer[ExperimentSummaryModel]
    def __init__(self, all_experiment_count: _Optional[int] = ..., completed_experiment_count: _Optional[int] = ..., cancelled_experiment_count: _Optional[int] = ..., failed_experiment_count: _Optional[int] = ..., created_experiment_count: _Optional[int] = ..., running_experiment_count: _Optional[int] = ..., all_experiments: _Optional[_Iterable[_Union[ExperimentSummaryModel, _Mapping]]] = ..., completed_experiments: _Optional[_Iterable[_Union[ExperimentSummaryModel, _Mapping]]] = ..., failed_experiments: _Optional[_Iterable[_Union[ExperimentSummaryModel, _Mapping]]] = ..., cancelled_experiments: _Optional[_Iterable[_Union[ExperimentSummaryModel, _Mapping]]] = ..., created_experiments: _Optional[_Iterable[_Union[ExperimentSummaryModel, _Mapping]]] = ..., running_experiments: _Optional[_Iterable[_Union[ExperimentSummaryModel, _Mapping]]] = ...) -> None: ...
