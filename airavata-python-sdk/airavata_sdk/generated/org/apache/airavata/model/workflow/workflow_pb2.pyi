from org.apache.airavata.model.commons import commons_pb2 as _commons_pb2
from org.apache.airavata.model.application.io import application_io_pb2 as _application_io_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class ApplicationState(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    APPLICATION_STATE_UNKNOWN: _ClassVar[ApplicationState]
    APPLICATION_STATE_CREATED: _ClassVar[ApplicationState]
    APPLICATION_STATE_VALIDATED: _ClassVar[ApplicationState]
    APPLICATION_STATE_SCHEDULED: _ClassVar[ApplicationState]
    APPLICATION_STATE_LAUNCHED: _ClassVar[ApplicationState]
    APPLICATION_STATE_EXECUTING: _ClassVar[ApplicationState]
    APPLICATION_STATE_CANCELING: _ClassVar[ApplicationState]
    APPLICATION_STATE_CANCELED: _ClassVar[ApplicationState]
    APPLICATION_STATE_COMPLETED: _ClassVar[ApplicationState]
    APPLICATION_STATE_FAILED: _ClassVar[ApplicationState]

class ComponentType(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    COMPONENT_TYPE_UNKNOWN: _ClassVar[ComponentType]
    APPLICATION: _ClassVar[ComponentType]
    HANDLER: _ClassVar[ComponentType]

class HandlerType(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    HANDLER_TYPE_UNKNOWN: _ClassVar[HandlerType]
    FLOW_STARTER: _ClassVar[HandlerType]
    FLOW_TERMINATOR: _ClassVar[HandlerType]

class HandlerState(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    HANDLER_STATE_UNKNOWN: _ClassVar[HandlerState]
    HANDLER_STATE_CREATED: _ClassVar[HandlerState]
    HANDLER_STATE_VALIDATED: _ClassVar[HandlerState]
    HANDLER_STATE_SCHEDULED: _ClassVar[HandlerState]
    HANDLER_STATE_LAUNCHED: _ClassVar[HandlerState]
    HANDLER_STATE_EXECUTING: _ClassVar[HandlerState]
    HANDLER_STATE_CANCELING: _ClassVar[HandlerState]
    HANDLER_STATE_CANCELED: _ClassVar[HandlerState]
    HANDLER_STATE_COMPLETED: _ClassVar[HandlerState]
    HANDLER_STATE_FAILED: _ClassVar[HandlerState]

class WorkflowState(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    WORKFLOW_STATE_UNKNOWN: _ClassVar[WorkflowState]
    WORKFLOW_STATE_CREATED: _ClassVar[WorkflowState]
    WORKFLOW_STATE_VALIDATED: _ClassVar[WorkflowState]
    WORKFLOW_STATE_SCHEDULED: _ClassVar[WorkflowState]
    WORKFLOW_STATE_LAUNCHED: _ClassVar[WorkflowState]
    WORKFLOW_STATE_EXECUTING: _ClassVar[WorkflowState]
    WORKFLOW_STATE_PAUSING: _ClassVar[WorkflowState]
    WORKFLOW_STATE_PAUSED: _ClassVar[WorkflowState]
    WORKFLOW_STATE_RESTARTING: _ClassVar[WorkflowState]
    WORKFLOW_STATE_CANCELING: _ClassVar[WorkflowState]
    WORKFLOW_STATE_CANCELED: _ClassVar[WorkflowState]
    WORKFLOW_STATE_COMPLETED: _ClassVar[WorkflowState]
    WORKFLOW_STATE_FAILED: _ClassVar[WorkflowState]
APPLICATION_STATE_UNKNOWN: ApplicationState
APPLICATION_STATE_CREATED: ApplicationState
APPLICATION_STATE_VALIDATED: ApplicationState
APPLICATION_STATE_SCHEDULED: ApplicationState
APPLICATION_STATE_LAUNCHED: ApplicationState
APPLICATION_STATE_EXECUTING: ApplicationState
APPLICATION_STATE_CANCELING: ApplicationState
APPLICATION_STATE_CANCELED: ApplicationState
APPLICATION_STATE_COMPLETED: ApplicationState
APPLICATION_STATE_FAILED: ApplicationState
COMPONENT_TYPE_UNKNOWN: ComponentType
APPLICATION: ComponentType
HANDLER: ComponentType
HANDLER_TYPE_UNKNOWN: HandlerType
FLOW_STARTER: HandlerType
FLOW_TERMINATOR: HandlerType
HANDLER_STATE_UNKNOWN: HandlerState
HANDLER_STATE_CREATED: HandlerState
HANDLER_STATE_VALIDATED: HandlerState
HANDLER_STATE_SCHEDULED: HandlerState
HANDLER_STATE_LAUNCHED: HandlerState
HANDLER_STATE_EXECUTING: HandlerState
HANDLER_STATE_CANCELING: HandlerState
HANDLER_STATE_CANCELED: HandlerState
HANDLER_STATE_COMPLETED: HandlerState
HANDLER_STATE_FAILED: HandlerState
WORKFLOW_STATE_UNKNOWN: WorkflowState
WORKFLOW_STATE_CREATED: WorkflowState
WORKFLOW_STATE_VALIDATED: WorkflowState
WORKFLOW_STATE_SCHEDULED: WorkflowState
WORKFLOW_STATE_LAUNCHED: WorkflowState
WORKFLOW_STATE_EXECUTING: WorkflowState
WORKFLOW_STATE_PAUSING: WorkflowState
WORKFLOW_STATE_PAUSED: WorkflowState
WORKFLOW_STATE_RESTARTING: WorkflowState
WORKFLOW_STATE_CANCELING: WorkflowState
WORKFLOW_STATE_CANCELED: WorkflowState
WORKFLOW_STATE_COMPLETED: WorkflowState
WORKFLOW_STATE_FAILED: WorkflowState

class ApplicationStatus(_message.Message):
    __slots__ = ("id", "state", "description", "updated_at")
    ID_FIELD_NUMBER: _ClassVar[int]
    STATE_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    UPDATED_AT_FIELD_NUMBER: _ClassVar[int]
    id: str
    state: ApplicationState
    description: str
    updated_at: int
    def __init__(self, id: _Optional[str] = ..., state: _Optional[_Union[ApplicationState, str]] = ..., description: _Optional[str] = ..., updated_at: _Optional[int] = ...) -> None: ...

class WorkflowApplication(_message.Message):
    __slots__ = ("id", "process_id", "application_interface_id", "compute_resource_id", "queue_name", "node_count", "core_count", "wall_time_limit", "physical_memory", "statuses", "errors", "created_at", "updated_at")
    ID_FIELD_NUMBER: _ClassVar[int]
    PROCESS_ID_FIELD_NUMBER: _ClassVar[int]
    APPLICATION_INTERFACE_ID_FIELD_NUMBER: _ClassVar[int]
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    QUEUE_NAME_FIELD_NUMBER: _ClassVar[int]
    NODE_COUNT_FIELD_NUMBER: _ClassVar[int]
    CORE_COUNT_FIELD_NUMBER: _ClassVar[int]
    WALL_TIME_LIMIT_FIELD_NUMBER: _ClassVar[int]
    PHYSICAL_MEMORY_FIELD_NUMBER: _ClassVar[int]
    STATUSES_FIELD_NUMBER: _ClassVar[int]
    ERRORS_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    UPDATED_AT_FIELD_NUMBER: _ClassVar[int]
    id: str
    process_id: str
    application_interface_id: str
    compute_resource_id: str
    queue_name: str
    node_count: int
    core_count: int
    wall_time_limit: int
    physical_memory: int
    statuses: _containers.RepeatedCompositeFieldContainer[ApplicationStatus]
    errors: _containers.RepeatedCompositeFieldContainer[_commons_pb2.ErrorModel]
    created_at: int
    updated_at: int
    def __init__(self, id: _Optional[str] = ..., process_id: _Optional[str] = ..., application_interface_id: _Optional[str] = ..., compute_resource_id: _Optional[str] = ..., queue_name: _Optional[str] = ..., node_count: _Optional[int] = ..., core_count: _Optional[int] = ..., wall_time_limit: _Optional[int] = ..., physical_memory: _Optional[int] = ..., statuses: _Optional[_Iterable[_Union[ApplicationStatus, _Mapping]]] = ..., errors: _Optional[_Iterable[_Union[_commons_pb2.ErrorModel, _Mapping]]] = ..., created_at: _Optional[int] = ..., updated_at: _Optional[int] = ...) -> None: ...

class DataBlock(_message.Message):
    __slots__ = ("id", "value", "type", "created_at", "updated_at")
    ID_FIELD_NUMBER: _ClassVar[int]
    VALUE_FIELD_NUMBER: _ClassVar[int]
    TYPE_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    UPDATED_AT_FIELD_NUMBER: _ClassVar[int]
    id: str
    value: str
    type: _application_io_pb2.DataType
    created_at: int
    updated_at: int
    def __init__(self, id: _Optional[str] = ..., value: _Optional[str] = ..., type: _Optional[_Union[_application_io_pb2.DataType, str]] = ..., created_at: _Optional[int] = ..., updated_at: _Optional[int] = ...) -> None: ...

class WorkflowConnection(_message.Message):
    __slots__ = ("id", "data_block", "from_type", "from_id", "from_output_name", "to_type", "to_id", "to_input_name", "created_at", "updated_at")
    ID_FIELD_NUMBER: _ClassVar[int]
    DATA_BLOCK_FIELD_NUMBER: _ClassVar[int]
    FROM_TYPE_FIELD_NUMBER: _ClassVar[int]
    FROM_ID_FIELD_NUMBER: _ClassVar[int]
    FROM_OUTPUT_NAME_FIELD_NUMBER: _ClassVar[int]
    TO_TYPE_FIELD_NUMBER: _ClassVar[int]
    TO_ID_FIELD_NUMBER: _ClassVar[int]
    TO_INPUT_NAME_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    UPDATED_AT_FIELD_NUMBER: _ClassVar[int]
    id: str
    data_block: DataBlock
    from_type: ComponentType
    from_id: str
    from_output_name: str
    to_type: ComponentType
    to_id: str
    to_input_name: str
    created_at: int
    updated_at: int
    def __init__(self, id: _Optional[str] = ..., data_block: _Optional[_Union[DataBlock, _Mapping]] = ..., from_type: _Optional[_Union[ComponentType, str]] = ..., from_id: _Optional[str] = ..., from_output_name: _Optional[str] = ..., to_type: _Optional[_Union[ComponentType, str]] = ..., to_id: _Optional[str] = ..., to_input_name: _Optional[str] = ..., created_at: _Optional[int] = ..., updated_at: _Optional[int] = ...) -> None: ...

class HandlerStatus(_message.Message):
    __slots__ = ("id", "state", "description", "updated_at")
    ID_FIELD_NUMBER: _ClassVar[int]
    STATE_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    UPDATED_AT_FIELD_NUMBER: _ClassVar[int]
    id: str
    state: HandlerState
    description: str
    updated_at: int
    def __init__(self, id: _Optional[str] = ..., state: _Optional[_Union[HandlerState, str]] = ..., description: _Optional[str] = ..., updated_at: _Optional[int] = ...) -> None: ...

class WorkflowHandler(_message.Message):
    __slots__ = ("id", "type", "inputs", "outputs", "statuses", "errors", "created_at", "updated_at")
    ID_FIELD_NUMBER: _ClassVar[int]
    TYPE_FIELD_NUMBER: _ClassVar[int]
    INPUTS_FIELD_NUMBER: _ClassVar[int]
    OUTPUTS_FIELD_NUMBER: _ClassVar[int]
    STATUSES_FIELD_NUMBER: _ClassVar[int]
    ERRORS_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    UPDATED_AT_FIELD_NUMBER: _ClassVar[int]
    id: str
    type: HandlerType
    inputs: _containers.RepeatedCompositeFieldContainer[_application_io_pb2.InputDataObjectType]
    outputs: _containers.RepeatedCompositeFieldContainer[_application_io_pb2.OutputDataObjectType]
    statuses: _containers.RepeatedCompositeFieldContainer[HandlerStatus]
    errors: _containers.RepeatedCompositeFieldContainer[_commons_pb2.ErrorModel]
    created_at: int
    updated_at: int
    def __init__(self, id: _Optional[str] = ..., type: _Optional[_Union[HandlerType, str]] = ..., inputs: _Optional[_Iterable[_Union[_application_io_pb2.InputDataObjectType, _Mapping]]] = ..., outputs: _Optional[_Iterable[_Union[_application_io_pb2.OutputDataObjectType, _Mapping]]] = ..., statuses: _Optional[_Iterable[_Union[HandlerStatus, _Mapping]]] = ..., errors: _Optional[_Iterable[_Union[_commons_pb2.ErrorModel, _Mapping]]] = ..., created_at: _Optional[int] = ..., updated_at: _Optional[int] = ...) -> None: ...

class WorkflowStatus(_message.Message):
    __slots__ = ("id", "state", "description", "updated_at")
    ID_FIELD_NUMBER: _ClassVar[int]
    STATE_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    UPDATED_AT_FIELD_NUMBER: _ClassVar[int]
    id: str
    state: WorkflowState
    description: str
    updated_at: int
    def __init__(self, id: _Optional[str] = ..., state: _Optional[_Union[WorkflowState, str]] = ..., description: _Optional[str] = ..., updated_at: _Optional[int] = ...) -> None: ...

class AiravataWorkflow(_message.Message):
    __slots__ = ("id", "experiment_id", "description", "applications", "handlers", "connections", "statuses", "errors", "created_at", "updated_at")
    ID_FIELD_NUMBER: _ClassVar[int]
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    APPLICATIONS_FIELD_NUMBER: _ClassVar[int]
    HANDLERS_FIELD_NUMBER: _ClassVar[int]
    CONNECTIONS_FIELD_NUMBER: _ClassVar[int]
    STATUSES_FIELD_NUMBER: _ClassVar[int]
    ERRORS_FIELD_NUMBER: _ClassVar[int]
    CREATED_AT_FIELD_NUMBER: _ClassVar[int]
    UPDATED_AT_FIELD_NUMBER: _ClassVar[int]
    id: str
    experiment_id: str
    description: str
    applications: _containers.RepeatedCompositeFieldContainer[WorkflowApplication]
    handlers: _containers.RepeatedCompositeFieldContainer[WorkflowHandler]
    connections: _containers.RepeatedCompositeFieldContainer[WorkflowConnection]
    statuses: _containers.RepeatedCompositeFieldContainer[WorkflowStatus]
    errors: _containers.RepeatedCompositeFieldContainer[_commons_pb2.ErrorModel]
    created_at: int
    updated_at: int
    def __init__(self, id: _Optional[str] = ..., experiment_id: _Optional[str] = ..., description: _Optional[str] = ..., applications: _Optional[_Iterable[_Union[WorkflowApplication, _Mapping]]] = ..., handlers: _Optional[_Iterable[_Union[WorkflowHandler, _Mapping]]] = ..., connections: _Optional[_Iterable[_Union[WorkflowConnection, _Mapping]]] = ..., statuses: _Optional[_Iterable[_Union[WorkflowStatus, _Mapping]]] = ..., errors: _Optional[_Iterable[_Union[_commons_pb2.ErrorModel, _Mapping]]] = ..., created_at: _Optional[int] = ..., updated_at: _Optional[int] = ...) -> None: ...
