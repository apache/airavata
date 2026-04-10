from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class TenantApprovalStatus(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    TENANT_APPROVAL_STATUS_UNKNOWN: _ClassVar[TenantApprovalStatus]
    REQUESTED: _ClassVar[TenantApprovalStatus]
    APPROVED: _ClassVar[TenantApprovalStatus]
    ACTIVE: _ClassVar[TenantApprovalStatus]
    DEACTIVATED: _ClassVar[TenantApprovalStatus]
    CANCELLED: _ClassVar[TenantApprovalStatus]
    DENIED: _ClassVar[TenantApprovalStatus]
    CREATED: _ClassVar[TenantApprovalStatus]
    DEPLOYED: _ClassVar[TenantApprovalStatus]
TENANT_APPROVAL_STATUS_UNKNOWN: TenantApprovalStatus
REQUESTED: TenantApprovalStatus
APPROVED: TenantApprovalStatus
ACTIVE: TenantApprovalStatus
DEACTIVATED: TenantApprovalStatus
CANCELLED: TenantApprovalStatus
DENIED: TenantApprovalStatus
CREATED: TenantApprovalStatus
DEPLOYED: TenantApprovalStatus

class TenantPreferences(_message.Message):
    __slots__ = ("tenant_admin_first_name", "tenant_admin_last_name", "tenant_admin_email")
    TENANT_ADMIN_FIRST_NAME_FIELD_NUMBER: _ClassVar[int]
    TENANT_ADMIN_LAST_NAME_FIELD_NUMBER: _ClassVar[int]
    TENANT_ADMIN_EMAIL_FIELD_NUMBER: _ClassVar[int]
    tenant_admin_first_name: str
    tenant_admin_last_name: str
    tenant_admin_email: str
    def __init__(self, tenant_admin_first_name: _Optional[str] = ..., tenant_admin_last_name: _Optional[str] = ..., tenant_admin_email: _Optional[str] = ...) -> None: ...

class TenantConfig(_message.Message):
    __slots__ = ("oauth_client_id", "oauth_client_secret", "identity_server_user_name", "identity_server_password_token")
    OAUTH_CLIENT_ID_FIELD_NUMBER: _ClassVar[int]
    OAUTH_CLIENT_SECRET_FIELD_NUMBER: _ClassVar[int]
    IDENTITY_SERVER_USER_NAME_FIELD_NUMBER: _ClassVar[int]
    IDENTITY_SERVER_PASSWORD_TOKEN_FIELD_NUMBER: _ClassVar[int]
    oauth_client_id: str
    oauth_client_secret: str
    identity_server_user_name: str
    identity_server_password_token: str
    def __init__(self, oauth_client_id: _Optional[str] = ..., oauth_client_secret: _Optional[str] = ..., identity_server_user_name: _Optional[str] = ..., identity_server_password_token: _Optional[str] = ...) -> None: ...

class Tenant(_message.Message):
    __slots__ = ("tenant_id", "tenant_approval_status", "tenant_name", "domain", "email_address", "tenant_acronym", "tenant_url", "tenant_public_abstract", "review_proposal_description", "declined_reason", "request_creation_time", "requester_username")
    TENANT_ID_FIELD_NUMBER: _ClassVar[int]
    TENANT_APPROVAL_STATUS_FIELD_NUMBER: _ClassVar[int]
    TENANT_NAME_FIELD_NUMBER: _ClassVar[int]
    DOMAIN_FIELD_NUMBER: _ClassVar[int]
    EMAIL_ADDRESS_FIELD_NUMBER: _ClassVar[int]
    TENANT_ACRONYM_FIELD_NUMBER: _ClassVar[int]
    TENANT_URL_FIELD_NUMBER: _ClassVar[int]
    TENANT_PUBLIC_ABSTRACT_FIELD_NUMBER: _ClassVar[int]
    REVIEW_PROPOSAL_DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    DECLINED_REASON_FIELD_NUMBER: _ClassVar[int]
    REQUEST_CREATION_TIME_FIELD_NUMBER: _ClassVar[int]
    REQUESTER_USERNAME_FIELD_NUMBER: _ClassVar[int]
    tenant_id: str
    tenant_approval_status: TenantApprovalStatus
    tenant_name: str
    domain: str
    email_address: str
    tenant_acronym: str
    tenant_url: str
    tenant_public_abstract: str
    review_proposal_description: str
    declined_reason: str
    request_creation_time: int
    requester_username: str
    def __init__(self, tenant_id: _Optional[str] = ..., tenant_approval_status: _Optional[_Union[TenantApprovalStatus, str]] = ..., tenant_name: _Optional[str] = ..., domain: _Optional[str] = ..., email_address: _Optional[str] = ..., tenant_acronym: _Optional[str] = ..., tenant_url: _Optional[str] = ..., tenant_public_abstract: _Optional[str] = ..., review_proposal_description: _Optional[str] = ..., declined_reason: _Optional[str] = ..., request_creation_time: _Optional[int] = ..., requester_username: _Optional[str] = ...) -> None: ...
