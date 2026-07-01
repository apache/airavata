from google.api import annotations_pb2 as _annotations_pb2
from google.protobuf import empty_pb2 as _empty_pb2
from airavata.services import experiment_service_pb2 as _experiment_service_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class AggregateState(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    QUEUED: _ClassVar[AggregateState]
    RUNNING: _ClassVar[AggregateState]
    COMPLETED: _ClassVar[AggregateState]
    FAILED: _ClassVar[AggregateState]
    MIXED: _ClassVar[AggregateState]
QUEUED: AggregateState
RUNNING: AggregateState
COMPLETED: AggregateState
FAILED: AggregateState
MIXED: AggregateState

class ValueList(_message.Message):
    __slots__ = ("values",)
    VALUES_FIELD_NUMBER: _ClassVar[int]
    values: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, values: _Optional[_Iterable[str]] = ...) -> None: ...

class SweepSpec(_message.Message):
    __slots__ = ("base", "sweep_axes", "name_prefix")
    class SweepAxesEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: ValueList
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[ValueList, _Mapping]] = ...) -> None: ...
    BASE_FIELD_NUMBER: _ClassVar[int]
    SWEEP_AXES_FIELD_NUMBER: _ClassVar[int]
    NAME_PREFIX_FIELD_NUMBER: _ClassVar[int]
    base: _experiment_service_pb2.ExperimentSpec
    sweep_axes: _containers.MessageMap[str, ValueList]
    name_prefix: str
    def __init__(self, base: _Optional[_Union[_experiment_service_pb2.ExperimentSpec, _Mapping]] = ..., sweep_axes: _Optional[_Mapping[str, ValueList]] = ..., name_prefix: _Optional[str] = ...) -> None: ...

class ExperimentIdList(_message.Message):
    __slots__ = ("experiment_ids",)
    EXPERIMENT_IDS_FIELD_NUMBER: _ClassVar[int]
    experiment_ids: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, experiment_ids: _Optional[_Iterable[str]] = ...) -> None: ...

class CreateExperimentSetRequest(_message.Message):
    __slots__ = ("set_name", "sweep", "existing")
    SET_NAME_FIELD_NUMBER: _ClassVar[int]
    SWEEP_FIELD_NUMBER: _ClassVar[int]
    EXISTING_FIELD_NUMBER: _ClassVar[int]
    set_name: str
    sweep: SweepSpec
    existing: ExperimentIdList
    def __init__(self, set_name: _Optional[str] = ..., sweep: _Optional[_Union[SweepSpec, _Mapping]] = ..., existing: _Optional[_Union[ExperimentIdList, _Mapping]] = ...) -> None: ...

class LaunchExperimentSetRequest(_message.Message):
    __slots__ = ("experiment_set_id", "notification_email")
    EXPERIMENT_SET_ID_FIELD_NUMBER: _ClassVar[int]
    NOTIFICATION_EMAIL_FIELD_NUMBER: _ClassVar[int]
    experiment_set_id: str
    notification_email: str
    def __init__(self, experiment_set_id: _Optional[str] = ..., notification_email: _Optional[str] = ...) -> None: ...

class GetExperimentSetRequest(_message.Message):
    __slots__ = ("experiment_set_id",)
    EXPERIMENT_SET_ID_FIELD_NUMBER: _ClassVar[int]
    experiment_set_id: str
    def __init__(self, experiment_set_id: _Optional[str] = ...) -> None: ...

class DeleteExperimentSetRequest(_message.Message):
    __slots__ = ("experiment_set_id",)
    EXPERIMENT_SET_ID_FIELD_NUMBER: _ClassVar[int]
    experiment_set_id: str
    def __init__(self, experiment_set_id: _Optional[str] = ...) -> None: ...

class ListExperimentSetsRequest(_message.Message):
    __slots__ = ("limit", "offset")
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    OFFSET_FIELD_NUMBER: _ClassVar[int]
    limit: int
    offset: int
    def __init__(self, limit: _Optional[int] = ..., offset: _Optional[int] = ...) -> None: ...

class ExperimentSet(_message.Message):
    __slots__ = ("experiment_set_id", "set_name", "owner", "gateway_id", "experiment_ids", "sweep", "creation_time", "updated_time")
    EXPERIMENT_SET_ID_FIELD_NUMBER: _ClassVar[int]
    SET_NAME_FIELD_NUMBER: _ClassVar[int]
    OWNER_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    EXPERIMENT_IDS_FIELD_NUMBER: _ClassVar[int]
    SWEEP_FIELD_NUMBER: _ClassVar[int]
    CREATION_TIME_FIELD_NUMBER: _ClassVar[int]
    UPDATED_TIME_FIELD_NUMBER: _ClassVar[int]
    experiment_set_id: str
    set_name: str
    owner: str
    gateway_id: str
    experiment_ids: _containers.RepeatedScalarFieldContainer[str]
    sweep: SweepSpec
    creation_time: int
    updated_time: int
    def __init__(self, experiment_set_id: _Optional[str] = ..., set_name: _Optional[str] = ..., owner: _Optional[str] = ..., gateway_id: _Optional[str] = ..., experiment_ids: _Optional[_Iterable[str]] = ..., sweep: _Optional[_Union[SweepSpec, _Mapping]] = ..., creation_time: _Optional[int] = ..., updated_time: _Optional[int] = ...) -> None: ...

class ListExperimentSetsResponse(_message.Message):
    __slots__ = ("experiment_sets",)
    EXPERIMENT_SETS_FIELD_NUMBER: _ClassVar[int]
    experiment_sets: _containers.RepeatedCompositeFieldContainer[ExperimentSet]
    def __init__(self, experiment_sets: _Optional[_Iterable[_Union[ExperimentSet, _Mapping]]] = ...) -> None: ...

class ExperimentStatusItem(_message.Message):
    __slots__ = ("experiment_id", "state", "process_id")
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    STATE_FIELD_NUMBER: _ClassVar[int]
    PROCESS_ID_FIELD_NUMBER: _ClassVar[int]
    experiment_id: str
    state: str
    process_id: str
    def __init__(self, experiment_id: _Optional[str] = ..., state: _Optional[str] = ..., process_id: _Optional[str] = ...) -> None: ...

class ExperimentSetStatus(_message.Message):
    __slots__ = ("experiment_set_id", "total", "counts_by_state", "aggregate", "items")
    class CountsByStateEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: int
        def __init__(self, key: _Optional[str] = ..., value: _Optional[int] = ...) -> None: ...
    EXPERIMENT_SET_ID_FIELD_NUMBER: _ClassVar[int]
    TOTAL_FIELD_NUMBER: _ClassVar[int]
    COUNTS_BY_STATE_FIELD_NUMBER: _ClassVar[int]
    AGGREGATE_FIELD_NUMBER: _ClassVar[int]
    ITEMS_FIELD_NUMBER: _ClassVar[int]
    experiment_set_id: str
    total: int
    counts_by_state: _containers.ScalarMap[str, int]
    aggregate: AggregateState
    items: _containers.RepeatedCompositeFieldContainer[ExperimentStatusItem]
    def __init__(self, experiment_set_id: _Optional[str] = ..., total: _Optional[int] = ..., counts_by_state: _Optional[_Mapping[str, int]] = ..., aggregate: _Optional[_Union[AggregateState, str]] = ..., items: _Optional[_Iterable[_Union[ExperimentStatusItem, _Mapping]]] = ...) -> None: ...
