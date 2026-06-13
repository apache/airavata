"""Write-path input models: permissive pydantic shapes naming the writable
fields the ``create_*`` / ``update_*`` helpers accept, so a caller can hand in a
plain snake_case dict and have it validated before it is assembled into a proto.

Enum fields are ``int | str | None`` (member NAME string or historical Thrift
int); timestamp fields are ``int | str | None`` (epoch millis or ISO string).
"""

from __future__ import annotations

from typing import Annotated, Optional, Union

from pydantic import BaseModel, BeforeValidator, ConfigDict


def _blank_to_none(value):
    """Map an empty-string form value to ``None`` so an ``int`` field doesn't
    choke on it (HTML number inputs can submit ``""`` for a cleared field)."""
    return None if value == "" else value


# An int field that mirrors a proto int32/int64: pydantic coerces a numeric
# string ("1") to int at the typed boundary; "" -> None (left unset -> proto 0).
_OptInt = Annotated[Optional[int], BeforeValidator(_blank_to_none)]


def proto_enum_value(enum_cls, value) -> int:
    """Resolve a wire value to a proto enum's INT value, with the PROTO enum as
    the single source of type-truth — there is NO Thrift remapping.

    Accepts the proto member NAME (full proto name, e.g. ``"EXPERIMENT_STATE_CREATED"``,
    or the prefix-stripped short alias, e.g. ``"LOCAL"``) or the proto int as-is;
    ``None`` / ``""`` / bool -> 0 (the proto UNKNOWN sentinel). *enum_cls* is a
    protobuf ``EnumTypeWrapper`` (e.g. ``compute_resource_pb2.JobSubmissionProtocol``).
    """
    if value is None or value == "" or isinstance(value, bool):
        return 0
    if isinstance(value, int):
        return value  # already the proto int — pass through, never remap
    name = str(value)
    if name in enum_cls.keys():
        return enum_cls.Value(name)
    # short alias: re-attach the enum's SCREAMING_SNAKE prefix, derived from the
    # 0-sentinel member name (e.g. JOB_SUBMISSION_PROTOCOL_UNKNOWN -> prefix).
    zero = enum_cls.DESCRIPTOR.values_by_number.get(0)
    if zero is not None and "_" in zero.name:
        prefix = zero.name[: zero.name.rfind("_") + 1]
        if (prefix + name) in enum_cls.keys():
            return enum_cls.Value(prefix + name)
    return 0


class _Base(BaseModel):
    # extra='allow' so an unexpected key round-trips untouched.
    model_config = ConfigDict(extra="allow", populate_by_name=True)


class WorkspaceDefaultsModel(_Base):
    most_recent_project_id: Optional[str] = None
    group_resource_profile_ids: list[str] = []
    most_recent_group_resource_profile_id: Optional[str] = None


class ProjectCreate(_Base):
    name: Optional[str] = None
    description: Optional[str] = None


class ComputationalResourceSchedulingCreate(_Base):
    # int fields mirror the proto int32s; the typed boundary coerces "1" -> 1.
    resource_host_id: Optional[str] = None
    total_cpu_count: _OptInt = None
    node_count: _OptInt = None
    number_of_threads: _OptInt = None
    queue_name: Optional[str] = None
    wall_time_limit: _OptInt = None
    total_physical_memory: _OptInt = None
    chessis_number: Optional[str] = None
    static_working_dir: Optional[str] = None
    override_login_user_name: Optional[str] = None
    override_scratch_location: Optional[str] = None
    override_allocation_project_number: Optional[str] = None
    m_group_count: _OptInt = None


class UserConfigurationDataCreate(_Base):
    airavata_auto_schedule: Optional[bool] = None
    override_manual_scheduled_params: Optional[bool] = None
    share_experiment_publicly: Optional[bool] = None
    computational_resource_scheduling: Optional[
        ComputationalResourceSchedulingCreate] = None
    throttle_resources: Optional[bool] = None
    input_storage_resource_id: Optional[str] = None
    output_storage_resource_id: Optional[str] = None
    experiment_data_dir: Optional[str] = None
    use_user_cr_pref: Optional[bool] = None
    group_resource_profile_id: Optional[str] = None


class ExperimentCreate(_Base):
    experiment_id: Optional[str] = None
    project_id: Optional[str] = None
    experiment_type: Union[int, str, None] = None
    experiment_name: Optional[str] = None
    description: Optional[str] = None
    execution_id: Optional[str] = None
    enable_email_notification: Optional[bool] = None
    email_addresses: Optional[list[str]] = None
    experiment_inputs: Optional[list[dict]] = None
    experiment_outputs: Optional[list[dict]] = None
    user_configuration_data: Optional[UserConfigurationDataCreate] = None


class NotificationCreate(_Base):
    title: Optional[str] = None
    notification_message: Optional[str] = None
    creation_time: Union[int, str, None] = None
    published_time: Union[int, str, None] = None
    expiration_time: Union[int, str, None] = None
    priority: Union[int, str, None] = None


class ParserCreate(_Base):
    id: Optional[str] = None
    image_name: Optional[str] = None
    output_dir_path: Optional[str] = None
    input_dir_path: Optional[str] = None
    execution_command: Optional[str] = None
    input_files: Optional[list[dict]] = None
    output_files: Optional[list[dict]] = None


class ApplicationInterfaceCreate(_Base):
    application_interface_id: Optional[str] = None
    application_name: Optional[str] = None
    application_description: Optional[str] = None
    application_modules: Optional[list[str]] = None
    application_inputs: Optional[list[dict]] = None
    application_outputs: Optional[list[dict]] = None
    archive_working_directory: Optional[bool] = None


class ApplicationDeploymentCreate(_Base):
    app_module_id: Optional[str] = None
    compute_host_id: Optional[str] = None
    executable_path: Optional[str] = None
    parallelism: Union[int, str, None] = None
    app_deployment_description: Optional[str] = None
    module_load_cmds: Optional[list[dict]] = None
    lib_prepend_paths: Optional[list[dict]] = None
    lib_append_paths: Optional[list[dict]] = None
    set_environment: Optional[list[dict]] = None
    pre_job_commands: Optional[list[dict]] = None
    post_job_commands: Optional[list[dict]] = None
    default_queue_name: Optional[str] = None
    default_node_count: Optional[int] = None
    default_cpu_count: Optional[int] = None
    default_walltime: Optional[int] = None
    editable_by_user: Optional[bool] = None


class GroupResourceProfileCreate(_Base):
    group_resource_profile_id: Optional[str] = None
    group_resource_profile_name: Optional[str] = None
    compute_preferences: Optional[list[dict]] = None
    compute_resource_policies: Optional[list[dict]] = None
    batch_queue_resource_policies: Optional[list[dict]] = None
