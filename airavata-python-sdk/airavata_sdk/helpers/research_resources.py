"""Research-domain helpers (proto-direct). Each family returns objects, not dicts:

* a plain resource -> its proto, returned wholesale;
* a resource needing cross-service sharing-access flags ->
  :class:`~airavata_sdk.helpers._envelope.WithAccess` (proto + chained scalars);
* a composed multi-proto shape (full-experiment) -> a pydantic model carrying
  the component protos / envelopes wholesale.
"""

from __future__ import annotations

from datetime import datetime, timezone
from typing import TYPE_CHECKING, Any, Optional

from pydantic import BaseModel, ConfigDict

from airavata_sdk.helpers._envelope import WithAccess
from airavata_sdk.helpers.models import (
    ApplicationDeploymentCreate,
    ApplicationInterfaceCreate,
    ExperimentCreate,
    NotificationCreate,
    ParserCreate,
    ProjectCreate,
    proto_enum_value,
)

if TYPE_CHECKING:
    from airavata_sdk.client import AiravataClient
    from airavata_sdk.generated.org.apache.airavata.model.appcatalog.appdeployment import (  # noqa: E501
        app_deployment_pb2,
    )
    from airavata_sdk.generated.org.apache.airavata.model.appcatalog.appinterface import (  # noqa: E501
        app_interface_pb2,
    )
    from airavata_sdk.generated.org.apache.airavata.model.appcatalog.parser import (  # noqa: E501
        parser_pb2,
    )
    from airavata_sdk.generated.org.apache.airavata.model.data.replica import (
        replica_catalog_pb2,
    )
    from airavata_sdk.generated.org.apache.airavata.model.experiment import (
        experiment_pb2,
    )
    from airavata_sdk.generated.org.apache.airavata.model.job import (
        job_pb2,
    )
    from airavata_sdk.generated.org.apache.airavata.model.workspace import (
        workspace_pb2,
    )


# Project (reference family) — WithAccess. is_owner is SDK-trivial
# (proto.owner == client.username); user_has_write_access is a chained
# sharing.user_has_access WRITE lookup.


def get_project(client: "AiravataClient", project_id: str) -> "WithAccess":
    p = client.research.get_project(project_id)
    return WithAccess(
        message=p,
        is_owner=(p.owner == client.username),
        user_has_write_access=client.sharing.user_has_access(
            resource_id=project_id,
            user_id=client.username,
            permission_type="WRITE",
        ),
    )


def list_projects(
    client: "AiravataClient",
    *,
    limit: int = -1,
    offset: int = 0,
) -> "list[WithAccess]":
    projects = client.research.get_user_projects(
        gateway_id=client.gateway_id,
        user_name=client.username,
        limit=limit,
        offset=offset,
    )
    return [
        WithAccess(
            message=p,
            is_owner=(p.owner == client.username),
            user_has_write_access=client.sharing.user_has_access(
                resource_id=p.project_id,
                user_id=client.username,
                permission_type="WRITE",
            ),
        )
        for p in projects
    ]


def create_project(
    client: "AiravataClient",
    data: "ProjectCreate | dict",
) -> "WithAccess":
    """``owner`` / ``gateway_id`` are forced from the client context. Re-fetched
    so the shape matches the read path.
    """
    from airavata_sdk.generated.org.apache.airavata.model.workspace import (
        workspace_pb2,
    )

    data = ProjectCreate.model_validate(data).model_dump(exclude_unset=True)
    project = workspace_pb2.Project(
        owner=client.username or "",
        gateway_id=client.gateway_id or "",
        name=data.get("name") or "",
        description=data.get("description") or "",
    )
    project_id = client.research.create_project(client.gateway_id, project)
    return get_project(client, project_id)


def update_project(
    client: "AiravataClient",
    project_id: str,
    data: "ProjectCreate | dict",
) -> "WithAccess":
    """Only ``name`` / ``description`` are mutable. Re-fetched so the shape
    matches the read path.
    """
    data = ProjectCreate.model_validate(data).model_dump(exclude_unset=True)
    project = client.research.get_project(project_id)
    if "name" in data:
        project.name = data["name"] or ""
    if "description" in data:
        project.description = data["description"] or ""
    client.research.update_project(project_id, project)
    return get_project(client, project_id)


# Gateway-catalog families (ApplicationModule, ApplicationInterface,
# Notification, GatewayResourceProfile) — WithAccess with no owner (is_owner
# always False) and user_has_write_access = the gateway-admin flag the ViewSet
# supplies as has_write (not a sharing lookup).


def get_application_module(
    client: "AiravataClient",
    app_module_id: str,
    *,
    has_write: bool,
) -> "WithAccess":
    m = client.research.get_application_module(app_module_id)
    return WithAccess(
        message=m,
        is_owner=False,
        user_has_write_access=has_write,
    )


def list_application_modules(
    client: "AiravataClient",
    *,
    has_write: bool,
    accessible_only: bool = True,
) -> "list[WithAccess]":
    """*accessible_only* True (default) -> only modules the caller can access;
    False -> every module in the gateway (the ``list_all`` action).
    """
    if accessible_only:
        modules = client.research.get_accessible_app_modules(
            gateway_id=client.gateway_id)
    else:
        modules = client.research.get_all_app_modules(
            gateway_id=client.gateway_id)
    return [
        WithAccess(message=m, is_owner=False, user_has_write_access=has_write)
        for m in modules
    ]


def create_application_module(
    client: "AiravataClient",
    data: dict,
    *,
    has_write: bool,
) -> "WithAccess":
    """Re-fetched so the server-assigned ``app_module_id`` is populated and the
    shape matches the read path.
    """
    from airavata_sdk.generated.org.apache.airavata.model.appcatalog.appdeployment import (  # noqa: E501
        app_deployment_pb2,
    )

    module = app_deployment_pb2.ApplicationModule(
        app_module_name=data.get("app_module_name") or "",
        app_module_version=data.get("app_module_version") or "",
        app_module_description=data.get("app_module_description") or "",
    )
    app_module_id = client.research.register_application_module(
        client.gateway_id, module)
    return get_application_module(client, app_module_id, has_write=has_write)


def update_application_module(
    client: "AiravataClient",
    app_module_id: str,
    data: dict,
    *,
    has_write: bool,
) -> "WithAccess":
    """``app_module_name`` / ``app_module_version`` / ``app_module_description``
    are mutable. Re-fetched so the shape matches the read path.
    """
    module = client.research.get_application_module(app_module_id)
    if "app_module_name" in data:
        module.app_module_name = data["app_module_name"] or ""
    if "app_module_version" in data:
        module.app_module_version = data["app_module_version"] or ""
    if "app_module_description" in data:
        module.app_module_description = data["app_module_description"] or ""
    client.research.update_application_module(app_module_id, module)
    return get_application_module(client, app_module_id, has_write=has_write)


# Experiment (experiments-core) — WithAccess. The whole process/task/job tree
# flows through the proto wholesale. is_owner compares the experiment's owner
# (user_name) against the caller; user_has_write_access is ownership OR a chained
# sharing WRITE lookup (owners always retain write/edit/launch rights).


def get_experiment(
    client: "AiravataClient",
    experiment_id: str,
) -> "WithAccess":
    e = client.research.get_experiment(experiment_id)
    is_owner = e.user_name == client.username
    return WithAccess(
        message=e,
        is_owner=is_owner,
        user_has_write_access=is_owner
        or client.sharing.user_has_access(
            resource_id=experiment_id,
            user_id=client.username,
            permission_type="WRITE",
        ),
    )


def get_experiment_proto(
    client: "AiravataClient",
    experiment_id: str,
) -> "experiment_pb2.ExperimentModel":
    """The bare proto (vs. the :func:`get_experiment` envelope), for callers that
    work with proto fields directly — notably portal output-view-provider data
    generation.
    """
    return client.research.get_experiment(experiment_id)


def get_experiments_in_project(
    client: "AiravataClient",
    project_id: str,
    *,
    limit: int = -1,
    offset: int = 0,
) -> "list[WithAccess]":
    experiments = client.research.get_experiments_in_project(
        project_id, limit, offset)
    return [
        _experiment_with_access(client, e)
        for e in experiments
    ]


def _experiment_with_access(
    client: "AiravataClient",
    e: "experiment_pb2.ExperimentModel",
) -> "WithAccess":
    is_owner = e.user_name == client.username
    return WithAccess(
        message=e,
        is_owner=is_owner,
        user_has_write_access=is_owner
        or client.sharing.user_has_access(
            resource_id=e.experiment_id,
            user_id=client.username,
            permission_type="WRITE",
        ),
    )


def list_experiment_jobs(
    client: "AiravataClient",
    experiment_id: str,
) -> "list[job_pb2.JobModel]":
    """Bare ``JobModel`` protos (``GetJobDetails``) — a job has no ownership or
    sharing concept.
    """
    return list(client.research.get_job_details(experiment_id))


# FullExperiment — a composed multi-proto shape: the experiment plus every
# resolved reference (project, app module, compute resource, input/output data
# products, job details) and the portal-computed output_views map. Each
# component is produced by the proto-direct component function above and carried
# wholesale, so the family inherits each family's field handling / access flags.
# Optional references resolve to None on lookup failure / no READ access.


class FullExperiment(BaseModel):
    model_config = ConfigDict(arbitrary_types_allowed=True)

    experiment_id: str
    experiment: Any
    project: Optional[Any]
    application_module: Optional[Any]
    compute_resource: Optional[Any]
    input_data_products: list[Any]
    output_data_products: list[Any]
    job_details: list[Any]
    output_views: dict


def _data_type_pb2():
    from airavata_sdk.generated.org.apache.airavata.model.application.io import (
        application_io_pb2 as io,
    )
    return io.DataType


def _collect_data_product_uris(io_objects) -> list[str]:
    """``airavata-dp`` URIs referenced by input/output protos: single-URI types
    (URI / STDOUT / STDERR) contribute their ``value``, URI_COLLECTION each
    comma-separated member. All single URIs first, then collection members.
    """
    DT = _data_type_pb2()
    single = [
        o.value for o in io_objects
        if (o.value and o.value.startswith("airavata-dp") and
            o.type in (DT.URI, DT.STDOUT, DT.STDERR))
    ]
    collection = [
        dp
        for o in io_objects
        if o.value and o.type == DT.URI_COLLECTION
        for dp in o.value.split(",")
        if o.value.startswith("airavata-dp")
    ]
    return single + collection


def get_full_experiment(
    client: "AiravataClient",
    experiment_id: str,
    *,
    project_has_read: bool,
    module_has_write: bool,
    data_product_write_fn,
    output_views_fn,
) -> "FullExperiment":
    """Compose the full-experiment view. Request-bound inputs the SDK cannot
    derive are supplied by the ViewSet:

    * *project_has_read* — when ``False`` the project is omitted.
    * *module_has_write* — the gateway-admin write flag for the nested module.
    * *data_product_write_fn* — ``(data_product_proto) -> bool``, the per-product
      ``user_has_write_access`` flag.
    * *output_views_fn* — ``(experiment, app_interface_or_None) -> dict``, the
      portal-computed view-provider map.

    Errors resolving an optional reference leave it ``None``.
    """
    from airavata_sdk.helpers import compute_resources

    experiment_env = get_experiment(client, experiment_id)
    experiment = experiment_env.message

    # Resolve referenced data products for outputs then inputs (order matches
    # the old view).
    output_uris = _collect_data_product_uris(experiment.experiment_outputs)
    input_uris = _collect_data_product_uris(experiment.experiment_inputs)
    output_products = [
        client.research.get_data_product(uri) for uri in output_uris]
    input_products = [
        client.research.get_data_product(uri) for uri in input_uris]

    # Resolve the application interface (execution_id) → module.
    application_interface = None
    try:
        application_interface = client.research.get_application_interface(
            experiment.execution_id)
    except Exception:
        application_interface = None

    output_views = output_views_fn(experiment, application_interface)

    application_module = None
    try:
        if (application_interface is not None and
                application_interface.application_modules):
            application_module = get_application_module(
                client,
                application_interface.application_modules[0],
                has_write=module_has_write,
            )
    except Exception:
        application_module = None

    # Resolve the compute resource from the scheduling host id, when set.
    compute_resource = None
    compute_resource_id = None
    if experiment.HasField("user_configuration_data"):
        ucd = experiment.user_configuration_data
        if ucd.HasField("computational_resource_scheduling"):
            compute_resource_id = (
                ucd.computational_resource_scheduling.resource_host_id)
    if compute_resource_id:
        try:
            compute_resource = compute_resources.get_compute_resource(
                client, compute_resource_id)
        except Exception:
            compute_resource = None

    # Resolve the project (only when the caller may READ it).
    project = None
    if project_has_read:
        project = get_project(client, experiment.project_id)

    job_details = list_experiment_jobs(client, experiment_id)

    input_data_products = [
        WithAccess(
            message=dp,
            is_owner=bool(dp.owner_name) and (dp.owner_name == client.username),
            user_has_write_access=data_product_write_fn(dp),
        )
        for dp in input_products
    ]
    output_data_products = [
        WithAccess(
            message=dp,
            is_owner=bool(dp.owner_name) and (dp.owner_name == client.username),
            user_has_write_access=data_product_write_fn(dp),
        )
        for dp in output_products
    ]

    return FullExperiment(
        experiment_id=experiment.experiment_id,
        experiment=experiment_env,
        project=project,
        application_module=application_module,
        compute_resource=compute_resource,
        input_data_products=input_data_products,
        output_data_products=output_data_products,
        job_details=job_details,
        output_views=output_views,
    )


def _build_input_data_object(data: dict):
    return _proto_input_data_object(data)


def _build_output_data_object(data: dict):
    return _proto_output_data_object(data)


def _build_computational_resource_scheduling(data: dict):
    from airavata_sdk.generated.org.apache.airavata.model.scheduling import (
        scheduling_pb2,
    )

    # Scheduling ints are coerced to int at the typed boundary
    # (ComputationalResourceSchedulingCreate), so they arrive here already int.
    return scheduling_pb2.ComputationalResourceSchedulingModel(
        resource_host_id=data.get("resource_host_id") or "",
        total_cpu_count=data.get("total_cpu_count") or 0,
        node_count=data.get("node_count") or 0,
        number_of_threads=data.get("number_of_threads") or 0,
        queue_name=data.get("queue_name") or "",
        wall_time_limit=data.get("wall_time_limit") or 0,
        total_physical_memory=data.get("total_physical_memory") or 0,
        chessis_number=data.get("chessis_number") or "",
        static_working_dir=data.get("static_working_dir") or "",
        override_login_user_name=data.get("override_login_user_name") or "",
        override_scratch_location=data.get("override_scratch_location") or "",
        override_allocation_project_number=data.get(
            "override_allocation_project_number") or "",
        m_group_count=data.get("m_group_count") or 0,
    )


def _build_user_configuration_data(data: dict):
    """Only the user-submittable scalar fields, plus the singular
    ``computational_resource_scheduling`` when present.
    """
    from airavata_sdk.generated.org.apache.airavata.model.experiment import (
        experiment_pb2,
    )

    crs = data.get("computational_resource_scheduling")
    kwargs = dict(
        airavata_auto_schedule=bool(data.get("airavata_auto_schedule", False)),
        override_manual_scheduled_params=bool(
            data.get("override_manual_scheduled_params", False)),
        share_experiment_publicly=bool(
            data.get("share_experiment_publicly", False)),
        throttle_resources=bool(data.get("throttle_resources", False)),
        input_storage_resource_id=data.get("input_storage_resource_id") or "",
        output_storage_resource_id=data.get("output_storage_resource_id") or "",
        experiment_data_dir=data.get("experiment_data_dir") or "",
        use_user_cr_pref=bool(data.get("use_user_cr_pref", False)),
        group_resource_profile_id=data.get("group_resource_profile_id") or "",
    )
    if crs is not None:
        kwargs["computational_resource_scheduling"] = (
            _build_computational_resource_scheduling(crs))
    return experiment_pb2.UserConfigurationDataModel(**kwargs)


def _build_experiment(client: "AiravataClient", data: dict):
    """Only the user-submittable fields (status / errors / processes / workflow
    are server-managed). ``gateway_id`` / ``user_name`` are forced from the
    client context.
    """
    from airavata_sdk.generated.org.apache.airavata.model.experiment import (
        experiment_pb2,
    )

    ucd = data.get("user_configuration_data")
    kwargs = dict(
        experiment_id=data.get("experiment_id") or "",
        project_id=data.get("project_id") or "",
        gateway_id=client.gateway_id or "",
        experiment_type=_experiment_type_int(data.get("experiment_type")),
        user_name=client.username or "",
        experiment_name=data.get("experiment_name") or "",
        description=data.get("description") or "",
        execution_id=data.get("execution_id") or "",
        enable_email_notification=bool(
            data.get("enable_email_notification", False)),
        email_addresses=list(data.get("email_addresses") or []),
        experiment_inputs=[
            _build_input_data_object(i)
            for i in (data.get("experiment_inputs") or [])],
        experiment_outputs=[
            _build_output_data_object(o)
            for o in (data.get("experiment_outputs") or [])],
    )
    if ucd is not None:
        kwargs["user_configuration_data"] = _build_user_configuration_data(ucd)
    return experiment_pb2.ExperimentModel(**kwargs)


def _experiment_type_int(value) -> int:
    """``experiment_type`` -> proto ``ExperimentType`` int via the proto enum
    (member NAME or proto int; ``None`` / ``""`` -> 0). The proto enum is the type
    truth — no Thrift remapping.
    """
    from airavata_sdk.generated.org.apache.airavata.model.experiment import (
        experiment_pb2,
    )

    return proto_enum_value(experiment_pb2.ExperimentType, value)


def build_experiment(
    client: "AiravataClient",
    data: "ExperimentCreate | dict",
):
    """Validate *data* and assemble a proto ``ExperimentModel`` (no RPC).

    The public parse entry point for callers that need only the model — e.g. the
    queue-settings calculator. ``create_experiment`` / ``update_experiment`` use
    the same validate-then-build path before submitting.
    """
    data = ExperimentCreate.model_validate(data).model_dump(exclude_unset=True)
    return _build_experiment(client, data)


def create_experiment(
    client: "AiravataClient",
    data: "ExperimentCreate | dict",
) -> "WithAccess":
    """The server mints the ``experiment_id``; re-fetched so the shape matches
    the read path. New id at ``result.message.experiment_id``.
    """
    experiment = build_experiment(client, data)
    experiment_id = client.research.create_experiment(
        client.gateway_id, experiment)
    return get_experiment(client, experiment_id)


def update_experiment(
    client: "AiravataClient",
    experiment_id: str,
    data: "ExperimentCreate | dict",
) -> "WithAccess":
    """The proto is rebuilt wholesale from *data* (``experiment_id`` forced),
    pushed, and re-fetched so the shape matches the read path.
    """
    experiment = build_experiment(client, data)
    experiment.experiment_id = experiment_id
    client.research.update_experiment(experiment_id, experiment)
    return get_experiment(client, experiment_id)


# ExperimentSummary (experiment-search) — WithAccess. is_owner always False (no
# owner field); user_has_write_access is a per-summary chained sharing WRITE
# lookup.


def search_experiments(
    client: "AiravataClient",
    *,
    filters: Optional[dict] = None,
    limit: int = -1,
    offset: int = 0,
) -> "list[WithAccess]":
    """*filters* is a ``map<string, string>`` keyed by ``ExperimentSearchFields``
    member name, passed straight through to ``SearchExperiments``.
    """
    summaries = client.research.search_experiments(
        gateway_id=client.gateway_id,
        user_name=client.username,
        filters=filters or {},
        limit=limit,
        offset=offset,
    )
    return [
        WithAccess(
            message=e,
            is_owner=False,
            user_has_write_access=client.sharing.user_has_access(
                resource_id=e.experiment_id,
                user_id=client.username,
                permission_type="WRITE",
            ),
        )
        for e in summaries
    ]


# ExperimentStatistics — bare proto (already carries the full shape: six
# per-state counts plus six per-state summary lists; nothing cross-service).


def get_experiment_statistics(
    client: "AiravataClient",
    *,
    from_time: int,
    to_time: int,
    user_name: Optional[str] = None,
    application_name: Optional[str] = None,
    resource_host_name: Optional[str] = None,
    limit: int = 50,
    offset: int = 0,
) -> "experiment_pb2.ExperimentStatistics":
    """``from_time`` / ``to_time`` are epoch-millis bounds; the optional filters
    map to string fields (``None`` -> ``""``).
    """
    return client.research.get_experiment_statistics(
        gateway_id=client.gateway_id,
        from_time=from_time,
        to_time=to_time,
        user_name=user_name or "",
        application_name=application_name or "",
        resource_host_name=resource_host_name or "",
        limit=limit,
        offset=offset,
    )


# Notification — gateway-catalog WithAccess (is_owner always False;
# user_has_write_access = has_write). The portal-only show_in_dashboard flag
# lives in a Django table and is merged by the ViewSet, not here.


def _to_epoch_ms(value) -> int:
    """Timestamp -> epoch-millis int. ``None`` / ``""`` / ``0`` -> 0; int as-is;
    ISO-8601 string -> epoch millis.
    """
    if not value:
        return 0
    if isinstance(value, bool):
        return 0
    if isinstance(value, int):
        return value
    if isinstance(value, float):
        return int(value)
    # ISO-8601 string — normalise a trailing ``Z`` to ``+00:00`` for fromisoformat.
    s = str(value)
    if s.endswith("Z"):
        s = s[:-1] + "+00:00"
    dt = datetime.fromisoformat(s)
    if dt.tzinfo is None:
        dt = dt.replace(tzinfo=timezone.utc)
    return int(dt.timestamp() * 1000)


def _to_priority_int(value) -> int:
    """priority -> proto ``NotificationPriority`` int via the proto enum (member
    NAME or proto int; ``None`` / ``""`` -> 0).
    """
    from airavata_sdk.generated.org.apache.airavata.model.workspace import (
        workspace_pb2,
    )

    return proto_enum_value(workspace_pb2.NotificationPriority, value)


def _build_notification(
    client: "AiravataClient",
    data: dict,
    *,
    base: "Optional[workspace_pb2.Notification]" = None,
) -> "workspace_pb2.Notification":
    """*base* (the update path) seeds the proto; absent keys keep the base value
    or the proto default. ``gateway_id`` is forced to the client's gateway.
    """
    from airavata_sdk.generated.org.apache.airavata.model.workspace import (
        workspace_pb2,
    )

    n = workspace_pb2.Notification()
    if base is not None:
        n.CopyFrom(base)
    n.gateway_id = client.gateway_id or ""
    if "title" in data:
        n.title = data["title"] or ""
    if "notification_message" in data:
        n.notification_message = data["notification_message"] or ""
    if "creation_time" in data:
        n.creation_time = _to_epoch_ms(data["creation_time"])
    if "published_time" in data:
        n.published_time = _to_epoch_ms(data["published_time"])
    if "expiration_time" in data:
        n.expiration_time = _to_epoch_ms(data["expiration_time"])
    if "priority" in data:
        n.priority = _to_priority_int(data["priority"])
    return n


def get_notification(
    client: "AiravataClient",
    notification_id: str,
    *,
    has_write: bool,
) -> "WithAccess":
    n = client.research.get_notification(client.gateway_id, notification_id)
    return WithAccess(
        message=n,
        is_owner=False,
        user_has_write_access=has_write,
    )


def list_notifications(
    client: "AiravataClient",
    *,
    has_write: bool,
) -> "list[WithAccess]":
    notifications = client.research.get_all_notifications(client.gateway_id)
    return [
        WithAccess(message=n, is_owner=False, user_has_write_access=has_write)
        for n in notifications
    ]


def create_notification(
    client: "AiravataClient",
    data: "NotificationCreate | dict",
    *,
    has_write: bool,
) -> "WithAccess":
    """The created proto is re-built with the server-assigned ``notification_id``."""
    data = NotificationCreate.model_validate(data).model_dump(
        exclude_unset=True)
    n = _build_notification(client, data)
    notification_id = client.research.create_notification(n)
    n.notification_id = notification_id
    return WithAccess(
        message=n,
        is_owner=False,
        user_has_write_access=has_write,
    )


def update_notification(
    client: "AiravataClient",
    notification_id: str,
    data: "NotificationCreate | dict",
    *,
    has_write: bool,
) -> "WithAccess":
    data = NotificationCreate.model_validate(data).model_dump(
        exclude_unset=True)
    base = client.research.get_notification(client.gateway_id, notification_id)
    n = _build_notification(client, data, base=base)
    n.notification_id = notification_id
    client.research.update_notification(n)
    return WithAccess(
        message=n,
        is_owner=False,
        user_has_write_access=has_write,
    )


def delete_notification(
    client: "AiravataClient",
    notification_id: str,
) -> None:
    client.research.delete_notification(client.gateway_id, notification_id)


# Parser — bare proto, gateway catalog (no ownership/sharing).


def _io_type_value(value) -> int:
    """parser ``type`` -> proto ``IOType`` int via the proto enum (member NAME or
    proto int; ``None`` / ``""`` -> 0).
    """
    from airavata_sdk.generated.org.apache.airavata.model.appcatalog.parser import (  # noqa: E501
        parser_pb2,
    )

    return proto_enum_value(parser_pb2.IOType, value)


def _build_parser(
    client: "AiravataClient",
    data: dict,
) -> "parser_pb2.Parser":
    """``gateway_id`` is forced to the client's gateway."""
    from airavata_sdk.generated.org.apache.airavata.model.appcatalog.parser import (  # noqa: E501
        parser_pb2,
    )

    return parser_pb2.Parser(
        id=data.get("id") or "",
        image_name=data.get("image_name") or "",
        output_dir_path=data.get("output_dir_path") or "",
        input_dir_path=data.get("input_dir_path") or "",
        execution_command=data.get("execution_command") or "",
        gateway_id=client.gateway_id or "",
        input_files=[
            parser_pb2.ParserInput(
                id=i.get("id") or "",
                name=i.get("name") or "",
                required_input=bool(i.get("required_input", False)),
                parser_id=i.get("parser_id") or "",
                type=_io_type_value(i.get("type")),
            )
            for i in (data.get("input_files") or [])
        ],
        output_files=[
            parser_pb2.ParserOutput(
                id=o.get("id") or "",
                name=o.get("name") or "",
                required_output=bool(o.get("required_output", False)),
                parser_id=o.get("parser_id") or "",
                type=_io_type_value(o.get("type")),
            )
            for o in (data.get("output_files") or [])
        ],
    )


def get_parser(client: "AiravataClient", parser_id: str) -> "parser_pb2.Parser":
    """Fetch a single parser and return the raw ``parser_pb2.Parser`` proto."""
    return client.research.get_parser(parser_id, client.gateway_id)


def list_parsers(client: "AiravataClient") -> "list[parser_pb2.Parser]":
    """Return the gateway's parsers as a list of ``parser_pb2.Parser`` protos."""
    return list(client.research.list_all_parsers(client.gateway_id))


def create_parser(
    client: "AiravataClient",
    data: "ParserCreate | dict",
) -> "parser_pb2.Parser":
    """Re-fetched so read and write paths emit the same shape."""
    data = ParserCreate.model_validate(data).model_dump(exclude_unset=True)
    parser = _build_parser(client, data)
    parser_id = client.research.save_parser(parser)
    return get_parser(client, parser_id)


def update_parser(
    client: "AiravataClient",
    parser_id: str,
    data: "ParserCreate | dict",
) -> "parser_pb2.Parser":
    """The proto is re-assembled from *data* (``id`` forced), saved, re-fetched."""
    data = ParserCreate.model_validate(data).model_dump(exclude_unset=True)
    merged = dict(data)
    merged["id"] = parser_id
    parser = _build_parser(client, merged)
    client.research.save_parser(parser)
    return get_parser(client, parser_id)


def delete_parser(client: "AiravataClient", parser_id: str) -> None:
    """Delete a parser by id."""
    client.research.remove_parser(parser_id, client.gateway_id)


# DataProduct — WithAccess. is_owner is SDK-trivial
# (proto.owner_name == client.username); user_has_write_access is the
# request-bound flag the ViewSet computes (owner / shared-dir gateway-admin /
# otherwise-allowed) and supplies as has_write.


def get_data_product(
    client: "AiravataClient",
    product_uri: str,
    *,
    has_write: bool,
) -> "WithAccess":
    p = client.research.get_data_product(product_uri)
    return WithAccess(
        message=p,
        is_owner=bool(p.owner_name) and (p.owner_name == client.username),
        user_has_write_access=has_write,
    )


def data_product_for_upload(
    *,
    gateway_id: str,
    owner_name: str,
    product_name: str,
    file_path: str,
    storage_resource_id: str,
    content_type: Optional[str] = None,
    product_size: int = 0,
) -> "replica_catalog_pb2.DataProductModel":
    """``storage.upload_file`` only transfers bytes, so the upload flow registers
    the product to mint a canonical URI. A single GATEWAY_DATA_STORE / TRANSIENT
    replica points at *file_path*; content type goes under ``mime-type`` metadata.
    """
    from airavata_sdk.generated.org.apache.airavata.model.data.replica import (
        replica_catalog_pb2 as rc,
    )

    product_metadata = {"mime-type": content_type} if content_type else {}
    return rc.DataProductModel(
        gateway_id=gateway_id or "",
        owner_name=owner_name or "",
        product_name=product_name or "",
        data_product_type=rc.DataProductType.FILE,
        product_size=product_size or 0,
        product_metadata=product_metadata,
        replica_locations=[rc.DataReplicaLocationModel(
            replica_name="{} gateway data store copy".format(product_name),
            replica_location_category=rc.ReplicaLocationCategory.GATEWAY_DATA_STORE,
            replica_persistent_type=rc.ReplicaPersistentType.TRANSIENT,
            storage_resource_id=storage_resource_id or "",
            file_path=file_path,
        )],
    )


def register_data_product(
    client: "AiravataClient",
    data_product: "replica_catalog_pb2.DataProductModel",
) -> str:
    """Pass-through used by the upload flow after :func:`data_product_for_upload`."""
    return client.research.register_data_product(data_product)


# ApplicationInterface — gateway-catalog WithAccess (is_owner always False;
# user_has_write_access = has_write). The portal-only show_queue_settings /
# queue_settings_calculator_id overrides are persisted by the ViewSet, not here.


def get_application_interface(
    client: "AiravataClient",
    app_interface_id: str,
    *,
    has_write: bool,
) -> "WithAccess":
    ai = client.research.get_application_interface(app_interface_id)
    return WithAccess(
        message=ai,
        is_owner=False,
        user_has_write_access=has_write,
    )


def list_application_interfaces(
    client: "AiravataClient",
    *,
    has_write: bool,
) -> "list[WithAccess]":
    interfaces = client.research.get_all_application_interfaces(
        client.gateway_id)
    return [
        WithAccess(message=ai, is_owner=False, user_has_write_access=has_write)
        for ai in interfaces
    ]


def _proto_input_data_object(data: dict):
    from airavata_sdk.generated.org.apache.airavata.model.application.io import (
        application_io_pb2 as io,
    )

    return io.InputDataObjectType(
        name=data.get("name") or "",
        value=data.get("value") or "",
        type=_data_type_int(data.get("type")),
        application_argument=data.get("application_argument") or "",
        standard_input=bool(data.get("standard_input", False)),
        user_friendly_description=data.get("user_friendly_description") or "",
        meta_data=_meta_data_str(data.get("meta_data")),
        input_order=data.get("input_order") or 0,
        is_required=bool(data.get("is_required", False)),
        required_to_added_to_command_line=bool(
            data.get("required_to_added_to_command_line", False)),
        data_staged=bool(data.get("data_staged", False)),
        storage_resource_id=data.get("storage_resource_id") or "",
        is_read_only=bool(data.get("is_read_only", False)),
        override_filename=data.get("override_filename") or "",
    )


def _proto_output_data_object(data: dict):
    from airavata_sdk.generated.org.apache.airavata.model.application.io import (
        application_io_pb2 as io,
    )

    return io.OutputDataObjectType(
        name=data.get("name") or "",
        value=data.get("value") or "",
        type=_data_type_int(data.get("type")),
        application_argument=data.get("application_argument") or "",
        is_required=bool(data.get("is_required", False)),
        required_to_added_to_command_line=bool(
            data.get("required_to_added_to_command_line", False)),
        data_movement=bool(data.get("data_movement", False)),
        location=data.get("location") or "",
        search_query=data.get("search_query") or "",
        output_streaming=bool(data.get("output_streaming", False)),
        storage_resource_id=data.get("storage_resource_id") or "",
        meta_data=_meta_data_str(data.get("meta_data")),
    )


def _data_type_int(value) -> int:
    """``DataType`` -> proto enum int via the proto enum (member NAME or proto
    int; ``None`` / ``""`` -> 0).
    """
    from airavata_sdk.generated.org.apache.airavata.model.application.io import (
        application_io_pb2 as io,
    )

    return proto_enum_value(io.DataType, value)


def _meta_data_str(value) -> str:
    """``meta_data`` -> proto JSON string. A str passes through; other values are
    ``json.dumps``ed; ``None`` -> ``""``.
    """
    import json

    if value is None:
        return ""
    if isinstance(value, str):
        return value
    try:
        return json.dumps(value)
    except (TypeError, ValueError):
        return ""


def _build_application_interface(
    client: "AiravataClient",
    data: dict,
    *,
    base: "Optional[app_interface_pb2.ApplicationInterfaceDescription]" = None,
) -> "app_interface_pb2.ApplicationInterfaceDescription":
    """*base* (the update path) seeds the proto. Nested input/output lists are
    replaced wholesale when present in *data*.
    """
    from airavata_sdk.generated.org.apache.airavata.model.appcatalog.appinterface import (  # noqa: E501
        app_interface_pb2 as ai_pb2,
    )

    ai = ai_pb2.ApplicationInterfaceDescription()
    if base is not None:
        ai.CopyFrom(base)
    if "application_interface_id" in data:
        ai.application_interface_id = data["application_interface_id"] or ""
    if "application_name" in data:
        ai.application_name = data["application_name"] or ""
    if "application_description" in data:
        ai.application_description = data["application_description"] or ""
    if "application_modules" in data:
        ai.application_modules[:] = list(data["application_modules"] or [])
    if "archive_working_directory" in data:
        ai.archive_working_directory = bool(data["archive_working_directory"])
    if "application_inputs" in data:
        del ai.application_inputs[:]
        ai.application_inputs.extend(
            _proto_input_data_object(i)
            for i in (data["application_inputs"] or []))
    if "application_outputs" in data:
        del ai.application_outputs[:]
        ai.application_outputs.extend(
            _proto_output_data_object(o)
            for o in (data["application_outputs"] or []))
    return ai


def create_application_interface(
    client: "AiravataClient",
    data: "ApplicationInterfaceCreate | dict",
    *,
    has_write: bool,
) -> "WithAccess":
    """Re-fetched so the server-assigned ``application_interface_id`` is populated."""
    data = ApplicationInterfaceCreate.model_validate(data).model_dump(
        exclude_unset=True)
    ai = _build_application_interface(client, data)
    app_interface_id = client.research.register_application_interface(
        client.gateway_id, ai)
    return get_application_interface(
        client, app_interface_id, has_write=has_write)


def update_application_interface(
    client: "AiravataClient",
    app_interface_id: str,
    data: "ApplicationInterfaceCreate | dict",
    *,
    has_write: bool,
) -> "WithAccess":
    data = ApplicationInterfaceCreate.model_validate(data).model_dump(
        exclude_unset=True)
    base = client.research.get_application_interface(app_interface_id)
    ai = _build_application_interface(client, data, base=base)
    ai.application_interface_id = app_interface_id
    client.research.update_application_interface(app_interface_id, ai)
    return get_application_interface(
        client, app_interface_id, has_write=has_write)


def delete_application_interface(
    client: "AiravataClient",
    app_interface_id: str,
) -> None:
    """Delete an application interface by id."""
    client.research.delete_application_interface(app_interface_id)


# ApplicationDeployment — WithAccess. is_owner always False (catalog entry, no
# owner). Unlike the other gateway-catalog families, user_has_write_access is a
# genuine per-resource CHAINED sharing WRITE lookup keyed on app_deployment_id.


def get_application_deployment(
    client: "AiravataClient",
    app_deployment_id: str,
) -> "WithAccess":
    d = client.research.get_application_deployment(app_deployment_id)
    return WithAccess(
        message=d,
        is_owner=False,
        user_has_write_access=client.sharing.user_has_access(
            resource_id=app_deployment_id,
            user_id=client.username,
            permission_type="WRITE",
        ),
    )


def list_application_deployments(
    client: "AiravataClient",
) -> "list[WithAccess]":
    deployments = client.research.get_accessible_application_deployments(
        client.gateway_id)
    return [
        WithAccess(
            message=d,
            is_owner=False,
            user_has_write_access=client.sharing.user_has_access(
                resource_id=d.app_deployment_id,
                user_id=client.username,
                permission_type="WRITE",
            ),
        )
        for d in deployments
    ]


def list_application_deployments_for_module(
    client: "AiravataClient",
    app_module_id: str,
) -> "list[WithAccess]":
    """Accessible deployments for a single app module (the gateway-wide
    accessible set filtered by ``app_module_id``)."""
    deployments = client.research.get_accessible_application_deployments(
        client.gateway_id)
    return [
        WithAccess(
            message=d,
            is_owner=False,
            user_has_write_access=client.sharing.user_has_access(
                resource_id=d.app_deployment_id,
                user_id=client.username,
                permission_type="WRITE",
            ),
        )
        for d in deployments
        if d.app_module_id == app_module_id
    ]


def list_application_deployments_for_module_and_profile(
    client: "AiravataClient",
    app_module_id: str,
    group_resource_profile_id: str,
) -> "list[WithAccess]":
    deployments = client.research.\
        get_application_deployments_for_app_module_and_group_resource_profile(
            app_module_id, group_resource_profile_id)
    return [
        WithAccess(
            message=d,
            is_owner=False,
            user_has_write_access=client.sharing.user_has_access(
                resource_id=d.app_deployment_id,
                user_id=client.username,
                permission_type="WRITE",
            ),
        )
        for d in deployments
    ]


def _proto_command_object(data: dict):
    from airavata_sdk.generated.org.apache.airavata.model.appcatalog.appdeployment import (  # noqa: E501
        app_deployment_pb2,
    )

    return app_deployment_pb2.CommandObject(
        command=data.get("command") or "",
        command_order=data.get("command_order") or 0,
    )


def _proto_set_env_paths(data: dict):
    from airavata_sdk.generated.org.apache.airavata.model.appcatalog.appdeployment import (  # noqa: E501
        app_deployment_pb2,
    )

    return app_deployment_pb2.SetEnvPaths(
        name=data.get("name") or "",
        value=data.get("value") or "",
        env_path_order=data.get("env_path_order") or 0,
    )


def _parallelism_input_to_proto_int(value) -> int:
    """``parallelism`` -> proto ``ApplicationParallelismType`` int via the proto
    enum (member NAME or proto int; ``None`` / ``""`` -> 0).
    """
    from airavata_sdk.generated.org.apache.airavata.model.parallelism import (
        parallelism_pb2,
    )

    return proto_enum_value(parallelism_pb2.ApplicationParallelismType, value)


def _build_application_deployment(
    client: "AiravataClient",
    data: dict,
) -> "app_deployment_pb2.ApplicationDeploymentDescription":
    from airavata_sdk.generated.org.apache.airavata.model.appcatalog.appdeployment import (  # noqa: E501
        app_deployment_pb2,
    )

    return app_deployment_pb2.ApplicationDeploymentDescription(
        app_module_id=data.get("app_module_id") or "",
        compute_host_id=data.get("compute_host_id") or "",
        executable_path=data.get("executable_path") or "",
        parallelism=_parallelism_input_to_proto_int(data.get("parallelism")),
        app_deployment_description=data.get("app_deployment_description") or "",
        module_load_cmds=[
            _proto_command_object(c)
            for c in (data.get("module_load_cmds") or [])
        ],
        lib_prepend_paths=[
            _proto_set_env_paths(p)
            for p in (data.get("lib_prepend_paths") or [])
        ],
        lib_append_paths=[
            _proto_set_env_paths(p)
            for p in (data.get("lib_append_paths") or [])
        ],
        set_environment=[
            _proto_set_env_paths(p)
            for p in (data.get("set_environment") or [])
        ],
        pre_job_commands=[
            _proto_command_object(c)
            for c in (data.get("pre_job_commands") or [])
        ],
        post_job_commands=[
            _proto_command_object(c)
            for c in (data.get("post_job_commands") or [])
        ],
        default_queue_name=data.get("default_queue_name") or "",
        default_node_count=data.get("default_node_count") or 0,
        default_cpu_count=data.get("default_cpu_count") or 0,
        default_walltime=data.get("default_walltime") or 0,
        editable_by_user=bool(data.get("editable_by_user", False)),
    )


def create_application_deployment(
    client: "AiravataClient",
    data: "ApplicationDeploymentCreate | dict",
    *,
    has_write: bool,
) -> "WithAccess":
    """Re-fetched so the server-assigned ``app_deployment_id`` is populated.
    *has_write* is forwarded (the creator has write access) rather than chained.
    """
    data = ApplicationDeploymentCreate.model_validate(data).model_dump(
        exclude_unset=True)
    deployment = _build_application_deployment(client, data)
    app_deployment_id = client.research.register_application_deployment(
        client.gateway_id, deployment)
    created = client.research.get_application_deployment(app_deployment_id)
    return WithAccess(
        message=created, is_owner=False, user_has_write_access=has_write)


def update_application_deployment(
    client: "AiravataClient",
    app_deployment_id: str,
    data: "ApplicationDeploymentCreate | dict",
    *,
    has_write: bool,
) -> "WithAccess":
    """The proto is rebuilt wholesale from *data* (``app_deployment_id`` forced),
    pushed, and re-fetched. *has_write* is forwarded (the ViewSet resolves it).
    """
    data = ApplicationDeploymentCreate.model_validate(data).model_dump(
        exclude_unset=True)
    deployment = _build_application_deployment(client, data)
    deployment.app_deployment_id = app_deployment_id
    client.research.update_application_deployment(
        app_deployment_id, deployment)
    updated = client.research.get_application_deployment(app_deployment_id)
    return WithAccess(
        message=updated, is_owner=False, user_has_write_access=has_write)


def delete_application_deployment(
    client: "AiravataClient",
    app_deployment_id: str,
) -> None:
    """Delete an application deployment by id."""
    client.research.delete_application_deployment(app_deployment_id)
