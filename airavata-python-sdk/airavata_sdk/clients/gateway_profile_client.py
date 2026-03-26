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

import logging
from typing import Optional

from airavata_sdk import Settings
from airavata_sdk.transport.utils import RestClient

logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)


class GatewayProfileClient(object):
    """Client for gateway profile operations via the unified REST API."""

    def __init__(self, access_token: str, base_url: Optional[str] = None):
        self.settings = Settings()
        self.client = RestClient(
            access_token=access_token,
            base_url=base_url or self.settings.API_SERVER_URL,
        )

    def add_gateway(self, gateway: dict) -> str:
        return self.client.post("/gateways", json=gateway)

    def update_gateway(self, gateway_id: str, gateway: dict) -> dict:
        return self.client.put(f"/gateways/{gateway_id}", json=gateway)

    def get_gateway(self, gateway_id: str) -> dict:
        return self.client.get(f"/gateways/{gateway_id}")

    def delete_gateway(self, gateway_id: str) -> None:
        self.client.delete(f"/gateways/{gateway_id}")

    def get_all_gateways(self) -> list[dict]:
        return self.client.get("/gateways")

    def is_gateway_exist(self, gateway_id: str) -> bool:
        try:
            self.client.get(f"/gateways/{gateway_id}")
            return True
        except Exception:
            return False

    def get_all_gateways_for_user(self, user_id: str) -> list[dict]:
        return self.client.get("/gateways")
