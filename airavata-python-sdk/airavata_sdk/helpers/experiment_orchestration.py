"""Experiment orchestration helpers over the gRPC facades. Every function takes
an :class:`~airavata_sdk.client.AiravataClient` plus explicit keyword arguments;
``experiment`` arguments are proto ``ExperimentModel`` instances.
"""

from __future__ import annotations

import logging
import os
from typing import TYPE_CHECKING, Optional
from urllib.parse import unquote, urlparse

# Importing ``airavata_sdk.generated`` puts the generated proto root on sys.path
# so the absolute ``from org.apache.airavata...`` imports inside the stubs resolve.
import airavata_sdk.generated  # noqa: F401
from airavata_sdk.generated.org.apache.airavata.model.application.io import (
    application_io_pb2,
)
from airavata_sdk.generated.org.apache.airavata.model.data.replica import (
    replica_catalog_pb2,
)
from airavata_sdk.generated.org.apache.airavata.model.status import status_pb2
from airavata_sdk.generated.org.apache.airavata.model.task import task_pb2

if TYPE_CHECKING:
    from airavata_sdk.client import AiravataClient
    from airavata_sdk.generated.org.apache.airavata.model.experiment import (
        experiment_pb2,
    )

logger = logging.getLogger(__name__)

# Directory (relative to the user's storage root) where freshly uploaded input
# files land before they are moved into a launched experiment's data directory.
TMP_INPUT_FILE_UPLOAD_DIR = "tmp"

# Permission name understood by the sharing service (it does
# ResourcePermissionType.valueOf(permission_type) server-side).
_WRITE_PERMISSION = "WRITE"

# Terminal process states for intermediate-output fetching.
_TERMINAL_PROCESS_STATES = (
    status_pb2.PROCESS_STATE_CANCELED,
    status_pb2.PROCESS_STATE_COMPLETED,
    status_pb2.PROCESS_STATE_FAILED,
)


def _gateway_data_store_replica(data_product):
    """The GATEWAY_DATA_STORE replica, or the first replica, or None."""
    replicas = [
        rep
        for rep in data_product.replica_locations
        if rep.replica_location_category == replica_catalog_pb2.GATEWAY_DATA_STORE
    ]
    if replicas:
        return replicas[0]
    return data_product.replica_locations[0] if data_product.replica_locations else None


def _replica_filesystem_path(replica):
    """The plain filesystem path from a replica's file_path (which may be stored
    as ``file://<host>:<path>`` or URL-encoded).
    """
    if replica is None or not replica.file_path:
        return None
    return unquote(urlparse(replica.file_path).path)


def is_input_file(
    data_product: "replica_catalog_pb2.DataProductModel",
) -> bool:
    """True iff the gateway-data-store replica's file_path lives directly under
    the tmp upload dir.
    """
    replica = _gateway_data_store_replica(data_product)
    path = _replica_filesystem_path(replica)
    if path is None:
        return False
    return os.path.basename(os.path.dirname(path)) == TMP_INPUT_FILE_UPLOAD_DIR


def launch(
    client: "AiravataClient",
    experiment_id: str,
    *,
    username: str,
) -> None:
    """Set storage ids + data dir, move tmp input uploads into it, persist, launch."""
    experiment = client.research.get_experiment(experiment_id)
    _set_storage_id_and_data_dir(client, experiment, username=username)
    _move_tmp_input_file_uploads_to_data_dir(client, experiment)
    client.research.update_experiment(experiment_id, experiment)
    client.research.launch_experiment(experiment_id, gateway_id=client.gateway_id)


def _set_storage_id_and_data_dir(client, experiment, *, username):
    """Default input/output storage to the gateway default, then ensure
    ``experiment_data_dir`` is set, creating the directory on storage if needed.
    """
    default_storage_id = client.storage.get_default_storage_resource_id()

    user_config = experiment.user_configuration_data
    user_config.input_storage_resource_id = default_storage_id
    user_config.output_storage_resource_id = default_storage_id

    if not user_config.experiment_data_dir:
        project = client.research.get_project(experiment.project_id)
        # "<project>/<experiment>" under the storage root (spaces -> underscores).
        exp_dir = "/".join(
            _sanitize_path_component(part)
            for part in (project.name, experiment.experiment_name)
        )
        # TODO(sdk-consolidation): the storage facade's create_dir has no
        # uniqueness guarantee, so two experiments with the same project+name
        # share a data dir (the old create_user_dir(create_unique=True) suffixed
        # on collision). A create_unique option would restore old behavior.
        resp = client.storage.create_dir(exp_dir, storage_resource_id=default_storage_id)
        created = getattr(resp, "created_path", "") or exp_dir
        user_config.experiment_data_dir = created
    else:
        client.storage.create_dir(
            user_config.experiment_data_dir, storage_resource_id=default_storage_id
        )


def _sanitize_path_component(name):
    return (name or "").strip().replace(" ", "_")


def _move_tmp_input_file_uploads_to_data_dir(client, experiment):
    """Relocate any URI / URI_COLLECTION input that is a tmp upload into the
    experiment data dir, rewriting the input value to the (same) data-product URI.
    """
    exp_data_dir = experiment.user_configuration_data.experiment_data_dir
    storage_id = experiment.user_configuration_data.input_storage_resource_id or None
    for experiment_input in experiment.experiment_inputs:
        if experiment_input.type == application_io_pb2.URI:
            if experiment_input.value:
                experiment_input.value = _move_if_tmp_input_file_upload(
                    client, experiment_input.value, exp_data_dir, storage_id
                )
        elif experiment_input.type == application_io_pb2.URI_COLLECTION:
            data_product_uris = (
                experiment_input.value.split(",") if experiment_input.value else []
            )
            moved_uris = [
                _move_if_tmp_input_file_upload(client, uri, exp_data_dir, storage_id)
                for uri in data_product_uris
            ]
            experiment_input.value = ",".join(moved_uris)


def _move_if_tmp_input_file_upload(client, data_product_uri, experiment_data_dir, storage_id):
    """If a tmp upload, relocate the bytes and repoint the replica's file_path in
    place — preserving the data-product URI. Returns the (same) URI.
    """
    data_product = client.research.get_data_product(data_product_uri)
    if not is_input_file(data_product):
        return data_product_uri

    replica = _gateway_data_store_replica(data_product)
    source_path = _replica_filesystem_path(replica)
    if source_path is None:
        logger.warning("No replica file path for data product %s; skipping move", data_product_uri)
        return data_product_uri

    file_name = data_product.product_name or os.path.basename(source_path)
    dest_path = _join_storage_path(experiment_data_dir, file_name)

    client.storage.move_file(source_path, dest_path, storage_resource_id=storage_id)

    # Repoint the replica's file_path to the new location and persist it.
    if replica.replica_id:
        replica.file_path = dest_path
        # TODO(sdk-consolidation): update_replica_location persists the new
        # file_path against the existing replica_id, preserving the data-product
        # URI. Verify round-trip against a live backend during portal
        # integration (research-service must honor a bare path as file_path).
        client.research.update_replica_location(replica.replica_id, replica)
    else:
        logger.warning(
            "Replica for data product %s has no replica_id; "
            "moved bytes but could not repoint replica file_path",
            data_product_uri,
        )
    return data_product_uri


def _join_storage_path(directory, name):
    if not directory:
        return name
    return directory.rstrip("/") + "/" + name


def clone(
    client: "AiravataClient",
    experiment_id: str,
    *,
    username: str,
    project_id: Optional[str] = None,
) -> str:
    """Clone into a writeable project (the given *project_id*, else the source's,
    else the first writeable), copy input files into fresh tmp uploads, null the
    data dir, persist. Returns the cloned id.
    """
    experiment = client.research.get_experiment(experiment_id)
    target_project_id = project_id or _get_writeable_project(client, experiment, username=username)

    cloned_experiment_id = client.research.clone_experiment(
        experiment_id,
        new_experiment_name="Clone of {}".format(experiment.experiment_name),
        new_experiment_project_id=target_project_id,
    )
    cloned_experiment = client.research.get_experiment(cloned_experiment_id)

    _copy_cloned_experiment_input_uris(client, cloned_experiment, username=username)

    # Null experiment_data_dir so a fresh one is created at launch time.
    cloned_experiment.user_configuration_data.experiment_data_dir = ""
    client.research.update_experiment(cloned_experiment.experiment_id, cloned_experiment)
    return cloned_experiment_id


def _get_writeable_project(client, experiment, *, username):
    """The experiment's own project if writeable, else the first writeable one."""
    project_id = experiment.project_id
    if _can_write(client, project_id, username=username):
        return project_id
    user_projects = client.research.get_user_projects(
        client.gateway_id, username, limit=-1, offset=0
    )
    for user_project in user_projects:
        if _can_write(client, user_project.project_id, username=username):
            return user_project.project_id
    raise Exception(
        "Could not find writeable project for user {} in gateway {}".format(
            username, client.gateway_id
        )
    )


def _can_write(client, entity_id, *, username) -> bool:
    return client.sharing.user_has_access(entity_id, username, _WRITE_PERMISSION)


def _copy_cloned_experiment_input_uris(client, cloned_experiment, *, username):
    """Copy each referenced data product into a fresh tmp upload and rewrite the
    input value to the new URI. Missing source files are dropped (URI) / omitted
    (URI_COLLECTION).
    """
    for experiment_input in cloned_experiment.experiment_inputs:
        if not experiment_input.value:
            continue
        if experiment_input.type == application_io_pb2.URI:
            cloned_uri = _copy_experiment_input_uri(client, experiment_input.value, username=username)
            if cloned_uri is None:
                logger.warning("Setting cloned input %s to null", experiment_input.name)
                experiment_input.value = ""
            else:
                experiment_input.value = cloned_uri
        elif experiment_input.type == application_io_pb2.URI_COLLECTION:
            data_product_uris = experiment_input.value.split(",") if experiment_input.value else []
            cloned_uris = []
            for uri in data_product_uris:
                cloned_uri = _copy_experiment_input_uri(client, uri, username=username)
                if cloned_uri is None:
                    logger.warning("Omitting a cloned input value for %s", experiment_input.name)
                else:
                    cloned_uris.append(cloned_uri)
            experiment_input.value = ",".join(cloned_uris)


def _copy_experiment_input_uri(client, data_product_uri, *, username):
    """The storage facade has no copy primitive, so download the source bytes,
    re-upload to a fresh tmp path, and register a new data product. Returns the
    new URI, or None if the source file does not exist.
    """
    source_product = client.research.get_data_product(data_product_uri)
    replica = _gateway_data_store_replica(source_product)
    source_path = _replica_filesystem_path(replica)
    storage_id = replica.storage_resource_id if replica is not None else None

    if source_path is None or not client.storage.file_exists(
        source_path, storage_resource_id=storage_id or None
    ):
        logger.warning("Could not find file for source data product %s", data_product_uri)
        return None

    download = client.storage.download_file(source_path, storage_resource_id=storage_id or None)
    file_name = source_product.product_name or os.path.basename(source_path)
    dest_path = _join_storage_path(TMP_INPUT_FILE_UPLOAD_DIR, file_name)

    content_type = source_product.product_metadata.get("mime-type", "")
    client.storage.upload_file(
        dest_path,
        download.content,
        file_name,
        storage_resource_id=storage_id or None,
        content_type=content_type,
    )

    # The facade's upload returns a DataProductModel without a URI; register it
    # explicitly to mint a persistent URI.
    new_product = _build_copy_data_product(
        client, source_product, dest_path, file_name, storage_id, username
    )
    new_uri = client.research.register_data_product(new_product)
    return new_uri


def _build_copy_data_product(client, source_product, dest_path, file_name, storage_id, username):
    """Build an unsaved DataProductModel for a copied input file."""
    replica = replica_catalog_pb2.DataReplicaLocationModel(
        storage_resource_id=storage_id or "",
        replica_name="{} gateway data store copy".format(file_name),
        replica_location_category=replica_catalog_pb2.GATEWAY_DATA_STORE,
        replica_persistent_type=replica_catalog_pb2.TRANSIENT,
        file_path=dest_path,
    )
    metadata = dict(source_product.product_metadata)
    return replica_catalog_pb2.DataProductModel(
        gateway_id=client.gateway_id,
        owner_name=username,
        product_name=file_name,
        data_product_type=replica_catalog_pb2.FILE,
        product_metadata=metadata,
        replica_locations=[replica],
    )


# ---------------------------------------------------------------------------
# Intermediate output
# ---------------------------------------------------------------------------

def _get_output_fetching_processes(experiment):
    """Most-recent-first list of the experiment's output-fetching processes."""
    processes = (
        sorted(experiment.processes, key=lambda p: p.creation_time, reverse=True)
        if experiment.processes
        else []
    )
    return [
        process
        for process in processes
        if any(task.task_type == task_pb2.OUTPUT_FETCHING for task in process.tasks)
    ]


def get_intermediate_output_process_status(
    client: "AiravataClient",
    experiment: "experiment_pb2.ExperimentModel",
    *output_names: str,
) -> "Optional[status_pb2.ProcessStatus]":
    """ProcessStatus of the intermediate-output fetch process, or None (no
    output-fetching processes / backend error). ``output_names`` is accepted for
    signature compatibility but the facade does not filter by it (see TODO).
    """
    output_fetching_processes = _get_output_fetching_processes(experiment)
    if not output_fetching_processes:
        return None
    try:
        # TODO(sdk-consolidation): research facade
        # get_intermediate_output_process_status(experiment_id) does not accept
        # output names; the old Thrift API filtered by output name. If
        # per-output status is required, the proto
        # GetIntermediateOutputProcessStatusRequest needs an output_names field.
        return client.research.get_intermediate_output_process_status(experiment.experiment_id)
    except Exception:
        logger.debug("Failed to get intermediate output process status", exc_info=True)
        return None


def can_fetch_intermediate_output(
    client: "AiravataClient",
    experiment: "experiment_pb2.ExperimentModel",
    output_name: str,
) -> bool:
    """True only when at least one job is ACTIVE and there is no in-progress
    (non-terminal) intermediate-output process.
    """
    jobs = []
    for process in experiment.processes:
        for task in process.tasks:
            for job in task.jobs:
                jobs.append(job)

    def latest_status_is_active(job):
        if not job.job_statuses:
            return False
        return job.job_statuses[-1].job_state == status_pb2.ACTIVE

    if not any(latest_status_is_active(job) for job in jobs):
        return False

    try:
        process_status = get_intermediate_output_process_status(client, experiment, output_name)
        # If there's no running process, status is None -> treat as fetchable.
        if process_status is None:
            return True
        return process_status.state in _TERMINAL_PROCESS_STATES
    except Exception:
        # An error here likely means there is no currently running process.
        return True


def fetch_intermediate_output(
    client: "AiravataClient",
    experiment_id: str,
    *output_names: str,
):
    """Start a fetch of the output file(s) for a currently running experiment."""
    return client.research.get_intermediate_outputs(
        experiment_id, output_names=list(output_names)
    )


def get_intermediate_output_data_products(
    client: "AiravataClient",
    experiment: "experiment_pb2.ExperimentModel",
    output_name: str,
) -> list:
    """DataProduct(s) for the named output: the most-recent completed
    output-fetching process's matching output, with its data-product URIs resolved.
    """
    output_fetching_processes = _get_output_fetching_processes(experiment)

    data_products = []
    if not output_fetching_processes:
        return data_products

    most_recent_completed_output = None
    for process in output_fetching_processes:
        if (
            not process.process_statuses
            or process.process_statuses[-1].state != status_pb2.PROCESS_STATE_COMPLETED
        ):
            continue
        for process_output in process.process_outputs:
            if process_output.name == output_name:
                most_recent_completed_output = process_output
                break
        if most_recent_completed_output is not None:
            break

    if most_recent_completed_output is not None:
        data_product_uris = []
        if most_recent_completed_output.value.startswith("airavata-dp://"):
            data_product_uris = most_recent_completed_output.value.split(",")
        for uri in data_product_uris:
            data_products.append(client.research.get_data_product(uri))
    return data_products
