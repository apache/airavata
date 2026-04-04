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
from airavata_sdk.transport.utils import create_gateway_service_stub

logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)


class TenantProfileClient:
    """Client for tenant/gateway profile operations (gRPC).

    Replaces the old Thrift TenantProfileService client.
    Delegates to the GatewayService gRPC stub, which now covers
    gateway CRUD that was previously in TenantProfileService.
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

        self._stub = create_gateway_service_stub(self.channel)

    def close(self):
        self.channel.close()

    def add_gateway(self, gateway):
        from airavata_sdk.generated.services import gateway_service_pb2 as pb2
        return self._stub.AddGateway(
            pb2.AddGatewayRequest(gateway=gateway),
            metadata=self._metadata,
        )

    def update_gateway(self, gateway_id, gateway):
        from airavata_sdk.generated.services import gateway_service_pb2 as pb2
        return self._stub.UpdateGateway(
            pb2.UpdateGatewayRequest(gateway_id=gateway_id, gateway=gateway),
            metadata=self._metadata,
        )

    def get_gateway(self, gateway_id):
        from airavata_sdk.generated.services import gateway_service_pb2 as pb2
        return self._stub.GetGateway(
            pb2.GetGatewayRequest(gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def delete_gateway(self, gateway_id):
        from airavata_sdk.generated.services import gateway_service_pb2 as pb2
        return self._stub.DeleteGateway(
            pb2.DeleteGatewayRequest(gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def get_all_gateways(self):
        from airavata_sdk.generated.services import gateway_service_pb2 as pb2
        return self._stub.GetAllGateways(
            pb2.GetAllGatewaysRequest(),
            metadata=self._metadata,
        )

    def is_gateway_exist(self, gateway_id):
        from airavata_sdk.generated.services import gateway_service_pb2 as pb2
        return self._stub.IsGatewayExist(
            pb2.IsGatewayExistRequest(gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def get_all_gateways_for_user(self, *args, **kwargs):
        """Not available in gRPC GatewayService."""
        raise NotImplementedError("getAllGatewaysForUser not available in gRPC GatewayService")
