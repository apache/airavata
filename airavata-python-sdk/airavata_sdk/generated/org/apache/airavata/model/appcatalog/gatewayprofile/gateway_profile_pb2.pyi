from org.apache.airavata.model.appcatalog.computeresource import compute_resource_pb2 as _compute_resource_pb2
from org.apache.airavata.model.data.movement import data_movement_pb2 as _data_movement_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class ComputeResourcePreference(_message.Message):
    __slots__ = ("compute_resource_id", "override_by_airavata", "login_user_name", "preferred_job_submission_protocol", "preferred_data_movement_protocol", "preferred_batch_queue", "scratch_location", "allocation_project_number", "resource_specific_credential_store_token", "usage_reporting_gateway_id", "quality_of_service", "reservation", "reservation_start_time", "reservation_end_time", "ssh_account_provisioner", "ssh_account_provisioner_config", "ssh_account_provisioner_additional_info")
    class SshAccountProvisionerConfigEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: str
        def __init__(self, key: _Optional[str] = ..., value: _Optional[str] = ...) -> None: ...
    COMPUTE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    OVERRIDE_BY_AIRAVATA_FIELD_NUMBER: _ClassVar[int]
    LOGIN_USER_NAME_FIELD_NUMBER: _ClassVar[int]
    PREFERRED_JOB_SUBMISSION_PROTOCOL_FIELD_NUMBER: _ClassVar[int]
    PREFERRED_DATA_MOVEMENT_PROTOCOL_FIELD_NUMBER: _ClassVar[int]
    PREFERRED_BATCH_QUEUE_FIELD_NUMBER: _ClassVar[int]
    SCRATCH_LOCATION_FIELD_NUMBER: _ClassVar[int]
    ALLOCATION_PROJECT_NUMBER_FIELD_NUMBER: _ClassVar[int]
    RESOURCE_SPECIFIC_CREDENTIAL_STORE_TOKEN_FIELD_NUMBER: _ClassVar[int]
    USAGE_REPORTING_GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    QUALITY_OF_SERVICE_FIELD_NUMBER: _ClassVar[int]
    RESERVATION_FIELD_NUMBER: _ClassVar[int]
    RESERVATION_START_TIME_FIELD_NUMBER: _ClassVar[int]
    RESERVATION_END_TIME_FIELD_NUMBER: _ClassVar[int]
    SSH_ACCOUNT_PROVISIONER_FIELD_NUMBER: _ClassVar[int]
    SSH_ACCOUNT_PROVISIONER_CONFIG_FIELD_NUMBER: _ClassVar[int]
    SSH_ACCOUNT_PROVISIONER_ADDITIONAL_INFO_FIELD_NUMBER: _ClassVar[int]
    compute_resource_id: str
    override_by_airavata: bool
    login_user_name: str
    preferred_job_submission_protocol: _compute_resource_pb2.JobSubmissionProtocol
    preferred_data_movement_protocol: _data_movement_pb2.DataMovementProtocol
    preferred_batch_queue: str
    scratch_location: str
    allocation_project_number: str
    resource_specific_credential_store_token: str
    usage_reporting_gateway_id: str
    quality_of_service: str
    reservation: str
    reservation_start_time: int
    reservation_end_time: int
    ssh_account_provisioner: str
    ssh_account_provisioner_config: _containers.ScalarMap[str, str]
    ssh_account_provisioner_additional_info: str
    def __init__(self, compute_resource_id: _Optional[str] = ..., override_by_airavata: bool = ..., login_user_name: _Optional[str] = ..., preferred_job_submission_protocol: _Optional[_Union[_compute_resource_pb2.JobSubmissionProtocol, str]] = ..., preferred_data_movement_protocol: _Optional[_Union[_data_movement_pb2.DataMovementProtocol, str]] = ..., preferred_batch_queue: _Optional[str] = ..., scratch_location: _Optional[str] = ..., allocation_project_number: _Optional[str] = ..., resource_specific_credential_store_token: _Optional[str] = ..., usage_reporting_gateway_id: _Optional[str] = ..., quality_of_service: _Optional[str] = ..., reservation: _Optional[str] = ..., reservation_start_time: _Optional[int] = ..., reservation_end_time: _Optional[int] = ..., ssh_account_provisioner: _Optional[str] = ..., ssh_account_provisioner_config: _Optional[_Mapping[str, str]] = ..., ssh_account_provisioner_additional_info: _Optional[str] = ...) -> None: ...

class StoragePreference(_message.Message):
    __slots__ = ("storage_resource_id", "login_user_name", "file_system_root_location", "resource_specific_credential_store_token")
    STORAGE_RESOURCE_ID_FIELD_NUMBER: _ClassVar[int]
    LOGIN_USER_NAME_FIELD_NUMBER: _ClassVar[int]
    FILE_SYSTEM_ROOT_LOCATION_FIELD_NUMBER: _ClassVar[int]
    RESOURCE_SPECIFIC_CREDENTIAL_STORE_TOKEN_FIELD_NUMBER: _ClassVar[int]
    storage_resource_id: str
    login_user_name: str
    file_system_root_location: str
    resource_specific_credential_store_token: str
    def __init__(self, storage_resource_id: _Optional[str] = ..., login_user_name: _Optional[str] = ..., file_system_root_location: _Optional[str] = ..., resource_specific_credential_store_token: _Optional[str] = ...) -> None: ...

class GatewayResourceProfile(_message.Message):
    __slots__ = ("gateway_id", "credential_store_token", "compute_resource_preferences", "storage_preferences", "identity_server_tenant", "identity_server_pwd_cred_token")
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    CREDENTIAL_STORE_TOKEN_FIELD_NUMBER: _ClassVar[int]
    COMPUTE_RESOURCE_PREFERENCES_FIELD_NUMBER: _ClassVar[int]
    STORAGE_PREFERENCES_FIELD_NUMBER: _ClassVar[int]
    IDENTITY_SERVER_TENANT_FIELD_NUMBER: _ClassVar[int]
    IDENTITY_SERVER_PWD_CRED_TOKEN_FIELD_NUMBER: _ClassVar[int]
    gateway_id: str
    credential_store_token: str
    compute_resource_preferences: _containers.RepeatedCompositeFieldContainer[ComputeResourcePreference]
    storage_preferences: _containers.RepeatedCompositeFieldContainer[StoragePreference]
    identity_server_tenant: str
    identity_server_pwd_cred_token: str
    def __init__(self, gateway_id: _Optional[str] = ..., credential_store_token: _Optional[str] = ..., compute_resource_preferences: _Optional[_Iterable[_Union[ComputeResourcePreference, _Mapping]]] = ..., storage_preferences: _Optional[_Iterable[_Union[StoragePreference, _Mapping]]] = ..., identity_server_tenant: _Optional[str] = ..., identity_server_pwd_cred_token: _Optional[str] = ...) -> None: ...
