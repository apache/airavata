from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class SummaryType(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    SUMMARY_TYPE_UNKNOWN: _ClassVar[SummaryType]
    SSH: _ClassVar[SummaryType]
    PASSWD: _ClassVar[SummaryType]
    CERT: _ClassVar[SummaryType]
SUMMARY_TYPE_UNKNOWN: SummaryType
SSH: SummaryType
PASSWD: SummaryType
CERT: SummaryType

class SSHCredential(_message.Message):
    __slots__ = ("gateway_id", "username", "passphrase", "public_key", "private_key", "persisted_time", "token", "description")
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    USERNAME_FIELD_NUMBER: _ClassVar[int]
    PASSPHRASE_FIELD_NUMBER: _ClassVar[int]
    PUBLIC_KEY_FIELD_NUMBER: _ClassVar[int]
    PRIVATE_KEY_FIELD_NUMBER: _ClassVar[int]
    PERSISTED_TIME_FIELD_NUMBER: _ClassVar[int]
    TOKEN_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    username: str
    passphrase: str
    public_key: str
    private_key: str
    persisted_time: int
    token: str
    description: str
    def __init__(self, gateway_id: _Optional[str] = ..., username: _Optional[str] = ..., passphrase: _Optional[str] = ..., public_key: _Optional[str] = ..., private_key: _Optional[str] = ..., persisted_time: _Optional[int] = ..., token: _Optional[str] = ..., description: _Optional[str] = ...) -> None: ...

class CredentialSummary(_message.Message):
    __slots__ = ("type", "gateway_id", "username", "public_key", "persisted_time", "token", "description")
    TYPE_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    USERNAME_FIELD_NUMBER: _ClassVar[int]
    PUBLIC_KEY_FIELD_NUMBER: _ClassVar[int]
    PERSISTED_TIME_FIELD_NUMBER: _ClassVar[int]
    TOKEN_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    type: SummaryType
    gateway_id: str
    username: str
    public_key: str
    persisted_time: int
    token: str
    description: str
    def __init__(self, type: _Optional[_Union[SummaryType, str]] = ..., gateway_id: _Optional[str] = ..., username: _Optional[str] = ..., public_key: _Optional[str] = ..., persisted_time: _Optional[int] = ..., token: _Optional[str] = ..., description: _Optional[str] = ...) -> None: ...

class CommunityUser(_message.Message):
    __slots__ = ("gateway_name", "username", "user_email")
    GATEWAY_NAME_FIELD_NUMBER: _ClassVar[int]
    USERNAME_FIELD_NUMBER: _ClassVar[int]
    USER_EMAIL_FIELD_NUMBER: _ClassVar[int]
    gateway_name: str
    username: str
    user_email: str
    def __init__(self, gateway_name: _Optional[str] = ..., username: _Optional[str] = ..., user_email: _Optional[str] = ...) -> None: ...

class CertificateCredential(_message.Message):
    __slots__ = ("community_user", "x509_cert", "not_after", "private_key", "life_time", "not_before", "persisted_time", "token")
    COMMUNITY_USER_FIELD_NUMBER: _ClassVar[int]
    X509_CERT_FIELD_NUMBER: _ClassVar[int]
    NOT_AFTER_FIELD_NUMBER: _ClassVar[int]
    PRIVATE_KEY_FIELD_NUMBER: _ClassVar[int]
    LIFE_TIME_FIELD_NUMBER: _ClassVar[int]
    NOT_BEFORE_FIELD_NUMBER: _ClassVar[int]
    PERSISTED_TIME_FIELD_NUMBER: _ClassVar[int]
    TOKEN_FIELD_NUMBER: _ClassVar[int]
    community_user: CommunityUser
    x509_cert: str
    not_after: str
    private_key: str
    life_time: int
    not_before: str
    persisted_time: int
    token: str
    def __init__(self, community_user: _Optional[_Union[CommunityUser, _Mapping]] = ..., x509_cert: _Optional[str] = ..., not_after: _Optional[str] = ..., private_key: _Optional[str] = ..., life_time: _Optional[int] = ..., not_before: _Optional[str] = ..., persisted_time: _Optional[int] = ..., token: _Optional[str] = ...) -> None: ...

class PasswordCredential(_message.Message):
    __slots__ = ("gateway_id", "portal_user_name", "login_user_name", "password", "description", "persisted_time", "token")
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    PORTAL_USER_NAME_FIELD_NUMBER: _ClassVar[int]
    LOGIN_USER_NAME_FIELD_NUMBER: _ClassVar[int]
    PASSWORD_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    PERSISTED_TIME_FIELD_NUMBER: _ClassVar[int]
    TOKEN_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    portal_user_name: str
    login_user_name: str
    password: str
    description: str
    persisted_time: int
    token: str
    def __init__(self, gateway_id: _Optional[str] = ..., portal_user_name: _Optional[str] = ..., login_user_name: _Optional[str] = ..., password: _Optional[str] = ..., description: _Optional[str] = ..., persisted_time: _Optional[int] = ..., token: _Optional[str] = ...) -> None: ...
