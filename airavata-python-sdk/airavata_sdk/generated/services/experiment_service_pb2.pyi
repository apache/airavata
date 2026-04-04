from google.api import annotations_pb2 as _annotations_pb2
from google.protobuf import empty_pb2 as _empty_pb2
from org.apache.airavata.model.experiment import experiment_pb2 as _experiment_pb2
from org.apache.airavata.model.status import status_pb2 as _status_pb2
from org.apache.airavata.model.application.io import application_io_pb2 as _application_io_pb2
from org.apache.airavata.model.job import job_pb2 as _job_pb2
from org.apache.airavata.model.scheduling import scheduling_pb2 as _scheduling_pb2
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
