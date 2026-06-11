"""Write-path input models: permissive pydantic shapes naming the writable
fields the ``create_*`` / ``update_*`` helpers accept, so a caller can hand in a
plain snake_case dict and have it validated before it is assembled into a proto.

Enum fields are ``int | str | None`` (member NAME string or historical Thrift
int); timestamp fields are ``int | str | None`` (epoch millis or ISO string).
"""

from __future__ import annotations

from typing import Optional, Union

from pydantic import BaseModel, ConfigDict


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
    user_configuration_data: Optional[dict] = None


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
