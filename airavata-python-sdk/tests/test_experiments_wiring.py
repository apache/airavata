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

"""Wiring tests for airavata.experiments' local gRPC client (``_apiserver``).

The Airavata Python SDK is now generated-only: ``airavata.experiments`` builds
its own thin client over the raw generated stubs + ``airavata.auth``, replacing
the deleted ``airavata.clients`` facade. These tests pin the wiring without
a live server — the service stubs are constructed off an ``authenticated_channel``
and each operator-facing method routes to the right stub with the right request
message (the same request shapes the old ``APIServerClient`` built, so the
764-line ``AiravataOperator`` call sites are untouched).
"""

from unittest import mock

from airavata.model.data.replica.replica_catalog_pb2 import (
    DataProductModel,
)
from airavata.services import (
    application_catalog_service_pb2,
    data_product_service_pb2,
    experiment_service_pb2,
    experiment_service_pb2_grpc,
    group_resource_profile_service_pb2,
    project_service_pb2,
    resource_service_pb2,
)

from airavata.experiments._apiserver import APIServerClient


def _client():
    # authenticated_channel builds a lazy gRPC channel — no connection needed.
    return APIServerClient(access_token="test-token")


def test_builds_the_seven_service_stubs():
    c = _client()
    for attr in (
        "_experiment",
        "_app_catalog",
        "_resource",
        "_gw_profile",
        "_grp_profile",
        "_project",
        "_data_product",
    ):
        assert getattr(c, attr) is not None, f"stub {attr} not built"
    assert isinstance(
        c._experiment, experiment_service_pb2_grpc.ExperimentServiceStub
    )


def test_get_experiment_routes_to_experiment_stub():
    c = _client()
    c._experiment = mock.Mock()
    c.get_experiment("exp-1")
    c._experiment.GetExperiment.assert_called_once()
    (req,), _ = c._experiment.GetExperiment.call_args
    assert isinstance(req, experiment_service_pb2.GetExperimentRequest)
    assert req.experiment_id == "exp-1"


def test_launch_experiment_passes_gateway_and_id():
    c = _client()
    c._experiment = mock.Mock()
    c.launch_experiment("exp-1", "gw-1")
    (req,), _ = c._experiment.LaunchExperiment.call_args
    assert isinstance(req, experiment_service_pb2.LaunchExperimentRequest)
    assert req.experiment_id == "exp-1"
    assert req.gateway_id == "gw-1"


def test_terminate_experiment_passes_gateway_and_id():
    c = _client()
    c._experiment = mock.Mock()
    c.terminate_experiment("exp-1", "gw-1")
    (req,), _ = c._experiment.TerminateExperiment.call_args
    assert req.experiment_id == "exp-1"
    assert req.gateway_id == "gw-1"


def test_register_data_product_routes_to_data_product_stub():
    c = _client()
    c._data_product = mock.Mock()
    c.register_data_product(DataProductModel(product_name="f.txt"))
    (req,), _ = c._data_product.RegisterDataProduct.call_args
    assert isinstance(req, data_product_service_pb2.RegisterDataProductRequest)
    assert req.data_product.product_name == "f.txt"


def test_get_user_projects_carries_defaults():
    c = _client()
    c._project = mock.Mock()
    c.get_user_projects("gw-1", "alice")
    (req,), _ = c._project.GetUserProjects.call_args
    assert isinstance(req, project_service_pb2.GetUserProjectsRequest)
    assert req.gateway_id == "gw-1"
    assert req.user_name == "alice"
    assert req.limit == -1


def test_resource_and_catalog_and_group_reads_route_correctly():
    c = _client()
    c._resource = mock.Mock()
    c._app_catalog = mock.Mock()
    c._grp_profile = mock.Mock()

    c.get_all_compute_resource_names()
    (req,), _ = c._resource.GetAllComputeResourceNames.call_args
    assert isinstance(req, resource_service_pb2.GetAllComputeResourceNamesRequest)

    c.get_application_inputs("iface-1")
    (req,), _ = c._app_catalog.GetApplicationInputs.call_args
    assert isinstance(req, application_catalog_service_pb2.GetApplicationInputsRequest)
    assert req.app_interface_id == "iface-1"

    c.get_group_resource_profile("grp-1")
    (req,), _ = c._grp_profile.GetGroupResourceProfile.call_args
    assert isinstance(
        req, group_resource_profile_service_pb2.GetGroupResourceProfileRequest
    )
    assert req.group_resource_profile_id == "grp-1"
