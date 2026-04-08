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
from google.protobuf.struct_pb2 import Struct

from airavata_sdk import Settings
from airavata_sdk.transport.utils import create_plan_service_stub

logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)


class PlanClient:
    """Client for the Plan Service (gRPC).

    Provides methods for saving, retrieving, and updating plans.
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

        self._stub = create_plan_service_stub(self.channel)

    def close(self):
        self.channel.close()

    def save_plan(self, id: str, data: dict):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        struct = Struct()
        struct.update(data)
        return self._stub.SavePlan(
            pb2.SavePlanRequest(id=id, data=struct),
            metadata=self._metadata,
        )

    def get_plan(self, plan_id: str):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._stub.GetPlan(
            pb2.GetPlanRequest(plan_id=plan_id),
            metadata=self._metadata,
        )

    def get_plans_by_user(self):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._stub.GetPlansByUser(
            pb2.GetPlansByUserRequest(),
            metadata=self._metadata,
        )

    def update_plan(self, plan_id: str, data: dict):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        struct = Struct()
        struct.update(data)
        return self._stub.UpdatePlan(
            pb2.UpdatePlanRequest(plan_id=plan_id, data=struct),
            metadata=self._metadata,
        )
