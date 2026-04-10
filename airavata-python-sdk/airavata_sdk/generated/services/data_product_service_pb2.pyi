from google.api import annotations_pb2 as _annotations_pb2
from google.protobuf import empty_pb2 as _empty_pb2
from org.apache.airavata.model.data.replica import replica_catalog_pb2 as _replica_catalog_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class RegisterDataProductRequest(_message.Message):
    __slots__ = ("data_product",)
    DATA_PRODUCT_FIELD_NUMBER: _ClassVar[int]
    data_product: _replica_catalog_pb2.DataProductModel
    def __init__(self, data_product: _Optional[_Union[_replica_catalog_pb2.DataProductModel, _Mapping]] = ...) -> None: ...

class RegisterDataProductResponse(_message.Message):
    __slots__ = ("product_uri",)
    PRODUCT_URI_FIELD_NUMBER: _ClassVar[int]
    product_uri: str
    def __init__(self, product_uri: _Optional[str] = ...) -> None: ...

class GetDataProductRequest(_message.Message):
    __slots__ = ("product_uri",)
    PRODUCT_URI_FIELD_NUMBER: _ClassVar[int]
    product_uri: str
    def __init__(self, product_uri: _Optional[str] = ...) -> None: ...

class RegisterReplicaLocationRequest(_message.Message):
    __slots__ = ("product_uri", "replica_location")
    PRODUCT_URI_FIELD_NUMBER: _ClassVar[int]
    REPLICA_LOCATION_FIELD_NUMBER: _ClassVar[int]
    product_uri: str
    replica_location: _replica_catalog_pb2.DataReplicaLocationModel
    def __init__(self, product_uri: _Optional[str] = ..., replica_location: _Optional[_Union[_replica_catalog_pb2.DataReplicaLocationModel, _Mapping]] = ...) -> None: ...

class RegisterReplicaLocationResponse(_message.Message):
    __slots__ = ("replica_id",)
    REPLICA_ID_FIELD_NUMBER: _ClassVar[int]
    replica_id: str
    def __init__(self, replica_id: _Optional[str] = ...) -> None: ...

class GetParentDataProductRequest(_message.Message):
    __slots__ = ("product_uri",)
    PRODUCT_URI_FIELD_NUMBER: _ClassVar[int]
    product_uri: str
    def __init__(self, product_uri: _Optional[str] = ...) -> None: ...

class GetChildDataProductsRequest(_message.Message):
    __slots__ = ("product_uri",)
    PRODUCT_URI_FIELD_NUMBER: _ClassVar[int]
    product_uri: str
    def __init__(self, product_uri: _Optional[str] = ...) -> None: ...

class GetChildDataProductsResponse(_message.Message):
    __slots__ = ("data_products",)
    DATA_PRODUCTS_FIELD_NUMBER: _ClassVar[int]
    data_products: _containers.RepeatedCompositeFieldContainer[_replica_catalog_pb2.DataProductModel]
    def __init__(self, data_products: _Optional[_Iterable[_Union[_replica_catalog_pb2.DataProductModel, _Mapping]]] = ...) -> None: ...

class UpdateDataProductRequest(_message.Message):
    __slots__ = ("product_uri", "data_product")
    PRODUCT_URI_FIELD_NUMBER: _ClassVar[int]
    DATA_PRODUCT_FIELD_NUMBER: _ClassVar[int]
    product_uri: str
    data_product: _replica_catalog_pb2.DataProductModel
    def __init__(self, product_uri: _Optional[str] = ..., data_product: _Optional[_Union[_replica_catalog_pb2.DataProductModel, _Mapping]] = ...) -> None: ...

class DeleteDataProductRequest(_message.Message):
    __slots__ = ("product_uri",)
    PRODUCT_URI_FIELD_NUMBER: _ClassVar[int]
    product_uri: str
    def __init__(self, product_uri: _Optional[str] = ...) -> None: ...

class GetReplicaLocationRequest(_message.Message):
    __slots__ = ("replica_id",)
    REPLICA_ID_FIELD_NUMBER: _ClassVar[int]
    replica_id: str
    def __init__(self, replica_id: _Optional[str] = ...) -> None: ...

class UpdateReplicaLocationRequest(_message.Message):
    __slots__ = ("replica_id", "replica_location")
    REPLICA_ID_FIELD_NUMBER: _ClassVar[int]
    REPLICA_LOCATION_FIELD_NUMBER: _ClassVar[int]
    replica_id: str
    replica_location: _replica_catalog_pb2.DataReplicaLocationModel
    def __init__(self, replica_id: _Optional[str] = ..., replica_location: _Optional[_Union[_replica_catalog_pb2.DataReplicaLocationModel, _Mapping]] = ...) -> None: ...

class DeleteReplicaLocationRequest(_message.Message):
    __slots__ = ("replica_id",)
    REPLICA_ID_FIELD_NUMBER: _ClassVar[int]
    replica_id: str
    def __init__(self, replica_id: _Optional[str] = ...) -> None: ...
