"""Unit tests for airavata_sdk.helpers.compute_resources.

The gateway-resource-profile family is proto-direct: ``get_gateway_resource_profile``
returns a ``WithAccess[GatewayResourceProfile]`` (the raw proto wrapped with the
caller's access flags), so its tests assert the wrapper carries the proto
verbatim plus ``is_owner`` / ``user_has_write_access``.  The standalone
gateway-storage-preference family is proto-direct too, but BARE: it has no
ownership / sharing fields, so ``get`` returns one ``StoragePreference`` proto
and ``list`` returns ``list[proto]`` (no envelope).  The still-legacy families
(the compute-resource / group-resource-profile / job-submission ``_x_dict``
transforms) keep their proto → neutral-dict tests.

Orchestration functions (``get_gateway_resource_profile``,
``update_gateway_resource_profile``) are tested via a lightweight stub client
that records the calls made.
"""

from airavata_sdk.helpers._envelope import WithAccess
from airavata_sdk.helpers.compute_resources import (
    _data_movement_protocol_int,
    _job_submission_protocol_int,
    build_gateway_resource_profile,
    build_group_resource_profile,
    create_gateway_storage_preference,
    create_group_resource_profile,
    delete_gateway_storage_preference,
    delete_group_resource_profile,
    get_cloud_job_submission,
    get_compute_resource,
    get_gateway_resource_profile,
    get_gateway_storage_preference,
    get_group_resource_profile,
    get_local_job_submission,
    get_ssh_job_submission,
    get_unicore_job_submission,
    list_compute_resource_names,
    list_gateway_storage_preferences,
    list_group_resource_profiles,
    update_gateway_resource_profile,
    update_gateway_storage_preference,
    update_group_resource_profile,
)


def _gp_pb2():
    from airavata_sdk.generated.org.apache.airavata.model.appcatalog.gatewayprofile import (  # noqa: E501
        gateway_profile_pb2,
    )
    return gateway_profile_pb2


def _make_storage_preference(**kwargs):
    return _gp_pb2().StoragePreference(**kwargs)


def _make_compute_preference(**kwargs):
    return _gp_pb2().ComputeResourcePreference(**kwargs)


def _make_profile(**kwargs):
    return _gp_pb2().GatewayResourceProfile(**kwargs)


# ---------------------------------------------------------------------------
# Protocol enum bridges
# ---------------------------------------------------------------------------

class TestProtocolEnumBridges:
    def test_job_submission_protocol_proto_to_thrift(self):
        # proto: 0 UNKNOWN, 1 LOCAL, 2 SSH, 3 GLOBUS, 4 UNICORE, 5 JSP_CLOUD,
        # 6 SSH_FORK, 7 LOCAL_FORK
        assert _job_submission_protocol_int(0) is None
        assert _job_submission_protocol_int(1) == 0   # LOCAL
        assert _job_submission_protocol_int(2) == 1   # SSH
        assert _job_submission_protocol_int(3) == 2   # GLOBUS
        assert _job_submission_protocol_int(4) == 3   # UNICORE
        assert _job_submission_protocol_int(5) == 4   # JSP_CLOUD -> CLOUD
        assert _job_submission_protocol_int(6) == 5   # SSH_FORK
        assert _job_submission_protocol_int(7) == 6   # LOCAL_FORK

    def test_data_movement_protocol_proto_to_thrift(self):
        # proto: 0 UNKNOWN, 1 LOCAL, 2 SCP, 3 SFTP, 4 GRID_FTP, 5 UNICORE_SS
        assert _data_movement_protocol_int(0) is None
        assert _data_movement_protocol_int(1) == 0    # LOCAL
        assert _data_movement_protocol_int(2) == 1    # SCP
        assert _data_movement_protocol_int(3) == 2    # SFTP
        assert _data_movement_protocol_int(4) is None  # GRID_FTP (proto-only)
        assert _data_movement_protocol_int(5) == 4    # UNICORE_STORAGE_SERVICE


# ---------------------------------------------------------------------------
# build_gateway_resource_profile (write path)
# ---------------------------------------------------------------------------

class TestBuildGatewayResourceProfile:
    def test_scalar_fields(self):
        pb = build_gateway_resource_profile({
            "gateway_id": "default",
            "credential_store_token": "cred",
            "identity_server_tenant": "tenant",
            "identity_server_pwd_cred_token": "pwd",
        })
        assert pb.gateway_id == "default"
        assert pb.credential_store_token == "cred"
        assert pb.identity_server_tenant == "tenant"
        assert pb.identity_server_pwd_cred_token == "pwd"

    def test_protocol_thrift_int_round_trip(self):
        # Thrift SSH=1 -> proto SSH=2; Thrift SFTP=2 -> proto SFTP=3
        pb = build_gateway_resource_profile({
            "compute_resource_preferences": [{
                "compute_resource_id": "cr-1",
                "preferred_job_submission_protocol": 1,
                "preferred_data_movement_protocol": 2,
            }],
        })
        c = pb.compute_resource_preferences[0]
        assert c.preferred_job_submission_protocol == 2   # proto SSH
        assert c.preferred_data_movement_protocol == 3    # proto SFTP

    def test_nested_storage_preference(self):
        pb = build_gateway_resource_profile({
            "storage_preferences": [{
                "storage_resource_id": "store-1",
                "login_user_name": "alice",
            }],
        })
        assert pb.storage_preferences[0].storage_resource_id == "store-1"
        assert pb.storage_preferences[0].login_user_name == "alice"

    def test_empty_lists(self):
        pb = build_gateway_resource_profile({"gateway_id": "default"})
        assert list(pb.compute_resource_preferences) == []
        assert list(pb.storage_preferences) == []


# ---------------------------------------------------------------------------
# Orchestration — stub client
# ---------------------------------------------------------------------------

class _FakeCompute:
    def __init__(self, profile):
        self._profile = profile
        self.updated_gateway_id = None
        self.updated_profile = None

    def get_gateway_resource_profile(self, gateway_id):
        return self._profile

    def update_gateway_resource_profile(self, gateway_id, profile):
        self.updated_gateway_id = gateway_id
        self.updated_profile = profile


class _FakeClient:
    def __init__(self, profile, gateway_id="default", username="alice"):
        self.compute = _FakeCompute(profile)
        self.gateway_id = gateway_id
        self.username = username


class TestGetGatewayResourceProfile:
    """``get_gateway_resource_profile`` is proto-direct: it returns a
    ``WithAccess[GatewayResourceProfile]`` carrying the raw proto verbatim plus
    the gateway-catalog access flags (``is_owner`` always ``False``;
    ``user_has_write_access`` is the supplied gateway-admin flag)."""

    def _profile(self):
        return _make_profile(
            gateway_id="default", credential_store_token="cred",
            compute_resource_preferences=[
                _make_compute_preference(
                    compute_resource_id="cr-1",
                    preferred_job_submission_protocol=2)],
            storage_preferences=[
                _make_storage_preference(storage_resource_id="store-1")])

    def test_returns_withaccess_carrying_the_proto(self):
        profile = self._profile()
        client = _FakeClient(profile)
        result = get_gateway_resource_profile(client, "default", has_write=True)
        assert isinstance(result, WithAccess)
        # the proto flows through wholesale — no field copied out
        assert result.message is profile
        assert result.message.gateway_id == "default"
        assert result.message.credential_store_token == "cred"
        assert (result.message.compute_resource_preferences[0]
                .compute_resource_id == "cr-1")
        assert (result.message.storage_preferences[0].storage_resource_id
                == "store-1")

    def test_is_owner_always_false(self):
        client = _FakeClient(self._profile())
        result = get_gateway_resource_profile(client, "default", has_write=True)
        assert result.is_owner is False

    def test_has_write_forwarded(self):
        client = _FakeClient(self._profile())
        assert get_gateway_resource_profile(
            client, "default", has_write=True).user_has_write_access is True
        assert get_gateway_resource_profile(
            client, "default", has_write=False).user_has_write_access is False


class TestUpdateGatewayResourceProfile:
    """``update_gateway_resource_profile`` builds the proto from the snake_case
    write dict, persists it via the facade, then re-fetches via
    ``get_gateway_resource_profile`` so the return is a
    ``WithAccess[GatewayResourceProfile]`` matching the read path."""

    def _profile(self):
        return _make_profile(
            gateway_id="default", credential_store_token="cred")

    def test_persists_and_refetches(self):
        client = _FakeClient(self._profile())
        result = update_gateway_resource_profile(
            client, "default",
            {"gateway_id": "default", "credential_store_token": "cred"},
            has_write=True)
        assert client.compute.updated_gateway_id == "default"
        assert client.compute.updated_profile is not None
        assert isinstance(result, WithAccess)
        assert result.message.gateway_id == "default"
        assert result.user_has_write_access is True

    def test_builds_proto_from_data(self):
        client = _FakeClient(self._profile())
        update_gateway_resource_profile(
            client, "default",
            {"gateway_id": "default", "identity_server_tenant": "t"},
            has_write=True)
        assert client.compute.updated_profile.identity_server_tenant == "t"


# ---------------------------------------------------------------------------
# Gateway storage-preference orchestration (standalone family)
# ---------------------------------------------------------------------------

class _FakeStoragePrefCompute:
    """Stub ``compute`` facade for the standalone storage-preference family."""

    def __init__(self, prefs):
        # prefs: dict storage_resource_id -> StoragePreference proto
        self._prefs = dict(prefs)
        self.added = []
        self.updated = []
        self.deleted = []

    def get_all_gateway_storage_preferences(self, gateway_id):
        self.last_list_gateway = gateway_id
        return list(self._prefs.values())

    def get_gateway_storage_preference(self, gateway_id, storage_resource_id):
        return self._prefs[storage_resource_id]

    def add_gateway_storage_preference(self, gateway_id, storage_resource_id, pref):
        self.added.append((gateway_id, storage_resource_id, pref))
        self._prefs[storage_resource_id] = pref

    def update_gateway_storage_preference(self, gateway_id, storage_resource_id, pref):
        self.updated.append((gateway_id, storage_resource_id, pref))
        self._prefs[storage_resource_id] = pref

    def delete_gateway_storage_preference(self, gateway_id, storage_resource_id):
        self.deleted.append((gateway_id, storage_resource_id))
        self._prefs.pop(storage_resource_id, None)


class _FakeStoragePrefClient:
    def __init__(self, prefs, gateway_id="default", username="alice"):
        self.compute = _FakeStoragePrefCompute(prefs)
        self.gateway_id = gateway_id
        self.username = username


class TestListGatewayStoragePreferences:
    """Proto-direct: ``list`` returns a ``list[StoragePreference]`` (bare protos,
    no envelope, no dict transform)."""

    def test_returns_protos(self):
        client = _FakeStoragePrefClient({
            "store-1": _make_storage_preference(
                storage_resource_id="store-1", login_user_name="alice"),
            "store-2": _make_storage_preference(
                storage_resource_id="store-2", login_user_name="bob"),
        })
        out = list_gateway_storage_preferences(client, "default")
        assert client.compute.last_list_gateway == "default"
        assert all(
            isinstance(p, _gp_pb2().StoragePreference) for p in out)
        assert {p.storage_resource_id for p in out} == {"store-1", "store-2"}

    def test_empty(self):
        client = _FakeStoragePrefClient({})
        assert list_gateway_storage_preferences(client, "default") == []


class TestGetGatewayStoragePreference:
    """Proto-direct: ``get`` returns the raw ``StoragePreference`` proto."""

    def test_returns_proto(self):
        pref = _make_storage_preference(
            storage_resource_id="store-1", file_system_root_location="/data")
        client = _FakeStoragePrefClient({"store-1": pref})
        out = get_gateway_storage_preference(client, "default", "store-1")
        # the proto flows through wholesale — no field copied out
        assert out is pref
        assert out.storage_resource_id == "store-1"
        assert out.file_system_root_location == "/data"

    def test_empty_token_stays_empty_string(self):
        # proto-direct drops the legacy empty-token -> None coercion; the proto's
        # empty string flows through verbatim.
        client = _FakeStoragePrefClient({
            "store-1": _make_storage_preference(
                storage_resource_id="store-1",
                resource_specific_credential_store_token=""),
        })
        out = get_gateway_storage_preference(client, "default", "store-1")
        assert out.resource_specific_credential_store_token == ""


class TestCreateGatewayStoragePreference:
    def test_persists_and_refetches(self):
        client = _FakeStoragePrefClient({})
        out = create_gateway_storage_preference(client, "default", {
            "storage_resource_id": "store-new",
            "login_user_name": "alice",
            "file_system_root_location": "/data",
        })
        assert len(client.compute.added) == 1
        gw, srid, pref = client.compute.added[0]
        assert gw == "default"
        assert srid == "store-new"
        assert pref.login_user_name == "alice"
        assert isinstance(out, _gp_pb2().StoragePreference)
        assert out.storage_resource_id == "store-new"
        assert out.login_user_name == "alice"


class TestUpdateGatewayStoragePreference:
    def test_path_id_authoritative(self):
        client = _FakeStoragePrefClient({
            "store-1": _make_storage_preference(storage_resource_id="store-1"),
        })
        out = update_gateway_storage_preference(client, "default", "store-1", {
            # a divergent id in the body must be ignored in favour of the path
            "storage_resource_id": "WRONG",
            "login_user_name": "carol",
        })
        assert len(client.compute.updated) == 1
        gw, srid, pref = client.compute.updated[0]
        assert srid == "store-1"
        assert pref.storage_resource_id == "store-1"
        assert pref.login_user_name == "carol"
        assert isinstance(out, _gp_pb2().StoragePreference)
        assert out.storage_resource_id == "store-1"


class TestDeleteGatewayStoragePreference:
    def test_delegates_to_facade(self):
        client = _FakeStoragePrefClient({
            "store-1": _make_storage_preference(storage_resource_id="store-1"),
        })
        result = delete_gateway_storage_preference(client, "default", "store-1")
        assert result is None
        assert client.compute.deleted == [("default", "store-1")]


# ---------------------------------------------------------------------------
# ComputeResourceDescription family (proto-direct)
# ---------------------------------------------------------------------------
#
# ``get_compute_resource`` is proto-direct: it returns the raw
# ``ComputeResourceDescription`` proto verbatim (this family carries no
# ownership / sharing fields, so there is no ``WithAccess`` envelope and no dict
# transform).  ``list_compute_resource_names`` passes the raw id->name map
# straight through.  The proto -> snake_case JSON serialization (enums as NAMES,
# verbatim file-system map keys) is pinned by the portal contract-snapshot test.

def _cr_pb2():
    from airavata_sdk.generated.org.apache.airavata.model.appcatalog.computeresource import (  # noqa: E501
        compute_resource_pb2,
    )
    return compute_resource_pb2


def _dm_pb2():
    from airavata_sdk.generated.org.apache.airavata.model.data.movement import (
        data_movement_pb2,
    )
    return data_movement_pb2


def _make_compute_resource(**kwargs):
    return _cr_pb2().ComputeResourceDescription(**kwargs)


class _FakeComputeCR:
    def __init__(self, cr, names):
        self._cr = cr
        self._names = names

    def get_compute_resource(self, compute_resource_id):
        return self._cr

    def get_all_compute_resource_names(self):
        return dict(self._names)


class _FakeCRClient:
    def __init__(self, cr, names, gateway_id="default", username="alice"):
        self.compute = _FakeComputeCR(cr, names)
        self.gateway_id = gateway_id
        self.username = username


class TestGetComputeResource:
    """Proto-direct: ``get`` returns the raw ``ComputeResourceDescription``
    proto (no envelope, no dict transform)."""

    def test_returns_proto_verbatim(self):
        cr = _make_compute_resource(
            compute_resource_id="cr-1", host_name="host",
            batch_queues=[_cr_pb2().BatchQueue(queue_name="normal")])
        client = _FakeCRClient(cr, {})
        out = get_compute_resource(client, "cr-1")
        # the proto flows through wholesale — no field copied out
        assert out is cr
        assert isinstance(out, _cr_pb2().ComputeResourceDescription)
        assert out.compute_resource_id == "cr-1"
        assert out.host_name == "host"
        assert out.batch_queues[0].queue_name == "normal"


class TestListComputeResourceNames:
    def test_passthrough_map(self):
        client = _FakeCRClient(None, {"cr-1": "host-1", "cr-2": "host-2"})
        out = list_compute_resource_names(client)
        assert out == {"cr-1": "host-1", "cr-2": "host-2"}


# ===========================================================================
# GroupResourceProfile
# ===========================================================================

def _grp():
    from airavata_sdk.generated.org.apache.airavata.model.appcatalog.groupresourceprofile import (  # noqa: E501
        group_resource_profile_pb2,
    )
    return group_resource_profile_pb2


def _make_slurm_pref(**kwargs):
    g = _grp()
    slurm = g.SlurmComputeResourcePreference(
        allocation_project_number="alloc-1",
        preferred_batch_queue="normal",
        ssh_account_provisioner="prov",
        group_ssh_account_provisioner_configs=[
            g.GroupAccountSSHProvisionerConfig(
                resource_id="r1", config_name="cn", config_value="cv")],
        reservations=[
            g.ComputeResourceReservation(
                reservation_id="rid", queue_names=["q1"],
                start_time=1705320000000, end_time=0)],
    )
    base = dict(
        compute_resource_id="cr-slurm",
        override_by_airavata=True,
        preferred_job_submission_protocol=2,   # proto SSH -> Thrift 1
        preferred_data_movement_protocol=3,    # proto SFTP -> Thrift 2
        resource_type=g.ResourceType.SLURM,
        specific_preferences=g.EnvironmentSpecificPreferences(slurm=slurm),
    )
    base.update(kwargs)
    return g.GroupComputeResourcePreference(**base)


def _make_aws_pref(**kwargs):
    g = _grp()
    base = dict(
        compute_resource_id="cr-aws",
        group_resource_profile_id="grp-1",
        resource_type=g.ResourceType.AWS,
        specific_preferences=g.EnvironmentSpecificPreferences(
            aws=g.AwsComputeResourcePreference(
                region="us-east-1", preferred_ami_id="ami-1",
                preferred_instance_type="t2.micro")),
    )
    base.update(kwargs)
    return g.GroupComputeResourcePreference(**base)


def _make_group_profile(**kwargs):
    g = _grp()
    base = dict(
        gateway_id="default",
        group_resource_profile_id="grp-1",
        group_resource_profile_name="Test GRP",
        compute_preferences=[_make_slurm_pref(), _make_aws_pref()],
        compute_resource_policies=[
            g.ComputeResourcePolicy(
                resource_policy_id="rp-1", compute_resource_id="cr-slurm",
                allowed_batch_queues=["normal", "long"])],
        batch_queue_resource_policies=[
            g.BatchQueueResourcePolicy(
                resource_policy_id="bq-1", compute_resource_id="cr-slurm",
                queuename="normal", max_allowed_nodes=10,
                max_allowed_cores=100, max_allowed_walltime=60)],
        creation_time=1705320000000,
        updated_time=1705323600000,
        default_credential_store_token="def-tok",
    )
    base.update(kwargs)
    return g.GroupResourceProfile(**base)


class TestBuildGroupResourceProfile:
    def test_forces_gateway_id_and_builds_tree(self):
        client = _FakeGroupClient(_make_group_profile())
        proto = build_group_resource_profile(client, {
            "gateway_id": "ignored",
            "group_resource_profile_name": "New",
            "compute_preferences": [{
                "compute_resource_id": "cr-1",
                "overrideby_airavata": True,
                "resource_type": 0,  # Thrift SLURM
                "allocation_project_number": "ap",
                "specific_preferences": {"slurm": {
                    "preferred_batch_queue": "q"}},
            }],
            "compute_resource_policies": [{
                "resource_policy_id": "rp", "allowed_batch_queues": ["a"]}],
            "batch_queue_resource_policies": [{
                "resource_policy_id": "bq", "queuename": "q",
                "max_allowed_nodes": 5}],
            "default_credential_store_token": "t",
        })
        assert proto.gateway_id == "default"  # forced from client
        assert proto.group_resource_profile_name == "New"
        cp = proto.compute_preferences[0]
        assert cp.override_by_airavata is True
        assert cp.resource_type == _grp().ResourceType.SLURM  # Thrift 0 -> proto
        assert cp.specific_preferences.WhichOneof("preferences") == "slurm"
        assert cp.specific_preferences.slurm.preferred_batch_queue == "q"
        # flattened allocation_project_number folded into the slurm member
        assert (cp.specific_preferences.slurm.allocation_project_number
                == "ap")
        assert proto.compute_resource_policies[0].resource_policy_id == "rp"
        assert proto.batch_queue_resource_policies[0].max_allowed_nodes == 5


# ---------------------------------------------------------------------------
# GroupResourceProfile orchestration (fake-client)
# ---------------------------------------------------------------------------

class _FakeGroupCompute:
    def __init__(self, profile):
        self._profile = profile
        self.created = None
        self.updated = None
        self.removed_prefs = []
        self.removed_compute_policies = []
        self.removed_bq_policies = []
        self.removed_profile = None

    def get_group_resource_list(self):
        return [self._profile]

    def get_group_resource_profile(self, grp_id):
        return self._profile

    def create_group_resource_profile(self, profile):
        self.created = profile
        # server assigns id + creation_time
        profile.group_resource_profile_id = "grp-new"
        profile.creation_time = 1705320000000
        return profile

    def update_group_resource_profile(self, grp_id, profile):
        self.updated = (grp_id, profile)

    def remove_group_compute_prefs(self, grp_id, compute_resource_id):
        self.removed_prefs.append((grp_id, compute_resource_id))

    def remove_group_compute_resource_policy(self, policy_id):
        self.removed_compute_policies.append(policy_id)

    def remove_group_batch_queue_resource_policy(self, policy_id):
        self.removed_bq_policies.append(policy_id)

    def remove_group_resource_profile(self, grp_id):
        self.removed_profile = grp_id


class _FakeGroupClient:
    def __init__(self, profile, gateway_id="default", username="alice"):
        self.compute = _FakeGroupCompute(profile)
        self.gateway_id = gateway_id
        self.username = username


class TestGroupResourceProfileOrchestration:
    def test_list_returns_withaccess_carrying_the_proto(self):
        profile = _make_group_profile()
        client = _FakeGroupClient(profile)
        out = list_group_resource_profiles(
            client, has_write_by_id={"grp-1": True})
        assert len(out) == 1
        assert isinstance(out[0], WithAccess)
        # the proto flows through wholesale — no field copied out.
        assert out[0].message is profile
        assert out[0].is_owner is False
        assert out[0].user_has_write_access is True

    def test_list_default_false(self):
        client = _FakeGroupClient(_make_group_profile())
        out = list_group_resource_profiles(client)
        assert out[0].user_has_write_access is False

    def test_get_returns_withaccess_carrying_the_proto(self):
        profile = _make_group_profile()
        client = _FakeGroupClient(profile)
        r = get_group_resource_profile(client, "grp-1", has_write=False)
        assert isinstance(r, WithAccess)
        assert r.message is profile
        assert r.message.group_resource_profile_id == "grp-1"
        assert r.is_owner is False
        assert r.user_has_write_access is False

    def test_get_forwards_has_write_true(self):
        client = _FakeGroupClient(_make_group_profile())
        r = get_group_resource_profile(client, "grp-1", has_write=True)
        assert r.user_has_write_access is True

    def test_create_persists_and_wraps(self):
        client = _FakeGroupClient(_make_group_profile())
        r = create_group_resource_profile(
            client, {"group_resource_profile_name": "New"})
        assert client.compute.created is not None
        assert client.compute.created.gateway_id == "default"
        assert isinstance(r, WithAccess)
        # the server-assigned id is on the wrapped proto.
        assert r.message.group_resource_profile_id == "grp-new"
        assert r.is_owner is False
        # newly created profile is owned by the caller -> has_write defaults True
        assert r.user_has_write_access is True

    def test_update_removes_orphans(self):
        # original has cr-slurm + cr-aws prefs, rp-1 + bq-1 policies
        original = _make_group_profile()
        client = _FakeGroupClient(original)
        # new payload keeps only cr-slurm; drops cr-aws, rp-1, bq-1
        update_group_resource_profile(client, "grp-1", {
            "group_resource_profile_name": "Test GRP",
            "compute_preferences": [{
                "compute_resource_id": "cr-slurm", "resource_type": 0}],
            "compute_resource_policies": [],
            "batch_queue_resource_policies": [],
        })
        # cr-aws pref removed
        assert ("grp-1", "cr-aws") in client.compute.removed_prefs
        assert ("grp-1", "cr-slurm") not in client.compute.removed_prefs
        assert "rp-1" in client.compute.removed_compute_policies
        assert "bq-1" in client.compute.removed_bq_policies
        assert client.compute.updated[0] == "grp-1"

    def test_delete(self):
        client = _FakeGroupClient(_make_group_profile())
        delete_group_resource_profile(client, "grp-1")
        assert client.compute.removed_profile == "grp-1"


# ===========================================================================
# Per-protocol job submission (Local / SSH / Unicore / Cloud)
# ===========================================================================
#
# Proto-direct: each ``get_{local,ssh,unicore,cloud}_job_submission`` returns the
# bare facade proto VERBATIM (no envelope, no dict transform).  These resources
# carry no cross-service fields, so there is nothing to union in.  The portal's
# ``to_jsonable`` serializes the proto to snake_case JSON (enums as member NAMES);
# that serialization is exercised by the portal contract snapshot, not here.

def _sec_proto(name):
    return _dm_pb2().SecurityProtocol.Value(name)


class _FakeJobSubCompute:
    def __init__(self, local=None, ssh=None, unicore=None, cloud=None):
        self._local = local
        self._ssh = ssh
        self._unicore = unicore
        self._cloud = cloud
        self.calls = []

    def get_local_job_submission(self, sid):
        self.calls.append(("local", sid))
        return self._local

    def get_ssh_job_submission(self, sid):
        self.calls.append(("ssh", sid))
        return self._ssh

    def get_unicore_job_submission(self, sid):
        self.calls.append(("unicore", sid))
        return self._unicore

    def get_cloud_job_submission(self, sid):
        self.calls.append(("cloud", sid))
        return self._cloud


class _FakeJobSubClient:
    def __init__(self, **kwargs):
        self.compute = _FakeJobSubCompute(**kwargs)


class TestJobSubmissionOrchestration:
    """Each helper returns the facade proto VERBATIM (identity), no transform."""

    def test_get_local_returns_proto_verbatim(self):
        c = _cr_pb2()
        proto = c.LOCALSubmission(
            job_submission_interface_id="ls-1",
            security_protocol=_sec_proto("LOCAL"))
        client = _FakeJobSubClient(local=proto)
        result = get_local_job_submission(client, "ls-1")
        assert result is proto
        assert client.compute.calls == [("local", "ls-1")]

    def test_get_ssh_returns_proto_verbatim(self):
        c = _cr_pb2()
        proto = c.SSHJobSubmission(
            job_submission_interface_id="ssh-1",
            security_protocol=_sec_proto("SSH_KEYS"),
            monitor_mode=c.MonitorMode.MONITOR_FORK)
        client = _FakeJobSubClient(ssh=proto)
        result = get_ssh_job_submission(client, "ssh-1")
        assert result is proto
        assert client.compute.calls == [("ssh", "ssh-1")]

    def test_get_unicore_returns_proto_verbatim(self):
        c = _cr_pb2()
        proto = c.UnicoreJobSubmission(
            job_submission_interface_id="u-1",
            security_protocol=_sec_proto("GSI"),
            unicore_end_point_url="https://u")
        client = _FakeJobSubClient(unicore=proto)
        result = get_unicore_job_submission(client, "u-1")
        assert result is proto
        assert client.compute.calls == [("unicore", "u-1")]

    def test_get_cloud_returns_proto_verbatim(self):
        c = _cr_pb2()
        proto = c.CloudJobSubmission(
            job_submission_interface_id="cl-1",
            security_protocol=_sec_proto("OAUTH"),
            provider_name=c.ProviderName.AWSEC2)
        client = _FakeJobSubClient(cloud=proto)
        result = get_cloud_job_submission(client, "cl-1")
        assert result is proto
        assert client.compute.calls == [("cloud", "cl-1")]


# ---------------------------------------------------------------------------
# Workspace defaults (edge-workspace-prefs)
# ---------------------------------------------------------------------------

from types import SimpleNamespace  # noqa: E402

from airavata_sdk.helpers.compute_resources import (  # noqa: E402
    accessible_group_resource_profile_ids,
    most_recent_writeable_project_id,
    resolve_workspace_defaults,
    user_can_write,
)


class _FakeResearch:
    def __init__(self, projects):
        self._projects = projects
        self.user_projects_calls = []

    def get_user_projects(self, gateway_id, user_name, limit=-1, offset=0):
        self.user_projects_calls.append((gateway_id, user_name, limit, offset))
        return self._projects


class _FakeComputeGrp:
    def __init__(self, grp_ids):
        self._grp_ids = grp_ids
        self.group_resource_list_calls = 0

    def get_group_resource_list(self):
        self.group_resource_list_calls += 1
        return [
            SimpleNamespace(group_resource_profile_id=i) for i in self._grp_ids
        ]


class _FakeSharing:
    def __init__(self, writeable_ids):
        self._writeable = set(writeable_ids)
        self.calls = []

    def user_has_access(self, resource_id, user_id, permission_type):
        self.calls.append((resource_id, user_id, permission_type))
        return resource_id in self._writeable


class _FakeWorkspaceClient:
    def __init__(self, *, project_ids=(), writeable_ids=(), grp_ids=(),
                 gateway_id="default", username="alice"):
        self.research = _FakeResearch(
            [SimpleNamespace(project_id=p) for p in project_ids])
        self.compute = _FakeComputeGrp(list(grp_ids))
        self.sharing = _FakeSharing(writeable_ids)
        self.gateway_id = gateway_id
        self.username = username


class TestWorkspaceDefaults:
    def test_user_can_write_true(self):
        client = _FakeWorkspaceClient(writeable_ids=["proj-1"])
        assert user_can_write(client, "proj-1") is True
        assert client.sharing.calls == [("proj-1", "alice", "WRITE")]

    def test_user_can_write_false(self):
        client = _FakeWorkspaceClient(writeable_ids=["proj-1"])
        assert user_can_write(client, "proj-2") is False

    def test_most_recent_writeable_project_picks_first_writeable(self):
        # proj-1 not writeable, proj-2 writeable -> returns proj-2.
        client = _FakeWorkspaceClient(
            project_ids=["proj-1", "proj-2", "proj-3"],
            writeable_ids=["proj-2", "proj-3"])
        assert most_recent_writeable_project_id(client) == "proj-2"
        # gateway / user come from the client context.
        assert client.research.user_projects_calls == [
            ("default", "alice", -1, 0)]

    def test_most_recent_writeable_project_none_when_no_writeable(self):
        client = _FakeWorkspaceClient(
            project_ids=["proj-1"], writeable_ids=[])
        assert most_recent_writeable_project_id(client) is None

    def test_most_recent_writeable_project_none_when_no_projects(self):
        client = _FakeWorkspaceClient(project_ids=[], writeable_ids=[])
        assert most_recent_writeable_project_id(client) is None

    def test_accessible_grp_ids(self):
        client = _FakeWorkspaceClient(grp_ids=["grp-1", "grp-2"])
        assert accessible_group_resource_profile_ids(client) == [
            "grp-1", "grp-2"]

    def test_accessible_grp_ids_empty(self):
        client = _FakeWorkspaceClient(grp_ids=[])
        assert accessible_group_resource_profile_ids(client) == []

    def test_resolve_defaults_full(self):
        client = _FakeWorkspaceClient(
            project_ids=["proj-1", "proj-2"],
            writeable_ids=["proj-2"],
            grp_ids=["grp-1", "grp-2"])
        defaults = resolve_workspace_defaults(client)
        assert defaults == {
            "most_recent_project_id": "proj-2",
            "group_resource_profile_ids": ["grp-1", "grp-2"],
            "most_recent_group_resource_profile_id": "grp-1",
        }

    def test_resolve_defaults_empty(self):
        client = _FakeWorkspaceClient(
            project_ids=[], writeable_ids=[], grp_ids=[])
        defaults = resolve_workspace_defaults(client)
        assert defaults == {
            "most_recent_project_id": None,
            "group_resource_profile_ids": [],
            "most_recent_group_resource_profile_id": None,
        }
