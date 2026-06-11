"""Unit tests for airavata_sdk.helpers.sharing_resources.

The **groups** family is proto-direct: ``get_group`` / ``list_groups`` /
``create_group`` / ``update_group`` return the raw ``GroupModel`` proto wrapped
in a :class:`WithGroupAccess` envelope (proto under ``.message`` + the six
computed access booleans).  Tests assert the proto flows through untouched and
the chained flag-computing calls are made with the right arguments.

The **shared-entities** family is also proto-direct: ``get_shared_entity`` /
``get_all_shared_entity`` return a composed pydantic :class:`SharedEntity` that
carries the underlying ``UserProfile`` protos and per-group
:class:`WithGroupAccess` envelopes WHOLESALE, with ``permission_type`` as the
permission member NAME string.  Tests assert the protos / envelopes flow through
untouched and the orchestration fires the right chained calls.

Orchestration functions are tested via lightweight stub clients that record the
calls made.
"""

from airavata_sdk.generated.org.apache.airavata.model.group import (
    group_manager_pb2 as gm_pb2,
)
from airavata_sdk.generated.org.apache.airavata.model.user import (
    user_profile_pb2 as up_pb2,
)
from airavata_sdk.helpers._envelope import WithGroupAccess
from airavata_sdk.helpers.sharing_resources import (
    GroupPermission,
    SharedEntity,
    UserPermission,
    _build_group,
    _compute_revokes_and_grants,
    _group_flags,
    _member_admin_diff,
    _user_at_gateway,
    _username_from_internal_id,
    apply_sharing_update,
    compute_sharing_deltas,
    create_group,
    delete_group,
    get_all_shared_entity,
    get_group,
    get_shared_entity,
    list_groups,
    update_group,
)


# ---------------------------------------------------------------------------
# Test data
# ---------------------------------------------------------------------------

def _group(
    id="g1",
    name="Group One",
    owner_id="alice@default",
    description="A test group",
    members=None,
    admins=None,
):
    return gm_pb2.GroupModel(
        id=id,
        name=name,
        owner_id=owner_id,
        description=description,
        members=members if members is not None else ["alice@default"],
        admins=admins if admins is not None else ["alice@default"],
    )


# ---------------------------------------------------------------------------
# Stub client
# ---------------------------------------------------------------------------

class _GatewayGroups:
    def __init__(self):
        self.admins_group_id = "admins-grp"
        self.read_only_admins_group_id = "ro-admins-grp"
        self.default_gateway_users_group_id = "default-users-grp"


class _FakeSharing:
    def __init__(self, groups=None, admin_access=True):
        self._groups = groups or []
        self._admin_access = admin_access
        self.calls = []

    def gm_get_groups(self):
        self.calls.append(("gm_get_groups",))
        return list(self._groups)

    def gm_get_group(self, group_id):
        self.calls.append(("gm_get_group", group_id))
        for g in self._groups:
            if g.id == group_id:
                return g
        return self._groups[0] if self._groups else _group(id=group_id)

    def gm_has_admin_access(self, group_id, admin_id):
        self.calls.append(("gm_has_admin_access", group_id, admin_id))
        return self._admin_access

    def gm_create_group(self, group):
        self.calls.append(("gm_create_group", group))
        return "new-group-id"

    def gm_update_group(self, group):
        self.calls.append(("gm_update_group", group))

    def gm_delete_group(self, group_id, owner_id):
        self.calls.append(("gm_delete_group", group_id, owner_id))

    def gm_add_users_to_group(self, user_ids, group_id):
        self.calls.append(("gm_add_users_to_group", list(user_ids), group_id))

    def gm_remove_users_from_group(self, user_ids, group_id):
        self.calls.append(
            ("gm_remove_users_from_group", list(user_ids), group_id))

    def gm_add_group_admins(self, group_id, admin_ids):
        self.calls.append(("gm_add_group_admins", group_id, list(admin_ids)))

    def gm_remove_group_admins(self, group_id, admin_ids):
        self.calls.append(("gm_remove_group_admins", group_id, list(admin_ids)))


class _FakeCompute:
    def __init__(self):
        self.calls = 0

    def get_gateway_groups(self):
        self.calls += 1
        return _GatewayGroups()


class _FakeClient:
    def __init__(self, groups=None, admin_access=True,
                 username="alice", gateway_id="default"):
        self.sharing = _FakeSharing(groups, admin_access)
        self.compute = _FakeCompute()
        self.username = username
        self.gateway_id = gateway_id


# ---------------------------------------------------------------------------
# Context helpers
# ---------------------------------------------------------------------------

class TestContextHelpers:
    def test_user_at_gateway(self):
        c = _FakeClient(username="bob", gateway_id="gw1")
        assert _user_at_gateway(c) == "bob@gw1"

    def test_group_flags_owner_member_admin(self):
        c = _FakeClient(groups=[_group()])
        g = _group(owner_id="alice@default", members=["alice@default"])
        flags = _group_flags(c, g)
        assert flags["is_admin"] is True
        assert flags["is_owner"] is True
        assert flags["is_member"] is True
        assert flags["is_gateway_admins_group"] is False

    def test_group_flags_gateway_admins_group(self):
        c = _FakeClient(admin_access=False)
        g = _group(id="admins-grp", owner_id="someoneelse@default", members=[])
        flags = _group_flags(c, g)
        assert flags["is_admin"] is False
        assert flags["is_owner"] is False
        assert flags["is_member"] is False
        assert flags["is_gateway_admins_group"] is True

    def test_group_flags_reuses_passed_gateway_groups(self):
        c = _FakeClient()
        gg = {
            "admins_group_id": "x",
            "read_only_admins_group_id": "y",
            "default_gateway_users_group_id": "z",
        }
        _group_flags(c, _group(), gateway_groups=gg)
        # No GetGatewayGroups RPC when caller supplies the map.
        assert c.compute.calls == 0


# ---------------------------------------------------------------------------
# Read orchestration
# ---------------------------------------------------------------------------

class TestGetGroup:
    def test_returns_with_group_access(self):
        g = _group(id="g1")
        result = get_group(_FakeClient(groups=[g]), "g1")
        # proto-direct: the raw GroupModel flows through under .message
        assert isinstance(result, WithGroupAccess)
        assert result.message is g
        assert result.message.id == "g1"
        # the six computed flags are carried alongside the proto
        assert result.is_owner is True
        assert result.is_member is True
        assert result.is_admin is True
        assert result.is_gateway_admins_group is False
        assert result.is_read_only_gateway_admins_group is False
        assert result.is_default_gateway_users_group is False


class TestListGroups:
    def test_returns_list_of_with_group_access(self):
        g1 = _group(id="g1")
        g2 = _group(id="g2")
        result = list_groups(_FakeClient(groups=[g1, g2]))
        assert all(isinstance(r, WithGroupAccess) for r in result)
        assert [r.message.id for r in result] == ["g1", "g2"]
        # the protos are passed through wholesale, not copied
        assert result[0].message is g1
        assert result[1].message is g2

    def test_offset_limit_slicing(self):
        groups = [_group(id=f"g{i}") for i in range(5)]
        result = list_groups(_FakeClient(groups=groups), limit=2, offset=1)
        assert [r.message.id for r in result] == ["g1", "g2"]

    def test_gateway_groups_fetched_once(self):
        groups = [_group(id=f"g{i}") for i in range(3)]
        c = _FakeClient(groups=groups)
        list_groups(c)
        assert c.compute.calls == 1


# ---------------------------------------------------------------------------
# Write orchestration
# ---------------------------------------------------------------------------

class TestBuildGroup:
    def test_forces_owner_from_client(self):
        c = _FakeClient(username="bob", gateway_id="gw1")
        g = _build_group(c, {"name": "N", "description": "D",
                             "members": ["x@gw1"], "admins": ["x@gw1"]})
        assert g.owner_id == "bob@gw1"
        assert g.name == "N"
        assert g.description == "D"
        assert list(g.members) == ["x@gw1"]
        assert list(g.admins) == ["x@gw1"]

    def test_defaults(self):
        c = _FakeClient(username="bob", gateway_id="gw1")
        g = _build_group(c, {})
        assert g.name == ""
        assert g.description == ""
        assert list(g.members) == []
        assert list(g.admins) == []


class TestCreateGroup:
    def test_assigns_id_and_returns_tuple(self):
        c = _FakeClient()
        result, proto, added = create_group(
            c, {"name": "N", "members": ["alice@default", "bob@default"]})
        assert proto.id == "new-group-id"
        # the read shape is a WithGroupAccess wrapping the same proto
        assert isinstance(result, WithGroupAccess)
        assert result.message is proto
        assert result.message.id == "new-group-id"
        # added members exclude the owner
        assert "bob@default" in added
        assert c.sharing.calls[0][0] == "gm_create_group"


class TestMemberAdminDiff:
    def test_added_and_removed_members(self):
        existing = _group(members=["a@gw", "b@gw"], admins=[])
        diff = _member_admin_diff(existing, {"members": ["b@gw", "c@gw"]})
        assert set(diff["added_members"]) == {"c@gw"}
        assert set(diff["removed_members"]) == {"a@gw"}

    def test_admin_not_member_added_to_members(self):
        existing = _group(members=["a@gw"], admins=[])
        diff = _member_admin_diff(
            existing, {"members": ["a@gw"], "admins": ["d@gw"]})
        assert "d@gw" in diff["added_admins"]
        # admin not in members -> added to members too
        assert "d@gw" in diff["added_members"]
        assert "d@gw" in diff["members"]

    def test_absent_keys_keep_existing(self):
        existing = _group(members=["a@gw"], admins=["a@gw"])
        diff = _member_admin_diff(existing, {})
        assert set(diff["members"]) == {"a@gw"}
        assert set(diff["admins"]) == {"a@gw"}
        assert diff["added_members"] == []
        assert diff["removed_members"] == []


class TestUpdateGroup:
    def test_fires_membership_rpcs(self):
        existing = _group(id="g1", members=["a@gw"], admins=["a@gw"])
        c = _FakeClient(groups=[existing])
        result, proto, added = update_group(
            c, "g1",
            {"name": "Renamed", "members": ["a@gw", "x@gw"]})
        names = [call[0] for call in c.sharing.calls]
        assert "gm_add_users_to_group" in names
        assert "gm_update_group" in names
        assert proto.name == "Renamed"
        assert "x@gw" in added

    def test_name_and_description_patched(self):
        existing = _group(id="g1", description="old")
        c = _FakeClient(groups=[existing])
        result, proto, added = update_group(
            c, "g1", {"description": "new"})
        assert proto.description == "new"
        # the read shape wraps the patched proto (proto-direct)
        assert isinstance(result, WithGroupAccess)
        assert result.message is proto
        assert result.message.description == "new"


class TestDeleteGroup:
    def test_uses_owner_id(self):
        existing = _group(id="g1", owner_id="alice@default")
        c = _FakeClient(groups=[existing])
        delete_group(c, "g1")
        assert ("gm_delete_group", "g1", "alice@default") in c.sharing.calls


# ===========================================================================
# SharedEntity family
# ===========================================================================

def _user_profile(user_id="bob@default", first_name="Bob"):
    return up_pb2.UserProfile(
        airavata_internal_user_id=user_id,
        user_id=user_id,
        gateway_id="default",
        first_name=first_name,
    )


class TestUsernameFromInternalId:
    def test_strips_gateway(self):
        assert _username_from_internal_id("bob@default") == "bob"

    def test_rightmost_at(self):
        # rindex picks the last @, so an @ in the username is preserved.
        assert _username_from_internal_id("a@b@default") == "a@b"


class TestComputeRevokesAndGrants:
    def test_grant_read(self):
        revokes, grants = _compute_revokes_and_grants(None, "READ")
        assert revokes == set()
        assert grants == {"READ"}

    def test_grant_write_implies_read(self):
        revokes, grants = _compute_revokes_and_grants(None, "WRITE")
        assert grants == {"READ", "WRITE"}

    def test_upgrade_read_to_write(self):
        revokes, grants = _compute_revokes_and_grants("READ", "WRITE")
        assert revokes == set()
        assert grants == {"WRITE"}

    def test_downgrade_manage_to_read(self):
        revokes, grants = _compute_revokes_and_grants(
            "MANAGE_SHARING", "READ")
        assert revokes == {"WRITE", "MANAGE_SHARING"}
        assert grants == set()

    def test_revoke_all(self):
        revokes, grants = _compute_revokes_and_grants("WRITE", None)
        assert revokes == {"READ", "WRITE"}
        assert grants == set()


class TestComputeSharingDeltas:
    def test_grant_and_revoke_buckets(self):
        existing = {"bob@default": "READ"}
        new = {"bob@default": "WRITE", "carol@default": "MANAGE_SHARING"}
        deltas = compute_sharing_deltas(existing, new)
        assert "bob@default" in deltas["grant"]["WRITE"]
        assert "carol@default" in deltas["grant"]["READ"]
        assert "carol@default" in deltas["grant"]["WRITE"]
        assert "carol@default" in deltas["grant"]["MANAGE_SHARING"]
        # bob keeps READ (no revoke for him)
        assert deltas["revoke"]["READ"] == []

    def test_revoke_when_removed(self):
        existing = {"bob@default": "WRITE"}
        new = {}
        deltas = compute_sharing_deltas(existing, new)
        assert set(deltas["revoke"]["READ"]) == {"bob@default"}
        assert set(deltas["revoke"]["WRITE"]) == {"bob@default"}


# ---------------------------------------------------------------------------
# Shared-entity orchestration stubs
# ---------------------------------------------------------------------------

class _FakeSharingSE:
    """Sharing facade stub for shared-entity orchestration tests.

    *direct_users* / *direct_groups* (and the ``accessible`` variants) are
    ``{permission_name -> [id, ...]}`` maps.
    """

    def __init__(self, direct_users=None, direct_groups=None,
                 accessible_users=None, accessible_groups=None,
                 groups=None, manage_sharing=True):
        self._direct_users = direct_users or {}
        self._direct_groups = direct_groups or {}
        self._accessible_users = accessible_users or {}
        self._accessible_groups = accessible_groups or {}
        self._groups = {g.id: g for g in (groups or [])}
        self._manage_sharing = manage_sharing
        self.calls = []

    def get_all_directly_accessible_users(self, entity_id, name):
        return list(self._direct_users.get(name, []))

    def get_all_directly_accessible_groups(self, entity_id, name):
        return list(self._direct_groups.get(name, []))

    def get_all_accessible_users(self, entity_id, name):
        return list(self._accessible_users.get(name, []))

    def get_all_accessible_groups(self, entity_id, name):
        return list(self._accessible_groups.get(name, []))

    def gm_get_group(self, group_id):
        return self._groups.get(group_id, _group(id=group_id))

    def gm_has_admin_access(self, group_id, admin_id):
        return False

    def user_has_access(self, resource_id, user_id, permission_type):
        self.calls.append(
            ("user_has_access", resource_id, user_id, permission_type))
        return self._manage_sharing

    def share_resource_with_users(self, entity_id, perms):
        self.calls.append(("share_users", entity_id, perms))

    def share_resource_with_groups(self, entity_id, perms):
        self.calls.append(("share_groups", entity_id, perms))

    def revoke_sharing_of_resource_from_users(self, entity_id, perms):
        self.calls.append(("revoke_users", entity_id, perms))

    def revoke_sharing_of_resource_from_groups(self, entity_id, perms):
        self.calls.append(("revoke_groups", entity_id, perms))


class _FakeIam:
    def __init__(self, profiles=None):
        # {username -> UserProfile}
        self._profiles = profiles or {}

    def get_user_profile_by_id(self, user_id, gateway_id):
        return self._profiles.get(
            user_id, _user_profile(user_id=f"{user_id}@{gateway_id}"))


class _FakeClientSE:
    def __init__(self, sharing, iam, username="alice", gateway_id="default"):
        self.sharing = sharing
        self.iam = iam
        self.username = username
        self.gateway_id = gateway_id


_GG = {
    "admins_group_id": "x",
    "read_only_admins_group_id": "y",
    "default_gateway_users_group_id": "z",
}


class TestGetSharedEntity:
    def test_directly_accessible_tree(self):
        # owner alice; bob has WRITE; group grp1 has READ
        sharing = _FakeSharingSE(
            direct_users={
                "READ": ["bob@default", "alice@default"],
                "WRITE": ["bob@default"],
                "MANAGE_SHARING": [],
                "OWNER": ["alice@default"],
            },
            direct_groups={
                "READ": ["grp1@default"],
                "WRITE": [],
                "MANAGE_SHARING": [],
            },
            groups=[_group(id="grp1@default", name="G1")],
            manage_sharing=True,
        )
        bob = _user_profile(user_id="bob@default", first_name="Bob")
        alice = _user_profile(user_id="alice@default", first_name="Alice")
        iam = _FakeIam(profiles={"alice": alice, "bob": bob})
        c = _FakeClientSE(sharing, iam, username="alice@default")
        se = get_shared_entity(c, "ent-1", gateway_groups=_GG)

        # proto-direct composed shape: a pydantic SharedEntity
        assert isinstance(se, SharedEntity)
        assert se.entity_id == "ent-1"
        # owner removed from users list -> only bob remains
        assert len(se.user_permissions) == 1
        up = se.user_permissions[0]
        assert isinstance(up, UserPermission)
        # the raw UserProfile proto flows through wholesale (not copied)
        assert up.user is bob
        assert up.user.user_id == "bob@default"
        # permission_type is the member NAME string
        assert up.permission_type == "WRITE"
        # owner is the raw UserProfile proto, passed through
        assert se.owner is alice
        assert se.owner.user_id == "alice@default"
        # group permission carries a WithGroupAccess envelope + the NAME
        assert len(se.group_permissions) == 1
        gp = se.group_permissions[0]
        assert isinstance(gp, GroupPermission)
        assert isinstance(gp.group, WithGroupAccess)
        assert gp.group.message.id == "grp1@default"
        assert gp.permission_type == "READ"
        # entity-level flags
        assert se.is_owner is True            # owner.user_id == client.username
        assert se.has_sharing_permission is True

    def test_all_uses_accessible_accessor(self):
        sharing = _FakeSharingSE(
            accessible_users={
                "READ": ["alice@default", "bob@default"],
                "WRITE": [],
                "MANAGE_SHARING": [],
                "OWNER": ["alice@default"],
            },
            accessible_groups={"READ": [], "WRITE": [], "MANAGE_SHARING": []},
            manage_sharing=False,
        )
        iam = _FakeIam()
        c = _FakeClientSE(sharing, iam, username="alice@default")
        se = get_all_shared_entity(c, "ent-2", gateway_groups=_GG)
        assert len(se.user_permissions) == 1
        assert se.user_permissions[0].user.user_id == "bob@default"
        assert se.user_permissions[0].permission_type == "READ"
        assert se.has_sharing_permission is False


class TestApplySharingUpdate:
    def test_fires_grant_and_revoke_rpcs(self):
        sharing = _FakeSharingSE()
        iam = _FakeIam()
        c = _FakeClientSE(sharing, iam)
        apply_sharing_update(
            c, "ent-1",
            existing_user_permissions={"bob@default": "READ"},
            new_user_permissions={"bob@default": "WRITE"},   # grant
            existing_group_permissions={"grp@default": "WRITE"},
            new_group_permissions={},                        # revoke
        )
        names = [call[0] for call in sharing.calls]
        assert "share_users" in names      # WRITE granted to bob
        assert "revoke_groups" in names    # group fully revoked

    def test_noop_when_unchanged(self):
        sharing = _FakeSharingSE()
        c = _FakeClientSE(sharing, _FakeIam())
        apply_sharing_update(
            c, "ent-1",
            existing_user_permissions={"bob@default": "WRITE"},
            new_user_permissions={"bob@default": "WRITE"},
            existing_group_permissions={},
            new_group_permissions={},
        )
        assert sharing.calls == []
