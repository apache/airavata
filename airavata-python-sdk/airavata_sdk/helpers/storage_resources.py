"""Storage-domain helpers.

Storage resources, per-protocol data movement, and user-storage file/directory
listings are proto-direct: each ``get_*`` / ``list_*`` returns its proto (or the
raw ``{storage_resource_id: host_name}`` map) as-is — these are gateway-level
catalog / metadata entries with no ownership or sharing fields. The data-product
download orchestration at the bottom spans the research + storage facades and
returns plain file-like objects (not part of the read contract).
"""

from __future__ import annotations

from typing import TYPE_CHECKING, Optional

if TYPE_CHECKING:
    from airavata_sdk.client import AiravataClient
    from airavata_sdk.generated.org.apache.airavata.model.appcatalog.storageresource import (  # noqa: E501
        storage_resource_pb2,
    )
    from airavata_sdk.generated.org.apache.airavata.model.data.movement import (  # noqa: E501
        data_movement_pb2,
    )
    from airavata_sdk.generated.services import file_service_pb2


def get_storage_resource(
    client: "AiravataClient",
    storage_resource_id: str,
) -> "storage_resource_pb2.StorageResourceDescription":
    return client.storage.get_storage_resource(storage_resource_id)


def list_storage_resource_names(client: "AiravataClient") -> dict[str, str]:
    """The ``{storage_resource_id: host_name}`` map, passed straight through."""
    return client.storage.get_all_storage_resource_names()


# Per-protocol data movement (Local / SCP / GridFTP) — bare proto, no envelope.


def get_local_data_movement(
    client: "AiravataClient",
    data_movement_id: str,
) -> "data_movement_pb2.LOCALDataMovement":
    return client.storage.get_local_data_movement(data_movement_id)


def get_scp_data_movement(
    client: "AiravataClient",
    data_movement_id: str,
) -> "data_movement_pb2.SCPDataMovement":
    return client.storage.get_scp_data_movement(data_movement_id)


def get_grid_ftp_data_movement(
    client: "AiravataClient",
    data_movement_id: str,
) -> "data_movement_pb2.GridFTPDataMovement":
    return client.storage.get_grid_ftp_data_movement(data_movement_id)


# User-storage paths (file/directory listings) — proto-direct. The per-entry
# write/shared-dir flags, hyperlinks, and the experiment-dir relative-path
# rewrite are portal path-decisions the ViewSet layers on the rendered proto.
# The orchestration helpers resolve the bare portal path to the absolute
# ``~/``-prefixed path the facade expects, then wrap the raw facade.


def resolve_user_storage_path(
    client: "AiravataClient",
    path: str,
    experiment_id: Optional[str] = None,
) -> str:
    """Resolve a portal user-storage path to the absolute facade path.

    A bare relative path is relative to the user's storage root (``~/``); with
    *experiment_id* it is relative to that experiment's data directory.
    """
    rel = (path or "").lstrip("/")
    if experiment_id:
        experiment = client.research.get_experiment(experiment_id)
        data_dir = (
            experiment.user_configuration_data.experiment_data_dir
            if experiment.HasField("user_configuration_data")
            else None
        ) or ""
        base = data_dir.rstrip("/")
        full = base + ("/" + rel if rel else "")
        if full.startswith("/") or full.startswith("~/"):
            return full
        return "~/" + full
    if rel.startswith("~"):
        return rel
    return "~/" + rel


def dir_exists(
    client: "AiravataClient",
    resolved_path: str,
) -> bool:
    return client.storage.dir_exists(resolved_path)


def create_dir(
    client: "AiravataClient",
    resolved_path: str,
) -> None:
    client.storage.create_dir(resolved_path)


def delete_file(
    client: "AiravataClient",
    resolved_path: str,
) -> None:
    client.storage.delete_file(resolved_path)


def delete_dir(
    client: "AiravataClient",
    resolved_path: str,
) -> None:
    client.storage.delete_dir(resolved_path)


def get_file_metadata(
    client: "AiravataClient",
    resolved_path: str,
) -> "file_service_pb2.FileMetadataResponse":
    return client.storage.get_file_metadata(resolved_path)


def list_dir(
    client: "AiravataClient",
    resolved_path: str,
) -> "file_service_pb2.ListDirResponse":
    return client.storage.list_dir(resolved_path)


def list_experiment_dir(
    client: "AiravataClient",
    resolved_path: str,
) -> "file_service_pb2.ListDirResponse":
    """Same proto shape as :func:`list_dir`; the ViewSet rewrites each entry's
    ``path`` relative to the experiment data dir.
    """
    return client.storage.list_dir(resolved_path)


# Data-product file download (output-view-provider data generation). Thin
# orchestration over the research (``GetDataProduct``) + storage
# (``FileExists`` / ``DownloadFile``) facades.


def data_product_file_path(data_product) -> Optional[str]:
    """First replica's ``file_path``, or None.

    The storage facade expects the FULL path, absolute or ``~/``-prefixed (a bare
    relative path NPEs server-side); a relative replica path is ``~/``-prefixed.
    """
    replicas = data_product.replica_locations
    if not replicas:
        return None
    file_path = replicas[0].file_path
    if not file_path:
        return None
    if not (file_path.startswith("/") or file_path.startswith("~/")):
        file_path = "~/" + file_path
    return file_path


def file_exists(
    client: "AiravataClient",
    resolved_path: str,
) -> bool:
    return client.storage.file_exists(resolved_path)


def download_data_product_files(
    client: "AiravataClient",
    data_product_uris,
) -> list:
    """For each ``airavata-dp://`` URI, fetch the product, resolve its first
    replica's file path, and download its bytes when the file exists.

    Returns a list of :class:`io.BytesIO` (``.name`` set to the download name or
    the path basename), in input-URI order. URIs with no replica / missing file
    contribute nothing.
    """
    import io
    import os

    output_files = []
    for uri in data_product_uris:
        data_product = client.research.get_data_product(uri)
        path = data_product_file_path(data_product)
        if path and client.storage.file_exists(path):
            resp = client.storage.download_file(path)
            output_file = io.BytesIO(resp.content)
            output_file.name = resp.name or os.path.basename(path)
            output_files.append(output_file)
    return output_files
