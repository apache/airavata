from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class SSHAccountProvisionerConfigParamType(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    SSH_ACCOUNT_PROVISIONER_CONFIG_PARAM_TYPE_UNKNOWN: _ClassVar[SSHAccountProvisionerConfigParamType]
    STRING: _ClassVar[SSHAccountProvisionerConfigParamType]
    CRED_STORE_PASSWORD_TOKEN: _ClassVar[SSHAccountProvisionerConfigParamType]
SSH_ACCOUNT_PROVISIONER_CONFIG_PARAM_TYPE_UNKNOWN: SSHAccountProvisionerConfigParamType
STRING: SSHAccountProvisionerConfigParamType
CRED_STORE_PASSWORD_TOKEN: SSHAccountProvisionerConfigParamType

class SSHAccountProvisionerConfigParam(_message.Message):
    __slots__ = ("name", "type", "is_optional", "description")
    NAME_FIELD_NUMBER: _ClassVar[int]
    TYPE_FIELD_NUMBER: _ClassVar[int]
    IS_OPTIONAL_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    name: str
    type: SSHAccountProvisionerConfigParamType
    is_optional: bool
    description: str
    def __init__(self, name: _Optional[str] = ..., type: _Optional[_Union[SSHAccountProvisionerConfigParamType, str]] = ..., is_optional: bool = ..., description: _Optional[str] = ...) -> None: ...

class SSHAccountProvisioner(_message.Message):
    __slots__ = ("name", "can_create_account", "can_install_ssh_key", "config_params")
    NAME_FIELD_NUMBER: _ClassVar[int]
    CAN_CREATE_ACCOUNT_FIELD_NUMBER: _ClassVar[int]
    CAN_INSTALL_SSH_KEY_FIELD_NUMBER: _ClassVar[int]
    CONFIG_PARAMS_FIELD_NUMBER: _ClassVar[int]
    name: str
    can_create_account: bool
    can_install_ssh_key: bool
    config_params: _containers.RepeatedCompositeFieldContainer[SSHAccountProvisionerConfigParam]
    def __init__(self, name: _Optional[str] = ..., can_create_account: bool = ..., can_install_ssh_key: bool = ..., config_params: _Optional[_Iterable[_Union[SSHAccountProvisionerConfigParam, _Mapping]]] = ...) -> None: ...
