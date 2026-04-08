from google.api import annotations_pb2 as _annotations_pb2
from google.protobuf import struct_pb2 as _struct_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class AgentInfoResponse(_message.Message):
    __slots__ = ("agent_id", "is_agent_up")
    AGENT_ID_FIELD_NUMBER: _ClassVar[int]
    IS_AGENT_UP_FIELD_NUMBER: _ClassVar[int]
    agent_id: str
    is_agent_up: bool
    def __init__(self, agent_id: _Optional[str] = ..., is_agent_up: bool = ...) -> None: ...

class AgentExecutionAck(_message.Message):
    __slots__ = ("execution_id", "error")
    EXECUTION_ID_FIELD_NUMBER: _ClassVar[int]
    ERROR_FIELD_NUMBER: _ClassVar[int]
    execution_id: str
    error: str
    def __init__(self, execution_id: _Optional[str] = ..., error: _Optional[str] = ...) -> None: ...

class AgentEnvSetupRequest(_message.Message):
    __slots__ = ("agent_id", "env_name", "libraries", "pip")
    AGENT_ID_FIELD_NUMBER: _ClassVar[int]
    ENV_NAME_FIELD_NUMBER: _ClassVar[int]
    LIBRARIES_FIELD_NUMBER: _ClassVar[int]
    PIP_FIELD_NUMBER: _ClassVar[int]
    agent_id: str
    env_name: str
    libraries: _containers.RepeatedScalarFieldContainer[str]
    pip: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, agent_id: _Optional[str] = ..., env_name: _Optional[str] = ..., libraries: _Optional[_Iterable[str]] = ..., pip: _Optional[_Iterable[str]] = ...) -> None: ...

class AgentEnvSetupResponse(_message.Message):
    __slots__ = ("execution_id", "setup", "status")
    EXECUTION_ID_FIELD_NUMBER: _ClassVar[int]
    SETUP_FIELD_NUMBER: _ClassVar[int]
    STATUS_FIELD_NUMBER: _ClassVar[int]
    execution_id: str
    setup: bool
    status: str
    def __init__(self, execution_id: _Optional[str] = ..., setup: bool = ..., status: _Optional[str] = ...) -> None: ...

class AgentCommandExecutionRequest(_message.Message):
    __slots__ = ("agent_id", "env_name", "working_dir", "arguments")
    AGENT_ID_FIELD_NUMBER: _ClassVar[int]
    ENV_NAME_FIELD_NUMBER: _ClassVar[int]
    WORKING_DIR_FIELD_NUMBER: _ClassVar[int]
    ARGUMENTS_FIELD_NUMBER: _ClassVar[int]
    agent_id: str
    env_name: str
    working_dir: str
    arguments: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, agent_id: _Optional[str] = ..., env_name: _Optional[str] = ..., working_dir: _Optional[str] = ..., arguments: _Optional[_Iterable[str]] = ...) -> None: ...

class AgentCommandExecutionResponse(_message.Message):
    __slots__ = ("execution_id", "executed", "response_string")
    EXECUTION_ID_FIELD_NUMBER: _ClassVar[int]
    EXECUTED_FIELD_NUMBER: _ClassVar[int]
    RESPONSE_STRING_FIELD_NUMBER: _ClassVar[int]
    execution_id: str
    executed: bool
    response_string: str
    def __init__(self, execution_id: _Optional[str] = ..., executed: bool = ..., response_string: _Optional[str] = ...) -> None: ...

class AgentAsyncCommandExecutionRequest(_message.Message):
    __slots__ = ("agent_id", "env_name", "working_dir", "arguments")
    AGENT_ID_FIELD_NUMBER: _ClassVar[int]
    ENV_NAME_FIELD_NUMBER: _ClassVar[int]
    WORKING_DIR_FIELD_NUMBER: _ClassVar[int]
    ARGUMENTS_FIELD_NUMBER: _ClassVar[int]
    agent_id: str
    env_name: str
    working_dir: str
    arguments: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, agent_id: _Optional[str] = ..., env_name: _Optional[str] = ..., working_dir: _Optional[str] = ..., arguments: _Optional[_Iterable[str]] = ...) -> None: ...

class AgentAsyncCommandExecutionResponse(_message.Message):
    __slots__ = ("execution_id", "process_id", "error_message")
    EXECUTION_ID_FIELD_NUMBER: _ClassVar[int]
    PROCESS_ID_FIELD_NUMBER: _ClassVar[int]
    ERROR_MESSAGE_FIELD_NUMBER: _ClassVar[int]
    execution_id: str
    process_id: int
    error_message: str
    def __init__(self, execution_id: _Optional[str] = ..., process_id: _Optional[int] = ..., error_message: _Optional[str] = ...) -> None: ...

class AgentAsyncCommandListRequest(_message.Message):
    __slots__ = ("agent_id",)
    AGENT_ID_FIELD_NUMBER: _ClassVar[int]
    agent_id: str
    def __init__(self, agent_id: _Optional[str] = ...) -> None: ...

class AgentAsyncCommand(_message.Message):
    __slots__ = ("process_id", "arguments")
    PROCESS_ID_FIELD_NUMBER: _ClassVar[int]
    ARGUMENTS_FIELD_NUMBER: _ClassVar[int]
    process_id: int
    arguments: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, process_id: _Optional[int] = ..., arguments: _Optional[_Iterable[str]] = ...) -> None: ...

class AgentAsyncCommandListResponse(_message.Message):
    __slots__ = ("execution_id", "commands", "error")
    EXECUTION_ID_FIELD_NUMBER: _ClassVar[int]
    COMMANDS_FIELD_NUMBER: _ClassVar[int]
    ERROR_FIELD_NUMBER: _ClassVar[int]
    execution_id: str
    commands: _containers.RepeatedCompositeFieldContainer[AgentAsyncCommand]
    error: str
    def __init__(self, execution_id: _Optional[str] = ..., commands: _Optional[_Iterable[_Union[AgentAsyncCommand, _Mapping]]] = ..., error: _Optional[str] = ...) -> None: ...

class AgentAsyncCommandTerminateRequest(_message.Message):
    __slots__ = ("agent_id", "process_id")
    AGENT_ID_FIELD_NUMBER: _ClassVar[int]
    PROCESS_ID_FIELD_NUMBER: _ClassVar[int]
    agent_id: str
    process_id: int
    def __init__(self, agent_id: _Optional[str] = ..., process_id: _Optional[int] = ...) -> None: ...

class AgentAsyncCommandTerminateResponse(_message.Message):
    __slots__ = ("execution_id", "status")
    EXECUTION_ID_FIELD_NUMBER: _ClassVar[int]
    STATUS_FIELD_NUMBER: _ClassVar[int]
    execution_id: str
    status: str
    def __init__(self, execution_id: _Optional[str] = ..., status: _Optional[str] = ...) -> None: ...

class AgentJupyterExecutionRequest(_message.Message):
    __slots__ = ("agent_id", "env_name", "code")
    AGENT_ID_FIELD_NUMBER: _ClassVar[int]
    ENV_NAME_FIELD_NUMBER: _ClassVar[int]
    CODE_FIELD_NUMBER: _ClassVar[int]
    agent_id: str
    env_name: str
    code: str
    def __init__(self, agent_id: _Optional[str] = ..., env_name: _Optional[str] = ..., code: _Optional[str] = ...) -> None: ...

class AgentJupyterExecutionResponse(_message.Message):
    __slots__ = ("execution_id", "executed", "response_string")
    EXECUTION_ID_FIELD_NUMBER: _ClassVar[int]
    EXECUTED_FIELD_NUMBER: _ClassVar[int]
    RESPONSE_STRING_FIELD_NUMBER: _ClassVar[int]
    execution_id: str
    executed: bool
    response_string: str
    def __init__(self, execution_id: _Optional[str] = ..., executed: bool = ..., response_string: _Optional[str] = ...) -> None: ...

class AgentPythonExecutionRequest(_message.Message):
    __slots__ = ("agent_id", "env_name", "working_dir", "code")
    AGENT_ID_FIELD_NUMBER: _ClassVar[int]
    ENV_NAME_FIELD_NUMBER: _ClassVar[int]
    WORKING_DIR_FIELD_NUMBER: _ClassVar[int]
    CODE_FIELD_NUMBER: _ClassVar[int]
    agent_id: str
    env_name: str
    working_dir: str
    code: str
    def __init__(self, agent_id: _Optional[str] = ..., env_name: _Optional[str] = ..., working_dir: _Optional[str] = ..., code: _Optional[str] = ...) -> None: ...

class AgentPythonExecutionResponse(_message.Message):
    __slots__ = ("execution_id", "executed", "response_string")
    EXECUTION_ID_FIELD_NUMBER: _ClassVar[int]
    EXECUTED_FIELD_NUMBER: _ClassVar[int]
    RESPONSE_STRING_FIELD_NUMBER: _ClassVar[int]
    execution_id: str
    executed: bool
    response_string: str
    def __init__(self, execution_id: _Optional[str] = ..., executed: bool = ..., response_string: _Optional[str] = ...) -> None: ...

class AgentKernelRestartRequest(_message.Message):
    __slots__ = ("agent_id", "env_name")
    AGENT_ID_FIELD_NUMBER: _ClassVar[int]
    ENV_NAME_FIELD_NUMBER: _ClassVar[int]
    agent_id: str
    env_name: str
    def __init__(self, agent_id: _Optional[str] = ..., env_name: _Optional[str] = ...) -> None: ...

class AgentKernelRestartResponse(_message.Message):
    __slots__ = ("execution_id", "status", "restarted")
    EXECUTION_ID_FIELD_NUMBER: _ClassVar[int]
    STATUS_FIELD_NUMBER: _ClassVar[int]
    RESTARTED_FIELD_NUMBER: _ClassVar[int]
    execution_id: str
    status: str
    restarted: bool
    def __init__(self, execution_id: _Optional[str] = ..., status: _Optional[str] = ..., restarted: bool = ...) -> None: ...

class AgentTunnelCreateRequest(_message.Message):
    __slots__ = ("agent_id", "local_port", "local_bind_host")
    AGENT_ID_FIELD_NUMBER: _ClassVar[int]
    LOCAL_PORT_FIELD_NUMBER: _ClassVar[int]
    LOCAL_BIND_HOST_FIELD_NUMBER: _ClassVar[int]
    agent_id: str
    local_port: int
    local_bind_host: str
    def __init__(self, agent_id: _Optional[str] = ..., local_port: _Optional[int] = ..., local_bind_host: _Optional[str] = ...) -> None: ...

class AgentTunnelAck(_message.Message):
    __slots__ = ("execution_id", "status", "error")
    EXECUTION_ID_FIELD_NUMBER: _ClassVar[int]
    STATUS_FIELD_NUMBER: _ClassVar[int]
    ERROR_FIELD_NUMBER: _ClassVar[int]
    execution_id: str
    status: int
    error: str
    def __init__(self, execution_id: _Optional[str] = ..., status: _Optional[int] = ..., error: _Optional[str] = ...) -> None: ...

class AgentTunnelCreateResponse(_message.Message):
    __slots__ = ("execution_id", "tunnel_id", "proxy_port", "proxy_host", "status")
    EXECUTION_ID_FIELD_NUMBER: _ClassVar[int]
    TUNNEL_ID_FIELD_NUMBER: _ClassVar[int]
    PROXY_PORT_FIELD_NUMBER: _ClassVar[int]
    PROXY_HOST_FIELD_NUMBER: _ClassVar[int]
    STATUS_FIELD_NUMBER: _ClassVar[int]
    execution_id: str
    tunnel_id: str
    proxy_port: int
    proxy_host: str
    status: str
    def __init__(self, execution_id: _Optional[str] = ..., tunnel_id: _Optional[str] = ..., proxy_port: _Optional[int] = ..., proxy_host: _Optional[str] = ..., status: _Optional[str] = ...) -> None: ...

class AgentTunnelTerminateRequest(_message.Message):
    __slots__ = ("agent_id", "tunnel_id")
    AGENT_ID_FIELD_NUMBER: _ClassVar[int]
    TUNNEL_ID_FIELD_NUMBER: _ClassVar[int]
    agent_id: str
    tunnel_id: str
    def __init__(self, agent_id: _Optional[str] = ..., tunnel_id: _Optional[str] = ...) -> None: ...

class GetAgentInfoRequest(_message.Message):
    __slots__ = ("agent_id",)
    AGENT_ID_FIELD_NUMBER: _ClassVar[int]
    agent_id: str
    def __init__(self, agent_id: _Optional[str] = ...) -> None: ...

class GetExecutionResponseRequest(_message.Message):
    __slots__ = ("execution_id",)
    EXECUTION_ID_FIELD_NUMBER: _ClassVar[int]
    execution_id: str
    def __init__(self, execution_id: _Optional[str] = ...) -> None: ...

class SavePlanRequest(_message.Message):
    __slots__ = ("id", "data")
    ID_FIELD_NUMBER: _ClassVar[int]
    DATA_FIELD_NUMBER: _ClassVar[int]
    id: str
    data: _struct_pb2.Struct
    def __init__(self, id: _Optional[str] = ..., data: _Optional[_Union[_struct_pb2.Struct, _Mapping]] = ...) -> None: ...

class UpdatePlanRequest(_message.Message):
    __slots__ = ("plan_id", "data")
    PLAN_ID_FIELD_NUMBER: _ClassVar[int]
    DATA_FIELD_NUMBER: _ClassVar[int]
    plan_id: str
    data: _struct_pb2.Struct
    def __init__(self, plan_id: _Optional[str] = ..., data: _Optional[_Union[_struct_pb2.Struct, _Mapping]] = ...) -> None: ...

class GetPlanRequest(_message.Message):
    __slots__ = ("plan_id",)
    PLAN_ID_FIELD_NUMBER: _ClassVar[int]
    plan_id: str
    def __init__(self, plan_id: _Optional[str] = ...) -> None: ...

class GetPlansByUserRequest(_message.Message):
    __slots__ = ()
    def __init__(self) -> None: ...

class GetPlansByUserResponse(_message.Message):
    __slots__ = ("plans",)
    PLANS_FIELD_NUMBER: _ClassVar[int]
    plans: _containers.RepeatedCompositeFieldContainer[PlanMessage]
    def __init__(self, plans: _Optional[_Iterable[_Union[PlanMessage, _Mapping]]] = ...) -> None: ...

class PlanMessage(_message.Message):
    __slots__ = ("id", "user_id", "gateway_id", "data")
    ID_FIELD_NUMBER: _ClassVar[int]
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    DATA_FIELD_NUMBER: _ClassVar[int]
    id: str
    user_id: str
    gateway_id: str
    data: str
    def __init__(self, id: _Optional[str] = ..., user_id: _Optional[str] = ..., gateway_id: _Optional[str] = ..., data: _Optional[str] = ...) -> None: ...
