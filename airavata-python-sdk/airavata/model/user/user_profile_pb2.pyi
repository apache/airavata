from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class Status(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    STATUS_UNKNOWN: _ClassVar[Status]
    ACTIVE: _ClassVar[Status]
    CONFIRMED: _ClassVar[Status]
    APPROVED: _ClassVar[Status]
    DELETED: _ClassVar[Status]
    DUPLICATE: _ClassVar[Status]
    GRACE_PERIOD: _ClassVar[Status]
    INVITED: _ClassVar[Status]
    DENIED: _ClassVar[Status]
    PENDING: _ClassVar[Status]
    PENDING_APPROVAL: _ClassVar[Status]
    PENDING_CONFIRMATION: _ClassVar[Status]
    SUSPENDED: _ClassVar[Status]
    DECLINED: _ClassVar[Status]
    EXPIRED: _ClassVar[Status]

class USCitizenship(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    US_CITIZENSHIP_UNKNOWN: _ClassVar[USCitizenship]
    US_CITIZEN: _ClassVar[USCitizenship]
    US_PERMANENT_RESIDENT: _ClassVar[USCitizenship]
    OTHER_NON_US_CITIZEN: _ClassVar[USCitizenship]

class Ethnicity(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    ETHNICITY_UNKNOWN: _ClassVar[Ethnicity]
    HISPANIC_LATINO: _ClassVar[Ethnicity]
    NOT_HISPANIC_LATINO: _ClassVar[Ethnicity]

class Race(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    RACE_UNKNOWN: _ClassVar[Race]
    ASIAN: _ClassVar[Race]
    AMERICAN_INDIAN_OR_ALASKAN_NATIVE: _ClassVar[Race]
    BLACK_OR_AFRICAN_AMERICAN: _ClassVar[Race]
    NATIVE_HAWAIIAN_OR_PACIFIC_ISLANDER: _ClassVar[Race]
    WHITE: _ClassVar[Race]

class Disability(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    DISABILITY_UNKNOWN: _ClassVar[Disability]
    HEARING_IMAPAIRED: _ClassVar[Disability]
    VISUAL_IMPAIRED: _ClassVar[Disability]
    MOBILITY_OR_ORTHOPEDIC_IMPAIRMENT: _ClassVar[Disability]
    OTHER_IMPAIRMENT: _ClassVar[Disability]
STATUS_UNKNOWN: Status
ACTIVE: Status
CONFIRMED: Status
APPROVED: Status
DELETED: Status
DUPLICATE: Status
GRACE_PERIOD: Status
INVITED: Status
DENIED: Status
PENDING: Status
PENDING_APPROVAL: Status
PENDING_CONFIRMATION: Status
SUSPENDED: Status
DECLINED: Status
EXPIRED: Status
US_CITIZENSHIP_UNKNOWN: USCitizenship
US_CITIZEN: USCitizenship
US_PERMANENT_RESIDENT: USCitizenship
OTHER_NON_US_CITIZEN: USCitizenship
ETHNICITY_UNKNOWN: Ethnicity
HISPANIC_LATINO: Ethnicity
NOT_HISPANIC_LATINO: Ethnicity
RACE_UNKNOWN: Race
ASIAN: Race
AMERICAN_INDIAN_OR_ALASKAN_NATIVE: Race
BLACK_OR_AFRICAN_AMERICAN: Race
NATIVE_HAWAIIAN_OR_PACIFIC_ISLANDER: Race
WHITE: Race
DISABILITY_UNKNOWN: Disability
HEARING_IMAPAIRED: Disability
VISUAL_IMPAIRED: Disability
MOBILITY_OR_ORTHOPEDIC_IMPAIRMENT: Disability
OTHER_IMPAIRMENT: Disability

class NSFDemographics(_message.Message):
    __slots__ = ("airavata_internal_user_id", "gender", "us_citizenship", "ethnicities", "races", "disabilities")
    AIRAVATA_INTERNAL_USER_ID_FIELD_NUMBER: _ClassVar[int]
    GENDER_FIELD_NUMBER: _ClassVar[int]
    US_CITIZENSHIP_FIELD_NUMBER: _ClassVar[int]
    ETHNICITIES_FIELD_NUMBER: _ClassVar[int]
    RACES_FIELD_NUMBER: _ClassVar[int]
    DISABILITIES_FIELD_NUMBER: _ClassVar[int]
    airavata_internal_user_id: str
    gender: str
    us_citizenship: USCitizenship
    ethnicities: _containers.RepeatedScalarFieldContainer[Ethnicity]
    races: _containers.RepeatedScalarFieldContainer[Race]
    disabilities: _containers.RepeatedScalarFieldContainer[Disability]
    def __init__(self, airavata_internal_user_id: _Optional[str] = ..., gender: _Optional[str] = ..., us_citizenship: _Optional[_Union[USCitizenship, str]] = ..., ethnicities: _Optional[_Iterable[_Union[Ethnicity, str]]] = ..., races: _Optional[_Iterable[_Union[Race, str]]] = ..., disabilities: _Optional[_Iterable[_Union[Disability, str]]] = ...) -> None: ...

class CustomDashboard(_message.Message):
    __slots__ = ("airavata_internal_user_id", "experiment_id", "name", "description", "project", "owner", "application", "compute_resource", "job_name", "job_id", "job_status", "job_creation_time", "notifications_to", "working_dir", "job_description", "creation_time", "last_modified_time", "wall_time", "cpu_count", "node_count", "queue", "inputs", "outputs", "storage_dir", "errors")
    AIRAVATA_INTERNAL_USER_ID_FIELD_NUMBER: _ClassVar[int]
    EXPERIMENT_ID_FIELD_NUMBER: _ClassVar[int]
    NAME_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    PROJECT_FIELD_NUMBER: _ClassVar[int]
    OWNER_FIELD_NUMBER: _ClassVar[int]
    APPLICATION_FIELD_NUMBER: _ClassVar[int]
    COMPUTE_RESOURCE_FIELD_NUMBER: _ClassVar[int]
    JOB_NAME_FIELD_NUMBER: _ClassVar[int]
    JOB_ID_FIELD_NUMBER: _ClassVar[int]
    JOB_STATUS_FIELD_NUMBER: _ClassVar[int]
    JOB_CREATION_TIME_FIELD_NUMBER: _ClassVar[int]
    NOTIFICATIONS_TO_FIELD_NUMBER: _ClassVar[int]
    WORKING_DIR_FIELD_NUMBER: _ClassVar[int]
    JOB_DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    CREATION_TIME_FIELD_NUMBER: _ClassVar[int]
    LAST_MODIFIED_TIME_FIELD_NUMBER: _ClassVar[int]
    WALL_TIME_FIELD_NUMBER: _ClassVar[int]
    CPU_COUNT_FIELD_NUMBER: _ClassVar[int]
    NODE_COUNT_FIELD_NUMBER: _ClassVar[int]
    QUEUE_FIELD_NUMBER: _ClassVar[int]
    INPUTS_FIELD_NUMBER: _ClassVar[int]
    OUTPUTS_FIELD_NUMBER: _ClassVar[int]
    STORAGE_DIR_FIELD_NUMBER: _ClassVar[int]
    ERRORS_FIELD_NUMBER: _ClassVar[int]
    airavata_internal_user_id: str
    experiment_id: str
    name: str
    description: str
    project: str
    owner: str
    application: str
    compute_resource: str
    job_name: str
    job_id: str
    job_status: str
    job_creation_time: str
    notifications_to: str
    working_dir: str
    job_description: str
    creation_time: str
    last_modified_time: str
    wall_time: str
    cpu_count: str
    node_count: str
    queue: str
    inputs: str
    outputs: str
    storage_dir: str
    errors: str
    def __init__(self, airavata_internal_user_id: _Optional[str] = ..., experiment_id: _Optional[str] = ..., name: _Optional[str] = ..., description: _Optional[str] = ..., project: _Optional[str] = ..., owner: _Optional[str] = ..., application: _Optional[str] = ..., compute_resource: _Optional[str] = ..., job_name: _Optional[str] = ..., job_id: _Optional[str] = ..., job_status: _Optional[str] = ..., job_creation_time: _Optional[str] = ..., notifications_to: _Optional[str] = ..., working_dir: _Optional[str] = ..., job_description: _Optional[str] = ..., creation_time: _Optional[str] = ..., last_modified_time: _Optional[str] = ..., wall_time: _Optional[str] = ..., cpu_count: _Optional[str] = ..., node_count: _Optional[str] = ..., queue: _Optional[str] = ..., inputs: _Optional[str] = ..., outputs: _Optional[str] = ..., storage_dir: _Optional[str] = ..., errors: _Optional[str] = ...) -> None: ...

class UserProfile(_message.Message):
    __slots__ = ("user_model_version", "airavata_internal_user_id", "user_id", "gateway_id", "emails", "first_name", "last_name", "middle_name", "name_prefix", "name_suffix", "orcid_id", "phones", "country", "nationality", "home_organization", "origination_affiliation", "creation_time", "last_access_time", "valid_until", "state", "comments", "labeled_uri", "gpg_key", "time_zone", "nsf_demographics", "custom_dashboard")
    USER_MODEL_VERSION_FIELD_NUMBER: _ClassVar[int]
    AIRAVATA_INTERNAL_USER_ID_FIELD_NUMBER: _ClassVar[int]
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    GATEWAY_ID_FIELD_NUMBER: _ClassVar[int]
    EMAILS_FIELD_NUMBER: _ClassVar[int]
    FIRST_NAME_FIELD_NUMBER: _ClassVar[int]
    LAST_NAME_FIELD_NUMBER: _ClassVar[int]
    MIDDLE_NAME_FIELD_NUMBER: _ClassVar[int]
    NAME_PREFIX_FIELD_NUMBER: _ClassVar[int]
    NAME_SUFFIX_FIELD_NUMBER: _ClassVar[int]
    ORCID_ID_FIELD_NUMBER: _ClassVar[int]
    PHONES_FIELD_NUMBER: _ClassVar[int]
    COUNTRY_FIELD_NUMBER: _ClassVar[int]
    NATIONALITY_FIELD_NUMBER: _ClassVar[int]
    HOME_ORGANIZATION_FIELD_NUMBER: _ClassVar[int]
    ORIGINATION_AFFILIATION_FIELD_NUMBER: _ClassVar[int]
    CREATION_TIME_FIELD_NUMBER: _ClassVar[int]
    LAST_ACCESS_TIME_FIELD_NUMBER: _ClassVar[int]
    VALID_UNTIL_FIELD_NUMBER: _ClassVar[int]
    STATE_FIELD_NUMBER: _ClassVar[int]
    COMMENTS_FIELD_NUMBER: _ClassVar[int]
    LABELED_URI_FIELD_NUMBER: _ClassVar[int]
    GPG_KEY_FIELD_NUMBER: _ClassVar[int]
    TIME_ZONE_FIELD_NUMBER: _ClassVar[int]
    NSF_DEMOGRAPHICS_FIELD_NUMBER: _ClassVar[int]
    CUSTOM_DASHBOARD_FIELD_NUMBER: _ClassVar[int]
    user_model_version: str
    airavata_internal_user_id: str
    user_id: str
    gateway_id: str
    emails: _containers.RepeatedScalarFieldContainer[str]
    first_name: str
    last_name: str
    middle_name: str
    name_prefix: str
    name_suffix: str
    orcid_id: str
    phones: _containers.RepeatedScalarFieldContainer[str]
    country: str
    nationality: _containers.RepeatedScalarFieldContainer[str]
    home_organization: str
    origination_affiliation: str
    creation_time: int
    last_access_time: int
    valid_until: int
    state: Status
    comments: str
    labeled_uri: _containers.RepeatedScalarFieldContainer[str]
    gpg_key: str
    time_zone: str
    nsf_demographics: NSFDemographics
    custom_dashboard: CustomDashboard
    def __init__(self, user_model_version: _Optional[str] = ..., airavata_internal_user_id: _Optional[str] = ..., user_id: _Optional[str] = ..., gateway_id: _Optional[str] = ..., emails: _Optional[_Iterable[str]] = ..., first_name: _Optional[str] = ..., last_name: _Optional[str] = ..., middle_name: _Optional[str] = ..., name_prefix: _Optional[str] = ..., name_suffix: _Optional[str] = ..., orcid_id: _Optional[str] = ..., phones: _Optional[_Iterable[str]] = ..., country: _Optional[str] = ..., nationality: _Optional[_Iterable[str]] = ..., home_organization: _Optional[str] = ..., origination_affiliation: _Optional[str] = ..., creation_time: _Optional[int] = ..., last_access_time: _Optional[int] = ..., valid_until: _Optional[int] = ..., state: _Optional[_Union[Status, str]] = ..., comments: _Optional[str] = ..., labeled_uri: _Optional[_Iterable[str]] = ..., gpg_key: _Optional[str] = ..., time_zone: _Optional[str] = ..., nsf_demographics: _Optional[_Union[NSFDemographics, _Mapping]] = ..., custom_dashboard: _Optional[_Union[CustomDashboard, _Mapping]] = ...) -> None: ...
