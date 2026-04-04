from org.apache.airavata.model.parallelism import parallelism_pb2 as _parallelism_pb2
from org.apache.airavata.model.data.movement import data_movement_pb2 as _data_movement_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class ResourceJobManagerType(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    RESOURCE_JOB_MANAGER_TYPE_UNKNOWN: _ClassVar[ResourceJobManagerType]
    FORK: _ClassVar[ResourceJobManagerType]
    PBS: _ClassVar[ResourceJobManagerType]
    SLURM: _ClassVar[ResourceJobManagerType]
    LSF: _ClassVar[ResourceJobManagerType]
    UGE: _ClassVar[ResourceJobManagerType]
    CLOUD: _ClassVar[ResourceJobManagerType]
    AIRAVATA_CUSTOM: _ClassVar[ResourceJobManagerType]
    HTCONDOR: _ClassVar[ResourceJobManagerType]

class JobManagerCommand(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    JOB_MANAGER_COMMAND_UNKNOWN: _ClassVar[JobManagerCommand]
    SUBMISSION: _ClassVar[JobManagerCommand]
    JOB_MONITORING: _ClassVar[JobManagerCommand]
    DELETION: _ClassVar[JobManagerCommand]
    CHECK_JOB: _ClassVar[JobManagerCommand]
    SHOW_QUEUE: _ClassVar[JobManagerCommand]
    SHOW_RESERVATION: _ClassVar[JobManagerCommand]
    SHOW_START: _ClassVar[JobManagerCommand]
    SHOW_CLUSTER_INFO: _ClassVar[JobManagerCommand]
    SHOW_NO_OF_RUNNING_JOBS: _ClassVar[JobManagerCommand]
    SHOW_NO_OF_PENDING_JOBS: _ClassVar[JobManagerCommand]

class FileSystems(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    FILE_SYSTEMS_UNKNOWN: _ClassVar[FileSystems]
    HOME: _ClassVar[FileSystems]
    WORK: _ClassVar[FileSystems]
    LOCALTMP: _ClassVar[FileSystems]
    SCRATCH: _ClassVar[FileSystems]
    ARCHIVE: _ClassVar[FileSystems]

class JobSubmissionProtocol(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    JOB_SUBMISSION_PROTOCOL_UNKNOWN: _ClassVar[JobSubmissionProtocol]
    LOCAL: _ClassVar[JobSubmissionProtocol]
    SSH: _ClassVar[JobSubmissionProtocol]
    GLOBUS: _ClassVar[JobSubmissionProtocol]
    UNICORE: _ClassVar[JobSubmissionProtocol]
    JSP_CLOUD: _ClassVar[JobSubmissionProtocol]
    SSH_FORK: _ClassVar[JobSubmissionProtocol]
    LOCAL_FORK: _ClassVar[JobSubmissionProtocol]

class MonitorMode(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    MONITOR_MODE_UNKNOWN: _ClassVar[MonitorMode]
    POLL_JOB_MANAGER: _ClassVar[MonitorMode]
    CLOUD_JOB_MONITOR: _ClassVar[MonitorMode]
    JOB_EMAIL_NOTIFICATION_MONITOR: _ClassVar[MonitorMode]
    XSEDE_AMQP_SUBSCRIBE: _ClassVar[MonitorMode]
    MONITOR_FORK: _ClassVar[MonitorMode]
    MONITOR_LOCAL: _ClassVar[MonitorMode]

class DMType(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    DM_TYPE_UNKNOWN: _ClassVar[DMType]
    COMPUTE_RESOURCE: _ClassVar[DMType]
    STORAGE_RESOURCE: _ClassVar[DMType]

class ProviderName(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    PROVIDER_NAME_UNKNOWN: _ClassVar[ProviderName]
    EC2: _ClassVar[ProviderName]
    AWSEC2: _ClassVar[ProviderName]
    RACKSPACE: _ClassVar[ProviderName]
RESOURCE_JOB_MANAGER_TYPE_UNKNOWN: ResourceJobManagerType
FORK: ResourceJobManagerType
PBS: ResourceJobManagerType
SLURM: ResourceJobManagerType
LSF: ResourceJobManagerType
UGE: ResourceJobManagerType
CLOUD: ResourceJobManagerType
AIRAVATA_CUSTOM: ResourceJobManagerType
HTCONDOR: ResourceJobManagerType
JOB_MANAGER_COMMAND_UNKNOWN: JobManagerCommand
SUBMISSION: JobManagerCommand
JOB_MONITORING: JobManagerCommand
DELETION: JobManagerCommand
CHECK_JOB: JobManagerCommand
SHOW_QUEUE: JobManagerCommand
SHOW_RESERVATION: JobManagerCommand
SHOW_START: JobManagerCommand
SHOW_CLUSTER_INFO: JobManagerCommand
SHOW_NO_OF_RUNNING_JOBS: JobManagerCommand
SHOW_NO_OF_PENDING_JOBS: JobManagerCommand
FILE_SYSTEMS_UNKNOWN: FileSystems
HOME: FileSystems
WORK: FileSystems
LOCALTMP: FileSystems
SCRATCH: FileSystems
ARCHIVE: FileSystems
JOB_SUBMISSION_PROTOCOL_UNKNOWN: JobSubmissionProtocol
LOCAL: JobSubmissionProtocol
SSH: JobSubmissionProtocol
GLOBUS: JobSubmissionProtocol
UNICORE: JobSubmissionProtocol
JSP_CLOUD: JobSubmissionProtocol
SSH_FORK: JobSubmissionProtocol
LOCAL_FORK: JobSubmissionProtocol
MONITOR_MODE_UNKNOWN: MonitorMode
POLL_JOB_MANAGER: MonitorMode
CLOUD_JOB_MONITOR: MonitorMode
JOB_EMAIL_NOTIFICATION_MONITOR: MonitorMode
XSEDE_AMQP_SUBSCRIBE: MonitorMode
MONITOR_FORK: MonitorMode
MONITOR_LOCAL: MonitorMode
DM_TYPE_UNKNOWN: DMType
COMPUTE_RESOURCE: DMType
STORAGE_RESOURCE: DMType
PROVIDER_NAME_UNKNOWN: ProviderName
EC2: ProviderName
AWSEC2: ProviderName
RACKSPACE: ProviderName

class ResourceJobManager(_message.Message):
    __slots__ = ("resource_job_manager_id", "resource_job_manager_type", "push_monitoring_endpoint", "job_manager_bin_path", "job_manager_commands", "parallelism_prefix")
    class JobManagerCommandsEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: int
        value: str
        def __init__(self, key: _Optional[int] = ..., value: _Optional[str] = ...) -> None: ...
    class ParallelismPrefixEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: int
        value: str
        def __init__(self, key: _Optional[int] = ..., value: _Optional[str] = ...) -> None: ...
    RESOURCE_JOB_MANAGER_ID_FIELD_NUMBER: _ClassVar[int]
    RESOURCE_JOB_MANAGER_TYPE_FIELD_NUMBER: _ClassVar[int]
    PUSH_MONITORING_ENDPOINT_FIELD_NUMBER: _ClassVar[int]
    JOB_MANAGER_BIN_PATH_FIELD_NUMBER: _ClassVar[int]
    JOB_MANAGER_COMMANDS_FIELD_NUMBER: _ClassVar[int]
    PARALLELISM_PREFIX_FIELD_NUMBER: _ClassVar[int]
    resource_job_manager_id: str
    resource_job_manager_type: ResourceJobManagerType
    push_monitoring_endpoint: str
    job_manager_bin_path: str
    job_manager_commands: _containers.ScalarMap[int, str]
    parallelism_prefix: _containers.ScalarMap[int, str]
    def __init__(self, resource_job_manager_id: _Optional[str] = ..., resource_job_manager_type: _Optional[_Union[ResourceJobManagerType, str]] = ..., push_monitoring_endpoint: _Optional[str] = ..., job_manager_bin_path: _Optional[str] = ..., job_manager_commands: _Optional[_Mapping[int, str]] = ..., parallelism_prefix: _Optional[_Mapping[int, str]] = ...) -> None: ...

class BatchQueue(_message.Message):
    __slots__ = ("queue_name", "queue_description", "max_run_time", "max_nodes", "max_processors", "max_jobs_in_queue", "max_memory", "cpu_per_node", "default_node_count", "default_cpu_count", "default_walltime", "queue_specific_macros", "is_default_queue")
    QUEUE_NAME_FIELD_NUMBER: _ClassVar[int]
    QUEUE_DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    MAX_RUN_TIME_FIELD_NUMBER: _ClassVar[int]
    MAX_NODES_FIELD_NUMBER: _ClassVar[int]
    MAX_PROCESSORS_FIELD_NUMBER: _ClassVar[int]
    MAX_JOBS_IN_QUEUE_FIELD_NUMBER: _ClassVar[int]
    MAX_MEMORY_FIELD_NUMBER: _ClassVar[int]
    CPU_PER_NODE_FIELD_NUMBER: _ClassVar[int]
    DEFAULT_NODE_COUNT_FIELD_NUMBER: _ClassVar[int]
    DEFAULT_CPU_COUNT_FIELD_NUMBER: _ClassVar[int]
    DEFAULT_WALLTIME_FIELD_NUMBER: _ClassVar[int]
    QUEUE_SPECIFIC_MACROS_FIELD_NUMBER: _ClassVar[int]
    IS_DEFAULT_QUEUE_FIELD_NUMBER: _ClassVar[int]
    queue_name: str
    queue_description: str
    max_run_time: int
    max_nodes: int
    max_processors: int
    max_jobs_in_queue: int
    max_memory: int
    cpu_per_node: int
    default_node_count: int
    default_cpu_count: int
    default_walltime: int
    queue_specific_macros: str
    is_default_queue: bool
    def __init__(self, queue_name: _Optional[str] = ..., queue_description: _Optional[str] = ..., max_run_time: _Optional[int] = ..., max_nodes: _Optional[int] = ..., max_processors: _Optional[int] = ..., max_jobs_in_queue: _Optional[int] = ..., max_memory: _Optional[int] = ..., cpu_per_node: _Optional[int] = ..., default_node_count: _Optional[int] = ..., default_cpu_count: _Optional[int] = ..., default_walltime: _Optional[int] = ..., queue_specific_macros: _Optional[str] = ..., is_default_queue: bool = ...) -> None: ...

class LOCALSubmission(_message.Message):
    __slots__ = ("job_submission_interface_id", "resource_job_manager", "security_protocol")
    JOB_SUBMISSION_INTERFACE_ID_FIELD_NUMBER: _ClassVar[int]
    RESOURCE_JOB_MANAGER_FIELD_NUMBER: _ClassVar[int]
    SECURITY_PROTOCOL_FIELD_NUMBER: _ClassVar[int]
    job_submission_interface_id: str
    resource_job_manager: ResourceJobManager
    security_protocol: _data_movement_pb2.SecurityProtocol
    def __init__(self, job_submission_interface_id: _Optional[str] = ..., resource_job_manager: _Optional[_Union[ResourceJobManager, _Mapping]] = ..., security_protocol: _Optional[_Union[_data_movement_pb2.SecurityProtocol, str]] = ...) -> None: ...

class SSHJobSubmission(_message.Message):
    __slots__ = ("job_submission_interface_id", "security_protocol", "resource_job_manager", "alternative_ssh_host_name", "ssh_port", "monitor_mode", "batch_queue_email_senders")
    JOB_SUBMISSION_INTERFACE_ID_FIELD_NUMBER: _ClassVar[int]
    SECURITY_PROTOCOL_FIELD_NUMBER: _ClassVar[int]
    RESOURCE_JOB_MANAGER_FIELD_NUMBER: _ClassVar[int]
    ALTERNATIVE_SSH_HOST_NAME_FIELD_NUMBER: _ClassVar[int]
    SSH_PORT_FIELD_NUMBER: _ClassVar[int]
    MONITOR_MODE_FIELD_NUMBER: _ClassVar[int]
    BATCH_QUEUE_EMAIL_SENDERS_FIELD_NUMBER: _ClassVar[int]
    job_submission_interface_id: str
    security_protocol: _data_movement_pb2.SecurityProtocol
    resource_job_manager: ResourceJobManager
    alternative_ssh_host_name: str
    ssh_port: int
    monitor_mode: MonitorMode
    batch_queue_email_senders: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, job_submission_interface_id: _Optional[str] = ..., security_protocol: _Optional[_Union[_data_movement_pb2.SecurityProtocol, str]] = ..., resource_job_manager: _Optional[_Union[ResourceJobManager, _Mapping]] = ..., alternative_ssh_host_name: _Optional[str] = ..., ssh_port: _Optional[int] = ..., monitor_mode: _Optional[_Union[MonitorMode, str]] = ..., batch_queue_email_senders: _Optional[_Iterable[str]] = ...) -> None: ...

class GlobusJobSubmission(_message.Message):
    __slots__ = ("job_submission_interface_id", "security_protocol", "globus_gate_keeper_end_point")
    JOB_SUBMISSION_INTERFACE_ID_FIELD_NUMBER: _ClassVar[int]
    SECURITY_PROTOCOL_FIELD_NUMBER: _ClassVar[int]
    GLOBUS_GATE_KEEPER_END_POINT_FIELD_NUMBER: _ClassVar[int]
    job_submission_interface_id: str
    security_protocol: _data_movement_pb2.SecurityProtocol
    globus_gate_keeper_end_point: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, job_submission_interface_id: _Optional[str] = ..., security_protocol: _Optional[_Union[_data_movement_pb2.SecurityProtocol, str]] = ..., globus_gate_keeper_end_point: _Optional[_Iterable[str]] = ...) -> None: ...

class UnicoreJobSubmission(_message.Message):
    __slots__ = ("job_submission_interface_id", "security_protocol", "unicore_end_point_url")
    JOB_SUBMISSION_INTERFACE_ID_FIELD_NUMBER: _ClassVar[int]
    SECURITY_PROTOCOL_FIELD_NUMBER: _ClassVar[int]
    UNICORE_END_POINT_URL_FIELD_NUMBER: _ClassVar[int]
    job_submission_interface_id: str
    security_protocol: _data_movement_pb2.SecurityProtocol
    unicore_end_point_url: str
    def __init__(self, job_submission_interface_id: _Optional[str] = ..., security_protocol: _Optional[_Union[_data_movement_pb2.SecurityProtocol, str]] = ..., unicore_end_point_url: _Optional[str] = ...) -> None: ...

class CloudJobSubmission(_message.Message):
    __slots__ = ("job_submission_interface_id", "security_protocol", "node_id", "executable_type", "provider_name", "user_account_name")
    JOB_SUBMISSION_INTERFACE_ID_FIELD_NUMBER: _ClassVar[int]
    SECURITY_PROTOCOL_FIELD_NUMBER: _ClassVar[int]
    NODE_ID_FIELD_NUMBER: _ClassVar[int]
    EXECUTABLE_TYPE_FIELD_NUMBER: _ClassVar[int]
    PROVIDER_NAME_FIELD_NUMBER: _ClassVar[int]
    USER_ACCOUNT_NAME_FIELD_NUMBER: _ClassVar[int]
    job_submission_interface_id: str
    security_protocol: _data_movement_pb2.SecurityProtocol
    node_id: str
    executable_type: str
    provider_name: ProviderName
    user_account_name: str
    def __init__(self, job_submission_interface_id: _Optional[str] = ..., security_protocol: _Optional[_Union[_data_movement_pb2.SecurityProtocol, str]] = ..., node_id: _Optional[str] = ..., executable_type: _Optional[str] = ..., provider_name: _Optional[_Union[ProviderName, str]] = ..., user_account_name: _Optional[str] = ...) -> None: ...

class JobSubmissionInterface(_message.Message):
    __slots__ = ("job_submission_interface_id", "job_submission_protocol", "priority_order")
    JOB_SUBMISSION_INTERFACE_ID_FIELD_NUMBER: _ClassVar[int]
    JOB_SUBMISSION_PROTOCOL_FIELD_NUMBER: _ClassVar[int]
    PRIORITY_ORDER_FIELD_NUMBER: _ClassVar[int]
    job_submission_interface_id: str
    job_submission_protocol: JobSubmissionProtocol
    priority_order: int
    def __init__(self, job_submission_interface_id: _Optional[str] = ..., job_submission_protocol: _Optional[_Union[JobSubmissionProtocol, str]] = ..., priority_order: _Optional[int] = ...) -> None: ...

class ComputeResourceDescription(_message.Message):
    __slots__ = ("compute_resource_id", "host_name", "host_aliases", "ip_addresses", "resource_description", "enabled", "batch_queues", "file_systems", "job_submission_interfaces", "data_movement_interfaces", "max_memory_per_node", "gateway_usage_reporting", "gateway_usage_module_load_command", "gateway_usage_executable", "cpus_per_node", "default_node_count", "default_cpu_count", "default_walltime")
    class FileSystemsEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: int
        value: str
        def __init__(self, key: _Optional[int] = ..., value: _Optional[str] = ...) -> None: ...
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    HOST_NAME_FIELD_NUMBER: _ClassVar[int]
    HOST_ALIASES_FIELD_NUMBER: _ClassVar[int]
    IP_ADDRESSES_FIELD_NUMBER: _ClassVar[int]
    RESOURCE_DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    ENABLED_FIELD_NUMBER: _ClassVar[int]
    BATCH_QUEUES_FIELD_NUMBER: _ClassVar[int]
    FILE_SYSTEMS_FIELD_NUMBER: _ClassVar[int]
    JOB_SUBMISSION_INTERFACES_FIELD_NUMBER: _ClassVar[int]
    DATA_MOVEMENT_INTERFACES_FIELD_NUMBER: _ClassVar[int]
    MAX_MEMORY_PER_NODE_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_USAGE_REPORTING_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_USAGE_MODULE_LOAD_COMMAND_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_USAGE_EXECUTABLE_FIELD_NUMBER: _ClassVar[int]
    CPUS_PER_NODE_FIELD_NUMBER: _ClassVar[int]
    DEFAULT_NODE_COUNT_FIELD_NUMBER: _ClassVar[int]
    DEFAULT_CPU_COUNT_FIELD_NUMBER: _ClassVar[int]
    DEFAULT_WALLTIME_FIELD_NUMBER: _ClassVar[int]
    compute_resource_id: str
    host_name: str
    host_aliases: _containers.RepeatedScalarFieldContainer[str]
    ip_addresses: _containers.RepeatedScalarFieldContainer[str]
    resource_description: str
    enabled: bool
    batch_queues: _containers.RepeatedCompositeFieldContainer[BatchQueue]
    file_systems: _containers.ScalarMap[int, str]
    job_submission_interfaces: _containers.RepeatedCompositeFieldContainer[JobSubmissionInterface]
    data_movement_interfaces: _containers.RepeatedCompositeFieldContainer[_data_movement_pb2.DataMovementInterface]
    max_memory_per_node: int
    gateway_usage_reporting: bool
    gateway_usage_module_load_command: str
    gateway_usage_executable: str
    cpus_per_node: int
    default_node_count: int
    default_cpu_count: int
    default_walltime: int
    def __init__(self, compute_resource_id: _Optional[str] = ..., host_name: _Optional[str] = ..., host_aliases: _Optional[_Iterable[str]] = ..., ip_addresses: _Optional[_Iterable[str]] = ..., resource_description: _Optional[str] = ..., enabled: bool = ..., batch_queues: _Optional[_Iterable[_Union[BatchQueue, _Mapping]]] = ..., file_systems: _Optional[_Mapping[int, str]] = ..., job_submission_interfaces: _Optional[_Iterable[_Union[JobSubmissionInterface, _Mapping]]] = ..., data_movement_interfaces: _Optional[_Iterable[_Union[_data_movement_pb2.DataMovementInterface, _Mapping]]] = ..., max_memory_per_node: _Optional[int] = ..., gateway_usage_reporting: bool = ..., gateway_usage_module_load_command: _Optional[str] = ..., gateway_usage_executable: _Optional[str] = ..., cpus_per_node: _Optional[int] = ..., default_node_count: _Optional[int] = ..., default_cpu_count: _Optional[int] = ..., default_walltime: _Optional[int] = ...) -> None: ...
