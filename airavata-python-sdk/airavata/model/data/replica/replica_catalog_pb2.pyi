from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class ReplicaLocationCategory(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    REPLICA_LOCATION_CATEGORY_UNKNOWN: _ClassVar[ReplicaLocationCategory]
    GATEWAY_DATA_STORE: _ClassVar[ReplicaLocationCategory]
    COMPUTE_RESOURCE: _ClassVar[ReplicaLocationCategory]
    LONG_TERM_STORAGE_RESOURCE: _ClassVar[ReplicaLocationCategory]
    OTHER: _ClassVar[ReplicaLocationCategory]

class ReplicaPersistentType(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    REPLICA_PERSISTENT_TYPE_UNKNOWN: _ClassVar[ReplicaPersistentType]
    TRANSIENT: _ClassVar[ReplicaPersistentType]
    PERSISTENT: _ClassVar[ReplicaPersistentType]

class DataProductType(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    DATA_PRODUCT_TYPE_UNKNOWN: _ClassVar[DataProductType]
    FILE: _ClassVar[DataProductType]
    COLLECTION: _ClassVar[DataProductType]
REPLICA_LOCATION_CATEGORY_UNKNOWN: ReplicaLocationCategory
GATEWAY_DATA_STORE: ReplicaLocationCategory
COMPUTE_RESOURCE: ReplicaLocationCategory
LONG_TERM_STORAGE_RESOURCE: ReplicaLocationCategory
OTHER: ReplicaLocationCategory
REPLICA_PERSISTENT_TYPE_UNKNOWN: ReplicaPersistentType
TRANSIENT: ReplicaPersistentType
PERSISTENT: ReplicaPersistentType
DATA_PRODUCT_TYPE_UNKNOWN: DataProductType
FILE: DataProductType
COLLECTION: DataProductType

class DataReplicaLocationModel(_message.Message):
    __slots__ = ("replica_id", "product_uri", "replica_name", "replica_description", "creation_time", "last_modified_time", "valid_until_time", "replica_location_category", "replica_persistent_type", "storage_resource_id", "file_path", "replica_metadata")
    class ReplicaMetadataEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: str
        def __init__(self, key: _Optional[str] = ..., value: _Optional[str] = ...) -> None: ...
    REPLICA_ID_FIELD_NUMBER: _ClassVar[int]
    PRODUCT_URI_FIELD_NUMBER: _ClassVar[int]
    REPLICA_NAME_FIELD_NUMBER: _ClassVar[int]
    REPLICA_DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    CREATION_TIME_FIELD_NUMBER: _ClassVar[int]
    LAST_MODIFIED_TIME_FIELD_NUMBER: _ClassVar[int]
    VALID_UNTIL_TIME_FIELD_NUMBER: _ClassVar[int]
    REPLICA_LOCATION_CATEGORY_FIELD_NUMBER: _ClassVar[int]
    REPLICA_PERSISTENT_TYPE_FIELD_NUMBER: _ClassVar[int]
    STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    FILE_PATH_FIELD_NUMBER: _ClassVar[int]
    REPLICA_METADATA_FIELD_NUMBER: _ClassVar[int]
    replica_id: str
    product_uri: str
    replica_name: str
    replica_description: str
    creation_time: int
    last_modified_time: int
    valid_until_time: int
    replica_location_category: ReplicaLocationCategory
    replica_persistent_type: ReplicaPersistentType
    storage_resource_id: str
    file_path: str
    replica_metadata: _containers.ScalarMap[str, str]
    def __init__(self, replica_id: _Optional[str] = ..., product_uri: _Optional[str] = ..., replica_name: _Optional[str] = ..., replica_description: _Optional[str] = ..., creation_time: _Optional[int] = ..., last_modified_time: _Optional[int] = ..., valid_until_time: _Optional[int] = ..., replica_location_category: _Optional[_Union[ReplicaLocationCategory, str]] = ..., replica_persistent_type: _Optional[_Union[ReplicaPersistentType, str]] = ..., storage_resource_id: _Optional[str] = ..., file_path: _Optional[str] = ..., replica_metadata: _Optional[_Mapping[str, str]] = ...) -> None: ...

class DataProductModel(_message.Message):
    __slots__ = ("product_uri", "gateway_id", "parent_product_uri", "product_name", "product_description", "owner_name", "data_product_type", "product_size", "creation_time", "last_modified_time", "product_metadata", "replica_locations")
    class ProductMetadataEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: str
        def __init__(self, key: _Optional[str] = ..., value: _Optional[str] = ...) -> None: ...
    PRODUCT_URI_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    PARENT_PRODUCT_URI_FIELD_NUMBER: _ClassVar[int]
    PRODUCT_NAME_FIELD_NUMBER: _ClassVar[int]
    PRODUCT_DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    OWNER_NAME_FIELD_NUMBER: _ClassVar[int]
    DATA_PRODUCT_TYPE_FIELD_NUMBER: _ClassVar[int]
    PRODUCT_SIZE_FIELD_NUMBER: _ClassVar[int]
    CREATION_TIME_FIELD_NUMBER: _ClassVar[int]
    LAST_MODIFIED_TIME_FIELD_NUMBER: _ClassVar[int]
    PRODUCT_METADATA_FIELD_NUMBER: _ClassVar[int]
    REPLICA_LOCATIONS_FIELD_NUMBER: _ClassVar[int]
    product_uri: str
    gateway_id: str
    parent_product_uri: str
    product_name: str
    product_description: str
    owner_name: str
    data_product_type: DataProductType
    product_size: int
    creation_time: int
    last_modified_time: int
    product_metadata: _containers.ScalarMap[str, str]
    replica_locations: _containers.RepeatedCompositeFieldContainer[DataReplicaLocationModel]
    def __init__(self, product_uri: _Optional[str] = ..., gateway_id: _Optional[str] = ..., parent_product_uri: _Optional[str] = ..., product_name: _Optional[str] = ..., product_description: _Optional[str] = ..., owner_name: _Optional[str] = ..., data_product_type: _Optional[_Union[DataProductType, str]] = ..., product_size: _Optional[int] = ..., creation_time: _Optional[int] = ..., last_modified_time: _Optional[int] = ..., product_metadata: _Optional[_Mapping[str, str]] = ..., replica_locations: _Optional[_Iterable[_Union[DataReplicaLocationModel, _Mapping]]] = ...) -> None: ...
