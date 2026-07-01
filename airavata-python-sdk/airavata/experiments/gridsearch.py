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

"""High-level gridsearch helper for the ExperimentSetService.

``gridsearch()`` builds a ``CreateExperimentSetRequest`` from a base experiment
spec + a parameter sweep grid and submits it via the ``AiravataOperator``'s
``api_server_client``. It is deliberately a standalone function rather than a
method on ``Experiment`` / ``Plan`` so it does not touch the existing
``/api/v1/plan`` REST path, which remains unchanged.
"""

from __future__ import annotations

from airavata.services import (
    experiment_service_pb2 as exp_pb2,
    experiment_set_service_pb2 as set_pb2,
)


def gridsearch(
    operator,
    set_name: str,
    project_id: str,
    application_interface_id: str,
    inputs: dict[str, str],
    resource: dict,
    sweep_axes: dict[str, list[str]],
    description: str = "",
) -> set_pb2.ExperimentSet:
    """Submit a gridsearch experiment set to the Airavata server.

    Args:
        operator: An ``AiravataOperator`` instance (provides ``api_server_client``).
        set_name: Human-readable name for the experiment set.
        project_id: Airavata project ID for the base experiment.
        application_interface_id: App-interface ID; inputs are keyed by input name.
        inputs: Base input values (``{input_name: string_value}``). The server
            expands sweep axes over these, so omit swept keys or supply the
            default value — the sweep axis values take precedence per experiment.
        resource: Mapping with compute resource fields:
            ``compute_resource_id``, ``group_resource_profile_id``,
            ``node_count``, ``total_cpu_count``, ``queue_name``,
            ``wall_time_limit`` (and optionally ``input_storage_resource_id``,
            ``output_storage_resource_id``).
        sweep_axes: ``{input_name: [val1, val2, ...]}`` — the Cartesian product
            is expanded by the server.
        description: Optional experiment description.

    Returns:
        The ``ExperimentSet`` proto returned by the server (contains the new
        ``experiment_set_id`` and the list of spawned experiment IDs).
    """
    resource_spec = exp_pb2.ResourceSpec(
        compute_resource_id=resource.get("compute_resource_id", ""),
        group_resource_profile_id=resource.get("group_resource_profile_id", ""),
        node_count=resource.get("node_count", 1),
        total_cpu_count=resource.get("total_cpu_count", 1),
        queue_name=resource.get("queue_name", ""),
        wall_time_limit=resource.get("wall_time_limit", 0),
        input_storage_resource_id=resource.get("input_storage_resource_id", ""),
        output_storage_resource_id=resource.get("output_storage_resource_id", ""),
    )

    base_spec = exp_pb2.ExperimentSpec(
        experiment_name=set_name,
        project_id=project_id,
        application_interface_id=application_interface_id,
        description=description,
        inputs=inputs,
        resource=resource_spec,
    )

    sweep_spec = set_pb2.SweepSpec(
        base=base_spec,
        sweep_axes={
            k: set_pb2.ValueList(values=v)
            for k, v in sweep_axes.items()
        },
        name_prefix=set_name,
    )

    req = set_pb2.CreateExperimentSetRequest(
        set_name=set_name,
        sweep=sweep_spec,
    )

    return operator.api_server_client.create_experiment_set(req)
