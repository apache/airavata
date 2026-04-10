from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Optional as _Optional

DESCRIPTOR: _descriptor.FileDescriptor

class ComputationalResourceSchedulingModel(_message.Message):
    __slots__ = ("resource_host_id", "total_cpu_count", "node_count", "number_of_threads", "queue_name", "wall_time_limit", "total_physical_memory", "chessis_number", "static_working_dir", "override_login_user_name", "override_scratch_location", "override_allocation_project_number", "m_group_count")
    RESOURCE_HOST_ID_FIELD_NUMBER: _ClassVar[int]
    TOTAL_CPU_COUNT_FIELD_NUMBER: _ClassVar[int]
    NODE_COUNT_FIELD_NUMBER: _ClassVar[int]
    NUMBER_OF_THREADS_FIELD_NUMBER: _ClassVar[int]
    QUEUE_NAME_FIELD_NUMBER: _ClassVar[int]
    WALL_TIME_LIMIT_FIELD_NUMBER: _ClassVar[int]
    TOTAL_PHYSICAL_MEMORY_FIELD_NUMBER: _ClassVar[int]
    CHESSIS_NUMBER_FIELD_NUMBER: _ClassVar[int]
    STATIC_WORKING_DIR_FIELD_NUMBER: _ClassVar[int]
    OVERRIDE_LOGIN_USER_NAME_FIELD_NUMBER: _ClassVar[int]
    OVERRIDE_SCRATCH_LOCATION_FIELD_NUMBER: _ClassVar[int]
    OVERRIDE_ALLOCATION_PROJECT_NUMBER_FIELD_NUMBER: _ClassVar[int]
    M_GROUP_COUNT_FIELD_NUMBER: _ClassVar[int]
    resource_host_id: str
    total_cpu_count: int
    node_count: int
    number_of_threads: int
    queue_name: str
    wall_time_limit: int
    total_physical_memory: int
    chessis_number: str
    static_working_dir: str
    override_login_user_name: str
    override_scratch_location: str
    override_allocation_project_number: str
    m_group_count: int
    def __init__(self, resource_host_id: _Optional[str] = ..., total_cpu_count: _Optional[int] = ..., node_count: _Optional[int] = ..., number_of_threads: _Optional[int] = ..., queue_name: _Optional[str] = ..., wall_time_limit: _Optional[int] = ..., total_physical_memory: _Optional[int] = ..., chessis_number: _Optional[str] = ..., static_working_dir: _Optional[str] = ..., override_login_user_name: _Optional[str] = ..., override_scratch_location: _Optional[str] = ..., override_allocation_project_number: _Optional[str] = ..., m_group_count: _Optional[int] = ...) -> None: ...
