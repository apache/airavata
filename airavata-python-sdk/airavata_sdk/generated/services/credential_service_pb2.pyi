from google.api import annotations_pb2 as _annotations_pb2
from google.protobuf import empty_pb2 as _empty_pb2
from org.apache.airavata.model.credential.store import credential_store_pb2 as _credential_store_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class GenerateAndRegisterSSHKeysRequest(_message.Message):
    __slots__ = ("gateway_id", "username", "description")
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    USERNAME_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    username: str
    description: str
    def __init__(self, gateway_id: _Optional[str] = ..., username: _Optional[str] = ..., description: _Optional[str] = ...) -> None: ...

class GenerateAndRegisterSSHKeysResponse(_message.Message):
    __slots__ = ("token",)
    TOKEN_FIELD_NUMBER: _ClassVar[int]
    token: str
    def __init__(self, token: _Optional[str] = ...) -> None: ...

class RegisterPwdCredentialRequest(_message.Message):
    __slots__ = ("gateway_id", "password_credential")
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    PASSWORD_CREDENTIAL_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    password_credential: _credential_store_pb2.PasswordCredential
    def __init__(self, gateway_id: _Optional[str] = ..., password_credential: _Optional[_Union[_credential_store_pb2.PasswordCredential, _Mapping]] = ...) -> None: ...

class RegisterPwdCredentialResponse(_message.Message):
    __slots__ = ("token",)
    TOKEN_FIELD_NUMBER: _ClassVar[int]
    token: str
    def __init__(self, token: _Optional[str] = ...) -> None: ...

class GetCredentialSummaryRequest(_message.Message):
    __slots__ = ("token_id", "gateway_id")
    TOKEN_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    token_id: str
    gateway_id: str
    def __init__(self, token_id: _Optional[str] = ..., gateway_id: _Optional[str] = ...) -> None: ...

class GetAllCredentialSummariesRequest(_message.Message):
    __slots__ = ("gateway_id", "type")
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    TYPE_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    type: _credential_store_pb2.SummaryType
    def __init__(self, gateway_id: _Optional[str] = ..., type: _Optional[_Union[_credential_store_pb2.SummaryType, str]] = ...) -> None: ...

class GetAllCredentialSummariesResponse(_message.Message):
    __slots__ = ("credential_summaries",)
    CREDENTIAL_SUMMARIES_FIELD_NUMBER: _ClassVar[int]
    credential_summaries: _containers.RepeatedCompositeFieldContainer[_credential_store_pb2.CredentialSummary]
    def __init__(self, credential_summaries: _Optional[_Iterable[_Union[_credential_store_pb2.CredentialSummary, _Mapping]]] = ...) -> None: ...

class DeleteSSHPubKeyRequest(_message.Message):
    __slots__ = ("token_id", "gateway_id")
    TOKEN_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    token_id: str
    gateway_id: str
    def __init__(self, token_id: _Optional[str] = ..., gateway_id: _Optional[str] = ...) -> None: ...

class DeletePWDCredentialRequest(_message.Message):
    __slots__ = ("token_id", "gateway_id")
    TOKEN_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    token_id: str
    gateway_id: str
    def __init__(self, token_id: _Optional[str] = ..., gateway_id: _Optional[str] = ...) -> None: ...

class DoesUserHaveSSHAccountRequest(_message.Message):
    __slots__ = ("compute_resource_id", "gateway_id", "username")
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    USERNAME_FIELD_NUMBER: _ClassVar[int]
    compute_resource_id: str
    gateway_id: str
    username: str
    def __init__(self, compute_resource_id: _Optional[str] = ..., gateway_id: _Optional[str] = ..., username: _Optional[str] = ...) -> None: ...

class DoesUserHaveSSHAccountResponse(_message.Message):
    __slots__ = ("has_account",)
    HAS_ACCOUNT_FIELD_NUMBER: _ClassVar[int]
    has_account: bool
    def __init__(self, has_account: bool = ...) -> None: ...

class IsSSHSetupCompleteRequest(_message.Message):
    __slots__ = ("compute_resource_id", "gateway_id", "username")
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    USERNAME_FIELD_NUMBER: _ClassVar[int]
    compute_resource_id: str
    gateway_id: str
    username: str
    def __init__(self, compute_resource_id: _Optional[str] = ..., gateway_id: _Optional[str] = ..., username: _Optional[str] = ...) -> None: ...

class IsSSHSetupCompleteResponse(_message.Message):
    __slots__ = ("is_complete",)
    IS_COMPLETE_FIELD_NUMBER: _ClassVar[int]
    is_complete: bool
    def __init__(self, is_complete: bool = ...) -> None: ...

class SetupSSHAccountRequest(_message.Message):
    __slots__ = ("compute_resource_id", "gateway_id", "username")
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    USERNAME_FIELD_NUMBER: _ClassVar[int]
    compute_resource_id: str
    gateway_id: str
    username: str
    def __init__(self, compute_resource_id: _Optional[str] = ..., gateway_id: _Optional[str] = ..., username: _Optional[str] = ...) -> None: ...

class SetupSSHAccountResponse(_message.Message):
    __slots__ = ("success",)
    SUCCESS_FIELD_NUMBER: _ClassVar[int]
    success: bool
    def __init__(self, success: bool = ...) -> None: ...
