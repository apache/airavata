"""Unit tests for airavata_sdk.helpers.research_resources.

PROJECT FAMILY (proto-direct reference)
---------------------------------------
The project family follows the proto-direct architecture: the helpers return
the raw ``workspace_pb2.Project`` proto unioned with the caller's sharing-access
flags in a :class:`~airavata_sdk.helpers._envelope.WithAccess` container.  The
tests below assert that ``get_project`` / ``list_projects`` / ``create_project``
/ ``update_project`` return a ``WithAccess`` carrying the proto message plus the
two bools (``is_owner`` / ``user_has_write_access``), and that the chained
``sharing.user_has_access`` call is made with the right arguments.  There is no
dict transform to test — the portal's generic renderer flattens the proto.

Other families still use the legacy dict path and are tested via a lightweight
stub client that records the calls made.
"""

from airavata_sdk.helpers._envelope import WithAccess
from airavata_sdk.helpers.research_resources import (
    create_application_module,
    create_notification,
    create_project,
    delete_notification,
    get_application_module,
    get_experiment_statistics,
    get_notification,
    get_project,
    list_application_modules,
    list_notifications,
    list_projects,
    search_experiments,
    update_application_module,
    update_notification,
    update_project,
)


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def _make_project(**kwargs):
    from airavata_sdk.generated.org.apache.airavata.model.workspace import workspace_pb2
    return workspace_pb2.Project(**kwargs)


# ---------------------------------------------------------------------------
# Project family — proto-direct + WithAccess (stub client)
# ---------------------------------------------------------------------------

class _FakeResearch:
    def __init__(self, project):
        self._project = project
        self.created_project = None
        self.updated_project = None
        self.created_gateway_id = None

    def get_project(self, project_id):
        return self._project

    def get_user_projects(self, gateway_id, user_name, limit=-1, offset=0):
        return [self._project]

    def create_project(self, gateway_id, project):
        self.created_project = project
        self.created_gateway_id = gateway_id
        return "new-proj-id"

    def update_project(self, project_id, project):
        self.updated_project = project


class _FakeSharing:
    def __init__(self, has_access=True):
        self._has_access = has_access
        self.calls = []

    def user_has_access(self, resource_id, user_id, permission_type):
        self.calls.append((resource_id, user_id, permission_type))
        return self._has_access


class _FakeClient:
    def __init__(self, project, gateway_id="gw1", username="alice",
                 sharing_has_access=True):
        self.research = _FakeResearch(project)
        self.sharing = _FakeSharing(sharing_has_access)
        self.gateway_id = gateway_id
        self.username = username


class TestGetProject:
    """``get_project`` returns ``WithAccess[Project]`` — the raw proto plus the
    two sharing-access bools.  No dict transform; the proto flows through."""

    def _project(self):
        return _make_project(
            project_id="proj-1", owner="alice", gateway_id="gw1",
            name="Test", creation_time=1705320000000,
        )

    def test_returns_with_access_container(self):
        client = _FakeClient(self._project())
        result = get_project(client, "proj-1")
        assert isinstance(result, WithAccess)

    def test_message_is_the_proto(self):
        p = self._project()
        client = _FakeClient(p)
        result = get_project(client, "proj-1")
        # The exact proto object flows through wholesale — not copied.
        assert result.message is p
        assert result.message.project_id == "proj-1"
        assert result.message.owner == "alice"

    def test_is_owner_true_when_username_matches(self):
        client = _FakeClient(self._project(), username="alice")
        result = get_project(client, "proj-1")
        assert result.is_owner is True

    def test_is_owner_false_when_username_differs(self):
        client = _FakeClient(self._project(), username="bob")
        result = get_project(client, "proj-1")
        assert result.is_owner is False

    def test_user_has_write_access_forwarded_from_sharing(self):
        client = _FakeClient(self._project(), sharing_has_access=False)
        result = get_project(client, "proj-1")
        assert result.user_has_write_access is False

    def test_user_has_write_access_true_from_sharing(self):
        client = _FakeClient(self._project(), sharing_has_access=True)
        result = get_project(client, "proj-1")
        assert result.user_has_write_access is True

    def test_sharing_called_with_correct_args(self):
        client = _FakeClient(self._project(), username="alice")
        get_project(client, "proj-1")
        assert len(client.sharing.calls) == 1
        resource_id, user_id, perm = client.sharing.calls[0]
        assert resource_id == "proj-1"
        assert user_id == "alice"
        assert perm == "WRITE"


class TestListProjects:
    def _project(self):
        return _make_project(
            project_id="proj-2", owner="alice", gateway_id="gw1",
            name="Listed Project", creation_time=1705320000000,
        )

    def test_returns_list_of_with_access(self):
        client = _FakeClient(self._project())
        results = list_projects(client)
        assert len(results) == 1
        assert isinstance(results[0], WithAccess)

    def test_message_carries_proto_and_flags(self):
        client = _FakeClient(self._project())
        result = list_projects(client)[0]
        assert result.message.project_id == "proj-2"
        assert result.is_owner is True
        assert result.user_has_write_access is True

    def test_passes_limit_and_offset(self):
        p = self._project()
        client = _FakeClient(p)
        # Overwrite get_user_projects to capture args
        calls = []
        original = client.research.get_user_projects

        def capturing(gateway_id, user_name, limit=-1, offset=0):
            calls.append((gateway_id, user_name, limit, offset))
            return original(gateway_id, user_name, limit, offset)

        client.research.get_user_projects = capturing
        list_projects(client, limit=10, offset=5)
        assert calls[0][2] == 10
        assert calls[0][3] == 5


class TestCreateProject:
    def test_returns_with_access_after_create(self):
        p = _make_project(project_id="new-proj-id", owner="alice",
                          gateway_id="gw1", name="New Project")
        client = _FakeClient(p)
        result = create_project(
            client, {"name": "New Project", "description": "Desc"})
        assert isinstance(result, WithAccess)
        assert result.message.project_id == "new-proj-id"

    def test_sets_owner_from_client(self):
        p = _make_project(project_id="new-proj-id", owner="alice",
                          gateway_id="gw1", name="New Project")
        client = _FakeClient(p, username="alice")
        create_project(client, {"name": "New Project"})
        assert client.research.created_project.owner == "alice"

    def test_sets_gateway_id_from_client(self):
        p = _make_project(project_id="new-proj-id", owner="alice",
                          gateway_id="gw1", name="New Project")
        client = _FakeClient(p, gateway_id="gw1")
        create_project(client, {"name": "New Project"})
        assert client.research.created_gateway_id == "gw1"


class TestUpdateProject:
    def _base_project(self):
        return _make_project(
            project_id="proj-3", owner="alice", gateway_id="gw1",
            name="Old Name", description="Old Desc",
        )

    def test_updates_name(self):
        client = _FakeClient(self._base_project())
        update_project(client, "proj-3", {"name": "New Name"})
        assert client.research.updated_project.name == "New Name"

    def test_updates_description(self):
        client = _FakeClient(self._base_project())
        update_project(client, "proj-3", {"description": "New Desc"})
        assert client.research.updated_project.description == "New Desc"

    def test_returns_with_access(self):
        p = self._base_project()
        client = _FakeClient(p)
        result = update_project(client, "proj-3", {"name": "Updated"})
        assert isinstance(result, WithAccess)
        assert result.message.project_id == "proj-3"


# ===========================================================================
# ApplicationModule
# ===========================================================================

def _make_app_module(**kwargs):
    from airavata_sdk.generated.org.apache.airavata.model.appcatalog.appdeployment import (  # noqa: E501
        app_deployment_pb2,
    )
    return app_deployment_pb2.ApplicationModule(**kwargs)


# The application-module family follows the proto-direct architecture
# (gateway-catalog variant): the helpers return the raw
# ``app_deployment_pb2.ApplicationModule`` proto wrapped in a ``WithAccess``
# container.  Unlike the project family there is no ownership concept — the
# proto has no ``owner`` field — so ``is_owner`` is always ``False`` and
# ``user_has_write_access`` is the gateway-admin flag the ViewSet passes in as
# ``has_write`` (NOT a chained ``sharing.user_has_access`` call).  There is no
# dict transform to test; the portal's generic renderer flattens the proto.


class _FakeAppResearch:
    def __init__(self, module):
        self._module = module
        self.registered_module = None
        self.registered_gateway_id = None
        self.updated_module = None
        self.updated_id = None

    def get_application_module(self, app_module_id):
        return self._module

    def get_accessible_app_modules(self, gateway_id):
        return [self._module]

    def get_all_app_modules(self, gateway_id):
        return [self._module]

    def register_application_module(self, gateway_id, application_module):
        self.registered_module = application_module
        self.registered_gateway_id = gateway_id
        return "new-mod-id"

    def update_application_module(self, app_module_id, application_module):
        self.updated_id = app_module_id
        self.updated_module = application_module


class _FakeAppClient:
    def __init__(self, module, gateway_id="gw1", username="alice"):
        self.research = _FakeAppResearch(module)
        self.gateway_id = gateway_id
        self.username = username


class TestGetApplicationModule:
    """``get_application_module`` returns ``WithAccess[ApplicationModule]`` — the
    raw proto plus the gateway-catalog access flags (``is_owner`` always
    ``False``; ``user_has_write_access`` forwarded from ``has_write``)."""

    def _module(self):
        return _make_app_module(
            app_module_id="mod-1", app_module_name="App",
            app_module_version="1.0", app_module_description="d",
        )

    def test_returns_with_access_container(self):
        client = _FakeAppClient(self._module())
        result = get_application_module(client, "mod-1", has_write=True)
        assert isinstance(result, WithAccess)

    def test_message_is_the_proto(self):
        m = self._module()
        client = _FakeAppClient(m)
        result = get_application_module(client, "mod-1", has_write=True)
        # The exact proto object flows through wholesale — not copied.
        assert result.message is m
        assert result.message.app_module_id == "mod-1"

    def test_is_owner_always_false(self):
        client = _FakeAppClient(self._module())
        result = get_application_module(client, "mod-1", has_write=True)
        assert result.is_owner is False

    def test_has_write_forwarded_true(self):
        client = _FakeAppClient(self._module())
        result = get_application_module(client, "mod-1", has_write=True)
        assert result.user_has_write_access is True

    def test_has_write_forwarded_false(self):
        client = _FakeAppClient(self._module())
        result = get_application_module(client, "mod-1", has_write=False)
        assert result.user_has_write_access is False


class TestListApplicationModules:
    def _module(self):
        return _make_app_module(
            app_module_id="mod-2", app_module_name="Listed",
        )

    def test_accessible_only_uses_accessible_facade(self):
        client = _FakeAppClient(self._module())
        calls = []
        client.research.get_accessible_app_modules = lambda gateway_id: (
            calls.append(("accessible", gateway_id)) or [self._module()])
        client.research.get_all_app_modules = lambda gateway_id: (
            calls.append(("all", gateway_id)) or [self._module()])
        list_application_modules(client, has_write=True, accessible_only=True)
        assert calls[0][0] == "accessible"

    def test_all_uses_all_facade(self):
        client = _FakeAppClient(self._module())
        calls = []
        client.research.get_accessible_app_modules = lambda gateway_id: (
            calls.append(("accessible", gateway_id)) or [self._module()])
        client.research.get_all_app_modules = lambda gateway_id: (
            calls.append(("all", gateway_id)) or [self._module()])
        list_application_modules(client, has_write=True, accessible_only=False)
        assert calls[0][0] == "all"

    def test_returns_list_of_with_access(self):
        client = _FakeAppClient(self._module())
        results = list_application_modules(client, has_write=True)
        assert len(results) == 1
        assert isinstance(results[0], WithAccess)

    def test_message_carries_proto_and_flags(self):
        client = _FakeAppClient(self._module())
        result = list_application_modules(client, has_write=True)[0]
        assert result.message.app_module_id == "mod-2"
        assert result.is_owner is False
        assert result.user_has_write_access is True


class TestCreateApplicationModule:
    def test_returns_with_access_after_create(self):
        m = _make_app_module(
            app_module_id="new-mod-id", app_module_name="New")
        client = _FakeAppClient(m)
        result = create_application_module(
            client, {"app_module_name": "New", "app_module_version": "1"},
            has_write=True)
        assert isinstance(result, WithAccess)
        assert result.message.app_module_id == "new-mod-id"

    def test_sets_fields_on_proto(self):
        m = _make_app_module(app_module_id="new-mod-id", app_module_name="New")
        client = _FakeAppClient(m)
        create_application_module(
            client,
            {"app_module_name": "New", "app_module_version": "2",
             "app_module_description": "desc"},
            has_write=True)
        reg = client.research.registered_module
        assert reg.app_module_name == "New"
        assert reg.app_module_version == "2"
        assert reg.app_module_description == "desc"

    def test_uses_client_gateway_id(self):
        m = _make_app_module(app_module_id="new-mod-id", app_module_name="New")
        client = _FakeAppClient(m, gateway_id="gw1")
        create_application_module(
            client, {"app_module_name": "New"}, has_write=True)
        assert client.research.registered_gateway_id == "gw1"


class TestUpdateApplicationModule:
    def _base_module(self):
        return _make_app_module(
            app_module_id="mod-3", app_module_name="Old",
            app_module_version="1", app_module_description="old",
        )

    def test_updates_name(self):
        client = _FakeAppClient(self._base_module())
        update_application_module(
            client, "mod-3", {"app_module_name": "New"}, has_write=True)
        assert client.research.updated_module.app_module_name == "New"

    def test_updates_version_and_description(self):
        client = _FakeAppClient(self._base_module())
        update_application_module(
            client, "mod-3",
            {"app_module_version": "2", "app_module_description": "new"},
            has_write=True)
        assert client.research.updated_module.app_module_version == "2"
        assert client.research.updated_module.app_module_description == "new"

    def test_returns_with_access(self):
        client = _FakeAppClient(self._base_module())
        result = update_application_module(
            client, "mod-3", {"app_module_name": "Updated"}, has_write=True)
        assert isinstance(result, WithAccess)
        assert result.message.app_module_id == "mod-3"


# ===========================================================================
# ExperimentSummary (experiment-search)
# ===========================================================================

def _make_experiment_summary(**kwargs):
    from airavata_sdk.generated.org.apache.airavata.model.experiment import (
        experiment_pb2,
    )
    return experiment_pb2.ExperimentSummaryModel(**kwargs)


class _FakeExpResearch:
    def __init__(self, summaries):
        self._summaries = summaries
        self.search_args = None

    def search_experiments(self, gateway_id, user_name, filters=None,
                           limit=-1, offset=0):
        self.search_args = (gateway_id, user_name, filters, limit, offset)
        return list(self._summaries)


class _FakeExpClient:
    def __init__(self, summaries, gateway_id="gw1", username="alice",
                 sharing_has_access=True):
        self.research = _FakeExpResearch(summaries)
        self.sharing = _FakeSharing(sharing_has_access)
        self.gateway_id = gateway_id
        self.username = username


class TestSearchExperiments:
    """``search_experiments`` returns ``list[WithAccess[ExperimentSummaryModel]]``
    — each raw proto unioned with the two sharing-access bools.  No dict
    transform; the proto flows through wholesale.  ``is_owner`` is always
    ``False`` (the summary proto has no ``owner`` field) and
    ``user_has_write_access`` is a per-experiment chained sharing WRITE lookup."""

    def _summary(self):
        return _make_experiment_summary(
            experiment_id="exp-1", project_id="proj-1", gateway_id="gw1",
            name="Searched", creation_time=1705320000000,
            experiment_status="EXECUTING",
        )

    def test_returns_list_of_with_access(self):
        client = _FakeExpClient([self._summary()])
        results = search_experiments(client)
        assert len(results) == 1
        assert isinstance(results[0], WithAccess)

    def test_message_is_the_proto(self):
        s = self._summary()
        client = _FakeExpClient([s])
        result = search_experiments(client)[0]
        # The exact proto object flows through wholesale — not copied.
        assert result.message is s
        assert result.message.experiment_id == "exp-1"
        assert result.message.experiment_status == "EXECUTING"

    def test_is_owner_always_false(self):
        """ExperimentSummaryModel has no owner field; is_owner is trivially
        False (matching the legacy serializer, which had no isOwner flag)."""
        client = _FakeExpClient([self._summary()], username="alice")
        result = search_experiments(client)[0]
        assert result.is_owner is False

    def test_user_has_write_access_true_from_sharing(self):
        client = _FakeExpClient([self._summary()], sharing_has_access=True)
        result = search_experiments(client)[0]
        assert result.user_has_write_access is True

    def test_write_access_forwarded_false(self):
        client = _FakeExpClient([self._summary()], sharing_has_access=False)
        result = search_experiments(client)[0]
        assert result.user_has_write_access is False

    def test_passes_gateway_and_username_and_filters(self):
        client = _FakeExpClient([self._summary()], gateway_id="gw1",
                                username="alice")
        search_experiments(
            client, filters={"USER_NAME": "alice"}, limit=10, offset=5)
        gw, user, filters, limit, offset = client.research.search_args
        assert gw == "gw1"
        assert user == "alice"
        assert filters == {"USER_NAME": "alice"}
        assert limit == 10
        assert offset == 5

    def test_none_filters_becomes_empty_dict(self):
        client = _FakeExpClient([self._summary()])
        search_experiments(client)
        _, _, filters, _, _ = client.research.search_args
        assert filters == {}

    def test_sharing_keyed_on_experiment_id(self):
        client = _FakeExpClient([self._summary()], username="alice")
        search_experiments(client)
        assert len(client.sharing.calls) == 1
        resource_id, user_id, perm = client.sharing.calls[0]
        assert resource_id == "exp-1"
        assert user_id == "alice"
        assert perm == "WRITE"


# ===========================================================================
# ExperimentStatistics (experiment-statistics — proto-direct)
# ===========================================================================
#
# Proto-direct: ``get_experiment_statistics`` returns the
# ``experiment_pb2.ExperimentStatistics`` proto WHOLESALE.  The proto already
# carries the full shape (six per-state counts + six per-state summary lists of
# ExperimentSummaryModel) and nothing cross-service is computed, so there is no
# dict transform and no WithAccess/pydantic wrapper to test — only that the proto
# is returned as-is and the facade is called with the right arguments.

def _make_experiment_statistics(**kwargs):
    from airavata_sdk.generated.org.apache.airavata.model.experiment import (
        experiment_pb2,
    )
    return experiment_pb2.ExperimentStatistics(**kwargs)


class _FakeStatsResearch:
    def __init__(self, stats):
        self._stats = stats
        self.call_args = None

    def get_experiment_statistics(self, gateway_id, from_time, to_time,
                                  user_name="", application_name="",
                                  resource_host_name="", limit=0, offset=0):
        self.call_args = dict(
            gateway_id=gateway_id, from_time=from_time, to_time=to_time,
            user_name=user_name, application_name=application_name,
            resource_host_name=resource_host_name, limit=limit, offset=offset)
        return self._stats


class _FakeStatsClient:
    def __init__(self, stats, gateway_id="gw1", username="alice"):
        self.research = _FakeStatsResearch(stats)
        self.gateway_id = gateway_id
        self.username = username


class TestGetExperimentStatistics:
    def _summary(self, **kwargs):
        base = dict(
            experiment_id="exp-1", project_id="proj-1", gateway_id="gw1",
            name="E", creation_time=1705320000000,
            experiment_status="COMPLETED",
        )
        base.update(kwargs)
        return _make_experiment_summary(**base)

    def _full_stats(self):
        return _make_experiment_statistics(
            all_experiment_count=10,
            completed_experiment_count=4,
            cancelled_experiment_count=1,
            failed_experiment_count=2,
            created_experiment_count=2,
            running_experiment_count=1,
            all_experiments=[self._summary(experiment_id="all-1")],
            completed_experiments=[self._summary(experiment_id="comp-1")],
            failed_experiments=[self._summary(experiment_id="fail-1")],
            cancelled_experiments=[self._summary(experiment_id="canc-1")],
            created_experiments=[self._summary(experiment_id="crea-1")],
            running_experiments=[self._summary(experiment_id="run-1")],
        )

    def _stats(self):
        return _make_experiment_statistics(all_experiment_count=3)

    def test_returns_the_proto_directly(self):
        """No dict transform, no wrapper — the proto flows through wholesale."""
        from airavata_sdk.generated.org.apache.airavata.model.experiment import (
            experiment_pb2,
        )
        stats = self._stats()
        client = _FakeStatsClient(stats)
        result = get_experiment_statistics(client, from_time=1000, to_time=2000)
        assert isinstance(result, experiment_pb2.ExperimentStatistics)
        assert result is stats

    def test_proto_carries_counts_and_summary_lists(self):
        client = _FakeStatsClient(self._full_stats())
        result = get_experiment_statistics(client, from_time=1, to_time=2)
        assert result.all_experiment_count == 10
        assert result.completed_experiment_count == 4
        assert result.cancelled_experiment_count == 1
        assert result.failed_experiment_count == 2
        assert result.created_experiment_count == 2
        assert result.running_experiment_count == 1
        assert result.all_experiments[0].experiment_id == "all-1"
        assert result.completed_experiments[0].experiment_id == "comp-1"
        assert result.failed_experiments[0].experiment_id == "fail-1"
        assert result.cancelled_experiments[0].experiment_id == "canc-1"
        assert result.created_experiments[0].experiment_id == "crea-1"
        assert result.running_experiments[0].experiment_id == "run-1"

    def test_passes_bounds_and_gateway(self):
        client = _FakeStatsClient(self._stats(), gateway_id="gw1")
        get_experiment_statistics(
            client, from_time=1000, to_time=2000, limit=25, offset=5)
        args = client.research.call_args
        assert args["gateway_id"] == "gw1"
        assert args["from_time"] == 1000
        assert args["to_time"] == 2000
        assert args["limit"] == 25
        assert args["offset"] == 5

    def test_none_filters_normalised_to_empty_string(self):
        client = _FakeStatsClient(self._stats())
        get_experiment_statistics(
            client, from_time=1000, to_time=2000,
            user_name=None, application_name=None, resource_host_name=None)
        args = client.research.call_args
        assert args["user_name"] == ""
        assert args["application_name"] == ""
        assert args["resource_host_name"] == ""

    def test_filters_forwarded(self):
        client = _FakeStatsClient(self._stats())
        get_experiment_statistics(
            client, from_time=1000, to_time=2000,
            user_name="bob", application_name="Gaussian",
            resource_host_name="host-x")
        args = client.research.call_args
        assert args["user_name"] == "bob"
        assert args["application_name"] == "Gaussian"
        assert args["resource_host_name"] == "host-x"


# ===========================================================================
# Notification — proto-direct + WithAccess (gateway-catalog variant)
# ===========================================================================
#
# Like the application-module family, the notification helpers return the raw
# ``workspace_pb2.Notification`` proto wrapped in a ``WithAccess`` container.
# There is no ownership concept (the proto has no ``owner`` field), so
# ``is_owner`` is always ``False`` and ``user_has_write_access`` is the
# gateway-admin flag the ViewSet passes in as ``has_write``.  There is no dict
# transform to test — the portal's generic renderer flattens the proto (enums as
# NAMES, int64 timestamps as STRINGS); that JSON shape is pinned by the portal's
# ``test_notifications_contract.py`` snapshot.  The create/update wire-decoders
# (``_to_epoch_ms`` / ``_to_priority_int``) remain on the write path and are
# tested by ``TestNotificationWireDecoders``.

def _make_notification(**kwargs):
    from airavata_sdk.generated.org.apache.airavata.model.workspace import (
        workspace_pb2,
    )
    return workspace_pb2.Notification(**kwargs)


def _priority(name):
    from airavata_sdk.generated.org.apache.airavata.model.workspace import (
        workspace_pb2,
    )
    return getattr(workspace_pb2.NotificationPriority, name)


class _FakeNotifResearch:
    def __init__(self, notification, listing=None):
        self._notification = notification
        self._listing = listing if listing is not None else [notification]
        self.created = None
        self.updated = None
        self.deleted = None

    def get_notification(self, gateway_id, notification_id):
        return self._notification

    def get_all_notifications(self, gateway_id):
        return list(self._listing)

    def create_notification(self, notification):
        self.created = notification
        return "new-notif-id"

    def update_notification(self, notification):
        self.updated = notification

    def delete_notification(self, gateway_id, notification_id):
        self.deleted = (gateway_id, notification_id)


class _FakeNotifClient:
    def __init__(self, notification, listing=None, gateway_id="gw1",
                 username="admin"):
        self.research = _FakeNotifResearch(notification, listing)
        self.gateway_id = gateway_id
        self.username = username


class TestGetNotification:
    """``get_notification`` returns ``WithAccess[Notification]`` — the raw proto
    plus the gateway-catalog access flags (``is_owner`` always ``False``;
    ``user_has_write_access`` forwarded from ``has_write``)."""

    def _n(self):
        return _make_notification(
            notification_id="notif-1", gateway_id="gw1", title="T",
            creation_time=1705320000000, priority=_priority("NORMAL"))

    def test_returns_with_access_container(self):
        client = _FakeNotifClient(self._n())
        result = get_notification(client, "notif-1", has_write=True)
        assert isinstance(result, WithAccess)

    def test_message_is_the_proto(self):
        n = self._n()
        client = _FakeNotifClient(n)
        result = get_notification(client, "notif-1", has_write=True)
        # The exact proto object flows through wholesale — not copied.
        assert result.message is n
        assert result.message.notification_id == "notif-1"
        assert result.message.priority == _priority("NORMAL")

    def test_is_owner_always_false(self):
        client = _FakeNotifClient(self._n())
        result = get_notification(client, "notif-1", has_write=True)
        assert result.is_owner is False

    def test_has_write_forwarded_true(self):
        client = _FakeNotifClient(self._n())
        result = get_notification(client, "notif-1", has_write=True)
        assert result.user_has_write_access is True

    def test_has_write_forwarded_false(self):
        client = _FakeNotifClient(self._n())
        result = get_notification(client, "notif-1", has_write=False)
        assert result.user_has_write_access is False


class TestListNotifications:
    def _n(self, nid="notif-1"):
        return _make_notification(
            notification_id=nid, gateway_id="gw1", title="T",
            priority=_priority("LOW"))

    def test_returns_list_of_with_access(self):
        client = _FakeNotifClient(
            self._n(), listing=[self._n("a"), self._n("b")])
        results = list_notifications(client, has_write=False)
        assert len(results) == 2
        assert all(isinstance(r, WithAccess) for r in results)

    def test_messages_carry_protos_and_flags(self):
        client = _FakeNotifClient(
            self._n(), listing=[self._n("a"), self._n("b")])
        results = list_notifications(client, has_write=False)
        assert {r.message.notification_id for r in results} == {"a", "b"}
        assert all(r.is_owner is False for r in results)
        assert all(r.user_has_write_access is False for r in results)

    def test_has_write_forwarded_to_every_item(self):
        client = _FakeNotifClient(
            self._n(), listing=[self._n("a"), self._n("b")])
        results = list_notifications(client, has_write=True)
        assert all(r.user_has_write_access is True for r in results)


class TestCreateNotification:
    def _n(self):
        return _make_notification(notification_id="x", gateway_id="gw1")

    def test_returns_with_access_with_new_id(self):
        client = _FakeNotifClient(self._n())
        result = create_notification(
            client, {"title": "Hi", "notification_message": "msg",
                     "priority": _priority("HIGH")},
            has_write=True)
        assert isinstance(result, WithAccess)
        assert result.message.notification_id == "new-notif-id"
        assert result.message.title == "Hi"
        assert result.message.priority == _priority("HIGH")
        assert result.is_owner is False
        assert result.user_has_write_access is True

    def test_forces_gateway_id_from_client(self):
        client = _FakeNotifClient(self._n(), gateway_id="gw1")
        create_notification(client, {"title": "Hi"}, has_write=True)
        assert client.research.created.gateway_id == "gw1"

    def test_has_write_forwarded(self):
        client = _FakeNotifClient(self._n())
        result = create_notification(
            client, {"title": "Hi"}, has_write=False)
        assert result.user_has_write_access is False


class TestUpdateNotification:
    def _base(self):
        return _make_notification(
            notification_id="notif-9", gateway_id="gw1", title="Old",
            notification_message="oldmsg", priority=_priority("LOW"))

    def test_updates_title(self):
        client = _FakeNotifClient(self._base())
        update_notification(
            client, "notif-9", {"title": "New"}, has_write=True)
        assert client.research.updated.title == "New"
        # untouched field preserved from base
        assert client.research.updated.notification_message == "oldmsg"

    def test_preserves_notification_id(self):
        client = _FakeNotifClient(self._base())
        update_notification(
            client, "notif-9", {"title": "New"}, has_write=True)
        assert client.research.updated.notification_id == "notif-9"

    def test_returns_with_access(self):
        client = _FakeNotifClient(self._base())
        result = update_notification(
            client, "notif-9", {"title": "New"}, has_write=False)
        assert isinstance(result, WithAccess)
        assert result.message.notification_id == "notif-9"
        assert result.message.title == "New"
        assert result.is_owner is False
        assert result.user_has_write_access is False


class TestDeleteNotification:
    def test_calls_facade_with_gateway_and_id(self):
        n = _make_notification(notification_id="notif-1", gateway_id="gw1")
        client = _FakeNotifClient(n, gateway_id="gw1")
        delete_notification(client, "notif-1")
        assert client.research.deleted == ("gw1", "notif-1")


class TestNotificationWireDecoders:
    """The create/update path accepts the portal wire format (ISO timestamps,
    priority NAME strings) as well as already-decoded proto values."""

    def test_create_accepts_iso_timestamps(self):
        from airavata_sdk.helpers.research_resources import _to_epoch_ms
        # JS Date toJSON form (millisecond precision, trailing Z)
        assert _to_epoch_ms("2024-01-15T12:00:00.000Z") == 1705320000000
        # microsecond + Z (DRF output form)
        assert _to_epoch_ms("2024-01-15T12:00:00.000000Z") == 1705320000000

    def test_epoch_ms_passthrough_and_falsy(self):
        from airavata_sdk.helpers.research_resources import _to_epoch_ms
        assert _to_epoch_ms(1705320000000) == 1705320000000
        assert _to_epoch_ms(0) == 0
        assert _to_epoch_ms(None) == 0
        assert _to_epoch_ms("") == 0

    def test_priority_name_decode(self):
        from airavata_sdk.helpers.research_resources import _to_priority_int
        assert _to_priority_int("LOW") == _priority("LOW")
        assert _to_priority_int("NORMAL") == _priority("NORMAL")
        assert _to_priority_int("HIGH") == _priority("HIGH")

    def test_priority_int_passthrough_and_unknown(self):
        from airavata_sdk.helpers.research_resources import _to_priority_int
        assert _to_priority_int(_priority("HIGH")) == _priority("HIGH")
        assert _to_priority_int(None) == 0
        assert _to_priority_int("") == 0
        assert _to_priority_int("BOGUS") == 0

    def test_create_with_wire_format_decodes_to_proto(self):
        client = _FakeNotifClient(
            _make_notification(notification_id="x", gateway_id="gw1"))
        result = create_notification(
            client,
            {"title": "Hi", "published_time": "2024-01-15T12:10:00.000Z",
             "priority": "NORMAL"},
            has_write=True)
        # facade received the decoded proto (epoch-millis int + enum int)
        assert client.research.created.published_time == 1705320600000
        assert client.research.created.priority == _priority("NORMAL")
        # the returned WithAccess carries that same proto wholesale
        assert result.message.published_time == 1705320600000
        assert result.message.priority == _priority("NORMAL")


# ===========================================================================
# Parser
# ===========================================================================

def _make_parser(**kwargs):
    from airavata_sdk.generated.org.apache.airavata.model.appcatalog.parser import (  # noqa: E501
        parser_pb2,
    )
    return parser_pb2.Parser(**kwargs)


def _make_parser_input(**kwargs):
    from airavata_sdk.generated.org.apache.airavata.model.appcatalog.parser import (  # noqa: E501
        parser_pb2,
    )
    return parser_pb2.ParserInput(**kwargs)


def _make_parser_output(**kwargs):
    from airavata_sdk.generated.org.apache.airavata.model.appcatalog.parser import (  # noqa: E501
        parser_pb2,
    )
    return parser_pb2.ParserOutput(**kwargs)


def _io_type(name):
    from airavata_sdk.generated.org.apache.airavata.model.appcatalog.parser import (  # noqa: E501
        parser_pb2,
    )
    return getattr(parser_pb2.IOType, name)


class TestIoTypeValueCoercion:
    """``_io_type_value`` accepts the proto member NAME or the proto integer."""

    def test_name_string_to_proto_int(self):
        from airavata_sdk.helpers.research_resources import _io_type_value
        assert _io_type_value("FILE") == _io_type("FILE")
        assert _io_type_value("PROPERTY") == _io_type("PROPERTY")

    def test_prefixed_name_accepted(self):
        from airavata_sdk.helpers.research_resources import _io_type_value
        assert _io_type_value("IO_TYPE_UNKNOWN") == 0
        assert _io_type_value("UNKNOWN") == 0

    def test_proto_int_passthrough(self):
        from airavata_sdk.helpers.research_resources import _io_type_value
        assert _io_type_value(_io_type("FILE")) == _io_type("FILE")
        assert _io_type_value(_io_type("PROPERTY")) == _io_type("PROPERTY")

    def test_none_and_unknown_to_zero(self):
        from airavata_sdk.helpers.research_resources import _io_type_value
        assert _io_type_value(None) == 0
        assert _io_type_value("") == 0
        assert _io_type_value("BOGUS") == 0
        assert _io_type_value(99) == 0
        assert _io_type_value(True) == 0


class _FakeParserResearch:
    def __init__(self, parser, listing=None):
        self._parser = parser
        self._listing = listing if listing is not None else [parser]
        # The most recently saved proto, re-fetched by create/update.
        self._saved_for_fetch = parser
        self.saved = None
        self.removed = None

    def get_parser(self, parser_id, gateway_id):
        # Mirror the server: return the saved proto with the requested id.
        from airavata_sdk.generated.org.apache.airavata.model.appcatalog.parser import (  # noqa: E501
            parser_pb2,
        )
        p = parser_pb2.Parser()
        p.CopyFrom(self._saved_for_fetch)
        p.id = parser_id
        return p

    def list_all_parsers(self, gateway_id):
        return list(self._listing)

    def save_parser(self, parser):
        self.saved = parser
        self._saved_for_fetch = parser
        return "new-parser-id"

    def remove_parser(self, parser_id, gateway_id):
        self.removed = (parser_id, gateway_id)


class _FakeParserClient:
    def __init__(self, parser, listing=None, gateway_id="gw1",
                 username="alice"):
        self.research = _FakeParserResearch(parser, listing)
        self.gateway_id = gateway_id
        self.username = username


class TestGetParser:
    def _p(self):
        return _make_parser(
            id="parser-1", gateway_id="gw1", image_name="img",
            input_files=[
                _make_parser_input(
                    id="in-1", name="infile", required_input=True,
                    parser_id="parser-1", type=_io_type("FILE")),
            ],
            output_files=[
                _make_parser_output(
                    id="out-1", name="outfile", required_output=False,
                    parser_id="parser-1", type=_io_type("PROPERTY")),
            ],
        )

    def test_returns_the_proto_directly(self):
        from airavata_sdk.generated.org.apache.airavata.model.appcatalog.parser import (  # noqa: E501
            parser_pb2,
        )
        from airavata_sdk.helpers.research_resources import get_parser
        client = _FakeParserClient(self._p())
        result = get_parser(client, "parser-1")
        # proto-direct: the facade proto is returned as-is, not a dict / envelope
        assert isinstance(result, parser_pb2.Parser)
        assert result.id == "parser-1"
        assert result.image_name == "img"

    def test_nested_files_and_enum_carried_on_proto(self):
        from airavata_sdk.helpers.research_resources import get_parser
        client = _FakeParserClient(self._p())
        result = get_parser(client, "parser-1")
        assert result.input_files[0].name == "infile"
        assert result.input_files[0].type == _io_type("FILE")
        assert result.output_files[0].type == _io_type("PROPERTY")


class TestListParsers:
    def _p(self, pid="parser-1"):
        return _make_parser(id=pid, gateway_id="gw1")

    def test_returns_list_of_protos(self):
        from airavata_sdk.generated.org.apache.airavata.model.appcatalog.parser import (  # noqa: E501
            parser_pb2,
        )
        from airavata_sdk.helpers.research_resources import list_parsers
        client = _FakeParserClient(
            self._p(), listing=[self._p("a"), self._p("b")])
        results = list_parsers(client)
        assert all(isinstance(r, parser_pb2.Parser) for r in results)
        assert {r.id for r in results} == {"a", "b"}


class TestCreateParser:
    def _p(self):
        return _make_parser(id="x", gateway_id="gw1")

    def test_returns_proto_with_new_id(self):
        from airavata_sdk.generated.org.apache.airavata.model.appcatalog.parser import (  # noqa: E501
            parser_pb2,
        )
        from airavata_sdk.helpers.research_resources import create_parser
        client = _FakeParserClient(self._p())
        result = create_parser(
            client,
            {"image_name": "img", "execution_command": "run",
             "input_files": [
                 {"name": "f", "required_input": True, "type": "FILE"}]})
        # proto-direct: the re-fetched proto carries the server-assigned id
        assert isinstance(result, parser_pb2.Parser)
        assert result.id == "new-parser-id"
        assert result.image_name == "img"
        assert result.input_files[0].type == _io_type("FILE")

    def test_forces_gateway_id_from_client(self):
        from airavata_sdk.helpers.research_resources import create_parser
        client = _FakeParserClient(self._p(), gateway_id="gw1")
        create_parser(client, {"image_name": "img"})
        assert client.research.saved.gateway_id == "gw1"

    def test_input_type_name_decoded_to_proto(self):
        from airavata_sdk.helpers.research_resources import create_parser
        client = _FakeParserClient(self._p())
        create_parser(
            client,
            {"input_files": [{"name": "f", "type": "PROPERTY"}],
             "output_files": [{"name": "g", "type": "FILE"}]})
        saved = client.research.saved
        assert saved.input_files[0].type == _io_type("PROPERTY")
        assert saved.output_files[0].type == _io_type("FILE")


class TestUpdateParser:
    def _p(self):
        return _make_parser(id="parser-9", gateway_id="gw1", image_name="old")

    def test_preserves_parser_id_on_save(self):
        from airavata_sdk.helpers.research_resources import update_parser
        client = _FakeParserClient(self._p())
        update_parser(client, "parser-9", {"image_name": "new"})
        assert client.research.saved.id == "parser-9"

    def test_returns_proto(self):
        from airavata_sdk.generated.org.apache.airavata.model.appcatalog.parser import (  # noqa: E501
            parser_pb2,
        )
        from airavata_sdk.helpers.research_resources import update_parser
        client = _FakeParserClient(self._p())
        result = update_parser(client, "parser-9", {"image_name": "new"})
        assert isinstance(result, parser_pb2.Parser)
        assert result.id == "parser-9"
        assert result.image_name == "new"


class TestDeleteParser:
    def test_calls_facade_with_id_and_gateway(self):
        from airavata_sdk.helpers.research_resources import delete_parser
        client = _FakeParserClient(
            _make_parser(id="parser-1", gateway_id="gw1"), gateway_id="gw1")
        delete_parser(client, "parser-1")
        assert client.research.removed == ("parser-1", "gw1")


# ===========================================================================
# DataProduct
# ===========================================================================

def _rc():
    from airavata_sdk.generated.org.apache.airavata.model.data.replica import (  # noqa: E501
        replica_catalog_pb2,
    )
    return replica_catalog_pb2


def _make_replica(**kwargs):
    return _rc().DataReplicaLocationModel(**kwargs)


def _make_data_product(**kwargs):
    return _rc().DataProductModel(**kwargs)




class _FakeDataProductResearch:
    def __init__(self, data_product):
        self._data_product = data_product
        self.registered = None

    def get_data_product(self, product_uri):
        return self._data_product

    def register_data_product(self, data_product):
        self.registered = data_product
        return "airavata-dp://new-uri"


class _FakeDataProductClient:
    def __init__(self, data_product, gateway_id="gw1", username="alice"):
        self.research = _FakeDataProductResearch(data_product)
        self.gateway_id = gateway_id
        self.username = username


class TestGetDataProduct:
    """Proto-direct read path: ``get_data_product`` returns a
    ``WithAccess[DataProductModel]`` — the raw proto unioned with the caller's
    ``is_owner`` (SDK-trivial ``owner_name == username``) and
    ``user_has_write_access`` (the request-bound *has_write* flag the ViewSet
    passes in)."""

    def _dp(self, owner_name="alice"):
        rc = _rc()
        return _make_data_product(
            product_uri="airavata-dp://1", gateway_id="gw1",
            product_name="f.dat", owner_name=owner_name,
            data_product_type=rc.DataProductType.FILE,
            creation_time=1705320000000)

    def test_returns_with_access_carrying_the_proto(self):
        from airavata_sdk.helpers._envelope import WithAccess
        from airavata_sdk.helpers.research_resources import get_data_product
        dp = self._dp()
        client = _FakeDataProductClient(dp)
        result = get_data_product(
            client, "airavata-dp://1", has_write=True)
        assert isinstance(result, WithAccess)
        # The proto flows through wholesale — no field copied out.
        assert result.message is dp
        assert result.message.product_uri == "airavata-dp://1"

    def test_is_owner_true_when_owner_matches_username(self):
        from airavata_sdk.helpers.research_resources import get_data_product
        client = _FakeDataProductClient(
            self._dp(owner_name="alice"), username="alice")
        result = get_data_product(client, "airavata-dp://1", has_write=True)
        assert result.is_owner is True

    def test_is_owner_false_when_owner_differs(self):
        from airavata_sdk.helpers.research_resources import get_data_product
        client = _FakeDataProductClient(
            self._dp(owner_name="alice"), username="bob")
        result = get_data_product(client, "airavata-dp://1", has_write=True)
        assert result.is_owner is False

    def test_is_owner_false_when_owner_empty(self):
        from airavata_sdk.helpers.research_resources import get_data_product
        client = _FakeDataProductClient(
            self._dp(owner_name=""), username="")
        result = get_data_product(client, "airavata-dp://1", has_write=True)
        # An empty owner_name never makes the (possibly empty) caller the owner.
        assert result.is_owner is False

    def test_user_has_write_access_reflects_has_write(self):
        from airavata_sdk.helpers.research_resources import get_data_product
        client = _FakeDataProductClient(self._dp())
        assert get_data_product(
            client, "airavata-dp://1", has_write=True).user_has_write_access
        assert not get_data_product(
            client, "airavata-dp://1", has_write=False).user_has_write_access


class TestDataProductForUpload:
    def test_builds_proto_with_single_replica(self):
        from airavata_sdk.helpers.research_resources import (
            data_product_for_upload,
        )
        dp = data_product_for_upload(
            gateway_id="gw1", owner_name="alice", product_name="f.dat",
            file_path="/data/tmp/f.dat", storage_resource_id="storage-1",
            content_type="text/plain", product_size=10)
        rc = _rc()
        assert dp.gateway_id == "gw1"
        assert dp.owner_name == "alice"
        assert dp.product_name == "f.dat"
        assert dp.data_product_type == rc.DataProductType.FILE
        assert dp.product_size == 10
        assert dp.product_metadata["mime-type"] == "text/plain"
        assert len(dp.replica_locations) == 1
        r = dp.replica_locations[0]
        assert r.file_path == "/data/tmp/f.dat"
        assert r.storage_resource_id == "storage-1"
        assert (r.replica_location_category
                == rc.ReplicaLocationCategory.GATEWAY_DATA_STORE)
        assert (r.replica_persistent_type
                == rc.ReplicaPersistentType.TRANSIENT)

    def test_no_content_type_means_empty_metadata(self):
        from airavata_sdk.helpers.research_resources import (
            data_product_for_upload,
        )
        dp = data_product_for_upload(
            gateway_id="gw1", owner_name="alice", product_name="f.dat",
            file_path="/p", storage_resource_id="s")
        assert dict(dp.product_metadata) == {}


class TestRegisterDataProduct:
    def test_returns_uri_and_passes_proto(self):
        from airavata_sdk.helpers.research_resources import (
            data_product_for_upload, register_data_product,
        )
        client = _FakeDataProductClient(_make_data_product(product_uri="x"))
        dp = data_product_for_upload(
            gateway_id="gw1", owner_name="alice", product_name="f",
            file_path="/p", storage_resource_id="s")
        uri = register_data_product(client, dp)
        assert uri == "airavata-dp://new-uri"
        assert client.research.registered is dp


# ===========================================================================
# ApplicationInterface
# ===========================================================================

def _io_pb2():
    from airavata_sdk.generated.org.apache.airavata.model.application.io import (
        application_io_pb2,
    )
    return application_io_pb2


def _ai_pb2():
    from airavata_sdk.generated.org.apache.airavata.model.appcatalog.appinterface import (  # noqa: E501
        app_interface_pb2,
    )
    return app_interface_pb2


def _make_input(**kwargs):
    io = _io_pb2()
    defaults = dict(
        name="in1", value="v", type=io.DataType.STRING,
        application_argument="-i", standard_input=False,
        user_friendly_description="ufd", meta_data='{"a": 1}',
        input_order=2, is_required=True,
        required_to_added_to_command_line=True, data_staged=False,
        storage_resource_id="sr1", is_read_only=False, override_filename="of")
    defaults.update(kwargs)
    return io.InputDataObjectType(**defaults)


def _make_output(**kwargs):
    io = _io_pb2()
    defaults = dict(
        name="out1", value="ov", type=io.DataType.URI,
        application_argument="-o", is_required=True,
        required_to_added_to_command_line=False, data_movement=True,
        location="loc", search_query="sq", output_streaming=False,
        storage_resource_id="sr2", meta_data="")
    defaults.update(kwargs)
    return io.OutputDataObjectType(**defaults)


def _make_interface(**kwargs):
    ai = _ai_pb2()
    defaults = dict(
        application_interface_id="echo_abc", application_name="Echo",
        application_description="desc", application_modules=["mod1"],
        archive_working_directory=True, has_optional_file_inputs=True,
        application_inputs=[_make_input()],
        application_outputs=[_make_output()])
    defaults.update(kwargs)
    return ai.ApplicationInterfaceDescription(**defaults)




class _FakeAppInterfaceResearch:
    def __init__(self, interface):
        self._interface = interface
        self.registered = None
        self.updated = None
        self.deleted = None

    def get_application_interface(self, app_interface_id):
        return self._interface

    def get_all_application_interfaces(self, gateway_id):
        return [self._interface]

    def register_application_interface(self, gateway_id, application_interface):
        self.registered = (gateway_id, application_interface)
        return "new-iface-id"

    def update_application_interface(self, app_interface_id,
                                     application_interface):
        self.updated = (app_interface_id, application_interface)

    def delete_application_interface(self, app_interface_id):
        self.deleted = app_interface_id


class _FakeAppInterfaceClient:
    def __init__(self, interface, gateway_id="gw1", username="alice"):
        self.research = _FakeAppInterfaceResearch(interface)
        self.gateway_id = gateway_id
        self.username = username


class TestApplicationInterfaceOrchestration:
    def test_get_returns_with_access(self):
        from airavata_sdk.helpers._envelope import WithAccess
        from airavata_sdk.helpers.research_resources import (
            get_application_interface,
        )
        client = _FakeAppInterfaceClient(_make_interface())
        result = get_application_interface(client, "echo_abc", has_write=True)
        # Gateway-catalog WithAccess: the raw proto under .message, no owner,
        # write access forwarded from the gateway-admin flag.
        assert isinstance(result, WithAccess)
        assert result.message is client.research._interface
        assert result.message.application_interface_id == "echo_abc"
        assert result.is_owner is False
        assert result.user_has_write_access is True

    def test_get_forwards_has_write_false(self):
        from airavata_sdk.helpers.research_resources import (
            get_application_interface,
        )
        client = _FakeAppInterfaceClient(_make_interface())
        result = get_application_interface(client, "echo_abc", has_write=False)
        assert result.user_has_write_access is False

    def test_list_returns_with_access(self):
        from airavata_sdk.helpers._envelope import WithAccess
        from airavata_sdk.helpers.research_resources import (
            list_application_interfaces,
        )
        client = _FakeAppInterfaceClient(_make_interface())
        items = list_application_interfaces(client, has_write=False)
        assert len(items) == 1
        assert isinstance(items[0], WithAccess)
        assert items[0].message is client.research._interface
        assert items[0].is_owner is False
        assert items[0].user_has_write_access is False

    def test_create_builds_proto_and_refetches(self):
        from airavata_sdk.helpers.research_resources import (
            create_application_interface,
        )
        client = _FakeAppInterfaceClient(_make_interface())
        data = {
            "application_name": "NewApp",
            "application_description": "nd",
            "application_modules": ["m1"],
            "archive_working_directory": True,
            "application_inputs": [{
                "name": "i", "type": "URI", "meta_data": {"x": 1}}],
            "application_outputs": [{"name": "o", "type": "STDOUT"}],
        }
        create_application_interface(client, data, has_write=True)
        gw, proto = client.research.registered
        assert gw == "gw1"
        assert proto.application_name == "NewApp"
        assert proto.application_modules == ["m1"]
        io = _io_pb2()
        assert proto.application_inputs[0].type == io.DataType.URI
        assert proto.application_inputs[0].meta_data == '{"x": 1}'
        assert proto.application_outputs[0].type == io.DataType.STDOUT

    def test_update_merges_and_forces_id(self):
        from airavata_sdk.helpers.research_resources import (
            update_application_interface,
        )
        client = _FakeAppInterfaceClient(_make_interface())
        update_application_interface(
            client, "echo_abc", {"application_name": "Renamed"},
            has_write=True)
        iface_id, proto = client.research.updated
        assert iface_id == "echo_abc"
        assert proto.application_interface_id == "echo_abc"
        assert proto.application_name == "Renamed"
        # Untouched fields retained from the base proto.
        assert proto.application_description == "desc"

    def test_delete(self):
        from airavata_sdk.helpers.research_resources import (
            delete_application_interface,
        )
        client = _FakeAppInterfaceClient(_make_interface())
        delete_application_interface(client, "echo_abc")
        assert client.research.deleted == "echo_abc"


class TestDataTypeIntCoercion:
    def test_name_to_int(self):
        from airavata_sdk.helpers.research_resources import _data_type_int
        io = _io_pb2()
        assert _data_type_int("STRING") == io.DataType.STRING
        assert _data_type_int("URI") == io.DataType.URI

    def test_int_passthrough(self):
        from airavata_sdk.helpers.research_resources import _data_type_int
        io = _io_pb2()
        assert _data_type_int(io.DataType.FLOAT) == io.DataType.FLOAT

    def test_none_and_unknown(self):
        from airavata_sdk.helpers.research_resources import _data_type_int
        assert _data_type_int(None) == 0
        assert _data_type_int("") == 0
        assert _data_type_int("NOPE") == 0


class TestMetaDataStr:
    def test_none_to_empty(self):
        from airavata_sdk.helpers.research_resources import _meta_data_str
        assert _meta_data_str(None) == ""

    def test_string_passthrough(self):
        from airavata_sdk.helpers.research_resources import _meta_data_str
        assert _meta_data_str('{"a": 1}') == '{"a": 1}'

    def test_dict_dumps(self):
        from airavata_sdk.helpers.research_resources import _meta_data_str
        import json
        assert json.loads(_meta_data_str({"a": 1})) == {"a": 1}


# ===========================================================================
# ApplicationDeployment  (proto-direct + WithAccess envelope)
# ===========================================================================
#
# The application-deployment family follows the proto-direct architecture
# (sharing-controlled gateway-catalog variant): the read helpers return the raw
# ``app_deployment_pb2.ApplicationDeploymentDescription`` proto wrapped in a
# ``WithAccess`` container.  The deployment proto has no ``owner`` field, so
# ``is_owner`` is always ``False``; ``user_has_write_access`` is a CHAINED
# ``sharing.user_has_access`` WRITE lookup keyed on ``app_deployment_id`` (the
# legacy ``user_has_access(request, app_deployment_id)``).  There is no dict
# transform, no Thrift-int parallelism mapping, and no order-sorting to test —
# the portal's generic renderer flattens the proto (enums as NAMES, nested lists
# in proto order).


def _ad_pb2():
    from airavata_sdk.generated.org.apache.airavata.model.appcatalog.appdeployment import (  # noqa: E501
        app_deployment_pb2,
    )
    return app_deployment_pb2


def _par_pb2():
    from airavata_sdk.generated.org.apache.airavata.model.parallelism import (
        parallelism_pb2,
    )
    return parallelism_pb2


def _make_deployment(**kwargs):
    ad = _ad_pb2()
    return ad.ApplicationDeploymentDescription(**kwargs)


class _FakeDeploymentSharing:
    def __init__(self, has_access=True):
        self._has_access = has_access
        self.calls = []

    def user_has_access(self, resource_id, user_id, permission_type):
        self.calls.append((resource_id, user_id, permission_type))
        if isinstance(self._has_access, dict):
            return bool(self._has_access.get(resource_id, False))
        return self._has_access


class _FakeDeploymentResearch:
    def __init__(self, deployment=None, deployments=None):
        self._deployment = deployment
        self._deployments = deployments or []
        self.registered = None
        self.updated = None
        self.deleted = None

    def get_application_deployment(self, app_deployment_id):
        return self._deployment

    def get_accessible_application_deployments(self, gateway_id):
        return list(self._deployments)

    def get_application_deployments_for_app_module_and_group_resource_profile(
            self, app_module_id, group_resource_profile_id):
        self.module_profile_args = (app_module_id, group_resource_profile_id)
        return list(self._deployments)

    def register_application_deployment(self, gateway_id, application_deployment):
        self.registered = application_deployment
        self.registered_gateway_id = gateway_id
        return "dep-new"

    def update_application_deployment(self, app_deployment_id, application_deployment):
        self.updated = (app_deployment_id, application_deployment)

    def delete_application_deployment(self, app_deployment_id):
        self.deleted = app_deployment_id


class _FakeDeploymentClient:
    def __init__(self, research, sharing_has_access=True,
                 username="alice", gateway_id="default"):
        self.research = research
        self.sharing = _FakeDeploymentSharing(sharing_has_access)
        self.username = username
        self.gateway_id = gateway_id


class TestParallelismInputToProtoInt:
    def test_name_string_accepted(self):
        from airavata_sdk.helpers.research_resources import (
            _parallelism_input_to_proto_int,
        )
        par = _par_pb2()
        assert _parallelism_input_to_proto_int("MPI") == \
            par.ApplicationParallelismType.MPI
        assert _parallelism_input_to_proto_int(
            "APPLICATION_PARALLELISM_TYPE_OPENMP") == \
            par.ApplicationParallelismType.OPENMP

    def test_proto_int_passthrough(self):
        from airavata_sdk.helpers.research_resources import (
            _parallelism_input_to_proto_int,
        )
        par = _par_pb2()
        assert _parallelism_input_to_proto_int(
            par.ApplicationParallelismType.CRAY_MPI) == \
            par.ApplicationParallelismType.CRAY_MPI

    def test_none_and_unknown_to_zero(self):
        from airavata_sdk.helpers.research_resources import (
            _parallelism_input_to_proto_int,
        )
        assert _parallelism_input_to_proto_int(None) == 0
        assert _parallelism_input_to_proto_int("") == 0
        assert _parallelism_input_to_proto_int("NOPE") == 0


class TestBuildApplicationDeployment:
    def test_build_from_snake_case(self):
        from airavata_sdk.helpers.research_resources import (
            _build_application_deployment,
        )
        client = _FakeDeploymentClient(_FakeDeploymentResearch())
        data = {
            "app_module_id": "mod-1",
            "compute_host_id": "host-1",
            "executable_path": "/bin/run",
            "parallelism": "MPI",  # proto member NAME (wire format)
            "module_load_cmds": [{"command": "x", "command_order": 0}],
            "set_environment": [{"name": "N", "value": "V", "env_path_order": 0}],
            "default_node_count": 3,
            "editable_by_user": True,
        }
        pb = _build_application_deployment(client, data)
        par = _par_pb2()
        assert pb.app_module_id == "mod-1"
        assert pb.parallelism == par.ApplicationParallelismType.MPI
        assert pb.module_load_cmds[0].command == "x"
        assert pb.set_environment[0].name == "N"
        assert pb.default_node_count == 3
        assert pb.editable_by_user is True


class TestGetApplicationDeployment:
    """``get_application_deployment`` returns ``WithAccess[...Deployment]`` — the
    raw proto plus ``is_owner`` (always ``False``) and the chained sharing WRITE
    flag keyed on ``app_deployment_id``."""

    def _deployment(self):
        par = _par_pb2()
        return _make_deployment(
            app_deployment_id="dep-1", app_module_id="m",
            parallelism=par.ApplicationParallelismType.MPI)

    def test_returns_with_access_container(self):
        from airavata_sdk.helpers.research_resources import (
            get_application_deployment,
        )
        client = _FakeDeploymentClient(
            _FakeDeploymentResearch(deployment=self._deployment()))
        result = get_application_deployment(client, "dep-1")
        assert isinstance(result, WithAccess)

    def test_message_is_the_proto(self):
        from airavata_sdk.helpers.research_resources import (
            get_application_deployment,
        )
        dep = self._deployment()
        client = _FakeDeploymentClient(
            _FakeDeploymentResearch(deployment=dep))
        result = get_application_deployment(client, "dep-1")
        # The exact proto object flows through wholesale — not copied.
        assert result.message is dep
        assert result.message.app_deployment_id == "dep-1"

    def test_is_owner_always_false(self):
        from airavata_sdk.helpers.research_resources import (
            get_application_deployment,
        )
        client = _FakeDeploymentClient(
            _FakeDeploymentResearch(deployment=self._deployment()))
        result = get_application_deployment(client, "dep-1")
        assert result.is_owner is False

    def test_write_access_reflects_sharing(self):
        from airavata_sdk.helpers.research_resources import (
            get_application_deployment,
        )
        client = _FakeDeploymentClient(
            _FakeDeploymentResearch(deployment=self._deployment()),
            sharing_has_access=True)
        assert get_application_deployment(
            client, "dep-1").user_has_write_access is True
        client = _FakeDeploymentClient(
            _FakeDeploymentResearch(deployment=self._deployment()),
            sharing_has_access=False)
        assert get_application_deployment(
            client, "dep-1").user_has_write_access is False

    def test_chained_sharing_call_arguments(self):
        from airavata_sdk.helpers.research_resources import (
            get_application_deployment,
        )
        client = _FakeDeploymentClient(
            _FakeDeploymentResearch(deployment=self._deployment()),
            username="alice")
        get_application_deployment(client, "dep-1")
        assert len(client.sharing.calls) == 1
        resource_id, user_id, perm = client.sharing.calls[0]
        assert resource_id == "dep-1"
        assert user_id == "alice"
        assert perm == "WRITE"


class TestListApplicationDeployments:
    def test_per_deployment_chained_write_lookup(self):
        from airavata_sdk.helpers.research_resources import (
            list_application_deployments,
        )
        d1 = _make_deployment(app_deployment_id="a")
        d2 = _make_deployment(app_deployment_id="b")
        client = _FakeDeploymentClient(
            _FakeDeploymentResearch(deployments=[d1, d2]),
            sharing_has_access={"a": True, "b": False})
        out = list_application_deployments(client)
        assert all(isinstance(x, WithAccess) for x in out)
        by_id = {x.message.app_deployment_id: x for x in out}
        assert by_id["a"].user_has_write_access is True
        assert by_id["b"].user_has_write_access is False
        assert by_id["a"].is_owner is False
        # One chained WRITE lookup per deployment, keyed on app_deployment_id.
        keyed = {c[0]: c[2] for c in client.sharing.calls}
        assert keyed == {"a": "WRITE", "b": "WRITE"}


class TestListApplicationDeploymentsForModuleAndProfile:
    def test_uses_module_profile_facade_and_wraps(self):
        from airavata_sdk.helpers.research_resources import (
            list_application_deployments_for_module_and_profile,
        )
        d1 = _make_deployment(app_deployment_id="a")
        research = _FakeDeploymentResearch(deployments=[d1])
        client = _FakeDeploymentClient(research, sharing_has_access=True)
        out = list_application_deployments_for_module_and_profile(
            client, "mod-1", "grp-1")
        assert research.module_profile_args == ("mod-1", "grp-1")
        assert isinstance(out[0], WithAccess)
        assert out[0].message.app_deployment_id == "a"
        assert out[0].user_has_write_access is True


class TestCreateUpdateDeleteApplicationDeployment:
    def test_create_registers_and_refetches_with_access(self):
        from airavata_sdk.helpers.research_resources import (
            create_application_deployment,
        )
        created = _make_deployment(app_deployment_id="dep-new", app_module_id="m")
        research = _FakeDeploymentResearch(deployment=created)
        client = _FakeDeploymentClient(research)
        out = create_application_deployment(
            client, {"app_module_id": "m"}, has_write=True)
        assert research.registered is not None
        assert isinstance(out, WithAccess)
        assert out.message.app_deployment_id == "dep-new"
        assert out.is_owner is False
        # has_write is forwarded, not chained, for the freshly created record.
        assert out.user_has_write_access is True
        assert client.sharing.calls == []

    def test_update_forces_id_and_returns_with_access(self):
        from airavata_sdk.helpers.research_resources import (
            update_application_deployment,
        )
        existing = _make_deployment(app_deployment_id="dep-1", app_module_id="m")
        research = _FakeDeploymentResearch(deployment=existing)
        client = _FakeDeploymentClient(research)
        out = update_application_deployment(
            client, "dep-1", {"app_module_id": "m2"}, has_write=False)
        sent_id, sent_pb = research.updated
        assert sent_id == "dep-1"
        assert sent_pb.app_deployment_id == "dep-1"
        assert isinstance(out, WithAccess)
        assert out.user_has_write_access is False

    def test_delete_calls_facade(self):
        from airavata_sdk.helpers.research_resources import (
            delete_application_deployment,
        )
        research = _FakeDeploymentResearch()
        client = _FakeDeploymentClient(research)
        delete_application_deployment(client, "dep-x")
        assert research.deleted == "dep-x"


# ===========================================================================
# Experiment (experiments-core)
# ===========================================================================

def _make_experiment(**kwargs):
    from airavata_sdk.generated.org.apache.airavata.model.experiment import (
        experiment_pb2,
    )
    return experiment_pb2.ExperimentModel(**kwargs)


def _make_job(**kwargs):
    from airavata_sdk.generated.org.apache.airavata.model.job import job_pb2
    return job_pb2.JobModel(**kwargs)


class _FakeExpCoreResearch:
    def __init__(self, experiment=None, jobs=None, experiments=None):
        self._experiment = experiment
        self._jobs = jobs or []
        self._experiments = experiments or []
        self.created = None
        self.created_gateway_id = None
        self.updated = None

    def get_experiment(self, experiment_id):
        return self._experiment

    def get_experiments_in_project(self, project_id, limit=-1, offset=0):
        self.in_project_args = (project_id, limit, offset)
        return self._experiments

    def get_job_details(self, experiment_id):
        return self._jobs

    def create_experiment(self, gateway_id, experiment):
        self.created = experiment
        self.created_gateway_id = gateway_id
        return "new-exp-id"

    def update_experiment(self, experiment_id, experiment):
        self.updated = (experiment_id, experiment)


class _FakeExpCoreSharing:
    def __init__(self, has_access=True):
        self._has_access = has_access
        self.calls = []

    def user_has_access(self, resource_id, user_id, permission_type):
        self.calls.append((resource_id, user_id, permission_type))
        return self._has_access


class _FakeExpCoreClient:
    def __init__(self, research, gateway_id="gw1", username="alice",
                 sharing_has_access=True):
        self.research = research
        self.sharing = _FakeExpCoreSharing(sharing_has_access)
        self.gateway_id = gateway_id
        self.username = username




class TestGetExperiment:
    """``get_experiment`` returns ``WithAccess[ExperimentModel]``: the raw proto
    (the whole tree flows through wholesale for the renderer to serialize), with
    ``is_owner`` trivially ``False`` and ``user_has_write_access`` a chained
    sharing WRITE lookup keyed on ``experiment_id``."""

    def _client(self, sharing_has_access=True):
        e = _make_experiment(experiment_id="exp-1", experiment_name="E")
        return _FakeExpCoreClient(
            _FakeExpCoreResearch(experiment=e),
            sharing_has_access=sharing_has_access)

    def test_returns_with_access_container(self):
        from airavata_sdk.helpers.research_resources import get_experiment
        result = get_experiment(self._client(), "exp-1")
        assert isinstance(result, WithAccess)

    def test_message_is_the_proto(self):
        from airavata_sdk.generated.org.apache.airavata.model.experiment import (
            experiment_pb2,
        )
        from airavata_sdk.helpers.research_resources import get_experiment
        result = get_experiment(self._client(), "exp-1")
        assert isinstance(result.message, experiment_pb2.ExperimentModel)
        assert result.message.experiment_id == "exp-1"
        assert result.message.experiment_name == "E"

    def test_is_owner_always_false(self):
        from airavata_sdk.helpers.research_resources import get_experiment
        result = get_experiment(self._client(), "exp-1")
        assert result.is_owner is False

    def test_user_has_write_access_true_from_sharing(self):
        from airavata_sdk.helpers.research_resources import get_experiment
        result = get_experiment(
            self._client(sharing_has_access=True), "exp-1")
        assert result.user_has_write_access is True

    def test_user_has_write_access_false_from_sharing(self):
        from airavata_sdk.helpers.research_resources import get_experiment
        result = get_experiment(
            self._client(sharing_has_access=False), "exp-1")
        assert result.user_has_write_access is False

    def test_sharing_keyed_on_experiment_id(self):
        from airavata_sdk.helpers.research_resources import get_experiment
        client = self._client()
        get_experiment(client, "exp-1")
        assert client.sharing.calls == [("exp-1", "alice", "WRITE")]


class TestGetExperimentProto:
    def test_returns_raw_proto(self):
        from airavata_sdk.helpers.research_resources import get_experiment_proto
        e = _make_experiment(experiment_id="exp-1", experiment_name="E")
        client = _FakeExpCoreClient(_FakeExpCoreResearch(experiment=e))
        result = get_experiment_proto(client, "exp-1")
        # The proto message itself is returned, not a WithAccess wrapper.
        assert result is e
        assert result.experiment_id == "exp-1"
        assert result.experiment_name == "E"


class TestListExperimentJobs:
    def test_returns_raw_job_protos(self):
        from airavata_sdk.generated.org.apache.airavata.model.job import job_pb2
        from airavata_sdk.helpers.research_resources import list_experiment_jobs
        jobs = [_make_job(job_id="j1"), _make_job(job_id="j2")]
        client = _FakeExpCoreClient(_FakeExpCoreResearch(jobs=jobs))
        result = list_experiment_jobs(client, "exp-1")
        # Proto-direct: each element is the raw JobModel proto (no transform, no
        # sharing wrapper — a job has no ownership concept).
        assert all(isinstance(j, job_pb2.JobModel) for j in result)
        assert [j.job_id for j in result] == ["j1", "j2"]


class TestGetExperimentsInProject:
    def test_returns_with_access_with_per_experiment_sharing(self):
        from airavata_sdk.helpers.research_resources import (
            get_experiments_in_project,
        )
        exps = [_make_experiment(experiment_id="a"),
                _make_experiment(experiment_id="b")]
        # has_access False only for "b": the fake returns a single value, so use
        # a per-id discriminating sharing stub.
        client = _FakeExpCoreClient(_FakeExpCoreResearch(experiments=exps))

        class _PerIdSharing:
            calls = []

            def user_has_access(self, resource_id, user_id, permission_type):
                self.calls.append((resource_id, user_id, permission_type))
                return resource_id == "a"

        client.sharing = _PerIdSharing()
        result = get_experiments_in_project(client, "proj-1")
        assert all(isinstance(r, WithAccess) for r in result)
        by_id = {
            r.message.experiment_id: r.user_has_write_access for r in result}
        assert by_id == {"a": True, "b": False}
        # Each experiment triggers its own chained WRITE lookup.
        assert client.sharing.calls == [
            ("a", "alice", "WRITE"), ("b", "alice", "WRITE")]

    def test_passes_limit_and_offset(self):
        from airavata_sdk.helpers.research_resources import (
            get_experiments_in_project,
        )
        research = _FakeExpCoreResearch(experiments=[])
        client = _FakeExpCoreClient(research)
        get_experiments_in_project(client, "proj-1", limit=5, offset=10)
        assert research.in_project_args == ("proj-1", 5, 10)


class TestCreateExperiment:
    def test_builds_creates_and_returns_with_access(self):
        from airavata_sdk.helpers.research_resources import create_experiment
        # The created experiment is re-fetched via get_experiment, so the fake
        # research returns it from get_experiment(new-id).
        refetched = _make_experiment(
            experiment_id="new-exp-id", experiment_name="New",
            project_id="proj-1")
        research = _FakeExpCoreResearch(experiment=refetched)
        client = _FakeExpCoreClient(research)
        result = create_experiment(
            client,
            {"project_id": "proj-1", "experiment_name": "New",
             "description": "d"})
        assert isinstance(result, WithAccess)
        assert result.message.experiment_id == "new-exp-id"
        # The proto sent to create_experiment carried the forced context.
        assert research.created.project_id == "proj-1"
        assert research.created.gateway_id == "gw1"
        assert research.created.user_name == "alice"
        assert research.created_gateway_id == "gw1"

    def test_user_configuration_data_built_when_present(self):
        from airavata_sdk.helpers.research_resources import create_experiment
        research = _FakeExpCoreResearch(
            experiment=_make_experiment(experiment_id="new-exp-id"))
        client = _FakeExpCoreClient(research)
        create_experiment(client, {
            "project_id": "p", "experiment_name": "E",
            "user_configuration_data": {
                "airavata_auto_schedule": True,
                "computational_resource_scheduling": {
                    "resource_host_id": "h", "total_cpu_count": 4}}})
        ucd = research.created.user_configuration_data
        assert ucd.airavata_auto_schedule is True
        assert ucd.computational_resource_scheduling.total_cpu_count == 4


class TestUpdateExperiment:
    def test_pushes_with_forced_id_and_returns_with_access(self):
        from airavata_sdk.helpers.research_resources import update_experiment
        refetched = _make_experiment(
            experiment_id="exp-9", experiment_name="Updated")
        research = _FakeExpCoreResearch(experiment=refetched)
        client = _FakeExpCoreClient(research)
        result = update_experiment(
            client, "exp-9", {"experiment_name": "Updated"})
        sent_id, sent_pb = research.updated
        assert sent_id == "exp-9"
        assert sent_pb.experiment_id == "exp-9"
        assert sent_pb.experiment_name == "Updated"
        assert isinstance(result, WithAccess)
        assert result.message.experiment_id == "exp-9"


class TestExperimentTypeInt:
    def test_thrift_int_round_trip(self):
        from airavata_sdk.helpers.research_resources import _experiment_type_int
        # Thrift 0 -> proto SINGLE_APPLICATION (1), Thrift 1 -> WORKFLOW (2).
        assert _experiment_type_int(0) == 1
        assert _experiment_type_int(1) == 2

    def test_name_string_accepted(self):
        from airavata_sdk.helpers.research_resources import _experiment_type_int
        assert _experiment_type_int("SINGLE_APPLICATION") == 1
        assert _experiment_type_int("WORKFLOW") == 2

    def test_none_maps_to_zero(self):
        from airavata_sdk.helpers.research_resources import _experiment_type_int
        assert _experiment_type_int(None) == 0
        assert _experiment_type_int("") == 0


# ---------------------------------------------------------------------------
# FullExperiment
# ---------------------------------------------------------------------------

def _make_full_input(**kwargs):
    from airavata_sdk.generated.org.apache.airavata.model.application.io import (
        application_io_pb2 as io,
    )
    return io.InputDataObjectType(**kwargs)


def _make_full_output(**kwargs):
    from airavata_sdk.generated.org.apache.airavata.model.application.io import (
        application_io_pb2 as io,
    )
    return io.OutputDataObjectType(**kwargs)


def _make_full_data_product(**kwargs):
    from airavata_sdk.generated.org.apache.airavata.model.data.replica import (
        replica_catalog_pb2,
    )
    return replica_catalog_pb2.DataProductModel(**kwargs)


def _make_app_interface(**kwargs):
    from airavata_sdk.generated.org.apache.airavata.model.appcatalog.appinterface import (  # noqa: E501
        app_interface_pb2,
    )
    return app_interface_pb2.ApplicationInterfaceDescription(**kwargs)


def _make_compute_resource(**kwargs):
    from airavata_sdk.generated.org.apache.airavata.model.appcatalog.computeresource import (  # noqa: E501
        compute_resource_pb2,
    )
    return compute_resource_pb2.ComputeResourceDescription(**kwargs)


def _dp_write(_dp):
    """Stub per-data-product write resolver (the ViewSet supplies one)."""
    return True


def _output_views(_experiment, _app_interface):
    return {}


class TestCollectDataProductUris:
    def test_single_uri_types_collected(self):
        from airavata_sdk.helpers.research_resources import (
            _collect_data_product_uris,
        )
        from airavata_sdk.generated.org.apache.airavata.model.application.io import (  # noqa: E501
            application_io_pb2 as io,
        )
        outs = [
            _make_full_output(name="o1", value="airavata-dp://1", type=io.DataType.URI),
            _make_full_output(name="o2", value="airavata-dp://2",
                         type=io.DataType.STDOUT),
            # not a dp uri -> skipped
            _make_full_output(name="o3", value="plain.txt", type=io.DataType.URI),
        ]
        assert _collect_data_product_uris(outs) == [
            "airavata-dp://1", "airavata-dp://2"]

    def test_uri_collection_expanded_after_singles(self):
        from airavata_sdk.helpers.research_resources import (
            _collect_data_product_uris,
        )
        from airavata_sdk.generated.org.apache.airavata.model.application.io import (  # noqa: E501
            application_io_pb2 as io,
        )
        items = [
            _make_full_output(name="a", value="airavata-dp://single",
                         type=io.DataType.URI),
            _make_full_output(name="b",
                         value="airavata-dp://x,airavata-dp://y",
                         type=io.DataType.URI_COLLECTION),
        ]
        assert _collect_data_product_uris(items) == [
            "airavata-dp://single", "airavata-dp://x", "airavata-dp://y"]


class _FakeFullResearch:
    def __init__(self, *, experiment, project=None, app_interface=None,
                 app_module=None, data_products=None, jobs=None):
        self._experiment = experiment
        self._project = project
        self._app_interface = app_interface
        self._app_module = app_module
        self._data_products = data_products or {}
        self._jobs = jobs or []

    def get_experiment(self, experiment_id):
        return self._experiment

    def get_data_product(self, uri):
        return self._data_products[uri]

    def get_application_interface(self, app_interface_id):
        if self._app_interface is None:
            raise RuntimeError("no app interface")
        return self._app_interface

    def get_application_module(self, app_module_id):
        return self._app_module

    def get_project(self, project_id):
        return self._project

    def get_job_details(self, experiment_id):
        return self._jobs


class _FakeCompute:
    def __init__(self, compute_resource=None):
        self._compute_resource = compute_resource

    def get_compute_resource(self, compute_resource_id):
        if self._compute_resource is None:
            raise RuntimeError("no compute resource")
        return self._compute_resource


class _FakeFullClient:
    def __init__(self, research, compute=None, gateway_id="gw1",
                 username="alice"):
        self.research = research
        self.compute = compute or _FakeCompute()
        self.gateway_id = gateway_id
        self.username = username
        self.sharing = _FakeSharing(True)


# ---------------------------------------------------------------------------
# FullExperiment — proto-direct composed pydantic shape
# ---------------------------------------------------------------------------
#
# ``get_full_experiment`` returns a :class:`FullExperiment` pydantic model whose
# fields carry the component protos / ``WithAccess`` envelopes WHOLESALE: the
# experiment is ``WithAccess[ExperimentModel]``, the project (when readable) is
# ``WithAccess[Project]``, the application module (when resolvable) is
# ``WithAccess[ApplicationModule]``, the compute resource is the raw
# ``ComputeResourceDescription`` proto, the input/output data products are
# ``WithAccess[DataProductModel]`` and the jobs are raw ``JobModel`` protos.  The
# portal renderer flattens the whole tree; these tests assert on the OBJECTS
# (no dict literals) — the JSON shape is pinned by the portal contract snapshot.


class TestGetFullExperiment:
    def _wire(self, *, with_refs=True):
        from airavata_sdk.generated.org.apache.airavata.model.application.io import (  # noqa: E501
            application_io_pb2 as io,
        )
        from airavata_sdk.generated.org.apache.airavata.model.experiment import (
            experiment_pb2,
        )
        from airavata_sdk.generated.org.apache.airavata.model.scheduling import (
            scheduling_pb2,
        )
        ucd = experiment_pb2.UserConfigurationDataModel(
            computational_resource_scheduling=(
                scheduling_pb2.ComputationalResourceSchedulingModel(
                    resource_host_id="comp-1")))
        e = _make_experiment(
            experiment_id="exp-1", project_id="proj-1", gateway_id="gw1",
            user_name="alice", experiment_name="E", execution_id="iface-1",
            experiment_inputs=[
                _make_full_input(name="in", value="airavata-dp://i",
                            type=io.DataType.URI)],
            experiment_outputs=[
                _make_full_output(name="out", value="airavata-dp://o",
                             type=io.DataType.URI)],
            user_configuration_data=ucd,
        )
        ai = _make_app_interface(
            application_interface_id="iface-1",
            application_modules=["mod-1"]) if with_refs else None
        module = _make_app_module(
            app_module_id="mod-1", app_module_name="mod") if with_refs else None
        compute = _make_compute_resource(
            compute_resource_id="comp-1",
            host_name="hpc") if with_refs else None
        project = _make_project(project_id="proj-1", owner="alice")
        jobs = [_make_job(job_id="j1")]
        data_products = {
            "airavata-dp://i": _make_full_data_product(
                product_uri="airavata-dp://i", owner_name="alice"),
            "airavata-dp://o": _make_full_data_product(
                product_uri="airavata-dp://o", owner_name="bob"),
        }
        research = _FakeFullResearch(
            experiment=e, project=project, app_interface=ai, app_module=module,
            data_products=data_products, jobs=jobs)
        client = _FakeFullClient(research, compute=_FakeCompute(compute))
        return client

    def _call(self, client, **overrides):
        from airavata_sdk.helpers.research_resources import get_full_experiment
        kwargs = dict(
            project_has_read=True,
            module_has_write=True,
            data_product_write_fn=_dp_write,
            output_views_fn=_output_views,
        )
        kwargs.update(overrides)
        return get_full_experiment(client, "exp-1", **kwargs)

    # ------------------------------------------------------------------
    # Composed pydantic shape
    # ------------------------------------------------------------------

    def test_returns_full_experiment_model(self):
        from airavata_sdk.helpers.research_resources import FullExperiment
        fe = self._call(self._wire(with_refs=True))
        assert isinstance(fe, FullExperiment)
        assert fe.experiment_id == "exp-1"

    def test_experiment_is_with_access_carrying_proto(self):
        from airavata_sdk.generated.org.apache.airavata.model.experiment import (
            experiment_pb2,
        )
        fe = self._call(self._wire(with_refs=True))
        assert isinstance(fe.experiment, WithAccess)
        assert isinstance(fe.experiment.message, experiment_pb2.ExperimentModel)
        assert fe.experiment.message.experiment_id == "exp-1"
        # WRITE comes from the chained sharing call (stub returns True).
        assert fe.experiment.user_has_write_access is True

    def test_project_is_with_access_carrying_proto(self):
        from airavata_sdk.generated.org.apache.airavata.model.workspace import (
            workspace_pb2,
        )
        fe = self._call(self._wire(with_refs=True))
        assert isinstance(fe.project, WithAccess)
        assert isinstance(fe.project.message, workspace_pb2.Project)
        assert fe.project.message.project_id == "proj-1"
        # is_owner SDK-trivial: project.owner == client.username ("alice").
        assert fe.project.is_owner is True
        assert fe.project.user_has_write_access is True

    def test_application_module_is_with_access_carrying_proto(self):
        from airavata_sdk.generated.org.apache.airavata.model.appcatalog.appdeployment import (  # noqa: E501
            app_deployment_pb2,
        )
        fe = self._call(self._wire(with_refs=True))
        assert isinstance(fe.application_module, WithAccess)
        assert isinstance(
            fe.application_module.message,
            app_deployment_pb2.ApplicationModule)
        assert fe.application_module.message.app_module_id == "mod-1"
        # gateway-catalog: no ownership, write flag is module_has_write.
        assert fe.application_module.is_owner is False
        assert fe.application_module.user_has_write_access is True

    def test_compute_resource_is_raw_proto(self):
        from airavata_sdk.generated.org.apache.airavata.model.appcatalog.computeresource import (  # noqa: E501
            compute_resource_pb2,
        )
        fe = self._call(self._wire(with_refs=True))
        assert isinstance(
            fe.compute_resource,
            compute_resource_pb2.ComputeResourceDescription)
        assert fe.compute_resource.compute_resource_id == "comp-1"

    def test_data_products_are_with_access_carrying_protos(self):
        from airavata_sdk.generated.org.apache.airavata.model.data.replica import (
            replica_catalog_pb2,
        )
        fe = self._call(self._wire(with_refs=True))
        assert [w.message.product_uri for w in fe.input_data_products] == [
            "airavata-dp://i"]
        assert [w.message.product_uri for w in fe.output_data_products] == [
            "airavata-dp://o"]
        for w in fe.input_data_products + fe.output_data_products:
            assert isinstance(w, WithAccess)
            assert isinstance(w.message, replica_catalog_pb2.DataProductModel)
            # write flag is the resolver result (stub returns True).
            assert w.user_has_write_access is True
        # is_owner SDK-trivial: owner_name == client.username ("alice").
        assert fe.input_data_products[0].is_owner is True   # owner_name="alice"
        assert fe.output_data_products[0].is_owner is False  # owner_name="bob"

    def test_jobs_are_raw_protos(self):
        from airavata_sdk.generated.org.apache.airavata.model.job import job_pb2
        fe = self._call(self._wire(with_refs=True))
        assert [j.job_id for j in fe.job_details] == ["j1"]
        assert all(isinstance(j, job_pb2.JobModel) for j in fe.job_details)

    def test_output_views_passed_through(self):
        def _views(_e, _ai):
            return {"out": ["plugin-a"]}
        fe = self._call(self._wire(with_refs=True), output_views_fn=_views)
        assert fe.output_views == {"out": ["plugin-a"]}

    # ------------------------------------------------------------------
    # Optional references
    # ------------------------------------------------------------------

    def test_project_omitted_without_read(self):
        fe = self._call(self._wire(with_refs=True), project_has_read=False)
        assert fe.project is None

    def test_unresolvable_references_become_none(self):
        # No app interface (raises) and no compute resource (raises) -> both None,
        # swallowed exactly like the old view's broad try/except.
        fe = self._call(self._wire(with_refs=False), module_has_write=False)
        assert fe.application_module is None
        assert fe.compute_resource is None
        # Data products + jobs still resolve.
        assert len(fe.input_data_products) == 1
        assert len(fe.output_data_products) == 1

    def test_data_product_write_resolver_invoked_per_product(self):
        seen = []

        def _write(dp):
            seen.append(dp.product_uri)
            return dp.product_uri.endswith("o")

        fe = self._call(self._wire(with_refs=True), data_product_write_fn=_write)
        # Both products resolved (outputs first, then inputs).
        assert set(seen) == {"airavata-dp://i", "airavata-dp://o"}
        assert fe.input_data_products[0].user_has_write_access is False
        assert fe.output_data_products[0].user_has_write_access is True
