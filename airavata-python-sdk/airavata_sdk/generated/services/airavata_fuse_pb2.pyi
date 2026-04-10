import datetime

from google.protobuf import timestamp_pb2 as _timestamp_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class RPCContext(_message.Message):
    __slots__ = ("GatewayId", "AccessToken", "AgentId")
    GATEWAYID_FIELD_NUMBER: _ClassVar[int]
    ACCESSTOKEN_FIELD_NUMBER: _ClassVar[int]
    AGENTID_FIELD_NUMBER: _ClassVar[int]
    GatewayId: str
    AccessToken: str
    AgentId: str
    def __init__(self, GatewayId: _Optional[str] = ..., AccessToken: _Optional[str] = ..., AgentId: _Optional[str] = ...) -> None: ...

class OpContext(_message.Message):
    __slots__ = ("FuseId", "Pid", "Uid")
    FUSEID_FIELD_NUMBER: _ClassVar[int]
    PID_FIELD_NUMBER: _ClassVar[int]
    UID_FIELD_NUMBER: _ClassVar[int]
    FuseId: int
    Pid: int
    Uid: int
    def __init__(self, FuseId: _Optional[int] = ..., Pid: _Optional[int] = ..., Uid: _Optional[int] = ...) -> None: ...

class StatFs(_message.Message):
    __slots__ = ("BlockSize", "Blocks", "BlocksFree", "BlocksAvailable", "IoSize", "Inodes", "InodesFree")
    BLOCKSIZE_FIELD_NUMBER: _ClassVar[int]
    BLOCKS_FIELD_NUMBER: _ClassVar[int]
    BLOCKSFREE_FIELD_NUMBER: _ClassVar[int]
    BLOCKSAVAILABLE_FIELD_NUMBER: _ClassVar[int]
    IOSIZE_FIELD_NUMBER: _ClassVar[int]
    INODES_FIELD_NUMBER: _ClassVar[int]
    INODESFREE_FIELD_NUMBER: _ClassVar[int]
    BlockSize: int
    Blocks: int
    BlocksFree: int
    BlocksAvailable: int
    IoSize: int
    Inodes: int
    InodesFree: int
    def __init__(self, BlockSize: _Optional[int] = ..., Blocks: _Optional[int] = ..., BlocksFree: _Optional[int] = ..., BlocksAvailable: _Optional[int] = ..., IoSize: _Optional[int] = ..., Inodes: _Optional[int] = ..., InodesFree: _Optional[int] = ...) -> None: ...

class FileInfo(_message.Message):
    __slots__ = ("Name", "Size", "Mode", "ModTime", "IsDir", "Ino")
    NAME_FIELD_NUMBER: _ClassVar[int]
    SIZE_FIELD_NUMBER: _ClassVar[int]
    MODE_FIELD_NUMBER: _ClassVar[int]
    MODTIME_FIELD_NUMBER: _ClassVar[int]
    ISDIR_FIELD_NUMBER: _ClassVar[int]
    INO_FIELD_NUMBER: _ClassVar[int]
    Name: str
    Size: int
    Mode: int
    ModTime: _timestamp_pb2.Timestamp
    IsDir: bool
    Ino: int
    def __init__(self, Name: _Optional[str] = ..., Size: _Optional[int] = ..., Mode: _Optional[int] = ..., ModTime: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., IsDir: bool = ..., Ino: _Optional[int] = ...) -> None: ...

class OpenedDir(_message.Message):
    __slots__ = ("Inode", "Handle", "OpContext", "CacheDir", "KeepCache")
    INODE_FIELD_NUMBER: _ClassVar[int]
    HANDLE_FIELD_NUMBER: _ClassVar[int]
    OPCONTEXT_FIELD_NUMBER: _ClassVar[int]
    CACHEDIR_FIELD_NUMBER: _ClassVar[int]
    KEEPCACHE_FIELD_NUMBER: _ClassVar[int]
    Inode: int
    Handle: int
    OpContext: OpContext
    CacheDir: bool
    KeepCache: bool
    def __init__(self, Inode: _Optional[int] = ..., Handle: _Optional[int] = ..., OpContext: _Optional[_Union[OpContext, _Mapping]] = ..., CacheDir: bool = ..., KeepCache: bool = ...) -> None: ...

class OpenedFile(_message.Message):
    __slots__ = ("Inode", "Handle", "KeepPageCache", "UseDirectIO", "OpenFlags", "OpContext")
    INODE_FIELD_NUMBER: _ClassVar[int]
    HANDLE_FIELD_NUMBER: _ClassVar[int]
    KEEPPAGECACHE_FIELD_NUMBER: _ClassVar[int]
    USEDIRECTIO_FIELD_NUMBER: _ClassVar[int]
    OPENFLAGS_FIELD_NUMBER: _ClassVar[int]
    OPCONTEXT_FIELD_NUMBER: _ClassVar[int]
    Inode: int
    Handle: int
    KeepPageCache: bool
    UseDirectIO: bool
    OpenFlags: int
    OpContext: OpContext
    def __init__(self, Inode: _Optional[int] = ..., Handle: _Optional[int] = ..., KeepPageCache: bool = ..., UseDirectIO: bool = ..., OpenFlags: _Optional[int] = ..., OpContext: _Optional[_Union[OpContext, _Mapping]] = ...) -> None: ...

class DirEntry(_message.Message):
    __slots__ = ("Name", "IsDir", "FileMode", "Info")
    NAME_FIELD_NUMBER: _ClassVar[int]
    ISDIR_FIELD_NUMBER: _ClassVar[int]
    FILEMODE_FIELD_NUMBER: _ClassVar[int]
    INFO_FIELD_NUMBER: _ClassVar[int]
    Name: str
    IsDir: bool
    FileMode: int
    Info: FileInfo
    def __init__(self, Name: _Optional[str] = ..., IsDir: bool = ..., FileMode: _Optional[int] = ..., Info: _Optional[_Union[FileInfo, _Mapping]] = ...) -> None: ...

class FileEntry(_message.Message):
    __slots__ = ("Inode", "Handle", "Offset", "Size", "Dst", "Data", "BytesRead", "OpContext")
    INODE_FIELD_NUMBER: _ClassVar[int]
    HANDLE_FIELD_NUMBER: _ClassVar[int]
    OFFSET_FIELD_NUMBER: _ClassVar[int]
    SIZE_FIELD_NUMBER: _ClassVar[int]
    DST_FIELD_NUMBER: _ClassVar[int]
    DATA_FIELD_NUMBER: _ClassVar[int]
    BYTESREAD_FIELD_NUMBER: _ClassVar[int]
    OPCONTEXT_FIELD_NUMBER: _ClassVar[int]
    Inode: int
    Handle: int
    Offset: int
    Size: int
    Dst: bytes
    Data: _containers.RepeatedScalarFieldContainer[bytes]
    BytesRead: int
    OpContext: OpContext
    def __init__(self, Inode: _Optional[int] = ..., Handle: _Optional[int] = ..., Offset: _Optional[int] = ..., Size: _Optional[int] = ..., Dst: _Optional[bytes] = ..., Data: _Optional[_Iterable[bytes]] = ..., BytesRead: _Optional[int] = ..., OpContext: _Optional[_Union[OpContext, _Mapping]] = ...) -> None: ...

class InodeAtt(_message.Message):
    __slots__ = ("Size", "Nlink", "FileMode", "Atime", "Mtime", "Ctime")
    SIZE_FIELD_NUMBER: _ClassVar[int]
    NLINK_FIELD_NUMBER: _ClassVar[int]
    FILEMODE_FIELD_NUMBER: _ClassVar[int]
    ATIME_FIELD_NUMBER: _ClassVar[int]
    MTIME_FIELD_NUMBER: _ClassVar[int]
    CTIME_FIELD_NUMBER: _ClassVar[int]
    Size: int
    Nlink: int
    FileMode: int
    Atime: _timestamp_pb2.Timestamp
    Mtime: _timestamp_pb2.Timestamp
    Ctime: _timestamp_pb2.Timestamp
    def __init__(self, Size: _Optional[int] = ..., Nlink: _Optional[int] = ..., FileMode: _Optional[int] = ..., Atime: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., Mtime: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., Ctime: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ...) -> None: ...

class StatFsReq(_message.Message):
    __slots__ = ("Name", "Context")
    NAME_FIELD_NUMBER: _ClassVar[int]
    CONTEXT_FIELD_NUMBER: _ClassVar[int]
    Name: str
    Context: RPCContext
    def __init__(self, Name: _Optional[str] = ..., Context: _Optional[_Union[RPCContext, _Mapping]] = ...) -> None: ...

class FileInfoReq(_message.Message):
    __slots__ = ("Name", "Context")
    NAME_FIELD_NUMBER: _ClassVar[int]
    CONTEXT_FIELD_NUMBER: _ClassVar[int]
    Name: str
    Context: RPCContext
    def __init__(self, Name: _Optional[str] = ..., Context: _Optional[_Union[RPCContext, _Mapping]] = ...) -> None: ...

class OpenDirReq(_message.Message):
    __slots__ = ("Name", "Context")
    NAME_FIELD_NUMBER: _ClassVar[int]
    CONTEXT_FIELD_NUMBER: _ClassVar[int]
    Name: str
    Context: RPCContext
    def __init__(self, Name: _Optional[str] = ..., Context: _Optional[_Union[RPCContext, _Mapping]] = ...) -> None: ...

class OpenFileReq(_message.Message):
    __slots__ = ("Name", "Context")
    NAME_FIELD_NUMBER: _ClassVar[int]
    CONTEXT_FIELD_NUMBER: _ClassVar[int]
    Name: str
    Context: RPCContext
    def __init__(self, Name: _Optional[str] = ..., Context: _Optional[_Union[RPCContext, _Mapping]] = ...) -> None: ...

class ReadDirReq(_message.Message):
    __slots__ = ("Name", "Context")
    NAME_FIELD_NUMBER: _ClassVar[int]
    CONTEXT_FIELD_NUMBER: _ClassVar[int]
    Name: str
    Context: RPCContext
    def __init__(self, Name: _Optional[str] = ..., Context: _Optional[_Union[RPCContext, _Mapping]] = ...) -> None: ...

class ReadFileReq(_message.Message):
    __slots__ = ("Name", "Context")
    NAME_FIELD_NUMBER: _ClassVar[int]
    CONTEXT_FIELD_NUMBER: _ClassVar[int]
    Name: str
    Context: RPCContext
    def __init__(self, Name: _Optional[str] = ..., Context: _Optional[_Union[RPCContext, _Mapping]] = ...) -> None: ...

class WriteFileReq(_message.Message):
    __slots__ = ("Name", "Context", "Data", "Offset")
    NAME_FIELD_NUMBER: _ClassVar[int]
    CONTEXT_FIELD_NUMBER: _ClassVar[int]
    DATA_FIELD_NUMBER: _ClassVar[int]
    OFFSET_FIELD_NUMBER: _ClassVar[int]
    Name: str
    Context: RPCContext
    Data: bytes
    Offset: int
    def __init__(self, Name: _Optional[str] = ..., Context: _Optional[_Union[RPCContext, _Mapping]] = ..., Data: _Optional[bytes] = ..., Offset: _Optional[int] = ...) -> None: ...

class SetInodeAttReq(_message.Message):
    __slots__ = ("Name", "Context", "Size", "FileMode", "ATime", "MTime")
    NAME_FIELD_NUMBER: _ClassVar[int]
    CONTEXT_FIELD_NUMBER: _ClassVar[int]
    SIZE_FIELD_NUMBER: _ClassVar[int]
    FILEMODE_FIELD_NUMBER: _ClassVar[int]
    ATIME_FIELD_NUMBER: _ClassVar[int]
    MTIME_FIELD_NUMBER: _ClassVar[int]
    Name: str
    Context: RPCContext
    Size: int
    FileMode: int
    ATime: _timestamp_pb2.Timestamp
    MTime: _timestamp_pb2.Timestamp
    def __init__(self, Name: _Optional[str] = ..., Context: _Optional[_Union[RPCContext, _Mapping]] = ..., Size: _Optional[int] = ..., FileMode: _Optional[int] = ..., ATime: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ..., MTime: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ...) -> None: ...

class StatFsRes(_message.Message):
    __slots__ = ("Result",)
    RESULT_FIELD_NUMBER: _ClassVar[int]
    Result: StatFs
    def __init__(self, Result: _Optional[_Union[StatFs, _Mapping]] = ...) -> None: ...

class FileInfoRes(_message.Message):
    __slots__ = ("Result",)
    RESULT_FIELD_NUMBER: _ClassVar[int]
    Result: FileInfo
    def __init__(self, Result: _Optional[_Union[FileInfo, _Mapping]] = ...) -> None: ...

class OpenDirRes(_message.Message):
    __slots__ = ("Result",)
    RESULT_FIELD_NUMBER: _ClassVar[int]
    Result: OpenedDir
    def __init__(self, Result: _Optional[_Union[OpenedDir, _Mapping]] = ...) -> None: ...

class OpenFileRes(_message.Message):
    __slots__ = ("Result",)
    RESULT_FIELD_NUMBER: _ClassVar[int]
    Result: OpenedFile
    def __init__(self, Result: _Optional[_Union[OpenedFile, _Mapping]] = ...) -> None: ...

class ReadDirRes(_message.Message):
    __slots__ = ("Result",)
    RESULT_FIELD_NUMBER: _ClassVar[int]
    Result: _containers.RepeatedCompositeFieldContainer[DirEntry]
    def __init__(self, Result: _Optional[_Iterable[_Union[DirEntry, _Mapping]]] = ...) -> None: ...

class ReadFileRes(_message.Message):
    __slots__ = ("Result",)
    RESULT_FIELD_NUMBER: _ClassVar[int]
    Result: FileEntry
    def __init__(self, Result: _Optional[_Union[FileEntry, _Mapping]] = ...) -> None: ...

class WriteFileRes(_message.Message):
    __slots__ = ("Result",)
    RESULT_FIELD_NUMBER: _ClassVar[int]
    Result: bool
    def __init__(self, Result: bool = ...) -> None: ...

class SetInodeAttRes(_message.Message):
    __slots__ = ("Result",)
    RESULT_FIELD_NUMBER: _ClassVar[int]
    Result: InodeAtt
    def __init__(self, Result: _Optional[_Union[InodeAtt, _Mapping]] = ...) -> None: ...
