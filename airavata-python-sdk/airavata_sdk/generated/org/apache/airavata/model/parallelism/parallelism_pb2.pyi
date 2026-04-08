from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from typing import ClassVar as _ClassVar

DESCRIPTOR: _descriptor.FileDescriptor

class ApplicationParallelismType(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    APPLICATION_PARALLELISM_TYPE_UNKNOWN: _ClassVar[ApplicationParallelismType]
    SERIAL: _ClassVar[ApplicationParallelismType]
    MPI: _ClassVar[ApplicationParallelismType]
    OPENMP: _ClassVar[ApplicationParallelismType]
    OPENMP_MPI: _ClassVar[ApplicationParallelismType]
    CCM: _ClassVar[ApplicationParallelismType]
    CRAY_MPI: _ClassVar[ApplicationParallelismType]
APPLICATION_PARALLELISM_TYPE_UNKNOWN: ApplicationParallelismType
SERIAL: ApplicationParallelismType
MPI: ApplicationParallelismType
OPENMP: ApplicationParallelismType
OPENMP_MPI: ApplicationParallelismType
CCM: ApplicationParallelismType
CRAY_MPI: ApplicationParallelismType
