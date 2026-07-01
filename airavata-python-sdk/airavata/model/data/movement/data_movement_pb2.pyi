from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from typing import ClassVar as _ClassVar

DESCRIPTOR: _descriptor.FileDescriptor

class DMType(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    DM_TYPE_UNKNOWN: _ClassVar[DMType]
    COMPUTE_RESOURCE: _ClassVar[DMType]
    STORAGE_RESOURCE: _ClassVar[DMType]
DM_TYPE_UNKNOWN: DMType
COMPUTE_RESOURCE: DMType
STORAGE_RESOURCE: DMType
