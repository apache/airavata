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

import json
import logging
from typing import Optional

import grpc

from airavata_sdk import Settings
from airavata_sdk.transport.utils import create_experiment_management_service_stub

logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)


class ExperimentManagementClient:
    """Client for the Experiment Management Service (gRPC).

    Provides methods for launching, terminating, and querying experiments.
    """

    def __init__(self, access_token: Optional[str] = None, claims: Optional[dict] = None):
        self.settings = Settings()
        host = self.settings.API_SERVER_HOSTNAME
        port = self.settings.API_SERVER_PORT
        secure = self.settings.API_SERVER_SECURE

        target = f"{host}:{port}"
        if secure:
            self.channel = grpc.secure_channel(target, grpc.ssl_channel_credentials())
        else:
            self.channel = grpc.insecure_channel(target)

        self._metadata: list[tuple[str, str]] = []
        if access_token:
            self._metadata.append(("authorization", f"Bearer {access_token}"))
        if claims:
            self._metadata.append(("x-claims", json.dumps(claims)))

        self._stub = create_experiment_management_service_stub(self.channel)

    def close(self):
        self.channel.close()

    def get_experiment(self, experiment_id: str):
        from airavata_sdk.generated.services import experiment_management_service_pb2 as pb2
        return self._stub.GetExperiment(
            pb2.GetAgentExperimentRequest(experiment_id=experiment_id),
            metadata=self._metadata,
        )

    def launch_experiment(
        self, experiment_name: str, project_name: str, remote_cluster: str,
        group: str = "", libraries: list[str] = None, pip: list[str] = None,
        mounts: list[str] = None, queue: str = "", wall_time: int = 0,
        cpu_count: int = 0, node_count: int = 0, memory: int = 0,
        input_storage_id: str = "", output_storage_id: str = "",
    ):
        from airavata_sdk.generated.services import experiment_management_service_pb2 as pb2
        return self._stub.LaunchExperiment(
            pb2.AgentLaunchRequest(
                experiment_name=experiment_name, project_name=project_name,
                remote_cluster=remote_cluster, group=group,
                libraries=libraries or [], pip=pip or [],
                mounts=mounts or [], queue=queue, wall_time=wall_time,
                cpu_count=cpu_count, node_count=node_count, memory=memory,
                input_storage_id=input_storage_id, output_storage_id=output_storage_id,
            ),
            metadata=self._metadata,
        )

    def launch_optimized_experiment(self, requests: list):
        from airavata_sdk.generated.services import experiment_management_service_pb2 as pb2
        return self._stub.LaunchOptimizedExperiment(
            pb2.LaunchOptimizedExperimentRequest(requests=requests),
            metadata=self._metadata,
        )

    def terminate_experiment(self, experiment_id: str):
        from airavata_sdk.generated.services import experiment_management_service_pb2 as pb2
        return self._stub.TerminateExperiment(
            pb2.TerminateAgentExperimentRequest(experiment_id=experiment_id),
            metadata=self._metadata,
        )

    def get_process_model(self, experiment_id: str):
        from airavata_sdk.generated.services import experiment_management_service_pb2 as pb2
        return self._stub.GetProcessModel(
            pb2.GetProcessModelRequest(experiment_id=experiment_id),
            metadata=self._metadata,
        )
