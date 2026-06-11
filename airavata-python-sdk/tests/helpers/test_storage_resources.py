"""Unit tests for airavata_sdk.helpers.storage_resources.

The **storage-resources** family is proto-direct: ``get_storage_resource``
returns the raw ``StorageResourceDescription`` proto as-is (no transform, no
envelope) and ``list_storage_resource_names`` returns the raw name map.  These
tests assert exactly that — the proto identity / fields and the name map — via a
lightweight stub client that records the calls made.

The OTHER families in the module (per-protocol data movement, user-storage,
data-product download) are still on the legacy ``_x_dict`` + camelize path and
keep their existing transform-level tests below.
"""

from airavata_sdk.helpers.storage_resources import (
    get_grid_ftp_data_movement,
    get_local_data_movement,
    get_scp_data_movement,
    get_storage_resource,
    list_storage_resource_names,
)


def _sr_pb2():
    from airavata_sdk.generated.org.apache.airavata.model.appcatalog.storageresource import (  # noqa: E501
        storage_resource_pb2,
    )
    return storage_resource_pb2


def _dm_pb2():
    from airavata_sdk.generated.org.apache.airavata.model.data.movement import (
        data_movement_pb2,
    )
    return data_movement_pb2


def _make_dmi(**kwargs):
    return _dm_pb2().DataMovementInterface(**kwargs)


def _make_storage_resource(**kwargs):
    return _sr_pb2().StorageResourceDescription(**kwargs)


# ---------------------------------------------------------------------------
# Storage-resources family — proto-direct (returns the proto / name map as-is)
# ---------------------------------------------------------------------------

class _FakeStorage:
    def __init__(self, resource=None, names=None):
        self._resource = resource
        self._names = names or {}
        self.requested_id = None
        self.list_called = False

    def get_storage_resource(self, storage_resource_id):
        self.requested_id = storage_resource_id
        return self._resource

    def get_all_storage_resource_names(self):
        self.list_called = True
        return dict(self._names)


class _FakeClient:
    def __init__(self, resource=None, names=None,
                 gateway_id="default", username="alice"):
        self.storage = _FakeStorage(resource, names)
        self.gateway_id = gateway_id
        self.username = username


class TestGetStorageResource:
    def _full(self):
        return _make_storage_resource(
            storage_resource_id="store-1",
            host_name="store.example.com",
            storage_resource_description="desc",
            enabled=True,
            data_movement_interfaces=[
                _make_dmi(
                    data_movement_interface_id="dm-1",
                    data_movement_protocol=_dm_pb2().DataMovementProtocol.SCP,
                    priority_order=5,
                    creation_time=1705320000000,
                    update_time=0,
                    storage_resource_id="store-1",
                ),
            ],
            creation_time=1705320000000,
            update_time=0,
        )

    def test_returns_proto_directly(self):
        # Proto-direct: the SDK returns the gRPC message itself, NOT a dict.
        sr = self._full()
        client = _FakeClient(resource=sr)
        out = get_storage_resource(client, "store-1")
        assert client.storage.requested_id == "store-1"
        assert out is sr
        assert isinstance(out, _sr_pb2().StorageResourceDescription)

    def test_proto_fields_intact(self):
        client = _FakeClient(resource=self._full())
        out = get_storage_resource(client, "store-1")
        assert out.storage_resource_id == "store-1"
        assert out.host_name == "store.example.com"
        assert out.enabled is True
        # Nested interface + enum stay native proto values (rendered to the
        # member NAME by the portal's MessageToDict, not a Thrift int here).
        assert len(out.data_movement_interfaces) == 1
        dmi = out.data_movement_interfaces[0]
        assert dmi.data_movement_protocol == _dm_pb2().DataMovementProtocol.SCP
        assert dmi.creation_time == 1705320000000


class TestListStorageResourceNames:
    def test_returns_name_map(self):
        client = _FakeClient(names={"store-1": "host1", "store-2": "host2"})
        out = list_storage_resource_names(client)
        assert client.storage.list_called is True
        assert out == {"store-1": "host1", "store-2": "host2"}

    def test_empty(self):
        client = _FakeClient(names={})
        assert list_storage_resource_names(client) == {}


# ===========================================================================
# Per-protocol data movement (Local / SCP / GridFTP) — proto-direct
# ===========================================================================
#
# Each ``get_*_data_movement`` helper returns the raw facade proto as-is (no
# transform, no envelope): the resources carry no hyperlink / ownership /
# sharing fields, mirroring the per-protocol job-submission family.  The portal's
# MessageToDict renders ``security_protocol`` to the member NAME, not a Thrift
# int — that is the portal contract test's concern, not these unit tests.

class _FakeDmStorage:
    def __init__(self, local=None, scp=None, grid_ftp=None):
        self._local = local
        self._scp = scp
        self._grid_ftp = grid_ftp
        self.requested_id = None

    def get_local_data_movement(self, data_movement_id):
        self.requested_id = data_movement_id
        return self._local

    def get_scp_data_movement(self, data_movement_id):
        self.requested_id = data_movement_id
        return self._scp

    def get_grid_ftp_data_movement(self, data_movement_id):
        self.requested_id = data_movement_id
        return self._grid_ftp


class _FakeDmClient:
    def __init__(self, **kwargs):
        self.storage = _FakeDmStorage(**kwargs)


class TestDataMovementProtoDirect:
    def test_get_local_returns_proto_directly(self):
        # Proto-direct: the SDK returns the gRPC message itself, NOT a dict.
        local = _dm_pb2().LOCALDataMovement(data_movement_interface_id="l-1")
        client = _FakeDmClient(local=local)
        out = get_local_data_movement(client, "l-1")
        assert client.storage.requested_id == "l-1"
        assert out is local
        assert isinstance(out, _dm_pb2().LOCALDataMovement)
        assert out.data_movement_interface_id == "l-1"

    def test_get_scp_returns_proto_directly(self):
        scp = _dm_pb2().SCPDataMovement(
            data_movement_interface_id="scp-1",
            security_protocol=_dm_pb2().SecurityProtocol.SSH_KEYS,
            alternative_scp_host_name="alt.host",
            ssh_port=2222,
        )
        client = _FakeDmClient(scp=scp)
        out = get_scp_data_movement(client, "scp-1")
        assert client.storage.requested_id == "scp-1"
        assert out is scp
        assert isinstance(out, _dm_pb2().SCPDataMovement)
        assert out.data_movement_interface_id == "scp-1"
        assert out.alternative_scp_host_name == "alt.host"
        assert out.ssh_port == 2222
        # The enum stays a native proto value (rendered to the member NAME by
        # the portal's MessageToDict, not a Thrift int here).
        assert out.security_protocol == _dm_pb2().SecurityProtocol.SSH_KEYS

    def test_get_grid_ftp_returns_proto_directly(self):
        grid_ftp = _dm_pb2().GridFTPDataMovement(
            data_movement_interface_id="g-1",
            security_protocol=_dm_pb2().SecurityProtocol.GSI,
            grid_ftp_end_points=["ep1", "ep2"],
        )
        client = _FakeDmClient(grid_ftp=grid_ftp)
        out = get_grid_ftp_data_movement(client, "g-1")
        assert client.storage.requested_id == "g-1"
        assert out is grid_ftp
        assert isinstance(out, _dm_pb2().GridFTPDataMovement)
        assert list(out.grid_ftp_end_points) == ["ep1", "ep2"]
        assert out.security_protocol == _dm_pb2().SecurityProtocol.GSI


# ===========================================================================
# User-storage paths (file/directory listings) — proto-direct
# ===========================================================================
#
# ``get_file_metadata`` returns the raw ``FileMetadataResponse`` proto as-is;
# ``list_dir`` / ``list_experiment_dir`` return the raw ``ListDirResponse``
# proto as-is (its ``directories`` / ``files`` are each a
# ``FileMetadataResponse``).  No ``_x_dict`` transform, no envelope.  The
# per-entry path-permission flags (``user_has_write_access`` / ``is_shared_dir``)
# and the experiment-dir relative-path rewrite are portal concerns layered on the
# rendered proto by the ViewSet — the portal contract test pins that shape.

from airavata_sdk.helpers.storage_resources import (  # noqa: E402
    create_dir,
    delete_dir,
    delete_file,
    dir_exists,
    get_file_metadata,
    list_dir,
    list_experiment_dir,
    resolve_user_storage_path,
)


def _fs_pb2():
    from airavata_sdk.generated.services import file_service_pb2
    return file_service_pb2


def _make_meta(**kwargs):
    return _fs_pb2().FileMetadataResponse(**kwargs)


class _FakeUserStorage:
    def __init__(self, listing=None, metadata=None, exists=True):
        self._listing = listing
        self._metadata = metadata
        self._exists = exists
        self.calls = []

    def dir_exists(self, path):
        self.calls.append(("dir_exists", path))
        return self._exists

    def create_dir(self, path):
        self.calls.append(("create_dir", path))

    def delete_file(self, path):
        self.calls.append(("delete_file", path))

    def delete_dir(self, path):
        self.calls.append(("delete_dir", path))

    def list_dir(self, path):
        self.calls.append(("list_dir", path))
        return self._listing

    def get_file_metadata(self, path):
        self.calls.append(("get_file_metadata", path))
        return self._metadata


class _FakeResearch:
    def __init__(self, experiment=None):
        self._experiment = experiment

    def get_experiment(self, experiment_id):
        return self._experiment


class _FakeUsClient:
    def __init__(self, storage=None, research=None):
        self.storage = storage
        self.research = research


class _FakeExperiment:
    """Minimal proto-like experiment with a data dir."""

    def __init__(self, data_dir, has_ucd=True):
        self._data_dir = data_dir
        self._has_ucd = has_ucd

        class _UCD:
            experiment_data_dir = data_dir
        self.user_configuration_data = _UCD()

    def HasField(self, name):
        return self._has_ucd


class TestResolveUserStoragePath:
    def test_bare_relative(self):
        client = _FakeUsClient()
        assert resolve_user_storage_path(client, "foo/bar") == "~/foo/bar"

    def test_leading_slash_stripped(self):
        client = _FakeUsClient()
        assert resolve_user_storage_path(client, "/foo") == "~/foo"

    def test_already_tilde(self):
        client = _FakeUsClient()
        assert resolve_user_storage_path(client, "~/foo") == "~/foo"

    def test_experiment_relative(self):
        client = _FakeUsClient(
            research=_FakeResearch(
                _FakeExperiment("/data/exp-1")))
        out = resolve_user_storage_path(client, "out.txt", experiment_id="exp-1")
        assert out == "/data/exp-1/out.txt"

    def test_experiment_empty_path(self):
        client = _FakeUsClient(
            research=_FakeResearch(_FakeExperiment("~/data/exp-1")))
        out = resolve_user_storage_path(client, "", experiment_id="exp-1")
        assert out == "~/data/exp-1"


class TestUserStorageOrchestration:
    def test_dir_exists_create_delete(self):
        storage = _FakeUserStorage(exists=True)
        client = _FakeUsClient(storage=storage)
        assert dir_exists(client, "~/p") is True
        create_dir(client, "~/p")
        delete_file(client, "~/f")
        delete_dir(client, "~/p")
        assert ("create_dir", "~/p") in storage.calls
        assert ("delete_file", "~/f") in storage.calls
        assert ("delete_dir", "~/p") in storage.calls


class TestGetFileMetadataProtoDirect:
    def test_returns_proto_directly(self):
        # Proto-direct: the SDK returns the gRPC message itself, NOT a dict.
        meta = _make_meta(name="a.txt", path="/home/u/a.txt", size=5,
                          modified_time=1705320000000, content_type="text/plain",
                          data_product_uri="airavata-dp://x", is_directory=False)
        storage = _FakeUserStorage(metadata=meta)
        client = _FakeUsClient(storage=storage)
        out = get_file_metadata(client, "~/a.txt")
        assert out is meta
        assert isinstance(out, _fs_pb2().FileMetadataResponse)
        assert ("get_file_metadata", "~/a.txt") in storage.calls

    def test_proto_fields_intact(self):
        meta = _make_meta(name="a.txt", path="/home/u/a.txt", size=5,
                          modified_time=1705320000000, content_type="text/plain",
                          data_product_uri="airavata-dp://x")
        client = _FakeUsClient(storage=_FakeUserStorage(metadata=meta))
        out = get_file_metadata(client, "~/a.txt")
        assert out.name == "a.txt"
        assert out.path == "/home/u/a.txt"
        assert out.size == 5
        assert out.modified_time == 1705320000000
        assert out.content_type == "text/plain"
        assert out.data_product_uri == "airavata-dp://x"
        assert out.is_directory is False


class TestListDirProtoDirect:
    def _listing(self):
        return _fs_pb2().ListDirResponse(
            directories=[_make_meta(name="d", path="/home/u/d", size=0,
                                    is_directory=True)],
            files=[_make_meta(name="f.txt", path="/home/u/f.txt", size=2,
                              modified_time=1705320000000)],
        )

    def test_returns_proto_directly(self):
        # Proto-direct: the SDK returns the ListDirResponse message, NOT a dict.
        listing = self._listing()
        storage = _FakeUserStorage(listing=listing)
        client = _FakeUsClient(storage=storage)
        out = list_dir(client, "~/")
        assert out is listing
        assert isinstance(out, _fs_pb2().ListDirResponse)
        assert ("list_dir", "~/") in storage.calls

    def test_proto_entries_intact(self):
        client = _FakeUsClient(storage=_FakeUserStorage(listing=self._listing()))
        out = list_dir(client, "~/")
        assert len(out.directories) == 1
        assert out.directories[0].name == "d"
        assert out.directories[0].is_directory is True
        assert len(out.files) == 1
        assert out.files[0].name == "f.txt"
        assert out.files[0].size == 2


class TestListExperimentDirProtoDirect:
    def _listing(self):
        return _fs_pb2().ListDirResponse(
            directories=[_make_meta(name="sub", path="/data/exp/sub", size=0,
                                    is_directory=True)],
            files=[_make_meta(name="o.txt", path="/data/exp/o.txt", size=3,
                              modified_time=1705320000000)],
        )

    def test_returns_proto_directly(self):
        listing = self._listing()
        storage = _FakeUserStorage(listing=listing)
        client = _FakeUsClient(storage=storage)
        out = list_experiment_dir(client, "/data/exp")
        assert out is listing
        assert isinstance(out, _fs_pb2().ListDirResponse)
        assert ("list_dir", "/data/exp") in storage.calls

    def test_proto_entries_intact(self):
        client = _FakeUsClient(storage=_FakeUserStorage(listing=self._listing()))
        out = list_experiment_dir(client, "/data/exp")
        assert out.directories[0].path == "/data/exp/sub"
        assert out.files[0].name == "o.txt"


# ===========================================================================
# Data-product file download (output-view-provider data generation)
# ===========================================================================

from airavata_sdk.helpers.storage_resources import (  # noqa: E402
    data_product_file_path,
    download_data_product_files,
    file_exists,
)


def _rc_pb2():
    from airavata_sdk.generated.org.apache.airavata.model.data.replica import (
        replica_catalog_pb2,
    )
    return replica_catalog_pb2


def _make_data_product(*, replica_paths=None, **kwargs):
    rc = _rc_pb2()
    replicas = [
        rc.DataReplicaLocationModel(file_path=p) for p in (replica_paths or [])
    ]
    return rc.DataProductModel(replica_locations=replicas, **kwargs)


class TestDataProductFilePath:
    def test_no_replicas_is_none(self):
        assert data_product_file_path(_make_data_product()) is None

    def test_empty_file_path_is_none(self):
        assert data_product_file_path(
            _make_data_product(replica_paths=[""])) is None

    def test_absolute_path_passthrough(self):
        dp = _make_data_product(replica_paths=["/storage/tmp/out.txt"])
        assert data_product_file_path(dp) == "/storage/tmp/out.txt"

    def test_tilde_path_passthrough(self):
        dp = _make_data_product(replica_paths=["~/out.txt"])
        assert data_product_file_path(dp) == "~/out.txt"

    def test_relative_path_is_tilde_prefixed(self):
        dp = _make_data_product(replica_paths=["sub/out.txt"])
        assert data_product_file_path(dp) == "~/sub/out.txt"

    def test_first_replica_used(self):
        dp = _make_data_product(replica_paths=["/a.txt", "/b.txt"])
        assert data_product_file_path(dp) == "/a.txt"


class _FakeDownloadStorage:
    def __init__(self, *, existing_paths=None, contents=None, names=None):
        self._existing = set(existing_paths or [])
        self._contents = contents or {}
        self._names = names or {}
        self.calls = []

    def file_exists(self, path):
        self.calls.append(("file_exists", path))
        return path in self._existing

    def download_file(self, path):
        self.calls.append(("download_file", path))
        resp = _fs_pb2().DownloadFileResponse(
            content=self._contents.get(path, b""),
            name=self._names.get(path, ""),
        )
        return resp


class _FakeDownloadResearch:
    def __init__(self, products_by_uri=None):
        self._products = products_by_uri or {}
        self.requested = []

    def get_data_product(self, uri):
        self.requested.append(uri)
        return self._products[uri]


class _FakeDownloadClient:
    def __init__(self, storage=None, research=None):
        self.storage = storage
        self.research = research


class TestFileExists:
    def test_delegates_to_facade(self):
        storage = _FakeDownloadStorage(existing_paths=["/a.txt"])
        client = _FakeDownloadClient(storage=storage)
        assert file_exists(client, "/a.txt") is True
        assert file_exists(client, "/missing.txt") is False
        assert ("file_exists", "/a.txt") in storage.calls


class TestDownloadDataProductFiles:
    def test_downloads_existing_files_in_order(self):
        dp1 = _make_data_product(replica_paths=["/storage/a.txt"])
        dp2 = _make_data_product(replica_paths=["/storage/b.txt"])
        research = _FakeDownloadResearch({
            "airavata-dp://1": dp1,
            "airavata-dp://2": dp2,
        })
        storage = _FakeDownloadStorage(
            existing_paths=["/storage/a.txt", "/storage/b.txt"],
            contents={"/storage/a.txt": b"AAA", "/storage/b.txt": b"BBB"},
            names={"/storage/a.txt": "a.txt"},
        )
        client = _FakeDownloadClient(storage=storage, research=research)
        files = download_data_product_files(
            client, ["airavata-dp://1", "airavata-dp://2"])
        assert len(files) == 2
        assert files[0].read() == b"AAA"
        assert files[0].name == "a.txt"
        assert files[1].read() == b"BBB"
        # Empty download name falls back to the path basename.
        assert files[1].name == "b.txt"

    def test_skips_products_with_no_replica(self):
        dp = _make_data_product(replica_paths=[])
        research = _FakeDownloadResearch({"airavata-dp://x": dp})
        storage = _FakeDownloadStorage()
        client = _FakeDownloadClient(storage=storage, research=research)
        files = download_data_product_files(client, ["airavata-dp://x"])
        assert files == []
        # No file_exists / download_file calls for a product without a replica.
        assert storage.calls == []

    def test_skips_missing_files(self):
        dp = _make_data_product(replica_paths=["/storage/gone.txt"])
        research = _FakeDownloadResearch({"airavata-dp://g": dp})
        storage = _FakeDownloadStorage(existing_paths=[])
        client = _FakeDownloadClient(storage=storage, research=research)
        files = download_data_product_files(client, ["airavata-dp://g"])
        assert files == []
        assert ("file_exists", "/storage/gone.txt") in storage.calls
        # download_file is not attempted when the file does not exist.
        assert ("download_file", "/storage/gone.txt") not in storage.calls
