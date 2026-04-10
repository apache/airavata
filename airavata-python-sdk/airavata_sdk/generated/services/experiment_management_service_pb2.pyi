from google.api import annotations_pb2 as _annotations_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class AgentLaunchRequest(_message.Message):
    __slots__ = ("experiment_name", "project_name", "remote_cluster", "group", "libraries", "pip", "mounts", "queue", "wall_time", "cpu_count", "node_count", "memory", "input_storage_id", "output_storage_id")
    EXPERIMENT_NAME_FIELD_NUMBER: _ClassVar[int]
    PROJECT_NAME_FIELD_NUMBER: _ClassVar[int]
    REMOTE_CLUSTER_FIELD_NUMBER: _ClassVar[int]
    GROUP_FIELD_NUMBER: _ClassVar[int]
    LIBRARIES_FIELD_NUMBER: _ClassVar[int]
    PIP_FIELD_NUMBER: _ClassVar[int]
    MOUNTS_FIELD_NUMBER: _ClassVar[int]
    QUEUE_FIELD_NUMBER: _ClassVar[int]
    WALL_TIME_FIELD_NUMBER: _ClassVar[int]
    CPU_COUNT_FIELD_NUMBER: _ClassVar[int]
    NODE_COUNT_FIELD_NUMBER: _ClassVar[int]
    MEMORY_FIELD_NUMBER: _ClassVar[int]
    INPUT_STORAGE_ID_FIELD_NUMBER: _ClassVar[int]
    OUTPUT_STORAGE_ID_FIELD_NUMBER: _ClassVar[int]
    experiment_name: str
    project_name: str
    remote_cluster: str
    group: str
    libraries: _containers.RepeatedScalarFieldContainer[str]
    pip: _containers.RepeatedScalarFieldContainer[str]
    mounts: _containers.RepeatedScalarFieldContainer[str]
    queue: str
    wall_time: int
    cpu_count: int
    node_count: int
    memory: int
    input_storage_id: str
    output_storage_id: str
    def __init__(self, experiment_name: _Optional[str] = ..., project_name: _Optional[str] = ..., remote_cluster: _Optional[str] = ..., group: _Optional[str] = ..., libraries: _Optional[_Iterable[str]] = ..., pip: _Optional[_Iterable[str]] = ..., mounts: _Optional[_Iterable[str]] = ..., queue: _Optional[str] = ..., wall_time: _Optional[int] = ..., cpu_count: _Optional[int] = ..., node_count: _Optional[int] = ..., memory: _Optional[int] = ..., input_storage_id: _Optional[str] = ..., output_storage_id: _Optional[str] = ...) -> None: ...

class AgentLaunchResponse(_message.Message):
    __slots__ = ("agent_id", "experiment_id", "env_name", "process_id")
    AGENT_ID_FIELD_NUMBER: _ClassVar[int]
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    ENV_NAME_FIELD_NUMBER: _ClassVar[int]
    PROCESS_ID_FIELD_NUMBER: _ClassVar[int]
    agent_id: str
    experiment_id: str
    env_name: str
    process_id: str
    def __init__(self, agent_id: _Optional[str] = ..., experiment_id: _Optional[str] = ..., env_name: _Optional[str] = ..., process_id: _Optional[str] = ...) -> None: ...

class AgentTerminateResponse(_message.Message):
    __slots__ = ("experiment_id", "terminated")
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    TERMINATED_FIELD_NUMBER: _ClassVar[int]
    experiment_id: str
    terminated: bool
    def __init__(self, experiment_id: _Optional[str] = ..., terminated: bool = ...) -> None: ...

class GetAgentExperimentRequest(_message.Message):
    __slots__ = ("experiment_id",)
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    experiment_id: str
    def __init__(self, experiment_id: _Optional[str] = ...) -> None: ...

class AgentExperimentResponse(_message.Message):
    __slots__ = ("experiment_json",)
    EXPERIMENT_JSON_FIELD_NUMBER: _ClassVar[int]
    experiment_json: str
    def __init__(self, experiment_json: _Optional[str] = ...) -> None: ...

class LaunchOptimizedExperimentRequest(_message.Message):
    __slots__ = ("requests",)
    REQUESTS_FIELD_NUMBER: _ClassVar[int]
    requests: _containers.RepeatedCompositeFieldContainer[AgentLaunchRequest]
    def __init__(self, requests: _Optional[_Iterable[_Union[AgentLaunchRequest, _Mapping]]] = ...) -> None: ...

class TerminateAgentExperimentRequest(_message.Message):
    __slots__ = ("experiment_id",)
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    experiment_id: str
    def __init__(self, experiment_id: _Optional[str] = ...) -> None: ...

class GetProcessModelRequest(_message.Message):
    __slots__ = ("experiment_id",)
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    experiment_id: str
    def __init__(self, experiment_id: _Optional[str] = ...) -> None: ...

class ProcessModelResponse(_message.Message):
    __slots__ = ("process_json", "found")
    PROCESS_JSON_FIELD_NUMBER: _ClassVar[int]
    FOUND_FIELD_NUMBER: _ClassVar[int]
    process_json: str
    found: bool
    def __init__(self, process_json: _Optional[str] = ..., found: bool = ...) -> None: ...
