from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class GatewayApprovalStatus(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    GATEWAY_APPROVAL_STATUS_UNKNOWN: _ClassVar[GatewayApprovalStatus]
    REQUESTED: _ClassVar[GatewayApprovalStatus]
    APPROVED: _ClassVar[GatewayApprovalStatus]
    ACTIVE: _ClassVar[GatewayApprovalStatus]
    DEACTIVATED: _ClassVar[GatewayApprovalStatus]
    CANCELLED: _ClassVar[GatewayApprovalStatus]
    DENIED: _ClassVar[GatewayApprovalStatus]
    CREATED: _ClassVar[GatewayApprovalStatus]
    DEPLOYED: _ClassVar[GatewayApprovalStatus]

class NotificationPriority(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    NOTIFICATION_PRIORITY_UNKNOWN: _ClassVar[NotificationPriority]
    LOW: _ClassVar[NotificationPriority]
    NORMAL: _ClassVar[NotificationPriority]
    HIGH: _ClassVar[NotificationPriority]
GATEWAY_APPROVAL_STATUS_UNKNOWN: GatewayApprovalStatus
REQUESTED: GatewayApprovalStatus
APPROVED: GatewayApprovalStatus
ACTIVE: GatewayApprovalStatus
DEACTIVATED: GatewayApprovalStatus
CANCELLED: GatewayApprovalStatus
DENIED: GatewayApprovalStatus
CREATED: GatewayApprovalStatus
DEPLOYED: GatewayApprovalStatus
NOTIFICATION_PRIORITY_UNKNOWN: NotificationPriority
LOW: NotificationPriority
NORMAL: NotificationPriority
HIGH: NotificationPriority

class Group(_message.Message):
    __slots__ = ("group_name", "description")
    GROUP_NAME_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    group_name: str
    description: str
    def __init__(self, group_name: _Optional[str] = ..., description: _Optional[str] = ...) -> None: ...

class Project(_message.Message):
    __slots__ = ("project_id", "owner", "gateway_id", "name", "description", "creation_time", "shared_users", "shared_groups")
    PROJECT_ID_FIELD_NUMBER: _ClassVar[int]
    OWNER_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    NAME_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    CREATION_TIME_FIELD_NUMBER: _ClassVar[int]
    SHARED_USERS_FIELD_NUMBER: _ClassVar[int]
    SHARED_GROUPS_FIELD_NUMBER: _ClassVar[int]
    project_id: str
    owner: str
    gateway_id: str
    name: str
    description: str
    creation_time: int
    shared_users: _containers.RepeatedScalarFieldContainer[str]
    shared_groups: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, project_id: _Optional[str] = ..., owner: _Optional[str] = ..., gateway_id: _Optional[str] = ..., name: _Optional[str] = ..., description: _Optional[str] = ..., creation_time: _Optional[int] = ..., shared_users: _Optional[_Iterable[str]] = ..., shared_groups: _Optional[_Iterable[str]] = ...) -> None: ...

class User(_message.Message):
    __slots__ = ("airavata_internal_user_id", "user_name", "gateway_id", "first_name", "last_name", "email")
    AIRAVATA_INTERNAL_USER_ID_FIELD_NUMBER: _ClassVar[int]
    USER_NAME_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    FIRST_NAME_FIELD_NUMBER: _ClassVar[int]
    LAST_NAME_FIELD_NUMBER: _ClassVar[int]
    EMAIL_FIELD_NUMBER: _ClassVar[int]
    airavata_internal_user_id: str
    user_name: str
    gateway_id: str
    first_name: str
    last_name: str
    email: str
    def __init__(self, airavata_internal_user_id: _Optional[str] = ..., user_name: _Optional[str] = ..., gateway_id: _Optional[str] = ..., first_name: _Optional[str] = ..., last_name: _Optional[str] = ..., email: _Optional[str] = ...) -> None: ...

class Gateway(_message.Message):
    __slots__ = ("airavata_internal_gateway_id", "gateway_id", "gateway_approval_status", "gateway_name", "domain", "email_address", "gateway_acronym", "gateway_url", "gateway_public_abstract", "review_proposal_description", "gateway_admin_first_name", "gateway_admin_last_name", "gateway_admin_email", "identity_server_user_name", "identity_server_password_token", "declined_reason", "oauth_client_id", "oauth_client_secret", "request_creation_time", "requester_username")
    AIRAVATA_INTERNAL_GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_APPROVAL_STATUS_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_NAME_FIELD_NUMBER: _ClassVar[int]
    DOMAIN_FIELD_NUMBER: _ClassVar[int]
    EMAIL_ADDRESS_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ACRONYM_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_URL_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_PUBLIC_ABSTRACT_FIELD_NUMBER: _ClassVar[int]
    REVIEW_PROPOSAL_DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ADMIN_FIRST_NAME_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ADMIN_LAST_NAME_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ADMIN_EMAIL_FIELD_NUMBER: _ClassVar[int]
    IDENTITY_SERVER_USER_NAME_FIELD_NUMBER: _ClassVar[int]
    IDENTITY_SERVER_PASSWORD_TOKEN_FIELD_NUMBER: _ClassVar[int]
    DECLINED_REASON_FIELD_NUMBER: _ClassVar[int]
    OAUTH_CLIENT_ID_FIELD_NUMBER: _ClassVar[int]
    OAUTH_CLIENT_SECRET_FIELD_NUMBER: _ClassVar[int]
    REQUEST_CREATION_TIME_FIELD_NUMBER: _ClassVar[int]
    REQUESTER_USERNAME_FIELD_NUMBER: _ClassVar[int]
    airavata_internal_gateway_id: str
    gateway_id: str
    gateway_approval_status: GatewayApprovalStatus
    gateway_name: str
    domain: str
    email_address: str
    gateway_acronym: str
    gateway_url: str
    gateway_public_abstract: str
    review_proposal_description: str
    gateway_admin_first_name: str
    gateway_admin_last_name: str
    gateway_admin_email: str
    identity_server_user_name: str
    identity_server_password_token: str
    declined_reason: str
    oauth_client_id: str
    oauth_client_secret: str
    request_creation_time: int
    requester_username: str
    def __init__(self, airavata_internal_gateway_id: _Optional[str] = ..., gateway_id: _Optional[str] = ..., gateway_approval_status: _Optional[_Union[GatewayApprovalStatus, str]] = ..., gateway_name: _Optional[str] = ..., domain: _Optional[str] = ..., email_address: _Optional[str] = ..., gateway_acronym: _Optional[str] = ..., gateway_url: _Optional[str] = ..., gateway_public_abstract: _Optional[str] = ..., review_proposal_description: _Optional[str] = ..., gateway_admin_first_name: _Optional[str] = ..., gateway_admin_last_name: _Optional[str] = ..., gateway_admin_email: _Optional[str] = ..., identity_server_user_name: _Optional[str] = ..., identity_server_password_token: _Optional[str] = ..., declined_reason: _Optional[str] = ..., oauth_client_id: _Optional[str] = ..., oauth_client_secret: _Optional[str] = ..., request_creation_time: _Optional[int] = ..., requester_username: _Optional[str] = ...) -> None: ...

class GatewayUsageReportingCommand(_message.Message):
    __slots__ = ("gateway_id", "compute_resource_id", "command")
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    COMMAND_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    compute_resource_id: str
    command: str
    def __init__(self, gateway_id: _Optional[str] = ..., compute_resource_id: _Optional[str] = ..., command: _Optional[str] = ...) -> None: ...

class Notification(_message.Message):
    __slots__ = ("notification_id", "gateway_id", "title", "notification_message", "creation_time", "published_time", "expiration_time", "priority")
    NOTIFICATION_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    TITLE_FIELD_NUMBER: _ClassVar[int]
    NOTIFICATION_MESSAGE_FIELD_NUMBER: _ClassVar[int]
    CREATION_TIME_FIELD_NUMBER: _ClassVar[int]
    PUBLISHED_TIME_FIELD_NUMBER: _ClassVar[int]
    EXPIRATION_TIME_FIELD_NUMBER: _ClassVar[int]
    PRIORITY_FIELD_NUMBER: _ClassVar[int]
    notification_id: str
    gateway_id: str
    title: str
    notification_message: str
    creation_time: int
    published_time: int
    expiration_time: int
    priority: NotificationPriority
    def __init__(self, notification_id: _Optional[str] = ..., gateway_id: _Optional[str] = ..., title: _Optional[str] = ..., notification_message: _Optional[str] = ..., creation_time: _Optional[int] = ..., published_time: _Optional[int] = ..., expiration_time: _Optional[int] = ..., priority: _Optional[_Union[NotificationPriority, str]] = ...) -> None: ...
