"""Compute-domain helpers.

Read paths are proto-direct: ``get_gateway_resource_profile`` /
``get_group_resource_profile`` return a ``WithAccess`` (``is_owner`` always
``False``, ``user_has_write_access`` supplied by the ViewSet as *has_write*);
``get_compute_resource`` and the four ``get_*_job_submission`` helpers return the
bare proto.

The group-resource-profile WRITE builders resolve protocol/resource-type enums
with ``proto_enum_value`` (the proto enum is the source of type-truth — it accepts
the proto member NAME or the proto int the frontend sends, never a Thrift int).
Read paths render enum NAMES via ``MessageToDict``.
"""

from __future__ import annotations

from typing import TYPE_CHECKING, Optional

from airavata_sdk.helpers._envelope import WithAccess
from airavata_sdk.helpers.models import (
    GroupResourceProfileCreate,
    WorkspaceDefaultsModel,
    proto_enum_value,
)

if TYPE_CHECKING:
    from airavata_sdk.client import AiravataClient
    from airavata_sdk.generated.org.apache.airavata.model.appcatalog.computeresource import (  # noqa: E501
        compute_resource_pb2,
    )
    from airavata_sdk.generated.org.apache.airavata.model.appcatalog.gatewayprofile import (  # noqa: E501
        gateway_profile_pb2,
    )
    from airavata_sdk.generated.org.apache.airavata.model.appcatalog.groupresourceprofile import (  # noqa: E501
        group_resource_profile_pb2,
    )


def _jsp_enum():
    from airavata_sdk.generated.org.apache.airavata.model.appcatalog.computeresource import (  # noqa: E501
        compute_resource_pb2,
    )
    return compute_resource_pb2.JobSubmissionProtocol


def _dmp_enum():
    from airavata_sdk.generated.org.apache.airavata.model.data.movement import (  # noqa: E501
        data_movement_pb2,
    )
    return data_movement_pb2.DataMovementProtocol


# StoragePreference — bare proto, no envelope (a flat message with no ownership
# or sharing fields). Write path builds the proto via _build_storage_preference.


def list_gateway_storage_preferences(
    client: "AiravataClient",
    gateway_id: str,
) -> "list[gateway_profile_pb2.StoragePreference]":
    return list(client.compute.get_all_gateway_storage_preferences(gateway_id))


def get_gateway_storage_preference(
    client: "AiravataClient",
    gateway_id: str,
    storage_resource_id: str,
) -> "gateway_profile_pb2.StoragePreference":
    return client.compute.get_gateway_storage_preference(
        gateway_id, storage_resource_id)


def create_gateway_storage_preference(
    client: "AiravataClient",
    gateway_id: str,
    data: dict,
) -> "gateway_profile_pb2.StoragePreference":
    pref = _build_storage_preference(data)
    client.compute.add_gateway_storage_preference(
        gateway_id, pref.storage_resource_id, pref)
    return get_gateway_storage_preference(
        client, gateway_id, pref.storage_resource_id)


def update_gateway_storage_preference(
    client: "AiravataClient",
    gateway_id: str,
    storage_resource_id: str,
    data: dict,
) -> "gateway_profile_pb2.StoragePreference":
    """The ``storage_resource_id`` path value overrides any value in *data*."""
    merged = dict(data)
    merged["storage_resource_id"] = storage_resource_id
    pref = _build_storage_preference(merged)
    client.compute.update_gateway_storage_preference(
        gateway_id, storage_resource_id, pref)
    return get_gateway_storage_preference(
        client, gateway_id, storage_resource_id)


def delete_gateway_storage_preference(
    client: "AiravataClient",
    gateway_id: str,
    storage_resource_id: str,
) -> None:
    client.compute.delete_gateway_storage_preference(
        gateway_id, storage_resource_id)


# GatewayResourceProfile — WithAccess (gateway-catalog: no owner, so is_owner is
# always False; user_has_write_access is the gateway-admin flag the ViewSet
# supplies as has_write, not a sharing lookup).


def get_gateway_resource_profile(
    client: "AiravataClient",
    gateway_id: str,
    *,
    has_write: bool,
) -> "WithAccess":
    p = client.compute.get_gateway_resource_profile(gateway_id)
    return WithAccess(
        message=p,
        is_owner=False,
        user_has_write_access=has_write,
    )


def _build_storage_preference(data: dict):
    from airavata_sdk.generated.org.apache.airavata.model.appcatalog.gatewayprofile import (  # noqa: E501
        gateway_profile_pb2,
    )
    return gateway_profile_pb2.StoragePreference(
        storage_resource_id=data.get("storage_resource_id") or "",
        login_user_name=data.get("login_user_name") or "",
        file_system_root_location=data.get("file_system_root_location") or "",
        resource_specific_credential_store_token=data.get(
            "resource_specific_credential_store_token") or "",
    )


def _build_compute_resource_preference(data: dict):
    from airavata_sdk.generated.org.apache.airavata.model.appcatalog.gatewayprofile import (  # noqa: E501
        gateway_profile_pb2,
    )
    return gateway_profile_pb2.ComputeResourcePreference(
        compute_resource_id=data.get("compute_resource_id") or "",
        # decamelized incoming ``overridebyAiravata`` -> ``overrideby_airavata``
        override_by_airavata=bool(data.get("overrideby_airavata", False)),
        login_user_name=data.get("login_user_name") or "",
        preferred_job_submission_protocol=proto_enum_value(
            _jsp_enum(), data.get("preferred_job_submission_protocol")),
        preferred_data_movement_protocol=proto_enum_value(
            _dmp_enum(), data.get("preferred_data_movement_protocol")),
        preferred_batch_queue=data.get("preferred_batch_queue") or "",
        scratch_location=data.get("scratch_location") or "",
        allocation_project_number=data.get("allocation_project_number") or "",
        resource_specific_credential_store_token=data.get(
            "resource_specific_credential_store_token") or "",
        usage_reporting_gateway_id=data.get("usage_reporting_gateway_id") or "",
        quality_of_service=data.get("quality_of_service") or "",
        reservation=data.get("reservation") or "",
        reservation_start_time=data.get("reservation_start_time") or 0,
        reservation_end_time=data.get("reservation_end_time") or 0,
        ssh_account_provisioner=data.get("ssh_account_provisioner") or "",
        ssh_account_provisioner_config=dict(
            data.get("ssh_account_provisioner_config") or {}),
        ssh_account_provisioner_additional_info=data.get(
            "ssh_account_provisioner_additional_info") or "",
    )


def build_gateway_resource_profile(
    data: dict,
) -> "gateway_profile_pb2.GatewayResourceProfile":
    from airavata_sdk.generated.org.apache.airavata.model.appcatalog.gatewayprofile import (  # noqa: E501
        gateway_profile_pb2,
    )
    return gateway_profile_pb2.GatewayResourceProfile(
        gateway_id=data.get("gateway_id") or "",
        credential_store_token=data.get("credential_store_token") or "",
        compute_resource_preferences=[
            _build_compute_resource_preference(c)
            for c in (data.get("compute_resource_preferences") or [])
        ],
        storage_preferences=[
            _build_storage_preference(s)
            for s in (data.get("storage_preferences") or [])
        ],
        identity_server_tenant=data.get("identity_server_tenant") or "",
        identity_server_pwd_cred_token=data.get(
            "identity_server_pwd_cred_token") or "",
    )


def update_gateway_resource_profile(
    client: "AiravataClient",
    gateway_id: str,
    data: dict,
    *,
    has_write: bool,
) -> "WithAccess":
    profile = build_gateway_resource_profile(data)
    client.compute.update_gateway_resource_profile(gateway_id, profile)
    return get_gateway_resource_profile(
        client, gateway_id, has_write=has_write)


# ComputeResourceDescription — bare proto, no envelope (no ownership/sharing).


def get_compute_resource(
    client: "AiravataClient",
    compute_resource_id: str,
) -> "compute_resource_pb2.ComputeResourceDescription":
    return client.compute.get_compute_resource(compute_resource_id)


def list_compute_resource_names(client: "AiravataClient") -> dict[str, str]:
    """The ``{compute_resource_id: host_name}`` map, passed straight through."""
    return client.compute.get_all_compute_resource_names()


# GroupResourceProfile — WithAccess. The profile has no owner (is_owner always
# False). user_has_write_access is NOT a single chained sharing lookup but a
# COMPOSITE request-bound boolean (WRITE on the profile AND READ on every
# credential token), so the ViewSet computes it and passes it in as has_write.


def _grp_pb2():
    from airavata_sdk.generated.org.apache.airavata.model.appcatalog.groupresourceprofile import (  # noqa: E501
        group_resource_profile_pb2,
    )
    return group_resource_profile_pb2


# GroupResourceProfile: write builders (snake_case dict -> proto).


def _build_ssh_provisioner_config(c: dict):
    grp = _grp_pb2()
    return grp.GroupAccountSSHProvisionerConfig(
        resource_id=c.get("resource_id", "") or "",
        group_resource_profile_id=c.get("group_resource_profile_id", "") or "",
        config_name=c.get("config_name", "") or "",
        config_value=c.get("config_value", "") or "",
    )


def _build_reservation(r: dict):
    grp = _grp_pb2()
    return grp.ComputeResourceReservation(
        reservation_id=r.get("reservation_id", "") or "",
        reservation_name=r.get("reservation_name", "") or "",
        queue_names=list(r.get("queue_names", []) or []),
        start_time=r.get("start_time", 0) or 0,
        end_time=r.get("end_time", 0) or 0,
    )


def _build_slurm_pref(s: dict):
    grp = _grp_pb2()
    return grp.SlurmComputeResourcePreference(
        allocation_project_number=s.get("allocation_project_number", "") or "",
        preferred_batch_queue=s.get("preferred_batch_queue", "") or "",
        quality_of_service=s.get("quality_of_service", "") or "",
        usage_reporting_gateway_id=s.get("usage_reporting_gateway_id", "") or "",
        ssh_account_provisioner=s.get("ssh_account_provisioner", "") or "",
        group_ssh_account_provisioner_configs=[
            _build_ssh_provisioner_config(c)
            for c in (s.get("group_ssh_account_provisioner_configs", []) or [])],
        ssh_account_provisioner_additional_info=s.get(
            "ssh_account_provisioner_additional_info", "") or "",
        reservations=[
            _build_reservation(r)
            for r in (s.get("reservations", []) or [])],
    )


def _build_aws_pref(a: dict):
    grp = _grp_pb2()
    return grp.AwsComputeResourcePreference(
        region=a.get("region", "") or "",
        preferred_ami_id=a.get("preferred_ami_id", "") or "",
        preferred_instance_type=a.get("preferred_instance_type", "") or "",
    )


def _build_group_compute_resource_preference(d: dict):
    """The write payload's ``specific_preferences`` (a ``{'slurm': {...}}`` / AWS
    dict) plus a flattened ``allocation_project_number`` / ``resource_type`` are
    mapped into the proto oneof. Enums resolve via the proto enum (NAME or proto int).
    """
    grp = _grp_pb2()
    sp = d.get("specific_preferences")
    resource_type = proto_enum_value(grp.ResourceType, d.get("resource_type"))
    msg = grp.GroupComputeResourcePreference(
        compute_resource_id=d.get("compute_resource_id", "") or "",
        group_resource_profile_id=d.get("group_resource_profile_id", "") or "",
        override_by_airavata=bool(d.get("overrideby_airavata", False)),
        login_user_name=d.get("login_user_name", "") or "",
        scratch_location=d.get("scratch_location", "") or "",
        preferred_job_submission_protocol=proto_enum_value(
            _jsp_enum(), d.get("preferred_job_submission_protocol")),
        preferred_data_movement_protocol=proto_enum_value(
            _dmp_enum(), d.get("preferred_data_movement_protocol")),
        resource_specific_credential_store_token=d.get(
            "resource_specific_credential_store_token", "") or "",
        resource_type=resource_type,
    )
    # Resolve the union: prefer an explicit specific_preferences, else infer
    # from the flattened allocation_project_number / resource_type / aws fields.
    slurm_data = None
    aws_data = None
    if isinstance(sp, dict):
        if "slurm" in sp and isinstance(sp["slurm"], dict):
            slurm_data = dict(sp["slurm"])
        elif "aws" in sp and isinstance(sp["aws"], dict):
            aws_data = dict(sp["aws"])
        elif "region" in sp or "preferred_ami_id" in sp:
            aws_data = dict(sp)
        elif sp:
            slurm_data = dict(sp)
    if slurm_data is None and aws_data is None:
        proto_resource_type = grp.ResourceType
        if resource_type == proto_resource_type.AWS or "region" in d:
            aws_data = {}
        elif ("allocation_project_number" in d
                or resource_type == proto_resource_type.SLURM):
            slurm_data = {}
    if "allocation_project_number" in d and slurm_data is not None:
        slurm_data.setdefault(
            "allocation_project_number", d["allocation_project_number"])
    if slurm_data is not None:
        msg.specific_preferences.CopyFrom(grp.EnvironmentSpecificPreferences(
            slurm=_build_slurm_pref(slurm_data)))
    elif aws_data is not None:
        msg.specific_preferences.CopyFrom(grp.EnvironmentSpecificPreferences(
            aws=_build_aws_pref(aws_data)))
    return msg


def _build_compute_resource_policy(d: dict):
    grp = _grp_pb2()
    return grp.ComputeResourcePolicy(
        resource_policy_id=d.get("resource_policy_id", "") or "",
        compute_resource_id=d.get("compute_resource_id", "") or "",
        group_resource_profile_id=d.get("group_resource_profile_id", "") or "",
        allowed_batch_queues=list(d.get("allowed_batch_queues", []) or []),
    )


def _build_batch_queue_resource_policy(d: dict):
    grp = _grp_pb2()
    return grp.BatchQueueResourcePolicy(
        resource_policy_id=d.get("resource_policy_id", "") or "",
        compute_resource_id=d.get("compute_resource_id", "") or "",
        group_resource_profile_id=d.get("group_resource_profile_id", "") or "",
        queuename=d.get("queuename", "") or "",
        max_allowed_nodes=d.get("max_allowed_nodes", 0) or 0,
        max_allowed_cores=d.get("max_allowed_cores", 0) or 0,
        max_allowed_walltime=d.get("max_allowed_walltime", 0) or 0,
    )


def build_group_resource_profile(
    client: "AiravataClient",
    data: dict,
) -> "group_resource_profile_pb2.GroupResourceProfile":
    """``gateway_id`` is forced from the client context."""
    grp = _grp_pb2()
    return grp.GroupResourceProfile(
        gateway_id=client.gateway_id or "",
        group_resource_profile_id=data.get(
            "group_resource_profile_id", "") or "",
        group_resource_profile_name=data.get(
            "group_resource_profile_name", "") or "",
        compute_preferences=[
            _build_group_compute_resource_preference(p)
            for p in (data.get("compute_preferences", []) or [])],
        compute_resource_policies=[
            _build_compute_resource_policy(p)
            for p in (data.get("compute_resource_policies", []) or [])],
        batch_queue_resource_policies=[
            _build_batch_queue_resource_policy(p)
            for p in (data.get("batch_queue_resource_policies", []) or [])],
        default_credential_store_token=data.get(
            "default_credential_store_token", "") or "",
    )


def list_group_resource_profiles(
    client: "AiravataClient",
    *,
    has_write_by_id: Optional[dict] = None,
) -> "list[WithAccess]":
    """*has_write_by_id* maps ``group_resource_profile_id`` -> the composite
    write flag the ViewSet resolved; ids absent from the map default to ``False``.
    """
    by_id = has_write_by_id or {}
    profiles = client.compute.get_group_resource_list()
    return [
        WithAccess(
            message=g,
            is_owner=False,
            user_has_write_access=bool(
                by_id.get(g.group_resource_profile_id, False)),
        )
        for g in profiles
    ]


def get_group_resource_profile(
    client: "AiravataClient",
    group_resource_profile_id: str,
    *,
    has_write: bool,
) -> "WithAccess":
    g = client.compute.get_group_resource_profile(group_resource_profile_id)
    return WithAccess(
        message=g,
        is_owner=False,
        user_has_write_access=has_write,
    )


def create_group_resource_profile(
    client: "AiravataClient",
    data: "GroupResourceProfileCreate | dict",
    *,
    has_write: bool = True,
) -> "WithAccess":
    """The server mints the id; the creator can write, so *has_write* defaults
    to ``True``.
    """
    data = GroupResourceProfileCreate.model_validate(data).model_dump(
        exclude_unset=True)
    grp = build_group_resource_profile(client, data)
    created = client.compute.create_group_resource_profile(grp)
    return WithAccess(
        message=created,
        is_owner=False,
        user_has_write_access=has_write,
    )


def update_group_resource_profile(
    client: "AiravataClient",
    group_resource_profile_id: str,
    data: "GroupResourceProfileCreate | dict",
    *,
    has_write: bool = True,
) -> "WithAccess":
    """Orphaned compute prefs / policies no longer present are removed
    individually before ``UpdateGroupResourceProfile``. The path id is
    authoritative; the updated proto is re-fetched (the update RPC returns Empty).
    """
    data = GroupResourceProfileCreate.model_validate(data).model_dump(
        exclude_unset=True)
    grp = build_group_resource_profile(client, data)
    grp.group_resource_profile_id = group_resource_profile_id
    original = client.compute.get_group_resource_profile(
        group_resource_profile_id)
    # Remove compute prefs / policies that are no longer present.
    new_pref_ids = {
        cp.compute_resource_id for cp in grp.compute_preferences}
    for cp in original.compute_preferences:
        if cp.compute_resource_id not in new_pref_ids:
            client.compute.remove_group_compute_prefs(
                cp.group_resource_profile_id, cp.compute_resource_id)
    new_policy_ids = {
        p.resource_policy_id for p in grp.compute_resource_policies}
    for p in original.compute_resource_policies:
        if p.resource_policy_id and p.resource_policy_id not in new_policy_ids:
            client.compute.remove_group_compute_resource_policy(
                p.resource_policy_id)
    new_bq_ids = {
        p.resource_policy_id for p in grp.batch_queue_resource_policies}
    for p in original.batch_queue_resource_policies:
        if p.resource_policy_id and p.resource_policy_id not in new_bq_ids:
            client.compute.remove_group_batch_queue_resource_policy(
                p.resource_policy_id)
    client.compute.update_group_resource_profile(
        group_resource_profile_id, grp)
    return get_group_resource_profile(
        client, group_resource_profile_id, has_write=has_write)


def delete_group_resource_profile(
    client: "AiravataClient",
    group_resource_profile_id: str,
) -> None:
    client.compute.remove_group_resource_profile(group_resource_profile_id)


# Per-protocol job submission (Local / SSH / Unicore / Cloud) — bare proto.


def get_local_job_submission(
    client: "AiravataClient",
    submission_id: str,
):
    return client.compute.get_local_job_submission(submission_id)


def get_ssh_job_submission(
    client: "AiravataClient",
    submission_id: str,
):
    return client.compute.get_ssh_job_submission(submission_id)


def get_unicore_job_submission(
    client: "AiravataClient",
    submission_id: str,
):
    return client.compute.get_unicore_job_submission(submission_id)


def get_cloud_job_submission(
    client: "AiravataClient",
    submission_id: str,
):
    return client.compute.get_cloud_job_submission(submission_id)


# Workspace defaults: the "talk to Airavata" grunt the portal
# WorkspacePreferencesHelper did inline — resolve sensible defaults for a fresh
# record and validate a stored record against the server. Return plain
# scalars/lists (no contract dict); the ORM read/write stays in the portal.


def user_can_write(client: "AiravataClient", resource_id: str) -> bool:
    return client.sharing.user_has_access(
        resource_id=resource_id,
        user_id=client.username,
        permission_type="WRITE",
    )


def most_recent_writeable_project_id(
    client: "AiravataClient",
) -> Optional[str]:
    """Id of the caller's first WRITE-accessible project (newest first), or None."""
    projects = client.research.get_user_projects(
        gateway_id=client.gateway_id,
        user_name=client.username,
        limit=-1,
        offset=0,
    )
    for project in projects:
        if user_can_write(client, project.project_id):
            return project.project_id
    return None


def accessible_group_resource_profile_ids(
    client: "AiravataClient",
) -> list[str]:
    profiles = client.compute.get_group_resource_list()
    return [g.group_resource_profile_id for g in profiles]


def resolve_workspace_defaults(client: "AiravataClient") -> dict:
    """Server-derived defaults the portal persists into its own ORM record:
    first WRITE-accessible project id, all accessible group-resource-profile ids
    (server order), and the first of those.
    """
    grp_ids = accessible_group_resource_profile_ids(client)
    return WorkspaceDefaultsModel.model_validate({
        "most_recent_project_id": most_recent_writeable_project_id(client),
        "group_resource_profile_ids": grp_ids,
        "most_recent_group_resource_profile_id": grp_ids[0] if grp_ids else None,
    }).model_dump()
