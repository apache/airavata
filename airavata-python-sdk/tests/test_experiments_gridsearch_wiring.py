#  Licensed to the Apache Software Foundation (ASF) under one or more
#  contributor license agreements.  See the NOTICE file distributed with
#  this work for additional information regarding copyright ownership.
#  The ASF licenses this file to You under the Apache License, Version 2.0
#  (the "License"); you may not use this file except in compliance with
#  the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

"""Wiring tests for the ExperimentSetService gRPC client methods in ``_apiserver``.

These tests assert that:
  - the ``_experiment_set`` stub is built from the same authenticated_channel
  - ``create_experiment_set`` routes to ``ExperimentSetServiceStub.CreateExperimentSet``
    with the prebuilt ``CreateExperimentSetRequest`` passed through unchanged
  - ``launch_experiment_set`` builds a ``LaunchExperimentSetRequest`` with the right fields
  - ``get_experiment_set`` builds a ``GetExperimentSetRequest`` with the right set_id
  - ``get_experiment_set_status`` builds a ``GetExperimentSetRequest`` (same shape) and
    routes to ``GetExperimentSetStatus``
  - ``list_experiment_sets`` builds a ``ListExperimentSetsRequest`` with limit/offset

No live server is needed; all stubs are mocked.
"""

from unittest import mock

from airavata.services import (
    experiment_service_pb2,
    experiment_set_service_pb2,
    experiment_set_service_pb2_grpc,
)

from airavata.experiments._apiserver import APIServerClient


def _client():
    return APIServerClient(access_token="test-token")


# ----------------------------------------------------------------- stub wiring

def test_experiment_set_stub_is_built():
    c = _client()
    assert hasattr(c, "_experiment_set"), "_experiment_set stub not built"
    assert isinstance(c._experiment_set, experiment_set_service_pb2_grpc.ExperimentSetServiceStub)


# --------------------------------------------------- create_experiment_set

def test_create_experiment_set_routes_to_stub():
    c = _client()
    c._experiment_set = mock.Mock()

    req = experiment_set_service_pb2.CreateExperimentSetRequest(
        set_name="my-sweep",
        sweep=experiment_set_service_pb2.SweepSpec(
            base=experiment_service_pb2.ExperimentSpec(
                experiment_name="sweep-base",
                project_id="proj-1",
                application_interface_id="iface-1",
                inputs={"param_a": "1.0"},
                resource=experiment_service_pb2.ResourceSpec(
                    compute_resource_id="cr-1",
                    group_resource_profile_id="grp-1",
                    node_count=1,
                    total_cpu_count=4,
                    queue_name="normal",
                    wall_time_limit=60,
                ),
            ),
            sweep_axes={"param_a": experiment_set_service_pb2.ValueList(values=["1.0", "2.0", "3.0"])},
            name_prefix="sweep-base",
        ),
    )

    c.create_experiment_set(req)

    c._experiment_set.CreateExperimentSet.assert_called_once()
    (passed_req,), _ = c._experiment_set.CreateExperimentSet.call_args
    # the wrapper passes the request through unchanged
    assert passed_req is req
    assert passed_req.set_name == "my-sweep"
    assert passed_req.sweep.sweep_axes["param_a"].values[:] == ["1.0", "2.0", "3.0"]
    assert passed_req.sweep.base.inputs["param_a"] == "1.0"
    assert passed_req.sweep.base.resource.compute_resource_id == "cr-1"


# --------------------------------------------------- launch_experiment_set

def test_launch_experiment_set_builds_request():
    c = _client()
    c._experiment_set = mock.Mock()

    c.launch_experiment_set("set-42", notification_email="user@example.com")

    c._experiment_set.LaunchExperimentSet.assert_called_once()
    (req,), _ = c._experiment_set.LaunchExperimentSet.call_args
    assert isinstance(req, experiment_set_service_pb2.LaunchExperimentSetRequest)
    assert req.experiment_set_id == "set-42"
    assert req.notification_email == "user@example.com"


def test_launch_experiment_set_default_email():
    c = _client()
    c._experiment_set = mock.Mock()

    c.launch_experiment_set("set-99")

    (req,), _ = c._experiment_set.LaunchExperimentSet.call_args
    assert req.experiment_set_id == "set-99"
    assert req.notification_email == ""


# --------------------------------------------------- get_experiment_set

def test_get_experiment_set_builds_request():
    c = _client()
    c._experiment_set = mock.Mock()

    c.get_experiment_set("set-7")

    c._experiment_set.GetExperimentSet.assert_called_once()
    (req,), _ = c._experiment_set.GetExperimentSet.call_args
    assert isinstance(req, experiment_set_service_pb2.GetExperimentSetRequest)
    assert req.experiment_set_id == "set-7"


# --------------------------------------------------- get_experiment_set_status

def test_get_experiment_set_status_builds_get_request():
    c = _client()
    c._experiment_set = mock.Mock()

    c.get_experiment_set_status("set-7")

    c._experiment_set.GetExperimentSetStatus.assert_called_once()
    (req,), _ = c._experiment_set.GetExperimentSetStatus.call_args
    assert isinstance(req, experiment_set_service_pb2.GetExperimentSetRequest)
    assert req.experiment_set_id == "set-7"


# --------------------------------------------------- list_experiment_sets

def test_list_experiment_sets_default_pagination():
    c = _client()
    c._experiment_set = mock.Mock()

    c.list_experiment_sets()

    c._experiment_set.ListExperimentSets.assert_called_once()
    (req,), _ = c._experiment_set.ListExperimentSets.call_args
    assert isinstance(req, experiment_set_service_pb2.ListExperimentSetsRequest)
    assert req.limit == 0
    assert req.offset == 0


def test_list_experiment_sets_custom_pagination():
    c = _client()
    c._experiment_set = mock.Mock()

    c.list_experiment_sets(limit=10, offset=20)

    (req,), _ = c._experiment_set.ListExperimentSets.call_args
    assert req.limit == 10
    assert req.offset == 20


# --------------------------------------------------- high-level gridsearch helper

def test_gridsearch_builds_create_request_and_calls_api():
    """gridsearch() builds a CreateExperimentSetRequest with correct sweep axes and base spec,
    calls create_experiment_set on the api_server_client, and returns the ExperimentSet."""
    from airavata.experiments.gridsearch import gridsearch
    from unittest.mock import MagicMock, patch

    fake_set = experiment_set_service_pb2.ExperimentSet(
        experiment_set_id="set-new",
        set_name="lr-sweep",
    )

    mock_client = MagicMock()
    mock_client.create_experiment_set.return_value = fake_set

    mock_operator = MagicMock()
    mock_operator.api_server_client = mock_client

    result = gridsearch(
        operator=mock_operator,
        set_name="lr-sweep",
        project_id="proj-1",
        application_interface_id="iface-1",
        inputs={"lr": "0.01", "epochs": "10"},
        resource=dict(
            compute_resource_id="cr-1",
            group_resource_profile_id="grp-1",
            node_count=1,
            total_cpu_count=4,
            queue_name="normal",
            wall_time_limit=60,
        ),
        sweep_axes={"lr": ["0.01", "0.001", "0.0001"]},
    )

    assert result is fake_set
    mock_client.create_experiment_set.assert_called_once()
    (req,), _ = mock_client.create_experiment_set.call_args
    assert isinstance(req, experiment_set_service_pb2.CreateExperimentSetRequest)
    assert req.set_name == "lr-sweep"
    assert req.sweep.base.project_id == "proj-1"
    assert req.sweep.base.application_interface_id == "iface-1"
    assert req.sweep.base.inputs["lr"] == "0.01"
    assert req.sweep.base.inputs["epochs"] == "10"
    assert req.sweep.base.resource.compute_resource_id == "cr-1"
    assert req.sweep.base.resource.group_resource_profile_id == "grp-1"
    assert req.sweep.base.resource.node_count == 1
    assert req.sweep.base.resource.total_cpu_count == 4
    assert req.sweep.base.resource.queue_name == "normal"
    assert req.sweep.base.resource.wall_time_limit == 60
    axes = req.sweep.sweep_axes
    assert list(axes["lr"].values) == ["0.01", "0.001", "0.0001"]
    assert req.sweep.name_prefix == "lr-sweep"
