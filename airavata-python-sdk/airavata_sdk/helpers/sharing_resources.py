"""Sharing-domain helpers, two families:

* **groups** (``GroupViewSet``) — the ``GroupModel`` proto wrapped in a
  :class:`~airavata_sdk.helpers._envelope.WithGroupAccess` carrying the six
  cross-context booleans (:func:`_group_flags`) the proto cannot compute on its
  own without coupling GroupManager to the gateway-groups catalog.
* **shared-entities** (``SharedEntityViewSet``) — a composed multi-proto shape
  (owner / per-user ``UserProfile`` protos + per-group ``WithGroupAccess``
  envelopes), returned as a pydantic :class:`SharedEntity` carrying them
  wholesale. ``permission_type`` is the permission member NAME string.
"""

from __future__ import annotations

from typing import TYPE_CHECKING, Any, Optional

from pydantic import BaseModel, ConfigDict

from airavata_sdk.helpers._envelope import WithGroupAccess

if TYPE_CHECKING:
    from airavata_sdk.client import AiravataClient


def _user_at_gateway(client: "AiravataClient") -> str:
    return f"{client.username}@{client.gateway_id}"


def _gateway_groups(client: "AiravataClient") -> dict:
    """The three gateway-group identity ids (``admins_group_id`` /
    ``read_only_admins_group_id`` / ``default_gateway_users_group_id``).
    """
    gg = client.compute.get_gateway_groups()
    return {
        "admins_group_id": gg.admins_group_id,
        "read_only_admins_group_id": gg.read_only_admins_group_id,
        "default_gateway_users_group_id": gg.default_gateway_users_group_id,
    }


def _group_flags(
    client: "AiravataClient",
    g,
    *,
    gateway_groups: Optional[dict] = None,
) -> dict:
    """The six per-group access booleans. *gateway_groups* may be supplied by the
    caller (a session-cached copy) to avoid a redundant ``GetGatewayGroups`` RPC.
    """
    me = _user_at_gateway(client)
    gg = gateway_groups if gateway_groups is not None else _gateway_groups(client)
    return dict(
        is_admin=client.sharing.gm_has_admin_access(g.id, me),
        is_owner=(g.owner_id == me),
        is_member=bool(g.members) and me in g.members,
        is_gateway_admins_group=(g.id == gg["admins_group_id"]),
        is_read_only_gateway_admins_group=(
            g.id == gg["read_only_admins_group_id"]),
        is_default_gateway_users_group=(
            g.id == gg["default_gateway_users_group_id"]),
    )


def get_group(
    client: "AiravataClient",
    group_id: str,
    *,
    gateway_groups: Optional[dict] = None,
) -> "WithGroupAccess":
    g = client.sharing.gm_get_group(group_id)
    return WithGroupAccess(
        message=g,
        **_group_flags(client, g, gateway_groups=gateway_groups),
    )


def wrap_groups(
    client: "AiravataClient",
    group_protos,
    *,
    gateway_groups: Optional[dict] = None,
) -> "list[WithGroupAccess]":
    """Wrap already-fetched ``GroupModel`` protos in ``WithGroupAccess`` (e.g. the
    groups a user belongs to). *gateway_groups* is resolved once and reused."""
    groups = list(group_protos)
    gg = gateway_groups if gateway_groups is not None else _gateway_groups(client)
    return [
        WithGroupAccess(message=g, **_group_flags(client, g, gateway_groups=gg))
        for g in groups
    ]


def list_groups(
    client: "AiravataClient",
    *,
    limit: int = -1,
    offset: int = 0,
    gateway_groups: Optional[dict] = None,
) -> "list[WithGroupAccess]":
    """``GetGroups`` returns the full list; *limit* / *offset* slice it
    in-process. *gateway_groups* is resolved once and reused across the page.
    """
    groups = list(client.sharing.gm_get_groups())
    end = offset + limit if limit > 0 else len(groups)
    page = groups[offset:end] if groups else []
    gg = gateway_groups if gateway_groups is not None else _gateway_groups(client)
    return [
        WithGroupAccess(message=g, **_group_flags(client, g, gateway_groups=gg))
        for g in page
    ]


def _group_manager_pb2():
    from airavata_sdk.generated.org.apache.airavata.model.group import (
        group_manager_pb2,
    )
    return group_manager_pb2


def _build_group(client: "AiravataClient", data: dict):
    """``owner_id`` is forced from the client context (``user@gateway``)."""
    return _group_manager_pb2().GroupModel(
        name=data.get("name") or "",
        description=data.get("description") or "",
        members=list(data.get("members") or []),
        admins=list(data.get("admins") or []),
        owner_id=_user_at_gateway(client),
    )


def _member_admin_diff(existing, data: dict) -> dict:
    """Added/removed member & admin id lists. New lists default to the existing
    proto values when absent from *data*. Admins not already members are added to
    both the member list and ``added_members``.
    """
    old_members = set(existing.members)
    new_members = set(data["members"]) if "members" in data else set(existing.members)
    removed_members = list(old_members - new_members)
    added_members = list(new_members - old_members)

    old_admins = set(existing.admins)
    new_admins = set(data["admins"]) if "admins" in data else set(existing.admins)
    removed_admins = list(old_admins - new_admins)
    added_admins = list(new_admins - old_admins)

    final_members = list(new_members)
    # Admins not yet members become members too.
    extra = list(new_admins - new_members)
    added_members = added_members + extra
    final_members = final_members + extra

    return dict(
        members=final_members,
        admins=list(new_admins),
        added_members=added_members,
        removed_members=removed_members,
        added_admins=added_admins,
        removed_admins=removed_admins,
    )


def create_group(
    client: "AiravataClient",
    data: dict,
    *,
    gateway_groups: Optional[dict] = None,
) -> tuple:
    """Returns ``(WithGroupAccess, group_proto, added_member_ids)``: the read
    shape plus the raw proto + newly added members the ViewSet needs to fan out
    ``user_added_to_group`` notifications (which stay in the portal).
    """
    group = _build_group(client, data)
    group_id = client.sharing.gm_create_group(group)
    group.id = group_id
    added_members = list(set(group.members) - {group.owner_id})
    result = WithGroupAccess(
        message=group,
        **_group_flags(client, group, gateway_groups=gateway_groups))
    return result, group, added_members


def update_group(
    client: "AiravataClient",
    group_id: str,
    data: dict,
    *,
    gateway_groups: Optional[dict] = None,
) -> tuple:
    """Patch ``name`` / ``description``, fire the membership / admin mutator RPCs
    for each non-empty diff, then ``UpdateGroup``. Returns ``(WithGroupAccess,
    group_proto, added_member_ids)`` (see :func:`create_group`).
    """
    group = client.sharing.gm_get_group(group_id)
    if "name" in data:
        group.name = data["name"] or ""
    if "description" in data:
        group.description = data["description"] or ""

    diff = _member_admin_diff(group, data)

    sharing = client.sharing
    if diff["added_members"]:
        sharing.gm_add_users_to_group(diff["added_members"], group.id)
    if diff["removed_members"]:
        sharing.gm_remove_users_from_group(diff["removed_members"], group.id)
    if diff["added_admins"]:
        sharing.gm_add_group_admins(group.id, diff["added_admins"])
    if diff["removed_admins"]:
        sharing.gm_remove_group_admins(group.id, diff["removed_admins"])

    group.members[:] = diff["members"]
    group.admins[:] = diff["admins"]
    sharing.gm_update_group(group)

    result = WithGroupAccess(
        message=group,
        **_group_flags(client, group, gateway_groups=gateway_groups))
    return result, group, diff["added_members"]


def delete_group(client: "AiravataClient", group_id: str) -> None:
    """The proto is fetched to recover the ``owner_id`` the delete RPC requires."""
    group = client.sharing.gm_get_group(group_id)
    client.sharing.gm_delete_group(group.id, group.owner_id)


# SharedEntity — a composed multi-proto shape (owner / per-user UserProfile
# protos + per-group WithGroupAccess envelopes from two services), returned as a
# pydantic model carrying them wholesale. permission_type is the member NAME
# string. is_owner = owner.user_id == client.username; has_sharing_permission is
# a chained MANAGE_SHARING lookup.

# Precedence: a later (more privileged) grant overwrites an earlier one, so WRITE
# (which implies READ) wins over a bare READ.
_PERMISSION_PRECEDENCE = ("READ", "WRITE", "MANAGE_SHARING")


class UserPermission(BaseModel):
    model_config = ConfigDict(arbitrary_types_allowed=True)

    user: Any  # raw UserProfile proto
    permission_type: str


class GroupPermission(BaseModel):
    model_config = ConfigDict(arbitrary_types_allowed=True)

    group: Any  # WithGroupAccess envelope
    permission_type: str


class SharedEntity(BaseModel):
    model_config = ConfigDict(arbitrary_types_allowed=True)

    entity_id: str
    owner: Any
    user_permissions: list[UserPermission]
    group_permissions: list[GroupPermission]
    is_owner: bool
    has_sharing_permission: bool


def _username_from_internal_id(internal_user_id: str) -> str:
    """The accessible-users RPCs return ``user@gateway`` ids but
    ``GetUserProfileById`` keys on the bare username.
    """
    return internal_user_id[0:internal_user_id.rindex("@")]


def _collect_permissions(accessor, entity_id: str) -> dict:
    """``{id -> permission_name}`` across the precedence order (most privileged
    grant wins). *accessor* is ``(entity_id, permission_name) -> list[id]``.
    """
    result: dict = {}
    for name in _PERMISSION_PRECEDENCE:
        for resource_id in accessor(entity_id, name):
            result[resource_id] = name
    return result


def _load_shared_entity(
    client: "AiravataClient",
    entity_id: str,
    *,
    directly: bool,
    gateway_groups: Optional[dict] = None,
) -> "SharedEntity":
    """*directly* selects the direct-only (True) vs. accessible-including-inherited
    (False) accessor pair. Loads the user/group permission maps, drops the owner
    from the users list, fetches every profile/group, and composes a
    :class:`SharedEntity`.
    """
    sharing = client.sharing
    if directly:
        users_accessor = sharing.get_all_directly_accessible_users
        groups_accessor = sharing.get_all_directly_accessible_groups
    else:
        users_accessor = sharing.get_all_accessible_users
        groups_accessor = sharing.get_all_accessible_groups

    users = _collect_permissions(users_accessor, entity_id)
    # The owner is the single DIRECT owner (the OWNER grant); there is exactly
    # one (indirect cascading owners are not returned by these RPCs).
    owner_ids = users_accessor(entity_id, "OWNER")
    owner_id = list(owner_ids)[0]
    users.pop(owner_id, None)

    groups = _collect_permissions(groups_accessor, entity_id)

    gg = (
        gateway_groups if gateway_groups is not None
        else _gateway_groups(client))

    def _load_profile(user_internal_id):
        return client.iam.get_user_profile_by_id(
            _username_from_internal_id(user_internal_id), client.gateway_id)

    user_permissions = [
        UserPermission(user=_load_profile(uid), permission_type=users[uid])
        for uid in users
    ]
    group_permissions = [
        GroupPermission(
            group=get_group(client, gid, gateway_groups=gg),
            permission_type=groups[gid])
        for gid in groups
    ]
    owner_profile = _load_profile(owner_id)

    return SharedEntity(
        entity_id=entity_id,
        owner=owner_profile,
        user_permissions=user_permissions,
        group_permissions=group_permissions,
        is_owner=(owner_profile.user_id == client.username),
        has_sharing_permission=client.sharing.user_has_access(
            resource_id=entity_id,
            user_id=client.username,
            permission_type="MANAGE_SHARING",
        ),
    )


def get_shared_entity(
    client: "AiravataClient",
    entity_id: str,
    *,
    gateway_groups: Optional[dict] = None,
) -> "SharedEntity":
    """Only directly granted permissions (the only ones the portal UI can edit)."""
    return _load_shared_entity(
        client, entity_id, directly=True, gateway_groups=gateway_groups)


def get_all_shared_entity(
    client: "AiravataClient",
    entity_id: str,
    *,
    gateway_groups: Optional[dict] = None,
) -> "SharedEntity":
    """All (direct + inherited) sharing settings."""
    return _load_shared_entity(
        client, entity_id, directly=False, gateway_groups=gateway_groups)


# Implied-permission sets for grant/revoke deltas: WRITE implies READ;
# MANAGE_SHARING implies READ + WRITE.
_IMPLIED_PERMISSIONS = {
    "READ": {"READ"},
    "WRITE": {"READ", "WRITE"},
    "MANAGE_SHARING": {"READ", "WRITE", "MANAGE_SHARING"},
}


def _compute_revokes_and_grants(current_name, new_name) -> tuple:
    """``(revokes, grants)`` permission-NAME sets for one id: diff the
    implied-permission sets of *current_name* / *new_name* (``None`` -> empty).
    """
    current = _IMPLIED_PERMISSIONS.get(current_name, set()) \
        if current_name is not None else set()
    new = _IMPLIED_PERMISSIONS.get(new_name, set()) \
        if new_name is not None else set()
    return (current - new, new - current)


def compute_sharing_deltas(existing: dict, new: dict) -> dict:
    """*existing* / *new* are ``{id -> permission_name}`` maps. Returns
    ``{"grant": {perm_name: [ids]}, "revoke": {...}}``.
    """
    grant = {"READ": [], "WRITE": [], "MANAGE_SHARING": []}
    revoke = {"READ": [], "WRITE": [], "MANAGE_SHARING": []}
    all_ids = set(existing.keys()) | set(new.keys())
    for resource_id in all_ids:
        revokes, grants = _compute_revokes_and_grants(
            existing.get(resource_id), new.get(resource_id))
        for name in revokes:
            revoke[name].append(resource_id)
        for name in grants:
            grant[name].append(resource_id)
    return {"grant": grant, "revoke": revoke}


def apply_sharing_update(
    client: "AiravataClient",
    entity_id: str,
    *,
    existing_user_permissions: dict,
    new_user_permissions: dict,
    existing_group_permissions: dict,
    new_group_permissions: dict,
) -> None:
    """Fire ``share_resource_with_*`` / ``revoke_sharing_of_resource_from_*`` for
    each non-empty delta bucket. The ``*_permissions`` args are
    ``{id -> permission_name}`` maps the caller assembles.
    """
    sharing = client.sharing

    user_deltas = compute_sharing_deltas(
        existing_user_permissions, new_user_permissions)
    for name, ids in user_deltas["grant"].items():
        if ids:
            sharing.share_resource_with_users(
                entity_id, {uid: name for uid in ids})
    for name, ids in user_deltas["revoke"].items():
        if ids:
            sharing.revoke_sharing_of_resource_from_users(
                entity_id, {uid: name for uid in ids})

    group_deltas = compute_sharing_deltas(
        existing_group_permissions, new_group_permissions)
    for name, ids in group_deltas["grant"].items():
        if ids:
            sharing.share_resource_with_groups(
                entity_id, {gid: name for gid in ids})
    for name, ids in group_deltas["revoke"].items():
        if ids:
            sharing.revoke_sharing_of_resource_from_groups(
                entity_id, {gid: name for gid in ids})
