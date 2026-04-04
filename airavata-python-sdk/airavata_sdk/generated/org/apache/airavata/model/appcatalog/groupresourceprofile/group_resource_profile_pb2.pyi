from org.apache.airavata.model.appcatalog.computeresource import compute_resource_pb2 as _compute_resource_pb2
from org.apache.airavata.model.data.movement import data_movement_pb2 as _data_movement_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class ResourceType(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    RESOURCE_TYPE_UNKNOWN: _ClassVar[ResourceType]
    SLURM: _ClassVar[ResourceType]
    AWS: _ClassVar[ResourceType]
RESOURCE_TYPE_UNKNOWN: ResourceType
SLURM: ResourceType
AWS: ResourceType

class GroupAccountSSHProvisionerConfig(_message.Message):
    __slots__ = ("resource_id", "group_resource_profile_id", "config_name", "config_value")
    RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    GROUP_RESOURCE_PROFILE_ID_FIELD_NUMBER: _ClassVar[int]
    CONFIG_NAME_FIELD_NUMBER: _ClassVar[int]
    CONFIG_VALUE_FIELD_NUMBER: _ClassVar[int]
    resource_id: str
    group_resource_profile_id: str
    config_name: str
    config_value: str
    def __init__(self, resource_id: _Optional[str] = ..., group_resource_profile_id: _Optional[str] = ..., config_name: _Optional[str] = ..., config_value: _Optional[str] = ...) -> None: ...

class ComputeResourceReservation(_message.Message):
    __slots__ = ("reservation_id", "reservation_name", "queue_names", "start_time", "end_time")
    RESERVATION_ID_FIELD_NUMBER: _ClassVar[int]
    RESERVATION_NAME_FIELD_NUMBER: _ClassVar[int]
    QUEUE_NAMES_FIELD_NUMBER: _ClassVar[int]
    START_TIME_FIELD_NUMBER: _ClassVar[int]
    END_TIME_FIELD_NUMBER: _ClassVar[int]
    reservation_id: str
    reservation_name: str
    queue_names: _containers.RepeatedScalarFieldContainer[str]
    start_time: int
    end_time: int
    def __init__(self, reservation_id: _Optional[str] = ..., reservation_name: _Optional[str] = ..., queue_names: _Optional[_Iterable[str]] = ..., start_time: _Optional[int] = ..., end_time: _Optional[int] = ...) -> None: ...

class SlurmComputeResourcePreference(_message.Message):
    __slots__ = ("allocation_project_number", "preferred_batch_queue", "quality_of_service", "usage_reporting_gateway_id", "ssh_account_provisioner", "group_ssh_account_provisioner_configs", "ssh_account_provisioner_additional_info", "reservations")
    ALLOCATION_PROJECT_NUMBER_FIELD_NUMBER: _ClassVar[int]
    PREFERRED_BATCH_QUEUE_FIELD_NUMBER: _ClassVar[int]
    QUALITY_OF_SERVICE_FIELD_NUMBER: _ClassVar[int]
    USAGE_REPORTING_GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    SSH_ACCOUNT_PROVISIONER_FIELD_NUMBER: _ClassVar[int]
    GROUP_SSH_ACCOUNT_PROVISIONER_CONFIGS_FIELD_NUMBER: _ClassVar[int]
    SSH_ACCOUNT_PROVISIONER_ADDITIONAL_INFO_FIELD_NUMBER: _ClassVar[int]
    RESERVATIONS_FIELD_NUMBER: _ClassVar[int]
    allocation_project_number: str
    preferred_batch_queue: str
    quality_of_service: str
    usage_reporting_gateway_id: str
    ssh_account_provisioner: str
    group_ssh_account_provisioner_configs: _containers.RepeatedCompositeFieldContainer[GroupAccountSSHProvisionerConfig]
    ssh_account_provisioner_additional_info: str
    reservations: _containers.RepeatedCompositeFieldContainer[ComputeResourceReservation]
    def __init__(self, allocation_project_number: _Optional[str] = ..., preferred_batch_queue: _Optional[str] = ..., quality_of_service: _Optional[str] = ..., usage_reporting_gateway_id: _Optional[str] = ..., ssh_account_provisioner: _Optional[str] = ..., group_ssh_account_provisioner_configs: _Optional[_Iterable[_Union[GroupAccountSSHProvisionerConfig, _Mapping]]] = ..., ssh_account_provisioner_additional_info: _Optional[str] = ..., reservations: _Optional[_Iterable[_Union[ComputeResourceReservation, _Mapping]]] = ...) -> None: ...

class AwsComputeResourcePreference(_message.Message):
    __slots__ = ("region", "preferred_ami_id", "preferred_instance_type")
    REGION_FIELD_NUMBER: _ClassVar[int]
    PREFERRED_AMI_ID_FIELD_NUMBER: _ClassVar[int]
    PREFERRED_INSTANCE_TYPE_FIELD_NUMBER: _ClassVar[int]
    region: str
    preferred_ami_id: str
    preferred_instance_type: str
    def __init__(self, region: _Optional[str] = ..., preferred_ami_id: _Optional[str] = ..., preferred_instance_type: _Optional[str] = ...) -> None: ...

class EnvironmentSpecificPreferences(_message.Message):
    __slots__ = ("slurm", "aws")
    SLURM_FIELD_NUMBER: _ClassVar[int]
    AWS_FIELD_NUMBER: _ClassVar[int]
    slurm: SlurmComputeResourcePreference
    aws: AwsComputeResourcePreference
    def __init__(self, slurm: _Optional[_Union[SlurmComputeResourcePreference, _Mapping]] = ..., aws: _Optional[_Union[AwsComputeResourcePreference, _Mapping]] = ...) -> None: ...

class GroupComputeResourcePreference(_message.Message):
    __slots__ = ("compute_resource_id", "group_resource_profile_id", "override_by_airavata", "login_user_name", "scratch_location", "preferred_job_submission_protocol", "preferred_data_movement_protocol", "resource_specific_credential_store_token", "resource_type", "specific_preferences")
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    GROUP_RESOURCE_PROFILE_ID_FIELD_NUMBER: _ClassVar[int]
    OVERRIDE_BY_AIRAVATA_FIELD_NUMBER: _ClassVar[int]
    LOGIN_USER_NAME_FIELD_NUMBER: _ClassVar[int]
    SCRATCH_LOCATION_FIELD_NUMBER: _ClassVar[int]
    PREFERRED_JOB_SUBMISSION_PROTOCOL_FIELD_NUMBER: _ClassVar[int]
    PREFERRED_DATA_MOVEMENT_PROTOCOL_FIELD_NUMBER: _ClassVar[int]
    RESOURCE_SPECIFIC_CREDENTIAL_STORE_TOKEN_FIELD_NUMBER: _ClassVar[int]
    RESOURCE_TYPE_FIELD_NUMBER: _ClassVar[int]
    SPECIFIC_PREFERENCES_FIELD_NUMBER: _ClassVar[int]
    compute_resource_id: str
    group_resource_profile_id: str
    override_by_airavata: bool
    login_user_name: str
    scratch_location: str
    preferred_job_submission_protocol: _compute_resource_pb2.JobSubmissionProtocol
    preferred_data_movement_protocol: _data_movement_pb2.DataMovementProtocol
    resource_specific_credential_store_token: str
    resource_type: ResourceType
    specific_preferences: EnvironmentSpecificPreferences
    def __init__(self, compute_resource_id: _Optional[str] = ..., group_resource_profile_id: _Optional[str] = ..., override_by_airavata: bool = ..., login_user_name: _Optional[str] = ..., scratch_location: _Optional[str] = ..., preferred_job_submission_protocol: _Optional[_Union[_compute_resource_pb2.JobSubmissionProtocol, str]] = ..., preferred_data_movement_protocol: _Optional[_Union[_data_movement_pb2.DataMovementProtocol, str]] = ..., resource_specific_credential_store_token: _Optional[str] = ..., resource_type: _Optional[_Union[ResourceType, str]] = ..., specific_preferences: _Optional[_Union[EnvironmentSpecificPreferences, _Mapping]] = ...) -> None: ...

class ComputeResourcePolicy(_message.Message):
    __slots__ = ("resource_policy_id", "compute_resource_id", "group_resource_profile_id", "allowed_batch_queues")
    RESOURCE_POLICY_ID_FIELD_NUMBER: _ClassVar[int]
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    GROUP_RESOURCE_PROFILE_ID_FIELD_NUMBER: _ClassVar[int]
    ALLOWED_BATCH_QUEUES_FIELD_NUMBER: _ClassVar[int]
    resource_policy_id: str
    compute_resource_id: str
    group_resource_profile_id: str
    allowed_batch_queues: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, resource_policy_id: _Optional[str] = ..., compute_resource_id: _Optional[str] = ..., group_resource_profile_id: _Optional[str] = ..., allowed_batch_queues: _Optional[_Iterable[str]] = ...) -> None: ...

class BatchQueueResourcePolicy(_message.Message):
    __slots__ = ("resource_policy_id", "compute_resource_id", "group_resource_profile_id", "queuename", "max_allowed_nodes", "max_allowed_cores", "max_allowed_walltime")
    RESOURCE_POLICY_ID_FIELD_NUMBER: _ClassVar[int]
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    GROUP_RESOURCE_PROFILE_ID_FIELD_NUMBER: _ClassVar[int]
    QUEUENAME_FIELD_NUMBER: _ClassVar[int]
    MAX_ALLOWED_NODES_FIELD_NUMBER: _ClassVar[int]
    MAX_ALLOWED_CORES_FIELD_NUMBER: _ClassVar[int]
    MAX_ALLOWED_WALLTIME_FIELD_NUMBER: _ClassVar[int]
    resource_policy_id: str
    compute_resource_id: str
    group_resource_profile_id: str
    queuename: str
    max_allowed_nodes: int
    max_allowed_cores: int
    max_allowed_walltime: int
    def __init__(self, resource_policy_id: _Optional[str] = ..., compute_resource_id: _Optional[str] = ..., group_resource_profile_id: _Optional[str] = ..., queuename: _Optional[str] = ..., max_allowed_nodes: _Optional[int] = ..., max_allowed_cores: _Optional[int] = ..., max_allowed_walltime: _Optional[int] = ...) -> None: ...

class GroupResourceProfile(_message.Message):
    __slots__ = ("gateway_id", "group_resource_profile_id", "group_resource_profile_name", "compute_preferences", "compute_resource_policies", "batch_queue_resource_policies", "creation_time", "updated_time", "default_credential_store_token")
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    GROUP_RESOURCE_PROFILE_ID_FIELD_NUMBER: _ClassVar[int]
    GROUP_RESOURCE_PROFILE_NAME_FIELD_NUMBER: _ClassVar[int]
    COMPUTE_PREFERENCES_FIELD_NUMBER: _ClassVar[int]
    COMPUTE_RESOURCE_POLICIES_FIELD_NUMBER: _ClassVar[int]
    BATCH_QUEUE_RESOURCE_POLICIES_FIELD_NUMBER: _ClassVar[int]
    CREATION_TIME_FIELD_NUMBER: _ClassVar[int]
    UPDATED_TIME_FIELD_NUMBER: _ClassVar[int]
    DEFAULT_CREDENTIAL_STORE_TOKEN_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    group_resource_profile_id: str
    group_resource_profile_name: str
    compute_preferences: _containers.RepeatedCompositeFieldContainer[GroupComputeResourcePreference]
    compute_resource_policies: _containers.RepeatedCompositeFieldContainer[ComputeResourcePolicy]
    batch_queue_resource_policies: _containers.RepeatedCompositeFieldContainer[BatchQueueResourcePolicy]
    creation_time: int
    updated_time: int
    default_credential_store_token: str
    def __init__(self, gateway_id: _Optional[str] = ..., group_resource_profile_id: _Optional[str] = ..., group_resource_profile_name: _Optional[str] = ..., compute_preferences: _Optional[_Iterable[_Union[GroupComputeResourcePreference, _Mapping]]] = ..., compute_resource_policies: _Optional[_Iterable[_Union[ComputeResourcePolicy, _Mapping]]] = ..., batch_queue_resource_policies: _Optional[_Iterable[_Union[BatchQueueResourcePolicy, _Mapping]]] = ..., creation_time: _Optional[int] = ..., updated_time: _Optional[int] = ..., default_credential_store_token: _Optional[str] = ...) -> None: ...
