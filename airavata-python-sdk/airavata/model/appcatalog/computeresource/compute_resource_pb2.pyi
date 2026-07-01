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

class ComputeResourceDescription(_message.Message):
    __slots__ = ("compute_resource_id", "host_name", "host_aliases", "ip_addresses", "resource_description", "enabled", "batch_queues", "file_systems", "max_memory_per_node", "gateway_usage_reporting", "gateway_usage_module_load_command", "gateway_usage_executable", "cpus_per_node", "default_node_count", "default_cpu_count", "default_walltime", "ssh_port", "resource_job_manager")
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
    MAX_MEMORY_PER_NODE_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_USAGE_REPORTING_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_USAGE_MODULE_LOAD_COMMAND_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_USAGE_EXECUTABLE_FIELD_NUMBER: _ClassVar[int]
    CPUS_PER_NODE_FIELD_NUMBER: _ClassVar[int]
    DEFAULT_NODE_COUNT_FIELD_NUMBER: _ClassVar[int]
    DEFAULT_CPU_COUNT_FIELD_NUMBER: _ClassVar[int]
    DEFAULT_WALLTIME_FIELD_NUMBER: _ClassVar[int]
    SSH_PORT_FIELD_NUMBER: _ClassVar[int]
    RESOURCE_JOB_MANAGER_FIELD_NUMBER: _ClassVar[int]
    compute_resource_id: str
    host_name: str
    host_aliases: _containers.RepeatedScalarFieldContainer[str]
    ip_addresses: _containers.RepeatedScalarFieldContainer[str]
    resource_description: str
    enabled: bool
    batch_queues: _containers.RepeatedCompositeFieldContainer[BatchQueue]
    file_systems: _containers.ScalarMap[int, str]
    max_memory_per_node: int
    gateway_usage_reporting: bool
    gateway_usage_module_load_command: str
    gateway_usage_executable: str
    cpus_per_node: int
    default_node_count: int
    default_cpu_count: int
    default_walltime: int
    ssh_port: int
    resource_job_manager: ResourceJobManager
    def __init__(self, compute_resource_id: _Optional[str] = ..., host_name: _Optional[str] = ..., host_aliases: _Optional[_Iterable[str]] = ..., ip_addresses: _Optional[_Iterable[str]] = ..., resource_description: _Optional[str] = ..., enabled: bool = ..., batch_queues: _Optional[_Iterable[_Union[BatchQueue, _Mapping]]] = ..., file_systems: _Optional[_Mapping[int, str]] = ..., max_memory_per_node: _Optional[int] = ..., gateway_usage_reporting: bool = ..., gateway_usage_module_load_command: _Optional[str] = ..., gateway_usage_executable: _Optional[str] = ..., cpus_per_node: _Optional[int] = ..., default_node_count: _Optional[int] = ..., default_cpu_count: _Optional[int] = ..., default_walltime: _Optional[int] = ..., ssh_port: _Optional[int] = ..., resource_job_manager: _Optional[_Union[ResourceJobManager, _Mapping]] = ...) -> None: ...
