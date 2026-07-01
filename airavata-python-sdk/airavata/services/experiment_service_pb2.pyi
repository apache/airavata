from google.api import annotations_pb2 as _annotations_pb2
from google.protobuf import empty_pb2 as _empty_pb2
from airavata.model.experiment import experiment_pb2 as _experiment_pb2
from airavata.model.status import status_pb2 as _status_pb2
from airavata.model.application.io import application_io_pb2 as _application_io_pb2
from airavata.model.job import job_pb2 as _job_pb2
from airavata.model.scheduling import scheduling_pb2 as _scheduling_pb2
from airavata.model.commons import commons_pb2 as _commons_pb2
from airavata.model.data.replica import replica_catalog_pb2 as _replica_catalog_pb2
from airavata.model.appcatalog.appinterface import app_interface_pb2 as _app_interface_pb2
from airavata.model.appcatalog.appdeployment import app_deployment_pb2 as _app_deployment_pb2
from airavata.model.appcatalog.computeresource import compute_resource_pb2 as _compute_resource_pb2
from airavata.model.workspace import workspace_pb2 as _workspace_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class CreateExperimentRequest(_message.Message):
    __slots__ = ("gateway_id", "experiment")
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    EXPERIMENT_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    experiment: _experiment_pb2.ExperimentModel
    def __init__(self, gateway_id: _Optional[str] = ..., experiment: _Optional[_Union[_experiment_pb2.ExperimentModel, _Mapping]] = ...) -> None: ...

class CreateExperimentResponse(_message.Message):
    __slots__ = ("experiment_id",)
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    experiment_id: str
    def __init__(self, experiment_id: _Optional[str] = ...) -> None: ...

class GetExperimentRequest(_message.Message):
    __slots__ = ("experiment_id",)
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    experiment_id: str
    def __init__(self, experiment_id: _Optional[str] = ...) -> None: ...

class ExperimentWithAccess(_message.Message):
    __slots__ = ("experiment", "access")
    EXPERIMENT_FIELD_NUMBER: _ClassVar[int]
    ACCESS_FIELD_NUMBER: _ClassVar[int]
    experiment: _experiment_pb2.ExperimentModel
    access: _commons_pb2.AccessFlags
    def __init__(self, experiment: _Optional[_Union[_experiment_pb2.ExperimentModel, _Mapping]] = ..., access: _Optional[_Union[_commons_pb2.AccessFlags, _Mapping]] = ...) -> None: ...

class DataProductWithAccess(_message.Message):
    __slots__ = ("data_product", "access")
    DATA_PRODUCT_FIELD_NUMBER: _ClassVar[int]
    ACCESS_FIELD_NUMBER: _ClassVar[int]
    data_product: _replica_catalog_pb2.DataProductModel
    access: _commons_pb2.AccessFlags
    def __init__(self, data_product: _Optional[_Union[_replica_catalog_pb2.DataProductModel, _Mapping]] = ..., access: _Optional[_Union[_commons_pb2.AccessFlags, _Mapping]] = ...) -> None: ...

class FullExperiment(_message.Message):
    __slots__ = ("experiment", "access", "project", "application_module", "application_interface", "compute_resource", "input_data_products", "output_data_products", "jobs")
    EXPERIMENT_FIELD_NUMBER: _ClassVar[int]
    ACCESS_FIELD_NUMBER: _ClassVar[int]
    PROJECT_FIELD_NUMBER: _ClassVar[int]
    APPLICATION_MODULE_FIELD_NUMBER: _ClassVar[int]
    APPLICATION_INTERFACE_FIELD_NUMBER: _ClassVar[int]
    COMPUTE_RESOURCE_FIELD_NUMBER: _ClassVar[int]
    INPUT_DATA_PRODUCTS_FIELD_NUMBER: _ClassVar[int]
    OUTPUT_DATA_PRODUCTS_FIELD_NUMBER: _ClassVar[int]
    JOBS_FIELD_NUMBER: _ClassVar[int]
    experiment: _experiment_pb2.ExperimentModel
    access: _commons_pb2.AccessFlags
    project: _workspace_pb2.Project
    application_module: _app_deployment_pb2.ApplicationModule
    application_interface: _app_interface_pb2.ApplicationInterfaceDescription
    compute_resource: _compute_resource_pb2.ComputeResourceDescription
    input_data_products: _containers.RepeatedCompositeFieldContainer[DataProductWithAccess]
    output_data_products: _containers.RepeatedCompositeFieldContainer[DataProductWithAccess]
    jobs: _containers.RepeatedCompositeFieldContainer[_job_pb2.JobModel]
    def __init__(self, experiment: _Optional[_Union[_experiment_pb2.ExperimentModel, _Mapping]] = ..., access: _Optional[_Union[_commons_pb2.AccessFlags, _Mapping]] = ..., project: _Optional[_Union[_workspace_pb2.Project, _Mapping]] = ..., application_module: _Optional[_Union[_app_deployment_pb2.ApplicationModule, _Mapping]] = ..., application_interface: _Optional[_Union[_app_interface_pb2.ApplicationInterfaceDescription, _Mapping]] = ..., compute_resource: _Optional[_Union[_compute_resource_pb2.ComputeResourceDescription, _Mapping]] = ..., input_data_products: _Optional[_Iterable[_Union[DataProductWithAccess, _Mapping]]] = ..., output_data_products: _Optional[_Iterable[_Union[DataProductWithAccess, _Mapping]]] = ..., jobs: _Optional[_Iterable[_Union[_job_pb2.JobModel, _Mapping]]] = ...) -> None: ...

class GetExperimentByAdminRequest(_message.Message):
    __slots__ = ("experiment_id",)
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    experiment_id: str
    def __init__(self, experiment_id: _Optional[str] = ...) -> None: ...

class UpdateExperimentRequest(_message.Message):
    __slots__ = ("experiment_id", "experiment")
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    EXPERIMENT_FIELD_NUMBER: _ClassVar[int]
    experiment_id: str
    experiment: _experiment_pb2.ExperimentModel
    def __init__(self, experiment_id: _Optional[str] = ..., experiment: _Optional[_Union[_experiment_pb2.ExperimentModel, _Mapping]] = ...) -> None: ...

class DeleteExperimentRequest(_message.Message):
    __slots__ = ("experiment_id",)
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    experiment_id: str
    def __init__(self, experiment_id: _Optional[str] = ...) -> None: ...

class SearchExperimentsRequest(_message.Message):
    __slots__ = ("gateway_id", "user_name", "filters", "limit", "offset")
    class FiltersEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: str
        def __init__(self, key: _Optional[str] = ..., value: _Optional[str] = ...) -> None: ...
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    USER_NAME_FIELD_NUMBER: _ClassVar[int]
    FILTERS_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    OFFSET_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    user_name: str
    filters: _containers.ScalarMap[str, str]
    limit: int
    offset: int
    def __init__(self, gateway_id: _Optional[str] = ..., user_name: _Optional[str] = ..., filters: _Optional[_Mapping[str, str]] = ..., limit: _Optional[int] = ..., offset: _Optional[int] = ...) -> None: ...

class SearchExperimentsResponse(_message.Message):
    __slots__ = ("experiments",)
    EXPERIMENTS_FIELD_NUMBER: _ClassVar[int]
    experiments: _containers.RepeatedCompositeFieldContainer[_experiment_pb2.ExperimentSummaryModel]
    def __init__(self, experiments: _Optional[_Iterable[_Union[_experiment_pb2.ExperimentSummaryModel, _Mapping]]] = ...) -> None: ...

class ExperimentSummaryWithAccess(_message.Message):
    __slots__ = ("summary", "access")
    SUMMARY_FIELD_NUMBER: _ClassVar[int]
    ACCESS_FIELD_NUMBER: _ClassVar[int]
    summary: _experiment_pb2.ExperimentSummaryModel
    access: _commons_pb2.AccessFlags
    def __init__(self, summary: _Optional[_Union[_experiment_pb2.ExperimentSummaryModel, _Mapping]] = ..., access: _Optional[_Union[_commons_pb2.AccessFlags, _Mapping]] = ...) -> None: ...

class SearchExperimentsWithAccessResponse(_message.Message):
    __slots__ = ("experiments",)
    EXPERIMENTS_FIELD_NUMBER: _ClassVar[int]
    experiments: _containers.RepeatedCompositeFieldContainer[ExperimentSummaryWithAccess]
    def __init__(self, experiments: _Optional[_Iterable[_Union[ExperimentSummaryWithAccess, _Mapping]]] = ...) -> None: ...

class GetExperimentStatusRequest(_message.Message):
    __slots__ = ("experiment_id",)
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    experiment_id: str
    def __init__(self, experiment_id: _Optional[str] = ...) -> None: ...

class GetExperimentOutputsRequest(_message.Message):
    __slots__ = ("experiment_id",)
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    experiment_id: str
    def __init__(self, experiment_id: _Optional[str] = ...) -> None: ...

class GetExperimentOutputsResponse(_message.Message):
    __slots__ = ("outputs",)
    OUTPUTS_FIELD_NUMBER: _ClassVar[int]
    outputs: _containers.RepeatedCompositeFieldContainer[_application_io_pb2.OutputDataObjectType]
    def __init__(self, outputs: _Optional[_Iterable[_Union[_application_io_pb2.OutputDataObjectType, _Mapping]]] = ...) -> None: ...

class GetExperimentsInProjectRequest(_message.Message):
    __slots__ = ("project_id", "limit", "offset")
    PROJECT_ID_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    OFFSET_FIELD_NUMBER: _ClassVar[int]
    project_id: str
    limit: int
    offset: int
    def __init__(self, project_id: _Optional[str] = ..., limit: _Optional[int] = ..., offset: _Optional[int] = ...) -> None: ...

class GetExperimentsInProjectResponse(_message.Message):
    __slots__ = ("experiments",)
    EXPERIMENTS_FIELD_NUMBER: _ClassVar[int]
    experiments: _containers.RepeatedCompositeFieldContainer[_experiment_pb2.ExperimentModel]
    def __init__(self, experiments: _Optional[_Iterable[_Union[_experiment_pb2.ExperimentModel, _Mapping]]] = ...) -> None: ...

class GetExperimentsInProjectWithAccessResponse(_message.Message):
    __slots__ = ("experiments",)
    EXPERIMENTS_FIELD_NUMBER: _ClassVar[int]
    experiments: _containers.RepeatedCompositeFieldContainer[ExperimentWithAccess]
    def __init__(self, experiments: _Optional[_Iterable[_Union[ExperimentWithAccess, _Mapping]]] = ...) -> None: ...

class GetUserExperimentsRequest(_message.Message):
    __slots__ = ("gateway_id", "user_name", "limit", "offset")
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    USER_NAME_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    OFFSET_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    user_name: str
    limit: int
    offset: int
    def __init__(self, gateway_id: _Optional[str] = ..., user_name: _Optional[str] = ..., limit: _Optional[int] = ..., offset: _Optional[int] = ...) -> None: ...

class GetUserExperimentsResponse(_message.Message):
    __slots__ = ("experiments",)
    EXPERIMENTS_FIELD_NUMBER: _ClassVar[int]
    experiments: _containers.RepeatedCompositeFieldContainer[_experiment_pb2.ExperimentModel]
    def __init__(self, experiments: _Optional[_Iterable[_Union[_experiment_pb2.ExperimentModel, _Mapping]]] = ...) -> None: ...

class GetDetailedExperimentTreeRequest(_message.Message):
    __slots__ = ("experiment_id",)
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    experiment_id: str
    def __init__(self, experiment_id: _Optional[str] = ...) -> None: ...

class UpdateExperimentConfigurationRequest(_message.Message):
    __slots__ = ("experiment_id", "configuration")
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    CONFIGURATION_FIELD_NUMBER: _ClassVar[int]
    experiment_id: str
    configuration: _experiment_pb2.UserConfigurationDataModel
    def __init__(self, experiment_id: _Optional[str] = ..., configuration: _Optional[_Union[_experiment_pb2.UserConfigurationDataModel, _Mapping]] = ...) -> None: ...

class UpdateResourceSchedulingRequest(_message.Message):
    __slots__ = ("experiment_id", "scheduling")
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    SCHEDULING_FIELD_NUMBER: _ClassVar[int]
    experiment_id: str
    scheduling: _scheduling_pb2.ComputationalResourceSchedulingModel
    def __init__(self, experiment_id: _Optional[str] = ..., scheduling: _Optional[_Union[_scheduling_pb2.ComputationalResourceSchedulingModel, _Mapping]] = ...) -> None: ...

class ValidateExperimentRequest(_message.Message):
    __slots__ = ("experiment_id",)
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    experiment_id: str
    def __init__(self, experiment_id: _Optional[str] = ...) -> None: ...

class ValidateExperimentResponse(_message.Message):
    __slots__ = ("is_valid", "validation_errors")
    IS_VALID_FIELD_NUMBER: _ClassVar[int]
    VALIDATION_ERRORS_FIELD_NUMBER: _ClassVar[int]
    is_valid: bool
    validation_errors: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, is_valid: bool = ..., validation_errors: _Optional[_Iterable[str]] = ...) -> None: ...

class LaunchExperimentRequest(_message.Message):
    __slots__ = ("experiment_id", "gateway_id")
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    experiment_id: str
    gateway_id: str
    def __init__(self, experiment_id: _Optional[str] = ..., gateway_id: _Optional[str] = ...) -> None: ...

class LaunchExperimentWithStorageSetupRequest(_message.Message):
    __slots__ = ("experiment_id", "gateway_id", "notification_email")
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    NOTIFICATION_EMAIL_FIELD_NUMBER: _ClassVar[int]
    experiment_id: str
    gateway_id: str
    notification_email: str
    def __init__(self, experiment_id: _Optional[str] = ..., gateway_id: _Optional[str] = ..., notification_email: _Optional[str] = ...) -> None: ...

class TerminateExperimentRequest(_message.Message):
    __slots__ = ("experiment_id", "gateway_id")
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    experiment_id: str
    gateway_id: str
    def __init__(self, experiment_id: _Optional[str] = ..., gateway_id: _Optional[str] = ...) -> None: ...

class CloneExperimentRequest(_message.Message):
    __slots__ = ("experiment_id", "new_experiment_name", "new_experiment_project_id")
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    NEW_EXPERIMENT_NAME_FIELD_NUMBER: _ClassVar[int]
    NEW_EXPERIMENT_PROJECT_ID_FIELD_NUMBER: _ClassVar[int]
    experiment_id: str
    new_experiment_name: str
    new_experiment_project_id: str
    def __init__(self, experiment_id: _Optional[str] = ..., new_experiment_name: _Optional[str] = ..., new_experiment_project_id: _Optional[str] = ...) -> None: ...

class CloneExperimentResponse(_message.Message):
    __slots__ = ("experiment_id",)
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    experiment_id: str
    def __init__(self, experiment_id: _Optional[str] = ...) -> None: ...

class CloneExperimentWithInputFilesRequest(_message.Message):
    __slots__ = ("experiment_id", "new_experiment_name", "new_experiment_project_id")
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    NEW_EXPERIMENT_NAME_FIELD_NUMBER: _ClassVar[int]
    NEW_EXPERIMENT_PROJECT_ID_FIELD_NUMBER: _ClassVar[int]
    experiment_id: str
    new_experiment_name: str
    new_experiment_project_id: str
    def __init__(self, experiment_id: _Optional[str] = ..., new_experiment_name: _Optional[str] = ..., new_experiment_project_id: _Optional[str] = ...) -> None: ...

class GetJobStatusesRequest(_message.Message):
    __slots__ = ("experiment_id",)
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    experiment_id: str
    def __init__(self, experiment_id: _Optional[str] = ...) -> None: ...

class GetJobStatusesResponse(_message.Message):
    __slots__ = ("job_statuses",)
    class JobStatusesEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: _status_pb2.JobStatus
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[_status_pb2.JobStatus, _Mapping]] = ...) -> None: ...
    JOB_STATUSES_FIELD_NUMBER: _ClassVar[int]
    job_statuses: _containers.MessageMap[str, _status_pb2.JobStatus]
    def __init__(self, job_statuses: _Optional[_Mapping[str, _status_pb2.JobStatus]] = ...) -> None: ...

class GetJobDetailsRequest(_message.Message):
    __slots__ = ("experiment_id",)
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    experiment_id: str
    def __init__(self, experiment_id: _Optional[str] = ...) -> None: ...

class GetJobDetailsResponse(_message.Message):
    __slots__ = ("jobs",)
    JOBS_FIELD_NUMBER: _ClassVar[int]
    jobs: _containers.RepeatedCompositeFieldContainer[_job_pb2.JobModel]
    def __init__(self, jobs: _Optional[_Iterable[_Union[_job_pb2.JobModel, _Mapping]]] = ...) -> None: ...

class FetchIntermediateOutputsRequest(_message.Message):
    __slots__ = ("experiment_id", "output_names")
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    OUTPUT_NAMES_FIELD_NUMBER: _ClassVar[int]
    experiment_id: str
    output_names: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, experiment_id: _Optional[str] = ..., output_names: _Optional[_Iterable[str]] = ...) -> None: ...

class GetIntermediateOutputProcessStatusRequest(_message.Message):
    __slots__ = ("experiment_id",)
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    experiment_id: str
    def __init__(self, experiment_id: _Optional[str] = ...) -> None: ...

class GetExperimentStatisticsRequest(_message.Message):
    __slots__ = ("gateway_id", "from_time", "to_time", "user_name", "application_name", "resource_host_name", "limit", "offset")
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    FROM_TIME_FIELD_NUMBER: _ClassVar[int]
    TO_TIME_FIELD_NUMBER: _ClassVar[int]
    USER_NAME_FIELD_NUMBER: _ClassVar[int]
    APPLICATION_NAME_FIELD_NUMBER: _ClassVar[int]
    RESOURCE_HOST_NAME_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    OFFSET_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    from_time: int
    to_time: int
    user_name: str
    application_name: str
    resource_host_name: str
    limit: int
    offset: int
    def __init__(self, gateway_id: _Optional[str] = ..., from_time: _Optional[int] = ..., to_time: _Optional[int] = ..., user_name: _Optional[str] = ..., application_name: _Optional[str] = ..., resource_host_name: _Optional[str] = ..., limit: _Optional[int] = ..., offset: _Optional[int] = ...) -> None: ...

class CreateExperimentFromSpecRequest(_message.Message):
    __slots__ = ("spec",)
    SPEC_FIELD_NUMBER: _ClassVar[int]
    spec: ExperimentSpec
    def __init__(self, spec: _Optional[_Union[ExperimentSpec, _Mapping]] = ...) -> None: ...

class ExperimentSpec(_message.Message):
    __slots__ = ("experiment_name", "project_id", "application_interface_id", "description", "inputs", "resource")
    class InputsEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: str
        def __init__(self, key: _Optional[str] = ..., value: _Optional[str] = ...) -> None: ...
    EXPERIMENT_NAME_FIELD_NUMBER: _ClassVar[int]
    PROJECT_ID_FIELD_NUMBER: _ClassVar[int]
    APPLICATION_INTERFACE_ID_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    INPUTS_FIELD_NUMBER: _ClassVar[int]
    RESOURCE_FIELD_NUMBER: _ClassVar[int]
    experiment_name: str
    project_id: str
    application_interface_id: str
    description: str
    inputs: _containers.ScalarMap[str, str]
    resource: ResourceSpec
    def __init__(self, experiment_name: _Optional[str] = ..., project_id: _Optional[str] = ..., application_interface_id: _Optional[str] = ..., description: _Optional[str] = ..., inputs: _Optional[_Mapping[str, str]] = ..., resource: _Optional[_Union[ResourceSpec, _Mapping]] = ...) -> None: ...

class ResourceSpec(_message.Message):
    __slots__ = ("compute_resource_id", "group_resource_profile_id", "node_count", "total_cpu_count", "queue_name", "wall_time_limit", "input_storage_resource_id", "output_storage_resource_id", "auto_schedule")
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    GROUP_RESOURCE_PROFILE_ID_FIELD_NUMBER: _ClassVar[int]
    NODE_COUNT_FIELD_NUMBER: _ClassVar[int]
    TOTAL_CPU_COUNT_FIELD_NUMBER: _ClassVar[int]
    QUEUE_NAME_FIELD_NUMBER: _ClassVar[int]
    WALL_TIME_LIMIT_FIELD_NUMBER: _ClassVar[int]
    INPUT_STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    OUTPUT_STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    AUTO_SCHEDULE_FIELD_NUMBER: _ClassVar[int]
    compute_resource_id: str
    group_resource_profile_id: str
    node_count: int
    total_cpu_count: int
    queue_name: str
    wall_time_limit: int
    input_storage_resource_id: str
    output_storage_resource_id: str
    auto_schedule: bool
    def __init__(self, compute_resource_id: _Optional[str] = ..., group_resource_profile_id: _Optional[str] = ..., node_count: _Optional[int] = ..., total_cpu_count: _Optional[int] = ..., queue_name: _Optional[str] = ..., wall_time_limit: _Optional[int] = ..., input_storage_resource_id: _Optional[str] = ..., output_storage_resource_id: _Optional[str] = ..., auto_schedule: bool = ...) -> None: ...
