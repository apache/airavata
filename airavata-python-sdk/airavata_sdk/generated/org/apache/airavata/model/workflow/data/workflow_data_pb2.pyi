from org.apache.airavata.model.application.io import application_io_pb2 as _application_io_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class WorkflowState(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    WORKFLOW_STATE_UNKNOWN: _ClassVar[WorkflowState]
    CREATED: _ClassVar[WorkflowState]
    STARTED: _ClassVar[WorkflowState]
    EXECUTING: _ClassVar[WorkflowState]
    COMPLETED: _ClassVar[WorkflowState]
    FAILED: _ClassVar[WorkflowState]
    CANCELLING: _ClassVar[WorkflowState]
    CANCELED: _ClassVar[WorkflowState]

class ComponentState(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    COMPONENT_STATE_UNKNOWN: _ClassVar[ComponentState]
    COMPONENT_CREATED: _ClassVar[ComponentState]
    WAITING: _ClassVar[ComponentState]
    READY: _ClassVar[ComponentState]
    RUNNING: _ClassVar[ComponentState]
    COMPONENT_COMPLETED: _ClassVar[ComponentState]
    COMPONENT_FAILED: _ClassVar[ComponentState]
    COMPONENT_CANCELED: _ClassVar[ComponentState]
WORKFLOW_STATE_UNKNOWN: WorkflowState
CREATED: WorkflowState
STARTED: WorkflowState
EXECUTING: WorkflowState
COMPLETED: WorkflowState
FAILED: WorkflowState
CANCELLING: WorkflowState
CANCELED: WorkflowState
COMPONENT_STATE_UNKNOWN: ComponentState
COMPONENT_CREATED: ComponentState
WAITING: ComponentState
READY: ComponentState
RUNNING: ComponentState
COMPONENT_COMPLETED: ComponentState
COMPONENT_FAILED: ComponentState
COMPONENT_CANCELED: ComponentState

class ComponentStatus(_message.Message):
    __slots__ = ("state", "reason", "timeof_state_change")
    STATE_FIELD_NUMBER: _ClassVar[int]
    REASON_FIELD_NUMBER: _ClassVar[int]
    TIMEOF_STATE_CHANGE_FIELD_NUMBER: _ClassVar[int]
    state: ComponentState
    reason: str
    timeof_state_change: int
    def __init__(self, state: _Optional[_Union[ComponentState, str]] = ..., reason: _Optional[str] = ..., timeof_state_change: _Optional[int] = ...) -> None: ...

class WorkflowStatus(_message.Message):
    __slots__ = ("state", "time_of_state_change", "reason")
    STATE_FIELD_NUMBER: _ClassVar[int]
    TIME_OF_STATE_CHANGE_FIELD_NUMBER: _ClassVar[int]
    REASON_FIELD_NUMBER: _ClassVar[int]
    state: WorkflowState
    time_of_state_change: int
    reason: str
    def __init__(self, state: _Optional[_Union[WorkflowState, str]] = ..., time_of_state_change: _Optional[int] = ..., reason: _Optional[str] = ...) -> None: ...

class WorkflowModel(_message.Message):
    __slots__ = ("template_id", "name", "graph", "gateway_id", "created_user", "image", "workflow_inputs", "workflow_outputs", "creation_time")
    TEMPLATE_ID_FIELD_NUMBER: _ClassVar[int]
    NAME_FIELD_NUMBER: _ClassVar[int]
    GRAPH_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    CREATED_USER_FIELD_NUMBER: _ClassVar[int]
    IMAGE_FIELD_NUMBER: _ClassVar[int]
    WORKFLOW_INPUTS_FIELD_NUMBER: _ClassVar[int]
    WORKFLOW_OUTPUTS_FIELD_NUMBER: _ClassVar[int]
    CREATION_TIME_FIELD_NUMBER: _ClassVar[int]
    template_id: str
    name: str
    graph: str
    gateway_id: str
    created_user: str
    image: bytes
    workflow_inputs: _containers.RepeatedCompositeFieldContainer[_application_io_pb2.InputDataObjectType]
    workflow_outputs: _containers.RepeatedCompositeFieldContainer[_application_io_pb2.OutputDataObjectType]
    creation_time: int
    def __init__(self, template_id: _Optional[str] = ..., name: _Optional[str] = ..., graph: _Optional[str] = ..., gateway_id: _Optional[str] = ..., created_user: _Optional[str] = ..., image: _Optional[bytes] = ..., workflow_inputs: _Optional[_Iterable[_Union[_application_io_pb2.InputDataObjectType, _Mapping]]] = ..., workflow_outputs: _Optional[_Iterable[_Union[_application_io_pb2.OutputDataObjectType, _Mapping]]] = ..., creation_time: _Optional[int] = ...) -> None: ...

class EdgeModel(_message.Message):
    __slots__ = ("edge_id", "name", "status", "description")
    EDGE_ID_FIELD_NUMBER: _ClassVar[int]
    NAME_FIELD_NUMBER: _ClassVar[int]
    STATUS_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    edge_id: str
    name: str
    status: ComponentStatus
    description: str
    def __init__(self, edge_id: _Optional[str] = ..., name: _Optional[str] = ..., status: _Optional[_Union[ComponentStatus, _Mapping]] = ..., description: _Optional[str] = ...) -> None: ...

class PortModel(_message.Message):
    __slots__ = ("port_id", "name", "status", "value", "description")
    PORT_ID_FIELD_NUMBER: _ClassVar[int]
    NAME_FIELD_NUMBER: _ClassVar[int]
    STATUS_FIELD_NUMBER: _ClassVar[int]
    VALUE_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    port_id: str
    name: str
    status: ComponentStatus
    value: str
    description: str
    def __init__(self, port_id: _Optional[str] = ..., name: _Optional[str] = ..., status: _Optional[_Union[ComponentStatus, _Mapping]] = ..., value: _Optional[str] = ..., description: _Optional[str] = ...) -> None: ...

class NodeModel(_message.Message):
    __slots__ = ("node_id", "name", "application_id", "application_name", "status", "description")
    NODE_ID_FIELD_NUMBER: _ClassVar[int]
    NAME_FIELD_NUMBER: _ClassVar[int]
    APPLICATION_ID_FIELD_NUMBER: _ClassVar[int]
    APPLICATION_NAME_FIELD_NUMBER: _ClassVar[int]
    STATUS_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    node_id: str
    name: str
    application_id: str
    application_name: str
    status: ComponentStatus
    description: str
    def __init__(self, node_id: _Optional[str] = ..., name: _Optional[str] = ..., application_id: _Optional[str] = ..., application_name: _Optional[str] = ..., status: _Optional[_Union[ComponentStatus, _Mapping]] = ..., description: _Optional[str] = ...) -> None: ...
