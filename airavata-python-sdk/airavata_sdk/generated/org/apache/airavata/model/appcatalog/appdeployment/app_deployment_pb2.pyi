from org.apache.airavata.model.parallelism import parallelism_pb2 as _parallelism_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class SetEnvPaths(_message.Message):
    __slots__ = ("name", "value", "env_path_order")
    NAME_FIELD_NUMBER: _ClassVar[int]
    VALUE_FIELD_NUMBER: _ClassVar[int]
    ENV_PATH_ORDER_FIELD_NUMBER: _ClassVar[int]
    name: str
    value: str
    env_path_order: int
    def __init__(self, name: _Optional[str] = ..., value: _Optional[str] = ..., env_path_order: _Optional[int] = ...) -> None: ...

class CommandObject(_message.Message):
    __slots__ = ("command", "command_order")
    COMMAND_FIELD_NUMBER: _ClassVar[int]
    COMMAND_ORDER_FIELD_NUMBER: _ClassVar[int]
    command: str
    command_order: int
    def __init__(self, command: _Optional[str] = ..., command_order: _Optional[int] = ...) -> None: ...

class ApplicationModule(_message.Message):
    __slots__ = ("app_module_id", "app_module_name", "app_module_version", "app_module_description")
    APP_MODULE_ID_FIELD_NUMBER: _ClassVar[int]
    APP_MODULE_NAME_FIELD_NUMBER: _ClassVar[int]
    APP_MODULE_VERSION_FIELD_NUMBER: _ClassVar[int]
    APP_MODULE_DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    app_module_id: str
    app_module_name: str
    app_module_version: str
    app_module_description: str
    def __init__(self, app_module_id: _Optional[str] = ..., app_module_name: _Optional[str] = ..., app_module_version: _Optional[str] = ..., app_module_description: _Optional[str] = ...) -> None: ...

class ApplicationDeploymentDescription(_message.Message):
    __slots__ = ("app_deployment_id", "app_module_id", "compute_host_id", "executable_path", "parallelism", "app_deployment_description", "module_load_cmds", "lib_prepend_paths", "lib_append_paths", "set_environment", "pre_job_commands", "post_job_commands", "default_queue_name", "default_node_count", "default_cpu_count", "default_walltime", "editable_by_user")
    APP_DEPLOYMENT_ID_FIELD_NUMBER: _ClassVar[int]
    APP_MODULE_ID_FIELD_NUMBER: _ClassVar[int]
    COMPUTE_HOST_ID_FIELD_NUMBER: _ClassVar[int]
    EXECUTABLE_PATH_FIELD_NUMBER: _ClassVar[int]
    PARALLELISM_FIELD_NUMBER: _ClassVar[int]
    APP_DEPLOYMENT_DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    MODULE_LOAD_CMDS_FIELD_NUMBER: _ClassVar[int]
    LIB_PREPEND_PATHS_FIELD_NUMBER: _ClassVar[int]
    LIB_APPEND_PATHS_FIELD_NUMBER: _ClassVar[int]
    SET_ENVIRONMENT_FIELD_NUMBER: _ClassVar[int]
    PRE_JOB_COMMANDS_FIELD_NUMBER: _ClassVar[int]
    POST_JOB_COMMANDS_FIELD_NUMBER: _ClassVar[int]
    DEFAULT_QUEUE_NAME_FIELD_NUMBER: _ClassVar[int]
    DEFAULT_NODE_COUNT_FIELD_NUMBER: _ClassVar[int]
    DEFAULT_CPU_COUNT_FIELD_NUMBER: _ClassVar[int]
    DEFAULT_WALLTIME_FIELD_NUMBER: _ClassVar[int]
    EDITABLE_BY_USER_FIELD_NUMBER: _ClassVar[int]
    app_deployment_id: str
    app_module_id: str
    compute_host_id: str
    executable_path: str
    parallelism: _parallelism_pb2.ApplicationParallelismType
    app_deployment_description: str
    module_load_cmds: _containers.RepeatedCompositeFieldContainer[CommandObject]
    lib_prepend_paths: _containers.RepeatedCompositeFieldContainer[SetEnvPaths]
    lib_append_paths: _containers.RepeatedCompositeFieldContainer[SetEnvPaths]
    set_environment: _containers.RepeatedCompositeFieldContainer[SetEnvPaths]
    pre_job_commands: _containers.RepeatedCompositeFieldContainer[CommandObject]
    post_job_commands: _containers.RepeatedCompositeFieldContainer[CommandObject]
    default_queue_name: str
    default_node_count: int
    default_cpu_count: int
    default_walltime: int
    editable_by_user: bool
    def __init__(self, app_deployment_id: _Optional[str] = ..., app_module_id: _Optional[str] = ..., compute_host_id: _Optional[str] = ..., executable_path: _Optional[str] = ..., parallelism: _Optional[_Union[_parallelism_pb2.ApplicationParallelismType, str]] = ..., app_deployment_description: _Optional[str] = ..., module_load_cmds: _Optional[_Iterable[_Union[CommandObject, _Mapping]]] = ..., lib_prepend_paths: _Optional[_Iterable[_Union[SetEnvPaths, _Mapping]]] = ..., lib_append_paths: _Optional[_Iterable[_Union[SetEnvPaths, _Mapping]]] = ..., set_environment: _Optional[_Iterable[_Union[SetEnvPaths, _Mapping]]] = ..., pre_job_commands: _Optional[_Iterable[_Union[CommandObject, _Mapping]]] = ..., post_job_commands: _Optional[_Iterable[_Union[CommandObject, _Mapping]]] = ..., default_queue_name: _Optional[str] = ..., default_node_count: _Optional[int] = ..., default_cpu_count: _Optional[int] = ..., default_walltime: _Optional[int] = ..., editable_by_user: bool = ...) -> None: ...
