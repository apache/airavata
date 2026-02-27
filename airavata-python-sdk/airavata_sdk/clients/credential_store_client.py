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


class CredentialStoreClient(object):
    """Client for credential store operations via the unified REST API."""

    def __init__(self, access_token: str, base_url: Optional[str] = None):
        self.settings = Settings()
        self.client = RestClient(
            access_token=access_token,
            base_url=base_url or self.settings.API_SERVER_URL,
        )

    def get_SSH_credential(self, token: str, gateway_id: str = None) -> dict:
        return self.client.get(f"/credentials/ssh/{token}", params={"gatewayId": gateway_id})

    def get_password_credential(self, token: str, gateway_id: str = None) -> dict:
        return self.client.get(f"/credentials/password/{token}", params={"gatewayId": gateway_id})

    def add_ssh_credential(self, credential: dict) -> str:
        return self.client.post("/credentials/ssh", json=credential)

    def add_password_credential(self, credential: dict) -> str:
        return self.client.post("/credentials/password", json=credential)

    def delete_credential(self, token: str, gateway_id: str = None) -> None:
        self.client.delete(f"/credentials/{token}", params={"gatewayId": gateway_id})

    def get_all_credential_summaries(self, gateway_id: str = None, credential_type: str = None) -> list[dict]:
        params = {}
        if gateway_id:
            params["gatewayId"] = gateway_id
        if credential_type:
            params["type"] = credential_type
        return self.client.get("/credential-summaries", params=params)
