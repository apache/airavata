from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class AgentPing(_message.Message):
    __slots__ = ("agentId",)
    AGENTID_FIELD_NUMBER: _ClassVar[int]
    agentId: str
    def __init__(self, agentId: _Optional[str] = ...) -> None: ...

class ShutdownRequest(_message.Message):
    __slots__ = ("agentId",)
    AGENTID_FIELD_NUMBER: _ClassVar[int]
    agentId: str
    def __init__(self, agentId: _Optional[str] = ...) -> None: ...

class CreateAgentRequest(_message.Message):
    __slots__ = ("executionId", "agentId", "containerId", "workingDir", "mounts")
    EXECUTIONID_FIELD_NUMBER: _ClassVar[int]
    AGENTID_FIELD_NUMBER: _ClassVar[int]
    CONTAINERID_FIELD_NUMBER: _ClassVar[int]
    WORKINGDIR_FIELD_NUMBER: _ClassVar[int]
    MOUNTS_FIELD_NUMBER: _ClassVar[int]
    executionId: str
    agentId: str
    containerId: str
    workingDir: str
    mounts: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, executionId: _Optional[str] = ..., agentId: _Optional[str] = ..., containerId: _Optional[str] = ..., workingDir: _Optional[str] = ..., mounts: _Optional[_Iterable[str]] = ...) -> None: ...

class CreateAgentResponse(_message.Message):
    __slots__ = ("executionId", "agentId", "status")
    EXECUTIONID_FIELD_NUMBER: _ClassVar[int]
    AGENTID_FIELD_NUMBER: _ClassVar[int]
    STATUS_FIELD_NUMBER: _ClassVar[int]
    executionId: str
    agentId: str
    status: str
    def __init__(self, executionId: _Optional[str] = ..., agentId: _Optional[str] = ..., status: _Optional[str] = ...) -> None: ...

class TerminateAgentRequest(_message.Message):
    __slots__ = ("executionId", "agentId")
    EXECUTIONID_FIELD_NUMBER: _ClassVar[int]
    AGENTID_FIELD_NUMBER: _ClassVar[int]
    executionId: str
    agentId: str
    def __init__(self, executionId: _Optional[str] = ..., agentId: _Optional[str] = ...) -> None: ...

class TerminateAgentResponse(_message.Message):
    __slots__ = ("executionId", "agentId", "status")
    EXECUTIONID_FIELD_NUMBER: _ClassVar[int]
    AGENTID_FIELD_NUMBER: _ClassVar[int]
    STATUS_FIELD_NUMBER: _ClassVar[int]
    executionId: str
    agentId: str
    status: str
    def __init__(self, executionId: _Optional[str] = ..., agentId: _Optional[str] = ..., status: _Optional[str] = ...) -> None: ...

class EnvSetupRequest(_message.Message):
    __slots__ = ("executionId", "envName", "libraries", "pip")
    EXECUTIONID_FIELD_NUMBER: _ClassVar[int]
    ENVNAME_FIELD_NUMBER: _ClassVar[int]
    LIBRARIES_FIELD_NUMBER: _ClassVar[int]
    PIP_FIELD_NUMBER: _ClassVar[int]
    executionId: str
    envName: str
    libraries: _containers.RepeatedScalarFieldContainer[str]
    pip: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, executionId: _Optional[str] = ..., envName: _Optional[str] = ..., libraries: _Optional[_Iterable[str]] = ..., pip: _Optional[_Iterable[str]] = ...) -> None: ...

class EnvSetupResponse(_message.Message):
    __slots__ = ("executionId", "status")
    EXECUTIONID_FIELD_NUMBER: _ClassVar[int]
    STATUS_FIELD_NUMBER: _ClassVar[int]
    executionId: str
    status: str
    def __init__(self, executionId: _Optional[str] = ..., status: _Optional[str] = ...) -> None: ...

class CommandExecutionRequest(_message.Message):
    __slots__ = ("executionId", "envName", "workingDir", "arguments")
    EXECUTIONID_FIELD_NUMBER: _ClassVar[int]
    ENVNAME_FIELD_NUMBER: _ClassVar[int]
    WORKINGDIR_FIELD_NUMBER: _ClassVar[int]
    ARGUMENTS_FIELD_NUMBER: _ClassVar[int]
    executionId: str
    envName: str
    workingDir: str
    arguments: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, executionId: _Optional[str] = ..., envName: _Optional[str] = ..., workingDir: _Optional[str] = ..., arguments: _Optional[_Iterable[str]] = ...) -> None: ...

class CommandExecutionResponse(_message.Message):
    __slots__ = ("executionId", "responseString")
    EXECUTIONID_FIELD_NUMBER: _ClassVar[int]
    RESPONSESTRING_FIELD_NUMBER: _ClassVar[int]
    executionId: str
    responseString: str
    def __init__(self, executionId: _Optional[str] = ..., responseString: _Optional[str] = ...) -> None: ...

class AsyncCommandExecutionRequest(_message.Message):
    __slots__ = ("executionId", "envName", "workingDir", "arguments")
    EXECUTIONID_FIELD_NUMBER: _ClassVar[int]
    ENVNAME_FIELD_NUMBER: _ClassVar[int]
    WORKINGDIR_FIELD_NUMBER: _ClassVar[int]
    ARGUMENTS_FIELD_NUMBER: _ClassVar[int]
    executionId: str
    envName: str
    workingDir: str
    arguments: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, executionId: _Optional[str] = ..., envName: _Optional[str] = ..., workingDir: _Optional[str] = ..., arguments: _Optional[_Iterable[str]] = ...) -> None: ...

class AsyncCommandExecutionResponse(_message.Message):
    __slots__ = ("executionId", "processId", "errorMessage")
    EXECUTIONID_FIELD_NUMBER: _ClassVar[int]
    PROCESSID_FIELD_NUMBER: _ClassVar[int]
    ERRORMESSAGE_FIELD_NUMBER: _ClassVar[int]
    executionId: str
    processId: int
    errorMessage: str
    def __init__(self, executionId: _Optional[str] = ..., processId: _Optional[int] = ..., errorMessage: _Optional[str] = ...) -> None: ...

class AsyncCommandListRequest(_message.Message):
    __slots__ = ("executionId",)
    EXECUTIONID_FIELD_NUMBER: _ClassVar[int]
    executionId: str
    def __init__(self, executionId: _Optional[str] = ...) -> None: ...

class AsyncCommand(_message.Message):
    __slots__ = ("processId", "arguments")
    PROCESSID_FIELD_NUMBER: _ClassVar[int]
    ARGUMENTS_FIELD_NUMBER: _ClassVar[int]
    processId: int
    arguments: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, processId: _Optional[int] = ..., arguments: _Optional[_Iterable[str]] = ...) -> None: ...

class AsyncCommandListResponse(_message.Message):
    __slots__ = ("executionId", "commands")
    EXECUTIONID_FIELD_NUMBER: _ClassVar[int]
    COMMANDS_FIELD_NUMBER: _ClassVar[int]
    executionId: str
    commands: _containers.RepeatedCompositeFieldContainer[AsyncCommand]
    def __init__(self, executionId: _Optional[str] = ..., commands: _Optional[_Iterable[_Union[AsyncCommand, _Mapping]]] = ...) -> None: ...

class AsyncCommandTerminateRequest(_message.Message):
    __slots__ = ("executionId", "processId")
    EXECUTIONID_FIELD_NUMBER: _ClassVar[int]
    PROCESSID_FIELD_NUMBER: _ClassVar[int]
    executionId: str
    processId: int
    def __init__(self, executionId: _Optional[str] = ..., processId: _Optional[int] = ...) -> None: ...

class AsyncCommandTerminateResponse(_message.Message):
    __slots__ = ("executionId", "status")
    EXECUTIONID_FIELD_NUMBER: _ClassVar[int]
    STATUS_FIELD_NUMBER: _ClassVar[int]
    executionId: str
    status: str
    def __init__(self, executionId: _Optional[str] = ..., status: _Optional[str] = ...) -> None: ...

class PythonExecutionRequest(_message.Message):
    __slots__ = ("executionId", "envName", "workingDir", "code")
    EXECUTIONID_FIELD_NUMBER: _ClassVar[int]
    ENVNAME_FIELD_NUMBER: _ClassVar[int]
    WORKINGDIR_FIELD_NUMBER: _ClassVar[int]
    CODE_FIELD_NUMBER: _ClassVar[int]
    executionId: str
    envName: str
    workingDir: str
    code: str
    def __init__(self, executionId: _Optional[str] = ..., envName: _Optional[str] = ..., workingDir: _Optional[str] = ..., code: _Optional[str] = ...) -> None: ...

class PythonExecutionResponse(_message.Message):
    __slots__ = ("executionId", "responseString")
    EXECUTIONID_FIELD_NUMBER: _ClassVar[int]
    RESPONSESTRING_FIELD_NUMBER: _ClassVar[int]
    executionId: str
    responseString: str
    def __init__(self, executionId: _Optional[str] = ..., responseString: _Optional[str] = ...) -> None: ...

class JupyterExecutionRequest(_message.Message):
    __slots__ = ("executionId", "envName", "code")
    EXECUTIONID_FIELD_NUMBER: _ClassVar[int]
    ENVNAME_FIELD_NUMBER: _ClassVar[int]
    CODE_FIELD_NUMBER: _ClassVar[int]
    executionId: str
    envName: str
    code: str
    def __init__(self, executionId: _Optional[str] = ..., envName: _Optional[str] = ..., code: _Optional[str] = ...) -> None: ...

class JupyterExecutionResponse(_message.Message):
    __slots__ = ("executionId", "responseString")
    EXECUTIONID_FIELD_NUMBER: _ClassVar[int]
    RESPONSESTRING_FIELD_NUMBER: _ClassVar[int]
    executionId: str
    responseString: str
    def __init__(self, executionId: _Optional[str] = ..., responseString: _Optional[str] = ...) -> None: ...

class KernelRestartRequest(_message.Message):
    __slots__ = ("executionId", "envName")
    EXECUTIONID_FIELD_NUMBER: _ClassVar[int]
    ENVNAME_FIELD_NUMBER: _ClassVar[int]
    executionId: str
    envName: str
    def __init__(self, executionId: _Optional[str] = ..., envName: _Optional[str] = ...) -> None: ...

class KernelRestartResponse(_message.Message):
    __slots__ = ("executionId", "status")
    EXECUTIONID_FIELD_NUMBER: _ClassVar[int]
    STATUS_FIELD_NUMBER: _ClassVar[int]
    executionId: str
    status: str
    def __init__(self, executionId: _Optional[str] = ..., status: _Optional[str] = ...) -> None: ...

class TunnelCreationRequest(_message.Message):
    __slots__ = ("executionId", "localPort", "localBindHost", "tunnelServerHost", "tunnelServerPort", "tunnelServerApiUrl", "tunnelServerToken")
    EXECUTIONID_FIELD_NUMBER: _ClassVar[int]
    LOCALPORT_FIELD_NUMBER: _ClassVar[int]
    LOCALBINDHOST_FIELD_NUMBER: _ClassVar[int]
    TUNNELSERVERHOST_FIELD_NUMBER: _ClassVar[int]
    TUNNELSERVERPORT_FIELD_NUMBER: _ClassVar[int]
    TUNNELSERVERAPIURL_FIELD_NUMBER: _ClassVar[int]
    TUNNELSERVERTOKEN_FIELD_NUMBER: _ClassVar[int]
    executionId: str
    localPort: int
    localBindHost: str
    tunnelServerHost: str
    tunnelServerPort: int
    tunnelServerApiUrl: str
    tunnelServerToken: str
    def __init__(self, executionId: _Optional[str] = ..., localPort: _Optional[int] = ..., localBindHost: _Optional[str] = ..., tunnelServerHost: _Optional[str] = ..., tunnelServerPort: _Optional[int] = ..., tunnelServerApiUrl: _Optional[str] = ..., tunnelServerToken: _Optional[str] = ...) -> None: ...

class TunnelCreationResponse(_message.Message):
    __slots__ = ("executionId", "status", "tunnelHost", "tunnelPort", "tunnelId")
    EXECUTIONID_FIELD_NUMBER: _ClassVar[int]
    STATUS_FIELD_NUMBER: _ClassVar[int]
    TUNNELHOST_FIELD_NUMBER: _ClassVar[int]
    TUNNELPORT_FIELD_NUMBER: _ClassVar[int]
    TUNNELID_FIELD_NUMBER: _ClassVar[int]
    executionId: str
    status: str
    tunnelHost: str
    tunnelPort: int
    tunnelId: str
    def __init__(self, executionId: _Optional[str] = ..., status: _Optional[str] = ..., tunnelHost: _Optional[str] = ..., tunnelPort: _Optional[int] = ..., tunnelId: _Optional[str] = ...) -> None: ...

class TunnelTerminationRequest(_message.Message):
    __slots__ = ("executionId", "tunnelId")
    EXECUTIONID_FIELD_NUMBER: _ClassVar[int]
    TUNNELID_FIELD_NUMBER: _ClassVar[int]
    executionId: str
    tunnelId: str
    def __init__(self, executionId: _Optional[str] = ..., tunnelId: _Optional[str] = ...) -> None: ...

class TunnelTerminationResponse(_message.Message):
    __slots__ = ("executionId", "status")
    EXECUTIONID_FIELD_NUMBER: _ClassVar[int]
    STATUS_FIELD_NUMBER: _ClassVar[int]
    executionId: str
    status: str
    def __init__(self, executionId: _Optional[str] = ..., status: _Optional[str] = ...) -> None: ...

class AgentMessage(_message.Message):
    __slots__ = ("agentPing", "createAgentResponse", "terminateAgentResponse", "envSetupResponse", "commandExecutionResponse", "pythonExecutionResponse", "jupyterExecutionResponse", "kernelRestartResponse", "tunnelCreationResponse", "tunnelTerminationResponse", "asyncCommandExecutionResponse", "asyncCommandListResponse", "asyncCommandTerminateResponse")
    AGENTPING_FIELD_NUMBER: _ClassVar[int]
    CREATEAGENTRESPONSE_FIELD_NUMBER: _ClassVar[int]
    TERMINATEAGENTRESPONSE_FIELD_NUMBER: _ClassVar[int]
    ENVSETUPRESPONSE_FIELD_NUMBER: _ClassVar[int]
    COMMANDEXECUTIONRESPONSE_FIELD_NUMBER: _ClassVar[int]
    PYTHONEXECUTIONRESPONSE_FIELD_NUMBER: _ClassVar[int]
    JUPYTEREXECUTIONRESPONSE_FIELD_NUMBER: _ClassVar[int]
    KERNELRESTARTRESPONSE_FIELD_NUMBER: _ClassVar[int]
    TUNNELCREATIONRESPONSE_FIELD_NUMBER: _ClassVar[int]
    TUNNELTERMINATIONRESPONSE_FIELD_NUMBER: _ClassVar[int]
    ASYNCCOMMANDEXECUTIONRESPONSE_FIELD_NUMBER: _ClassVar[int]
    ASYNCCOMMANDLISTRESPONSE_FIELD_NUMBER: _ClassVar[int]
    ASYNCCOMMANDTERMINATERESPONSE_FIELD_NUMBER: _ClassVar[int]
    agentPing: AgentPing
    createAgentResponse: CreateAgentResponse
    terminateAgentResponse: TerminateAgentResponse
    envSetupResponse: EnvSetupResponse
    commandExecutionResponse: CommandExecutionResponse
    pythonExecutionResponse: PythonExecutionResponse
    jupyterExecutionResponse: JupyterExecutionResponse
    kernelRestartResponse: KernelRestartResponse
    tunnelCreationResponse: TunnelCreationResponse
    tunnelTerminationResponse: TunnelTerminationResponse
    asyncCommandExecutionResponse: AsyncCommandExecutionResponse
    asyncCommandListResponse: AsyncCommandListResponse
    asyncCommandTerminateResponse: AsyncCommandTerminateResponse
    def __init__(self, agentPing: _Optional[_Union[AgentPing, _Mapping]] = ..., createAgentResponse: _Optional[_Union[CreateAgentResponse, _Mapping]] = ..., terminateAgentResponse: _Optional[_Union[TerminateAgentResponse, _Mapping]] = ..., envSetupResponse: _Optional[_Union[EnvSetupResponse, _Mapping]] = ..., commandExecutionResponse: _Optional[_Union[CommandExecutionResponse, _Mapping]] = ..., pythonExecutionResponse: _Optional[_Union[PythonExecutionResponse, _Mapping]] = ..., jupyterExecutionResponse: _Optional[_Union[JupyterExecutionResponse, _Mapping]] = ..., kernelRestartResponse: _Optional[_Union[KernelRestartResponse, _Mapping]] = ..., tunnelCreationResponse: _Optional[_Union[TunnelCreationResponse, _Mapping]] = ..., tunnelTerminationResponse: _Optional[_Union[TunnelTerminationResponse, _Mapping]] = ..., asyncCommandExecutionResponse: _Optional[_Union[AsyncCommandExecutionResponse, _Mapping]] = ..., asyncCommandListResponse: _Optional[_Union[AsyncCommandListResponse, _Mapping]] = ..., asyncCommandTerminateResponse: _Optional[_Union[AsyncCommandTerminateResponse, _Mapping]] = ...) -> None: ...

class ServerMessage(_message.Message):
    __slots__ = ("shutdownRequest", "createAgentRequest", "terminateAgentRequest", "envSetupRequest", "commandExecutionRequest", "pythonExecutionRequest", "jupyterExecutionRequest", "kernelRestartRequest", "tunnelCreationRequest", "tunnelTerminationRequest", "asyncCommandExecutionRequest", "asyncCommandListRequest", "asyncCommandTerminateRequest")
    SHUTDOWNREQUEST_FIELD_NUMBER: _ClassVar[int]
    CREATEAGENTREQUEST_FIELD_NUMBER: _ClassVar[int]
    TERMINATEAGENTREQUEST_FIELD_NUMBER: _ClassVar[int]
    ENVSETUPREQUEST_FIELD_NUMBER: _ClassVar[int]
    COMMANDEXECUTIONREQUEST_FIELD_NUMBER: _ClassVar[int]
    PYTHONEXECUTIONREQUEST_FIELD_NUMBER: _ClassVar[int]
    JUPYTEREXECUTIONREQUEST_FIELD_NUMBER: _ClassVar[int]
    KERNELRESTARTREQUEST_FIELD_NUMBER: _ClassVar[int]
    TUNNELCREATIONREQUEST_FIELD_NUMBER: _ClassVar[int]
    TUNNELTERMINATIONREQUEST_FIELD_NUMBER: _ClassVar[int]
    ASYNCCOMMANDEXECUTIONREQUEST_FIELD_NUMBER: _ClassVar[int]
    ASYNCCOMMANDLISTREQUEST_FIELD_NUMBER: _ClassVar[int]
    ASYNCCOMMANDTERMINATEREQUEST_FIELD_NUMBER: _ClassVar[int]
    shutdownRequest: ShutdownRequest
    createAgentRequest: CreateAgentRequest
    terminateAgentRequest: TerminateAgentRequest
    envSetupRequest: EnvSetupRequest
    commandExecutionRequest: CommandExecutionRequest
    pythonExecutionRequest: PythonExecutionRequest
    jupyterExecutionRequest: JupyterExecutionRequest
    kernelRestartRequest: KernelRestartRequest
    tunnelCreationRequest: TunnelCreationRequest
    tunnelTerminationRequest: TunnelTerminationRequest
    asyncCommandExecutionRequest: AsyncCommandExecutionRequest
    asyncCommandListRequest: AsyncCommandListRequest
    asyncCommandTerminateRequest: AsyncCommandTerminateRequest
    def __init__(self, shutdownRequest: _Optional[_Union[ShutdownRequest, _Mapping]] = ..., createAgentRequest: _Optional[_Union[CreateAgentRequest, _Mapping]] = ..., terminateAgentRequest: _Optional[_Union[TerminateAgentRequest, _Mapping]] = ..., envSetupRequest: _Optional[_Union[EnvSetupRequest, _Mapping]] = ..., commandExecutionRequest: _Optional[_Union[CommandExecutionRequest, _Mapping]] = ..., pythonExecutionRequest: _Optional[_Union[PythonExecutionRequest, _Mapping]] = ..., jupyterExecutionRequest: _Optional[_Union[JupyterExecutionRequest, _Mapping]] = ..., kernelRestartRequest: _Optional[_Union[KernelRestartRequest, _Mapping]] = ..., tunnelCreationRequest: _Optional[_Union[TunnelCreationRequest, _Mapping]] = ..., tunnelTerminationRequest: _Optional[_Union[TunnelTerminationRequest, _Mapping]] = ..., asyncCommandExecutionRequest: _Optional[_Union[AsyncCommandExecutionRequest, _Mapping]] = ..., asyncCommandListRequest: _Optional[_Union[AsyncCommandListRequest, _Mapping]] = ..., asyncCommandTerminateRequest: _Optional[_Union[AsyncCommandTerminateRequest, _Mapping]] = ...) -> None: ...
